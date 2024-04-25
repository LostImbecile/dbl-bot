package com.github.egubot.commands.chatgpt;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.ChatGPTContext;
import com.github.egubot.interfaces.Command;

public class ChatgptConversationClearCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "gpt clear";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if(!ChatGPTContext.isAIOn())
			return false;
		
		ChatGPTContext.getConversation().clear();
		msg.getChannel().sendMessage("Conversation cleared :thumbsup:");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
