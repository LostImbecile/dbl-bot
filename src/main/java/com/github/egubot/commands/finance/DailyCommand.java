package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.features.finance.BalanceManager;
import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.finance.UserFinanceData;

public class DailyCommand implements Command {

	@Override
	public String getName() {
		return "daily";
	}

	@Override
	public String getDescription() {
		return "Earn money by working in the server economy";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "Economy";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		UserBalance serverData = UserBalanceContext.getServerBalance(msg);
		UserFinanceData userOld = serverData.getUserData(msg);
		UserFinanceData userNew = BalanceManager.applyDaily(serverData, msg);
		if (userNew == null)
			msg.getChannel().sendMessage("Already claimed! Wait for daily reset at 00:00 UTC.");
		else {
			serverData.setUserData(msg, userNew);
			msg.getChannel().sendMessage("Claimed $" + (userNew.getBalance() - userOld.getBalance() + "!"));
		}
		return true;
	}

}