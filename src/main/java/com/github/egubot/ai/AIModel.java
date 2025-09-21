package com.github.egubot.ai;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.egubot.facades.SystemPromptContext;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.main.Bot;
import com.github.egubot.objects.APIResponse;
import com.github.egubot.objects.ModelListResponse;
import com.github.egubot.shared.utils.DateUtils;
import com.github.egubot.shared.utils.JSONUtilities;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class AIModel implements Shutdownable {
	private static final Logger logger = LogManager.getLogger(AIModel.class.getName());
	private final CloseableHttpClient httpClient;
	protected String model;
	protected String apiKey;
	protected String url;
	protected String listModelsURL = null;
	protected String temperature = "1";
	protected String systemPrompt;
	protected int tokenLimit = 4096;
	protected boolean sendSystemAsSystem = true;

	public AIModel(String model, String apiKey, String url) {
		this(model, apiKey, url, 30000, 20000, 10000);
	}

	public AIModel(String model, String apiKey, String url, int socketTimeout, int connectTimeout,
			int connectionRequestTimeout) {
		this.model = model;
		this.apiKey = apiKey;
		this.url = url;

		// Remove hardcoded system prompt - will be retrieved per-server
		this.systemPrompt = null;

		this.httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setSocketTimeout(socketTimeout)
						.setConnectTimeout(connectTimeout) 
						.setConnectionRequestTimeout(connectionRequestTimeout) 
						.build())
				.build();
	}

	public AIModel(String model, String apiKey, String url, String temperature) {
		this(model, apiKey, url);
		if (!temperature.isBlank())
			this.temperature = temperature;
	}

	public AIModel(String model, String apiKey, String url, int tokenLimit, int socketTimeout, int connectTimeout,
			int connectionRequestTimeout) {
		this(model, apiKey, url, socketTimeout, connectTimeout, connectionRequestTimeout);
		this.tokenLimit = tokenLimit;
	}

	public AIModel(String model, String apiKey, String url, int tokenLimit) {
		this(model, apiKey, url);
		this.tokenLimit = tokenLimit;
	}

	public AIModel(String model, String apiKey, String url, String temperature, int tokenLimit) {
		this(model, apiKey, url, temperature);
		this.tokenLimit = tokenLimit;
	}

	public APIResponse sendRequest(String prompt, String author, List<String> conversation) throws IOException {
		return sendRequest(prompt, author, conversation, null);
	}
	
	public APIResponse sendRequest(String prompt, String author, List<String> conversation, Long serverId) throws IOException {
		HttpPost postRequest = getPost();

		String body = buildRequestBody(prompt, author, conversation, serverId);
		StringEntity entity = new StringEntity(body, "UTF-8");
		postRequest.setEntity(entity);

		HttpResponse response = getHttpClient().execute(postRequest);

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			return parseResponse(response);
		} else {
			return new APIResponse(statusCode);
		}
	}

	public HttpPost getPost() {
		HttpPost postRequest = new HttpPost(url);

		postRequest.addHeader("Authorization", "Bearer " + apiKey);
		postRequest.addHeader("Content-Type", "application/json");
		return postRequest;
	}

	public APIResponse parseResponse(HttpResponse response) throws IOException {
		String responseBody = EntityUtils.toString(response.getEntity());

		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

		JsonArray choicesArray = jsonObject.getAsJsonArray("choices");
		JsonObject choice = choicesArray.get(0).getAsJsonObject();
		String message = choice.get("message").getAsJsonObject().get("content").getAsString();

		JsonObject usage = jsonObject.getAsJsonObject("usage");
		int promptTokens = usage.get("prompt_tokens").getAsInt();
		int totalTokens = usage.get("total_tokens").getAsInt();

		return new APIResponse(message, promptTokens, totalTokens);
	}

	public String processPlaceholders(String text) {
		return processPlaceholders(text, null);
	}
	
	public String processPlaceholders(String text, Long serverId) {
		if (text == null) return "";
		
		String processed = text.replace("{date}", DateUtils.getDateNow())
				   .replace("{botName}", Bot.getName())
				   .replace("{ownerName}", getOwnerName())
				   .replace("{model}", getModelName())
				   .replace("{tokenLimit}", String.valueOf(getTokenLimit()));
		
		if (serverId != null) {
			processed = processed.replace("{serverId}", String.valueOf(serverId))
					   .replace("{serverName}", getServerName(serverId))
					   .replace("{botNickname}", getBotNickname(serverId))
					   .replace("{memberCount}", getMemberCount(serverId))
					   .replace("{serverOwner}", getServerOwner(serverId));
		}
		
		return processed;
	}
	
	private static String getServerName(Long serverId) {
		try {
			return Bot.getApi().getServerById(serverId)
					.map(server -> server.getName())
					.orElse("Unknown Server");
		} catch (Exception e) {
			return "Unknown Server";
		}
	}
	
	private static String getBotNickname(Long serverId) {
		try {
			return Bot.getApi().getServerById(serverId)
					.map(server -> server.getNickname(Bot.getApi().getYourself()).orElse(Bot.getName()))
					.orElse(Bot.getName());
		} catch (Exception e) {
			return Bot.getName();
		}
	}
	
	private static String getMemberCount(Long serverId) {
		try {
			return Bot.getApi().getServerById(serverId)
					.map(server -> String.valueOf(server.getMemberCount()))
					.orElse("Unknown");
		} catch (Exception e) {
			return "Unknown";
		}
	}
	
	private static String getServerOwner(Long serverId) {
		try {
			return Bot.getApi().getServerById(serverId)
					.map(server -> server.getOwner().map(user -> user.getName()).orElse("Unknown"))
					.orElse("Unknown");
		} catch (Exception e) {
			return "Unknown";
		}
	}
	
	protected String buildRequestBody(String prompt, String author, List<String> conversation) {
		return buildRequestBody(prompt, author, conversation, null);
	}
	
	protected String buildRequestBody(String prompt, String author, List<String> conversation, Long serverId) {
		StringBuilder body = new StringBuilder();
		body.append("{\"messages\": [");
		
		String systemPromptText = SystemPromptContext.getSystemPrompt(serverId);
		boolean sendAsSystem = SystemPromptContext.getSendAsSystem(serverId);
		String processedSystemPrompt = processPlaceholders(systemPromptText, serverId);
		
		if (sendAsSystem) {
			body.append(reformatInput(processedSystemPrompt, "system"));
		} else {
			body.append(reformatInput(processedSystemPrompt, "user"));
		}

		if (conversation != null && !conversation.isEmpty()) {
			for (String element : conversation) {
				body.append(", ").append(element);
			}
		}
		
		// Add current user message
		body.append(", " + reformatInput(prompt, author) + "]");
		body.append(", \"model\": \"" + model + "\"");
		body.append(", \"temperature\": " + temperature + "}");

		return body.toString();
	}

	public String reformatInput(String txt, String author) {
		txt = JSONUtilities.jsonify(txt);
		author = JSONUtilities.jsonify(author);

		if (author.equals("system"))
			return "{\"role\": \"system\", \"content\": \"" + txt + "\"}";
		else if (author.equals("assistant"))
			return "{\"role\": \"assistant\", \"content\": \"" + txt + "\"}";

		return "{\"role\": \"user\", \"content\": \"" + author + ": " + txt + "\"}";
	}
	
	public ModelListResponse getModelsList() throws IOException {
		if (this.listModelsURL == null || this.listModelsURL.isBlank()) {
			logger.warn("listModelsURL is not configured for AIModel.");
			return null;
		}

		HttpGet getRequest = new HttpGet(this.listModelsURL);
		getRequest.addHeader("Authorization", "Bearer " + this.apiKey);
		
		HttpResponse response = getHttpClient().execute(getRequest);
		int statusCode = response.getStatusLine().getStatusCode();
		String responseBody = EntityUtils.toString(response.getEntity());

		if (statusCode == 200) {
			Gson gson = new Gson();
			return gson.fromJson(responseBody, ModelListResponse.class);
		} else {
			logger.error("Failed to get models list. Status: {}, Body: {}", statusCode, responseBody);
			return null; 
		}
	}

	public static String getOwnerName() {
		if (Bot.getOwnerUser() != null)
			return Bot.getOwnerUser().getName();
		return "unknown";
	}

	public String getSystemPrompt() {
		return systemPrompt;
	}

	public void setSystemPrompt(String prompt) {
		this.systemPrompt = prompt;
	}

	public int getTokenLimit() {
		return tokenLimit;
	}

	public void setTokenLimit(int tokenLimit) {
		this.tokenLimit = tokenLimit;
	}

	@Override
	public void shutdown() {
		try {
			getHttpClient().close();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

	public String getModelName() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	public String getListModelsURL() {
		return listModelsURL;
	}

	public void setListModelsURL(String listModelsURL) {
		this.listModelsURL = listModelsURL;
	}

	public boolean isSendSystemAsSystem() {
		return sendSystemAsSystem;
	}

	public void setSendSystemAsSystem(boolean sendSystemAsSystem) {
		this.sendSystemAsSystem = sendSystemAsSystem;
	}
}