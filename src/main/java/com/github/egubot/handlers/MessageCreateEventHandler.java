package com.github.egubot.handlers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.github.egubot.facades.AIContext;
import com.github.egubot.facades.CustomAIFacade;
import com.github.egubot.facades.StorageFacadesHandler;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.managers.CommandManager;
import com.github.egubot.managers.ShutdownManager;
import com.github.egubot.shared.Shared;
import com.github.egubot.shared.utils.FileUtilities;
import com.github.egubot.storage.ConfigManager;
import com.github.egubot.storage.DataManagerSwitcher;

public class MessageCreateEventHandler implements MessageCreateListener, Shutdownable {
	private static final Logger logger = LogManager.getLogger(MessageCreateEventHandler.class.getName());

	private static boolean readBotMessages = false;

	public static final ExecutorService executorService = Executors.newFixedThreadPool(10);
	private static ShutdownManager shutdownManager = Shared.getShutdown();
	private static boolean isTestActive = false;

	private static boolean isInitialised = false;

	public MessageCreateEventHandler() {
		shutdownManager.registerShutdownable(this);
		initialise();
	}

	public static synchronized void initialise() {
		if (!isInitialised) {
			/*
			 * I store templates, responses and all that stuff online in case someone
			 * else uses the bot on their end, so the data needs to be initialised
			 * from an online storage each time. I used discord for this, a cloud
			 * services could do better.
			 */
			StorageFacadesHandler.initialise();
			DataManagerSwitcher.setOnline(ConfigManager.getBooleanProperty("Is_Storage_Online"));
			isInitialised = true;
		}
	}

	@Override
	public void onMessageCreate(MessageCreateEvent e) {
		executorService.submit(() -> handleOnMessageCreate(e));
	}

	private void handleOnMessageCreate(MessageCreateEvent e) {
		try {
			Message msg = e.getMessage();
			String msgText = msg.getContent();
			String lowCaseTxt = msgText.toLowerCase();

			// Ignore bots unless changed
			if (!msg.getAuthor().isRegularUser() && !readBotMessages) {
				return;
			}

			try {
				if (isTestActive) {
					Class<?> testClass = Class.forName("com.github.egubot.features.Test");
					Method checkMethod = testClass.getMethod("check", MessageCreateEvent.class, Message.class,
							String.class);
					checkMethod.invoke(null, e, msg, msgText);
				}
			} catch (Exception e1) {
			}

			// Replaces the sentence below with the contents of the attachment
			// No real purpose besides avoiding character limits currently
			if (lowCaseTxt.contains("[attachment text replace]") && !msg.getAttachments().isEmpty()) {
				msgText = replaceAttachmentText(msg, msgText);
				lowCaseTxt = msgText.toLowerCase();
			}

			// Check commands
			if (CommandManager.processMessage(msg))
				return;

			if (AIContext.getGpt3().respondIfChannelActive(msg, msgText)) {
				return;
			}
			
			if (AIContext.getLlama3().respondIfChannelActive(msg, msgText)) {
				return;
			}

			if (CustomAIFacade.respond(msg, lowCaseTxt)) {
				return;
			}

			if (StorageFacadesHandler.respond(msg, msgText))
				return;

		} catch (Exception e1) {
			logger.error("Main handler encountered an error.\nUser message: " + e.getMessageContent(), e1);
			Thread.currentThread().interrupt();
		}
	}

	public static void shutdownInternalClasses() {
		try {
			StorageFacadesHandler.shutdownStatic();
		} catch (Exception e) {
			logger.error("Failed to shut timers down.", e);
		}

	}

	public void shutdown() {
		shutdownInternalClasses();
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
				// If tasks don't finish within time, forceful shutdown
				executorService.shutdownNow();
				logger.error("Executor Service shutdown was forced.");
			}
		} catch (InterruptedException e) {
			logger.error("Shutdown failed.", e);
		}
	}

	public static void toggleBotReadMode() {
		readBotMessages = !readBotMessages;
	}

	private String replaceAttachmentText(Message msg, String msgText) {
		String st = "";

		try {
			st = FileUtilities.readInputStream(msg.getAttachments().get(0).asInputStream(), "\n");
		} catch (IOException e) {
			//
		}

		return msgText.replace("[attachment text replace]", st);
	}

	@Override
	public int getShutdownPriority() {
		return 10;
	}

	public static void toggleTestClass() {
		isTestActive = !isTestActive;
	}

}