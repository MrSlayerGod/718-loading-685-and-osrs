package com.rs.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.WorldTile;

public final class NPCSpawns {

	public static final void init() {
		loadSpawnsList("data/npc/spawnsList.txt");
		loadSpawnsList("data/npc/customSpawnsList.txt");
	}

	private static class NPCSpawn {

		private NPCSpawn(int npcId, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
			this.npcId = npcId;
			this.tile = tile;
			this.mapAreaNameHash = mapAreaNameHash;
			this.canBeAttackFromOutOfArea = canBeAttackFromOutOfArea;
		}

		private int npcId;
		private WorldTile tile;
		private int mapAreaNameHash;
		private boolean canBeAttackFromOutOfArea;
	}

	private static final List<NPCSpawn>[][] spawns = new ArrayList[256][256];

	private static final void loadSpawnsList(String path) {
		try {
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
				if (npcId < 0 || npcId >= Utils.getNPCDefinitionsSize() || (npcId >= 15941 && npcId <= Settings.OSRS_NPC_OFFSET && npcId != 16001 && npcId != 16000 && npcId != 16006 && npcId != 16008
						&& npcId != 16018 && npcId != 16020 && npcId != 16025 && npcId != 16026))
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
				addNPCSpawn(npcId, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
			}
			in.close();
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

	public static final void loadNPCSpawns(int regionId) {
		int x = (regionId >> 8);
		int y = (regionId & 0xff);
		if (x == -1 || y == -1 || spawns[x][y] == null)
			return;
		for (NPCSpawn spawn : spawns[x][y]) {
			if (spawn.npcId >= Utils.getNPCDefinitionsSize())
				continue;
			World.spawnNPC(spawn.npcId, spawn.tile, spawn.mapAreaNameHash, spawn.canBeAttackFromOutOfArea);
		}
		spawns[x][y] = null;
	}

	private static final void addNPCSpawn(int npcId, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		// World.spawnNPC(npcId, tile, mapAreaNameHash,
		// canBeAttackFromOutOfArea);

		int x = tile.getRegionX();
		int y = tile.getRegionY();
		if (spawns[x][y] == null)
			spawns[x][y] = new ArrayList<NPCSpawn>();
		spawns[x][y].add(new NPCSpawn(npcId, tile, mapAreaNameHash, canBeAttackFromOutOfArea));
	}
	
	public static void preloadSpawns() {
		/*for (int x = 0; x < spawns.length; x++)
			for (int y = 0; y < spawns.length; y++)
				if (spawns[x][y] != null)
					World.getRegion((x << 8) + y, true);*/
		World.getRegion(10553, true);
		World.getRegion(10554, true);
		World.getRegion(7479, true);
		World.getRegion(7223, true);
		World.getRegion(14908, true);
			
	}

	private NPCSpawns() {
	}
}
