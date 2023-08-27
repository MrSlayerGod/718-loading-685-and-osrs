package com.rs.game.npc.others.zalcano;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.actions.Smelting;
import com.rs.game.player.actions.mining.Mining;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.controllers.Controller;

/**
 * @author Simplex
 * @since May 23, 2020
 */
public class ZalcanoController extends Controller {

	private int timer;
	
    @Override
    public void start() {
    	sendInterfaces();
    }
    
    @Override
    public void process() {
    	timer++;
    	refreshHPBar();
		if (timer % 100 == 0)
			playMusic(); // so that music doesnt get replaced
    }
    

	public void playMusic() {
		player.getMusicsManager().playOSRSMusic("The Spurned Demon");
	}

    public void leave(boolean force) {
        Zalcano.handleRemove(player);
        if(force)
            player.setLocation(new WorldTile(Zalcano.OUTSIDE));
        else if(player.getX() == 3033)
            player.useStairs(-1, new WorldTile(3033, 6063, 0), 1, 1, null, true);
        else
            player.useStairs(-1, new WorldTile(3034, 6063, 0), 1, 1, null, true);

        removeControler();
    }

    @Override
    public boolean logout() {
        leave(true);
        return true;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        return true;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        return true;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        return true;
    }
    @Override
    public boolean sendDeath() {
        Zalcano.handleRemove(player);
        return super.sendDeath();
    }

    @Override
    public boolean processNPCClick1(NPC npc) {
        if(npc.getId() == Zalcano.ZALCANO_ID || npc.getId() == Zalcano.ZALCANO_DOWNED_ID || npc.getId() == Zalcano.GOLEM_ID) {
            Zalcano.attack(player, npc);
            return false;
        }
        return true;
    }
    
	@Override
	public void sendInterfaces() {
		player.getInterfaceManager().setOverlay(1285, true);
		for (int i = 0; i < 3; i++)
			player.getPackets().sendHideIComponent(1285, i, true);
		player.getPackets().sendHideIComponent(1285, 4, true);
		refreshHPBar();
	}
	
	private void refreshHPBar() {
		player.getPackets().sendHideIComponent(1285, 3, Zalcano.ZALCANO_INSTANCE.hasFinished());
		if (Zalcano.ZALCANO_INSTANCE.hasFinished())
			return;
		player.getPackets().sendCSVarInteger(1923,
				(int) ((double) (Zalcano.ZALCANO_INSTANCE.getMaxHitpoints() - Zalcano.ZALCANO_INSTANCE.getHitpoints()) * (double) 7500
						/ (double) Zalcano.ZALCANO_INSTANCE.getMaxHitpoints()));
	}

    @Override
    public boolean processObjectClick1(WorldObject object) {
        int id = object.getId();
        if (id == Zalcano.DOOR_ID) {
            leave(false);
            removeControler();
            return false;
        } else if(id == Zalcano.GLOW_ROCK_ID) {
            player.getActionManager().setAction(new Mining(object, Mining.RockDefinitions.Tephra));
            return false;
        } else if(id == Zalcano.FURNACE_ID) {
            player.getActionManager().setAction(new Smelting(Smelting.SmeltingBar.REFINED_TEPHRA, object, 28));
            return false;
        } else if(id == Zalcano.ALTAR_ID) {
            player.getActionManager().setAction(new Smelting(Smelting.SmeltingBar.IMBUED_TEPHRA, object, 28));
            return false;
        }
        return true;
    }

    @Override
    public void magicTeleported(int type) {
        Zalcano.handleRemove(player);
        removeControler();
    }

    @Override
    public void forceClose() {
        Zalcano.handleRemove(player);
        removeControler();
    }
}
