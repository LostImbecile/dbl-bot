package com.github.egubot.commands.gemini;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class GeminiGetModelCommand implements Command {

	@Override
	public String getName() {
		return "gem model";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		msg.getChannel().sendMessage("```m\n" + AIContext.getGemini().getModel().getModelName() + "```");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Get the currently active Gemini AI model information";
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