package com.github.egubot.storage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigManager {
	private static final Logger logger = LogManager.getLogger(ConfigManager.class.getName());
	private static final String CONFIG_FILE_PATH = "config.properties";
	private static final Properties properties = new Properties();

	// Load configuration properties from file
	static {
		try {
			FileInputStream fileInputStream = new FileInputStream(CONFIG_FILE_PATH);
			properties.load(fileInputStream);
			fileInputStream.close();
		} catch (Exception e) {
			// I don't want anything happening here
		}
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static int getIntProperty(String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			return -1;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public static boolean getBooleanProperty(String key) {
		String value = properties.getProperty(key);
		return Boolean.parseBoolean(value);
	}

	public static double getDoubleProperty(String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			return -1;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public static void setProperty(String key, String value) {
		properties.setProperty(key, value);
		savePropertiesToFile();
	}

	public static void setIntProperty(String key, int value) {
		properties.setProperty(key, String.valueOf(value));
		savePropertiesToFile();
	}

	public static void setBooleanProperty(String key, boolean value) {
		properties.setProperty(key, String.valueOf(value));
		savePropertiesToFile();
	}

	public static void setDoubleProperty(String key, double value) {
		properties.setProperty(key, String.valueOf(value));
		savePropertiesToFile();
	}

	private static void savePropertiesToFile() {
		try (FileOutputStream fileOutputStream = new FileOutputStream(CONFIG_FILE_PATH)) {
			properties.store(fileOutputStream, null);
		} catch (IOException e) {
			logger.error("Couldn't write to config file.", e);
		}
	}

	public static void main(String[] args) {
		System.out.println(getBooleanProperty("Is_Storage_Online"));
	}
}
