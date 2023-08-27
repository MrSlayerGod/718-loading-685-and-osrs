package com.rs.game.player.content;

import com.rs.discord.Bot;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Deadman {

    public static int LEVEL_DIFFERENCE = 5;

    public static void check(Player player) {
        if (player.isDeadman() && player.getControlerManager().getControler() == null) {
            if (!World.isBank(player)) {
                if (!player.isCanPvp()) {
                    player.setCanPvp(true);
                    player.gfx(2000);
                    setTarget(player);
                }
            } else if (player.isCanPvp()) {
                player.setCanPvp(false);
                player.gfx(2000);
                setTarget(player);
            }
        }
    }

    public static int getIcon(Player player) {
        if (player.getKillingSpree() == 1)
            return 6;
        if (player.getKillingSpree() == 2)
            return 5;
        if (player.getKillingSpree() == 3)
            return 4;
        if (player.getKillingSpree() == 4)
            return 3;
        if (player.getKillingSpree() >= 5)
            return 2;
       return 7;
    }

    public static void dropRandomItem(Player player, Player killer) {
        List<Item> allItems = new LinkedList<>();
        for (Item item : player.getEquipment().getItems().getItems()) {
            if (item != null)
                allItems.add(item);
        }
        for (Item item : player.getInventory().getItems().getItems()) {
            if (item != null)
                allItems.add(item);
        }
        for (Item[] tab : player.getBank().getTabs()) {
            if (tab != null)
                for (Item item : tab)
                    if (item != null)
                        allItems.add(item);
        }
        if (allItems.isEmpty())
            return;
        Item item = allItems.get(Utils.random(allItems.size()));
        delete: {
            if (player.getEquipment().getItems().contains(item)) {
                player.getEquipment().getItems().remove(item);
                player.getEquipment().init();
                break delete;
            }
            if (player.getInventory().getItems().contains(item)) {
                player.getInventory().getItems().remove(item);
                player.getInventory().refresh();
                break delete;
            }
            if (player.getBank().getItem(item.getId()) == item) {
                player.getBank().removeItem(item.getId());
                if (item.getDefinitions().isStackable())
                    item = new Item(item.getNotedId(), item.getAmount());
                break delete;
            }
            return; //couldnt find
        }
        player.getPackets().sendGameMessage("You lost "+item.getName()+", x "+item.getAmount()+"!");
        World.addGroundItem(item, player.getLastWorldTile(), killer, true, 300, 0);
        Bot.sendLog(Bot.KILL_DEATH_CHANNEL, "[type=KILL][name="+killer.getUsername()+"][target="+player.getUsername()+"][deadman=true][item="+item.getName()+" x "+item.getAmount());


    }

    public static void setTarget(Player player) {
        sendInterfaces(player);
    }

    public static boolean isAttackable(Player player, Entity target, boolean message) {
        if (target instanceof NPC)
            return true;
        Player targetP = (Player) target;
        if (!player.isDeadman() || player.getControlerManager().getControler() != null)
            return true;
        int minLevel = player.getSkills().getCombatLevelWithSummoning() - LEVEL_DIFFERENCE;
        int maxLevel = player.getSkills().getCombatLevelWithSummoning() + LEVEL_DIFFERENCE;
        int targetLevel = targetP.getSkills().getCombatLevelWithSummoning();
        boolean attackable = targetLevel >= minLevel && targetLevel <= maxLevel;
        if (!attackable && message)
            player.getPackets().sendGameMessage("The difference between your Combat level and the Combat level of " + targetP.getDisplayName() + " is too great.");
        return attackable;
    }

    public static void login(Player player) {
        if (!player.isDeadman() || player.getControlerManager().getControler() != null)
            return;
        player.gfx(2000);
        check(player);
    }

    public static void sendInterfaces(Player player) {
        if (!player.isDeadman() || player.getControlerManager().getControler() != null)
            return;
        if (!player.getInterfaceManager().containsInterface(591))
            player.getInterfaceManager().setOverlay(591, false);
        player.getPackets().sendIComponentText(591, 8, "None");
        int minLevel = Math.max(3, player.getSkills().getCombatLevelWithSummoning() - LEVEL_DIFFERENCE);
        int maxLevel = Math.min(138, player.getSkills().getCombatLevelWithSummoning() + LEVEL_DIFFERENCE);
        player.getPackets().sendIComponentText(591, 9, !player.isCanPvp() ? "Safe Zone" : ("Attackable: <br>"+minLevel+" - "+maxLevel+" cb"));
        player.getPackets().sendHideIComponent(745, 6, !player.isCanPvp() );
    }

}
