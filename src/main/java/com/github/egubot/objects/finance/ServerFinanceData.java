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
		this.totalWon = totalWon;
	}

	public double getTotalLost() {
		return totalLost;
	}

	public void setTotalLost(double totalLost) {
		this.totalLost = totalLost;
	}

	public double getPrizePool() {
		return prizePool;
	}

	public void setPrizePool(double prizePool) {
		this.prizePool = prizePool;
	}

	public double getBaseTransferLimit() {
		return baseTransferLimit;
	}

	public void setBaseTransferLimit(double baseTransferLimit) {
		this.baseTransferLimit = baseTransferLimit;
	}

	public void addTotalWon(double amount) {
		this.totalWon += amount;
	}

	public void addTotalLost(double amount) {
		this.totalLost += amount;
		this.prizePool -= amount;
	}

}
