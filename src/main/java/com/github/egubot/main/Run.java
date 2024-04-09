package com.github.egubot.main;

import java.awt.GraphicsEnvironment;
import java.io.Console;
import java.io.File;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.exception.MissingIntentException;

import com.github.egubot.gui.GUIApplication;
import com.github.egubot.shared.Shared;
import com.github.egubot.storage.ConfigManager;

public class Run {
	public static final Logger logger = LogManager.getLogger(Run.class.getName());
	private static String[] args;

	public static void main(String[] args) {
		Run.setArgs(args);
		int exitCode = 0;
		try {
			// For the GUI, not implemented so keep commented
			if (ConfigManager.getBooleanProperty("CommandLine_Version"))
				runInConsole(args);
			else
				GUIApplication.main(args);

		} catch (MissingIntentException e) {
			logger.fatal("Missing intent. Program will exit.", e);
			exitCode = 1;
		} catch (Exception e) {
			logger.fatal("Fatal uncaught error. Program will exit.", e);
			exitCode = 1;
		} finally {
			Shared.getShutdown().initiateShutdown(exitCode);
		}
	}

	public static void runInConsole(String[] args) {
		/*
		 * Runs cmd through another cmd and launches the bot
		 * for info go here:
		 * https://learn.microsoft.com/en-us/windows-server/administration/windows-
		 * commands/cmd
		 * 
		 */
		Console console = System.console();

		// If the bot isn't already in a console it runs the main method
		if (console == null && !GraphicsEnvironment.isHeadless() && getJarName().contains(".jar")) {
			try {
				Runtime.getRuntime().exec(getRunInConsoleCommand());
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

	public static String getCmdTitle() {
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

		return title;
	}

	public static String[] getRunInConsoleCommand() {
		return new String[] { "cmd", "/K", "Start", "\"" + getCmdTitle() + "\"", "java", "-Xms100m", "-Xmx800m", "-jar",
				getJarName(), String.join(" ", args), "&& exit" };
	}

	public static String[] getRunCommand() {
		return new String[] { "java", "-Xms100m", "-Xmx800m", "-jar", getJarName(), String.join(" ", args) };
	}

	public static String getJarName() {
		String jar;
		try {
			jar = new File(Run.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName();
		} catch (URISyntaxException e) {
			// default
			jar = "bot.jar";
		}
		return jar;
	}

	public static String[] getArgs() {
		return args;
	}

	public static void setArgs(String[] args) {
		Run.args = args;
	}

}
