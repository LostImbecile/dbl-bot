package com.github.egubot.objects;

import java.util.HashMap;
import java.util.Map;

public class Abbreviations {
	private Map<String, String> abbreviationMap;

    public Abbreviations() {
        abbreviationMap = new HashMap<>();
    }

    public void addAbbreviation(String name, String id) {
        abbreviationMap.put(name, id);
    }

    public String getAbbreviationId(String name) {
        return abbreviationMap.get(name);
    }

	public static String getReactionId(String id) {
		if (id.matches("<.*>"))
			return id.replaceAll("[<>]", "").replace("<a", "").replaceFirst(":", "");
		
		return id;
	}
	
	public String replaceReactionIds(String input) {
		 for (Map.Entry<String, String> entry : abbreviationMap.entrySet()) {
	            input = input.replace(entry.getValue(), getReactionId(entry.getValue()));
	        }
	        return input;
	}

	public String replaceAbbreviations(String input) {
        for (Map.Entry<String, String> entry : abbreviationMap.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }

	public Map<String, String> getAbbreviationMap() {
		return abbreviationMap;
	}

}
