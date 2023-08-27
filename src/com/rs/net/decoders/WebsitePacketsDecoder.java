package com.rs.net.decoders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rs.Settings;
import com.rs.login.GameWorld;
import com.rs.login.Login;
import com.rs.login.Offences;
import com.rs.login.account.Account;
import com.rs.net.LoginServerChannelManager;
import com.rs.net.WebsiteClient;
import com.rs.net.encoders.LoginChannelsPacketEncoder;
import com.rs.utils.Encrypt;
import com.rs.utils.Logger;
import com.rs.utils.LoginFilesManager;
import com.rs.utils.Utils;

public class WebsitePacketsDecoder {

	public static void decodeIncomingMessage(WebsiteClient client, String message) {
		if (Settings.DEBUG)
			Logger.log("Website", "Received message:" + message);

		if (message.equals("GET_VAR:players_online")) {
			client.sendMessage("" + Login.getTotalOnline());
		} else if (message.startsWith("GET_VAR:players_online_w")) {
			GameWorld world = Login.getWorld(Integer.parseInt(message.substring(24)));
			client.sendMessage("" + (world != null ? (world.getGamePlayersOnline() + world.getLobbyPlayersOnline()) : -1));
		} else if (message.startsWith("LOGIN:")) {
			String[] spl = message.substring(6).split("\\@");
			String username = spl[0];
			String password = Encrypt.encryptSHA1(spl[1]);
			
			if (!Utils.invalidAccountName(username)) {
				Account account = Login.forceLoadAccount(username);
				if (account == null || !account.getPassword().equals(password)) {
					client.sendMessage(account == null && !Settings.HOSTED ? "loginok" : "notok");
				} else {
					client.sendMessage("loginok");
				}
			}
			else
				client.sendMessage("notok");
		} else if (message.startsWith("GET_RIGHTS:")) {
			String username = message.substring(11);
			
			if (Utils.invalidAccountName(username))
				client.sendMessage("0");
			else {
				Account account = Login.forceLoadAccount(username);
				if (account == null) {
					client.sendMessage("0");
				} else {
					if (Login.isBanned(account))
						client.sendMessage("0");
					else
						client.sendMessage("" + account.getRights());
				}
			}
		} else if (message.startsWith("CHANGE_PASSWORD:")) {
			String[] spl = message.substring(16).split("\\@");
			String username = spl[0];
			String password = Encrypt.encryptSHA1(spl[1]);
			String npassword = Encrypt.encryptSHA1(spl[2]);

			Account account = Login.forceLoadAccount(username);
			if (account == null || !account.getPassword().equals(password)) {
				client.sendMessage("notok");
			} else {
				account.setPassword(npassword);
				if (account.getWorld() == null)
					LoginFilesManager.saveAccount(account);
				client.sendMessage("changeok");
			}
		} else if (message.startsWith("SET_PASSWORD:")) {
			String username = message.substring(13, message.indexOf(";"));
			String password = Encrypt.encryptSHA1(message.substring(message.indexOf(";") + 1));

			Account account = Login.forceLoadAccount(username);
			if (account == null) {
				client.sendMessage("notok");
			} else {
				account.setPassword(password);
				if (account.getWorld() == null)
					LoginFilesManager.saveAccount(account);
				client.sendMessage("setok");
			}
		} else if (message.startsWith("GET_VAR:@") && message.endsWith("/email")) {
			String username = message.substring(9, message.length() - 6);
			Account account = Login.forceLoadAccount(username);
			if (account == null) {
				client.sendMessage("null");
			} else {
				client.sendMessage(account.getEmail() != null ? account.getEmail() : "null");
			}
		} else if (message.startsWith("SET_EMAIL:")) {
			String username = message.substring(10, message.indexOf(";"));
			String email = message.substring(message.indexOf(";") + 1);

			Account account = Login.forceLoadAccount(username);
			if (account == null) {
				client.sendMessage("notok");
			} else {
				account.setEmail(email.equals("null") ? null : email);
				if (account.getWorld() == null)
					LoginFilesManager.saveAccount(account);
				client.sendMessage("setok");
			}
		} else if (message.startsWith("STORE_PURCHASE:")) {
			String username = message.substring(15, message.indexOf(";"));
			String item = message.substring(message.indexOf(";") + 1);

			Account account = Login.findAccount(username);
			if (account == null || account.isLobby()) {
				client.sendMessage("coffline");
			} else {
				LoginServerChannelManager.sendReliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodeStorePurchase(username, item).trim());
				client.sendMessage("purchaseok");
			}
		} else if (message.startsWith("REDEEM_AUTH:")) {
			String username = message.substring(12, message.indexOf(";"));
			int points = Integer.parseInt(message.substring(message.indexOf(";") + 1));
			Account account = Login.findAccount(username);
			if (account == null || account.isLobby()) {
				client.sendMessage("coffline");
			} else {
				if (points >= Settings.VOTE_MIN_AMOUNT)
					account.updateLastVote();
				LoginServerChannelManager.sendReliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodeStorePurchase(username, "vote_tokens:" + points).trim());
				client.sendMessage("redeemok");
			}
		} else if (message.startsWith("GET_VAR:@") && message.endsWith("/offences")) {
			String username = message.substring(9, message.length() - 9);
			List<String> users = new ArrayList<String>();
			List<String> ips = new ArrayList<String>();
			users.add(username);
			
			StringBuilder msg = new StringBuilder();
			boolean first = true;
			Map<Integer, List<Offences.Metadata>> offences = Login.findAllOffences(users, ips);
			for (int type = 0; type < 4; type++) {
				for (Offences.Metadata offence : offences.get(type)) {
					String mod_name = Login.getDisplayName(offence.getModerator());
					if (mod_name == null)
						mod_name = offence.getUsername();
					
					if (!first)
						msg.append("<seperator_b>");
					else
						first = false;					
					
					msg.append(type);
					msg.append("<seperator_a>");
					msg.append(offence.getUid());
					msg.append("<seperator_a>");
					msg.append(offence.getTime() / 1000);
					msg.append("<seperator_a>");
					msg.append(offence.getIp());
					msg.append("<seperator_a>");
					msg.append(mod_name);
					msg.append("<seperator_a>");
					msg.append(offence.getReason());
					msg.append("<seperator_a>");
					msg.append(offence.hasExpired() ? 0 : ((offence.getExpires() - Utils.currentTimeMillis()) / 1000));
					msg.append("<seperator_a>");
					msg.append(offence.getState());
					msg.append("<seperator_a>");
					msg.append(offence.getAppeal() == null ? "null" : offence.getAppeal());
					msg.append("<seperator_a>");
					msg.append(offence.getReason() == null ? "null" : offence.getReason());
				}
			}
			
			if (msg.length() < 1)
				msg.append("null");
			
			
			client.sendMessage(msg.toString());
			
		}
	}

}
