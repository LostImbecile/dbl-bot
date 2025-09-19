package com.github.egubot.objects.ai;

import java.util.ArrayList;
import java.util.List;

public class SystemPromptCollection {
	private List<ServerSystemPromptData> servers;
	
	public SystemPromptCollection() {
		this.servers = new ArrayList<>();
	}
	
	public List<ServerSystemPromptData> getServers() {
		return servers;
	}
	
	public void setServers(List<ServerSystemPromptData> servers) {
		this.servers = servers;
	}
}