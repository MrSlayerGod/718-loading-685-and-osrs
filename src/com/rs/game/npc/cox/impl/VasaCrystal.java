package com.rs.game.npc.cox.impl;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.impl.VasaChamber;
import com.rs.game.tasks.WorldTasksManager;

public class VasaCrystal extends COXBoss {

    private VasaChamber chamber;

    public VasaCrystal(ChambersOfXeric raid, int id, WorldTile tile, VasaChamber chamber) {
        super(raid, id, tile, chamber);
        this.chamber = chamber;
        this.setCantFollowUnderCombat(true);
        this.addFreezeDelay(Integer.MAX_VALUE);
    }

    public void resetAttackers() {
        getTeam().forEach(p -> {
            if (p != null && p.getAttackedBy() == this) {
                p.resetCombat();
                p.getActionManager().forceStop();
            }
        });
    }

    @Override
    public void sendDeath(Entity killer) {
        resetCombat();
        resetAttackers();
        regenerate();
        chamber.getVasa().setCrystalVulnerable(this, false);
        //finish();
    }

    private void regenerate() {
        WorldTasksManager.schedule(() -> {
            reset();
        }, 10);
    }

    @Override
    public boolean preAttackCheck(Player attacker) {
        if(!chamber.getVasa().getSelectedCrystalObject().matches(this)) {
            attacker.sendMessage("The crystal is currently invulnerable!");
            return false;
        }
        return true;
    }
}
