/**
 * 
 */
package com.rs.game.player.content;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

/**
 * @author dragonkk(Alex)
 * Jan 8, 2018
 */
public class MythGuild {

	public static void teleport(Player player) {
		Magic.sendTeleportSpell(player, 8939, 8941, 1576, 1577, 0, 0, new WorldTile(2329, 2786, 0), 3, true, Magic.ITEM_TELEPORT);
	}
	
	public static void enter(Player player, WorldObject object) {
		if (player.getSkills().getLevelForXp(Skills.MAGIC) < 75
				|| player.getSkills().getLevelForXp(Skills.SMITHING) < 70
				|| player.getSkills().getLevelForXp(Skills.MINING) < 68
				|| player.getSkills().getLevelForXp(Skills.CRAFTING) < 62
				|| player.getSkills().getLevelForXp(Skills.AGILITY) < 60
				|| player.getSkills().getLevelForXp(Skills.CONSTRUCTION) < 50
				|| player.getSkills().getLevelForXp(Skills.HITPOINTS) < 50) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 28038,
					"Come back once you are level 75 magic, 70 smithing, 69 mining, 62 crafting, 60 agility, 50 construction and 50 hitpoints.");
			return;
		}
		player.lock(2);
		if (object.getId() == 131617)
			player.addWalkSteps(object.getX() + (object.getX() > player.getX() ? 1 : -1), object.getY(), 2, false);
		else
			player.addWalkSteps(object.getX(), object.getY() + (object.getY() > player.getY() ? 1 : -1), 2, false);
	}
}
