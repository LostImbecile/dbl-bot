package com.github.egubot.main;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.shared.Shared;

public class Restart {
	private Restart() {
	}

	public static void restart() throws IOException {
		// Clean up resources if necessary

		// Restart the application
		String[] args = Run.getArgs();
		ProcessBuilder builder = new ProcessBuilder(Run.getRunCommand(args, Run.getTitle()));

		StreamRedirector.println("", "");
		Shared.getStatus().setStatusOffline();
		Shared.getStatus().disconnect();
		StreamRedirector.println("", "\nRestarting...\n");
		CompletableFuture.runAsync(() -> {
			try {
				if (Run.getJarName().contains(".jar"))
					builder.start();
				else
					Main.main(args);

			} catch (IOException e) {
				Main.logger.error(e);
			}

			System.exit(0);
		});
	}

}
