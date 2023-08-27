package com.rs.game.npc.theatreOfBlood.verzikVitur;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class NycolasAthanatos extends NPC {

    private static final int HEAL_PROJECTILE_ID = 6587, POSIION_PROJECTILE_ID = 6588;
    public static final int NYCOLAS_ATHANTOS_ID = 28384;

    private int delay;
    private VerzikVitur boss;

    public NycolasAthanatos(VerzikVitur boss, WorldTile tile) {
        super(NYCOLAS_ATHANTOS_ID, tile, -1, true, true);
        this.boss = boss;
        setForceMultiArea(true);
        setIntelligentRouteFinder(true);
    }

    @Override
    public void processNPC() {
        //super.tick();

        if (boss == null || !boss.isRunning() || isDead()) {
            return;
        }

        if (boss.isDead() || boss.getId() != VerzikVitur.PHASE_2) {
            finish();
            return;
        }
        this.resetWalkSteps();
        calcFollow(boss, true);
     //   walkTo(new EntityStrategy(boss), RouteType.ADVANCED);
        if (!Utils.isOnRange(this, boss, 1)) {
            return;
        }

        resetWalkSteps();

        if (delay-- > 0) {
            return;
        }

        delay = 4;
        faceEntity(boss);

        final boolean poisoned = getPoison().isPoisoned();
        //Projectile projectile = new Projectile(poisoned ? POSIION_PROJECTILE_ID : HEAL_PROJECTILE_ID, this, boss, 6, 0, 40, 36, 41, 5);
      //  World.addProjectile(projectile);
        int msDelay = World.sendProjectile(this, boss, poisoned ? POSIION_PROJECTILE_ID : HEAL_PROJECTILE_ID, 40, 36, 36, 41, 0, getSize() * 32);
        
        
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
            	if (hasFinished() || isDead() || boss.hasFinished() || !boss.isRunning())
            		return;
                if (poisoned) {
                    boss.getPoison().makePoisoned(Utils.random(500) + 200);
                 //   finish();
                	sendDeath(NycolasAthanatos.this);
                } else {
                    boss.heal(Utils.random(150, 500));
                }
            }
        }, CombatScript.getDelay(msDelay) + 1);
    }
    
	@Override
	public void setTarget(Entity target) {

	}
	
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
}
