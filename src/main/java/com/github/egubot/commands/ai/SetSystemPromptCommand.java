package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import com.github.egubot.facades.SystemPromptContext;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class SetSystemPromptCommand implements Command {

	@Override
	public String getName() {
		return "sys set";
	}

	@Override
	public String getDescription() {
		return "Set a custom system prompt for AI interactions in this server";
	}

	@Override
	public String getUsage() {
		return getName() + " <prompt text>";
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
			msg.getChannel().sendMessage("You need administrator permissions to change the system prompt.");
			return true;
		}

		if (arguments == null || arguments.trim().isEmpty()) {
			msg.getChannel().sendMessage("Please provide a system prompt. Usage: `!sys set <prompt>`");
			return true;
		}

		SystemPromptContext.setSystemPrompt(msg, arguments.trim());
		msg.getChannel().sendMessage("System prompt updated for this server.");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}