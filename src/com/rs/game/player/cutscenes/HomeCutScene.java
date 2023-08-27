package com.rs.game.player.cutscenes;

import java.util.ArrayList;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.player.Player;
import com.rs.game.player.cutscenes.actions.ConstructMapAction;
import com.rs.game.player.cutscenes.actions.CreateNPCAction;
import com.rs.game.player.cutscenes.actions.CutsceneAction;
import com.rs.game.player.cutscenes.actions.CutsceneCodeAction;
import com.rs.game.player.cutscenes.actions.DestroyCachedObjectAction;
import com.rs.game.player.cutscenes.actions.LookCameraAction;
import com.rs.game.player.cutscenes.actions.MoveNPCAction;
import com.rs.game.player.cutscenes.actions.MovePlayerAction;
import com.rs.game.player.cutscenes.actions.NPCAnimationAction;
import com.rs.game.player.cutscenes.actions.NPCFaceTileAction;
import com.rs.game.player.cutscenes.actions.NPCForceTalkAction;
import com.rs.game.player.cutscenes.actions.NPCGraphicAction;
import com.rs.game.player.cutscenes.actions.PlayerAnimationAction;
import com.rs.game.player.cutscenes.actions.PlayerFaceTileAction;
import com.rs.game.player.cutscenes.actions.PlayerForceTalkAction;
import com.rs.game.player.cutscenes.actions.PlayerGraphicAction;
import com.rs.game.player.cutscenes.actions.PosCameraAction;

public class HomeCutScene extends Cutscene {

	private static int DRAGONKK = 1;
	private static int GUARD1 = 2;
	private static int GUARD2 = 3;

	public boolean hiddenMinimap() {
		return false;
	}

	public CutsceneAction[] getActions(final Player player) {
		ArrayList<CutsceneAction> actionsList = new ArrayList<CutsceneAction>();
		actionsList.add(new ConstructMapAction(213, 478, 3, 3));
		actionsList.add(new MovePlayerAction(10, 0, 0, 127, -1));
		actionsList.add(new LookCameraAction(10, 8, 1000, -1));
		actionsList.add(new PosCameraAction(10, 0, 3000, 3));
		actionsList.add(new CreateNPCAction(DRAGONKK, 1302, 10, 6, 0, "God of Creation", 13337, -1));
		actionsList.add(new NPCFaceTileAction(DRAGONKK, 10, 5, -1));
		actionsList.add(new NPCGraphicAction(DRAGONKK, new Graphics(184), 2));
		actionsList.add(new NPCForceTalkAction(DRAGONKK, "....", 3));
		actionsList.add(new NPCForceTalkAction(DRAGONKK, "Kamehameha!@!@!@!", -1));
		actionsList.add(new NPCAnimationAction(DRAGONKK, new Animation(2108), 3));
		actionsList.add(new NPCFaceTileAction(DRAGONKK, 9, 6, -1));
		actionsList.add(new MovePlayerAction(9, 6, 0, 127, -1));
		actionsList.add(new PlayerFaceTileAction(9, 5, -1));
		actionsList.add(new PlayerAnimationAction(new Animation(2111), -1));
		actionsList.add(new PlayerGraphicAction(new Graphics(184), 1));
		actionsList.add(new DestroyCachedObjectAction(DRAGONKK, 0));
		actionsList.add(new PlayerFaceTileAction(9, 7, 1));
		actionsList.add(new PlayerFaceTileAction(8, 6, 1));
		actionsList.add(new PlayerFaceTileAction(10, 6, 1));
		actionsList.add(new PlayerForceTalkAction("Huh?", 1));
		actionsList.add(new PlayerAnimationAction(new Animation(857), -1));
		actionsList.add(new PlayerForceTalkAction("Where am I?", 3));
		actionsList.add(new CreateNPCAction(GUARD1, 589, 3, 7, 0, -1));
		actionsList.add(new CreateNPCAction(GUARD2, 945, 3, 5, 0, -1));
		actionsList.add(new MoveNPCAction(GUARD1, 8, 7, false, 0));
		actionsList.add(new MoveNPCAction(GUARD2, 8, 5, false, 2));
		actionsList.add(new NPCForceTalkAction(GUARD1, "You! What are you doing here?", -1));
		actionsList.add(new PlayerFaceTileAction(8, 6, 3));
		actionsList.add(new PlayerForceTalkAction("Idk... Walking??", 2));
		actionsList.add(new NPCForceTalkAction(GUARD1, "You must have slipped", 1));
		actionsList.add(new NPCForceTalkAction(GUARD1, "and hit your head on the crystal.", 1));
		actionsList.add(new NPCForceTalkAction(GUARD2, "Does it matter?", 1));
		actionsList.add(new NPCForceTalkAction(GUARD1, "Let's just take "+(player.getAppearence().isMale() ?"him" : "her") + " to Onyx...", 2));
		actionsList.add(new MoveNPCAction(GUARD1, 15, 7, false, -1));
		actionsList.add(new MovePlayerAction(15, 6, false, -1));
		actionsList.add(new MoveNPCAction(GUARD2, 15, 5, false, 0));
		/*actionsList.add(new PlayerForceTalkAction("What\'s Onyx?", 0));
		actionsList.add(new NPCForceTalkAction(GUARD2, "Dammit...", 5));*/
		actionsList.add(new ConstructMapAction(193, 450, 8, 8));
		actionsList.add(new LookCameraAction(28, 30, 2000, -1));
		actionsList.add(new PosCameraAction(28, 15, 4000, -1));
		actionsList.add(new MovePlayerAction(29, 25, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new PlayerFaceTileAction(29, 26, -1));
		actionsList.add(new MoveNPCAction(GUARD1, 28, 25, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new NPCFaceTileAction(GUARD1, 28, 26, -1));
		actionsList.add(new MoveNPCAction(GUARD2, 30, 25, 0, Player.TELE_MOVE_TYPE, -1));
		actionsList.add(new NPCFaceTileAction(GUARD2, 30, 26, 0));
		actionsList.add(new MoveNPCAction(GUARD1, 28, 31, false, -1));
		actionsList.add(new MovePlayerAction(29, 31, false, -1));
		actionsList.add(new MoveNPCAction(GUARD2, 30, 31, false, 6));
		actionsList.add(new NPCGraphicAction(GUARD1, new Graphics(184), -1));
		actionsList.add(new NPCGraphicAction(GUARD2, new Graphics(184), 0));
		actionsList.add(new DestroyCachedObjectAction(GUARD1, -1));
		actionsList.add(new DestroyCachedObjectAction(GUARD2, 2));
		actionsList.add(new PlayerForceTalkAction("Hummm. What now...", 3));
		actionsList.add(new CutsceneCodeAction(new Runnable() {
			@Override
			public void run() {
				player.getControlerManager().forceStop();
				//player.getDialogueManager().startDialogue("SimpleNPCMessage", 947, "Welcome to Onyx!", "If you have any questions make sure to read the guide book in your inventory.");
				player.getDialogueManager().startDialogue("SelectModeDialogue");
					
			}
		}, -1));
		return actionsList.toArray(new CutsceneAction[actionsList.size()]);
	}
}
