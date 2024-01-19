package com.weatherapi.forecast;

import com.google.gson.annotations.SerializedName;

public class Hour {
	@SerializedName("time_epoch")
    long timeEpoch;
    String time;
    @SerializedName("temp_c")
    double tempC;
    @SerializedName("temp_f")
    double tempF;
    @SerializedName("is_day")
    int isDay;
    Condition condition;
    @SerializedName("wind_mph")
    double windMph;
    @SerializedName("wind_kph")
    double windKph;
    @SerializedName("wind_degree")
    int windDegree;
    @SerializedName("wind_dir")
    String windDir;
    @SerializedName("pressure_mb")
    double pressureMb;
    @SerializedName("pressure_in")
    double pressureIn;
    @SerializedName("precip_mm")
    double precipMm;
    @SerializedName("precip_in")
    double precipIn;
    @SerializedName("snow_cm")
    double snowCm;
    int humidity;
    int cloud;
    @SerializedName("feelslike_c")
    double feelslikeC;
    @SerializedName("feelslike_f")
    double feelslikeF;
    @SerializedName("windchill_c")
    double windchillC;
    @SerializedName("windchill_f")
    double windchillF;
    @SerializedName("heatindex_c")
    double heatindexC;
    @SerializedName("heatindex_f")
    double heatindexF;
    @SerializedName("dewpoint_c")
    double dewpointC;
    @SerializedName("dewpoint_f")
    double dewpointF;
    @SerializedName("will_it_rain")
    int willItRain;
    @SerializedName("chance_of_rain")
    int chanceOfRain;
    @SerializedName("will_it_snow")
    int willItSnow;
    @SerializedName("chance_of_snow")
    int chanceOfSnow;
    @SerializedName("vis_km")
    double visKm;
    @SerializedName("vis_miles")
    double visMiles;
    @SerializedName("gust_mph")
    double gustMph;
    @SerializedName("gust_kph")
    double gustKph;
    double uv;
    @SerializedName("short_rad")
    double shortRad;
    @SerializedName("diff_rad")
    double diffRad;
}
