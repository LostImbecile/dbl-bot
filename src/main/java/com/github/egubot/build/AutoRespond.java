package com.github.egubot.build;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.event.message.MessageCreateEvent;

import com.github.egubot.objects.Abbreviations;

public class AutoRespond extends OnlineDataManager {

	private static String idKey = "Responses_Message_ID";
	private static String resourcePath = "/MessageResponses.txt";
	private static String parameterSplit = ">>>>>>>>>>";

	public AutoRespond(DiscordApi api) throws Exception {
		super(api, idKey, resourcePath, "Autorespond", true);

		this.setLockedDataEndIndex(92);
	}

	public boolean respond(String msgText, MessageCreateEvent e) {
		/*
		 * Content Example:
		 * Equal Split Normal Split test Split ok Split emoji
		 * Split emoji Split etc....
		 */
		Message reference;
		String[] responseContents;
		boolean replyFlag = false;
		for (int i = 0; i < getData().size(); i++) {
			responseContents = getData().get(i).replace("\t", "").split(parameterSplit);

			if (responseContents[1].strip().toLowerCase().matches("normal.*")) {
				if (responseContents[0].strip().toLowerCase().matches("equal.*")) {
					if (msgText.equalsIgnoreCase(responseContents[2].strip()))
						replyFlag = true;

				} else if (responseContents[0].strip().toLowerCase().matches("contain.*")) {
					if (msgText
							.matches("(.*)(?i)(?<!\\w)" + responseContents[2].strip().toLowerCase() + "(?![\\w-])(.*)"))
						replyFlag = true;

				} else if (responseContents[0].strip().toLowerCase().matches("match.*")) {
					if (msgText.matches(responseContents[2].strip().toLowerCase()))
						replyFlag = true;

				}

				if (replyFlag) {
					responseContents[3] = responseContents[3].replace("%n", "\n").replaceAll(":<(.*)>:", ":$1:");
					if (e.getMessage().getMessageReference().isPresent()) {
						reference = e.getMessage().getMessageReference().get().getMessage().get();
						reference.reply(responseContents[3], false);
						for (int j = 4; j < responseContents.length; j++) {
							responseContents[j] = Abbreviations.getReactionId(responseContents[j].strip());
							reference.addReactions(responseContents[j]);
						}
					} else {
						e.getChannel().sendMessage(responseContents[3]);
						for (int j = 4; j < responseContents.length; j++) {
							responseContents[j] = Abbreviations.getReactionId(responseContents[j].strip());
							e.getMessage().addReactions(responseContents[j]);
						}
					}
					return true;
				}
			} else if (responseContents[1].strip().equalsIgnoreCase("Embed")) {

			} else if (responseContents[1].strip().equalsIgnoreCase("Special")) {

			} else {
				System.err.println("\nThe following line is invalid and cannot be invoked:\n" + getData().get(i));
			}
		}

		return false;
	}

	public void writeResponse(String msgText, Messageable e) {
		try {
			String newResponse = msgText.substring("b-response create".length()).strip();

			newResponse = reformatResponse(newResponse);
			boolean isNameExist = false;

			if (!isResponseValid(newResponse))
				throw new Exception();

			for (int j = 0; j < getData().size(); j++) {
				if (isResponseEqual(getData().get(j), newResponse)) {
					isNameExist = true;
					break;
				}
			}

			if (isNameExist) {
				e.sendMessage("Already exists <:joea:1144008494568194099>");
			} else {
				getData().add(newResponse);
				writeData(e);
			}
		} catch (Exception e1) {
			e.sendMessage("Correct format:"
					+ "\nb-response create type >> message >> response (>> reaction1 >> reaction2 >>...)" + "\n\nTypes:"
					+ "\nContain, equal and match");
		}

	}

	private boolean isResponseValid(String st) {
		String[] token = st.replaceAll("[ \n%n]", "").split(parameterSplit);
		if (token[2].equals("") || token.length < 4 || (token[3].equals("") && token.length < 5 && token[4].equals("")))
			return false;

		return true;
	}

	private String reformatResponse(String st) {
		st = st.replaceFirst(">>", ">> Normal >>");
		st = st.replace(">>", parameterSplit);
		String lowcase = st.toLowerCase();
		if (lowcase.matches("equal.*") || lowcase.matches("contain.*") || lowcase.matches("match.*"))
			return st;
		else
			return null;
	}

	public void removeResponse(String msgText, Messageable e, boolean isOwner) {
		try {
			if (isOwner)
				setLockedDataEndIndex(0);

			String st = msgText.substring("b-response remove".length()).strip();

			if (st.equals(""))
				throw new Exception();

			boolean isNameExist = false;

			for (int j = getLockedDataEndIndex(); j < getData().size(); j++) {
				if (isResponseEqual(getData().get(j), st)) {
					isNameExist = true;
					getData().remove(j);
					break;
				}
			}

			if (isNameExist) {
				writeData(e);
			} else {
				e.sendMessage("No such invocation message");
			}
		} catch (Exception e1) {
			e.sendMessage("What?");
		}
	}

	public static String getResponseInvocation(String st) {
		String[] token = st.replace("\n", "").split(parameterSplit);

		if (token.length < 3)
			return st.strip();

		return token[2].strip();
	}

	public static boolean isResponseEqual(String st, String st2) {
		st = getResponseInvocation(st).toLowerCase().replace(" ", "");
		st2 = getResponseInvocation(st2).toLowerCase().replace(" ", "");

		return st.matches(st2 + "(.*)") || st2.matches(st + "(.*)");
	}

}
