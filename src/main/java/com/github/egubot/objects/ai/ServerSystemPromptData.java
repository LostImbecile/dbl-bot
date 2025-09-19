package com.github.egubot.objects.ai;

import com.github.egubot.facades.DefaultSystemPromptContext;

public class ServerSystemPromptData {
	private long serverId;
	private String systemPrompt;
	private boolean sendAsSystem = true;
	
	public ServerSystemPromptData() {
	}
	
	public ServerSystemPromptData(long serverId) {
		this.serverId = serverId;
		this.systemPrompt = DefaultSystemPromptContext.getDefaultSystemPrompt();
	}
	
	public ServerSystemPromptData(long serverId, String systemPrompt) {
		this.serverId = serverId;
		this.systemPrompt = systemPrompt;
	}
	
	public long getServerId() {
		return serverId;
	}
	
	public void setServerId(long serverId) {
		this.serverId = serverId;
	}
	
	public String getSystemPrompt() {
		return systemPrompt;
	}
	
	public void setSystemPrompt(String systemPrompt) {
		this.systemPrompt = systemPrompt;
	}
	
	public boolean isSendAsSystem() {
		return sendAsSystem;
	}
	
	public void setSendAsSystem(boolean sendAsSystem) {
		this.sendAsSystem = sendAsSystem;
	}
}