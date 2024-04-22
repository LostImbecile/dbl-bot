package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.Llama3Context;
import com.github.egubot.interfaces.Command;

public class LLama3Command implements Command {

	@Override
	public String getName() {
		return "llm";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		return Llama3Context.respond(msg, arguments);
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
