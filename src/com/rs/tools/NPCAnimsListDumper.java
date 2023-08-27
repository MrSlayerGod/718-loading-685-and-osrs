package com.rs.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCConfig;
import com.rs.cache.loaders.StanceConfig;
import com.rs.utils.Utils;

public class NPCAnimsListDumper {

	public static void main(String[] args) throws IOException {
		Cache.init();
		File file = new File("npcAnimList.txt");
		if (file.exists())
			file.delete();
		else
			file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		 writer.append("//Version = 745\n");
		writer.flush();
		for (int id = 0; id < Utils.getNPCDefinitionsSize(); id++) {
			NPCConfig def = NPCConfig.forID(id);
			if (def.renderEmote != -1) {
				StanceConfig defs = StanceConfig.forID(def.renderEmote);
				writer.write(id + ", run: " + defs.runAnimation + ", walk: " + defs.walkAnimation + ", stand: " +defs.standAnimation+ ", loop: "
						+ Arrays.toString(defs.loopAnimations));
				writer.newLine();
				writer.flush();
			}
		}
		writer.close();
	}

}
