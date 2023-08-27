package com.rs.game.npc.theatreOfBlood.nycolas.actions;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.Default;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.nycolas.Nycolas;
import com.rs.game.npc.theatreOfBlood.nycolas.NycolasSpawn;
import com.rs.game.player.Player;

public class MagicAttackAction implements TOBAction {

    @Override
    public int use(NPC npc) {
    	   Player client = npc instanceof Nycolas ? ((Nycolas)npc).getClosestPlayer() :  ((NycolasSpawn)npc).getClosestPlayer();
        if (client == null) {
            return 0;
        }
        npc.setNextFaceWorldTile(client);
        npc.setNextAnimation(new Animation(27989));


        //        Default.delayHit(npc, 0, client, Default.getMeleeHit(npc, Default.getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, client)));

        
        int msDelay = World.sendProjectile(npc, client, 6580, 0, 41, 36, 41, 16, npc.getSize() * 32);
        Default.delayHit(npc, CombatScript.getDelay(msDelay), client, Default.getMagicHit(npc, Default.getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, client)));

      /*  Projectile projectile = new Projectile(6580, npc, client, 6, 0, 30, 30, 41, 5);
        World.addProjectile(projectile);
        Tasks.scheduleWorldTask(new TaskNew(npc) {
            @Override
            public void run() {
                client.hit(new Hit(npc, Misc.random3(npc.maxHit()), HitType.MAGIC));
            }
        }, projectile.getCycleDelay(), 0);*/
        return 5;
    }
}
