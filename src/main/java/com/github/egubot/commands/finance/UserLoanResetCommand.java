package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.shared.utils.MessageUtils;

public class UserLoanResetCommand implements Command {

	@Override
	public String getName() {
		return "loan reset";
	}

	@Override
	public String getDescription() {
		return "Reset a user's loan status (admin only)";
	}

	@Override
	public String getUsage() {
		return getName() + " <@user>";
	}

	@Override
	public String getCategory() {
		return "Economy";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (UserInfoUtilities.isPrivilegedOwner(msg)) {
			long userID = msg.getId();
			UserBalance serverData = UserBalanceContext.getServerBalance(msg);

			if (!arguments.isBlank() && !MessageUtils.getPingedUsers(arguments).isEmpty()) {
				userID = Long.parseLong(MessageUtils.getPingedUsers(arguments).get(0));
			}
			UserFinanceData userData = serverData.getUserData(userID);
			userData.setUserLoan(null);
			serverData.setUserData(userID, userData);
			msg.getChannel().sendMessage("User Loan Reset!");
		} else {
			msg.getChannel().sendMessage("<:huh:1184466187938185286>");
		}
		return true;
	}

}