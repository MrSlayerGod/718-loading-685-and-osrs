package com.rs.game.player.actions;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.npc.others.zalcano.Zalcano;
import com.rs.game.player.CoalBag;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

public class Smelting extends Action {

	public enum SmeltingBar {

		BRONZE(1, 6.2, new Item[]
		{ new Item(436), new Item(438) }, new Item(2349), Skills.SMITHING),

		BLURITE(8, 8.0, new Item[]
		{ new Item(668) }, new Item(9467), Skills.SMITHING),

		IRON(15, 12.5, new Item[]
		{ new Item(440) }, new Item(2351), Skills.SMITHING),

		SILVER(20, 13.7, new Item[]
		{ new Item(442) }, new Item(2355), Skills.SMITHING),

		STEEL(30, 17.5, new Item[]
		{ new Item(440), new Item(453, 2) }, new Item(2353), Skills.SMITHING),

		GOLD(40, 22.5, new Item[]
		{ new Item(444) }, new Item(2357), Skills.SMITHING),

		MITHRIL(50, 30, new Item[]
		{ new Item(447), new Item(453, 4) }, new Item(2359), Skills.SMITHING),

		ADAMANT(70, 37.5, new Item[]
		{ new Item(449), new Item(453, 6) }, new Item(2361), Skills.SMITHING),

		RUNE(85, 50, new Item[]
		{ new Item(451), new Item(453, 8) }, new Item(2363), Skills.SMITHING),

		NOVITE(1, 7, new Item[]
		{ new Item(17630) }, new Item(17650), Skills.SMITHING),

		BATHUS(10, 13.3, new Item[]
		{ new Item(17632) }, new Item(17652), Skills.SMITHING),

		MARMAROS(20, 19.6, new Item[]
		{ new Item(17634) }, new Item(17654), Skills.SMITHING),

		KRATONITE(30, 25.9, new Item[]
		{ new Item(17636) }, new Item(17656), Skills.SMITHING),

		FRACTITE(40, 32.2, new Item[]
		{ new Item(17638) }, new Item(17658), Skills.SMITHING),

		ZEPHYRIUM(50, 38.5, new Item[]
		{ new Item(17640) }, new Item(17660), Skills.SMITHING),

		ARGONITE(60, 44.8, new Item[]
		{ new Item(17642) }, new Item(17662), Skills.SMITHING),

		KATAGON(70, 51.1, new Item[]
		{ new Item(17644) }, new Item(17664), Skills.SMITHING),

		GORGONITE(80, 57.4, new Item[]
		{ new Item(17646) }, new Item(17666), Skills.SMITHING),

		PROMETHIUM(90, 63.7, new Item[]
		{ new Item(17648) }, new Item(17668), Skills.SMITHING),

		CANNON_BALLS(35, 25.6, new Item[]
		{ new Item(2353, 1), new Item(4, 1) }, new Item(2, 4 * 3), Skills.SMITHING), //3x more aka 12

		MOLTEN_GLASS(1, 20, new Item[]
		{ new Item(1783, 1), new Item(1781, 1) }, new Item(1775, 1), Skills.CRAFTING),

		SILVER_SICKLE(18, 50, new Item[]
		{ new Item(2355, 1), new Item(2976, 1) }, new Item(2961, 1), Skills.CRAFTING),

		HOLY_SYMBOL(16, 50, new Item[]
		{ new Item(2355, 1), new Item(1599, 1) }, new Item(1718, 1), Skills.CRAFTING),

		UNHOLY_SYMBOL(16, 50, new Item[]
		{ new Item(2355, 1), new Item(1594, 1) }, new Item(1724, 1), Skills.CRAFTING),

		UNCHARGED_TIARA(23, 52.5, new Item[]
		{ new Item(2355, 1), new Item(5523, 1) }, new Item(5525, 1), Skills.CRAFTING),
		
		SLAYER_RING(75, 15, new Item[]
		{ new Item(2357, 1), new Item(1592, 1), new Item(4155, 1) }, new Item(13281, 1), Skills.CRAFTING),

		REFINED_TEPHRA(1, 10, new Item[]
				{ new Item(Zalcano.TEPHRA, 1) }, new Item(Zalcano.REFINED_TEPHRA, 1), Skills.SMITHING),

		IMBUED_TEPHRA(1, 5, new Item[]
				{ new Item(Zalcano.REFINED_TEPHRA, 1) }, new Item(Zalcano.IMBUED_TEPHRA, 1), Skills.RUNECRAFTING);

		public static SmeltingBar getBar(int buttonId) {
			if (buttonId >= SmeltingBar.values().length)
				return SmeltingBar.BRONZE;
			return SmeltingBar.values()[buttonId];
		}
		
		public static SmeltingBar getBarWithOre(int oreId) {
			for (SmeltingBar bar : SmeltingBar.values()) {
				for (Item item : bar.getItemsRequired())
					if (item.getId() == oreId)
						return bar;
			}
			return null;
		}

		private int levelRequired;
		private double experience;
		private Item[] itemsRequired;
		private Item producedBar;
		private int skillType;

		private SmeltingBar(int levelRequired, double experience, Item[] itemsRequired, Item producedBar, int skillType) {
			this.levelRequired = levelRequired;
			this.experience = experience;
			this.itemsRequired = itemsRequired;
			this.producedBar = producedBar;
			this.skillType = skillType;
		}

		public Item[] getItemsRequired() {
			return itemsRequired;
		}

		public int getLevelRequired() {
			return levelRequired;
		}

		public Item getProducedBar() {
			return producedBar;
		}

		public double getExperience() {
			return experience;
		}

		public int getSkillType() {
			return skillType;
		}
	}

	public SmeltingBar bar;
	public WorldObject object;
	public int ticks;

	public Smelting(SmeltingBar bar, WorldObject object, int ticks) {
		this.object = object;
		this.bar = bar;
		this.ticks = ticks;
	}

	@Override
	public boolean start(Player player) {
		if (bar == null || player == null || object == null) {
			return false;
		}
		if (!player.getInventory().containsItemToolBelt(bar.getItemsRequired()[0].getId(), bar.getItemsRequired()[0].getAmount())) {
			sendMessage(player, bar,"You need " + bar.getItemsRequired()[0].getDefinitions().getName() + " to create a " + bar.getProducedBar().getDefinitions().getName() + ".");
			return false;
		}
		if (bar.getItemsRequired().length > 1) {
			int secondary = bar.getItemsRequired()[1].getId();
			int secondaryAmt = bar.getItemsRequired()[1].getAmount();
			if(secondary == CoalBag.COAL_ID) {
				if(player.getCoalBag().getCoal()+player.getInventory().getAmountOf(secondary) < secondaryAmt) {
					player.sendMessage("You need " + secondaryAmt + " coal to create a " + bar.getProducedBar().getName() + ".");
					return false;
				}
			} else {
				if (!player.getInventory().containsItemToolBelt(secondary, secondaryAmt)) {
					sendMessage(player, bar,"You need " + bar.getItemsRequired()[1].getDefinitions().getName() + " to create a " + bar.getProducedBar().getDefinitions().getName() + ".");
					return false;
				}
			}
		}
		if (player.getSkills().getLevel(bar.getSkillType()) < bar.getLevelRequired()) {
			sendMessage(player, bar,"You need a Smithing level of at least " + bar.getLevelRequired() + " to smelt " + bar.getProducedBar().getDefinitions().getName());
			return false;
		}
		if (!player.getSlayerManager().hasLearnedRing() && bar == SmeltingBar.SLAYER_RING) {
			player.getPackets().sendGameMessage("You lack the knowledge to create a ring of slaying, perhaps a Slayer Master could assist you.");
			return false;
		}
		sendMessage(player, bar,"You place the required ores and attempt to create a bar of " + bar.getProducedBar().getDefinitions().getName().toLowerCase().replace(" bar", "") + ".");
		return true;
	}

	@Override
	public boolean process(Player player) {
		if (bar == null || player == null || object == null) {
			return false;
		}
		if (!player.getInventory().containsItemToolBelt(bar.getItemsRequired()[0].getId(), bar.getItemsRequired()[0].getAmount())) {
			sendMessage(player, bar,"You need " + bar.getItemsRequired()[0].getDefinitions().getName() + " to create a " + bar.getProducedBar().getDefinitions().getName() + ".");
			return false;
		}
		if (bar.getItemsRequired().length > 1) {
			int secondary = bar.getItemsRequired()[1].getId();
			int secondaryAmt = bar.getItemsRequired()[1].getAmount();
			if(secondary == CoalBag.COAL_ID) {
				if(player.getCoalBag().getCoal()+player.getInventory().getAmountOf(secondary) < secondaryAmt) {
					player.sendMessage("You need " + secondaryAmt + " coal to create a " + bar.getProducedBar().getName() + ".");
					return false;
				}
			} else {
				if (!player.getInventory().containsItemToolBelt(secondary, secondaryAmt)) {
					sendMessage(player, bar,"You need " + bar.getItemsRequired()[1].getDefinitions().getName() + " to create a " + bar.getProducedBar().getDefinitions().getName() + ".");
					return false;
				}
			}
		}
		if (player.getSkills().getLevel(bar.getSkillType()) < bar.getLevelRequired()) {
			sendMessage(player, bar,"You need a Smithing level of at least " + bar.getLevelRequired() + " to smelt " + bar.getProducedBar().getDefinitions().getName());
			return false;
		}
		player.faceObject(object);
		return true;
	}

	public boolean isSuccessFull(Player player) {
		if (bar == SmeltingBar.IRON) {
			if (player.getEquipment().getItem(Equipment.SLOT_RING) != null && player.getEquipment().getItem(Equipment.SLOT_RING).getId() == 2568) {
				return true;
			} else {
				return Utils.random(100) <= (player.getSkills().getLevel(bar.getSkillType()) >= 45 ? 80 : 50);
			}
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		ticks--;
		if(bar == SmeltingBar.IMBUED_TEPHRA) {
			player.setNextGraphics(new Graphics(186));
			player.setNextAnimation(new Animation(791));
		} else
			player.setNextAnimation(new Animation(3243));
		double xp = bar.getExperience();
		if (bar == SmeltingBar.GOLD && player.getEquipment().getGlovesId() == 776)
			xp *= 2.5;
		player.getSkills().addXp(bar.getSkillType(), xp);
		for (Item required : bar.getItemsRequired()) {
			if (required.getId() == 4 || required.getId() == 2976 || required.getId() == 1594 || required.getId() == 1599 || required.getId() == 5523)
				continue;
			int coalFromBag = 0;
			if(required.getId() == CoalBag.COAL_ID) {
				coalFromBag = player.getCoalBag().smithingIntercept(player, required.getAmount());
			}
			player.getInventory().deleteItem(required.getId(), required.getAmount() - coalFromBag);
		}
		if (isSuccessFull(player)) {
			int amount = bar.getProducedBar().getAmount();
			if (bar.getProducedBar().getDefinitions().isStackable())
				amount *= Settings.getCraftRate();
			if (bar == SmeltingBar.MOLTEN_GLASS)
				player.getInventory().addItem(new Item(1925));
			player.getInventory().addItem(bar.getProducedBar().getId(), amount);
			
			
			if (bar == SmeltingBar.IMBUED_TEPHRA)
				player.getPackets().sendGameMessage("You bind the altar's power into some refined tephra.", true);
			else if(bar == SmeltingBar.REFINED_TEPHRA)
				player.getPackets().sendGameMessage("You heat the tephra in the furnance and smelt it down into a refined lump.", true);
			sendMessage(player, bar,"You retrieve a bar of " + bar.getProducedBar().getDefinitions().getName().toLowerCase().replace(" bar", "") + ".");
		} else {
			sendMessage(player, bar,"The ore is too impure and you fail to refine it.");
		}
		if (ticks > 0) {
			return 1;
		}
		return -1;
	}

	public void sendMessage(Player player, SmeltingBar bar, String msg) {
		if (bar != SmeltingBar.IMBUED_TEPHRA && bar != SmeltingBar.REFINED_TEPHRA)
			player.getPackets().sendGameMessage(msg, true);
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}
}
