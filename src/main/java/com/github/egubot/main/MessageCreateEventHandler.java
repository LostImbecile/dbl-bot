package com.github.egubot.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.azure.services.Translate;
import com.github.egubot.build.AutoRespond;
import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.build.RollTemplates;
import com.github.egubot.features.LegendsRoll;
import com.github.egubot.features.LegendsSearch;
import com.github.egubot.features.MessageFormats;
import com.github.egubot.features.MessageTimers;
import com.github.egubot.gpt2.DiscordAI;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.objects.CharacterHash;
import com.github.egubot.shared.SendObjects;
import com.github.egubot.storage.OnlineDataManager;
import com.openai.chatgpt.ChatGPT;

public class MessageCreateEventHandler implements MessageCreateListener, Shutdownable {

	private static final long HOUR = 1000 * 60 * 60L;
	private String testServerID = KeyManager.getID("Test_Server_ID");
	private String userTargetID = KeyManager.getID("User_Target_ID");
	private String mainServerID = KeyManager.getID("Main_Server_ID");
	private String userTargetMsgID = KeyManager.getID("User_Target_Msg_ID");
	private String userTargetChannelID = KeyManager.getID("User_Target_Msg_Channel_ID");
	private String gpt2ChannelID = KeyManager.getID("GPT2_Channel_ID");
	private String timerLengthMessage = KeyManager.getID("Dead_Chat_Timer_Msg");
	private String wheelChannelID = KeyManager.getID("Wheel_Channel_ID");
	private String backupWebsiteFlag = KeyManager.getID("Backup_Website_Flag");
	private String sirioMsgContent;

	private boolean isAnimated = true;
	private boolean isCustomAIOn = false;
	private boolean readBotMessages = false;
	private boolean isChatGPTOn = false;
	private boolean isTranslateOn = false;

	private String chatGPTActiveChannelID = "";

	private static ArrayList<String> chatgptConversation = new ArrayList<>(0);

	private DiscordApi api;

	private LegendsDatabase legendsWebsite;
	private LegendsRoll legendsRoll;
	private AutoRespond autoRespond;
	private RollTemplates templates;
	private LegendsSearch legendsSearch;
	private Translate translate = new Translate();

	private boolean isRollCommandActive;
	private boolean testMode;

	private MessageTimers testTimer;
	private MessageTimers userTargetTimer;
	private MessageTimers deadChatTimer;

	private IncomingWebhook testWebhook;
	private IncomingWebhook egubotWebhook;

	private boolean dbLegendsMode;

	private final ExecutorService executorService;
	private ShutdownManager shutdownManager;

	public MessageCreateEventHandler(DiscordApi api, ShutdownManager shutdownManager, boolean testMode,
			boolean dbLegendsMode) throws Exception {
		/*
		 * I store templates, responses and all that stuff online in case someone
		 * else uses the bot on their end, so the data needs to be initialised
		 * from an online storage each time. I used discord for this, a cloud
		 * services could do better.
		 */
		this.api = api;
		this.testMode = testMode;
		this.isAnimated = !testMode;
		this.dbLegendsMode = dbLegendsMode;
		this.isRollCommandActive = dbLegendsMode;
		this.executorService = Executors.newFixedThreadPool(10);
		this.shutdownManager = shutdownManager;

		initialiseDataStorage();
		initialiseWebhooks();
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

			if (checkChatGPTCommands(msg, msgText, lowCaseTxt)) {
				return;
			}

			// Ignore bots unless changed
			if (!msg.getAuthor().isRegularUser() && !readBotMessages) {
				return;
			}

			if (lowCaseTxt.contains("terminate")) {
				terminate(msg);
			}

			if (lowCaseTxt.equals("refresh")) {
				refresh(msg);
				return;
			}
			
			if (isOwner(msg) && lowCaseTxt.matches("b-message edit(?s).*")) {
				String st = lowCaseTxt.replace("b-message edit", "").strip();
				String id = st.substring(0, st.indexOf(" "));
				String edit = st.substring(st.indexOf(" "));
				api.getMessageById(id, e.getChannel()).get().edit(edit);
			}


			// This is so I can run a test version and a non-test version at the same time
			if (testMode && !msg.getServer().get().getIdAsString().equals(testServerID))
				return;
			if (!testMode && msg.getServer().get().getIdAsString().equals(testServerID))
				return;

			String channelID = msg.getChannel().getIdAsString();
			String authorID = msg.getAuthor().getIdAsString();

			checkChannelTimer(channelID);

			// Replaces the sentence below with the contents of the attachment
			// No real purpose besides avoiding character limits currently
			if (lowCaseTxt.contains("[attachment text replace]") && !e.getMessage().getAttachments().isEmpty()) {
				msgText = replaceAttachmentText(e, msgText);
				lowCaseTxt = msgText.toLowerCase();
			}

			if (checkDBLegendsCommands(msg, lowCaseTxt, channelID)) {
				return;
			}

			if (checkTranslateCommands(msg, lowCaseTxt)) {
				return;
			}

			if (checkWeatherCommands(msg, lowCaseTxt)) {
				return;
			}

			if (tryAutoRespondCommands(msg, msgText, lowCaseTxt)) {
				return;
			}

			if (testMode && (checkTimerTasks(msg, msgText, lowCaseTxt))) {
				return;
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
				e.getChannel().sendMessage(msgText.replace("parrot", ""));
			}

			if (lowCaseTxt.equals("ai activate")) {
				isCustomAIOn = true;
				return;
			}

			checkCustomAI(msg, lowCaseTxt, channelID);

			if (autoRespond.respond(msgText, e)) {
				return;
			}

			if (authorID.equals(userTargetID)) {
				try {
					userTargetTimer.sendDelayedRateLimitedMessage(e.getChannel(), sirioMsgContent, true);
				} catch (Exception e1) {

				}
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void initialiseDataStorage() throws Exception {
		/*
		 * You'll want to make your own classes for this stuff usually,
		 * as these are bot/feature specific, autorespond however
		 * can be used for all bots.
		 * 
		 */
		autoRespond = new AutoRespond(api);
		autoRespond.toggleManager(true);
		shutdownManager.registerShutdownable(autoRespond);

		if (dbLegendsMode) {
			// Fetches data from the legends website and initialises
			// classes that are based on it, or doesn't if that fails
			try {
				System.out.println("\nFetching characters from dblegends.net...");
				legendsWebsite = new LegendsDatabase();

				if (legendsWebsite.isDataFetchSuccessfull()) {

					if (backupWebsiteFlag.equals("true")) {
						System.out.println("Character database was successfully built!\nWebsite Backup uploading...");

						// Upload current website HTML as backup
						new Thread(() -> {
							try {

								new OnlineDataManager(api, "Website_Backup_Msg_ID", "website_backup",
										LegendsDatabase.getWebsiteAsInputStream("https://dblegends.net/characters"),
										false).writeData(null);

							} catch (Exception e) {

							}

						}).start();
					} else {
						System.out.println("Character database was successfully built!");
					}

				} else {
					System.err.println("Character database missing information. Trying Backup...");

					OnlineDataManager backup = new OnlineDataManager(api, "Website_Backup_Msg_ID", "Website Backup",
							LegendsDatabase.getWebsiteAsInputStream("https://dblegends.net/"), true);

					legendsWebsite = new LegendsDatabase(backup.getData());
					if (!legendsWebsite.isDataFetchSuccessfull()) {
						System.err.println("Warning: Backup is also missing information.");
					}
				}

			} catch (Exception e) {
				System.err.println("\nFailed to build character database. Relevant commands will be inactive.");
				isRollCommandActive = false;
			}

			if (isRollCommandActive) {
				templates = new RollTemplates(api, legendsWebsite);
				templates.toggleManager(true);
				shutdownManager.registerShutdownable(templates);

				legendsRoll = new LegendsRoll(legendsWebsite, templates.getRollTemplates());

				legendsSearch = new LegendsSearch(legendsWebsite, templates.getRollTemplates());
			}
		}
	}

	private void initialiseWebhooks() {
		/*
		 * Note, doesn't check for zones, will simply not work as expected
		 * if discord goes out of sync with your timezone.
		 * (only in the case of timers that depend on message creation timestamps)
		 * 
		 */
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
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
						length = Double.parseDouble(api
								.getMessageById(timerLengthMessage, api.getTextChannelById(userTargetChannelID).get())
								.join().getContent());
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
					sirioMsgContent = api
							.getMessageById(userTargetMsgID, api.getTextChannelById(userTargetChannelID).get()).get()
							.getContent();

					List<ServerChannel> channels = api.getServerById(mainServerID).get().getChannels();
					Object[] messages;
					Message temp;
					Instant lastMessageDate = Instant.now().minusMillis(6 * HOUR);
					for (int i = 0; i < channels.size(); i++) {
						if (channels.get(i).asTextChannel().isPresent()) {
							messages = channels.get(i).asTextChannel().get().getMessages(50).get().toArray();
							for (int j = 0; j < messages.length; j++) {
								temp = (Message) messages[j];
								if (temp != null && temp.getAuthor().isYourself()
										&& temp.getContent().equals(sirioMsgContent)) {
									if (temp.getCreationTimestamp().isAfter(lastMessageDate)) {
										lastMessageDate = temp.getCreationTimestamp();
										// System.out.println(lastMessageDate);
									}
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
		});
		t.start();
	}

	private boolean checkWeatherCommands(Message msg, String lowCaseTxt) {
		if (lowCaseTxt.matches("b-weather(?s).*")) {

			SendObjects.sendWeather(msg, lowCaseTxt);

			return true;
		}
		return false;
	}

	private boolean checkTranslateCommands(Message msg, String lowCaseTxt) {
		if (isTranslateOn) {

			try {
				if (lowCaseTxt.length() < 140
						&& !translate.detectLanguage(lowCaseTxt, true).matches("(?:en)|(?:Error.*)")) {
					msg.getChannel().sendMessage(translate.post(lowCaseTxt, true));
				}

			} catch (IOException e1) {
			}

		}
		if (lowCaseTxt.matches("b-translate(?s).*")) {
			if (lowCaseTxt.equals("b-translate set on")) {
				isTranslateOn = true;
				return true;
			}
			if (lowCaseTxt.equals("b-translate set off")) {
				isTranslateOn = false;
				return true;
			}
			if (lowCaseTxt.contains("b-translate set")) {
				String st = lowCaseTxt.replace("b-translate set", "").strip();
				if (st.contains("-")) {
					String[] toFrom = st.split("-");
					translate.setFrom(toFrom[0]);
					translate.setTo(toFrom[1]);
				} else {
					translate.setTo(st);
					translate.setFrom("");
				}
				return true;
			}
			if (lowCaseTxt.contains("b-translate languages")) {

				try {
					msg.getChannel().sendMessage(
							IOUtils.toInputStream(Translate.getTranslateLanguages(), StandardCharsets.UTF_8),
							"languages.txt");
				} catch (IOException e1) {
					msg.getChannel().sendMessage("Failed to send :thumbs_down");
				}

				return true;
			}

			try {
				if (msg.getMessageReference().isPresent()) {

					Message ref = msg.getMessageReference().get().getMessage().get();
					String content = ref.getContent();
					if (content.equals("")) {
						msg.getChannel().sendMessage(MessageFormats.createTranslateEmbed(ref, translate));
					} else {
						msg.getChannel().sendMessage(translate.post(content, true),
								MessageFormats.createTranslateEmbed(ref, translate));
					}
				} else {
					String content = lowCaseTxt.replace("b-translate", "").strip();

					if (content.equals("")) {
						msg.getChannel().sendMessage(MessageFormats.createTranslateEmbed(msg, translate));
					} else {
						msg.getChannel().sendMessage(translate.post(content, true),
								MessageFormats.createTranslateEmbed(msg, translate));

					}

				}
			} catch (IOException e1) {
				msg.getChannel().sendMessage("Failed to connect to endpoint :thumbs_down:");
			}

			return true;
		}
		return false;
	}

	private boolean checkTimerTasks(Message msg, String msgText, String lowCaseTxt) {
		if (lowCaseTxt.equals("start task")) {
			try {
				testWebhook.updateAvatar(msg.getAuthor().getAvatar()).join();
				testWebhook.updateChannel(msg.getChannel().asServerTextChannel().get());
				testTimer.setStartTime(null, msg.getCreationTimestamp());
				testTimer.sendScheduledMessage(testWebhook, msgText, true);
			} catch (Exception e1) {
			}
			return true;
		}

		if (lowCaseTxt.equals("cancel task")) {
			try {
				testTimer.cancelRecurringTimer();
			} catch (Exception e1) {

			}
			return true;
		}

		return false;
	}

	private void refresh(Message msg) throws Exception {
		if (!msg.getAuthor().getIdAsString().equals(userTargetID)) {
			msg.getChannel().sendMessage("Refreshing...").join();
			System.out.println("\nRefreshing MessageCreateEventHandler.");

			initialiseDataStorage();
			initialiseWebhooks();

			msg.getChannel().sendMessage("Refreshed :ok_hand:");
		} else {
			msg.getChannel().sendMessage("no");
		}
	}

	private void terminate(Message msg) {
		boolean isOwner = isOwner(msg);
		String st = msg.getContent().toLowerCase().replace("terminate", "").strip();
		if (st.equals("") || st.equals(api.getYourself().getMentionTag())) {
			if (msg.getServer().get().getOwnerId() == msg.getAuthor().getId() || isOwner) {
				msg.getChannel().sendMessage("Terminating...").join();
				System.out.println("\nTerminate message invoked.");
				shutdownManager.initiateShutdown(0);
			} else {
				msg.getChannel().sendMessage("<a:no:1195656310356717689>");
			}
		}
	}

	private void checkCustomAI(Message msg, String lowCaseTxt, String channelID) {
		// Personal AI, refer to its class for info
		if (isCustomAIOn) {
			if (lowCaseTxt.equals("ai terminate")) {
				isCustomAIOn = false;
				return;
			}

			if (testMode || channelID.equals(gpt2ChannelID) || lowCaseTxt.matches("ai(?s).*")) {

				try {
					String aiUrl = "http://localhost:5000"; // Update with your AI server URL
					try (DiscordAI discordAI = new DiscordAI(aiUrl)) {
						String input = lowCaseTxt.replace("ai", "").strip();

						String generatedText = discordAI.generateText(input);

						if (!generatedText.matches("Error:(?s).*")) {
							msg.getChannel().sendMessage(generatedText);
						} else if (!generatedText.contains("Connect to localhost:5000"))
							System.err.println("AI Response: " + generatedText);
					}
				} catch (Exception e1) {
					// not worth bothering with
				}

			}
		}
	}

	private boolean checkChatGPTCommands(Message msg, String msgText, String lowCaseTxt) {
		if (lowCaseTxt.equals("chatgpt activate")) {
			isChatGPTOn = true;
			return true;
		}

		if (isChatGPTOn) {
			if (lowCaseTxt.equals("chatgpt deactivate")) {
				isChatGPTOn = false;
				return true;
			}

			if (lowCaseTxt.matches("gpt(?s).*") || msg.getChannel().getIdAsString().equals(chatGPTActiveChannelID)) {
				if (lowCaseTxt.equals("gpt clear")) {
					msg.getChannel().sendMessage("Conversation cleared :thumbsup:");
					chatgptConversation.clear();
					return true;
				}

				if (lowCaseTxt.equals("gpt channel on")) {
					chatGPTActiveChannelID = msg.getChannel().getIdAsString();
					return true;
				}

				if (lowCaseTxt.equals("gpt channel off")) {
					chatGPTActiveChannelID = "";
					return true;
				}

				try {
					String[] response = ChatGPT.chatGPT(msgText, msg.getAuthor().getName(), chatgptConversation);
					msg.getChannel().sendMessage(response[0]);
					// 4096 Token limit that includes sent messages
					// Important to stay under it
					try {
						if (Integer.parseInt(response[1]) > 3300) {

							for (int i = 0; i < 5; i++) {
								chatgptConversation.remove(0);
							}
						}
					} catch (Exception e1) {

					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				return true;
			}

			if (msg.getAuthor().isYourself()) {
				chatgptConversation.add(ChatGPT.reformatInput(msgText, "assistant"));
				return false;
			}

			chatgptConversation.add(ChatGPT.reformatInput(msgText, msg.getAuthor().getName()));
		}
		return false;

	}

	private boolean tryAutoRespondCommands(Message msg, String msgText, String lowCaseTxt) {

		if (lowCaseTxt.matches("b-response(?s).*")) {
			boolean isOwner = isOwner(msg);
			if (lowCaseTxt.contains("b-response create")) {
				if (!lowCaseTxt.contains("sleep"))
					autoRespond.writeResponse(msgText, msg, isOwner);
				else
					msg.getChannel().sendMessage("nah");
				return true;
			}

			if (lowCaseTxt.contains("b-response remove")) {
				autoRespond.removeResponse(msgText, msg.getChannel(), isOwner);
				return true;
			}

			if (lowCaseTxt.equals("b-response send")) {
				autoRespond.sendData(msg.getChannel());
				return true;
			}

			if (lowCaseTxt.contains("b-response lock") && isOwner) {
				try {
					int x = Integer.parseInt(lowCaseTxt.replaceAll("\\D", ""));
					autoRespond.setLockedDataEndIndex(x);
					autoRespond.writeData(msg.getChannel(), false);
				} catch (Exception e1) {

				}
				return true;
			}
			if (lowCaseTxt.equals("b-response update")) {
				try {
					autoRespond.writeData(msg.getChannel());
				} catch (Exception e1) {
				}
			}

		}

		return false;
	}

	private boolean checkDBLegendsCommands(Message msg, String lowCaseTxt, String channelID) {
		if (isRollCommandActive) {
			if (lowCaseTxt.matches("b-(?s).*")) {

				if (checkTemplateCommands(msg, lowCaseTxt)) {
					return true;
				}

				try {
					if (lowCaseTxt.contains("b-search")) {
						legendsSearch.search(lowCaseTxt, api, msg.getChannel());
						return true;
					}

					if (lowCaseTxt.contains("b-roll")) {
						String st = lowCaseTxt;
						legendsRoll.rollCharacters(st, msg.getChannel(), isAnimated);
						return true;
					}

				} catch (Exception e1) {
					msg.getChannel().sendMessage("Filter couldn't be parsed <:huh:1184466187938185286>");
					return true;
				}

				if (lowCaseTxt.equals("b-character send")) {
					SendObjects.sendCharacters(msg.getChannel(), legendsWebsite.getCharactersList());
					return true;
				}

				if (lowCaseTxt.equals("b-character printemptyids")) {
					CharacterHash.printEmptyIDs(legendsWebsite.getCharactersList());
					return true;
				}

				if (lowCaseTxt.equals("b-tag send")) {
					SendObjects.sendTags(msg.getChannel(), legendsWebsite.getTags());
					return true;
				}

			}

			if (channelID.equals(wheelChannelID)) {
				if (lowCaseTxt.equals("skip")) {
					msg.getChannel().sendMessage("Disabled roll animation :ok_hand:");
					isAnimated = false;
					return true;
				}

				if (lowCaseTxt.equals("unskip")) {
					msg.getChannel().sendMessage("Enabled roll animation :thumbs_up:");
					isAnimated = true;
					return true;
				}

				if (lowCaseTxt.equals("roll")) {
					legendsRoll.rollCharacters("b-roll6 t1", msg.getChannel(), isAnimated);
					return true;
				}
			}

			if (lowCaseTxt.equals("disable roll animation")) {
				msg.getChannel().sendMessage("Disabled");
				isAnimated = false;
				return true;
			}

			if (lowCaseTxt.equals("enable roll animation")) {
				msg.getChannel().sendMessage("Enabled");
				isAnimated = true;
				return true;
			}
		}

		return false;
	}

	private boolean checkTemplateCommands(Message msg, String lowCaseTxt) {
		if (lowCaseTxt.contains("b-template create")) {
			templates.writeTemplate(lowCaseTxt, msg.getChannel());
			return true;
		}

		if (lowCaseTxt.contains("b-template remove")) {
			templates.removeTemplate(lowCaseTxt, msg.getChannel(), isOwner(msg));
			return true;
		}

		if (lowCaseTxt.contains("b-template send")) {
			templates.sendData(msg.getChannel());
			return true;
		}

		if (lowCaseTxt.contains("b-template lock") && isOwner(msg)) {
			try {
				int x = Integer.parseInt(lowCaseTxt.replaceAll("\\D", ""));
				templates.setLockedDataEndIndex(x);
				templates.writeData(msg.getChannel(), false);
			} catch (Exception e1) {
			}

			return true;
		}

		return false;
	}

	private void checkChannelTimer(String channelID) {
		try {
			if (channelID.equals(egubotWebhook.getChannel().get().getIdAsString())) {

				deadChatTimer.cancelRecurringTimer();
				deadChatTimer.setStartTime(null, null);
				deadChatTimer.sendScheduledMessage(egubotWebhook, "Dead chat", true);

			}
		} catch (Exception e1) {

		}
	}

	private boolean isOwner(Message msg) {
		boolean isOwner = false;
		if (msg.getAuthor().isBotOwner() || msg.getAuthor().isTeamMember()) {
			isOwner = true;
		}
		return isOwner;
	}

	public void shutdown() {
		executorService.shutdown();
		try {
			if (deadChatTimer != null)
				deadChatTimer.terminateTimer();
			if (testTimer != null)
				testTimer.terminateTimer();
			if (userTargetTimer != null)
				userTargetTimer.terminateTimer();
		} catch (Exception e) {

		}
		try {
			if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
				// If tasks don't finish within time, forceful shutdown
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
	}

	private String replaceAttachmentText(MessageCreateEvent e, String msgText) {
		StringBuilder st2 = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				e.getMessage().getAttachments().get(0).asInputStream(), StandardCharsets.UTF_8))) {
			String st;

			while ((st = br.readLine()) != null) {
				st2.append(st + "\n");
			}

		} catch (IOException e2) {

		}

		return msgText.replace("[attachment text replace]", st2);
	}

	@Override
	public int getShutdownPriority() {
		// TODO Auto-generated method stub
		return 10;
	}

}