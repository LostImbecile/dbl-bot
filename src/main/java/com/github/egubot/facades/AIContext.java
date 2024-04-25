package com.github.egubot.facades;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.ai.AIModel;
import com.github.egubot.objects.APIResponse;

public abstract class AIContext {
    private static final Logger logger = LogManager.getLogger(AIContext.class.getName());
    private static List<String> conversation = Collections.synchronizedList(new LinkedList<String>());
    private static boolean isAIOn = false;
	private static String activeChannelID = "";
    private static int lastTokens = 0;
    private static AIModel model;
    
    public static boolean respondIfChannelActive(Message msg, String msgText) {

		if (isAIOn && (msg.getChannel().getIdAsString().equals(activeChannelID))) {
			return respond(msg, msgText);
		}
		return false;

	}

    public static boolean respond(Message msg, String msgText) {
        try {
            conversation.add(AIModel.reformatInput(msgText, msg.getAuthor().getName()));
            APIResponse response = getModel().sendRequest(msgText, msg.getAuthor().getName(), conversation);

            if (!response.isError()) {
                msg.getChannel().sendMessage(response.getResponse());
                conversation.add(AIModel.reformatInput(response.getResponse(), "assistant"));

                lastTokens = response.getTotalTokens();
                if (lastTokens > getModel().getTokenLimit() - 1000) {
                    int deleteCount = Math.min(getModel().getTokenLimit() / 4096, 5);
                    for (int i = 0; i < deleteCount; i++) {
                        conversation.remove(0);
                    }
                }
            }
            return true;
        } catch (IOException e) {
            logger.error(e);
        }
        return false;
    }

    public static List<String> getConversation() {
        return conversation;
    }

    public static void setConversation(List<String> conversation) {
        AIContext.conversation = conversation;
    }

    public static int getLastTokens() {
        return lastTokens;
    }

	public static AIModel getModel() {
		return model;
	}
	
	public static boolean isAIOn() {
		return isAIOn;
	}

	public static void setAIOn(boolean isChatGPTOn) {
		isAIOn = isChatGPTOn;
	}
	
	public static void toggle() {
		isAIOn = !isAIOn;
	}

	public static String getActiveChannelID() {
		return activeChannelID;
	}

	public static void setActiveChannelID(String chatGPTActiveChannelID) {
		activeChannelID = chatGPTActiveChannelID;
	}

	public static void setModel(AIModel model) {
		AIContext.model = model;
	}

}