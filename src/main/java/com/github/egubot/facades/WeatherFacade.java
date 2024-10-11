package com.github.egubot.facades;

import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.github.egubot.features.legends.LegendsEmbedBuilder;
import com.weatherapi.forecast.Weather;
import com.weatherapi.forecast.WeatherForecast;

public class WeatherFacade {
	
	private WeatherFacade() {
	}

	public static void sendWeather(Messageable e, String text) {
		String[] args = text.toLowerCase().split(" ");
		String city = args[0];
		String minimal = "";
		if (args.length == 2) {
			minimal = args[1];
		}
		Weather response = WeatherForecast.getForecastData("3", city);
		if (response.isError()) {
			e.sendMessage(response.getErrorMessage());
		} else {
			boolean isMinimal = !minimal.equals("detailed");
			EmbedBuilder[] embeds = createWeatherEmbed(response, isMinimal);
			e.sendMessage(embeds);
		}
	}

	public static EmbedBuilder[] createWeatherEmbed(Weather weather, boolean minimal) {
		if (weather.isNull() || weather.isError())
			return new EmbedBuilder[0];

		EmbedBuilder[] embeds = new EmbedBuilder[3];
		String title = weather.getName() + ", " + weather.getCountry();
		setTodayEmbed(weather, minimal, embeds, title);
		if (weather.getNumDays() > 2) {
			getTomorrowEmbed(weather, minimal, embeds, title);
			if (!minimal) {
				getAfterTomorrowEmbed(weather, embeds, title);
			}
		}
		return embeds;
	}

	private static void getAfterTomorrowEmbed(Weather weather, EmbedBuilder[] embeds, String title) {
		embeds[2] = new EmbedBuilder();
		embeds[2].setAuthor(title, null, weather.getAfterTomorrowIcon());
		embeds[2].setThumbnail(weather.getAfterTomorrowIcon());
		embeds[2].setColor(weather.getAfterTomorrowColor());
		embeds[2].setFooter(weather.getAfterTomorrowDate());
		embeds[2].setDescription(getAfterTomorrowDescription(weather));
	}

	private static void getTomorrowEmbed(Weather weather, boolean minimal, EmbedBuilder[] embeds, String title) {
		embeds[1] = new EmbedBuilder();
		embeds[1].setAuthor(title, null, weather.getTomorrowIcon());
		embeds[1].setThumbnail(weather.getTomorrowIcon());
		embeds[1].setColor(weather.getTomorrowColor());
		embeds[1].setFooter(weather.getTomorrowDate());
		embeds[1].setDescription(getTomorrowDescription(weather, minimal));
	}

	private static void setTodayEmbed(Weather weather, boolean minimal, EmbedBuilder[] embeds, String title) {
		embeds[0] = new EmbedBuilder();
		embeds[0].setAuthor(title, null, weather.getCurrentIcon());
		embeds[0].setThumbnail(weather.getCurrentIcon());
		embeds[0].setColor(weather.getTodayColour());
		embeds[0].setFooter(weather.getTodayDate());
		embeds[0].setDescription(getTodayDescription(weather, minimal));
	}

	private static String getAfterTomorrowDescription(Weather weather) {
		String description;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("After Tomorrow, ");
		stringBuilder.append(weather.getConditionAfterTomorrow());
		stringBuilder.append("\nMax: ");
		stringBuilder.append(weather.getMaxTempAfterTomorrow());
		stringBuilder.append("C\nMin: ");
		stringBuilder.append(weather.getMinTempAfterTomorrow());
		stringBuilder.append("C\nHumidity: ");
		stringBuilder.append(weather.getAfterTomorrowHumidity());
		stringBuilder.append("%\nWind: ");
		stringBuilder.append(weather.getAfterTomorrowWind());
		stringBuilder.append("mph\n\nChance of Rain: ");
		stringBuilder.append(weather.getAfterTomorrowChanceOfRain());
		stringBuilder.append("%\nChance of Snow: ");
		stringBuilder.append(weather.getAfterTomorrowChanceOfSnow());
		stringBuilder.append("%");
		stringBuilder.append(LegendsEmbedBuilder.EQUALISE);
		description = stringBuilder.toString();
		return description;
	}

	private static String getTomorrowDescription(Weather weather, boolean minimal) {
		String description;
		if (!minimal) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Tomorrow, ");
			stringBuilder.append(weather.getConditionTomorrow());
			stringBuilder.append("\nMax: ");
			stringBuilder.append(weather.getMaxTempTomorrow());
			stringBuilder.append("C\nMin: ");
			stringBuilder.append(weather.getMinTempTomorrow());
			stringBuilder.append("C\nHumidity: ");
			stringBuilder.append(weather.getTomorrowHumidity());
			stringBuilder.append("%\nWind: ");
			stringBuilder.append(weather.getTomorrowWind());
			stringBuilder.append("mph\n\nChance of Rain: ");
			stringBuilder.append(weather.getTomorrowChanceOfRain());
			stringBuilder.append("%\nChance of Snow: ");
			stringBuilder.append(weather.getTomorrowChanceOfSnow());
			stringBuilder.append("%");
			stringBuilder.append(LegendsEmbedBuilder.EQUALISE);
			description = stringBuilder.toString();
		} else {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Tomorrow, ");
			stringBuilder.append(weather.getConditionTomorrow());
			stringBuilder.append("\nMax: ");
			stringBuilder.append(weather.getMaxTempTomorrow());
			stringBuilder.append("C, Min: ");
			stringBuilder.append(weather.getMinTempTomorrow());
			stringBuilder.append("C\nWind: ");
			stringBuilder.append(weather.getTomorrowWind());
			stringBuilder.append("mph\nRain: ");
			stringBuilder.append(weather.getTomorrowChanceOfRain());
			stringBuilder.append("%, Snow: ");
			stringBuilder.append(weather.getTomorrowChanceOfSnow());
			stringBuilder.append("%");
			stringBuilder.append(LegendsEmbedBuilder.EQUALISE);
			description = stringBuilder.toString();
		}
		return description;
	}

	private static String getTodayDescription(Weather weather, boolean minimal) {
		String description;
		if (!minimal) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Today ");
			stringBuilder.append(weather.getLocaltime().replaceAll(".* ", ""));
			stringBuilder.append(", ");
			stringBuilder.append(weather.getCurrentConditionText());
			stringBuilder.append("\nNow: ");
			stringBuilder.append(weather.getCurrentTempC());
			stringBuilder.append("C");
			stringBuilder.append("\nFeels Like: ");
			stringBuilder.append(weather.getCurrentFeelslikeC());
			stringBuilder.append("C\nHumidty: ");
			stringBuilder.append(weather.getCurrentHumidity());
			stringBuilder.append("%\nWind: ");
			stringBuilder.append(weather.getCurrentWindMph());
			stringBuilder.append("mph, ");
			stringBuilder.append(weather.getCurrentWindDir());
			stringBuilder.append("\n\nChance of Rain: ");
			stringBuilder.append(weather.getTodayChanceOfRain());
			stringBuilder.append("%\nChance of Snow: ");
			stringBuilder.append(weather.getAfterTomorrowChanceOfSnow());
			stringBuilder.append("%");
			stringBuilder.append(LegendsEmbedBuilder.EQUALISE);
			description = stringBuilder.toString();
		} else {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Today ");
			stringBuilder.append(weather.getLocaltime().replaceAll(".* ", ""));
			stringBuilder.append(", ");
			stringBuilder.append(weather.getCurrentConditionText());
			stringBuilder.append("\nNow: ");
			stringBuilder.append(weather.getCurrentTempC());
			stringBuilder.append("C\nWind: ");
			stringBuilder.append(weather.getCurrentWindMph());
			stringBuilder.append("mph");
			stringBuilder.append("\nRain: ");
			stringBuilder.append(weather.getTodayChanceOfRain());
			stringBuilder.append("%, Snow: ");
			stringBuilder.append(weather.getAfterTomorrowChanceOfSnow());
			stringBuilder.append("%");
			stringBuilder.append(LegendsEmbedBuilder.EQUALISE);
			description = stringBuilder.toString();
		}
		return description;
	}
}
