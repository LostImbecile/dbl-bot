package com.github.egubot.objects;

public class KeyValue {

	private String key;
	private String value;

	public KeyValue(String tokenKey, String value) {
		this.key = tokenKey;
		this.value = value;
	}

	public String getTokenKey() {
		return key;
	}

	public void setTokenKey(String apiKey) {
		this.key = apiKey;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
