package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.DefaultSystemPromptContext;
import com.github.egubot.main.Bot;
import com.github.egubot.interfaces.Command;

public class GetDefaultSystemPromptCommand implements Command {

	@Override
	public String getName() {
		return "sys default get";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!msg.getAuthor().asUser().isPresent()) {
			return false;
		}
		
		if (!msg.getAuthor().asUser().get().equals(Bot.getOwnerUser())) {
			msg.getChannel().sendMessage("Only the bot owner can view the default system prompt.");
			return true;
		}
		
		String defaultPrompt = DefaultSystemPromptContext.getDefaultSystemPrompt();
		String truncated = defaultPrompt.length() > 1800 ? defaultPrompt.substring(0, 1800) + "..." : defaultPrompt;
		msg.getChannel().sendMessage("Current default system prompt:\n```\n" + truncated + "\n```");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}