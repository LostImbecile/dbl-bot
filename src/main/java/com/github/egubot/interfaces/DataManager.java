package com.github.egubot.interfaces;

import java.util.List;

import org.javacord.api.entity.message.Messageable;

public interface DataManager {
	public void writeData(Messageable e);

	List<String> getData();

	void setData(List<String> data);

	public int getLockedDataEndIndex();

	public void setLockedDataEndIndex(int lockedDataEndIndex);

	public void sendData(Messageable e);
}
