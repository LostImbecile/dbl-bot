package com.github.egubot.features;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.github.egubot.managers.KeyManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GitHubDetailsGrabber {
	private static final Pattern GITHUB_FILE_PATTERN = Pattern.compile("https://github\\.com/[^/]+/[^/]+/blob/.*");
	private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile("https://github\\.com/[^/]+/[^/]+");
	private static final Pattern GITHUB_OWNER_REPO_PATTERN = Pattern.compile("https://github\\.com/([^/]+)/([^/]+).*");

	private static final String token = KeyManager.getToken("GitHub_Access_Token"); // optional

	public static String getGitHubFileStructure(String url) {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			String repoUrl = getRepoUrl(url);
			if (repoUrl != null) {
				String owner = repoUrl.split("/")[0];
				String repo = repoUrl.split("/")[1];

				HttpGet request = new HttpGet("https://api.github.com/repos/" + owner + "/" + repo + "/contents");
				if (!token.isBlank()) {
					request.setHeader("Authorization", "Bearer " + token);
				}

				CloseableHttpResponse response = httpClient.execute(request);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					String json = EntityUtils.toString(entity);
					JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();

					StringBuilder fileStructure = new StringBuilder();
					for (JsonElement element : jsonArray) {
						JsonObject file = element.getAsJsonObject();
						if (file.get("type").getAsString().equals("dir")) {
							fileStructure.append(
									getDirectoryStructure(file.get("path").getAsString(), owner, repo, httpClient));
						} else {
							fileStructure.append(file.get("path").getAsString()).append("\n");
						}
					}
					return fileStructure.toString();
				} else {
					return response.getStatusLine().getReasonPhrase();
				}
			} else {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}

	private static String getDirectoryStructure(String path, String owner, String repo,
			CloseableHttpClient httpClient) {
		try {
			HttpGet request = new HttpGet("https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path);
			if (!token.isBlank()) {
				request.setHeader("Authorization", "Bearer " + token);
			}

			CloseableHttpResponse response = httpClient.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				String json = EntityUtils.toString(entity);
				JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();

				StringBuilder directoryStructure = new StringBuilder();
				for (JsonElement element : jsonArray) {
					JsonObject file = element.getAsJsonObject();
					if (file.get("type").getAsString().equals("dir")) {
						directoryStructure
								.append(getDirectoryStructure(file.get("path").getAsString(), owner, repo, httpClient));
					} else {
						directoryStructure.append(file.get("path").getAsString()).append("\n");
					}
				}
				return directoryStructure.toString();
			} else {
				return "";
			}
		} catch (IOException e) {
			return "";
		}
	}

	private static String getRepoUrl(String url) {
		Matcher matcher = GITHUB_OWNER_REPO_PATTERN.matcher(url);
		if (matcher.matches()) {
			return matcher.group(1) + "/" + matcher.group(2);
		} else {
			String[] parts = url.split("/");
			if (parts.length >= 4) {
				return parts[3] + "/" + parts[4];
			} else {
				return null;
			}
		}
	}

	public static String getGitHubFileOrReadmeContents(String url) {
		if (GITHUB_FILE_PATTERN.matcher(url).matches()) {
			return getGitHubFileContents(url);
		} else if (GITHUB_REPO_PATTERN.matcher(url).matches()) {
			return getGitHubReadmeContents(url);
		} else {
			return null;
		}
	}

	private static String getGitHubFileContents(String url) {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpGet request = new HttpGet(url.replace("github.com", "raw.githubusercontent.com").replace("blob/", ""));
			if (!token.isBlank()) {
				request.setHeader("Authorization", "Bearer " + token);
			}

			CloseableHttpResponse response = httpClient.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity);
			} else {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}

	private static String getGitHubReadmeContents(String url) {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			Matcher matcher = GITHUB_OWNER_REPO_PATTERN.matcher(url);
			if (matcher.matches()) {
				String owner = matcher.group(1);
				String repo = matcher.group(2);

				HttpGet request = new HttpGet("https://api.github.com/repos/" + owner + "/" + repo);
				if (!token.isBlank()) {
					request.setHeader("Authorization", "Bearer " + token);
				}

				CloseableHttpResponse response = httpClient.execute(request);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					String json = EntityUtils.toString(entity);
					JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
					String defaultBranch = jsonObject.get("default_branch").getAsString();

					request = new HttpGet("https://raw.githubusercontent.com/" + owner + "/" + repo + "/"
							+ defaultBranch + "/README.md");
					if (!token.isBlank()) {
						request.setHeader("Authorization", "Bearer " + token);
					}

					response = httpClient.execute(request);
					statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == 200) {
						entity = response.getEntity();
						return EntityUtils.toString(entity);
					} else {
						return null;
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}

	public static String getFileNameFromUrl(String url) {
		if (url.contains("blob") || url.contains("tree")) {
			return url.lastIndexOf("/") > 0 ? url.substring(url.lastIndexOf("/") + 1) : null;
		} else {
			return null;
		}
	}

	public static void main(String[] args) {
		System.out.println(getFileNameFromUrl("https://github.com/Lostlmbecile/dbl-bot/blob/master/README.md"));
		System.out.println(getGitHubFileOrReadmeContents(
				"https://github.com/Lostlmbecile/dbl-bot/blob/master/src/main/java/com/github/egubot/features/legends/LegendsPool.java"));
	}
}