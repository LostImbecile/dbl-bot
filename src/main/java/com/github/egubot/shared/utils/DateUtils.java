package com.github.egubot.shared.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.egubot.shared.Shared;

public class DateUtils {
	public static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	public static final DateTimeFormatter TIME_ONLY = DateTimeFormatter.ofPattern("H:mm");
	public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd-MM-yyyy, H:mm");
	private static final Pattern DELAY_PATTERN = Pattern.compile("(\\d+)([Mwdhms])");
	private static final Pattern DELAY_VALIDATION_Pattern = Pattern.compile("(?:\\d+[smhdwM]){1,6}");

	public static ZonedDateTime getNowObject() {
		return ZonedDateTime.now(getZoneID());
	}
	
	public static boolean isValidDelay(String delayString) {
	    return DELAY_VALIDATION_Pattern.matcher(delayString).matches();
	}
	
	public static String formatDurationAsDelay(Duration duration) {
		long seconds = duration.toSeconds();
		long days = seconds / (24 * 3600);
		seconds %= (24 * 3600);
		long hours = seconds / 3600;
		seconds %= 3600;
		long minutes = seconds / 60;
		seconds %= 60;
		return String.format("%dw%dd%dh%dm%ds", days / 7, days % 7, hours, minutes, seconds);
	}
	
	public static Duration parseDelayString(String delayString) {
		int months = 0;
		int weeks = 0;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;

		Matcher matcher = DELAY_PATTERN.matcher(delayString);
		while (matcher.find()) {
			int value = Integer.parseInt(matcher.group(1));
			switch (matcher.group(2)) {
			case "M":
				months = value;
				break;
			case "w":
				weeks = value;
				break;
			case "d":
				days = value;
				break;
			case "h":
				hours = value;
				break;
			case "m":
				minutes = value;
				break;
			case "s":
				seconds = value;
				break;
			default:
				seconds = 1;
			}
		}

		Duration totalDuration = Duration.ofDays((long) months * 30 + weeks * 7 + days).plusHours(hours)
				.plusMinutes(minutes).plusSeconds(seconds);
		return totalDuration.compareTo(Duration.ofSeconds(1)) < 0 ? Duration.ofSeconds(1) : totalDuration;
	}
	
	public static long addDurationToEpochMillis(long epochMillis, Duration duration) {
	    return epochMillis + duration.toMillis();
	}

	public static long addDelayStringToEpochMillis(long epochMillis, String durationString) {
	    if (!isValidDelay(durationString)) {
	        throw new IllegalArgumentException("Invalid duration string format.");
	    }
	    Duration duration = parseDelayString(durationString);
	    return addDurationToEpochMillis(epochMillis, duration);
	}

	
	public static String timeLeftAsDelay(long epochMillis) {
	    long currentTimeMillis = System.currentTimeMillis();
	    if (currentTimeMillis >= epochMillis) {
	        return "0";  // Time has passed
	    }
	    Duration remainingDuration = Duration.ofMillis(epochMillis - currentTimeMillis);
	    return formatDurationAsDelay(remainingDuration);
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

	public static long daysSinceEpoch() {
		return Instant.now().toEpochMilli() / 86400000L;
	}

	/**
	 * 
	 * @param firstMillis
	 * @param millis
	 * @return true if first operand is later
	 */
	public static boolean isLater(long firstMillis, long millis) {
		return firstMillis > millis;
	}

	/**
	 * 
	 * @param millis
	 * @return true if time was passed
	 */
	public static boolean hasPassed(long millis) {
		return isLater(Instant.now().toEpochMilli(), millis);
	}

	public static void main(String[] args) {
		System.out.println(getDateTimeNow());
		System.out.println(getDateNow());
		System.out.println(getTimeNow());
	}
}
