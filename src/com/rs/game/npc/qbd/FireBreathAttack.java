package com.rs.game.npc.qbd;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.DragonfireShield;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * Represents a default fire breath attack.
 * 
 * @author Emperor
 * 
 */
public final class FireBreathAttack implements QueenAttack {

	/**
	 * The animation of the attack.
	 */
	private static final Animation ANIMATION = new Animation(16721);

	/**
	 * The graphic of the attack.
	 */
	private static final Graphics GRAPHIC = new Graphics(3143);

	@Override
	public int attack(final QueenBlackDragon npc, final Player victim) {
		npc.setNextAnimation(ANIMATION);
		npc.setNextGraphics(GRAPHIC);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				super.stop();
				String message = getProtectMessage(victim);
				int hit;
				if (message != null) {  //nerfed dmg by half
					//hit = Utils.random(0, message.contains("prayer") ? 460 : 235);
					hit = (Utils.random(150) + Utils.random((int) (720 * Combat.getDragonFireMultiplier(victim))));
					victim.getPackets().sendGameMessage(message, true);
					DragonfireShield.chargeDFS(victim, true);
				} else {
					hit = Utils.random(400, 710);
					victim.getPackets().sendGameMessage("You are horribly burned by the dragon's breath!", true);
				}
				victim.setNextAnimation(new Animation(Combat.getDefenceEmote(victim)));
				victim.applyHit(new Hit(npc, hit, HitLook.REGULAR_DAMAGE));
			}
		}, 1);
		return Utils.random(4, 15); // Attack delay seems to be random a lot.
	}

	@Override
	public boolean canAttack(QueenBlackDragon npc, Player victim) {
		return true;
	}

	/**
	 * Gets the dragonfire protect message.
	 * 
	 * @param player
	 *            The player.
	 * @return The message to send, or {@code null} if the player was
	 *         unprotected.
	 */
	public static final String getProtectMessage(Player player) {
		if (Combat.hasAntiDragProtection(player)) {
			return "Your shield absorbs most of the dragon's breath!";
		}
		if (player.hasFireImmunity()) {
			return "Your potion absorbs most of the dragon's breath!";
		}
		if (player.getPrayer().isMageProtecting()) {
			return "Your prayer absorbs most of the dragon's breath!";
		}
		return null;
	}
}