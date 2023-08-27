package com.rs.game.npc.theatreOfBlood.verzikVitur.phase3;


import java.util.List;

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

public class MagicAOEAttack implements TOBAction {

    @Override
    public int use(NPC npc) {
        VerzikVitur verzik = (VerzikVitur) npc;
        List<Player> targetList = verzik.getRaid().getTargets(verzik);
        if (targetList.isEmpty()) {
            return 0;
        }

        verzik.anim(28124);
        verzik.faceEntity(targetList.get(0));
        for (Player target : targetList) {
            //Projectile projectile = new Projectile(6580, npc, target, 6, 0, 30, 30, 41, 5);
           // World.addProjectile(projectile);
            int msDelay = World.sendProjectile(npc, target, 6580, 40, 36, 36, 41, 0, npc.getSize() * 32);

            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                	if (verzik.hasFinished() || !verzik.isRunning())
                		return;
                    target.applyHit(new Hit(verzik, Utils.random(380), HitLook.MAGIC_DAMAGE));
                }
            }, CombatScript.getDelay(msDelay) + 1);
        }
        return verzik.getPhaseThreeDelay();
    }
}
