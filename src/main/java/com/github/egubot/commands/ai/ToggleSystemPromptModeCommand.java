package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import com.github.egubot.facades.SystemPromptContext;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ToggleSystemPromptModeCommand implements Command {

	@Override
	public String getName() {
		return "sys toggle";
	}

	@Override
	public String getDescription() {
		return "Toggle between sending the system prompt as a message or as system for AI interactions";
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
		Server server = ServerInfoUtilities.getServer(msg);
		if (server == null) {
			msg.getChannel().sendMessage("This command can only be used in a server.");
			return true;
		}
		if (!UserInfoUtilities.isAdmin(msg)) {
			msg.getChannel().sendMessage("You need administrator permissions to toggle system prompt mode.");
			return true;
		}
		
		boolean currentMode = SystemPromptContext.getSendAsSystem(msg);
		SystemPromptContext.setSendAsSystem(msg, !currentMode);
		
		String modeText = !currentMode ? "system message" : "user message";
		msg.getChannel().sendMessage("System prompt delivery mode changed to: " + modeText);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}