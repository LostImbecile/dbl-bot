package com.github.egubot.commands.finance;

import java.util.List;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.features.finance.FinanceEmbedBuilder;
import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.utils.MessageUtils;

public class BalanceGetCommand implements Command {

	@Override
	public String getName() {
		return "balance";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		UserBalance serverData = UserBalanceContext.getServerBalance(msg);
		MessageAuthor author = msg.getAuthor();
		long id = msg.getAuthor().getId();
		if (!arguments.isBlank()) {
			List<String> mentioned = MessageUtils.getPingedUsers(arguments);
			if (!mentioned.isEmpty()) {
				id = Long.parseLong(mentioned.get(0));
				author = null;
			}
		}

		msg.getChannel().sendMessage(FinanceEmbedBuilder.buildUserDetailsEmbed(serverData.getUserData(id),
				serverData.getServerFinanceData(), author));
		return true;
	}

}
