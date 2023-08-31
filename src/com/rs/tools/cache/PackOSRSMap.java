/**
 * 
 */
package com.rs.tools.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import com.alex.store.Store;
import com.alex.utils.Constants;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemConfig;

/**
 * @author dragonkk(Alex)
 * Sep 5, 2017
 */
public class PackOSRSMap {

	//https://archive.runestats.com/osrs/ xteas for new revisions link

	private final static HashMap<Integer, int[]> keys = new HashMap<Integer, int[]>();
	
	public static final void loadUnpackedKeys() {
		try {
			File unpacked = new File("data/map/keys/keysOSRS/");
			File[] xteasFiles = unpacked.listFiles();
			for (File region : xteasFiles) {
				String name = region.getName();
				if (!name.contains(".txt")) {
					continue;
				}
				int regionId = Short.parseShort(name.replace(".txt", ""));
				if (regionId <= 0) {
					continue;
				}
				BufferedReader in = new BufferedReader(new FileReader(region));
				final int[] xteas = new int[4];
				for (int index = 0; index < 4; index++) 
					xteas[index] = Integer.parseInt(in.readLine());
				keys.put(regionId, xteas);
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//m50_161, 12961. -1
	
	private static boolean packMap(int fromMapID, int toMapID, boolean check) {
		int fromMapX = (fromMapID >> 8) ;
		int fromMapY = (fromMapID & 0xff);
		int toMapX = (toMapID >> 8) ;
		int toMapY = (toMapID & 0xff);
		
		
		int mapArchiveID = osrsData.getIndexes()[5].getArchiveId("m"+fromMapX+"_"+fromMapY);
		int landscapeArchiveID = osrsData.getIndexes()[5].getArchiveId("l"+fromMapX+"_"+fromMapY);
		if (mapArchiveID == -1 || landscapeArchiveID == -1) {
		//	System.out.println("Failed to pack "+fromMapID);
		}
		int umapArchiveID = osrsData.getIndexes()[5].getArchiveId("um"+fromMapX+"_"+fromMapY);
		int ulandscapeArchiveID = osrsData.getIndexes()[5].getArchiveId("ul"+fromMapX+"_"+fromMapY);

		int toMapArchiveID = Cache.STORE.getIndexes()[5].getArchiveId("m"+toMapX+"_"+toMapY);
		int toLandscapeArchiveID = Cache.STORE.getIndexes()[5].getArchiveId("l"+toMapX+"_"+toMapY);
		if ((toMapArchiveID != -1 || toLandscapeArchiveID != -1) && check) {
		//	System.out.println("Already exists.");
			return false;
		}
		int utoMapArchiveID = Cache.STORE.getIndexes()[5].getArchiveId("um"+toMapX+"_"+toMapY);
		int utoLandscapeArchiveID = Cache.STORE.getIndexes()[5].getArchiveId("ul"+toMapX+"_"+toMapY);

		
		
		byte[] mapSettingsData = osrsData.getIndexes()[5].getFile(mapArchiveID);
		byte[] objectsData = osrsData.getIndexes()[5].getFile(landscapeArchiveID, 0, keys.get(fromMapID));
		
		byte[] umapSettingsData = osrsData.getIndexes()[5].getFile(umapArchiveID);
		byte[] uobjectsData = osrsData.getIndexes()[5].getFile(ulandscapeArchiveID, 0, keys.get(fromMapID));
		
		if (mapSettingsData == null || objectsData == null) {
		//	System.out.println("OSRS map "+fromMapID+" data is null.");
			return false;
		}
		if (toMapArchiveID == -1)
			toMapArchiveID = Cache.STORE.getIndexes()[5].getLastArchiveId()+1;
		if (toLandscapeArchiveID == -1)
			toLandscapeArchiveID = Cache.STORE.getIndexes()[5].getLastArchiveId()+2;
		if (utoMapArchiveID == -1)
			utoMapArchiveID = Cache.STORE.getIndexes()[5].getLastArchiveId()+3;
		if (utoLandscapeArchiveID == -1)
			utoLandscapeArchiveID = Cache.STORE.getIndexes()[5].getLastArchiveId()+4;
	
		
		Cache.STORE.getIndexes()[5].putFile(toMapArchiveID, 0, Constants.GZIP_COMPRESSION, mapSettingsData, null, false, false, ("m"+toMapX+"_"+toMapY).hashCode(), -1);
		Cache.STORE.getIndexes()[5].putFile(toLandscapeArchiveID, 0, Constants.GZIP_COMPRESSION, objectsData, null, false, false, ("l"+toMapX+"_"+toMapY).hashCode(), -1);
	
		if (umapSettingsData != null)
			Cache.STORE.getIndexes()[5].putFile(utoMapArchiveID, 0, Constants.GZIP_COMPRESSION, umapSettingsData, null, false, false, ("um"+toMapX+"_"+toMapY).hashCode(), -1);
		if (uobjectsData != null)
			Cache.STORE.getIndexes()[5].putFile(utoLandscapeArchiveID, 0, Constants.GZIP_COMPRESSION, uobjectsData, null, false, false, ("ul"+toMapX+"_"+toMapY).hashCode(), -1);
		if (umapSettingsData != null)
			System.out.println("pack ug");
		//System.out.println("Packed map "+fromMapID);
		System.out.print(", "+toMapID);
		return true;
	}
	
	static Store osrsData;

	public static void main2(String[] args) throws IOException {
		//13139, 13395
		int fromMapID = 13395;
		 	int rx = (fromMapID >> 8) ;
			int ry = (fromMapID & 0xff);
		System.out.println(rx * 64 + 32);
		System.out.println(ry * 64 + 32);
	}
	
	public static int[] OSRS_MAP_IDS = {4665, 4666, 16209, 4667, 4921, 4922, 4923, 4924, 5021, 5022, 5023, 5177, 5178, 5179, 5180, 5277, 5278, 5279, 5280, 5433, 5434, 5435, 5534, 5535, 5536, 10842,
 14386, 14642, 23956, 23957, 23958, 23959, 23955, 23954, 23953, 23952, 23951, 23950, 23949, 4763, 7822, 8078, 8268, 12612, 12613, 13122, 13125, 13379, 14999, 15000, 23948, 14649, 14395, 14394, 9291, 14398, 14142, 9023, 9771, 9515, 9259, 6043, 6220, 6223, 6552, 6553, 6742, 6808, 6809, 6814, 6815, 7070, 7071, 7249, 7326, 7327, 7563, 7564, 7565, 7819, 7820, 7821, 8075, 8076, 8077, 8331, 8332, 8333, 9123, 11408, 12701, 12702, 12703, 12959, 14242, 14243, 13139, 13395, 14650, 14651, 14652, 14906, 14907, 14908, 15162, 15163, 15164, 14932, 15188, 11851, 12106, 9008, 8495, 8496, 8751, 9007, 12958, 12961, 9042, 4662, 4663, 4664, 4883, 4918, 4919, 4920, 5139, 5140, 5174, 5175, 5176, 5275, 5395, 5430, 5431, 5432, 5437, 5684, 5685, 5686, 5687, 5688, 5689, 5690, 5691, 5692, 5693, 5789, 5940, 5941, 5942, 5943, 5944, 5945, 5946, 5947, 5948, 5949, 5950, 6196, 6197, 6198, 6199, 6200, 6201, 6202, 6203, 6204, 6205, 6206, 6207, 6298, 6300, 6301, 6303, 6452, 6453, 6454, 6455, 6456, 6457, 6458, 6459, 6460, 6461, 6462, 6463, 6474, 6477, 6555, 6556, 6557, 6708, 6709, 6710, 6711, 6712, 6713, 6714, 6715, 6716, 6717, 6718, 6719, 6729, 6730, 6810, 6811, 6812, 6813, 6964, 6965, 6966, 6967, 6968, 6969, 6970, 6971, 6972, 6973, 6974, 6987, 7067, 7068, 7069, 7220, 7221, 7222, 7223, 7224, 7225, 7226, 7227, 7228, 7229, 7242, 7476, 7477, 7478, 7479, 7480, 7481, 7482, 7483, 7484, 7485, 7514, 7733, 7734, 7735, 7736, 7737, 7738, 7766, 7767, 7770, 7995, 7996, 7997, 7998, 8023, 8494, 8747, 8748, 8750, 8789, 9003, 9004, 9006, 9103, 9112, 9116, 9358, 9359, 9360, 9363, 9614, 9615, 9618, 9619, 9807, 9869, 9870, 9871, 9872, 10063, 10125, 10126, 10127, 10128, 10382, 10383, 10384, 10581, 10582, 10837, 11159, 11590, 11661, 11662, 11663, 11846, 11847, 11850, 11864, 12120, 12362, 12363, 12375, 12376, 12448, 12622, 13134, 13136, 13137, 13204, 13390, 13391, 13392, 13394, 13396, 13644, 13646, 13658, 13659, 13900, 13915, 14154, 14155, 14156, 14495, 14496, 14681, 14937, 15007, 15008, 15009, 15262, 15263, 15264};
	public static int[] ZEAH_MAP_IDS = {4665, 4666, 4667, 4921, 4922, 4923, 4924, 5177, 5178, 5179, 5180, 5433, 5434, 5435, 14649, 14395, 14394, 14398, 14142, 9023, 9771, 9515, 9259,  14650, 14651, 14652, 14906, 14907, 14908, 15162, 15163, 15164, 4662, 4663, 4664, 4918, 4919, 4920, 5174, 5175, 5176, 5430, 5431, 5432, 5437, 5684, 5685, 5686, 5687, 5688, 5689, 5690, 5691, 5692, 5693, 5940, 5941, 5942, 5943, 5944, 5945, 5946, 5947, 5948, 5949, 5950, 6196, 6197, 6198, 6199, 6200, 6201, 6202, 6203, 6204, 6205, 6206, 6207, 6452, 6453, 6454, 6455, 6456, 6457, 6458, 6459, 6460, 6461, 6462, 6463, 6708, 6709, 6710, 6711, 6712, 6713, 6714, 6715, 6716, 6717, 6718, 6719, 6964, 6965, 6966, 6967, 6968, 6969, 6970, 6971, 6972, 6973, 6974, 7220, 7221, 7222, 7223, 7224, 7225, 7226, 7227, 7228, 7229, 7476, 7477, 7478, 7479, 7480, 7481, 7482, 7483, 7484, 7485, 7733, 7734, 7735, 7736, 7737, 7738, 7995, 7996, 7997, 7998, 8494, 8495, 8496, 8747, 8748, 8750, 8751, 9003, 9004, 9006, 9007, 9008};

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main66(String[] args) throws IOException {
		for (int i = 0; i < 65535; i++) {
			int x = (i >> 8) ;
			int y = (i & 0xff);
			if (x >= 148/8 && x <= 247/8 && y >= 424/8 && y <= 501/8
					&& isOSRSMap(i) && !isZeahMap(i))
				System.out.print(i+", ");
		}
	}
	
	
	public static boolean isOSRSMap(int id) {
		for (int map : OSRS_MAP_IDS)
			if (id == map)
				return true;
		return false;
	}

	
	public static boolean isZeahMap(int id) {
		for (int map : ZEAH_MAP_IDS)
			if (id == map)
				return true;
		return false;
	}
	
	
	/**
	 * 
	 * IF THE MAP IS OSRS OBJECTS ALL ADD TO Settings.OSRS_MAP_IDS the toMapID on both client & server Settings.java
	 * 
	 * 
	 * @param toMapID - ::coords, regionid (large number that tells the 64x64 chunk)
	 * @param mapSettingsID - the file with terrain settings or & envrioment, add to folder customMaps
	 * @param objectsFileID - the file with object positions, add to folder customMaps
	 * @param check - if override existing map if theres one already in these coords. false to prevent mistakes
	 * @return
	 * @throws IOException
	 */
	private static boolean packMapCustom(int toMapID, int mapSettingsID, int objectsFileID, boolean check) throws IOException {
		int toMapX = (toMapID >> 8) ;
		int toMapY = (toMapID & 0xff);


		int toMapArchiveID = Cache.STORE.getIndexes()[5].getArchiveId("m"+toMapX+"_"+toMapY);
		int toLandscapeArchiveID = Cache.STORE.getIndexes()[5].getArchiveId("l"+toMapX+"_"+toMapY);
		if ((toMapArchiveID != -1 || toLandscapeArchiveID != -1) && check) {
			System.out.println("Already exists.");
			return false;
		}



		byte[] mapSettingsData = Files.readAllBytes(new File("customMaps/"+mapSettingsID+".dat").toPath());/// osrsData.getIndexes()[5].getFile(mapArchiveID);
		byte[] objectsData = Files.readAllBytes(new File("customMaps/"+objectsFileID+".dat").toPath());


		if (mapSettingsData == null || objectsData == null) {
		//	System.out.println("OSRS map "+fromMapID+" data is null.");
			return false;
		}
		if (toMapArchiveID == -1)
			toMapArchiveID = Cache.STORE.getIndexes()[5].getLastArchiveId()+1;
		if (toLandscapeArchiveID == -1)
			toLandscapeArchiveID = Cache.STORE.getIndexes()[5].getLastArchiveId()+2;


		Cache.STORE.getIndexes()[5].putFile(toMapArchiveID, 0, Constants.GZIP_COMPRESSION, mapSettingsData, null, false, false, ("m"+toMapX+"_"+toMapY).hashCode(), -1);
		Cache.STORE.getIndexes()[5].putFile(toLandscapeArchiveID, 0, Constants.GZIP_COMPRESSION, objectsData, null, false, false, ("l"+toMapX+"_"+toMapY).hashCode(), -1);

		//System.out.println("Packed map "+fromMapID);
		System.out.print(", "+toMapID);
		return true;
	}
	
	
	/**
	 * PACK CUSTOM MAPS HERE
	 * please keep coords so easier to know where they were added, just comment out once packed 
	 */
	public static void main(String[] args) throws IOException {
		Cache.init();
		
		boolean rewrite = false;
		rewrite |= packMapCustom( 8503,2042, 2043, false);
		//rewrite |= packMapCustom(8207, 9002, 9003, false);
		// EDGE
		 //rewrite |= packMapCustom(12342, 624, 625, false);
		 //rewrite |= packMapCustom(12341, 622, 623, false);

		/* CHAMBERS OF XERIC
		rewrite |= packMapCustom(7951, 9000, 9001, false);
		rewrite |= packMapCustom(8207, 9002, 9003, false);

		// OLM
		// north of new maps
		rewrite |= packMapCustom(7953, 9004, 9005, false);
		// over alex's
		rewrite |= packMapCustom(23955, 9004, 9005, false);

		rewrite |= packMapCustom(13914, 2702, 2703, false);*/

		if (rewrite) {
			Cache.STORE.getIndexes()[5].rewriteTable();
			System.out.println("Done");
		}
		
	}
	
	/**
	 * @param args
	 * @throws IOException
	 * main5
	 */
	public static void main1(String[] args) throws IOException {
		Cache.init();
		osrsData = new Store(
				"C:\\Users\\Gebruiker\\Desktop\\667\\");
		loadUnpackedKeys();
		boolean rewrite = false;
		for (int i = 0; i < 65535; i++) {
			int x = (i >> 8) ;
			int y = (i & 0xff);
		/*	if (x >= 148/8 && x <= 247/8 && y >= 424/8 && y <= 501/8)
				rewrite |= packMap(i, i, false);*/
		//	region 4670 (148, 501)
		//	region 7733 (247, 424)
			
			rewrite |= packMap(i, i, true);
		}
		/*//2nd part nightmare map
		rewrite |= packMap(14642, 14642, false);
		rewrite |= packMap(14643, 14643, false);
		rewrite |= packMap(14898, 14898, false);
		rewrite |= packMap(14899, 14899, false);
		rewrite |= packMap(14900, 14900, false);
		rewrite |= packMap(14901, 14901, false);
		rewrite |= packMap(15000, 15000, false);*/
		//island of stone
	//	rewrite |= packMap(9790, 9790, false);
		
		//prifinnas
		rewrite |= packMap(12598, 12598, false);
	//	rewrite |= packMap(12600, 12600, false);
	//	rewrite |= packMap(14142, 14142, false);

		/*
		rewrite |= packMap(14386, 14386, true); 
		rewrite |= packMap(14642, 14642, true); 
		
		//raids2
		/*rewrite |= packMap(12869, 23956, true); 
		rewrite |= packMap(13123, 23957, true); 
		rewrite |= packMap(12611, 23958, true); 
		rewrite |= packMap(12867, 23959, true); */
		
	
		//5973 9812 0 23961
		
/*
		rewrite |= packMap(12889, 23955, true); //raids resource rooms  - 6000 9400 -> unused
		rewrite |= packMap(13141, 23954, true); //raids resource rooms  - 6000 9350 -> unused
		rewrite |= packMap(13140, 23953, true); //raids resource rooms  - 6000 9270 -> unused
		rewrite |= packMap(13145, 23952, true); //raids resource rooms  - 6000 9200 -> unused
		rewrite |= packMap(13397, 23951, true); //raids resource rooms  - 6000 9120 -> unused
		rewrite |= packMap(13393, 23950, true); //raids Jewelled Crabs  - 6000 9088 -> unused
		rewrite |= packMap(13138, 23949, true); //raids tecton - 6000 9028 -> unused
//		rewrite |= packMap(6727, 23948, true); //garygole boss, 6000 9000 -> unused
/*		rewrite |= packMap(12961, 12961, true); //scorpio pit 3235 10334
		rewrite |= packMap(9043, 9042, true); //inferno 2274, 5294
		rewrite |= packMap(12190, 12958, true); //wilderness godwars dungeon 3229 10123 0
		1946 3105 0
		
		
		//new poison waste
		rewrite |= packMap(9008, 9008, false);
		rewrite |= packMap(8495, 8495, false);
		rewrite |= packMap(8496, 8496, false);
		rewrite |= packMap(8751, 8751, false);
		rewrite |= packMap(9007, 9007, false);
		
		/*\for (int i = 0; i < 65535; i++) {
			rewrite |= packMap(i, i, true);
		}*/
	/*	rewrite = true;
		rewrite |= packMap(9043, 9042, false); //inferno 2274, 5294
		rewrite |= packMap(12190, 12958, false);
		
		for (int map : OSRS_MAP_IDS)
			packMap(map, map, false);*/
		/*rewrite |= packMap(11851, 11851, false); //inferno 2274, 5294
		rewrite |= packMap(12106, 12106, false);*/
	
		/*rewrite |= packMap(8280, 14932, false); //crash site cavern 3778 5405 0
		rewrite |= packMap(8536, 15188, false);*/
		

	/*	rewrite |= packMap(14650, 14650, false);
		rewrite |= packMap(14651, 14651, false);
		rewrite |= packMap(14652, 14652, false);
		rewrite |= packMap(14906, 14906, false);
		rewrite |= packMap(14907, 14907, false);
		rewrite |= packMap(14908, 14908, false);
		rewrite |= packMap(15162, 15162, false);
		rewrite |= packMap(15163, 15163, false);
		rewrite |= packMap(15164, 15164, false);*/
		
		
	
	/*rewrite |= packMap(9772, 9259, false);
	rewrite |= packMap(10028, 9515, false);
	rewrite |= packMap(10284, 9771, false);
		for (int i = 0; i < 65535; i++) 
			rewrite |= packMap(i, i, true);
		//10028
		

		rewrite |= packMap(14652, 14652, false);
		rewrite |= packMap(14142, 14142, false);
		rewrite |= packMap(14398, 14398, false);
		rewrite |= packMap(9023, 9023, false);*/
		
		//rewrite |= packMap(14649, 14649, false);
		//rewrite |= packMap(14395, 14395, false);
		if (rewrite) {
			Cache.STORE.getIndexes()[5].rewriteTable();
			System.out.println("Done");
		}
	}
	
	
	
}
