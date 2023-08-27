/**
 * 
 */
package com.rs.game.npc.abyssalNexus;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author dragonkk(Alex)
 * Nov 9, 2017
 */
@SuppressWarnings("serial")
public class Tentacle extends NPC {

	public static final int STUNNED_ID = 25911, AWAKEN_ID = 25912;
	
	private int baseID;
	private boolean left;
	
	public Tentacle(int id, boolean left, WorldTile tile) {
		super(id, tile, -1, true, true);
		baseID = id;
		this.left = left;
		setCantFollowUnderCombat(true);
		setCantSetTargetAutoRelatio(true);
	}

	public void setSleeping() {
		this.setNextNPCTransformation(baseID);
		reset();
	}
	
	public void setStunned() {
		if (getId() == STUNNED_ID)
			return;
		this.setNextNPCTransformation(STUNNED_ID);
		setNextAnimation(new Animation(27112));
		reset();
	}
	
	@Override
	public void reset() {
		super.reset();
		this.setNextFaceEntity(null);
		setNextFaceWorldTile(new WorldTile(getRespawnTile().transform(left ? 3 : 4, -1, 0)));
	}
	
	public void setAwaken(Player target) {
		this.setNextNPCTransformation(AWAKEN_ID);
		setNextAnimation(new Animation(baseID == 25910 ? 27108 : baseID == 25913 ? 27114 : 27108));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (getId() != AWAKEN_ID || Tentacle.this.hasFinished())
					return;
				getCombat().setTarget(target);
			}
		}, 4);
	}
	
}
