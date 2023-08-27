package com.rs.game.player.content.raids.cox;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogues.impl.HerbloreD;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * @since Dec 03, 2020
 */
public class ChambersHerblore {

    public static final int EMPTY_GOURD_VIAL = Settings.OSRS_ITEM_OFFSET + 20800;
    public static final int WATER_FILLED_GOURD_VIAL = Settings.OSRS_ITEM_OFFSET + 20801;

    public static void init() {
        ObjectHandler.register(129772, 1, (player, obj) -> pickGourdTree(player, false));
        ObjectHandler.register(129772, 2, (player, obj) -> pickGourdTree(player, true));
        for(Herbs herbs : Herbs.values())
            InventoryOptionsHandler.register(herbs.grimyId, 1, ((player, item) -> cleanHerb(player, herbs)));
    }

    public static void cleanHerb(Player player, Herbs herb) {
        Item item = player.getInventory().findItem(herb.grimyId);
        if(item != null) {
            item.setId(herb.cleanId);
            player.getInventory().refresh();
            player.getSkills().addXp(Skills.HERBLORE, 4);
            player.sendMessage("You clean the " + ItemConfig.forID(herb.cleanId).name + ".");
        }
    }

    public static void pickGourdTree(Player player, boolean lots) {
        if(ChambersOfXeric.getRaid(player) == null) {
            player.sendMessage("You can't reach the fruit.");
            return;
        }
        int slots = player.getInventory().getFreeSlots();
        int amount = lots ? slots : 1;
        if(slots == 0) {
            player.sendMessage("Your inventory is full.");
            return;
        }
        player.anim(Settings.OSRS_ANIMATIONS_OFFSET + 2280);
        player.lock(2);
        WorldTasksManager.schedule(() -> {
            player.getInventory().addItem(EMPTY_GOURD_VIAL, amount);

            player.getPackets().sendGameMessage("You pick " + (lots ? "some" : "a")
                    + " gourd fruit" + (lots ? "s" : "") + " from the tree, tearing the top"
                    + (lots ? "s" : "") + " off in the process.");
        }, 2);
    }

    public static void fillGourdVials(Player player) {
        WorldTask task = new WorldTask() {
            @Override
            public void run() {
                player.anim(832);
                player.getPackets().sendGameMessage("You fill the gourd vial.");
                for(int i = 3 + Utils.random(8); i >= 0; i--) {
                    Item empty = player.getInventory().findItem(EMPTY_GOURD_VIAL);
                    if(empty != null) {
                        empty.setId(WATER_FILLED_GOURD_VIAL);
                    } else {
                        player.sendMessage("You have run out of gourd vials to fill.");
                        stop();
                        player.getActionManager().forceStop();
                        break;
                    }
                }
                player.getInventory().refresh();
            }
        };


        WorldTasksManager.schedule(task, 0, 0);

        player.getActionManager().createSkillingLock(()->{
            // on interrupt
            player.anim(-1);
            player.setNextFaceEntity(null);
            task.stop();
        });
    }

    public static int getTier(Player player, Potions p) {
        int potionTier = -1;
        int herbloreLevel = player.getSkills().getLevel(Skills.HERBLORE);
        for (int i = 0; i < 3; i++) {
            if (herbloreLevel >= p.levelReqs[i]) {
                potionTier = i;
                break;
            }
        }
        return potionTier;
    }

    public static boolean make(Player player, Item item1, Item item2) {
        int tier = -1;
        ArrayList<Potions> pots = new ArrayList<>();

        if(InventoryOptionsHandler.contains(50905, item1, item2) != null) {
            tier = getTier(player, Potions.ELDER);

            if(tier == -1) {
                player.sendMessage("You must have a Herblore level of 47 to make this potion.");
                return true;
            } else {
                pots.addAll(Arrays.stream(CB_POTS)
                        .filter(pot -> player.getInventory().containsItem(pot.secondaryId, 1))
                        .collect(Collectors.toList()));
            }
        } else if(InventoryOptionsHandler.contains(50908, item1, item2) != null) {
            tier = getTier(player, Potions.REVITALISATION);

            if(tier == -1) {
                player.sendMessage("You must have a Herblore level of 52 to make this potion.");
                return true;
            } else {
                pots.addAll(Arrays.stream(NON_CB_POTS)
                        .filter(pot -> player.getInventory().containsItem(pot.secondaryId, 1))
                        .collect(Collectors.toList()));
            }
        } else if(InventoryOptionsHandler.contains(50902, item1, item2) != null) {
            tier = getTier(player, Potions.OVERLOAD);
            if(tier == -1) {
                player.sendMessage("You must have a Herblore level of 60 to make an Overload potion.");
                return true;
            } else {
                if(Arrays.stream(Potions.OVERLOAD.secondaryPotions[tier])
                        .anyMatch(i-> InventoryOptionsHandler.contains(i, item1, item2) != null)) {
                    pots.add(Potions.OVERLOAD);
                }
            }
        } else {
            return false;
        }

        player.getDialogueManager().startDialogue(new ChambersHerbloreD(), pots.toArray(new Potions[pots.size()]), tier);
        return true;
    }

    private static Potions[] CB_POTS = {Potions.ELDER, Potions.TWISTED, Potions.KODAI};
    private static Potions[] NON_CB_POTS = {Potions.XERIC_ACID, Potions.REVITALISATION, Potions.PRAYER_ENHANCE};

    private static void createOverload(Player player, Potions potion, final int amount, int potionTier) {
        Item herb = player.getInventory().findItem(potion.herbId);
        Item elder = player.getInventory().findItem(potion.secondaryPotions[potionTier][0]);
        Item kodai = player.getInventory().findItem(potion.secondaryPotions[potionTier][1]);
        Item twisted = player.getInventory().findItem(potion.secondaryPotions[potionTier][2]);

        if (herb != null && elder != null && kodai != null && twisted != null) {
            WorldTask task = new WorldTask() {
                int created = 0;
                @Override
                public void run() {
                    Item herb = player.getInventory().findItem(potion.herbId);
                    Item elder = player.getInventory().findItem(potion.secondaryPotions[potionTier][0]);
                    Item kodai = player.getInventory().findItem(potion.secondaryPotions[potionTier][1]);
                    Item twisted = player.getInventory().findItem(potion.secondaryPotions[potionTier][2]);

                    if (herb != null && elder != null && kodai != null && twisted != null) {
                        herb.setId(potion.potionIds[potionTier]);
                        elder.setId(-1);
                        kodai.setId(-1);
                        twisted.setId(-1);
                        player.getInventory().refresh();
                        player.getSkills().addXp(Skills.HERBLORE, 66);
                        player.anim(5363);
                        player.getPackets().sendGameMessage("You mix your noxifer herb together with an elder, kodai and twisted potion into an overload.");
                    } else {
                        stop();
                    }
                    if(++created == amount) {
                        stop();
                        player.getActionManager().forceStop();
                    }
                }
            };

            WorldTasksManager.schedule(task, 0, 0);

            player.getActionManager().createSkillingLock(()->{
                // on interrupt
                player.anim(-1);
                player.setNextFaceEntity(null);
                task.stop();
            });
        } else {
            player.sendMessage("You need an Elder(4), Kodai(4) and Twisted(4) to create an overload. ");
        }
    }

    public static void createPotion(Player player, Potions potion, int amount, int potionTier) {
        Item herb = player.getInventory().findItem(potion.herbId);
        Item secondary = player.getInventory().findItem(potion.secondaryId);
        Item gourd = player.getInventory().findItem(WATER_FILLED_GOURD_VIAL);
        if (herb != null && secondary != null && gourd != null) {
            WorldTask task = new WorldTask() {
                int created = 0;
                @Override
                public void run() {
                    Item herb = player.getInventory().findItem(potion.herbId);
                    Item secondary = player.getInventory().findItem(potion.secondaryId);
                    Item gourd = player.getInventory().findItem(WATER_FILLED_GOURD_VIAL);

                    if (herb != null && secondary != null && gourd != null) {
                        player.getSkills().addXp(Skills.HERBLORE, 26);
                        player.setNextAnimation(new Animation(363));
                        player.getPackets().sendGameMessage("You mix your " + herb.getDefinitions().name + " together with your "
                                + secondary.getDefinitions().name + " into a potion perfectly!");

                        gourd.setId(potion.potionIds[potionTier]);
                        herb.setId(-1);
                        if(secondary.getAmount() > 1)
                            secondary.setAmount(secondary.getAmount() - 1);
                        else
                            secondary.setId(-1);
                        player.getInventory().refresh();
                    } else {
                        stop();
                        player.getActionManager().forceStop();
                    }

                    if(++created == amount) {
                        stop();
                        player.getActionManager().forceStop();
                    }
                }
            };

            WorldTasksManager.schedule(task, 0, 0);

            player.getActionManager().createSkillingLock(() -> {
                // on interrupt
                task.stop();
                WorldTasksManager.schedule(() -> {
                    player.resetAnim();
                });
            });
        }
    }

    public static void mixPotion(Player player, Potions potion, int quantity, int tier) {
        player.stopAll();
        if(potion == Potions.OVERLOAD) {
            createOverload(player, potion, quantity, tier);
        } else {
            createPotion(player, potion, quantity, tier);
        }
    }

    /**
     * Potion brewing
     */
    enum Potions {

        /* combat potions */
        ELDER(20905, 20910, new int[]{70, 59, 47}, new int[]{20924, 20920, 20916}),
        TWISTED(20905, 20912, new int[]{70, 59, 47}, new int[]{20936, 20932, 20928}),
        KODAI(20905, 20911, new int[]{70, 59, 47}, new int[]{20948, 20944, 20940}),

        /* restore potions */
        REVITALISATION(20908, 20910, new int[]{78, 65, 52}, new int[]{20960, 20956, 20952}),
        PRAYER_ENHANCE(20908, 20912, new int[]{78, 65, 52}, new int[]{20972, 20968, 20964}),
        XERIC_ACID(20908, 20911, new int[]{78, 65, 52}, new int[]{20984, 20980, 20976}),

        /* overload */
        OVERLOAD(new int[][]{
                {20924, 20936, 20948},
                {20920, 20932, 20944},
                {20916, 20928, 20940}}, 20902, new int[]{90, 75, 60}, new int[]{20996, 20992, 20988});

        public int herbId, secondaryId;
        public int[] levelReqs, potionIds;
        public int[][] secondaryPotions;

        Potions(int herbId, int secondaryId, int[] levelReqs, int[] potionIds) {
            this.herbId = herbId + Settings.OSRS_ITEM_OFFSET;
            this.secondaryId = secondaryId + Settings.OSRS_ITEM_OFFSET;
            this.levelReqs = levelReqs;
            this.potionIds = potionIds;
            for (int i = 0; i < potionIds.length; i++) {
                potionIds[i] += Settings.OSRS_ITEM_OFFSET;
            }
        }

        Potions(int[][] secondaryPotions, int herbId, int[] levelReqs, int[] potionIds) {
            this.secondaryPotions = secondaryPotions;
            this.levelReqs = levelReqs;
            this.potionIds = potionIds;
            this.herbId = herbId + Settings.OSRS_ITEM_OFFSET;
            for (int i = 0; i < potionIds.length; i++) {
                potionIds[i] += Settings.OSRS_ITEM_OFFSET;
            }
            for (int i = 0; i < this.secondaryPotions.length; i++) {
                for (int j = 0; j < this.secondaryPotions[i].length; j++) {
                    this.secondaryPotions[i][j] += Settings.OSRS_ITEM_OFFSET;
                }
            }
        }
    }

    /**
     * Herbs
     */
    enum Herbs {

        NOXIFER(20901, 20902),
        GOLPAR(20904, 20905),
        BUCHU_LEAF(20907, 20908);

        private int grimyId, cleanId;

        Herbs(int grimyId, int cleanId) {
            this.grimyId = grimyId + Settings.OSRS_ITEM_OFFSET;
            this.cleanId = cleanId + Settings.OSRS_ITEM_OFFSET;
        }
    }
}
