package com.weatherapi.forecast;

import java.awt.Color;
import java.util.List;

import com.github.egubot.logging.StreamRedirector;

public class Weather {
	String name;
	String region;
	String country;
	String localtime;

	double currentTempC;
	String currentConditionText;
	double currentWindMph;
	String currentWindDir;
	int currentHumidity;
	double currentFeelslikeC;
	String currentIcon;
	String todayDate;
	int todayChanceOfRain;
	int todayChanceOfSnow;
	Color todayColour;

	String tomorrowDate;
	double maxTempTomorrow;
	double minTempTomorrow;
	String conditionTomorrow;
	String tomorrowIcon;
	int tomorrowChanceOfRain;
	int tomorrowChanceOfSnow;
	double tomorrowWind;
	int tomorrowHumidity;
	Color tomorrowColor;

	String afterTomorrowDate;
	double maxTempAfterTomorrow;
	double minTempAfterTomorrow;
	String conditionAfterTomorrow;
	String afterTomorrowIcon;
	int afterTomorrowChanceOfRain;
	int afterTomorrowChanceOfSnow;
	double afterTomorrowWind;
	int afterTomorrowHumidity;
	Color afterTomorrowColor;

	List<Hour> hourlyForecast;

	boolean isNull = true;
	boolean isError = false;
	int numDays;

	String errorMessage;

	public Weather(WeatherResponse weatherResponse) {
		if (weatherResponse != null) {
			this.isNull = false;
			setLocation(weatherResponse);
			setCurrent(weatherResponse);

			this.numDays = weatherResponse.forecast.forecastday.size();
			if (numDays > 2) {
				setTomorrow(weatherResponse);
				setAfterTomorrow(weatherResponse);
				setToday(weatherResponse);
			} else {
				setToday(weatherResponse);
			}
		}
	}

	public Weather(String response) {
		this.isError = true;
		this.errorMessage = response;
	}

	private void setToday(WeatherResponse weatherResponse) {
		Forecastday forecastday = weatherResponse.forecast.forecastday.get(0);
		this.todayChanceOfRain = forecastday.day.chanceOfRain;
		this.todayChanceOfSnow = forecastday.day.chanceOfSnow;
		this.hourlyForecast = forecastday.hour;
		this.todayColour = getColourFromTemp(forecastday.day.avgTempC);
		this.todayDate = forecastday.date;
	}

	private void setAfterTomorrow(WeatherResponse weatherResponse) {
		Forecastday forecastday = weatherResponse.forecast.forecastday.get(2);
		this.afterTomorrowDate = forecastday.date;
		this.maxTempAfterTomorrow = forecastday.day.maxTempC;
		this.minTempAfterTomorrow = forecastday.day.minTempC;
		this.conditionAfterTomorrow = forecastday.day.condition.text;
		this.afterTomorrowIcon = "https:" + forecastday.day.condition.icon;
		this.afterTomorrowChanceOfRain = forecastday.day.chanceOfRain;
		this.afterTomorrowChanceOfSnow = forecastday.day.chanceOfSnow;
		this.afterTomorrowColor = getColourFromTemp(forecastday.day.avgTempC);
		this.afterTomorrowHumidity = forecastday.day.avgHumidity;
		this.afterTomorrowWind = forecastday.day.maxWindMph;
	}

	private void setTomorrow(WeatherResponse weatherResponse) {
		Forecastday forecastday = weatherResponse.forecast.forecastday.get(1);
		this.tomorrowDate = forecastday.date;
		this.maxTempTomorrow = forecastday.day.maxTempC;
		this.minTempTomorrow = forecastday.day.minTempC;
		this.conditionTomorrow = forecastday.day.condition.text;
		this.tomorrowIcon = "https:" + forecastday.day.condition.icon;
		this.tomorrowChanceOfRain = forecastday.day.chanceOfRain;
		this.tomorrowChanceOfSnow = forecastday.day.chanceOfSnow;
		this.tomorrowColor = getColourFromTemp(forecastday.day.avgTempC);
		this.tomorrowHumidity = forecastday.day.avgHumidity;
		this.tomorrowWind = forecastday.day.maxWindMph;
	}

	private void setCurrent(WeatherResponse weatherResponse) {
		this.currentTempC = weatherResponse.current.tempC;
		this.currentConditionText = weatherResponse.current.condition.text;

		this.currentWindMph = weatherResponse.current.windMph;
		this.currentWindDir = weatherResponse.current.windDir;
		this.currentHumidity = weatherResponse.current.humidity;
		this.currentFeelslikeC = weatherResponse.current.feelslikeC;
		this.currentIcon = "https:" + weatherResponse.current.condition.icon;
	}

	private void setLocation(WeatherResponse weatherResponse) {
		this.name = weatherResponse.location.name;
		this.region = weatherResponse.location.region;
		this.country = weatherResponse.location.country;
		this.localtime = weatherResponse.location.localtime;
	}

	// method to print the weather information
	public void printWeather() {
		if (isError) {
			StreamRedirector.println("",errorMessage);
			return;
		}
		if (isNull) {
			StreamRedirector.println("","null");
			return;
		}
		StreamRedirector.println("","Name: " + name);
		StreamRedirector.println("","Region: " + region);
		StreamRedirector.println("","Country: " + country);
		StreamRedirector.println("","Localtime: " + localtime);

		StreamRedirector.println("","\nCurrent Weather:");
		StreamRedirector.println("","Temp C: " + currentTempC);
		StreamRedirector.println("","Condition: " + currentConditionText);
		StreamRedirector.println("","Icon: " + currentIcon);
		StreamRedirector.println("","Wind MPH: " + currentWindMph);
		StreamRedirector.println("","Wind Direction: " + currentWindDir);
		StreamRedirector.println("","Humidity: " + currentHumidity);
		StreamRedirector.println("","Feelslike C: " + currentFeelslikeC);

		StreamRedirector.println("","\nTomorrow's Weather:");
		StreamRedirector.println("","Date:" + tomorrowDate);
		StreamRedirector.println("","Max Temp: " + maxTempTomorrow);
		StreamRedirector.println("","Min Temp: " + minTempTomorrow);
		StreamRedirector.println("","Condition: " + conditionTomorrow);

		StreamRedirector.println("","\nTimeline:");
		for (Hour hour : hourlyForecast) {
			StreamRedirector.println("","Time: " + hour.time);
			StreamRedirector.println("","Temp: " + hour.tempC);
			StreamRedirector.println("","Condition: " + hour.condition.text);
			StreamRedirector.println("","Icon: " + hour.condition.icon);
			StreamRedirector.println("","Chance of Rain: " + hour.chanceOfRain);
			StreamRedirector.println("","Chance of Snow: " + hour.chanceOfSnow);
			StreamRedirector.println("","");
		}
	}

	public String getName() {
		return name;
	}

	public String getRegion() {
		return region;
	}

	public String getCountry() {
		return country;
	}

	public String getLocaltime() {
		return localtime;
	}

	public double getCurrentTempC() {
		return currentTempC;
	}

	public String getCurrentConditionText() {
		return currentConditionText;
	}

	public double getCurrentWindMph() {
		return currentWindMph;
	}

	public String getCurrentWindDir() {
		return currentWindDir;
	}

	public int getCurrentHumidity() {
		return currentHumidity;
	}

	public double getCurrentFeelslikeC() {
		return currentFeelslikeC;
	}

	public String getCurrentIcon() {
		return currentIcon;
	}

	public int getTodayChanceOfRain() {
		return todayChanceOfRain;
	}

	public int getTodayChanceOfSnow() {
		return todayChanceOfSnow;
	}

	public String getTomorrowDate() {
		return tomorrowDate;
	}

	public double getMaxTempTomorrow() {
		return maxTempTomorrow;
	}

	public double getMinTempTomorrow() {
		return minTempTomorrow;
	}

	public String getConditionTomorrow() {
		return conditionTomorrow;
	}

	public String getTomorrowIcon() {
		return tomorrowIcon;
	}

	public int getTomorrowChanceOfRain() {
		return tomorrowChanceOfRain;
	}

	public int getTomorrowChanceOfSnow() {
		return tomorrowChanceOfSnow;
	}

	public String getAfterTomorrowDate() {
		return afterTomorrowDate;
	}

	public double getMaxTempAfterTomorrow() {
		return maxTempAfterTomorrow;
	}

	public double getMinTempAfterTomorrow() {
		return minTempAfterTomorrow;
	}

	public String getConditionAfterTomorrow() {
		return conditionAfterTomorrow;
	}

	public String getAfterTomorrowIcon() {
		return afterTomorrowIcon;
	}

	public int getAfterTomorrowChanceOfRain() {
		return afterTomorrowChanceOfRain;
	}

	public int getAfterTomorrowChanceOfSnow() {
		return afterTomorrowChanceOfSnow;
	}

	public List<Hour> getHourlyForecast() {
		return hourlyForecast;
	}

	public boolean isNull() {
		return isNull;
	}

	public int getNumDays() {
		return numDays;
	}

	public Color getTodayColour() {
		return todayColour;
	}

	public Color getTomorrowColor() {
		return tomorrowColor;
	}

	public Color getAfterTomorrowColor() {
		return afterTomorrowColor;
	}

	public Color getColourFromTemp(double temp) {
		Color colour;
		if (temp > 30) {
			colour = Color.RED;
		} else if (temp > 20) {
			colour = Color.ORANGE;
		} else if (temp > 10) {
			colour = Color.YELLOW;
		} else if (temp > 0) {
			colour = Color.CYAN;
		} else {
			colour = Color.LIGHT_GRAY;
		}
		return colour;

	}

	public String getTodayDate() {
		return todayDate;
	}

	public double getTomorrowWind() {
		return tomorrowWind;
	}

	public int getTomorrowHumidity() {
		return tomorrowHumidity;
	}

	public double getAfterTomorrowWind() {
		return afterTomorrowWind;
	}

	public int getAfterTomorrowHumidity() {
		return afterTomorrowHumidity;
	}

	public boolean isError() {
		return isError;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
