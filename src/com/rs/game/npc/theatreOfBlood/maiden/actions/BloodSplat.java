package com.rs.game.npc.theatreOfBlood.maiden.actions;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.maiden.BloodSpawn;
import com.rs.game.npc.theatreOfBlood.maiden.Maiden;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * 
 * @author cjay
 * Converted to onyx by dragonkk(alex)
 */
public class BloodSplat implements TOBAction {

    private static final Animation ANIMATION = new Animation(28091);
    public static final Graphics SPLAT_HIT_GRAPHIC = new Graphics(1579 + 5000, 0, 10);

    private static final int PROJECTILE = 1578 + 5000;

    @Override
    public int use(NPC npc) {
        Maiden maiden = (Maiden) npc;
        maiden.setNextFaceWorldTile(maiden.transform(7, 2, 0)); 
        maiden.setNextAnimation(ANIMATION);

        maiden.submit(client -> {
            boolean third = Utils.random(3) == 1;
            WorldTile[] projectiles = new WorldTile[third ? 3 : 2];
            projectiles[0] = new WorldTile(client);
            projectiles[1] = client.transform(-1 + Utils.random(3), -1 + Utils.random(3), 0);
            if (third)
            	  projectiles[2] = client.transform(-2 + Utils.random(5), -2 + Utils.random(5), 0);
              //  projectiles[2] = new Projectile(PROJECTILE, maiden, client.getPosition().randomize(3).toTile(), 10, 0, 0, 0, 41, 5);
//int msDelay = World.sendProjectile(maiden, client, PROJECTILE, 38, 26, 36, 36, 16, npc.getSize() * 32);
            for (int index = 0; index < projectiles.length; index++) {
                WorldTile toTile = projectiles[index];
               // World.addProjectile(projectile);
            	int msDelay = World.sendProjectile(maiden, toTile, PROJECTILE, 0, 41, 50, 36, 0, npc.getSize() * 32);
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                       	if (maiden.hasFinished() || !maiden.isRunning())
                    		return;
                        World.sendGraphics(maiden, SPLAT_HIT_GRAPHIC, toTile);

                        maiden.addSpot(toTile, 8);
                        if (!Utils.collides(toTile.getX(), toTile.getY(), 1, client.getX(), client.getY(), client.getSize())) 
                            return;

                        int damage = Utils.random(300);
                        client.applyHit(new Hit(maiden, damage, HitLook.REGULAR_DAMAGE));
                        maiden.heal(damage);
                        new BloodSpawn(maiden, toTile);
                      /*  BloodSpawn mob = (BloodSpawn) maiden.raid.spawnNpc(null, BloodSpawn.BLOOD_SPAWN_ID, tile.getX(), tile.getY(), tile.getRealLevel(),
                                1, (int) (120 * 14.3), 0, 0, 0, false, false);*/
                     /*   mob.setMaiden(maiden);
                        mob.setAggressive(false);
                        mob.randomWalk = false;*/
                    }
                }, CombatScript.getDelay(msDelay)+1);
            }
        });
        return 5;
    }
}
