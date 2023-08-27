package com.rs.game.npc.others;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.Drop;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class WildyWyrm extends NPC {

	private boolean emerged;
	private boolean force;
	private int cycle;
	private Entity target;
	private int healCycle;
	
	
	public WildyWyrm(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setLureDelay(2000);
		setCantFollowUnderCombat(true);
		setForceTargetDistance(16);
		setDropRateFactor(2);
	}
	
	public void emerge() {
		force = true;
	}
	
	@Override
	public void reset() {
		setNextNPCTransformation(2417);
		setCombatLevel(-1);
		emerged = false;
		force = false;
		cycle = 0;
		healCycle = 0;
		target = null;
		setCantInteract(false);
		super.reset();
	}
	
	@Override
	public void drop() {
		for(Entity t : getReceivedDamageSources()) {
			if(!(t instanceof Player))
				continue;
			Player player = (Player) t;
			player.setKilledWildyWyrm();
			player.getPackets().sendGameMessage("You contributed to the wyrm's death, and it leaves a fury shark behind.");
			sendDrop(player, new Drop(20429, 1, 1));
		}
		super.drop();
	}
	
	@Override
	public void processNPC() {
		if(isDead())
			return;
		if(cycle > 0) {
			cycle--;
			if(cycle == 2 && !emerged) {
				setNextNPCTransformation(2417);
				setCombatLevel(-1);
			}else if (cycle == 3 && emerged) {
				for(Entity t : getPossibleTargets()) {
					if(Utils.colides(t.getX(), t.getY(), t.getSize(), getX(), getY(), getSize())) {
						t.applyHit(new Hit(this, Utils.random(50)+100, HitLook.REGULAR_DAMAGE));
						if(t instanceof Player) 
							((Player)t).setPrayerDelay(5000);
						t.addFreezeDelay(1200, true);
					}
				}
			}else if(cycle == 0) {
				getCombat().setCombatDelay(1);
				setCantInteract(false);
				if(target != null) {
					setTarget(target);
					target = null;
				}
			}
			return;
		}
		if(!emerged) {
			if(getCombat().getTarget() != null) {
				emerged = true;
				cycle = 4;
				target = getCombat().getTarget();
				setNextAnimation(new Animation(12795));
				setNextNPCTransformation(3334);
				setCombatLevel(562);
				setCantInteract(true);
				return;
			}
		}else if (emerged || force) {
			
			if(getCombat().getTarget() == null || force) {
				emerged = false;
				force = false;
				cycle = 4;
				target = getCombat().getTarget();
				setNextAnimation(new Animation(12796));
				setCantInteract(true);
				return;
			}else{
				healCycle++;
				if(healCycle >= 50 && getMaxHitpoints()/2 > getHitpoints()) {
					double h = getPossibleTargets().size()*0.01D;
					if(h < 0.01d || h > 0.1d)
						h = 0.1d;
					healCycle = 0;
					heal((int) (getMaxHitpoints()*h));
				}
			}
		}
		//cuz ofisCantFollowUnderCombat he doesnt reset even if u far lo(ofc he resets if u die logout blbablabl)
		if(getCombat().getTarget() != null && !getCombat().getTarget().withinDistance(this, 16)) 
			this.removeTarget();
		super.processNPC();
	}
	
	public static void handleInspect(final Player player, final NPC npc) {
		if(!(npc instanceof WildyWyrm))
			return;
		final WildyWyrm wyrm = (WildyWyrm) npc;
		if(wyrm.emerged || wyrm.isCantInteract() || wyrm.cycle != 0 || wyrm.getCombat().getTarget() != null) {
			player.getPackets().sendGameMessage("Someone else is doing that.");
			return;
		}
		wyrm.setHitpoints(npc.getMaxHitpoints());
		wyrm.setCantInteract(true);
		player.setNextAnimation(new Animation(4278));
		player.lock(3);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				wyrm.setCantInteract(false);
				wyrm.setTarget(player);
			}
		}, 2);
	}
}
