package com.rs.game.npc.others.zalcano;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.RouteEvent;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.mining.MiningBase;
import com.rs.game.player.content.NPCKillLog;
import com.rs.game.player.content.collectionlog.CategoryType;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.rs.game.player.actions.mining.MiningBase.getPickAxeDefinitions;

/**
 * @author Simplex
 * @since May 22, 2020
 */
public class Zalcano extends NPC {

	
	//TODO
	private static final Drop[] DROPS = {
			//runes
			new Drop(565, 103, 480),
			new Drop(564, 304, 926),
			new Drop(560, 201, 830),
			new Drop(563, 218, 770),
			new Drop(566, 91, 388),
			new Drop(561, 1, 700),
			//resources
			new Drop(443, 255, 800),
			new Drop(445, 128, 721),
			new Drop(454, 380, 815),
			new Drop(448, 83, 387),
			new Drop(450, 71, 276),
			new Drop(452, 5, 26),
			new Drop(2354, 80, 463),
			new Drop(2360, 75, 459),
			new Drop(2362, 21, 103),
			new Drop(2364, 7, 23),
			new Drop(1618, 3, 19),
			new Drop(1632, 1, 11),
			new Drop(9194, 5, 38),
			new Drop(7937, 1274, 4422)
	};
	
	public static Zalcano ZALCANO_INSTANCE;
    public static boolean disable = false;
    private static boolean challengeMode = false;
    public static int challengeModeState = 0; // 0 = on >=10 players | 1 = force on | 2 = force off

    protected static final WorldTile OUTSIDE = new WorldTile(3034, 6063, 0);
    private static WorldTile NW_ROCK, NE_ROCK, SW_ROCK, SE_ROCK;

    // objects
    public static final int DOOR_ID = 136201, FURNACE_ID = 136195, ALTAR_ID = 136196,
            DEPLETED_ROCK_ID = 136194, GLOW_ROCK_ID = 136193,
            RED_DEMONIC_SYMBOL_ID = 136199, BLUE_DEMONIC_SYMBOL_ID = 136200;

    // npcs
    public static final int ZALCANO_ID = 29049, ZALCANO_DOWNED_ID = 29050, GOLEM_ID = 29051;

    // anims
    private static final int ZALCANO_MELEE_ANIM = 28431,
            ZALCANO_ROCK_SWITCH_ANIM = 28432, ZALCANO_DEMONIC_SYMBOL_ANIM = 28433,
            ZALCANO_STOMP_ANIM = 28435, ZALCANO_SPAWN_GOLEM_ANIM = 28432,
            ZALCANO_BLOCK_ANIM = 28436, ZALCANO_DOWN_ANIM = 28437, ZALCANO_STAND_UP_ANIM = 28439,
            ZALCANO_DEATH_ANIM = 28440, ROCK_THROW_ANIM = 27618;

    // gfx
    private static final int RED_SYMBOL_GFX = 6725, BLUE_SYMBOL_GFX = 6726,
            FALLING_ROCK_GFX = 6727, FIRE_PROJECTILE_GFX = 6728, EXPLODE_GLOW_ROCK_GFX = 6729,
            GOLEM_EXPLODE_GFX = 6730, TEPHRA_THROW_GFX = 6731,
            TEPHRA_IMPACT_GFX = 6732, SMALL_FALLING_ROCKS_GFX = 5060;

    // items
    public static final int TEPHRA = 53905, REFINED_TEPHRA = 53906, IMBUED_TEPHRA = 53907,
            CRYSTAL_SHARD = 53962, CRYSTAL_ARMOUR_SEED = 53956, CRYSTAL_TOOL_SEED = 53953,
            CRYSTAL_WEAPON_SEED = 34207;

    private static NPC golem = null;

    // downed hp bar
    private static int hp = 10000;

    public static boolean isDowned = false;

    private static CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<Player>();

    public static void attack(Player player, NPC npc) {
        if (npc.getId() == ZALCANO_DOWNED_ID) {
            player.setRouteEvent(new RouteEvent(npc, new Runnable() {
                @Override
                public void run() {
                    mineZalcano(npc, player);
                    return;
                }
            }, true));
        } else if (npc.getId() == ZALCANO_ID || npc.getId() == GOLEM_ID) {
            if (hasImbuedTephra(player))
                throwTephra(npc, player);
            else if (npc.getId() == ZALCANO_ID)
                player.sendMessage("You need to bring Zalcano down before you can do any damage to her!");
            else if (npc.getId() == GOLEM_ID)
                player.sendMessage("The golem can only be damaged by imbued tephra.");
        }
    }

    private static boolean hasImbuedTephra(Player player) {
        return player.getInventory().containsItemToolBelt(IMBUED_TEPHRA) || player.getEquipment().getItems().containsOne(new Item(IMBUED_TEPHRA));
    }

    private static void throwTephra(NPC npc, Player player) {
      	player.setLastTarget(npc);
        player.getActionManager().setAction(new Action() {

            @Override
            public boolean start(Player player) {
                player.faceEntity(npc);
                return true;
            }

            @Override
            public boolean process(Player player) {
                return true;
            }

            @Override
            public int processWithDelay(Player player) {
                if (npc.isDead() || Zalcano.isDowned || !hasImbuedTephra(player) || npc.isCantInteract()
                        || (npc.getId() == GOLEM_ID && golem == null)) {
                    player.anim(-1);
                    return -1;
                }

                player.setNextFaceEntity(npc);

                if (!Utils.isOnRange(player.getX(), player.getY(), player.getSize(), npc.getX(), npc.getY(), npc.getSize(), 6)) {
                    player.calcFollow(npc, 2, true, false);
                    if(!Utils.isOnRange(player.getX(), player.getY(), player.getSize(), npc.getX(), npc.getY(), npc.getSize(), player.isRunning() ? 8 : 7))
                        return 0; // if they won't be in range next tick don't start throw
                } else {
                    player.resetWalkSteps();
                }

                player.getEquipment().removeAmmo(IMBUED_TEPHRA, -1);
                player.getInventory().deleteItem(IMBUED_TEPHRA, 1);
                World.sendProjectile(player, npc, TEPHRA_THROW_GFX, 32, 50, 40, 20, 8, 0);
                player.anim(ROCK_THROW_ANIM);

                // add world event so if the skill action cancels the projectile will still impact
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        npc.setNextGraphics(new Graphics(TEPHRA_IMPACT_GFX));
                        if (!npc.isDead() && !isDowned) {
                            int damage = 150; // scale with rc + smithing
                            Boolean buff = (Boolean) player.getTemporaryAttributtes().put(Key.ZALCANO_DAMAGE_BUFF, Boolean.TRUE);
                            if (buff == Boolean.TRUE) damage += 150;

                            int smithRc = player.getSkills().getLevel(Skills.SMITHING)
                                    + player.getSkills().getLevel(Skills.RUNECRAFTING);
                            int roll = (int) Math.min((double) (198 - smithRc) * 0.10, 5); // cap at 1/5
                            // ex. 70 sm 70 rc = 29+29 *0.10 = roll of 5, 1/6 chance to fail
                            // ex. 95 sm 87 rc = 4+16 *0.10 = roll of 2, 1/9 chance to fail
                            if (Utils.random(10 - roll) == 1) {
                                // 99 both skills = never miss, otherwise miss
                                if (smithRc < 198) {
                                    damage = 0;
                                }
                            }

                            npc.addReceivedDamage(player, damage);
                            if (damage == 0)
                                npc.applyHit(new Hit(player, 0, Hit.HitLook.REGULAR_DAMAGE));
                            else npc.applyHit(new Hit(player, Utils.random(damage), Hit.HitLook.REGULAR_DAMAGE));
                            
                            if(npc.getId() == ZALCANO_ID) {
                                Integer currentDamage = (Integer) player.getTemporaryAttributtes().getOrDefault(Key.ZALCANO_TEPHRA_DAMAGE, 0);
                                player.getTemporaryAttributtes().put(Key.ZALCANO_TEPHRA_DAMAGE, currentDamage + damage);
                            }
                        }

                        stop();
                    }
                }, 1, 1);

                return 3;
            }

            @Override
            public void stop(Player player) {
                player.setNextFaceEntity(null);
            }

        });
    }

    private static void mineZalcano(NPC npc, Player player) {
    	player.setLastTarget(npc);
        MiningBase.PickAxeDefinitions axeDefinitions = getPickAxeDefinitions(player, false);

        player.getActionManager().setAction(new Action() {
            int loop = 0;

            @Override
            public boolean start(Player player) {
                player.setNextAnimation(new Animation(axeDefinitions.getAnimationId()));
                return true;
            }

            @Override
            public boolean process(Player player) {
                if (npc.isDead()) {
                    player.anim(-1);
                    return false;
                }

                if(npc.isCantInteract()) {
                    // wait til zalc finishes the anim
                    return true;
                } else if(!isDowned) {
                    player.anim(-1);
                    return false;
                }

                player.faceEntity(npc);
                player.setNextAnimation(new Animation(axeDefinitions.getAnimationId()));
                if (loop % 3 == 0) {
                    Integer currentDamage = (Integer) player.getTemporaryAttributtes().getOrDefault(Key.ZALCANO_MINING_DAMAGE, 0);
                    int damage = Utils.random(0, 300);
                    npc.applyHit(new Hit(player, damage, Hit.HitLook.REGULAR_DAMAGE));
                    player.getTemporaryAttributtes().put(Key.ZALCANO_MINING_DAMAGE, currentDamage + damage);
                }
                loop++;
                return true;
            }

            @Override
            public int processWithDelay(Player player) {
                return 0;
            }

            @Override
            public void stop(Player player) {
            }

        });
    }

    public static void handleRemove(Player player) {
        players.remove(player);
        player.getTemporaryAttributtes().remove(Key.ZALCANO_TEPHRA_DAMAGE);
        player.getTemporaryAttributtes().remove(Key.ZALCANO_MINING_DAMAGE);
        player.getTemporaryAttributtes().remove(Key.ZALCANO_DAMAGE_BUFF);
        player.setLargeSceneView(false);
		player.getInterfaceManager().removeOverlay(true);
        resetZalcItems(player);
    }

    public static void handleJoin(Player player) {
        if (!players.contains(player))
            players.add(player);
        player.setLargeSceneView(true);
        if (player.getX() == 3033) {
            player.useStairs(-1, new WorldTile(3033, 6061, 0), 1, 1, null, true);
        } else {
            player.useStairs(-1, new WorldTile(3034, 6061, 0), 1, 1, null, true);
        }
        player.getControlerManager().startControler("ZalcanoController");
    }

    public static boolean hasJoined(Player player) {
        return players.contains(player);
    }

    public static void init() {
        NW_ROCK = new WorldTile(3025, 6057, 0);
        NE_ROCK = new WorldTile(3040, 6057, 0);
        SW_ROCK = new WorldTile(3025, 6040, 0);
        SE_ROCK = new WorldTile(3040, 6040, 0);

        ROCKS = new WorldTile[]{NW_ROCK, NE_ROCK, SW_ROCK, SE_ROCK};

        Golem.init();
    }

    public Zalcano(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setIntelligentRouteFinder(true);
        restart();
    }

    public static boolean isZalcanoNPC(NPC npc) {
        return npc.getId() == ZALCANO_ID || npc.getId() == ZALCANO_DOWNED_ID;
    }

    private void restart() {
        players.stream().filter(Objects::nonNull).forEach(Zalcano::resetZalcItems);
        Golem.removeDroppedTephra();

        // so players cant stash items on ground for next round
        /*World.removeAllGroundItemOf(IMBUED_TEPHRA, 12126);
        World.removeAllGroundItemOf(REFINED_TEPHRA, 12126);
        World.removeAllGroundItemOf(TEPHRA, 12126);*/

        // random glow rock on spawn
        int rnd = Utils.random(0, ROCKS.length);
        previousRock = rnd;
        WorldObject rock = World.getStandartObject(ROCKS[rnd]);
        WorldObject glowRock = new WorldObject(GLOW_ROCK_ID, 10, rock.getRotation(), ROCKS[rnd]);
        World.spawnObject(glowRock);
    }

    private static void resetZalcItems(Player player) {
        player.getInventory().deleteItem(TEPHRA, Integer.MAX_VALUE);
        player.getInventory().deleteItem(REFINED_TEPHRA, Integer.MAX_VALUE);
        player.getInventory().deleteItem(IMBUED_TEPHRA, Integer.MAX_VALUE);
        if (player.getEquipment().getWeaponId() == IMBUED_TEPHRA)
            player.getEquipment().removeAmmo(IMBUED_TEPHRA, -1, true);
    }

    @Override
    public void drop() {
        final double REWARD_PTS_CAP = 5000.0, MIN_TO_BE_REWARDED = 500.0;

        Player mvp = null;
        int mvpPts = -1;

        // decide mvp
        for (Player player : players) {
            if(player == null) continue;

            int rewardsPts = (int) player.getTemporaryAttributtes().getOrDefault(Key.ZALCANO_MINING_DAMAGE, 0);
            rewardsPts += (int) player.getTemporaryAttributtes().getOrDefault(Key.ZALCANO_TEPHRA_DAMAGE, 0) * 2;

            if (mvpPts < rewardsPts) {
                mvp = player;
                mvpPts = rewardsPts;
            }
        }

        for (Player player : players) {
            if (player == null)
                continue;

            int rewardsPts = (int) player.getTemporaryAttributtes().getOrDefault(Key.ZALCANO_MINING_DAMAGE, 0);
            rewardsPts += (int) player.getTemporaryAttributtes().getOrDefault(Key.ZALCANO_TEPHRA_DAMAGE, 0) * 2;
            rewardsPts = Math.min((int)REWARD_PTS_CAP, rewardsPts);

            if(rewardsPts < MIN_TO_BE_REWARDED) {
                player.sendMessage("You did not do enough damage to receive a drop from Zalcano.");
                continue; // must do at least 5% damage
            }

            double dropModifier = (double) rewardsPts / REWARD_PTS_CAP; // ex 2500 / 5000 = 0.50

            ArrayList<Item> drops = new ArrayList<Item>();

            // always drop shards
            drops.add(new Item(CRYSTAL_SHARD, (int) (3.0 * dropModifier)));

            if(player == mvp) {
                // drop ashes to only mvp
                drops.add(new Item(592, 1));
                dropModifier += 0.20; // 20% extra reward for mvp
            }

            if(challengeMode) dropModifier += 0.50; // 50% more rewards during challenge mode

            if (Utils.random((int)(200 / player.getDropRateMultiplierI())) == 0) {
             /**   int drop = Utils.random(3);
                // 1 roll per kill, not 3
                switch(drop) {
                    default: case 0:
                        drop = CRYSTAL_TOOL_SEED;
                        break;
                    case 1:
                        drop = CRYSTAL_ARMOUR_SEED;
                        break;
                    case 2:
                        drop = CRYSTAL_WEAPON_SEED;
                        break;
                }*/
                drops.add(new Item(CRYSTAL_TOOL_SEED, 1));
                player.getCollectionLog().add(CategoryType.BOSSES, "Zalcano", new Item(CRYSTAL_TOOL_SEED));
            }
            
            if (Utils.random((int)(750 / player.getDropRateMultiplierI())) == 0) {
                drops.add(new Item(53908, 1));
                player.getCollectionLog().add(CategoryType.BOSSES, "Zalcano", new Item(53908));
            }
            
            if (Utils.random((int)(8000 / player.getDropRateMultiplierI())) == 0) {
                drops.add(new Item(6571, 1));
                player.getCollectionLog().add(CategoryType.BOSSES, "Zalcano", new Item(6571));
            }

            for (Item item : drops) {
                this.sendDrop(player, new Drop(item.getId(), item.getAmount(), item.getAmount()));
            }
            Drop drop = DROPS[Utils.random(DROPS.length)];
            this.sendDrop(player, new Drop(drop.getItemId(),
                    (int) ((double)drop.getMinAmount() * dropModifier),
                    (int) ((double)drop.getMaxAmount() * dropModifier)));

            checkSlayer(player);
            
            LuckyPets.checkBossPet(player, this);
        }

        // must remove dmg after drops are calculated
        players.stream().filter(Objects::nonNull).forEach(player -> {
            player.getTemporaryAttributtes().remove(Key.ZALCANO_TEPHRA_DAMAGE);
            player.getTemporaryAttributtes().remove(Key.ZALCANO_MINING_DAMAGE);
            player.getTemporaryAttributtes().remove(Key.ZALCANO_DAMAGE_BUFF);
        });
    }

    private static final WorldTile[] GOLEM_SPAWNS = {new WorldTile(3033, 6037, 0),
            new WorldTile(3033, 6061, 0), new WorldTile(3045, 6046, 0)};

    public static void finishGolem() {
        if (golem != null) {
            golem.finish();
            golem = null;
        }
    }

    public void spawnGolem() {
    	finishGolem();

        final WorldTile spawnTile = Utils.random(GOLEM_SPAWNS);
        final Zalcano zalcano = this;

        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (isDowned) {
                    finishGolem();
                    stop();
                    return;
                }

                if (loop == 0) {
                    zalcano.setStopRandomWalk();
                    zalcano.resetWalkSteps();
                    zalcano.setNextFaceWorldTile(spawnTile);
                    World.sendProjectile(zalcano, spawnTile, FIRE_PROJECTILE_GFX, 95, 25, 32, 25, 0, 0);
                    //} else if (loop == 1) {
                } else if (loop == 2) {
                    World.sendGraphics(zalcano, new Graphics(GOLEM_EXPLODE_GFX, 0, 200), spawnTile);
                    golem = World.spawnNPC(GOLEM_ID, spawnTile, -1, false, true);
                    golem.setRandomWalk(0);
                    golem.addFreezeDelay(challengeMode ? 0 : 1800);
                    golem.setNextFaceEntity(zalcano);
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    int tick = 0;

    @Override
    public void processNPC() {
        if (disable || isDead() || isDowned || isCantInteract()) {
            return;
        }

        if (challengeModeState == 1) challengeMode = true;
        else  challengeMode = false;

        tick++;
        if (tick % (challengeMode ? 7 : 11) == 0) {
            changePhase();
        }

        if (golem != null) {
            if (!golem.isDead()) {
                checkGolemCollision();
            }
        }

        super.processNPC();
    }

    private void checkGolemCollision() {
        if (Utils.isOnRange(golem, this, 0)) {
            World.sendGraphics(this, new Graphics(GOLEM_EXPLODE_GFX), new WorldTile(golem.getX(), golem.getY(), 0));
            int add = golem.getHitpoints() * 4;
            golem.setHitpoints(0);
            this.setHitpoints(Math.min(this.getHitpoints() + add, this.getMaxHitpoints()));
            if(challengeMode)
                this.setHitpoints(3000);
            World.sendGraphics(golem, new Graphics(GOLEM_EXPLODE_GFX, 0, 200), new WorldTile(golem.getX(), golem.getY(), 0));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if (golem == null)
                        return;

                    golem.finish();
                    golem = null;
                    stop();
                }
            }, 0, 1);
        } else {
            if (!golem.isFrozen()) {
            	golem.resetWalkSteps();
            	golem.addWalkSteps(this.getX(), this.getY(), -1, false);
            }
               // golem.calcFollow(this, false);
        }
    }

    private Phase lastPhase = null, forcePhase = null;

    private enum Phase {
        ROCK_SWITCH, DEMONIC_SYMBOLS, BOULDERS, PEBBLES, GOLEM
    }

    private void changePhase() {
        setStopRandomWalk();
        resetWalkSteps();

        Phase newPhase;

        if (forcePhase == null) {
            do newPhase = Utils.randomEnum(Phase.class);
            while (newPhase == lastPhase || (newPhase == Phase.DEMONIC_SYMBOLS && demonicSymbolsActive));
        } else {
            newPhase = forcePhase;
        }

        switch (newPhase) {
            case GOLEM:
                anim(ZALCANO_SPAWN_GOLEM_ANIM);
                spawnGolem();
                break;
            case ROCK_SWITCH:
                anim(ZALCANO_ROCK_SWITCH_ANIM);
                spawnGlowingRock();
                break;
            case DEMONIC_SYMBOLS:
                anim(ZALCANO_DEMONIC_SYMBOL_ANIM);
                spawnDemonicSymbols();
                break;
            case BOULDERS:
                anim(ZALCANO_STOMP_ANIM);
                spawnBoulders();
                break;
            case PEBBLES:
                anim(ZALCANO_STOMP_ANIM);
                spawnPebbles();
                break;
        }

        lastPhase = newPhase;
        forcePhase = null;
    }

    private void spawnPebbles() {
        Zalcano z = this;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                for (Player player : players) {
                    if (player != null) {
                        player.setNextGraphics(new Graphics(SMALL_FALLING_ROCKS_GFX));
                        if(challengeMode) {
                            if(Utils.random(2) == 1) player.applyHit(new Hit(z, Utils.random(50, 100), Hit.HitLook.REGULAR_DAMAGE));
                            if(Utils.random(2) == 1) player.applyHit(new Hit(z, Utils.random(50, 100), Hit.HitLook.REGULAR_DAMAGE));
                            if(Utils.random(2) == 1) player.applyHit(new Hit(z, Utils.random(50, 100), Hit.HitLook.REGULAR_DAMAGE));
                        }
                        player.applyHit(new Hit(z, Utils.random(50, 100), Hit.HitLook.REGULAR_DAMAGE));
                    }
                }
                stop();
            }
        }, 1, 1);
    }

    private static ArrayList<WorldTile> boulderTiles = new ArrayList<WorldTile>();
    private static final int BOULDER_CAP = 100;

    private void spawnBoulders() {
        boulderTiles.clear();
        int boulders = Math.min(players.size() + (challengeMode ? 80 : 6) +
                Utils.random(1, 4), BOULDER_CAP);

        // add drops on each player
        players.stream().filter(Objects::nonNull).forEach(player -> {
        	WorldTile tile = new WorldTile(player.getX(), player.getY(), 0);
        	if (!anyMatch(tile))
        		boulderTiles.add(tile);
        });

        // add 6-10 random
        for (int i = players.size(); i < boulders; i++) {
            WorldTile tile;

            do tile = new WorldTile(3020 + Utils.random(26), 6036 + Utils.random(27), 0);
            while (!World.isTileFree(tile, 1) || anyMatch(tile));

            boulderTiles.add(tile);
        }

        Zalcano z = this;

        for (WorldTile boulderTile : boulderTiles) {
            World.sendGraphics(z, new Graphics(FALLING_ROCK_GFX), boulderTile);
        }

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                for (WorldTile boulderTile : boulderTiles) {
                    for (Player player : players) {
                        if (player != null) {
                            if (player.getX() == boulderTile.getX() && player.getY() == boulderTile.getY()) {
                                player.applyHit(new Hit(z, Utils.random(300, challengeMode ? 700 : 450), Hit.HitLook.REGULAR_DAMAGE));
                            }
                        }
                    }
                }
                stop();
            }
        }, 6, 1);
    }

    private boolean anyMatch(WorldTile tile) {
        for (WorldTile t : boulderTiles)
            if (t != null && t.matches(tile))
                return true;
        return false;
    }

    public static boolean demonicSymbolsActive = false;

    private void spawnDemonicSymbols() {
        demonicSymbolsActive = true;
        Zalcano zalcano = this;

        final WorldObject[] symbols = DemonicCircles.getRandomPattern();

        // reset all to red
        Arrays.stream(symbols).filter(Objects::nonNull).forEach(symbol -> {
            symbol.setId(RED_DEMONIC_SYMBOL_ID);
        });

        if(!challengeMode) {
            int blueSymbols = symbols.length > 12 ? 5 : 4;
            while (blueSymbols > 0) {
                int i = Utils.random(symbols.length);
                if (symbols[i].getId() != BLUE_DEMONIC_SYMBOL_ID) {
                    symbols[i].setId(BLUE_DEMONIC_SYMBOL_ID);
                    blueSymbols--;
                }
            }
        }
        
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (isDowned) {
                    for (WorldObject obj : symbols) {
                        WorldObject symbol = World.getObjectWithType(obj, 10);
                        if (symbol != null) {
                            World.removeObject(symbol);
                        }
                    }
                    demonicSymbolsActive = false;
                    stop();
                    return;
                }

                if (loop == 1) {
                    for (WorldObject obj : symbols) {
                        World.spawnObjectTemporary(obj, 12000);
                    }
                } else if (loop >= 2 && loop < 20) {
                    for (Player player : players) {
                        if (player != null && !player.isDead()) {
                            Boolean buffed = Boolean.FALSE;

                            for (WorldObject obj : symbols) {
                                if (Utils.colides(player.getX(), player.getY(), 1, obj.getX(), obj.getY(), 3)) {
                                    if (obj.getId() == 136199) {
                                        player.sendMessage("You feel the symbol below you burning through you.");
                                        player.gfx(RED_SYMBOL_GFX);
                                        // lower damage first 6 ticks
                                        int damage = loop <= 6 ? Utils.random(loop * 10) : Utils.random(200, 260);
                                        player.applyHit(new Hit(zalcano, damage, Hit.HitLook.REGULAR_DAMAGE));
                                        player.setRunEnergy(Math.max(0, player.getRunEnergy() - 10));
                                    } else {
                                        buffed = (Boolean) player.getTemporaryAttributtes().getOrDefault(Key.ZALCANO_DAMAGE_BUFF, Boolean.FALSE);
                                        if (buffed == Boolean.FALSE) {
                                            buffed = Boolean.TRUE;
                                            player.gfx(BLUE_SYMBOL_GFX);
                                            player.getTemporaryAttributtes().put(Key.ZALCANO_DAMAGE_BUFF, Boolean.TRUE);
                                            player.sendMessage("You feel the symbol below your feet fill you with power.");
                                            break;
                                        }
                                    }
                                }
                            }

                            if (buffed == Boolean.FALSE) {
                                // player walked off symbol
                                player.getTemporaryAttributtes().put(Key.ZALCANO_DAMAGE_BUFF, Boolean.FALSE);
                            }
                        }
                    }
                } else if (loop >= 24) {
                    players.stream().filter(Objects::nonNull).forEach(plr ->
                            plr.getTemporaryAttributtes().put(Key.ZALCANO_DAMAGE_BUFF, Boolean.FALSE));
                    demonicSymbolsActive = false;
                    stop();
                }
                loop++;
            }
        }, 0, 0);
    }

    private static WorldTile[] ROCKS;

    private static int previousRock = 0;

    private void spawnGlowingRock() {
        if (NE_ROCK == null) {
            init();
        }

        int rndRock;
        do rndRock = Utils.random(0, ROCKS.length);
        while (rndRock == previousRock);

        final int glowingRock = rndRock;

        this.setNextFaceWorldTile(ROCKS[previousRock]);

        Zalcano zalcano = this;

        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 1) {
                    World.sendProjectile(zalcano, new WorldTile(ROCKS[previousRock].getX() + 1, ROCKS[previousRock].getY() + 1, 0), EXPLODE_GLOW_ROCK_GFX, 95, 50, 32, 10, 0, 0);
                    //} else if(loop == 1) {
                } else if (loop == 4) {

                    World.sendGraphics(zalcano, new Graphics(1310), ROCKS[previousRock].transform(1, 1, 0));

                    for (Player player : players) {
                        if (player != null) {
                            if (!player.isDead() && Utils.isOnRange(player.getX(), player.getY(), 0, ROCKS[previousRock].getX(), ROCKS[previousRock].getY(), 3, 1)) {
                                player.sendMessage("The glowing rock explodes beside you!");
                                player.applyHit(new Hit(zalcano, Utils.random(200, 450), Hit.HitLook.MAGIC_DAMAGE));
                            }
                        }
                    }

                } else if(loop == 5) {

                    WorldObject glowRock = World.getObjectWithType(ROCKS[glowingRock], 10);
                    WorldObject prevRock = World.getObjectWithType(ROCKS[previousRock], 10);

                    WorldObject depletedRock = new WorldObject(DEPLETED_ROCK_ID, 10, prevRock.getRotation(), ROCKS[previousRock]);
                    WorldObject newRock = new WorldObject(GLOW_ROCK_ID, 10, glowRock.getRotation(), ROCKS[glowingRock]);

                    World.spawnObject(depletedRock);
                    World.spawnObject(newRock);

                    previousRock = glowingRock;
                    stop();
                }
                loop++;
            }
        }, 0, 0);
    }

    @Override
    public void sendDeath(Entity source) {
        if (golem != null) {
            golem.finish();
            golem = null;
        }

        if (!isDowned) {
            // shield destroyed
            down();
        } else {
            // real death
            isDowned = false;
            players.stream().filter(Objects::nonNull).forEach(Zalcano::resetZalcItems);
            setNextNPCTransformation(ZALCANO_ID);
            hp = 10000;

            // remove glow rock
            WorldObject prevRock = World.getStandartObject(ROCKS[previousRock]);
            WorldObject depletedRock = new WorldObject(DEPLETED_ROCK_ID, 10, prevRock.getRotation(), ROCKS[previousRock]);
            World.spawnObject(depletedRock);

            players.stream().filter(Objects::nonNull).forEach(Zalcano::resetZalcItems);
            Golem.removeDroppedTephra();

            fullDeath(source);
        }
    }

    private void fullDeath(Entity source) {
        final NPCCombatDefinitions defs = getCombatDefinitions();
        resetWalkSteps();
        getCombat().removeTarget();
        setNextAnimation(new Animation(defs.getDeathEmote()));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
               /* if (source instanceof Player) {
                    Player player = (Player) source;
                    NPCKillLog.addKill(player, "Zalcano", 0, true);
                }*/
                setHitpoints(0);
                anim(-1);
                drop();
                reset();
                setLocation(getRespawnTile());
                finish();
                setRespawnTask();
                tick = -defs.getRespawnDelay();
                stop();
            }
        }, defs.getDeathDelay(), 1);
    }

    @Override
    public void onSpawn() {
        restart();
    }

    private void down() {
        int hpThreshold = Math.max(0, hp - 3800);
        anim(ZALCANO_DOWN_ANIM);
        setNextNPCTransformation(ZALCANO_DOWNED_ID);
        setCantInteract(true);
        addFreezeDelay(10000);
        setHitpoints(hp);
        isDowned = true;
        Zalcano zalcano = this;
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (getHitpoints() == 0) {
                    stop();
                    return;
                }

                if (loop < 26 && !isCantInteract()) {
                    if (getHitpoints() < hpThreshold) {
                        hp = hpThreshold;
                        loop = 25;
                        setCantInteract(true);
                    }

                    // hit all players who walk on zalc while downed
                    players.stream().filter(Objects::nonNull).forEach(player -> {
                        if (Utils.collides(player, zalcano)) {
                            player.applyHit(new Hit(zalcano, Utils.random(150, 300), Hit.HitLook.REGULAR_DAMAGE));
                        }
                    });
                }

                if (loop == 1) {
                    setCantInteract(false);
                } else if (loop == 25) {
                    anim(ZALCANO_STAND_UP_ANIM);
                    setNextNPCTransformation(ZALCANO_ID);
                    setCantInteract(true);
                } else if (loop == 26) {
                    hp = getHitpoints();
                    setHitpoints(3000);
                } else if (loop == 28) {
                    // end of stand-up anim, damage anyone beside
                    players.stream().filter(Objects::nonNull).forEach(player -> {
                        if (Utils.isOnRange(zalcano, player, 0)) {
                            player.applyHit(new Hit(zalcano, Utils.random(challengeMode ? 400 : 150, 450), Hit.HitLook.REGULAR_DAMAGE));
                            player.lock(1);
                            player.anim(425);
                            player.resetWalkSteps();
                            // TODO force walk back 1`step away from Zalcano
                        }
                    });

                    tick = 0;
                    forcePhase = Phase.ROCK_SWITCH;
                    setCantInteract(false);
                    isDowned = false;
                    stop();
                }
                loop++;
            }
        }, 1, 1);
    }
    
    
    @Override
    public int getMaxHitpoints() {
    	return getId() == ZALCANO_ID ? 3000 : 10000;
    }
    
	@Override
	public int getHitbarSprite(Player player) {
		return getId() == ZALCANO_ID ? 22468 : 22191;
	}
}