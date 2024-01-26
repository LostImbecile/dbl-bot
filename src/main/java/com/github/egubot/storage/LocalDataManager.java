package com.github.egubot.storage;

import java.util.List;

import org.javacord.api.entity.message.Messageable;

import com.github.egubot.interfaces.DataManager;

public class LocalDataManager implements DataManager{

	@Override
	public void writeData(Messageable e) {
		//
	}

	@Override
	public List<String> getData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setData(List<String> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getLockedDataEndIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLockedDataEndIndex(int lockedDataEndIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendData(Messageable e) {
		// TODO Auto-generated method stub
		
	}

}
