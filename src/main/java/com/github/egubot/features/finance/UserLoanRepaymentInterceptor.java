package com.github.egubot.features.finance;

import com.github.egubot.features.finance.BalanceManager.UserPair;
import com.github.egubot.interfaces.finance.TransferInterceptor;
import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.objects.finance.UserFinanceData.UserLoan;

public class UserLoanRepaymentInterceptor implements TransferInterceptor {
	@Override
	public boolean canTransfer(UserFinanceData sender, UserFinanceData receiver, double amount, double baseTransferLimit) {
		// Always allow transfers for loan repayment
		return sender.getUserLoan() != null && receiver != null && sender.getUserLoan().getLenderId() == receiver.getUserID();
	}
	
	@Override
	public void afterTransfer(UserFinanceData sender, UserFinanceData receiver, double amount) {
		UserLoan userLoan = sender.getUserLoan();
		double remaining = userLoan.getAmount();
		remaining -= amount;
		if (remaining <= 0) {
			sender.setUserLoan(null);
		} else {
			userLoan.setAmount(remaining);
		}
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public String getTransferType() {
		return UserPair.TRANSFER_TYPE_USER_LOAN_REPAYMENT;
	}

}