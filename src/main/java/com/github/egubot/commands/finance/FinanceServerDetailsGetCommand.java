package com.github.egubot.commands.finance;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.features.finance.FinanceEmbedBuilder;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Command;

public class FinanceServerDetailsGetCommand implements Command {

	@Override
	public String getName() {
		return "balance server";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		UserBalance serverData = UserBalanceContext.getServerBalance(msg);
		msg.getChannel().sendMessage(FinanceEmbedBuilder.buildServerDetailsEmbed(serverData.getServerFinanceData(),
				ServerInfoUtilities.getServer(msg)));
		return true;
	}

}
