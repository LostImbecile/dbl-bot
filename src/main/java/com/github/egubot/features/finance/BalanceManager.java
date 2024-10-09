package com.github.egubot.features.finance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.javacord.api.entity.message.Message;
import org.reflections.Reflections;

import com.github.egubot.build.UserBalance;
import com.github.egubot.interfaces.finance.BalanceInterceptor;
import com.github.egubot.interfaces.finance.BankLoanInterceptor;
import com.github.egubot.interfaces.finance.TransferInterceptor;
import com.github.egubot.interfaces.finance.UserLoanInterceptor;
import com.github.egubot.objects.finance.UserFinanceData;

public class BalanceManager {
	private static List<BalanceInterceptor> balanceInterceptors = new ArrayList<>();
	private static List<TransferInterceptor> transferInterceptors = new ArrayList<>();
	private static List<UserLoanInterceptor> userLoanInterceptors = new ArrayList<>();
	private static List<BankLoanInterceptor> bankLoanInterceptors = new ArrayList<>();

	private BalanceManager() {
	}

	static {
		Reflections reflections = new Reflections("com.github.egubot.features.finance");

		for (Class<? extends BalanceInterceptor> interceptor : reflections.getSubTypesOf(BalanceInterceptor.class)) {
			try {
				balanceInterceptors.add(interceptor.getConstructor().newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(balanceInterceptors);

		for (Class<? extends TransferInterceptor> interceptor : reflections.getSubTypesOf(TransferInterceptor.class)) {
			try {
				transferInterceptors.add(interceptor.getConstructor().newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(transferInterceptors);

		for (Class<? extends UserLoanInterceptor> interceptor : reflections.getSubTypesOf(UserLoanInterceptor.class)) {
			try {
				userLoanInterceptors.add(interceptor.getConstructor().newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(userLoanInterceptors);

		for (Class<? extends BankLoanInterceptor> interceptor : reflections.getSubTypesOf(BankLoanInterceptor.class)) {
			try {
				bankLoanInterceptors.add(interceptor.getConstructor().newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(bankLoanInterceptors);
	}

	public static UserPair updateBalance(UserBalance serverData, Message msg, double amount) {
		UserFinanceData userData = getUserDataCopy(serverData, msg);
		UserFinanceData lenderData = null;

		double change = 0;
		for (BalanceInterceptor interceptor : balanceInterceptors) {
			change = interceptor.apply(serverData, userData, amount);
			if (change != 0) {
				UserFinanceData temp = interceptor.afterApply(serverData, userData, amount, change);

				if (temp != null)
					lenderData = temp; // only one lender at once

				amount += change;
			}

		}
		userData.setBalance(userData.getBalance() + amount);

		return new UserPair(userData, lenderData);
	}

	public static UserPair transferMoney(UserBalance serverData, Message msg, long receiverId, double amount) {
		UserFinanceData sender = getUserDataCopy(serverData, msg);
		UserFinanceData receiver = getUserDataCopy(serverData, receiverId);
		UserPair userPair = new UserPair(sender, receiver);
		boolean canTransfer = false;
		for (TransferInterceptor interceptor : transferInterceptors) {
			if (interceptor.canTransfer(sender, receiver, amount,
					serverData.getServerFinanceData().getBaseTransferLimit())) {
				canTransfer = true;
				interceptor.afterTransfer(sender, receiver, amount);
				userPair.setTransferType(interceptor.getTransferType());
				;
				break;
			}
		}

		if (canTransfer) {
			sender.setBalance(sender.getBalance() - amount);
			receiver.setBalance(receiver.getBalance() + amount);
			return userPair;
		}
		return null;
	}

	public static UserPair applyUserLoan(UserBalance serverData, Message msg, long borrowerId, double amount,
			long dueDate, double penaltyRate) {
		UserFinanceData lender = getUserDataCopy(serverData, msg);
		UserFinanceData borrower = getUserDataCopy(serverData, borrowerId);
		UserPair userPair = null;
		for (UserLoanInterceptor loanInterceptor : userLoanInterceptors) {
			if (loanInterceptor.canLoan(lender, borrower, amount)) {
				loanInterceptor.applyLoan(lender, borrower, amount, dueDate, penaltyRate);
				userPair = new UserPair(lender, borrower);
			}
		}
		return userPair;
	}

	public static UserFinanceData applyBankLoan(UserBalance serverData, Message msg, double amount) {
		UserFinanceData user = getUserDataCopy(serverData, msg);
		boolean canLoan = false;
		for (BankLoanInterceptor loanInterceptor : bankLoanInterceptors) {
			if (loanInterceptor.canLoan(user, amount)) {
				canLoan = true;
				loanInterceptor.applyLoan(user, amount);
			}
		}
		if (canLoan)
			return user;
		return null;
	}

	public static UserFinanceData applyDaily(UserBalance serverData, Message msg) {
		UserFinanceData user = getUserDataCopy(serverData, msg);
		double amount = DailyClaimManager.apply(user);
		if (amount != 0)
			return user;
		return null;
	}

	public static UserFinanceData applyHourly(UserBalance serverData, Message msg) {
		UserFinanceData user = getUserDataCopy(serverData, msg);
		if (HourlyClaimManager.apply(user) != 0)
			return user;
		return null;
	}

	private static UserFinanceData getUserDataCopy(UserBalance serverData, long userId) {
		return new UserFinanceData(serverData.getUserData(userId));
	}

	private static UserFinanceData getUserDataCopy(UserBalance serverData, Message msg) {
		return new UserFinanceData(serverData.getUserData(msg));
	}

	public static class UserPair {
		public static final String TRANSFER_TYPE_USER_LOAN_REPAYMENT = "user_loan_repayment";
		public static final String TRANSFER_TYPE_BANK_LOAN_REPAYMENT = "bank_loan_repayment";
		public static final String TRANSFER_TYPE_NORMAL_TRANSFER = "normal";
		private UserFinanceData user1;
		private UserFinanceData user2;
		private String transferType = TRANSFER_TYPE_NORMAL_TRANSFER;

		public UserPair(UserFinanceData user1, UserFinanceData user2) {
			this.user1 = user1;
			this.user2 = user2;
		}

		public UserFinanceData getUser1() {
			return user1;
		}

		public UserFinanceData getUser2() {
			return user2;
		}

		public String getTransferType() {
			return transferType;
		}

		public void setTransferType(String transferType) {
			this.transferType = transferType;
		}
	}

}