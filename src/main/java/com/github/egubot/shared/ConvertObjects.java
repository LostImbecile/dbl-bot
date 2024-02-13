package com.github.egubot.shared;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ConvertObjects {
	private ConvertObjects() {
	}

	public static String listToText(List<String> data) {
		return String.join(" ", data);
	}

	public static String listToText(List<String> data, String joinLinesWith) {
		return String.join(joinLinesWith, data);
	}

	public static List<String> textToList(String jsonData) {
		return new ArrayList<>(List.of(jsonData.split("\n")));
	}

	public static String instantToString(Instant instant) {
		String[] date = instant.toString().split("[Tz.]");
		return date[0] + ", " + date[1].substring(0, date[1].length() - 3);
	}

	public static String convertMilliSecondsToTime(long ms) {
		ms = ms / 1000;
		long hours = ms / 3600;
		long minutes = (ms % 3600) / 60;
		long remainingSeconds = ms % 60;
		if (hours > 0) {
			return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
		} else if (minutes > 0) {
			return String.format("%02d:%02d", minutes, remainingSeconds);
		} else {
			return String.format("%02ds", remainingSeconds);
		}
	}
}
