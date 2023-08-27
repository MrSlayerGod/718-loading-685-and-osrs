/**
 * 
 */
package com.rs.game.npc.zulrah.action;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.zulrah.Zulrah;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Nov 5, 2017
 */
public class RangeAttack extends ZulrahAction {

	/* (non-Javadoc)
	 * @see com.rs.game.npc.zulrah.action.ZulrahAction#use(com.rs.game.npc.zulrah.Zulrah)
	 */
	@Override
	public int use(Zulrah zulrah) {
		if (zulrah.isFirstWave())
			return 0;
		Player player = zulrah.getShrine().getPlayer();
		zulrah.setNextAnimation(new Animation(25068));
		zulrah.setNextFaceEntity(player);
		World.sendProjectile(zulrah, player, 6044, 60, 20, 40, 35, 16, 74);		
		CombatScript.delayHit(zulrah, 1, player, CombatScript.getRangeHit(zulrah, Utils.random(310)+100));
		if (Utils.random(3) == 0)
			player.getPoison().makeEnvenomed(60);
		return 4;
	}

}
