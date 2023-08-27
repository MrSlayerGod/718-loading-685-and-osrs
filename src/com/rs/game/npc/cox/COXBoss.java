package com.rs.game.npc.cox;

import com.rs.Settings;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.cox.impl.*;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.player.content.raids.cox.chamber.impl.ScavengerChamber;
import com.rs.utils.Utils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * @since Jul 19, 2020
 */
public class COXBoss extends NPC {

    public static double COX_BOSS_BUFF = 1.75;

    private boolean init;
    protected ChambersOfXeric raid;
    private int wave;

    private Chamber chamber;

    public WorldTile getChamberBaseTile() {
        return chamber.getBaseTile();
    }

    public Chamber getChamber() {
        return chamber;
    }


    public void debug(String s) {
        if (Settings.DEBUG) {
            // forceTalk(s);
            // getTeam().forEach(plr -> plr.asPlayer().sendMessage("<col=ff981f><shad=0>[DEBUG]: <col=ffffff><shad=0>" + s));
        }
    }

    public List<Player> getTeam() {
        WorldTile tile = raid.getMapTileBaseWorldTile(getChamberBaseTile().getX(), getChamberBaseTile().getY(), getChamberBaseTile().getPlane());

        return raid.getTeam().stream().filter(player ->
                player.getPlane() == tile.getPlane()
                        && player.getX() > tile.getX()
                        && player.getX() < tile.getX() + 32
                        && player.getY() > tile.getY()
                        && player.getY() < tile.getY() + 32)
                .collect(Collectors.toList());
    }

    @Override
    public boolean preAttackCheck(Player attacker) {
        ChambersOfXeric raid = ChambersOfXeric.getRaid(attacker);
        if(attacker.getRights() != 2 && raid != null) {
            if(!ScavengerChamber.isScav(this.getId()) && raid.getCurrentChamber(attacker) != getChamber()) {
                // player is trying to attack from outside chamber
                attacker.sendMessage("I can't reach that!");
                return false;
            }
        }
        return super.preAttackCheck(attacker);
    }

    public COXBoss(ChambersOfXeric raid, int id, WorldTile tile, Chamber chamber) {
        super(id, tile, -1, true, true);
        this.raid = raid;
        this.chamber = chamber;
        setup();
        raid.addNPC(this);
        //chamberBaseTile = raid.getMapTileBaseWorldTile(roomCoords.getX(), roomCoords.getY(), roomCoords.getPlane());
        if(raid != null && raid.getTeamSize() > 0) {
            // this is for npcs like olm claws, vespine soliders, etc that are spawned
            // after raid start
            scale(raid.getTeamSize());
        }
    }

    public void setup() {
        setRandomWalk(0);
        //setCapDamage(500);
        setCombatLevel(1000);
        if(this instanceof GreatOlm || this.getId() == 27604 || this.getId() == 27605 || this.getId() == 27606)
            setCombatLevel(2000);
        setForceMultiArea(true);
        setForceMultiAttacked(true);
        if(!(this instanceof VasaNistirio)) {
            setForceTargetDistance(8);
            setIntelligentRouteFinder(true); //true, doesnt stack
            setForceAgressive(true);
        }
    }

    public WorldTile getChamberTile(int x, int y) {
        return getChamberBaseTile().transform(x, y, 0);
    }

    @Override
    public void processNPC() {
        super.processNPC();
        raid.setHPBar(this);
        //forceTalk("cb l " + getCombatLevel() + " | def " + getDefinitions().combatLevel);
    }

    public void swapTarget() {
        getCombat().removeTarget();
        if(getTeam().size() < 1)
            return;
        else {
            Player target = Utils.get(getTeam());
            getCombat().setTarget(target);
        }
    }

    public void sendDeath(Entity killer) {
        super.sendDeath(killer);
    }

    public Player getClosestPlayer() {
        Optional<Player> client = raid.getTargets(this).stream().min(Comparator.comparingDouble(c -> getDistance(this, c)));
        return client.orElse(null);
    }

    public void submit(Consumer<Player> consumer) {
        raid.getTargets(this).forEach(consumer);
    }

    public double getDistance(WorldTile a, WorldTile b) {
        return Math.hypot(a.getX() - b.getX(), a.getY() - b.getY());
    }

    public boolean isRunning() {
        return raid != null && raid.getStage() == MapInstance.Stages.RUNNING;
    }

    public Player getRandomPlayer() {
        List<Player> targets = raid.getTargets(this);
        if (targets.isEmpty())
            return null;
        return targets.get(Utils.random(targets.size()));
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.3;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.3;
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.3;
    }

    public ChambersOfXeric getRaid() {
        return raid;
    }

    @Override
    public int getMaxHitpoints() {
        int hp = super.getMaxHitpoints();
        if(init && this instanceof VespulaPortal && getRaid().getTeamSize() > 2) {
            return getRaid().getTeamSize() * 1000;
        }
        if (init && getRaid() != null && getRaid().getTeamSize() > 1 && !ScavengerChamber.isScav(this.getId()) && raid != null) {
            //int hp = (int) ((raid.getTeamSize() * super.getMaxHitpoints() / 5) * 0.7);
            //int base = (int) (super.getMaxHitpoints() * 0.3);
            //double multiplier = Math.max(1.0, raid.getTeamSize() * 0.2);
            double multiplier = getRaid().getTeamSize() == 1 ? 1.0 : getRaid().getTeamSize() * .75;
            hp = (int) ((double)hp * multiplier);
        }
        if(init && !raid.isOsrsRaid()) {
            hp *= 2;
        }
        return hp;
    }

    private boolean rewardNoPoints = false;

    public boolean isRewardNoPoints() {
        return rewardNoPoints;
    }

    public void setRewardNoPoints(boolean rewardNoPoints) {
        this.rewardNoPoints = rewardNoPoints;
    }

    public boolean isInit() {
        return init;
    }

    public void scale(int size) {
        setBonuses();
        init = true;
        double buff = COX_BOSS_BUFF * (raid.isOsrsRaid() ? 1 : 1.1);
        if(this instanceof VasaNistirio || this instanceof VasaCrystal || this instanceof Vanguard) {
            buff = COX_BOSS_BUFF;
        }
        for(int i = 0; i < getBonuses().length; i++) {
            setBonus(i, (getBonuses()[i] * buff) * (1 + (size * .05)));
        }
        setHitpoints(getMaxHitpoints());
    }
}
