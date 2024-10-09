package com.github.egubot.features.finance;

import java.time.Instant;
import java.util.Random;

import com.github.egubot.build.UserBalance;
import com.github.egubot.interfaces.finance.BalanceInterceptor;
import com.github.egubot.interfaces.finance.BankLoanInterceptor;
import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.objects.finance.UserFinanceData.BankLoan;
import com.github.egubot.shared.utils.DateUtils;

public class BankLoanProcessor implements BalanceInterceptor, BankLoanInterceptor {
	public static final Random rng = new Random();

	@Override
	public double apply(UserBalance serverData, UserFinanceData data, double amount) {
		if(amount <= 0)
			return 0;
		BankLoan bankLoan = data.getBankLoan();
		if (bankLoan != null && DateUtils.hasPassed(bankLoan.getDueDate())) {
			return calculateDeduction(bankLoan, amount);
		}
		return 0;
	}

	private double calculateDeduction(BankLoan bankLoan, double amount) {
		double percentCut = amount * 0.1;
		if (bankLoan.getAmount() > percentCut)
			return - percentCut;
		return - bankLoan.getAmount();
	}

	@Override
	public UserFinanceData afterApply(UserBalance serverData, UserFinanceData data, double amount, double deduction) {
		BankLoan bankLoan = data.getBankLoan();
		if (bankLoan != null) {
			bankLoan.setAmount(bankLoan.getAmount() + deduction);
			if (bankLoan.getAmount() <= 0) {
				data.setCreditScore(Math.max(bankLoan.getCreditScoreGainOnRepayment() + data.getCreditScore(), 5));
				data.setBankLoan(null);
			} else {
				int penalty = rng.nextInt(2);
				bankLoan.setCreditScoreGainOnRepayment(bankLoan.getCreditScoreGainOnRepayment() - penalty);
			}
		}
		return null;
	}

	@Override
	public boolean canLoan(UserFinanceData user, double amount) {
		int creditScore = user.getCreditScore();
		int maxLoanAmount = calculateMaxLoanAmount(creditScore);
		int minLoanAmount = calculateMinLoanAmount(creditScore);
		return user.getBankLoan() == null && amount <= maxLoanAmount && amount >= minLoanAmount;
	}

	@Override
	public void applyLoan(UserFinanceData user, double amount) {
		int creditScore = user.getCreditScore();
		BankLoan loan = new BankLoan();
		loan.setInterestRate(calculateInterestRate(creditScore));
		loan.setAmount(amount * loan.getInterestRate());
		loan.setIssueDate(DateUtils.getDateTimeNow());
		loan.setDueDate(Instant.now().toEpochMilli() + (1000 * 60 * 60 * 1000)); // 1 day
		loan.setCreditScoreGainOnRepayment(calculateCreditScoreGain(creditScore, amount));
		user.setBankLoan(loan);
		user.setBalance(user.getBalance() + amount);
	}

	private int calculateMaxLoanAmount(int creditScore) {
		return creditScore * 10;
	}

	private int calculateMinLoanAmount(int creditScore) {
		return creditScore * 5; // half the max
	}

	private double calculateInterestRate(int creditScore) {
		return Math.max(15 - creditScore / 10, 5); // Min of 5, starts at 14
	}

	private int calculateCreditScoreGain(int currentScore, double amount) {
		double loanPercent = (amount / calculateMaxLoanAmount(currentScore)) + 0.01;
		// Min of 3, grows by 1 every 20 points starting at 40 with full loans
		return Math.max(3, (int) ((currentScore / 20.0 + 2) * loanPercent)); 
	}

	@Override
	public int getPriority() {
		return 1;
	}
}
