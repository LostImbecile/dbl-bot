package com.github.egubot.commands.chatgpt;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class ChatgptConversationClearCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "gpt clear";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		AIContext.getGpt3().clearConversation(msg);
		msg.getChannel().sendMessage("Conversation cleared :thumbsup:");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
