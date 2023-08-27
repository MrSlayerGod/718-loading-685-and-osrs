package com.rs.game.npc.theatreOfBlood.nycolas;

import java.util.function.Consumer;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class NycolasPillar extends NPC {

    public static final int NYCOLAS_PILLAR = 28379;
    private static final double[] HEALTH_RATIOS = {0.50, 0.0};

    private TheatreOfBlood raid;
    
    public NycolasPillar(TheatreOfBlood raid, WorldTile tile) {
        super(NYCOLAS_PILLAR, tile, -1, true, true);
        this.raid = raid;
        setCantFollowUnderCombat(true);
        setForceMultiArea(true);
    }

    @Override
    public void setHitpoints(int change) {
    	if (raid != null && !isDead()) {
            int maxHp = getMaxHitpoints(), addition = 0;
            for (int index = HEALTH_RATIOS.length - 1; index >= 0; index--) {
                if (maxHp * HEALTH_RATIOS[index] >= change) {
                    addition++;
                }
            }

            int nextID = 132862 + addition;
            WorldObject object = World.getObjectWithType(new WorldTile(getX(), getY(), getPlane()), 10);
            if (object != null && nextID != object.getId()) {
               // World.removeObject(objects[0]);
                World.spawnObject(new WorldObject(nextID, object.getType(), object.getRotation(), object));
            }

    	}
        super.setHitpoints(change);
    }

    @Override
    public void sendDeath(Entity source) {
    	super.sendDeath(source);
        submit(client -> {
        	client.getPackets().sendCameraShake(3, 12, 25, 12, 25);
            client.applyHit(new Hit(this, 100 + Utils.random(350), HitLook.REGULAR_DAMAGE));
            client.getPackets().sendGameMessage("<col=16711680>A pillar was destroyed!");
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                	if (raid.getStage() != Stages.RUNNING || !raid.getTeam().contains(client))
                		return;
                	client.getPackets().sendResetCamera();
                }
            }, 4);
        });
    }
    
	public void submit(Consumer<Player> consumer) {
		raid.getTargets(this).forEach(consumer);
	}
	
	@Override
	public void processNPC() {
		
	}
	
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
	
	@Override
	public void setTarget(Entity target) {
		
	}
	
}
