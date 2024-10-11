package com.github.egubot.features.finance;

import com.github.egubot.objects.finance.ServerFinanceData;
import com.github.egubot.objects.finance.UserFinanceData;
import com.github.egubot.shared.utils.DateUtils;

public class HourlyClaimManager {

	public static double apply(UserFinanceData data, ServerFinanceData server) {
		double amount = 0;
		if (canClaimHourly(data)) {
			data.setLastHourlyClaim(DateUtils.hoursSinceEpoch());
			amount = calculateAmount(data, server);
		}
		return amount;
	}

	public static int calculateAmount(UserFinanceData data, ServerFinanceData server) {
		return server.getBaseHourly() + (data.getCreditScore() / 6);
	}

	private static boolean canClaimHourly(UserFinanceData data) {
		long lastClaim = data.getLastHourlyClaim();
		if (lastClaim == 0) {
			return true;
		}
		return DateUtils.hoursSinceEpoch() - lastClaim >= 1;
	}

}