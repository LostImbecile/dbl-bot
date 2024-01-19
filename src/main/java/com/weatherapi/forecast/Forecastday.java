package com.weatherapi.forecast;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Forecastday {
	String date;
    @SerializedName("date_epoch")
    long dateEpoch;
    Day day;
    Astro astro;
    List<Hour> hour;
}
