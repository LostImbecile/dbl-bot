package com.github.egubot.facades;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.SystemPromptManager;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.shared.Shared;

public class SystemPromptContext implements Shutdownable {
	private static Map<Long, SystemPromptManager> systemPromptMap = new ConcurrentHashMap<>();
	
	static {
		// Register static instance for shutdown cleanup
		Shared.getShutdown().registerShutdownable(new SystemPromptContext());
	}

	private SystemPromptContext() {
	}

	public static void shutdownStatic() {
		Map<Long, SystemPromptManager> map = systemPromptMap;
		systemPromptMap = null;
		if (map == null)
			return;

		for (SystemPromptManager manager : map.values()) {
			if (manager != null) {
				manager.shutdown();
			}
		}
	}

	@Override
	public void shutdown() {
		shutdownStatic();
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

	public static String getSystemPrompt(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		if (serverID == -1) {
			return getDefaultSystemPrompt();
		}
		SystemPromptManager manager = getSystemPromptManager(serverID);
		return manager != null ? manager.getSystemPrompt(serverID) : getDefaultSystemPrompt();
	}

	public static String getSystemPrompt(Long serverId) {
		if (serverId == null) {
			return getDefaultSystemPrompt();
		}
		SystemPromptManager manager = getSystemPromptManager(serverId);
		return manager != null ? manager.getSystemPrompt(serverId) : getDefaultSystemPrompt();
	}

	public static void setSystemPrompt(Message msg, String systemPrompt) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		if (serverID != -1) {
			SystemPromptManager manager = getSystemPromptManager(serverID);
			if (manager != null) {
				manager.setSystemPrompt(serverID, systemPrompt);
			}
		}
	}

	public static void setSystemPrompt(Long serverId, String systemPrompt) {
		if (serverId != null) {
			SystemPromptManager manager = getSystemPromptManager(serverId);
			if (manager != null) {
				manager.setSystemPrompt(serverId, systemPrompt);
			}
		}
	}

	public static boolean getSendAsSystem(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		if (serverID == -1) {
			return true;
		}
		SystemPromptManager manager = getSystemPromptManager(serverID);
		return manager == null || manager.getSendAsSystem();
	}

	public static boolean getSendAsSystem(Long serverId) {
		if (serverId == null) {
			return true;
		}
		SystemPromptManager manager = getSystemPromptManager(serverId);
		return manager == null || manager.getSendAsSystem();
	}

	public static void setSendAsSystem(Message msg, boolean sendAsSystem) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		if (serverID != -1) {
			SystemPromptManager manager = getSystemPromptManager(serverID);
			if (manager != null) {
				manager.setSendAsSystem(serverID, sendAsSystem);
			}
		}
	}

	public static void setSendAsSystem(Long serverId, boolean sendAsSystem) {
		if (serverId != null) {
			SystemPromptManager manager = getSystemPromptManager(serverId);
			if (manager != null) {
				manager.setSendAsSystem(serverId, sendAsSystem);
			}
		}
	}

	public static void resetToDefault(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		if (serverID != -1) {
			SystemPromptManager manager = getSystemPromptManager(serverID);
			if (manager != null) {
				manager.resetToDefault(serverID);
			}
		}
	}

	public static SystemPromptManager getSystemPromptManager(long serverID) {
		return systemPromptMap.computeIfAbsent(serverID, SystemPromptManager::new);
	}

	private static String getDefaultSystemPrompt() {
		return DefaultSystemPromptContext.getDefaultSystemPrompt();
	}
}