package com.rs.game.player.content.commands;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Summoning;
import com.rs.game.player.content.Summoning.Pouch;
import com.rs.net.decoders.handlers.ButtonHandler;
import com.rs.utils.Utils;

public class GearCommands {

	/**
	 * @author _Andy @rune-server.org
	 */

	final static int MODERN = 0, ANCIENT = 1, LUNAR = 2;

	public static enum Gears {
		MAIN2("main2", new int[][]
		{
		{ 23255, 1 },
		{ 23279, 1 },
		{ 23351, 2 },
		{ 15272, 2 },
		{ 23567, 2 },
		{ 6737, 1 },
		{ 11732, 1 },
		{ 15272, 2 },
		{ 6585, 1 },
		{ 19748, 1 },
		{ 15272, 2 },
		{ 4736, 1 },
		{ 12681, 1 },
		{ 4720, 1 },
		{ 4722, 1 },
		{ 5698, 1 },
		{ 4151, 1 },
		{ 20072, 1 },
		{ 8013, 500 },
		{ 555, 6000 },
		{ 560, 4000 },
		{ 565, 3000 },
		{ 12437, 500 } },
		/*inventory items, order of spawning^*/
		new int[][]
		{
		{ 2412, 1 },
		{ 4708, 1 },
		{ 7462, 1 },
		{ 6731, 1 },
		{ 6920, 1 },
		{ 15486, 1 },
		{ 4712, 1 },
		{ 4714, 1 },
		{ 6889, 1 },
		{ 18335, 1 } },
		/*equipment items, any order^*/
		/*modified stats, dont add any if you want max, skillId, level^*/
		true, ANCIENT, true),
		/*boolean curses, int spellbook, boolean spawnFamiliar*/
		MAIN("main", new int[][]
		{
		{ 11724, 1 },
		{ 4151, 1 },
		{ 6585, 1 },
		{ 4736, 1 },
		{ 11726, 1 },
		{ 20072, 1 },
		{ 11732, 1 },
		{ 12437, 500 },
		{ 15272, 1 },
		{ 5698, 1 },
		{ 23279, 1 },
		{ 23255, 1 },
		{ 15272, 2 },
		{ 23567, 2 },
		{ 15272, 2 },
		{ 23351, 2 },
		{ 15272, 4 },
		{ 555, 6000 },
		{ 560, 4000 },
		{ 565, 3000 },
		{ 8013, 500 } }, new int[][]
		{
		{ 2412, 1 },
		{ 12681, 1 },
		{ 7462, 1 },
		{ 6737, 1 },
		{ 6920, 1 },
		{ 15486, 1 },
		{ 4712, 1 },
		{ 4714, 1 },
		{ 6889, 1 },
		{ 18335, 1 } }, true, ANCIENT, true), ZERK("zerk", new int[][]
		{
		{ 10551, 1 },
		{ 15443, 1 },
		{ 6585, 1 },
		{ 23255, 1 },
		{ 3483, 1 },
		{ 8850, 1 },
		{ 10548, 1 },
		{ 23279, 1 },
		{ 10370, 1 },
		{ 5698, 1 },
		{ 23567, 2 },
		{ 15272, 2 },
		{ 23351, 2 },
		{ 15272, 7 },
		{ 12437, 500 },
		{ 555, 6000 },
		{ 560, 4000 },
		{ 565, 3000 },
		{ 8013, 500 } }, new int[][]
		{
		{ 2412, 1 },
		{ 14499, 1 },
		{ 7462, 1 },
		{ 6737, 1 },
		{ 6920, 1 },
		{ 15486, 1 },
		{ 7399, 1 },
		{ 6924, 1 },
		{ 6889, 1 },
		{ 18335, 1 } }, new int[][]
		{
		{ 0, 80 },
		{ 1, 45 } }, true, ANCIENT, true), ZERK2("zerk2", new int[][]
		{
		{ 23255, 1 },
		{ 23279, 1 },
		{ 23351, 2 },
		{ 15272, 2 },
		{ 23567, 2 },
		{ 6737, 1 },
		{ 4131, 1 },
		{ 15272, 2 },
		{ 6585, 1 },
		{ 19748, 1 },
		{ 15272, 2 },
		{ 24382, 1 },
		{ 3751, 1 },
		{ 10551, 1 },
		{ 14490, 1 },
		{ 5698, 1 },
		{ 4151, 1 },
		{ 8850, 1 },
		{ 8013, 500 },
		{ 555, 6000 },
		{ 560, 4000 },
		{ 565, 3000 },
		{ 12437, 500 } }, new int[][]
		{
		{ 2412, 1 },
		{ 14499, 1 },
		{ 7462, 1 },
		{ 6731, 1 },
		{ 3105, 1 },
		{ 22207, 1 },
		{ 7399, 1 },
		{ 6924, 1 },
		{ 6889, 1 },
		{ 18335, 1 } }, new int[][]
		{
		{ 0, 80 },
		{ 1, 45 } }, true, ANCIENT, true), DHAROKS("dharoks", new int[][]
		{
		{ 4718, 1 },
		{ 23567, 2 },
		{ 23351, 1 },
		{ 23255, 1 },
		{ 5698, 1 },
		{ 23279, 1 },
		{ 23609, 1 },
		{ 557, 1000000 },
		{ 560, 1000000 },
		{ 9075, 1000000 },
		{ 15272, 17 } }, new int[][]
		{
		{ 19748, 1 },
		{ 4716, 1 },
		{ 7462, 1 },
		{ 6737, 1 },
		{ 11732, 1 },
		{ 4151, 1 },
		{ 4720, 1 },
		{ 4722, 1 },
		{ 20072, 1 },
		{ 6585, 1 } }, true, LUNAR, false), PURE("pure", new int[][]
		{
		{ 23255, 1 },
		{ 23279, 1 },
		{ 23351, 1 },
		{ 23567, 1 },
		{ 23303, 1 },
		{ 15272, 11 },
		{ 11090, 2 },
		{ 22424, 1 },
		{ 15272, 1 },
		{ 4153, 1 },
		{ 4587, 1 },
		{ 5698, 1 },
		{ 8013, 500 },
		{ 15272, 4 } }, new int[][]
		{
		{ 10499, 1 },
		{ 7459, 1 },
		{ 6737, 1 },
		{ 3105, 1 },
		{ 861, 1 },
		{ 19157, 1000 },
		{ 6585, 1 },
		{ 544, 1 },
		{ 2497, 1 },
		{ 656, 1 } }, new int[][]
		{
		{ 0, 60 },
		{ 1, 1 },
		{ 5, 45 } }, false, ANCIENT, false), NH("nh", new int[][]
		{
		{ 23255, 1 },
		{ 23279, 1 },
		{ 23351, 1 },
		{ 23567, 1 },
		{ 23303, 1 },
		{ 15272, 7 },
		{ 10499, 1 },
		{ 24379, 1 },
		{ 15272, 2 },
		{ 9185, 1 },
		{ 15272, 3 },
		{ 5698, 1 },
		{ 4587, 1 },
		{ 15272, 1 },
		{ 8013, 500 },
		{ 555, 6000 },
		{ 560, 4000 },
		{ 565, 3000 },
		{ 12437, 500 } }, new int[][]
		{
		{ 2412, 1 },
		{ 6109, 1 },
		{ 7459, 1 },
		{ 6737, 1 },
		{ 626, 1 },
		{ 24092, 1 },
		{ 10458, 1 },
		{ 6108, 1 },
		{ 22424, 1 },
		{ 9244, 1000 },
		{ 6585, 1 } }, new int[][]
		{
		{ 0, 60 },
		{ 1, 1 },
		{ 5, 45 } }, false, ANCIENT, true), B6040("6040", new int[][]
		{
		{ 23255, 1 },
		{ 23279, 1 },
		{ 23351, 2 },
		{ 15272, 2 },
		{ 23567, 2 },
		{ 15272, 4 },
		{ 6585, 1 },
		{ 19748, 1 },
		{ 15272, 2 },
		{ 10386, 1 },
		{ 6128, 1 },
		{ 6129, 1 },
		{ 6130, 1 },
		{ 5698, 1 },
		{ 4587, 1 },
		{ 8850, 1 },
		{ 8013, 500 },
		{ 555, 6000 },
		{ 560, 4000 },
		{ 565, 3000 },
		{ 12437, 500 } }, new int[][]
		{
		{ 2412, 1 },
		{ 14499, 1 },
		{ 7462, 1 },
		{ 6737, 1 },
		{ 4097, 1 },
		{ 4675, 1 },
		{ 7399, 1 },
		{ 6924, 1 },
		{ 6889, 1 },
		{ 18335, 1 } }, new int[][]
		{
		{ 0, 60 },
		{ 1, 40 } }, true, ANCIENT, true), TURM("turm", new int[][]
		{
		{ 23255, 1 },
		{ 23279, 1 },
		{ 23351, 2 },
		{ 15272, 2 },
		{ 23567, 2 },
		{ 15272, 4 },
		{ 6585, 1 },
		{ 19748, 1 },
		{ 15272, 2 },
		{ 6322, 1 },
		{ 2613, 1 },
		{ 9674, 1 },
		{ 9676, 1 },
		{ 5698, 1 },
		{ 4587, 1 },
		{ 8849, 1 },
		{ 8013, 500 },
		{ 555, 6000 },
		{ 560, 4000 },
		{ 565, 3000 },
		{ 12437, 500 } }, new int[][]
		{
		{ 2412, 1 },
		{ 14499, 1 },
		{ 7460, 1 },
		{ 6737, 1 },
		{ 626, 1 },
		{ 4675, 1 },
		{ 7399, 1 },
		{ 6924, 1 },
		{ 6889, 1 },
		{ 18335, 1 } }, new int[][]
		{
		{ 0, 60 },
		{ 1, 30 } }, true, ANCIENT, true), F2P("f2p", new int[][]
		{
		{ 113, 1 },
		{ 373, 11 },
		{ 1319, 1 },
		{ 373, 4 },
		{ 1333, 1 },
		{ 373, 10 } }, new int[][]
		{
		{ 1169, 1 },
		{ 4316, 1 },
		{ 1725, 1 },
		{ 890, 500 },
		{ 853, 1 },
		{ 1129, 1 },
		{ 1065, 1 },
		{ 1099, 1 },
		{ 1061, 1 } }, new int[][]
		{
		{ 0, 40 },
		{ 1, 1 },
		{ 5, 45 } }, false, MODERN, false);
		private int[][] inventory;
		private int[][] equipment;
		private int[][] stats;
		private boolean curses;
		private int spellbook;
		private boolean spawnFamiliar;
		private String name;

		private Gears(String name, int[][] inventory, int[][] equipment, int[][] stats, boolean curses, int spellbook, boolean spawnFamiliar) {
			this.name = name;
			this.inventory = inventory;
			this.equipment = equipment;
			this.stats = stats;
			this.curses = curses;
			this.spellbook = spellbook;
			this.spawnFamiliar = spawnFamiliar;
		}

		private Gears(String name, int[][] inventory, int[][] equipment, boolean curses, int spellbook, boolean spawnFamiliar) {
			this(name, inventory, equipment, new int[][]
			{
			{ 0, 99 } }, curses, spellbook, spawnFamiliar);
		}

		public String getName() {
			return name;
		}
	}

	private final static int[][] forSwitch =
	{
	{ 6109, 656, 658, 660, 662, 664, 2900, 2910, 2920, 2930, 6860, 6858, 6856, 6862, 13101 },

	{ 3105, 626, 628, 630, 632, 634, 1837, 2579 },

	{ 4675, 24092, 24094, 24096, 24098 },

	{ 6107, 10458, 10460, 10462, 19380 },

	{ 6108, 10464, 10466, 10468, 19386, 646 },

	{ 2412, 2413, 2414 },

	{ 15486, 22207, 22209, 22211, 22213 },

	{ 6920, 626, 4097, 4117, 4107 },

	{ 14499, 6109, 660, 662, 664, 2900, 6860, 6858, 6856, 6862, 13101, 4109, 4099, 4089, 6918, 15602, 15608, 15620, 15614 },

	{ 6889, 24100, 24102, 24104, 24106 },

	{ 6924, 15610, 15616, 15622, 4093, 4103, 4113, 7398, 14501 },

	{ 7399, 4091, 4101, 4111, 6916, 15600, 15606, 15612, 15618, 14497 } };

	private static int getRandomSwitchedItem(Player player, int init) {
		int switchCape = player.isCompletedFightKiln() ? 23659 : player.isCompletedFightCaves() ? 6570 : init == 10499 ? 10499 : 19748, imbueZerk = player.isDonator() || player.isVIPDonator() ? 15220 : 6737, imbueSeers = player.isDonator() || player.isVIPDonator() ? 15018 : 6731, randomTeamCape = 4316 + Utils.random(4413 - 4316);
		switch (init) {
		case 6737:
			return imbueZerk;
		case 10499:
		case 19748:
			return switchCape;
		case 6731:
			return imbueSeers;
		}
		if (init >= 4316 && init < 4414)
			return ItemConfig.forID(randomTeamCape).isNoted() ? randomTeamCape - 1 : randomTeamCape;
		for (int[] array : forSwitch) {
			for (int member : array) {
				if (member == init)
					return array[Utils.random(array.length)];
			}
		}
		return init;
	}

	private static void refresh(Player player) {
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

	private static void setStats(Player player, Gears gear) {
		for (int id = 0; id < player.getSkills().getLevels().length; id++) {
			if (id >= 7 && id < 23 || id > 23)
				continue;
			player.getSkills().setXp(id, Skills.getXPForLevel(99));
		}
		for (int[] skills : gear.stats) {
			player.getSkills().setXp(skills[0], Skills.getXPForLevel(skills[1]));
		}
	}

	private static void setInventory(Player player, Gears gear) {
		player.getInventory().reset();
		for (int[] id : gear.inventory) {
			int switchedItem = getRandomSwitchedItem(player, id[0]);
			player.getInventory().addItem(switchedItem, id[1]);
		}
	}

	private static void setEquipment(Player player, Gears gear) {
		player.getEquipment().reset();
		for (int[] id : gear.equipment) {
			int switchedItem = getRandomSwitchedItem(player, id[0]);
			player.getEquipment().getItems().set(ItemConfig.forID(switchedItem).equipSlot, new Item(switchedItem, id[1]));
		}
	}

	private static void setMisc(Player player, Gears gear) {
		player.getPrayer().setPrayerBook(gear.curses);
		player.getCombatDefinitions().setSpellBook(gear.spellbook);
		if (player.getFamiliar() == null && gear.spawnFamiliar)
			Summoning.spawnFamiliar(player, Pouch.WOLPERTINGER);
	}

	private static void set(Player p, Gears g) {
		setInventory(p, g);
		setEquipment(p, g);
		setStats(p, g);
		setMisc(p, g);
		refresh(p);
	}

	public static String getSetupNamesC(Player player) {
		String setups = "";
		if (player.getSetups() != null) {
			for (CustomGear setup : player.getSetups()) {
				if (setup == null)
					continue;
				setups += setup.getName() + (", ");
			}
		}
		return " " + setups;
	}

	private static String getSetupNamesG() {
		String loadouts = "";
		int count = 0;
		for (Gears gear : Gears.values()) {
			count++;
			loadouts += gear.getName() + (count < Gears.values().length ? ", " : ".");
		}
		return loadouts;
	}

	public static void removeCustomGear(Player player, String name) {
		if (player.removeSetup(name))
			player.getPackets().sendGameMessage("Removed gear setup: " + name + ". Remaining setups:" + getSetupNamesC(player));
	}

	public static void saveCustomGear(Player player, String name) {
		if (!getSetupNamesC(player).replace(", ", "@ ").contains(" " + name + "@") && !getSetupNamesG().replace(", ", "@ ").replace(".", "@").contains(" " + name + "@")) {
			if (player.addSetup(new CustomGear(player, name)))
				player.getPackets().sendGameMessage("Added gear setup: " + name + ". Current setups:" + getSetupNamesC(player));
			else
				player.getPackets().sendGameMessage("You've reached your maximum amount of gearsetups!");
		} else
			player.getPackets().sendGameMessage("You've already got a setup with this name!");
	}

	public static boolean isGearSet(Player p, String type) {
		for (Gears gear : Gears.values()) {
			if (gear == null)
				continue;
			if (gear.name.toLowerCase().equals(type)) {
				set(p, gear);
				return true;
			}
		}
		if (p.getSetups() != null) {
			for (CustomGear gear : p.getSetups()) {
				if (gear == null)
					continue;
				if (!gear.getName().equals(type))
					continue;
				gear.set(p);
				return true;
			}
		}
		p.getPackets().sendGameMessage("Gear setup not found. Available loadouts:" + getSetupNamesC(p) + getSetupNamesG());
		return false;
	}
}