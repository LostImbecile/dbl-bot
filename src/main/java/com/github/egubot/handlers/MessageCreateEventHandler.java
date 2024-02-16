package com.github.egubot.handlers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.github.egubot.facades.ChatGPTContext;
import com.github.egubot.facades.CustomAIFacade;
import com.github.egubot.facades.StorageFacadesHandler;
import com.github.egubot.features.MessageTimers;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.main.Bot;
import com.github.egubot.managers.CommandManager;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.managers.ShutdownManager;
import com.github.egubot.shared.FileUtilities;
import com.github.egubot.shared.Shared;
import com.github.egubot.storage.ConfigManager;
import com.github.egubot.storage.DataManagerSwitcher;

public class MessageCreateEventHandler implements MessageCreateListener, Shutdownable {
	private static final Logger logger = LogManager.getLogger(MessageCreateEventHandler.class.getName());
	private static final long HOUR = 1000 * 60 * 60L;

	private static String testServerID = KeyManager.getID("Test_Server_ID");
	private static String userTargetID = KeyManager.getID("User_Target_ID");
	private static String mainServerID = KeyManager.getID("Main_Server_ID");
	private static String userTargetMsgID = KeyManager.getID("User_Target_Msg_ID");
	private static String userTargetChannelID = KeyManager.getID("User_Target_Msg_Channel_ID");
	private static String timerLengthMessage = KeyManager.getID("Dead_Chat_Timer_Msg");
	private static String userTargetMsgContent;

	private static boolean readBotMessages = false;
	private static DiscordApi api = Bot.getApi();

	private static boolean testMode = Shared.isTestMode();

	private static boolean isUserTimerOn = ConfigManager.getBooleanProperty("User_Target_Enable");
	private static MessageTimers testTimer = null;
	private static MessageTimers userTargetTimer = null;
	private static MessageTimers deadChatTimer = null;

	private static IncomingWebhook testWebhook = null;
	private static IncomingWebhook egubotWebhook = null;

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
			executorService.submit(() -> initialiseWebhooks());
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

			ChatGPTContext.addAssistantResponse(msg, msgText);

			// Ignore bots unless changed
			if (!msg.getAuthor().isRegularUser() && !readBotMessages) {
				return;
			}

			// This is so I can run a test version and a non-test version at the same time
			if (testMode && !isTestServer(msg))
				return;
			if (!testMode && isTestServer(msg))
				return;

			if (CommandManager.processMessage(msg))
				return;

			try {
				if (isTestActive) {
					Class<?> testClass = Class.forName("com.github.egubot.features.Test");
					Method checkMethod = testClass.getMethod("check", MessageCreateEvent.class, Message.class,
							String.class);
					checkMethod.invoke(null, e, msg, msgText);
				}
			} catch (Exception e1) {
			}

			String authorID = msg.getAuthor().getIdAsString();

			checkChannelTimer(msg);

			// Replaces the sentence below with the contents of the attachment
			// No real purpose besides avoiding character limits currently
			if (lowCaseTxt.contains("[attachment text replace]") && !msg.getAttachments().isEmpty()) {
				msgText = replaceAttachmentText(msg, msgText);
				lowCaseTxt = msgText.toLowerCase();
			}

			if (testMode && (checkTimerTasks(msg, msgText))) {
				return;
			}

			if (ChatGPTContext.repond(msg, msgText)) {
				return;
			}

			if (CustomAIFacade.respond(msg, lowCaseTxt)) {
				return;
			}

			if (StorageFacadesHandler.respond(msg, msgText))
				return;

			if (userTargetTimer != null && authorID.equals(userTargetID)) {
				try {
					userTargetTimer.sendDelayedRateLimitedMessage(e.getChannel(), userTargetMsgContent, true);
				} catch (Exception e1) {

				}
			}

		} catch (Exception e1) {
			logger.error("Main handler encountered an error.", e1);
			Thread.currentThread().interrupt();
		}
	}

	private boolean isTestServer(Message msg) {
		if (msg.isServerMessage())
			return msg.getServer().get().getIdAsString().equals(testServerID);
		return false;
	}

	public static void initialiseWebhooks() {
		if (testMode)
			return;
		/*
		 * Note, doesn't check for zones, will simply not work as expected
		 * if discord goes out of sync with your timezone.
		 * (only in the case of timers that depend on message creation timestamps)
		 */
		long testWebhookID;
		long egubotWebhookID;
		try {
			egubotWebhookID = Long.parseLong(KeyManager.getID("Egubot_Webhook_ID"));
			egubotWebhook = api.getWebhookById(egubotWebhookID).get().asIncomingWebhook().get();
			egubotWebhook.updateAvatar(api.getUserById(userTargetID).get().getAvatar());
			egubotWebhook.updateName(
					api.getUserById(userTargetID).get().getDisplayName(api.getServerById(mainServerID).get()));
		} catch (Exception e) {
			egubotWebhook = null;
		}
		try {
			testWebhookID = Long.parseLong(KeyManager.getID("Test_Webhook_ID"));
			testWebhook = api.getWebhookById(testWebhookID).get().asIncomingWebhook().get();
		} catch (Exception e) {
			testWebhook = null;
		}

		try {
			testTimer = new MessageTimers(5000, null, null);

		} catch (Exception e) {
			testTimer = null;
		}

		// Schedules the message based on the last message sent in
		// the webhook's chat.
		try {
			double length;
			try {
				length = Double.parseDouble(
						api.getMessageById(timerLengthMessage, api.getTextChannelById(userTargetChannelID).get()).join()
								.getContent());
			} catch (Exception e) {
				length = 24;
			}

			deadChatTimer = new MessageTimers((long) (length * HOUR), null,
					egubotWebhook.getChannel().get().getMessages(1).get().first().getCreationTimestamp());
			deadChatTimer.sendScheduledMessage(egubotWebhook, "Dead chat", true);
		} catch (Exception e) {
			deadChatTimer = null;
		}

		// Checks the first 50 messages in all channels in the server
		// to see the last time it replied with a specific message
		if (!isUserTimerOn)
			return;
		try {
			userTargetMsgContent = api
					.getMessageById(userTargetMsgID, api.getTextChannelById(userTargetChannelID).get()).get()
					.getContent();

			List<ServerChannel> channels = api.getServerById(mainServerID).get().getChannels();
			Message[] messages;
			Message temp;
			Instant lastMessageDate = Instant.now().minusMillis(6 * HOUR);
			for (ServerChannel channel : channels) {
				if (channel.asTextChannel().isPresent()) {
					messages = channel.asTextChannel().get().getMessages(50).get().toArray(new Message[0]);
					for (Message message : messages) {
						temp = message;
						if (temp != null && temp.getAuthor().isYourself()
								&& temp.getContent().equals(userTargetMsgContent)
								&& (temp.getCreationTimestamp().isAfter(lastMessageDate))) {
							lastMessageDate = temp.getCreationTimestamp();
						}
					}
				}
			}

			// Starts a delay timer based on the last time it replied with
			// a specific message
			userTargetTimer = new MessageTimers(6 * HOUR, null, lastMessageDate);
		} catch (Exception e) {
			userTargetTimer = null;
		}

	}

	private boolean checkTimerTasks(Message msg, String msgText) {
		if (msgText.equals("start task")) {
			try {
				testWebhook.updateAvatar(msg.getAuthor().getAvatar()).join();
				testWebhook.updateChannel(msg.getChannel().asServerTextChannel().get());
				testTimer.setStartTime(null, msg.getCreationTimestamp());
				testTimer.sendScheduledMessage(testWebhook, msgText, true);
			} catch (Exception e1) {
				logger.error("Failed to start timer task.", e1);
			}
			return true;
		}

		if (msgText.equals("cancel task")) {
			try {
				testTimer.cancelRecurringTimer();
			} catch (Exception e1) {
				logger.error("Failed to cancel timer task.", e1);
			}
			return true;
		}

		return false;
	}

	private void checkChannelTimer(Message msg) {
		try {
			String channelID = msg.getChannel().getIdAsString();
			if (deadChatTimer != null && channelID.equals(egubotWebhook.getChannel().get().getIdAsString())) {

				deadChatTimer.cancelRecurringTimer();
				deadChatTimer.setStartTime(null, null);
				deadChatTimer.sendScheduledMessage(egubotWebhook, "Dead chat", true);

			}
		} catch (Exception e1) {
			//
		}
	}

	public static void shutdownInternalClasses() {
		try {
			if (deadChatTimer != null)
				deadChatTimer.terminateTimer();
			if (testTimer != null)
				testTimer.terminateTimer();
			if (userTargetTimer != null)
				userTargetTimer.terminateTimer();

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
			Thread.currentThread().interrupt();
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