package com.rs.game.npc.others;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

@SuppressWarnings("serial")
public class MutatedZygomites extends ConditionalDeath {

	boolean lvl74;

	public MutatedZygomites(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(7421, null, false, id, tile, mapAreaNameHash, true, false);
		this.lvl74 = id == 3344;
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (!isDead() && !isUnderCombat() && !isFungus() && !isCantInteract())
			resetNPC();
	}
	
	@Override
	public void finish() {
		resetNPC();
		super.finish();
	}
	
	public boolean isFungus() {
		return getId() == 3344 || getId() == 3345;
	}
	
	private void resetNPC() {
		setNextNPCTransformation(lvl74 ? 3344 : 3345);
		setNextWorldTile(getRespawnTile());
	}

	public static void transform(final Player player, final NPC npc) {
		if (npc.isCantInteract())
			return;
		player.setNextAnimation(new Animation(2988));
		npc.setNextNPCTransformation(npc.getId() + 2);
		npc.setNextAnimation(new Animation(2982));
		npc.setCantInteract(true);
		npc.getCombat().setTarget(player);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				npc.setCantInteract(false);
			}
		}, 1);
	}
}
