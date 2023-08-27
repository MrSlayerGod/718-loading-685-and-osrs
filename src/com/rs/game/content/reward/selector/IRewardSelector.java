package com.rs.game.content.reward.selector;

import com.rs.game.content.reward.collection.RewardCollection;
import com.rs.game.content.reward.collection.RewardItem;
import com.rs.game.content.reward.selector.impl.AllRewardSelector;
import com.rs.game.content.reward.selector.impl.SingleRewardSelector;
import com.rs.game.player.Player;

import java.util.List;

/**
 * Represents a reward selector interface to select {@link RewardItem} from {@link RewardCollection#getAllRewards()}
 */
public interface IRewardSelector {

    IRewardSelector SINGLE_REWARD_SELECTOR = new SingleRewardSelector();
    IRewardSelector ALL_REWARD_SELECTOR = new AllRewardSelector();

    /**
     * This will select x amount of {@link RewardItem}'s
     * @param player - the player we selecting for
     * @param collection - the collection of items
     * @return
     */
    List<RewardItem> select(Player player, RewardCollection collection);


}
