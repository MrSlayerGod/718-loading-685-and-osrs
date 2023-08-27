package com.rs.game.npc.combat.impl.superiorslayer;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;

/**
 * @author Simplex
 * @since Sep 22, 2020
 */
public class SpikedTuroth extends CombatScript {

    public static final int ID = 7800;

    @Override
    public Object[] getKeys() {
        return new Object[]
                { ID };
    }

    @Override
    public int attack(NPC npc, Entity target) {
        NPCCombatDefinitions def = npc.getCombatDefinitions();
        npc.anim(def.getAttackEmote());
        delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), def.getAttackStyle(), target)));
        return def.getAttackDelay();
    }
}
