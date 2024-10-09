package com.github.egubot.interfaces.finance;

import com.github.egubot.interfaces.HasPriority;
import com.github.egubot.objects.finance.UserFinanceData;

public interface BankLoanInterceptor extends HasPriority{
	boolean canLoan(UserFinanceData user, double amount);

	void applyLoan(UserFinanceData user, double amount);
}