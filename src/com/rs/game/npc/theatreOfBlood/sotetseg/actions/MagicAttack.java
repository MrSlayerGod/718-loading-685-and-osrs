package com.rs.game.npc.theatreOfBlood.sotetseg.actions;

import com.rs.game.Animation;
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

public class MagicAttack implements TOBAction {

    private static final int PROJECTILE = 6606;

    @Override
    public int use(NPC npc) {
    	Sotetseg boss = (Sotetseg) npc;
        Player target = boss.getRandomPlayer();
        if (target == null) {
            return 0;
        }

        npc.setNextFaceEntity(target);
        npc.setNextAnimation(new Animation(28139));

       // Projectile projectile = new Projectile(PROJECTILE, npc, target, 3, 0, 40, 36, 41, 5);
        //World.addProjectile(projectile);
        int msDelay = World.sendProjectile(npc, target, PROJECTILE, 40, 36, 36, 41, 16, npc.getSize() * 32);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (!target.getPrayer().isMageProtecting()) 
                	target.setPrayerDelay(3000/*4800*/);
                
                target.applyHit(new Hit(npc, Utils.random(450), HitLook.MAGIC_DAMAGE));
            }
        }, CombatScript.getDelay(msDelay)+1);
        return 4;
    }
}
