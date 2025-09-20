package com.github.egubot.commands.chatgpt;

import org.javacord.api.entity.message.Message;

import com.github.egubot.ai.AIModelHandler;
import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class ChatGPTGetModelCommand implements Command {

	@Override
	public String getName() {
		return "gpt model";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		AIModelHandler handler = AIContext.getChatGPT();
		msg.getChannel().sendMessage("```m\n" + handler.getModel().getModelName() + "```");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Get the currently active ChatGPT model information";
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