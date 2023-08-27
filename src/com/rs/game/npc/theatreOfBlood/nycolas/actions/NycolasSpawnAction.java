package com.rs.game.npc.theatreOfBlood.nycolas.actions;


import java.util.ArrayList;

import com.rs.game.Animation;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.Default;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.nycolas.NycolasPillar;
import com.rs.game.npc.theatreOfBlood.nycolas.NycolasSpawn;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class NycolasSpawnAction implements TOBAction {

    private NycolasPillar pillar;

    @Override
    public int use(NPC npc) {
        NycolasSpawn spawn = (NycolasSpawn) npc;
        if (pillar == null) {
            pillar = findClosestPillar(spawn);
            return 0;
        }

        if (pillar.isDead() || pillar.hasFinished()) {
            Player client = spawn.getClosestPlayer();
            if (client == null) {
                return -1;
            }
            spawn.setAttackedBy(client);
       //     spawn.underAttack = true;
        //    spawn.killerId = client.getId();
            return -1; // Cancel action here.
        }

        if (!Utils.isOnRange(pillar.getX(), pillar.getY(), pillar.getSize(), spawn.getX(), spawn.getY(), spawn.getSize(), 1)) {
            //spawn.walkTo(new EntityStrategy(pillar), Entity.RouteType.ADVANCED);
        	spawn.resetWalkSteps();
        	spawn.calcFollow(pillar, -1, true, true);
         //   System.out.println(pillar.getHitpoints()+", walk");
            return 0;
        }

        spawn.setNextFaceWorldTile(pillar);
        spawn.setNextAnimation(new Animation(getAttackAnimation(spawn.getId())));
        Default.delayHit(npc, 0, pillar, Default.getMeleeHit(npc, Utils.random(spawn.getMaxHit()+1)));
       // pillar.hit(new Hit(spawn, Misc.random3(spawn.maxHit()), HitType.MELEE));
        return 4;
    }

    private static int getAttackAnimation(int id) {
        switch (id) {
            case 28342:
            case 28345:
                return 28004;
            case 28343:
            case 28346:
                return 28001;
            case 28344:
            case 28347:
                return 27990;
        }
        return -1;
    }

    private static NycolasPillar findClosestPillar(NycolasSpawn spawn) {
       /* Optional<NycolasPillar> client = Stream.of(spawn.getRaid().getNycolasGenerator().getPillars()).filter(p -> !p.isDead() && !p.hasFinished())
                .min(Comparator.comparingDouble(p -> spawn.getDistance(spawn.getX() + spawn.getSize()/2d, spawn.getY() + spawn.getSize()/2d, p.getX() + 1.5d, p.getY() + 1.5d)));
        return client.orElse(null);*/
    	ArrayList<NycolasPillar> alive = new ArrayList<NycolasPillar>();
    	for (NycolasPillar pillar : spawn.getRaid().getNycolasGenerator().getPillars())
    		if (!pillar.isDead() && !pillar.hasFinished())
    			alive.add(pillar);
    	return alive.isEmpty() ? null : alive.get(Utils.random(alive.size()));
    }

 }
