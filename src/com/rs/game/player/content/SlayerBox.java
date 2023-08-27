package com.rs.game.player.content;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.impl.superiorslayer.GreaterAbyssalDemon;
import com.rs.game.player.Player;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.utils.Utils;

import java.util.Arrays;

/**
 * @author Simplex
 * @since Sep 15, 2020
 */
public class SlayerBox {
    public static void init() {
        Arrays.stream(Boxes.values()).forEach(box -> {
            InventoryOptionsHandler.register(box.id, 1, (box::open));
        });
    }

    public static boolean roll(Player killer, NPC npc) {
        int npcCbLvl = npc.getCombatLevel();

        if(Utils.random(25) != 1)
            return false;
        Boxes box;
        if(npcCbLvl < 51) {
            box = Boxes.SMALL;
        } else if(npcCbLvl < 120) {
            box = Boxes.MED;
        } else if(npcCbLvl < 300){
            box = Boxes.LARGE;
        } else {
        	box = Boxes.BOSS;
        }

        if(npc instanceof GreaterAbyssalDemon) {
            box = Boxes.LARGE;
        }

        box.drop(killer, npc);
        return true;
    }

    public enum Boxes {
        SMALL(27000, new Item(995, 325_000), "99ff66"),
        MED(27001, new Item(995, 1_000_000), "ffd952"),
        LARGE(27002, new Item(995, 1_750_000), "800000"),
        BOSS(27003, new Item(995, 3_250_000), "f3003f");

        Boxes(int id, Item loot, String clr) {
            this.id = id;
            this.loot = loot;
            this.clr = clr;
        }

        /*
         * Random amount 30-100% of amount
         */
        public Item get() {
            return loot.clone().setAmount((int) Utils.random((double)loot.getAmount()/3d, loot.getAmount()));
        }

        public void open(Player player, Item item) {
            Item loot = get();
            player.getInventory().deleteItem(item);
            player.getDialogueManager().startDialogue("ItemMessage",
                    "Inside the " + item.getName() + " you find <br>" +
                    "<shad="+getColor()+">" + loot.amtAndName() +  "</shad>!", id);
            if(loot.getId() == 995)
                player.getInventory().addItemMoneyPouch(loot);
            else player.getInventory().addItem(item);
        }

        public void drop(Player player, NPC npc) {
            Item box = new Item(id);
            player.sendMessage("<col=" + clr +"><shad=0>A " + box.getName() + " dropped on the floor!");
            npc.sendDrop(player, new Drop(box.getId(), box.getAmount(), box.getAmount()));
            //World.addGroundItem(box, new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()), player, true, 60);
        }

        private String getColor() {
            return clr;
        }

        int id; Item loot;
        String clr;

    }
}
