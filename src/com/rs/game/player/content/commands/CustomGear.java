package com.rs.game.player.content.commands;

import java.io.Serializable;
import java.util.Arrays;

import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Summoning;
import com.rs.game.player.content.Summoning.Pouch;
import com.rs.net.decoders.handlers.ButtonHandler;

public final class CustomGear implements Serializable {


	private static final long serialVersionUID = 5667753111392604622L;
	
	final static int MODERN = 0, ANCIENT = 1, LUNAR = 2;
	private ItemsContainer<Item> invo;
	private ItemsContainer<Item> equip;
	private double[] xp;
	private int spellbook;
	private boolean prayerBook;
	private Pouch familiar;
	private String name;
	
	public CustomGear(Player player, String name) {
		save(player, name);
	}
	
	private int getSpellBook(Player player) {
		int book = player.getCombatDefinitions().getSpellBook();
		return book == 192 ? MODERN : book == 193 ? ANCIENT : book == 430 ? LUNAR : MODERN;
	}
	
	public void printAll() {
		System.out.println("name: "+name+", attxp: "+xp[0]+", spellbook: "+spellbook+", pray: "+prayerBook+", familiar: "+familiar);
	}
	
	private void save(Player player, String name) {
		this.invo = player.getInventory().getItems().asItemContainer();
		this.equip = player.getEquipment().getItems().asItemContainer();
		this.xp = Arrays.copyOf(player.getSkills().getXp(), 7);
		this.spellbook = getSpellBook(player);
		this.prayerBook = player.getPrayer().isAncientCurses();
		familiar = player.getFamiliar() == null ? null : player.getFamiliar().getPouch();
		this.name = name;
	}
	
	private void setEquipment(Player player) {
		for (int index = 0; index < equip.getItems().length; index++) {
			player.getEquipment().getItems().set(index, equip.getItems()[index]);
		}
	}
	
	private void setInventory(Player player) {
		for (int index = 0; index < invo.getItems().length; index++) {
			player.getInventory().getItems().set(index, invo.getItems()[index]);
		}
	}
	
	private void setItems(Player p) {
		setInventory(p);
		setEquipment(p);
	}
	

	private void setMisc(Player player) {
		player.getCombatDefinitions().setSpellBook(spellbook);
		player.getPrayer().setPrayerBook(prayerBook);
		if (player.getFamiliar() == null && familiar != null) 
			Summoning.spawnFamiliar(player, familiar);
	}
	
	private void setStats(Player player) {
		for (int id = 0; id < xp.length; id++)
			player.getSkills().setXp(id, xp[id]);
	}
	
	private void refresh(Player player) {
		player.getInventory().init();
		player.getEquipment().init();
		player.getSkills().restoreSkills();
		player.setHitpoints(player.getMaxHitpoints());
		player.refreshHitPoints();
		player.getPrayer().restorePrayer(player.getSkills().getLevel(Skills.PRAYER) * 10);
		player.getInterfaceManager().closeXPDisplay();
		player.getInterfaceManager().sendXPDisplay();
		ButtonHandler.refreshEquipBonuses(player);
		player.getAppearence().generateAppearenceData();
	}
	
	public String getName() {
		return name;
	}
	
	public void set(Player p) {
		setItems(p);
		setMisc(p);
		setStats(p);
		refresh(p);
	}
	
	public ItemsContainer<Item> getInventory() {
		return invo;
	}
	
	public ItemsContainer<Item> getEquipment() {
		return equip;
	}
}