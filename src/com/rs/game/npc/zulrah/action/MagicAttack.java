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
public class MagicAttack extends ZulrahAction {


	@Override
	public int use(Zulrah zulrah) {
		Player player = zulrah.getShrine().getPlayer();
		zulrah.setNextAnimation(new Animation(25068));
		zulrah.setNextFaceEntity(player);
		World.sendProjectile(zulrah, player, 6046, 60, 20, 40, 35, 16, 74);		
		CombatScript.delayHit(zulrah, 2, player, CombatScript.getMagicHit(zulrah, Utils.random(310)+100));
		return 4;
	}

}
