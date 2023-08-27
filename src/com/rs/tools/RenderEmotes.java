package com.rs.tools;

import java.io.IOException;

import com.rs.cache.Cache;
import com.rs.cache.loaders.StanceConfig;

public class RenderEmotes {
	public static void main(String[] args) throws IOException {
		Cache.init();
		int emoteId = 2048;
		skip:for (int i = 0; i < Cache.STORE.getIndexes()[2].getLastFileId(32)+1; i++) {
		    if(!Cache.STORE.getIndexes()[2].fileExists(32, i))
			continue;
		 /*   for(int i2 = 0; i2 < Utils.getNPCDefinitionsSize(); i2++)
			if(NPCDefinitions.getNPCDefinitions(i2).renderEmote == i)
			    continue skip;*/
		    StanceConfig defs = StanceConfig.forID(i);
		    if (defs.standAnimation == emoteId || defs.walkAnimation == emoteId)
			System.out.println("RenderID: " + i);
		}

	}
}
