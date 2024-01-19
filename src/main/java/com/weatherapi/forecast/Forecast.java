package com.weatherapi.forecast;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Forecast {
	@SerializedName("forecastday")
    List<Forecastday> forecastday;
}
