package com.weatherapi.forecast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.github.egubot.features.MessageFormats;
import com.github.egubot.main.KeyManager;
import com.github.egubot.shared.JSONUtilities;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class WeatherForecast {
	private static String apiKey = KeyManager.getToken("Weather_API_Key");
	private static String url = "http://api.weatherapi.com/v1/forecast.json?key=" + apiKey;

	public static String getForecastJSON(String days, String city) {
		if (days == null || days.equals("")) {
			days = "3";
		}
		if (city == null || city.equals("")) {
			city = "London";
		}
		days = days.strip();
		city = city.strip();

		String forecastURL = url + "&q=" + city + "&days=" + days;

		try {
			StringBuilder result = new StringBuilder();
			URL url = new URL(forecastURL);

			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {

				String line;
				while ((line = br.readLine()) != null) {
					result.append(line);
				}

			}

			return JSONUtilities.prettify(result.toString());
		} catch (IOException e) {
			return checkErrorCode(e.getMessage());
		}

	}

	private static String checkErrorCode(String errorMessage) {
		String errorCode = errorMessage.replaceAll(".*response code:\\s*(\\d+)(?s).*", "$1");
		switch (errorCode) {
		case "401":
			return "Error: Invalid Authentication.";
		case "403":
			return "Error: Request Unauthorised. Plan limits exceeded";
		case "400":
			return "Error: The server had an error while processing your request.";
		default:
			return "Error: No clue what the problem is, try again later.";
		}
	}

	public static Weather getForecastData(String days, String city) {
		String jsonResponse = getForecastJSON(days, city);

		Gson gson = new Gson();
		WeatherResponse weatherResponse;
		try {
			weatherResponse = gson.fromJson(jsonResponse, WeatherResponse.class);
			return new Weather(weatherResponse);
		} catch (JsonSyntaxException e) {
			return new Weather(jsonResponse);
		}

	}

	public static void main(String[] args) {

		getForecastData("", "paris").printWeather();
	}

	public void sendWeather(Message msg, String lowCaseTxt) {
		String[] args = lowCaseTxt.replace("b-weather","").strip().split(" ");
		String city = args[0];
		String minimal = "";
		if (args.length == 2) {
			minimal = args[1];
		}
		Weather response = WeatherForecast.getForecastData("3", city);
		if (response.isError) {
			msg.getChannel().sendMessage(response.getErrorMessage());
		} else {
			boolean isMinimal = !minimal.equals("detailed");
			EmbedBuilder[] embeds = MessageFormats.createWeatherEmbed(response, isMinimal);
			msg.getChannel().sendMessage(embeds);
		}
	}

}
