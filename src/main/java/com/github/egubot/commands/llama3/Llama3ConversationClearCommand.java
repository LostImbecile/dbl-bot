package com.github.egubot.commands.llama3;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.Llama3Context;
import com.github.egubot.interfaces.Command;

public class Llama3ConversationClearCommand implements Command{

	@Override
	public String getName() {
		return "aa clear";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		
		Llama3Context.getConversation().clear();
		msg.getChannel().sendMessage("Conversation cleared :thumbsup:");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
