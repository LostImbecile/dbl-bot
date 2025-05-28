package com.github.egubot.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.objects.APIResponse;
import com.github.egubot.objects.ModelData;
import com.github.egubot.shared.Shared;

public class AIModelHandler {
	private static final Logger logger = LogManager.getLogger(AIModelHandler.class.getName());
	private Map<Long, List<String>> conversations = new HashMap<>();
	private Map<Long, Integer> lastTokens = new HashMap<>();
	private Map<Long, Boolean> isActive = new HashMap<>();

	private boolean isAIOn = true;
	private AIModel model = null;
	private boolean testValid = true;

	public AIModelHandler(AIModel model) {
		this.model = model;
		Shared.getShutdown().registerShutdownable(model);
		if (!testModel()) {
			logger.error("Model test failed. Model {} is not functional.", model.getModelName());
			isAIOn = false;
		}
	}

	public AIModelHandler(AIModel model, boolean testValid) {
		this.testValid = testValid;
		this.model = model;
		Shared.getShutdown().registerShutdownable(model);
	}

	public boolean testModel() {
		if (!testValid)
			return true;
		try {
			String testInput = "Hello, how are you?";
			String testName = "Test User";
			APIResponse response = model.sendRequest(testInput, testName, null);
			return !response.isError();
		} catch (IOException e) {
			logger.error("Model test failed with exception", e);
			return false;
		}
	}

	public boolean respondIfChannelActive(Message msg, String msgText) {
		if (isAIOn) {
			Long channelId = msg.getChannel().getId();
			if (Boolean.TRUE.equals(isActive.getOrDefault(channelId, false))) {
				return respond(msg, msgText);
			}
		}
		return false;
	}
	
	public List<String> getModelList() {
		if (model != null) {
			try {
				ArrayList<String> modelList = new ArrayList<>();
				for (ModelData modelData : model.getModelsList().getData()) {
					if(modelData.getObject().equals("model")) {
						modelList.add(modelData.getId());
					}
				}
				return modelList;
			} catch (IOException e) {
				return Collections.emptyList();
			}
		}
		return Collections.emptyList();
	}

	public boolean respond(Message msg, String msgText) {
		try {
			Long channelId = msg.getChannel().getId();
			conversations.putIfAbsent(channelId, new LinkedList<>());
			List<String> conversation = conversations.get(channelId);

			String userNameField = "Username (" + msg.getAuthor().getName() + ")";
			if(!msg.getAuthor().getName().equalsIgnoreCase(msg.getAuthor().getDisplayName()))
				userNameField += ", Display Name(" + msg.getAuthor().getDisplayName() + ")";
			
			APIResponse response = getModel().sendRequest(msgText, userNameField, conversation);

			if (!response.isError()) {
				conversation.add(model.reformatInput(msgText, "user"));
				msg.getChannel().sendMessage(response.getResponse().replaceAll("<think>(?s).*</think>", ""));
				conversation.add(model.reformatInput(response.getResponse(), "assistant"));

				lastTokens.put(channelId, response.getTotalTokens());
				if (lastTokens.get(channelId) > getModel().getTokenLimit() - 1000) {
					int deleteCount = Math.min(getModel().getTokenLimit() / 4096, 5);
					conversations.get(channelId).subList(0, deleteCount).clear();
				}
			} else
				msg.getChannel().sendMessage("Error: " + response.getResponse());
			return true;
		} catch (IOException e) {
			msg.getChannel().sendMessage("Timed out.");
			logger.error(e);
		}
		return false;
	}

	public void clearConversation(Message msg) {
		Long channelId = msg.getChannel().getId();
		if (conversations.containsKey(channelId)) {
			conversations.get(channelId).clear();
			lastTokens.put(channelId, 0);
		}
	}

	public int getLastTokens(Message msg) {
		return lastTokens.getOrDefault(msg.getChannel().getId(), 0);
	}

	public AIModel getModel() {
		return model;
	}

	public boolean isAIOn() {
		return isAIOn;
	}

	public void setAIOn(boolean isOn) {
		isAIOn = isOn;
	}

	public void toggleChannel(Message msg) {
		Long channelId = msg.getChannel().getId();
		boolean value = isActive.getOrDefault(channelId, false);
		isActive.put(channelId, !value);
	}

	public void toggle() {
		isAIOn = !isAIOn;
	}

	public void setModel(AIModel model) {
		this.model = model;
		if (!testModel()) {
			logger.error("Model test failed. Model is not functional.");
			isAIOn = false;
		}
	}
}