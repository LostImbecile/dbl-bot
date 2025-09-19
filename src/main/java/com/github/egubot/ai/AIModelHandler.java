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

import com.github.egubot.facades.SystemPromptContext;
import com.github.egubot.objects.APIResponse;
import com.github.egubot.objects.ModelData;
import com.github.egubot.shared.Shared;
import com.google.gemini.GeminiAI;

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
			Long serverId = msg.getServer().map(server -> server.getId()).orElse(null);
			conversations.putIfAbsent(channelId, new LinkedList<>());
			List<String> conversation = conversations.get(channelId);

			String userNameField = "Username (" + msg.getAuthor().getName() + ")";
			if(!msg.getAuthor().getName().equalsIgnoreCase(msg.getAuthor().getDisplayName()))
				userNameField += ", Display Name(" + msg.getAuthor().getDisplayName() + ")";
			
			clearConversationIfTooLarge(channelId, getModel().getTokenLimit() - 2000);
			
			APIResponse response = getModel().sendRequest(msgText, userNameField, conversation, serverId);

			if (!response.isError()) {
				conversation.add(model.reformatInput(msgText, "user"));
				msg.getChannel().sendMessage(response.getResponse().replaceAll("<think>(?s).*</think>", ""));
				conversation.add(model.reformatInput(response.getResponse(), "assistant"));

				lastTokens.put(channelId, response.getTotalTokens());
			} else {
				String errorMsg = "API Error: " + response.getResponse();
				msg.getChannel().sendMessage(errorMsg);
				logger.warn("API error for channel {}: Status {}, Message: {}", channelId, response.getStatusCode(), response.getResponse());
			}
			return true;
		} catch (java.net.SocketTimeoutException e) {
			msg.getChannel().sendMessage("Request timed out - the AI service took too long to respond.");
			logger.error("Socket timeout for channel {}: {}", msg.getChannel().getId(), e.getMessage());
		} catch (java.net.ConnectException e) {
			msg.getChannel().sendMessage("Connection failed - unable to reach AI service.");
			logger.error("Connection error for channel {}: {}", msg.getChannel().getId(), e.getMessage());
		} catch (IOException e) {
			msg.getChannel().sendMessage("Network error - please try again later.");
			logger.error("IO Exception for channel {}: ", msg.getChannel().getId(), e);
		}
		return false;
	}

	public boolean respond(Message msg, String msgText, List<String> attachmentLinks) {
		try {
			Long channelId = msg.getChannel().getId();
			Long serverId = msg.getServer().map(server -> server.getId()).orElse(null);
			conversations.putIfAbsent(channelId, new LinkedList<>());
			List<String> conversation = conversations.get(channelId);

			String userNameField = "Username (" + msg.getAuthor().getName() + ")";
			if(!msg.getAuthor().getName().equalsIgnoreCase(msg.getAuthor().getDisplayName()))
				userNameField += ", Display Name(" + msg.getAuthor().getDisplayName() + ")";
			
			clearConversationIfTooLarge(channelId, getModel().getTokenLimit() - 2000);
			
			APIResponse response;
			if (attachmentLinks != null && !attachmentLinks.isEmpty() && model instanceof GeminiAI) {
				GeminiAI gemini = (GeminiAI) model;
				response = gemini.sendRequestWithImage(msgText, attachmentLinks.get(0), userNameField, conversation, serverId);
			} else {
				response = getModel().sendRequest(msgText, userNameField, conversation, serverId);
			}
			
			if (!response.isError()) {
				conversation.add(model.reformatInput(msgText, "user"));
				msg.getChannel().sendMessage(response.getResponse().replaceAll("<think>(?s).*</think>", ""));
				conversation.add(model.reformatInput(response.getResponse(), "assistant"));
				lastTokens.put(channelId, response.getTotalTokens());
			} else {
				String errorMsg = "API Error: " + response.getResponse();
				msg.getChannel().sendMessage(errorMsg);
				logger.warn("API error for channel {}: Status {}, Message: {}", channelId, response.getStatusCode(), response.getResponse());
			}
			return true;
		} catch (java.net.SocketTimeoutException e) {
			msg.getChannel().sendMessage("Request timed out - the AI service took too long to respond.");
			logger.error("Socket timeout for channel {}: {}", msg.getChannel().getId(), e.getMessage());
		} catch (java.net.ConnectException e) {
			msg.getChannel().sendMessage("Connection failed - unable to reach AI service.");
			logger.error("Connection error for channel {}: {}", msg.getChannel().getId(), e.getMessage());
		} catch (IOException e) {
			msg.getChannel().sendMessage("Network error - please try again later.");
			logger.error("IO Exception for channel {}: ", msg.getChannel().getId(), e);
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

	public void clearConversationIfTooLarge(Long channelId, int maxTokens) {
		if (conversations.containsKey(channelId)) {
			List<String> conversation = conversations.get(channelId);
			int estimatedTokens = lastTokens.getOrDefault(channelId, 0);
			
			if (estimatedTokens > maxTokens) {
				int removeCount = conversation.size() / 3;
				if (removeCount > 0 && removeCount < conversation.size()) {
					try {
						conversation.subList(0, removeCount).clear();
						logger.info("Proactively cleared {} conversation entries for channel {} (estimated {} tokens)", 
								removeCount, channelId, estimatedTokens);
						lastTokens.put(channelId, estimatedTokens / 2);
					} catch (Exception e) {
						logger.error("Failed to proactively clear conversation: ", e);
						conversation.clear();
						lastTokens.put(channelId, 0);
					}
				}
			}
		}
	}

	public boolean isChannelActive(Long channelId) {
		return Boolean.TRUE.equals(isActive.getOrDefault(channelId, false));
	}

	public void setChannelActive(Long channelId, boolean active) {
		isActive.put(channelId, active);
	}

	public void setSystemPromptAsUser(boolean asUser) {
		// This method is now deprecated as it's handled per-server
		// Consider removing or updating to work with a specific server
	}

	public boolean isSystemPromptAsUser() {
		// This method is now deprecated as it's handled per-server
		// Consider removing or updating to work with a specific server
		return false;
	}

	public String getProcessedSystemPrompt() {
		return getProcessedSystemPrompt(null);
	}
	
	public String getProcessedSystemPrompt(Long serverId) {
		if (model != null) {
			String systemPrompt = SystemPromptContext.getSystemPrompt(serverId);
			return model.processPlaceholders(systemPrompt, serverId);
		}
		return "";
	}
}