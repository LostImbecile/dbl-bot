package com.github.egubot.facades;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.AutoRespond;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.shared.UserInfoUtilities;

public class AutoRespondFacade implements Shutdownable{
	private AutoRespond autoRespond = null;
	
	public AutoRespondFacade() {
		autoRespond = new AutoRespond();
	}

	public boolean checkCommands(Message msg, String msgText) {
		String lowCaseTxt = msgText.toLowerCase();
		if (lowCaseTxt.matches("b-response(?s).*")) {
			boolean isOwner = UserInfoUtilities.isOwner(msg);

			if (lowCaseTxt.contains("b-response create")) {
				if (!lowCaseTxt.contains("sleep"))
					autoRespond.writeResponse(msgText, msg, isOwner);
				else
					msg.getChannel().sendMessage("nah");
				return true;
			}

			if (lowCaseTxt.contains("b-response remove")) {
				autoRespond.removeResponse(msgText, msg.getChannel(), isOwner);
				return true;
			}

			if (lowCaseTxt.contains("b-response edit")) {
				autoRespond.updateResponse(msgText, msg.getChannel(), isOwner);
				return true;
			}

			if (lowCaseTxt.equals("b-response send")) {
				autoRespond.sendData(msg.getChannel());
				return true;
			}

			if (lowCaseTxt.contains("b-response lock") && isOwner) {
				try {
					int x = Integer.parseInt(lowCaseTxt.replaceAll("\\D", ""));
					autoRespond.setLockedDataEndIndex(x);
					autoRespond.writeData(msg.getChannel(), false);
				} catch (Exception e1) {
					//
				}
				return true;
			}
			if (lowCaseTxt.equals("b-response update")) {
				try {
					autoRespond.writeData(msg.getChannel());
				} catch (Exception e1) {
					//
				}
			}

		}

		return false;
	}

	@Override
	public void shutdown() {
		if (autoRespond != null)
			autoRespond.shutdown();		
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

	public boolean respond(String msgText, Message msg) {
		return autoRespond.respond(msgText, msg);
	}
}
