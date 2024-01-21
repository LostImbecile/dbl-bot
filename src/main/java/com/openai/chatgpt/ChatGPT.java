package com.openai.chatgpt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.egubot.main.KeyManager;
import com.github.egubot.shared.JSONUtilities;

public class ChatGPT {

	public static String[] chatGPT(String prompt, String author, List<String> conversation) {

		String[] evaluatedResponse = new String[2];
		try {

			String url = "https://api.openai.com/v1/chat/completions";
			String apiKey = KeyManager.getToken("ChatGPT_API_Key");
			String model = "gpt-3.5-turbo";

			URL obj;
			HttpURLConnection connection;
			obj = new URL(url);
			connection = (HttpURLConnection) obj.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Authorization", "Bearer " + apiKey);
			connection.setRequestProperty("Content-Type", "application/json");

			// Very important to have them as they could indefinitely lock up
			// your thread.
			int timeoutMillis = 10000;
			connection.setConnectTimeout(timeoutMillis);
			connection.setReadTimeout(timeoutMillis * 2);

			connection.setDoOutput(true);
			/*
			 * The request body, format is in the methods below.
			 * Write your own system message here to guide the AI's interactions
			 * Likely for the bot to not adhere to it
			 */
			String body = "{\"model\": \"" + model + "\"" + ", \"messages\": " + "[{\"role\": \"system\""
					+ ", \"content\": \"You are a discord bot called bamox in a discord server, you are not to assist, "
					+ "but have casual conversations, pretend that you are not an AI but a human. "
					+ "You will not speak like an AI, and prefer to send short responses. "
					+ "Since the server has many people, the prompt will start with the message author's name\"}";

			// Convo history
			for (int i = 0; i < conversation.size(); i++) {
				body += "," + conversation.get(i);
			}

			body += "," + reformatInput(prompt, author) + "]}";

			// This is what sends the message
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(body);
			writer.flush();
			writer.close();

			// Response from ChatGPT
			BufferedReader br = new BufferedReader(
					new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			String line;

			StringBuffer response = new StringBuffer();

			while ((line = br.readLine()) != null) {
				response.append(line);
			}
			br.close();

			// If you need more info from the response, read the full response
			// and create a method that extracts what you care about
			evaluatedResponse[0] = reformatResponse(response.toString());
			evaluatedResponse[1] = getTokensUsed(response.toString());
			return evaluatedResponse;
		} catch (Exception e) {
			return checkErrorCode(e.getMessage());
		}
	}

	private static String[] checkErrorCode(String errorMessage) {
		String errorCode = errorMessage.replaceAll(".*response code:\\s*(\\d+)(?s).*", "$1");
		String error = "error";
		switch (errorCode) {
		case "429":
			return new String[] { "Error: Rate limit or quota reached.", error };
		case "401":
			return new String[] { "Error: Invalid Authentication.", error };
		case "500":
			return new String[] { "Error: The server had an error while processing your request.", error };
		case "503":
			return new String[] { "Error: The engine is currently overloaded.", error };
		default:
			return new String[] { "Error: No clue what the problem is, try again later.", error };

		}
	}

	private static String getTokensUsed(String st) {

		try {
			int start = st.indexOf("total_tokens");
			st = st.substring(start);
			st = st.substring(0, st.indexOf("}"));

			start = st.indexOf(":");
			st = st.substring(start + 1).strip();

			return st;
		} catch (Exception e) {
			return "1";
		}
	}

	public static String extractMessageFromJSONResponse(String response) {
		int contentStart = response.indexOf("content") + 11;
		int contentEnd = response.indexOf("finish_reason", contentStart);
		String st = response.substring(contentStart, contentEnd);
		st = st.substring(0, st.lastIndexOf("}"));

		return st.substring(0, st.lastIndexOf("\""));

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

	public static String reformatResponse(String response) {
		response = extractMessageFromJSONResponse(response);
		response = JSONUtilities.dejsonify(response);
		return response;
	}
}
