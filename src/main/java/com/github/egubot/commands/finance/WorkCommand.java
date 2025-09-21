package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;
import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.features.finance.WorkManager;
import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.finance.ServerFinanceData;
import com.github.egubot.objects.finance.UserFinanceData;

public class WorkCommand implements Command {

	@Override
	public String getName() {
		return "work";
	}

	@Override
	public String getDescription() {
		return "Work to earn money in the server economy";
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
		ServerFinanceData server = serverData.getServerFinanceData();
		UserFinanceData userData = serverData.getUserData(msg);
		double reward = WorkManager.getReward(server);
		msg.getChannel().sendMessage(WorkManager.getRewardMessage(userData, reward));
		if (reward > 0) {
			userData.setBalance(userData.getBalance() + reward);
			server.addToPrizePool(-reward);
			serverData.setUserData(msg.getId(), userData);
		}

		return true;
	}

}