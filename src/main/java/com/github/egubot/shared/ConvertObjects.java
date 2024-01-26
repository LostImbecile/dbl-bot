package com.github.egubot.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.github.egubot.objects.CharacterHash;
import com.github.egubot.objects.Characters;

public class ConvertObjects {
	public static String listToText(List<String> data) {
		return String.join(" ", data);
	}

	public static List<String> textToList(String jsonData) {
		return new ArrayList<>(Arrays.asList(jsonData.split("\n")));
	}

	public static CharacterHash arraytoCharacterHash(List<Characters> arr) {
		CharacterHash hash = new CharacterHash();

		for (int i = 0; i < arr.size(); i++) {
			if(hash.put(arr.get(i)))
				System.err.println("\nSite ID clash for: " + arr.get(i).getSiteID());
		}
		return hash;
	}
}
