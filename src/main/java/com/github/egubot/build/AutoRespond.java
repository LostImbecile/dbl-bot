package com.github.egubot.build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.event.message.MessageCreateEvent;

import com.github.egubot.objects.Abbreviations;
import com.github.egubot.objects.Response;
import com.github.egubot.objects.ResponseList;
import com.github.egubot.shared.ConvertObjects;
import com.github.egubot.shared.JSONUtilities;
import com.github.egubot.storage.DataManagerSwitcher;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class AutoRespond extends DataManagerSwitcher {

	private static final Gson gson = new Gson();
	private static String resourcePath = "/MessageResponses.txt";
	private static String parameterSplit = ">>>>>>>>>>";
	private static String idKey = "Responses_Message_ID";
	private ResponseList autoRespondData;
	private ArrayList<Response> responses;

	private Random rng = new Random();

	public AutoRespond(DiscordApi api) {
		super(api, idKey, resourcePath, "Autorespond", true);
	}

	public boolean respond(String msgText, MessageCreateEvent e) {
		/*
		 * Content Example:
		 * Equal Split Normal Split test Split ok Split emoji
		 * Split emoji Split etc....
		 */
		Message reference;
		Response response;

		String replyMsg;
		List<String> reactions;
		boolean replyFlag = false;
		boolean deleteFlag = false;
		for (int i = 0; i < responses.size(); i++) {
			response = responses.get(i);
			reactions = response.getReactions();

			if (response.getResponseType().equalsIgnoreCase("normal")) {
				if (response.getMatchType().equalsIgnoreCase("equal")) {
					if (msgText.equalsIgnoreCase(response.getInvocMsg()))
						replyFlag = true;

				} else if (response.getMatchType().equalsIgnoreCase("contain")) {
					if (msgText.matches("(?s).*?(?i)(?<![\\w-.])(?:" + response.getInvocMsg() + ")(?![\\w-.])(?s).*+"))
						replyFlag = true;

				} else if (response.getMatchType().equalsIgnoreCase("match")) {
					if (msgText.matches("(?i)" + response.getInvocMsg()))
						replyFlag = true;

				} else if (response.getMatchType().equalsIgnoreCase("msg delete")) {
					if (msgText.matches("(?s).*?" + response.getInvocMsg() + "(?s).*+"))
						deleteFlag = true;
				} else if (response.getMatchType().equalsIgnoreCase("user delete")) {
					if (e.getMessageAuthor().getIdAsString().equals(response.getInvocMsg().replaceAll("[<>@ ]", "")))
						deleteFlag = true;
				}

				if (replyFlag) {
					response.incrementUsage();
					replyMsg = parseResponseMsg(response.getResponseMessage());
					if (e.getMessage().getMessageReference().isPresent()) {
						reference = e.getMessage().getMessageReference().get().getMessage().get();
						reference.reply(replyMsg, false);

						for (int j = 0; j < reactions.size(); j++) {
							reference.addReaction(Abbreviations.getReactionId(reactions.get(j)));
						}

					} else {
						e.getChannel().sendMessage(replyMsg);

						for (int j = 0; j < reactions.size(); j++) {
							e.getMessage().addReaction(Abbreviations.getReactionId(reactions.get(j)));
						}
					}
					
					try {
						// In order to update usage
						// Updates in 10m for online storage
						writeData(null, false);
					} catch (Exception e1) {
					}
					return true;
				}

				if (deleteFlag) {
					e.getMessage().delete();
					return true;
				}

			} else if (response.getResponseType().equalsIgnoreCase("Embed")) {

			} else if (response.getResponseType().equalsIgnoreCase("Special")) {

			} else {
				System.err.println("\nThe following line is invalid and cannot be invoked:\n" + getData().get(i));
			}
		}

		return false;
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
			String newResponse = msgText.substring("b-response create".length()).strip();

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
				writeData(e.getChannel(), false);
			}
		} catch (Exception e1) {
			e.getChannel()
					.sendMessage("Correct format:"
							+ "\nb-response create type >> message >> response (>> reaction1 >> reaction2 >>...)"
							+ "\n\nAlternative:\nb-response create type >> message >> option1 ?? option2 ?? ... >> ..."
							+ "\n\nTypes:" + "\nContain, equal and match");
		}

	}

	private String reformatResponse(String st, boolean isOwner) {
		st = st.replaceFirst(">>", ">> Normal >>");
		st = st.replace("\n", "%n");
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

			String st = msgText.substring("b-response remove".length()).strip();

			if (st.equals(""))
				throw new Exception();

			boolean isNameExist = false;

			Response response;
			for (int j = startIndex; j < responses.size(); j++) {
				response = responses.get(j);
				if (Response.isInvocEqual(response.getInvocMsg(), st)) {
					isNameExist = true;
					responses.remove(j);
					break;
				}
			}

			if (isNameExist) {
				writeData(e, false);
			} else {
				e.sendMessage("No such invocation message");
			}
		} catch (Exception e1) {
			e.sendMessage("What?");
		}
	}

	@Override
	public void updateObjects() {
		try {
			String jsonData = ConvertObjects.listToText(getData());
			autoRespondData = gson.fromJson(jsonData, ResponseList.class);
			autoRespondData.setCount(autoRespondData.getResponses().size());
			setLockedDataEndIndex(autoRespondData.getLockedDataIndex());
		} catch (JsonSyntaxException e) {
			autoRespondData = convertOldDataToResponses(getData(), parameterSplit);
		}
		responses = (ArrayList<Response>) autoRespondData.getResponses();
		Collections.sort(responses);
	}

	@Override
	public void updateDataFromObjects() {
		autoRespondData.setLockedDataIndex(getLockedDataEndIndex());
		autoRespondData.setCount(responses.size());
		String jsonData = gson.toJson(autoRespondData, ResponseList.class);
		jsonData = JSONUtilities.prettify(jsonData);
		setData(ConvertObjects.textToList(jsonData));
	}

	public ResponseList convertOldDataToResponses(List<String> data, String separator) {
		ResponseList responseList = new ResponseList();
		Response response;
		for (int i = 0; i < data.size(); i++) {
			response = convertStringToResponse(data.get(i), separator);
			if (response != null)
				responseList.getResponses().add(response);

		}
		responseList.setLockedDataIndex(getLockedDataEndIndex());
		responseList.setCount(responseList.getResponses().size());
		return responseList;
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
