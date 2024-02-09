package com.github.egubot.build;

import java.io.IOException;

import com.github.egubot.storage.DataManagerHandler;

public class PresetTimers extends DataManagerHandler{
	private static String resourcePath = "Timers.txt";
	private static String idKey = "Timers_Message_ID";

	public PresetTimers() throws IOException{
		super(idKey, resourcePath,"Timers", true);
	}
	
	public static void main(String[] args) {
		
	}
}
