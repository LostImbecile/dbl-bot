package com.github.egubot.gpt2;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.github.egubot.shared.utils.FileUtilities;
import com.github.egubot.shared.utils.JSONUtilities;

public class DiscordAI implements AutoCloseable {
	private final CloseableHttpClient httpClient;
	private final String aiUrl;

	public DiscordAI(String aiUrl) {
		this.aiUrl = aiUrl;
		this.httpClient = HttpClients.createDefault();
	}

	public String generateText(String input) {
		/*
		 * Connects to a client/server and sends and receives messages from it
		 * I'm using python on the other end with flask, pretty simple implementation.
		 * As with chatgpt, it's in a JSON format
		 */
		try {
			// Make sure URL is the same as the other side's
			HttpPost request = new HttpPost(aiUrl + "/generate");

			// Important step to avoid format induced errors
			input = JSONUtilities.jsonify(input);

			// You set all of these by yourself, the two sides need to be compatible
			StringEntity params = new StringEntity("{\"data\": \"" + input + "\"}");
			request.addHeader("content-type", "application/json");
			request.setEntity(params);

			HttpResponse response = httpClient.execute(request);
			String st = FileUtilities.readInputStream(response.getEntity().getContent());

			st = JSONUtilities.dejsonify(st);
			st = cleanDuplicates(st);

			st = st.replace("{\"generated_text\":\"", "");
			if (st.length() >= 2)
				st = st.substring(0, st.length() - 2);
			return st;
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}

	public void close() {
		try {
			httpClient.close();
		} catch (Exception e) {
		}
	}

	public static String cleanDuplicates(String input) {
		while (input.contains("\n\n")) {
			input = input.replace("\n\n", "\n");
		}
		while (input.contains("\"\"")) {
			input = input.replace("\"\"", "\"");
		}
		while (input.contains("//")) {
			input = input.replace("//", "/");
		}
		input = input.replace("\\", "");
		return input;
	}
}
