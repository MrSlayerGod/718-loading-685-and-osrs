package com.rs.sql;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.rs.cache.Cache;
import com.rs.game.player.Player;

public class Highscores implements Runnable {

	private Player player;
	
	public Highscores(Player player) {
		this.player = player;
	}

	public static void main(String[] args) throws IOException, SQLException {
		Cache.init();
		Player player = new Player();
		//player.username = "testhi";
		player.setKillCount(10);
		player.setDeathCount(9);
		new com.rs.sql.Highscores(player).run();
	/*	Database db = new Database("34.66.96.173", "login", "t4@6ahl_5YzTuVxX", "forums");
		if (!db.init()) {
			System.err.println("uh.");
			return;
		}

		PreparedStatement statement = db.prepare("SELECT name,email,members_pass_hash FROM core_members WHERE LOWER(email) LIKE LOWER(?) LIMIT 1");
		statement.setString(1, "alex_dkk@outlook.com");
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			String name = result.getString("name").toLowerCase();
			String email = result.getString("email").toLowerCase();
			String  hash = result.getString("members_pass_hash");

			String pass = "Amflp27456";
			System.out.println(BCrypt.checkpw(pass, hash));

			System.out.println(name+", "+email+", "+hash);

			System.out.println(BCrypt.hashpw(pass, BCrypt.gensalt()));

			System.out.println(BCrypt.checkpw(pass, BCrypt.hashpw(pass, BCrypt.gensalt())));

			//Amflp27456
		}
		db.destroyAll();*/
		/*Database db = new Database("34.66.96.173", "login", "t4@6ahl_5YzTuVxX", "forums");
		if (!db.init())
			return;

		String username = "dragonkk";

		PreparedStatement statement = db.prepare("SELECT member_group_id FROM core_members WHERE LOWER(name) LIKE LOWER(?) LIMIT 1");
		statement.setString(1, username);
		ResultSet result = statement.executeQuery();
		if (!result.next()) {
			db.destroyAll();
			return;
		}
		int groupID = result.getInt("member_group_id");

		int newGroupID = 14;

		System.out.println(groupID);

		if (groupID == newGroupID) {
			db.destroyAll();
			return;
		}

		PreparedStatement stmt2 = db.prepare("UPDATE core_members SET member_group_id=? WHERE LOWER(name) LIKE LOWER(?) LIMIT 1");
		stmt2.setInt(1, newGroupID);
		stmt2.setString(2, username);
		stmt2.execute();
		db.destroyAll();*/
	}

	@Override
	public void run() {
		try {
			Database db = new Database("192.99.5.144", "matrixr_gs", "G9KmgRB9vATP", "matrixr_hiscores");

			String name = player.getUsername();
			
			if (!db.init()) {
				System.err.println("Failing to update "+name+" highscores. Database could not connect.");
				return;
			}
				
			PreparedStatement stmt = db.prepare(generateQuery());
			stmt.setString(1, name);
			stmt.setInt(2, player.getRights());
			stmt.setInt(3, player.isDeadman() ? 4 : player.isUltimateIronman() ? 3 : player.isIronman() ? 2 : 1);
			stmt.setInt(4, player.getKillCount());
			stmt.setInt(5, player.getDeathCount());
			//stmt2.setInt(3, player.isSuperFast() ? 6 : player.isFast() ? 0 : player.isNormal() ? 1 : player.isDeadman() ? 2 : player.isIronman() ? 3 : player.isUltimateIronman() ? 4 : 5);
			//stmt2.setInt(4, player.getSkills().getTotalLevel());
			stmt.setLong(6, player.getSkills().getTotalXp());
			
			for (int i = 0; i < 25; i++)
				stmt.setInt(7 + i, Math.min(200000000,(int)player.getSkills().getXp()[i]));

			//stmt2.setInt(31, player.getKillCount());
			//stmt2.setInt(32, player.getDeathCount());

			stmt.execute();
			
			db.destroyAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String generateQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("REPLACE INTO hs_users (");
		sb.append("username, ");
		sb.append("rights, ");
		sb.append("mode, ");
		sb.append("kills, ");
		sb.append("deaths, ");
		//sb.append("total_level, ");
		sb.append("overall_xp, ");
		sb.append("attack_xp, ");
		sb.append("defence_xp, ");
		sb.append("strength_xp, ");
		sb.append("constitution_xp, ");
		sb.append("ranged_xp, ");
		sb.append("prayer_xp, ");
		sb.append("magic_xp, ");
		sb.append("cooking_xp, ");
		sb.append("woodcutting_xp, ");
		sb.append("fletching_xp, ");
		sb.append("fishing_xp, ");
		sb.append("firemaking_xp, ");
		sb.append("crafting_xp, ");
		sb.append("smithing_xp, ");
		sb.append("mining_xp, ");
		sb.append("herblore_xp, ");
		sb.append("agility_xp, ");
		sb.append("thieving_xp, ");
		sb.append("slayer_xp, ");
		sb.append("farming_xp, ");
		sb.append("runecrafting_xp, ");
		sb.append("hunter_xp, ");
		sb.append("construction_xp, ");
		sb.append("summoning_xp, ");
		sb.append("dungeoneering_xp) ");
		sb.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		return sb.toString();
	}
	
}