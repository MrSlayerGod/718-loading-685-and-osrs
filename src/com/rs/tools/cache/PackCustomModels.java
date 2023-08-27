/**
 * 
 */
package com.rs.tools.cache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.alex.utils.Constants;
import com.rs.cache.Cache;

/**
 * @author dragonkk(Alex)
 * Sep 5, 2017
 */
public class PackCustomModels {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Cache.init();
		/*
		if (true == true) {
			int count = 3;
			for (int i = 100000; i < 200000; i++) {
				byte[] data = Cache.STORE.getIndexes()[Constants.MODELS_INDEX].getFile(i);
				if (data == null) {
					
					System.out.println(i);
					if (--count == 0)
						break;
				}
			}
			return;
		}*/
		
		for (File file : new File("model/").listFiles()) {
			if (file.getName().startsWith("."))
				continue;
			int id = Integer.parseInt(file.getName().replace(".dat", ""));
			System.out.println("attempting "+id);
			Cache.STORE.getIndexes()[Constants.MODELS_INDEX].putFile(id, 0, Constants.GZIP_COMPRESSION, Files.readAllBytes(file.toPath()), null, false, false, -1, -1);
			System.out.println("packed "+id);
		}
		try {
			System.out.println("saving..");
			Cache.STORE.getIndexes()[Constants.MODELS_INDEX].rewriteTable();
			System.out.println("saved");
		} catch(Exception e) {
			e.printStackTrace();;
		}
	}
	
/*	public int getArchiveId() {
		return getId() >>> 8;
	}

	public int getFileId() {
		return 0xff & getId();
	}*/
}
