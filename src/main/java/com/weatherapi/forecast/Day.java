package com.weatherapi.forecast;

import com.google.gson.annotations.SerializedName;

public class Day {
	@SerializedName("maxtemp_c")
	double maxTempC;
	@SerializedName("maxtemp_f")
	double maxTempF;
	@SerializedName("mintemp_c")
	double minTempC;
	@SerializedName("mintemp_f")
	double minTempF;
	@SerializedName("avgtemp_c")
	double avgTempC;
	@SerializedName("avgtemp_f")
	double avgTempF;
	@SerializedName("maxwind_mph")
	double maxWindMph;
	@SerializedName("maxwind_kph")
	double maxWindKph;
	@SerializedName("totalprecip_mm")
	double totalPrecipMm;
	@SerializedName("totalprecip_in")
	double totalPrecipIn;
	@SerializedName("totalsnow_cm")
	double totalSnowCm;
	@SerializedName("avgvis_km")
	double avgVisKm;
	@SerializedName("avgvis_miles")
	double avgVisMiles;
	@SerializedName("avghumidity")
	int avgHumidity;
	@SerializedName("condition")
	Condition condition;
	@SerializedName("daily_chance_of_rain")
	int chanceOfRain;
	@SerializedName("daily_chance_of_snow")
	int chanceOfSnow;
	double uv;
}
