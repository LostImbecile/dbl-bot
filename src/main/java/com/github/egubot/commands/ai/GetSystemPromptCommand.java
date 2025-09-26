package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import com.github.egubot.facades.SystemPromptContext;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class GetSystemPromptCommand implements Command {

	@Override
	public String getName() {
		return "sys get";
	}

	@Override
	public String getDescription() {
		return "Get the current system prompt for this server's AI interactions";
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
			msg.getChannel().sendMessage("You need administrator permissions to view the system prompt.");
			return true;
		}
		String prompt = SystemPromptContext.getSystemPrompt(msg);
		String truncated = prompt.length() > 1800 ? prompt.substring(0, 1800) + "..." : prompt;
		msg.getChannel().sendMessage("Current system prompt for this server:\n```\n" + truncated + "\n```");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}