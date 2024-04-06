package com.github.egubot.main;

import java.awt.GraphicsEnvironment;
import java.io.Console;
import java.io.IOException;

import com.github.egubot.gui.GUIApplication;
import com.github.egubot.storage.ConfigManager;

public class Run {

	public static void main(String[] args) {
		try {

			// For the GUI, not implemented so keep commented
			if (ConfigManager.getBooleanProperty("CommandLine_Version"))
				runInConsole(args);
			else
				GUIApplication.main(args);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void runInConsole(String[] args) throws IOException {
		/*
		 * Runs cmd through another cmd and launches the bot
		 * for info go here:
		 * https://learn.microsoft.com/en-us/windows-server/administration/windows-
		 * commands/cmd
		 * 
		 */
		Console console = System.console();

		String title = "Discord Bot";
		for (String arg : args) {
			if (arg.toLowerCase().contains("title:")) {
				title = arg.replaceAll("[-\"]", "").replaceAll("(?i)title:", "");
				break;
			}
		}
		if (title.isBlank()) {
			title = "Discord Bot";
		}
		// If the bot isn't already in a console it runs the main method
		if (console == null && !GraphicsEnvironment.isHeadless()) {
			try {
				Runtime.getRuntime().exec(new String[] { "cmd", "/K", "Start \"" + title
						+ "\" java -Xms100m -Xmx800m -jar bot.jar " + String.join(" ", args) + "&& exit", });
			} catch (Exception e) {
				// If you're not on windows just run the bot through the
				// terminal or create a shell script for it.
				System.exit(0);
			}
		} else {
			// Arguments you send are handed down to the main class normally
			Main.main(args);
		}
	}

}
