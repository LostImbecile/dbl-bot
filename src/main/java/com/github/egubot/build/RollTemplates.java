package com.github.egubot.build;

import java.util.List;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.storage.DataManagerSwitcher;

public class RollTemplates extends DataManagerSwitcher {
	private static String idKey = "Templates_Message_ID";
	private static String resourcePath = "/RollTemplates.txt";

	private LegendsDatabase legendsWebsite;

	public RollTemplates(DiscordApi api, LegendsDatabase legendsWebsite) throws Exception {
		super(api, idKey, resourcePath, "Filter Templates", true);

		this.legendsWebsite = legendsWebsite;
	}

	public void removeTemplate(String msgText, Messageable e, boolean isOwner) {

		try {
			int startIndex = getLockedDataEndIndex();

			if (isOwner)
				startIndex = 0;

			String st = msgText.substring("b-template remove".length()).strip();

			try {
				st = st.substring(0, st.indexOf(" "));
			} catch (Exception e1) {

			}

			if (st.isBlank())
				throw new Exception();

			boolean isNameExist = false;

			for (int j = startIndex; j < getData().size(); j++) {
				if (isTemplateEqual(getData().get(j), st)) {
					isNameExist = true;
					getData().remove(j);
					break;
				}
			}

			if (isNameExist) {
				writeData(e, false);
			} else {
				e.sendMessage("\"" + st + "\" is not a template <a:L:1155894861644435516>");
			}
		} catch (Exception e1) {
			e.sendMessage("Remove what?");
		}
	}

	public void writeTemplate(String msgText, Messageable e) {
		try {
			String[] tokens = msgText.split("b-template create");
			String newTemplate = tokens[1].strip().replaceFirst(" ", " (") + ")\n";

			String temp;
			do {
				temp = tokens[1];
				for (int j = 0; j < getData().size(); j++) {
					tokens[1] = tokens[1].replace(getTemplateName(getData().get(j)).toLowerCase(),
							(getTemplateBody(getData().get(j)).toLowerCase()));
				}
			} while (!temp.equals(tokens[1]));

			tokens[1] = tokens[1].replaceAll("[-()&|+\n\t]", " ").trim().replaceAll("\\s+", " ");

			tokens = tokens[1].split(" ");
			boolean isTag = false;
			boolean isNameExist = false;

			for (int j = 0; j < getData().size(); j++) {
				if (isTemplateEqual(getData().get(j), newTemplate)) {
					isNameExist = true;
					break;
				}
			}

			if (isNameExist) {
				e.sendMessage("\"" + tokens[0] + "\" already exists <:joea:1144008494568194099>");
				return;
			}

			for (int i = 1; i < tokens.length; i++) {

				for (int j = 0; j < legendsWebsite.getTags().size(); j++) {
					if (tokens[i].equalsIgnoreCase(legendsWebsite.getTags().get(j).getName())) {
						isTag = true;
					}
				}
				if (!isTag) {
					e.sendMessage("\"" + tokens[i] + "\"?? not a tag <:joeh:1144008503355265075>");
					break;
				}
			}

			if (isTag) {
				getData().add(newTemplate);
				writeData(e, false);
			}
		} catch (Exception e1) {
			e.sendMessage("Correct format:" + "\nb-template create name filters");
		}
	}

	public static String getTemplateName(String st) {
		try {
			st = st.substring(0, st.indexOf(" "));
		} catch (Exception e) {
		}
		return st;
	}

	public static String getTemplateBody(String st) {
		try {
			st = st.substring(st.indexOf(" ") + 1);
		} catch (Exception e) {
		}

		return st;
	}

	static boolean isTemplateEqual(String st, String st2) {

		st = getTemplateName(st).toLowerCase();
		st2 = getTemplateName(st2).toLowerCase();

		return st.matches(st2 + "(?s).*") || st2.matches(st + "(?s).*");
	}

	public List<String> getRollTemplates() {
		return getData();
	}

}
