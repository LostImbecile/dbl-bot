package com.github.egubot.main;

import java.awt.GraphicsEnvironment;
import java.io.Console;

public class Run {

	public static void main(String[] args) {
		try {
			/*
			 * Runs cmd through another cmd and launches the bot
			 * for info go here:
			 * https://learn.microsoft.com/en-us/windows-server/administration/windows-
			 * commands/cmd
			 */ Console console = System.console();

			String title = "Discord Bot";
			for (int i = 0; i < args.length; i++) {
				if (args[i].toLowerCase().contains("title:")) {
					title = args[i].replaceAll("[-\"]", "").replaceAll("(?i)title:", "");
					break;
				}
			}
			if (title.equals("")) {
				title = "Discord Bot";
			}
			// If the bot isn't already in a console it runs the main method
			if (console == null && !GraphicsEnvironment.isHeadless()) {
				try {
					Runtime.getRuntime().exec(
							new String[] { "cmd", "/K", "Start \"" + title + "\" java -Xms40m -Xmx200m -jar bot.jar "
									+ String.join(" ", args) + "&& exit", });
				} catch (Exception e) {
					// If you're not on windows just run the bot through the
					// terminal or create a shell script for it.
					System.exit(0);
				}
			} else {
				// Arguments you send are handed down to the main class normally
				Main.main(args);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
