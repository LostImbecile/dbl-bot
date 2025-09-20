package com.github.egubot.commands.gemini;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class GeminiConversationClearCommand implements Command {

	@Override
	public String getName() {
		return "gem clear";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		AIContext.getGemini().clearConversation(msg);
		msg.getChannel().sendMessage("Conversation cleared :thumbsup:");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Clear the current Gemini AI conversation history and context";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "AI";
	}
}