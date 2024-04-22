package com.github.egubot.objects;

public class APIResponse {
	private String response;
	private int promptTokens;
	private int totalTokens;
	private boolean isError = false;

	public APIResponse(String response, int promptTokens, int totalTokens) {
		super();
		this.response = response;
		this.promptTokens = promptTokens;
		this.totalTokens = totalTokens;
		this.setError(false);
	}
	
	public APIResponse(String response, int promptTokens, int totalTokens, boolean isError) {
		super();
		this.response = response;
		this.promptTokens = promptTokens;
		this.totalTokens = totalTokens;
		this.setError(isError);
	}

	public APIResponse(String message, boolean isError) {
		this.response = message;
		this.setError(isError);
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public int getPromptTokens() {
		return promptTokens;
	}

	public void setPromptTokens(int promptTokens) {
		this.promptTokens = promptTokens;
	}

	public int getTotalTokens() {
		return totalTokens;
	}

	public void setTotalTokens(int totalTokens) {
		this.totalTokens = totalTokens;
	}

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

}
