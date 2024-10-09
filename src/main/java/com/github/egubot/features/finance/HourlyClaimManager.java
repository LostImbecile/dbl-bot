package com.github.egubot.features.finance;

import com.github.egubot.objects.finance.UserFinanceData;

public class HourlyClaimManager {

	public static double apply(UserFinanceData data) {
		double amount = 0;
		if (canClaimHourly(data)) {
			data.setLastHourlyClaim(System.currentTimeMillis());
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
		return System.currentTimeMillis() - lastClaim >= 60 * 60 * 1000; // 1 hour
	}

}