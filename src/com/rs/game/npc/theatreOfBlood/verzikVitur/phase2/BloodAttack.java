package com.rs.game.npc.theatreOfBlood.verzikVitur.phase2;

import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class BloodAttack implements TOBAction {

    private static final int PROJECTILE = 6591;
    private static final int PROJECTILE_HIT = 6592;

    @Override
    public int use(NPC npc) {
    	VerzikVitur boss = (VerzikVitur) npc;
        Player target = boss.getRandomPlayer();
        if (target == null) {
            return 0;
        }

        npc.faceEntity(target);
        npc.setNextAnimation(new Animation(28114));

        //Projectile projectile = new Projectile(PROJECTILE, npc, target, 6, 0, 40, 36, 41, 5);
        //World.addProjectile(projectile);
        int msDelay = World.sendProjectile(boss, target, PROJECTILE, 40, 36, 36, 41, 0, npc.getSize() * 32);

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
            	if (boss.hasFinished() || !boss.isRunning())
            		return;
                int damage = Utils.random(450);
                target.applyHit(new Hit(npc, damage, HitLook.MAGIC_DAMAGE));
                target.gfx(PROJECTILE_HIT);
                target.getPrayer().drainPrayer(Utils.random(damage));
            }
        }, CombatScript.getDelay(msDelay) + 1);
        return 4;
    }
}
