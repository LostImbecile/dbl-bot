package com.github.egubot.main;

import java.awt.GraphicsEnvironment;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.github.egubot.gui.GUIApplication;
import com.github.egubot.storage.ConfigManager;

public class Run {
	private static String[] args;
	private static String title;

	public static void main(String[] args) {
		try {
			Run.setArgs(args);
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

		getCmdTitle(args);
		
		// If the bot isn't already in a console it runs the main method
		if (console == null && !GraphicsEnvironment.isHeadless() && getJarName().contains(".jar")) {
			try {
				Runtime.getRuntime().exec(getRunCommand(args, getTitle()));
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

	public static void getCmdTitle(String[] args) {
		setTitle("Discord Bot");
		for (String arg : args) {
			if (arg.toLowerCase().contains("title:")) {
				setTitle(arg.replaceAll("[-\"]", "").replaceAll("(?i)title:", ""));
				break;
			}
		}
		if (getTitle().isBlank()) {
			setTitle("Discord Bot");
		}
	}

	public static String[] getRunCommand(String[] args, String title) {
		return new String[] { "cmd", "/K", "Start \"" + title + "\" java -Xms100m -Xmx800m -jar " + getJarName() + " "
				+ String.join(" ", args) + "&& exit", };
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

	public static String getTitle() {
		return title;
	}

	public static void setTitle(String title) {
		Run.title = title;
	}

}
