package com.github.egubot.interfaces.finance;

import com.github.egubot.build.UserBalance;
import com.github.egubot.interfaces.HasPriority;
import com.github.egubot.objects.finance.UserFinanceData;

public interface BalanceUseInterceptor extends HasPriority {
	
	/**
	 * Note that amount should be positive
	 * 
	 * @param serverData
	 * @param data
	 * @param amount     to remove
	 */
	void processBalanceUse(UserBalance serverData, UserFinanceData data, double amount);

	/**
	 * Note that amount should be positive
	 * 
	 * @param serverData
	 * @param data
	 * @param amount     to add
	 */
	void processBalanceRetract(UserBalance serverData, UserFinanceData data, double amount);

}
