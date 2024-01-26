package com.weatherapi.forecast;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
	public Location location;
	public Current current;
	public Forecast forecast;
}

class Location {
	String name;
	String region;
	String country;
	double lat;
	double lon;
	@SerializedName("tz_id")
	String tzId;
	@SerializedName("localtime_epoch")
	long localtimeEpoch;
	String localtime;
}

class Forecast {
	@SerializedName("forecastday")
    List<Forecastday> forecastday;
}

class Current {
	@SerializedName("last_updated_epoch")
	long lastUpdatedEpoch;
	@SerializedName("last_updated")
	String lastUpdated;
	@SerializedName("temp_c")
	double tempC;
	boolean isDay;
	Condition condition;
	@SerializedName("wind_mph")
	double windMph;
	@SerializedName("wind_dir")
	String windDir;
	int humidity;
	@SerializedName("feelslike_c")
	double feelslikeC;
}