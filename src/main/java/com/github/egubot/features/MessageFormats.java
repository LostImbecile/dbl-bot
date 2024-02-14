package com.github.egubot.features;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.objects.Characters;
import com.github.egubot.objects.SummonBanner;

public class MessageFormats {
	// This replaces spaces with an invisible character
	public static final String EQUALISE = String.format("%n%80s", "‏‏‎ ").replace(" ", "\u2005");
	public static final String INLINE_EQUALISE = String.format("%n%35s", "‏‏‎ ").replace(" ", "\u2005");
	private static Random rng = new Random();

	private MessageFormats() {
	}

	public static void animateRolledCharacters(List<Characters> pool, Message msg, EmbedBuilder[] embeds,
			int rollAmount) {
		EmbedBuilder[] rollEmbeds;
		int randomIndex;

		/*
		 * You can send 5 messages around every second before hitting the rate limit
		 * Number is very inconsistent so you'll never get a smooth animation regardless
		 * of what you do.
		 */
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();

		}

		for (int i = 0; i < rollAmount; i++) {
			rollEmbeds = Arrays.copyOf(embeds, i + 1);

			for (int j = 0; j < 4; j++) {
				randomIndex = rng.nextInt(pool.size());

				rollEmbeds[i] = createCharacterEmbed(pool.get(randomIndex));

				msg.edit(rollEmbeds).join();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			rollEmbeds[i] = embeds[i];

			msg.edit(rollEmbeds).join();

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

		}

		msg.edit("Finished <a:saikyo:792521951100796958><a:da:792522031601287218>", embeds);

	}

	public static EmbedBuilder createCharacterEmbed(Characters unit) {

		String lfStatus;
		String zenkaiStatus;
		String coolerStatus;

		if (unit.isZenkai())
			zenkaiStatus = " (Zenkai)";
		else
			zenkaiStatus = "";

		if (unit.isLF())
			lfStatus = " (LF)";
		else
			lfStatus = "";

		if (unit.getCharacterName().toLowerCase().contains("cooler") && !unit.getRarity().equals("EXTREME"))
			coolerStatus = " (Strongest)";
		else
			coolerStatus = "";

		return new EmbedBuilder().setThumbnail(unit.getImageLink()).setColor(unit.getColour())
				.setDescription(unit.getRarity() + lfStatus + zenkaiStatus + coolerStatus + EQUALISE)
				.setAuthor(unit.getCharacterName(), unit.getPageLink(), unit.getImageLink())
				.setFooter(unit.getGameID());

	}

	public static List<EmbedBuilder> buildSummonCharacterEmbeds(Map<Integer, Double> oneRotation,
			Map<Integer, Double> threeRotation, Map<Integer, Double> customRotation,
			Map<Integer, Integer> focusCharacters) {
		List<EmbedBuilder> embeds = new ArrayList<>(focusCharacters.size());
		EmbedBuilder temp;
		int numberOfRotations = 0;
		if (customRotation != null)
			numberOfRotations = (int) Math.ceil(customRotation.get(-1));

		String rateFormat = "%.1f%%";

		for (Entry<Integer, Integer> entry : focusCharacters.entrySet()) {
			int id = entry.getKey();
			Characters character = LegendsDatabase.getCharacterHash().get(id);
			temp = createCharacterEmbed(character);

			double rate = oneRotation.get(id);

			temp.addInlineField("One Rotation", getCharacterRatesField(rateFormat, rate, entry.getValue()));

			rate = threeRotation.get(id);

			temp.addInlineField("Three Rotations", getCharacterRatesField(rateFormat, rate, entry.getValue()));

			if (customRotation != null) {
				rate = customRotation.get(id);

				temp.addInlineField(numberOfRotations + " Rotations",
						getCharacterRatesField(rateFormat, rate, entry.getValue()));

			}
			embeds.add(temp);
		}
		return embeds;
	}

	private static String getCharacterRatesField(String rateFormat, double rate, int zPower) {
		int red2Pulls = LegendsSummonRates.getRed2PullsNeeded(zPower);
		int sevenStarPulls = LegendsSummonRates.getSevenStarsPullsNeeded(zPower);
		double getRedTwoChance = LegendsSummonRates.getConsecutiveChance(red2Pulls, rate);
		double getSevenStarChance = LegendsSummonRates.getConsecutiveChance(sevenStarPulls, rate);
		return "Once: " + String.format(rateFormat, rate * 100) + "\n7 Star: "
				+ String.format(rateFormat, getSevenStarChance * 100) + "\nRed 2: "
				+ String.format(rateFormat, getRedTwoChance * 100) + INLINE_EQUALISE;
	}

	public static EmbedBuilder buildSummonTotalEmbed(Map<String, Double> oneRotationTotal,
			Map<String, Double> threeRotationTotal, Map<String, Double> customRotationTotal, int[] rotationCosts,
			SummonBanner banner) {

		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Total Chance");
		embed.setColor(Color.CYAN);
		embed.setFooter("Fields exclude each other.");
		embed.setDescription(banner.getTitle() + EQUALISE);
		embed.setThumbnail(banner.getImageURL());
		String rateFormat = "%.1f%%";

		StringBuilder description = new StringBuilder();
		for (Entry<String, Double> entry : oneRotationTotal.entrySet()) {
			double rate = entry.getValue() * 100;
			if (rate < 0.5)
				continue;

			description.append(entry.getKey() + ": " + String.format(rateFormat, rate) + "\n");
		}
		description.append("\nCost: " + String.format("%,d", (rotationCosts[0] + rotationCosts[1])));
		embed.addInlineField("One Rotation", description.toString() + INLINE_EQUALISE);

		// reset
		description.setLength(0);

		for (Entry<String, Double> entry : threeRotationTotal.entrySet()) {
			double rate = entry.getValue() * 100;
			if (rate < 0.5)
				continue;

			description.append(entry.getKey() + ": " + String.format(rateFormat, rate) + "\n");
		}
		description.append("\nCost: " + String.format("%,d", (rotationCosts[0] + 3 * rotationCosts[1])));
		embed.addInlineField("Three Rotations", description.toString() + INLINE_EQUALISE);

		description.setLength(0);

		if (customRotationTotal != null) {
			// Entry -1 has number of rotations
			int numberOfRotations = (int) Math.ceil(customRotationTotal.get("-1"));
			for (Entry<String, Double> entry : customRotationTotal.entrySet()) {
				double rate = entry.getValue() * 100;
				if (rate < 0.5 || entry.getKey().equals("-1"))
					continue;

				description.append(entry.getKey() + ": " + String.format(rateFormat, rate) + "\n");
			}
			description.append(
					"\nCost: " + String.format("%,d", (rotationCosts[0] + numberOfRotations * rotationCosts[1])));
			embed.addInlineField(numberOfRotations + " Rotations", description.toString() + INLINE_EQUALISE);
		}

		return embed;
	}
}
