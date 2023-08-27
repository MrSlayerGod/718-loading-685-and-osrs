package com.rs.game.npc.theatreOfBlood.verzikVitur;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

@SuppressWarnings("serial")
public class NycolasMemetos extends NPC {

    public static final int NYCOLAS_MEMTOS_ID = 28385;
    private static final int HEAL_PROJECTILE_ID = 6587;

    private int delay;
    private VerzikVitur boss;

    public NycolasMemetos(VerzikVitur boss, WorldTile tile) {
        super(NYCOLAS_MEMTOS_ID, tile, -1, true, true);
        this.boss = boss;
        this.delay = 15;
        setNextAnimation(new Animation(28098));
        setForceMultiArea(true);
        setIntelligentRouteFinder(true);
    }

    @Override
    public void processNPC() {
        //super.tick();
        if (isDead() || boss == null || !boss.isRunning() || isDead() ) {
            return;
        }
        randomWalk();

        if (boss.isDead() || boss.getId() != VerzikVitur.PHASE_2) {
            finish();
            return;
        }

        if (delay-- != 0) {
            return;
        }

        setNextAnimation(new Animation(28097));

       // Projectile projectile = new Projectile(HEAL_PROJECTILE_ID, this, boss, 6, 0, 40, 36, 41, 5);
        //World.addProjectile(projectile);
        int msDelay = World.sendProjectile(this, boss, HEAL_PROJECTILE_ID, 40, 36, 36, 41, 0, getSize() * 32);

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
            	if (hasFinished() || isDead() || boss.hasFinished() || !boss.isRunning())
            		return;
              //  finish();
            	sendDeath(NycolasMemetos.this);
            }
        }, 3);

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
            	if (hasFinished() || isDead() || boss.hasFinished() || !boss.isRunning())
            		return;
                boss.heal(getHitpoints() << 1);
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
