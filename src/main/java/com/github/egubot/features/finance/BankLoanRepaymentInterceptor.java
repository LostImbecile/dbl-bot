package com.github.egubot.features.finance;

import com.github.egubot.features.finance.BalanceManager.UserPair;
import com.github.egubot.interfaces.finance.TransferInterceptor;
import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.objects.finance.UserFinanceData.BankLoan;

public class BankLoanRepaymentInterceptor implements TransferInterceptor {
	@Override
	public boolean canTransfer(UserFinanceData sender, UserFinanceData receiver, double amount,
			double baseTransferLimit) {
		// Only allow transfer if the original amount for bank loan has been spent
		return sender.getBankLoan() != null && receiver == null
				&& (sender.getBankLoan().leftBeforeAllowingPayback() <= 0 || sender.getBankLoan().isOverdue());
	}

	@Override
	public double afterTransfer(UserFinanceData sender, UserFinanceData receiver, double amount) {
		BankLoan bankLoan = sender.getBankLoan();
		double deducted = bankLoan.getAmount();
		double remaining = deducted;
		remaining -= amount;
		if (remaining <= 0) {
			sender.setCreditScore(sender.getCreditScore() + bankLoan.getCreditScoreGainOnRepayment());
			sender.setBankLoan(null);
			sender.getLastTransaction().add(0,
					"-$" + amount + " for bank loan. New Credit Score: " + sender.getCreditScore());
			return deducted;
		} else {
			bankLoan.setAmount(remaining);
			sender.getLastTransaction().add(0, "-$" + amount + " for bank loan. Remaining: $" + bankLoan.getAmount());
			return amount;
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