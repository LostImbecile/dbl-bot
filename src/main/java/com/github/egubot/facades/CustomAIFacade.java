package com.github.egubot.facades;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.gpt2.DiscordAI;
import com.github.egubot.managers.KeyManager;

public class CustomAIFacade {
	private static final Logger logger = LogManager.getLogger(CustomAIFacade.class.getName());
	private static String gpt2ChannelID = KeyManager.getID("GPT2_Channel_ID");
	private static boolean isCustomAIOn = false;
	private static boolean testMode;
	
	public static boolean respond(Message msg, String lowCaseTxt) {
		// Personal AI, refer to its class for info
		String channelID = msg.getChannel().getIdAsString();
		
		if (isCustomAIOn &&  (testMode || channelID.equals(gpt2ChannelID))) {
				getAIResponse(msg, lowCaseTxt);
				return true;
		}
		
		return false;
	}

	public static void getAIResponse(Message msg, String text) {
		try {
			String aiUrl = "http://localhost:5000"; // Update with your AI server URL
			try (DiscordAI discordAI = new DiscordAI(aiUrl)) {

				String generatedText = discordAI.generateText(text);

				if (!generatedText.matches("Error:(?s).*")) {
					msg.getChannel().sendMessage(generatedText);
				} else if (!generatedText.contains("Connect to localhost:5000"))
					logger.warn("AI Response: {}", generatedText);
			}
		} catch (Exception e1) {
			// not worth bothering with
		}
	}

	public static String getGpt2ChannelID() {
		return gpt2ChannelID;
	}

	public static void setGpt2ChannelID(String gpt2ChannelID) {
		CustomAIFacade.gpt2ChannelID = gpt2ChannelID;
	}

	public static boolean isCustomAIOn() {
		return isCustomAIOn;
	}

	public static void setCustomAIOn(boolean isCustomAIOn) {
		CustomAIFacade.isCustomAIOn = isCustomAIOn;
	}
}
