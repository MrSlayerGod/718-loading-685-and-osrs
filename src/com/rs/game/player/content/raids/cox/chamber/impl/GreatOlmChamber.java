package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.npc.cox.impl.GreatOlm;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.ChambersRewards;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colour;
import com.rs.utils.TopCox;

import static com.rs.game.tasks.WorldTasksManager.schedule;

/**
 * @author Simplex
 * @since Nov 01, 2020
 */
public class GreatOlmChamber extends Chamber {

    public GreatOlm olm;

    public GreatOlmChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    public int doorY;

    @Override
    public void onRaidStart() {
        GreatOlmChamber chamber = this;
        doorY = getRaid().getTile(134, 33).getY();
        olm = new GreatOlm(getRaid(), getRaid().getTile(38+96, 42), chamber);
    }

    @Override
    public void onActivation() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                olm.rise();
                stop();
            }
        }, 5, 1);
    }

    @Override
    public void bossDeath() {
        if(getRaid().hasFinished())
            return;

        /*getRaid().getTeam().forEach(p -> {
                    getRaid().addPoints(p.getUsername(), 100000);
                });*/
        // add rewards chest
        WorldObject chest = World.getObjectWithType(getRaid().getTile(33 + 96, 55, 0), 10);
        chest.updateId(Settings.OSRS_OBJECTS_OFFSET + 30028);

        // remove crystal blocking exit
        WorldObject crystal = World.getObjectWithType(getRaid().getTile(32 + 96, 53, 0), 10);
        World.sendObjectAnimation(crystal, new Animation(27506));

        schedule(() -> crystal.remove(), 3);

        int points = 0;
        for(Integer i : getRaid().getPointMap().values())
            points+=i;

        if(points > 10000 && getRaid().getRaidTime() > 10000) {
            TopCox.addRank(getRaid(), getRaid().isOsrsMode() ? 0 : 1, points);
        }

        getRaid().raidCompleted();

        getRaid().getTeam().forEach(p -> {
            p.sendMessage(Colour.RAID_PURPLE.wrap("Congratulations - your raid is complete! Duration: " + Colour.RED.wrap(getRaid().formatRaidTime()) + "."));
            p.completedChambers(getRaid());
        });

        ChambersRewards.giveRewards(getRaid(), chest);

        getRaid().getTeam().forEach(p -> {
            p.sendMessage(String.format("Total points: " + Colour.RAID_PURPLE.wrap("%,d") + ", Personal points: " + Colour.RAID_PURPLE.wrap("%,d") + " (" + Colour.RAID_PURPLE.wrap("%.2f") + "%%)",
                    getRaid().getPartyPoints(), getRaid().getPoints(p), ((double) getRaid().getPoints(p) / getRaid().getPartyPoints()) * 100));
        });
    }

    public GreatOlm getOlm() {
        return olm;
    }

}
