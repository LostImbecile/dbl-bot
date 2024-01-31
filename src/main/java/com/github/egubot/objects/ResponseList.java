package com.github.egubot.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ResponseList {
	@SerializedName("count")
	private int count = 0;
	@SerializedName("lock_index")
	private int lockedDataIndex;
	@SerializedName("responses")
	private List<Response> responses;

	public ResponseList() {
		this.responses = Collections.synchronizedList(new ArrayList<>(100));
	}

	public List<Response> getResponses() {
		return responses;
	}

	public void setResponses(List<Response> responses) {
		this.responses = responses;
	}

	public void setLockedDataIndex(int lockedDataIndex) {
		this.lockedDataIndex = lockedDataIndex;
	}

	public int getLockedDataIndex() {
		return lockedDataIndex;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
