package com.rs.game.player.content.grandExchange;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;

import com.rs.game.player.Player;


public class ItemPrice implements Serializable {
	
	private static final long serialVersionUID = -5657608578345854199L;

	public ItemPrice(int value) {
		transactions = new LinkedList<Transaction>();
		this.value = value;
	}
	
	protected LinkedList<Transaction> transactions;
	protected int value;
	
	public void addTransaction(Player player, int value) {
		Transaction last = findTransaction(player);
		if (last != null)
			transactions.remove(last);
		else if (transactions.size() >= 100) //cap last 100 diff player trades or else price will stagnate
			transactions.poll(); //removes top
		transactions.add(new Transaction(player.getUsername(), player.getSession().getIP(), value)); //adds bottoms
	}
	
	public void updateValue() {
		if (transactions.size() < 3)  //min of 3 transactions
			return;
		int[] values = new int[transactions.size()];
		int i = 0;
		for (Transaction t : transactions)
			values[i++] = t.value;
		Arrays.sort(values); //median price
		long v = values.length % 2 == 0 ? 
				(((long)values[values.length/2] + (long)values[values.length/2 - 1])/2l) : values[values.length/2];
		value = (int) v;
	}
	
	private Transaction findTransaction(Player player) {
		String ip = player.getSession().getIP();
		for (Transaction t : transactions)
			if (t.name.equalsIgnoreCase(player.getName()) || t.ip.equalsIgnoreCase(ip))
				return t;
		return null;
	}
}
