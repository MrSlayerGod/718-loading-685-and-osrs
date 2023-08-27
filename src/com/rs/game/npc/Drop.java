package com.rs.game.npc;

import com.rs.game.item.Item;
import com.rs.utils.Utils;

public class Drop {

	private int itemId, minAmount, maxAmount, rarity;

	public Drop(int itemId, int minAmount, int maxAmount) {
		this(itemId, minAmount, maxAmount, Drops.ALWAYS);
	}
	
	public Drop(int itemId, int minAmount, int maxAmount, int rarity) {
		this.itemId = itemId;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.rarity = rarity;
	}

	public int getMinAmount() {
		return minAmount;
	}

	public int getExtraAmount() {
		return maxAmount - minAmount;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	public int getItemId() {
		return itemId;
	}
	
	public int getRarity() {
		return rarity;
	}

	public Item getItem() {
		return new Item(this.itemId, Utils.random(this.minAmount, this.maxAmount));
	}

}
