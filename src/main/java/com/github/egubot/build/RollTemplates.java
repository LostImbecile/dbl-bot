package com.github.egubot.build;

import java.io.IOException;
import java.util.List;

import org.javacord.api.entity.message.Messageable;

import com.github.egubot.objects.legends.Tags;
import com.github.egubot.storage.DataManagerHandler;

public class RollTemplates extends DataManagerHandler {
	private static String idKey = "Templates_Message_ID";
	private static String resourcePath = "Filter_Templates.txt";

	public RollTemplates() throws IOException {
		super(idKey, resourcePath, "Filter Templates", true);
	}

	public RollTemplates(long serverID) throws IOException {
		super(idKey, resourcePath, "Filter Templates", serverID, false);
	}

	public void removeTemplate(String msgText, Messageable e, boolean isOwner) {

		try {
			int startIndex = getLockedDataEndIndex();

			if (isOwner)
				startIndex = 0;

			String st = msgText.toLowerCase();

			try {
				st = st.substring(0, st.indexOf(" "));
			} catch (Exception e1) {

			}

			if (st.isBlank())
				throw new Exception();

			boolean isNameExist = false;

			for (int j = startIndex; j < getData().size(); j++) {
				if (isTemplateEqual(getData().get(j), st)) {
					isNameExist = true;
					getData().remove(j);
					break;
				}
			}

			if (isNameExist) {
				writeData(e);
			} else {
				e.sendMessage("\"" + st + "\" is not a template <a:L:1155894861644435516>");
			}
		} catch (Exception e1) {
			e.sendMessage("Remove what?");
		}
	}

	public void writeTemplate(String msgText, Messageable e) {
		try {
			String newTemplate = msgText.replaceFirst(" ", " (") + ")\n";

			String temp;
			do {
				temp = msgText;
				for (String element : getData()) {
					msgText = msgText.replace(getTemplateName(element).toLowerCase(),
							(getTemplateBody(element).toLowerCase()));
				}
			} while (!temp.equals(msgText));

			msgText = msgText.replaceAll("[-()&|+\n\t]", " ").trim().replaceAll("\\s+", " ");

			String[] tokens = msgText.split(" ");
			boolean isTag = false;
			boolean isNameExist = false;

			for (String element : getData()) {
				if (isTemplateEqual(element, newTemplate)) {
					isNameExist = true;
					break;
				}
			}

			if (isNameExist) {
				e.sendMessage("\"" + tokens[0] + "\" already exists <:joea:1144008494568194099>");
				return;
			}

			for (int i = 1; i < tokens.length; i++) {

				for (Tags element : LegendsDatabase.getTags()) {
					if (tokens[i].equalsIgnoreCase(element.getName())) {
						isTag = true;
					}
				}
				if (!isTag) {
					e.sendMessage("\"" + tokens[i] + "\"?? not a tag <:joeh:1144008503355265075>");
					break;
				}
			}

			if (isTag) {
				getData().add(newTemplate);
				writeData(e);
			}
		} catch (Exception e1) {
			e.sendMessage("Correct format:\nb-template create name filters");
		}
	}

	public static String getTemplateName(String st) {
		try {
			st = st.substring(0, st.indexOf(" "));
		} catch (Exception e) {
		}
		return st;
	}

	public static String getTemplateBody(String st) {
		try {
			st = st.substring(st.indexOf(" ") + 1);
		} catch (Exception e) {
		}

		return st;
	}

	static boolean isTemplateEqual(String st, String st2) {

		st = getTemplateName(st).toLowerCase();
		st2 = getTemplateName(st2).toLowerCase();

		return st.matches(st2 + "(?s).*") || st2.matches(st + "(?s).*");
	}

	public List<String> getRollTemplates() {
		return getData();
	}

}
