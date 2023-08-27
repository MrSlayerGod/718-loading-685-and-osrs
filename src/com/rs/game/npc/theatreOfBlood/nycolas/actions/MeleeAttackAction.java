package com.rs.game.npc.theatreOfBlood.nycolas.actions;


import com.rs.game.Animation;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.Default;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.nycolas.Nycolas;
import com.rs.game.npc.theatreOfBlood.nycolas.NycolasSpawn;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class MeleeAttackAction implements TOBAction {

    @Override
    public int use(NPC npc) {
        Player client = npc.getId() == Nycolas.MELEE_NYCOLAS_ID ? ((Nycolas)npc).getRandomPlayer() : (npc instanceof Nycolas ? ((Nycolas)npc).getClosestPlayer() :  ((NycolasSpawn)npc).getClosestPlayer());
        if (client == null) {
            return 0;
        }
        if (!Utils.isOnRange(client, npc, 0)) {
          //  npc.walkTo(new EntityStrategy(client), Entity.RouteType.ADVANCED);
        	npc.resetWalkSteps();
        	npc.calcFollow(client, -1, true, true);
            return 0;
        }
        npc.setNextFaceWorldTile(client);
        npc.setNextAnimation(new Animation(28004));
        Default.delayHit(npc, 0, client, Default.getMeleeHit(npc, Default.getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, client)));
        //client.hit(new Hit(npc, Misc.random3(npc.maxHit()), HitType.MELEE));
        return 5;
    }
}
