package com.rs.game.npc.theatreOfBlood.verzikVitur.phase3;


import java.util.List;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.NPC;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class MeleeAOEAttack implements TOBAction {

    @Override
    public int use(NPC npc) {
        final VerzikVitur verzik = (VerzikVitur) npc;
        final List<Player> targetList = verzik.getRaid().getTargets(verzik);
        if (targetList.isEmpty()) {
            return 0;
        }

        Player tank = targetList.get(0);
        if (!Utils.isOnRange(tank, verzik, 1)) {
            return 0;
        } else if (Utils.collides(tank, verzik)) {
            return 0;
        }

        verzik.anim(28123);
        verzik.faceEntity(tank);
        for (Player target : targetList) {
            if (!Utils.isOnRange(target, verzik, 1)) {
                continue;
            }

            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                 	if (verzik.hasFinished() || !verzik.isRunning())
                		return;
                 	 target.applyHit(new Hit(verzik, Utils.random(680), HitLook.MELEE_DAMAGE));
                }
            },0);
        }
        return verzik.getPhaseThreeDelay();
    }
}
