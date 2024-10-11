package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.features.finance.BalanceManager;
import com.github.egubot.features.finance.BalanceManager.UserPair;
import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.finance.UserFinanceData;

public class BankLoanRepayCommand implements Command {

	@Override
	public String getName() {
		return "bank loan pay";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		double amount = 0;
		try {
			amount = Double.parseDouble(arguments);
		} catch (Exception e) {
			msg.getChannel().sendMessage("Invalid Amount!");
			return true;
		}
		UserBalance userBalance = UserBalanceContext.getServerBalance(msg);
		UserFinanceData userData = userBalance.getUserData(msg);
		if (userData.getBankLoan() == null)
			msg.getChannel().sendMessage("Already paid off!");
		else {
			if (userData.getBalance() < amount) {
				msg.getChannel().sendMessage("You don't have enough balance!");
				return true;
			}
			UserPair userPair = BalanceManager.transferMoney(userBalance, msg, 0, amount);
			if (userPair == null) {
				msg.getChannel().sendMessage("Loan can't be repayed yet. Use the money first.");
				return true;
			}
			userBalance.setUserData(userPair);
			String st = "Transferred $" + amount + " to bank!";
			if (userPair.getUser1().getBankLoan() == null)
				st += " Loan is paid off!";
			else
				st += " $" + userPair.getUser1().getBankLoan().getAmount() + " remaining.";

			msg.getChannel().sendMessage(st);
		}
		return true;
	}

}
