package com.github.egubot.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.interfaces.UpdatableObjects;

public class DataManagerHandler implements Shutdownable, UpdatableObjects {
	protected static final Logger logger = LogManager.getLogger(DataManagerHandler.class.getName());
	private static List<DataManagerHandler> instances = new ArrayList<>();
	private static boolean isSQLite = !ConfigManager.getBooleanProperty("Is_Storage_Local");
	private BaseDataManager dataManager;
	private String dataName;
	private boolean useSQLite;

	public DataManagerHandler(String dataName, boolean useSQLite) {
		this.dataName = dataName;
		this.useSQLite = useSQLite;
		instances.add(this);
		initialiseDataManager();
	}

	private void initialiseDataManager() {
		if (isSQLite && useSQLite) {
			try {
				dataManager = new SQLiteDataManager(dataName);
			} catch (IOException e) {
				logger.error(e);
				dataManager = new LocalDataManager(dataName);
			}
		} else {
			dataManager = new LocalDataManager(dataName);
		}

		try {
			initialise(false);
		} catch (IOException e) {
			logger.error("Failed to initialise {}", dataName, e);
		}
	}

	private void switchDataManager() {
		BaseDataManager oldManager = dataManager;
		BaseDataManager newManager;

		try {
			if (isSQLite) {
				newManager = new SQLiteDataManager(dataName);
			} else {
				newManager = new LocalDataManager(dataName);
			}

			migrateData(oldManager, newManager);

			// If migration is successful, update the dataManager
			dataManager = newManager;
			oldManager.close();
		} catch (IOException e) {
			logger.error("Failed to switch data manager for {}", dataName, e);
		}
	}

	private void migrateData(BaseDataManager oldManager, BaseDataManager newManager) throws IOException {
		List<String> currentData = oldManager.getData();
		newManager.setData(currentData);
		newManager.writeData(null);

		// Verify data integrity after migration
		List<String> verificationData = newManager.getData();
		if (!currentData.equals(verificationData)) {
			throw new IOException("Data integrity check failed after migration");
		}
	}

	public List<String> getData() {
		return dataManager.getData();
	}

	public void setData(List<String> data) {
		dataManager.setData(data);
	}

	public static boolean switchAllManagers() {
		List<DataManagerHandler> successfulSwitches = new ArrayList<>();
		List<BaseDataManager> oldManagers = new ArrayList<>();

		for (DataManagerHandler handler : instances) {
			if (handler.useSQLite) {
				BaseDataManager oldManager = handler.dataManager;
				try {
					handler.switchDataManager();
					successfulSwitches.add(handler);
					oldManagers.add(oldManager);
				} catch (Exception e) {
					logger.error("Failed to switch manager for {}", handler.dataName, e);
					for (int i = 0; i < successfulSwitches.size(); i++) {
						successfulSwitches.get(i).dataManager = oldManagers.get(i);
					}
					return false; // Exit without changing global isSQLite
				}
			}
		}

		// If all switches were successful, update the global setting
		ConfigManager.setBooleanProperty("Is_Storage_Local", !isSQLite);
		return true;
	}

	public int getLockedDataEndIndex() {
		return dataManager.getLockedDataEndIndex();
	}

	public void setLockedDataEndIndex(int lockedDataEndIndex) {
		dataManager.setLockedDataEndIndex(lockedDataEndIndex);
	}

	public void sendData(Messageable e) {
		dataManager.sendData(e);
	}

	public void writeData(Messageable e) {
		updateDataFromObjects();
		dataManager.writeData(e);
	}

	public void readData() {
		dataManager.readData();
		updateObjects();
	}

	public void initialise(boolean verbose) throws IOException {
		dataManager.initialise(verbose);
		updateObjects();
	}

	@Override
	public void shutdown() {
		dataManager.close();
		instances.remove(this);
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

	@Override
	public void updateObjects() {
		// Implementation depends on your specific needs
	}

	@Override
	public void updateDataFromObjects() {
		// Implementation depends on your specific needs
	}

	public static boolean isSQLite() {
		return isSQLite;
	}

	public static void setSQLite(boolean isSQLite) {
		DataManagerHandler.isSQLite = isSQLite;
	}

	public static void toggleSQLite() {
		isSQLite = !isSQLite;
	}
}