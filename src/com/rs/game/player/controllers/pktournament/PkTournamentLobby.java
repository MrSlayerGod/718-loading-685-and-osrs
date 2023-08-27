package com.rs.game.player.controllers.pktournament;

import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.npc.NPC;
import com.rs.game.player.controllers.Controller;
import com.rs.utils.Utils;

public class PkTournamentLobby extends Controller {

	@Override
	public void start() {
		sendInterfaces();
	}

	@Override
	public boolean processNPCClick2(NPC n) {
		return true;
	}

	@Override
	public boolean canRemoveEquip(int slot, int item) {
		return true;
	}

	@Override
	public boolean canAttack(Entity target) {
		return canHit(target);
	}

	@Override
	public boolean canDropItem(Item item) {
		player.sendMessage("You cannot drop items in PK Tournaments.");
		return false;
	}


	public void leave() {
		player.getInterfaceManager().removeOverlay(false);
		PkTournament.handleRemoval(player);
	}

	@Override
	public void sendInterfaces() {
		player.getPackets().sendIComponentText(1194, 7, "");
		player.getPackets().sendIComponentText(1194, 6, "");
		player.getPackets().sendIComponentText(1194, 8, "");
		player.getInterfaceManager().setOverlay(1194, false);
	}

	@Override
	public void process() {
		player.getPackets().sendIComponentText(1194, 8,
				PkTournament.canJoin ? "" + Utils.formatTime2(PkTournament.joinMS - Utils.currentTimeMillis())
						: PkTournament.intermission ? "<col=FFFFFF>Intermission  <col=FFC400>" + Utils.formatTime2(PkTournament.intermissionMS - Utils.currentTimeMillis())
						: "- Round " + (PkTournament.round + 1) + " -");
	}


	@Override
	public boolean logout() {
		player.setLocation(new WorldTile(3086, 3498, 0));
		PkTournament.handleRemoval(player);
		return true;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
	//	player.getDialogueManager().startDialogue("SimpleMessage", "Speak to " + NPCConfig.forID(PkTournament.NPC_ID).getName() + " to exit the tournament.");
		return false;
	}
	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		switch (interfaceId) {
			case 182:
				player.sendMessage("You can not logout during a pvp tournament!");
				return false;
			case 271:
			case 749:
				if(componentId == 4 || (componentId == 8 && slotId >= 16 && slotId <= 18)) {
					if(PkTournament.getType().prayerDisabled()) {
						player.sendMessage("Protection prayers are disabled in " + PkTournament.getType().getFormattedName() + " PK Tournaments!");
						return false;
					}
				}
				break;
		}


		return true;
	}
	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
	//	player.getDialogueManager().startDialogue("SimpleMessage", "Speak to " + NPCConfig.forID(PkTournament.NPC_ID).getName() + " to exit the tournament.");
		return false;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
		//player.getDialogueManager().startDialogue("SimpleMessage", "Speak to " + NPCConfig.forID(PkTournament.NPC_ID).getName() + " to exit the tournament.");
		return false;
	}

	@Override
	public boolean sendDeath() {
		PkTournament.getType().setup(player);
		return true;
	}
	
	
	public boolean processObjectClick1(WorldObject object) {
		int id = object.getId();
		if (id == 130397) {
			leave();
			//player.sendMessage("You can not leave the fight! Kill your opponent or die trying!");
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick2(WorldObject object) {
		int id = object.getId();
		return true;
	}

	@Override
	public void magicTeleported(int type) {
		removeControler();
		leave();
	}

	@Override
	public void forceClose() {
		leave();
	}
}
