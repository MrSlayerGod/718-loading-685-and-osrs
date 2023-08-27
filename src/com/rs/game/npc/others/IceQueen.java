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
public class IceQueen extends NPC  {

	public IceQueen(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}
	
	@Override
	public void checkSlayer(Player killer) {
		super.checkSlayer(killer);
		killer.getQuestManager().completeQuest(Quests.HEROES_QUEST_2);
	}
}
