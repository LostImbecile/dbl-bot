package com.github.egubot.facades;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import com.azure.services.Translate;

public class TranslateFacade {
	private static final Logger logger = LogManager.getLogger(TranslateFacade.class.getName());
	private static Translate translate = new Translate();
	private static boolean isTranslateOn = false;
	
	private TranslateFacade() {
	}
	
	public static void toggleTranslate() {
		isTranslateOn = !isTranslateOn;
	}

	public static boolean checkCommands(Message msg, String lowCaseTxt) {
		if (isTranslateOn) {
			try {
				if (lowCaseTxt.length() < 140) {
					String lang = translate.detectLanguage(lowCaseTxt, true);
					if (!lang.contains("en") && !lang.contains("Error"))
						msg.getChannel().sendMessage(translate.post(lowCaseTxt, true));
				}

			} catch (IOException e1) {
				logger.error("Failed to translate.", e1);
			}

		}
		return false;
	}


	public static Translate getTranslate() {
		return translate;
	}

	public static void setTranslate(Translate translate) {
		TranslateFacade.translate = translate;
	}
}
