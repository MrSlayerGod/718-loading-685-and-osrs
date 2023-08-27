package com.rs.game.npc.theatreOfBlood.sotetseg.actions;


import java.util.List;
import java.util.stream.Collectors;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.sotetseg.Sotetseg;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class MegaBall implements TOBAction {

    private static final int PROJECTILE = 6604;
    private static final int HIT_GFX = 6605;

    @Override
    public int use(NPC npc) {
    	Sotetseg boss = (Sotetseg) npc;
        Player client = boss.getRandomPlayer();
        if (client == null) {
            return 0;
        }

        npc.setNextFaceEntity(client);
        npc.setNextAnimation(new Animation(28139));

        int msDelay = World.sendProjectile(npc, client, PROJECTILE, 45, 41, 12, 41, 16, npc.getSize() * 32);
       // Projectile projectile = new Projectile(PROJECTILE, npc, client, 2, 45, 40, 36, 41, 5);
        //World.addProjectile(projectile);

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
            	if (boss.hasFinished() || !boss.isRunning() || boss.isHiddenDimension())
            		return;
                List<Player> targets = boss.getRaid().getTargets(npc).stream().filter(target ->
                        target.withinDistance(client, 3)).collect(Collectors.toList());
                int damage = Utils.random(140 * boss.getRaid().getTeamSize()) + 450;
                int size = targets.size();
                for (int index = 0; index < size; index++) {
                    Player target = targets.get(index);
                    target.applyHit(new Hit(npc, damage / size, HitLook.REGULAR_DAMAGE));
                    target.setNextGraphics(new Graphics(HIT_GFX));
                }
            }
        }, CombatScript.getDelay(msDelay) + 1);
        return 4;
    }
}
