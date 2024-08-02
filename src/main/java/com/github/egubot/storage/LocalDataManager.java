package com.github.egubot.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.interfaces.DataManager;
import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.shared.utils.FileUtilities;

public class LocalDataManager implements DataManager {
	private static final Logger logger = LogManager.getLogger(LocalDataManager.class.getName());
	private List<String> data = Collections.synchronizedList(new ArrayList<String>());
	public static final String STORAGE_FOLDER = "Storage";

	private int lockedDataEndIndex = 0;
	private String dataName;
	private String fileName;

	public LocalDataManager(String dataName) {
		this.dataName = dataName;
		this.fileName = STORAGE_FOLDER + File.separator + dataName.replace(" ", "_") + ".txt";
	}

	@Override
	public void initialise(boolean verbose) {
		readData(null);
		if (verbose)
			StreamRedirector.println("info", "\n" + dataName + " data successfully loaded!\nDate of last update: "
					+ FileUtilities.getFileLastModified(fileName));
	}

	public void writeData(String data) {
		try {
			FileUtilities.writeToFile(data, fileName);
		} catch (Exception e1) {
			logger.error("Failed to write and upload data.", e1);
		}
	}

	@Override
	public void writeData(Messageable e) {
		try {
			FileUtilities.writeToFile(getData(), fileName);
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
	public void readData(Messageable e) {
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
		return FileUtilities.getFileInputStream(fileName, true);
	}

	public int getLockedDataEndIndex() {
		return lockedDataEndIndex;
	}

	public void setLockedDataEndIndex(int lockedDataEndIndex) {
		this.lockedDataEndIndex = lockedDataEndIndex;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = Collections.synchronizedList(data);
	}

}
