package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.executor.GameExecutorManager;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.Dungeon;
import com.rs.game.player.content.dungeoneering.DungeonManager;

public class Highscores {

	public static void updatePlayer(final Player player) {
		if (!Settings.HOSTED || Settings.WORLD_ID != 1 || player.getRights() == 2 || player.isDungeoneer() || player.isYoutuber() || player.getSkills().getTemporaryXP() != null)
			return;
		GameExecutorManager.slowExecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					/*int[] skills = new int[25];
					for (int i = 0; i < skills.length; i++)
						skills[i] = Math.min(200000000, (int) player.getSkills().getXp()[i]);
					com.everythingrs.hiscores.Hiscores.update(Settings.EVERYTHING_RS_SECRET_KEY,  player.isExtreme() ? "Extreme Mode" : player.isFast() ? "Easy Mode": player.isIronman() ? "Ironman Mode" : "Normal Mode", player.getUsername().replace("_", " "), player.getRights(), skills, false);
				*/
					new com.rs.sql.Highscores(player).run();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});	
		/*GameExecutorManager.slowExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					updateSkill(player);
				} catch (Throwable e) {
					e.printStackTrace();
					//failed to update highscore, w/e, updaten ex time u logout then
				}
			}
		});*/
	}

	private static void updateSkill(Player player) throws Throwable {
		if (Settings.WORLD_ID != 1)
			return;
		/*String url = Settings.HIGHSCORES_API_LINK + "?xpmode=" + player.getXpRateMode();
		url += "&username=" + URLEncoder.encode(player.getUsername(), "UTF-8");
		url += "&displayname=" + URLEncoder.encode(player.getDisplayName(), "UTF-8");
		for (int i = 0; i < Skills.SKILL_NAME.length; i++) {
			url += "&s" + i + "=" + (i + 1);
			url += "&l" + i + "=" + (player.getRights() >= 2 ? 1 : player.getSkills().getLevelForXp(i));
			url += "&x" + i + "=" + (player.getRights() >= 2 ? 0 : (int) player.getSkills().getXp(i));
		}
		url += "&s" + Skills.SKILL_NAME.length + "=0";
		url += "&l" + Skills.SKILL_NAME.length + "=" + (player.getRights() >= 2 ? 1 : player.getSkills().getTotalLevel());
		url += "&x" + Skills.SKILL_NAME.length + "=" + (player.getRights() >= 2 ? 0 : (long) player.getSkills().getTotalXp());
		URLConnection c = new URL(url).openConnection();
		c.setConnectTimeout(5000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
		reader.close();*/
	}
	
	
	public static void updateDung(final DungeonManager dung, final Dungeon dungeon, final long time, final long date, final int gameuid, final int teamsize, final int type, final long seed, Player player) {
		if (!Settings.HOSTED)
			return;
		/*
		final DungeonPartyManager party = dung.getParty();
		if (party.getDificulty() != teamsize || party.getComplexity() != 6)
			return; // not eglible
		
		if (party.getSize() != DungeonConstants.LARGE_DUNGEON)
			return; // not eglible
		

		int floor = party.getFloor();
		
		
		long pentime = time;
		long realtime = time;
		
		int percent = 0;
		if (dung.getVisibleBonusRoomsCount() > 0)
			percent = (int)Math.floor(13d * ((double)dung.getVisibleBonusRoomsCount() / (double)(dungeon.getRoomsCount() - dungeon.getCritCount())));
		
		if (teamsize < 3)
			pentime += (13-percent)*1000l*120l;
		else
			pentime += (13-percent)*1000l*60l;
		
		if (party.isKeyShare()) {
			switch (party.getTeam().size()) {
				default:
				case 5:
					pentime *= 1.35d;
					break;
				case 4:
					pentime *= 1.4d;
					break;
				case 3:
					pentime *= 1.45d;
					break;
				case 2:
					pentime *= 1.55d;
					break;
				case 1:
					pentime *= 1.0d;
					break;
			}
		}
		
		
		if (party.isGuideMode())
			pentime *= 1.1d;
		
		
		final long pentime_ = pentime;
		final long realtime_ = realtime;
		
		GameExecutorManager.slowExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					String url = Settings.HIGHSCORES_API_LINK + "?dung=true";
					url += "&username=" + URLEncoder.encode(player.getUsername(), "UTF-8");
					url += "&displayname=" + URLEncoder.encode(player.getDisplayName(), "UTF-8");
					url += "&diffc=" + teamsize;
					url += "&floor=" + floor;
					url += "&time=" + (pentime_/1000);
					url += "&time_original=" + (realtime_/1000);
					url += "&date=" + (date/1000);
					url += "&gameuid=" + gameuid;
					url += "&type=" + type;
					url += "&seed=" + seed;
					
					URLConnection c = new URL(url).openConnection();
					c.setConnectTimeout(5000);
					BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
					reader.close();
					
				} catch (Throwable e) {
					e.printStackTrace();
					//failed to update highscore, w/e, updaten ex time u logout then
				}
			}
		});
		*/
		
	}

}
