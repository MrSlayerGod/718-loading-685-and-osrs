package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;

/**
 * @author Simplex
 * @since Nov 05, 2020
 */
public class ResourceChamber extends Chamber {

    public ResourceChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    @Override
    public void onRaidStart() {
    }
}
