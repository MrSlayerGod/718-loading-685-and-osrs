package com.rs.game.content.reward.selector.impl;


import com.rs.game.content.reward.collection.RewardCollection;
import com.rs.game.content.reward.collection.RewardItem;
import com.rs.game.content.reward.selector.IRewardSelector;
import com.rs.game.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Selects all the rewards in the {{@link RewardCollection#getAllRewards()}}
 */
public class AllRewardSelector implements IRewardSelector {

    @Override
    public List<RewardItem> select(Player player, RewardCollection collection) {

        // Here we get all the rewards a player is capable of receiving
        var potentialRewards = new ArrayList<RewardItem>();
        for (var potentialItem : collection.getAllRewards()) {
            if (potentialItem.getCriteria().test(player)) {
                potentialRewards.add(potentialItem);
            }
        }

        return potentialRewards;
    }

}
