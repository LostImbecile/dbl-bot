package com.github.egubot.features.finance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.javacord.api.entity.message.Message;
import org.reflections.Reflections;

import com.github.egubot.build.UserBalance;
import com.github.egubot.interfaces.finance.EarningLossInterceptor;
import com.github.egubot.interfaces.finance.BalanceUseInterceptor;
import com.github.egubot.interfaces.finance.BankLoanInterceptor;
import com.github.egubot.interfaces.finance.TransferInterceptor;
import com.github.egubot.interfaces.finance.UserLoanInterceptor;
import com.github.egubot.objects.finance.UserFinanceData;

public class BalanceManager {
	private static List<EarningLossInterceptor> earningLossInterceptors = new ArrayList<>();
	private static List<TransferInterceptor> transferInterceptors = new ArrayList<>();
	private static List<UserLoanInterceptor> userLoanInterceptors = new ArrayList<>();
	private static List<BankLoanInterceptor> bankLoanInterceptors = new ArrayList<>();
	private static List<BalanceUseInterceptor> balanceUseInterceptors = new ArrayList<>();

	private BalanceManager() {
	}

	static {
		Reflections reflections = new Reflections("com.github.egubot.features.finance");

		for (Class<? extends EarningLossInterceptor> interceptor : reflections
				.getSubTypesOf(EarningLossInterceptor.class)) {
			try {
				earningLossInterceptors.add(interceptor.getConstructor().newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(earningLossInterceptors);

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

		for (Class<? extends BalanceUseInterceptor> interceptor : reflections
				.getSubTypesOf(BalanceUseInterceptor.class)) {
			try {
				balanceUseInterceptors.add(interceptor.getConstructor().newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(balanceUseInterceptors);
	}

	public static UserPair updateBalance(UserBalance serverData, long userId, double amount) {
		UserFinanceData userData = getUserDataCopy(serverData, userId);
		UserFinanceData lenderData = null;
		userData.setLastTransaction(new ArrayList<>());
		String sign = "$";
		if (amount > 0)
			sign = "+" + sign;
		else
			sign = "-" + sign;
		userData.getLastTransaction().add("Processing transaction with amount: " + sign + amount);

		double change = 0;
		for (EarningLossInterceptor interceptor : earningLossInterceptors) {
			change = interceptor.apply(serverData, userData, amount);
			if (change != 0) {
				UserFinanceData temp = interceptor.afterApply(serverData, userData, amount, change);

				if (temp != null)
					lenderData = temp; // only one lender at once

				amount += change;
			}
		}
		userData.getLastTransaction().add(0, sign + amount);
		userData.setBalance(userData.getBalance() + amount);

		return new UserPair(userData, lenderData);
	}

	public static UserFinanceData registerLoss(UserBalance serverData, long userId, double amount) {
		UserFinanceData userData = getUserDataCopy(serverData, userId);
		amount = -Math.abs(amount);

		for (EarningLossInterceptor interceptor : earningLossInterceptors) {
			interceptor.apply(serverData, userData, amount);
		}
		return userData;
	}

	public static UserFinanceData registerLoss(UserBalance serverData, Message msg, double amount) {
		return registerLoss(serverData, msg.getAuthor().getId(), amount);
	}

	public static UserPair updateBalance(UserBalance serverData, Message msg, double amount) {
		return updateBalance(serverData, msg.getAuthor().getId(), amount);
	}

	public static UserPair transferMoney(UserBalance serverData, Message msg, long receiverId, double amount) {
		UserFinanceData sender = getUserDataCopy(serverData, msg);
		UserFinanceData receiver = null;
		if (receiverId != 0)
			receiver = getUserDataCopy(serverData, receiverId);
		UserPair userPair = new UserPair(sender, receiver);
		boolean canTransfer = false;
		sender.setLastTransaction(new ArrayList<>());
		sender.getLastTransaction().add("Processing $" + amount + " Transaction");

		for (TransferInterceptor interceptor : transferInterceptors) {
			if (interceptor.canTransfer(sender, receiver, amount,
					serverData.getServerFinanceData().getBaseTransferLimit())) {
				canTransfer = true;
				amount = interceptor.afterTransfer(sender, receiver, amount);
				userPair.setTransferType(interceptor.getTransferType());
				break;
			}
		}

		if (canTransfer) {
			sender.setBalance(sender.getBalance() - amount);
			if (receiver != null)
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
				loanInterceptor.applyLoan(serverData, lender, borrower, amount, dueDate, penaltyRate);
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

	public static UserFinanceData applyBalanceUse(UserBalance serverData, long userID, double amount) {
		UserFinanceData user = getUserDataCopy(serverData, userID);
		if (amount <= 0 || user.getBalance() < amount)
			return null;

		for (BalanceUseInterceptor balanceInterceptor : balanceUseInterceptors) {
			balanceInterceptor.processBalanceUse(serverData, user, amount);
		}

		user.setBalance(user.getBalance() - amount);

		return user;
	}

	public static UserFinanceData applyBalanceUse(UserBalance serverData, Message msg, double amount) {
		return applyBalanceUse(serverData, msg.getAuthor().getId(), amount);
	}

	public static UserFinanceData applyBalanceRetract(UserBalance serverData, long userID, double amount) {
		UserFinanceData user = getUserDataCopy(serverData, userID);
		if (amount <= 0)
			return null;

		for (BalanceUseInterceptor balanceInterceptor : balanceUseInterceptors) {
			balanceInterceptor.processBalanceRetract(serverData, user, amount);
		}

		user.setBalance(user.getBalance() + amount);

		return user;
	}

	public static UserFinanceData applyBalanceRetract(UserBalance serverData, Message msg, double amount) {
		return applyBalanceRetract(serverData, msg.getAuthor().getId(), amount);
	}

	public static boolean canUseAmount(UserFinanceData user, double amount) {
		return user.getBalance() > 0 || user.getBalance() >= amount;
	}

	public static boolean canUseAmount(UserBalance serverData, Message msg, double amount) {
		return canUseAmount(serverData, msg.getAuthor().getId(), amount);
	}

	public static boolean canUseAmount(UserBalance serverData, long userID, double amount) {
		return canUseAmount(serverData.getUserData(userID), amount);
	}

	public static UserFinanceData applyDaily(UserBalance serverData, Message msg) {
		UserFinanceData user = getUserDataCopy(serverData, msg);
		double amount = DailyClaimManager.apply(user, serverData.getServerFinanceData());
		if (amount != 0) {
			user.setBalance(user.getBalance() + amount);
			return user;
		}
		return null;
	}

	public static UserFinanceData applyHourly(UserBalance serverData, Message msg) {
		UserFinanceData user = getUserDataCopy(serverData, msg);
		double amount = HourlyClaimManager.apply(user, serverData.getServerFinanceData());
		if (amount != 0) {
			user.setBalance(user.getBalance() + amount);
			return user;
		}
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