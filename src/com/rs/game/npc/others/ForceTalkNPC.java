package com.rs.game.npc.others;

import com.rs.game.ForceTalk;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class ForceTalkNPC extends NPC {

	private String[] messages;
	public ForceTalkNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, String... messages) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		this.messages = messages;
	}

	@Override
	public void processNPC() {
		if (messages == null)
			return;
		if (Utils.random(20) == 0) 
			setNextForceTalk(new ForceTalk(messages[Utils.random(messages.length)]));
		super.processNPC();
	}
}
