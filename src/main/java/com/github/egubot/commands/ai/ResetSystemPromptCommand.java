package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;

import com.github.egubot.facades.SystemPromptContext;
import com.github.egubot.interfaces.Command;

public class ResetSystemPromptCommand implements Command {

	@Override
	public String getName() {
		return "sys reset";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!msg.getAuthor().asUser().isPresent()) {
			return false;
		}
		
		if (!msg.getServer().isPresent()) {
			msg.getChannel().sendMessage("This command can only be used in a server.");
			return true;
		}
		
		if (!msg.getServer().get().hasPermission(msg.getAuthor().asUser().get(), PermissionType.ADMINISTRATOR)) {
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