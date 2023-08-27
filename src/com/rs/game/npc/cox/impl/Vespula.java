package com.rs.game.npc.cox.impl;

import com.rs.game.*;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.combat.impl.cox.VespulaCombat;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.actions.PlayerCombat;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.player.content.raids.cox.chamber.impl.VespulaChamber;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.GodWars;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colour;
import com.rs.utils.Direction;
import com.rs.utils.NPCDrops;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.rs.game.npc.combat.impl.cox.VespulaCombat.FLYING;
import static com.rs.game.npc.combat.impl.cox.VespulaCombat.GROUNDED;

/**
 * @author Simplex
 * @since Nov 10, 2020
 */
public class Vespula extends COXBoss {
    WorldTile protTile;

    public Vespula(ChambersOfXeric raid, int id, WorldTile tile, Chamber chamber) {
        super(raid, id, tile, chamber);
        protTile = tile.clone();
        setDrops();
    }

    public void setDrops() {
        Drops drops = new Drops(false);
        @SuppressWarnings("unchecked")
        List<Drop>[] dList = new ArrayList[Drops.VERY_RARE + 1];
        for (int i = 0; i < dList.length; i++)
            dList[i] = new ArrayList<Drop>();
        for(Drop drop : ALWAYS_DROPS) {
            dList[Drops.ALWAYS].add(drop);
        }
        drops.addDrops(dList);
        NPCDrops.addDrops(GROUNDED, drops);
        NPCDrops.addDrops(FLYING, drops);
    }

    private static Drop[] ALWAYS_DROPS =
    {
            new Drop(50893, 1, 1), 	// Transdimensional notes
            new Drop(50984, 2, 2),	// Xeric's aid (+)(4)
            new Drop(50960, 1, 1),	// Revitalisation (+)(4)
            new Drop(50972, 1, 1)	// Prayer enhance (+)(4)
    };

    private int protectionTicks = 0;

    public void attackedPortal(Player player) {
        protectionTicks = 15;
        if(getCombat().getTarget() != null) {
            getCombat().reset();
            //forceTalk("Protect the portal!");
        }
        setNextFaceEntity(null);
    }

    @Override
    public void sendDeath(final Entity source) {
        final NPCCombatDefinitions defs = getCombatDefinitions();
        resetWalkSteps();
        getCombat().removeTarget();
        setNextAnimation(null);
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(getId() == GROUNDED ? 27458 : 27459));
                } else if (loop >= defs.getDeathDelay()) {
                    drop();
                    finish();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    private boolean healing = false;

    @Override
    public void handleIngoingHit(final Hit hit) {
        if(healing && getHitpoints() + hit.getDamage() < getMaxHitpoints()) {
            setHitpoints(getHitpoints() + hit.getDamage());
        }
        if (getHitpoints() > getMaxHitpoints() / 5 && (getHitpoints() - hit.getDamage() <= getMaxHitpoints() / 5)) {
            // land
            setNextNPCTransformation(GROUNDED);
            anim(27457);
            getRaid().getTeam().forEach(p->p.sendMessage(Colour.DARK_RED.wrap("The portal is now vulnerable!")));
            WorldTasksManager.schedule(() -> {
                if (!isDead() && !hasFinished()) {
                    setNextNPCTransformation(FLYING);
                    anim(27452);
                    setHitpoints(getMaxHitpoints());
                    healing = false;
                }
            }, 50);

            healing = true;
        }
    }

    private ArrayList<Player> team;
    WorldTile p1 = null;
    WorldTile p2 = null;

    private int tick = 0;
    @Override
    public void processNPC() {
        if(!getRaid().hasStarted())
            return;

        if(!getChamber().isActivated() && tick % 10 == 0) {
            if(p1 == null) {
                 p1 = getRespawnTile().clone().relative(-6, 0);
                 p2 = getRespawnTile().clone().relative(6, 0);
            }
            if(p1.distance(this) > p2.distance(this))
                this.addWalkSteps(p1.getX(), p1.getY());
            else
                this.addWalkSteps(p2.getX(), p2.getY());
            return;
        }

        VespulaChamber chamber = (VespulaChamber) getChamber();

        if(tick++%2 == 0) {
            getTeam().forEach(player -> {
                if (Utils.collides(player, this)) {
                    player.applyHit(new Hit(this, Utils.random(90), Hit.HitLook.REGULAR_DAMAGE));
                }
            });
        }

        if(getId() != GROUNDED && protectionTicks-- > 0) {
            if(protectionTicks % 2 == 0)
                anim(27454);
            if(!matches(protTile)) {
                resetWalkSteps();
                addWalkSteps(protTile.getX(), protTile.getY(), -1);
            } else {
                if(protectionTicks % 2 == 0)
                    setDirection(Utils.random(Direction.values()), true);
            }

            for(Player player : getTeam()) {
                if(targetted.contains(player.getUsername())) {
                    targetted.remove(player.getUsername());
                    continue;
                }
                if(Utils.collides(this, player) || chamber.getProtectedTiles().stream().anyMatch(t->player.matches(t))) {
                    targetted.add(player.getUsername());
                    WorldTile targetTile = player.clone();
                    World.sendGraphics(2330, targetTile);
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            if(player.matches(targetTile))
                                player.applyHit(null, Utils.random(220), Hit.HitLook.MELEE_DAMAGE);
                        }
                    });
                }
            }
        } else {
            super.processNPC();
        }
    }

    public static ArrayList<String> targetted = new ArrayList<>();

    @Override
    public boolean preAttackCheck(Player attacker) {
        if(PlayerCombat.isUsingMelee(attacker)
                && getId() == VespulaCombat.FLYING) {
            attacker.sendMessage("Vespula is flying too high for you to hit with melee!");
            return false;
        }

        return true;
    }

    public int getProtectionTicks() {
        return protectionTicks;
    }
}
