package com.github.egubot.facades;

import org.javacord.api.entity.message.Message;

public class WebFacadesHandler {
	private TranslateFacade translate = new TranslateFacade();
	private WeatherFacade weather = new WeatherFacade();

	public boolean checkCommands(Message msg, String msgText, String lowCaseTxt) {
		return translate.checkCommands(msg, lowCaseTxt) || weather.checkCommands(msg, lowCaseTxt)
				|| WebDriverFacade.checkCommands(msg, msgText, lowCaseTxt);
	}
}
