package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.features.finance.BalanceManager;
import com.github.egubot.features.finance.BalanceManager.UserPair;
import com.github.egubot.features.finance.RouletteWheel;
import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.finance.UserFinanceData;

public class RouletteWheelCommand implements Command {

	@Override
	public String getName() {
		return "bet";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		double amount = 0;
		boolean isAll = false;

		if (arguments.contains("all")) {
			isAll = true;
		} else {
			try {
				amount = Double.parseDouble(arguments.split("\\s+")[0]);
			} catch (Exception e) {
				msg.getChannel().sendMessage("Invalid amount!");
				return true;
			}
		}
		arguments = arguments.toLowerCase();

		if (arguments.contains("red") || arguments.contains("black") || arguments.contains("green")) {
			String color = arguments.split("\\s+")[1];
			switch (color) {
			case "red":
				color = RouletteWheel.RED;
				break;
			case "black":
				color = RouletteWheel.BLACK;
				break;
			case "green":
				color = RouletteWheel.GREEN;
				break;
			default:
				msg.getChannel().sendMessage("Format: bet amount red/black/green");
				return true;
			}

			UserBalance serverData = UserBalanceContext.getServerBalance(msg);
			UserFinanceData userData = serverData.getUserData(msg);

			if (isAll) {
				amount = userData.getBalance();
			}
			if (!BalanceManager.canUseAmount(userData, amount)) {
				msg.getChannel().sendMessage("You don't have enough balance!");
				return true;
			}

			userData = BalanceManager.applyBalanceUse(serverData, msg, amount);
			double reward = RouletteWheel.handleIndividualBet(msg, amount, color);
			serverData.setUserData(msg, userData);
			if (reward > 0) {
				UserPair userPair = BalanceManager.updateBalance(serverData, msg, reward);
				serverData.setUserData(userPair);
				msg.getChannel().sendMessage("You won! :partying_face:\nNew balance: $" + serverData.getBalance(msg));
			} else {
				msg.getChannel().sendMessage("You lost the bet :clown:\nNew balance: $" + userData.getBalance());
				userData = BalanceManager.registerLoss(serverData, msg, amount);
				serverData.setUserData(msg, userData);
			}
		} else if (!isAll) {
			RouletteWheel.sendEmbedWithReactions(msg.getChannel(), amount);
		} else {
			msg.getChannel().sendMessage("Invalid for this type of bet");
		}
		return true;
	}
}
