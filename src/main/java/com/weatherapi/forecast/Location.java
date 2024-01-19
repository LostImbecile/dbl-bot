package com.weatherapi.forecast;

import com.google.gson.annotations.SerializedName;

public class Location {
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
