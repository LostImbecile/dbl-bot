package com.github.egubot.facades;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.github.egubot.features.MessageFormats;
import com.weatherapi.forecast.Weather;
import com.weatherapi.forecast.WeatherForecast;

public class WeatherFacade {

	public boolean checkCommands(Message msg, String lowCaseTxt) {
		if (lowCaseTxt.matches("b-weather(?s).*")) {

			sendWeather(msg, lowCaseTxt);

			return true;
		}
		return false;
	}

	public static void sendWeather(Message msg, String lowCaseTxt) {
		String[] args = lowCaseTxt.replaceFirst("b-weather", "").strip().split(" ");
		String city = args[0];
		String minimal = "";
		if (args.length == 2) {
			minimal = args[1];
		}
		Weather response = WeatherForecast.getForecastData("3", city);
		if (response.isError()) {
			msg.getChannel().sendMessage(response.getErrorMessage());
		} else {
			boolean isMinimal = !minimal.equals("detailed");
			EmbedBuilder[] embeds = createWeatherEmbed(response, isMinimal);
			msg.getChannel().sendMessage(embeds);
		}
	}

	public static EmbedBuilder[] createWeatherEmbed(Weather weather, boolean minimal) {
		if (weather.isNull() || weather.isError())
			return new EmbedBuilder[0];

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
					+ "%" + MessageFormats.EQUALISE;
		} else {
			description = "Today " + weather.getLocaltime().replaceAll(".* ", "") + ", "
					+ weather.getCurrentConditionText() + "\nNow: " + weather.getCurrentTempC() + "C\nWind: "
					+ weather.getCurrentWindMph() + "mph" + "\nRain: " + weather.getTodayChanceOfRain() + "%, Snow: "
					+ weather.getAfterTomorrowChanceOfSnow() + "%" + MessageFormats.EQUALISE;
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
						+ "%" + MessageFormats.EQUALISE;
			} else {
				description = "Tomorrow, " + weather.getConditionTomorrow() + "\nMax: " + weather.getMaxTempTomorrow()
						+ "C, Min: " + weather.getMinTempTomorrow() + "C\nWind: " + weather.getTomorrowWind()
						+ "mph\nRain: " + weather.getTomorrowChanceOfRain() + "%, Snow: "
						+ weather.getTomorrowChanceOfSnow() + "%" + MessageFormats.EQUALISE;
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
						+ weather.getAfterTomorrowChanceOfSnow() + "%" + MessageFormats.EQUALISE;
				embeds[2].setDescription(description);
			}

		}
		return embeds;
	}
}
