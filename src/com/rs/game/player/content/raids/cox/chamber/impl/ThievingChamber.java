package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.game.*;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Direction;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author Simplex
 * @since Nov 04, 2020
 */
public class ThievingChamber extends Chamber {

    /* OBJECTS */
    private static int CLOSED_CHEST = 129742, EMPTY_CHEST = 129743, GRUBS_CHEST = 129744, HATCHED_CHEST = 129745;
    public static final int EMPTY_TROUGH = 129746;
    public static final int FILLED_TROUGH = 129874;

    /* NPCS */
    private static final int CORRUPTED_SCAVENGER = 27602;
    protected static final int SLEEPING_SCAVENGER = 27603;

    /* ITEMS */
    private static final int CAVERN_GRUBS = 50885;

    /* TILES */
    private static final WorldTile SCAV_SPAWN = new WorldTile(9, 22, 2);
    protected static final WorldTile SCAV_SLEEP = new WorldTile(9, 25, 2);

    private enum ChestType {
        POISON(EMPTY_CHEST),
        HATCHING(GRUBS_CHEST),
        HATCHED(HATCHED_CHEST),
        BATS(EMPTY_CHEST),
        EMPTY(EMPTY_CHEST),
        ;
        private int objId;
        ChestType(int objId) {
            this.objId = objId;
        }

        public void effect(WorldObject chest, Player player) {
            int slots = player.getInventory().getFreeSlots();
            slots = Math.min(6, slots);

            switch(this) {
                case POISON:
                    World.sendGraphics(player, new Graphics(5184, 0, 100), chest);
                    player.applyHit(player, Utils.random(10, 30), Hit.HitLook.POISON_DAMAGE);
                    break;
                case HATCHING:
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "No cocoons in this chest have hatched.");
                    break;
                case HATCHED:
                    player.getInventory().addItemDrop(CAVERN_GRUBS, slots);
                    player.getDialogueManager().startDialogue("ItemMessage",
                            "You find some grubs in the chest.", CAVERN_GRUBS);
                    break;
                case BATS:
                    player.getInventory().addItemDrop(50883, slots);
                    player.getDialogueManager().startDialogue("ItemMessage",
                            "You find some bats in the chest.", 50883);
                    break;
                case EMPTY:
                    player.sendMessage("This chest is empty.");
                    break;

            }
        }

        private static ChestType roll() {
            if (Utils.rollDie(20, 1))
                return ChestType.BATS;
            if (Utils.rollDie(8, 1))
                return ChestType.POISON;
            if (Utils.rollDie(3, 1))
                return ChestType.HATCHING;
            return ChestType.HATCHED;
        }
    }

    public ThievingChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    /**
     * Next open will receive grubs
     */
    public ArrayList<WorldTile> grubLocations = new ArrayList<>();
    public ArrayList<WorldTile> poisonLocations = new ArrayList<>();

    /* Local vars */
    private COXBoss scav;
    private int grubs;
    private int consumedGrubs;
    private int maxHP;
    private boolean puzzleCompleted;

    public int getMaxHitpoints() {
        return maxHP;
    }

    public void setPuzzleCompleted() {
        puzzleCompleted = true;
    }

    private WorldObject trough;

    public int getConsumedGrubs() {
        return consumedGrubs;
    }

    public void removeConsumed() {
        consumedGrubs--;
    }

    public void eatGrub() {
        grubs--;
        consumedGrubs++;

        if(consumedGrubs == maxHP) {
            scav.sendDeath(null);
        }

        if(trough != null && grubs == 0) {
            trough.updateId(EMPTY_TROUGH);
        }
    }

    /**
     * Static initializations
     */
    public static void init() {
        ObjectHandler.register(CLOSED_CHEST, 1, ThievingChamber::openChest);
        ObjectHandler.register(EMPTY_TROUGH, 1, ThievingChamber::depositGrubs);
        ObjectHandler.register(FILLED_TROUGH, 1, ThievingChamber::depositGrubs);
    }

    /**
     * Remove all grubs, add points, change object to filled trough
     */
    public static void depositGrubs(Player player, WorldObject trough) {
        ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
        if(raid != null) {
            int amount = player.getInventory().getAmountOf(CAVERN_GRUBS);
            /*if(player.getRights() != 2)
                amount = 1000;*/
            if (amount == 0) {
                player.sendMessage("You don't have any cavern grubs to deposit on the trough.");
                return;
            }
            player.anim(20832);
            player.getInventory().deleteItem(CAVERN_GRUBS, amount);
            raid.getThievingChamber().addGrubs(amount);
            if (trough.getId() == EMPTY_TROUGH)
                trough.updateId(FILLED_TROUGH);
            player.sendMessage("You deposit the cave grubs in the trough.");

            if(!raid.getThievingChamber().puzzleCompleted) {
                amount = Math.min(raid.getThievingChamber().getGrubsRemaining(), amount);
                raid.addPoints(player.getUsername(), amount * 50);
            }
        }
    }

    private int getGrubsRemaining() {
        if(scav != null) {
            return scav.getHitpoints();
        } else {
            return 0;
        }
    }

    /**
     * Deposit grubs
     */
    private void addGrubs(int amount) {
        grubs += amount;
    }

    protected int getGrubs() {
        return grubs;
    }

    /**
     * Attempt to open a thieving room chest
     */
    public static void openChest(Player player, WorldObject object) {
        ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
        if(raid != null) {
            player.stopAll();
            player.anim(20832);
            player.lock(1);
            WorldTasksManager.schedule(() -> {
                if (Utils.random(1.0) > ((double)player.getSkills().getLevel(Skills.THIEVING) / 2.0 / 100.0)) {
                    // open attempt failed
                    WorldObject chest = World.getObjectWithType(object, 10);
                    if(chest != null && chest.getId() == CLOSED_CHEST) {
                        openChest(player, object);
                    }
                    return;
                } else {
                    // successful open  attempt
                    ChestType chestType;

                    if(raid.getThievingChamber().getGrub(object)) {
                        chestType = ChestType.HATCHED;
                    } else if(raid.getThievingChamber().poisonLocations.stream().anyMatch(worldTile -> object.matches(worldTile))) {
                        chestType = ChestType.POISON;
                    } else {
                        chestType = ChestType.roll();
                        if(chestType == ChestType.POISON) {
                            raid.getThievingChamber().poisonLocations.add(object.clone());
                        }
                    }

                    World.spawnObjectTemporary(new WorldObject(chestType.objId, 10, object.getRotation(), object.clone()), 10000);
                    chestType.effect(object, player);
                }
            });
        }
    }

    /**
     * If this tile has spawned hatchlings reward player and remove from list
     * else add this location to the list, next open is grubs
     */
    private boolean getGrub(WorldTile tile) {
        WorldTile grub = null;

        for(WorldTile t : grubLocations)
            if(t.matches(tile))
                grub = t;

        if(grub != null) {
            grubLocations.remove(grub);
            return true;
        }

        // in case multiple players picked the same chest
        if(grubLocations.stream().noneMatch(mult -> tile.matches(mult)))
            grubLocations.add(tile.clone());
        return false;
    }

    /**
     * Spawn corrupted scavenger, start eating process
     */
    @Override
    public void onRaidStart() {
        scav = new CorruptedScavenger(getRaid(), CORRUPTED_SCAVENGER, getWorldTile(SCAV_SPAWN), this);
        trough = World.getObjectWithType(getWorldTile(9, 21), 10);
        maxHP = getRaid().getTeamSize() * 15;
        if(maxHP == 0) maxHP = 15;
    }

    @Override
    public boolean chamberCompleted(Player player) {
        if(player.getX() < scav.getX() && !puzzleCompleted) {
            player.sendMessage("You're too afraid to attempt to sneak by the scavenger!");
            return false;
        }
        return true;
    }
}

/**
 * Corrupted Scvavenger NPC
 */
class CorruptedScavenger extends COXBoss {
    private boolean init = false;

    public CorruptedScavenger(ChambersOfXeric raid, int id, WorldTile tile, Chamber chamber) {
        super(raid, id, tile, chamber);
        setForceAgressive(false);
        setDirection(Direction.SOUTH, true);
        setRandomWalk(0);
        init = true;
    }

    /**
     * Never attack this NPC.
     */
    @Override
    public boolean preAttackCheck(Player attacker) {
        attacker.sendMessage("You cannot attack this NPC.");
        return false;
    }

    /**
     * Once the NPC dies, walk to the final resting place and.. sleep.
     */
    @Override
    public void sendDeath(Entity killer) {
        COXBoss scav = this;
        WorldTile t = getChamber().getWorldTile(ThievingChamber.SCAV_SLEEP);
        addWalkSteps(t.getX(), t.getY(), -1, false);
        ThievingChamber thievingChamber = (ThievingChamber) getChamber();
        WorldTasksManager.schedule(new WorldTask() {
            int failSafe = 15;
            int delay = 2;
            boolean atTile = false;
            @Override
            public void run() {
                if(!atTile && scav.matches(t)) {
                    delay = 1;
                    atTile = true;
                }

                if(delay>0) {
                    delay--;
                    return;
                }

                if(atTile) {
                    setNextNPCTransformation(ThievingChamber.SLEEPING_SCAVENGER);
                    anim(27497);
                    forceTalk("Yawwwwwwwn!");
                    thievingChamber.setPuzzleCompleted();
                    stop();
                } else if(failSafe-- == 0) {
                    setNextNPCTransformation(ThievingChamber.SLEEPING_SCAVENGER);
                    anim(-1);
                    setNextWorldTile(t);
                }
            }
        }, 0, 0);
    }

    @Override
    public int getHitbarSprite(Player player) {
        return 21416;
    }

    int inactiveTicks = 0;
    int eatCooldown = 0;

    @Override
    public int getMaxHitpoints() {
        return !init ? 15 : ((ThievingChamber) getChamber()).getMaxHitpoints();
    }

    @Override
    public int getHitpoints() {
        return !init ? 15 : getMaxHitpoints() - ((ThievingChamber) getChamber()).getConsumedGrubs();
    }

    @Override
    public void processNPC() {
        if(getHitpoints() <= 0)
            return;
        applyHit(this, 0); // show hp bar always

        if (eatCooldown > 0) {
            eatCooldown--;
        } else if(raid.getThievingChamber().getGrubs() > 0) {
            raid.getThievingChamber().eatGrub();
            anim(20256);
            eatCooldown = 2;
        } else if(inactiveTicks++ > 6 && raid.getThievingChamber().getConsumedGrubs() > 0) {
            raid.getThievingChamber().removeConsumed();
            inactiveTicks = 0;
        }
    }
}
