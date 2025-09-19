package com.github.egubot.build;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.egubot.facades.DefaultSystemPromptContext;
import com.github.egubot.objects.ai.ServerSystemPromptData;
import com.github.egubot.objects.ai.SystemPromptCollection;
import com.github.egubot.shared.utils.ConvertObjects;
import com.github.egubot.shared.utils.JSONUtilities;
import com.github.egubot.storage.DataManagerHandler;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class SystemPromptManager extends DataManagerHandler {
	private Map<Long, ServerSystemPromptData> serverPromptMap;
	private SystemPromptCollection promptCollection;

	public SystemPromptManager() {
		super("System_Prompts", true);
	}

	public synchronized String getSystemPrompt(Long serverId) {
		if (serverId == null) {
			return DefaultSystemPromptContext.getDefaultSystemPrompt();
		}
		return getServerData(serverId).getSystemPrompt();
	}

	public synchronized void setSystemPrompt(Long serverId, String systemPrompt) {
		if (serverId != null) {
			ServerSystemPromptData serverData = getServerData(serverId);
			serverData.setSystemPrompt(systemPrompt);
			writeData(null);
		}
	}

	public synchronized boolean getSendAsSystem(Long serverId) {
		if (serverId == null) {
			return true;
		}
		return getServerData(serverId).isSendAsSystem();
	}

	public synchronized void setSendAsSystem(Long serverId, boolean sendAsSystem) {
		if (serverId != null) {
			ServerSystemPromptData serverData = getServerData(serverId);
			serverData.setSendAsSystem(sendAsSystem);
			writeData(null);
		}
	}

	public synchronized void resetToDefault(Long serverId) {
		if (serverId != null) {
			serverPromptMap.put(serverId, new ServerSystemPromptData(serverId));
			writeData(null);
		}
	}

	public ServerSystemPromptData getServerData(long serverId) {
		return serverPromptMap.computeIfAbsent(serverId, k -> new ServerSystemPromptData(serverId));
	}

	@Override
	public void updateObjects() {
		try {
			Gson gson = new Gson();
			String jsonData = ConvertObjects.listToText(getData());
			promptCollection = gson.fromJson(jsonData, SystemPromptCollection.class);
		} catch (JsonSyntaxException e) {
			logger.error("Syntax Error updating objects", e);
		}
		if (serverPromptMap == null)
			serverPromptMap = new ConcurrentHashMap<>();

		if (promptCollection == null) {
			promptCollection = new SystemPromptCollection();
		} else {
			for (ServerSystemPromptData serverData : promptCollection.getServers()) {
				serverPromptMap.put(serverData.getServerId(), serverData);
			}
		}
	}

	@Override
	public void updateDataFromObjects() {
		if (promptCollection == null) {
			return;
		}
		promptCollection.setServers(new ArrayList<>(serverPromptMap.values().stream().toList()));
		Gson gson = new Gson();
		String jsonData = gson.toJson(promptCollection);
		jsonData = JSONUtilities.prettify(jsonData);
		setData(ConvertObjects.textToList(jsonData));
	}

	public SystemPromptCollection getPromptCollection() {
		return promptCollection;
	}

	public void setPromptCollection(SystemPromptCollection promptCollection) {
		this.promptCollection = promptCollection;
	}
}