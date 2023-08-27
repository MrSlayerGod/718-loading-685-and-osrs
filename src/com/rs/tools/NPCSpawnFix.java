/**
 * 
 */
package com.rs.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCConfig;
import com.rs.game.WorldTile;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Nov 28, 2017
 */
public class NPCSpawnFix {

	public static void main(String[] args) throws IOException {
		Cache.init();
		init();
	}
	public static final void init() {
		loadSpawnsList("data/npc/zeahSpawnList.txt");
	}

	private static final void loadSpawnsList(String path) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("data/npc/zeahfixed.txt"));
			BufferedReader in = new BufferedReader(new FileReader(path));
			int count = 0;
			while (true) {
				count++;
				String line = in.readLine();
				if (line == null)
					break;
				if (line.startsWith("//") || line.startsWith("RSBOT"))
					continue;
				String[] splitedLine = line.split(" - ", 2);
				if (splitedLine.length != 2) {
					in.close();
					throw new RuntimeException("Invalid NPC Spawn line: " + line + " , line number: " + count);
				}
				int npcId = Integer.parseInt(splitedLine[0]);
				if (npcId >= Utils.getNPCDefinitionsSize() || (npcId >= 15941 && npcId <= Settings.OSRS_NPC_OFFSET))
					continue;
				String[] splitedLine2 = splitedLine[1].split(" ", 5);
				if (splitedLine2.length != 3 && splitedLine2.length != 5) {
					in.close();
					throw new RuntimeException("Invalid NPC Spawn line: " + line + " , line number: " + count);
				}
				WorldTile tile = new WorldTile(Integer.parseInt(splitedLine2[0]), Integer.parseInt(splitedLine2[1]), Integer.parseInt(splitedLine2[2]));
				int mapAreaNameHash = -1;
				boolean canBeAttackFromOutOfArea = true;
				if (splitedLine2.length == 5) {
					mapAreaNameHash = Utils.getNameHash(splitedLine2[3]);
					canBeAttackFromOutOfArea = Boolean.parseBoolean(splitedLine2[4]);
				}
				int oldId = npcId;
				npcId = getID(npcId);
				NPCConfig config = NPCConfig.forID(npcId);
				if (config.getName().startsWith("Reanimated"))
					continue;
				out.newLine();
				out.write("//"+config.getName()+", "+config.combatLevel+", "+oldId);
				out.flush();
				out.newLine();
				out.write(npcId+" - "+(tile.getX()  )+" "+(tile.getY() )+" "+tile.getPlane());
				out.flush();
				
				//-1124 x
				//- 2752 y
			}
			in.close();
			out.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static int getID(int id) {
		NPCConfig config = NPCConfig.forID(id);
		if (config.getName() == null || config.getName().equalsIgnoreCase("null")
				|| config.getName().equalsIgnoreCase("banker"))
			return id;
		for (int i = 0; i < 20000; i++) {
			NPCConfig config2 = NPCConfig.forID(i);
			if (config2.getName() == null || config2.getName().equalsIgnoreCase("null"))
				continue;
			if (config2.getName().equals(config.getName()) &&  config.combatLevel ==  config2.combatLevel && Arrays.equals(config2.actions, config.actions)) {
				System.out.println("converted "+config2.getName()+", "+config.combatLevel+", "+i+", "+id);
				return i;
			}
		}
		return id;
	}

}
