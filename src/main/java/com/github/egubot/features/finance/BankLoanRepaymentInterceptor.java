package com.github.egubot.features.finance;

import com.github.egubot.features.finance.BalanceManager.UserPair;
import com.github.egubot.interfaces.finance.TransferInterceptor;
import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.objects.finance.UserFinanceData.BankLoan;

public class BankLoanRepaymentInterceptor implements TransferInterceptor {
	@Override
	public boolean canTransfer(UserFinanceData sender, UserFinanceData receiver, double amount,
			double baseTransferLimit) {
		// Always allow transfers for loan repayment
		return sender.getBankLoan() != null && receiver == null;
	}

	@Override
	public void afterTransfer(UserFinanceData sender, UserFinanceData receiver, double amount) {
		BankLoan bankLoan = sender.getBankLoan();
		double remaining = bankLoan.getAmount();
		remaining -= amount;
		if (remaining <= 0) {
			sender.setCreditScore(sender.getCreditScore() + bankLoan.getCreditScoreGainOnRepayment());
			sender.setBankLoan(null);
		} else {
			bankLoan.setAmount(remaining);
		}
	}

	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	public String getTransferType() {
		return UserPair.TRANSFER_TYPE_BANK_LOAN_REPAYMENT;
	}

}