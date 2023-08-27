/**
 * 
 */
package com.rs.game.npc.zulrah.action;

import com.rs.game.Animation;
import com.rs.game.npc.zulrah.Zulrah;
import com.rs.game.npc.zulrah.ZulrahColor;
import com.rs.game.npc.zulrah.ZulrahPosition;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author dragonkk(Alex)
 * Nov 5, 2017
 */
public class SwitchColor extends ZulrahAction {

	private ZulrahColor color;
	private ZulrahPosition position;
	
	public SwitchColor(ZulrahColor color, ZulrahPosition position) {
		this.color = color;
		this.position = position;
	}
	
	@Override
	public int use(Zulrah zulrah) {
		if (zulrah.isFirstWave()) {
			transfom(zulrah);
			return 6;
		}
		zulrah.setCantInteract(true);
		zulrah.setNextAnimation(new Animation(25072));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (zulrah.hasFinished() || !zulrah.getShrine().isRunning())
					return;
				transfom(zulrah);
			}
		}, 2);
		return 8;
	}
	
	private void transfom(Zulrah zulrah) {
		if (!zulrah.isFirstWave()) {
			zulrah.setNextNPCTransformation(Zulrah.ID + color.ordinal());
			zulrah.setNextFaceEntity(null);
			zulrah.setNextWorldTile(zulrah.getShrine().getWorldTileReal(position.getTile()));
			zulrah.setCantInteract(false);
		}
		zulrah.setNextFaceWorldTile(zulrah.getShrine().getWorldTile(18+2, 39+2)); //mid
		zulrah.setNextAnimation(new Animation(25071));
	}

}
