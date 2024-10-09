package com.github.egubot.interfaces.finance;

import com.github.egubot.interfaces.HasPriority;
import com.github.egubot.objects.finance.UserFinanceData;

public interface UserLoanInterceptor extends HasPriority {
	boolean canLoan(UserFinanceData lender, UserFinanceData borrower, double amount);

	void applyLoan(UserFinanceData lender, UserFinanceData borrower, double amount, long dueDate, double penaltyRate);

}