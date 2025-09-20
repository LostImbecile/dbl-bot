package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class BalanceSetCommand implements Command {

	@Override
	public String getName() {
		return "balance set";
	}

	@Override
	public String getDescription() {
		return "Set a user's balance to a specific amount in the server economy";
	}

	@Override
	public String getUsage() {
		return getName() + " <@user> <amount>";
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
			UserBalance userBalance = UserBalanceContext.getServerBalance(msg);
			if(userBalance.setBalance(msg, arguments))
				msg.getChannel().sendMessage("New Balance: $" + userBalance.getBalance(msg));
		} else {
			msg.getChannel().sendMessage("<:huh:1184466187938185286>");
		}
		return true;
	}

}