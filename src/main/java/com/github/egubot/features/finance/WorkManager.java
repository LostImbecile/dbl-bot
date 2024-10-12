package com.github.egubot.features.finance;

import java.util.Random;

import com.github.egubot.objects.finance.ServerFinanceData;
import com.github.egubot.objects.finance.UserFinanceData;

public class WorkManager {
	private static final Random random = new Random();

	private WorkManager() {
	}

	public static double getReward(ServerFinanceData server) {
		double reward = round(server.getPrizePool() * getRewardFactor());
		if (reward != 0)
			return Math.max(0.1, reward);
		return 0;
	}

	private static double round(double num) {
		return Math.round(num * 10.0) / 10.0;
	}

	public static double getRewardFactor() {
		double rand = random.nextDouble() * 100;

		if (rand < 40.0) {
			return 0.0; // 40% chance
		} else if (rand < 75.0) {
			return 0.0001; // 35% chance
		} else if (rand < 90.0) {
			return 0.0005; // 15% chance
		} else if (rand < 95.0) {
			return 0.001; // 5% chance
		} else if (rand < 98.0) {
			return 0.002; // 3%
		} else if (rand < 99.9) {
			return 0.01; // 1.9%
		} else {
			return 1.0; // 0.1% chance (99.9 + 0.1)
		}
	}

	public static String getRewardMessage(UserFinanceData user, double reward) {
		String message = "";
		double relativeReward = reward / user.getBalance();

		// Messages for 0 reward
		if (reward == 0) {
			String[] zeroRewardMessages = {
					"Well, look at that! You've earned a grand total of **0**! Congratulations on your remarkable talent for achieving absolutely nothing",
					"Ah, the illustrious **0**! You must be a master of the art of doing absolutely nothing.",
					"Oh, the sweet taste of **0**! A reward so insignificant, it’s practically a magician's trick—now you see it, now you don't.",
					"Zero! Zip! Nada! At this rate, you're the undisputed champion of the **Zero Reward League**. Should I send you a trophy made of air?",
					"A round of applause for your spectacular **0**! Truly a feat that defies the laws of productivity. Keep up the excellent non-effort!",
					"Bravo! A magnificent **0**! You’ve outdone yourself this time. They say every journey begins with a single step; but you have exclusively decided to stay put!",
					"Well, would you look at that? A shining **0**.",
					"You have just unlocked the exclusive **‘No Effort**’ achievement :clown:",
					"Perhaps it’s time to write a book on the art of **inaction**?",
					"A glorious **0**, and a lifetime of fame and fortune!",
					"A spectacular **0**! Just think, with that kind of reward, you could almost buy a *single* pixel on a computer screen! Such wealth, much envy!",
					"Congratulations! You might just win the prestigious ‘Nothing Award’! " };
			message = zeroRewardMessages[random.nextInt(zeroRewardMessages.length)];
			return message;
		}

		// Messages for small rewards
		String[] smallRewardMessages = { "Oh, bravo! You’ve snagged a glorious **$" + reward
				+ "**! Truly a masterclass in financial wizardry! At this rate, you might just afford a whisper of a dream!",
				"Well, color me impressed! A princely sum of **$" + reward
						+ "**! At this pace, you’ll be rolling in riches... in approximately forever. Keep it up, or maybe take up knitting instead!",
				"Ah, what do we have here? A bountiful harvest of **$" + reward
						+ "**! That’s like finding a rare Pokémon in the wild—if it were the one that no one wants! Gotta catch 'em all, right?",
				"Look at you, the connoisseur of minuscule rewards! You've miraculously acquired **$" + reward
						+ "**! With that kind of coin, you could practically fund a cup of coffee... someday!",
				"Well, slap my knee and call me surprised! You’ve raked in a whopping **$" + reward
						+ "**! You’re practically a financial mogul at this rate—just a few more decades of this, and you might get a *snack*!",
				"Goodness gracious! A jaw-dropping **$" + reward
						+ "**! With that treasure, you could start a collection... of receipts. Best to keep those dreams alive, my friend!",
				"Oh, you clever little workhorse! **$" + reward
						+ "**! That’s almost enough to buy yourself a nice pencil to doodle your future *dreams*!",
				"You’ve just netted **$" + reward
						+ "**! That’s practically the same as finding an extra french fry at the bottom of the bag—delightful, yet utterly useless!",
				"Look at that! **$" + reward
						+ "**! Enough to give your savings account a slight tickle, but let’s not get too crazy now!",
				"Congratulations! You’ve earned **$" + reward
						+ "**! With rewards like this, you’re one step closer to buying a shiny new *dream*... maybe one with extra pixels!",
				"Splendid! You have a grand total of **$" + reward
						+ "**! At this rate, you could become the world’s leading expert in the science of *almost*!",
				"A majestic **$" + reward
						+ "**! You’re practically swimming in riches... in an inflatable kiddie pool. Dive right in!" };

		// Select message based on relative reward ranges
		if (relativeReward <= 0.0001) {
			message = smallRewardMessages[random.nextInt(2)];
		} else if (relativeReward <= 0.0005) {
			message = smallRewardMessages[random.nextInt(3) + 2];

		} else if (relativeReward <= 0.001) {
			message = smallRewardMessages[random.nextInt(4) + 5];

		} else if (relativeReward <= 0.002) {
			message = smallRewardMessages[random.nextInt(smallRewardMessages.length)];
		} else if (relativeReward <= 0.01) {
			message = smallRewardMessages[random.nextInt(smallRewardMessages.length)];
		} else {
			message = smallRewardMessages[random.nextInt(smallRewardMessages.length)];
		}

		return message;
	}

}