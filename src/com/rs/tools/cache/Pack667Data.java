/**
 * 
 */
package com.rs.tools.cache;

import java.io.IOException;

import com.alex.store.Index;
import com.alex.store.Store;
import com.alex.utils.Constants;
import com.alex.utils.Utils;
import com.rs.cache.Cache;

/**
 * @author dragonkk(Alex)
 * Sep 5, 2017
 */
public class Pack667Data {

	public static final int MODEL_OFFSET = 400000, ITEM_OFFSET = 60000;

	public static void main1(String[] args) throws IOException {
		boolean packModels = true;
		boolean packItems = true;


		Cache.init();

		Store data2 = new Store("C:\\Users\\Alex\\Downloads\\avaloncache\\cache\\");
			Index interfaces = Cache.STORE.getIndexes()[Constants.INTERFACE_DEFINITIONS_INDEX];
			Index interfaces2 = data2.getIndexes()[Constants.INTERFACE_DEFINITIONS_INDEX];

			for (int i = interfaces.getLastArchiveId()+1; i < interfaces2.getLastArchiveId() + 1; i++) {
				byte[] data = interfaces2.getFile(i);
				if (data == null)
					continue;
				interfaces.putArchive(i, data2, false, false);
			}
		interfaces.rewriteTable();
		System.out.println(interfaces2.getLastArchiveId());
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		boolean packModels = true;
		boolean packItems = true;
		
		
		Cache.init();

		Store osrsData = new Store("D:\\venomite 202 data osrs\\718-osrs-\\data\\cache\\");
		
		if (packModels) {
			System.out.println("Packing 685 models.");
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
			System.out.println("Packing 685 items.");
			Index items = Cache.STORE.getIndexes()[Constants.ITEM_DEFINITIONS_INDEX];
			Index items667 = osrsData.getIndexes()[Constants.ITEM_DEFINITIONS_INDEX];
			int count = Utils.getItemDefinitionsSize(osrsData);
			for (int i = 0; i < count; i++) {
				byte[] data = items667.getFile(i >>> 8, 0xff & i);
				if (data == null)
					continue;
				int id = ITEM_OFFSET + i;
				items.putFileNoRewriteTable(id >>> 8, 0xff & id, data);
			}
			items.rewriteTable();
			
		}
	}
	
}
