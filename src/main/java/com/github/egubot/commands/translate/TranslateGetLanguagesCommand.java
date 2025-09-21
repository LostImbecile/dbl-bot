package com.github.egubot.commands.translate;

import java.io.IOException;

import org.javacord.api.entity.message.Message;

import com.azure.services.Translate;
import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.utils.FileUtilities;

public class TranslateGetLanguagesCommand implements Command {

	@Override
	public String getName() {
		return "translate languages";
	}

	@Override
	public String getDescription() {
		return "Get list of all available languages for translation";
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
	public boolean execute(Message msg, String arguments) {
		try {
			msg.getChannel().sendMessage(FileUtilities.toInputStream(Translate.getTranslateLanguages()),
					"languages.txt");
		} catch (IOException e1) {
			msg.getChannel().sendMessage("Failed to send :thumbs_down");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}