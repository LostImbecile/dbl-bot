package com.github.egubot.objects.legends;

import java.util.List;
import java.util.Map;

import com.github.egubot.features.legends.LegendsSummonRates;

public class SummonResults {
	Map<Integer, Double> oneRotation;
	Map<Integer, Double> threeRotation;
	Map<Integer, Double> customRotation;
	SummonBanner banner;
	List<SummonCharacter> focusCharacters;
	int numOfRotationsToGetFocusCharacter;
	
	public SummonResults(SummonBanner banner, List<SummonCharacter> focusCharacters) {
		this.banner = banner;
		this.focusCharacters = focusCharacters;
	}
	
	public int[] getRotationCosts() {
		return LegendsSummonRates.calculateRotationCosts(banner);
	}
	
	public int getIndividualPulls(int rotationNumber) {
		return LegendsSummonRates.calculateAmountOfIndividualPulls(banner, rotationNumber);
	}

	public Map<String, Double> getRotationTotal(Map<Integer, Double> rotation) {
		if(rotation == null)
			return null;
		return LegendsSummonRates.getTotalSummonsRates(banner, rotation);
	}

	public Map<Integer, Double> getOneRotation() {
		return oneRotation;
	}

	public void setOneRotation(Map<Integer, Double> oneRotation) {
		this.oneRotation = oneRotation;
	}

	public Map<Integer, Double> getThreeRotation() {
		return threeRotation;
	}

	public void setThreeRotation(Map<Integer, Double> threeRotation) {
		this.threeRotation = threeRotation;
	}

	public Map<Integer, Double> getCustomRotation() {
		return customRotation;
	}

	public void setCustomRotation(Map<Integer, Double> customRotation) {
		this.customRotation = customRotation;
	}

	public SummonBanner getBanner() {
		return banner;
	}

	public void setBanner(SummonBanner banner) {
		this.banner = banner;
	}

	public List<SummonCharacter> getFocusCharacters() {
		return focusCharacters;
	}

	public void setFocusCharacters(List<SummonCharacter> focusCharacters) {
		this.focusCharacters = focusCharacters;
	}

	public int getNumOfRotationsToGetFocusCharacter() {
		return numOfRotationsToGetFocusCharacter;
	}

	public void setNumOfRotationsToGetFocusCharacter(int numOfRotationsToGetFocusCharacter) {
		this.numOfRotationsToGetFocusCharacter = numOfRotationsToGetFocusCharacter;
	}
}
