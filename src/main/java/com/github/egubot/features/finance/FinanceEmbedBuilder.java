package com.github.egubot.features.finance;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

import com.github.egubot.objects.finance.ServerFinanceData;
import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.objects.finance.UserFinanceData.BankLoan;
import com.github.egubot.objects.finance.UserFinanceData.UserLoan;
import com.github.egubot.shared.utils.ConvertObjects;
import com.github.egubot.shared.utils.DateUtils;
import com.github.egubot.shared.utils.FileUtilities;

public class FinanceEmbedBuilder {
	// This replaces spaces with an invisible character
	public static final String EQUALISE = String.format("%n%80s", "‏‏‎ ").replace(" ", "\u2005");
	public static final String INLINE_EQUALISE = String.format("%n%35s", "‏‏‎ ").replace(" ", "\u2005");
	private static BufferedImage bankIcon;
	private static BufferedImage dollarIcon;

	static {
		try {
			bankIcon = FileUtilities.loadImageFromResources("bank.png");
		} catch (IOException e) {
			bankIcon = null;
		}
		try {
			dollarIcon = FileUtilities.loadImageFromResources("dollar.png");
		} catch (Exception e) {
			dollarIcon = null;
		}
	}

	private FinanceEmbedBuilder() {
	}

	public static EmbedBuilder buildUserDetailsEmbed(UserFinanceData userData, ServerFinanceData server,
			MessageAuthor user) {
		String earnings = "Total: $" + userData.getTotalEarnings() + "\nToday: $" + userData.getEarningsSum()
				+ "\nAvg Today: $" + userData.getDailyAverageEarnings();
		String losses = "Total: $" + userData.getTotalLosses() + "\nToday: $" + userData.getLossesSum()
				+ "\nAvg Today: $" + userData.getDailyAverageLosses();
		String claims = "Daily: $" + DailyClaimManager.calculateAmount(userData, server) + "\nHourly: $"
				+ HourlyClaimManager.calculateAmount(userData, server);
		String transfers = "Remaining: $"
				+ TransferLimitInterceptor.calculateTransferLimit(userData, server.getBaseTransferLimit())
				+ "\nTransferred Today: $" + userData.getDailyTransferred();
		EmbedBuilder embed = new EmbedBuilder().addInlineField("Balance", "$" + userData.getBalance())
				.addInlineField("Credit Score", userData.getCreditScore() + " points")
				.addField("Recurring Claims", claims).addField("User Transfer Details", transfers)
				.addInlineField("Earnings", earnings).addInlineField("Losses", losses);
		if (user != null)
			embed.setAuthor(user);
		if (!userData.getLastTransaction().isEmpty())
			embed.addField("Last Transaction", ConvertObjects.listToText(userData.getLastTransaction(), "\n"));

		return embed;
	}

	public static EmbedBuilder buildBankLoanEmbed(BankLoan bankLoan) {
		String amount = bankLoan.getOriginalAmount() + " Loan";
		String creditScoreGain = (bankLoan.getCreditScoreGainOnRepayment() >= 0 ? "+" : "-")
				+ bankLoan.getCreditScoreGainOnRepayment();
		String status = "Active & waiting for repayment.";
		if (bankLoan.isOverdue())
			status = "Overdue; 0-2 points of credit score will be deducted on each win along with 10% of earnings.";
		else if (bankLoan.leftBeforeAllowingPayback() > 0)
			status = "$" + bankLoan.leftBeforeAllowingPayback() + " have to be used before repayment is allowed.";
		return new EmbedBuilder().setAuthor(amount, null, dollarIcon)
				.addField("Amount With Interest", "$" + bankLoan.getAmount())
				.addField("Due Date", DateUtils.epochMillisToDiscordTimeStamp(bankLoan.getDueDate()))
				.addField("Interest Rate", bankLoan.getInterestRate() + "%")
				.addField("Credit Score Gain", creditScoreGain).setThumbnail(bankIcon).addField("Status", status);
	}

	public static EmbedBuilder buildServerDetailsEmbed(ServerFinanceData serverData, Server server) {
		String stats = "Total won: $" + serverData.getTotalWon() + "\nTotal Lost: $" + serverData.getTotalLost()
				+ "\nPrize Pool: $" + serverData.getPrizePool();
		String baseValues = "Daily: $" + serverData.getBaseDaily() + "\nHourly: $" + serverData.getBaseHourly()
				+ "\nBase Transfer Limit: $" + serverData.getBaseTransferLimit();
		return new EmbedBuilder().setAuthor(server.getName(), null, server.getIcon().orElse(null))
				.addField("Stats", stats).addField("Base Values", baseValues);
	}

	public static EmbedBuilder buildUserLoanEmbed(UserLoan userLoan) {
		return new EmbedBuilder().addField("Lender", "<@" + userLoan.getLenderId() + ">")
				.addField("Remaining Amount", "$" + userLoan.getAmount())
				.addField("Due Date:", DateUtils.epochMillisToDiscordRelativeTimeStamp(userLoan.getDueDate()))
				.addField("Earning Deduction on Overdue", (userLoan.getPenaltyRate() * 100) + "%").addField("Status",
						userLoan.isAppliedPenalty() ? "Overdue. Amount to be paid was doubled." : "Active.");
	}

}
