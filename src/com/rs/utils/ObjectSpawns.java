package com.rs.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;

public final class ObjectSpawns {

	public static final void init() {
		if (!(new File("data/map/packedSpawns")).exists()) {
			packObjectSpawns();
		}

		/* clip tiles inside bushes at home (home teleport randomization sometimes traps inside bushes)
		for(int i = 0; i < 4; i++) {
			World.getRegion(12342).addFloor(0, 27, 38+i);
			World.getRegion(12342).addFloor(0, 32, 41+i);
		}*/

		// clip behind grim reaper (safe zone from mini reaper spawn attack)
		for (int x = 55; x <= 61; x++) {
			World.getRegion(16209).addFloor(0, x, 52);
		}
		for (int x = 1; x <= 7; x++) {
			World.getRegion(16465).addFloor(0, x, 52);
		}
		World.getRegion(16209).addFloor(0, 61, 53);
		World.getRegion(16465).addFloor(0, 1, 53);
		World.getRegion(16209).addFloor(0, 61, 54);
		World.getRegion(16209).addFloor(0, 62, 54);
		World.getRegion(16209).addFloor(0, 63, 54);
		World.getRegion(16465).addFloor(0, 0, 54);
		World.getRegion(16465).addFloor(0, 1, 54);
	}

	private static final void packObjectSpawns() {
		Logger.log("ObjectSpawns", "Packing object spawns...");
		if (!(new File("data/map/packedSpawns")).mkdir()) {
			throw new RuntimeException("Couldn\'t create packedSpawns directory.");
		} else {
			try {
				BufferedReader e = new BufferedReader(new FileReader("data/map/unpackedSpawnsList.txt"));

				while (true) {
					String line = e.readLine();
					if (line == null) {
						e.close();
						break;
					}

					if (!line.startsWith("//")) {
						String[] splitedLine = line.split(" - ");
						if (splitedLine.length != 2) {
							e.close();
							throw new RuntimeException("Invalid Object Spawn line: " + line);
						}

						String[] splitedLine2 = splitedLine[0].split(" ");
						String[] splitedLine3 = splitedLine[1].split(" ");
						if (splitedLine2.length != 3 || splitedLine3.length != 3) {
							e.close();
							throw new RuntimeException("Invalid Object Spawn line: " + line);
						}

						int objectId = Integer.parseInt(splitedLine2[0]);
						int type = Integer.parseInt(splitedLine2[1]);
						int rotation = Integer.parseInt(splitedLine2[2]);
						WorldTile tile = new WorldTile(Integer.parseInt(splitedLine3[0]), Integer.parseInt(splitedLine3[1]), Integer.parseInt(splitedLine3[2]));
						addObjectSpawn(objectId, type, rotation, tile.getRegionId(), tile);
					}
				}
			} catch (Throwable var9) {
				Logger.handle(var9);
			}

		}
	}

	public static final void loadObjectSpawns(int regionId) {
		File file = new File("data/map/packedSpawns/" + regionId + ".os");
		if (file.exists()) {
			try {
				RandomAccessFile e = new RandomAccessFile(file, "r");
				FileChannel channel = e.getChannel();
				MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0L, channel.size());
				Region region = World.getRegion(regionId);

				while (buffer.hasRemaining()) {
					int objectId = buffer.getInt();
					int type = buffer.get() & 255;
					int rotation = buffer.get() & 255;
					int plane = buffer.get() & 255;
					int x = buffer.getShort() & '\uffff';
					int y = buffer.getShort() & '\uffff';
					WorldObject object = new WorldObject(objectId, type, rotation, x, y, plane);
					region.spawnObject(object, plane, object.getXInRegion(), object.getYInRegion(), false);
				}

				channel.close();
				e.close();
			} catch (FileNotFoundException var13) {
				var13.printStackTrace();
			} catch (IOException var14) {
				var14.printStackTrace();
			}

		}
	}

	private static final void addObjectSpawn(int objectId, int type, int rotation, int regionId, WorldTile tile) {
		try {
			DataOutputStream e = new DataOutputStream(new FileOutputStream("data/map/packedSpawns/" + regionId + ".os", true));
			e.writeInt(objectId);
			e.writeByte(type);
			e.writeByte(rotation);
			e.writeByte(tile.getPlane());
			e.writeShort(tile.getX());
			e.writeShort(tile.getY());
			e.flush();
			e.close();
		} catch (FileNotFoundException var6) {
			var6.printStackTrace();
		} catch (IOException var7) {
			var7.printStackTrace();
		}

	}

}
