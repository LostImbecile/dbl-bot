package com.github.egubot.build;

import java.util.List;
import org.javacord.api.entity.message.Message;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.storage.DataManagerHandler;

public class LegendsMaintenance extends DataManagerHandler {

	public LegendsMaintenance()  {
		super("Legends Maintenance", false);
	}

	public void registerServer(Message msg, String args) {
		String serverID = ServerInfoUtilities.getServerID(msg) + "";
		for (String st : getData()) {
			if (st.startsWith(serverID)) {
				msg.getChannel().sendMessage("Server already registered");
				return;
			}
		}
		getData().add(serverID + " " + args);
		writeData(msg.getChannel());
	}

	public void removeServer(Message msg) {
		String serverID = ServerInfoUtilities.getServerID(msg) + "";
		List<String> data = getData();
		for (int i = 0; i < data.size(); i++) {
			String st = data.get(i);
			if (st.startsWith(serverID)) {
				data.remove(i);
				writeData(msg.getChannel());
				return;
			}
		}
	}

	public void update(Message msg, String args) {
		String serverID = ServerInfoUtilities.getServerID(msg) + "";
		for (int i = 0; i < getData().size(); i++) {
			String st = getData().get(i);
			if (st.startsWith(serverID)) {
				getData().set(i, serverID + " " + args);
				writeData(msg.getChannel());
				return;
			}
		}
	}

}
