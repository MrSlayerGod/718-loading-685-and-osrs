package com.rs.game.npc.combat.impl;

import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.worldboss.CallusFrostborne;
import com.rs.utils.Utils;

public class CallusPhase1 extends CombatScript {

    @Override
    public Object[] getKeys() {
        return new Object[]
                {21200};
    }

    @Override
    public int attack(final NPC npc, final Entity targetEntity) {
        if(Utils.random(CallusFrostborne.ARENA_CLEAR_CHANCE) == 0) {
            return ((CallusFrostborne) npc).arenaClearAttack();
        }
        if(Utils.random(CallusFrostborne.ICE_BALL_CHANCE) == 0) {
            return ((CallusFrostborne) npc).iceballBarrageAttack();
        }
        if(Utils.random(CallusFrostborne.SNOW_STORM_CHANCE) == 0) {
            return ((CallusFrostborne) npc).snowScreenAttack();
        }

		return ((CallusFrostborne) npc).standardAttack(this);
    }

}
