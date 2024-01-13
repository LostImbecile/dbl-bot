package com.github.egubot.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
import com.github.egubot.build.OnlineDataManager;
import com.github.egubot.build.RollTemplates;
import com.github.egubot.features.LegendsRoll;
import com.github.egubot.features.LegendsSearch;
import com.github.egubot.features.TimedAction;
import com.github.egubot.gpt2.DiscordAI;
import com.github.egubot.objects.CharacterHash;
import com.github.egubot.objects.Characters;
import com.github.egubot.objects.Tags;
import com.openai.chatgpt.ChatGPT;

public class MessageCreateEventHandler implements MessageCreateListener {

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

	private TimedAction testTimer;
	private TimedAction userTargetTimer;
	private TimedAction deadChatTimer;

	private IncomingWebhook testWebhook;
	private IncomingWebhook egubotWebhook;

	private boolean dbLegendsMode;

	public MessageCreateEventHandler(DiscordApi api, boolean testMode, boolean dbLegendsMode) throws Exception {
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

		initialiseDataStorage();
		initialiseWebhooks();
	}

	@Override
	public void onMessageCreate(MessageCreateEvent e) {
		Message msg = e.getMessage();
		// Regex replaces non-utf8 characters
		String msgText = e.getMessageContent();
		String lowCaseTxt = msgText.toLowerCase();

		try {

			if (checkChatGPTCommands(e, msg, msgText, lowCaseTxt)) {
				return;
			}
			// Ignore bots unless changed
			if (!msg.getAuthor().isRegularUser() && !readBotMessages) {
				return;
			}

			if (lowCaseTxt.equals("terminate")) {
				terminate(e, msg);
			}

			if (lowCaseTxt.equals("refresh")) {
				refresh(e, msg);
				return;
			}

			// This is so I can run a test version and a non-test version at the same time
			if (testMode && !msg.getServer().get().getIdAsString().equals(testServerID))
				return;
			if (!testMode && msg.getServer().get().getIdAsString().equals(testServerID))
				return;

			String channelID = msg.getChannel().getIdAsString();
			String authorID = msg.getAuthor().getIdAsString();

			checkChannelTimer(channelID);

			if (lowCaseTxt.contains("[attachment text replace]") && !e.getMessage().getAttachments().isEmpty()) {
				msgText = replaceAttachmentText(e, msgText);
				lowCaseTxt = msgText.toLowerCase();
			}

			if (checkDBLegendsCommands(e, msgText, lowCaseTxt, channelID)) {
				return;
			}

			if (checkTranslateCommands(e, msg, lowCaseTxt)) {
				return;
			}

			if (tryAutoRespondCommands(e, msgText, lowCaseTxt)) {
				return;
			}

			if (testMode && (checkTimerTasks(e, msgText, lowCaseTxt))) {
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

			if (lowCaseTxt.matches("parrot.*")) {
				e.getChannel().sendMessage(msgText.replace("parrot", ""));
			}

			if (lowCaseTxt.equals("ai activate")) {
				isCustomAIOn = true;
				return;
			}

			if (autoRespond.respond(msgText, e)) {
				return;
			}

			if (authorID.equals(userTargetID)) {
				try {
					userTargetTimer.sendDelayedRateLimitedMessage(e.getChannel(), sirioMsgContent, true);
				} catch (Exception e1) {

				}
			}

			checkCustomAI(e, msgText, lowCaseTxt, channelID);

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
										LegendsDatabase.getWebsiteAsInputStream(), false).writeData(null);

							} catch (Exception e) {

							}

						}).start();
					} else {
						System.out.println("Character database was successfully built!");
					}

				} else {
					System.err.println("Character database missing information. Trying Backup...");

					OnlineDataManager backup = new OnlineDataManager(api, "Website_Backup_Msg_ID", "Website Backup",
							LegendsDatabase.getWebsiteAsInputStream(), true);

					legendsWebsite = new LegendsDatabase(backup.getData());
					if (!legendsWebsite.isDataFetchSuccessfull()) {
						System.err.println("Warning: Backup is also missing information.");
					}
				}

			} catch (IOException e) {
				System.err.println("\nFailed to build character database. Relevant commands will be inactive.");
				isRollCommandActive = false;
			}

			if (isRollCommandActive) {
				templates = new RollTemplates(api, legendsWebsite);

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
					testTimer = new TimedAction(5000, null, null);

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

					deadChatTimer = new TimedAction((long) (length * HOUR), null,
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
					userTargetTimer = new TimedAction(6 * HOUR, null, lastMessageDate);
				} catch (Exception e) {
					userTargetTimer = null;
				}
			}
		});
		t.start();
	}

	private boolean checkTranslateCommands(MessageCreateEvent e, Message msg, String lowCaseTxt) {
		if(isTranslateOn) {
			try {
				if(lowCaseTxt.length() < 140 && !translate.detectLanguage(lowCaseTxt, true).matches("(?:en)|(?:Error.*)")) {
					e.getChannel().sendMessage(translate.post(lowCaseTxt));
				}
			} catch (IOException e1) {
			}
		}
		if (lowCaseTxt.matches("b-translate.*")) {
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
				}
				return true;
			}
			if (lowCaseTxt.contains("b-translate languages")) {
				try {
					e.getChannel().sendMessage(IOUtils.toInputStream(Translate.getLanguages(), StandardCharsets.UTF_8),
							"languages.txt");
				} catch (IOException e1) {
					e.getChannel().sendMessage("Failed to send :thumbs_down");
				}
				return true;
			}

			try {
				if (msg.getMessageReference().isPresent()) {

					e.getChannel().sendMessage(
							translate.post(msg.getMessageReference().get().getMessage().get().getContent()));

				} else {
					String st = lowCaseTxt.replace("b-translate", "").strip();

					e.getChannel().sendMessage(translate.post(st));

				}
			} catch (IOException e1) {
				e.getChannel().sendMessage("Failed to connect to endpoint :thumbs_down:");
			}
			return true;
		}
		return false;
	}

	private boolean checkTimerTasks(MessageCreateEvent e, String msgText, String lowCaseTxt) {
		if (lowCaseTxt.equals("start task")) {
			try {
				testWebhook.updateAvatar(e.getMessageAuthor().getAvatar()).join();
				testWebhook.updateChannel(e.getChannel().asServerTextChannel().get());
				testTimer.setStartTime(null, e.getMessage().getCreationTimestamp());
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

	private void refresh(MessageCreateEvent e, Message msg) throws Exception {
		if (!msg.getAuthor().getIdAsString().equals(userTargetID)) {
			e.getChannel().sendMessage("Refreshing...").join();
			System.out.println("\nRefreshing MessageCreateEventHandler.");

			initialiseDataStorage();
			initialiseWebhooks();

			e.getChannel().sendMessage("Refreshed :ok_hand:");
		} else {
			e.getChannel().sendMessage("no");
		}
	}

	private void terminate(MessageCreateEvent e, Message msg) {
		boolean isOwner = isOwner(msg);

		if (msg.getServer().get().getOwnerId() == msg.getAuthor().getId() || isOwner) {
			e.getChannel().sendMessage("Terminating...").join();
			System.out.println("\nTerminate message invoked.");
			new StatusManager(api, testMode).exit();

			System.exit(0);
		} else {
			e.getChannel().sendMessage("no");
		}
	}

	private void checkCustomAI(MessageCreateEvent e, String msgText, String lowCaseTxt, String channelID) {
		Thread newThread;
		// Personal AI, refer to its class for info
		if (isCustomAIOn) {
			if (lowCaseTxt.equals("ai terminate")) {
				isCustomAIOn = false;
				return;
			}

			if (testMode || channelID.equals(gpt2ChannelID) || lowCaseTxt.matches("ai.*")) {
				String st = msgText;
				newThread = new Thread(new Runnable() {
					public void run() {
						try {
							String aiUrl = "http://localhost:5000"; // Update with your AI server URL
							try (DiscordAI discordAI = new DiscordAI(aiUrl)) {
								String input = st.replaceAll("^[Aa][Ii]", "").strip();

								String generatedText = discordAI.generateText(input);

								if (!generatedText.matches("Error:.*")) {
									e.getChannel().sendMessage(generatedText);
								} else if (!generatedText.contains("Connect to localhost:5000"))
									System.err.println("AI Response: " + generatedText);
							}
						} catch (Exception e) {
							// not worth bothering with
						}
					}
				});
				newThread.start();
			}
		}
	}

	private boolean checkChatGPTCommands(MessageCreateEvent e, Message msg, String msgText, String lowCaseTxt) {
		Thread newThread;
		if (lowCaseTxt.equals("chatgpt activate")) {
			isChatGPTOn = true;
			return true;
		}

		if (isChatGPTOn) {
			if (lowCaseTxt.equals("chatgpt deactivate")) {
				isChatGPTOn = false;
				return true;
			}

			if (lowCaseTxt.matches("gpt.*") || e.getChannel().getIdAsString().equals(chatGPTActiveChannelID)) {
				if (lowCaseTxt.equals("gpt clear")) {
					e.getChannel().sendMessage("Conversation cleared :thumbsup:");
					chatgptConversation.clear();
					return true;
				}

				if (lowCaseTxt.equals("gpt channel on")) {
					chatGPTActiveChannelID = e.getChannel().getIdAsString();
					return true;
				}

				if (lowCaseTxt.equals("gpt channel off")) {
					chatGPTActiveChannelID = "";
					return true;
				}

				String st = msgText;
				newThread = new Thread(new Runnable() {
					public void run() {
						try {
							String[] response = ChatGPT.chatGPT(st, msg.getAuthor().getName(), chatgptConversation);
							e.getChannel().sendMessage(response[0]);
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
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				newThread.start();
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

	private boolean tryAutoRespondCommands(MessageCreateEvent e, String msgText, String lowCaseTxt) {

		if (lowCaseTxt.matches("b-.*")) {
			boolean isOwner = isOwner(e.getMessage());
			if (lowCaseTxt.contains("b-response create")) {
				if (!lowCaseTxt.contains("sleep"))
					autoRespond.writeResponse(msgText, e.getChannel(), isOwner);
				else
					e.getChannel().sendMessage("nah");
				return true;
			}

			if (lowCaseTxt.contains("b-response remove")) {
				autoRespond.removeResponse(msgText, e.getChannel(), isOwner);
				return true;
			}

			if (lowCaseTxt.equals("b-response send")) {
				autoRespond.sendData(e.getChannel());
				return true;
			}

			if (lowCaseTxt.contains("b-response lock") && isOwner) {
				try {
					int x = Integer.parseInt(lowCaseTxt.replaceAll("\\D", ""));
					autoRespond.setLockedDataEndIndex(x);
					autoRespond.writeData(e.getChannel());
				} catch (Exception e1) {

				}

				return true;
			}
		}

		return false;
	}

	private boolean checkDBLegendsCommands(MessageCreateEvent e, String msgText, String lowCaseTxt, String channelID) {
		Thread newThread;
		if (isRollCommandActive) {
			if (lowCaseTxt.matches("b-.*")) {

				if (lowCaseTxt.contains("b-template create")) {
					templates.writeTemplate(lowCaseTxt, e.getChannel());
					return true;
				}

				if (lowCaseTxt.contains("b-template remove")) {
					templates.removeTemplate(lowCaseTxt, e.getChannel(), isOwner(e.getMessage()));
					return true;
				}

				if (lowCaseTxt.contains("b-template send")) {
					templates.sendData(e.getChannel());
					return true;
				}

				if (lowCaseTxt.contains("b-template lock") && isOwner(e.getMessage())) {
					try {
						int x = Integer.parseInt(lowCaseTxt.replaceAll("\\D", ""));
						templates.setLockedDataEndIndex(x);
						templates.writeData(e.getChannel());
					} catch (Exception e1) {
					}

					return true;
				}

				/*
				 * Features that introduce a long delay with no consequence
				 * should be run in a separate thread, or the bot won't be
				 * able to do anything till they're done.
				 */
				try {
					if (lowCaseTxt.contains("b-search")) {
						String st = msgText;
						newThread = new Thread(() ->

						legendsSearch.search(st, api, e.getChannel())

						);
						newThread.start();
						return true;
					}

					if (lowCaseTxt.contains("b-roll")) {
						String st = lowCaseTxt;
						newThread = new Thread(() ->

						legendsRoll.rollCharacters(st, e.getChannel(), isAnimated)

						);
						newThread.start();
						return true;
					}

				} catch (Exception e1) {
					e.getChannel().sendMessage("Filter couldn't be parsed <:huh:1184466187938185286>");
					return true;
				}

				if (lowCaseTxt.equals("b-character send")) {
					Characters.sendCharacters(e.getChannel(), legendsWebsite.getCharactersList());
					return true;
				}

				// Prints empty IDs so I can manually change very large IDs to smaller ones
				// Saves time or memory when working with a hash.
				if (lowCaseTxt.equals("b-character printemptyids")) {
					CharacterHash.printEmptyIDs(legendsWebsite.getCharactersList());
					return true;
				}

				if (lowCaseTxt.equals("b-tag send")) {
					Tags.sendTags(e.getChannel(), legendsWebsite.getTags());
					return true;
				}

			}

			if (channelID.equals(wheelChannelID)) {
				if (lowCaseTxt.equals("skip")) {
					e.getChannel().sendMessage("Disabled roll animation :ok_hand:");
					isAnimated = false;
					return true;
				}

				if (lowCaseTxt.equals("unskip")) {
					e.getChannel().sendMessage("Enabled roll animation :thumbs_up:");
					isAnimated = true;
					return true;
				}

				if (lowCaseTxt.equals("roll")) {
					newThread = new Thread(() ->

					legendsRoll.rollCharacters("b-roll6 t1", e.getChannel(), isAnimated)

					);
					newThread.start();
					return true;
				}
			}

			if (lowCaseTxt.equals("disable roll animation")) {
				e.getChannel().sendMessage("Disabled");
				isAnimated = false;
				return true;
			}

			if (lowCaseTxt.equals("enable roll animation")) {
				e.getChannel().sendMessage("Enabled");
				isAnimated = true;
				return true;
			}
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

	private String replaceAttachmentText(MessageCreateEvent e, String msgText) {
		StringBuilder st2 = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				e.getMessage().getAttachments().get(0).asInputStream(), StandardCharsets.UTF_8))) {
			String st;

			while ((st = br.readLine()) != null) {
				st2.append(st);
			}

		} catch (IOException e2) {

		}

		return msgText.replace("[attachment text replace]", st2);
	}

}