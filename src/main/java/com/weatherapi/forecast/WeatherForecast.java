package com.weatherapi.forecast;

import java.io.IOException;
import java.net.URL;
import com.github.egubot.main.KeyManager;
import com.github.egubot.shared.FileUtilities;
import com.github.egubot.shared.JSONUtilities;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class WeatherForecast {
	private static String apiKey = KeyManager.getToken("Weather_API_Key");
	private static String url = "http://api.weatherapi.com/v1/forecast.json?key=" + apiKey;

	public static String getForecastJSON(String days, String city) {
		if (days == null || days.isBlank()) {
			days = "3";
		}
		if (city == null || city.isBlank()) {
			city = "London";
		}
		days = days.strip();
		city = city.strip();

		String forecastURL = url + "&q=" + city + "&days=" + days;

		try {
			URL url = new URL(forecastURL);
			String result = FileUtilities.readInputStream(url.openStream());

			return JSONUtilities.prettify(result);
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

}
