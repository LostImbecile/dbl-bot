package com.github.egubot.facades;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Shutdownable;

public class StorageFacadesHandler implements Shutdownable {
	private static final Logger logger = LogManager.getLogger(StorageFacadesHandler.class.getName());
	private AutoRespondFacade autoRespond = null;
	private LegendsCommandsFacade legends = null;

	public StorageFacadesHandler(){
		try {
			autoRespond = new AutoRespondFacade();
		} catch (IOException e) {
			autoRespond = null;
		}
		legends = new LegendsCommandsFacade();
	}

	public boolean checkCommands(Message msg, String msgText, String lowCaseTxt) {
		return legends.checkCommands(msg, lowCaseTxt)
				|| (autoRespond != null && autoRespond.checkCommands(msg, msgText));
	}

	public boolean respond(Message msg, String msgText) {
		return autoRespond != null && autoRespond.respond(msgText, msg);
	}

	@Override
	public void shutdown() {
		try {
			if (autoRespond != null)
				autoRespond.shutdown();
			if (legends != null)
				legends.shutdown();
		} catch (Exception e) {
			logger.error("Failed to shut storage classes down.", e);
		}

	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

}
