package com.github.egubot.commands.translate;

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

	@Override
	public String getDescription() {
		return "Toggle automatic translation feature on or off for the server";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "Translation";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}
}