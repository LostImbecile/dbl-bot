package com.github.egubot.commands.ai;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.DefaultSystemPromptContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class GetDefaultSystemPromptCommand implements Command {

	@Override
	public String getName() {
		return "sys default get";
	}

	@Override
	public String getDescription() {
		return "Get the current default system prompt for AI interactions";
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
		if (!UserInfoUtilities.isOwner(msg)) {
			return false;
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