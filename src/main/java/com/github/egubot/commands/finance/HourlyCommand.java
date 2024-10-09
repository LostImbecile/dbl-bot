package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.features.finance.BalanceManager;
import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.finance.UserFinanceData;

public class HourlyCommand implements Command {

	@Override
	public String getName() {
		return "hourly";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		UserBalance serverData = UserBalanceContext.getUserBalance(msg);
		UserFinanceData userOld = serverData.getUserData(msg);
		UserFinanceData userNew = BalanceManager.applyHourly(serverData, msg);
		if (userNew == null)
			msg.getChannel().sendMessage("Already claimed! Wait for next hour to start.");
		else {
			serverData.setUserData(msg, userNew);
			msg.getChannel().sendMessage("Claimed $" + (userNew.getBalance() - userOld.getBalance() + "!"));
		}
		return true;
	}

}
