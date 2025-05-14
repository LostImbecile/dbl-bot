package com.github.egubot.objects;

import java.util.List;

public class ModelListResponse {
	private String object;
	private List<ModelData> data;

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public List<ModelData> getData() {
		return data;
	}

	public void setData(List<ModelData> data) {
		this.data = data;
	}
}