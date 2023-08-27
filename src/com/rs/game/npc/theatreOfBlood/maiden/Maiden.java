package com.rs.game.npc.theatreOfBlood.maiden;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.TOBBoss;
import com.rs.game.npc.theatreOfBlood.maiden.actions.BloodSplat;
import com.rs.game.npc.theatreOfBlood.maiden.actions.TornadoMagic;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.utils.Utils;

/**
 * 
 * @author cjay
 * Converted to onyx by dragonkk(alex)
 */
@SuppressWarnings("serial")
public class Maiden extends TOBBoss {


    public static final int MAIDEN_ID = 28360;
	
	public Maiden(TheatreOfBlood raid) {
		super(raid, 0, MAIDEN_ID, raid.getTile(90, 28, 0));
		this.spots = new LinkedList<>();
		setNextFaceWorldTile(transform(7, 2, 0));
		setCantFollowUnderCombat(true);
	}

	private static final double[] HEALTH_RATIOS = { 0.9, 0.75, 0.50, 0.25, 0.0 };
	private static final WorldTile[] SPAWN_LOCATIONS = { // TODO convert
			new WorldTile(102, 19, 0), new WorldTile(102, 42, 0), new WorldTile(106, 19, 0),
			new WorldTile(106, 42, 0), new WorldTile(110, 19, 0), new WorldTile(110, 42, 0),
			new WorldTile(114, 19, 0), new WorldTile(114, 42, 0), new WorldTile(115, 22, 0),
			new WorldTile(115, 39, 0), };

	private int delay, minimumTornadoDamage;
	private List<BloodSpot> spots;
	
    private static class BloodSpot {
        private final WorldTile tile;
        private int duration;

        public BloodSpot(WorldTile tile, int duration) {
            this.tile = tile;
            this.duration = duration;
        }
    }

    @Override
    public void processNPC() {
        if (isDead() || raid.getTargets(this).isEmpty()) 
            return;

        Iterator<BloodSpot> it$ = spots.iterator();
        while (it$.hasNext()) {
            BloodSpot spot = it$.next();

            submit(client -> {
                if (!Utils.collides(spot.tile.getX(), spot.tile.getY(), 1, client.getX(), client.getY(), client.getSize())) 
                    return;
                int damage = Utils.random(200) + 1;
                client.applyHit(new Hit(this, damage, HitLook.REGULAR_DAMAGE));
                heal(damage);
            });

            if (spot.duration-- != 0) {
                continue;
            }

            it$.remove();
        }

        if (delay-- > 0) {
            return;
        }

        delay = getNextAction().use(this);
       // super.processNPC();
		raid.setHPBar(this);
    }

    
    @Override
    public void handleIngoingHit(Hit hit) {
        super.handleIngoingHit(hit);

        int maxHp = getMaxHitpoints(), addition = 0;
        for (int index = HEALTH_RATIOS.length - 1; index >= 0; index--) {
            double ratio = HEALTH_RATIOS[index];
            if (maxHp * ratio >= getHitpoints() - hit.getDamage()) {
                addition++;
            }
        }

        int nextID = MAIDEN_ID + addition;
        if (nextID > getId()) {
            if (nextID > getId() && addition != 1 && addition != 5) {
                for (int count = 0; count < raid.getTeamSize() * 2; count++) {
                    WorldTile tile = SPAWN_LOCATIONS[count];
                    WorldTile real = raid.getTile(tile.getX(), tile.getY(), tile.getPlane());
                    new NylocasMatomenos(this, real);/* (NylocasMatomenos) raid.spawnNpc(null, NylocasMatomenos.NYLOCAS_MATOMENOS_ID, real.getX(), real.getY(), real.getRealLevel(),

                            -1, 200, 11, 0, 90, false, false);*/
                //    healer.setAggressive(false);
                 //   healer.randomWalk = false;
                  //  healer.walkTo(new EntityStrategy(this), RouteType.ADVANCED);
                }
            }

            this.setNextNPCTransformation(MAIDEN_ID + addition);
        }
    }
    

    /*@Override
    public void setHitpoints(int hp) {
    	if (spots != null) {
            int maxHp = getMaxHitpoints(), addition = 0;
            for (int index = HEALTH_RATIOS.length - 1; index >= 0; index--) {
                double ratio = HEALTH_RATIOS[index];
                if (maxHp * ratio >= hp) {
                    addition++;
                }
            }
            int nextID = MAIDEN_ID + addition;
            if (nextID != getId()) 
            	setNextNPCTransformation(MAIDEN_ID + addition);
    	}
        super.setHitpoints(hp);
    }*/
    

    private TOBAction getNextAction() {
        if (Utils.random(8) == 1)
            return new BloodSplat();
        return new TornadoMagic();
    }

    public void incrementMinimumTornadoDamage() {
        this.minimumTornadoDamage+= 10;
    }

    public int getMinimumTornadoDamage() {
        return minimumTornadoDamage;
    }
    

    public void addSpot(WorldTile tile, int duration) {
        for (BloodSpot spot : spots) {
            if (!spot.tile.matches(tile))
                continue;
            spot.duration = duration;
            return;
        }

        spots.add(new BloodSpot(tile, duration));
    }

    public void clearBloodSpots() {
        spots.clear();
    }
    
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
	
	@Override
	public void setTarget(Entity target) {
		
	}
    
}
