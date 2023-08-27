package com.rs.game.content.reward.collection;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a single reward item, immutable
 */
public final class RewardItem {

    private final int id;
    private final int minAmount;
    private final int maxAmount;

    /**
     * What happens when a player receives this item
     */
    private BiConsumer<Player, Item> onReceive = (emptyPlayer, emptyItem) -> {};

    /**
     * Checks if a player can receive this reward or not
     */
    private Predicate<Player> criteria = player -> true;

    public RewardItem(int id, int minAmount, int maxAmount) {
        this.id = id;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public RewardItem(int id, int amount) {
        this.id = id;
        this.minAmount = amount;
        this.maxAmount = amount;
    }

    public RewardItem(Item item) {
        this.id = item.getId();
        this.minAmount = item.getAmount();
        this.maxAmount = item.getAmount();
    }

    public RewardItem withCriteria(Predicate<Player> criteria) {
        this.criteria = criteria;
        return this;
    }

    public RewardItem withOnReceive(BiConsumer<Player, Item> onReceive) {
        this.onReceive = onReceive;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    /**
     * This method will return a random reward item
     * @return
     */
    public Item getRewardItem() {
        return new Item(id, Utils.random(minAmount, maxAmount));
    }

    public Predicate<Player> getCriteria() {
        return criteria;
    }

    public BiConsumer<Player, Item> getOnReceive() {
        return onReceive;
    }
}
