package com.rs.game.npc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.cache.loaders.NPCConfig;
import com.rs.game.TemporaryAtributtes;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.CombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.actions.HerbCleaning;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.NPCKillLog;
import com.rs.game.player.content.prayer.Burying.Bone;
import com.rs.net.decoders.handlers.ButtonHandler;
import com.rs.utils.*;

public class Drops {

	public static double NERF_DROP_RATE = 1.0;
	public static double NERF_DROP_RATE_CW = 0.95;
	
	public static Map<String, Double> nerfedPlayers;
	public static boolean needsSaving;
	
	public static void init() {
		nerfedPlayers = SerializableFilesManager.loadNerfDrops();
		if (nerfedPlayers == null)
			nerfedPlayers = new HashMap<String, Double>();

		ButtonHandler.register(DROP_INTERFACE_ID, 22, 1, (Player player, int slot1, int slot2, int action) -> {
			player.getTemporaryAttributtes().put(TemporaryAtributtes.Key.SEARCH_NPC_DROP, Boolean.TRUE);
			player.getPackets().sendInputLongTextScript("Search for a monster:");
		});
	}
	
	public static double getNerfDrop(Player player) {
		Double drop = nerfedPlayers.get(player.getUsername());
		return drop == null ? 1 : drop;
	}
	
	public static void nerfPlayer(Player target, double rate) {
		if (rate == 1)
			nerfedPlayers.remove(target.getUsername());
		else
			nerfedPlayers.put(target.getUsername(), rate);
		needsSaving = true;
	}
	
	
	public static void save() {
		if (!needsSaving)
			return;
		needsSaving = false;
		SerializableFilesManager.saveNerfDrops(nerfedPlayers);
	}
	
	
	public static final int ALWAYS = 0, COMMOM = 1, UNCOMMON = 2, RARE = 3, VERY_RARE = 4;

	//100% always, 100% commum, 75% uncommum, 2% rare, 1% very rare
	//  public static final double[] DROP_RATES = { 100.0, 100.0, 75.0, 2.0,  1.0 };

	//2nd eco
	/*	public static final double[] DROP_RATES =
	{ 100.0, 90.0, 70.0, 0.5, 0.3 };*/
	//boosted 20%
	//100.0, 90.0, 70.0, 0.2, 0.1
		public static final double[] DROP_RATES =
		{ 100.0, 75.0, 50.0, 0.15, 0.075 };//0.1, 0.05
	
	public static final int[] CHARMS =
	{ 12158, 12159, 12160, 12163 };
	public static final Drop[] RARE_DROP_TABLE =
	{
		new Drop(1623, 1, 1),
		new Drop(1621, 1, 1),
		new Drop(1619, 1, 1),
		new Drop(1617, 1, 1),
		new Drop(1453, 1, 1),
		new Drop(1462, 1, 1),
		new Drop(987, 1, 1),
		new Drop(985, 1, 1),
		new Drop(995, 250, 1200),
		new Drop(1247, 1, 1),
		new Drop(830, 5, 5),
		new Drop(1201, 1, 1),
		new Drop(1319, 1, 1),
		new Drop(1373, 1, 1),
		new Drop(2366, 1, 1),
		new Drop(1249, 1, 1), //d spear
		new Drop(52731, 1, 1), //d hasta
		new Drop(1149, 1, 1),
		new Drop(1187, 1, 1),
		new Drop(563, 45, 45),
		new Drop(563, 5, 50),
		new Drop(561, 47, 77),
		new Drop(566, 20, 20),
		new Drop(565, 50, 50),
		new Drop(892, 150, 150),
		new Drop(443, 100, 100),
		new Drop(995, 250, 1200) // again
		,
		new Drop(1215, 1, 1),
		new Drop(892, 150, 500) // again
		,
		new Drop(9143, 200, 200),
		new Drop(533, 151, 500),
		new Drop(2999, 25, 250),
		new Drop(258, 33, 33),
		new Drop(3001, 30, 120),
		new Drop(270, 10, 100),
		new Drop(454, 150, 750),
		new Drop(450, 150, 800),
		new Drop(7937, 100, 1450),
		new Drop(1441, 25, 35),
		new Drop(1443, 25, 36),
		new Drop(1444, 1, 1),
		new Drop(372, 125, 1000),
		new Drop(384, 250, 500),
		new Drop(5321, 3, 3),
		new Drop(1631, 1, 1),
		new Drop(1615, 1, 1),
		new Drop(1392, 200, 200),
		new Drop(574, 1000, 1000),
		new Drop(570, 1000, 1000),
		new Drop(452, 1, 100),
		new Drop(2362, 145, 700),
		new Drop(2364, 1, 150),
		new Drop(5315, 1, 50),
		new Drop(5316, 1, 6),
		new Drop(5289, 10, 10),
		new Drop(5304, 1, 31),
		new Drop(5300, 1, 1),
		new Drop(1516, 100, 450),
		new Drop(21620, 4, 4),
		new Drop(9342, 150, 150),
		new Drop(1216, 50, 50),
		new Drop(20667, 1, 1),
		new Drop(6686, 250, 250) };

	private boolean acessRareTable;
	private Drop[][] drops;
	private Drop[][] gearRareDrops;

	public Drops(boolean acessRareTable) {
		this.acessRareTable = acessRareTable;
		drops = new Drop[VERY_RARE + 1][];
		gearRareDrops = new Drop[VERY_RARE - RARE + 1][];

	}
	private int[] dropTableLine = {42,53,64,75,86,97,108,119,130,141,152,163,174,185,
			196,207,218,229,240,251,262,273,284,295,306,317,328,339,350,361,
			372,383,394,405,416,427,438,449,460,471,482,493,504,515,526,537,
			548,559,570,581,592,603,614,625,636,647,658,669,680,691,702,713,
			724,735,746,457,768};

	public static void search(Player player, String value) {
		NPCConfig npc = NPCDrops.search(value);
		if(npc == null) {
			player.sendMessage("No monster found for search: <col=ff0000>" + value);
			return;
		}
		if(!npc.getName().toLowerCase().equals(value.toLowerCase())) {
			player.sendMessage("No exact match found for <col=ffff00>\"" + value + "\"</col>, closest monster: <col=ffff00>" + npc.getName() +"</col>.");
		} else {
			player.sendMessage("Displaying drop table for <col=00ff00>" + npc.getName()+ "</col>.");
		}
		Drops drops = NPCDrops.getDrops(npc.id);

		if (NPCDrops.getDrops(-npc.id) != null) {
			drops = NPCDrops.getDrops(-npc.id);
			player.getPackets().sendGameMessage("This boss has hard mode drops!");
		}


		NPC worldnpc = null; // need worldnpc get to specific drop rate factor
		for(NPC n : World.getNPCs()) {
			if (npc.id == n.getId()) {
				worldnpc = n;
				break;
			}
		}
		double e = (player.getRights() == 2 ? player.getDropRateMultiplier() : player.getDropRateMultiplierI()) * (worldnpc == null ? 1 : worldnpc.getDropRateFactor()) * (player.getRights() == 2 ? 1 : 1.25);
		drops.dropInterface(player, e, npc);
	}

	public void writeDrop(Player player, int child, Drop drop, String tableName, int dropRate) {
		if(child >= dropTableLine.length)
			return;
		if(drop == null) {
			player.getPackets().sendHideIComponent(DROP_INTERFACE_ID, dropTableLine[child], true);
			return;
		} else {
			player.getPackets().sendHideIComponent(DROP_INTERFACE_ID, dropTableLine[child], false);
		}
		player.getPackets().sendItemOnIComponent(DROP_INTERFACE_ID, dropTableLine[child]+3, drop.getItemId(), drop.getMaxAmount()); //sends the item to component
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, dropTableLine[child]+4, ItemConfig.forID(drop.getItemId()).getName()); //sends the item name
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, dropTableLine[child]+6, drop.getMinAmount() + ""); //sends minimum
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, dropTableLine[child]+8, drop.getMaxAmount() + ""); //sends maximum
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, dropTableLine[child]+9, tableName); //sends table name
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, dropTableLine[child]+10, "1/" + dropRate); //sends the drop rate
	}

	public static final int
			NAME_COMPONENT = 29,
			COMBAT_COMPONENT = 782,
			HITPOINTS_COMPONENT = 783,
			MAX_HIT_COMPONENT = 784,
			AGGRESSIVE_COMPONENT = 785,
			UNUSED_COMPONENT = 786,
			UNUSED2_COMPONENT = 787,
			UNUSED3_COMPONENT = 788,
			UNUSED4_COMPONENT = 789,
			UNUSED5_COMPONENT = 802,
			UNUSED6_COMPONENT = 804,
			UNUSED7_COMPONENT = 806,
			UNUSED8_COMPONENT = 808,
			UNUSED9_COMPONENT = 810;

	public void writeNPCPanel(Player player, NPCConfig npc) {
		NPCCombatDefinitions cbdef = NPCCombatDefinitionsL.getNPCCombatDefinitions(npc.id);
		double[] bdef = NPCBonuses.getBonuses(npc.id);

		NPCBonuses.getBonuses(npc.id);
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, NAME_COMPONENT, npc.getName());
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, COMBAT_COMPONENT, npc.combatLevel + "");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, HITPOINTS_COMPONENT, cbdef == null ? "0" : (cbdef.getHitpoints() > 0 && player.isOldHitLook() ? cbdef.getHitpoints() /10 : cbdef.getHitpoints()) + "");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, MAX_HIT_COMPONENT, cbdef == null ? "" : (cbdef.getMaxHit() > 0 && player.isOldHitLook() ? cbdef.getMaxHit() / 10 : cbdef.getMaxHit()) + "");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, AGGRESSIVE_COMPONENT, cbdef == null ? "" :
				cbdef.getAgressivenessType() == NPCCombatDefinitions.PASSIVE ? "Passive" : cbdef.getAgressivenessType() == NPCCombatDefinitions.AGRESSIVE ? "Yes" : "");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, UNUSED_COMPONENT, acessRareTable ? "Yes" : "No");

		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, 779, "Melee Att.");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, 780, "Range Att.");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, 781, "Magic Att.");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, 803, "Stab Def.");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, 805, "Slash Def.");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, 807, "Crush Def.");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, 809, "Ranged Def.");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, 811, "Magic Def.");
		//- stabatt(melee att) slashatt(unused) crushatt(unused) magicatt rangeatt stabdef slashdef crushdef magicdef rangedef
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, UNUSED2_COMPONENT, bdef == null ? "0" : bdef[CombatDefinitions.STAB_ATTACK] + "");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, UNUSED3_COMPONENT, bdef == null ? "0" : bdef[CombatDefinitions.RANGE_ATTACK] + "");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, UNUSED4_COMPONENT, bdef == null ? "0" : bdef[CombatDefinitions.MAGIC_ATTACK] + "");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, UNUSED5_COMPONENT, bdef == null ? "0" : bdef[CombatDefinitions.STAB_DEF] + "");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, UNUSED6_COMPONENT, bdef == null ? "0" : bdef[CombatDefinitions.SLASH_DEF] + "");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, UNUSED7_COMPONENT, bdef == null ? "0" : bdef[CombatDefinitions.CRUSH_DEF] + "");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, UNUSED8_COMPONENT, bdef == null ? "0" : bdef[CombatDefinitions.RANGE_DEF] + "");
		player.getPackets().sendIComponentText(DROP_INTERFACE_ID, UNUSED9_COMPONENT, bdef == null ? "0" : bdef[CombatDefinitions.RANGE_DEF] + "");
	}

	public static final int DROP_INTERFACE_ID = 3067;

	public void dropInterface(Player player, double e, NPCConfig npc) {
		writeNPCPanel(player, npc);
		int index = 0;

		// if (acessRareTable)
		if (drops[ALWAYS] != null) {
			for (Drop drop : drops[ALWAYS])
				writeDrop(player, index++, drop, "<col=00ffff>ALWAYS", 1);
		}

		for (int i = COMMOM; i <= VERY_RARE; i++) {
			int total = (i < RARE ? 0 : gearRareDrops[i - RARE] == null ? 0 : gearRareDrops[i - RARE].length) + (drops[i] != null ? drops[i].length : 0);
			if (total == 0)
				continue;

			double rate = DROP_RATES[i] * e; //hide nerf

			boolean twoTables = i >= RARE && gearRareDrops[i - RARE] != null && drops[i] != null && drops[i].length != 0;
			if (twoTables)
				rate *= 2;

			if (i < RARE || player.getRights() == 2)//unhide nerf
				rate /= 1.25;

			int chance = (int) (100d / (rate));

			String tblClr = i == VERY_RARE ? "661a00" : i == RARE ? "cc3300" :i == UNCOMMON ? "ffff00" : "00ff00";
			//rate = Math.min(100,rate);
			String tableName = i == COMMOM ? "COMMON" : i == UNCOMMON ? "UNCOMMON" : i == RARE ? "RARE" : "VERY RARE";
			tableName = "<col=" + tblClr +">" + tableName;


			if (i >= RARE && gearRareDrops[i - RARE] != null) {
				double ratePerItem = (rate / (double)gearRareDrops[i - RARE].length) / (twoTables ? 2 : 1);
				int chancePerTable = (int) (100d / (rate / (twoTables ? 2 : 1)));

				if (Settings.DOUBLE_DROP_RATES) {
					chancePerTable = (int) (100d / (rate / (twoTables ? 2 : 1)) * .5);
				}

				int chancePerItem = (int) (100d / ratePerItem);
				for (Drop drop : gearRareDrops[i - RARE]) {
					writeDrop(player, index++, drop, tableName, chancePerTable);
				}
			}
			if (drops[i] != null) {
				//double rate = DROP_RATES[i] / drops[i].length * e;
				for (Drop drop : drops[i]) {

					double ratePerItem = (rate / (double)drops[i].length) / (twoTables ? 2 : 1);
					int chancePerItem = (int) ((100d / ratePerItem));


					if (Settings.DOUBLE_DROP_RATES) {
						chancePerItem = (int) ((100d / ratePerItem) * .5) ;
					}

					writeDrop(player, index++, drop, tableName, chancePerItem);
				}
			}
		}

		while(index<dropTableLine.length)
			writeDrop(player, index++, null, null, 0);

		player.getInterfaceManager().sendInterface(DROP_INTERFACE_ID);
	}

	public void viewDrops(Player player, NPC npc) {
		double e = (player.getRights() == 2 ? player.getDropRateMultiplier() : player.getDropRateMultiplierI()) * npc.getDropRateFactor() * 1.3;

		dropInterface(player, e, NPCConfig.forID(npc.getId()));
	}
	
	public void oldDropInterface(Player player, NPC npc) {

		double e = (player.getRights() == 2 ? player.getDropRateMultiplier() : player.getDropRateMultiplierI()) * npc.getDropRateFactor() * 1.3;

		String name = npc.getName();
		List<String> lines = new ArrayList<String>(300);
		if (acessRareTable)
			lines.add("Has RTD access");
		lines.add("------------------");
		lines.add("ALWAYS");
		if (drops[ALWAYS] != null) {
			for (Drop drop : drops[ALWAYS])
				lines.add("<col=ffffff>"+ItemConfig.forID(drop.getItemId()).getName()+" x"+Utils.getFormattedNumber(drop.getMinAmount())
						+ (drop.getMinAmount() != drop.getMaxAmount() ? ("-"+Utils.getFormattedNumber(drop.getMaxAmount())) : ""));
		}
		for (int i = COMMOM; i <= VERY_RARE; i++) {

			int total = (i < RARE ? 0 : gearRareDrops[i - RARE] == null ? 0 : gearRareDrops[i - RARE].length) + (drops[i] != null ? drops[i].length : 0);
			if (total == 0)
				continue;

			lines.add("------------------");
			double rate = DROP_RATES[i] * e; //hide nerf

			boolean twoTables = i >= RARE && gearRareDrops[i - RARE] != null && drops[i] != null && drops[i].length != 0;
			if (twoTables)
				rate *= 2;

			if (i < RARE || player.getRights() == 2)//unhide nerf
				rate /= 1.25;

			int chance = (int) (100d / (rate));

			//rate = Math.min(100,rate);
			lines.add((i == COMMOM ? "COMMON" : i == UNCOMMON ? "UNCOMMON" : i == RARE ? "RARE" : "VERY RARE")+(rate > 100 ? "" : (" ("+(new DecimalFormat("0.####").format(rate)+"%, 1 in "+chance+")"))));
			if (i >= RARE && gearRareDrops[i - RARE] != null) {
				double ratePerItem = (rate / (double)gearRareDrops[i - RARE].length) / (twoTables ? 2 : 1);
				int chancePerTable = (int) (100d / (rate / (twoTables ? 2 : 1)));
				int chancePerItem = (int) (100d / ratePerItem);

				if (drops[i] != null && drops[i].length != 0)
					lines.add("Gear Table "+" ("+new DecimalFormat("0.####").format(rate/2)+"%, 1 in "+chancePerTable+")");
				for (Drop drop : gearRareDrops[i - RARE])
					lines.add("<col="+(i == VERY_RARE ? "661a00" : i == RARE ? "cc3300" :i == UNCOMMON ? "ffff00" : "00ff00")+">"+ItemConfig.forID(drop.getItemId()).getName()+" x"+Utils.getFormattedNumber(drop.getMinAmount())		+ (drop.getMinAmount() != drop.getMaxAmount() ? ("-"+Utils.getFormattedNumber(drop.getMaxAmount())) : "")
									+ (rate > 100 ? "" : " ("+new DecimalFormat("0.####").format(ratePerItem)+"%, 1 in "+chancePerItem+")")
							/*+" 1/"+(int)(100/rate)+""*/);

				;
				if (drops[i] != null && drops[i].length != 0)
					lines.add("Misc Table "+" ("+new DecimalFormat("0.####").format(rate/2)+"%, 1 in "+chancePerTable+")");
			}
			if (drops[i] != null) {
				//double rate = DROP_RATES[i] / drops[i].length * e;
				for (Drop drop : drops[i]) {

					double ratePerItem = (rate / (double)drops[i].length) / (twoTables ? 2 : 1);
					int chancePerItem = (int) (100d / ratePerItem);
					lines.add("<col="+(i == VERY_RARE ? "661a00" : i == RARE ? "cc3300" :i == UNCOMMON ? "ffff00" : "00ff00")+">"+ItemConfig.forID(drop.getItemId()).getName()+" x"+Utils.getFormattedNumber(drop.getMinAmount())
									+ (drop.getMinAmount() != drop.getMaxAmount() ? ("-"+Utils.getFormattedNumber(drop.getMaxAmount())) : "")

									+ (rate > 100 ? "" : " ("+new DecimalFormat("0.####").format(ratePerItem)+"%, 1 in "+chancePerItem+")")
							/*+" 1/"+(int)(100/rate)+""*/);
				}
			}
		}
		lines.add("------------------");
		NPCKillLog.sendQuestTabBG(player, "DROPS - "+name, lines.toArray(new String[lines.size()]));
	}

	public List<Drop> generateDrops(Player killer, double multiplier) {
		List<Drop> d = new ArrayList<Drop>();
		boolean ringOfWealth = killer != null && Combat.hasRingOfWealth(killer);
		double nerf = getNerfDrop(killer);
	/*	if (ringOfWealth)
			e += (killer.getEquipment().getRingId() == 25488 || killer.getEquipment().getRingId() == 42785 ? 0.03 : 0.01); //1% extra chance
	*/	if (drops[ALWAYS] != null) {
			for (Drop drop : drops[ALWAYS])
				d.add(drop);
		}
		for (int i = COMMOM; i <= VERY_RARE; i++) {
			double mult = multiplier * (i >= RARE ? nerf : 1);
			Drop drop = getDrop(i, mult);
			if (drop != null) {
				if (i >= RARE && ringOfWealth) {
					killer.getPackets().sendGameMessage("<col=ff7000>Your ring of wealth shines more brightly!", true);
					ringOfWealth = false;
				}
				d.add(drop);
			}
		}
		if (acessRareTable && Utils.random((int) (5000 / multiplier)) == 0) {
			Drop drop = getRareDropTable();
			if (drop.getItemId() != 20667 || ringOfWealth)
				d.add(drop);
		}
		return d;
	}
	
	public void setAcessRareTable(boolean t) {
		acessRareTable = t;
	}

	public void addCharms(List<Drop> d, int size) {
		double chance = Math.min(0.8, size * 0.20);
		if (!d.isEmpty() && chance >= Math.random()) 
			d.add(new Drop(CHARMS[Utils.random(CHARMS.length)], 1, size));
			
	}

	public Drop getRareDropTable() {
		return RARE_DROP_TABLE[Utils.random(RARE_DROP_TABLE.length)];
	}
	public Drop[] getDrops(int rarity) {
		return drops[rarity];
	}
	
	public Drop getDrop(int rarity, double e) {
		if (rarity >= RARE) {
			e *= NERF_DROP_RATE;
			
			if (gearRareDrops[rarity - RARE] != null && gearRareDrops[rarity - RARE].length != 0 && Math.random() * 100 <= (DROP_RATES[rarity] * e))
				return gearRareDrops[rarity - RARE][Utils.random(gearRareDrops[rarity - RARE].length)];
		}
		if (drops[rarity] != null && drops[rarity].length != 0 && Math.random() * 100 <= (DROP_RATES[rarity] * e))
			return drops[rarity][Utils.random(drops[rarity].length)];
		return null;
	}

	public static boolean countsAsGear(int id) {
		return id == 25483 || id == 25478 || id == 52966 || id == 52988 || id == 51918 || id == 11704 || id == 11702 || id == 11706 || id == 11708 || id == 13754 || id == 11286 || id == 21369 || id == 13746 || id == 13748 || id == 13750 || id == 13752 || id == 22498
				|| id == 42927 || id == 42932 || id == 42922 || id == 6571
				|| id == 42004 || id == 43273
				|| id == 43227 || id == 43229 || id == 43231 || id == 43233  || id == 49496
				|| id == 52006 || id == 51637 || id == 51730
				|| id == 54268;
	}

	public void addDrops(List<Drop>[] dList) {
		for (int i = 0; i < dList.length; i++) {
			if (dList[i] == null)
				continue;
			if (i >= RARE) {
				ArrayList<Drop> cleanedGear = new ArrayList<Drop>();
				for (Drop drop : dList[i].toArray(new Drop[dList[i].size()])) {
					if (countsAsGear(drop.getItemId()) || ItemConfig.forID(drop.getItemId()).isWearItem()) {
						cleanedGear.add(drop);
						dList[i].remove(drop);
					}
				}
				if (cleanedGear.size() > 0)
					gearRareDrops[i - RARE] = cleanedGear.toArray(new Drop[cleanedGear.size()]);
			}
			drops[i] = dList[i].toArray(new Drop[dList[i].size()]);
		}
	}
	
	
	public static boolean isSeedHerb(Item item) {
		int id = item.getDefinitions().isNoted() ? item.getDefinitions().getCertId() : item.getId();
		return HerbCleaning.getHerb(id) != null;
	}
	
	public static boolean isBone(Item item) {
		int id = item.getDefinitions().isNoted() ? item.getDefinitions().getCertId() : item.getId();
		return Bone.forId(id) != null;
	}
	
	public static boolean isCharm(Item item) {
		for (int id : CHARMS)
			if (id == item.getId())
				return true;
		return item.getId() == 12168;
	}

	public Integer[] dropsToIntArray() {
		return dropsToIntArray(true);
	}
	
	public Integer[] dropsToIntArray(boolean checkAnnounce) {
		ArrayList<Integer> dropList = new ArrayList<Integer>();

		if(this.gearRareDrops != null) {
			for(int i = 0; i <= 1; i++) {
				Drop[] drops = gearRareDrops[i];
				if (drops != null) {
					for(Drop drop : drops)
						if(drop != null && (!checkAnnounce || NPC.announceDrop(-1, drop)))
							dropList.add(drop.getItemId());
				}
			}
		}
		if(this.drops != null) {
			for(int i = 0; i < drops.length; i++) {
				Drop[] drops = this.drops[i];
				if (drops != null) {
					for(Drop drop : drops)
						if(drop != null && ((!checkAnnounce && i >= UNCOMMON) || NPC.announceDrop(-1, drop)))
							dropList.add(drop.getItemId());
				}
			}
		}

		/*ArrayList<Integer> dropList = new ArrayList<>();
		for(int i = 0; i <= 1; i++) {
			Drop[] drops = gearRareDrops[i];
			if (drops != null) {
				for(Drop drop : drops)
					if(drop != null)
						dropList.add(drop.getItemId());
			}
		}*/

		return dropList.toArray(new Integer[dropList.size()]);
	}
}
