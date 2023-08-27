package com.rs.game.npc.others;

import java.util.concurrent.TimeUnit;

import com.rs.executor.GameExecutorManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Ent extends NPC {

	private Entity source;
	private long deathTime;
	private int originalID;
	public Ent(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		originalID = id;
	}

	@Override
	public void sendDeath(final Entity source) {
		final NPCCombatDefinitions defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= defs.getDeathDelay()) {
					drop();
					reset();
					transformIntoLog(source);
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	public void transformIntoLog(Entity source) {
		this.source = source;
		deathTime = Utils.currentTimeMillis();
		final int remainsId = 26595;
		setNextNPCTransformation(remainsId);
		setRandomWalk(0);
		GameExecutorManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					if (remainsId == getId())
						takeLogs();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 3, TimeUnit.MINUTES);

	}

	public boolean canCut(Player player) {
		return Utils.currentTimeMillis() - deathTime > 60000 || player == source;
	}

	public void takeLogs() {
		setNPC(originalID);
		setLocation(getRespawnTile());
		setRandomWalk(NORMAL_WALK);
		finish();
		if (!isSpawned())
			setRespawnTask();
	}

}
