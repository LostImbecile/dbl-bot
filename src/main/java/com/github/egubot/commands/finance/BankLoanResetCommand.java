package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.finance.UserFinanceData;

public class BankLoanResetCommand implements Command {

	@Override
	public String getName() {
		return "bank loan reset";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (UserInfoUtilities.isPrivilegedOwner(msg)) {
			UserBalance userBalance = UserBalanceContext.getServerBalance(msg);
			UserFinanceData userData = userBalance.getUserData(msg);
			userData.setBankLoan(null);
			userBalance.setUserData(msg, userData);
			msg.getChannel().sendMessage("Bank Loan Reset!");
		} else {
			msg.getChannel().sendMessage("<:huh:1184466187938185286>");
		}
		return true;
	}

}
