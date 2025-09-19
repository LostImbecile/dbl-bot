package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.DefaultSystemPromptContext;
import com.github.egubot.main.Bot;
import com.github.egubot.interfaces.Command;

public class SetDefaultSystemPromptCommand implements Command {

	@Override
	public String getName() {
		return "sys default set";
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