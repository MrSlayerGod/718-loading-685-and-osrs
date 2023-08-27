package com.rs.game.npc.theatreOfBlood.verzikVitur.phase2;


import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.verzikVitur.NycolasAthanatos;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikNycolas;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class NycolasAttack implements TOBAction {

    private static final int PROJECTILE_ID = 6586;

    @Override
    public int use(NPC npc) {
        npc.anim(28114);
        VerzikVitur boss = (VerzikVitur) npc;
        Player client = boss.getRandomPlayer();
        if (client == null) {
            return 0;
        }
        boss.faceEntity(client);
        final WorldTile tile = client.getMiddleWorldTile();
        int msDelay = World.sendProjectile(npc, tile, PROJECTILE_ID, 40, 36, 36, 1, 0, npc.getSize() * 32);
        //Projectile projectile = new Projectile(PROJECTILE_ID, npc, tile, 1, 0, 40, 36, 41, 5);
       // World.addProjectile(projectile);

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
            	if (boss.hasFinished() || !boss.isRunning())
            		return;
                for (Player target : boss.getRaid().getTargets(npc)) {
                 //   Position pos = npc.getPosition().randomize(8);
                	WorldTile pos = npc.transform(Utils.random(8), Utils.random(8), 0);
                 /*   VerzikNycolas spawn = (VerzikNycolas) npc.raid.spawnNpc(null, VerzikNycolas.getRandomId(), pos.getX(), pos.getY(), pos.getZ(),
                            -1, 11, 0, 0, 0, false, false);*/
                	VerzikNycolas spawn = new VerzikNycolas(boss.getRaid(), target, pos);
                    //spawn.setTarget(target);

                    if (!Utils.collides(tile.getX(), tile.getY(), 1, target.getX(), target.getY(), target.getSize()))
                        continue;
                    target.applyHit(new Hit(boss, Utils.random(450), HitLook.REGULAR_DAMAGE));
                }
                NycolasAthanatos athanatos = new NycolasAthanatos(boss, tile);/*(NycolasAthanatos) npc.raid.spawnNpc(null, NycolasAthanatos.NYCOLAS_ATHANTOS_ID, tile.getX(), tile
                        .getY(), tile.getRealLevel(), -1, Short.MAX_VALUE, 0, 0, 500, false, false);*/
              //  athanatos.setBoss((VerzikVitur) npc);
            }
        }, CombatScript.getDelay(msDelay) + 1);
        return 4;
    }
}
