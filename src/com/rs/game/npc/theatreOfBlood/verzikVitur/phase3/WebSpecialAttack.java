package com.rs.game.npc.theatreOfBlood.verzikVitur.phase3;


import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.npc.theatreOfBlood.verzikVitur.WebSpotNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class WebSpecialAttack implements TOBAction {

    private static final int PROJECTILE = 6601;

    @Override
    public int use(NPC npc) {
        final VerzikVitur verzik = (VerzikVitur) npc;

        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                	verzik.resetWalkSteps();
                	verzik.calcFollow(verzik.getRaid().getTile(96, 147), true);
                   // verzik.walkTo(new FixedTileStrategy(3168, 4307), Entity.RouteType.ADVANCED);
                } else if (loop >= 2 && loop % 2 == 0) {
                    Player client = verzik.getRandomPlayer();
                    if (client != null) {
                    verzik.faceEntity(client);
                    WorldTile[] projectiles = new WorldTile[3];
                    projectiles[0] = new WorldTile(client);//new Projectile(PROJECTILE, verzik, client.getTile(), 6, 15, 43, 0, 41, 5);
                    projectiles[1] = client.transform(-1 + Utils.random(3), -1 + Utils.random(3), 0);//new Projectile(PROJECTILE, verzik, client.getPosition().randomize(2).toTile(), 6, 15, 43, 0, 41, 5);
                    projectiles[2] = client.transform(-2 + Utils.random(5), -2 + Utils.random(5), 0); //new Projectile(PROJECTILE, verzik, client.getPosition().randomize(3).toTile(), 6, 15, 43, 0, 41, 5);

                    for (int index = 0; index < projectiles.length; index++) {
                    	 WorldTile toTile = projectiles[index];
                        //World.addProjectile(projectile);
                    		int msDelay = World.sendProjectile(verzik, toTile, PROJECTILE, 43, 0, 50, 36, 15, npc.getSize() * 32);
                    		 WorldTasksManager.schedule(new WorldTask() {
                            @Override
                            public void run() {
                             	if (verzik.hasFinished() || !verzik.isRunning())
                            		return;
                                boolean contains = false;
                                /*for (WorldObject object : npc.raid.getObjects()) {
                                    if (object.getX() == toTile.getX() && object.getY() == toTile.getY()) {
                                        contains = true;
                                        break;
                                    }
                                }*/
                                if (World.getObjectWithId(toTile, 132734) != null)
                                	contains = true;

                                if (!contains) {
                                	new WebSpotNPC(verzik.getRaid(), toTile);
                                   /* verzik.getRaid().spawnNpc(null, WebSpotNPC.WEB_SPOT_ID, toTile.getX(), toTile.getY(), toTile.getPlane(), -1, 300, 0, 0, 0,

                                            false, false);*/
                                }
                            }
                        }, CombatScript.getDelay(msDelay)+1);
                    }
                    }
                }
                if (loop == 2) {
                    verzik.anim(28127);
                } else if (loop >= 15) {
                    verzik.anim(-1);
                    stop();
                }
                loop++;
            }
        }, 0, 1); //change to 0 if too slow
        return 30;
    }
}
