package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import com.github.egubot.facades.SystemPromptContext;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ResetSystemPromptCommand implements Command {

	@Override
	public String getName() {
		return "sys reset";
	}

	@Override
	public String getDescription() {
		return "Reset the server's system prompt to the default system prompt";
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
			msg.getChannel().sendMessage("You need administrator permissions to reset the system prompt.");
			return true;
		}
		
		SystemPromptContext.resetToDefault(msg);
		msg.getChannel().sendMessage("System prompt reset to default for this server.");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}