package com.github.egubot.commands.finance;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.features.finance.BalanceManager.UserPair;
import com.github.egubot.features.finance.BalanceManager;
import com.github.egubot.features.finance.FinanceEmbedBuilder;
import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.shared.utils.DateUtils;
import com.github.egubot.shared.utils.MessageUtils;

public class UserLoanCommand implements Command {

	@Override
	public String getName() {
		return "loan";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		String[] args = arguments.split(" ");
		if (arguments.isBlank()) {
			sendLoanDetails(msg);
			return true;
		} else if (args.length < 2) {
			sendFormatMessage(msg);
			return true;
		}
		List<String> mentionedUser = MessageUtils.getPingedUsers(args[0]);
		if (mentionedUser.isEmpty()) {
			sendFormatMessage(msg);
			return true;
		}
		long borrowerId = Long.parseLong(mentionedUser.get(0));
		if (borrowerId == msg.getId()) {
			msg.getChannel().sendMessage("<:huh:1184466187938185286>");
			return true;
		}
		double amount = 0;
		try {
			amount = Double.parseDouble(args[1]);
		} catch (Exception e) {
			sendFormatMessage(msg);
			return true;
		}
		if (amount <= 0) {
			msg.getChannel().sendMessage("Invalid Amount!");
			return true;
		}
		double deductionPercent = 0.1;
		try {
			deductionPercent = Double.parseDouble(args[3]);
		} catch (Exception e) {
		}
		if (deductionPercent < 0.1 || deductionPercent > 1) {
			deductionPercent = 0.1;
		}
		String delay = "1d";
		long dueDate;
		try {
			delay = args[2];
		} catch (Exception e) {
		}
		if (!DateUtils.isValidDelay(delay)) {
			delay = "1d";
		}
		dueDate = DateUtils.addDelayStringToEpochMillis(Instant.now().toEpochMilli(), delay);

		UserBalance serverData = UserBalanceContext.getServerBalance(msg);
		if (serverData.getUserData(borrowerId).getUserLoan() != null) {
			msg.getChannel().sendMessage("That user already has a loan!");
			return true;
		}
		if (serverData.getUserData(msg).getBalance() < amount) {
			msg.getChannel().sendMessage("You don't have money to lend them!");
			return true;
		}

		EmbedBuilder confirmationEmbed = new EmbedBuilder().setTitle("Loan Confirmation")
				.setDescription("<@" + borrowerId + "> Do you accept this loan?\n" + "Amount: $" + amount + "\n"
						+ "Due Date: " + DateUtils.epochMillisToDiscordRelativeTimeStamp(dueDate) + "\n"
						+ "Deduction Percent: " + (deductionPercent * 100) + "%")
				.setFooter("React with ✅ to accept the loan within 1 minute");

		double amount2 = amount;
		double deduction2 = deductionPercent;
		msg.getChannel().sendMessage(confirmationEmbed).thenAcceptAsync(confirmationMsg -> {
			confirmationMsg.addReaction("✅");

			confirmationMsg.addReactionAddListener(event -> {
				if (event.getUserId() == borrowerId && event.getEmoji().equalsEmoji("✅")) {
					UserPair userPair = BalanceManager.applyUserLoan(serverData, msg, borrowerId, amount2, dueDate,
							deduction2);
					if (userPair == null) {
						msg.getChannel().sendMessage("Failed to give loan :(");
						return;
					}
					serverData.setUserData(userPair);
					msg.getChannel().sendMessage(
							FinanceEmbedBuilder.buildUserLoanEmbed(serverData.getUserData(borrowerId).getUserLoan()));
				}
			}).removeAfter(1, TimeUnit.MINUTES).addRemoveHandler(() -> {
				msg.getChannel().sendMessage("Loan request timed out or was not accepted.");
			});
		});

		return true;
	}

	private void sendFormatMessage(Message msg) {
		msg.getChannel().sendMessage("""
				**Format**: loan @user amount 0M0w0d0h0s percentDeductionOnOverdue\

				**Example**: loan @user 30 1d 0.5\

				-> User has to pay $30 within 1 day or 50% of his earnings are used to pay 2x(loan) back\

				\

				**Notes**:
				- you only need user and amount, the rest are 1d and 0.1 (10%) by default
				- deduction is 0.1-1 (10%-100%)""");
	}

	private void sendLoanDetails(Message msg) {
		UserBalance serverData = UserBalanceContext.getServerBalance(msg);
		UserFinanceData userData = serverData.getUserData(msg);
		if (userData.getUserLoan() == null) {
			msg.getChannel().sendMessage("You don't have a loan! Request one from a different user.");
		} else {
			msg.getChannel().sendMessage(FinanceEmbedBuilder.buildUserLoanEmbed(userData.getUserLoan()));
		}
	}

}
