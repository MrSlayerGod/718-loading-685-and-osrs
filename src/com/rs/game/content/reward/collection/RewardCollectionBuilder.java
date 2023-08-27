package com.rs.game.content.reward.collection;

import com.rs.game.content.reward.RewardRollType;
import com.rs.game.content.reward.selector.IRewardSelector;
import com.rs.game.item.Item;
import com.rs.game.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RewardCollectionBuilder {

    private RewardRollType type = RewardRollType.DEFAULT;
    private Function<Player, Double> percentage;
    private List<RewardItem> items = new ArrayList<>();

    private BiConsumer<Player, Item> onReceive;

    private Predicate<Player> playerPredicate;

    private IRewardSelector selector = IRewardSelector.SINGLE_REWARD_SELECTOR;

    public RewardCollectionBuilder(RewardRollType type, double initialPercentage) {
        this.type = type;
        this.percentage = p -> initialPercentage;
    }

    public RewardCollectionBuilder withDynamicPercentage(Function<Player, Double> percentage) {
        this.percentage = percentage;
        return this;
    }

    public RewardCollectionBuilder withRewardItems(RewardItem... items) {
        this.items.addAll(Arrays.asList(items));
        return this;
    }

    public RewardCollectionBuilder withSelector(IRewardSelector selector) {
        this.selector = selector;
        return this;
    }

    public RewardCollectionBuilder withGlobalReceiveItem(BiConsumer<Player, Item> consumer) {
        this.onReceive = consumer;
        return this;
    }

    public RewardCollectionBuilder withGlobalCriteria(Predicate<Player> playerPredicate ) {
        this.playerPredicate = playerPredicate;
        return this;
    }

    public RewardCollection build() {
        var newCollection = new RewardCollection(0, type, selector)
                .withDynamicPercentage(percentage);

        for(var i : items) {
            var newBuiltReward = new RewardItem(i.getId(), i.getMinAmount(), i.getMaxAmount());
            if (onReceive != null) {
                newBuiltReward.withCriteria(playerPredicate);
            }
            if (onReceive != null) {
                newBuiltReward.withOnReceive(onReceive);
            }
            newCollection.addReward(newBuiltReward);
        }

        return newCollection;
    }

}
