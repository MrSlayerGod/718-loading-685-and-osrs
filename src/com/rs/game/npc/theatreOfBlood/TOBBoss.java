/**
 * 
 */
package com.rs.game.npc.theatreOfBlood;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 *
 */
@SuppressWarnings("serial")
public class TOBBoss extends NPC {
	
	protected TheatreOfBlood raid;
	private int wave;

	public TOBBoss(TheatreOfBlood raid, int wave, int id, WorldTile tile) {
		super(id, tile, -1, true, true);
		this.wave = wave;
		this.raid = raid;
		setHitpoints(getMaxHitpoints()); //sets hp after set team size
		setForceMultiArea(true);
		setRandomWalk(0);
	}
	
	
	@Override
	public void processNPC() {
		super.processNPC();
		raid.setHPBar(this);
	}
	
	public void sendDeath(Entity killer) {
		if (raid != null && raid.getStage() == Stages.RUNNING)
			raid.clearWave(wave);
		super.sendDeath(killer);
	}
	
    public Player getClosestPlayer() {
        Optional<Player> client = raid.getTargets(this).stream().min(Comparator.comparingDouble(c -> getDistance(this, c)));
        return client.orElse(null);
    }

    public void submit(Consumer<Player> consumer) {
    	 raid.getTargets(this).forEach(consumer);
    }

    public double getDistance(WorldTile a, WorldTile b) {
        return Math.hypot(a.getX() - b.getX(), a.getY() - b.getY());
    }
    
    public boolean isRunning() {
    	return raid != null && raid.getStage() == Stages.RUNNING; 
    }
    
    public Player getRandomPlayer() {
        List<Player> targets = raid.getTargets(this);
        if (targets.isEmpty())
            return null;
        return targets.get(Utils.random(targets.size()));
    }
    
	@Override
	public double getMagePrayerMultiplier() {
		return 0.3;
	}
	
	@Override
	public double getRangePrayerMultiplier() {
		return 0.3;
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.3;
	}
	
	public TheatreOfBlood getRaid() {
		return raid;
	}
	
	public int getMaxHitpoints() {
		if (raid != null && raid.getTeamSize() >= 1 && raid.getTeamSize() < 5) {
			//int hp = (int) ((raid.getTeamSize() * super.getMaxHitpoints() / 5) * 0.7);
			//int base = (int) (super.getMaxHitpoints() * 0.3);
			double multiplier = Math.max(0.75, 1 - (5-raid.getTeamSize())*0.125);
			//return base + hp;
			return (int) (super.getMaxHitpoints() * multiplier);
		}
		return super.getMaxHitpoints();
	}
}
