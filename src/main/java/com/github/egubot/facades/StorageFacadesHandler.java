package com.github.egubot.facades;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.github.egubot.interfaces.Shutdownable;

public class StorageFacadesHandler implements Shutdownable {
	private static final Logger logger = LogManager.getLogger(StorageFacadesHandler.class.getName());

	private StorageFacadesHandler() {
	}

	public static void initialise() {
		try {
			AutoRespondContext.initialise();
		} catch (IOException e) {
			logger.error("Autorespond broke", e);
		}
		try {
			ScheduledTasksContext.initialise();
		}catch (IOException e) {
			logger.error("ScheduledTasks broke", e);
		}
		LegendsCommandsContext.initialise();
	}

	public static void shutdownStatic() {
		try {
			AutoRespondContext.shutdownStatic();
			LegendsTemplatesContext.shutdownStatic();
			ScheduledTasksContext.shutdownStatic();
		} catch (Exception e) {
			logger.error("Failed to shut storage classes down.", e);
		}
	}

	@Override
	public void shutdown() {
		shutdownStatic();
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

}
