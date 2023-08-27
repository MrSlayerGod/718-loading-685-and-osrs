package com.rs.tools;

import java.io.IOException;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCConfig;
import com.rs.utils.Utils;

public class NPCCheck {

	public static void main(String[] args) throws IOException {
		Cache.init();
		for (int id = 0; id < Utils.getNPCDefinitionsSize(); id++) {
			NPCConfig def = NPCConfig.forID(id);
			if (def.name.contains("Elemental")) {
				System.out.println(id + " - " + def.name);
			}
		}
	}

}
