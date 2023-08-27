package com.rs.game.npc.theatreOfBlood.sotetseg.actions;


import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.NPC;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.sotetseg.Sotetseg;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class MeleeAttack implements TOBAction {

    @Override
    public int use(NPC npc) {
    	Sotetseg boss = (Sotetseg) npc;
        Player target = boss.getRandomPlayer();
        if (target == null) {
            return 0;
        }

        if (!Utils.isOnRange(target.getX(), target.getY(), target.getSize(), npc.getX(), npc.getY(), npc.getSize(), 1)) {
            return 0;
        }
        npc.setNextFaceEntity(target);
        npc.setNextAnimation(new Animation(28138));

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
            	   if (!target.getPrayer().isMeleeProtecting()) 
                   	target.setPrayerDelay(3000/*4800*/);
            	   target.applyHit(new Hit(npc, Utils.random(450), HitLook.MELEE_DAMAGE));
            }
        }, 0);
        return 4;
    }
}
