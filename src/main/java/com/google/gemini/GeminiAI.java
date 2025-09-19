package com.google.gemini;

import com.github.egubot.ai.AIModel;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.objects.APIResponse;
import com.github.egubot.storage.ConfigManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GeminiAI extends AIModel {
	private static final Logger logger = LogManager.getLogger(GeminiAI.class.getName());
	private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions";
	private static final String GEMINI_MODEL_LIST_URL = "https://generativelanguage.googleapis.com/v1beta/openai/models";
	static final String GEMINI_API_KEY = KeyManager.getToken("Gemini_API_Key");

	public GeminiAI() {
		super(getConfigModel(), GEMINI_API_KEY, GEMINI_URL, "1.0", 8192);
		this.setListModelsURL(GEMINI_MODEL_LIST_URL);
	}

	public static String getConfigModel() {
		String model = ConfigManager.getProperty("Gemini_Model");
		if (model == null || model.isBlank()) {
			model = "gemini-2.5-flash-lite";
			ConfigManager.setProperty("Gemini_Model", model);
		}
		return model;
	}

	@Override
	public void setModel(String model) {
		ConfigManager.setProperty("Gemini_Model", model);
		this.model = model;
	}

	public APIResponse sendRequestWithImage(String prompt, String imageUrl) throws IOException {
		return sendRequestWithImage(prompt, imageUrl, null, null, null);
	}

	public APIResponse sendRequestWithImage(String prompt, String imageUrl, String author, List<String> conversation, Long serverId) throws IOException {
		logger.info("Sending image request with URL: {}", imageUrl);
		
		String processedImageUrl = imageUrl;
		if (imageUrl != null && !imageUrl.startsWith("data:")) {
			processedImageUrl = convertToBase64Image(imageUrl);
		}
		
		HttpPost postRequest = getPost();
		String body = buildImageRequestBody(prompt, processedImageUrl);
		logger.debug("Request body: {}", body);
		StringEntity entity = new StringEntity(body, "UTF-8");
		postRequest.setEntity(entity);

		HttpResponse response = getHttpClient().execute(postRequest);

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			return parseResponse(response);
		} else {
			String errorBody = EntityUtils.toString(response.getEntity());
			logger.error("Gemini API error: Status {}, Body: {}", statusCode, errorBody);
			return new APIResponse(statusCode);
		}
	}

	private String convertToBase64Image(String imageUrl) throws IOException {
		HttpGet getRequest = new HttpGet(imageUrl);
		HttpResponse response = getHttpClient().execute(getRequest);
		
		if (response.getStatusLine().getStatusCode() == 200) {
			byte[] imageBytes = EntityUtils.toByteArray(response.getEntity());
			String base64Image = Base64.getEncoder().encodeToString(imageBytes);
			
			String mimeType = "image/jpeg";
			if (imageUrl.toLowerCase().contains(".png")) {
				mimeType = "image/png";
			} else if (imageUrl.toLowerCase().contains(".gif")) {
				mimeType = "image/gif";
			} else if (imageUrl.toLowerCase().contains(".webp")) {
				mimeType = "image/webp";
			}
			
			return "data:" + mimeType + ";base64," + base64Image;
		} else {
			throw new IOException("Failed to download image: " + response.getStatusLine().getStatusCode());
		}
	}

	private String buildImageRequestBody(String prompt, String imageUrl) {
		JsonArray messages = new JsonArray();
		
		JsonObject message = new JsonObject();
		message.addProperty("role", "user");

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

		message.add("content", contentArray);
		messages.add(message);

		JsonObject requestBody = new JsonObject();
		requestBody.add("messages", messages);
		requestBody.addProperty("model", model);
		requestBody.addProperty("temperature", Double.parseDouble(temperature));
		requestBody.addProperty("stream", false);

		return requestBody.toString();
	}

	public static void main(String[] args) throws IOException {
		System.out.println(new GeminiAI().sendRequest("Hello, how are you?", "placeholder", null).getResponse());
	}
}