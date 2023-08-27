package com.rs.game.item;

import java.io.Serializable;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.player.content.custom.CustomItems;
import com.rs.utils.Utils;

public class Item implements Serializable {

	private static final long serialVersionUID = -6485003878697568087L;

	private int newId;
	private short id;
	protected int amount;
	//protected int degrade;

	public int getId() {
		check();
		return newId != 0 ? newId : id;
	}

	@Override
	public Item clone() {
		return new Item(newId, amount);
	}
	
	public Item(int id) {
		this(id, 1);
	}

	public Item(int id, int amount) {
		this(id, amount, -1);
	}

	public Item(int id, int amount, int degrade) {
		this(id, amount, degrade, false);
	}

	public Item(int id, int amount, int degrade, boolean amt0) {
		this.newId = id;
		this.amount = amount;
		//this.degrade = degrade;
		if (this.amount <= 0 && !amt0) {
			this.amount = 1;
		}
	}

	public Item(Item item) {
		this.newId = item.getId();
		this.amount = item.getAmount();
	//	this.degrade = item.getDegrade();
	}
	
	public void check() {
		newId = CustomItems.getNonLuckyID(newId);
	}

	public ItemConfig getDefinitions() {
		return ItemConfig.forID(getId());
	}

	public Item setAmount(int amount) {
		this.amount = amount;
		return this;
	}
	
//	public void setDegrade(int degrade) {
	//	this.degrade = degrade;
	//}

	public void setId(int id) {
		this.newId = id;
	}

	public int getAmount() {
		return amount;
	}
	
	//public int getDegrade() {
	//	return degrade;
	//}

	public String getName() {
		return getDefinitions().getName();
	}
	
	@Override
	public String toString() {
		return "Item (" + newId + ", " + amount + ")";
	}

	public boolean isNoted() {
		ItemConfig config = ItemConfig.forID(newId);
		return getDefinitions().isNoted() && config.getCertId() != -1;
	}

	public boolean hasNote() {
		return ItemConfig.forID(newId).isNoted();
	}

	public int getNotedId() {
		return ItemConfig.forID(newId).getCertId();
	}

    public String amtAndName() {
		int id = newId > 0 ? newId : this.id;
		return String.format("%s x %s", Utils.getFormattedNumber(getAmount()), getName());
    }

	// used for item actions
    private int fromSlot = -1;

	public int getFromSlot() {
		return fromSlot;
	}

	public void setFromSlot(int fromSlot) {
		this.fromSlot = fromSlot;
	}
}
