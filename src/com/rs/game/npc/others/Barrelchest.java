/**
 * 
 */
package com.rs.game.npc.others;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.QuestManager.Quests;

/**
 * @author dragonkk(Alex)
 * Mar 1, 2018
 */
@SuppressWarnings("serial")
public class Barrelchest extends NPC  {

	public Barrelchest(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setIntelligentRouteFinder(true);
		setDropRateFactor(5);
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.6;
	}
	
	@Override
	public void checkSlayer(Player killer) {
		super.checkSlayer(killer);
		killer.getQuestManager().completeQuest(Quests.THE_GREAT_BRAIN_ROBBERY);
	}
}
