package com.github.egubot.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.javacord.api.entity.message.Messageable;

import com.google.gson.reflect.TypeToken;

public abstract class BaseDataManager {
	public static final String STORAGE_FOLDER = "Storage";
    protected String dataName;
    protected List<String> data = Collections.synchronizedList(new ArrayList<String>());
    protected int lockedDataEndIndex;
    
	protected String filePath;
	protected String fileName;
    

    protected BaseDataManager(String dataName) {
        this.dataName = dataName;
    }

    public abstract void initialise(boolean verbose) throws IOException;
    public abstract void writeData(Messageable e);
    public abstract void readData(Messageable e);
    public abstract void sendData(Messageable e);
    
    public abstract <T> void writeJSON(String key, T object);
    public abstract <T> T readJSON(String key, Class<T> type);
    public abstract <T> List<T> readJSONList(String key, TypeToken<List<T>> typeToken);

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public int getLockedDataEndIndex() {
        return lockedDataEndIndex;
    }

    public void setLockedDataEndIndex(int lockedDataEndIndex) {
        this.lockedDataEndIndex = lockedDataEndIndex;
    }

    public abstract void close();
}