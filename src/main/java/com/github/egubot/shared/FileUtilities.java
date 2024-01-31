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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileUtilities {

	public static void writeListToJson(List<?> list, String fileName) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(list, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public static OutputStream getFileOutputStream(String fileName) {
		try {
			return new FileOutputStream(fileName);
		} catch (IOException e) {
			return null;
		}
	}

	public static InputStream getFileInputStream(String fileName) {
		InputStream input;
		File file = new File(fileName);

		if (file.exists()) {
			input = getNewFile(fileName, file);

		} else {

			input = FileUtilities.class.getResourceAsStream("/" + fileName);

			if (input == null) {
				input = getNewFile(fileName, file);
			}
		}

		return input;
	}

	public static <T> List<T> readListFromJson(String fileName, Class<T[]> type) {
		try (InputStream input = getFileInputStream(fileName)) {
			Gson gson = new Gson();
			T[] array = gson.fromJson(new InputStreamReader(input), type);
			if (array != null) {
				return new ArrayList<>(List.of(array));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	private static InputStream getNewFile(String fileName, File file) {
		try {
			file.createNewFile();
			return new FileInputStream(fileName);
		} catch (Exception e1) {
			return null;
		}
	}

	public static String readInputStream(InputStream is) {
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			
		}

		return result.toString();
	}
	
	public static String readInputStream(InputStream is, String appendToLine) {
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line + appendToLine);
			}
		} catch (IOException e) {
			
		}

		return result.toString();
	}

	public static void main(String[] args) {
		//
	}
}
