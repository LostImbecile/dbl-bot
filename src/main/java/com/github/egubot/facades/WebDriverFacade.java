package com.github.egubot.facades;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.webautomation.AIResponseGenerator;

public class WebDriverFacade {
	private static final Logger logger = LogManager.getLogger(WebDriverFacade.class.getName());
	
	public static boolean checkCommands(Message msg, String lowCaseText) {
		if (lowCaseText.matches("b-insult(?s).*")) {
			String[] options = lowCaseText.replaceFirst("b-insult", "").split(">>");
			if (options.length < 2) {
				msg.getChannel().sendMessage("Hast thou no target, no foe, or no purpose in mind?");
			} else {
				try (AIResponseGenerator a = new AIResponseGenerator()) {
					msg.getChannel().sendMessage("Will be whispered in time.");
					String response = a.getResponse(options[0], options[1]);
					msg.getAuthor().asUser().get().sendMessage(response);
				} catch (Exception e) {
					logger.error("Failed to get response from online AI.", e);
					msg.getAuthor().asUser().get().sendMessage("Perhaps not.");
				}
			}
			return true;
		}
		return false;
	}

}
