package com.rs.game.player.controllers;

import com.rs.Settings;
import com.rs.game.WorldTile;

public class CallusController extends Controller {

	@Override
	public void start() {
	}

	@Override
	public void process() {
	}
	
	@Override
	public boolean logout() {
		player.setLocation(Settings.START_PLAYER_LOCATION);
        return true;
    }
	
	
	public void removeOverlay() {
		// snow screen
		getPlayer().getInterfaceManager().removeOverlay(true);
		getPlayer().getPackets().sendBlackOut(0);
	}

	@Override
	public boolean sendDeath() {
		removeOverlay();
		removeControler();

		if(player.getRights() == 2) {
			player.reset();
			player.setNextWorldTile(new WorldTile(2399, 4034, 0));
			return false;
		} else
			return super.sendDeath();
	}

	@Override
	public void forceClose() {
		removeOverlay();
		super.forceClose();
	}
	@Override
	public void magicTeleported(int type) {
		removeOverlay();
		removeControler();
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		removeOverlay();
		removeControler();
		return super.processMagicTeleport(toTile);
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		removeOverlay();
		removeControler();
		return super.processMagicTeleport(toTile);
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		removeOverlay();
		removeControler();
		return super.processMagicTeleport(toTile);
	}
}
