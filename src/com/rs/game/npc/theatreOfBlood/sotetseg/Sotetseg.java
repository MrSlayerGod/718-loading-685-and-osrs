package com.rs.game.npc.theatreOfBlood.sotetseg;


import java.util.LinkedList;
import java.util.List;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.TOBBoss;
import com.rs.game.npc.theatreOfBlood.sotetseg.actions.MagicAttack;
import com.rs.game.npc.theatreOfBlood.sotetseg.actions.MegaBall;
import com.rs.game.npc.theatreOfBlood.sotetseg.actions.MeleeAttack;
import com.rs.game.npc.theatreOfBlood.sotetseg.actions.RangeAttack;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Sotetseg extends TOBBoss {

    public static final int SOTETSEG_ID = 28388;
    public static final WorldTile TELEPORT_TILE = new WorldTile(75, 84, 0);//3275, 4308, 0);

    private static final TOBAction[] ACTIONS = {new MeleeAttack(), new MagicAttack(), new RangeAttack(), new MegaBall()};
    private static final double[] HEALTH_RATIOS = {0.333, 0.666};

    private static final int HIGHLIGHTED_TILE = 133035, PERMENANT_TILE = 133036;

    private int actionDelay;

    private int phase;
    private boolean hasVortex;

    private WorldObject highlight;
    private WorldObject[] objects;
    private List<WorldTile> steps;
    private int lastBall;

    public Sotetseg(TheatreOfBlood raid) {
        super(raid, 3, SOTETSEG_ID, raid.getTile(77, 101, 0));

        this.steps = new LinkedList<>();
        this.actionDelay = 12;
    }

    @Override
    public void processNPC() {
     //   super.tick();//destroys the npc
        if (raid == null || isDead() || raid.getTargets(this).isEmpty())
            return;
        raid.setHPBar(this);
        if (actionDelay-- == 0) {
            this.actionDelay = action().use(this);
        }
        if ((phase == 1 || phase == 2) && !steps.isEmpty()) {
        	
            List<Player> targets = raid.getTargets(this);
            boolean finish = true;
            for (Player target : targets) {
                if (target.getY() < raid.getTile(0, 101).getY()/*4325*/ || target.isLocked()) { 
                    finish = false;
                }
            }

            if (targets.isEmpty() || !finish) {
                return;
            }

            this.actionDelay = 4;
            this.hasVortex = false;
            this.steps.clear();
            clearObjects();

            this.setNextNPCTransformation(getId() + 1);
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.actionDelay = 4;
        this.hasVortex = false;
        this.steps.clear();
        clearObjects();
    }

    @Override
    public void setHitpoints(int change) {
        int maxHp = getMaxHitpoints(), hp = getHitpoints(), phase = 0;
        if (hp != 0 && !hasFinished()) {
            for (int index = HEALTH_RATIOS.length - 1; index >= 0; index--) {
                if (maxHp * HEALTH_RATIOS[index] >= change) {
                    phase++;
                }
            }

            if (phase > this.phase) {
                this.actionDelay = -1;
                setNextNPCTransformation(getId() - 1);

                this.phase = phase;
                List<Player> targets = raid.getTargets(this);
                if (targets.isEmpty()) {
                    return;
                }

                Player selected = getRandomPlayer();

                for (int index = 0; index < targets.size(); index++) {
                	Player target = targets.get(index);
                    if (target.equals(selected))
                        continue;
                    target.lock(4);
                    target.stopAll();
                    target.setNextWorldTile(raid.getTile(Sotetseg.TELEPORT_TILE.getX(), Sotetseg.TELEPORT_TILE.getY()));
                    target.resetReceivedDamage();
                    target.setTeleporting(true); // don't receive damage while locked
                    WorldTasksManager.schedule(() -> target.setTeleporting(false), 4);
                }

                selected.getPackets().sendGameMessage("You have been selected.");
                selected.lock(4);
                selected.stopAll();
                selected.setNextWorldTile(raid.getTile(32, 85, 3));//3360, 4309, 3));
                selected.resetReceivedDamage();
                selected.setTeleporting(true); // don't receive damage while locked
                WorldTasksManager.schedule(() -> selected.setTeleporting(false), 4);

                this.steps = calculateSteps();
                this.objects = new WorldObject[steps.size()];
                for (int index = 0; index < steps.size(); index++) {
                    World.spawnObject(objects[index] = new WorldObject(PERMENANT_TILE, 22, 0, steps.get(index)));
                }
            }
        }

        super.setHitpoints(change);
    }

    private static final int MAX_Y = 101/*4325*/, MIN_X = 26/*3354*/, MAX_X =39 /*3367*/;

    private List<WorldTile> calculateSteps() {
        final List<WorldTile> steps = new LinkedList<>();
        int maxY = raid.getTile(0, MAX_Y).getY();
        int minX = raid.getTile(MIN_X, 0).getX();
        int maxX = raid.getTile(MAX_X, 0).getX();
        
        WorldTile base = raid.getTile(26 + Utils.random(13), 87, 3); //raid.getTile(3354 + Utils.random(13), 4311, 3);
        steps.add(base);

        WorldTile next = base;
        int bias = -1;
        while (next.getY() != maxY) { 
            int nextY = next.getY(); // can only go north lol
            int nextX = next.getX(); // go east or west

            boolean eastOrWest = Utils.random(2) == 1;
            boolean north = !eastOrWest;

            if (north) {
                nextY++;
                bias = -1;
            }

            int attemps = 0;
            if (eastOrWest) {
                do {
                    int adjust = (bias == -1 ? (Utils.random(2) == 1 ? -1 : 1) : bias == 0 ? -1 : 1);
                    if (bias == -1) {
                        if (adjust == -1)
                            bias = 0;
                        else if (adjust == 1)
                            bias = 1;
                    }
                    nextX = nextX + adjust; // go east or west

                    attemps++;
                    if (attemps == 50) {
                        nextX = next.getX();
                        nextY++;
                        bias = -1;
                        break;
                    }
                } while (nextX < minX || nextX > maxX || contains(new WorldTile(nextX, nextY - 1, next.getPlane()), steps));
            }

            WorldTile candidate = new WorldTile(nextX, nextY, next.getPlane());
            if (!candidate.withinDistance(next, 1))
                continue;

            next = candidate;
            steps.add(next);
        }
        return steps;
    }
    

    public boolean contains(WorldTile tile, List<WorldTile> stepsl) {
    	for (WorldTile steps : stepsl)
    		if (steps.getX() == tile.getX() && steps.getY() == steps.getY())
    			return true;
        return false;// steps.contains(tile);
    }

    public TOBAction action() {
    	lastBall--;
        if (Utils.random(12) == 1 && lastBall <= 0) {
        	lastBall = 4;
            return ACTIONS[3];
        }
        if (Utils.random(7) == 1)
            return ACTIONS[2];
        return Utils.random(2) == 1 ? ACTIONS[0] : ACTIONS[1];
    }

    public void setHighlight(WorldTile tile) {
        if (highlight != null) {
            World.removeObject(highlight);
        }
        World.spawnObject(highlight = new WorldObject(HIGHLIGHTED_TILE, 22, 0, tile));
    }

    public void clearObjects() {
    	if (objects != null) {
            for (WorldObject object : objects) {
                World.removeObject(object);
            }
    	}
    	if (highlight != null)
    		World.removeObject(highlight);
        this.steps.clear();
    }

    public boolean isHiddenDimension() {
        return !steps.isEmpty();
    }

    public boolean isSafe(WorldTile tile) {
    	for (WorldTile steps : steps)
    		if (steps.getX() == tile.getX() && steps.getY() == tile.getY())
    			return true;
        return false;// steps.contains(tile);
    }

    //26, 87 goes to    73, 86 (47, -1, -3)
    
    public WorldTile getStep(int index) {
        if (index >= steps.size()) {
            return null;
        }

        WorldTile tile = steps.get(index);
        return new WorldTile(tile.getX() + 47, tile.getY() - 1, tile.getPlane() - 3); 
    }

    public boolean hasVortex() {
        return hasVortex;
    }

    public void setHasVortex() {
        if (hasVortex) {
            return;
        }
        hasVortex = true;

        WorldTile base = steps.get(0);
       /* raid.spawnNpc(null, RedVortex.RED_VORTEX_ID, base.getX() - 81, base.getY() - 1,
                base.getRealLevel() - 3, -1, 10000, 0, 0, 0, false, false);*/
        new RedVortex(raid, this, new WorldTile(base.getX() + 47, base.getY() - 1,base.getPlane() - 3)); 
    }
}
