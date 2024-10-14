package com.github.egubot.objects.finance;

import com.github.egubot.shared.utils.DateUtils;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UserFinanceData {
	@SerializedName("user_id")
	private long userID = -1;
	@SerializedName("balance")
	private double balance = 0.0;
	@SerializedName("last_daily_claim")
	private long lastDailyClaim = 0;
	@SerializedName("last_hourly_claim")
	private long lastHourlyClaim = 0;
	@SerializedName("last_earnings_update")
	private long lastEarningsUpdate = 0;
	@SerializedName("total_earnings")
	private double totalEarnings = 0.0;
	@SerializedName("total_losses")
	private double totalLosses = 0.0;
	// Fields to calculate daily averages
	@SerializedName("earnings_sum")
	private double earningsSum = 0.0;
	@SerializedName("losses_sum")
	private double lossesSum = 0.0;
	@SerializedName("earnings_count")
	private int earningsCount = 0;
	@SerializedName("losses_count")
	private int lossesCount = 0;
	@SerializedName("daily_avg_earnings")
	private double dailyAverageEarnings = 0.0;
	@SerializedName("daily_avg_losses")
	private double dailyAverageLosses = 0.0;
	@SerializedName("credit_score")
	private int creditScore = 10;
	// User Loans
	@SerializedName("user_loan")
	private UserLoan userLoan = null;
	@SerializedName("bank_loan")
	private BankLoan bankLoan = null;
	// Assets
	@SerializedName("assets")
	private List<Asset> assets = null;
	// Transfer Limit
	@SerializedName("daily_transferred")
	private double dailyTransferred = 0.0;
	@SerializedName("last_transaction")
	private List<String> lastTransaction = new ArrayList<>();

	public UserFinanceData(long userID) {
		this.userID = userID;
	}

	public UserFinanceData(String userID) {
		this.userID = Long.parseLong(userID);
	}

	// Methods to update earnings and losses averages
	public void addEarnings(double amount) {
		resetDailyStats();
		amount = round(amount);
		earningsSum += amount;
		earningsCount++;
		totalEarnings += amount;
		// Not a necessary check but just in case
		if (earningsCount > 0)
			dailyAverageEarnings = round(earningsSum / earningsCount);
	}

	public void addLoss(double amount) {
		resetDailyStats();
		amount = round(amount);
		lossesSum += amount;
		lossesCount++;
		totalLosses += amount;
		if (lossesCount > 0)
			dailyAverageLosses = round(lossesSum / lossesCount);
	}

	public void resetDailyStats() {
		resetDaily(DateUtils.daysSinceEpoch());
	}

	private void resetDaily(long daysSinceEpoch) {
		if (lastEarningsUpdate < daysSinceEpoch) {
			System.out.println(lastEarningsUpdate);
			lossesSum = 0.0;
			lossesCount = 0;
			earningsSum = 0.0;
			earningsCount = 0;
			dailyAverageEarnings = 0.0;
			dailyAverageLosses = 0.0;
			dailyTransferred = 0.0;
			lastEarningsUpdate = daysSinceEpoch;
		}

	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = round(balance);
	}

	private static double round(double num) {
		return Math.round(num * 100.0) / 100.0;
	}

	public double getTotalEarnings() {
		return totalEarnings;
	}

	public void setTotalEarnings(double totalEarnings) {
		this.totalEarnings = totalEarnings;
	}

	public double getTotalLosses() {
		return totalLosses;
	}

	public void setTotalLosses(double totalLosses) {
		this.totalLosses = totalLosses;
	}

	public double getEarningsSum() {
		resetDailyStats();
		return earningsSum;
	}

	public void setEarningsSum(double earningsSum) {
		this.earningsSum = earningsSum;
	}

	public double getLossesSum() {
		resetDailyStats();
		return lossesSum;
	}

	public void setLossesSum(double lossesSum) {
		this.lossesSum = lossesSum;
	}

	public int getEarningsCount() {
		resetDailyStats();
		return earningsCount;
	}

	public void setEarningsCount(int earningsCount) {
		this.earningsCount = earningsCount;
	}

	public int getLossesCount() {
		resetDailyStats();
		return lossesCount;
	}

	public void setLossesCount(int lossesCount) {
		this.lossesCount = lossesCount;
	}

	public double getDailyAverageEarnings() {
		resetDailyStats();
		return dailyAverageEarnings;
	}

	public void setDailyAverageEarnings(double dailyAverageEarnings) {
		this.dailyAverageEarnings = dailyAverageEarnings;
	}

	public double getDailyAverageLosses() {
		resetDailyStats();
		return dailyAverageLosses;
	}

	public void setDailyAverageLosses(double dailyAverageLosses) {
		this.dailyAverageLosses = dailyAverageLosses;
	}

	public int getCreditScore() {
		return creditScore;
	}

	public void setCreditScore(int creditScore) {
		this.creditScore = creditScore;
	}

	public UserLoan getUserLoan() {
		return userLoan;
	}

	public void setUserLoan(UserLoan userLoan) {
		this.userLoan = userLoan;
	}

	public BankLoan getBankLoan() {
		return bankLoan;
	}

	public void setBankLoan(BankLoan bankLoan) {
		this.bankLoan = bankLoan;
	}

	public List<Asset> getAssets() {
		return assets;
	}

	public void setAssets(List<Asset> assets) {
		this.assets = assets;
	}

	public double getDailyTransferred() {
		resetDailyStats();
		return dailyTransferred;
	}

	public void setDailyTransferred(double transferLimit) {
		this.dailyTransferred = round(transferLimit);
	}

	public static class BankLoan {
		@SerializedName("amount")
		private double amount;
		@SerializedName("original_amount")
		private double originalAmount;
		@SerializedName("interest_rate")
		private double interestRate;
		@SerializedName("issue_date")
		private String issueDate;
		@SerializedName("due_date")
		private long dueDate;
		@SerializedName("credit_score_gain")
		private int creditScoreGainOnRepayment;
		@SerializedName("amount_used_before_repayment")
		private double amountUsedBeforeRepayment = 0.0;

		public BankLoan() {
		}

		public void applyInterest() {
			setAmount(getAmount() + getAmount() * getInterestRate() / 100);
		}

		public double getAmount() {
			return amount;
		}

		public void setAmount(double amount) {
			this.amount = round(amount);
		}

		public double getInterestRate() {
			return interestRate;
		}

		public void setInterestRate(double interestRate) {
			this.interestRate = interestRate;
		}

		public String getIssueDate() {
			return issueDate;
		}

		public void setIssueDate(String issueDate) {
			this.issueDate = issueDate;
		}

		public long getDueDate() {
			return dueDate;
		}

		public void setDueDate(long dueDate) {
			this.dueDate = dueDate;
		}

		public int getCreditScoreGainOnRepayment() {
			return creditScoreGainOnRepayment;
		}

		public void setCreditScoreGainOnRepayment(int creditScoreGainOnRepayment) {
			this.creditScoreGainOnRepayment = creditScoreGainOnRepayment;
		}

		public BankLoan(BankLoan bankLoan) {
			this.amount = bankLoan.amount;
			this.interestRate = bankLoan.interestRate;
			this.issueDate = bankLoan.issueDate;
			this.dueDate = bankLoan.dueDate;
			this.creditScoreGainOnRepayment = bankLoan.creditScoreGainOnRepayment;
			this.amountUsedBeforeRepayment = bankLoan.amountUsedBeforeRepayment;
			this.originalAmount = bankLoan.originalAmount;
		}

		public double getOriginalAmount() {
			return originalAmount;
		}

		public void setOriginalAmount(double originalAmount) {
			this.originalAmount = round(originalAmount);
		}

		public double getAmountUsedBeforeRepayment() {
			return amountUsedBeforeRepayment;
		}

		public void setAmountUsedBeforeRepayment(double amountUsedBeforeRepayment) {
			this.amountUsedBeforeRepayment = round(amountUsedBeforeRepayment);
		}

		public void addAmountUsedBeforeRepayment(double amount) {
			this.amountUsedBeforeRepayment += round(amount);
		}

		public boolean isOverdue() {
			return DateUtils.hasPassed(dueDate);
		}

		public double leftBeforeAllowingPayback() {
			return round(getOriginalAmount() - getAmountUsedBeforeRepayment());
		}

	}

	public static class UserLoan {
		@SerializedName("amount")
		private double amount;
		@SerializedName("due_date")
		private long dueDate;
		@SerializedName("penalty_rate")
		private double penaltyRate; // in %
		@SerializedName("lender_id")
		private long lenderId;
		@SerializedName("applied_penalty")
		private boolean appliedPenalty = false;

		public UserLoan() {
		}

		public void applyPenalty() {
			setAmount(getAmount() * 2);
		}

		public double getAmount() {
			return amount;
		}

		public void setAmount(double amount) {
			this.amount = round(amount);
		}

		public double getPenaltyRate() {
			return penaltyRate;
		}

		public void setPenaltyRate(double penaltyRate) {
			this.penaltyRate = penaltyRate;
		}

		public long getDueDate() {
			return dueDate;
		}

		public void setDueDate(long dueDate) {
			this.dueDate = dueDate;
		}

		public long getLenderId() {
			return lenderId;
		}

		public void setLenderId(long lenderId) {
			this.lenderId = lenderId;
		}

		public UserLoan(UserLoan userLoan) {
			this.amount = userLoan.amount;
			this.dueDate = userLoan.dueDate;
			this.penaltyRate = userLoan.penaltyRate;
			this.lenderId = userLoan.lenderId;
			this.appliedPenalty = userLoan.appliedPenalty;
		}

		public boolean isAppliedPenalty() {
			return appliedPenalty;
		}

		public void setAppliedPenalty(boolean appliedPenalty) {
			this.appliedPenalty = appliedPenalty;
		}
	}

	// Custom Asset Class
	public static class Asset {
		@SerializedName("name")
		private String name;

		@SerializedName("quantity")
		private int quantity;

		public Asset() {
		}

		public Asset(String name, int quantity) {
			this.name = name;
			this.quantity = quantity;
		}

		public Asset(Asset asset) {
			this.name = asset.name;
			this.quantity = asset.quantity;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
	}

	public List<Asset> copyAssets() {
		return assets.stream().map(Asset::new).toList();
	}

	public UserFinanceData(UserFinanceData userFinanceData) {
		this.userID = userFinanceData.userID;
		this.balance = userFinanceData.balance;
		this.lastDailyClaim = userFinanceData.lastDailyClaim;
		this.lastHourlyClaim = userFinanceData.lastHourlyClaim;
		this.totalEarnings = userFinanceData.totalEarnings;
		this.totalLosses = userFinanceData.totalLosses;
		this.earningsSum = userFinanceData.earningsSum;
		this.lossesSum = userFinanceData.lossesSum;
		this.earningsCount = userFinanceData.earningsCount;
		this.lossesCount = userFinanceData.lossesCount;
		this.dailyAverageEarnings = userFinanceData.dailyAverageEarnings;
		this.dailyAverageLosses = userFinanceData.dailyAverageLosses;
		this.creditScore = userFinanceData.creditScore;
		this.dailyTransferred = userFinanceData.dailyTransferred;
		this.lastEarningsUpdate = userFinanceData.lastEarningsUpdate;
		
		if (userFinanceData.userLoan != null)
			this.userLoan = new UserLoan(userFinanceData.userLoan);
		if (userFinanceData.bankLoan != null)
			this.bankLoan = new BankLoan(userFinanceData.bankLoan);
		if (userFinanceData.assets != null)
			this.assets = userFinanceData.copyAssets();
		if (userFinanceData.lastTransaction != null)
			this.lastTransaction = new ArrayList<>(userFinanceData.lastTransaction);
	}

	public long getLastDailyClaim() {
		return lastDailyClaim;
	}

	public void setLastDailyClaim(long lastDailyClaim) {
		this.lastDailyClaim = lastDailyClaim;
	}

	public long getLastHourlyClaim() {
		return lastHourlyClaim;
	}

	public void setLastHourlyClaim(long lastHourlyClaim) {
		this.lastHourlyClaim = lastHourlyClaim;
	}

	public long getLastEarningsUpdate() {
		return lastEarningsUpdate;
	}

	public void setLastEarningsUpdate(long lastEarningsUpdate) {
		this.lastEarningsUpdate = lastEarningsUpdate;
	}

	public List<String> getLastTransaction() {
		return lastTransaction;
	}

	public void setLastTransaction(List<String> lastTransaction) {
		this.lastTransaction = lastTransaction;
	}

}
