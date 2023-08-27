package com.rs.game.player.content.surpriseevents;

import com.rs.game.player.content.surpriseevents.arenaimpl.CastleArena;
import com.rs.game.player.content.surpriseevents.arenaimpl.ElvenArena;
import com.rs.utils.Utils;

public class ArenaFactory {

	public static EventArena randomEventArena(boolean multi) {
		int rnd = Utils.random(2);
		switch (rnd) {
			case 0:
			default:
				return new ElvenArena(multi);
			case 1:
				return new CastleArena(multi);
			
		}
		
	}

}
