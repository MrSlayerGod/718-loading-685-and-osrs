package com.rs.game.player.content.grandExchange;

import java.io.Serializable;

public class Transaction implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3175228218272440261L;
	
	protected String name;
	protected String ip;
	protected int value;
	
	public Transaction(String name, String ip, int value) {
		this.name = name;
		this.ip = ip;
		this.value = value;
	}
}
