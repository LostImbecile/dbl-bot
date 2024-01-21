package com.github.egubot.features;

import java.util.List;
import java.util.Random;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.azure.services.Translate;
import com.github.egubot.objects.Characters;
import com.weatherapi.forecast.Weather;

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

	public static EmbedBuilder[] createWeatherEmbed(Weather weather, boolean minimal) {
		if (weather.isNull() || weather.isError())
			return new EmbedBuilder[0];

		String equalise = EQUALISE + "‎‏‏‎                    ‎‎";
		EmbedBuilder[] embeds = new EmbedBuilder[3];
		String title = weather.getName() + ", " + weather.getCountry();
		embeds[0] = new EmbedBuilder();
		embeds[0].setAuthor(title, null, weather.getCurrentIcon());
		embeds[0].setThumbnail(weather.getCurrentIcon());
		embeds[0].setColor(weather.getTodayColour());
		embeds[0].setFooter(weather.getTodayDate());
		String description;
		if (!minimal) {
			description = "Today " + weather.getLocaltime().replaceAll(".* ", "") + ", "
					+ weather.getCurrentConditionText() + "\nNow: " + weather.getCurrentTempC() + "C" + "\nFeels Like: "
					+ weather.getCurrentFeelslikeC() + "C\nHumidty: " + weather.getCurrentHumidity() + "%\nWind: "
					+ weather.getCurrentWindMph() + "mph, " + weather.getCurrentWindDir() + "\n\nChance of Rain: "
					+ weather.getTodayChanceOfRain() + "%\nChance of Snow: " + weather.getAfterTomorrowChanceOfSnow()
					+ "%" + equalise;
		} else {
			description = "Today " + weather.getLocaltime().replaceAll(".* ", "") + ", "
					+ weather.getCurrentConditionText() + "\nNow: " + weather.getCurrentTempC() + "C\nWind: "
					+ weather.getCurrentWindMph() + "mph" + "\nRain: " + weather.getTodayChanceOfRain() + "%, Snow: "
					+ weather.getAfterTomorrowChanceOfSnow() + "%" + equalise;
		}
		embeds[0].setDescription(description);
		if (weather.getNumDays() > 2) {
			embeds[1] = new EmbedBuilder();
			embeds[1].setAuthor(title, null, weather.getTomorrowIcon());
			embeds[1].setThumbnail(weather.getTomorrowIcon());
			embeds[1].setColor(weather.getTomorrowColor());
			embeds[1].setFooter(weather.getTomorrowDate());
			if (!minimal) {
				description = "Tomorrow, " + weather.getConditionTomorrow() + "\nMax: " + weather.getMaxTempTomorrow()
						+ "C\nMin: " + weather.getMinTempTomorrow() + "C\nHumidity: " + weather.getTomorrowHumidity()
						+ "%\nWind: " + weather.getTomorrowWind() + "mph\n\nChance of Rain: "
						+ weather.getTomorrowChanceOfRain() + "%\nChance of Snow: " + weather.getTomorrowChanceOfSnow()
						+ "%" + equalise;
			} else {
				description = "Tomorrow, " + weather.getConditionTomorrow() + "\nMax: " + weather.getMaxTempTomorrow()
						+ "C, Min: " + weather.getMinTempTomorrow() + "C\nWind: " + weather.getTomorrowWind()
						+ "mph\nRain: " + weather.getTomorrowChanceOfRain() + "%, Snow: "
						+ weather.getTomorrowChanceOfSnow() + "%" + equalise;
			}
			embeds[1].setDescription(description);

			if (!minimal) {
				embeds[2] = new EmbedBuilder();
				embeds[2].setAuthor(title, null, weather.getAfterTomorrowIcon());
				embeds[2].setThumbnail(weather.getAfterTomorrowIcon());
				embeds[2].setColor(weather.getAfterTomorrowColor());
				embeds[2].setFooter(weather.getAfterTomorrowDate());
				description = "After Tomorrow, " + weather.getConditionAfterTomorrow() + "\nMax: "
						+ weather.getMaxTempAfterTomorrow() + "C\nMin: " + weather.getMinTempAfterTomorrow()
						+ "C\nHumidity: " + weather.getAfterTomorrowHumidity() + "%\nWind: "
						+ weather.getAfterTomorrowWind() + "mph\n\nChance of Rain: "
						+ weather.getAfterTomorrowChanceOfRain() + "%\nChance of Snow: "
						+ weather.getAfterTomorrowChanceOfSnow() + "%" + equalise;
				embeds[2].setDescription(description);
			}

		}
		return embeds;
	}

	public static EmbedBuilder[] createTranslateEmbed(Message msg, Translate translate) {
		String content;
		List<Embed> embeds = msg.getEmbeds();
		EmbedBuilder[] translatedEmbeds = new EmbedBuilder[10];
		
		if (!embeds.isEmpty()) {
			Embed embed;
			boolean isError = true;
			for (int i = 0; i < embeds.size() && i < 10; i++) {
				embed = embeds.get(i);
				translatedEmbeds[i] = new EmbedBuilder();

				try {
					content = embed.getDescription().get();
					content = translate.post(content, true);
					translatedEmbeds[i].setDescription(content);
					isError = false;
				} catch (Exception e) {

				}

				try {
					content = embed.getAuthor().get().getName();
					content = translate.post(content, false);
					translatedEmbeds[i].setAuthor(content);
					isError = false;
				} catch (Exception e) {
				}

				try {
					content = embed.getTitle().get();
					content = translate.post(content, false);
					translatedEmbeds[i].setTitle(content);
					isError = false;
				} catch (Exception e) {
				}

				try {
					translatedEmbeds[i].setThumbnail(embed.getThumbnail().get().getUrl().toExternalForm());
				} catch (Exception e) {
				}

				try {
					translatedEmbeds[i].setImage(embed.getImage().get().getUrl().toExternalForm());
				} catch (Exception e) {
				}
				try {
					translatedEmbeds[i].setColor(embed.getColor().get());
				} catch (Exception e) {
				}

			}
			if (isError) {
				msg.getChannel().sendMessage("Error: Translating Embed Failed.");
				return new EmbedBuilder[0];
			}
			return translatedEmbeds;
		} 
		return new EmbedBuilder[0];
	}
}
