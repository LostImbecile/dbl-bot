package com.github.egubot.shared;

import java.time.ZoneId;
import java.util.Scanner;

import com.github.egubot.managers.ShutdownManager;
import com.github.egubot.managers.StatusManager;

public class Shared {
	private static ShutdownManager shutdown = new ShutdownManager();
	private static StatusManager status;
	private static Scanner input = new Scanner(System.in);
	private static boolean testMode = false;
	private static boolean dbLegendsMode = true;
	private static String timeZone = null;
	
	private Shared() {
	}

	public static ShutdownManager getShutdown() {
		return shutdown;
	}

	public static void setShutdown(ShutdownManager shutdown) {
		Shared.shutdown = shutdown;
	}

	public static StatusManager getStatus() {
		return status;
	}

	public static void setStatus(StatusManager status) {
		Shared.status = status;
	}

	public static Scanner getSystemInput() {
		return input;
	}

	public static void setInput(Scanner input) {
		Shared.input = input;
	}

	public static boolean isTestMode() {
		return testMode;
	}

	public static void setTestMode(boolean testMode) {
		Shared.testMode = testMode;
	}

	public static boolean isDbLegendsMode() {
		return dbLegendsMode;
	}

	public static void setDbLegendsMode(boolean dbLegendsMode) {
		Shared.dbLegendsMode = dbLegendsMode;
	}

	public static String getTimeZone() {
		return timeZone;
	}

	public static ZoneId getZoneID() {
		if(getTimeZone() == null)
			setTimeZone("Europe/London");
		return ZoneId.of(Shared.getTimeZone());
	}
	public static void setTimeZone(String timeZone) {
		Shared.timeZone = timeZone;
	}

}
