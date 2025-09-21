package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.DefaultSystemPromptContext;
import com.github.egubot.interfaces.Command;
import com.github.egubot.main.Bot;

public class ReloadDefaultSystemPromptCommand implements Command {

	@Override
	public String getName() {
		return "sys default reload";
	}

	@Override
	public String getDescription() {
		return "Reload the default system prompt from the configuration file";
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
		return PermissionLevel.OWNER;
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!msg.getAuthor().asUser().isPresent()) {
			return false;
		}
		
		if (!msg.getAuthor().asUser().get().equals(Bot.getOwnerUser())) {
			msg.getChannel().sendMessage("Only the bot owner can reload the default system prompt.");
			return true;
		}
		
		DefaultSystemPromptContext.reloadDefaultPrompt();
		msg.getChannel().sendMessage("Default system prompt reloaded from storage.");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}