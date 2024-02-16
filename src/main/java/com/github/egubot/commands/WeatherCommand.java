package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.WeatherFacade;
import com.github.egubot.interfaces.Command;

public class WeatherCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "weather";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		WeatherFacade.sendWeather(msg.getChannel(), arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		// TODO Auto-generated method stub
		return true;
	}

}
