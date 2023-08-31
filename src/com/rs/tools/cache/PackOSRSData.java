/**
 * 
 */
package com.rs.tools.cache;

import java.io.IOException;

import com.alex.store.Index;
import com.alex.store.Store;
import com.alex.utils.Constants;
import com.rs.cache.Cache;

/**
 * @author dragonkk(Alex)
 * Sep 5, 2017
 */
public class PackOSRSData {

	public static final int MODEL_OFFSET = 200000, ITEM_OFFSET = 30000, SKELETONS_OFFSET = 5000, SKINS_OFFSET = 5000
			, ANIMATIONS_OFFSET = 20000, GRAPHICS_OFFSET = 5000, NPCS_OFFSET = 20000, OBJECTS_OFFSET = 100000, MUSIC_OFFSET = 1200, FLOOR_OFFSET = 1000,
			SPRITES_OFFSET = 20000;
	

	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
	/*	if (true == true) {
			Cache.init();
			Store osrsData = new Store("C:\\Users\\alex_\\downloads\\osrs-server\\data\\cache\\");
			
			System.out.println(Utils.getNPCDefinitionsSize());
			return;
		}*/
		
		boolean packModels = true;
		boolean packItems = true;
		boolean packSkeletons = true;
		boolean packSkins = true;
		boolean packAnimations = true;
		boolean packGraphics = true;
		boolean packNPCs = true;
		boolean packObjects = true;
		boolean packMusics = true;
		boolean packFloor = true;
		boolean packSprites = true;
		Cache.init();

		Store osrsData = new Store(
				"C:\\Users\\Gebruiker\\Desktop\\192 cache\\");
			//	"C:\\Users\\alex_\\git\\osrs-server\\data\\cache\\");
		
		
		
		if (packModels) {
			System.out.println("Packing osrs models.");
			Index models = Cache.STORE.getIndexes()[Constants.MODELS_INDEX];
			Index osrsModels = osrsData.getIndexes()[Constants.MODELS_INDEX];
		
			for (int i = 0; i < osrsModels.getLastArchiveId() + 1; i++) {
				byte[] data = osrsModels.getFile(i);
				if (data == null)
					continue;
				models.putFileNoRewriteTable(MODEL_OFFSET + i, 0, data);
			}
			models.rewriteTable();
			//model + 200000
		}
		if (packItems) {
			System.out.println("Packing osrs items.");
			Index items = Cache.STORE.getIndexes()[Constants.ITEM_DEFINITIONS_INDEX];
			Index osrsConfig = osrsData.getIndexes()[2];
			
			for (int i = 0; i <= osrsConfig.getLastFileId(10); i++) {
				byte[] data = osrsConfig.getFile(10, i);
				if (data == null)
					continue;
				int id = ITEM_OFFSET + i;
				items.putFileNoRewriteTable(id >>> 8, 0xff & id, data);
			}
			items.rewriteTable();
		}
		if (packSkeletons) {
			System.out.println("Packing osrs skeletons.");
			Index skeletons = Cache.STORE.getIndexes()[0];
			Index osrsSkeletons = osrsData.getIndexes()[0];
			for (int i = 0; i <= osrsSkeletons.getLastArchiveId(); i++) {
				if (!osrsSkeletons.archiveExists(i))
					continue;
				int id = i + SKELETONS_OFFSET;
				skeletons.putArchive(0, i, id, osrsData, false, false);
			}
			skeletons.rewriteTable();
		}
		if (packSkins) {
			System.out.println("Packing osrs skins.");
			Index skins = Cache.STORE.getIndexes()[1];
			Index osrsSkins = osrsData.getIndexes()[1];
			for (int i = 0; i <= osrsSkins.getLastArchiveId(); i++) {
				if (!osrsSkins.archiveExists(i))
					continue;
				int id =  i + SKINS_OFFSET;
				skins.putArchive(1, i, id, osrsData, false, false);
			}
			skins.rewriteTable();
		}
		if (packAnimations) {
			System.out.println("Packing osrs animations.");
			Index items = Cache.STORE.getIndexes()[20];
			Index osrsConfig = osrsData.getIndexes()[2]; 
			
			for (int i = 0; i <= osrsConfig.getLastFileId(12); i++) {
				byte[] data = osrsConfig.getFile(12, i);
				if (data == null)
					continue;
				int id = ANIMATIONS_OFFSET + i;
				items.putFileNoRewriteTable(id >>> 7, id & 0x7f, data);
			}
			items.rewriteTable();
		}
		if (packGraphics) {
			System.out.println("Packing osrs graphics.");
			Index items = Cache.STORE.getIndexes()[21];
			Index osrsConfig = osrsData.getIndexes()[2]; 
			
			for (int i = 0; i <= osrsConfig.getLastFileId(13); i++) {
				byte[] data = osrsConfig.getFile(13, i);
				if (data == null)
					continue;
				int id = GRAPHICS_OFFSET + i;
				items.putFileNoRewriteTable(id >>> 735411752, id & 0xff, data);
			}
			items.rewriteTable();
		}
		if (packNPCs) {
			System.out.println("Packing osrs npcs.");
			Index items = Cache.STORE.getIndexes()[18];
			Index osrsConfig = osrsData.getIndexes()[2]; 
			
			for (int i = 0; i <= osrsConfig.getLastFileId(9); i++) {
				byte[] data = osrsConfig.getFile(9, i);
				if (data == null)
					continue;
				int id = NPCS_OFFSET + i;
				items.putFileNoRewriteTable(id >>> 134238215, id & 0x7f, data);
			}
			items.rewriteTable();
		}
		
		if (packObjects) {
			System.out.println("Packing osrs objects.");
			Index items = Cache.STORE.getIndexes()[16];
			Index osrsConfig = osrsData.getIndexes()[2]; 
			
			for (int i = 0; i <= osrsConfig.getLastFileId(6); i++) {
				byte[] data = osrsConfig.getFile(6, i);
				if (data == null)
					continue;
				int id = OBJECTS_OFFSET + i;
				items.putFileNoRewriteTable(id >>> -1135990488, id & 0xff, data);
			}
			items.rewriteTable();
		}
		
		if (packMusics) {
			System.out.println("Packing osrs musics.");
			Index midi = Cache.STORE.getIndexes()[6];
			Index osrsMIDI = osrsData.getIndexes()[6]; 
			for (int i = 0; i <= osrsMIDI.getLastArchiveId(); i++) {
				if (!osrsMIDI.archiveExists(i))
					continue;
				int id = i + MUSIC_OFFSET;
				midi.putArchive(6, i, id, osrsData, false, false);
			}
			midi.rewriteTable();
		}
		if (packSprites) {
			System.out.println("Packing osrs sprites.");
			Index sprites = Cache.STORE.getIndexes()[8];
			Index osrsSprites = osrsData.getIndexes()[8]; 
			for (int i = 0; i <= osrsSprites.getLastArchiveId(); i++) {
				if (!osrsSprites.archiveExists(i))
					continue;
				int id = i + SPRITES_OFFSET;
				sprites.putArchive(8, i, id, osrsData, false, false);
			}
			sprites.rewriteTable();
		}
		
		if (packFloor) {
			Index config = Cache.STORE.getIndexes()[2]; 
			Index osrsConfig = osrsData.getIndexes()[2]; 
			System.out.println("Packing osrs floor.");
			
			for (int i = 0; i < osrsConfig.getLastFileId(1); i++) {
				byte[] data = osrsConfig.getFile(1, i);
				if (data == null)
					continue;
				config.putFileNoRewriteTable(1, i + FLOOR_OFFSET, data);
			}
			for (int i = 0; i < osrsConfig.getLastFileId(4); i++) {
				byte[] data = osrsConfig.getFile(4, i);
				if (data == null)
					continue;
				config.putFileNoRewriteTable(4, i + FLOOR_OFFSET, data);
			}
			config.rewriteTable();
		}
		//149, 173
		System.out.println(Cache.STORE.getIndexes()[2].getLastFileId(1));
		System.out.println(Cache.STORE.getIndexes()[2].getLastFileId(4));
	}
	
}
