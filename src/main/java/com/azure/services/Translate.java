package com.azure.services;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.github.egubot.managers.KeyManager;
import com.github.egubot.shared.FileUtilities;
import com.github.egubot.shared.JSONUtilities;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Translate {
	private static String key = KeyManager.getToken("Azure_Translate_API_Key");
	private static String location = "global";
	private String to;
	private String from;
	private String url;

	public Translate() {
		this.to = "en";
		this.from = "";
		buildURL();
	}

	public Translate(String from, String to) {
		if (to == null) {
			to = "en";
		}
		if (from == null) {
			from = "";
		}
		this.to = to.toLowerCase();
		this.from = from.toLowerCase();
		buildURL();
	}

	public String post(String text, boolean appendLanguage) throws IOException {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			String requestBody = "[{\"Text\": \"" + JSONUtilities.jsonify(text) + "\"}]";
			HttpPost httpPost = new HttpPost(url);

			// Set headers
			httpPost.setHeader("Ocp-Apim-Subscription-Key", key);
			httpPost.setHeader("Ocp-Apim-Subscription-Region", location);
			httpPost.setHeader("Content-type", "application/json");

			// Set request body
			httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

			// Execute the request
			HttpResponse response = client.execute(httpPost);

			// Process the response
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				String result = FileUtilities.readInputStream(response.getEntity().getContent());
				return extractTranslationFromResponse(result, appendLanguage);
			} else {
				return checkStatusCode(statusCode);
			}
		} 
	}

	public String extractTranslationFromResponse(String response, boolean appendLanguage) {
		JsonElement jsonElement = JsonParser.parseString(response.replace("\\u00A0", ""));

		String translationText;
		String output = "";
		if (appendLanguage) {
			String originalLanguage;
			String outputLanguage;

			try {
				originalLanguage = jsonElement.getAsJsonArray().get(0).getAsJsonObject().get("detectedLanguage")
						.getAsJsonObject().get("language").getAsString();
			} catch (Exception e) {
				originalLanguage = from;
			}

			try {
				outputLanguage = jsonElement.getAsJsonArray().get(0).getAsJsonObject().get("translations")
						.getAsJsonArray().get(0).getAsJsonObject().get("to").getAsString();
			} catch (Exception e) {
				outputLanguage = to;
			}
			output = originalLanguage + "-" + outputLanguage + ":\n";
		}
		try {
			translationText = jsonElement.getAsJsonArray().get(0).getAsJsonObject().get("translations").getAsJsonArray()
					.get(0).getAsJsonObject().get("text").getAsString();
		} catch (Exception e) {
			return null;
		}

		return output + translationText;
	}

	public String extractDetectedLanguageFromResponse(String response, boolean getLanguageOnly) {
		JsonElement jsonElement = JsonParser.parseString(response.replace("\\u00A0", ""));

		String language = null;
		String score = null;
		try {
			language = jsonElement.getAsJsonArray().get(0).getAsJsonObject().get("language").getAsString();
		} catch (Exception e) {
		}

		if (getLanguageOnly) {
			return language;
		}
		try {
			score = jsonElement.getAsJsonArray().get(0).getAsJsonObject().get("score").getAsString();
		} catch (Exception e) {
		}

		return "Language: " + language + "\nScore: " + score;
	}

	private void buildURL() {
		this.url = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0&from=" + from + "&to=" + to;
	}

	public static String getTranslateLanguages() throws IOException {
		String url = "https://api.cognitive.microsofttranslator.com/languages?api-version=3.0&scope=translation";
		String result = FileUtilities.readInputStream(new URL(url).openStream());

		return JSONUtilities.prettify(result).replace("\"", "").replace("dir: ltr\n ", "").replace("dir: rtl\n ", "");
	}

	public static String getTransliterateLanguages() throws IOException {
		String url = "https://api.cognitive.microsofttranslator.com/languages?api-version=3.0&scope=transliteration";
		String result = FileUtilities.readInputStream(new URL(url).openStream());

		return JSONUtilities.prettify(result).replace("\"", "");
	}

	public String detectLanguage(String text, boolean getLanguageOnly) throws IOException {
		HttpClient client = HttpClients.createDefault();
		String requestBody = "[{\"Text\": \"" + JSONUtilities.jsonify(text) + "\"}]";
		HttpPost httpPost = new HttpPost("https://api.cognitive.microsofttranslator.com/detect?api-version=3.0");

		// Set headers
		httpPost.setHeader("Ocp-Apim-Subscription-Key", key);
		httpPost.setHeader("Ocp-Apim-Subscription-Region", location);
		httpPost.setHeader("Content-type", "application/json");

		// Set request body
		httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

		// Execute the request
		HttpResponse response = client.execute(httpPost);

		// Process the response
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			String result = FileUtilities.readInputStream(response.getEntity().getContent());

			return extractDetectedLanguageFromResponse(result, getLanguageOnly);
		} else {
			return checkStatusCode(statusCode);
		}
	}

	private String checkStatusCode(int statusCode) {
		switch (statusCode) {
		case 429:
			return "Error: Rate limit or quota reached.";
		case 401:
			return "Error: Invalid Authentication.";
		case 403:
			return "Error: Request Unauthorised.";
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

	public void setTo(String to) {
		this.to = to;
		buildURL();
	}

	public void setFrom(String from) {
		this.from = from;
		buildURL();
	}

	public static void main(String[] args) {
		try {
			System.out.println(new Translate("", "en").post(
					"J’aimerais vraiment conduire votre voiture autour du pâté de maisons plusieurs fois!", true));

		} catch (IOException e) {
			
		}
	}
}
