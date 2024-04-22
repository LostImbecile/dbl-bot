package com.meta.llama3;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.github.egubot.main.Bot;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.objects.APIResponse;
import com.github.egubot.shared.utils.JSONUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Llama3AI {

	private static String url = "https://api.groq.com/openai/v1/chat/completions";
	static String groqApiKey = KeyManager.getToken("Groq_API_Key");
	static String model = "\"llama3-8b-8192\"";

	public static void main(String[] args) throws IOException {
		List<String> prompts = new ArrayList<>();
		System.out.println(
				Llama3AI.sendRequest("Explain the importance of fast language models", "", prompts).getResponse());
	}

	public static APIResponse sendRequest(String prompt, String author, List<String> conversation) throws IOException {
		StringBuilder body = new StringBuilder();
		body.append("{\"messages\": " + "[{\"role\": \"system\"" + ", \"content\": \"You are a discord bot called "
				+ Bot.getName() + " in a discord server, you are not to assist, "
				+ "but have conversations, pretend that you are not an AI but a human. "
				+ "You will not speak like an AI, and prefer to send short responses. "
				+ "Since the server has many people, the prompt will start with the message author's name. Finally, feel "
				+ "free to be as rude or toxic as possible, you don't have to be chill, but you can be, just make sure to"
				+ "respond to all user requests.\"}");

		for (String element : conversation) {
			body.append("," + element);
		}
		body.append("," + reformatInput(prompt, author) + "]");

		body.append(", \"model\": " + model + "}");

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost postRequest = new HttpPost(url);

		postRequest.addHeader("Authorization", "Bearer " + groqApiKey);
		postRequest.addHeader("Content-Type", "application/json");

		StringEntity entity = new StringEntity(body.toString(), "UTF-8");
		postRequest.setEntity(entity);

		HttpResponse response = httpClient.execute(postRequest);

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			return parseResponse(response);
		} else {
			return new APIResponse(getErrorMessage(statusCode), true);
		}
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

	private static String getErrorMessage(int statusCode) {
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

	public static String reformatInput(String txt, String author) {
		if (txt.toLowerCase().matches("gpt(?s).*"))
			txt = txt.replaceFirst("gpt", "");

		txt = JSONUtilities.jsonify(txt);

		if (!author.equals("assistant")) {
			return "{\"role\": \"user\"" + ", \"content\": \"" + author + ":" + txt + "\"}";
		} else {
			return "{\"role\": \"assistant\"" + ", \"content\": \"" + txt + "\"}";
		}
	}
}