package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;

import com.github.egubot.facades.SystemPromptContext;
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
		if (!msg.getAuthor().asUser().isPresent()) {
			return false;
		}
		
		if (!msg.getServer().isPresent()) {
			msg.getChannel().sendMessage("This command can only be used in a server.");
			return true;
		}
		
		if (!msg.getServer().get().hasPermission(msg.getAuthor().asUser().get(), PermissionType.ADMINISTRATOR)) {
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