package com.github.egubot.build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.github.egubot.objects.Characters;
import com.github.egubot.objects.Tags;

/*
 * Implementation specific, the start() method works
 * for all websites, everything else however is for
 * this specific one.
 */
public class LegendsDatabase {
	private static ArrayList<Characters> charactersList = new ArrayList<>(0);
	private static ArrayList<Tags> tags = new ArrayList<>();
	private static ArrayList<String> lines = new ArrayList<>();

	public LegendsDatabase() throws IOException {
		start();
	}

	public List<Characters> getCharactersList() {
		return charactersList;
	}

	public List<Tags> getTags() {
		return tags;
	}

	public List<String> getLines() {
		return lines;
	}

	public void start() throws IOException {
		// Make a URL to the web page
		URL url = new URL("https://dblegends.net/");

		// Get the input stream through URL Connection
		URLConnection con = url.openConnection();
		InputStream is = con.getInputStream();

		/*
		 * Once you have the Input Stream, it's just plain old Java IO stuff.
		 * For binary content, it's better to directly read the bytes from stream and
		 * write to the target file.
		 */

		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			String line = null;

			// You can process the text as you read it instead of adding it first
			// depending on what you want to do
			while ((line = br.readLine()) != null) {
				lines.add(line.replace("é", "e"));
			}
			// System.out.println("Lines read: " + lines.size());

			getSpecialTags();
			getAllTags(lines);
			getCharacters(lines);

		}
	}

	//
	private static void getSpecialTags() {
		/*
		 * Index matters for some of the tags here so don't reorder
		 * the lines, I can use the IDs to add them, but I want to
		 * avoid any future clash without adding more processing steps
		 */
		tags.add(new Tags(12003, "ultra")); // 0
		tags.add(new Tags(12002, "sparking"));// 1
		tags.add(new Tags(12001, "ex"));// 2
		tags.add(new Tags(12000, "hero"));// 3
		tags.add(new Tags(10000, "male"));// 4
		tags.add(new Tags(10001, "female"));// 5
		tags.add(new Tags(15000, "red"));// 6
		tags.add(new Tags(15001, "yel"));// 7
		tags.add(new Tags(15002, "pur"));// 8
		tags.add(new Tags(15003, "grn"));// 9
		tags.add(new Tags(15004, "blu"));// 10
		tags.add(new Tags(15070, "lgt"));// 11
		tags.add(new Tags(-1, "zenkai"));// 12
		tags.add(new Tags(-1, "lf"));// 13
		tags.add(new Tags(-1, "year1"));// 14
		tags.add(new Tags(-1, "year2"));// 15
		tags.add(new Tags(-1, "year3"));// 16
		tags.add(new Tags(-1, "year4"));// 17
		tags.add(new Tags(-1, "year5"));// 18
		tags.add(new Tags(-1, "year6"));// 19
		tags.add(new Tags(-1, "year7"));// 20
		tags.add(new Tags(-1, "year8"));// 21
		tags.add(new Tags(-1, "old"));// 22
		tags.add(new Tags(-1, "new"));// 23
		tags.add(new Tags(-1, "event"));//24
	}

	private static void getAllTags(List<String> lines) {
		String beginWrite = "- All Tags -";
		String endWrite = "<br/><br/>";

		String[] token;
		String st = "", line;
		boolean writeFlag = false;
		for (int i = 0; i < lines.size(); i++) {
			line = lines.get(i);
			if (line.contains(beginWrite)) {
				writeFlag = true;
				continue;
			}
			if (line.contains(endWrite))
				break;

			if (writeFlag) {
				token = line.split("option value=");

				for (int j = 0; j < token.length; j++) {
					try {
						st = token[j];
						if (token[j].contains("\"") && token[j].contains(">") && token[j].contains("<")) {
							processTag(st);
						}
					} catch (Exception e) {
						System.err.println("Tag ID failed to fetch from line:\n" + token[j]);
					}
				}
			}

		}
	}

	private static void processTag(String st) {
		String name;
		String value;
		value = st.substring(st.indexOf("\"") + 1, st.indexOf("\"", st.indexOf("\"") + 1));
		name = st.substring(st.indexOf(">") + 1, st.indexOf("<")).replaceAll("[\"()#]", "").replaceAll("[ /]", "_");
		tags.add(new Tags(Integer.parseInt(value), name.toLowerCase()));
	}

	private static void getCharacters(List<String> lines) {
		/*
		 * To get data from a website you just read its HTML content (with say, f12),
		 * and then identify any patterns you find.
		 * 
		 * There are different ways to do it depending on the website, and you
		 * can get containers directly as well to avoid having to look for certain
		 * keywords and the sort. Java does have support for that stuff so you
		 * can look for some online, this was easier for me so I went for it.
		 * 
		 * This class is fixed, changing a number or a line might break it, so
		 * it's preferred that you do not touch it.
		 */
		String beginWrite = "\"character-container text-center justify-content-around justify-content-center d-flex flex-wrap\"";
		String endWrite = "</main>";
		String newCharacter = "chara-list chara-listing zoom";
		String lineWithID = "/character.php?";
		String colour = "data-element";
		String rarity = "data-rarity";
		String zenkaiStatus = "data-zenkai";
		String lfStatus = "data-lf";
		String ignID = "title=";
		String image = "src=";
		String name = "\"card-header name\"";
		String dataTags = "data-tags=";
		String line;
		boolean writeFlag = false;
		int characterIndex = 0;
		for (int i = 160; i < lines.size(); i++) {
			line = lines.get(i);

			if (line.contains(beginWrite)) {
				writeFlag = true;
			}

			if (line.contains(endWrite))
				break;

			if (writeFlag) {

				if (line.contains(newCharacter)) {

					charactersList.add(new Characters());
					characterIndex++;
				}

				if (line.contains(lineWithID) && line.contains("\"")) {
					setSiteID(line, characterIndex);

				} else if (line.contains(colour) && line.contains("=")) {

					setColour(line, characterIndex);

				} else if (line.contains(rarity) && line.contains("=")) {

					setRarity(line, characterIndex);

				} else if (line.contains(zenkaiStatus) && line.contains("=")) {

					setZenkaiStatus(line, characterIndex);

				} else if (line.contains(lfStatus) && line.contains("=")) {

					setLFStatus(line, characterIndex);

				} else if (line.contains(ignID) && line.contains("=")) {

					setGameID(line, characterIndex);

				} else if (line.contains(image) && line.contains("=")) {
					setImageLink(line, characterIndex);

				} else if (line.contains(name)) {
					setName(lines, characterIndex, i);

				} else if (line.contains(dataTags) && line.contains("=") && line.contains(">")) {

					setTags(line, characterIndex);

				}
			}

		}
	}

	private static void setTags(String line, int characterIndex) {
		String[] token;
		String st;
		st = line.substring(line.indexOf("=") + 1, line.indexOf(">")).replace("\"", "").strip();
		token = st.split(" ");

		for (int j = 0; j < token.length; j++)
			getTag(token[j], charactersList.get(characterIndex - 1));

		charactersList.get(characterIndex - 1).setImageLink(st);
	}

	private static void setName(List<String> lines, int characterIndex, int i) {
		String st;
		String line;
		line = lines.get(i + 1);
		if (line.contains(">") && line.contains("<")) {
			st = processName(line);
			charactersList.get(characterIndex - 1).setCharacterName(st);
		}
	}

	private static void setImageLink(String line, int characterIndex) {
		String st;
		st = line.substring(line.indexOf("src"));
		st = st.substring(st.indexOf("=") + 1).replace("\"", "").strip();
		charactersList.get(characterIndex - 1).setImageLink(st);
	}

	private static void setGameID(String line, int characterIndex) {
		String st;
		st = line.substring(line.indexOf("=") + 1, line.length() - 2).replace("\"", "").strip();
		charactersList.get(characterIndex - 1).setGameID(st);
		evaluateReleaseDate(st, characterIndex);
	}

	private static void setLFStatus(String line, int characterIndex) {
		String st;
		st = line.substring(line.indexOf("=") + 1).replace("\"", "").strip();
		evaluateLFStatus(st, characterIndex);
	}

	private static void setZenkaiStatus(String line, int characterIndex) {
		String st;
		st = line.substring(line.indexOf("=") + 1).replace("\"", "").strip();
		evaluateZenkaiStatus(st, characterIndex);
	}

	private static void setRarity(String line, int characterIndex) {
		String st;
		st = line.substring(line.indexOf("=") + 1).replace("\"", "").strip();
		charactersList.get(characterIndex - 1).setRarity(st);
	}

	private static void setColour(String line, int characterIndex) {
		String st;
		st = line.substring(line.indexOf("=") + 1).replace("\"", "").strip();
		charactersList.get(characterIndex - 1).setColour(st);
	}

	private static void setSiteID(String line, int characterIndex) {
		String[] token;
		String st;
		token = line.split("id=");
		if (token.length > 1) {

			st = token[1].substring(0, token[1].indexOf("\"") + 1).replace("\"", "").strip();
			charactersList.get(characterIndex - 1).setSiteID(Integer.parseInt(st));

		}
	}

	private static void evaluateZenkaiStatus(String st, int characterIndex) {
		if (st.equals("-1"))
			charactersList.get(characterIndex - 1).setZenkai(false);
		else {
			charactersList.get(characterIndex - 1).setZenkai(true);
			tags.get(12).getCharacters().put(charactersList.get(characterIndex - 1));
		}
	}

	private static void evaluateLFStatus(String st, int characterIndex) {
		if (st.equals("0"))
			charactersList.get(characterIndex - 1).setLF(false);
		else {
			charactersList.get(characterIndex - 1).setLF(true);
			tags.get(13).getCharacters().put(charactersList.get(characterIndex - 1));
		}
	}

	private static String processName(String line) {
		String st;
		st = line.substring(line.indexOf(">") + 1, line.indexOf("<")).replace("\"", "").replace("Super Saiyan ", "SSJ")
				.replace("SSJGod", "SSG").replace("SSG SS", "SSGSS").strip();
		return st;
	}

	private static void evaluateReleaseDate(String st, int characterIndex) {
		int releaseDate;
		int yearIndex;
		if (!st.contains("EVT")) {
			try {
				
				releaseDate = Integer.parseInt(st.substring(st.indexOf("L") + 1, st.indexOf("-")));
				yearIndex = 13;
				if (releaseDate == 0)
					yearIndex++;

				while (releaseDate > 0) {
					releaseDate -= 12;
					yearIndex++;
				}
				
				// Years from index 14 to 21
				if (yearIndex > 13 && yearIndex < 22) {
					// Adds units on the edge of the year to both years
					if (releaseDate == 1 && yearIndex > 14)
						tags.get(yearIndex - 1).getCharacters().put(charactersList.get(characterIndex - 1));

					if (releaseDate == -1 && yearIndex < 21)
						tags.get(yearIndex + 1).getCharacters().put(charactersList.get(characterIndex - 1));
					
					tags.get(yearIndex).getCharacters().put(charactersList.get(characterIndex - 1));
				}
				
				// Add old or new tag
				if (yearIndex < 16)
					tags.get(22).getCharacters().put(charactersList.get(characterIndex - 1));
				else
					tags.get(23).getCharacters().put(charactersList.get(characterIndex - 1));


			} catch (NumberFormatException e) {
				System.out.println("Failed to parse: " + st);
			}
		} else {
			// Specific units someone wanted added
			releaseDate = charactersList.get(characterIndex - 1).getSiteID();
			if (releaseDate == 45 || releaseDate == 162 || releaseDate == 157 || releaseDate == 183
					|| releaseDate == 201 || releaseDate == 246) {
				tags.get(14).getCharacters().put(charactersList.get(characterIndex - 1));
				tags.get(22).getCharacters().put(charactersList.get(characterIndex - 1));
			}
			
			// Add to event tag
			tags.get(24).getCharacters().put(charactersList.get(characterIndex - 1));
		}
	}

	private static void getTag(String st, Characters characters) {
		int id;
		id = Integer.parseInt(st);
		for (int k = 0; k < tags.size(); k++) {
			if (tags.get(k).getId() == id) {
				tags.get(k).getCharacters().put(characters);
			}
		}
	}
}
