package com.github.egubot.facades;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.build.RollTemplates;
import com.github.egubot.features.legends.LegendsRoll;
import com.github.egubot.features.legends.LegendsSearch;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.managers.NewsFeedManager;
import com.github.egubot.objects.legends.LegendsNewsPiece;
import com.github.egubot.shared.Shared;
import com.github.egubot.shared.utils.ConvertObjects;
import com.github.egubot.shared.utils.FileUtilities;
import com.github.egubot.storage.ConfigManager;
import com.github.egubot.storage.LocalDataManager;

public class LegendsCommandsContext implements Shutdownable {
	private static final Logger logger = LogManager.getLogger(LegendsCommandsContext.class.getName());
	private static boolean backupWebsiteFlag = ConfigManager.getBooleanProperty("Backup_Website_Flag");
	private static String wheelChannelID = KeyManager.getID("Wheel_Channel_ID");

	private static boolean isLegendsMode = Shared.isDbLegendsMode();
	private static boolean isAnimated = !Shared.isTestMode();

	private LegendsCommandsContext() {
	}

	public static void initialise() {
		if (isLegendsMode) {
			// Fetches data from the legends website and initialises
			// classes that are based on it, or doesn't if that fails
			try {
				StreamRedirector.println("info", "\nFetching characters from dblegends.net...");

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
				}
			}

		}

		LegendsNewsContext.initialise();
	}

	private static void backupLegendsWebsite() throws IOException {
		if (LegendsDatabase.isDataFetchSuccessfull()) {

			if (backupWebsiteFlag) {
				StreamRedirector.println("info", "Character database was successfully built!");
				// Upload current website HTML as backup
				saveLegendsWebsiteBackup();
			} else {
				StreamRedirector.println("info", "Character database was successfully built!");
			}

		} else {
			logger.warn("Character database missing information. Trying Backup...");
			getLegendsWebsiteBackup();
		}
	}

	private static void getLegendsWebsiteBackup() throws IOException {
		if (FileUtilities.isFileExist(LocalDataManager.STORAGE_FOLDER + File.separator + "Website_Backup.txt")) {
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
		LegendsTemplatesContext.shutdownStatic();
		LegendsNewsContext.shutdownStatic();
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

	public static LegendsRoll getLegendsRoll(Message msg) {
		RollTemplates templates = LegendsTemplatesContext.getTemplates(msg);
		return templates == null ? new LegendsRoll(getDefaultTemplates())
				: new LegendsRoll(templates.getRollTemplates());
	}

	public static List<String> getDefaultTemplates() {
		return LegendsTemplatesContext.getDefaultRollTemplates();
	}

	public static LegendsSearch getLegendsSearch(Message msg) {
		RollTemplates templates = LegendsTemplatesContext.getTemplates(msg);
		return templates == null ? new LegendsSearch(getDefaultTemplates())
				: new LegendsSearch(templates.getRollTemplates());
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

	public static NewsFeedManager<LegendsNewsPiece> getNewsManager() {
		return LegendsNewsContext.getNewsManager();
	}
}
