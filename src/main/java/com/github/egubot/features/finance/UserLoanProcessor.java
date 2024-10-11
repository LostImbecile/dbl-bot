package com.github.egubot.features.finance;

import com.github.egubot.build.UserBalance;
import com.github.egubot.interfaces.finance.EarningLossInterceptor;
import com.github.egubot.interfaces.finance.UserLoanInterceptor;
import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.objects.finance.UserFinanceData.UserLoan;
import com.github.egubot.shared.utils.DateUtils;

public class UserLoanProcessor implements EarningLossInterceptor, UserLoanInterceptor {

	@Override
	public double apply(UserBalance serverData, UserFinanceData data, double amount) {
		if (amount <= 0)
			return 0;
		UserLoan userLoan = data.getUserLoan();
		if (userLoan != null && DateUtils.hasPassed(userLoan.getDueDate())) {
			return calculateDeduction(userLoan, amount);
		}
		return 0;
	}

	@Override
	public UserFinanceData afterApply(UserBalance serverData, UserFinanceData data, double amount, double deduction) {
		UserLoan userLoan = data.getUserLoan();
		if (userLoan != null) {
			if (!userLoan.isAppliedPenalty()) {
				userLoan.setAppliedPenalty(true);
				userLoan.setAmount(userLoan.getAmount() * 2);
			}
			UserFinanceData lender = serverData.getUserData(userLoan.getLenderId());
			lender.setBalance(lender.getBalance() - deduction);
			userLoan.setAmount(userLoan.getAmount() + deduction);

			if (userLoan.getAmount() <= 0) {
				data.setUserLoan(null);
				data.getLastTransaction().add(0, "-$" + (-deduction) + " from user loan. Loan is fully paid off.");
			} else
				data.getLastTransaction().add(0,
						"-$" + (-deduction) + " from user loan. Remaining amount: $" + userLoan.getAmount());
			return lender;
		}
		return null;
	}

	private double calculateDeduction(UserLoan userLoan, double amount) {
		double percentCut = amount * userLoan.getPenaltyRate();
		if (userLoan.getAmount() > percentCut)
			return -percentCut;
		return -userLoan.getAmount();
	}

	@Override
	public boolean canLoan(UserFinanceData lender, UserFinanceData borrower, double amount) {
		return lender.getBalance() >= amount && borrower.getUserLoan() == null && amount > 0
				&& lender.getUserID() != borrower.getUserID();
	}

	@Override
	public void applyLoan(UserBalance serverData, UserFinanceData lender, UserFinanceData borrower, double amount,
			long dueDate, double penaltyRate) {
		UserLoan loan = new UserLoan();
		double tax = amount * 0.15;
		loan.setAmount(amount);
		loan.setDueDate(dueDate);
		loan.setPenaltyRate(penaltyRate);
		loan.setLenderId(lender.getUserID());
		borrower.setUserLoan(loan);
		lender.setBalance(lender.getBalance() - amount);
		borrower.setBalance(borrower.getBalance() + amount - tax);
		serverData.getServerFinanceData().addToPrizePool(tax);
	}

	@Override
	public int getPriority() {
		return 0;
	}
}
