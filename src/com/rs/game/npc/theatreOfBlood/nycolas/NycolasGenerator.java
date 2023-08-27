package com.rs.game.npc.theatreOfBlood.nycolas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.utils.Utils;

public class NycolasGenerator extends TimerTask {

    private static final int EAST = 0, WEST = 1, SOUTH = 2;

    private static final WorldTile[] CENTERS = { 
            new WorldTile(165, 88, 0),
            new WorldTile(154, 88, 0),
            new WorldTile(159, 83, 0),
    };
    private static final WorldTile[] CENTER_ADJACENTS = {
            new WorldTile(165, 89, 0),
            new WorldTile(154, 89, 0),
            new WorldTile(160, 83, 0),
    };

    private static final WorldTile[] STARTS = {
            new WorldTile(174, 88, 0),
            new WorldTile(145, 88, 0),
            new WorldTile(159, 74, 0),
    };
    private static final WorldTile[] START_ADJACENTS = { 
            new WorldTile(174, 89, 0),
            new WorldTile(145, 89, 0),
            new WorldTile(160, 74, 0),
    };

    private static final int MAX_WAVE = 30, NEXT_WAVE_CYCLES = 15; // Every fifteen seconds

    private final TheatreOfBlood raid;
    private int nextWave, waveCount = 21;

    private NycolasPillar[] pillars;
    private List<NycolasSpawn> spawnList;

    public NycolasGenerator(TheatreOfBlood raid, NycolasPillar ne, NycolasPillar nw, NycolasPillar se, NycolasPillar sw) {
        this.raid = raid;
        this.pillars = new NycolasPillar[]{ne, nw, se, sw};
        this.spawnList = new ArrayList<>();
    }

    @Override
    public void run() {
    	if (raid == null || pillars == null || spawnList == null)
    		return;
        if (raid.getStage() != Stages.RUNNING) {
            cancel();
            return;
        }

        if (raid.getTargets(pillars[0]).isEmpty()) 
            return;
        
        
        
        boolean allDead = true;
        for (int index = 0; index < pillars.length; index++) {
            NycolasPillar pillar = pillars[index];
            if (pillar.isDead() || pillar.hasFinished()) {
                continue;
            }
            allDead = false;
            break;
        }

        if (allDead) {
            pillars[0].submit(client -> {
                client.applyHit(new Hit(client, client.getHitpoints(), HitLook.REGULAR_DAMAGE));
                client.getPackets().sendGameMessage("<col=16711680>All pillars are destroyed, you have failed to secure them!");
            });

            for (NycolasSpawn spawn : spawnList) 
                spawn.finish();

            cancel();
            return;
        }

        if (waveCount == MAX_WAVE) {
            if (spawnList.isEmpty()) {
                new Nycolas(raid); //boss
                cancel();
                return;
            }
            return;
        }
        raid.setHPBar(MAX_WAVE-waveCount, MAX_WAVE - 21, 2);

        if (nextWave-- == 0) {
            waveCount++;

            nextWave = NEXT_WAVE_CYCLES;

            Integer[] directions = calcDirections();
            for (int index = 0; index < directions.length; index++) {
                int direction = directions[index];

                int rnd = Utils.random(3);
                boolean big = waveCount >= 15 && Utils.random(2) == 1;

                WorldTile start = STARTS[direction];
                start = raid.getTile(start.getX(), start.getY(), start.getPlane());
                WorldTile center = CENTERS[direction];
                center = raid.getTile(center.getX(), center.getY(), center.getPlane());

               /* NycolasSpawn right = (NycolasSpawn) raid.spawnNpc(null, (rnd == 0 ? NycolasSpawn.NYLOCAS_HAGIOS : rnd == 1 ? NycolasSpawn.NYLOCAS_ISCHYROS :
                                NycolasSpawn.NYLOCAS_TOXOBOLOS)[big ? 1 : 0], start.getX(), start.getY(), start.getRealLevel(),
                        -1, big ? 22 : 11, big ? 24 : 17, big ? 250 : 200, 0, false, false);*/
                NycolasSpawn right = new NycolasSpawn(raid, (rnd == 0 ? NycolasSpawn.NYLOCAS_HAGIOS : rnd == 1 ? NycolasSpawn.NYLOCAS_ISCHYROS :
                    NycolasSpawn.NYLOCAS_TOXOBOLOS)[big ? 1 : 0], start, center);
                spawnList.add(right);
                if (!big) {
                    rnd = Utils.random(3);
                    WorldTile startAdjacent = START_ADJACENTS[direction];
                    startAdjacent = raid.getTile(startAdjacent.getX(), startAdjacent.getY(), startAdjacent.getPlane());
                    WorldTile centerAdjacent = CENTER_ADJACENTS[direction];
                    centerAdjacent = raid.getTile(centerAdjacent.getX(), centerAdjacent.getY(), centerAdjacent.getPlane());

                   /* NycolasSpawn left = (NycolasSpawn) raid.spawnNpc(null, (rnd == 0 ? NycolasSpawn.NYLOCAS_HAGIOS : rnd == 1 ? NycolasSpawn.NYLOCAS_ISCHYROS :
                                    NycolasSpawn.NYLOCAS_TOXOBOLOS)[0], startAdjacent.getX(), startAdjacent.getY(),
                            startAdjacent.getRealLevel(), -1, 11, 17, 200, 0, false,
                            false);
                    left.setCenter(centerAdjacent);*/
                    NycolasSpawn left = new NycolasSpawn(raid,(rnd == 0 ? NycolasSpawn.NYLOCAS_HAGIOS : rnd == 1 ? NycolasSpawn.NYLOCAS_ISCHYROS :
                        NycolasSpawn.NYLOCAS_TOXOBOLOS)[0], startAdjacent, centerAdjacent);
                    spawnList.add(left);
                }
            }
        }
    }

    public void addSpawn(NycolasSpawn spawn) {
        spawnList.add(spawn);
    }

    public void removeSpawn(NycolasSpawn spawn) {
        spawnList.remove(spawn);
    }

    public NycolasPillar[] getPillars() {
        return pillars;
    }

    private Integer[] calcDirections() {
        int numTeam = raid.getTeamSize();
        int numDirections =  numTeam <= 1 ? 1 : numTeam <= 3 ? (Utils.random(2) + 1) : numTeam == 4 ? (Utils.random(2) + 2) : 3;

        Set<Integer> directions = new HashSet<>();
        while (directions.size() < numDirections) {
            directions.add(Utils.random(SOUTH + 1));
        }
        return directions.toArray(new Integer[directions.size()]);
    }


}
