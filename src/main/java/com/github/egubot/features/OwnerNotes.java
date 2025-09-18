package com.github.egubot.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.javacord.api.entity.message.Message;

import com.github.egubot.storage.LocalDataManager;

public class OwnerNotes {
	private static final LocalDataManager dataManager = new LocalDataManager("Owner_Notes");
	private static final List<String> notes = Collections.synchronizedList(new ArrayList<String>());
	
	static {
		dataManager.initialise(true);
		for (String s : dataManager.getData()) {
			try {
				notes.add(s);
			} catch (Exception e) {
			}
		}
	}
	
	public static void addNote(Message msg, String note) {
		if (!notes.contains(note)) {
			notes.add(note);
			dataManager.getData().add(note);
			dataManager.writeData(msg == null ? null : msg.getChannel());
		}
	}
	
	public static void removeNote(Message msg, int id) {
		id = id - 1;
		if (notes.size() > id) {
			String note = notes.get(id);
			notes.remove(id);
			dataManager.getData().remove(note);
			dataManager.writeData(msg == null ? null : msg.getChannel());
		}
	}
	
	public static List<String> getNotes() {
		return notes;
	}
	
	public static void clearNotes() {
		notes.clear();
		dataManager.getData().clear();
		dataManager.writeData(null);
	}
	
	public static String formatNotes() {
		if (notes.isEmpty()) {
			return "No notes";
		}
		int id = 1;
		String[] notesArr = new String[notes.size()];
		for (String s : notes) {
			notesArr[id - 1] = String.format("%d. %s", id++, s);
		}
		StringBuilder sb = new StringBuilder();
		for (String s : notesArr) {
			sb.append(s).append("\n");
		}
		return sb.toString();
	}
}
