package com.github.egubot.facades;

import java.util.List;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.build.RollTemplates;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.shared.UserInfoUtilities;

public class LegendsTemplatesFacade implements Shutdownable {
	private RollTemplates templates = null;

	public LegendsTemplatesFacade(LegendsDatabase legendsWebsite) {
		templates = new RollTemplates(legendsWebsite);
	}

	public boolean checkTemplateCommands(Message msg, String lowCaseTxt) {
		boolean isOwner = UserInfoUtilities.isOwner(msg);
		if (lowCaseTxt.contains("b-template create")) {
			templates.writeTemplate(lowCaseTxt, msg.getChannel());
			return true;
		}

		if (lowCaseTxt.contains("b-template remove")) {
			templates.removeTemplate(lowCaseTxt, msg.getChannel(), isOwner);
			return true;
		}

		if (lowCaseTxt.contains("b-template send")) {
			templates.sendData(msg.getChannel());
			return true;
		}

		if (lowCaseTxt.contains("b-template lock") && isOwner) {
			try {
				int x = Integer.parseInt(lowCaseTxt.replaceAll("\\D", ""));
				templates.setLockedDataEndIndex(x);
				templates.writeData(msg.getChannel(), false);
			} catch (Exception e1) {
				//
			}

			return true;
		}

		return false;
	}

	public List<String> getRollTemplates() {
		return templates.getRollTemplates();
	}

	@Override
	public void shutdown() {
		if (templates != null)
			templates.shutdown();
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}
}
