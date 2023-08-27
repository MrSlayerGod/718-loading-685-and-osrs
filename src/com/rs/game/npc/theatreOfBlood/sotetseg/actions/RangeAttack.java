package com.rs.game.npc.theatreOfBlood.sotetseg.actions;


import java.util.Collections;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.sotetseg.Sotetseg;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class RangeAttack implements TOBAction {

    private static final int PROJECTILE = 6607;

    @Override
    public int use(NPC npc) {
    	Sotetseg boss = (Sotetseg) npc;
        List<Player> targets = boss.getRaid().getTargets(npc);
        if (targets.isEmpty()) {
            return 0;
        }

        Collections.shuffle(targets);

        npc.setNextFaceEntity(targets.get(0));
        npc.setNextAnimation(new Animation(28139));

        Entity start = npc;
        int delay = 0;
        for (int index = 0; index < targets.size(); index++) {
            Player target = targets.get(index);
            int msDelay = Utils.getProjectileTime(start, target, 40, 36, 36/10, 41, 16, npc.getSize() * 32);
            //Projectile projectile = new Projectile(PROJECTILE, start, target, 6, 0, 40, 36, 41, 5);

           final Entity finalStart = start;
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                	if (boss.hasFinished() || !boss.isRunning())
                		return;
                 //   World.addProjectile(projectile);
                	int msDelay = World.sendProjectile(finalStart, target, PROJECTILE,40, 36, 36, 41, 16, npc.getSize() * 32);
                	   WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                        	if (boss.hasFinished() || !boss.isRunning())
                        		return;
                            if (!target.getPrayer().isRangeProtecting()) 
                            	target.setPrayerDelay(3000/*4800*/);
                            target.applyHit(new Hit(npc, Utils.random(450), HitLook.RANGE_DAMAGE));
                        }
                    }, CombatScript.getDelay(msDelay) + 1);
                }
            }, delay);

            delay += CombatScript.getDelay(msDelay) + 1;
            start = target;
        }
        return 4;
    }
}
