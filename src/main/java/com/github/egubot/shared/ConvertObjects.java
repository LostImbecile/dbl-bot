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
}
