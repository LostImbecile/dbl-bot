package com.github.egubot.features.finance;

import com.github.egubot.features.finance.BalanceManager.UserPair;
import com.github.egubot.interfaces.finance.TransferInterceptor;
import com.github.egubot.objects.finance.UserFinanceData;

public class TransferLimitInterceptor implements TransferInterceptor {
	@Override
	public boolean canTransfer(UserFinanceData sender, UserFinanceData receiver, double amount,
			double baseTransferLimit) {
		if (receiver == null)
			return false;
		double transferLimit = calculateTransferLimit(sender, baseTransferLimit);
		return amount <= transferLimit && amount > 0 && sender.getUserID() != receiver.getUserID();
	}

	@Override
	public double afterTransfer(UserFinanceData sender, UserFinanceData receiver, double amount) {
		sender.setDailyTransferred(sender.getDailyTransferred() + amount);
		return amount;
	}

	public static double calculateTransferLimit(UserFinanceData sender, double baseTransferLimit) {
		double limit = baseTransferLimit;
		limit += calculateCreditScoreGain(sender.getCreditScore());
		limit -= sender.getDailyTransferred();
		if (sender.getBalance() < limit) {
			limit = sender.getBalance();
		}
		return limit;
	}

	public static int calculateCreditScoreGain(int creditScore) {
		return creditScore * 10;
	}

	@Override
	public int getPriority() {
		return 100;
	}

	@Override
	public String getTransferType() {
		return UserPair.TRANSFER_TYPE_NORMAL_TRANSFER;
	}

}