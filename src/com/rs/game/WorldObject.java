package com.rs.game;

import com.rs.cache.loaders.ObjectConfig;
import com.rs.game.player.Player;

import java.util.List;

@SuppressWarnings("serial")
public class WorldObject extends WorldTile {

	private int id;
	private int type;
	private int rotation;
	private int life;

	public WorldObject(int id, int type, int rotation, WorldTile tile) {
		super(tile.getX(), tile.getY(), tile.getPlane());
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = 1;
	}

	public WorldObject(int id, int type, int rotation, int x, int y, int plane) {
		super(x, y, plane);
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = 1;
	}

	public WorldObject(int id, int type, int rotation, int x, int y, int plane, int life) {
		super(x, y, plane);
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = life;
	}

	public WorldObject(WorldObject object) {
		super(object.getX(), object.getY(), object.getPlane());
		this.id = object.id;
		this.type = object.type;
		this.rotation = object.rotation;
		this.life = object.life;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getRotation() {
		return rotation;
	}

	public WorldObject setRotation(int rotation) {
		this.rotation = rotation;
		return this;
	}

	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		this.life = life;
	}

	public void decrementObjectLife() {
		this.life--;
	}

	public ObjectConfig getDefinitions() {
		return ObjectConfig.forID(id);
	}

	public WorldObject setId(int id) {
		this.id = id;
		return this;
	}

	public void updateId(int newId) {
		if(newId == -1) {
			World.removeObject(this);
		} else {
			id = newId;
			WorldObject obj = new WorldObject(newId, getType(), getRotation(),
					getX(), getY(), getPlane());
			World.spawnObject(obj);
		}
	}
	public void setType(int type) {
		this.type = type;
	}

    public void remove() {
		World.removeObject(this);
		removed = true;
    }

    boolean removed = false;

    public boolean isRemoved() {
    	return removed;
		//return World.getObjectWithId(this, this.type) != null;
    }

    public void anim(int anim) {
		World.sendObjectAnimation(this, new Animation(anim));
    }

    public WorldObject copy() {
    	return new WorldObject(id, type, rotation, clone());
    }
}
