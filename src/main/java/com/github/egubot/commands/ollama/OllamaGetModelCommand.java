package com.github.egubot.commands.ollama;

import org.javacord.api.entity.message.Message;

import com.github.egubot.ai.AIModelHandler;
import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class OllamaGetModelCommand implements Command {

	@Override
	public String getName() {
		return "qq model";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		AIModelHandler handler = AIContext.getOllama();
		msg.getChannel().sendMessage("```m\n" + handler.getModel().getModelName() + "```");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}
}
