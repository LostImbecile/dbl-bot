package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;

import com.github.egubot.facades.SystemPromptContext;
import com.github.egubot.interfaces.Command;

public class GetSystemPromptCommand implements Command {

	@Override
	public String getName() {
		return "sys get";
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
			msg.getChannel().sendMessage("You need administrator permissions to view the system prompt.");
			return true;
		}
		
		String systemPrompt = SystemPromptContext.getSystemPrompt(msg);
		String truncated = systemPrompt.length() > 1800 ? systemPrompt.substring(0, 1800) + "..." : systemPrompt;
		msg.getChannel().sendMessage("Current system prompt for this server:\n```\n" + truncated + "\n```");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}