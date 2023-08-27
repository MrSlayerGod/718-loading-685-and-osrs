package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.game.*;
import com.rs.game.npc.Drop;
import com.rs.game.npc.cox.impl.ChambersGuardian;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.utils.Utils;

/**
 * @author Simplex
 * @since Dec 04, 2020
 */
public class GuardianChamber extends Chamber {

    public static WorldTile[] SPAWNS = {new WorldTile(7, 12, 1),
                                        new WorldTile(7, 16, 1)};

    public ChambersGuardian guardian[] = new ChambersGuardian[2];

    public GuardianChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    @Override
    public void onRaidStart() {
        for (int i = 0; i < guardian.length; i++) {
            guardian[i] = new ChambersGuardian(getRaid(), 27569 + i, getWorldTile(SPAWNS[i]), this);
            World.addFloor(guardian[i].clone());
        }
    }

    @Override
    public boolean chamberCompleted(Player player) {
        for(ChambersGuardian guardian : guardian) {
            if(guardian.isDead() || guardian.hasFinished())
                continue;
            else {
                player.sendMessage("The guardians prevent you from passing.");
                player.applyHit(player, Utils.random(50, 100));
                return false;
            }
        }
        return true;
    }
}

