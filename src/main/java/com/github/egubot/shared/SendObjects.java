package com.github.egubot.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.github.egubot.features.MessageFormats;
import com.github.egubot.objects.Characters;
import com.github.egubot.objects.Tags;
import com.weatherapi.forecast.Weather;
import com.weatherapi.forecast.WeatherForecast;

public class SendObjects {
	
	public static void sendTags(Messageable e, List<Tags> tags) {
		try {
			String fileName = "Tags.txt";
			File tempFile = new File(fileName);

			try (FileWriter file = new FileWriter(tempFile)) {
				String prevCategory = tags.get(0).getCategory();
				// Numbers likely won't exceed the thousand (less than 100 units are added yearly)
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
	
	public static void sendWeather(Message msg, String lowCaseTxt) {
		String[] args = lowCaseTxt.replace("b-weather","").strip().split(" ");
		String city = args[0];
		String minimal = "";
		if (args.length == 2) {
			minimal = args[1];
		}
		Weather response = WeatherForecast.getForecastData("3", city);
		if (response.isError()) {
			msg.getChannel().sendMessage(response.getErrorMessage());
		} else {
			boolean isMinimal = !minimal.equals("detailed");
			EmbedBuilder[] embeds = MessageFormats.createWeatherEmbed(response, isMinimal);
			msg.getChannel().sendMessage(embeds);
		}
	}
}
