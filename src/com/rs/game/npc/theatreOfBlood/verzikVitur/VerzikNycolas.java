package com.rs.game.npc.theatreOfBlood.verzikVitur;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class VerzikNycolas extends NPC {

    private static final int[] SPAWNS = {28381, 28382, 28383};

    
    private TheatreOfBlood raid;
    private Player target;
    private int delay;


    
    public VerzikNycolas(TheatreOfBlood raid, Player target, WorldTile tile) {
        super(getRandomId(), tile, -1, true, true);
        this.raid = raid;
        this.target = target;
        this.delay = 15;
        setForceMultiArea(true);
        setIntelligentRouteFinder(true);
    }

    @Override
    public void processNPC() {
       // super.tick();
        if (target == null || raid == null || raid.getStage() != Stages.RUNNING
        		|| isCantInteract() || isDead()) {
            return;
        }

        if (target.isDead() || delay-- <= 0) {
            explode();
            return;
        }

      //  walkTo(new EntityStrategy(target), RouteType.ADVANCED);
        this.resetWalkSteps();
        calcFollow(target, true);
        faceEntity(target);
        if (!Utils.isOnRange(target, this, 0)) {
            return;
        }

        resetWalkSteps();
        explode();
    }

    private void explode() {
    	this.setCantInteract(true);
        setNextAnimation(new Animation(getExplodeEmote(getId())));
        for (Player client : raid.getTargets(this)) {
            if (!Utils.isOnRange(client, this, 1)) {
                continue;
            }

            client.applyHit(new Hit(this, Utils.random(600) + 100, HitLook.REGULAR_DAMAGE));
        }

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
            	if (hasFinished() || isDead())
            		return;
                finish();
            }
        }, 3);
    }

    private int getExplodeEmote(int id) {
        switch (id) {
            case 28381:
                return 28006;
            case 28382:
                return 28000;
            case 28383:
                return 27992;
        }
        return -1;
    }

    public static boolean isVerzikNycolas(int id) {
        for (int npc : SPAWNS) {
            if (id == npc)
                return true;
        }
        return false;
    }

    public static int getRandomId() {
        return SPAWNS[Utils.random(SPAWNS.length)];
    }
    
	@Override
	public void setTarget(Entity target) {

	}
	
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
}
