package com.rs.game.player.content.seasonalEvents;

import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Stopwatch;
import com.rs.utils.Utils;
import com.rs.utils.DropTable.*;

import java.util.HashMap;

/**
 * @author Simplex
 * created on 2021-03-19
 */
public class Easter2021NPC extends NPC {

    private static final int UNCRACKED_EGG = 70103, CRACKED_EGG = 70104;

    private static final WorldTile
            NORTHWEST_TILE = new WorldTile(3089, 3500, 0),
            NORTHEAST_TILE = new WorldTile(3114, 3500, 0),
            SOUTHEAST_TILE = new WorldTile(3114, 3475, 0),
            SOUTHWEST_TILE = new WorldTile(3089, 3475, 0);

    private static WorldTile[] walkQueue = {
            NORTHEAST_TILE, SOUTHEAST_TILE, SOUTHWEST_TILE, NORTHWEST_TILE
    };

    private int walkStep, eggsToDrop;
    private Stopwatch lastSpawn = new Stopwatch();

    enum SkillBasedEggCrack {
        FLETCHING(17299, 3300, 8170),
        WOODCUTTING(17304 , 3301, 6364),
        SMITHING(17309 , 3305, 5416 ),
        MINING(17310 , 3304 , 4761),
        STRENGTH(5418, 3300, 1746),
        COOKING(17314 , 3306 , 897),
        RUNECRAFTING(17279 , 3270 , 791);

        public static SkillBasedEggCrack get(int skill) {
            String n = Skills.SKILL_NAME[skill].toUpperCase();
            return SkillBasedEggCrack.valueOf(n);
        }

        SkillBasedEggCrack(int anim, int gfx, int failAnim) {
            this.anim = anim;
            this.gfx = gfx;
            this.failAnim = failAnim;
        }

        int anim, gfx, failAnim;

        public boolean crack(CrackableEgg egg, Player player) {
            if(player.getSkills().getLevel(egg.skill) < egg.levelReq) {
                player.anim(failAnim);
                player.sendMessage("This egg requires level "+egg.levelReq+" " + Skills.SKILL_NAME[egg.skill] + " to crack!");
                return false;
            } else {
                boolean didCrack = Utils.rollDie(5, 1);

                if(!didCrack) {
                    player.sendMessage("Your " + Skills.SKILL_NAME[egg.skill] + " skill fails to crack the egg!");
                    player.anim(failAnim);
                    player.applyHit(null, 10 + Utils.random(20));
                } else {
                    player.anim(anim);
                    player.gfx(gfx);
                    player.sendMessage("You crack the egg with your " + Skills.SKILL_NAME[egg.skill] + " skill");
                }
                return didCrack;
            }
        }
    }

    class CrackableEgg {
        int[] POSSIBLE_SKILLS = {Skills.FLETCHING, Skills.WOODCUTTING, Skills.MINING, Skills.SMITHING, Skills.STRENGTH, Skills.COOKING, Skills.RUNECRAFTING};
        public int skill = Utils.random(POSSIBLE_SKILLS);
        public int levelReq = 50 + Utils.random(30);
        public long creationMS = Utils.currentTimeMillis();
    }

    private static HashMap<Integer, CrackableEgg> spawnedEggs = new HashMap<>();

    public static void init() {
        ObjectHandler.register(UNCRACKED_EGG, 1, (player, obj) -> {
            CrackableEgg crackableEgg = spawnedEggs.get(obj.getTileHash());

            if(crackableEgg == null || obj.getId() != UNCRACKED_EGG) {
                player.sendMessage("Oh no, you're too late!");
                return;
            }

            boolean expiredEgg = Utils.currentTimeMillis() - System.currentTimeMillis() > 120_000;
            if(expiredEgg) {
                player.sendMessage("Aggggh!! That egg was expired!");
                player.applyHit(null, 69, Hit.HitLook.POISON_DAMAGE);
                return;
            }

            player.lock(2);
            if (SkillBasedEggCrack.get(crackableEgg.skill).crack(crackableEgg, player)) {
                spawnedEggs.remove(obj.getTileHash());
                ItemDrop itemDrop = Easter2021.CRACKABLE_EGG_TABLE.roll();

                if(itemDrop.isAnnounceDrop()) {
                    World.sendNews( "<col=ffff00><shad=00ffff>" + player.getName() + " has found " + itemDrop.get().getAmount() + " x " + itemDrop.get().getName() + " in an Easter egg!", 0);
                }

                WorldTasksManager.schedule(() -> {
                    player.setLootbeam(World.addGroundItem(itemDrop.get(), new WorldTile(obj), player, true, 60));
                    player.sendMessage("<col=00ffff><shad=ffff00>Inside the egg you find: " + Utils.getFormattedNumber(itemDrop.get().getAmount()) + " x " + itemDrop.get().getName() + "!");
                    obj.updateId(CRACKED_EGG);
                    World.unclipTile(obj);
                    WorldTasksManager.schedule(() -> {
                        obj.remove();
                    }, 15);
                }, 2);
            }
        });
    }

    public Easter2021NPC(int id, WorldTile tile) {
        super(id, tile, -1, false, true);

        walkStep = 0;
        eggsToDrop = Easter2021.CRACKABLE_EGGS_TO_DROP;
        lastSpawn.delayMS(Utils.random(3000, 15000));
    }

    @Override
    public void processNPC() {
        if(isTeleporting())
            return;
        processWalkQueue();

        if(eggsToDrop > 0) {
            processEggDrop();
        }
    }

    private void processWalkQueue() {
        WorldTile step = walkQueue[walkStep];

        if(matches(step)) {
            walkStep++;
            if(walkStep == walkQueue.length)
                walkStep = 0;
            step = walkQueue[walkStep];
        }

        if(Utils.rollDie(30)) {
            setTeleporting(3);
            resetWalkSteps();
            World.sendGraphics(188, this);
            World.sendGraphics(188, step);
            final WorldTile toTile = step;
            WorldTasksManager.schedule(() -> {
                setNextWorldTile(toTile);
            }, 1);
        } else {
            addWalkSteps(step.getX(), step.getY(), -1, false);
        }

    }

    private void processEggDrop() {
        // small egg drop
        if(Utils.random(4) == 0) {
            if(Utils.rollDie(2)) {

                String a = "";
                for(int i = 0; i<Utils.random(1, 10);i++) a+="a";
                forceTalk("<col=00ffff><shad=ffff00>Ha" + a + "ppy Easter!");
            } else {
                forceTalk("<col=00ffff><shad=ffff00>Eggs for everyone!");
            }

            area(1).forEach(worldTile -> {
                if(World.isTileFree(worldTile, 1) && Utils.random(3) == 0)
                    World.addGroundItem(new Item(1961), worldTile, null, false, -1);
            });
        }

        // crackable egg drop
        if(lastSpawn.finished()) {
            WorldTile t = this.relative(-1 + Utils.random(0, 2), -1 + Utils.random(0, 2));
            spawnedEggs.put(t.getTileHash(), new CrackableEgg());

            WorldObject obj = new WorldObject(UNCRACKED_EGG, 10, Utils.random(4), t);
            World.spawnObject(obj);

            lastSpawn.delayMS(Utils.random(3000, 15000));
            if(--eggsToDrop == 0) {
                this.gfx(3014);
                WorldTasksManager.schedule(() -> {
                    Easter2021NPC.this.finish();
                }, 4);
                if(Utils.random(4) == 0) {
                    if(Utils.rollDie(2)) {
                        String a = "a";
                        for(int i = 0; i<Utils.random(1, 10);i++) a+="a";
                        forceTalk("<col=00ffff><shad=ffff00>H" + a + "ppy Easter!");
                    } else {
                        forceTalk("<col=00ffff><shad=ffff00>Eggs for everyone!");
                    }

                    area(1).forEach(worldTile -> {
                        if(World.isTileFree(worldTile, 1) && Utils.random(3) == 0)
                            World.addGroundItem(new Item(1961), worldTile, null, false, -1);
                    });
                }
                World.sendNews("<col=00ffff><shad=ffff00>The Easter bunny has finished dropping eggs, hurry and you might still be able to grab one at home!", 1);
            }
        }
    }
}
