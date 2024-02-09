package com.github.egubot.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.egubot.objects.Characters;
import com.github.egubot.objects.Tags;

/*
 * Implementation specific, the start() method works
 * for all websites, everything else however is for
 * this specific one.
 */
public class LegendsDatabase {
	private static final Logger logger = LogManager.getLogger(LegendsDatabase.class.getName());
	private static ArrayList<Characters> charactersList = new ArrayList<>(500);
	private static ArrayList<Tags> tags = new ArrayList<>(100);

	public static final String WEBSITE_URL = "https://dblegends.net/characters";
	private boolean isDataFetchSuccessfull;

	public LegendsDatabase(String st) {
		Document document = Jsoup.parse(st);
		getData(document);
	}

	public LegendsDatabase() throws IOException {
		Document document = Jsoup.connect(WEBSITE_URL).get();
		getData(document);
	}

	public static List<Characters> getCharactersList() {
		return charactersList;
	}

	public static List<Tags> getTags() {
		return tags;
	}

	public void getData(Document document) {
		addSpecialTags();
		getAllTags(document);

		getCharacters(document);

		// 546 is the current unit count as an additional measure
		setDataFetchSuccessfull(charactersList.size() > 545);
	}

	//
	private void addSpecialTags() {
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

	private static void getAllTags(Document document) {
		Elements optionElements = document.select("option");

		for (Element option : optionElements) {
			try {
				int value = Integer.parseInt(option.attr("value"));
				String label = option.text().toLowerCase().replace("é", "e");
				tags.add(new Tags(value, label));
			} catch (NumberFormatException e) {
				// Not an issue here
			}
		}
	}

	private static void getCharacters(Document document) {
		Elements characters = document.select("a.chara-list");
		for (Element character : characters) {
			String charaUrl = character.attr("href");
			String name = character.attr("data-charaname");
			String colour = character.attr("data-element");
			String rarity = character.attr("data-rarity");
			String zenkai = character.attr("data-zenkai");
			String lf = character.attr("data-lf");
			String tags = character.attr("data-tags");
			String imgUrl = character.select(".character-thumb img").attr("src");
			String gameID = character.select("div[title]").attr("title");

			// Alternate names and image
			// String charaFormName = character.attr("data-charaformname");
			// String img2Url = character.select(".character2").attr("src");

			if (gameID.isBlank())
				continue;

			Characters newCharacter = new Characters();
			newCharacter.setImageLink(imgUrl);
			newCharacter.setColour(colour);
			newCharacter.setGameID(gameID);
			newCharacter.setRarity(rarity);
			newCharacter.setCharacterName(processName(name));
			setSiteID(charaUrl, newCharacter);
			setZenkaiStatus(zenkai, newCharacter);
			setLFStatus(lf, newCharacter);
			setTags(tags, newCharacter);
			evaluateReleaseDate(gameID, newCharacter);
			charactersList.add(newCharacter);
		}
	}

	private static void setLFStatus(String st, Characters character) {
		if (st.equals("0"))
			character.setLF(false);
		else {
			character.setLF(true);
			tags.get(13).getCharacters().put(character);
		}
	}

	private static void setSiteID(String line, Characters character) {
		try {
			String st = line.replace("/character/", "");
			character.setSiteID(Integer.parseInt(st));
		} catch (NumberFormatException e) {
			logger.error(e);
		}
	}

	private static void setZenkaiStatus(String st, Characters character) {
		try {
			if (st.equals("-1"))
				character.setZenkai(false);
			else {
				character.setZenkai(true);
				tags.get(12).getCharacters().put(character);
			}
		} catch (StringIndexOutOfBoundsException e) {
			logger.error(e);
		}
	}

	private static void setTags(String line, Characters character) {
		String[] token = line.split(" ");

		for (String id : token) {
			getTag(id, character);
		}
	}

	private static String processName(String line) {
		try {
			return line.replace("Super Saiyan ", "SSJ").replace("SSJGod", "SSG").replace("SSG SS", "SSGSS").strip();
		} catch (StringIndexOutOfBoundsException e) {
			logger.error(e);
			return "Error";
		}
	}

	private static void evaluateReleaseDate(String st, Characters character) {
		try {
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
							tags.get(yearIndex - 1).getCharacters().put(character);

						if (releaseDate == -1 && yearIndex < 21)
							tags.get(yearIndex + 1).getCharacters().put(character);

						tags.get(yearIndex).getCharacters().put(character);
					}

					// Add old or new tag
					if (yearIndex < 16)
						tags.get(22).getCharacters().put(character);
					else
						tags.get(23).getCharacters().put(character);

				} catch (NumberFormatException e) {
					System.out.println("Failed to parse: " + st);
				}
			} else {
				// Specific units someone wanted added
				releaseDate = character.getSiteID();
				if (releaseDate == 45 || releaseDate == 162 || releaseDate == 157 || releaseDate == 183
						|| releaseDate == 201 || releaseDate == 246) {
					tags.get(14).getCharacters().put(character);
					tags.get(22).getCharacters().put(character);
				}

				// Add to event tag
				tags.get(24).getCharacters().put(character);
			}
		} catch (StringIndexOutOfBoundsException e) {
			logger.error(e);
		}
	}

	private static void getTag(String st, Characters character) {
		int id = Integer.parseInt(st);
		for (Tags tag : tags) {
			if (tag.getId() == id) {
				tag.getCharacters().put(character);
				return;
			}
		}
	}

	public boolean isDataFetchSuccessfull() {
		return isDataFetchSuccessfull;
	}

	public void setDataFetchSuccessfull(boolean isDataFetchSuccessfull) {
		this.isDataFetchSuccessfull = isDataFetchSuccessfull;
	}

}
