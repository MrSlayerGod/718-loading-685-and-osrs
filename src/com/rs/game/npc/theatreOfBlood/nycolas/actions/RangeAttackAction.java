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

public class RangeAttackAction implements TOBAction {

    @Override
    public int use(NPC npc) {
        Player client = npc instanceof Nycolas ? ((Nycolas)npc).getClosestPlayer() :  ((NycolasSpawn)npc).getClosestPlayer();
        if (client == null) {
            return 0;
        }

        npc.setNextFaceWorldTile(client);
        npc.setNextAnimation(new Animation(27999));

        
        int msDelay = World.sendProjectile(npc, client, getProjectile(npc), 0, 41, 36, 41, 16, npc.getSize() * 32);
        Default.delayHit(npc, CombatScript.getDelay(msDelay), client, Default.getRangeHit(npc, Default.getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, client)));

        
    /*    Projectile projectile = new Projectile(getProjectile(npc), npc, client, 6, 0, 40, 36, 41, 5);
        World.addProjectile(projectile);
        Tasks.scheduleWorldTask(new TaskNew(npc) {
            @Override
            public void run() {
                client.hit(new Hit(npc, Misc.random3(npc.maxHit()), HitType.RANGED));
            }
        }, projectile.getCycleDelay(), 0);*/
        return 5;
    }

    private int getProjectile(NPC npc) {
        switch (npc.getId()) {
            case Nycolas.RANGE_NYCOLAS_ID:
                return 6561;
            case 28346:
                return 6560;
            case 28343:
                return 6559;
        }
        return -1;
    }
}
