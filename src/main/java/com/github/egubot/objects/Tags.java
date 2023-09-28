package com.github.egubot.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.javacord.api.entity.message.Messageable;

public class Tags {
	private static final String MAIN_TAG = "Main Tag";
	private static final String RARITY = "Rarity";
	private static final String COLOUR = "Colour";
	private static final String COMBAT_TYPE = "Combat Type";
	private static final String SAGA = "Saga";
	private static final String MAJOR_ATTR = "Major Attribute";
	private static final String MINOR_ATTR = "Minor Attribute";
	private static final String CHAR_NAME = "Character Name";
	private static final String CUSTOM = "Custom Tag";
	private static final String GENDER = "Gender";

	private int id;
	private String name;
	private String category;
	private CharacterHash characters = new CharacterHash();

	public Tags(int id, String name) {
		super();
		setId(id);
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;

		updateCategory();
	}

	private void updateCategory() {
		if (id >= 8200000) {
			category = MAJOR_ATTR;
		} else if (id >= 8110000) {
			category = MINOR_ATTR;
		} else if (id >= 8000000) {
			category = MAJOR_ATTR;
		} else if (id >= 50000) {
			category = CHAR_NAME;
		} else if (id >= 20000) {
			category = SAGA;
		} else if (id >= 15000) {
			category = COLOUR;
		} else if (id >= 13000) {
			category = COMBAT_TYPE;
		} else if (id >= 12000) {
			category = RARITY;
		} else if (id >= 10000) {
			category = GENDER;
		} else if (id != -1) {
			category = MAIN_TAG;
		} else {
			category = CUSTOM;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CharacterHash getCharacters() {
		return characters;
	}

	public void setCharacters(CharacterHash characters) {
		this.characters = characters;
	}

	public static void sendTags(Messageable e, List<Tags> tags) {
		try {
			File tempFile = new File("Tags.txt");

			try (FileWriter file = new FileWriter(tempFile)) {
				String prevCategory = tags.get(0).category;
				// Numbers likely won't exceed the thousand (less than 100 units are added yearly)
				String formattedSize = String.format("%1$3d", tags.get(0).getCharacters().getOccupiedSize());
				
				file.write(prevCategory + ":\n");
				file.write("[" + formattedSize + "]" + " - " + tags.get(0).name + "\n");
				for (int i = 1; i < tags.size(); i++) {
					formattedSize = String.format("%1$3d", tags.get(i).getCharacters().getOccupiedSize());
					if (!prevCategory.equals(tags.get(i).category)) {
						prevCategory = tags.get(i).category;
						file.write("\n" + prevCategory + ":\n");
					}
					file.write("[" + formattedSize + "]" + " - " + tags.get(i).name + "\n");
				}
			}
			InputStream stream = new FileInputStream("Tags.txt");
			e.sendMessage(stream, "Tags.txt").join();

			stream.close();

			tempFile.delete();
		} catch (IOException e1) {
			e.sendMessage("Failed to send");
		}
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}
