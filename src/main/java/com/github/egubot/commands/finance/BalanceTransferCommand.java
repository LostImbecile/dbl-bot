package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.features.finance.BalanceManager;
import com.github.egubot.features.finance.BalanceManager.UserPair;
import com.github.egubot.features.finance.TransferLimitInterceptor;
import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.finance.UserFinanceData.UserLoan;
import com.github.egubot.shared.utils.MessageUtils;

public class BalanceTransferCommand implements Command {

	@Override
	public String getName() {
		return "transfer";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (arguments.isBlank() || arguments.split(" ").length != 2
				|| MessageUtils.getPingedUsers(arguments).isEmpty()) {
			msg.getChannel().sendMessage("Please provide a user and an amount.");
			return true;
		}

		UserBalance serverData = UserBalanceContext.getServerBalance(msg);

		String receiver = MessageUtils.getPingedUsers(arguments).get(0);
		String[] args = arguments.split(" ");

		if (args[0].contains(receiver))
			arguments = args[1];
		else
			arguments = args[0];
		long recieverId = Long.parseLong(receiver);
		if (recieverId == msg.getAuthor().getId()) {
			msg.getChannel().sendMessage("<:huh:1184466187938185286>");
			return true;
		}
		try {
			UserPair userPair = BalanceManager.transferMoney(serverData, msg, recieverId,
					Double.parseDouble(arguments));
			if (userPair == null) {
				double senderLimit = TransferLimitInterceptor.calculateTransferLimit(serverData.getUserData(msg),
						serverData.getServerFinanceData().getBaseTransferLimit());
				double receiverLimit = TransferLimitInterceptor.calculateTransferLimit(
						serverData.getUserData(recieverId), serverData.getServerFinanceData().getBaseTransferLimit());
				if (receiverLimit < senderLimit) {
					msg.getChannel().sendMessage(String.format(
							"This transfer exceeds the remaining $%s limit of <@%s>.", receiverLimit, receiver));
				} else {
					msg.getChannel()
							.sendMessage(String.format("This transfer exceeds your remaining $%s limit.", senderLimit));
				}
				return true;
			}

			serverData.setUserData(userPair);

			String st = "Transfered $" + arguments + " to <@" + receiver + ">";
			if (userPair.getTransferType().equals(UserPair.TRANSFER_TYPE_USER_LOAN_REPAYMENT)) {
				st += " as loan repayment!";
				UserLoan userLoan = userPair.getUser1().getUserLoan();
				if (userLoan != null)
					st += "\nRemaining: $" + userLoan.getAmount();
				else
					st += " Loan is paid off!";
			} else
				st += "!\nNew Balance: " + userPair.getUser1().getBalance();
			msg.getChannel().sendMessage(st);
		} catch (NumberFormatException e) {
			msg.getChannel().sendMessage("Please provide a valid amount.");
		}
		return true;
	}

}
