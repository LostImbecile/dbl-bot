package com.github.egubot.commands.llama3;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.Llama3Context;
import com.github.egubot.interfaces.Command;

public class Llama3GetTokensCommand implements Command {

	@Override
	public String getName() {
		return "aa tokens";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		msg.getChannel().sendMessage("```java\nTokens Used: \"" + Llama3Context.getLastTokens() + "\"```");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
