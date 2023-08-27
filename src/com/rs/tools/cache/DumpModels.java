/**
 * 
 */
package com.rs.tools.cache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.alex.utils.Constants;
import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCConfig;

/**
 * @author dragonkk(Alex)
 * Sep 5, 2017
 */
public class DumpModels {
	
	public static void main2(String[] args) throws IOException, InterruptedException {
		/*for (int i = 0; i < 500; i++) {
			String out = new Scanner(new URL("https://gamestop100.com/create-new-job?prefix=&siteId=onyxftw&toplistId=2&userId=319&jobId=13").openStream(), "UTF-8").useDelimiter("\\A").next();
			System.out.println(i+", "+out);
			Thread.sleep(3000);
		}*/
	/*	BufferedWriter writer = new BufferedWriter(new FileWriter(new File("names.txt")));
		for (File file : new File("C:\\Users\\Alex\\Desktop\\account").listFiles()) {
			String name = file.getName().replace(".ser", "").replace("_", "%20").toLowerCase();
			writer.write(name);
			writer.newLine();
		}
		writer.flush();
		writer.close();*/
	/*	Cache.init();
		int x = 2269/64;
		int y = 3072/64;
		System.out.println(Cache.STORE.getIndexes()[Constants.MAPS_INDEX].getArchiveId("m"+x+"_"+y));
		Files.write(new File("dump/m.dat").toPath(), Cache.STORE.getIndexes()[Constants.MAPS_INDEX].getFile(Cache.STORE.getIndexes()[Constants.MAPS_INDEX].getArchiveId("m"+x+"_"+y)));
		Files.write(new File("dump/l.dat").toPath(), Cache.STORE.getIndexes()[Constants.MAPS_INDEX].getFile(Cache.STORE.getIndexes()[Constants.MAPS_INDEX].getArchiveId("l"+x+"_"+y)));
		*/
	}

	public static void main(String[] args) throws IOException {
		Cache.init();
		//dump(8719);
		for (int model : NPCConfig.forID(25870).models)  
			dump(model);
	/*	for (int model : NPCConfig.forID(22043).models)  
			dump(model);*/
	/*	for (int i = 5120; i <= 5123; i++)
		for (int model : NPCConfig.forID(i).models)  
			dump(model);
	/*	dump(ItemConfig.forID(42926).model);
		dump(ItemConfig.forID(42926).maleEquip1);
		dump(ItemConfig.forID(42926).femaleEquip1);*/
	}
	
	private static void dump(int i) throws IOException {
		if (i == -1)
			return;
		Files.write(new File("dump/"+i+".dat").toPath(), Cache.STORE.getIndexes()[Constants.MODELS_INDEX].getFile(i));
	}
}
