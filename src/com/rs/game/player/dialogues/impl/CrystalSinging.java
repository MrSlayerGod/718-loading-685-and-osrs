package com.rs.game.player.dialogues.impl;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.Cooking;
import com.rs.game.player.actions.mining.Mining;
import com.rs.game.player.actions.mining.MiningBase;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.actions.woodcutting.WoodcuttingBase;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.Arrays;

import static com.rs.game.npc.others.zalcano.Zalcano.*;
import static com.rs.game.player.dialogues.impl.CrystalSinging.*;

/**
 * @author Simplex
 * @since May 31, 2020
 */
public class CrystalSinging extends Dialogue {
    // obj
    public static int SINGING_BOWL = 136552;

    // item
    public static int CRYSTAL_BODY = 53975, CRYSTAL_LEGS = 53979, CRYSTAL_HELM = 53971,
            CRYSTAL_BOW = 4212, CRYSTAL_SHIELD = 4224, CRYSTAL_HALBRED = 53987,
          /*  BLADE_OF_SAELDOR = 53995,*/ ENHANCED_CRYSTAL_KEY = 53951,
            CRYSTAL_AXE = 53673, CRYSTAL_PICKAXE = 53680, CRYSTAL_HARPOON = 53762;

    private WorldObject object;
    private int[] ids = new int[SingingRecipes.values().length];

    @Override
    public void start() {
        object = (WorldObject) parameters[0];
        int count = 0;
        for (SingingRecipes sing : SingingRecipes.values()) {
            // show all items so players can see the ingredients by clicking
            //    if (sing.hasItems(player)) {
            ids[count++] = sing.getProduct().getId();
            //    }
        }
        if (count == 0) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You don't have the ingredients to sing anything.");
            return;
        }
        SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to sing,<br>then click on the item to begin.", 28, ids, null);
    }

    @Override
    public void run(int interfaceId, int componentId) {
        int slot = SkillsDialogue.getItemSlot(componentId);
        if (slot >= ids.length) {
            end();
            return;
        }
        SingingRecipes singingRecipes = SingingRecipes.values()[slot];
        int quantity = Math.max(1, Math.min(SkillsDialogue.getQuantity(player), player.getInventory().getAmountOf(singingRecipes.getRequiredItems()[singingRecipes.getRequiredItems().length-1].getId())));
    
        for (int i2 = 0; i2 < quantity; i2++) {
            if (singingRecipes != null) {
                String err = null;

                if (!singingRecipes.hasItems(player)) {
                    StringBuilder sb = new StringBuilder();
                    for (Item i : singingRecipes.getRequiredItems())
                        sb.append(i.getAmount() + " x " + i.getName() + ", ");
                    err = "To create the " + singingRecipes.getProduct().getName() + " you need: <br>" + sb.toString().substring(0, sb.length() - 2) + ".";
                }
                if (player.getSkills().getLevel(Skills.SMITHING) < singingRecipes.getLvl()) {
                    err = "You must have a Smithing level of " + singingRecipes.getLvl() + " to sing that.";
                }
                if (player.getSkills().getLevel(Skills.CRAFTING) < singingRecipes.getLvl()) {
                    err = "You must have a Crafting level of " + singingRecipes.getLvl() + " to sing that.";
                }
               
                if (err != null) {
                    end();
                    player.getDialogueManager().startDialogue("ItemMessage", err, singingRecipes.getProduct().getId());
                    return;
                }

                player.getInventory().removeItems(singingRecipes.getRequiredItems());
                player.getInventory().addItem(singingRecipes.getProduct());

                player.getSkills().addXp(Skills.SMITHING, singingRecipes.getXp());
                player.getSkills().addXp(Skills.CRAFTING, singingRecipes.getXp());

                if (i2 == 0) {
                    player.setNextAnimation(new Animation(3645));
                    player.lock(3);
                    end();
                	player.getDialogueManager().startDialogue("ItemMessage", "With the help of the crystal bowl, you sing a beautiful song and shape the crystals.", singingRecipes.getProduct().getId());
                }
            }
        }
    }

    @Override
    public void finish() {
    }

    enum SingingRecipes {
        HELM(new Item[]{new Item(CRYSTAL_SHARD, 50), new Item(CRYSTAL_ARMOUR_SEED, 1)},
                new Item(CRYSTAL_HELM), 70, 2500),
        LEGS(new Item[]{new Item(CRYSTAL_SHARD, 100), new Item(CRYSTAL_ARMOUR_SEED, 2)},
                new Item(CRYSTAL_LEGS), 72, 7500),
        BODY(new Item[]{new Item(CRYSTAL_SHARD, 150), new Item(CRYSTAL_ARMOUR_SEED, 3)},
                new Item(CRYSTAL_BODY), 74, 5000),
        AXE(new Item[]{new Item(CRYSTAL_SHARD, 120), new Item(CRYSTAL_TOOL_SEED), new Item(6739)},
                new Item(CRYSTAL_AXE), 76, 6000),
        PICKAXE(new Item[]{new Item(CRYSTAL_SHARD, 120), new Item(CRYSTAL_TOOL_SEED), new Item(15259)},
                new Item(CRYSTAL_PICKAXE), 76, 6000),
        HARPOON(new Item[]{new Item(CRYSTAL_SHARD, 120), new Item(CRYSTAL_TOOL_SEED), new Item(51028)},
                new Item(CRYSTAL_HARPOON), 76, 6000),
        HALBRED(new Item[]{new Item(CRYSTAL_SHARD, 40), new Item(CRYSTAL_WEAPON_SEED)},
                new Item(CRYSTAL_HALBRED), 76, 2000),
        SHIELD(new Item[]{new Item(CRYSTAL_SHARD, 40),  new Item(CRYSTAL_WEAPON_SEED)},
                new Item(CRYSTAL_SHIELD), 78, 0),//,/ n room on interface *
        BOW(new Item[]{new Item(CRYSTAL_SHARD, 40), new Item(CRYSTAL_WEAPON_SEED)},
                new Item(CRYSTAL_BOW), 78, 0),
        KEY(new Item[]{new Item(CRYSTAL_SHARD, 10), new Item(989, 1)},
                new Item(ENHANCED_CRYSTAL_KEY), 80, 0);
    /*    BLADE(new Item[]{new Item(CRYSTAL_ARMOUR_SEED, 2), new Item(CRYSTAL_SHARD, 1000)},
                new Item(BLADE_OF_SAELDOR), 82, 0);*/

        public Item[] getRequiredItems() {
            return requiredItems;
        }

        public Item getProduct() {
            return product;
        }

        public int getXp() {
            return xp;
        }

        public int getLvl() {
            return lvl;
        }

        public boolean hasItems(Player player) {
            return Arrays.stream(getRequiredItems())
                    .filter(item -> !player.getInventory().containsItem(item)).count() == 0;
        }

        Item[] requiredItems;
        Item product;
        int xp;

        int lvl;

        SingingRecipes(Item[] requiredItems, Item product, int lvl, int xp) {
            this.requiredItems = requiredItems;
            this.product = product;
            this.lvl = lvl;
            this.xp = xp;
        }
    }
}
