package com.rs.game.player.controllers.pktournament;

import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class PkTournamentSpectating extends Controller {

    @Override
    public void start() {
    	
        sendInterfaces();
    }

    public void leave() {
        player.getInterfaceManager().removeOverlay(false);
        PkTournament.handleRemoval(player);
    }

    @Override
    public void sendInterfaces() {
        player.getInterfaceManager().removeOverlay(false);
        //player.getInterfaceManager().setOverlay(809, false);
    }

    @Override
    public boolean canDropItem(Item item) {
        player.sendMessage("You cannot drop items in PK Tournaments.");
        return false;
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
		return false;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
    	player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
		return false;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
    	player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
		return false;
    }
    
    @Override
	public boolean sendDeath() {
		player.lock(8);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
				 	removeControler();
			   		leave();    
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					player.anim(-1);
					player.unlock();
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}
    

    @Override
	public boolean processObjectClick1(WorldObject object) {
		int id = object.getId();
		if (id == 130397 || id == 132755) {
			removeControler();
			leave();
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
		removeControler();
		leave();
    }
}
