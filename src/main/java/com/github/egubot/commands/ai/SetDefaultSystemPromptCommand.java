package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.DefaultSystemPromptContext;
import com.github.egubot.interfaces.Command;
import com.github.egubot.main.Bot;

public class SetDefaultSystemPromptCommand implements Command {

	@Override
	public String getName() {
		return "sys default set";
	}

	@Override
	public String getDescription() {
		return "Set a new default system prompt for all AI interactions across servers";
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
		return PermissionLevel.OWNER;
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!msg.getAuthor().asUser().isPresent()) {
			return false;
		}
		
		if (!msg.getAuthor().asUser().get().equals(Bot.getOwnerUser())) {
			msg.getChannel().sendMessage("Only the bot owner can change the default system prompt.");
			return true;
		}
		
		if (arguments == null || arguments.trim().isEmpty()) {
			msg.getChannel().sendMessage("Please provide a default system prompt. Usage: `!setdefaultsystemprompt <prompt>`");
			return true;
		}
		
		DefaultSystemPromptContext.setDefaultSystemPrompt(arguments.trim());
		msg.getChannel().sendMessage("Default system prompt updated globally. This will affect new servers and servers that reset to default.");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}