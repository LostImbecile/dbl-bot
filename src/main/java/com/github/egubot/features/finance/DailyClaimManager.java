package com.github.egubot.features.finance;

import com.github.egubot.objects.finance.ServerFinanceData;
import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.shared.utils.DateUtils;

public class DailyClaimManager {

	public static double apply(UserFinanceData data, ServerFinanceData server) {
		double amount = 0;
		if (canClaimDaily(data)) {
			data.setLastDailyClaim(DateUtils.daysSinceEpoch());
			amount = calculateAmount(data, server);
		}
		return amount;
	}

	public static int calculateAmount(UserFinanceData data, ServerFinanceData server) {
		return server.getBaseDaily() + (data.getCreditScore() / 2);
	}

	private static boolean canClaimDaily(UserFinanceData data) {
		long lastClaim = data.getLastDailyClaim();
		if (lastClaim == 0) {
			return true;
		}
		return DateUtils.daysSinceEpoch() - lastClaim >= 1; // next day started
	}

}