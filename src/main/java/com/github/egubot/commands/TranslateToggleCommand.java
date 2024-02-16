package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.TranslateFacade;
import com.github.egubot.interfaces.Command;

public class TranslateToggleCommand implements Command {

	@Override
	public String getName() {
		return "translate toggle";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		TranslateFacade.toggleTranslate();
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
