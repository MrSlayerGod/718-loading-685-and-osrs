package com.rs.game.npc.nightmare;

import java.util.List;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Husk extends NPC  {

	private Entity target;
	
	public Husk(Entity target, int id, WorldTile tile) {
		super(id, tile, -1, true, true);
		this.setCantSetTargetAutoRelatio(false);
		setRandomWalk(0);
		setForceMultiArea(true);
		setLureDelay(6000);//approximately 6 seconds lure
		this.target = target;
		this.setTarget(target);
		this.getCombat().setCombatDelay(4);
		anim(28567);
	}
	
	
	@Override
	public void processNPC() {
		if (target == null)
			return;
		super.processNPC();
		if (!target.withinDistance(this)) {
			sendDeath(target);
			return;
		}
		if (target != null && !isDead()) {
			target.addFreezeDelay(10000, true);
			setTarget(target);
		}
	}
	
	public void sendDeath(Entity source) {
		if (target != null && target.withinDistance(this)) {
			List<Integer> npcsIndexes = World.getRegion(getRegionId()).getNPCsIndexes();
			int count = 0;
			if (npcsIndexes != null) {
				for (int npcIndex : npcsIndexes) {
					NPC npc = World.getNPCs().get(npcIndex);
					if (npc == null || npc.hasFinished() || npc.isDead() || !(npc instanceof Husk))
						continue;
					if (npc.getCombat().getTarget() == target)
						count++;
				}
			}
			if (count == 0) {
				target.setFreezeDelay(0);
				((Player)target).getPackets().sendGameMessage("<col=00FF00>As the last husk dies, you feel yourself become free of the Nightmare's trance!");
			}
		}
		super.sendDeath(source);
	}

}
