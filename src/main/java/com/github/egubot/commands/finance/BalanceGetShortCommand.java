package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;
import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.interfaces.Command;

public class BalanceGetShortCommand implements Command {

	@Override
	public String getName() {
		return "balance short";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		UserBalance serverData = UserBalanceContext.getServerBalance(msg);
		msg.getChannel().sendMessage("Balance: $" + serverData.getBalance(msg));
		return true;
	}

}
