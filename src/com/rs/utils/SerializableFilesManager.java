package com.rs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.rs.Settings;
import com.rs.game.player.content.clans.Clan;
import com.rs.game.player.content.grandExchange.ExchangeStock;
import com.rs.game.player.content.grandExchange.ItemPrice;
import com.rs.game.player.content.grandExchange.Offer;

public class SerializableFilesManager {

	private static final String CLAN_PATH = "clans/";
	private static final String GE_OFFERS = "grandExchangeOffers.ser";
	private static final String GE_PRICES = "grandExchangePrices.ser";
	private static final String GE_STOCK = "grandExchangeStock.ser";
	private static final String DT_HISCORES = "dominionTowerHiscores.ser";
	private static final String PK_HISCORES = "pkHiscores.ser";
	private static final String TOP_DONATOR = "topDonator.ser";
	private static final String TOP_COX = "topCox.ser";
	private static final String M_TOP_DONATOR = "mTopDonator.ser";
	private static final String M_RESET = "mReset.ser";
	private static final String TOP_VOTER = "topVoter.ser";
	private static final String M_TOP_VOTER = "mTopVoter.ser";
	private static final String BOSS_KILLS = "bossKills.ser";
	private static final String NERF_DROPS = "nerfDrops.ser";
	private static final String BOSS_TIMERS = "bossTimers.ser";
	private static final String TOP_DUNG = "topDung.ser";
	private static final String REFERRAL_SYSTEM = "referralSystem.ser";

//	private static MiniFS filesystem;

	private SerializableFilesManager() {
		throw new Error();
	}

	public static synchronized void init() {
		/*try {
			if (Settings.HOSTED)
				filesystem = MiniFS.open(Settings.DATA_PATH + Settings.WORLD_ID);
			else {
				if (new File(Settings.DATA_PATH + "_" + System.getProperty("user.name") + ".data").exists()) {
					filesystem = MiniFS.open(Settings.DATA_PATH + "_" + System.getProperty("user.name"));
				} else {
					Utils.copyFile(new File(Settings.DATA_PATH + "_Admin.data"), new File(Settings.DATA_PATH + "_" + System.getProperty("user.name") + ".data"));
					filesystem = MiniFS.open(Settings.DATA_PATH + "_" + System.getProperty("user.name"));
				}
			}
		} catch (Throwable t) {
			Logger.handle(t);
			throw new Error("Failed to load file system.");
		}*/
	}
	
	private synchronized static String[] listFiles(String path) {
		return new File(Settings.DATA_PATH + Settings.WORLD_ID+ "/"+ path).list();
	}
	
	private synchronized static boolean fileExists(String path) {
		return new File(Settings.DATA_PATH + Settings.WORLD_ID+ "/"+ path).exists();
	}
	
	private synchronized static boolean deleteFile(String path) {
		return new File(Settings.DATA_PATH + Settings.WORLD_ID+ "/"+ path).delete();
	}

	public static synchronized void flush() {
		/*try {
			boolean ok = filesystem.flush();
			if (!ok)
				throw new RuntimeException("Couldn't flush fs.");
		} catch (Throwable t) {
			Logger.handle(t);
		}*/
	}

	public synchronized static boolean containsClan(String name) {
		return /*filesystem.*/fileExists(CLAN_PATH + name + ".ser");
	}

	public synchronized static Clan loadClan(String name) {
		try {
			return (Clan) loadObject(CLAN_PATH + name + ".ser");
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}

	public synchronized static void saveClan(Clan clan) {
		try {
			storeObject(clan, CLAN_PATH + clan.getClanName() + ".ser");
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

	public synchronized static void deleteClan(Clan clan) {
		try {
			/*filesystem.*/deleteFile(CLAN_PATH + clan.getClanName() + ".ser");
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}

	@SuppressWarnings("unchecked")
	public static synchronized HashMap<Long, Offer> loadGEOffers() {
		if (/*filesystem.*/fileExists(GE_OFFERS)) {
			try {
				return (HashMap<Long, Offer>) loadObject(GE_OFFERS);
			} catch (Throwable t) {
				Logger.handle(t);
				return new HashMap<Long, Offer>();
			}
		} else {
			return new HashMap<Long, Offer>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized HashMap<Integer, ItemPrice> loadGEPrices() {
		if (/*filesystem.*/fileExists(GE_PRICES)) {
			try {
				return (HashMap<Integer, ItemPrice>) loadObject(GE_PRICES);
			} catch (Throwable t) {
				Logger.handle(t);
			}
		}
		return new HashMap<Integer, ItemPrice>();
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized HashMap<String, ExchangeStock> loadGEStock() {
		if (/*filesystem.*/fileExists(GE_STOCK)) {
			try {
				return (HashMap<String, ExchangeStock>) loadObject(GE_STOCK);
			} catch (Throwable t) {
				Logger.handle(t);
			}
		}
		return new HashMap<String, ExchangeStock>();
	}

	public static synchronized void saveGEOffers(HashMap<Long, Offer> offers) {
		try {
			SerializableFilesManager.storeObject(offers, GE_OFFERS);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}

	public static synchronized void saveGEPrices(HashMap<Integer, ItemPrice> prices) {
		try {
			SerializableFilesManager.storeObject(prices, GE_PRICES);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}

	public static synchronized void saveGEStock(HashMap<String, ExchangeStock> stock) {
		try {
			SerializableFilesManager.storeObject(stock, GE_STOCK);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}
	
	
	public synchronized static  DTRank[] loadDTHiscores() {
		try {
			return (DTRank[]) loadObject(DT_HISCORES);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
	
	public static synchronized void saveDTHiscores(DTRank[] ranks) {
		try {
			SerializableFilesManager.storeObject(ranks, DT_HISCORES);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}
	
	public synchronized static PkRank[] loadPKHiscores() {
		try {
			return (PkRank[]) loadObject(PK_HISCORES);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
	
	public static synchronized void savePKHiscores(PkRank[] ranks) {
		try {
			SerializableFilesManager.storeObject(ranks, PK_HISCORES);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}
	
	public synchronized static TopDonator[] loadTopDonator() {
		try {
			return (TopDonator[]) loadObject(TOP_DONATOR);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}

	public synchronized static TopCox[][][] loadTopCox() {
		try {
			return (TopCox[][][]) loadObject(TOP_COX);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
	
	public static synchronized void saveTopDonator(TopDonator[] ranks) {
		try {
			SerializableFilesManager.storeObject(ranks, TOP_DONATOR);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}

	public static synchronized void saveTopCox(TopCox[][][] ranks) {
		try {
			SerializableFilesManager.storeObject(ranks, TOP_COX);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized static Map<String, Double> loadMTopDonator() {
		try {
			return (Map<String, Double>) loadObject(M_TOP_DONATOR);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
	
	public static synchronized void saveMTopDonator(Map<String, Double> ranks) {
		try {
			SerializableFilesManager.storeObject((Serializable) ranks, M_TOP_DONATOR);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized static Map<String, Integer> loadMTopVoter() {
		try {
			return (Map<String, Integer>) loadObject(M_TOP_VOTER);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public synchronized static MonthlyReset loadMonthlyReset() {
		try {
			return (MonthlyReset) loadObject(M_RESET);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
	
	public static synchronized void saveMTopVoter(Map<String, Integer> ranks) {
		try {
			SerializableFilesManager.storeObject((Serializable) ranks, M_TOP_VOTER);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}
	
	public synchronized static TopVoter[] loadTopVoter() {
		try {
			return (TopVoter[]) loadObject(TOP_VOTER);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
	
	public static synchronized void saveTopVoter(TopVoter[] ranks) {
		try {
			SerializableFilesManager.storeObject(ranks, TOP_VOTER);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}
	
	public synchronized static TopDung[] loadTopDung() {
		try {
			return (TopDung[]) loadObject(TOP_DUNG);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
	
	public static synchronized void saveTopDung(TopDung[] ranks) {
		try {
			SerializableFilesManager.storeObject(ranks, TOP_DUNG);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}

	public static synchronized void saveMonthlyReset(MonthlyReset reset) {
		try {
			SerializableFilesManager.storeObject(reset, M_RESET);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}
	
	public synchronized static ReferralSystem loadReferralSystem() {
		try {
			return (ReferralSystem) loadObject(REFERRAL_SYSTEM);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
	
	public static synchronized void saveReferralSystem(ReferralSystem system) {
		try {
			SerializableFilesManager.storeObject(system, REFERRAL_SYSTEM);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public synchronized static Map<String, BossKillsScore> loadBossKills() {
		try {
			return (Map<String, BossKillsScore>) loadObject(BOSS_KILLS);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
	
	public static synchronized void saveNerfDrops(Map<String, Double> ranks) {
		try {
			SerializableFilesManager.storeObject((Serializable) ranks, NERF_DROPS);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized static Map<String, Double> loadNerfDrops() {
		try {
			return (Map<String, Double>) loadObject(NERF_DROPS);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
	
	public static synchronized void saveBossTimers(Map<String, BossTimerScore> ranks) {
		try {
			SerializableFilesManager.storeObject((Serializable) ranks, BOSS_TIMERS);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized static Map<String, BossTimerScore> loadBossTimers() {
		try {
			return (Map<String, BossTimerScore>) loadObject(BOSS_TIMERS);
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
	
	public static synchronized void saveBossKills(Map<String, BossKillsScore> ranks) {
		try {
			SerializableFilesManager.storeObject((Serializable) ranks, BOSS_KILLS);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}
	
	


	public static synchronized Object loadObject(String f) throws IOException, ClassNotFoundException {
		if (!fileExists(f))
			return null;
	/*	byte[] data = filesystem.getFile(f);
		if (data == null)
			return null;*/
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(Settings.DATA_PATH + Settings.WORLD_ID + "/"+ f));
		Object object = in.readObject();
		in.close();
		return object;
	}

	public static synchronized void storeObject(Serializable o, String f) throws IOException {
		//ByteArrayOutputStream baos = new ByteArrayOutputStream();
		File file = new File(Settings.DATA_PATH + Settings.WORLD_ID + "/");
		if(!file.exists()) {
			file.mkdirs();
		}
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Settings.DATA_PATH + Settings.WORLD_ID + "/"+ f));
		out.writeObject(o);
		out.flush();
		/*boolean ok = filesystem.putFile(f, baos.toByteArray());
		if (!ok)
			throw new RuntimeException("Couldn't put file");*/
		out.close();
	}
}
