package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.facades.WeatherFacade;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.interfaces.DiscordTimerTask;

public class WeatherCommand implements Command, DiscordTimerTask {

	@Override
	public String getName() {
		return "weather";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		sendWeather(msg.getChannel(), arguments);
		return true;
	}

	public void sendWeather(Messageable msg, String arguments) {
		WeatherFacade.sendWeather(msg, arguments);
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

	@Override
	public boolean execute(long targetChannel, String arguments) throws Exception {
		sendWeather(ServerInfoUtilities.getTextableRegularServerChannel(targetChannel), arguments);
		return true;
	}

}
