package com.rs.game.npc.theatreOfBlood.verzikVitur.phase2;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.NPC;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class SlamAttack implements TOBAction {

    @Override
    public int use(NPC npc) {
        boolean inMeleeRange = true;
        VerzikVitur boss = (VerzikVitur) npc;
        for (Player target : boss.getRaid().getTargets(npc)) {
            if (Utils.isOnRange(target, npc, 0))
                continue;
            inMeleeRange = false;
        }

        if (!inMeleeRange) {
            return 0;
        }
        npc.anim(28116);
        for (Player target : boss.getRaid().getTargets(npc)) {
             if (Utils.isOnRange(target, npc, 0))
                continue;

            if (!target.isFrozen()) {
                //target.setStunDelay(7);
            	target.addFreezeDelay(4200);
                target.gfx(6575);
            }

            target.applyHit(new Hit(npc, Utils.random(400), HitLook.MELEE_DAMAGE));
        }

        return 4;
    }
}
