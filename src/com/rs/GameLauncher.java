package com.rs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.alex.store.Index;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemConfig;
import com.rs.cache.loaders.NPCConfig;
import com.rs.cache.loaders.ObjectConfig;
import com.rs.discord.Bot;
import com.rs.executor.GameExecutorManager;
import com.rs.executor.PlayerHandlerThread;
import com.rs.game.World;
import com.rs.game.map.MapBuilder;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.npc.Drops;
import com.rs.game.npc.combat.CombatScriptsHandler;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.FishingSpotsHandler;
import com.rs.game.player.content.FriendsChat;
import com.rs.game.player.content.NPCKillLog;
import com.rs.game.player.content.SacrificeAltar;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.controllers.ControllerHandler;
import com.rs.game.player.cutscenes.CutscenesHandler;
import com.rs.game.player.dialogues.DialogueHandler;
import com.rs.net.GameChannelsManager;
import com.rs.net.LoginClientChannelManager;
import com.rs.utils.*;
import com.rs.utils.huffman.Huffman;

public class GameLauncher {

	/**
	 * Whether shutdown has been started
	 */
	public static volatile boolean shutdown;
	/**
	 * Time when delayed shutdown started.
	 */
	public static volatile long delayedShutdownStart;
	/**
	 * Delay in seconds when delayed shutdown will start.
	 */
	public static volatile int delayedShutdownDelay;

	public static void main(String[] args) throws Exception {
		skipSSLValidation();
		if (args.length < 4) {
			System.out.println("USE: worldid(int) debug(boolean) hosted(boolean) pvpworld(boolean)");
			return;
		}
		Settings.WORLD_ID = Integer.parseInt(args[0]);
		Settings.DEBUG = Boolean.parseBoolean(args[1]);
		Settings.HOSTED = Boolean.parseBoolean(args[2]);
		Settings.SPAWN_WORLD = Boolean.parseBoolean(args[3]);
		if (!Settings.HOSTED) {
			Settings.LOGIN_SERVER_ADDRESS_BASE = new InetSocketAddress("127.0.0.1", 37777);
			Settings.LOGIN_CLIENT_ADDRESS_BASE = new InetSocketAddress("127.0.0.1", 37778);

			new Thread(() -> { //keep login server together for local host to save time on restarts
				try {
					LoginLauncher.main(new String[] {Boolean.toString(Settings.DEBUG), Boolean.toString(Settings.HOSTED)});
				} catch (Throwable e) {
					e.printStackTrace();
					System.exit(0);
				}

			}).start();
		}
		long currentTime = Utils.currentTimeMillis();
		Logger.log("Launcher", "Initing File System...");
		SerializableFilesManager.init();
		Logger.log("Launcher", "Initing Cache...");
		Cache.init();
		Huffman.init();
		Logger.log("Launcher", "Initing Data Files...");
		Censor.init();
		MapArchiveKeys.init();
		MapAreas.init();
		ObjectSpawns.init();
		ObjectExamines.init();
		NPCSpawns.init();
		NPCCombatDefinitionsL.init();
		NPCBonuses.init();
		NPCDrops.init();
		NPCExamines.init();
		ItemExamines.init();
		ItemWeights.init();
		ItemDestroys.init();
		ItemSpawns.init();
		MusicHints.init();
		ShopsHandler.init();
		GrandExchange.init();
		ChambersOfXeric.loadOsrsBlacklist();
		DTRank.init();
		PkRank.init();
		TopDonator.init();
		TopCox.init();
		MTopDonator.init();
		TopVoter.init();
		MTopVoter.init();
		ReferralSystem.init();
		Drops.init();
		TopDung.init();
		BossKillsScore.init();
		BossTimerScore.init();
		SacrificeAltar.init();
		Logger.log("Launcher", "Initing Controlers...");
		ControllerHandler.init();
		Logger.log("Launcher", "Initing Combat Data...");
		AmmunitionDefinitionsLoader.loadDefinitions();
		WeaponTypesLoader.loadDefinitions();
		Logger.log("Launcher", "Initing Fishing Spots...");
		FishingSpotsHandler.init();
		Logger.log("Launcher", "Initing NPC Combat Scripts...");
		CombatScriptsHandler.init();
		Logger.log("Launcher", "Initing Dialogues...");
		DialogueHandler.init();
		Logger.log("Launcher", "Initing Cutscenes...");
		CutscenesHandler.init();
		Logger.log("Launcher", "Initing Friend Chats...");
		FriendsChat.init();
		Logger.log("Launcher", "Initing Clans Manager...");
		ClansManager.init();
		Logger.log("Launcher", "Initing Executor Manager...");
		GameExecutorManager.init();
		Logger.log("Launcher", "Initing Boss Instances...");
		BossInstanceHandler.init();
		Logger.log("Launcher", "Initing World...");
		World.init();
		Logger.log("Launcher", "Initing Region Builder...");
		MapBuilder.init();
		Logger.log("Launcher", "Initing Discord bot...");
		Bot.init(false);
		Logger.log("Launcher", "Initing Game Channels Manager...");
		try {
			GameChannelsManager.init();
		} catch (Throwable e) {
			Logger.handle(e);
			Logger.log("Launcher", "Failed initing Game Channels Manager. Shutting down...");
			System.exit(1);
			return;
		}
		Logger.log("Launcher", "Initing Login Client Channel Manager...");
		try {
			LoginClientChannelManager.init();
		} catch (Throwable e) {
			Logger.handle(e);
			Logger.log("Launcher", "Failed initing Login Client Manager. Shutting down...");
			System.exit(1);
			return;
		}
		Logger.log("Launcher", "Game Server took " + (Utils.currentTimeMillis() - currentTime) + " milli seconds to launch.");
		addAutoSavingTask();
		addCleanMemoryTask();
		addRecalculatePricesTask();
		MonthlyReset.init();

		Thread console = new Thread("console thread") {
			@Override
			public void run() {
				Scanner scanner = new Scanner(System.in);
				while (!shutdown) {
					try {
						String line = scanner.nextLine();
						if (line.startsWith("logreq ")) {
							String[] spl = line.substring(7).split("\\s\\|\\=\\|\\s");
							System.err.println("Requesting " + spl[1] + " from " + spl[0]);
							Player player = World.getPlayerByDisplayNameAll(spl[0]);
							if (player != null) {
								player.getPackets().sendLogReq(spl[1]);
								System.err.println("Sent!");
							} else {
								System.err.println("Player not found!");
							}
						} else {
							System.err.println("Unknown cmd");
						}

					} catch (Throwable t) {
						Logger.handle(t);
					}
				}
				scanner.close();
			}
		};
		console.setDaemon(true);
		console.start();
		
		while (!shutdown) {
			try {
				Thread.sleep(1000);
			} catch (Throwable t) {
			}
		}

		processShutdown();

	}
	
	public static void skipSSLValidation() throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
 
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
 
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

	public static boolean initDelayedShutdown(int delay) {
		if (shutdown || delayedShutdownStart != 0)
			return false;

		delayedShutdownStart = Utils.currentTimeMillis();
		delayedShutdownDelay = delay;
		for (Player player : World.getPlayers()) {
			if (player == null || !player.hasStarted() || player.hasFinished())
				continue;
			player.getPackets().sendSystemUpdate(delay, false);
		}
		for (Player player : World.getLobbyPlayers()) {
			if (player == null || !player.hasStarted() || player.hasFinished())
				continue;
			player.getPackets().sendSystemUpdate(delay, true);
		}
		GameExecutorManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				initShutdown();
			}
		}, delay, TimeUnit.SECONDS);

		return true;
	}

	public static boolean initShutdown() {
		if (shutdown)
			return false;

		shutdown = true;
		return true;
	}

	private static void processShutdown() {
		Logger.log("Launcher", "Shutdown has been started!");

		Logger.log("Launcher", "Shutting down game network channels...");
		GameChannelsManager.shutdown();

		for (int cycle = 0;; cycle++) {
			Logger.log("Launcher", "Logging out players... Cycle #" + cycle);
			if (World.getPlayers().size() == 0 && World.getLobbyPlayers().size() == 0)
				break;
			for (Player player : World.getPlayers()) {
				player.disconnect(true, false);
				player.finish();
			}
			for (Player player : World.getLobbyPlayers()) {
				player.disconnect(true, false);
				player.finish();
			}
			Logger.log("Launcher", "Logging out players: " + (World.getPlayers().size() + World.getLobbyPlayers().size()) + ".");
			try {
				Thread.sleep(2000);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		Logger.log("Launcher", "Awaiting world shutdown...");
		GameExecutorManager.shutdown(true);

		Logger.log("Launcher", "Awaiting for packets to arrive to login server...");
		LoginClientChannelManager.awaitQueue();

		Logger.log("Launcher", "Shutting down login network channels...");
		LoginClientChannelManager.shutdown();

		Logger.log("Launcher", "Saving files...");
		saveFiles();

		Logger.log("Launcher", "Done...");

	}

	private static void addCleanMemoryTask() {
		GameExecutorManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					cleanMemory(Runtime.getRuntime().freeMemory() < Settings.MIN_FREE_MEM_ALLOWED);
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 10, TimeUnit.MINUTES);
	}

	private static void addAutoSavingTask() {
		GameExecutorManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					savePlayers();
					saveFiles();
				} catch (Throwable e) {
					Logger.handle(e);
				}

			}
		}, 5, 5, TimeUnit.MINUTES);
	}

	private static void addRecalculatePricesTask() { //change to 24h
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		int minutes = (int) ((c.getTimeInMillis() - Utils.currentTimeMillis()) / 1000 / 60);
		/*int halfDay = 12 * 60;
		if (minutes > halfDay)
			minutes -= halfDay;*/
		GameExecutorManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					World.setSkillOfTheDay();
					World.setBossOfTheDay();
					GrandExchange.recalcPrices();
					for (Player player : World.getPlayers())
						player.getDeals().reset();
					for (Player player : World.getPlayers()) {
				    	player.getPackets().sendGameMessage("Skill of the day: <col=cc33ff>"+Skills.SKILL_NAME[World.getSkillOfTheDay()]+"</col> | Boss of the day: <col=cc33ff>"+ NPCKillLog.BOSS_NAMES[World.getBossOfTheDay()]);
				    	player.getPackets().sendGameMessage("Deal of day has been updated!");
						player.getPackets().sendGameMessage("Grand exchange prices have been updated!");
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}

			}
		}, minutes+1, 24 * 60, TimeUnit.MINUTES);
	}

	public static void savePlayers() {
		for (Player player : World.getPlayers()) {
			if (player == null || World.BOTS.contains(player))
				continue;
			byte[] data = SerializationUtilities.tryStoreObject(player);
			if (data == null || data.length <= 0)
				continue;
			PlayerHandlerThread.addSave(player.getUsername(), data);
		}
		for (Player player : World.getLobbyPlayers()) {
			if (player == null)
				continue;
			byte[] data = SerializationUtilities.tryStoreObject(player);
			if (data == null || data.length <= 0)
				continue;
			PlayerHandlerThread.addSave(player.getUsername(), data);
		}
	}

	private static void saveFiles() {
		GrandExchange.save();
		DTRank.save();
		PkRank.save();
		TopDonator.save();
		TopCox.save();
		MTopDonator.save();
		TopVoter.save();
		MTopVoter.save();
		ReferralSystem.save();
		Drops.save();
		TopDung.save();
		BossKillsScore.save();
		BossTimerScore.save();
		MonthlyReset.save();
		SerializableFilesManager.flush();
	}

	public static void cleanMemory(boolean force) throws IOException {
		if (force) {
			ItemConfig.clearItemsDefinitions();
			NPCConfig.clearNPCDefinitions();
			ObjectConfig.clearObjectDefinitions();
			/*    skip:for (Region region : World.getRegions().values()) {
				for(int regionId : MapBuilder.FORCE_LOAD_REGIONS)
				    if(regionId == region.getRegionId())
					continue skip;
				region.unloadMap();
			    }*/
		}
		for (Index index : Cache.STORE.getIndexes())
			if(index != null) {
				index.resetCachedFiles();
				index.getMainFile().resetCachedArchives();
			}
		GameExecutorManager.fastExecutor.purge();
		System.gc();
	}

}
