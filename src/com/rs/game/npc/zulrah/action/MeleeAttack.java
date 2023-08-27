/**
 * 
 */
package com.rs.game.npc.zulrah.action;

import com.rs.game.Animation;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.zulrah.Zulrah;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Nov 5, 2017
 */
public class MeleeAttack extends ZulrahAction {

	@Override
	public int use(Zulrah zulrah) {
		Player player = zulrah.getShrine().getPlayer();
		zulrah.setNextAnimation(new Animation(25806));
		zulrah.setNextFaceEntity(player);
		if (Utils.isOnRange(zulrah, player, 2) && zulrah.clipedProjectile(player, false))
			CombatScript.delayHit(zulrah, 0, player, CombatScript.getRegularHit(zulrah, Utils.random(310)+100));
		return 4;
	}

}
