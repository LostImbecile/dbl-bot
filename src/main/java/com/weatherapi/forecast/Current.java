package com.weatherapi.forecast;

import com.google.gson.annotations.SerializedName;

public class Current {
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
