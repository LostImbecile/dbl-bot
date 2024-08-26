package com.github.egubot.ai;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.objects.APIResponse;
import com.github.egubot.shared.Shared;

public class AIModelHandler {
    private static final Logger logger = LogManager.getLogger(AIModelHandler.class.getName());
    private Map<Long, List<String>> conversations = new HashMap<>();
    private Map<Long, Integer> lastTokens = new HashMap<>();
    private Map<Long, Boolean> isActive = new HashMap<>();

    private boolean isAIOn = true;
    private AIModel model = null;

    public AIModelHandler(AIModel model) {
        this.model = model;
        Shared.getShutdown().registerShutdownable(model);
        if (!testModel()) {
            logger.error("Model test failed. Model {} is not functional.", model.getModelName());
            isAIOn = false;
        }
    }

    public boolean testModel() {
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

    public boolean respond(Message msg, String msgText) {
        try {
            Long channelId = msg.getChannel().getId();
            conversations.putIfAbsent(channelId, new LinkedList<>());
            List<String> conversation = conversations.get(channelId);

            conversation.add(model.reformatInput(msgText, msg.getAuthor().getName()));
            APIResponse response = getModel().sendRequest(msgText, msg.getAuthor().getName(), conversation);

            if (!response.isError()) {
                msg.getChannel().sendMessage(response.getResponse());
                conversation.add(model.reformatInput(response.getResponse(), "assistant"));

                lastTokens.put(channelId, response.getTotalTokens());
                if (lastTokens.get(channelId) > getModel().getTokenLimit() - 1000) {
                    int deleteCount = Math.min(getModel().getTokenLimit() / 4096, 5);
                    conversations.get(channelId).subList(0, deleteCount).clear();
                }
            }
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