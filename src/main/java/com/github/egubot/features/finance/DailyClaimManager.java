package com.github.egubot.features.finance;

import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.shared.utils.DateUtils;

public class DailyClaimManager {

	public static double apply(UserFinanceData data) {
		double amount = 0;
		if (canClaimDaily(data)) {
			data.setLastDailyClaim(DateUtils.daysSinceEpoch());
			amount = calculateAmount(data);
		}
		return amount;
	}

	private static int calculateAmount(UserFinanceData data) {
		return 100 + (data.getCreditScore() / 2);
	}

	private static boolean canClaimDaily(UserFinanceData data) {
		long lastClaim = data.getLastDailyClaim();
		if (lastClaim == 0) {
			return true;
		}
		return DateUtils.daysSinceEpoch() - lastClaim >= 1; // next day started
	}

}