package com.rs.game.npc.combat.impl.superiorslayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.content.Slayer;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Simplex
 * @since Aug 25, 2020
 */
public class MonstrousBasilisk extends CombatScript {

    public static final int ID = 27395;
    @Override
    public Object[] getKeys() {
        return new Object[]
                { ID };
    }

    @Override
    public int attack(NPC npc, final Entity target) {
        NPCCombatDefinitions def = npc.getCombatDefinitions();
        if (!Slayer.hasReflectiveEquipment(target)) {
            Player targetPlayer = (Player) target;
            int randomSkill = Utils.random(0, 6);
            int currentLevel = targetPlayer.getSkills().getLevel(randomSkill);
            targetPlayer.getSkills().set(randomSkill, currentLevel < 5 ? 0 : currentLevel - 5);
            delayHit(npc, 0, target, getMeleeHit(npc, targetPlayer.getMaxHitpoints() / 10));
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    target.setNextGraphics(new Graphics(747));
                }
            });
            // TODO player emote hands on ears
        } else
            delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), def.getAttackStyle(), target)));
        npc.setNextAnimation(new Animation(def.getAttackEmote()));
        return def.getAttackDelay();
    }
}
