package com.github.egubot.interfaces;

import java.io.IOException;
import java.util.List;

import org.javacord.api.entity.message.Messageable;

public interface DataManager {
	List<String> getData();

	void setData(List<String> data);
	
	public void initialise(boolean verbose) throws IOException;

	public int getLockedDataEndIndex();

	public void setLockedDataEndIndex(int lockedDataEndIndex);

	public void sendData(Messageable e);
	
	public void writeData(Messageable e);
	
	public void readData(Messageable e);
}
