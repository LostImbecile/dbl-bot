package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class SetTransferLimitCommand implements Command {

	@Override
	public String getName() {
		return "transfer set";
	}

	@Override
	public String getDescription() {
		return "Set the transfer limit for users in the server economy";
	}

	@Override
	public String getUsage() {
		return getName() + " <amount>";
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
		if (!arguments.isBlank() && UserInfoUtilities.isPrivilegedOwner(msg)) {
			UserBalance serverData = UserBalanceContext.getServerBalance(msg);
			double amount = 0;
			try {
				amount = Double.parseDouble(arguments);
			} catch (NumberFormatException e) {
				msg.getChannel().sendMessage("Invalid Amount!");
				return true;
			}
			if (amount < 0) {
				msg.getChannel().sendMessage("Invalid Amount!");
				return true;
			}
			serverData.getServerFinanceData().setBaseTransferLimit(amount);
			msg.getChannel().sendMessage("New Transfer Limit: $" + serverData.getServerFinanceData().getBaseTransferLimit());
		} else {
			msg.getChannel().sendMessage("<:huh:1184466187938185286>");
		}
		return true;
	}

}