package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.interfaces.Command;

public class BalanceGetCommand implements Command {

	@Override
	public String getName() {
		return "balance";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		msg.getChannel().sendMessage("Your balance is: " + UserBalanceContext.getUserBalance(msg).getBalance(msg));
		return true;
	}

}
