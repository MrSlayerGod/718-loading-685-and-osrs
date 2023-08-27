/**
 * 
 */
package com.rs.game.player.content.seasonalEvents;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.minigames.lms.LastManStandingGame;
import com.rs.game.minigames.lms.LastManStandingLobby;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.player.Player;
import com.rs.game.player.actions.PlayerFollow;
import com.rs.game.player.actions.Rest;
import com.rs.game.player.actions.SitRoundTable;
import com.rs.game.player.actions.ViewingOrb;
import com.rs.game.player.actions.construction.SitChair;
import com.rs.game.player.actions.woodcutting.DreamTreeWoodcutting;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.pktournament.PkTournamentGame;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Apr 3, 2018
 */
public class Easter2018 {

	public static void process(Player player) {
		Controller c = player.getControlerManager().getControler();
		if (!player.getActionManager().hasAction()
				|| (c != null && (PkTournament.inMinigame(player) || c instanceof LastManStandingLobby || c instanceof LastManStandingGame))
				|| (player.getActionManager().getAction() instanceof Rest)
				|| (player.getActionManager().getAction() instanceof DreamTreeWoodcutting)
				|| (player.getActionManager().getAction() instanceof ViewingOrb)
				|| (player.getActionManager().getAction() instanceof PlayerFollow)
				|| (player.getActionManager().getAction() instanceof SitChair)
				|| (player.getActionManager().getAction() instanceof SitRoundTable)
				|| !player.isActive(120000)
				|| Utils.random(1500 * 2) != 0) //1500x2
			return;
		player.setLootbeam(World.addGroundItem(new Item(1961), new WorldTile(player), player, true, 60));
	}
}
