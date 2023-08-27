package com.rs.game.player.actions;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;

public class ViewingOrb extends Action {

	private WorldTile tile;
	private WorldTile[] tps;

	public ViewingOrb(WorldTile[] tps) {
		this.tps = tps;
	}

	public WorldTile[] getTps() {
		return tps;
	}

	@Override
	public boolean start(Player player) {
		if (!process(player))
			return false;
		tile = new WorldTile(player);
		player.getEmotesManager().setNextEmoteEnd(Integer.MAX_VALUE);
		player.getAppearence().switchHidden();
		player.getPackets().sendBlackOut(5);
		player.setNextWorldTile(tps[0]);
		player.getInterfaceManager().sendInventoryInterface(374);
		return true;
	}

	@Override
	public boolean process(Player player) {
		if (player.getPoison().isPoisoned()) {
			player.getPackets().sendGameMessage("You can't use orb while you're poisoned.");
			return false;
		}
		if (player.getFamiliar() != null) {
			player.getPackets().sendGameMessage("You can't use orb with a familiar.");
			return false;
		}
		if (player.getPet() != null) {
			player.getPackets().sendGameMessage("You can't use orb with a pet.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		return 0;
	}

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 3);
		player.getEmotesManager().setNextEmoteEnd(1200);
		player.getInterfaceManager().removeInventoryInterface();
		player.getAppearence().switchHidden();
		player.getPackets().sendBlackOut(0);
		player.setNextWorldTile(tile); // this probably causes cft.
	}
}
