package com.github.egubot.main;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.exception.MissingIntentException;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;

import com.github.egubot.features.SendMessagesFromConsole;
import com.github.egubot.handlers.LostConnectionHandler;
import com.github.egubot.handlers.MessageCreateEventHandler;
import com.github.egubot.handlers.ReconnectEventHandler;
import com.github.egubot.handlers.ResumeEventHandler;
import com.github.egubot.shared.Shared;

public class Main {
	private static final Logger logger = LogManager.getLogger(Main.class.getName());

	/*
	 * You can create your own bot and gets its token from here:
	 * https://discord.com/developers/applications
	 * 
	 * Discord (bot) documentation:
	 * https://discord.com/developers/docs/intro
	 * 
	 * I'm using Javacord, you can read some of its details here:
	 * https://javacord.org/wiki/
	 * https://github.com/Javacord/Javacord
	 * 
	 * Javacord documentation:
	 * https://javadoc.io/doc/org.javacord/javacord-api
	 */
	public static void main(String[] args) throws IOException {
		FallbackLoggerConfiguration.setDebug(false);
		int exitCode = 0;

		String arguments = checkArguments(args);

		// Important to have all keys, some will be created for
		// you, and the rest could be ignored.
		KeyManager.checkKeys();
		String token = KeyManager.getToken("Discord_API_Key");
		
		try {
			try {
				// For info about intents check the links at the start of the class
				BotApi.initialise(new DiscordApiBuilder().setToken(token)
						.addIntents(Intent.MESSAGE_CONTENT, Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGES).login()
						.join());

				if (BotApi.getApi().getServers().isEmpty()) {
					System.out.println(
							"You can invite the bot by using the following url:\n" + BotApi.getApi().createBotInvite());
					System.out.println("\nPlease invite it before continuing.");
				}
			} catch (Exception e1) {
				logger.error("Invalid token. Exiting.");
				KeyManager.updateKeys("Discord_API_Key", "-1", KeyManager.tokensFileName);
				Shared.getShutdown().initiateShutdown(1);
			}

			/*
			 * Status message to prevent multiple instances being up at once, bot needs to
			 * send and edit it.
			 * 
			 * I'm using discord to log it, but you should use a cloud service normally.
			 * If you won't give the bot to someone else this isn't needed.
			 * 
			 * Just checking online status doesn't work unless you use a second
			 * bot for it, as the moment you connect status will be online regardless.
			 */
			initialiseStatus();

			if (Shared.getStatus().isOnline() && !Shared.isTestMode()) {
				System.out
						.println("\nAn instance is already online.\n\nIf that isn't the case, type \"restart\" below.");
				String st = Shared.getSystemInput().nextLine();
				if (st.strip().equalsIgnoreCase("restart")) {
					restartMain(args);
				}
			} else {

				System.out.println(
						"You can invite the bot by using the following url:\n" + BotApi.getApi().createBotInvite());

				addListeners();

				setBotOnline(BotApi.getApi());

				checkSendMessagesFromConsoleArg(arguments);
			}
		} catch (MissingIntentException e) {
			logger.fatal("Missing intent. Program will exit.", e);
			exitCode = 1;
		} catch (IOException e) {
			logger.fatal("Fatal Error: Database Not Found. Program will exit.", e);
			exitCode = 1;
		} catch (Exception e) {
			logger.fatal("Fatal uncaught error. Program will exit.", e);
			exitCode = 1;
		} finally {
			Shared.getShutdown().initiateShutdown(exitCode);
		}
	}

	private static String checkArguments(String[] args) {
		// Current arguments:
		// dbl_off , test , sendmessages
		String arguments = String.join(" ", args).toLowerCase();
		Shared.setTestMode(checkTestmode(arguments));
		Shared.setDbLegendsMode(checkDBLegendsMode(arguments));
		return arguments;
	}

	private static void initialiseStatus() {
		StatusManager status = new StatusManager();
		Shared.setStatus(status);
		Shared.getShutdown().registerShutdownable(status);

		// Comment this out if you don't want a status message or
		// configure it as you run the code, the latter is preferred.
		status.checkMessageID();
	}

	private static void restartMain(String[] args) throws IOException {
		System.out.println();
		Shared.getStatus().setStatusOffline();
		Shared.getStatus().disconnect();
		System.out.println("\nRestarting...\n");

		Main.main(args);
	}

	private static void setBotOnline(DiscordApi api) {
		// Refer to the class to change activity or learn how to
		// control it
		Shared.getStatus().changeActivity();
		Shared.getStatus().setStatusOnline();

		String botName = api.getYourself().getName();
		botName = botName.replaceFirst("^\\p{L}", Character.toUpperCase(botName.charAt(0)) + "");

		System.out.println(
				"\n" + botName + " is fully online. Press enter here to exit, or write terminate in the server."
						+ "\nPlease don't exit by closing the console.");
	}

	private static void checkSendMessagesFromConsoleArg(String arguments) {
		/*
		 * This is to use the bot to send your messages
		 * directly from the console. Type "password"
		 * if argument wasn't sent.
		 */
		if (!arguments.contains("sendmessages")) {
			if (Shared.getSystemInput().nextLine().equals("password"))
				SendMessagesFromConsole.start();
		} else {
			SendMessagesFromConsole.start();
		}
	}

	private static void addListeners() throws Exception {
		/*
		 * Listeners, heart of the bot.
		 * Customise these yourself, the classes I made are for
		 * my personal use, best to write your own, but you can
		 * use these as an example.
		 */
		MessageCreateEventHandler messageHandler = new MessageCreateEventHandler();
		Shared.getShutdown().registerShutdownable(messageHandler);
		BotApi.getApi().addMessageCreateListener(messageHandler);
		BotApi.getApi().addReconnectListener(new ReconnectEventHandler());
		BotApi.getApi().addResumeListener(new ResumeEventHandler());
		BotApi.getApi().addLostConnectionListener(new LostConnectionHandler());
	}

	private static boolean checkDBLegendsMode(String arguments) {
		return !arguments.contains("dbl_off");
	}

	private static boolean checkTestmode(String arguments) {
		return arguments.contains("test");
	}

}