package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.Wilderness;

public class HomeTeleport extends Action {

	private final int HOME_ANIMATION = 16385, HOME_GRAPHIC = 3017;
	public static final WorldTile LUMBRIDGE_LODE_STONE = new WorldTile(3233, 3221, 0), BURTHORPE_LODE_STONE = new WorldTile(2899, 3544, 0), LUNAR_ISLE_LODE_STONE = new WorldTile(2085, 3914, 0),
			BANDIT_CAMP_LODE_STONE = new WorldTile(3214, 2954, 0), TAVERLY_LODE_STONE = new WorldTile(2878, 3442, 0), ALKARID_LODE_STONE = new WorldTile(3297, 3184, 0),
			VARROCK_LODE_STONE = new WorldTile(3214, 3376, 0), EDGEVILLE_LODE_STONE = new WorldTile(3067, 3505, 0), FALADOR_LODE_STONE = new WorldTile(2967, 3403, 0),
			PORT_SARIM_LODE_STONE = new WorldTile(3011, 3215, 0), DRAYNOR_VILLAGE_LODE_STONE = new WorldTile(3105, 3298, 0), ARDOUGNE_LODE_STONE = new WorldTile(2634, 3348, 0),
			CATHERBAY_LODE_STONE = new WorldTile(2831, 3451, 0), YANILLE_LODE_STONE = new WorldTile(2529, 3094, 0), SEERS_VILLAGE_LODE_STONE = new WorldTile(2689, 3482, 0)
			, DONATOR_ZONE = new WorldTile(3367, 5220, 0), HOME_LODE_STONE = new WorldTile(2143, 3541, 0),
			VIP_ZONE = new WorldTile(3710, 5563, 0);

	private int currentTime;
	private WorldTile tile;

	public HomeTeleport(WorldTile tile) {
		this.tile = tile;
	}
	
	public static void useLodestone(Player player, int componentId) {
		player.stopAll();
		WorldTile destTile = null;
		switch (componentId) {
		case 47:
			destTile = HomeTeleport.LUMBRIDGE_LODE_STONE;
			break;
		case 42:
			destTile = HomeTeleport.BURTHORPE_LODE_STONE;
			break;
		case 39:
			destTile = HomeTeleport.LUNAR_ISLE_LODE_STONE;
			break;
		case 7:
			destTile = HomeTeleport.BANDIT_CAMP_LODE_STONE;
			break;
		case 50:
			destTile = HomeTeleport.TAVERLY_LODE_STONE;
			break;
		case 40:
			destTile = HomeTeleport.ALKARID_LODE_STONE;
			break;
		case 51:
			destTile = HomeTeleport.VARROCK_LODE_STONE;
			break;
		case 45:
			destTile = HomeTeleport.EDGEVILLE_LODE_STONE;
			break;
		case 46:
			destTile = HomeTeleport.FALADOR_LODE_STONE;
			break;
		case 48:
			destTile = HomeTeleport.PORT_SARIM_LODE_STONE;
			break;
		case 44:
			destTile = HomeTeleport.DRAYNOR_VILLAGE_LODE_STONE;
			break;
		case 41:
			destTile = HomeTeleport.ARDOUGNE_LODE_STONE;
			break;
		case 43:
			destTile = HomeTeleport.CATHERBAY_LODE_STONE;
			break;
		case 52:
			destTile = HomeTeleport.YANILLE_LODE_STONE;
			break;
		case 49:
			destTile = HomeTeleport.SEERS_VILLAGE_LODE_STONE;
			break;
		case -2:
			destTile = HomeTeleport.HOME_LODE_STONE;
			break;
		case -3:
			destTile = HomeTeleport.DONATOR_ZONE;
			break;
		}
		if (destTile != null) {
			player.setPreviousLodestone(componentId);
			player.getActionManager().setAction(new HomeTeleport(destTile));
		}
	}

	@Override
	public boolean start(final Player player) {
		return process(player);
	}

	@Override
	public int processWithDelay(Player player) {
		if (tile == HOME_LODE_STONE) {
			Magic.sendCommandTeleportSpell(player, tile);
			return -1;
		}
		if (player.isDonator()) {
			if (!player.isLocked())
				Magic.sendTeleportSpell(player, 13493, 13494, 2437, 2435, 0, 0, tile, 6, false, 0);
			return -1;
		}
		if (currentTime++ == 0) {
			player.setNextAnimation(new Animation(HOME_ANIMATION));
			player.setNextGraphics(new Graphics(HOME_GRAPHIC));
		} else if (currentTime == 18) {
			player.setNextWorldTile(tile.transform(0, 1, 0));
			player.getControlerManager().magicTeleported(Magic.MAGIC_TELEPORT);
			if (player.getControlerManager().getControler() == null)
				Magic.teleControlersCheck(player, tile);
			player.setNextFaceWorldTile(new WorldTile(tile.getX(), tile.getY(), tile.getPlane()));
			player.setDirection(6);
			if (player.getControlerManager().getControler() instanceof DungeonController)
				return -1;
			player.lock(11);
		} else if (currentTime == 19) {
			player.setNextGraphics(new Graphics(HOME_GRAPHIC + 1));
			player.setNextAnimation(new Animation(HOME_ANIMATION + 1));
		} else if (currentTime == 23) {
			player.setNextGraphics(new Graphics(3018));
			player.setNextAnimation(new Animation(16386));
		} else if (currentTime == 24)
			player.setNextAnimation(new Animation(16393));
		else if (currentTime == 27) {
			player.setNextAnimation(new Animation(-1));
			player.setNextWorldTile(tile);
		} else if (currentTime == 28) {
			return -1;
		}
		return 0;
	}

	@Override
	public boolean process(Player player) {
		int delay = 10000;
		if (player.getControlerManager().getControler() instanceof DungeonController
				|| ((tile == VIP_ZONE || tile == DONATOR_ZONE || tile == HOME_LODE_STONE) && !(player.getControlerManager().getControler() instanceof Wilderness)))
			delay = 0;
		if (delay != 0 && player.isUnderCombat()) {
			player.getPackets().sendGameMessage("You can't home teleport shortly after the end of combat.");
			return false;
		}
		return player.getControlerManager().processMagicTeleport(tile);
	}

	@Override
	public void stop(Player player) {
		player.setNextAnimation(new Animation(-1));
		player.setNextGraphics(new Graphics(-1));
	}
}
