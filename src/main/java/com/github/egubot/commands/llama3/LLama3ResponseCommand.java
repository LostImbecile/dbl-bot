package com.github.egubot.commands.llama3;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.Llama3Context;
import com.github.egubot.interfaces.Command;

public class LLama3ResponseCommand implements Command {

	@Override
	public String getName() {
		return "aa";
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
