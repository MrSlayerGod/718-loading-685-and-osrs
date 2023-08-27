package com.rs.login;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import com.rs.Settings;
import com.rs.executor.LoginExecutorManager;
import com.rs.login.account.Account;
import com.rs.net.LoginServerChannelManager;
import com.rs.net.encoders.LoginChannelsPacketEncoder;

public class ForumAuthManager {

	public static void registerAuth(final Account account, final String username, final String password) {
		if (!Settings.HOSTED || true) {
			LoginServerChannelManager.sendUnreliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodePlayerGameMessage(account.getUsername(), "Can't auth \"" + username + "\" \"" + password + "\", running in local mode.").trim());
			return;
		}

		if (account.getForumAuthId() != -1) {
			LoginServerChannelManager.sendUnreliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodePlayerGameMessage(account.getUsername(), "Your forum account is already authorised.").trim());
			return;
		}

		LoginExecutorManager.authsExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					String link = Settings.WEB_API_LINK + "?world=eco&action=regauth" + "&username=" + URLEncoder.encode(username.toString(), "UTF-8") + "&password=" + URLEncoder.encode(password.toString(), "UTF-8") + "&gameuser=" + URLEncoder.encode(account.getUsername(), "UTF-8");
					URLConnection c = new URL(link).openConnection();
					c.setConnectTimeout(5000);
					BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
					String response = reader.readLine();
					reader.close();
					if (response.startsWith("reg-ok:")) {
						String[] spl = response.split("\\:");
						int userID = Integer.parseInt(spl[1]);
						String ranks = spl[2];
						account.setForumAuthId(userID);
						account.syncRanksFromForumGroups(ranks);
						LoginServerChannelManager.sendUnreliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodePlayerGameMessage(account.getUsername(), "Forum account " + username.toString() + " has been authorized to your game account sucessfully.").trim());

					} else if (response.equals("error")) {
						throw new RuntimeException();
					} else if (response.equals("reg-alreadyauthed")) {
						LoginServerChannelManager.sendUnreliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodePlayerGameMessage(account.getUsername(), "That account is already authorised by someone else.").trim());
					} else if (response.equals("reg-wronginfo")) {
						LoginServerChannelManager.sendUnreliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodePlayerGameMessage(account.getUsername(), "You have entered incorrect username or password.").trim());
					} else {
						LoginServerChannelManager.sendUnreliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodePlayerGameMessage(account.getUsername(), "Unexpected server response.").trim());
					}
				} catch (Throwable t) {
					t.printStackTrace();
					LoginServerChannelManager.sendUnreliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodePlayerGameMessage(account.getUsername(), "An error occured, please try again later.").trim());
				}
			}
		}, 0L, TimeUnit.MILLISECONDS);
	}

	public static void syncAuth(final Account account) {
		if (!Settings.HOSTED || true)
			return;

		if (account.getForumAuthId() <= 0)
			account.setForumAuthId(-1);

		if (account.getForumAuthId() != -1) {
			LoginExecutorManager.authsExecutor.schedule(new Runnable() {
				@Override
				public void run() {
					try {
						String link = Settings.WEB_API_LINK + "?world=eco&action=auth&gameuser=" + URLEncoder.encode(account.getUsername(), "UTF-8") + "&userid=" + account.getForumAuthId();
						URLConnection c = new URL(link).openConnection();
						c.setConnectTimeout(1000);
						BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
						String response = reader.readLine();
						if (response.startsWith("ok-")) {
							account.syncRanksFromForumGroups(response.substring(3));
						} else if (response.equals("notauthed") || response.equals("gusermismatch")) {
							account.setForumAuthId(-1);
							account.syncRanksFromForumGroups(new int[] { 2 }); // standart users.
							LoginServerChannelManager.sendUnreliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodePlayerGameMessage(account.getUsername(), "Your forum account was unauthorised automatically.").trim());
						} else {
							LoginServerChannelManager.sendUnreliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodePlayerGameMessage(account.getUsername(), "An unknown response was received while trying to verify your forum account auth key.").trim());
						}
						reader.close();
					} catch (Throwable t) {
						t.printStackTrace();
						LoginServerChannelManager.sendUnreliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodePlayerGameMessage(account.getUsername(), "An error occured while trying to verify your forum account auth key.").trim());
					}
				}
			}, 5000, TimeUnit.MILLISECONDS);
		} else {
			boolean donator = account.hasRank(Account.RANK_DONATOR);
			boolean extremeDonator = account.hasRank(Account.RANK_EXTREME_DONATOR);
			account.syncRanksFromForumGroups(new int[] { 2 });
			if (donator)
				account.setRank(Account.RANK_DONATOR, true);
			if (extremeDonator)
				account.setRank(Account.RANK_EXTREME_DONATOR, true);
			LoginExecutorManager.authsExecutor.schedule(new Runnable() {
				@Override
				public void run() {
					LoginServerChannelManager.sendUnreliablePacket(account.getWorld(), LoginChannelsPacketEncoder.encodePlayerGameMessage(account.getUsername(), "You currently don't have have forum account authorised. Talk to closest oracle of dawn to authorize it.").trim());
				}
			}, 5000, TimeUnit.MILLISECONDS);

		}

	}

}
