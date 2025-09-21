package com.github.egubot.commands.translate;

import org.javacord.api.entity.message.Message;

import com.azure.services.Translate;
import com.github.egubot.facades.TranslateFacade;
import com.github.egubot.interfaces.Command;

public class TranslateSetLanguageCommand implements Command {

	@Override
	public String getName() {
		return "translate set";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		Translate translate = TranslateFacade.getTranslate();
		if (arguments.contains("-")) {
			String[] toFrom = arguments.split("-");
			translate.setFrom(toFrom[0]);
			translate.setTo(toFrom[1]);
		} else {
			translate.setTo(arguments);
			translate.setFrom("");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

	@Override
	public String getDescription() {
		return "Set the target language for automatic translation";
	}

	@Override
	public String getUsage() {
		return getName() + " <language_code>";
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