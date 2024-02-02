package com.github.egubot.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.javacord.api.entity.message.Messageable;
import com.github.egubot.objects.Characters;
import com.github.egubot.objects.Tags;

public class SendObjects {
	
	private SendObjects() {
	}

	public static void sendTags(Messageable e, List<Tags> tags) {
		try {
			String fileName = "Tags.txt";
			File tempFile = new File(fileName);

			try (FileWriter file = new FileWriter(tempFile)) {
				String prevCategory = tags.get(0).getCategory();
				// Numbers likely won't exceed the thousand (less than 100 units are added
				// yearly)
				String formattedSize = String.format("%1$3d", tags.get(0).getCharacters().getOccupiedSize());

				file.write(prevCategory + ":\n");
				file.write("[" + formattedSize + "]" + " - " + tags.get(0).getName() + "\n");
				for (int i = 1; i < tags.size(); i++) {
					formattedSize = String.format("%1$3d", tags.get(i).getCharacters().getOccupiedSize());
					if (!prevCategory.equals(tags.get(i).getCategory())) {
						prevCategory = tags.get(i).getCategory();
						file.write("\n" + prevCategory + ":\n");
					}
					file.write("[" + formattedSize + "]" + " - " + tags.get(i).getName() + "\n");
				}
			}
			InputStream stream = new FileInputStream(fileName);
			e.sendMessage(stream, fileName).join();

			stream.close();

			tempFile.delete();
		} catch (IOException e1) {
			e.sendMessage("Failed to send");
		}
	}

	public static void sendCharacters(Messageable e, List<Characters> characters) {
		try {
			File tempFile = new File("Characters.txt");

			try (FileWriter file = new FileWriter(tempFile)) {
				String gameID;
				String name;
				for (Characters character : characters) {
					gameID = "[" + character.getGameID();
					name = character.getCharacterName();
					file.write(String.format("%-12s] - %s%n", gameID, name));
				}
			}
			InputStream stream = new FileInputStream("Characters.txt");
			e.sendMessage(stream, "Characters.txt").join();

			stream.close();
			tempFile.delete();
		} catch (IOException e1) {
			e.sendMessage("Failed to send");
		}
	}

}
