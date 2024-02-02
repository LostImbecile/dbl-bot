package com.github.egubot.shared;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileUtilities {
	protected static final Logger logger = LogManager.getLogger(FileUtilities.class.getName());

	public static void writeListToJson(List<?> list, String fileName) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(list, writer);
		} catch (IOException e) {
			logger.error("Failed to write list to file.", e);
		}
	}

	public static OutputStream getFileOutputStream(String fileName) {
		try {
			return new FileOutputStream(fileName);
		} catch (IOException e) {
			logger.error("Failed to get output stream.", e);
			return null;
		}
	}

	public static InputStream getFileInputStream(String fileName, boolean createFileIfNotFound) {
		InputStream input;
		File file = new File(fileName);

		if (file.exists()) {
			input = getNewFile(file);

		} else {

			input = FileUtilities.class.getResourceAsStream("/" + fileName);

			if (input == null && createFileIfNotFound) {
				input = getNewFile(file);
			}
		}

		return input;
	}

	public static <T> List<T> readListFromJson(String fileName, Class<T[]> type) {
		try (InputStream input = getFileInputStream(fileName, true)) {
			Gson gson = new Gson();
			T[] array = gson.fromJson(new InputStreamReader(input), type);
			if (array != null) {
				return new ArrayList<>(List.of(array));
			}
		} catch (IOException e) {
			logger.error("Failed to read list from file.", e);
		}
		return new ArrayList<>();
	}

	private static InputStream getNewFile(File file) {
		try {
			if (!file.createNewFile()) {
				if (file.exists())
					return new FileInputStream(file);
				return null;
			} else {
				return new FileInputStream(file);
			}
		} catch (Exception e) {
			logger.error("Failed to create new file.", e);
			return null;
		}
	}

	public static String readInputStream(InputStream is) {
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			logger.error("Failed to read input stream.", e);
		}

		return result.toString();
	}

	public static String readInputStream(InputStream is, String appendToLine) {
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line + appendToLine);
			}
		} catch (IOException e) {
			logger.error("Failed to read input stream.", e);
		}

		return result.toString();
	}

	public static InputStream toInputStream(String st) {
		return IOUtils.toInputStream(st, StandardCharsets.UTF_8);
	}

	public static InputStream toInputStream(List<String> stringList) {
		String concatenatedString = String.join("\n", stringList);
		return toInputStream(concatenatedString);
	}

	public static void main(String[] args) {
		//
	}
}
