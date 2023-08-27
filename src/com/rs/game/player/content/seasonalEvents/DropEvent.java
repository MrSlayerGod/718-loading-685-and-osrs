package com.rs.game.player.content.seasonalEvents;

import java.util.TimerTask;

import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class DropEvent {

	private static enum Drops {
		PUMPKIN(1959);

		private int id;

		private Drops(int id) {
			this.id = id;
		}
	}

	private static final Drops CURRENT = null;
	private static final int RATE = 60; //every 60 second

	private static boolean enabled() {
		return CURRENT != null;
	}

	public static void init() {
		if (!enabled())
			return;
		GameExecutorManager.fastExecutor.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					for (int i = 0; i < 1000; i++) {
						int x = 2048 + Utils.random(1855);
						int y = 2496 + Utils.random(1663);
						if (!World.isTileFree(0, x, y, 1))
							continue;
						World.addGroundItem(new Item(CURRENT.id), new WorldTile(x, y, 0));
						return;
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}

		}, RATE * 1000, RATE * 1000);
	}

}
