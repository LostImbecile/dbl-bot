package com.github.egubot.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.shared.utils.FileUtilities;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LocalDataManager extends BaseDataManager {
	private static final Logger logger = LogManager.getLogger(LocalDataManager.class.getName());

	public LocalDataManager(String dataName) {
		super(dataName);
		this.filePath = STORAGE_FOLDER + File.separator +  dataName.replace(" ", "_") + ".txt";
		this.fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
	}

	@Override
	public void initialise(boolean verbose) {
		readData();
		if (verbose)
			StreamRedirector.println("info", "\n" + dataName + " data successfully loaded!\nDate of last update: "
					+ FileUtilities.getFileLastModified(filePath));
	}

	public void writeDataToFile(String data) {
		try {
			FileUtilities.writeToFile(data, filePath);
		} catch (Exception e1) {
			logger.error("Failed to write and upload data.", e1);
		}
	}
	
	@Override
    public <T> void writeJSON(String key, T object) {
        String json = new Gson().toJson(object);
        FileUtilities.writeToFile(json, STORAGE_FOLDER + File.separator + key + ".json");
    }

    @Override
    public <T> T readJSON(String key, Class<T> type) {
        String json = FileUtilities.readFile(STORAGE_FOLDER + File.separator + key + ".json");
		return new Gson().fromJson(json, type);
    }

    @Override
    public <T> List<T> readJSONList(String key, TypeToken<List<T>> typeToken) {
        String json = FileUtilities.readFile(STORAGE_FOLDER + File.separator + key + ".json");
		return new Gson().fromJson(json, typeToken.getType());
    }

	@Override
	public void writeData(Messageable e) {
		try {
			FileUtilities.writeToFile(getData(), filePath);
			if (e != null)
				e.sendMessage("Updated <:drink:1184466286944735272>");
		} catch (Exception e1) {
			if (e != null)
				e.sendMessage("Couldn't update <:sad:1020780174901522442>");

			logger.error("Failed to write and upload data.", e1);
		}
	}

	@Override
	public void sendData(Messageable e) {
		try {
			e.sendMessage(FileUtilities.toBufferedInputStream(getData()), fileName).join();
		} catch (Exception e1) {
			e.sendMessage("Failed");
			logger.error("Failed to send data online.", e1);
		}
	}

	@Override
	public void readData() {
		try (BufferedReader br = FileUtilities.getBufferedReader(getInputStream())) {
			String st;

			data.clear();
			Pattern lockedDataPattern = Pattern.compile("(?i)lockeddataindex=(\\d+)$");

			while ((st = br.readLine()) != null) {
				st = st.strip();

				if (st.isBlank())
					break;
				if (lockedDataPattern.matcher(st).matches()) {
					try {
						this.lockedDataEndIndex = Integer.parseInt(st.replaceAll("\\D", ""));
					} catch (Exception e1) {
					}
				}

				data.add(st);
			}
		} catch (IOException e1) {
			logger.error("Reading local data failed.", e1);
		}
	}

	private InputStream getInputStream() {
		return FileUtilities.getFileInputStream(filePath, true);
	}

	@Override
	public void setData(List<String> data) {
		this.data = Collections.synchronizedList(data);
	}

	@Override
	public void close() {
		writeData(null);
	}

}
