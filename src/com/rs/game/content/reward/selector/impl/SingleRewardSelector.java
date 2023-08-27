package com.rs.game.content.reward.selector.impl;

import com.rs.game.content.reward.collection.RewardCollection;
import com.rs.game.content.reward.collection.RewardItem;
import com.rs.game.content.reward.selector.IRewardSelector;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SingleRewardSelector implements IRewardSelector {

    @Override
    public List<RewardItem> select(Player player, RewardCollection collection) {

        // Here we get all the rewards a player is capable of receiving
        var potentialRewards = new ArrayList<RewardItem>();
        for (var potentialItem : collection.getAllRewards()) {
            if (potentialItem.getCriteria().test(player)) {
                potentialRewards.add(potentialItem);
            }
        }
        RewardItem reward = Utils.randomFrom(potentialRewards);
        return new ArrayList<RewardItem>(Arrays.asList(reward));
    }

}
