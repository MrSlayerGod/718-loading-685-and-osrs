package com.rs.game.npc.others;

import com.rs.game.ForceTalk;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Mogre extends NPC {

	private static final String[] MESSAGES = {
		"Da boom-boom kill all da fishies!",
		"I smack you good!",
		"Smash stupid human!",
		"Tasty human!",
		"Human hit me on the head!",
		"I get you!",
		"Human scare all da fishies!" 
	};
	private Player owner;

	public Mogre(Player owner, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(114, tile, mapAreaNameHash, canBeAttackFromOutOfArea, true);
		this.owner = owner;
		setNextForceTalk(new ForceTalk(MESSAGES[Utils.random(MESSAGES.length)]));
		getCombat().setTarget(owner);
		owner.getHintIconsManager().addHintIcon(this, 1, -1, false);
	}

	public void processNPC() {
		super.processNPC();
		if (owner == null || !owner.withinDistance(this, 16) || (!isUnderCombat() && !isDead())) {
			finish();
			return;
		}
	}
	
	@Override
	public void finish() {
		super.finish();
		if (owner != null)
			owner.getHintIconsManager().removeUnsavedHintIcon();
	}
	
	public Player getOwner() {
		return owner;
	}
}
