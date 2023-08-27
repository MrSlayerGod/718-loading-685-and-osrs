package com.rs.game.player.content.agility;

import java.util.ArrayList;

import com.rs.game.Animation;
import com.rs.game.player.Player;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.player.cutscenes.actions.CutsceneAction;
import com.rs.game.player.cutscenes.actions.MovePlayerAction;
import com.rs.game.player.cutscenes.actions.PlayerAnimationAction;
import com.rs.game.player.cutscenes.actions.PlayerFaceTileAction;
import com.rs.game.player.cutscenes.actions.PosCameraAction;

public class Hefin {
	
	public static void leap(Player player) {
		if (!Agility.hasLevel(player, 77))
			return;
		player.getCutscenesManager().play(new Cutscene() {

			@Override
			public boolean hiddenMinimap() {
				return false;
			}

			@Override
			public CutsceneAction[] getActions(Player player) {
				ArrayList<CutsceneAction> actionsList = new ArrayList<CutsceneAction>();
				
				actionsList.add(new MovePlayerAction(2179, 3402, 1, Player.WALK_MOVE_TYPE, -1));
				actionsList.add(new PlayerFaceTileAction(2176, 3418, 0));
				//actionsList.add(new LookCameraAction(2169, 3411, 4500, -1, -1, -1));
				actionsList.add(new PlayerAnimationAction(new Animation(24587), 1));
				actionsList.add(new PosCameraAction(2167, 3411, 5000, 8));
				actionsList.add(new MovePlayerAction(2180, 3419, 1, Player.TELE_MOVE_TYPE, -1));
				
				return actionsList.toArray(new CutsceneAction[actionsList.size()]);
			}
		});
	}
	
	public static void traverse(Player player) {
		player.getCutscenesManager().play(new Cutscene() {

			@Override
			public boolean hiddenMinimap() {
				return false;
			}

			@Override
			public CutsceneAction[] getActions(Player player) {
				ArrayList<CutsceneAction> actionsList = new ArrayList<CutsceneAction>();
				
				actionsList.add(new MovePlayerAction(2180, 3419, 1, Player.WALK_MOVE_TYPE, 1));
				actionsList.add(new PlayerFaceTileAction(2180, 3423, 1));
				actionsList.add(new PlayerAnimationAction(new Animation(25011), 1));
				actionsList.add(new PosCameraAction(2158, 3420, 11000, 16));
				actionsList.add(new MovePlayerAction(2171, 3437, 1, Player.TELE_MOVE_TYPE, 1));
				
				return actionsList.toArray(new CutsceneAction[actionsList.size()]);
			}
		});
	}
	
	
}
