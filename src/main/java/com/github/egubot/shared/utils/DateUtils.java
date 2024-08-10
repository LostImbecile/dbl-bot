package com.github.egubot.shared.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.github.egubot.shared.Shared;

public class DateUtils {
	public static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	public static final DateTimeFormatter TIME_ONLY = DateTimeFormatter.ofPattern("H:mm");
	public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd-MM-yyyy, H:mm");

	public static ZonedDateTime getNowObject() {
		return ZonedDateTime.now(getZoneID());
	}

	public static ZoneId getZoneID() {
		if (Shared.getTimeZone() != null)
			return Shared.getZoneID();
		return ZoneId.of("Europe/London");
	}

	public static String getDateNow() {
		return getDate(getNowObject());
	}

	public static String getTimeNow() {
		return getTime(getNowObject());
	}

	public static String getDateTimeNow() {
		return getDateTime(getNowObject());
	}

	public static String getDate(ZonedDateTime time) {
		return time.format(DATE_ONLY);
	}

	public static String getTime(ZonedDateTime time) {
		return time.format(TIME_ONLY);
	}

	public static String getDateTime(ZonedDateTime time) {
		return time.format(DATE_TIME);
	}

	public static String getDate(Instant instant) {
		ZonedDateTime time = instant.atZone(getZoneID());
		return time.format(DATE_ONLY);
	}

	public static String getTime(Instant instant) {
		ZonedDateTime time = instant.atZone(getZoneID());
		return time.format(TIME_ONLY);
	}

	public static String getDateTime(Instant instant) {
		ZonedDateTime time = instant.atZone(getZoneID());
		return time.format(DATE_TIME);
	}

	public static String getTime(long millis) {
		return getTime(Instant.ofEpochMilli(millis));
	}

	public static String getDateTime(long millis) {
		return getDateTime(Instant.ofEpochMilli(millis));
	}

	public static String getDate(long millis) {
		return getDate(Instant.ofEpochMilli(millis));
	}

	public static void main(String[] args) {
		System.out.println(getDateTimeNow());
		System.out.println(getDateNow());
		System.out.println(getTimeNow());
	}
}
