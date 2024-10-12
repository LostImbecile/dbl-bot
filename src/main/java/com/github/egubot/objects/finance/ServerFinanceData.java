package com.github.egubot.objects.finance;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class ServerFinanceData {
	@SerializedName("users")
	private List<UserFinanceData> users = new ArrayList<>();
	@SerializedName("total_won")
	private double totalWon;
	@SerializedName("total_lost")
	private double totalLost;
	@SerializedName("prize_pool")
	private double prizePool;
	@SerializedName("base_transfer_limit")
	private double baseTransferLimit = 100.0;
	@SerializedName("base_daily")
	private int baseDaily = 100;
	@SerializedName("base_hourly")
	private int baseHourly = 30;

	// Getters and Setters
	public List<UserFinanceData> getUsers() {
		return users;
	}

	public void setUsers(List<UserFinanceData> users) {
		this.users = users;
	}

	public double getTotalWon() {
		return totalWon;
	}

	public void setTotalWon(double totalWon) {
		this.totalWon = round(totalWon);
	}

	public double getTotalLost() {
		return totalLost;
	}

	public void setTotalLost(double totalLost) {
		this.totalLost = round(totalLost);
	}

	public double getPrizePool() {
		return prizePool;
	}

	public void setPrizePool(double prizePool) {
		this.prizePool = round(prizePool);
	}

	public synchronized double getBaseTransferLimit() {
		return baseTransferLimit;
	}

	public synchronized void setBaseTransferLimit(double baseTransferLimit) {
		this.baseTransferLimit = round(baseTransferLimit);
	}

	public synchronized void addToPrizePool(double amount) {
		this.prizePool = round(amount + prizePool);
	}

	public synchronized void addTotalWon(double amount) {
		this.totalWon = round(amount + totalWon);
	}

	private double round(double amount) {
		return Math.round(amount * 10.0) / 10.0;
	}

	public synchronized void addTotalLost(double amount) {
		this.totalLost = round(amount + totalLost);
		addToPrizePool(amount);
	}

	public int getBaseDaily() {
		return baseDaily;
	}

	public void setBaseDaily(int baseDaily) {
		this.baseDaily = baseDaily;
	}

	public int getBaseHourly() {
		return baseHourly;
	}

	public void setBaseHourly(int baseHourly) {
		this.baseHourly = baseHourly;
	}

}
