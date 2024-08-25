package com.github.egubot.commands.groq;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class GroqConversationClearCommand implements Command{

	@Override
	public String getName() {
		return "aa clear";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		AIContext.getGroq().clearConversation(msg);
		msg.getChannel().sendMessage("Conversation cleared :thumbsup:");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
