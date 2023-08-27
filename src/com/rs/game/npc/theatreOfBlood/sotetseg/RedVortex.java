package com.rs.game.npc.theatreOfBlood.sotetseg;


import java.util.List;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class RedVortex extends NPC {

    public static final int RED_VORTEX_ID = 28389;

    private int stepPosition;
    private boolean reverse;
    private TheatreOfBlood raid;
    private Sotetseg boss;

    public RedVortex(TheatreOfBlood raid, Sotetseg boss, WorldTile tile) {
        super(RED_VORTEX_ID, tile, -1, true, true);
        this.raid = raid;
        this.boss = boss;
    }

    @Override
    public void processNPC() {
        //super.tick();
    	if (raid == null || boss == null)
    		return;
        if (boss.isDead() || boss.hasFinished() || !boss.hasVortex()) {
            finish();
            return;
        }

        List<Player> targets = raid.getTargets(this);
        if (targets.isEmpty()) {
        	finish();
            return;
        }

        if (stepPosition == -1) {
            stepPosition = 0;
            reverse = false;
        }

        WorldTile tile = boss.getStep(reverse ? stepPosition-- : stepPosition++);
        if (tile != null) {
        	resetWalkSteps();
        	addWalkSteps(tile.getX(), tile.getY());
            //walkTo(new FixedTileStrategy(tile.getX(), tile.getY()), RouteType.SIMPLE);
        } else {
            reverse = true;
        }

        boolean damage = false;
        for (int index = targets.size() - 1; index >= 0; index--) {
            Player target = targets.get(index);
            if (!target.withinDistance(this, 0))
                continue;
            damage = true;
            break;
        }

        if (!damage) {
            return;
        }

        for (int index = targets.size() - 1; index >= 0; index--) {
        	Player target = targets.get(index);
            target.applyHit(new Hit(this, Utils.random(300) + 100, HitLook.REGULAR_DAMAGE));
        }
    }
}
