package com.rs.game.npc.theatreOfBlood.verzikVitur.phase2;


import java.util.Collections;
import java.util.List;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class ChainAttack implements TOBAction {

    private static final int PROJECTILE = 6585;

    @Override
    public int use(NPC npc) {
    	VerzikVitur boss = (VerzikVitur) npc;
        List<Player> targets = boss.getRaid().getTargets(npc);
        if (targets.isEmpty()) {
            return 0;
        }

        Collections.shuffle(targets);

        npc.anim(28114);
        npc.faceEntity(targets.get(0));

        Entity start = npc;
        int delay = 0;
        for (int index = 0; index < targets.size(); index++) {
        	Player target = targets.get(index);
           // Projectile projectile = new Projectile(PROJECTILE, start, target, 6, 0, 40, 36, 41, 5);
        	int msDelay = Utils.getProjectileTime(start, target, 40, 36, 36/10, 41, 16, npc.getSize() * 32);
        	
        	
            final Entity finalStart = start;
            int finalIndex = index;
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                	if (boss.hasFinished() || !boss.isRunning())
                		return;
                   // World.addProjectile(projectile);
                	int msDelay = World.sendProjectile(finalStart, target, PROJECTILE,40, 36, 36, 41, 16, npc.getSize() * 32);
                	 WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                        	if (boss.hasFinished() || !boss.isRunning())
                        		return;
                            int damage = Utils.random(finalIndex == targets.size() - 1 ? 450 : 80);
                            if (target.getEquipment().getBootsId() == 7159 || target.getEquipment().getBootsId() == 7161)
                                damage /= 2;
                            target.applyHit(new Hit(npc, damage, HitLook.RANGE_DAMAGE));
                        }
                    }, CombatScript.getDelay(msDelay) + 1);
                }
            }, delay);

            delay += CombatScript.getDelay(msDelay) + 1;
            start = target;

            if (passesThroughBoss(start, target, npc)) {
                break;
            }
        }
        return 4;
    }

    private boolean passesThroughBoss(Entity start, Entity target, Entity boss) {
        WorldTile step = start.getMiddleWorldTile();
        WorldTile dest = target.getMiddleWorldTile();
        int step2 = 0;
        while ((step.getX() != dest.getX() || step.getY() != dest.getY())  && step2++ < 200) {
            int xOffset = step.getX() > dest.getX() ? -1 : step.getX() < dest.getX() ? 1 : 0;
            int yOffset = step.getY() > dest.getY() ? -1 : step.getY() < dest.getY() ? 1 : 0;
            WorldTile next = step.transform(xOffset, yOffset, 0);
            if (Utils.collides(next.getX(), next.getY(), 1, boss.getX(), boss.getY(), boss.getSize())) {
               return true;
            }
            step.moveLocation(xOffset, yOffset, 0);
        }
        return false;
    }
}
