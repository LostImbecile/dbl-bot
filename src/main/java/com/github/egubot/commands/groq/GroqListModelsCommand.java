package com.github.egubot.commands.groq;

import java.util.List;

import org.javacord.api.entity.message.Message;

import com.github.egubot.ai.AIModelHandler;
import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.utils.ConvertObjects;

public class GroqListModelsCommand implements Command {

	@Override
	public String getName() {
		return "aa list";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		AIModelHandler handler = AIContext.getGroq();
		List<String> list = handler.getModelList();
		if (list.isEmpty())
			msg.getChannel().sendMessage("No models available");
		else
			msg.getChannel().sendMessage("```m\n" + ConvertObjects.listToText(list, "\n") + "```");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}
}
