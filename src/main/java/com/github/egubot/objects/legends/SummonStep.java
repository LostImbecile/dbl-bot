package com.github.egubot.objects.legends;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SummonStep {
	private int currencyNeeded = 0;
	private String currencyType = "";
	private String rewardTitle = "";
	private String rewardURL = "";
	private String specialAttributeString = "";
	private int rewardNum = 0;
	private int numberOfPulls = 0;

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

	public boolean isExOrHigher() {
		return this.specialAttributeString.contains("EX100");
	}

	public boolean isSparkingOrHigher() {
		return this.specialAttributeString.contains("SP100");
	}

	public boolean isOneSparkingGuaranteed() {
		return this.specialAttributeString.contains("OverSP_1");
	}

	public boolean isThreeSparkingGuaranteed() {
		return this.specialAttributeString.contains("OverSP_3");
	}

	public boolean isOneLFGuaranteed() {
		return this.specialAttributeString.contains("LL100_1");
	}

	public boolean isThreeLFGuaranteed() {
		return this.specialAttributeString.contains("LL100_3");
	}

	public boolean isLFDouble() {
		return this.specialAttributeString.contains("LL_Double");
	}
	
	public boolean isLFTriple() {
		return this.specialAttributeString.contains("LL_Triple");
	}

	public boolean isUltraDouble() {
		return this.specialAttributeString.contains("UL_Double");
	}
	
	public boolean isUltraTriple() {
		return this.specialAttributeString.contains("UL_Triple");
	}

	public void setNumberOfPulls(int numberOfPulls) {
		this.numberOfPulls = numberOfPulls;
	}

	private static final Pattern PULLS_PATTERN = Pattern.compile("acquire (\\d+) character");

	public void setNumberOfPulls(String text) {
		if (text == null)
			return;
		Matcher matcher = PULLS_PATTERN.matcher(text);
		if (matcher.find()) {
			this.numberOfPulls = Integer.parseInt(matcher.group(1));
		} else if (text.contains("ten")) {
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
				+ rewardTitle + "\nrewardURL=" + rewardURL + "\nspecialAttribute=" + specialAttributeString
				+ "\nrewardNum=" + rewardNum + "\nnumberOfPulls=" + numberOfPulls + "\n]";
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
		if (specialAttributeString == null)
			return "";
		return specialAttributeString;
	}

	public void setSpecialAttribute(String specialAttribute) {
		this.specialAttributeString = specialAttribute;
	}

	public void setCurrencyNeeded(int currencyNeeded) {
		this.currencyNeeded = currencyNeeded;
	}

	public void setRewardNum(int rewardNum) {
		this.rewardNum = rewardNum;
	}
}
