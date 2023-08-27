package com.rs.game.npc.inferno;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.controllers.Inferno;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

@SuppressWarnings("serial")
public class JalAk extends InfernoNPC {

	public JalAk(WorldTile tile) {
		super(Inferno.BLOB, tile);
	}

	@Override
	public void sendDeath(Entity source) {
		final NPCCombatDefinitions defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		final WorldTile tile = this;
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
					setNextGraphics(new Graphics(2924 + getSize()));
				} else if (loop >= defs.getDeathDelay()) {
					reset();
					InfernoNPC npc = new InfernoNPC(27696, tile);
					npc.setTarget(source);
					npc = new InfernoNPC(27694, tile.transform(1, 1, 0));
					npc.setTarget(source);
					npc = new InfernoNPC(27695, tile.transform(2, 2, 0));
					npc.setTarget(source);
					finish();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

}
