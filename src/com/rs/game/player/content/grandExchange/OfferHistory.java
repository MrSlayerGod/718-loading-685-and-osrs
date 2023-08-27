package com.rs.game.player.content.grandExchange;

import java.io.Serializable;

public class OfferHistory implements Serializable {

	private static final long serialVersionUID = 7322642705393018764L;

	private String owner;
	private int id, quantity, price;
	private boolean bought;

	public OfferHistory(int id, int quantity, int price, boolean bought) {
		this(null, id, quantity, price, bought);
	}
	
	public OfferHistory(String owner, int id, int quantity, int price, boolean bought) {
		this.owner = owner;
		this.id = id;
		this.quantity = quantity;
		this.price = price;
		this.bought = bought;
	}

	public int getId() {
		return id;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getPrice() {
		return price;
	}

	public boolean isBought() {
		return bought;
	}
	
	public String getOwner() {
		return owner;
	}
}
