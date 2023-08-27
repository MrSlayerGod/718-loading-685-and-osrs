package com.rs.game.npc.combat.impl.superiorslayer;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;

/**
 * @author Simplex
 * @since Sep 22, 2020
 */
public class MalevolentMage extends CombatScript {

    public static final int ID = 27396;

    @Override
    public Object[] getKeys() {
        return new Object[]
                { 27395 };
    }

    @Override
    public int attack(NPC npc, Entity target) {
        NPCCombatDefinitions def = npc.getCombatDefinitions();
        npc.anim(def.getAttackEmote());
        delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), def.getAttackStyle(), target)));
        return def.getAttackDelay();
    }
}
