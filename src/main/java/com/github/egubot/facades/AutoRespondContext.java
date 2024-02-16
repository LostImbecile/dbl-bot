package com.github.egubot.facades;

import java.io.IOException;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.AutoRespond;
import com.github.egubot.interfaces.Shutdownable;

public class AutoRespondContext implements Shutdownable {
	private static AutoRespond autoRespond = null;

	private AutoRespondContext() {
	}

	public static void initialise() throws IOException {
		autoRespond = new AutoRespond();
	}

	public static void shutdownStatic() {
		if (autoRespond != null)
			autoRespond.shutdown();
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
		return autoRespond.respond(msgText, msg);
	}

	public static AutoRespond getAutoRespond() {
		return autoRespond;
	}
	
}
