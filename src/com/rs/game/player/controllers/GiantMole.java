package com.rs.game.player.controllers;

import java.util.List;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

public class GiantMole extends Controller {

	private int hintIcon;
	@Override
	public void start() {
		player.setNextWorldTile(new WorldTile(1752, 5137, 0));
		player.lock(1);
		player.getPackets()
				.sendGameMessage("You seem to have dropped down into a network of mole tunnels.");
	}
	
	@Override
	public void process() {
		NPC mole = null;
		look:{ //lazy code
			List<Integer> npcIndexes = World.getRegion(6992).getNPCsIndexes();
			if (npcIndexes != null) {
				for (Integer index : npcIndexes)
					if (index != null) {
						NPC npc = World.getNPCs().get(index);
						if (npc != null && npc.getId() == 3340) {
							mole =  npc;
							break look;
						}
					}
			}
			npcIndexes = World.getRegion(6993).getNPCsIndexes();
			if (npcIndexes != null) {
				for (Integer index : npcIndexes)
					if (index != null) {
						NPC npc = World.getNPCs().get(index);
						if (npc != null && npc.getId() == 3340) {
							mole =  npc;
							break look;
						}
					}
			}
		}
		if ((mole == null || mole.isDead() || mole.hasFinished())) {
			if (hintIcon > 0) {
				player.getHintIconsManager().removeUnsavedHintIcon();
				hintIcon = 0;
			}
		} else {
			if (hintIcon != 1 && mole.withinDistance(player)) {
				player.getHintIconsManager().addHintIcon(mole, 0, -1, false);
				hintIcon = 1;
			} else if (hintIcon != 2 && !mole.withinDistance(player)) {
				player.getHintIconsManager().addHintIcon(mole.getX(), mole.getY(), 0, 150, 2, 0, -1, false);
				hintIcon = 2;
			}
		}
	}

	public void removeIcon() {
		if (hintIcon > 0) {
			player.getHintIconsManager().removeUnsavedHintIcon();
			hintIcon = 0;
		}
	}
	
	@Override
	public boolean processObjectClick1(WorldObject object) {
		return true;
	}

	@Override
	public void magicTeleported(int type) {
		removeIcon();
		removeControler();
	}

	@Override
	public boolean sendDeath() {
		removeIcon();
		removeControler();
		return true;
	}

	@Override
	public boolean login() {
		start();
		return false; // so doesnt remove script
	}

	@Override
	public boolean logout() {
		return false; // so doesnt remove script
	}
	
	@Override
	public void forceClose() {
		removeIcon();
	}

}
