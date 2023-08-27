/**
 * 
 */
package com.rs.game.player.content;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;

import com.everythingrs.donate.Donation;
import com.rs.cache.Cache;
import com.rs.discord.Bot;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.npc.others.Mimic;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.GodWars;
import com.rs.sql.Database;
import com.rs.utils.Logger;
import com.rs.utils.MTopVoter;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex) Sep 21, 2017
 */
public class Donations {

	public static void vote(Player player) {
		try {
			/*if (!player.hasVotedInLast24Hours())
				player.setVotesIn24h(0);
			else if (player.getVotesIn24h() >= 10)
				return;*/

			Database db = new Database("192.99.5.144", "matrixr_gs", "G9KmgRB9vATP", "matrixr_vote");
			if (!db.init()) {
				player.getPackets().sendGameMessage("Api Services are currently offline. Please check back shortly!");
				return;
			}
			PreparedStatement statement1 = db.prepare("SELECT voted_on FROM votes WHERE LOWER(username) LIKE LOWER(?) AND claimed='0'");
			statement1.setString(1, player.getUsername());
			ResultSet result = statement1.executeQuery();
			int votes = 0;
			while (result.next()) {
				long votedOn = result.getLong("voted_on");
				if (votedOn == -1)
					continue;
				votes++;
			}
			if (votes > 5) // capped at 5
				votes = 5;

			/*if (votes + player.getVotesIn24h() > 10)
				votes = 10 - player.getVotesIn24h();*/

			if (votes == 0) {
				player.getPackets().sendGameMessage("You haven't voted in any website yet.");
				db.destroyAll();
				return;
			}
			if (votes < 2) {
				player.getPackets().sendGameMessage("You need to vote in at least 2 websites in order to claim. Current votes: "+votes);
				db.destroyAll();
				return;
			}

			PreparedStatement statement3 = db.prepare("UPDATE votes SET claimed='1' WHERE LOWER(username) LIKE LOWER(?) AND claimed='0'");
			statement3.setString(1, player.getUsername());
			statement3.execute();
			db.destroyAll();
			/*String username = player.getUsername();//player.getUsername().replaceAll("_", "%20"); // opposite
			URL url = new URL("https://onyxftw.com/checkvote.php?usr=" + username);

			URLConnection con = url.openConnection();
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);

			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String results = reader.readLine();
			reader.close();

			String[] fields = results.replace("\"", "").replace("{", "").replace("}", "").split(",");
			int votes = 0;
			boolean hasLegitVotes = false;
			int voteType = 0;
			for (String f : fields) {
				String[] values = f.split(":");
				if (values[0].equalsIgnoreCase("everythingrs") || values[0].equalsIgnoreCase("runelocus")
						|| values[0].equalsIgnoreCase("topg") || values[0].equalsIgnoreCase("rspslist")
						|| values[0].equalsIgnoreCase("rsps100")) {
					int vote = Integer.parseInt(values[1]);
					if (vote > 0) {
						if (!values[0].equalsIgnoreCase("rsps100"))
							hasLegitVotes = true;
						votes += Integer.parseInt(values[1]);
						voteType++;
					}
				}
			}
			if (votes == 0 || !hasLegitVotes) {
				player.getPackets().sendGameMessage("You haven't voted in any website yet.");
				return;
			}
			if (voteType < 3 || votes < 3) {
				player.getPackets().sendGameMessage("You need to vote in at least 3 websites in order to claim. Current votes: "+votes);
				return;
			}
			if (votes > 5) // capped at 5
				votes = 5;
			if (votes + player.getVotesIn24h() > 10)
				votes = 10 - player.getVotesIn24h();*/

			// System.out.println(votes+", "+results);

			/*url = new URL("https://onyxftw.com/votesystem/803s4u1nhir6s4zq.php?user=" + username);

			con = url.openConnection();
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);

			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			results = reader.readLine();
			reader.close();*/


			player.setVoteCount(player.getVoteCount() + votes);
			MTopVoter.add(player, votes);
			player.setVotesIn24h(player.getVotesIn24h() + votes);

			int mimics = votes <= 5 ? 1 : 2;

			if (player.getControlerManager().getControler() != null) {
				player.getPackets().sendGameMessage("<col=ff8000>Your vote tickets were added to your bank!");
				player.getPackets().sendGameMessage("<col=ff8000>A mimic chest was added to your bank!");
				player.getBank().addItem(Mimic.MIMIC_CASKET, mimics, false);
				player.getBank().addItem(25459, votes, false);
			} else {
				player.getInventory().addItemDrop(25459, votes);
				player.getInventory().addItemDrop(Mimic.MIMIC_CASKET, mimics);
				player.getPackets().sendGameMessage("<col=ff8000>You received " + votes + " Vote ticket" + (votes != 1 ? "s" : "") + "!");
				player.getPackets().sendGameMessage("<col=ff8000>You received "+ mimics + " Mimic chest!" + (mimics != 1 ? "s" : "") + "!");
			}
			player.getSquealOfFortune().giveEarnedSpins(votes);
			player.refreshLastVote();
			World.sendNews(player, Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " has just voted! Thank you!", 5);
			player.getPackets().sendGameMessage("<col=ff8000>Thank you for voting!");

			Logger.globalLog(player.getUsername(), player.getSession().getIP(),
					new String(" voted " + votes + " times."));

		} catch (Throwable e) {
			e.printStackTrace();
			player.getPackets().sendGameMessage("API Services are currently offline. Please check back shortly!");
		}
	}

	public static void main(String[] args) throws SQLException, IOException {
		Cache.init();
		claim(new Player());
	}

	public static void claim(Player player) throws SQLException {
	//	player.getPackets().sendGameMessage("Looking up donations...");
		Database db = new Database("192.99.5.144", "matrixr_gs", "G9KmgRB9vATP", "matrixr_forums");
		if (!db.init()) {
			player.getPackets().sendGameMessage("Api Services are currently offline. Please check back shortly!");
			return;
		}
		PreparedStatement statement1 = db.prepare("SELECT member_id FROM core_members WHERE LOWER(name) LIKE LOWER(?) LIMIT 1");
		statement1.setString(1, player.getUsername());

		ResultSet result1 = statement1.executeQuery();
		if(!result1.next()) {
			player.getPackets().sendGameMessage("Error claiming donation. Please contact an admin.");
			return;
		}
		int member_id = result1.getInt("member_id");

		boolean donated = false;

		PreparedStatement statement2 = db.prepare("SELECT i_items FROM nexus_invoices WHERE i_member=? AND i_status='paid'");
		statement2.setInt(1, member_id);
		ResultSet result2 = statement2.executeQuery();
		while (result2.next()) {
			String[] items = result2.getString("i_items").replace("{1:{", "[{").split("\\{");
			for (int i = 1; i < items.length; i++) {
				donated = true;
				String filter = items[i].substring(items[i].indexOf("cost\":")+7);

				filter = filter.substring(0, filter.indexOf("\""));
				double cost = Double.parseDouble(filter);

				filter = items[i].substring(items[i].indexOf("itemName\":")+11);
				filter = filter.substring(0, filter.indexOf("\""));
				String name = filter;



				filter = items[i].substring(items[i].indexOf("quantity\":")+10);
				filter = filter.substring(0, filter.indexOf(","));
				int quantity = Integer.parseInt(filter);

				filter = items[i].substring(items[i].indexOf("itemID\":")+8);
				filter = filter.substring(0, filter.indexOf(","));
				int itemID = Integer.parseInt(filter);

				Donation donation = getDonation(itemID);

				if (donation == null)
					player.getPackets().sendGameMessage("Donation not found: "+name+"("+itemID+"). Please report this to an admin.");

				claim(player, donation, name, quantity, cost);
			}
		}

		if (!donated)
			player.getPackets().sendGameMessage("You currently don't have any items waiting. You must donate first!");
		else {
			PreparedStatement statement3 = db.prepare("UPDATE nexus_invoices SET i_status='clai' WHERE i_member=? AND i_status='paid'");
			statement3.setInt(1, member_id);
			statement3.execute();
			player.getPackets().sendGameMessage("Thank you for donating and supporting the server!");
		}

		db.destroyAll();
	}

	public static Donation getDonation(int productID) {
		for (Donation d : Donation.values())
			if (d.productID == productID)
				return d;
		return null;
	}
	public static enum Donation {

		DONATOR_RANK(2, new Item(25425, 1)),
		SUPER_DONATOR_RANK(4, new Item(25426, 1)),
		EXTREME_DONATOR_RANK(5, new Item(25427, 1)),
		LEGENDARY_DONATOR_RANK(6, new Item(25428, 1)),
		VIP_DONATOR_RANK(7, new Item(25429, 1)),
		NORMAL_SUPER_DONATOR_RANK_UPGRADE(8, new Item(25437, 1)),
		SUPER_EXTREME_DONATOR_RANK_UPGRADE(9, new Item(25438, 1)),
		EXTREME_LEGENDARY_DONATOR_RANK_UPGRADE(11, new Item(25439, 1)),
		LEGENDARY_VIP_DONATOR_RANK_UPGRADE(12, new Item(25440, 1)),
		SUPER_MYSTERY_BOX(3, new Item(6199, 1)),
		PREMIUM_MYSTERY_BOX(15, new Item(25436, 1)),
		GOD_MYSTERY_BOX(13, new Item(25453, 1)),
		AURA_MYSTERY_BOX(14, new Item(25763, 1)),
		CHRISTMAS_BOX(16, new Item(25553, 1)),
		LOOTERS_AMULET(17, new Item(25470, 1))
		;

		private int productID;
		private Item[] items;

		private Donation(int productID, Item... items) {
			this.productID = productID;
			this.items = items;
		}
		
		public int getProductID() {
			return productID;
		}
	}

	public static void claim(Player player, Donation donation, String name, int quantity, double cost) {
		World.sendNews(player, "<col=FFFF00>"+Utils.formatPlayerNameForDisplay(player.getDisplayName())
				+ " has donated for <shad=fffff><col=D80000>" +Utils.getFormattedNumber(quantity)+" "+name + "<col=FFFF00><shad=000>, thanks!", 4);
		double total = cost * quantity;
		player.increaseDonated(total);
		Bot.sendLog(Bot.DONATIONS_CHANNEL, "[type=DONATION][name="+player.getUsername()+"]"+"[donation="+name + " x " + quantity+", unit_price=" + cost +", total=" + total + "]");
		
		for (int i = 0; i < quantity; i++) {
			for (Item item : donation.items) {
				if (player.isUltimateIronman())
					player.getInventory().addItemDrop(item.getId(), item.getAmount());
				else
					player.getBank().addItem(item.getId(), item.getAmount(), false);
			}
		}
		player.getPackets().sendGameMessage(name + " x" + quantity + " has been added to your " + (player.isUltimateIronman() ? "inventory" : "bank."));
		Logger.globalLog(player.getUsername(), player.getSession().getIP(),
				new String(" donated for " + Utils.getFormattedNumber(quantity)+" "+name + "."));
	}

	public static void tierDonatorTicket(Player player, int id) {
		int rank = id == 25493 ? 6 : ((id - 25425) + 1);
		if (player.getDonator() >= rank) {
			player.getPackets().sendGameMessage("You already have this rank benefits.");
			return;
		}
		player.getInventory().deleteItem(id, 1);
		player.setDonator(rank);
		player.sendAccountRank();
		player.getPackets()
				.sendGameMessage("You tear the donator ticket. Please relog to make sure your rank applies.");
	}
	

	public static void tierUpgradeDonatorTicket(Player player, int id) {
		int rank = id == 25494 ? 6 : ((id - 25437) + 2);
		if (player.getDonator() >= rank) {
			player.getPackets().sendGameMessage("You already have this rank benefits.");
			return;
		}
		if (player.getDonator() != rank-1) {
			player.getPackets().sendGameMessage("You don't have the rank to use this ticket.");
			return;
		}
		player.getInventory().deleteItem(id, 1);
		player.setDonator(rank);
		player.sendAccountRank();
		player.getPackets()
				.sendGameMessage("You tear the upgrade donator ticket. Please relog to make sure your rank applies.");
	}

}
