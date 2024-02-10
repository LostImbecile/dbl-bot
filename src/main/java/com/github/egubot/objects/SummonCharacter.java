package com.github.egubot.objects;

public class SummonCharacter {
	private Characters character;
	private int zPowerAmount;
	private double summonRate;
	private boolean isNew;

	public Characters getCharacter() {
		return character;
	}

	public void setCharacter(Characters character) {
		this.character = character;
	}

	public int getzPowerAmount() {
		return zPowerAmount;
	}

	public void setzPowerAmount(String leftValue) {
		String st = leftValue.replace("x", "");
		try {
			this.zPowerAmount = Integer.parseInt(st);
		} catch (Exception e) {
			this.zPowerAmount = 0;
		}
	}

	public void setzPowerAmount(int zPowerAmount) {
		this.zPowerAmount = zPowerAmount;
	}

	public double getSummonRate() {
		return summonRate;
	}

	public void setSummonRate(double summonRate) {
		this.summonRate = summonRate;
	}

	public void setSummonRate(String summonRate) {
		String st = summonRate.replaceFirst("x", "").replaceFirst("%", "");
		try {
			this.summonRate = Double.parseDouble(st) / 100;
		} catch (Exception e) {
			this.summonRate = 0;
		}
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	@Override
	public String toString() {
		return "SummonCharacter [\ncharacter=" + character + "\nzPowerAmount=" + zPowerAmount + "\nsummonRate="
				+ summonRate + "\nisNew=" + isNew + "\n]";
	}

}
