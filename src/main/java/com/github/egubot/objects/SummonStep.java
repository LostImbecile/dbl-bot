package com.github.egubot.objects;

public class SummonStep {
	private int currencyNeeded;
	private String currencyType;
	private String rewardTitle;
	private String rewardURL;
	private String specialAttribute;
	private int rewardNum;
	private int numberOfPulls;

	public void setRewardTitle(String rewardTitle) {
		this.rewardTitle = rewardTitle;
	}

	public String getRewardURL() {
		return rewardURL;
	}

	public void setRewardURL(String rewardURL) {
		this.rewardURL = rewardURL;
	}

	public int getRewardNum() {
		return rewardNum;
	}

	public void setRewardNum(String rewardNum) {
		String st = rewardNum.replaceFirst("x", "");
		try {
			this.rewardNum = Integer.parseInt(st);
		} catch (Exception e) {
			this.rewardNum = 0;
		}
	}

	public int getNumberOfPulls() {
		return numberOfPulls;
	}

	public void setNumberOfPulls(int numberOfPulls) {
		this.numberOfPulls = numberOfPulls;
	}

	public void setNumberOfPulls(String text) {
		if (text.contains("ten")) {
			this.numberOfPulls = 10;
		} else if (text.contains("seven")) {
			this.numberOfPulls = 7;
		} else if (text.contains("five")) {
			this.numberOfPulls = 5;
		} else if (text.contains("three")) {
			this.numberOfPulls = 3;
		} else if (text.contains("one")) {
			this.numberOfPulls = 1;
		} else if (text.contains("nine")) {
			this.numberOfPulls = 9;
		} else if (text.contains("eight")) {
			this.numberOfPulls = 8;
		} else if (text.contains("six")) {
			this.numberOfPulls = 6;
		} else if (text.contains("four")) {
			this.numberOfPulls = 4;
		} else if (text.contains("two")) {
			this.numberOfPulls = 2;
		}
	}

	@Override
	public String toString() {
		return "SummonStep [\ncurrencyNeeded=" + currencyNeeded + "\ncurrencyType=" + currencyType + "\nrewardTitle="
				+ rewardTitle + "\nrewardURL=" + rewardURL + "\nspecialAttribute=" + specialAttribute + "\nrewardNum="
				+ rewardNum + "\nnumberOfPulls=" + numberOfPulls + "\n]";
	}

	public String getCurrencyType() {
		return currencyType;
	}

	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}

	public int getCurrencyNeeded() {
		return currencyNeeded;
	}

	public void setCurrencyNeeded(String currencyNeeded) {
		String st = currencyNeeded.replace("x", "");
		try {
			this.currencyNeeded = Integer.parseInt(st);
		} catch (NumberFormatException e) {
			this.currencyNeeded = 0;
		}
	}

	public String getRewardTitle() {
		return rewardTitle;
	}

	public String getSpecialAttribute() {
		return specialAttribute;
	}

	public void setSpecialAttribute(String specialAttribute) {
		this.specialAttribute = specialAttribute;
	}

	public void setCurrencyNeeded(int currencyNeeded) {
		this.currencyNeeded = currencyNeeded;
	}

	public void setRewardNum(int rewardNum) {
		this.rewardNum = rewardNum;
	}
}
