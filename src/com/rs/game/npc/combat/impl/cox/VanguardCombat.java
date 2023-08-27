package com.rs.game.npc.combat.impl.cox;

import com.rs.Settings;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.npc.cox.impl.Vanguard;
import com.rs.game.player.Projectile;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.raids.cox.chamber.impl.VanguardChamber;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.NPCCombatDefinitionsL;
import com.rs.utils.Utils;

/**
 * @author Simplex
 * @since Oct 26, 2020
 */
public class VanguardCombat extends CombatScript {
    private static final Projectile RANGED_PROJECTILE = new Projectile(Settings.OSRS_GFX_OFFSET + 1332, 5, 0, 20, 0, 60, 64);
    private static final Projectile MAGIC_PROJECTILE = new Projectile(Settings.OSRS_GFX_OFFSET + 1331, 5, 0, 20, 0, 60, 64);

    @Override
    public Object[] getKeys() {
        return new Object[]{Settings.OSRS_NPC_OFFSET + 7527, Settings.OSRS_NPC_OFFSET + 7528, Settings.OSRS_NPC_OFFSET + 7529};
    }

    public void meleeAttack(NPC npc, Entity target) {
        NPCCombatDefinitions def = NPCCombatDefinitionsL.getNPCCombatDefinitions(npc.getId());
        npc.anim(def.getAttackEmote());
        int dmg = def.getMaxHit() / (target.isPlayer() && target.asPlayer().getPrayer().isMeleeProtecting() ? 2 : 1);
        target.applyHit(new Hit(npc, Utils.random(dmg), Hit.HitLook.MELEE_DAMAGE));
    }

    @Override
    public int attack(NPC npc, Entity target) {
        if (isWalking(npc)) {
            return 0;
        }
        NPCCombatDefinitions def = NPCCombatDefinitionsL.getNPCCombatDefinitions(npc.getId());
        switch (npc.getId()) {
            case 27527:
                meleeAttack(npc, target);
                break;
            case 27529:
                magicAttack(npc, target);
                break;
            case 27528:
                rangedAttack(npc, target);
                break;
        }

        return def.getAttackDelay();
    }

    private boolean isWalking(NPC npc) {
        Vanguard v = (Vanguard) npc;
        VanguardChamber chamber = (VanguardChamber) v.getChamber();
        return chamber.walking;
    }

    private void rangedAttack(NPC npc, Entity target) {
        Vanguard vanguard = (Vanguard) npc;

        NPCCombatDefinitions def = NPCCombatDefinitionsL.getNPCCombatDefinitions(npc.getId());
        npc.anim(def.getAttackEmote());
        //int dmg = target.asPlayer().getPrayer().isRangeProtecting() ? def.getMaxHit() : def.getMaxHit() / 2;
        int delay = RANGED_PROJECTILE.fire(npc, target.clone());
        //delayHit(npc, CombatScript.getDelay(delay), target, new Hit(npc, Utils.random(dmg), Hit.HitLook.RANGE_DAMAGE));
        //target.applyHit(getRangeHit(vanguard, getRandomMaxHit(vanguard, def.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
        for (int i = 0; i < 2; i++) {
            WorldTile pos;
            if(i == 0)
                pos = target.clone();
            else
                pos = Utils.get(target.area(1, (worldTile -> World.isTileFree(worldTile, 1))));
            WorldTasksManager.schedule(() -> {
                World.sendGraphics(vanguard, new Graphics(Settings.OSRS_GFX_OFFSET + 659, 0, 40), pos);
                if (!isWalking(npc))
                    vanguard.getTeam().stream().filter(p->p.matches(pos)).forEach(p->p.applyHit(getRangeHit(vanguard, getRandomMaxHit(vanguard, 220, NPCCombatDefinitions.RANGE, p))));
            }, CombatScript.getDelay(delay));
        }
    }

    private void magicAttack(NPC npc, Entity target) {
        Vanguard vanguard = (Vanguard) npc;

        NPCCombatDefinitions def = NPCCombatDefinitionsL.getNPCCombatDefinitions(npc.getId());
        npc.anim(def.getAttackEmote());
        //int dmg = def.getMaxHit();
        int delay = MAGIC_PROJECTILE.fire(npc, target.clone());
        //delayHit(npc, CombatScript.getDelay(delay), target, new Hit(npc, Utils.random(dmg), Hit.HitLook.MAGIC_DAMAGE));
        //target.applyHit(getMagicHit(vanguard, getRandomMaxHit(vanguard, def.getMaxHit(), NPCCombatDefinitions.MAGE, target)));

        for (int i = 0; i < 2; i++) {
            WorldTile pos;
            if(i == 0)
                pos = target.clone();
            else
                pos = Utils.get(target.area(1, (worldTile -> World.isTileFree(worldTile, 1))));
            WorldTasksManager.schedule(() -> {
                World.sendGraphics(vanguard, new Graphics(Settings.OSRS_GFX_OFFSET + 659, 0, 40), pos);
                if (!isWalking(npc))
                    vanguard.getTeam().stream().filter(p->p.matches(pos)).forEach(p->p.applyHit(getMagicHit(vanguard, getRandomMaxHit(vanguard, 220, NPCCombatDefinitions.MAGE, p))));
            }, CombatScript.getDelay(delay));
        }
    }
}
