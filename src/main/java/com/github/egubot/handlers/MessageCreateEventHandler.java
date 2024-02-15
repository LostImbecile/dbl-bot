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

import com.github.egubot.facades.ChatGPTFacade;
import com.github.egubot.facades.CustomAIFacade;
import com.github.egubot.facades.StorageFacadesHandler;
import com.github.egubot.facades.WebFacadesHandler;
import com.github.egubot.features.MessageTimers;
import com.github.egubot.features.SoundPlayback;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.main.Bot;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.managers.ShutdownManager;
import com.github.egubot.shared.FileUtilities;
import com.github.egubot.shared.Shared;
import com.github.egubot.shared.UserInfoUtilities;
import com.github.egubot.storage.ConfigManager;
import com.github.egubot.storage.DataManagerSwitcher;

public class MessageCreateEventHandler implements MessageCreateListener, Shutdownable {
	private static final Logger logger = LogManager.getLogger(MessageCreateEventHandler.class.getName());
	private static final long HOUR = 1000 * 60 * 60L;

	private String testServerID = KeyManager.getID("Test_Server_ID");
	private String userTargetID = KeyManager.getID("User_Target_ID");
	private String mainServerID = KeyManager.getID("Main_Server_ID");
	private String userTargetMsgID = KeyManager.getID("User_Target_Msg_ID");
	private String userTargetChannelID = KeyManager.getID("User_Target_Msg_Channel_ID");
	private String timerLengthMessage = KeyManager.getID("Dead_Chat_Timer_Msg");
	private String userTargetMsgContent;

	private boolean readBotMessages = false;
	private DiscordApi api;

	private StorageFacadesHandler storageFacades;
	private WebFacadesHandler webFacades = new WebFacadesHandler();
	private ChatGPTFacade gpt = new ChatGPTFacade();
	private CustomAIFacade customAi = new CustomAIFacade();

	private boolean testMode;

	private boolean isUserTimerOn = ConfigManager.getBooleanProperty("User_Target_Enable");
	private MessageTimers testTimer = null;
	private MessageTimers userTargetTimer = null;
	private MessageTimers deadChatTimer = null;

	private IncomingWebhook testWebhook = null;
	private IncomingWebhook egubotWebhook = null;

	private final ExecutorService executorService;
	private ShutdownManager shutdownManager;
	private boolean isTestActive = false;
	private static String prefix = Bot.getPrefix();

	public MessageCreateEventHandler() {
		/*
		 * I store templates, responses and all that stuff online in case someone
		 * else uses the bot on their end, so the data needs to be initialised
		 * from an online storage each time. I used discord for this, a cloud
		 * services could do better.
		 */
		this.api = Bot.getApi();
		this.testMode = Shared.isTestMode();
		this.executorService = Executors.newFixedThreadPool(10);
		this.shutdownManager = Shared.getShutdown();
		shutdownManager.registerShutdownable(this);

		DataManagerSwitcher.setOnline(ConfigManager.getBooleanProperty("Is_Storage_Online"));

		storageFacades = new StorageFacadesHandler();
		executorService.submit(this::initialiseWebhooks);
	}

	@Override
	public void onMessageCreate(MessageCreateEvent e) {
		executorService.submit(() -> handleOnMessageCreate(e));

	}

	private void handleOnMessageCreate(MessageCreateEvent e) {
		Message msg = e.getMessage();
		// Regex replaces non-utf8 characters
		String msgText = msg.getContent();
		String lowCaseTxt = msgText.toLowerCase();

		try {

			if (gpt.checkCommands(msg, msgText, lowCaseTxt)) {
				return;
			}

			// Ignore bots unless changed
			if (!msg.getAuthor().isRegularUser() && !readBotMessages) {
				return;
			}

			if (checkBotMessageControlCommands(msg, lowCaseTxt)) {
				return;
			}

			try {
				if (isTestActive) {
					Class<?> testClass = Class.forName("com.github.egubot.features.Test");
					Method checkMethod = testClass.getMethod("check", MessageCreateEvent.class, Message.class,
							String.class);
					checkMethod.invoke(null, e, msg, msgText);
				} else if (lowCaseTxt.equals(prefix + "test toggle")) {
					isTestActive = !isTestActive;
				}
			} catch (Exception e1) {
			}
			// This is so I can run a test version and a non-test version at the same time
			if (testMode && !isTestServer(msg))
				return;
			if (!testMode && isTestServer(msg))
				return;

			String authorID = msg.getAuthor().getIdAsString();

			checkChannelTimer(msg);

			// Replaces the sentence below with the contents of the attachment
			// No real purpose besides avoiding character limits currently
			if (lowCaseTxt.contains("[attachment text replace]") && !msg.getAttachments().isEmpty()) {
				msgText = replaceAttachmentText(msg, msgText);
				lowCaseTxt = msgText.toLowerCase();
			}

			if (storageFacades.checkCommands(msg, msgText, lowCaseTxt)) {
				return;
			}

			if (webFacades.checkCommands(msg, msgText, lowCaseTxt))
				return;

			try {
				if (SoundPlayback.checkMusicCommands(msg, lowCaseTxt)) {
					return;
				}
			} catch (Exception e1) {
				logger.error(e1);
			}

			if (testMode && (checkTimerTasks(msg, msgText, lowCaseTxt))) {
				return;
			}

			if (lowCaseTxt.equals(prefix + "verse")) {
				msg.getChannel().sendMessage(
						FileUtilities.readURL("https://labs.bible.org/api/?passage=random&type=text&formatting=plain"));
			}

			if (lowCaseTxt.matches("parrot(?s).*") && UserInfoUtilities.isOwner(msg)) {
				e.getChannel().sendMessage(msgText.replaceFirst("parrot", ""));
				return;
			}

			if (customAi.checkCommands(msg, lowCaseTxt))
				return;

			if (storageFacades.respond(msg, msgText))
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

	private boolean checkBotMessageControlCommands(Message msg, String lowCaseTxt) throws Exception {
		if (lowCaseTxt.startsWith("terminate")) {
			terminate(msg);
			return true;
		}

		if (lowCaseTxt.equals("refresh")) {
			refresh(msg);
			return true;
		}

		if (lowCaseTxt.equals("toggle bot read mode") && UserInfoUtilities.isOwner(msg)) {
			readBotMessages = !readBotMessages;
			return true;
		}

		if (lowCaseTxt.equals(prefix + "toggle manager") && UserInfoUtilities.isOwner(msg)) {
			DataManagerSwitcher.setOnline(!DataManagerSwitcher.isOnline());
			return true;
		}

		if (UserInfoUtilities.isOwner(msg) && lowCaseTxt.matches(prefix + "message(?s).*")) {
			try {
				if (lowCaseTxt.contains(prefix + "message edit")) {
					String st = lowCaseTxt.replaceFirst(prefix + "message edit", "").strip();
					String id = st.substring(0, st.indexOf(" "));
					String edit = st.substring(st.indexOf(" "));
					api.getMessageById(id, msg.getChannel()).get().edit(edit);
					return true;
				}
				if (lowCaseTxt.contains(prefix + "message delete")) {
					String st = lowCaseTxt.replaceFirst(prefix + "message delete", "").strip();
					api.getMessageById(st, msg.getChannel()).get().delete();
					return true;
				}
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				logger.error("Failed to change or delete a message.", e);
			}
		}
		return false;
	}

	private boolean isTestServer(Message msg) {
		if (msg.isServerMessage())
			return msg.getServer().get().getIdAsString().equals(testServerID);
		return false;
	}

	private void initialiseWebhooks() {
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

	private boolean checkTimerTasks(Message msg, String msgText, String lowCaseTxt) {
		if (lowCaseTxt.equals("start task")) {
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

		if (lowCaseTxt.equals("cancel task")) {
			try {
				testTimer.cancelRecurringTimer();
			} catch (Exception e1) {
				logger.error("Failed to cancel timer task.", e1);
			}
			return true;
		}

		return false;
	}

	private void refresh(Message msg) {
		if (UserInfoUtilities.isOwner(msg)) {
			msg.getChannel().sendMessage("Refreshing...").join();
			System.out.println("\nRefreshing " + MessageCreateEventHandler.class.getName() + ".");

			// Important to make sure any remaining data is uploaded first
			shutdownInternalClasses();

			storageFacades = new StorageFacadesHandler();
			executorService.submit(this::initialiseWebhooks);

			msg.getChannel().sendMessage("Refreshed :ok_hand:");
		} else {
			msg.getChannel().sendMessage("no");
		}
	}

	private void terminate(Message msg) {
		boolean isOwner = UserInfoUtilities.isOwner(msg);

		String st = msg.getContent().toLowerCase().replace("terminate", "").strip();
		if (st.isBlank() || st.equals(api.getYourself().getMentionTag())) {
			if (msg.getServer().get().getOwnerId() == msg.getAuthor().getId() || isOwner) {
				msg.getChannel().sendMessage("Terminating...").join();
				logger.warn("\nTerminate message invoked.");
				shutdownManager.initiateShutdown(0);
			} else {
				msg.getChannel().sendMessage("<a:no:1195656310356717689>");
			}
		}
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

	public void shutdownInternalClasses() {
		try {
			if (deadChatTimer != null)
				deadChatTimer.terminateTimer();
			if (testTimer != null)
				testTimer.terminateTimer();
			if (userTargetTimer != null)
				userTargetTimer.terminateTimer();
			if (storageFacades != null)
				storageFacades.shutdown();
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

}