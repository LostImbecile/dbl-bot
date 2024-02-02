package com.github.egubot.handlers;

import java.io.IOException;
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

import com.github.egubot.facades.AutoRespondFacade;
import com.github.egubot.facades.ChatGPTFacade;
import com.github.egubot.facades.CustomAIFacade;
import com.github.egubot.facades.LegendsCommandsFacade;
import com.github.egubot.facades.TranslateFacade;
import com.github.egubot.facades.WeatherFacade;
import com.github.egubot.facades.WebDriverFacade;
import com.github.egubot.features.MessageTimers;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.main.BotApi;
import com.github.egubot.main.KeyManager;
import com.github.egubot.main.ShutdownManager;
import com.github.egubot.objects.Attributes;
import com.github.egubot.shared.FileUtilities;
import com.github.egubot.shared.JSONUtilities;
import com.github.egubot.shared.Shared;
import com.github.egubot.shared.UserInfoUtilities;
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
	private String sirioMsgContent;

	private boolean readBotMessages = false;
	private DiscordApi api;

	private AutoRespondFacade autoRespond = null;
	private LegendsCommandsFacade legends = null;
	private ChatGPTFacade gpt = new ChatGPTFacade();
	private CustomAIFacade customAi = new CustomAIFacade();
	private TranslateFacade translate = new TranslateFacade();
	private WeatherFacade weather = new WeatherFacade();

	private boolean testMode;

	private MessageTimers testTimer = null;
	private MessageTimers userTargetTimer = null;
	private MessageTimers deadChatTimer = null;

	private IncomingWebhook testWebhook = null;
	private IncomingWebhook egubotWebhook = null;

	private final ExecutorService executorService;
	private ShutdownManager shutdownManager;

	public MessageCreateEventHandler() throws Exception {
		/*
		 * I store templates, responses and all that stuff online in case someone
		 * else uses the bot on their end, so the data needs to be initialised
		 * from an online storage each time. I used discord for this, a cloud
		 * services could do better.
		 */
		this.api = BotApi.getApi();
		this.testMode = Shared.isTestMode();
		this.executorService = Executors.newFixedThreadPool(10);
		this.shutdownManager = Shared.getShutdown();

		DataManagerSwitcher.setOnline(true);
		initialiseDataStorage();
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

			if (legends.checkCommands(msg, lowCaseTxt)) {
				return;
			}

			if (translate.checkCommands(msg, lowCaseTxt)) {
				return;
			}

			if (weather.checkCommands(msg, lowCaseTxt)) {
				return;
			}

			if (autoRespond.checkCommands(msg, msgText)) {
				return;
			}

			if (WebDriverFacade.checkCommands(msg, lowCaseTxt)) {
				return;
			}

			if (testMode && (checkTimerTasks(msg, msgText, lowCaseTxt))) {
				return;
			}

			if (lowCaseTxt.equals("b-send attributes")) {
				msg.getChannel().sendMessage(FileUtilities.toInputStream(
						JSONUtilities.toJsonPrettyPrint(new Attributes(), Attributes.class)), "Attributes.txt");
			}

			if (lowCaseTxt.equals("spam mode off")) {
				readBotMessages = false;
				return;
			}

			if (lowCaseTxt.equals("spam mode on")) {
				readBotMessages = true;
				return;
			}

			if (lowCaseTxt.matches("parrot(?s).*")) {
				e.getChannel().sendMessage(msgText.replaceFirst("parrot", ""));
				return;
			}

			if (customAi.checkCommands(msg, lowCaseTxt))
				return;

			if (autoRespond.respond(msgText, msg)) {
				return;
			}

			if (userTargetTimer != null && authorID.equals(userTargetID)) {
				try {
					userTargetTimer.sendDelayedRateLimitedMessage(e.getChannel(), sirioMsgContent, true);
				} catch (Exception e1) {

				}
			}

		} catch (Exception e1) {
			logger.error("Main handler encountered an error.", e1);
			Thread.currentThread().interrupt();
		}
	}

	private boolean checkBotMessageControlCommands(Message msg, String lowCaseTxt) throws Exception {
		if (lowCaseTxt.contains("terminate")) {
			terminate(msg);
			return true;
		}

		if (lowCaseTxt.equals("refresh")) {
			refresh(msg);
			return true;
		}

		if (UserInfoUtilities.isOwner(msg) && lowCaseTxt.matches("b-message(?s).*")) {
			try {
				if (lowCaseTxt.contains("b-message edit")) {
					String st = lowCaseTxt.replaceFirst("b-message edit", "").strip();
					String id = st.substring(0, st.indexOf(" "));
					String edit = st.substring(st.indexOf(" "));
					api.getMessageById(id, msg.getChannel()).get().edit(edit);
					return true;
				}
				if (lowCaseTxt.contains("b-message delete")) {
					String st = lowCaseTxt.replaceFirst("b-message delete", "").strip();
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

	private void initialiseDataStorage() throws Exception {
		/*
		 * You'll want to make your own classes for this stuff usually,
		 * as these are bot/feature specific, autorespond however
		 * can be used for all bots.
		 * 
		 */
		autoRespond = new AutoRespondFacade();
		legends = new LegendsCommandsFacade();
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
		try {
			sirioMsgContent = api.getMessageById(userTargetMsgID, api.getTextChannelById(userTargetChannelID).get())
					.get().getContent();

			List<ServerChannel> channels = api.getServerById(mainServerID).get().getChannels();
			Message[] messages;
			Message temp;
			Instant lastMessageDate = Instant.now().minusMillis(6 * HOUR);
			for (ServerChannel channel : channels) {
				if (channel.asTextChannel().isPresent()) {
					messages = channel.asTextChannel().get().getMessages(50).get().toArray(new Message[0]);
					for (Message message : messages) {
						temp = message;
						if (temp != null && temp.getAuthor().isYourself() && temp.getContent().equals(sirioMsgContent)
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

	private void refresh(Message msg) throws Exception {
		if (!UserInfoUtilities.isUserEqual(msg.getAuthor(), userTargetID)) {
			msg.getChannel().sendMessage("Refreshing...").join();
			System.out.println("\nRefreshing " + MessageCreateEventHandler.class.getName() + ".");

			// Important to make sure any remaining data is uploaded first
			shutdownInternalClasses();

			initialiseDataStorage();
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
		} catch (Exception e) {
			logger.error("Failed to shut timers down.", e);
		}
		try {
			if (autoRespond != null)
				autoRespond.shutdown();
			if (legends != null)
				legends.shutdown();
		} catch (Exception e) {
			logger.error("Failed to shut storage classes down.", e);
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