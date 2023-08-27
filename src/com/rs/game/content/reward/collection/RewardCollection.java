package com.rs.game.content.reward.collection;

import com.rs.game.content.reward.RewardRollType;
import com.rs.game.content.reward.selector.IRewardSelector;
import com.rs.game.item.Item;
import com.rs.game.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RewardCollection {

    /**
     * What percentage will this reward collection be rolled
     */
    private final double initialPercentage;

    private final RewardRollType rollType;

    /**
     * Which selector we should be using
     */
    private IRewardSelector selector = IRewardSelector.SINGLE_REWARD_SELECTOR;

    /**
     * Used to predict a user based percentage
     */
    private Function<Player, Double> dynamicPercentage;

    public RewardCollection(double initialPercentage, RewardRollType rollType, IRewardSelector selector) {
        this.initialPercentage = initialPercentage;
        this.dynamicPercentage = p -> initialPercentage;
        this.rollType = rollType;
        this.selector = selector;
    }

    public final RewardCollection withSelector(IRewardSelector selector) {
        this.selector = selector;
        return this;
    }

    public final RewardCollection withDynamicPercentage(Function<Player, Double> dynamicPercentage) {
        this.dynamicPercentage = dynamicPercentage;
        return this;
    }

    public RewardCollection(double initialPercentage, RewardRollType rollType) {
        this.initialPercentage = initialPercentage;
        this.rollType = rollType;
    }

    private final List<RewardItem> allRewards = new ArrayList<>();

    public void addReward(RewardItem item) {
        this.allRewards.add(item);
    }

    public List<RewardItem> getAllRewards() {
        return allRewards;
    }

    /**
     * Will generate rewards
     * @return
     */
    public List<RewardItem> generateRewards(Player player) {
        var selection = selector.select(player, this)
                .stream()
                .toList();
        return selection;
    }


    /**
     * Returns the percentage for a player
     * @param player - the player to calculate a percentage for
     * @return a percentage like 10.0 for 10%
     */
    public double getPercentage(Player player) {
        if (dynamicPercentage == null)
            dynamicPercentage = p -> this.initialPercentage;
        return dynamicPercentage.apply(player);
    }

    public double getInitialPercentage() {
        return initialPercentage;
    }

    public RewardRollType getRollType() {
        return rollType;
    }




}
