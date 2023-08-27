package com.rs.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.alex.store.Store;
import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ObjectConfig;
import com.rs.executor.GameExecutorManager;
import com.rs.game.item.FloorItem;
import com.rs.game.player.MusicsManager;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.io.InputStream;
import com.rs.utils.ItemSpawns;
import com.rs.utils.Logger;
import com.rs.utils.NPCSpawns;
import com.rs.utils.ObjectSpawns;
import com.rs.utils.Utils;

public class Region {
	public static final int[] OBJECT_SLOTS = new int[]
	{ 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3 };
	public static final int OBJECT_SLOT_WALL = 0;
	public static final int OBJECT_SLOT_WALL_DECORATION = 1;
	public static final int OBJECT_SLOT_FLOOR = 2;
	public static final int OBJECT_SLOT_FLOOR_DECORATION = 3;

	protected int regionId;
	protected RegionMap map;
	protected RegionMap clipedOnlyMap;

	protected List<Integer> playersIndexes;
	protected List<Integer> npcsIndexes;
	protected List<WorldObject> spawnedObjects;
	protected List<WorldObject> removedOriginalObjects;
	protected List<Projectile> projectiles;
	private List<FloorItem> groundItems;
	protected WorldObject[][][][] objects;
	private volatile int loadMapStage;
	private boolean loadedNPCSpawns;
	private boolean loadedObjectSpawns;
	private boolean loadedItemSpawns;
	private int[] musicIds;
	private boolean osrs;

	public Region(int regionId) {
		this.regionId = regionId;
		this.spawnedObjects = new CopyOnWriteArrayList<WorldObject>();
		this.removedOriginalObjects = new CopyOnWriteArrayList<WorldObject>();
		this.projectiles = new CopyOnWriteArrayList<Projectile>();
		osrs = isOSRSMap();
		loadMusicIds();
		// indexes null by default cuz we dont want them on mem for regions that
		// players cant go in
	}

	public void checkLoadMap() {
		if (getLoadMapStage() == 0) {
			setLoadMapStage(1);
			GameExecutorManager.slowExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						loadRegionMap();
						setLoadMapStage(2);
						if (!isLoadedObjectSpawns()) {
							loadObjectSpawns();
							setLoadedObjectSpawns(true);
						}
						if (!isLoadedNPCSpawns()) {
							loadNPCSpawns();
							setLoadedNPCSpawns(true);
						}
						if (!isLoadedItemSpawns()) {
							loadItemSpawns();
							setLoadedItemSpawns(true);
						}
					} catch (Throwable e) {
						Logger.handle(e);
					}
				}
			});
		}
	}

	private void loadNPCSpawns() {
		NPCSpawns.loadNPCSpawns(regionId);
	}

	private void loadObjectSpawns() {
		ObjectSpawns.loadObjectSpawns(regionId);
	}

	private void loadItemSpawns() {
		ItemSpawns.loadItemSpawns(regionId);
	}

	/**
	 * Unload's map from memory.
	 */
	public void unloadMap() {
		if (getLoadMapStage() == 2 && (playersIndexes == null || playersIndexes.isEmpty()) && (npcsIndexes == null || npcsIndexes.isEmpty())) {
			objects = null;
			map = null;
			setLoadMapStage(0);
		}
	}

	public RegionMap forceGetRegionMapClipedOnly() {
		if (clipedOnlyMap == null)
			clipedOnlyMap = new RegionMap(regionId, true);
		return clipedOnlyMap;
	}

	public RegionMap forceGetRegionMap() {
		if (map == null)
			map = new RegionMap(regionId, false);
		return map;
	}

	public RegionMap getRegionMap() {
		return map;
	}

	public int getMask(int plane, int localX, int localY) {
		if (map == null || getLoadMapStage() != 2)
			return -1; // cliped tile
		return map.getMasks()[plane][localX][localY];
	}

	public int getMaskClipedOnly(int plane, int localX, int localY) {
		if (clipedOnlyMap == null || getLoadMapStage() != 2)
			return -1; // cliped tile
		return clipedOnlyMap.getMasks()[plane][localX][localY];
	}

	public void setMask(int plane, int localX, int localY, int mask) {
		if (map == null || getLoadMapStage() != 2)
			return; // cliped tile

		if (localX >= 64 || localY >= 64 || localX < 0 || localY < 0) {
			WorldTile tile = new WorldTile(map.getRegionX() + localX, map.getRegionY() + localY, plane);
			int regionId = tile.getRegionId();
			int newRegionX = (regionId >> 8) * 64;
			int newRegionY = (regionId & 0xff) * 64;
			World.getRegion(tile.getRegionId()).setMask(plane, tile.getX() - newRegionX, tile.getY() - newRegionY, mask);
			return;
		}

		map.setMask(plane, localX, localY, mask);
	}

	public void clip(WorldObject object, int x, int y) {
		if (object.getId() == -1) //dont clip or noclip with id -1
			return;
		if (map == null)
			map = new RegionMap(regionId, false);
		if (clipedOnlyMap == null)
			clipedOnlyMap = new RegionMap(regionId, true);
		int plane = object.getPlane();
		int type = object.getType();
		int rotation = object.getRotation();
		if (x < 0 || y < 0 || x >= map.getMasks()[plane].length || y >= map.getMasks()[plane][x].length)
			return;
		ObjectConfig objectDefinition = ObjectConfig.forID(object.getId()); // load
		// here

		if (type == 22 ? objectDefinition.getClipType() != 1 : objectDefinition.getClipType() == 0)
			return;
		if (type >= 0 && type <= 3) {
			if (!objectDefinition.ignoreClipOnAlternativeRoute) //disabled those walls for now since theyre guard corners, temporary fix
				map.addWall(plane, x, y, type, rotation, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
			if (objectDefinition.isProjectileCliped())
				clipedOnlyMap.addWall(plane, x, y, type, rotation, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
		} else if (type >= 9 && type <= 21) {
			int sizeX;
			int sizeY;
			if (rotation != 1 && rotation != 3) {
				sizeX = objectDefinition.getSizeX();
				sizeY = objectDefinition.getSizeY();
			} else {
				sizeX = objectDefinition.getSizeY();
				sizeY = objectDefinition.getSizeX();
			}
			map.addObject(plane, x, y, sizeX, sizeY, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
			if (objectDefinition.isProjectileCliped())
				clipedOnlyMap.addObject(plane, x, y, sizeX, sizeY, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
		} else if (type == 22) {
			map.addFloor(plane, x, y); // dont ever fucking think about removing it..., some floor deco objects DOES BLOCK WALKING
		}
	}

	public void addFloor(int plane, int x, int y) {
		if (map == null)
			map = new RegionMap(regionId, false);
		if (clipedOnlyMap == null)
			clipedOnlyMap = new RegionMap(regionId, true);
		map.addFloor(plane, x, y);
	}
	
	public void removeFloor(int plane, int x, int y) {
		if (map == null)
			map = new RegionMap(regionId, false);
		if (clipedOnlyMap == null)
			clipedOnlyMap = new RegionMap(regionId, true);
		map.removeFloor(plane, x, y);
	}

	
	public void unclip(int plane, int x, int y) {
		if (map == null)
			map = new RegionMap(regionId, false);
		if (clipedOnlyMap == null)
			clipedOnlyMap = new RegionMap(regionId, true);
		map.setMask(plane, x, y, 0);
	}

	public void forceClip(boolean clip, WorldTile t, int sizeX, int sizeY) {
		if(clip) {
			forceGetRegionMap().addObject(t.getPlane(), t.getXInRegion(), t.getYInRegion(), sizeX, sizeY, true, true);
			forceGetRegionMapClipedOnly().addObject(t.getPlane(), t.getXInRegion(), t.getYInRegion(), sizeX, sizeY, true, true);
		} else {
			forceGetRegionMap().removeObject(t.getPlane(), t.getXInRegion(), t.getYInRegion(), sizeX, sizeY, true, true);
			forceGetRegionMapClipedOnly().removeObject(t.getPlane(), t.getXInRegion(), t.getYInRegion(), sizeX, sizeY, true, true);
		}
	}

	public void unclip(WorldObject object, int x, int y) {
		if (object.getId() == -1) //dont clip or noclip with id -1
			return;
		if (map == null)
			map = new RegionMap(regionId, false);
		if (clipedOnlyMap == null)
			clipedOnlyMap = new RegionMap(regionId, true);
		int plane = object.getPlane();
		int type = object.getType();
		int rotation = object.getRotation();
		if (x < 0 || y < 0 || x >= map.getMasks()[plane].length || y >= map.getMasks()[plane][x].length)
			return;
		ObjectConfig objectDefinition = ObjectConfig.forID(object.getId()); // load
		// here
		if (type == 22 ? objectDefinition.getClipType() != 1 : objectDefinition.getClipType() == 0)
			return;
		if (type >= 0 && type <= 3) {
			map.removeWall(plane, x, y, type, rotation, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
			if (objectDefinition.isProjectileCliped())
				clipedOnlyMap.removeWall(plane, x, y, type, rotation, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
		} else if (type >= 9 && type <= 21) {
			int sizeX;
			int sizeY;
			if (rotation != 1 && rotation != 3) {
				sizeX = objectDefinition.getSizeX();
				sizeY = objectDefinition.getSizeY();
			} else {
				sizeX = objectDefinition.getSizeY();
				sizeY = objectDefinition.getSizeX();
			}
			map.removeObject(plane, x, y, sizeX, sizeY, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
			if (objectDefinition.isProjectileCliped())
				clipedOnlyMap.removeObject(plane, x, y, sizeX, sizeY, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
		} else if (type == 22) {
			map.removeFloor(plane, x, y);
		}
	}

	public void spawnObject(WorldObject object, int plane, int localX, int localY, boolean original) {
		//shouldnt happen but could with rotated instances
		if (localX >= 64 || localY >= 64 || localX < 0 || localY < 0) {
			WorldTile tile = new WorldTile(map.getRegionX() + localX, map.getRegionY() + localY, plane);
			int regionId = tile.getRegionId();
			int newRegionX = (regionId >> 8) * 64;
			int newRegionY = (regionId & 0xff) * 64;
			World.getRegion(tile.getRegionId()).spawnObject(object, plane, tile.getX() - newRegionX, tile.getY() - newRegionY, original);
			return;
		}
		if (objects == null)
			objects = new WorldObject[4][64][64][4];
		int slot = OBJECT_SLOTS[object.getType()];
		if (original) {
			objects[plane][localX][localY][slot] = object;
			clip(object, localX, localY);
			if (object.getId() == 127215 && regionId == 6200) {
				World.spawnNPC(16000, object, -1, true);
				removeObject(object, plane, localX, localY);
			}
		} else {
			WorldObject spawned = getSpawnedObjectWithSlot(plane, localX, localY, slot);
			// found non original object on this slot. removing it since we
			// replacing with a new non original
			if (spawned != null) {
				spawnedObjects.remove(spawned);
				// unclips non orignal old object which had been cliped so can
				// clip the new non original
				unclip(spawned, localX, localY);
			}
			WorldObject removed = getRemovedObjectWithSlot(plane, localX, localY, slot);
			// there was a original object removed. lets readd it
			if (removed != null) {
				object = removed;
				removedOriginalObjects.remove(object);
				// adding non original object to this place
			} else if (objects[plane][localX][localY][slot] != object) {
				spawnedObjects.add(object);
				// unclips orignal old object which had been cliped so can clip
				// the new non original
				if (objects[plane][localX][localY][slot] != null)
					unclip(objects[plane][localX][localY][slot], localX, localY);
			} else if (spawned == null) {
				if (Settings.DEBUG)
					Logger.log(this, "Requested object to spawn is already spawned.(Shouldnt happen)");
				return;
			}
			// clips spawned object(either original or non original)
			clip(object, localX, localY);
			for (Player p2 : World.getPlayers()) {
				if (p2 == null || !p2.hasStarted() || p2.hasFinished() || !p2.getMapRegionsIds().contains(regionId))
					continue;
				p2.getPackets().sendAddObject(object);
			}
		}
	}

	public void removeObject(WorldObject object, int plane, int localX, int localY) {
		if (objects == null)
			objects = new WorldObject[4][64][64][4];
		int slot = OBJECT_SLOTS[object.getType()];
		WorldObject removed = getRemovedObjectWithSlot(plane, localX, localY, slot);
		if (removed != null) {
			removedOriginalObjects.remove(object);
			clip(removed, localX, localY);
		}
		WorldObject original = null;
		// found non original object on this slot. removing it since we
		// replacing with real one or none if none
		WorldObject spawned = getSpawnedObjectWithSlot(plane, localX, localY, slot);
		if (spawned != null) {
			object = spawned;
			spawnedObjects.remove(object);
			unclip(object, localX, localY);
			WorldObject o = objects[plane][localX][localY][slot];
			if (o != null) {// original
				// unclips non original to clip original above
				clip(o, localX, localY);
				original = o;
			}
			// found original object on this slot. removing it since requested
		} else if (objects[plane][localX][localY][slot] == object) { // removes  original
			unclip(object, localX, localY);
			removedOriginalObjects.add(object);
		} else {
			if (Settings.DEBUG)
				Logger.log(this, "Requested object to remove wasnt found.(Shouldnt happen)");
			return;
		}
		for (Player p2 : World.getPlayers()) {
			if (p2 == null || !p2.hasStarted() || p2.hasFinished() || !p2.getMapRegionsIds().contains(regionId))
				continue;
			if (original != null)
				p2.getPackets().sendAddObject(original);
			else
				p2.getPackets().sendRemoveObject(object);
		}

	}

	public WorldObject getStandartObject(int plane, int x, int y) {
		return getObjectWithSlot(plane, x, y, OBJECT_SLOT_FLOOR);
	}

	public WorldObject getObjectWithType(int plane, int x, int y, int type) {
		WorldObject object = getObjectWithSlot(plane, x, y, OBJECT_SLOTS[type]);
		return object != null && object.getType() == type ? object : null;
	}

	public WorldObject getObjectWithSlot(int plane, int x, int y, int slot) {
		if (objects == null)
			return null;
		WorldObject o = getSpawnedObjectWithSlot(plane, x, y, slot);
		if (o == null) {
			if (getRemovedObjectWithSlot(plane, x, y, slot) != null)
				return null;
			return objects[plane][x][y][slot];
		}
		return o;
	}

	public WorldObject getRealObject(int plane, int x, int y, int type) {
		return objects[plane][x][y][OBJECT_SLOTS[type]];
	}

	public WorldObject getSpawnedObjectWithSlot(int plane, int x, int y, int slot) {
		for (WorldObject object : spawnedObjects) {
			if (object.getXInRegion() == x && object.getYInRegion() == y && object.getPlane() == plane && OBJECT_SLOTS[object.getType()] == slot)
				return object;
		}
		return null;
	}

	public WorldObject getRemovedObjectWithSlot(int plane, int x, int y, int slot) {
		for (WorldObject object : removedOriginalObjects) {
			if (object.getXInRegion() == x && object.getYInRegion() == y && object.getPlane() == plane && OBJECT_SLOTS[object.getType()] == slot)
				return object;
		}
		return null;
	}

	public WorldObject[] getAllObjects(int plane, int x, int y) {
		if (objects == null)
			return null;
		return objects[plane][x][y];
	}

	public List<WorldObject> getAllObjects() {
		if (objects == null)
			return null;
		List<WorldObject> list = new LinkedList<WorldObject>();
		for (int z = 0; z < 4; z++)
			for (int x = 0; x < 64; x++)
				for (int y = 0; y < 64; y++) {
					if (objects[z][x][y] == null)
						continue;
					for (WorldObject o : objects[z][x][y])
						if (o != null)
							list.add(o);
				}
		return list;
	}

	public boolean containsObjectWithId(int plane, int x, int y, int id) {
		WorldObject object = getObjectWithId(plane, x, y, id);
		return object != null && object.getId() == id;
	}

	public WorldObject getObjectWithId(int plane, int x, int y, int id) {
		if (objects == null)
			return null;
		for (WorldObject object : removedOriginalObjects) {
			if (object.getId() == id && object.getXInRegion() == x && object.getYInRegion() == y && object.getPlane() == plane)
				return null;
		}
		for (int i = 0; i < 4; i++) {
			WorldObject object = objects[plane][x][y][i];
			if (object != null && object.getId() == id) {
				WorldObject spawned = getSpawnedObjectWithSlot(plane, x, y, OBJECT_SLOTS[object.getType()]);
				return spawned == null ? object : spawned;
			}
		}
		for (WorldObject object : spawnedObjects) {
			if (object.getXInRegion() == x && object.getYInRegion() == y && object.getPlane() == plane && object.getId() == id)
				return object;
		}
		return null;
	}

	public WorldObject getObjectWithId(int id, int plane) {
		if (objects == null)
			return null;
		for (WorldObject object : spawnedObjects) {
			if (object.getId() == id && object.getPlane() == plane)
				return object;
		}
		for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 64; y++) {
				for (int slot = 0; slot < objects[plane][x][y].length; slot++) {
					WorldObject object = objects[plane][x][y][slot];
					if (object != null && object.getId() == id)
						return object;
				}
			}
		}
		return null;
	}

	public List<WorldObject> getSpawnedObjects() {
		return spawnedObjects;
	}

	public List<WorldObject> getRemovedOriginalObjects() {
		return removedOriginalObjects;
	}
	
	public boolean addProjectile(Projectile projectile) {
		return projectiles.add(projectile);
	}
	
	public List<Projectile> getProjectiles() {
		return projectiles;
	}
	
	public void removeProjectiles() {
		projectiles.clear();
	}
	
	public void loadRegionMap() {
		boolean osrs = isOSRSMap();
		int regionX = (regionId >> 8) ;
		int regionY = (regionId & 0xff);
		int archiveId = Cache.STORE.getIndexes()[5].getArchiveId("m"+regionX+"_"+regionY);//Utils.getMapArchiveId(regionX, regionY);
		if (archiveId == -1)
			return;
		byte[] mapSettingsData = Cache.STORE.getIndexes()[5].getFile(archiveId);//, 3);
		byte[][][] mapSettings = loadMapSettings(mapSettingsData);
		if(mapSettingsData == null) 
			return;
		//client returns if no map settings
		
		int archiveId2 = Cache.STORE.getIndexes()[5].getArchiveId("l"+regionX+"_"+regionY);//Utils.getMapArchiveId(regionX, regionY);
		if (archiveId2 == -1)
			return;
		byte[] objectsData = Cache.STORE.getIndexes()[5].getFile(archiveId2);
		if(objectsData != null)
			loadMapObjects(objectsData, regionX, regionY, mapSettings, osrs);
		
/*	if (Settings.DEBUG && landContainerData == null && landArchiveId != -1 && MapArchiveKeys.getMapKeys(regionId) != null)
	    Logger.log(this, "Missing xteas for region " + regionId + ".");*/
	}
	
	public byte[][][] loadMapSettings(byte[] data) {
		byte[][][] mapSettings;
		if (data != null) {
			mapSettings = new byte[4][64][64];
			InputStream stream = new InputStream(data);
			for (int plane = 0; plane < 4; plane++) {
				for (int x = 0; x < 64; x++) {
					for (int y = 0; y < 64; y++) {
						while (true) {
							int value = stream.readUnsignedByte();
							if (value == 0) {
								break;
							} else if (value == 1) {
								stream.readByte();
								break;
							} else if (value <= 49) {
								stream.readByte();

							} else if (value <= 81) {
								mapSettings[plane][x][y] = (byte) (value - 49);
							}
						}
					/*	int value = stream.readUnsignedByte();
						if((value & 0x1) != 0) {
							 stream.readUnsignedByte();
							 stream.readUnsignedSmart();

						}
						if((value & 0x2) != 0) {
							mapSettings[plane][x][y] = (byte) stream.readByte();

						}
						if((value & 0x4) != 0) {
							stream.readUnsignedSmart(); //setted to 30
						
						}
						if((value & 0x8) != 0) {
							 stream.readUnsignedByte();
	
						}*/
					}
				}
			}
			for (int plane = 0; plane < 4; plane++) {
				for (int x = 0; x < 64; x++) {
					for (int y = 0; y < 64; y++) {
						if ((mapSettings[plane][x][y] & 0x1) == 1) {
							int realPlane = plane;
							if ((mapSettings[1][x][y] & 2) == 2)
								realPlane--;
							if (realPlane >= 0)
								forceGetRegionMap().addUnwalkable(realPlane, x, y);
						}
					}
				}
			}
		} else {
			mapSettings = null;
			for (int plane = 0; plane < 4; plane++) {
				for (int x = 0; x < 64; x++) {
					for (int y = 0; y < 64; y++) {
						forceGetRegionMap().addUnwalkable(plane, x, y);
					}
				}
			}
		}
		return mapSettings;
	}
	
	public boolean isOSRSMap() {
		return isOSRSMap(regionId);
	}
	
	public static boolean isOSRSMap(int id) {
		for (int map : Settings.OSRS_MAP_IDS)
			if (id == map)
				return true;
		return false;
	}
	
	public void loadMapObjects(byte[] data, int regionX, int regionY, byte[][][] mapSettings, boolean osrs) {
		InputStream landStream = new InputStream(data);
		int objectId = -1;
		int incr;
		while ((incr = landStream.readSmart2()) != 0) {
			objectId += incr;
			int location = 0;
			int incr2;
			while ((incr2 = landStream.readUnsignedSmart()) != 0) {
				location += incr2 - 1;
				int localX = (location >> 6 & 0x3f);
				int localY = (location & 0x3f);
				int plane = location >> 12;
				int objectData = landStream.readUnsignedByte();
				int type = objectData >> 2;
				int rotation = objectData & 0x3;
				if (localX < 0 || localX >= 64 || localY < 0 || localY >= 64)
					continue;
				int objectPlane = plane;
				if (mapSettings != null && (mapSettings[1][localX][localY] & 2) == 2)
					objectPlane--;
				if (objectPlane < 0 || objectPlane >= 4 || plane < 0 || plane >= 4)
					continue;
				int id = objectId + (osrs ? Settings.OSRS_OBJECTS_OFFSET : 0);
				spawnObject(new WorldObject(id, type, rotation, localX + regionX*64, localY + regionY*64, objectPlane), objectPlane, localX, localY, true);
			}
		}
	}

	public int getRotation(int plane, int x, int y) {
		return 0;
	}

	/**
	 * Get's ground item with specific id on the specific location in this
	 * region.
	 */
	public FloorItem getGroundItem(int id, WorldTile tile, Player player) {
		if (groundItems == null)
			return null;
		FloorItem ironManItem = null;
		for (FloorItem item : groundItems) {
			if ((item.isInvisible()) && (item.hasOwner() && !player.getUsername().equals(item.getOwner())))
				continue;
			if (item.getId() == id && tile.getX() == item.getTile().getX() && tile.getY() == item.getTile().getY() && tile.getPlane() == item.getTile().getPlane()) {
				if ((player.isIronman() || player.isUltimateIronman() || player.isHCIronman()) && (item.hasOwner() && !player.getUsername().equals(item.getOwner()))) {
					ironManItem = item;
					continue;
				}
				/*if (player.isExtreme() && item.hasOwner() && !item.isExtreme()) {
					ironManItem = item;
					continue;
				}*/
				return item;
			}
		}
		if (ironManItem != null) {
		/*	if (player.isExtreme()) 
				player.getPackets().sendGameMessage("You can't pickup normal people items as an extreme player.");
			else*/
				player.getPackets().sendGameMessage("You can't pickup other people items as an ironman.");
		}
		return null;
	}

	/**
	 * Return's list of ground items that are currently loaded. List may be null
	 * if there's no ground items. Modifying given list is prohibited.
	 * 
	 * @return
	 */
	public List<FloorItem> getGroundItems() {
		return groundItems;
	}

	/**
	 * Return's list of ground items that are currently loaded. This method
	 * ensures that returned list is not null. Modifying given list is
	 * prohibited.
	 * 
	 * @return
	 */
	public List<FloorItem> getGroundItemsSafe() {
		if (groundItems == null)
			groundItems = new CopyOnWriteArrayList<FloorItem>();
		return groundItems;
	}

	public List<Integer> getPlayerIndexes() {
		return playersIndexes;
	}

	public int getPlayerCount() {
		return playersIndexes == null ? 0 : playersIndexes.size();
	}

	public List<Integer> getNPCsIndexes() {
		return npcsIndexes;
	}

	public void addPlayerIndex(int index) {
		// creates list if doesnt exist
		if (playersIndexes == null)
			playersIndexes = new CopyOnWriteArrayList<Integer>();
		if (playersIndexes.contains(index))
			return;
		playersIndexes.add(index);
	}

	public void addNPCIndex(int index) {
		// creates list if doesnt exist
		if (npcsIndexes == null)
			npcsIndexes = new CopyOnWriteArrayList<Integer>();
		if (npcsIndexes.contains(index))
			return;
		npcsIndexes.add(index);
	}

	public void removePlayerIndex(Integer index) {
		if (playersIndexes == null) // removed region example cons or dung
			return;
		playersIndexes.remove(index);
	}

	public boolean removeNPCIndex(Integer index) {
		if (npcsIndexes == null) // removed region example cons or dung
			return false;
		return npcsIndexes.remove(index);
	}

	public void loadMusicIds() {
		String[] musicNames = getMusicNames(regionId);
		if(musicNames.length == 0)
			return;
		musicIds = new int[musicNames.length];
		for(int i = 0; i < musicNames.length; i++)
			musicIds[i] = getMusicId(musicNames[i]);
	}

	public int getRandomMusicId() {
		if (musicIds == null)
			return -1;
		return musicIds[Utils.random(musicIds.length)];
	}
	
	public int[] getMusicIds() {
		return musicIds;
	}

	public int getLoadMapStage() {
		return loadMapStage;
	}

	public void setLoadMapStage(int loadMapStage) {
		this.loadMapStage = loadMapStage;
	}

	public boolean isLoadedObjectSpawns() {
		return loadedObjectSpawns;
	}

	public void setLoadedObjectSpawns(boolean loadedObjectSpawns) {
		this.loadedObjectSpawns = loadedObjectSpawns;
	}

	public boolean isLoadedNPCSpawns() {
		return loadedNPCSpawns;
	}

	public void setLoadedNPCSpawns(boolean loadedNPCSpawns) {
		this.loadedNPCSpawns = loadedNPCSpawns;
	}

	public int getRegionId() {
		return regionId;
	}

	/**
	 * 
			names.add("Way of the Wyrm");
			names.add("Ful to the Brim");
			names.add("Kanon of Kahlith");
	 * @param args
	 * @throws IOException
	 */
	//595, 635
	public static void main(String[] args) throws IOException {
		String name = "The Spurned Demon";
		Cache.init();
		
		Store osrsData = new Store(
				"C:\\Users\\alex\\Downloads\\OSRSCD_1.0.3\\data\\");
		
		int id = osrsData.getIndexes()[6].getArchiveId(name.toLowerCase().replace(" ", "_"));
		int id2 = osrsData.getIndexes()[6].getArchiveId(name.toLowerCase());
		System.out.println(id+", "+id2);
	}
	
	//use this one from nowon. 
	public static final String[] getMusicNames(int regionId) {
		
		List<String> names = new ArrayList<String>();
	
		switch(regionId) {
		case 23961:
			names.add("Bounty Hunter Level 1");
			names.add("Bounty Hunter Level 2");
			names.add("Bounty Hunter Level 3");
			break;
		case 15000:
		case 15256:
			names.add("The Everlasting Slumber");
			break;
		case 15515: //nightmare fight
			names.add("The Bane of Ashihama");
			break;
		case 21828:
		case 21829:
		case 22085:
		case 22084:
			names.add("Citadel Theme");
			break;
		case 10841:
			names.add("Ghost of Christmas Presents");
			break;
		case 16209:
		case 16465:
			names.add("Halloween Party");
			break;
		case 9891: //jormungand bridge
		case 9890:
		case 9635:
		case 9634:
			names.add("Lair of the Basilisk");
			break;
		case 9790: //island of stone
			names.add("Jaws of the Basilisk");
			break;
		case 6991:
			names.add("Dreamstate");
			break;
		case 12610:
			names.add("Safety in Numbers");
			break;
		case 12354:
			names.add("Incarceration");
			break;
		case 5279:
		case 5280:
		case 5536:
			names.add("Way of the Wyrm");
			//names.add("Ful to the Brim");
		//	names.add("Kanon of Kahlith");
			break;
		case 5435:
			names.add("The Forsaken Tower");
			break;
		case 5178:
			names.add("Getting Down to Business"); 
			break;
		case 5434:
			names.add("On the Frontline");
			break;
		case 4922:
			names.add("Hoe Down"); //enterinf farming guild
			names.add("A Farmer's Grind"); //west wing
			names.add("Grow Grow Grow"); //north farming guild
			break;
		case 4923:
		case 5179:
		case 5180:
			names.add("Burning Desire");
			break;
		case 5177:
		case 5433:
			names.add("Servants of Strife");
			break;
		case 5432:
			names.add("Gill Bill");
			break;
		case 4920:
		case 4664:
			names.add("Stuck in the Mire");
			break;
		case 4921:
			names.add("Newbie Farming");
			names.add("Stuck in the Mire");
			break;
		case 4665:
		case 4666:
			names.add("Newbie Farming");
			break;
		case 14386:
			names.add("Lament of Meiyerditch");
			break;
		case 14387:
			names.add("The Last Shanty");
			break;
		case 14642:
			names.add("Welcome to the Theatre");
			break;
		case 14161:
			names.add("The Phoenix");
			break;
		case 14679:
		case 14678:
		case 14934:
		case 14935: //vip zone
			names.add("A Familiar Feeling");
			break;
		case 8792:
			names.add("Go with the Flow");
			break;
		case 15148:
			names.add("Zombiism");
			break;
		case 11166:
			names.add("Romancing the Crone");
		case 9023:
			names.add("On Thin Ice");
			break;
		case 14142:
		case 14243:
		case 6223:
			names.add("The Forsaken");
			break;
		case 8332:
		case 8076:
		case 7821:
		case 7820:
		case 7564:
			names.add("Grumpy");
			break;
		case 9259:
			names.add("Mythical");
			break;
		case 9515:
		case 9771:
			names.add("On the Shore");
			break;
		case 14908:
		case 14652:
		case 14649:
		case 14907:
			names.add("Preservation");
			break;
		case 14650:
		case 14651:
		case 14906:
		case 15163:
		case 15162:
			names.add("Preserved");
			break;
		case 14495:
		case 14496:
			names.add("Fossilised");
			break;
		case 7228:
			names.add("Soul Fall");
			break;
		case 5430:
		case 5431:
		case 5176:
		case 5275:
			names.add("March of the Shayzien");
			break;
		case 6457:
			names.add("Kourend the Magnificent");
			//names.add("Halloween Party"); //hallowen event
			//names.add("Land of Snow"); //christmas event
			break;
		case 6968:
		case 6967:
		case 7223:
		case 7222:
		case 6711:
			names.add("Country Jig");
			break;
		case 6456:
		case 6966:
		case 5941:
		case 6198:
		case 6454:
		case 6197:
			names.add("The Forlorn Homestead");
			break;
		case 7226:
		case 7227:
		case 7225:
		case 6971:
		case 6970:
			names.add("Down by the Docks");
			break;
		case 5947:
		case 5946:
		case 6202:
		case 6203:
		case 5690:
		case 5691:
		case 5948:
		case 5692:
			names.add("Dwarven Domain");
			break;
		case 6461:
			names.add("The Doors of Dinh");
			break;
		case 4919:
			names.add("The Desolate Mage");
			break;
		case 5175:
			names.add("Ascent");
			break;
		case 6714:
		case 6715:
		case 6458:
		case 6459:
		case 6557:
		case 6556:
		case 6813:
		case 6812:
		case 6716:
			names.add("Arcane");
			break;
		case 18258:
			names.add("Mor Ul Rek");
			break;
		case 11088:
			names.add("Labyrinth");
			break;
		case 13200:
		case 10061:
		case 10317:
			names.add("The Depths");
			break;
		case 14932:
		case 15188:
			names.add("Monkey Badness");
			break;
		case 8023:
			names.add("Monkey Business");
			break;
		case 10035:
			names.add("Sad Meadow");
			break;
		case 8008:
			names.add("Fight or Flight");
			break;
		case 7752:
			names.add("Temple of Light");
			break;
		case 5140:
		case 4883:
		case 5395:
			names.add("Maws Jaws Claws");
			break;
		case 5139:
			names.add("That Sullen Hall");
			break;
		case 12106:
		case 11850:
		case 11851:
		case 12362:
		case 12363:
			names.add("Invader");
			break;
		case 9116:
			names.add("Troubled Waters");
			break;
		case 9619:
		case 9363:
			names.add("Devils May Care");
			break;
		case 9519:
			names.add("Romper Chomper");
			break;
		case 5943:
		case 5688:
			names.add("Rugged Terrain");
			break;
		case 5944:
		case 6200:
		case 5689:
			names.add("The Militia");
			break;
		case 8751:
			names.add("Thrall of the Serpent");
			break;
		case 9008:
			names.add("Coil");
			break;
		case 12961:
			names.add("Scorpia Dances");
			break;
		case 12343:
			names.add("Lightness");
		case 13358:
			names.add("Dynasty");
			break;
		case 13458:
			names.add("Bone Dry");
			break;
		case 9273:
			names.add("Making Waves");
			break;
		case 10394:
			names.add("Oriental");
			break;
		case 10038:
			names.add("Voyage");
			break;
		case 10037:
			names.add("Waterfall");
			break;
		case 5961:
			names.add("Fight of the Dwarves");
			break;
		case 12109:
			names.add("The Rogues' Den");
			break;
		case 6741:
			names.add("Second Vision");
			break;
		case 17246: //toplvl runespan
		case 17247:
		case 17502:
		case 17503:
			names.add("Runecalm");
			names.add("Runefear");
			break;
		case 16734: //second lvl runespan
		case 16735:
		case 16478:
		case 16479:
			names.add("Runeiverse");
			names.add("Runenergy");
			names.add("Runespan");
			names.add("Runesphere");
			names.add("Runetine");
			break;
		case 15966: //first lvl runespan
		case 15967:
		case 15710:
		case 15711:
			names.add("Runearia");
			names.add("Runefire");
			names.add("Runescar");
			names.add("Runesque");
			names.add("Runeward");
			names.add("Runewaste");
			names.add("Runewrath");
			break;
		case 12337:
		case 12437:
			names.add("Vision");
			break;
		case 12176: //jakindo lair
			names.add("Natural Selection");
			break;
		case 10803: //witchaven
			names.add("The Mollusc Menace");
			break;
		case 11562:
			names.add("Marooned");
			break;	
		case 10795:
			names.add("Monkey Madness");
			break;	
		case 11051:
			names.add("Monkey Madness");
			names.add("Temple");
			break;	
		case 11050:
		case 10794:
			names.add("Island Life");
			break;	
		case 11056:
			names.add("Jungle Community");
			break;	
		case 11312:
			names.add("Jungle Troubles");
			break;	
		case 11311:
			names.add("Tribal");
			break;	
		case 11054:
			names.add("Jungly1");
			break;	
		case 11055:
			names.add("Jungly3");
			break;	
		case 13099:
			names.add("Sphinx");
			break;
			case 13356:
				names.add("Sunburn");
				break;
			case 12846:
			names.add("Sunburn");
			break;
			case 12845:
			names.add("Scarab");
			break;
			case 12945:
			names.add("Sarcophagus");
			break;
			case 9270:
			names.add("Eagle Peak");
			break;
			case 9527:
			case 9528:
			case 9272:
			case 9271:
			case 9016:
			names.add("On the Wing");
			break;
			case 10810:
			case 10811:
			names.add("Poles Apart");
			break;
			case 11060:
			case 11316:
			names.add("Background");
			break;
			case 11310:
			names.add("Ambient Jungle");
			break;
			case 10833:
			names.add("Dorgeshuun Deep");
			break;
			
			case 11822:
				names.add("Tribal2");
				break;	
			case 9782:
				names.add("Gnome King");
			break;
			case 11423:
			case 11422:
				names.add("Land of the Dwarves");
			break;
			case 11679: 
				names.add("Tale of Keldagrim");
			break;
			case 11678:  ///kdalgrim train
				names.add("Slice of Station");
				break;
			case 6743: //fist of guthix outside
				names.add("Waiting for the Hunt");
				break;
			case 8781: //burthop games room
				names.add("Competition");
			break;
			case 9777: //observatory
				names.add("Serenade");
				break;
			case 9778: //unholy altar
				names.add("Expecting");
				break;
			case 13138: //meeting story / dz
			case 13393:
				names.add("Ardougne Ago");
			break;
			case 11801: //exiled kalphite lair
				names.add("Coleoptera");
				break;
			case 11578:
			case 11323:
				names.add("Frostbite");
				break;
			case 15173:
				names.add("Faces Obscura");
				break;
			case 14917:
				names.add("Dying Light");
				break;
			case 14916:
				names.add("Cliffhanger");
				break;
			case 15172:
				names.add("Empyrean Citadel");
				break;
			case 16738: //staff zone custom
				names.add("End Song");
				break;
			case 8244:
			case 8499:
			case 8500:
			case 8501:
			case 8755:
			case 8756:
			case 8757:
			case 9011:
			case 9012:
			case 9013:
			case 12995:
			case 12126:
			case 12127:
				/*names.add("Elven Daffodil");
				names.add("Elven Dhalia");
				names.add("Elven Heart");
				names.add("Elven Holly");
				names.add("Elven Sunlight");
				names.add("Elven Nightshade");
				names.add("Elven Sunrise");
				names.add("Elven Sunset");
				names.add("Elven Voice");
				names.add("Elven Rose");
				names.add("Elven Snapegrass");
				names.add("Elven Bluebell");
				names.add("Elven Lily");
				names.add("The Twilight Twain");
				names.add("Henceward!");
				names.add("Baxtorian's Hollow");
				names.add("Among Tirannwn Trees");*/
				
				names.add("Crystal Castle");
				names.add("Dance of the Meilyr");
				names.add("Faith of the Hefin");
				names.add("The Tower of Voices");
				names.add("Mystics of Nature");
				names.add("Architects of Prifddinas");
				names.add("The Seed of Crwys");
				//Elven Guardians missing id
				//Iorwerth's Lament missing id
				break;
			case 13250:
				names.add("Trahaearn Toil");
				break;
			case 12994:
			case 12993:
			case 12738:
				names.add("Sharp End of the Crystal");
				break;
			case 9266: // Underground Pass exit
				names.add("Breeze");
				names.add("Elven Mist");
				break;
			case 9779:
				names.add("Underground Pass");
				break;
			case 9291:
				names.add("Cursed");
				break;
		}
		
		
		String name1 = getMusicName1(regionId);
		if(name1 != null)
			names.add(name1);
		String name2 = getMusicName2(regionId);
		if(name2 != null)
			names.add(name2);
		String name3 = getMusicName3(regionId);
		if(name3 != null)
			names.add(name3);
		
		return names.toArray(new String[names.size()]);
		
	}
	
	
	public static final String getMusicName3(int regionId) {
		switch (regionId) {
		case 8252: //lunar isle
		case 8253:
			return "The Lunar Isle";
		case 8508: //lunar isle
		case 8509:
			return "Isle of Everywhere";
		case 9377: //lunar isle dungeon
			return "Way of the Enchanter";
		case 13152: // crucible
			return "Steady";
		case 13151: // crucible
			return "Hunted";
		case 12895: // crucible
			return "Target";
		case 12896: // crucible
			return "I Can See You";
		case 11575: // burthope
			return "Spiritual";
		case 18512:
		case 18511:
		case 19024:
			return "Tzhaar City III";
	/*	case 18255: // fight pits
			return "Tzhaar Supremacy III";*/
		case 14948:
			return "Dominion Lobby III";
		default:
			return null;
		}
	}

	public static final String getMusicName2(int regionId) {
		switch (regionId) {
		case 12850: // lumbry castle
			return "The Duke";
		case 13152: // crucible
			return "I Can See You";
		case 13151: // crucible
			return "You Will Know Me";
		case 12895: // crucible
			return "Steady";
		case 12896: // crucible
			return "Hunted";
		case 12853:
			return "Cellar Song";
		case 11573: // taverley
		case 11572:
			return "Taverley Enchantment";
		case 11575: // burthope
			return "Taverley Adventure";
			/*
			 * kalaboss
			 */
		case 13626:
		case 13882:
			return "Born to Do This";
		case 13368: //kalaboss wild
			return "Undercurrent";
		case 13116: //wilderness demonic ruins
			return "Scape Sad";
			/*
			 * kalaboss
			 */
		case 13625:
		case 13627:
		case 13881:
			return "Daemonheim Fremenniks";
		case 18512:
		case 18511:
		case 19024:
			return "Tzhaar City II";
	/*	case 18255: // fight pits
			return "Tzhaar Supremacy II";*/
		case 14948:
			return "Dominion Lobby II";
		default:
			return null;
		}
	}

	public static final String getMusicName1(int regionId) {
		switch (regionId) {
		case 9265: //ltdya
			return "Far Away";
		case 9009: //south east isafdar
			return "Forest";
		case 8001: //poison waste slayer dungeon
			return "Waste Defaced";
		case 9007:
		case 9264:
			return "Lost Soul"; 
		case 8496: //port tyras
		case 8497:
			return "Riverside";
		case 8752: //south of tyras camp
			return "Exposed";
		case 8753: //tyras camp
			return "Meridian";
		case 9010: //south east prifinas
			return "Crystal Castle";
		case 8498: //south west prifinas
			return "Everywhere";
		case 8754: //elf camp
			return "Woodland";
		case 9523: //arandar
		case 9267:
		case 9268:
			return "Overpass";
		case 10042: //waterbird island
			return "The Desolate Isle";
		case 9886:
			return "The Monsters Below";
		case 10142:
		case 6298: //wc guild ent
			return "Subterranea";
		case 10144:
			return "Corridors of Power";
		case 10400:
			return "Slither and Thither";
		case 10300:
			return "Etcetera";
		case 10044:
			return "Miscellania";
		case 7236:
		case 7748:
		case 7492:
			return "Xenophobe";
		case 11589:
			return "Dagannoth Dawn";
		case 10835:
			return "Dorgeshuun City";
		case 10834:
			return "Dorgeshuun Deep";
		case 12601:
		case 13112:
		case 13669:
			return "Dark";
		case 13113:
			return "Shining";
		case 12857:
			return "Dead Can Dance";
		case 12093:
			return "Pirates of Peril";
		case 13874:
			return "Distant Land";
		case 13614:
			return "Desert Heat";
		case 13613:
			return "Over To Nardah";
		case 13872:
		case 13873:
			return "The Golem";
		case 13871:
			return "Kharidian Nights";
		case 10537: //pest control island
			return "Null and Void";
		case 6995: //ancient cavern
		case 6994:
			return "Barb Wire";
		case 6482: //kuradal dungeon
			return "Final Destination";
		case 9526: //gnome ball field
			return "Gnomeball";
		case 10034: //Battlefield north of Tree Gnome Village., 
			return "Attack1";
		case 10033: //Tree Gnome Village
			return "Emotion";
		case 11418: //Dwarven tunnel under Wolf Mountain
			return "Beyond";
		case 8774: //taverly slayer dungeon
			return "Taverley Lament";
		case 11576:
			return "Kingdom";
		case 11320:
			return "Tremble";
		case 12616: //tarns lair
			return "Undead Dungeon";
		case 10388:
			return "Cavern";
		case 12107:
			return "Into the Abyss";
		case 11164:
			return "The Slayer";
		case 10908:
		case 10907:
			return "Masquerade";
		case 4707:
		case 4451:
		case 5221:
		case 5220:
		case 5219:
		case 4453:
		case 4709:
			return "Hunting Dragons";
		case 12115:
			return "Dimension X";
		case 8527: //braindeath island
			return "Aye Car Rum Ba";
		case 8528: //braindeath mountain
			return "Blistering Barnacles";
		case 13206: //goblin mines under lumby
			return "The Lost Tribe";
		case 12949:
		case 12950:
		case 12693:
			return "Cave of the Goblins";
		case 12948:
			return "The Power of Tears";
		case 11416: //dramen tree
		case 12958: //wild godwars dung
			return "Underground";
		case 14638: //mosleharms
			return "In the Brine";
		case 14637:
		case 14894:
			return "Life's a Beach!";
		case 14494: //mosleharms cave
			return "Little Cave of Horrors";
		case 11673: //taverly dungeon musics
			return "Courage";
		case 11672:
			return "Dunjun";
		case 11417:
			return "Arabique";
		case 11671:
			return "Royale";
		case 13977:
			return "Stillness";
		case 13622:
			return "Morytania";
		case 13722:
			return "Mausoleum";
		case 10906:
			return "Twilight";
		case 12181: //Asgarnian Ice Dungeon's wyvern area
			return "Woe of the Wyvern";
		case 11925: //Asgarnian Ice Dungeon
			return "Starlight";
		case 13617: //abbey
			return "Citharede Requiem";
		case 13361: //desert verms
			return "Valerio's Song";
		case 13910: //The Tale of the Muspah cave entrance
		case 13654:
			return "Rest for the Weary";
		case 13656: //The Tale of the Muspah cave ice verms area
			return "The Muspah's Tomb";
		case 11057: //brimhaven and arroundd
			return "High Seas";
		case 10802:
			return "Jungly2";
		case 10801:
			return "Landlubber";
		case 11058:
			return "Jolly-R";
		case 10901: //brimhaven dungeon entrance
			return "Pathways";
		case 10645: //brimhaven dungeon
		case 10644:
		case 10900:
			return "7th Realm";
		case 11315: //crandor
		case 11314:
			return "The Shadow";
		case 11414: //karanja underground
			return "Attack2";
		case 11413:
			return "Dangerous Road";
		case 7505: //strongholf of security war
			return "Dogs of War";
		case 8017: //strongholf of security famine
			return "Food for Thought";
		case 8530: //strongholf of security pestile
			return "Malady";
		case 9297: //strongholf of security death
			return "Dance of Death";
		case 10040:
			return "Lighthouse";
		case 10140: // inside lighthouse
			return "Out of the Deep";
		case 9797:
			return "Crystal Cave";
		case 9541:
			return "Faerie";
		case 11927: // gamers grotto
			return "Cave Background";
		case 14646:// Port Phasmatys
			return "The Other Side";
		case 14746:// Ectofuntus
			return "Phasmatys";
		case 14747:// Port Phasmatys brewery
			return "Brew Hoo Hoo";
		case 13152: // crucible
			return "Hunted";
		case 13151: // crucible
			return "Target";
		case 12895: // crucible
			return "I Can See You";
		case 12896: // crucible
			return "You Will Know Me";
		case 12597:
			return "Spirit";
		case 13109:
			return "Medieval";
		case 13110:
			return "Honkytonky Parade";
		case 10658:
			return "Espionage";
		case 13899: // water altar
			return "Zealot";
		case 10039:
			return "Legion";
		case 11319: // warriors guild
			return "Warriors' Guild";
		case 11575: // burthope
			return "Spiritual";
		case 11573: // taverley
		case 11752:
			return "Taverley Ambience";
		case 7473:
			return "The Waiting Game";
		case 18512:
		case 18511:
		case 19024:
			return "Tzhaar City I";
		case 18255: // fight pits
			return "Fire and Brimstone";
		/*	return "Tzhaar Supremacy I";*/
		case 14672:
		case 14671:
		case 14415:
		case 14416:
			return "Living Rock";
		case 11157: // Brimhaven Agility Arena
			return "Aztec";
		case 15446:
			return "The Pact";
		case 15957:
		case 15958:
			return "Dead and Buried";
		case 12848:
			return "Arabian3";
		case 12954:
		case 12442:
		case 12441:
			return "Scape Cave";
		case 12185:
		case 11929:
			return "Dwarf Theme";
		case 12184:
			return "Workshop";
		case 6992:
		case 6993: // mole lair
			return "The Mad Mole";
		case 9776: // castle wars
			return "Melodrama";
		case 10029:
		case 10285:
			return "Jungle Hunt";
		case 14231: // barrows under
			return "Dangerous Way";
		case 12856: // chaos temple
			return "Faithless";
		case 13104:
		case 12847: // arround desert camp
		case 13359:
		case 13102:
			return "Desert Voyage";
		case 13103:
			return "Lonesome";
		case 12589: // granite mine
		case 12591:
			return "The Desert";
		case 18517: //polipore dungeon
		case 18516:
		case 18773:
		case 18775:
		case 13407: // crucible entrance
		case 13360: // dominion tower outside
			return "";
		case 14948:
			return "Dominion Lobby I";
		case 11836: // lava maze near kbd entrance
		case 12192:
		case 12193:
			return "Attack3";
		case 11834: //forgotten cimitery
			return "Wilderness3";
		case 12091: // lava maze west
		case 12347: //lava maze south
		case 12859:
		case 12603:
			return "Wilderness2";
		case 12092: // lava maze north
			return "Wild Side";
		case 9781:
			return "Gnome Village";
		case 11339: // air altar
			return "Serene";
		case 11083: // mind altar
			return "Miracle Dance";
		case 10827: // water altar
			return "Zealot";
		case 10571: // earth altar
			return "Down to Earth";
		case 10315: // fire altar
			return "Quest";
		case 8523: // cosmic altar
			return "Stratosphere";
		case 9035: // chaos altar
			return "Complication";
		case 8779: // death altar
			return "La Mort";
		case 10059: // body altar
			return "Heart and Mind";
		case 9803: // law altar
			return "Righteousness";
		case 9547: // nature altar
			return "Understanding";
		case 9804: // blood altar
		case 14900:
		case 14899:
		case 14643:
			return "Bloodbath";
		case 13107:
			return "Arabian2";
		case 13105:
		case 13106:
			return "Al Kharid";
		case 12342: // edge
			return "Forever";
		case 10806:
			return "Overture";
		case 10899:
			return "Karamja Jam";
		case 13623:
			return "The Terrible Tower";
		case 12374:
			return "The Route of All Evil";
		case 12630:
		case 12629:
			return "The Route of the Problem";
		case 12885:
		case 12886:
		case 13141:
		case 13142:
			return "The Route of All Evil";
		case 9802:
			return "Undead Dungeon";
		case 8763: //pirate cove
			return "The Galleon";
		case 10809: // east rellekka
			return "Borderland";
		case 10553: // Rellekka
		case 10554:
		case 10297:
			return "Rellekka";
		case 10552: // south
			return "Saga";
		case 10296: // south west
			return "Lullaby";
		case 10828: // south east
			return "Legend";
		case 9275:
			return "Volcanic Vikings";
		case 11061:
		case 11317:
			return "Fishing";
		case 9551: case 11807:
			return "TzHaar!";
		case 12345:
			return "Eruption";
		case 12090: //west <-
			return "Gaol";
		case 12089:
		case 12446:
		case 12445:
		case 13114:
		case 11833:
			return "Wilderness";
		case 12343:
			return "Dangerous";
		case 14131:
			return "Dance of the Undead";
		case 11588:
			return "The Vacant Abyss";
		case 11844:
			return "Bane of Summer";
		case 13363: // duel arena hospital
			return "Shine";
		case 13362: // duel arena
			return "Duel Arena";
		case 12082: // port sarim
			return "Sea Shanty2";
		case 12081: // port sarim south
			return "Tomorrow";
		case 11602:
			return "Strength of Saradomin";
		case 12590:
			return "Bandit Camp";
		case 10329:
			return "The Sound of Guthix";
		case 9033:
			return "Attack5";
			// godwars
		case 11603:
			return "Zamorak Zoo";
		case 11346:
			return "Armadyl Alliance";
		case 11347:
			return "Armageddon";
			// black kngihts fortess
		case 12086:
			return "Knightmare";
			// tzaar
		case 9552:
			return "Fire and Brimstone";
			// kq
		case 13972:
			return "Insect Queen";
			// clan wars free for all:
		case 11094:
		case 11862:
			return "Clan Wars";
			/*
			 * tutorial island
			 */
		case 12336:
			return "Newbie Melody";
			//dark warrior fortress
		case 12088:
			return "Army of Darkness";
			/*
			 * darkmeyer
			 */
		case 14644:
		case 14388:
			return "Darkmeyer";
		case 12183:
			return "Metalwork";
			/*
			 * Lumbridge, falador and region.
			 */
		case 11574: // heroes guild
			return "Splendour";
		case 12851:
			return "Autumn Voyage";
		case 12861:
			return "Venomous";
		case 12338: // draynor and market
			return "Unknown Land";
			
		case 12339: // draynor up
			return "Start";
		case 12340: // draynor mansion
			return "Spooky";
		case 12850: // lumbry castle
			return "Harmony";
		case 12849: // east lumbridge swamp
			return "Yesteryear";
		case 12593: // at Lumbridge Swamp.
			return "Book of Spells";
		case 12594: // on the path between Lumbridge and Draynor.
			return "Dream";
		case 12595: // at the Lumbridge windmill area.
			return "Flute Salad";
		case 12854: // at Varrock Palace.
			return "Adventure";
		case 12853: // at varrock center
			return "Garden";
		case 12852: // varock mages
			return "Expanse";
		case 13108:
			return "Still Night";
		case 12083:
			return "Wander";
		case 11828:
		case 12084:
			return "Fanfare";
		case 11829:
			return "Scape Soft";
		case 11577:
			return "Mad Eadgar";
		case 10293: // at the Fishing Guild.
			return "Mellow";
		case 11837: //wild agility course
			return "Deep Wildy";
		case 13117: //rouges castle
			return "Regal";
		case 11824:
			return "Mudskipper Melody";
		case 11570:
			return "Wandar";
		case 12341:
			return "Barbarianims";
		case 12855:
			return "Crystal Sword";
		case 12344:
			return "Wildwood";
		case 12599:
			return "Doorways";
		case 11832: //west of dark warrior fort
			return "Close Quarters";
		case 12598:
			return "The Trade Parade";
		case 11318:
		case 11419:
			return "Ice Melody";
		case 12600:
			return "Moody";
		case 12605:
			return "Scape Wild";
		case 13372:
			return "Witching";
		case 13373:
			return "Everlasting Fire";
		case 10032: // west yannile:
			return "Big Chords";
		case 10288: // east yanille
			return "Magic Dance";
		case 11826: // Rimmington
			return "Long Way Home";
		case 11825: // rimmigton coast
			return "Attention";
		case 11827: // north rimmigton
			return "Nightfall";
			/*
			 * Camelot and region.
			 */
		case 11062:
		case 10805:
			return "Camelot";
		case 10550:
			return "Talking Forest";
		case 10549:
			return "Lasting";
		case 10548:
			return "Wonderous";
		case 10547:
			return "Baroque";
		case 10290:
			return "Ballad of Enchantment";
		case 10291:
			return "Knightly";
		case 10292:
			return "The Tower";
		case 11571: // crafting guild
			return "Miles Away";
		case 11595: // ess mine
			return "Rune Essence";
		case 10294:
			return "Theme";
		case 12349:
			return "Mage Arena";
		case 13365: // digsite
			return "Venture";
		case 13364: // exams center
			return "Medieval";
		case 13878: // canifis
			return "Village";
		case 13877: // canafis south
			return "Waterlogged";
			/*
			 * Mobilies Armies.
			 */
		case 9516:
			return "Command Centre";
		case 12596: // champions guild
			return "Greatness";
		case 10804: // legends guild
			return "Trinity";
		case 11601:
			return "Zaros Zeitgeist"; // zaros godwars
		case 12633: //digsite zaros
			return "Zaros Stirs";
		case 13626:
		case 13882:
			return "Daemonheim Entrance";
		default:
			return null;
		}
	}
	
	//1653 similar to love
	public static final int getMusicId(String musicName) {
		if (musicName == null)
			return -1;
		if (musicName.equals(""))
			return -2;
		int id = (int) ClientScriptMap.getMap(1345).getKeyForValue(musicName);
		if (id == -1) {
			id = -Cache.STORE.getIndexes()[6].getArchiveId(musicName.toLowerCase().replace(" ", "_"));
			if (id == 1)
				id = -Cache.STORE.getIndexes()[6].getArchiveId(musicName.toLowerCase());
			if (id == 1) {
				if (musicName.equalsIgnoreCase("the militia"))
					id = -1649;
				else if (musicName.equalsIgnoreCase("invader"))
					id = -1641;
				else if (musicName.equalsIgnoreCase("monkey badness"))
					id = -1668;
				else if (musicName.equalsIgnoreCase("monkey business"))
					id = -1667;
				else if (musicName.equalsIgnoreCase("mor ul rek"))
					id = -1702;
				else if (musicName.equalsIgnoreCase("arcane"))
					id = -1655;
				else if (musicName.equalsIgnoreCase("Country Jig"))
					id = -1656;
				else if (musicName.equalsIgnoreCase("The Desolate Mage"))
					id = -1695;
				else if (musicName.equalsIgnoreCase("The Doors of Dinh"))
					id = -1677;
				else if (musicName.equalsIgnoreCase("Down by the Docks"))
					id = -1650;
				else if (musicName.equalsIgnoreCase("The Forlorn Homestead"))
					id = -1652;
				else if (musicName.equalsIgnoreCase("Dwarven Domain"))
					id = -1646;
				else if (musicName.equalsIgnoreCase("March of the Shayzien"))
					id = -1688;
				else if (musicName.equalsIgnoreCase("Soul Fall"))
					id = -1644;
				else if (musicName.equalsIgnoreCase("Preservation"))
					id = -1712;
				else if (musicName.equalsIgnoreCase("Preserved"))
					id = -1713;
				else if (musicName.equalsIgnoreCase("Fossilised"))
					id = -1714;
				else if (musicName.equalsIgnoreCase("Mythical"))
					id = -1750;
				else if (musicName.equalsIgnoreCase("On Thin Ice"))
					id = -1736;
				else if (musicName.equalsIgnoreCase("The Forsaken"))
					id = -1727;
				else if (musicName.equalsIgnoreCase("Tempest"))
					id = -1718;
				else if (musicName.equalsIgnoreCase("Fire in the Deep"))
					id = -1693;
				else if (musicName.equalsIgnoreCase("Welcome to the Theatre"))
					id = -1756;
				else if (musicName.equalsIgnoreCase("The Maiden's Sorrow"))
					id = -1770;
				else if (musicName.equalsIgnoreCase("The Maiden's Anger"))
					id = -1769;
				else if (musicName.equalsIgnoreCase("Welcome to my Nightmare"))
					id = -1778;
				else if (musicName.equalsIgnoreCase("The Nightmare Continues"))
					id = -1771;
				else if (musicName.equalsIgnoreCase("Dance of the Nylocas"))
					id = -1780;
				else if (musicName.equalsIgnoreCase("Arachnids of Vampyrium"))
					id = -1779;
				else if (musicName.equalsIgnoreCase("The Dark Beast Sotetseg"))
					id = -1784;
				else if (musicName.equalsIgnoreCase("Power of the Shadow Realm"))
					id = -1781;
				else if (musicName.equalsIgnoreCase("Predator Xarpus"))
					id = -1767;
				else if (musicName.equalsIgnoreCase("Last King of the Yarasa"))
					id = -1764;
				else if (musicName.equalsIgnoreCase("It's not over 'til..."))
					id = -1766;
				else if (musicName.equalsIgnoreCase("The Fat Lady Sings"))
					id = -1772;
				else if (musicName.equalsIgnoreCase("The Curtain Closes"))
					id = -1782;
				else if (musicName.equalsIgnoreCase("Servants of Strife"))
					id = -1828;
				else if (musicName.equalsIgnoreCase("A Farmer's Grind"))
					id = -1809;
				else if (musicName.equalsIgnoreCase("On the Frontline"))
					id = -1805;
				else if (musicName.equalsIgnoreCase("The Forsaken Tower"))
					id = -1824;
				else if (musicName.equalsIgnoreCase("Alchemical Attack!"))
					id = -1798;
				else if (musicName.equalsIgnoreCase("The Seed of Crwys"))
					id = -1859;
				else if (musicName.equalsIgnoreCase("Mystics of Nature"))
					id = -1855;
				else if (musicName.equalsIgnoreCase("Architects of Prifddinas"))
					id = -1851;
				else if (musicName.equalsIgnoreCase("Sharp End of the Crystal"))
					id = -1857;
				else if (musicName.equalsIgnoreCase("Trahaearn Toil"))
					id = -1849;
				else if (musicName.equalsIgnoreCase("The Everlasting Slumber"))
					id = -1864;
				else if (musicName.equalsIgnoreCase("The Bane of Ashihama"))
					id = -1863;
				else if (musicName.equalsIgnoreCase("The Spurned Demon"))
					id = -1862;
				
				
			}
			if (id < 0 && !MusicsManager.OSRS_MUSIC_NAMES.containsKey(id))
				MusicsManager.OSRS_MUSIC_NAMES.put(id, musicName);
		}
		return id;
	}

	public boolean isLoadedItemSpawns() {
		return loadedItemSpawns;
	}

	public void setLoadedItemSpawns(boolean loadedItemSpawns) {
		this.loadedItemSpawns = loadedItemSpawns;
	}

	/**
	 * @return the osrs
	 */
	public boolean isOsrs() {
		return osrs;
	}

	/**
	 * @param osrs the osrs to set
	 */
	public void setOsrs(boolean osrs) {
		this.osrs = osrs;
	}
}