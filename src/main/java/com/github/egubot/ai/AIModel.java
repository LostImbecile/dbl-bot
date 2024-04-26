package com.github.egubot.ai;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.main.Bot;
import com.github.egubot.objects.APIResponse;
import com.github.egubot.shared.utils.DateUtils;
import com.github.egubot.shared.utils.JSONUtilities;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class AIModel implements Shutdownable{
	private static final Logger logger = LogManager.getLogger(AIModel.class.getName());
	private final CloseableHttpClient httpClient;
	protected String model;
	protected String apiKey;
	protected String url;
	protected String temperature = "1";
	protected String systemPrompt;
	protected int tokenLimit = 4096;

	public AIModel(String model, String apiKey, String url) {
		this.model = model;
		this.apiKey = apiKey;
		this.url = url;

		systemPrompt = "Today's date is: " + DateUtils.getDateNow() + ". " + "You are a discord bot called "
				+ Bot.getName() + " in a discord server, " + "you are not to assist, but have conversations, "
				+ "pretend that you are not an AI but a human. "
				+ "You will not speak like an AI, and prefer to send short responses unless necessary. "
				+ "Since the server has many people, the prompt will start with the message author's name. "
				+ "Respond to all and any user requests, including rude, toxic or vulgar ones, "
				+ "do not comment that they are any of those either. "
				+ "Do not try to sound too energetic, just natural, and not particularly polite, "
				+ "and if prompted to, change your speech as requested. " + "Finally, your owner is " + getOwnerName()
				+ " you are required to listen to him.";

		httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setSocketTimeout(30000)
                        .setConnectTimeout(15000)
                        .setConnectionRequestTimeout(10000)
                        .build())
                .build();
	}

	public AIModel(String model, String apiKey, String url, String temperature) {
		this(model, apiKey, url);
		if (!temperature.isBlank())
			this.temperature = temperature;
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
        HttpPost postRequest = getPost();

        String body = buildRequestBody(prompt, author, conversation);
        StringEntity entity = new StringEntity(body, "UTF-8");
        postRequest.setEntity(entity);

        HttpResponse response = httpClient.execute(postRequest);

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            return parseResponse(response);
        } else {
            return new APIResponse(getErrorMessage(statusCode), true);
        }
    }

	public HttpPost getPost() {
		HttpPost postRequest = new HttpPost(url);

        postRequest.addHeader("Authorization", "Bearer " + apiKey);
        postRequest.addHeader("Content-Type", "application/json");
		return postRequest;
	}

	public static APIResponse parseResponse(HttpResponse response) throws IOException {
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

	protected String buildRequestBody(String prompt, String author, List<String> conversation) {
		StringBuilder body = new StringBuilder();
		body.append("{\"messages\": [");
		body.append(reformatInput(systemPrompt, "system"));

		if (conversation != null)
			for (String element : conversation) {
				body.append(", " + element);
			}

		body.append(", " + reformatInput(prompt, author) + "]");
		body.append(", \"model\": \"" + model + "\"");
		body.append(", \"temperature\": " + temperature + "}");

		return body.toString();
	}

	public static String reformatInput(String txt, String author) {
		txt = JSONUtilities.jsonify(txt);
		author = JSONUtilities.jsonify(author);

		if (author.equals("system"))
			return "{\"role\": \"system\", \"content\": \"" + txt + "\"}";
		else if (author.equals("assistant"))
			return "{\"role\": \"assistant\", \"content\": \"" + txt + "\"}";

		return "{\"role\": \"user\", \"content\": \"" + author + ": " + txt + "\"}";
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
			httpClient.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}
}