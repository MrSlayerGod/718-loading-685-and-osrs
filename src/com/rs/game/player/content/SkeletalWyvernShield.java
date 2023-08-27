package com.rs.game.player.content;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Smithing;

public class SkeletalWyvernShield {

	public static void joinPieces(Player player) {
		if (!player.getInventory().containsItemToolBelt(Smithing.HAMMER)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a hammer in order to work with the visage.");
			return;
		}
		if (player.getSkills().getLevel(Skills.SMITHING) < 66) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a Smithing level of 66 to forge a skeletal wyvern shield.");
			return;
		}
		if (player.getSkills().getLevel(Skills.MAGIC) < 66) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a Magic level of 66 to forge a skeletal wyvern shield.");
			return;
		}
		if (!player.getInventory().containsItem(2890, 1)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need an elemental shield to forge a skeletal wyvern shield.");
			return;
		}
		if (!player.getInventory().containsItem(51637, 1)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a wyvern visage to forge a skeletal wyvern shield.");
			return;
		}
		player.lock(2);
		player.setNextAnimation(new Animation(898));
		player.getInventory().deleteItem(2890, 1);
		player.getInventory().deleteItem(51637, 1);
		player.getInventory().addItem(51634, 1);
		player.getSkills().addXp(Skills.SMITHING, 2000);
		player.getSkills().set(Skills.MAGIC, 0);
		player.getDialogueManager().startDialogue("SimpleDialogue", "At a great cost to your personal magical energies, you have crafted the wyvern visage and elemental shield into an ancient wyvern shield.");
	}

	public static void chargeDFW(Player player, boolean fully) {
		int shieldId = player.getEquipment().getShieldId();
		if (shieldId != 51634 && shieldId != 51633)
			return;
		if (shieldId == 51634) {
			player.getEquipment().getItem(Equipment.SLOT_SHIELD).setId(51633);
			player.getEquipment().refresh(Equipment.SLOT_SHIELD);
			player.getAppearence().generateAppearenceData();
		}
		if (player.getCharges().getCharges(51633) == 50) {
			player.getPackets().sendGameMessage("Your skeletal wyvern shield is already full.", true);
			return;
		}
		player.getCharges().addCharges(51633, fully ? 50 : 1, Equipment.SLOT_SHIELD);
		player.getCombatDefinitions().refreshBonuses();
		player.setNextAnimationNoPriority(new Animation(6695));
		player.setNextGraphics(new Graphics(6399));
		player.getPackets().sendGameMessage("Your skeletal wyvern shield glows more brightly.", true);
	}

	public static void empty(Player player) {
		player.lock(1);
		player.getCharges().addCharges(51633, -50, -1);
		player.setNextGraphics(new Graphics(6397));
	//	player.setNextAnimation(new Animation(6700));
		player.getPackets().sendGameMessage("You empty your skeletal wyvern shield.");
	}

	public static boolean isDragonFireShield(int id) {
		if (id == 1540 || id == 51637)
			return true;
		return false;
	}
}
