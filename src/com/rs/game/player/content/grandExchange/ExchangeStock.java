package com.rs.game.player.content.grandExchange;

import java.io.Serializable;

import com.rs.game.player.content.VirtualValues.VirtualStock;
import com.rs.utils.Utils;

public class ExchangeStock implements Serializable {
	
	private static final long serialVersionUID = -4063383396423871324L;
	
	private final int id;
	private int stock;
	private long timer;
	
	public ExchangeStock(int id, VirtualStock vs) {
		this.id = id;
		setStock(vs.getCap());
		setTimer(Utils.currentTimeMillis() + vs.getTimer());
	}

	public int getId() {
		return id;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public long getTimer() {
		return timer;
	}

	public void setTimer(long timer) {
		this.timer = timer;
	}
}
