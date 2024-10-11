package com.github.egubot.features.finance;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.github.egubot.build.UserBalance;
import com.github.egubot.facades.UserBalanceContext;
import com.github.egubot.features.finance.BalanceManager.UserPair;
import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.shared.utils.DateUtils;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;

public class RouletteWheel {
	private static final Random random = new Random();
	public static final String RED = "🔴";
	public static final String BLACK = "⚫";
	public static final String GREEN = "🟢";

	private RouletteWheel() {
	}

	public static String spin() {
		int result = random.nextInt(37); // 0 to 36 (Green: 0, Red: 1-18, Black: 19-36)
		if (result == 0) {
			return GREEN;
		} else if (result <= 18) {
			return RED;
		} else {
			return BLACK;
		}
	}

	private static String getResultMessage(String result) {
		String msg = "The Roullette Landed on **";
		msg += getColourName(result);
		msg += "** " + result + "!";
		return msg;
	}

	private static String getColourName(String result) {
		switch (result) {
		case RED:
			return "RED";
		case BLACK:
			return "BLACK";
		case GREEN:
			return "GREEN";
		default:
			return null;
		}
	}

	public static double handleIndividualBet(Message message, double amount, String color) {
		String result = spin();
		message.getChannel().sendMessage(getResultMessage(result));
		double reward = 0;
		if (result.equals(color)) {
			reward = amount * 2;
		}

		return reward;
	}

	public static void handleGroupBet(Message message, List<User> redUsers, List<User> blackUsers,
			List<User> greenUsers, double amount) {
		String result = spin();
		StringBuilder resultMessage = new StringBuilder();
		message.edit(getResultMessage(result));

		double rewardFactor = 0;
		double[] rewardFactors = calculateRewardFactor(redUsers.size(), blackUsers.size(), greenUsers.size());
		List<User> winners = null;
		Map<String, List<User>> losers = new HashMap<>();
		String winningColor;

		switch (result) {
		case RED:
			winners = redUsers;
			rewardFactor = rewardFactors[0];
			winningColor = getColourName(RED);
			losers.put(getColourName(BLACK), blackUsers);
			losers.put(getColourName(GREEN), greenUsers);
			break;
		case BLACK:
			winners = blackUsers;
			rewardFactor = rewardFactors[1];
			winningColor = getColourName(BLACK);
			losers.put(getColourName(RED), redUsers);
			losers.put(getColourName(GREEN), greenUsers);
			break;
		default:
			winners = greenUsers;
			rewardFactor = rewardFactors[2];
			winningColor = getColourName(GREEN);
			losers.put(getColourName(RED), redUsers);
			losers.put(getColourName(BLACK), blackUsers);
			break;
		}

		UserBalance serverData = UserBalanceContext.getServerBalance(message);

		for (User winner : winners) {
			double reward = amount * rewardFactor;
			resultMessage.append(String.format("%s won on **%s** :partying_face:%nNew balance: $%.1f%n",
					winner.getMentionTag(), winningColor, updateWin(serverData, winner, reward)));
		}

		for (Map.Entry<String, List<User>> entry : losers.entrySet()) {
			String losingColor = entry.getKey();
			List<User> colorLosers = entry.getValue();
			for (User loser : colorLosers) {
				resultMessage.append(String.format("%s lost on **%s** :clown:%nNew balance: $%.1f%n",
						loser.getMentionTag(), losingColor, updateLoss(serverData, loser, amount)));
			}
		}

		message.getChannel().sendMessage(resultMessage.toString());
	}

	public static double[] calculateRewardFactor(int redUsers, int blackUsers, int greenUsers) {
		int totalBettors = redUsers + blackUsers + greenUsers;
		double baseReward = 2.0;
		double smallAdjustment = 0.1;
		double largeAdjustment = 0.2;

		double redFactor = baseReward;
		double blackFactor = baseReward;
		double greenFactor = 14.0;

		if (totalBettors <= 1) {
			return new double[] { redFactor, blackFactor, greenFactor };
		}

		// Adjust rewards based on bet distribution
		if (redUsers > 0)
			redFactor -= smallAdjustment * (redUsers - 1);
		if (blackUsers > 0)
			blackFactor -= smallAdjustment * (blackUsers - 1);
		if (greenUsers > 0)
			greenFactor -= smallAdjustment * (greenUsers - 1);

		// Boost rewards for colors with fewer bets
		if (redUsers < blackUsers || redUsers < greenUsers)
			redFactor += smallAdjustment;
		if (blackUsers < redUsers || blackUsers < greenUsers)
			blackFactor += smallAdjustment;
		if (greenUsers < redUsers || greenUsers < blackUsers)
			greenFactor += largeAdjustment;

		// Additional boost for the color with the least bets
		if (redUsers < blackUsers && redUsers < greenUsers)
			redFactor += largeAdjustment;
		else if (blackUsers < greenUsers)
			blackFactor += largeAdjustment;
		else
			greenFactor += largeAdjustment;

		// Ensure rewards don't go below 1.8x or 13x
		redFactor = round(Math.max(redFactor, 1.8));
		blackFactor = round(Math.max(blackFactor, 1.8));
		greenFactor = round(Math.max(greenFactor, 13));

		return new double[] { redFactor, blackFactor, greenFactor };
	}

	private static double round(double value) {
		return Math.round(value * 10.0) / 10.0;
	}

	private static double updateWin(UserBalance serverData, User user, double reward) {
		UserPair userPair = BalanceManager.updateBalance(serverData, user.getId(), reward);
		serverData.setUserData(userPair);
		return userPair.getUser1().getBalance();
	}

	private static double updateLoss(UserBalance serverData, User user, double loss) {
		UserFinanceData userData = BalanceManager.registerLoss(serverData, user.getId(), loss);
		serverData.setUserData(user.getId(), userData);
		return userData.getBalance();
	}

	public static void sendEmbedWithReactions(Messageable channel, double amount) {
		EmbedBuilder embed = new EmbedBuilder().setTitle("Choose a color")
				.setDescription("React with Red, Black, or Green");

		String timestamp = DateUtils.epochMillisToDiscordRelativeTimeStamp(
				DateUtils.addDelayStringToEpochMillis(Instant.now().toEpochMilli(), "30s"));
		channel.sendMessage(timestamp, embed).thenAcceptAsync(message -> {
			List<User> redUsers = new ArrayList<>();
			List<User> blackUsers = new ArrayList<>();
			List<User> greenUsers = new ArrayList<>();

			message.addReactions(RED, BLACK, GREEN);

			ReactionAddListener reactionAddListener = event -> {
				User user = event.requestUser().join();
				if (user.isBot())
					return;

				switch (event.getEmoji().asUnicodeEmoji().orElse("")) {
				case RED:
					if (!RouletteWheel.addToList(amount, message, redUsers, user))
						return;
					break;
				case BLACK:
					if (!RouletteWheel.addToList(amount, message, blackUsers, user))
						return;
					break;
				case GREEN:
					if (!RouletteWheel.addToList(amount, message, greenUsers, user))
						return;
					break;
				default:
					return;
				}
				RouletteWheel.updateEmbed(message, redUsers.size(), blackUsers.size(), greenUsers.size());
			};

			ReactionRemoveListener reactionRemoveListener = event -> {
				User user = event.requestUser().join();
				if (user.isBot())
					return;

				switch (event.getEmoji().asUnicodeEmoji().orElse("")) {
				case RED:
					if (redUsers.contains(user)) {
						redUsers.remove(user);
						retractUserBalance(amount, message, user);
					} else
						return;
					break;
				case BLACK:
					if (blackUsers.contains(user)) {
						blackUsers.remove(user);
						retractUserBalance(amount, message, user);
					} else
						return;
					break;
				case GREEN:
					if (greenUsers.contains(user)) {
						greenUsers.remove(user);
						retractUserBalance(amount, message, user);
					} else
						return;
					break;
				default:
					return;
				}
				RouletteWheel.updateEmbed(message, redUsers.size(), blackUsers.size(), greenUsers.size());
			};

			message.addReactionRemoveListener(reactionRemoveListener).removeAfter(30, TimeUnit.SECONDS);
			message.addReactionAddListener(reactionAddListener).removeAfter(30, TimeUnit.SECONDS)
					.addRemoveHandler(() -> {
						message.edit("");
						handleGroupBet(message, redUsers, blackUsers, greenUsers, amount);
					});
		});
	}

	private static boolean addToList(double amount, Message message, List<User> userList, User user) {
		userList.add(user);
		return RouletteWheel.removeUserBalance(amount, message, user);
	}

	private static boolean removeUserBalance(double amount, Message message, User user) {
		UserBalance serverData = UserBalanceContext.getServerBalance(message);
		UserFinanceData userData = serverData.getUserData(user.getId());
		if (userData.getBalance() >= amount) {
			userData = BalanceManager.applyBalanceUse(serverData, user.getId(), amount);
			serverData.setUserData(user.getId(), userData);
			return true;
		} else {
			message.getChannel().sendMessage(user.getMentionTag() + " You don't have enough balance!");
			return false;
		}
	}

	private static void updateEmbed(Message message, int redCount, int blackCount, int greenCount) {
		double[] rewardFactor = calculateRewardFactor(redCount, blackCount, greenCount);
		EmbedBuilder updatedEmbed = new EmbedBuilder().setTitle("Reward").setDescription(
				"🔴: " + rewardFactor[0] + "x\n⚫: " + rewardFactor[1] + "x\n🟢: " + rewardFactor[2] + "x");
		message.edit(updatedEmbed);
	}

	private static void retractUserBalance(double amount, Message message, User user) {
		UserBalance serverData = UserBalanceContext.getServerBalance(message);
		UserFinanceData userData = BalanceManager.applyBalanceRetract(serverData, user.getId(), amount);
		serverData.setUserData(user.getId(), userData);
	}
}
