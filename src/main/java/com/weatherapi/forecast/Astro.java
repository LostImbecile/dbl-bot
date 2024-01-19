package com.weatherapi.forecast;

import com.google.gson.annotations.SerializedName;

public class Astro {
    String sunrise;
    String sunset;
    @SerializedName("moonrise")
    String moonRise;
    @SerializedName("moonset")
    String moonSet;
    @SerializedName("moon_phase")
    String moonPhase;
    @SerializedName("moon_illumination")
    int moonIllumination;
    @SerializedName("is_moon_up")
    int isMoonUp;
    @SerializedName("is_sun_up")
    int isSunUp;
}
