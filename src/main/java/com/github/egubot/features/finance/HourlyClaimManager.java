package com.github.egubot.features.finance;

import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.shared.utils.DateUtils;

public class HourlyClaimManager {

	public static double apply(UserFinanceData data) {
		double amount = 0;
		if (canClaimHourly(data)) {
			data.setLastHourlyClaim(DateUtils.hoursSinceEpoch());
			amount = calculateAmount(data);
		}
		return amount;
	}

	private static int calculateAmount(UserFinanceData data) {
		return 30 + (data.getCreditScore() / 6);
	}

	private static boolean canClaimHourly(UserFinanceData data) {
		long lastClaim = data.getLastHourlyClaim();
		if (lastClaim == 0) {
			return true;
		}
		return DateUtils.hoursSinceEpoch() - lastClaim >= 1;
	}

}