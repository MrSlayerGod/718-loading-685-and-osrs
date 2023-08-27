package com.rs.game.content.reward;

import com.rs.game.content.reward.collection.RewardCollection;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a reward table with collections
 */
public class RewardTable {

    private List<RewardCollection> rewardCollections = new ArrayList<>();

    public List<Item> generateReward(Player player, int rolls, double percentageBoost) {
        var sortedList = this.getSortedList(player);
        var items = new ArrayList<Item>();
        for (int i = 0; i < rolls; i++) {
            for (var collection : sortedList) {
                var rolledItems = roll(player, collection, percentageBoost);
                items.addAll(rolledItems);
            }
        }
        return items;
    }

    private List<Item> roll(Player player, RewardCollection collection, double percentageBoost) {
        var items = new ArrayList<Item>();
        final var newCalculatedPercentage = Utils.increaseByPercent(collection.getPercentage(player), percentageBoost);
        final var randomPercent = Utils.randomDouble() * 100;

        if (randomPercent <= newCalculatedPercentage) {
            var rewards = collection.generateRewards(player);

            for (var rewardItem : rewards) {
                var randomItem = rewardItem.getRewardItem();
                rewardItem.getOnReceive().accept(player, randomItem);
                items.add(randomItem);
            }

            // If the collection's roll type is default, we want to break since we have a reward already.
            if (collection.getRollType() == RewardRollType.DEFAULT) {
                return items;
            }
        }
        return items;
    }


    public List<Item> generateReward(Player player) {
        return generateReward(player, 1, 0);
    }

    public List<Item> generateReward(Player player, int rolls) {
        return generateReward(player, rolls, 0);
    }


    /**
     * This sorts the reward table by {@link RewardRollType#ALWAYS} first and percentage second
     */
    public List<RewardCollection> getSortedList(Player player) {

        var sortedList = new ArrayList<RewardCollection>();
        var alwaysEntries = rewardCollections.stream()
                .filter(e -> e.getRollType() == RewardRollType.ALWAYS)
                .toList();

        var otherEntries = rewardCollections.stream()
                .filter(e -> e.getRollType() == RewardRollType.DEFAULT)
                .sorted(Comparator.comparingDouble(e -> e.getPercentage(player)))
                .toList();

        sortedList.addAll(alwaysEntries);
        sortedList.addAll(otherEntries);

        return sortedList;
    }

    public List<RewardCollection> getRewardCollections() {
        return rewardCollections;
    }

}
