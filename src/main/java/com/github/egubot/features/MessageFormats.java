package com.github.egubot.features;

import java.util.List;
import java.util.Random;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.github.egubot.objects.Characters;

public class MessageFormats {

	public static final String EQUALISE = "\n‎‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎   ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎‏‏‎ ‎";

	public static void animateRolledCharacters(List<Characters> pool, Message msg, EmbedBuilder[] embeds,
			int rollAmount) {
		EmbedBuilder[] rollEmbeds;
		Random rng = new Random();
		int randomIndex;

		/*
		 * You can send 5 messages around every second before hitting the rate limit
		 * Number is very inconsistent so you'll never get a smooth animation regardless
		 * of what you do.
		 */
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

		}

		for (int i = 0; i < rollAmount; i++) {
			rollEmbeds = new EmbedBuilder[i + 1];

			for (int j = 0; j < i; j++) {
				rollEmbeds[j] = embeds[j];
			}

			for (int j = 0; j < 4; j++) {
				randomIndex = rng.nextInt(pool.size());

				rollEmbeds[i] = createCharacterEmbed(pool.get(randomIndex));

				msg.edit(rollEmbeds).join();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {

				}
			}
			rollEmbeds[i] = embeds[i];

			msg.edit(rollEmbeds).join();

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {

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
}
