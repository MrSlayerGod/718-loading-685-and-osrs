package com.rs.game.npc.theatreOfBlood.nycolas;

import java.util.Comparator;
import java.util.Optional;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.NPC;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.nycolas.actions.MagicAttackAction;
import com.rs.game.npc.theatreOfBlood.nycolas.actions.MeleeAttackAction;
import com.rs.game.npc.theatreOfBlood.nycolas.actions.NycolasSpawnAction;
import com.rs.game.npc.theatreOfBlood.nycolas.actions.RangeAttackAction;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class NycolasSpawn extends NPC {

    public static final int[] NYLOCAS_ISCHYROS = {28342, 28345}; // Small, Large 
    public static final int[] NYLOCAS_TOXOBOLOS = {28343, 28346}; // Small, Large 
    public static final int[] NYLOCAS_HAGIOS = {28344, 28347}; // Small, Large 

    private TOBAction action;
    private int delay;

    private boolean reachedCenter;
    private WorldTile center;
    private TheatreOfBlood raid;
    
    public NycolasSpawn(TheatreOfBlood raid, int id, WorldTile tile, WorldTile center) {
        super(id, tile, -1, true, true);
        this.raid = raid;
        this.center = center;
        this.action = new NycolasSpawnAction();
        setDelay(8);
        setForceMultiArea(true);
        setIntelligentRouteFinder(true);
      //  setAggressive(false);
    }

    @Override
    public void processNPC() {
        if (isDead() || raid == null || raid.getTargets(this).isEmpty()) {
            return;
        }

        if (center != null && !reachedCenter) {
            if (Utils.collides(getX(), getY(), getSize(), center.getX(), center.getY(), 1)) {
                this.reachedCenter = true;
                delay = 0;
            } else {
            	resetWalkSteps();
            	if (!isFrozen())
            		addWalkSteps(center.getX(), center.getY(), 50, false);
            	return; //dont let it perform action or could bug
            }
        }

        if (action != null) {
            if (delay-- <= 0) {
                delay = action.use(this);
                if (delay == -1) {
                    action = null;
                }
            }
        } else {
            if (delay-- <= 0) {
                delay = getAction().use(this);
            }
        }
    }
    
    public TOBAction getAction() {
        if (isHagios(getId())) {
            return new MagicAttackAction();
        } else if (isIschyros(getId())) {
            return new MeleeAttackAction();
        }
        return new RangeAttackAction();
    }

    public void sendDeath(Entity killer) {
    	WorldTile tile = new WorldTile(this);
    	super.sendDeath(killer);
    	  if (getId() == NYLOCAS_HAGIOS[1]) {
              onLargeDeath(NYLOCAS_HAGIOS[0]);
          } else if (getId() == NYLOCAS_ISCHYROS[1]) {
              onLargeDeath(NYLOCAS_ISCHYROS[0]);
          } else if (getId() == NYLOCAS_TOXOBOLOS[1]) {
              onLargeDeath(NYLOCAS_TOXOBOLOS[0]);
          } //else {
              raid.getNycolasGenerator().removeSpawn(this);
        //  }
    }
  /*  @Override
    public void setHitpoints(int hp) {
        boolean deadBefore = isDead();
        super.setHitpoints(hp);
        if (!deadBefore && isDead()) {
            if (getId() == NYLOCAS_HAGIOS[1]) {
                onLargeDeath(NYLOCAS_HAGIOS[0]);
            } else if (getId() == NYLOCAS_ISCHYROS[1]) {
                onLargeDeath(NYLOCAS_ISCHYROS[0]);
            } else if (getId() == NYLOCAS_TOXOBOLOS[1]) {
                onLargeDeath(NYLOCAS_TOXOBOLOS[0]);
            } else {
                raid.getNycolasGenerator().removeSpawn(this);
            }
        }
    }*/

    private void onLargeDeath(int id) {
        WorldTile center = getMiddleWorldTile();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
            	if (raid == null || raid.getStage() != Stages.RUNNING)
            		return;
              /*  NycolasSpawn first = (NycolasSpawn) raid.spawnNpc(null, id, center.getX(), center.getY(), center.getPlane(),
                        -1, 11, 17, 200, 0, false, false);
                NycolasSpawn second = (NycolasSpawn) raid.spawnNpc(null, id, center.getX(), center.getY(), center.getPlane(),
                        -1, 11, 17, 200, 0, false, false);*/
                NycolasSpawn first = new NycolasSpawn(raid, id, center, !reachedCenter ? NycolasSpawn.this.center : null);
                NycolasSpawn second = new NycolasSpawn(raid, id, center, !reachedCenter ? NycolasSpawn.this.center : null);

                first.setDelay(2);
                second.setDelay(2);

              /*  if (!reachedCenter) {
                    first.setCenter(center);
                    second.setCenter(center);
                }*/

                raid.getNycolasGenerator().addSpawn(first);
                raid.getNycolasGenerator().addSpawn(second);
            }
        }, 4);
    }

    @Override
    public void handleIngoingHit(Hit hit) {
    	if (hit.getLook() == HitLook.MELEE_DAMAGE || hit.getLook() == HitLook.MAGIC_DAMAGE || hit.getLook() == HitLook.RANGE_DAMAGE) {
            int id = getId();
            if (isIschyros(id) && hit.getLook() != HitLook.MELEE_DAMAGE) {
                hit.setDamage(0);
            } else if (isToxobolos(id) && hit.getLook() != HitLook.RANGE_DAMAGE) {
                hit.setDamage(0);
            } else if (isHagios(id) && hit.getLook() != HitLook.MAGIC_DAMAGE) {
                hit.setDamage(0);
            }
    	}
        super.handleIngoingHit(hit);
    }
    
 /*   @Override
	public void addFreezeDelay(long time, boolean entangleMessage) {
    	 this.action = null;
         super.addFreezeDelay(time, entangleMessage);
	}*/

 /*   @Override
    public void setStunDelay(long delay) {
        this.action = null;
        super.setStunDelay(delay);
    }

    @Override
    public void setFrozenDelay(long delay) {
        this.action = null;
        super.setFrozenDelay(delay);
    }

    @Override
    public void setBoundDelay(long delay) {
        this.action = null;
        super.setBoundDelay(delay);
    }

    @Override
    public void freeze(int i) {
        this.action = null;
        super.freeze(i);
    }*/


    public boolean hasAction() {
        return action != null;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public static boolean isIschyros(int id) {
        for (int index = 0; index < NYLOCAS_ISCHYROS.length; index++) {
            if (NYLOCAS_ISCHYROS[index] == id)
                return true;
        }
        return false;
    }

    public static boolean isToxobolos(int id) {
        for (int index = 0; index < NYLOCAS_TOXOBOLOS.length; index++) {
            if (NYLOCAS_TOXOBOLOS[index] == id)
                return true;
        }
        return false;
    }

    public static boolean isHagios(int id) {
        for (int index = 0; index < NYLOCAS_HAGIOS.length; index++) {
            if (NYLOCAS_HAGIOS[index] == id)
                return true;
        }
        return false;
    }
    
	@Override
	public void setTarget(Entity target) {
		if (action instanceof NycolasSpawnAction)
			action = null;
	}
	
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
	
	public TheatreOfBlood getRaid() {
		return raid;
	}

    public double getDistance(double x, double y, double x2, double y2) {
        return Math.hypot(x - x2, y - y2);
    }
    
    public double getDistance(WorldTile a, WorldTile b) {
        return Math.hypot(a.getX() - b.getX(), a.getY() - b.getY());
    }

    public Player getClosestPlayer() {
        Optional<Player> client = raid.getTargets(this).stream().min(Comparator.comparingDouble(c -> getDistance(this, c)));
        return client.orElse(null);
    }
}

