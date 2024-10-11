package com.github.egubot.interfaces.finance;

import com.github.egubot.interfaces.HasPriority;
import com.github.egubot.objects.finance.UserFinanceData;

public interface TransferInterceptor extends HasPriority {
	/**
	 * Should be able to handle a null receiver
	 * 
	 * @param sender
	 * @param receiver          can be null
	 * @param amount
	 * @param baseTransferLimit
	 * @return true if the transfer is allowed
	 */
	boolean canTransfer(UserFinanceData sender, UserFinanceData receiver, double amount, double baseTransferLimit);

	/**
	 * Process the transfer and return the amount to be removed from the user's
	 * balance
	 * 
	 * @param sender
	 * @param receiver
	 * @param amount
	 * @return amount deducted in the transfer
	 */
	default double afterTransfer(UserFinanceData sender, UserFinanceData receiver, double amount) {
		sender.getLastTransaction().add(0, "Transferred $" + amount);
		return 0;
	}

	String getTransferType();
}