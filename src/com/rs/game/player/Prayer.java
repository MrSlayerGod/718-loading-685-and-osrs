package com.rs.game.player;

import java.io.Serializable;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.World;
import com.rs.game.minigames.clanwars.ClanWars;
import com.rs.game.minigames.clanwars.ClanWars.Rules;
import com.rs.game.npc.NPC;
import com.rs.game.npc.dungeonnering.DungeonBoss;
import com.rs.game.npc.nightmare.TheNightmare;
import com.rs.game.npc.others.WildyWyrm;
import com.rs.game.npc.randomEvent.CombatEventNPC;
import com.rs.game.npc.theatreOfBlood.TOBBoss;
import com.rs.game.player.QuestManager.Quests;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class Prayer implements Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = -2082861520556582824L;

	private final static int[][] prayerLvls =
	{
		// normal prayer book
		{ 1, 4, 7, 8, 9, 10, 13, 16, 19, /*22,*/ 25, 26, 27, 28, 31, 34, 35, 37, 40, 43, 44, 45, 46, 49, 52, 55, 60, 65, 70, 74, 77 },
		// ancient prayer book
		{ 50, 50, 52, 54, 56, 59, 62, 65, 68, 71, 74, 76, 78, 80, 82, 84, 86, 89, 92, 95 } };

	private final static int[][][] closePrayers =
	{
	{ // normal prayer book
		{ 0, 5, 12 }, // Skin prayers 0
			{ 1, 6, 13 }, // Strength prayers 1
			{ 2, 7, 14 }, // Attack prayers 2
			{ 3, 10, 19 }, // Range prayers 3
			{ 4, 11, 20 }, // Magic prayers 4
			{ 8, /*9,*/ 26 }, // Restore prayers 5
			{ 9 }, // Protect item prayers 6
			{ 16, 17, 18 }, // Protect prayers 7
			{ 15 }, // Other protect prayers 8
			{ 21, 22, 23 }, // Other special prayers 9
			{ 25, 27, 28, 29 }, // Other prayers 10
			{ 24 }, //boost prayers 11
		},
		{ // ancient prayer book
		{ 0 }, // Protect item prayers 0
			{ 1, 2, 3, 4 }, // sap prayers 1
			{ 5 }, // other prayers 2
			{ 7, 8, 9, 17, 18 }, // protect prayers 3
			{ 6 }, // other protect prayers 4
			{ 10, 11, 12, 13, 14, 15, 16 }, // leech prayers 5
			{ 19 }, // other prayers
		} };

	private final static int[] prayerSlotValues =
	{
		1,
		2,
		4,
		262144,
		524288,
		8,
		16,
		32,
		64,
		//128,//fast heal disabled
		256,
		1048576,
		2097152,
		512,
		1024,
		2048,
		16777216,
		4096,
		8192,
		16384,
		4194304,
		8388608,
		32768,
		65536,
		131072,
		128,//preserve
		33554432,
		134217728,
		67108864,
		268435456 * 2,
		268435456 };

	private final static double[][] prayerDrainRate =
	{
	{ 1.2, 1.2, 1.2, 1.2, 1.2, 0.6, 0.6, 0.6, 3.6, /*1.8, */1.8, 0.6, 0.6, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 1.2, 0.6, 0.18, 1.8, 0.18, 0.24, 0.15, 0.2, 0.18 },
	{ 1.8, 0.24, 0.24, 0.24, 0.24, 1.8, 0.3, 0.3, 0.3, 0.3, 0.36, 0.36, 0.36, 0.36, 0.36, 0.36, 0.36, 1.2, 0.2, 0.2 } };

	private transient Player player;
	private transient boolean[][] onPrayers;
	private transient boolean usingQuickPrayer;
	private transient int onPrayersCount;

	private boolean[][] quickPrayers;
	private int prayerpoints;
	private transient int[] leechBonuses;
	private boolean ancientcurses;
	private transient long[] nextDrain;
	private transient boolean boostedLeech;

	public double getMageMultiplier() {
		if (onPrayersCount == 0)
			return 1.0;
		double value = 1.0;

		// normal
		if (usingPrayer(0, 4))
			value += 0.05;
		else if (usingPrayer(0, 11))
			value += 0.10;
		else if (usingPrayer(0, 20))
			value += 0.15;
		else if (usingPrayer(0, 29))
			value += 0.25;
		else if (usingPrayer(1, 3)) {
			double d = (leechBonuses[2]);
			value += d / 100;
		} else if (usingPrayer(1, 12)) {
			double d = (5 + leechBonuses[5]);
			value += d / 100;
		}
		return value;
	}
	
	public double getRangeStrengthMultiplier() {
		double value = getRangeMultiplier();
		if (usingPrayer(0, 28))
			value += 0.03;
		return value;
	}

	public double getRangeMultiplier() {
		if (onPrayersCount == 0)
			return 1.0;
		double value = 1.0;

		// normal
		if (usingPrayer(0, 3))
			value += 0.05;
		else if (usingPrayer(0, 10))
			value += 0.10;
		else if (usingPrayer(0, 19))
			value += 0.15;
		else if (usingPrayer(0, 28))
			value += 0.20;
		else if (usingPrayer(1, 2)) {
			double d = (leechBonuses[1]);
			value += d / 100;
		} else if (usingPrayer(1, 11)) {
			double d = (5 + leechBonuses[4]);
			value += d / 100;
		}
		return value;
	}

	public double getAttackMultiplier() {
		if (onPrayersCount == 0)
			return 1.0;
		double value = 1.0;

		// normal
		if (usingPrayer(0, 2))
			value += 0.05;
		else if (usingPrayer(0, 7))
			value += 0.10;
		else if (usingPrayer(0, 14))
			value += 0.15;
		else if (usingPrayer(0, 25))
			value += 0.15;
		else if (usingPrayer(0, 27))
			value += 0.20;
		else if (usingPrayer(1, 1)) {
			double d = (leechBonuses[0]);
			value += d / 100;
		} else if (usingPrayer(1, 10)) {
			double d = (5 + leechBonuses[3]);
			value += d / 100;
		} else if (usingPrayer(1, 19)) {
			double d = (15 + leechBonuses[8]);
			value += d / 100;
		}
		return value;
	}

	public double getStrengthMultiplier() {
		if (onPrayersCount == 0)
			return 1.0;
		double value = 1.0;

		// normal
		if (usingPrayer(0, 1))
			value += 0.05;
		else if (usingPrayer(0, 6))
			value += 0.10;
		else if (usingPrayer(0, 13))
			value += 0.15;
		else if (usingPrayer(0, 25))
			value += 0.18;
		else if (usingPrayer(0, 27))
			value += 0.23;
		else if (usingPrayer(1, 1)) {
			double d = (leechBonuses[0]);
			value += d / 100;
		} else if (usingPrayer(1, 14)) {
			double d = (5 + leechBonuses[7]);
			value += d / 100;
		} else if (usingPrayer(1, 19)) {
			double d = (23 + leechBonuses[10]);
			value += d / 100;
		}
		return value;
	}

	public double getDefenceMultiplier() {
		if (onPrayersCount == 0)
			return 1.0;
		double value = 1.0;

		// normal
		if (usingPrayer(0, 0))
			value += 0.05;
		else if (usingPrayer(0, 5))
			value += 0.10;
		else if (usingPrayer(0, 12))
			value += 0.15;
		else if (usingPrayer(0, 25))
			value += 0.20;
		else if (usingPrayer(0, 27))
			value += 0.25;
		else if (usingPrayer(0, 28))
			value += 0.25;
		else if (usingPrayer(0, 29))
			value += 0.25;
		else if (usingPrayer(1, 1)) {
			double d = (leechBonuses[0]);
			value += d / 100;
		} else if (usingPrayer(1, 13)) {
			double d = (6 + leechBonuses[6]);
			value += d / 100;
		} else if (usingPrayer(1, 19)) {
			double d = (15 + leechBonuses[9]);
			value += d / 100;
		}
		return value;
	}

	public boolean reachedMax(int bonus) {
		if (bonus != 8 && bonus != 9 && bonus != 10)
			return leechBonuses[bonus] >= 20;
		else
			return false;
	}

	public void increaseLeechBonus(int bonus) {
		leechBonuses[bonus]++;
		if (bonus == 0) {
			adjustStat(0, leechBonuses[bonus]);
			adjustStat(1, leechBonuses[bonus]);
			adjustStat(2, leechBonuses[bonus]);
		} else if (bonus == 1) {
			adjustStat(2, leechBonuses[bonus]);
			adjustStat(3, leechBonuses[bonus]);
		} else if (bonus == 2) {
			adjustStat(2, leechBonuses[bonus]);
			adjustStat(4, leechBonuses[bonus]);
		} else if (bonus == 3)
			adjustStat(0, leechBonuses[bonus]);
		else if (bonus == 4)
			adjustStat(3, leechBonuses[bonus]);
		else if (bonus == 5)
			adjustStat(4, leechBonuses[bonus]);
		else if (bonus == 6)
			adjustStat(2, leechBonuses[bonus]);
		else if (bonus == 7)
			adjustStat(1, leechBonuses[bonus]);
	}

	public void increaseTurmoilBonus(Entity entity) {
		if (entity instanceof Player) {
			Player p2 = (Player) entity;
			leechBonuses[8] = (int) Math.floor(0.1 * p2.getSkills().getLevelForXp(Skills.ATTACK));
			leechBonuses[9] = (int) Math.floor(0.1 * p2.getSkills().getLevelForXp(Skills.DEFENCE));
			leechBonuses[10] = (int) Math.floor(0.1 * p2.getSkills().getLevelForXp(Skills.STRENGTH));
		} else if (entity instanceof NPC) {
			int combatLevel = ((NPC) entity).getCombatLevel();
			int level = combatLevel * 99 / (138 * 2);
			if (level > 99)
				level = 99;
			leechBonuses[8] = leechBonuses[9] = leechBonuses[10] = (int) Math.floor(0.1 * level);
		}
		adjustStat(0, leechBonuses[8]);
		adjustStat(1, leechBonuses[10]);
		adjustStat(2, leechBonuses[9]);
	}

	public void adjustStat(int stat, int percentage) {
		player.getVarsManager().sendVarBit(6857 + stat, 30 + percentage);
	}

	public void closePrayers(int prayerId) {
		if (ancientcurses) {
			if (prayerId == 1) {
				if (leechBonuses[0] > 0)
					player.getPackets().sendGameMessage("Your Attack is now unaffected by sap and leech curses.", true);
				adjustStat(0, 0);
				adjustStat(1, 0);
				adjustStat(2, 0);
				leechBonuses[0] = 0;
			} else if (prayerId == 2) {
				if (leechBonuses[1] > 0)
					player.getPackets().sendGameMessage("Your Range is now unaffected by sap and leech curses.", true);
				adjustStat(2, 0);
				adjustStat(4, 0);
				leechBonuses[1] = 0;
			} else if (prayerId == 3) {
				if (leechBonuses[2] > 0)
					player.getPackets().sendGameMessage("Your Magic is now unaffected by sap and leech curses.", true);
				adjustStat(2, 0);
				adjustStat(5, 0);
				leechBonuses[2] = 0;
			} else if (prayerId == 10) {
				if (leechBonuses[3] > 0)
					player.getPackets().sendGameMessage("Your Attack is now unaffected by sap and leech curses.", true);
				adjustStat(0, 0);
				leechBonuses[3] = 0;
			} else if (prayerId == 11) {
				if (leechBonuses[4] > 0)
					player.getPackets().sendGameMessage("Your Ranged is now unaffected by sap and leech curses.", true);
				adjustStat(4, 0);
				leechBonuses[4] = 0;
			} else if (prayerId == 12) {
				if (leechBonuses[5] > 0)
					player.getPackets().sendGameMessage("Your Magic is now unaffected by sap and leech curses.", true);
				adjustStat(5, 0);
				leechBonuses[5] = 0;
			} else if (prayerId == 13) {
				if (leechBonuses[6] > 0)
					player.getPackets().sendGameMessage("Your Defence is now unaffected by sap and leech curses.", true);
				adjustStat(2, 0);
				leechBonuses[6] = 0;
			} else if (prayerId == 14) {
				if (leechBonuses[7] > 0)
					player.getPackets().sendGameMessage("Your Strength is now unaffected by sap and leech curses.", true);
				adjustStat(1, 0);
				leechBonuses[7] = 0;
			} else if (prayerId == 19) {
				leechBonuses[8] = 0;
				leechBonuses[9] = 0;
				leechBonuses[10] = 0;
				adjustStat(0, 0);
				adjustStat(1, 0);
				adjustStat(2, 0);
			}
		}
	}

	public int getPrayerHeadIcon() {
		if (onPrayersCount == 0)
			return -1;
		int value = -1;
		if (usingPrayer(0, 15))
			value += 8;
		if (usingPrayer(0, 16))
			value += 3;
		else if (usingPrayer(0, 17))
			value += 2;
		else if (usingPrayer(0, 18))
			value += 1;
		else if (usingPrayer(0, 21))
			value += 4;
		else if (usingPrayer(0, 22))
			value += 6;
		else if (usingPrayer(0, 23))
			value += 5;
		else if (usingPrayer(1, 6)) {
			value += 16;
			if (usingPrayer(1, 8))
				value += 2;
			else if (usingPrayer(1, 7))
				value += 3;
			else if (usingPrayer(1, 9))
				value += 1;
		} else if (usingPrayer(1, 7))
			value += 14;
		else if (usingPrayer(1, 8))
			value += 15;
		else if (usingPrayer(1, 9))
			value += 13;
		else if (usingPrayer(1, 17))
			value += 20;
		else if (usingPrayer(1, 18))
			value += 21;
		return value;
	}

	public void switchSettingQuickPrayer() {
		usingQuickPrayer = !usingQuickPrayer;
		player.getPackets().sendCSVarInteger(181, usingQuickPrayer ? 1 : 0);// activates
		// quick
		// choose
		unlockPrayerBookButtons();
		if (usingQuickPrayer) // switchs tab to prayer
			player.getPackets().sendCSVarInteger(168, 6);
	}

	private transient boolean quickPrayerOn;

	public void switchQuickPrayers() {
		boolean hasQuickPrayers = false;
		for (boolean prayer : quickPrayers[getPrayerBook()]) {
			if (prayer) {
				hasQuickPrayers = true;
				break;
			}
		}
		if (!hasQuickPrayers) {
			player.getPackets().sendGameMessage("You don't have any quick prayers selected.");
			return;
		}
		if (!checkPrayer())
			return;
		if (player.getCurrentFriendsChat() != null) {
			ClanWars war = player.getCurrentFriendsChat().getClanWars();
			if (war != null && war.get(Rules.NO_PRAYER) && (war.getFirstPlayers().contains(player) || war.getSecondPlayers().contains(player))) {
				player.getPackets().sendGameMessage("Prayer has been disabled during this war.");
				return;
			}
		}
		quickPrayerOn = !quickPrayerOn;
		if (hasPrayersOn())
			closeAllPrayers(!quickPrayerOn);
		if (quickPrayerOn) {
			int index = 0;
			for (boolean prayer : quickPrayers[getPrayerBook()]) {
				if (prayer)
					usePrayer(index);
				index++;
			}
			player.getPackets().sendCSVarInteger(182, 1);
			recalculatePrayer();
		}
	}

	private void closePrayers(int[]... prayers) {
		for (int[] prayer : prayers)
			for (int prayerId : prayer)
				if (usingQuickPrayer)
					quickPrayers[getPrayerBook()][prayerId] = false;
				else {
					if (onPrayers[getPrayerBook()][prayerId])
						onPrayersCount--;
					onPrayers[getPrayerBook()][prayerId] = false;
					closePrayers(prayerId);

				}
	}

	public void closeProtectionPrayers() {
		if (getPrayerBook() == 1)
			closePrayers(closePrayers[1][3], closePrayers[1][4]);
		else
			closePrayers(closePrayers[0][7], closePrayers[0][8]);
		recalculatePrayer();
		player.getAppearence().generateAppearenceData();
	}
	
	public void closeProtectionPrayer(int id) {
		closePrayers(new int[] { id });
		recalculatePrayer();
		player.getAppearence().generateAppearenceData();
	}

	public void switchPrayer(int prayerId) {
		if (!usingQuickPrayer)
			if (!checkPrayer())
				return;
		usePrayer(prayerId);
		recalculatePrayer();
	}

	private boolean usePrayer(int prayerId) {
		if (prayerId < 0 || prayerId >= prayerLvls[getPrayerBook()].length)
			return false;
		if (getPrayerBook() == 0) {
			if (prayerId == 24) {
				if (!player.isPreserveUnlocked()) {
					quickPrayers[0][24] = false;//failsafe for people who had it selected before disabling
					player.getDialogueManager().startDialogue("SimpleMessage",
							"You need a <col=0040ff>Prayer</col> level of 55, and to have learnt the prayer ", "in order to use <col=0040ff>Preserve</col>.");
					return false;
				}
			} else if (prayerId == 25) {
				if (/*!player.getQuestManager().completedQuest(Quests.KINGS_RANSOM)*/
						player.getSkills().getLevelForXp(Skills.DEFENCE) < 65
						|| player.getSkills().getLevelForXp(Skills.MAGIC) < 45) { //and have completed the King's Ransom quest's Knight Wave reward 
					player.getPackets().sendGameMessage("You need a Prayer level of 60, a Defence level of 65 and a Magic level of 45 to use Chivalry.");
					return false;
				}
			} else if (prayerId == 27) {
				if (/*!player.getQuestManager().completedQuest(Quests.KINGS_RANSOM) || */
						
						
						player.getSkills().getLevelForXp(Skills.DEFENCE) < 70
						|| player.getSkills().getLevelForXp(Skills.MAGIC) < 45
						) {
					player.getPackets().sendGameMessage("You need a Prayer level of 70, a Defence level of 70 and a Magic level of 45 to use Piety.");
					return false;
				}
			} else if (prayerId == 28) {
				if (player.getSkills().getLevelForXp(Skills.DEFENCE) < 70 || !player.isRigourUnlocked()) {
					quickPrayers[0][28] = false;//failsafe for people who had it selected before disabling
					player.getDialogueManager().startDialogue("SimpleMessage",
							"You need a <col=0040ff>Prayer</col> level of 74, a Defence level of 70, and to have ", "learnt the prayer in order to use <col=0040ff>Rigour</col>.");
					return false;
				}
			} else if (prayerId == 29) {
				if (player.getSkills().getLevelForXp(Skills.DEFENCE) < 70 || !player.isAuguryUnlocked()) {
					quickPrayers[0][29] = false;//failsafe for people who had it selected before disabling
					player.getDialogueManager().startDialogue("SimpleMessage",
							"You need a <col=0040ff>Prayer</col> level of 77, a Defence level of 70, and to have", "learnt the prayer in order to use <col=0040ff>Augury</col>.");
					return false;
				}
			}
		} else if (getPrayerBook() == 1) {
			if (player.getSkills().getLevelForXp(Skills.DEFENCE) < 30) {
				player.getPackets().sendGameMessage("You need a defence level of at least 30 to use this prayer.");
				return false;
			}
		}
		if (player.getSkills().getLevelForXp(5) < prayerLvls[this.getPrayerBook()][prayerId]) {
			player.getPackets().sendGameMessage("You need a prayer level of at least " + prayerLvls[getPrayerBook()][prayerId] + " to use this prayer.");
			return false;
		}
		if (player.isPrayerBlocked()) {
			if ((ancientcurses && prayerId >= 6 && prayerId <= 9) || (!ancientcurses && prayerId >= 15 && prayerId <= 18)) {
				player.getPackets().sendGameMessage("You are currently injured and cannot use protection prayers!");
				return false;
			}
		}
		if (player.getCurrentFriendsChat() != null) {
			ClanWars war = player.getCurrentFriendsChat().getClanWars();
			if (war != null && war.get(Rules.NO_PRAYER) && (war.getFirstPlayers().contains(player) || war.getSecondPlayers().contains(player))) {
				player.getPackets().sendGameMessage("Prayer has been disabled during this war.");
				return false;
			}
		}
		if (!usingQuickPrayer) {
			if (onPrayers[getPrayerBook()][prayerId]) {
				onPrayers[getPrayerBook()][prayerId] = false;
				closePrayers(prayerId);
				onPrayersCount--;
				player.getAppearence().generateAppearenceData();
				player.getPackets().sendSound(2663, 0, 1);
				return true;
			}
		} else {
			if (quickPrayers[getPrayerBook()][prayerId]) {
				quickPrayers[getPrayerBook()][prayerId] = false;
				player.getPackets().sendSound(2663, 0, 1);
				return true;
			}
		}
		boolean needAppearenceGenerate = false;
		if (getPrayerBook() == 0) {
			switch (prayerId) {
			case 0:
			case 5:
			case 12:
				closePrayers(closePrayers[getPrayerBook()][0], closePrayers[getPrayerBook()][10]);
				break;
			case 1:
			case 6:
			case 13:
				closePrayers(closePrayers[getPrayerBook()][1], closePrayers[getPrayerBook()][3], closePrayers[getPrayerBook()][4], closePrayers[getPrayerBook()][10]);
				break;
			case 2:
			case 7:
			case 14:
				closePrayers(closePrayers[getPrayerBook()][2], closePrayers[getPrayerBook()][3], closePrayers[getPrayerBook()][4], closePrayers[getPrayerBook()][10]);
				break;
			case 3:
			case 10:
			case 19:
				closePrayers(closePrayers[getPrayerBook()][1], closePrayers[getPrayerBook()][2], closePrayers[getPrayerBook()][3],closePrayers[getPrayerBook()][4], closePrayers[getPrayerBook()][10]);
				break;
			case 4:
			case 11:
			case 20:
				closePrayers(closePrayers[getPrayerBook()][1], closePrayers[getPrayerBook()][2],closePrayers[getPrayerBook()][3], closePrayers[getPrayerBook()][4], closePrayers[getPrayerBook()][10]);
				break;
			case 8:
			//case 9:
			case 26:
				closePrayers(closePrayers[getPrayerBook()][5]);
				break;
			case 9:
				closePrayers(closePrayers[getPrayerBook()][6]);
				break;
			case 16:
			case 17:
			case 18:
				closePrayers(closePrayers[getPrayerBook()][7], closePrayers[getPrayerBook()][9]);
				needAppearenceGenerate = true;
				break;
			case 15:
				closePrayers(closePrayers[getPrayerBook()][8], closePrayers[getPrayerBook()][9]);
				needAppearenceGenerate = true;
				break;
			case 21:
			case 22:
			case 23:
				closePrayers(closePrayers[getPrayerBook()][7], closePrayers[getPrayerBook()][8], closePrayers[getPrayerBook()][9]);
				needAppearenceGenerate = true;
				break;
			case 25:
			case 27:
			case 28:
			case 29:
				closePrayers(closePrayers[getPrayerBook()][0], closePrayers[getPrayerBook()][1], closePrayers[getPrayerBook()][2], closePrayers[getPrayerBook()][3], closePrayers[getPrayerBook()][4], closePrayers[getPrayerBook()][10]);
				break;
			case 24:
				closePrayers(closePrayers[getPrayerBook()][11]);
				break;
			default:
				return false;
			}
		} else {
			switch (prayerId) {
			case 0:
				if (!usingQuickPrayer) {
					player.setNextAnimation(new Animation(12567));
					player.setNextGraphics(new Graphics(2213));
				}
				closePrayers(closePrayers[getPrayerBook()][0]);
				break;
			case 1:
			case 2:
			case 3:
			case 4:
				closePrayers(closePrayers[getPrayerBook()][5], closePrayers[getPrayerBook()][6]);
				break;
			case 5:
				if (!usingQuickPrayer) {
					player.setNextAnimation(new Animation(12589));
					player.setNextGraphics(new Graphics(2266));
				}
				closePrayers(closePrayers[getPrayerBook()][2]);
				break;
			case 7:
			case 8:
			case 9:
			case 17:
			case 18:
				closePrayers(closePrayers[getPrayerBook()][3]);
				needAppearenceGenerate = true;
				break;
			case 6:
				closePrayers(closePrayers[getPrayerBook()][4]);
				needAppearenceGenerate = true;
				break;
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
				closePrayers(closePrayers[getPrayerBook()][1], closePrayers[getPrayerBook()][6]);
				break;
			case 19:
				// stop changing this idiot. it doesnt stop walk on rs
				if (!usingQuickPrayer) {
					player.setNextAnimation(new Animation(12565));
					player.setNextGraphics(new Graphics(2226));
				}
				closePrayers(closePrayers[getPrayerBook()][1], closePrayers[getPrayerBook()][5], closePrayers[getPrayerBook()][6]);
				break;
			default:
				return false;
			}
		}
		if (!usingQuickPrayer) {
			onPrayers[getPrayerBook()][prayerId] = true;
			resetDrainPrayer(prayerId);
			onPrayersCount++;
			if (needAppearenceGenerate)
				player.getAppearence().generateAppearenceData();
		} else {
			quickPrayers[getPrayerBook()][prayerId] = true;
		}
		player.getPackets().sendSound(2662, 0, 1);
		return true;
	}

	public void processPrayer() {
		if (!hasPrayersOn())
			return;
		boostedLeech = false;
	}

	// 600

	public void processPrayerDrain() {
		if (!hasPrayersOn())
			return;
		int prayerBook = getPrayerBook();
		long currentTime = Utils.currentTimeMillis();
		int drain = 0;
		double prayerPoints = player.getCombatDefinitions().getBonuses()[CombatDefinitions.PRAYER_BONUS];
		int hatId = player.getEquipment().getHatId();
		if (hatId >= 18744 && hatId <= 18746) //hallos give hidden effect 15pray bonus
			prayerPoints += 15;
		for (int index = 0; index < onPrayers[prayerBook].length; index++) {
			if (onPrayers[prayerBook][index]) {
				long drainTimer = nextDrain[index];
				if (drainTimer != 0 && drainTimer <= currentTime) {
					int rate = (int) ((prayerDrainRate[getPrayerBook()][index] * 1000) + (prayerPoints * 50));
					int passedTime = (int) (currentTime - drainTimer);
					drain++;
					int count = 0;
					while (passedTime >= rate && count++ < 10) {
						drain++;
						passedTime -= rate;
					}
					nextDrain[index] = (currentTime + rate) - passedTime;
				}
			}
		}
		if (drain > 0) {
			drainPrayer(drain);
			if (!checkPrayer())
				closeAllPrayers();
		}
	}

	public void resetDrainPrayer(int index) {
		nextDrain[index] = (long) (Utils.currentTimeMillis() + (prayerDrainRate[getPrayerBook()][index] * 1000) + (player.getCombatDefinitions().getBonuses()[CombatDefinitions.PRAYER_BONUS] * 50));
	}

	public int getOnPrayersCount() {
		return onPrayersCount;
	}

	public void closeAllPrayers(boolean reset) {
		onPrayers = new boolean[][]
		{ new boolean[30], new boolean[20] };
		leechBonuses = new int[11];
		onPrayersCount = 0;
		if (reset) {
			player.getPackets().sendCSVarInteger(182, 0);
			this.quickPrayerOn = false;
		}
		player.getVarsManager().sendVar(ancientcurses ? 1582 : 1395, 0);
		player.getAppearence().generateAppearenceData();
		resetStatAdjustments();
	}

	public void closeAllPrayers() {
		closeAllPrayers(true);
	}

	public boolean hasPrayersOn() {
		return onPrayersCount > 0;
	}

	private boolean checkPrayer() {
		if (prayerpoints <= 0) {
			player.getPackets().sendSound(2672, 0, 1);
			player.getPackets().sendGameMessage("Please recharge your prayer at the Lumbridge Church.");
			return false;
		}
		return true;
	}

	private int getPrayerBook() {
		return ancientcurses == false ? 0 : 1;
	}

	private void recalculatePrayer() {
		int value = 0;
		int index = 0;
		for (boolean prayer : (!usingQuickPrayer ? onPrayers[getPrayerBook()] : quickPrayers[getPrayerBook()])) {
			if (prayer)
				value += ancientcurses ? Math.pow(2, index) : prayerSlotValues[index];
			index++;
		}
		player.getVarsManager().sendVar(ancientcurses ? (usingQuickPrayer ? 1587 : 1582) : (usingQuickPrayer ? 1397 : 1395), value);
	}

	public void refresh() {
		player.getPackets().sendCSVarInteger(181, usingQuickPrayer ? 1 : 0);
		player.getVarsManager().sendVar(1584, ancientcurses ? 1 : 0);
		unlockPrayerBookButtons();
	}

	public void resetStatAdjustments() {
		for (int i = 0; i < 5; i++)
			adjustStat(i, 0);
	}

	public void init() {
		player.getPackets().sendCSVarInteger(181, usingQuickPrayer ? 1 : 0);
		player.getVarsManager().sendVar(1584, ancientcurses ? 1 : 0);
		resetStatAdjustments();
	}

	public void unlockPrayerBookButtons() {
		player.getPackets().sendUnlockIComponentOptionSlots(271, usingQuickPrayer ? 42 : 8, 0, 29, 0);
	}

	public void setPrayerBook(boolean ancientcurses) {
		closeAllPrayers();
		this.ancientcurses = ancientcurses;
		player.getInterfaceManager().sendPrayerBook();
		refresh();
	}

	public Prayer() {
		quickPrayers = new boolean[][]
		{ new boolean[30], new boolean[20] };
		prayerpoints = 10;
	}

	public void setPlayer(Player player) {
		this.player = player;
		onPrayers = new boolean[][]
		{ new boolean[30], new boolean[20] };
		nextDrain = new long[30];
		leechBonuses = new int[11];
	}

	public boolean isAncientCurses() {
		return ancientcurses;
	}

	public boolean isUsingQuickPrayer() {
		return usingQuickPrayer;
	}

	public boolean isBoostedLeech() {
		return boostedLeech;
	}

	public void setBoostedLeech(boolean boostedLeech) {
		this.boostedLeech = boostedLeech;
	}

	public int getPrayerpoints() {
		return prayerpoints;
	}

	public void setPrayerpoints(int prayerpoints) {
		this.prayerpoints = prayerpoints;
	}

	public void refreshPrayerPoints() {
		player.getVarsManager().sendVar(2382, prayerpoints / (player.isOldHitLook() ? 10 : 1));
	}

	public void drainPrayerOnHalf() {
		if (prayerpoints > 0) {
			prayerpoints = prayerpoints / 2;
			refreshPrayerPoints();
		}
	}

	public boolean hasFullPrayerpoints() {
		return getPrayerpoints() >= player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
	}

	public void drainPrayer(int amount) {
		int shieldID = player.getEquipment().getShieldId();
		int weaponID = player.getEquipment().getWeaponId();
		if (amount > 1 && !player.isCanPvp() && (weaponID == 25592 || weaponID == 25609 || weaponID == 25575 || shieldID == 25495 || shieldID == 25702 || shieldID == 25495 || shieldID == 13744 || shieldID == 23700))
			amount /= 2;
		if ((prayerpoints - amount) >= 0)
			prayerpoints -= amount;
		else
			prayerpoints = 0;
		refreshPrayerPoints();
	}

	public void drainPrayer() {
		prayerpoints = 0;
		refreshPrayerPoints();
	}

	public void restorePrayer(int amount) {
		int maxPrayer = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
		if ((prayerpoints + amount) <= maxPrayer)
			prayerpoints += amount;
		else
			prayerpoints = maxPrayer;
		refreshPrayerPoints();
	}

	public void reset() {
		closeAllPrayers();
		prayerpoints = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
		refreshPrayerPoints();
	}

	public boolean isUsingProtectionPrayer() {
		return isMageProtecting() || isRangeProtecting() || isMeleeProtecting();
	}

	public boolean isProtectingItem() {
		return ancientcurses ? usingPrayer(1, 0) : usingPrayer(0, 9);
	}

	public boolean isMageProtecting() {
		return ancientcurses ? usingPrayer(1, 7) : usingPrayer(0, 16);
	}

	public boolean isRangeProtecting() {
		return ancientcurses ? usingPrayer(1, 8) : usingPrayer(0, 17);
	}

	public boolean isMeleeProtecting() {
		return ancientcurses ? usingPrayer(1, 9) : usingPrayer(0, 18);
	}
	
	public boolean usingPrayer(int book, int prayerId) {
		Boolean shufflePrayer = (Boolean) player.getTemporaryAttributtes().get(Key.SHUFFLE_PRAYERS);
		if (shufflePrayer != null) {
			if (book == 1 && prayerId == 7) 
				prayerId = 8;
			else if (book == 1 && prayerId == 8) 
				prayerId = 9;
			else if (book == 1 && prayerId == 9) 
				prayerId = 7;
			else if (book == 0 && prayerId == 16) 
				prayerId = 17;
			else if (book == 0 && prayerId == 17) 
				prayerId = 18;
			else if (book == 0 && prayerId == 18) 
				prayerId = 16;
		}
		return onPrayers[book][prayerId];
	}

	public boolean canReflect(Entity entity) {
		if (entity instanceof DungeonBoss || entity instanceof WildyWyrm || entity instanceof CombatEventNPC
				|| entity instanceof TOBBoss || entity instanceof TheNightmare) {
			player.getPackets().sendGameMessage("You are unable to reflect damage back to this creature.", true);
			return false;
		}
		return entity.getMaxHitpoints() > 1;
	}

	public void handleHitPrayers(final Entity target, Hit hit) {
		if (!hasPrayersOn() || hit.getDamage() == 0)
			return;
		if (!ancientcurses) {
			if (target instanceof Player && usingPrayer(0, 23))
				((Player) target).getPrayer().drainPrayer(hit.getDamage() / 4);
		} else if (!boostedLeech) {
			if (usingPrayer(1, 19)) { //turmoil
				increaseTurmoilBonus(target);
				boostedLeech = true;
				return;
			}
			if (usingPrayer(1, 1) && Utils.random(5) == 0) {//sap attack
				if (reachedMax(0)) {
					player.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
				} else {
					increaseLeechBonus(0);
					player.getPackets().sendGameMessage("Your curse drains Attack from the enemy, boosting your Attack.", true);
				}
				player.setNextAnimation(new Animation(12569));
				player.setNextGraphics(new Graphics(2214));
				boostedLeech = true;
				World.sendProjectile(player, target, 2215, 35, 35, 20, 5, 0, 0);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						target.setNextGraphics(new Graphics(2216));
					}
				}, 1);
				return;
			}
			if (usingPrayer(1, 10) && Utils.random(8) == 0) {//leech attack
				if (reachedMax(3)) {
					player.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
				} else {
					increaseLeechBonus(3);
					player.getPackets().sendGameMessage("Your curse drains Attack from the enemy, boosting your Attack.", true);
				}
				player.setNextAnimation(new Animation(12575));
				boostedLeech = true;
				World.sendProjectile(player, target, 2231, 35, 35, 20, 5, 0, 0);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						target.setNextGraphics(new Graphics(2232));
					}
				}, 1);
				return;
			}
			if (usingPrayer(1, 14) && Utils.random(8) == 0) { //leech strength
				if (reachedMax(7)) {
					player.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
				} else {
					increaseLeechBonus(7);
					player.getPackets().sendGameMessage("Your curse drains Strength from the enemy, boosting your Strength.", true);
				}
				player.setNextAnimation(new Animation(12575));
				boostedLeech = true;
				World.sendProjectile(player, target, 2248, 35, 35, 20, 5, 0, 0);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						target.setNextGraphics(new Graphics(2250));
					}
				}, 1);
				return;
			}

			if (usingPrayer(1, 2) && Utils.random(5) == 0) { // sap range
				if (reachedMax(1)) {
					player.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
				} else {
					increaseLeechBonus(1);
					player.getPackets().sendGameMessage("Your curse drains Range from the enemy, boosting your Range.", true);
				}
				player.setNextAnimation(new Animation(12569));
				player.setNextGraphics(new Graphics(2217));
				boostedLeech = true;
				World.sendProjectile(player, target, 2218, 35, 35, 20, 5, 0, 0);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						target.setNextGraphics(new Graphics(2219));
					}
				}, 1);
				return;
			}
			if (usingPrayer(1, 11) && Utils.random(8) == 0) { //leech range
				if (reachedMax(4)) {
					player.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
				} else {
					increaseLeechBonus(4);
					player.getPackets().sendGameMessage("Your curse drains Range from the enemy, boosting your Range.", true);
				}
				player.setNextAnimation(new Animation(12575));
				boostedLeech = true;
				World.sendProjectile(player, target, 2236, 35, 35, 20, 5, 0, 0);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						target.setNextGraphics(new Graphics(2238));
					}
				});
				return;
			}
			if (usingPrayer(1, 3) && Utils.random(5) == 0) { // sap mage
				if (reachedMax(2)) {
					player.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
				} else {
					increaseLeechBonus(2);
					player.getPackets().sendGameMessage("Your curse drains Magic from the enemy, boosting your Magic.", true);
				}
				player.setNextAnimation(new Animation(12569));
				player.setNextGraphics(new Graphics(2220));
				boostedLeech = true;
				World.sendProjectile(player, target, 2221, 35, 35, 20, 5, 0, 0);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						target.setNextGraphics(new Graphics(2222));
					}
				}, 1);
				return;
			}
			if (usingPrayer(1, 12) && Utils.random(8) == 0) { //leech magic
				if (reachedMax(5)) {
					player.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
				} else {
					increaseLeechBonus(5);
					player.getPackets().sendGameMessage("Your curse drains Magic from the enemy, boosting your Magic.", true);
				}
				player.setNextAnimation(new Animation(12575));
				boostedLeech = true;
				World.sendProjectile(player, target, 2240, 35, 35, 20, 5, 0, 0);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						target.setNextGraphics(new Graphics(2242));
					}
				}, 1);
				return;
			}

			if (usingPrayer(1, 13) && Utils.random(8) == 0) { // leech defence
				if (reachedMax(6)) {
					player.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
				} else {
					increaseLeechBonus(6);
					player.getPackets().sendGameMessage("Your curse drains Defence from the enemy, boosting your Defence.", true);
				}
				player.setNextAnimation(new Animation(12575));
				boostedLeech = true;
				World.sendProjectile(player, target, 2244, 35, 35, 20, 5, 0, 0);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						target.setNextGraphics(new Graphics(2246));
					}
				}, 1);
				return;
			}
			if (target instanceof Player) {
				Player targetPlayer = (Player) target;
				if (usingPrayer(1, 15) && Utils.random(8) == 0) { //leech run
					if (targetPlayer.getRunEnergy() <= 0) {
						player.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
					} else {
						player.setRunEnergy(player.getRunEnergy() > 90 ? 100 : player.getRunEnergy() + 10);
						targetPlayer.setRunEnergy(targetPlayer.getRunEnergy() > 10 ? targetPlayer.getRunEnergy() - 10 : 0);
					}
					player.setNextAnimation(new Animation(12575));
					boostedLeech = true;
					World.sendProjectile(player, target, 2256, 35, 35, 20, 5, 0, 0);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							target.setNextGraphics(new Graphics(2258));
						}
					}, 1);
					return;
				}
				if (usingPrayer(1, 4) && Utils.random(16) == 0) { // sap spec
					if (targetPlayer.getCombatDefinitions().getSpecialAttackPercentage() <= 0) {
						player.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
					} else {
						targetPlayer.getCombatDefinitions().desecreaseSpecialAttack(10);
					}
					player.setNextAnimation(new Animation(12569));
					player.setNextGraphics(new Graphics(2223));
					boostedLeech = true;
					World.sendProjectile(player, target, 2224, 35, 35, 20, 5, 0, 0);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							target.setNextGraphics(new Graphics(2225));
						}
					}, 1);
					return;
				}
				if (usingPrayer(1, 16) && Utils.random(16) == 0) { //leech spec
					if (targetPlayer.getCombatDefinitions().getSpecialAttackPercentage() <= 0) {
						player.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
					} else {
						player.getCombatDefinitions().restoreSpecialAttack(10);
						targetPlayer.getCombatDefinitions().desecreaseSpecialAttack(10);
					}
					player.setNextAnimation(new Animation(12575));
					boostedLeech = true;
					World.sendProjectile(player, target, 2252, 35, 35, 20, 5, 0, 0);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							target.setNextGraphics(new Graphics(2254));
						}
					}, 1);
					return;
				}
			}
		}

	}
}
