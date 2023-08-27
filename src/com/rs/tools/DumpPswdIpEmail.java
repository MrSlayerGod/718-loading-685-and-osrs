package com.rs.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.rs.Settings;
import com.rs.login.DisplayNames;
import com.rs.login.Offences;
import com.rs.login.account.Account;
import com.rs.utils.Logger;
import com.rs.utils.LoginFilesManager;

public class DumpPswdIpEmail {

	public static void main(String[] args) throws Throwable {
		Settings.HOSTED = args.length < 1 ? true : Boolean.parseBoolean(args[0]);
		Logger.log("Launcher", "Initing File System...");
		LoginFilesManager.init();
		DisplayNames names = LoginFilesManager.loadDisplayNames();
		names.initReverseMapping();
		Offences offences = LoginFilesManager.loadOffences();
		BufferedWriter writer = new BufferedWriter(new FileWriter("dump_data.txt"));
		for (String acc : LoginFilesManager.getAllAccounts()) {
			try {
				Account account = (Account) LoginFilesManager.loadAccount(acc.substring("accounts/".length(), acc.length() - ".acc".length()));
				if (account == null)
					continue;

				writer.write("Start Information of [" + account.getUsername() + "]\r\n");
				writer.write("Created on:" + formatDate(account.getCreationDate()) + "\r\n");
				writer.write("Last vote bonus:" + formatDate(account.getLastVote()) + "\r\n");
				writer.write("Forum user id:" + account.getForumAuthId() + "\r\n");
				writer.write("Display name:" + names.getDisplayName(account.getUsername()) + "\r\n");
				writer.write("Previous display name:" + names.getPreviousDisplayName(account.getUsername()) + "\r\n");
				writer.write("Email:" + account.getEmail() + "\r\n");
				writer.write("Current password:" + account.getPassword() + "\r\n");
				for (String pswd : account.getPasswordList())
					writer.write("Used password(s):" + pswd + "\r\n");
				writer.write("Last ip:" + account.getLastIp() + "\r\n");
				for (String ip : account.getIpList())
					writer.write("Logged ip(s):" + ip + "\r\n");
				List<String> usernames = new ArrayList<String>();
				usernames.add(account.getUsername());
				Map<Integer, List<Offences.Metadata>> data = offences.findAllOffences(usernames, account.getIpList());
				for (Integer type : data.keySet()) {
					String tstring = offType[type.intValue()];
					List<Offences.Metadata> allOff = data.get(type);
					for (Offences.Metadata offence : allOff) {
						if (offence.getUsername().equals(account.getUsername())) {
							// direct offence
							writer.write("Direct " + tstring + " " + offenceString(offence) + "\r\n");
						} else {
							// indirect offence
							writer.write("Indirect " + tstring + " " + offenceString(offence) + "\r\n");
						}
					}
				}

				writer.write("End Information of [" + account.getUsername() + "]\r\n\r\n\r\n");
				writer.flush();

			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		writer.close();
		System.exit(0);
	}

	public static String[] offType = new String[]
	{ "Ip ban", "Ip mute", "Ban", "Mute" };
	public static Calendar calendar = Calendar.getInstance();

	public static String formatDate(long date) {
		calendar.setTimeInMillis(date);
		return calendar.get(Calendar.YEAR) + " " + (calendar.get(Calendar.MONTH) + 1) + " " + calendar.get(Calendar.DAY_OF_MONTH) + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
	}

	public static String offenceString(Offences.Metadata offence) {
		return "[user=" + offence.getUsername() + ",ip=" + offence.getIp() + ",mod=" + offence.getModerator() + ",reason=" + offence.getReason() + ",added=" + formatDate(offence.getTime()) + ",expires=" + formatDate(offence.getExpires()) + "]";
	}

}
