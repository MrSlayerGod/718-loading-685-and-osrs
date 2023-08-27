package com.rs.game.npc.cox.impl;

import com.rs.Settings;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.impl.VanguardChamber;
import com.rs.game.tasks.WorldTasksManager.WorldTaskList;
import com.rs.utils.NPCDrops;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simplex
 * @since Oct 26, 2020
 */
public class Vanguard extends COXBoss {


    public Vanguard(int id, WorldTile tile, ChambersOfXeric raid) {
        super(raid, id, tile, raid.getVanguardChamber());
        setCanWalkNPC(true);
        setDrops();
        setCombatLevel(500);
    }

    @Override
    public boolean preAttackCheck(Player attacker) {
        return this.getId() != 27525;
    }

    @Override
    public void processNPC() {
        Player target = getClosestPlayer();
        if(target != null) {
            if (target.getLastTarget() != this && target.distance(this.getRespawnTile()) > 7)
                setTarget(null);
            else if(getCombat().getTarget() != target && distance(target) <= 8)
                setTarget(target);
        }
        super.processNPC();
    }

    public void setDrops() {
        Drops drops = new Drops(false);
        @SuppressWarnings("unchecked")
        List<Drop>[] dList = new ArrayList[Drops.VERY_RARE + 1];
        for (int i = 0; i < dList.length; i++)
            dList[i] = new ArrayList<Drop>();
        for(Drop drop : ALWAYS_DROPS) {
            dList[Drops.ALWAYS].add(drop);
        }
        for(Drop drop : COMMON_DROPS) {
            dList[Drops.COMMOM].add(drop);
        }
        drops.addDrops(dList);
        NPCDrops.addDrops(VanguardChamber.MAGE, drops);
        NPCDrops.addDrops(VanguardChamber.RANGED, drops);
        NPCDrops.addDrops(VanguardChamber.MELEE, drops);
        // down/inactive
        NPCDrops.addDrops(27525, drops);
        NPCDrops.addDrops(27526, drops);
    }

    private static Drop[] ALWAYS_DROPS =
            {
                    new Drop(50895, 1, 1), 	// Vanguard judgement
                    new Drop(50924, 1, 1),	// Elder (+)(4)
                    new Drop(50936, 1, 1),	// Twisted (+)(4)
                    new Drop(50948, 1, 1),	// Kodai (+)(4)
                    new Drop(50984, 1, 2),	// Xeric's aid (+)(4)
            };

    private static Drop[] COMMON_DROPS =
            {
                    new Drop(50960, 1, 1),	// Revitalisation (+)(4)
                    new Drop(50996, 1, 1)	// Overload (+)(4)
            };
    @Override
    public void handleIngoingHit(final Hit hit) {
        if(getId() == 27526) {
            hit.setDamage(0);
            resetReceivedDamage();
            resetReceivedHits();
            return;
        }

        if(getId() == VanguardChamber.RANGED && hit.getLook() != Hit.HitLook.MELEE_DAMAGE) {
            if(hit.getSource().isPlayer()) {
                hit.getSource().asPlayer().sendMessage("Only melee attacks will effect the ranged Vanguard!");
            }
            hit.setDamage(0);
        }

        if(getId() == VanguardChamber.MAGE && hit.getLook() != Hit.HitLook.RANGE_DAMAGE) {
            if(hit.getSource().isPlayer()) {
                hit.getSource().asPlayer().sendMessage("Only ranged attacks will effect the mage Vanguard!");
            }
            hit.setDamage(0);
        }

        if(getId() == VanguardChamber.MELEE && hit.getLook() != Hit.HitLook.MAGIC_DAMAGE) {
            if(hit.getSource().isPlayer()) {
                hit.getSource().asPlayer().sendMessage("Only magic attacks will effect the melee Vanguard!");
            }
            hit.setDamage(0);
        }

        super.handleIngoingHit(hit);
    }

    public void debug(String s) {
        if(Settings.DEBUG) {
            //
            // forceTalk(s);
            getTeam().forEach(plr->plr.asPlayer().sendMessage("<col=ff981f><shad=0>[DEBUG]: <col=ffffff><shad=0>" + s));
        }
    }

    @Override
    public void sendDeath(Entity source) {
        super.sendDeath(source);
    }
}
