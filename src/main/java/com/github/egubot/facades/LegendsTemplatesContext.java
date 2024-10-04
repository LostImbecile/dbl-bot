package com.github.egubot.facades;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.RollTemplates;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Shutdownable;

public class LegendsTemplatesContext implements Shutdownable {
	private static Map<Long, RollTemplates> templatesMap = new ConcurrentHashMap<>();
	private static RollTemplates defaultTemplates;

	public static void initialise() throws IOException {
		defaultTemplates = new RollTemplates();
	}

	public static List<String> getRollTemplates(Message msg) {
		RollTemplates templates = getTemplates(msg);
		return templates == null ? null : templates.getRollTemplates();
	}

	public static List<String> getDefaultRollTemplates() {
		RollTemplates templates = getDefaultTemplates();
		return templates == null ? null : templates.getRollTemplates();
	}

	private static RollTemplates getDefaultTemplates() {
		return defaultTemplates;
	}

	public static void shutdownStatic() {
		Map<Long, RollTemplates> map = templatesMap;
		templatesMap = null;
		if (map == null)
			return;
		for (RollTemplates templates : map.values()) {
			templates.shutdown();
		}
		if (defaultTemplates != null)
			defaultTemplates.shutdown();
	}

	@Override
	public void shutdown() {
		shutdownStatic();
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

	public static RollTemplates getTemplates(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		if (serverID == -1) {
			return null;
		}
		return templatesMap.computeIfAbsent(serverID, k -> {
			RollTemplates templates = new RollTemplates(serverID);
			templates.setData(new ArrayList<>(getDefaultRollTemplates()));
			return templates;
		});
	}
}
