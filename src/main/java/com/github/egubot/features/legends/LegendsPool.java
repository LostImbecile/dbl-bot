package com.github.egubot.features.legends;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.build.RollTemplates;
import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.objects.legends.CharacterHash;
import com.github.egubot.objects.legends.Characters;
import com.github.egubot.objects.legends.Tags;

public abstract class LegendsPool {
	private List<String> rollTemplates;

	protected LegendsPool(List<String> rollTemplates) {
		this.rollTemplates = rollTemplates;
	}

	protected abstract List<Characters> getPool(String msgText);

	protected Set<Characters> analyseAndCreatePool(String input) {
		try {
			input = preprocess(input);

			String[] filters = input.split(" ");
			LinkedList<String> opStack = new LinkedList<>();
			LinkedList<String> conStack = new LinkedList<>();
			LinkedList<Set<Characters>> pools = new LinkedList<>();

			// Turn to postfix from infix (5 + 3 to 3 5 + or so)
			turnToPostfix(filters, opStack, conStack);

			filters = conStack.get(0).split(" ");
			conStack.clear();

			// Evaluate the -now- postfix filters
			evaluatePostfix(filters, conStack, pools);

			return pools.get(0);
		} catch (Exception e) {
			return null;
		}
	}

	private void evaluatePostfix(String[] filters, List<String> operands, List<Set<Characters>> pool) {
		LinkedList<String> operandStack = (LinkedList<String>) operands;
		LinkedList<Set<Characters>> pools = (LinkedList<Set<Characters>>) pool;
		String[] subPoolFilter;
		boolean popTwo = true;

		/*
		 * Too long to explain, but what this does is, pop an element, if it's
		 * an operand push it to the operand stack, if it's an operator, pop
		 * two elements from the operand stack, and perform the operation on them,
		 * then push the result back.
		 * 
		 * The result here is represented by "-1" as it's a pool, when there
		 * are two -1 in a row, the operation is performed between the two pools.
		 * 
		 * If there's only one -1 and one normal operand, the pool is sent
		 * and a new one is created from the normal operand, then the operation
		 * is performed on them.
		 * 
		 * Due to how getSubPool() works, it's possible to make this quicker
		 * by sending more tags when the same is operation is performed in
		 * succession, for instance:
		 * tag1 tag2 tag3 tag4 tag5 + + + +
		 * 
		 * Could just have all 5 tags sent at once in the filter.
		 * 
		 * This can be done by checking how many operators match the current
		 * one, and creating a filter of their number minus the position
		 * of the first -1 in the operand stack or so.
		 */

		for (String filter : filters) {
			if (isOperator(filter)) {
				if (popTwo) {
					subPoolFilter = new String[2];
					subPoolFilter[0] = pop(operandStack);
					subPoolFilter[1] = pop(operandStack);
					pools.push(getSubPool(subPoolFilter, new CharacterHash(), null, filter));
					operandStack.push("-1");
				} else {
					if (operandStack.size() >= 2
							&& (!operandStack.get(0).equals("-1") || !operandStack.get(1).equals("-1"))) {
						subPoolFilter = new String[1];

						// Order matters for "-"
						if (operandStack.get(0).equals("-1")) {
							pop(operandStack);
							subPoolFilter[0] = pop(operandStack);
							operandStack.push("-1");
							pools.push(getSubPool(subPoolFilter, pools.get(0), null, filter));
						} else {
							subPoolFilter[0] = pop(operandStack);
							pools.push(getSubPool(subPoolFilter, null, pools.get(0), filter));
						}

						pools.remove(1);

					} else if (pools.size() >= 2) {

						pools.push(getSubPool(null, pools.get(0), pools.get(1), filter));
						pools.remove(1);
						pools.remove(1);

						// Pop two "-1" and push another
						pop(operandStack);
					}
				}

				popTwo = false;

			} else {
				operandStack.push(filter);
				if (operandStack.size() >= 2 && !operandStack.get(0).equals("-1") && !operandStack.get(1).equals("-1"))
					popTwo = true;
				else
					popTwo = false;
			}
		}

		// In the case of there being only one tag (no equation/subpools)
		if (!operandStack.isEmpty() && pools.isEmpty()) {
			subPoolFilter = new String[1];
			subPoolFilter[0] = pop(operandStack);
			pools.push(getSubPool(subPoolFilter, new CharacterHash(), null, "&"));
		}
	}

	private void turnToPostfix(String[] filters, List<String> operators, List<String> operands) {
		LinkedList<String> opStack = (LinkedList<String>) operators;
		LinkedList<String> conStack = (LinkedList<String>) operands;

		String temp;
		// This is done the exact same way it is for any infix
		// to postfix conversion, so read the details online.
		for (String filter : filters) {

			if (isOperator(filter)) {

				if (isHigherPriority(opStack, filter)) {
					opStack.push(filter);
				} else {
					while (!isHigherPriority(opStack, filter)) {
						temp = pop(conStack) + " " + pop(conStack) + " " + pop(opStack);
						conStack.push(temp);
					}
					opStack.push(filter);
				}
			} else if (isOpenBracket(filter)) {
				opStack.push(filter);
			} else if (isClosedBracket(filter)) {
				while (!isOpenBracket(opStack.get(0))) {
					temp = pop(conStack) + " " + pop(conStack) + " " + pop(opStack);
					conStack.push(temp);
				}
				pop(opStack);
			} else {
				conStack.push(filter);
			}

		}
		while (!opStack.isEmpty()) {
			temp = pop(conStack) + " " + pop(conStack) + " " + pop(opStack);
			conStack.push(temp);
		}
	}

	private String preprocess(String st) {
		/*
		 * This replaces all templates, adds spaces and operands,
		 * removes extra spaces, and replaces all brackets with
		 * the regular ones.
		 * 
		 * Some methods might have been used unnecessarily so
		 * it's best to remove them if needed. trim() mainly.
		 */
		st = st.toLowerCase().trim();
		String temp;

		// Replace templates till everything is a tag
		do {
			temp = st;
			for (int i = 0; i < rollTemplates.size(); i++) {
				st = replaceTemplates(st, i);
			}
		} while (!temp.equals(st));

		// Replace all brackets and operators and add spaces for them, as well as remove
		// extra spaces
		st = st.replaceAll("[\\[({<]", " ( ").replaceAll("[\\])}>]", " ) ").replace("+", " + ").replace("-", " - ")
				.trim().replaceAll("\\s+", " ");

		// This adds an "&" at the right positions between operands
		// till there's an operator between all of them
		do {
			temp = st;
			st = st.replaceAll("(\\w+|\\))(\\s)(\\w+|\\()", "$1 & $3").trim();
		} while (!temp.equals(st));

		st = st.replace("frieza_no_brother", "cooler");

		return st;
	}

	private String replaceTemplates(String st, int i) {
		String name = RollTemplates.getTemplateName(rollTemplates.get(i)).toLowerCase();
		String body = RollTemplates.getTemplateBody(rollTemplates.get(i)).toLowerCase();

		// (\\b) is a word boundary, so it's only going to replace the
		// name if it isn't connected to an underscore, letter or number
		return st.replaceAll("\\b" + name + "\\b", body);
	}

	private String pop(List<String> list) {

		try {
			return ((LinkedList<String>) list).pop();
		} catch (Exception e) {
			return "";
		}
	}

	private boolean isClosedBracket(String st) {
		return st.equals(")");
	}

	private boolean isOpenBracket(String st) {
		return st.equals("(");
	}

	private boolean isHigherPriority(List<String> opStack, String st) {
		if (opStack.isEmpty())
			return true;
		String lastOp = opStack.get(0);

		return getPriority(lastOp) < getPriority(st);
	}

	private int getPriority(String st) {
		if (st.equals("&"))
			return 2;
		if (st.equals("+") || st.equals("-"))
			return 1;

		return 0;
	}

	private boolean isOperator(String st) {

		return st.equals("+") || st.equals("-") || st.equals("&");

	}

	private Set<Characters> getSubPool(String[] subPoolFilter, Set<Characters> subPool1, Set<Characters> subPool2,
			String operation) {

		// Either combines the pools if two are sent, or
		// creates a new one from the filters
		Tags tag;

		if (subPool1 != null && subPool2 != null) {
			combineSubPools(subPool1, subPool2, operation);

		} else if (subPoolFilter != null) {
			for (String tagCondition : subPoolFilter) {
				for (int i = 0; i < LegendsDatabase.getTags().size(); i++) {
					tag = LegendsDatabase.getTags().get(i);

					if (tag.getName().equalsIgnoreCase(tagCondition)) {
						// Initialise subPool if it's empty
						if (subPool1 != null) {
							if (subPool1.isEmpty()) {

								for (int k = 0; k < tag.getCharacters().size(); k++) {
									if (tag.getCharacters().get(k) != null)
										((CharacterHash) subPool1).put(tag.getCharacters().get(k));
								}
							} else {
								combineSubPools(subPool1, tag.getCharacters(), operation);
							}
						} else {
							if (subPool2.isEmpty()) {
								for (int k = 0; k < tag.getCharacters().size(); k++) {
									if (tag.getCharacters().get(k) != null)
										((CharacterHash) subPool2).put(tag.getCharacters().get(k));
								}
							} else {
								subPool1 = tag.getCharacters().clone();
								combineSubPools(subPool1, subPool2, operation);
							}
						}
					}
				}

			}
		}

		return subPool1;
	}

	static void combineSubPools(Set<Characters> subPool1, Set<Characters> subPool2, String operation) {
		int siteID;
		int initialSize;

		CharacterHash pool1, pool2;
		pool1 = (CharacterHash) subPool1;
		pool2 = (CharacterHash) subPool2;

		if (operation.equals("&")) {
			initialSize = pool1.size();
			for (int i = 0; i < initialSize; i++) {
				if (pool1.get(i) == null)
					continue;

				siteID = pool1.get(i).getSiteID();

				if (pool2.get(siteID) == null) {
					pool1.remove(pool1.get(siteID));
				}

			}
		} else if (operation.equals("+")) {
			initialSize = pool2.size();
			for (int i = 0; i < initialSize; i++) {
				pool1.put(pool2.get(i));
			}
		} else if (operation.equals("-")) {
			initialSize = pool1.size();
			for (int i = 0; i < initialSize; i++) {
				if (pool1.get(i) == null)
					continue;

				siteID = pool1.get(i).getSiteID();

				pool1.remove(pool2.get(siteID));
			}
		} else {
			StreamRedirector.println("events", "undefined operation at legends reroll: " + operation);
		}

	}
}
