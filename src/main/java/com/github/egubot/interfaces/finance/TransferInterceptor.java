package com.github.egubot.interfaces.finance;

import com.github.egubot.interfaces.HasPriority;
import com.github.egubot.objects.finance.UserFinanceData;

public interface TransferInterceptor extends HasPriority{
	/**
	 * Should be able to handle a null receiver
	 * @param sender
	 * @param receiver can be null
	 * @param amount
	 * @param baseTransferLimit
	 * @return true if the transfer is allowed
	 */
    boolean canTransfer(UserFinanceData sender, UserFinanceData receiver, double amount, double baseTransferLimit);
    
    default void afterTransfer(UserFinanceData sender, UserFinanceData receiver, double amount) {
    	// do nothing
    }
    
    int getPriority();
    
	default int compareTo(TransferInterceptor other) {
		return getPriority() - other.getPriority();
	}

	String getTransferType();
}