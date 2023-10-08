package com.github.egubot.main;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.github.egubot.build.AutoRespond;
import com.github.egubot.build.LegendsDatabase;
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
	private String siriosID = KeyManager.getID("Sirio_User_ID");
	private String eguID = KeyManager.getID("Egu_Server_ID");
	private String sirioMsgID = KeyManager.getID("Sirio_Msg_ID");
	private String sirioChannelID = KeyManager.getID("Sirio_Msg_Channel_ID");
	private String gpt2ChannelID = KeyManager.getID("GPT2_Channel_ID");
	private String timerLengthMessage = KeyManager.getID("Dead_Chat_Timer_Msg");
	private String sirioMsgContent;

	private boolean isAnimated = true;
	private boolean isCustomAIOn = true;

	private String chatGPTActiveChannelID = "";

	private static ArrayList<String> chatgptConversation = new ArrayList<>(0);

	private DiscordApi api;

	private LegendsDatabase legendsWebsite;
	private LegendsRoll legendsRoll;
	private AutoRespond autoRespond;
	private RollTemplates templates;
	private LegendsSearch legendsSearch;

	private boolean isRollCommandActive = true;
	private boolean testMode;

	private TimedAction testTimer;
	private TimedAction sirioTimer;
	private TimedAction deadChatTimer;

	private IncomingWebhook testWebhook;
	private IncomingWebhook egubotWebhook;

	public MessageCreateEventHandler(DiscordApi api, boolean testMode) throws Exception {
		/*
		 * I store templates, responses and all that stuff online in case someone
		 * else uses the bot on their end, so the data needs to be initialised
		 * from an online storage each time. I used discord for this, a cloud
		 * services could do better.
		 */
		this.api = api;
		this.testMode = testMode;
		this.isAnimated = !testMode;

		initialiseDataStorage();
		initialiseWebhooks();
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
					egubotWebhook.updateAvatar(api.getUserById(siriosID).get().getAvatar());
					egubotWebhook
							.updateName(api.getUserById(siriosID).get().getDisplayName(api.getServerById(eguID).get()));
				} catch (Exception e) {
					egubotWebhook = null;
					System.err.println("\nFailed to fetch egubot's webhook.");
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
						length = Double.parseDouble(
								api.getMessageById(timerLengthMessage, api.getTextChannelById(sirioChannelID).get())
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
					sirioMsgContent = api.getMessageById(sirioMsgID, api.getTextChannelById(sirioChannelID).get()).get()
							.getContent();

					List<ServerChannel> channels = api.getServerById(eguID).get().getChannels();
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
					sirioTimer = new TimedAction(6 * HOUR, null, lastMessageDate);
				} catch (Exception e) {
					sirioTimer = null;
				}
			}
		});
		t.start();
	}

	private void initialiseDataStorage() throws Exception {
		/*
		 * You'll want to make your own classes for this stuff usually,
		 * as these are bot/feature specific, autorespond however
		 * can be used for all bots.
		 * 
		 */
		autoRespond = new AutoRespond(api);

		// Fetches data from the legends website and initialises
		// classes that are based on it, or doesn't if that fails
		try {
			System.out.println("\nFetching characters from dblegends.net...");
			legendsWebsite = new LegendsDatabase();
			System.out.println("Character database was successfully built!");
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

	@Override
	public void onMessageCreate(MessageCreateEvent e) {
		Thread newThread;
		Message msg = e.getMessage();
		boolean isOwner = false;
		// Regex replaces non-utf8 characters
		String msgText = e.getMessageContent().replaceAll("[^\\x00-\\x7F]", "");
		String lowCaseTxt = msgText.toLowerCase();

		try {

			if (msg.getAuthor().isYourself()) {
				chatgptConversation.add(ChatGPT.reformatInput(msgText, "assistant"));
				return;
			}

			if (!msg.getAuthor().isUser())
				return;

			try {
				if (e.getChannel().getIdAsString().equals(egubotWebhook.getChannel().get().getIdAsString())) {

					deadChatTimer.cancelRecurringTimer();
					deadChatTimer.setStartTime(null, null);
					deadChatTimer.sendScheduledMessage(egubotWebhook, "Dead chat", true);

				}
			} catch (Exception e1) {

			}

			if (msg.getAuthor().isBotOwner() || msg.getAuthor().isTeamMember()) {
				isOwner = true;
			}
			// This is so I can run a test version and non-test version at the same time
			if (testMode && !msg.getServer().get().getIdAsString().equals(testServerID))
				return;
			if (!testMode && msg.getServer().get().getIdAsString().equals(testServerID))
				return;

			if (msgText.equalsIgnoreCase("terminate")) {

				if (!msg.getAuthor().getIdAsString().equals(siriosID)) {
					e.getChannel().sendMessage("Terminating...").join();
					System.out.println("\nTerminate message invoked.");
					new StatusManager(api, testMode).exit();

					System.exit(0);
				} else {
					e.getChannel().sendMessage("no");
				}
			}

			if (isRollCommandActive) {
				if (lowCaseTxt.matches("b-template create(.*)")) {
					templates.writeTemplate(lowCaseTxt, e.getChannel());
					return;
				}

				if (lowCaseTxt.matches("b-template remove(.*)")) {
					templates.removeTemplate(lowCaseTxt, e.getChannel(), isOwner);
					return;
				}

				if (lowCaseTxt.matches("b-template send(.*)")) {
					templates.sendData(e.getChannel());
					return;
				}

				/*
				 * Features that introduce a long delay with no consequence
				 * should be run in a separate thread, or the bot won't be
				 * able to do anything till they're done.
				 */
				try {
					if (lowCaseTxt.matches("b-search(.*)")) {
						newThread = new Thread(() ->

						legendsSearch.search(msgText, api, e.getChannel())

						);
						newThread.start();
						return;
					}

					if (lowCaseTxt.matches("disable roll animation(.*)")) {
						e.getChannel().sendMessage("Disabled");
						isAnimated = false;
						return;
					}

					if (lowCaseTxt.matches("enable roll animation(.*)")) {
						e.getChannel().sendMessage("Enabled");
						isAnimated = true;
						return;
					}

					if (lowCaseTxt.matches("b-roll(.*)")) {
						newThread = new Thread(() ->

						legendsRoll.rollCharacters(lowCaseTxt, e.getChannel(), isAnimated)

						);
						newThread.start();
						return;
					}
				} catch (Exception e1) {
					e.getChannel().sendMessage("Filter couldn't be parsed <:gokuhuh:1009185335881768970>");
					return;
				}
			}

			if (lowCaseTxt.matches("b-response create(.*)")) {
				if (!lowCaseTxt.contains("sleep"))
					autoRespond.writeResponse(msgText, e.getChannel());
				else
					e.getChannel().sendMessage("nah");
				return;
			}

			if (lowCaseTxt.matches("b-response remove(.*)")) {
				autoRespond.removeResponse(msgText, e.getChannel(), isOwner);
				return;
			}

			if (lowCaseTxt.matches("b-response send(.*)")) {
				autoRespond.sendData(e.getChannel());
				return;
			}

			if (lowCaseTxt.matches("b-tag send.*")) {
				Tags.sendTags(e.getChannel(), legendsWebsite.getTags());
			}

			if (lowCaseTxt.matches("b-character send.*")) {
				Characters.sendCharacters(e.getChannel(), legendsWebsite.getCharactersList());
			}

			// Prints empty IDs so I can manually change very large IDs to smaller ones
			// Saves time or memory when working with a hash.
			if (lowCaseTxt.matches("b-character printemptyids.*")) {
				CharacterHash.printEmptyIDs(legendsWebsite.getCharactersList());
			}

			if (testMode && lowCaseTxt.matches("start task(.*)")) {
				try {
					testWebhook.updateAvatar(e.getMessageAuthor().getAvatar()).join();
					testWebhook.updateChannel(e.getChannel().asServerTextChannel().get());
					testTimer.setStartTime(null, e.getMessage().getCreationTimestamp());
					testTimer.sendScheduledMessage(testWebhook, msgText, true);
				} catch (Exception e1) {
				}
				return;
			}

			if (lowCaseTxt.matches("cancel task(.*)")) {
				try {
					testTimer.cancelRecurringTimer();
				} catch (Exception e1) {

				}
				return;
			}

			if (lowCaseTxt.matches("gpt clear(.*)")) {
				e.getChannel().sendMessage("Conversation cleared :thumbsup:");
				chatgptConversation.clear();
				return;
			}

			chatgptConversation.add(ChatGPT.reformatInput(msgText, msg.getAuthor().getName()));
			if (lowCaseTxt.matches("gpt activate channel.*")) {
				chatGPTActiveChannelID = e.getChannel().getIdAsString();
				return;
			}

			if (lowCaseTxt.matches("gpt deactivate.*")) {
				chatGPTActiveChannelID = "";
				return;
			}

			if (lowCaseTxt.matches("gpt(.*)") || chatGPTActiveChannelID.equals(e.getChannel().getIdAsString())) {

				newThread = new Thread(new Runnable() {
					public void run() {

						try {
							String[] response = ChatGPT.chatGPT(msgText, msg.getAuthor().getName(),
									chatgptConversation);
							e.getChannel().sendMessage(response[0]);

							// 4096 Token limit that includes sent messages
							// Important to stay under it
							if (Integer.parseInt(response[1]) > 3300) {

								for (int i = 0; i < 5; i++) {
									chatgptConversation.remove(0);
								}
							}
						} catch (Exception e) {
						}
					}
				});
				newThread.start();
				return;
			}

			if (lowCaseTxt.matches("ai terminate(.*)")) {
				isCustomAIOn = false;
				return;
			}

			if (autoRespond.respond(msgText, e))
				return;

			if (e.getMessageAuthor().getIdAsString().equals(siriosID)) {
				try {
					sirioTimer.sendDelayedRateLimitedMessage(e.getChannel(), sirioMsgContent, true);
				} catch (Exception e1) {

				}
			}

			// Personal AI, refer to its class for info
			if (isCustomAIOn) {
				if (testMode || e.getChannel().getIdAsString().equals(gpt2ChannelID) || lowCaseTxt.matches("ai(.*)")) {
					newThread = new Thread(new Runnable() {
						public void run() {
							try {
								String aiUrl = "http://localhost:5000"; // Update with your AI server URL
								try (DiscordAI discordAI = new DiscordAI(aiUrl)) {
									String input = msgText;

									if (lowCaseTxt.matches("ai(.*)"))
										input = input.replaceFirst("ai", "").strip();

									String generatedText = discordAI.generateText(input);

									if (!generatedText.matches("Error:(.*)")) {
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

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}