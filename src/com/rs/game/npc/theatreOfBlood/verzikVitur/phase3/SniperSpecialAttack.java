package com.rs.game.npc.theatreOfBlood.verzikVitur.phase3;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class SniperSpecialAttack implements TOBAction {

    private Set<Integer> tiles = new HashSet<>();

    @Override
    public int use(NPC npc) {
    	 final VerzikVitur verzik = (VerzikVitur) npc;
        WorldTasksManager.schedule(new WorldTask() {
            private int loop;

            @Override
            public void run() {
                loop++;
                if (loop == 0) {
                    npc.anim(28126);
                } else if (loop == 2) {
                    calculateTiles(verzik);
                } else if (loop == 14) {
                	verzik.submit(client -> {
                      //  Projectile projectile = new Projectile(6596, npc, client, 6, 90, 60, 10, 0, 0);
                	       int msDelay = World.sendProjectile(npc, client, 6596, 40, 36, 36, 41, 45, npc.getSize() * 32);
                	       WorldTasksManager.schedule(new WorldTask() {
                            @Override
                            public void run() {
                            	if (verzik.hasFinished() || !verzik.isRunning())
                            		return;
                                List<Player> targets = verzik.getRaid().getTargets(npc);
                                boolean shares = false;
                                for (Player other : targets) {
                                    if (other.getX() == client.getX() && other.getY() == client.getY()) {
                                        shares = true;
                                        break;
                                    }
                                }

                                if (!shares && tiles.contains(client.getTileHash())) {
                                    client.setNextGraphics(new Graphics(6597, 0, 100));
                                } else {
                                    client.applyHit(new Hit(npc, Utils.random(700), HitLook.REGULAR_DAMAGE));
                                }
                            }
                	       }, CombatScript.getDelay(msDelay) + 1);
                    });
                } else if (loop >= 16) {
                    stop();
                    return;
                }

                if (loop == 2 || loop % 4 == 0) {
                    for (int tile : tiles)
                        World.sendGraphics(npc, new Graphics(6595), new WorldTile(tile));
                }
            }
        }, 0, 0);
        return 16;
    }

    private void calculateTiles(VerzikVitur npc) {
        final List<Player> targets = npc.getRaid().getTargets(npc);
        final WorldTile base = npc.getRaid().getTile(96, 161); 
        int step = 0;
        do {
        	WorldTile position = new WorldTile(base.getX() - 4 + Utils.random(5), base.getY() - 4 + Utils.random(5), 0);
            tiles.add(position.getTileHash());
        } while (tiles.size() != targets.size() && step++  < 200);
    }
}
