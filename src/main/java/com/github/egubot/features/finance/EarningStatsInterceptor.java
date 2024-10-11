package com.github.egubot.features.finance;

import com.github.egubot.build.UserBalance;
import com.github.egubot.interfaces.finance.EarningLossInterceptor;
import com.github.egubot.objects.finance.UserFinanceData;

public class EarningStatsInterceptor implements EarningLossInterceptor {

	@Override
	public double apply(UserBalance serverData, UserFinanceData data, double amount) {
		if (amount > 0) {
			data.addEarnings(amount);
			serverData.getServerFinanceData().addTotalWon(amount);
		} else if (amount < 0) {
			data.addLoss(Math.abs(amount));
			serverData.getServerFinanceData().addTotalLost(Math.abs(amount));
		}
		return 0;
	}

	@Override
	public int getPriority() {
		return 100;
	}
}
