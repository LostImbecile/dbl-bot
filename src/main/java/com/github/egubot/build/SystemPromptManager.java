package com.github.egubot.build;

import java.io.File;

import com.github.egubot.facades.DefaultSystemPromptContext;
import com.github.egubot.objects.ai.ServerSystemPromptData;
import com.github.egubot.storage.LocalDataManager;

public class SystemPromptManager {
	private final LocalDataManager dataManager;
	private final long serverId;
	private ServerSystemPromptData serverData;

	public SystemPromptManager(long serverId) {
		this.serverId = serverId;
		this.dataManager = new LocalDataManager(serverId + File.separator + "System_Prompt");
		this.dataManager.initialise(true);
		loadServerData();
	}

	private void loadServerData() {
		if (dataManager.getData().isEmpty()) {
			serverData = new ServerSystemPromptData(serverId);
			saveServerData();
		} else {
			String prompt = String.join("\n", dataManager.getData());
			serverData = new ServerSystemPromptData(serverId, prompt);
		}
	}

	private void saveServerData() {
		dataManager.getData().clear();
		if (serverData.getSystemPrompt() != null) {
			String[] lines = serverData.getSystemPrompt().split("\n");
			for (String line : lines) {
				dataManager.getData().add(line);
			}
		}
		dataManager.writeData(null);
	}

	public synchronized String getSystemPrompt(Long serverId) {
		if (serverData == null) {
			return DefaultSystemPromptContext.getDefaultSystemPrompt();
		}
		return serverData.getSystemPrompt();
	}

	public synchronized void setSystemPrompt(Long serverId, String systemPrompt) {
		if (serverData == null) {
			serverData = new ServerSystemPromptData(serverId, systemPrompt);
		} else {
			serverData.setSystemPrompt(systemPrompt);
		}
		saveServerData();
	}

	public synchronized boolean getSendAsSystem() {
		if (serverData == null) {
			return true;
		}
		return serverData.isSendAsSystem();
	}

	public synchronized void setSendAsSystem(Long serverId, boolean sendAsSystem) {
		if (serverData == null) {
			serverData = new ServerSystemPromptData(serverId);
		}
		serverData.setSendAsSystem(sendAsSystem);
		saveServerData();
	}

	public synchronized void resetToDefault(Long serverId) {
		serverData = new ServerSystemPromptData(serverId);
		saveServerData();
	}

	public void shutdown() {
		// LocalDataManager doesn't need explicit shutdown
	}
}