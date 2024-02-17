package com.github.egubot.facades;

import java.io.IOException;
import java.util.List;

import com.github.egubot.build.RollTemplates;
import com.github.egubot.interfaces.Shutdownable;

public class LegendsTemplatesContext implements Shutdownable {
	private static RollTemplates templates = null;

	public static void initialise() throws IOException {
		templates = new RollTemplates();
	}

	public static List<String> getRollTemplates() {
		return templates.getRollTemplates();
	}

	public static void shutdownStatic() {
		if (templates != null)
			templates.shutdown();
	}

	@Override
	public void shutdown() {
		shutdownStatic();
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

	public static RollTemplates getTemplates() {
		return templates;
	}
}
