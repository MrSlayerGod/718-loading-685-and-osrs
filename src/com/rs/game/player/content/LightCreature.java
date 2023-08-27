/**
 * 
 */
package com.rs.game.player.content;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author dragonkk(Alex)
 * Oct 27, 2017
 */
public class LightCreature {

	public  static void teleport(NPC npc, Player player) {
		npc.setForceWalk(player);
		player.getPackets().sendGameMessage("The light creature is attracted to your beam and comes towards you...", true);
		player.lock();
		player.setNextGraphics(new Graphics(1933));
		WorldTasksManager.schedule(new WorldTask() {

			boolean tp;
			@Override
			public void run() {
				if (!tp) {
					player.getPackets().sendResetCamera();
					player.setNextWorldTile(new WorldTile(3226, 9517, 1));
					player.getInterfaceManager().sendInterface(818);
					player.getPackets().sendHideIComponent(818, 0, false);
					player.getAppearence().setRenderEmote(913);
					tp = true;
				} else {
					player.getAppearence().setRenderEmote(-1);
					player.setNextAnimation(new Animation(2047));
					player.closeInterfaces();
					player.setNextWorldTile(new WorldTile(2566, 5738, 0)); //closer
					player.lock(1);
					stop();
				}
				
			}
			
		}, 3, 16);
		
	}
}
