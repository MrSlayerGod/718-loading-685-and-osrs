package com.rs.game.npc.theatreOfBlood.maiden.actions;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.maiden.Maiden;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * 
 * @author cjay
 * Converted to onyx by dragonkk(alex)
 */
public class TornadoMagic implements TOBAction {

    private static final Animation ANIMATION = new Animation(28092);
    private static final int PROJECTILE = 1577 + 5000;

    @Override
    public int use(NPC npc) {
        Maiden maiden = (Maiden) npc;
        Player client = maiden.getClosestPlayer();
        if (client == null) {
            return 0;
        }
        maiden.faceEntity(client);
        maiden.setNextAnimation(ANIMATION);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
            	if (maiden.hasFinished() || !maiden.isRunning())
            		return;
                //Projectile projectile = new Projectile(PROJECTILE, maiden, client, 6, 15, 80, 36, 41, 5);
            	int msDelay = World.sendProjectile(maiden, client, PROJECTILE, 0, 0, 50, 25, 0, npc.getSize() * 32);
/*
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                    	if (maiden.hasFinished() || !maiden.isRunning())
                    		return;
                        client.applyHit(new Hit(maiden, maiden.getMinimumTornadoDamage() + Utils.random(36), HitLook.MAGIC_DAMAGE));
                    }
                }, CombatScript.getDelay(msDelay));*/
                CombatScript.delayHitMS(maiden, msDelay, client, CombatScript.getMagicHit(maiden, maiden.getMinimumTornadoDamage() + Utils.random(maiden.getId() == 28364 ? 530 : 360)));
            }
        }, 2);
        return 5;
    }
}
