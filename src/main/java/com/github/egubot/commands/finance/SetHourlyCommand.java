package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class SetHourlyCommand implements Command {

	@Override
	public String getName() {
		return "hourly set";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!arguments.isBlank() && UserInfoUtilities.isPrivilegedOwner(msg)) {
			UserBalance serverData = UserBalanceContext.getServerBalance(msg);
			int amount = 0;
			try {
				amount = Integer.parseInt(arguments);
			} catch (NumberFormatException e) {
				msg.getChannel().sendMessage("Invalid Amount!");
				return true;
			}
			if (amount < 0) {
				msg.getChannel().sendMessage("Invalid Amount!");
				return true;
			}
			serverData.getServerFinanceData().setBaseHourly(amount);
			msg.getChannel().sendMessage("New Hourly: $" + serverData.getServerFinanceData().getBaseHourly());
		} else {
			msg.getChannel().sendMessage("<:huh:1184466187938185286>");
		}
		return true;
	}

}
