package com.github.egubot.main;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.shared.Shared;

public class Restart {
	private Restart() {
	}

	public static void restart() {
		if (Run.getJarName().contains(".jar")) {
			StreamRedirector.println("info", "\nRestarting...\n");

			CompletableFuture.runAsync(() -> {
				try {
					ProcessBuilder builder = new ProcessBuilder(Run.getRunCommand());
					builder.start();
				} catch (IOException e) {
					Run.logger.error(e);
				}

			}).thenRun(() -> {
	            // Shut down the current process after starting the new one
	            Shared.getShutdown().initiateShutdown(2);
	        });
			
		}
	}

}
