package com.github.egubot.facades;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.features.legends.LegendsRoll;
import com.github.egubot.features.legends.LegendsSearch;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.shared.ConvertObjects;
import com.github.egubot.shared.FileUtilities;
import com.github.egubot.shared.Shared;
import com.github.egubot.storage.ConfigManager;
import com.github.egubot.storage.LocalDataManager;

public class LegendsCommandsContext implements Shutdownable {
	private static final Logger logger = LogManager.getLogger(LegendsCommandsContext.class.getName());
	private static boolean backupWebsiteFlag = ConfigManager.getBooleanProperty("Backup_Website_Flag");
	private static String wheelChannelID = KeyManager.getID("Wheel_Channel_ID");

	private static LegendsTemplatesContext templates = null;
	private static LegendsRoll legendsRoll = null;
	private static LegendsSearch legendsSearch = null;
	private static boolean isLegendsMode = Shared.isDbLegendsMode();
	private static boolean isAnimated = !Shared.isTestMode();

	private LegendsCommandsContext() {
	}

	public static void initialise() {
		if (isLegendsMode) {
			// Fetches data from the legends website and initialises
			// classes that are based on it, or doesn't if that fails
			try {
				System.out.println("\nFetching characters from dblegends.net...");
				
				LegendsDatabase.initialise();
				
				backupLegendsWebsite();
			} catch (Exception e) {
				logger.warn("Failed to build character database. Relevant commands will be inactive.");
				logger.error("Failed to build character database.", e);
				isLegendsMode = false;
			}

			if (isLegendsMode) {
				try {
					LegendsTemplatesContext.initialise();
				} catch (IOException e) {
					isLegendsMode = false;
					logger.error("Failed to initialise roll templates.", e);
					return;
				}

				legendsRoll = new LegendsRoll(LegendsTemplatesContext.getRollTemplates());

				legendsSearch = new LegendsSearch(LegendsTemplatesContext.getRollTemplates());
			}
		}
	}

	private static void backupLegendsWebsite() throws IOException {
		if (LegendsDatabase.isDataFetchSuccessfull()) {

			if (backupWebsiteFlag) {
				System.out.println("Character database was successfully built!");
				// Upload current website HTML as backup
				saveLegendsWebsiteBackup();
			} else {
				System.out.println("Character database was successfully built!");
			}

		} else {
			logger.warn("Character database missing information. Trying Backup...");
			getLegendsWebsiteBackup();
		}
	}

	private static void getLegendsWebsiteBackup() throws IOException {
		if (FileUtilities.isFileExist("Website_Backup.txt")) {
			LocalDataManager backup = new LocalDataManager("Website Backup");
			backup.initialise(false);
			LegendsDatabase.initialise(ConvertObjects.listToText(backup.getData()));
			if (!LegendsDatabase.isDataFetchSuccessfull()) {
				logger.warn("Backup is also missing information.");
			}
		} else {
			logger.warn("There is no backup.");
			throw new IOException();
		}
	}

	public static void saveLegendsWebsiteBackup() {
		try {
			LocalDataManager backup = new LocalDataManager("Website Backup");
			backup.writeData(FileUtilities.readURL(LegendsDatabase.WEBSITE_URL));

		} catch (Exception e) {
			logger.error("Failed to save website backup", e);
		}
	}

	@Override
	public void shutdown() {
		if (templates != null)
			templates.shutdown();
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

	public static boolean isBackupWebsiteFlag() {
		return backupWebsiteFlag;
	}

	public static String getWheelChannelID() {
		return wheelChannelID;
	}

	public static LegendsTemplatesContext getTemplates() {
		return templates;
	}

	public static LegendsRoll getLegendsRoll() {
		return legendsRoll;
	}

	public static LegendsSearch getLegendsSearch() {
		return legendsSearch;
	}

	public static boolean isLegendsMode() {
		return isLegendsMode;
	}

	public static boolean isAnimated() {
		return isAnimated;
	}
	
	public static void toggleIsAnimated() {
		isAnimated = !isAnimated;
	}

	public static void setAnimated(boolean isAnimated) {
		LegendsCommandsContext.isAnimated = isAnimated;
	}
}
