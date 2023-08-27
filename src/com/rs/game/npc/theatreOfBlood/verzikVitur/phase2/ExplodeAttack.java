package com.rs.game.npc.theatreOfBlood.verzikVitur.phase2;

import java.util.List;

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

public class ExplodeAttack implements TOBAction {

    private static final int PROJECTILE = 6583;
    private static final int HIT_PROJECTILE = 6584;

    @Override
    public int use(NPC npc) {
    	VerzikVitur boss = (VerzikVitur) npc;
        npc.anim(28114);
        List<Player> targets = boss.getRaid().getTargets(npc);
        if (targets.isEmpty()) {
            return 0;
        }
        npc.faceEntity(targets.get(0));
        boss.submit(client -> {
            WorldTile tile = new WorldTile(client);
            //Projectile projectile = new Projectile(PROJECTILE, npc, tile,  Misc.random3(4) == 1 ? 3 : 5, 35, 30, 0, 41, 5);
           // World.addProjectile(projectile);
            int msDelay = World.sendProjectile(boss, tile, PROJECTILE, 40, 36, Utils.random(4) == 1 ? 25 : 30, 41, 0, npc.getSize() * 32 + 32);

            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                	if (boss.hasFinished() || !boss.isRunning())
                		return;
                	World.sendGraphics(boss, new Graphics(HIT_PROJECTILE), tile);
                   // World.addGraphic(new GroundGraphic(tile, new Graphic(HIT_PROJECTILE)));
                	 if (client.getX() != tile.getX() || client.getY() != tile.getY()) {
                        return;
                    }

                    client.applyHit(new Hit(npc, Utils.random(450), HitLook.RANGE_DAMAGE));
                    if (!client.isFrozen())
                        client.addFreezeDelay(4200);
                    client.getPackets().sendGameMessage("There is nothing under there for you.");
                }
            }, CombatScript.getDelay(msDelay) + 1);
        });
        return 4;
    }
}
