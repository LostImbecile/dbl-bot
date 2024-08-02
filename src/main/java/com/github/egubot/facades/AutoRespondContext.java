package com.github.egubot.facades;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.build.AutoRespond;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Shutdownable;

public class AutoRespondContext implements Shutdownable {
	private static final Logger logger = LogManager.getLogger(AutoRespondContext.class.getName());
	private static Map<Long, AutoRespond> autoRespondMap = new HashMap<>();

	private AutoRespondContext() {
	}

	public static void shutdownStatic() {
		for (AutoRespond autoRespond : autoRespondMap.values()) {
			if (autoRespond != null) {
				autoRespond.shutdown();
			}
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

	public static boolean respond(String msgText, Message msg) {
		AutoRespond autoRespond = getAutoRespond(msg);
		return autoRespond != null && autoRespond.respond(msgText, msg);
	}

	public static AutoRespond getAutoRespond(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		if (serverID == -1) {
			return null;
		}
		return autoRespondMap.computeIfAbsent(serverID, k -> {
			try {
				return new AutoRespond(serverID);
			} catch (IOException e) {
				logger.error(e);
			}
			return null;
		});
	}

}
