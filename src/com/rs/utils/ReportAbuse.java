package com.rs.utils;

import com.rs.discord.Bot;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.net.LoginClientChannelManager;
import com.rs.net.LoginProtocol;
import com.rs.net.encoders.LoginChannelsPacketEncoder;

public class ReportAbuse {

	public static void report(Player player) {
		report(player, null);
	}

	public static void report(Player player, String name) {
		if (player.getInterfaceManager().containsScreenInter()) {
			player.getPackets().sendGameMessage("Please close the interface that you opened before activating the 'Report' system.");
			return;
		}
		if (name != null)
			player.getPackets().sendCSVarString(24, name);
		if (player.getRights() > 0)
			player.getPackets().sendHideIComponent(594, 8, false);
		player.getInterfaceManager().sendInterface(594);

	}

	public static void report(Player player, String reportedName, int type, boolean mute) {
		if (mute && player.getRights() < 1)
			return;
		Player reported = World.getPlayerByDisplayName(reportedName);
		if (mute && reported != null) 
			LoginClientChannelManager.sendUnreliablePacket(LoginChannelsPacketEncoder.encodeAddOffence(LoginProtocol.OFFENCE_ADDTYPE_MUTE, reported.getDisplayName(), player.getUsername(), "Mute by report interface(" + getType(type) + ")", Utils.currentTimeMillis() + (1000 * 60 * 60 * 48)).trim());
		
		player.getPackets().sendGameMessage("Thank-you, your abuse report has been received.");
		Bot.sendLog(Bot.REPORTS_CHANNEL, "[type=REPORT][name="+player.getUsername()+"][target="+reportedName+"][reason="+getType(type)+"]");
	}

	private static String getType(int id) {
		switch (id) {
		case 6:
			return "Buying or selling account";
		case 9:
			return "Encouraging rule breaking";
		case 5:
			return "Staff impersonation";
		case 7:
			return "Macroing or use of bots";
		case 15:
			return "Scamming";
		case 4:
			return "Exploiting a bug";
		case 16:
			return "Seriously offensive language";
		case 17:
			return "Solicitation";
		case 18:
			return "Disruptive behaviour";
		case 19:
			return "Offensive account name";
		case 20:
			return "Real-life threats";
		case 13:
			return "Asking for or providing contact information";
		case 21:
			return "Breaking real-world laws";
		case 11:
			return "Advertising websites";
		}
		return "Unknown";
	}

}
