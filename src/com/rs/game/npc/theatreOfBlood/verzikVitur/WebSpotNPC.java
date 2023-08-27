package com.rs.game.npc.theatreOfBlood.verzikVitur;


import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class WebSpotNPC extends NPC {

    public static final int WEB_SPOT_ID = 28376;

    private TheatreOfBlood raid;
    private WorldObject web;
    private int remaining;

    private List<Player> trapped;

    public WebSpotNPC(TheatreOfBlood raid, WorldTile tile) {
        super(WEB_SPOT_ID, tile, -1, true, true);
        this.raid = raid;
        this.remaining = 15;
        setForceMultiArea(true);
        setIntelligentRouteFinder(true);
    }

    @Override
    public void processNPC() {
     //  super.tick();//destroys the npc
        if (isDead() || raid.getStage() != Stages.RUNNING || raid.getTargets(this).isEmpty())
            return;

        if (web == null) {
        	World.spawnObject(web = new WorldObject(132734, 10, 0, getMiddleWorldTile()));
          //  raid.addObject(web = new WorldObject(132734, getCenterTile(), 10, 0));
            this.trapped = new LinkedList<>();
            submit(client -> {
                if (!Utils.collides(this, client))
                    return;
                trapped.add(client);
            });
        }

        if (remaining == 0) {
            explode();
            return;
        }
        remaining--;

        for (Player client : trapped) {
            client.stopAll();
            //client.setStunDelay(2, false);
            client.addFreezeDelay(1200);
        }
    }

    private void explode() {
        submit(client -> {
            if (!Utils.collides(this, client)) {
                return;
            }

            int maxHP = getMaxHitpoints();
            int hp = getHitpoints();
            client.applyHit(new Hit(this, (hp / maxHP) * Utils.random(700), HitLook.REGULAR_DAMAGE));
        });

        trapped.clear();
        World.removeObject(web);
        finish();
    }

    @Override
    public void sendDeath(Entity killer) {
    	 World.removeObject(web);
    	 super.sendDeath(killer);
    }

    public List<Player> getTrapped() {
        return trapped;
    }

    public boolean isTrapped(Player client) {
        return trapped.contains(client);
    }
    
    public void submit(Consumer<Player> consumer) {
   	 raid.getTargets(this).forEach(consumer);
   }

}
