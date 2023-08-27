/**
 * 
 */
package com.rs.game.minigames.stealingcreation;

import com.rs.Settings;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 4, 2017
 */
public class SCRewards {

	public static int VOLATILE_XP = 29340, SACRED_XP = 24450;
	public static enum SCTransform {
		BODY(SCItem.PLATEBODY, SCItem.ROPE_TOP, SCItem.BODY),
		LEGS(SCItem.PLATELEGS, SCItem.ROBE_BOTTOM, SCItem.CHAPS),
		HELM(SCItem.HELM, SCItem.HAT, SCItem.COIF),
		WEAPON(SCItem.SCIMITAR, SCItem.STAFF, SCItem.BOW),
		VOLATILE(SCItem.VOLATILE_TOOL, SCItem.VOLATILE_PICKAXE, SCItem.VOLATILE_HATCHET, SCItem.VOLATILE_HARPOON, SCItem.VOLATILE_BUTTERFLY_NET, SCItem.VOLATILE_FLETCHING_KNIFE, SCItem.VOLATILE_FLETCHING_KNIFE, SCItem.VOLATILE_HAMMER, SCItem.VOLATILE_NEEDLE),
		SACRED(SCItem.SACRED_TOOL, SCItem.SACRED_PICKAXE, SCItem.SACRED_HATCHET, SCItem.SACRED_HARPOON, SCItem.SACRED_BUTTERFLY_NET, SCItem.SACRED_FLETCHING_KNIFE, SCItem.SACRED_FLETCHING_KNIFE, SCItem.SACRED_HAMMER, SCItem.SACRED_NEEDLE),
		SHIELD(SCItem.MELEE_SHIELD, SCItem.MAGIC_SHIELD, SCItem.RANGED_SHIELD),
		
		;
		
		private SCItem[] items;
		
		private SCTransform(SCItem... items) {
			this.items = items;
		}
	}
	public static enum SCItem {
		PLATEBODY(Skills.ATTACK, 14094),
		PLATELEGS(Skills.ATTACK, 14095),
		HELM(Skills.ATTACK, 14096),
		SCIMITAR(Skills.ATTACK, 14097),
		ROPE_TOP(Skills.MAGIC, 14114),
		ROBE_BOTTOM(Skills.MAGIC, 14115),
		HAT(Skills.MAGIC, 14116),
		STAFF(Skills.MAGIC, 14117),
		BODY(Skills.RANGE, 14118),
		CHAPS(Skills.RANGE, 14119),
		COIF(Skills.RANGE, 14120),
		BOW(Skills.RANGE, 14121),
		VOLATILE_TOOL(-1, 14098),
		VOLATILE_PICKAXE(Skills.MINING, 14099),
		VOLATILE_HATCHET(Skills.WOODCUTTING, 14100),
		VOLATILE_HARPOON(Skills.FISHING, 14101),
		VOLATILE_BUTTERFLY_NET(Skills.HUNTER, 14102),
		VOLATILE_FLETCHING_KNIFE(Skills.FLETCHING, 14103),
		VOLATILE_HAMMER(Skills.SMITHING, 14104),
		VOLATILE_NEEDLE(Skills.CRAFTING, 14105),
		SACRED_TOOL(-1, 14106),
		SACRED_PICKAXE(Skills.MINING, 14107),//done
		SACRED_HATCHET(Skills.WOODCUTTING, 14108),//done
		SACRED_HARPOON(Skills.FISHING, 14109), //done
		SACRED_BUTTERFLY_NET(Skills.HUNTER, 14110), //done
		SACRED_FLETCHING_KNIFE(Skills.FLETCHING, 14111), //done
		SACRED_HAMMER(Skills.SMITHING, 14112), //done
		SACRED_NEEDLE(Skills.CRAFTING, 14113), 
		MELEE_SHIELD(Skills.ATTACK, 21527), 
		MAGIC_SHIELD(Skills.MAGIC, 21528), 
		RANGED_SHIELD(Skills.RANGE, 21529), 
		;
		
		private int skill, id;
		private SCItem(int skill, int id) {
			this.skill = skill;
			this.id = id;
		}
	}
	
	private static SCItem getItem(int id) {
		for (SCItem item : SCItem.values())
			if (item.id == id)
				return item;
		return null;
	}
	
	private static SCTransform getTransform(SCItem item) {
		for (SCTransform transform : SCTransform.values())
			for (SCItem i : transform.items)
				if (i == item)
					return transform;
		return null;
	}
	
	//return sc bonus
	public static int addSkillXP(Player player, int skill, int xp) {
		/*if (player.isIronman() || player.isHCIronman())
			return 0;*/
		if (skill == Skills.STRENGTH || skill == Skills.DEFENCE)
			skill = Skills.ATTACK;
		else if (skill == Skills.CONSTRUCTION)
			skill = Skills.SMITHING;
		for (SCItem item : player.getScXP().keySet()) {
			if (item.skill == skill && (player.getInventory().containsOneItem(item.id) || player.getEquipment().containsOneItem(item.id))) {
				int remainingXP = getRemainingXP(player, item);
				int bonusXP = Math.min(xp, remainingXP);
				reduceRemainingXP(player, item, bonusXP);
				return bonusXP;
			}
		}
		return 0;
		
	}
	
	private static int getRemainingXP(Player player, SCItem item) {
		Integer xp = player.getScXP().get(item);
		return xp == null ? 0 : xp;
	}
	
	private static void reduceRemainingXP(Player player, SCItem item, int amount) {
		int xp = getRemainingXP(player, item);
		if (xp <= 0)
			return;
		xp -= amount;
		if (xp <= 0)
			player.getScXP().remove(item);
		else
			player.getScXP().put(item, xp);
	}
	
	private static void addRemainingXP(Player player, SCItem item, int amount) {
		player.getScXP().put(item, getRemainingXP(player, item) + amount);
	}
	
	public static boolean check(Player player, int id) {
		SCItem item = getItem(id);
		if (item == null)
			return false;
		SCTransform transform = getTransform(item);
		if (transform == null)
			return false;
		//double max = transform == SCTransform.VOLATILE ? VOLATILE_XP : SACRED_XP;
		int xp = getRemainingXP(player, item);
		player.getPackets().sendGameMessage("There is " + xp + " bonus xp remaining.");
		
		//int perc = (int) (/*xp >= max ? 100 :*/ (xp / max * 100d)); //lets you see above max, in case you have multple tools
		return true;
	}
	
	public static boolean recharge(Player player, int id) {
		SCItem item = getItem(id);
		if (item == null)
			return false;
		SCTransform transform = getTransform(item);
		if (transform == null)
			return false;
		addRemainingXP(player, item, (int) ((transform == SCTransform.VOLATILE ? VOLATILE_XP : SACRED_XP) * (player.isUltimateIronman() || player.isIronman() ? 3 : (Settings.XP_RATE  * (player.isFast() || player.isSuperFast() ? Settings.FAST_MODE_MULTIPLIER : 1)))));
		return true;
	}
	
	public static boolean transform(Player player, int id, boolean equipping, boolean revert) {
		SCItem item = getItem(id);
		if (item == null)
			return false;
		SCTransform transform = getTransform(item);
		if (transform == null)
			return false;
		double remainingXP = Math.min(getRemainingXP(player, item), (transform == SCTransform.VOLATILE ? VOLATILE_XP : SACRED_XP) * (player.isUltimateIronman() || player.isIronman() ? 3 : (Settings.XP_RATE  * (player.isFast() || player.isSuperFast() ? Settings.FAST_MODE_MULTIPLIER : 1))));
		if (transform == SCTransform.VOLATILE || transform == SCTransform.SACRED) {
			if (remainingXP == 0) {
				if (item == SCItem.SACRED_TOOL || item == SCItem.VOLATILE_TOOL) {
					player.getPackets().sendGameMessage("This tool has no energy remaining.");
					return true;
				}
				replace(player, item.id, transform.items[0].id, equipping);
				return true;
			}
		}
		SCItem next = null;
		if (revert) 
			next = transform.items[0];
		else {
			while (next == null || next == item) {
				next = transform == SCTransform.SACRED || transform == SCTransform.SACRED ?
						 transform.items[Utils.random(transform.items.length - 1)+1] 
						: transform.items[Utils.random(transform.items.length)];
			}
		}
		replace(player, item.id, next.id, equipping);
		reduceRemainingXP(player, item, (int) remainingXP);
		addRemainingXP(player, next, (int) (remainingXP * ((transform == SCTransform.VOLATILE && item != SCItem.VOLATILE_TOOL) ? 0.9d : 1d)));
		return true;
	}
	
	private static void replace(Player player, int remove, int add, boolean equipping) {
		if (equipping) {
			for (int i = 0; i < player.getEquipment().getItems().getSize(); i++) {
				Item item = player.getEquipment().getItem(i);
				if (item != null && item.getId() == remove) {
					item.setId(add);
					player.getEquipment().refresh(i);
					player.getAppearence().generateAppearenceData();
					break;
				}
			}
		} else {
			for (int i = 0; i < player.getInventory().getItems().getSize(); i++) {
				Item item = player.getInventory().getItem(i);
				if (item != null && item.getId() == remove) {
					item.setId(add);
					player.getInventory().refresh(i);
					break;
				}
			}
		}
	}
}
