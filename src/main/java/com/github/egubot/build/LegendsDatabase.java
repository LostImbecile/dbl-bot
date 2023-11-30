package com.github.egubot.build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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
	private ArrayList<Characters> charactersList = new ArrayList<>(0);
	private ArrayList<Tags> tags = new ArrayList<>(0);
	private ArrayList<String> lines = new ArrayList<>(0);

	private boolean isDataFetchSuccessfull;

	public LegendsDatabase(List<String> lines) throws IOException {
		setLines(lines);
		getData();
	}

	public LegendsDatabase(InputStream is) throws IOException {
		// To read from a different input
		readData(is);
		getData();
	}

	public LegendsDatabase() throws IOException {
		// Read from website
		InputStream is = getWebsiteAsInputStream();
		readData(is);
		getData();
	}

	public static InputStream getWebsiteAsInputStream() throws IOException {
		// Default constructor reads from the website
		// Make a URL to the web page
		URL url = new URL("https://dblegends.net/");

		// Get the input stream through URL Connection
		return url.openStream();
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

	public void getData() throws IOException {
		getSpecialTags();
		getAllTags(lines);

		// If 0 then data for all units was fetched, o.w, there's a problem
		// 546 is the current unit count as an additional measure
		if (getCharacters(lines) == 0 && charactersList.size() > 545) {
			setDataFetchSuccessfull(true);
		} else {
			setDataFetchSuccessfull(false);
		}

	}

	private void readData(InputStream is) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			String line = null;

			// You can process the text as you read it instead of adding it first
			// depending on what you want to do
			while ((line = br.readLine()) != null) {
				lines.add(line.replace("é", "e"));
			}
			// System.out.println("Lines read: " + lines.size());

		}
	}

	//
	private void getSpecialTags() {
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
		tags.add(new Tags(-1, "event"));// 24
	}

	private void getAllTags(List<String> lines) {
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

	private void processTag(String st) {
		String name;
		String value;
		value = st.substring(st.indexOf("\"") + 1, st.indexOf("\"", st.indexOf("\"") + 1));
		name = st.substring(st.indexOf(">") + 1, st.indexOf("<")).replaceAll("[\"()#]", "").replaceAll("[ /]", "_");
		tags.add(new Tags(Integer.parseInt(value), name.toLowerCase()));
	}

	private int getCharacters(List<String> lines) {
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
		String lineWithID = "/character/";
		String colour = "data-element";
		String rarity = "data-rarity";
		String zenkaiStatus = "data-zenkai";
		String lfStatus = "data-lf";
		String ignID = "title=";
		String image = "src=";
		String name = "\"card-header name";
		String tags = "data-tags=";
		String line;

		int dataCounter = 0;
		int characterIndex = 0;
		boolean writeFlag = false;

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
					if (setSiteID(line, characterIndex) != 0) {
						dataCounter++;
					}

				} else if (line.contains(colour) && line.contains("=")) {

					setColour(line, characterIndex);
					dataCounter++;

				} else if (line.contains(rarity) && line.contains("=")) {

					setRarity(line, characterIndex);
					dataCounter++;

				} else if (line.contains(zenkaiStatus) && line.contains("=")) {

					setZenkaiStatus(line, characterIndex);
					dataCounter++;

				} else if (line.contains(lfStatus) && line.contains("=")) {

					setLFStatus(line, characterIndex);
					dataCounter++;

				} else if (line.contains(ignID) && line.contains("=")) {

					setGameID(line, characterIndex);
					dataCounter++;

				} else if (line.contains(image) && line.contains("=")) {

					setImageLink(line, characterIndex);

					if (!line.contains("alt="))
						dataCounter++;

				} else if (line.contains(name)) {

					setName(lines, characterIndex, i);

					if (!line.contains("Form"))
						dataCounter++;

				} else if (line.contains(tags) && line.contains("=") && line.contains(">")) {

					if (setTags(line, characterIndex) > 5) {
						dataCounter++;
					}

				}
			}

		}

		// Each unit should have 9 pieces of data related to it
		// if any is missing there's a problem

		return characterIndex * 9 - dataCounter;

	}

	private int setTags(String line, int characterIndex) {
		String[] token;
		String st;
		st = line.substring(line.indexOf("=") + 1, line.indexOf(">")).replace("\"", "").strip();
		token = st.split(" ");

		int tagNum = 0;
		for (int j = 0; j < token.length; j++) {
			tagNum += getTag(token[j], charactersList.get(characterIndex - 1));
		}

		return tagNum;
		// charactersList.get(characterIndex - 1).setImageLink(st);
	}

	private void setName(List<String> lines, int characterIndex, int i) {
		String st;
		String line;
		line = lines.get(i + 1);
		if (line.contains(">") && line.contains("<")) {
			st = processName(line);
			charactersList.get(characterIndex - 1).setCharacterName(st);
		}
	}

	private void setImageLink(String line, int characterIndex) {
		String st;
		st = line.substring(line.indexOf("src"));
		st = st.substring(st.indexOf("=") + 1).replace("\"", "").strip();
		charactersList.get(characterIndex - 1).setImageLink(st);
	}

	private void setGameID(String line, int characterIndex) {
		String st;
		st = line.substring(line.indexOf("=") + 1, line.length() - 2).replace("\"", "").strip();
		charactersList.get(characterIndex - 1).setGameID(st);
		evaluateReleaseDate(st, characterIndex);
	}

	private void setLFStatus(String line, int characterIndex) {
		String st;
		st = line.substring(line.indexOf("=") + 1).replace("\"", "").strip();
		evaluateLFStatus(st, characterIndex);
	}

	private void setZenkaiStatus(String line, int characterIndex) {
		String st;
		st = line.substring(line.indexOf("=") + 1).replace("\"", "").strip();
		evaluateZenkaiStatus(st, characterIndex);
	}

	private void setRarity(String line, int characterIndex) {
		String st;
		st = line.substring(line.indexOf("=") + 1).replace("\"", "").strip();
		charactersList.get(characterIndex - 1).setRarity(st);
	}

	private void setColour(String line, int characterIndex) {
		String st;
		st = line.substring(line.indexOf("=") + 1).replace("\"", "").strip();
		charactersList.get(characterIndex - 1).setColour(st);
	}

	private int setSiteID(String line, int characterIndex) {
		String[] token;
		String st;
		token = line.split("/character/");
		if (token.length > 1) {

			st = token[1].substring(0, token[1].indexOf("\"") + 1).replace("\"", "").strip();
			charactersList.get(characterIndex - 1).setSiteID(Integer.parseInt(st));
			return 1;
		}
		return 0;
	}

	private void evaluateZenkaiStatus(String st, int characterIndex) {
		if (st.equals("-1"))
			charactersList.get(characterIndex - 1).setZenkai(false);
		else {
			charactersList.get(characterIndex - 1).setZenkai(true);
			tags.get(12).getCharacters().put(charactersList.get(characterIndex - 1));
		}
	}

	private void evaluateLFStatus(String st, int characterIndex) {
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

	private void evaluateReleaseDate(String st, int characterIndex) {
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

	private int getTag(String st, Characters characters) {
		int id;
		id = Integer.parseInt(st);
		for (int k = 0; k < tags.size(); k++) {
			if (tags.get(k).getId() == id) {
				tags.get(k).getCharacters().put(characters);
				return 1;
			}
		}
		return 0;
	}

	public boolean isDataFetchSuccessfull() {
		return isDataFetchSuccessfull;
	}

	public void setDataFetchSuccessfull(boolean isDataFetchSuccessfull) {
		this.isDataFetchSuccessfull = isDataFetchSuccessfull;
	}

	public void setLines(List<String> lines) {
		this.lines = new ArrayList<>(lines);
	}
}
