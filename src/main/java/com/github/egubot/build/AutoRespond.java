package com.github.egubot.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.UpdatableObjects;
import com.github.egubot.objects.Abbreviations;
import com.github.egubot.objects.Attributes;
import com.github.egubot.objects.autorespond.Response;
import com.github.egubot.objects.autorespond.ResponseList;
import com.github.egubot.shared.ConvertObjects;
import com.github.egubot.shared.JSONUtilities;
import com.github.egubot.storage.DataManagerHandler;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class AutoRespond extends DataManagerHandler implements UpdatableObjects {

	private static String resourcePath = "Autorespond.txt";
	private static String idKey = "Responses_Message_ID";
	private ResponseList autoRespondData;
	private List<Response> responses;

	private Random rng = new Random();

	public AutoRespond() throws IOException {
		super(idKey, resourcePath, "Autorespond", true);
	}

	public boolean respond(String msgText, Message msg) {
		if (responses == null)
			return false;

		List<String> reactions;
		boolean replyFlag = false;
		boolean deleteFlag = false;
		for (Response response : responses) {
			reactions = response.getReactions();

			if (response.getResponseType().equalsIgnoreCase("normal")) {
				switch (response.getMatchType().toLowerCase()) {
				case "equal":
					if (msgText.equalsIgnoreCase(response.getInvocMsg()))
						replyFlag = true;
					break;
				case "contain":
					if (msgText.matches("(?s).*?(?i)(?<![\\w-.])(?:" + response.getInvocMsg() + ")(?![\\w-.])(?s).*+"))
						replyFlag = true;
					break;
				case "match":
					if (msgText.matches("(?i)" + response.getInvocMsg()))
						replyFlag = true;
					break;
				case "msg delete":
					if (msgText.matches("(?s).*?" + response.getInvocMsg() + "(?s).*+"))
						deleteFlag = true;
					break;
				case "user delete":
					if (msg.getAuthor().getIdAsString().equals(response.getInvocMsg().replaceAll("[<>@ ]", "")))
						deleteFlag = true;
					break;
				}
				if (replyFlag) {
					respond(msg, reactions, replyFlag, response);
					return true;
				}

				if (deleteFlag) {
					msg.delete();
					return true;
				}

			} else if (response.getResponseType().equalsIgnoreCase("Embed")) {

			} else if (response.getResponseType().equalsIgnoreCase("Special")) {

			} else {
				System.err.println("\nThe following line is invalid and cannot be invoked:\n" + response.toString());
			}
		}

		return false;
	}

	private boolean respond(Message msg, List<String> reactions, boolean replyFlag, Response response) {
		Message reference;
		String replyMsg;
		response.incrementUsage();
		replyMsg = parseResponseMsg(response.getResponseMessage());

		if (msg.getMessageReference().isPresent()) {
			reference = msg.getMessageReference().get().getMessage().get();
			if (isReplyToReference(response, reference)) {
				reference.reply(replyMsg, false);

				for (String reaction : reactions) {
					reference.addReaction(Abbreviations.getReactionId(reaction));
				}
				replyFlag = false;
			}
		}
		if (!isRespondToUser(response, msg))
			replyFlag = false;

		if (replyFlag) {
			msg.getChannel().sendMessage(replyMsg);

			for (String reaction : reactions) {
				msg.addReaction(Abbreviations.getReactionId(reaction));
			}
		}

		try {
			// In order to update usage
			// Updates in 10m for online storage
			writeData(null);
		} catch (Exception e1) {
		}
		return replyFlag;
	}

	private boolean isReplyToReference(Response response, Message msg) {
		Attributes attr = response.getAttr();
		MessageAuthor author = msg.getAuthor();

		// This is so the author of the message or the reference
		// gets completely ignored if they are set to be ignored
		if (!isRespondToUser(response, msg))
			return false;

		if (attr.isReplyToAuthor() && UserInfoUtilities.isUserEqual(author, response.getAuthorID()))
			return true;
		if (attr.isReplyToOwner() && (UserInfoUtilities.isOwner(msg) || UserInfoUtilities.isServerOwner(msg)))
			return true;
		if (attr.isReplyToAdmin() && author.isServerAdmin())
			return true;
		if (attr.isReplyToReply())
			return true;

		return false;
	}

	private boolean isRespondToUser(Response response, Message msg) {
		Attributes attr = response.getAttr();
		MessageAuthor author = msg.getAuthor();
		if (attr.isDisabled())
			return false;
		if (attr.isIgnoreAuthor() && UserInfoUtilities.isUserEqual(author, response.getAuthorID()))
			return false;
		if (attr.isIgnoreOwner() && UserInfoUtilities.isOwner(msg))
			return false;
		if (attr.isIgnoreAdmin() && author.isServerAdmin())
			return false;

		return true;
	}

	private String parseResponseMsg(String response) {
		String[] randomOptions = response.split("\\?\\?");
		if (randomOptions.length > 1) {
			response = randomOptions[rng.nextInt(randomOptions.length)];
		}
		return response.replace("%n", "\n").replaceAll(":<(.*)>:", ":$1:");
	}

	public void writeResponse(String msgText, Message e, boolean isOwner) {
		try {
			String newResponse = msgText.replace("\n", "%n");

			newResponse = reformatResponse(newResponse, isOwner);
			boolean isNameExist = false;

			Response newResp = convertStringToResponse(newResponse, ">>");

			for (int j = 0; j < responses.size(); j++) {
				// If it's null it throws an exception so the message is sent
				if (newResp.equals(responses.get(j))) {
					isNameExist = true;
					break;
				}
			}
			newResp.setAuthor(e.getAuthor().getIdAsString() + "-" + e.getAuthor().getDiscriminatedName());

			if (isNameExist) {
				e.getChannel().sendMessage("Already exists <:joea:1144008494568194099>");
			} else {
				responses.add(newResp);
				writeData(e.getChannel());
			}
		} catch (NullPointerException | StringIndexOutOfBoundsException e1) {
			e.getChannel()
					.sendMessage("Correct format:"
							+ "\nb-response create type >> message >> response (>> reaction1 >> reaction2 >>...)"
							+ "\n\nAlternative:\nb-response create type >> message >> option1 ?? option2 ?? ... >> ..."
							+ "\n\nTypes:" + "\nContain, equal and match");
		} catch (Exception e1) {
			String error = "Problematic Message: \"" + msgText + "\"";
			logger.error(error, e1);
		}

	}

	private String reformatResponse(String st, boolean isOwner) {
		st = st.replaceFirst(">>", ">> Normal >>");
		String responseType = st.substring(0, st.indexOf(">>")).strip();
		if (responseType.equals("equal") || responseType.equals("contain") || responseType.equals("match"))
			return st;
		if (isOwner && (responseType.equals("msg delete") || responseType.equals("user delete")))
			return st + " >> Delete";

		return null;
	}

	public void removeResponse(String msgText, Messageable e, boolean isOwner) {
		try {
			int startIndex = getLockedDataEndIndex();

			if (isOwner)
				startIndex = 0;

			if (msgText.isBlank())
				throw new Exception();

			boolean isNameExist = false;

			Response response;
			for (int j = startIndex; j < responses.size(); j++) {
				response = responses.get(j);
				if (Response.isInvocEqual(response.getInvocMsg(), msgText)) {
					if (response.getAttr().isDeletable() || isOwner) {
						isNameExist = true;
						responses.remove(j);
					}
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

	public void updateResponse(String msgText, Messageable e, boolean isOwner) {
		try {
			int startIndex = getLockedDataEndIndex();

			if (isOwner)
				startIndex = 0;

			if (msgText.isBlank())
				throw new Exception();

			String[] arguments = msgText.split(">>");
			if (arguments.length < 2)
				throw new Exception();

			msgText = arguments[0].strip();
			boolean isResponseExist = false;

			Response response = null;
			for (int j = startIndex; j < responses.size(); j++) {
				response = responses.get(j);
				if (Response.isInvocEqual(response.getInvocMsg(), msgText)) {
					if (response.getAttr().isEditable() || isOwner)
						isResponseExist = true;
					break;
				}
			}

			boolean isUpdated = false;
			if (isResponseExist) {
				for (int i = 1; i < arguments.length; i++) {
					String argument = arguments[i];
					if (response.updateResponse(argument))
						isUpdated = true;
				}
			}

			if (isResponseExist && isUpdated) {
				writeData(e);
			} else if (isResponseExist) {
				e.sendMessage("Nothing was updated.");
			} else {
				e.sendMessage("No such invocation message.");
			}
		} catch (Exception e1) {
			e.sendMessage("What?");
		}
	}

	@Override
	public void updateObjects() {
		try {
			Gson gson = new Gson();
			String jsonData = ConvertObjects.listToText(getData());
			autoRespondData = gson.fromJson(jsonData, ResponseList.class);
			autoRespondData.setCount(autoRespondData.getResponses().size());
			setLockedDataEndIndex(autoRespondData.getLockedDataIndex());
		} catch (JsonSyntaxException e) {
			logger.error("Syntax Error updating objects", e);
		} catch (NullPointerException e) {
			logger.error("Null pointer updating objects", e);
		}
		if (autoRespondData == null)
			autoRespondData = new ResponseList();
		responses = Collections.synchronizedList((ArrayList<Response>) autoRespondData.getResponses());
		Collections.sort(responses);
	}

	@Override
	public void updateDataFromObjects() {
		if (autoRespondData == null)
			return;
		Gson gson = new Gson();
		autoRespondData.setLockedDataIndex(getLockedDataEndIndex());
		autoRespondData.setCount(responses.size());
		String jsonData = gson.toJson(autoRespondData, ResponseList.class);
		jsonData = JSONUtilities.prettify(jsonData);
		setData(ConvertObjects.textToList(jsonData));
	}

	private static Response convertStringToResponse(String st, String separator) {
		if (st == null)
			return null;

		String matchType;
		String responseType;
		String invocMsg;
		String responseMsg;
		String[] values = st.split(separator);
		List<String> reactions = new ArrayList<>(0);
		if (values.length > 3) {
			matchType = values[0].strip();
			responseType = values[1].strip();
			invocMsg = values[2].strip();
			responseMsg = values[3].strip();
			if (values.length > 4) {
				for (int j = 4; j < values.length; j++) {
					reactions.add(values[j].strip());
				}
			}
			return new Response(matchType, responseType, invocMsg, responseMsg, reactions, JSONUtilities.generateId());
		} else
			return null;
	}

}
