package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.features.finance.BalanceManager;
import com.github.egubot.features.finance.BankLoanProcessor;
import com.github.egubot.features.finance.FinanceEmbedBuilder;
import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.finance.UserFinanceData;

public class BankLoanCommand implements Command {

	@Override
	public String getName() {
		return "bank loan";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (arguments.isBlank()) {
			sendLoanDetails(msg);
			return true;
		}
		double amount = getAmount(arguments);
		if (amount <= 0) {
			msg.getChannel().sendMessage("Invalid Amount!");
			return true;
		}
		UserBalance serverData = UserBalanceContext.getServerBalance(msg);
		UserFinanceData userOld = serverData.getUserData(msg);
		UserFinanceData userNew = BalanceManager.applyBankLoan(serverData, msg, amount);
		if (userNew == null) {
			sendLoanFailedMessage(msg, amount, userOld);
		} else {
			serverData.setUserData(msg, userNew);
			msg.getChannel().sendMessage(FinanceEmbedBuilder.buildBankLoanEmbed(userNew.getBankLoan()));
		}
		return true;
	}

	private void sendLoanDetails(Message msg) {
		UserBalance serverData = UserBalanceContext.getServerBalance(msg);
		UserFinanceData user = serverData.getUserData(msg);
		if (user.getBankLoan() == null) {
			int maxAmount = BankLoanProcessor.calculateMaxLoanAmount(user.getCreditScore());
			int minAmount = BankLoanProcessor.calculateMinLoanAmount(user.getCreditScore());
			msg.getChannel().sendMessage(
					"You don't have a loan! Request one with `bank loan <amount>`\nRange for your credit score: $"
							+ minAmount + " - $" + maxAmount);
		} else {
			msg.getChannel().sendMessage(FinanceEmbedBuilder.buildBankLoanEmbed(user.getBankLoan()));
		}
	}

	private void sendLoanFailedMessage(Message msg, double amount, UserFinanceData userOld) {
		if (userOld.getBankLoan() == null) {
			String st = "Can't loan $" + amount + "!";
			int maxAmount = BankLoanProcessor.calculateMaxLoanAmount(userOld.getCreditScore());
			int minAmount = BankLoanProcessor.calculateMinLoanAmount(userOld.getCreditScore());
			st += " Must be within $" + minAmount + " - $" + maxAmount + ".";
			msg.getChannel().sendMessage(st);
		} else {
			msg.getChannel().sendMessage("You already have a loan!");
		}
	}

	private double getAmount(String arguments) {
		try {
			return Double.parseDouble(arguments);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

}
