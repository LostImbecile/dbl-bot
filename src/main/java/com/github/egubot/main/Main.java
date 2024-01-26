package com.github.egubot.main;

import java.io.IOException;
import java.util.Scanner;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.exception.MissingIntentException;

import com.github.egubot.features.SendMessagesFromConsole;

public class Main {
	static ShutdownManager shutdown = new ShutdownManager();
	static StatusManager status = new StatusManager();
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
		shutdown.registerShutdownable(status);
		DiscordApi api = null;	

		// Current arguments:
		// dbl_off , test , sendmessages
		String arguments = String.join(" ", args).toLowerCase();
		boolean testMode = false;
		int exitCode = 0;
		
		if (arguments.contains("test"))
			testMode = true;

		// Important to have all keys, some will be created for
		// you, and the rest could be ignored.
		KeyManager.checkKeys();
		String token = KeyManager.getToken("Discord_API_Key");

		try {

			try {
				// For info about intents check the links at the start of the class
				api = new DiscordApiBuilder().setToken(token)
						.addIntents(Intent.MESSAGE_CONTENT, Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGES).login().join();
				if (api.getServers().isEmpty()) {
					System.out.println("You can invite the bot by using the following url:\n" + api.createBotInvite());
					System.out.println("\nPlease invite it before continuing.");
				}
			} catch (Exception e1) {
				System.err.println("\nInvalid token. Exiting.");
				KeyManager.updateKeys("Discord_API_Key", "-1", KeyManager.tokensFileName);
				System.exit(1);
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
			status.setApi(api);
			status.setTestMode(testMode);

			// Comment this out if you don't want a status message or
			// configure it as you run the code, the latter is preferred.
			status.checkMessageID();

			Scanner input = new Scanner(System.in);

			if (status.isOnline() && !testMode) {
				System.out
						.println("\nAn instance is already online.\n\nIf that isn't the case, type \"restart\" below.");
				String st = input.nextLine();
				if (st.strip().equalsIgnoreCase("restart")) {
					System.out.println();
					status.setStatusOffline();
					status.disconnect();
					System.out.println("\nRestarting...\n");

					Main.main(args);
				}
			} else {

				System.out.println("You can invite the bot by using the following url:\n" + api.createBotInvite());

				boolean dbLegendsMode;
				if (arguments.contains("dbl_off")) {
					dbLegendsMode = false;
				} else {
					dbLegendsMode = true;
				}
				/*
				 * Listeners, heart of the bot.
				 * Customise these yourself, the classes I made are for
				 * my personal use, best to write your own, but you can
				 * use these as an example.
				 */
				MessageCreateEventHandler messageHandler = new MessageCreateEventHandler(api, shutdown, testMode,
						dbLegendsMode);
				shutdown.registerShutdownable(messageHandler);
				api.addMessageCreateListener(messageHandler);
				api.addReconnectListener(new ReconnectEventHandler(testMode, api));
				api.addResumeListener(new ResumeEventHandler(testMode, api));
				api.addLostConnectionListener(new LostConnectionHandler());

				// Refer to the class to change activity or learn how to
				// control it
				status.changeActivity();

				status.setStatusOnline();

				String botName = api.getYourself().getName();
				botName = botName.replaceFirst("^\\p{L}", Character.toUpperCase(botName.charAt(0)) + "");

				System.out.println(
						"\n" + botName + " is fully online. Press enter here to exit, or write terminate in the server."
								+ "\nPlease don't exit by closing the console.");

				/*
				 * This is to use the bot to send your messages
				 * directly from the console.
				 */
				if (!arguments.contains("sendmessages")) {
					if (input.nextLine().equals("password"))
						SendMessagesFromConsole.start(api, input);

				} else {
					SendMessagesFromConsole.start(api, input);
				}
			}

		} catch (MissingIntentException e) {
			System.err.println("\nMissing intent. Program will exit.");
			e.printStackTrace();
			exitCode = 1;
		} catch (IOException e) {
			System.err.println("\nFatal Error: Database Not Found. Program will exit.");
			e.printStackTrace();
			exitCode = 1;
		} catch (Exception e) {
			System.err.println("\nFatal uncaught error. Program will exit.");
			e.printStackTrace();
			exitCode = 1;
		} finally {
			shutdown.initiateShutdown(exitCode);
		}
	}

}