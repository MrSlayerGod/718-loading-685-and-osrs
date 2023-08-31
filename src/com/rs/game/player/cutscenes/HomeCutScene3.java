package com.rs.game.player.cutscenes;

import java.util.ArrayList;

import com.rs.game.Animation;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.HouseControler;
import com.rs.game.player.cutscenes.actions.CreateNPCAction;
import com.rs.game.player.cutscenes.actions.CutsceneAction;
import com.rs.game.player.cutscenes.actions.CutsceneCodeAction;
import com.rs.game.player.cutscenes.actions.MovePlayerAction;
import com.rs.game.player.cutscenes.actions.NPCForceTalkAction;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.ReferralSystem;

public class HomeCutScene3 extends Cutscene {

	@Override
	public boolean hiddenMinimap() {
		return true;
	}

	@Override
	public boolean showYourselfToOthers() {
		return false;
	}
	
	private static final int ORACLE = 2;

	public CutsceneAction[] getActions(final Player player) {
		ArrayList<CutsceneAction> actionsList = new ArrayList<CutsceneAction>();
		actionsList.add(new CutsceneCodeAction(new Runnable() {
			@Override
			public void run() {
				player.getInterfaceManager().sendInventoryInterface(375);
				player.getPackets().sendIComponentText(375, 3, "Skip tutorial");
			}
		}, -1));

		actionsList.add(new CreateNPCAction(ORACLE, 2253, 3126, 3710, 0, "Wise Old Man", 0, -1));

		actionsList.add(new NPCForceTalkAction(ORACLE, "Welcome to runic, adventurer. ", 8));
		actionsList.add(new NPCForceTalkAction(ORACLE, "Runic is a 718 revision along with HD graphics with a blend of OSRS content.", 8));

		// Home Overview

		actionsList.add(new MovePlayerAction(3087, 3500, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new NPCForceTalkAction(ORACLE, "You are now in the glorious city of Edgeville, recently updated.", 8));
		actionsList.add(new NPCForceTalkAction(ORACLE, "Here you will find other adventurers trading, skilling, maybe boasting about a new item or two...", 8));

		// Thieving

		actionsList.add(new MovePlayerAction(3095, 3508, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new NPCForceTalkAction(ORACLE, "These stalls offer precious gems which can be good money for a beginner.", 8));

		//Shops

		actionsList.add(new MovePlayerAction(3080, 3509, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new NPCForceTalkAction(ORACLE, "Traveling merchants gather here and offer a variety of useful items and weaponry.", 8));

		//Slayer

		actionsList.add(new MovePlayerAction(3094, 3478, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new NPCForceTalkAction(ORACLE, "Consult Slayer Masters to learn to slay down powerful creatures and get rich doing so.", 8));

		/*//Portal

		actionsList.add(new MovePlayerAction(3101, 3499, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new NPCForceTalkAction(ORACLE, "Travel through the Portal Nexus to teleport anywhere in the game", 8));

		//Guide Directory

		actionsList.add(new MovePlayerAction(3123, 3492, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new NPCForceTalkAction(ORACLE, "This all in one guide will offer answers to most questions you have while exploring Matrix.", 8));

		//Inferno Statue

		actionsList.add(new MovePlayerAction(3088, 3477, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new NPCForceTalkAction(ORACLE, "Matrix is home to some of the best PvM content anywhere.", 8));*/


		actionsList.add(new MovePlayerAction(	2815, 5513, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new NPCForceTalkAction(ORACLE, "Use ::spawnpk to pvp agaisnt other players with 100% free spawned items!", 8));

		// Back to Home

		//PvP Tournament

		actionsList.add(new MovePlayerAction(5983, 9806, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new NPCForceTalkAction(ORACLE, "The boldest of adventures fight -- other adventurers -- for fortune and glory!", 8));


		actionsList.add(new MovePlayerAction(3087, 3500, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new NPCForceTalkAction(ORACLE, "Go forth and make a name for yourself, while treating others with respect.", 8));
		actionsList.add(new NPCForceTalkAction(ORACLE, "Enjoy this game we all love.", 8));

		return actionsList.toArray(new CutsceneAction[actionsList.size()]);
	}
	
	public void stopCutscene(Player player) {
		if (player.getAppearence().isHidden())
			player.getAppearence().switchHidden();
		player.getInterfaceManager().removeOverlay(true);
		player.getInterfaceManager().removeInventoryInterface();
		player.setNextAnimation(new Animation(-1));
		super.stopCutscene(player);
		player.getControlerManager().forceStop();
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
			//	player.getDialogueManager().startDialogue("SimpleNPCMessage", 946, "Welcome to Onyx!", "If you have any questions make sure to read the guide book in your inventory.");
			//	player.getTemporaryAttributtes().put(Key.REFERRAL_NAME, Boolean.TRUE);
			//	player.getPackets().sendInputNameScript("How did you find us?");
				if(ReferralSystem.isNewPlayer(player))
					player.getDialogueManager().startDialogue("ReferralD");
			}
		});
	}

}
