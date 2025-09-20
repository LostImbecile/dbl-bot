package com.github.egubot.commands.gemini;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class GeminiChannelToggleCommand implements Command {

	@Override
	public String getName() {
		return "gem channel toggle";
	}

	@Override
	public String getDescription() {
		return "Toggle Gemini AI always active mode on or off for specific channels";
	}

	@Override
	public String getUsage() {
		return getName() + " [#channel]";
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
			AIContext.getGemini().toggleChannel(msg);
			msg.getChannel().sendMessage("Gemini channel toggled");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}