package com.github.egubot.commands.gemini;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class GeminiToggleCommand implements Command {

	@Override
	public String getName() {
		return "gem toggle";
	}

	@Override
	public String getDescription() {
		return "Toggle Gemini AI functionality on or off globally";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "AI";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (UserInfoUtilities.isOwner(msg)) {
			AIContext.getGemini().toggle();
			msg.getChannel().sendMessage("Gemini toggled");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}