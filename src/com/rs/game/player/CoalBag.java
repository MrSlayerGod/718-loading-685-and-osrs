package com.rs.game.player;

import com.rs.game.item.Item;

import java.io.Serializable;

/**
 * @author Simplex
 * @since Apr 29, 2020
 */
public class CoalBag implements Serializable {

    private static final long serialVersionUID = -5319761533891220201L;

    public static final int OPENED_COAL_BAG_ID = 54480;
    public static final int COAL_BAG_ID = 18339;
    public static final int COAL_ID = 453;

    private static final int MAX_COAL_STORAGE = 27;

    private int coalStored = 0;

    private transient Player player;

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void openBag() {
        int slot = player.getInventory().getItems().getThisItemSlot(COAL_BAG_ID);
        if(slot > -1) {
            player.getInventory().replaceItem(OPENED_COAL_BAG_ID, 1, slot);
            player.sendMessage("You open the Coal bag. Coal will automatically be added while mining.");
        }
    }

    public void closeBag() {
        int slot = player.getInventory().getItems().getThisItemSlot(OPENED_COAL_BAG_ID);
        if(slot > -1) {
            player.getInventory().replaceItem(COAL_BAG_ID, 1, slot);
            player.sendMessage("You close the Coal bag.");
        }
    }

    public void fill() {
        if(MAX_COAL_STORAGE == coalStored) {
            player.sendMessage("Your Coal bag is full.");
            return;
        }

        int add = player.getInventory().getAmountOf(COAL_ID);

        if(add == 0) {
            player.sendMessage("You do not have any coal to store.");
            return;
        }

        if(coalStored + add > MAX_COAL_STORAGE)
            add = MAX_COAL_STORAGE - coalStored;

        player.sendMessage("You add " + add + " " + (add == 1 ? "piece" : "pieces") + " of coal to your Coal bag.");
        coalStored += add;
        player.getInventory().deleteItem(COAL_ID, add);
    }

    public void check() {
        player.sendMessage("The bag is holding " + coalStored + " coal.");
    }

    public void empty() {
        int space = player.getInventory().getFreeSlots();

        if (space == 0) {
            player.sendMessage("Your inventory is full.");
            return;
        }

        if(coalStored == 0) {
            player.sendMessage("There is no coal in the bag.");
            return;
        }

       int remove = space >= coalStored ? coalStored : space;
       coalStored -= remove;
       player.getInventory().addItem(COAL_ID, remove);
       player.sendMessage("You remove " + remove + " coal.");
    }

    public void add(int i) {
        coalStored += i;

        if(coalStored > MAX_COAL_STORAGE)
            coalStored = MAX_COAL_STORAGE;
    }

    public boolean isFull() {
        return coalStored >= MAX_COAL_STORAGE;
    }

    public int intercept(Player player, Item item, int amount) {
        int intercepted = Math.min(MAX_COAL_STORAGE - coalStored, amount);
        player.getCoalBag().coalStored += intercepted;
        if(intercepted > 0)
            player.sendMessage("You withdraw " + intercepted + " coal directly to your coal bag.");
        return intercepted;
    }

    public int smithingIntercept(Player player, int amount) {
        int coalWithdrawn = coalStored >= amount ? amount : coalStored;
        coalStored -= coalWithdrawn;
        player.sendMessage("You take " + coalWithdrawn + " coal from your coal bag.");
        return coalWithdrawn;
    }

    public int getCoal() {
        return coalStored;
    }
}
