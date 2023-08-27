package com.rs.game.player.content;

import com.rs.discord.Bot;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

public class SacrificeAltar implements Serializable {


    @Serial
    private static final long serialVersionUID = -5830472597614494624L;

    private static SacrificeAltar altar;

    private boolean claimedFireCape, claimedTwistedBow;

    public static void init() {
        try {
            altar = (SacrificeAltar) SerializableFilesManager.loadObject("sacrifices.ser");
        } catch (Throwable e) {
           e.printStackTrace();
        }
        if (altar == null)
            altar = new SacrificeAltar();
    }

    public static void save() {
        try {
            SerializableFilesManager.storeObject(altar, "sacrifices.ser");
        } catch (IOException e) {
            Logger.handle(e);
        }
    }

    public static void claimFireCape(Player player) {
        if (altar.claimedFireCape) {
            player.sendMessage("This item has already been sacrificed!");
            return;
        }
        if (player.getDonated() > 0 || player.isDonator()) {
            player.sendMessage("Your not allowed to use donations as advantage on this contest!");
            return;
        }
        if (!player.getInventory().containsItem(6570, 1)) {
            player.sendMessage("You don't have a fire cape!");
            return;
        }
        player.getInventory().deleteItem(6570, 1);
        altar.claimedFireCape = true;
        save();
        World.sendNews("Congratulations to "+player.getDisplayName()+" on being the first to sacrifice Fire Cape, PM Neo for your prize!",0);
        Bot.sendLog(Bot.ACTIVITIES_CHANNEL, "[type=ACTIVITY][name="+player.getUsername()+"][activity=WIN FIRE CAPE]");
    }

    public static void claimTwistedBow(Player player) {
        if (altar.claimedTwistedBow) {
            player.sendMessage("This item has already been sacrificed!");
            return;
        }
        if (player.getDonated() > 0 || player.isDonator()) {
            player.sendMessage("Your not allowed to use donations as advantage on this contest!");
            return;
        }
        if (!player.getInventory().containsItem(50997, 1)) {
            player.sendMessage("You don't have a twisted bow!");
            return;
        }
        player.getInventory().deleteItem(50997, 1);
        altar.claimedTwistedBow = true;
        save();
        World.sendNews("Congratulations to "+player.getDisplayName()+" on being the first to sacrifice Twisted Bow, PM Neo for your prize!",0);
        Bot.sendLog(Bot.ACTIVITIES_CHANNEL, "[type=ACTIVITY][name="+player.getUsername()+"][activity=WIN TWISTED BOW]");

    }
}
