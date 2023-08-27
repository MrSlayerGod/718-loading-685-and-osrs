package com.rs.game.content.reward;

import com.rs.game.content.reward.collection.RewardCollection;
import com.rs.game.content.reward.collection.RewardItem;
import com.rs.game.player.Player;

import java.util.function.Function;

public class RewardTableBuilder {

    private RewardTable table;

    public RewardTableBuilder() {
        this.table = new RewardTable();
    }

    public RewardTableBuilder addBasicCollection(double percentage, RewardRollType rollType, RewardItem... items) {
        var collection = new RewardCollection(percentage, rollType);
        for (var item : items) {
            collection.addReward(item);
        }

        this.table.getRewardCollections().add(collection);
        return this;
    }

    public RewardTableBuilder addCollection(RewardCollection collection) {
        this.table.getRewardCollections().add(collection);
        return this;
    }

    public RewardTableBuilder addDynamicPercentageCollection(Function<Player, Double> percentage, RewardRollType type, RewardItem... items) {
        var collection = new RewardCollection(0, type)
                .withDynamicPercentage(percentage);
        for (var item : items) {
            collection.addReward(item);
        }

        this.table.getRewardCollections().add(collection);
        return this;
    }


    /**
     * Builds the table
     * @return
     */
    public RewardTable build() {
        return table;
    }


}
