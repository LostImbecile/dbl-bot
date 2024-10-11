package com.github.egubot.interfaces.finance;

import com.github.egubot.build.UserBalance;
import com.github.egubot.interfaces.HasPriority;
import com.github.egubot.objects.finance.UserFinanceData;

public interface EarningLossInterceptor extends HasPriority {
	
	/**
	 * Note that amount may be negative or 0
	 * 
	 * @param serverData
	 * @param data
	 * @param amount
	 * @return change amount to be applied to the balance, may be positive or negative
	 */
	double apply(UserBalance serverData, UserFinanceData data, double amount);

	/**
	 * Called after apply, returns the data of any user modified besides the original user
	 * 
	 * @param serverData
	 * @param data
	 * @param amount
	 * @param changeAmount may be positive or negative
	 * @return an additional modified user's data 
	 */
	default UserFinanceData afterApply(UserBalance serverData, UserFinanceData data, double amount, double changeAmount) {
		// nothing, return type is in case an additional user was modified
		return null;
	}

}
