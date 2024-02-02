package com.github.egubot.facades;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.gpt2.DiscordAI;
import com.github.egubot.main.KeyManager;

public class CustomAIFacade {
	private static final Logger logger = LogManager.getLogger(CustomAIFacade.class.getName());
	private String gpt2ChannelID = KeyManager.getID("GPT2_Channel_ID");
	private boolean isCustomAIOn = false;
	private boolean testMode;
	
	public boolean checkCommands(Message msg, String lowCaseTxt) {
		String channelID = msg.getChannel().getIdAsString();
		// Personal AI, refer to its class for info
		if (lowCaseTxt.equals("ai activate")) {
			isCustomAIOn = true;
			return true;
		}
		
		if (isCustomAIOn) {
			if (lowCaseTxt.equals("ai terminate")) {
				isCustomAIOn = false;
				return true;
			}

			if (testMode || channelID.equals(gpt2ChannelID) || lowCaseTxt.matches("ai(?s).*")) {

				try {
					String aiUrl = "http://localhost:5000"; // Update with your AI server URL
					try (DiscordAI discordAI = new DiscordAI(aiUrl)) {
						String input = lowCaseTxt.replace("ai", "").strip();

						String generatedText = discordAI.generateText(input);

						if (!generatedText.matches("Error:(?s).*")) {
							msg.getChannel().sendMessage(generatedText);
						} else if (!generatedText.contains("Connect to localhost:5000"))
							logger.warn("AI Response: {}", generatedText);
					}
				} catch (Exception e1) {
					// not worth bothering with
				}
				return true;
			}
		}
		
		return false;
	}
}
