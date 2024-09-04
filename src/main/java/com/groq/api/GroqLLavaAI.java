package com.groq.api;

import com.github.egubot.ai.AIModel;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.objects.APIResponse;
import com.github.egubot.storage.ConfigManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

public class GroqLLavaAI extends AIModel {
	private static final String MODEL_ID = "Groq_Vision_Model";
	private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
	static final String GROQ_API_KEY = KeyManager.getToken("Groq_API_Key");

	public GroqLLavaAI() {
		super(getConfigModel(), GROQ_API_KEY, GROQ_URL, "0", 4096);
	}

	public APIResponse sendRequest(String prompt, String imageUrl) throws IOException {
		HttpPost postRequest = getPost();

		String body = buildRequestBody(prompt, imageUrl);
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

	public static String getConfigModel() {
		String model = ConfigManager.getProperty(MODEL_ID);
		if (model == null || model.isBlank()) {
			model = "llava-v1.5-7b-4096-preview";
			ConfigManager.setProperty(MODEL_ID, model);
		}
		return model;
	}

	@Override
	public void setModel(String model) {
		ConfigManager.setProperty(MODEL_ID, model);
		this.model = model;
	}

	private String buildRequestBody(String prompt, String imageUrl) {
		JsonArray messages = new JsonArray();
		messages.add(buildMessage(prompt, imageUrl));

		JsonObject requestBody = new JsonObject();
		requestBody.add("messages", messages);
		requestBody.addProperty("model", model);
		requestBody.addProperty("temperature", Double.parseDouble(temperature));
		requestBody.addProperty("stream", false);

		return requestBody.toString();
	}

	private JsonObject buildMessage(String prompt, String imageUrl) {
		JsonArray contentArray = new JsonArray();

		JsonObject textContent = new JsonObject();
		textContent.addProperty("type", "text");
		textContent.addProperty("text", prompt);
		contentArray.add(textContent);

		if (imageUrl != null && !imageUrl.isBlank()) {
			JsonObject imageContent = new JsonObject();
			imageContent.addProperty("type", "image_url");

			JsonObject imageUrlObject = new JsonObject();
			imageUrlObject.addProperty("url", imageUrl);
			imageContent.add("image_url", imageUrlObject);

			contentArray.add(imageContent);
		}

		JsonObject message = new JsonObject();
		message.addProperty("role", "user");
		message.add("content", contentArray);

		return message;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(new GroqLLavaAI().sendRequest("what's in the image", "").getResponse());
	}
}