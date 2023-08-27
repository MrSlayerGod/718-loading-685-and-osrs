package com.rs.game.player;

import java.io.Serializable;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.utils.Utils;

public class SquealOfFortune implements Serializable {


	public static final boolean DISABLE_SOF = true;
	// sof chances:
	// 100% for common (It's a must to have 100% for common due to at least one
	// reward must be picked)
	// 35% for uncommon
	// 0.089% for rare (0.08% was originally)
	// 0.01% for jackpot

	// version of sof rewards
	public static final int SOF_VERSION = 1;

	//divided chance by 3 now since it was still way too common o.o
	public static final double[] SOF_CHANCES = new double[]
	{ 1.0D, 0.43D, 0.002D, 0.0005D };

	public static final int[] SOF_COMMON_CASH_AMOUNTS = new int[]
	{ 10000, 25000, 50000, 100000 };
	public static final int[] SOF_UNCOMMON_CASH_AMOUNTS = new int[]
	{ 25000, 50000, 100000, 200000 };
	public static final int[] SOF_RARE_CASH_AMOUNTS = new int[]
	{ 500000, 1000000, 1500000, 3000000 };
	//too much moneybeing added from sof
	public static final int[] SOF_JACKPOT_CASH_AMOUNTS = new int[]
	{ 3000000, 4000000, 5000000, 60000000 };
	public static final int[] SOF_COMMON_LAMPS = new int[]
	{ 23713, 23717, 23721, 23725, 23729, 23737, 23733, 23741, 23745, 23749, 23753, 23757, 23761, 23765, 23769, 23778, 23774, 23786, 23782, 23794, 23790, 23802, 23798, 23810, 23806, 23814 };
	public static final int[] SOF_UNCOMMON_LAMPS = new int[]
	{ 23714, 23718, 23722, 23726, 23730, 23738, 23734, 23742, 23746, 23750, 23754, 23758, 23762, 23766, 23770, 23779, 23775, 23787, 23783, 23795, 23791, 23803, 23799, 23811, 23807, 23815 };
	public static final int[] SOF_RARE_LAMPS = new int[]
	{ 23715, 23719, 23723, 23727, 23731, 23739, 23735, 23743, 23747, 23751, 23755, 23759, 23763, 23767, 23771, 23780, 23776, 23788, 23784, 23796, 23792, 23804, 23800, 23812, 23808, 23816 };
	public static final int[] SOF_JACKPOT_LAMPS = new int[]
	{ 23716, 23720, 23724, 23728, 23732, 23740, 23736, 23744, 23748, 23752, 23756, 23760, 23764, 23768, 23773, 23781, 23777, 23789, 23785, 23797, 23793, 23805, 23801, 23813, 23809, 23817 };
	public static final int[] SOF_COMMON_OTHERS = new int[]
	{/* 1965, 1511, 1205, 438, 327, 555, 556, 882, 1925, 314, 313, 436,*/
	1515, 1513, 447, 449, 451, 11212, 11230, 49484, 542, 544, 538, 540, 536, 534, 18830, 41943, 52124, 15332, 6685,
			
	//auras
			20957, 20958, 20961, 20962, 20963, 20964, 20965, 20967, 23848, 22899, 20964, 23840, 22284, 20966, 22883, 22905, 20965, 22280, 22927
			
	
	};
	public static final int[] SOF_UNCOMMON_OTHERS = new int[]
	{ 
			4087, 4585, 4587, 3140, 1149, 1215, 1065, 2487, 2489, 2491, 2493, 2495, 2497,
			24154, 24154, 24155, 24155, /*1119, 1125, 1121, */1123, /*1127, 1131, 1133,*/ 6322, 1135,2499,2501,2503, 12971, 4091, /*1295, 1297, 1299,*/ 1303, 1301, /*1327, 1325,*/ 1331, 1329, /*1311, */1333, /*1315, 1313,*/ 1319, /*1317, 1367,*/
		/*1365, */1371,/* 1369,*/ /*1273, */1373, /*1361, */1271, 1275, /*843, 849, 1355, */1357/*, 9174, 9177, 853, */,857, 9183/*, 9181, 9179,*/,
		
		//auras
		22901, 22907, 23850, 22268, 22274, 22282, 22286, 22290, 23842,  20786, 22276, 22929, 22272, 22270, 22298, 22294, 22300, 22296, 22292
	
	};
	public static final int[] SOF_RARE_OTHERS = new int[]
	{
		// cash
		995, 995, 995, 995, 995, 995, 995, 995,
		// lucky items
		23665, 23666, 23667, 23668, 23669, 23670, 23671, 23672, 23673, 23674, 23675, 23676, 23677, 23678, 23679, 23680, 23681, 23682, 23691, 23692, 23693, 23694, 23695, 23696, 23687, 23688, 23689,
		23684, 23686, 23685, 23697, 23690, 23699, 23700, 23683, 23698,
		// auras
		22919, 22931, 22921, 22887, 23852, 22903, 22909, 22917, 22913, 22923, 22911, 22915, 22925, 23844, 22891, 22893, 22933
		};

	public static final int[] SOF_JACKPOT_OTHERS = new int[]
	{
		// cash
		995, 995, 995, 995, 995, 995, 995, 995,
		// auras
		23862, 23864, 23866, 23870, 23846, 23846, 23858, 23856, 23860, 23868, 23872, 23878, 23874, 23854, 23876, 22889,23880, 23882, 23884, 23886, 23890, 23892, 23894
		// pets
		,21512, 12469, 12471, 12473, 12475

	};

	
	private static final long serialVersionUID = -5330047553089876572L;

	private static final int RARITY_COMMON = 0;
	private static final int RARITY_UNCOMMON = 1;
	private static final int RARITY_RARE = 2;
	private static final int RARITY_JACKPOT = 3;

	private static final int SPIN_TYPE_DAILY = 0;
	private static final int SPIN_TYPE_EARNED = 1;
	private static final int SPIN_TYPE_BOUGHT = 2;

	private static final int SOF_STATUS_CLAIMINVOK = 1;
	private static final int SOF_STATUS_CLAIMINVBAD = 2;
	private static final int SOF_STATUS_CLAIMBANKOK = 3;
	private static final int SOF_STATUS_CLAIMBANKBAD = 4;
	private static final int SOF_STATUS_CLAIMPOUCHOK = 5;
	private static final int SOF_STATUS_CLAIMPOUCHBAD = 6;
	private static final int SOF_STATUS_DISABLED = 7;

	private transient Player player;

	private int version;
	private long lastDailySpinsGiveaway;

	private int dailySpins;
	private int earnedSpins;
	private int boughtSpins;

	private int rewardSlot;
	private int jackpotSlot;
	private Item[] rewards;

	public SquealOfFortune() {
		version = SOF_VERSION;
		lastDailySpinsGiveaway = Utils.currentTimeMillis();
		dailySpins = 0;
		earnedSpins = 0;
		boughtSpins = 0;
		rewardSlot = -1;
		jackpotSlot = -1;
	}

	public void setPlayer(Player player) {
		this.player = player;

	}

	public void processClick(int packetId, int interfaceId, int componentId, int e1, int e2) {
		if (interfaceId == 1139) { // squeal tab
			if (componentId == 18)
				openSpinInterface();
			else {
				player.getPackets().sendOpenURL(Settings.STORE_LINK);
			}
		} else if (interfaceId == 1252) { // squeal overlay
			if (componentId == 5) {
				player.getPackets().sendGameMessage("You can access the Squeal of Fortune from the side panel, and you can show the button again by logging out and back in.");
				player.getInterfaceManager().closeSquealOverlay();
			} else {
				openSpinInterface();
			}
		} else if (interfaceId == 1253) { // squeal main
			if (componentId == 106 || componentId == 258) { // hide/close button
				player.getInterfaceManager().setDefaultRootInterface();
			} else if (componentId == 7 || componentId == 321) { // buy spins on main/reward
				player.getPackets().sendOpenURL(Settings.STORE_LINK);
			} else if (componentId == 93) { // spin button
				if (jackpotSlot != -1 && rewardSlot == -1)
					pickReward();
				else
					openExistingReward();
			}
			//else if (componentId == 93 && rewardSlot != -1) { // they double clicked spin button
			// in rs, it was made that if you double click the spin button it will show rewards instantly
			//	player.getPackets().sendGlobalConfig(1781, -1); // disable spinning
			//	player.getPackets().sendRunScript(5906); // force call to sof_displayPrize();
			//   }
			else if ((componentId == 192 || componentId == 239) && rewardSlot != -1) { // picking reward
				obtainReward(componentId == 239);
			} else if (componentId == 273 && jackpotSlot == -1 && getTotalSpins() > 0) { // play again
				generateRewards(getNextSpinType());
				player.getVarsManager().forceSendVarBit(11155, jackpotSlot + 1);
				player.getPackets().sendItems(665, rewards);
				player.getVarsManager().forceSendVarBit(10861, 0);
				player.getPackets().sendExecuteScript(5879); // sof_setupHooks();
				sendSpinCounts();
			}
		}
	}

	public void processItemClick(int slotId, int itemId, Item item) {
		if (itemId == 24154 || itemId == 24155) { // spin ticket and double spin ticket
			player.getInventory().deleteItem(itemId, 1);
		//	giveEarnedSpins(itemId == 24154 ? 1 : 2);
		//	this.giveBoughtSpins((itemId == 24154 ? 1 : 2) * item.getAmount());
			player.getInventory().addItemMoneyPouch(new Item(995, itemId == 24154 ? 500000 : 1000000));
		}
	}

	private void pickReward() {
		if (!useSpin())
			return;
		int rewardRarity = RARITY_COMMON;
		double roll = Utils.randomDouble();
		if (roll <= SOF_CHANCES[RARITY_JACKPOT])
			rewardRarity = RARITY_JACKPOT; // we have a winner here...
		else if (roll <= SOF_CHANCES[RARITY_RARE])
			rewardRarity = RARITY_RARE;
		else if (roll <= SOF_CHANCES[RARITY_UNCOMMON])
			rewardRarity = RARITY_UNCOMMON;

		int[] possibleSlots = new int[13];
		int possibleSlotsCount = 0;
		for (int i = 0; i < 13; i++) {
			if (getSlotRarity(i, jackpotSlot) == rewardRarity)
				possibleSlots[possibleSlotsCount++] = i;
		}

		rewardSlot = possibleSlots[Utils.random(possibleSlotsCount)];

		if (rewardRarity >= RARITY_RARE) {
			announceWin();
		}

		player.getVarsManager().forceSendVarBit(10860, rewardSlot);
		player.getVarsManager().forceSendVarBit(10861, 1); // block spin & set reward
		player.getPackets().sendCSVarInteger(1790, getRewardStatusType());
		player.getPackets().sendCSVarInteger(1781, getBestRewardSpoofSlot());

	}

	private void obtainReward(boolean discard) {
		int type = getRewardStatusType();
		if ((discard && type == SOF_STATUS_DISABLED) || (!discard && type != SOF_STATUS_CLAIMINVOK && type != SOF_STATUS_CLAIMPOUCHOK && type != SOF_STATUS_CLAIMBANKOK))
			return;

		player.getVarsManager().forceSendVarBit(10861, 0); // prepare for next spin
		player.getPackets().sendCSVarInteger(1790, 0);
		player.getPackets().sendItems(665, new Item[13]);

		if (!discard) {
			if (type == SOF_STATUS_CLAIMPOUCHOK) {
				player.getInventory().addItemMoneyPouch(rewards[rewardSlot]);
			} else if (type == SOF_STATUS_CLAIMINVOK) {
				player.getInventory().addItem(rewards[rewardSlot]);
			} else if (type == SOF_STATUS_CLAIMBANKOK) {
				player.getBank().addItem(rewards[rewardSlot].getId(), rewards[rewardSlot].getAmount(), false);
			}
		}

		rewards = null;
		jackpotSlot = -1;
		rewardSlot = -1;
	}

	public void forceBankReward() {
		player.getVarsManager().forceSendVarBit(10861, 0); // prepare for next spin
		player.getPackets().sendCSVarInteger(1790, 0);
		player.getPackets().sendItems(665, new Item[13]);
		player.getBank().addItem(rewards[rewardSlot].getId(), rewards[rewardSlot].getAmount(), false);
		rewards = null;
		jackpotSlot = -1;
		rewardSlot = -1;
	}

	private void announceWin() {
		Item item = rewards[rewardSlot];
		if (item.getDefinitions().isStackable() || item.getDefinitions().isNoted() || item.getAmount() > 1) {
			World.sendNews(player, player.getDisplayName() + " <col=ff7200>has just won <col=ff0000>" + Utils.getFormattedNumber(item.getAmount()) + " x " + item.getName() + "<col=ff7200> on Squeal of Fortune!", World.WORLD_NEWS);
		} else {
			World.sendNews(player, player.getDisplayName() + " <col=ff7200>has just won <col=ff0000>" + item.getName() + "<col=ff7200> on Squeal of Fortune!", 0);
		}

	}

	public void openSpinInterface() {
		/*if (player.isIronman()) {
			player.getPackets().sendGameMessage("You can't use this feature as an ironman.");
			return;
		}*/
		if (player.getInterfaceManager().containsInventoryInter() || player.getInterfaceManager().containsScreenInter()) {
			player.getPackets().sendGameMessage("Please finish what you are doing before opening Squeal of Fortune.");
			return;
		}
		if (player.getControlerManager().getControler() != null) {
			player.getPackets().sendGameMessage("You can't open Squeal of Fortune in this area.");
			return;
		}
		
		
		player.stopAll();
		
		if (version != SOF_VERSION) {
			dailySpins = Math.min(dailySpins, 3);
			earnedSpins = Math.min(earnedSpins, 20);
			boughtSpins = Math.min(boughtSpins, 20);
			rewardSlot = -1;
			jackpotSlot = -1;
			rewards = null;
			
			version = SOF_VERSION;
			player.getPackets().sendGameMessage("Squeal of fortune has been updated, and spins have been reduced to 20.");
		}

		sendSpinCounts();
		if (rewardSlot != -1) {
			openExistingReward();
		} else if (getTotalSpins() < 1) {
			openNoSpinsLeft();
		} else {
			openSpin();
		}
	}

	private void openExistingReward() {
		player.getVarsManager().forceSendVarBit(11155, jackpotSlot + 1); // need to send all items because otherwise it will set wrong color for rarity etc
		player.getPackets().sendItems(665, rewards);
		player.getPackets().sendRootInterface(1253, 0);
		player.getVarsManager().forceSendVarBit(10860, rewardSlot);
		player.getVarsManager().forceSendVarBit(10861, 1); // block spin & set reward
		player.getPackets().sendCSVarInteger(1790, getRewardStatusType());
		player.getPackets().sendExecuteScript(5906); // force call to sof_displayPrize();

	}

	private void openNoSpinsLeft() {
		player.getVarsManager().forceSendVarBit(11155, Utils.random(13) + 1);
		player.getPackets().sendItems(665, new Item[13]);
		player.getPackets().sendRootInterface(1253, 0);
		player.getVarsManager().forceSendVarBit(10861, 0);
		player.getPackets().sendCSVarInteger(1790, 0);
		player.getPackets().sendExecuteScript(5906); // force call to sof_displayPrize();
	}

	private void openSpin() {
		if (rewards == null) {
			generateRewards(getNextSpinType());
		}

		player.getVarsManager().forceSendVarBit(11155, jackpotSlot + 1);
		player.getPackets().sendItems(665, rewards);
		player.getPackets().sendRootInterface(1253, 0);
		player.getVarsManager().forceSendVarBit(10861, 0); // force allow spin
	}

	public void sendSpinCounts() {
		player.getVarsManager().forceSendVarBit(10862, dailySpins);
		player.getVarsManager().forceSendVarBit(11026, earnedSpins);
		player.getPackets().sendCSVarInteger(1800, boughtSpins);
		// must send all three otherwise it wont trigger refresh code @ cs2
	}

	private void generateRewards(int spinType) {
		jackpotSlot = Utils.random(13);
		rewards = new Item[13];
		for (int i = 0; i < rewards.length; i++)
			rewards[i] = generateReward(spinType, getSlotRarity(i, jackpotSlot));
	}

	private Item generateReward(int spinType, int rarityType) {
		// TODO different rewards depending on spin type.
		boolean isLamp = Utils.random(2) == 0; // 50% lamp
		if (isLamp) {
			int[] lamps = SOF_COMMON_LAMPS;
			if (rarityType == RARITY_JACKPOT)
				lamps = SOF_JACKPOT_LAMPS;
			else if (rarityType == RARITY_RARE)
				lamps = SOF_RARE_LAMPS;
			else if (rarityType == RARITY_UNCOMMON)
				lamps = SOF_UNCOMMON_LAMPS;

			return new Item(lamps[Utils.random(lamps.length)], 1);
		} else {
			int[] items = SOF_COMMON_OTHERS;
			if (rarityType == RARITY_JACKPOT)
				items = SOF_JACKPOT_OTHERS;
			else if (rarityType == RARITY_RARE)
				items = SOF_RARE_OTHERS;
			else if (rarityType == RARITY_UNCOMMON)
				items = SOF_UNCOMMON_OTHERS;

			int itemId = items[Utils.random(items.length)];
			int amount;
			if (itemId == 995) {
				int[] amounts = SOF_COMMON_CASH_AMOUNTS;
				if (rarityType == RARITY_JACKPOT)
					amounts = SOF_JACKPOT_CASH_AMOUNTS;
				else if (rarityType == RARITY_RARE)
					amounts = SOF_RARE_CASH_AMOUNTS;
				else if (rarityType == RARITY_UNCOMMON)
					amounts = SOF_UNCOMMON_CASH_AMOUNTS;
				amount = amounts[Utils.random(amounts.length)];
			} else {
				ItemConfig defs = ItemConfig.forID(itemId);
				amount = rarityType > RARITY_COMMON || (!defs.isStackable() && !defs.isNoted()) ? 1 : (Utils.random(10) + 1);
			}

			return new Item(itemId, amount);
		}
	}

	private int getRewardStatusType() {
		if (!Settings.SQUEAL_OF_FORTUNE_ENABLED)
			return SOF_STATUS_DISABLED;

		Item reward = rewards[rewardSlot];
		if (reward.getId() == 995) { // coins go to pouch
			long amt = player.getMoneyPouch().getCoinsAmount() + reward.getAmount();
			return amt > Integer.MAX_VALUE || amt <= 0 ? SOF_STATUS_CLAIMPOUCHBAD : SOF_STATUS_CLAIMPOUCHOK;
		} else if (!reward.getDefinitions().isStackable() && !reward.getDefinitions().isNoted() && reward.getAmount() == 1) { // non stackable items to inv
			return player.getInventory().hasFreeSlots() ? SOF_STATUS_CLAIMINVOK : SOF_STATUS_CLAIMINVBAD;
		} else { // other items go to bank
			if (player.getBank().getItem(reward.getId()) != null) {
				long amt = player.getBank().getItem(reward.getId()).getAmount() + reward.getAmount();
				return amt > Integer.MAX_VALUE || amt <= 0 ? SOF_STATUS_CLAIMBANKBAD : SOF_STATUS_CLAIMBANKOK;
			} else
				return player.getBank().hasBankSpace() ? SOF_STATUS_CLAIMBANKOK : SOF_STATUS_CLAIMBANKBAD;
		}
	}

	private int getBestRewardSpoofSlot() {
		int wonRarity = getSlotRarity(rewardSlot, jackpotSlot);
		if (wonRarity == RARITY_JACKPOT)
			return rewardSlot; // nothing to spoof
		int spoofMinType = wonRarity == RARITY_RARE ? RARITY_JACKPOT : RARITY_RARE;
		int bestSlot = -1;
		int bestDistance = Integer.MAX_VALUE;
		for (int i = 0; i < 13; i++) {
			if (i == rewardSlot || getSlotRarity(i, jackpotSlot) < spoofMinType)
				continue; // skip self & not rare
			int distance = distanceTo(i, rewardSlot);
			if (bestSlot == -1 || distance < bestDistance) {
				bestSlot = i;
				bestDistance = distance;
			}
		}

		return bestSlot;
	}

	private int distanceTo(int from, int to) {
		if (from == to)
			return 0;
		else if (from > to)
			return (13 - from) + to;
		else
			// (from < to)
			return (to - from);
	}

	private int getSlotRarity(int slot, int jackpotSlot) {
		if (slot == jackpotSlot) // jackpot overrides the slot
			return RARITY_JACKPOT;
		switch (slot) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 10:
		case 12:
			return RARITY_COMMON;
		case 2:
		case 6:
		case 9:
		case 11:
			return RARITY_UNCOMMON;
		case 0:
		case 4:
		case 8:
			return RARITY_RARE;
		default: // default case added so compiler can add tableswitch instruction instead of lookupswitch
			throw new RuntimeException("Bad slot");
		}
	}

	private int getTotalSpins() {
		return dailySpins + earnedSpins + boughtSpins;
	}

	private int getNextSpinType() {
		if (dailySpins > 0)
			return SPIN_TYPE_DAILY;
		else if (earnedSpins > 0)
			return SPIN_TYPE_EARNED;
		else if (boughtSpins > 0)
			return SPIN_TYPE_BOUGHT;
		else
			return -1;
	}

	private boolean useSpin() {
		int type = getNextSpinType();
		if (type == -1)
			return false;

		if (type == SPIN_TYPE_DAILY)
			setDailySpins(dailySpins - 1);
		else if (type == SPIN_TYPE_EARNED)
			setEarnedSpins(earnedSpins - 1);
		else if (type == SPIN_TYPE_BOUGHT)
			setBoughtSpins(boughtSpins - 1);

		return true;
	}

	/**
	 * Give's daily spins for donators, only if daily spins < 2.
	 */
	public void giveDailySpins() {
		if (DISABLE_SOF || (Utils.currentTimeMillis() - lastDailySpinsGiveaway) < (24 * 60 * 60 * 1000)) // 24 hours
			return;
		lastDailySpinsGiveaway = Utils.currentTimeMillis();
		int previous = dailySpins;
		
		dailySpins += player.getDonator() + 1;

		if (dailySpins > 8) // max limit of daily spins bitconfig is 8
			dailySpins = 8;

		if (dailySpins > previous) {
		/*	if (player.isDonator())
				player.getPackets().sendGameMessage("<col=FF0000>You have been awarded " + (dailySpins - previous) + " squeal of fortune spins for being " + (player.isExtremeDonator() ? "extreme donator" : "donator") + ".");
			else*/
				player.getPackets().sendGameMessage("<col=FF0000>You have been awarded " + (dailySpins - previous) + " squeal of fortune spins.");
			player.getPackets().sendGameMessage("<col=FF0000>Click on the squeal of fortune tab to use them.");
		}

		if (dailySpins != previous)
			sendSpinCounts();
	}

	public void giveEarnedSpins(int amount) {
		if (true)
			return;
		int previous = earnedSpins;
		earnedSpins += amount;
		if (earnedSpins > previous) {
			player.getPackets().sendGameMessage("<col=FF0000>You have earned " + (earnedSpins - previous) + " squeal of fortune spins.");
			player.getPackets().sendGameMessage("<col=FF0000>Click on the squeal of fortune tab to use them.");
		}

		if (earnedSpins != previous)
			sendSpinCounts();
	}

	public void giveBoughtSpins(int amount) {
		int previous = boughtSpins;
		boughtSpins += amount;
		if (boughtSpins > previous) { //bought
			player.getPackets().sendGameMessage("<col=FF0000>You have received " + (boughtSpins - previous) + " squeal of fortune spins.");
			player.getPackets().sendGameMessage("<col=FF0000>Click on the squeal of fortune tab to use them.");
		}

		if (boughtSpins != previous)
			sendSpinCounts();
	}

	public void resetSpins() {
		dailySpins = 0;
		earnedSpins = 0;
		boughtSpins = 0;
		sendSpinCounts();
	}

	public int getDailySpins() {
		return dailySpins;
	}

	public void setDailySpins(int dailySpins) {
		int previous = this.dailySpins;
		this.dailySpins = dailySpins;
		if (this.dailySpins != previous)
			sendSpinCounts();
	}

	public int getEarnedSpins() {
		return earnedSpins;
	}

	public void setEarnedSpins(int earnedSpins) {
		int previous = this.earnedSpins;
		this.earnedSpins = earnedSpins;
		if (this.earnedSpins != previous)
			sendSpinCounts();
	}

	public int getBoughtSpins() {
		return boughtSpins;
	}

	public void setBoughtSpins(int boughtSpins) {
		int previous = this.boughtSpins;
		this.boughtSpins = boughtSpins;
		if (this.boughtSpins != previous)
			sendSpinCounts();
	}

	public boolean autospin() {

		int spins = player.getSquealOfFortune().getBoughtSpins()
				+ player.getSquealOfFortune().getDailySpins()
				+ player.getSquealOfFortune().getEarnedSpins();

		if(spins == 0) {
			player.sendMessage("You have no spins left.");
			return false;
		}

		generateRewards(getNextSpinType());
		player.getVarsManager().forceSendVarBit(11155, jackpotSlot + 1);
		player.getPackets().sendItems(665, rewards);
		player.getVarsManager().forceSendVarBit(10861, 0);
		player.getPackets().sendExecuteScript(5879); // sof_setupHooks();

		pickReward();

		Item item = new Item(rewards[rewardSlot].getId(), rewards[rewardSlot].getAmount());
		player.sendMessage("You've won ... <col=ffff00>" + item.getAmount() + "</col> x <col=ffff00>" + item.getName() + "</col>!");
		forceBankReward();
		return true;
	}
}
