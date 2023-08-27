package com.rs.game.item;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;

public class FloorItem extends Item {

	private static final long serialVersionUID = -2287633342490535089L;


	private boolean chambers;
	private WorldTile tile;
	private String ownerName;
	// 0 visible, 1 invisible, 2 visible and reappears 30sec after taken
	private int type;
	private boolean extreme;

	public FloorItem(int id) {
		super(id);
	}

	public void setChambers() {
		chambers = true;
	}
	public boolean isChambers() {
		return chambers;
	}

	@Override
	public Item setAmount(int amount) {
		this.amount = amount;
		return this;
	}

	public FloorItem(Item item, WorldTile tile, Player owner, boolean underGrave, boolean invisible) {
		super(item.getId(), item.getAmount());
		this.tile = tile;
		if (owner != null) {
			this.ownerName = owner.getUsername();
			this.extreme = owner.isDeadman();
		}
		this.type = invisible ? 1 : 0;
	}

	@Deprecated
	public FloorItem(Item item, WorldTile tile, boolean appearforever) {
		super(item.getId(), item.getAmount());
		this.tile = tile;
		this.type = appearforever ? 2 : 0;
	}

	public WorldTile getTile() {
		return tile;
	}

	public boolean isInvisible() {
		return type == 1;
	}

	public boolean isForever() {
		return type == 2;
	}

	public String getOwner() {
		return ownerName;
	}

	public boolean hasOwner() {
		return ownerName != null;
	}
	
	public boolean isExtreme() {
		return extreme;
	}

	public void setInvisible(boolean invisible) {
		type = invisible ? 1 : 0;
	}

}
