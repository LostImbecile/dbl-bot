package com.github.egubot.objects;

public class APIResponse {
	private String response;
	private int promptTokens;
	private int totalTokens;
	private int statusCode;
	private boolean isError = false;

	public APIResponse(String response, int promptTokens, int totalTokens) {
		super();
		this.response = response;
		this.promptTokens = promptTokens;
		this.totalTokens = totalTokens;
		this.setStatusCode(200);
		this.setError(false);
	}
	
	public APIResponse(int statusCode) {
		this.response = getErrorMessage(statusCode);
		this.setStatusCode(statusCode);
		this.setError(true);
	}
	
	protected static String getErrorMessage(int statusCode) {
		switch (statusCode) {
		case 429:
			return "Error: Too many requests.";
		case 401:
			return "Error: Invalid Authentication.";
		case 403:
			return "Error: Request Unauthorised.";
		case 422:
			return "Error: Cannot process entity";
		case 404:
			return "Error: 404 Not found";
		case 206:
			return "Error: Partial request";
		case 400:
			return "Error: The server had an error while processing your request.";
		case 502:
			return "Error: Bad Gateway.";
		case 503:
			return "Error: The engine is currently overloaded.";
		default:
			return "Error: No clue what the problem is, try again later.";
		}
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

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}
