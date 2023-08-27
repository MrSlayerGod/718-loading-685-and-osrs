package com.rs.game.player.content;

import java.util.concurrent.TimeUnit;

import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.utils.Utils;

public final class LivingRockCavern {

	private static enum Rocks {
		COAL_ROCK_1(new WorldObject(5999, 10, 1, 3690, 5146, 0)),
		COAL_ROCK_2(new WorldObject(5999, 10, 2, 3690, 5125, 0)),
		COAL_ROCK_3(new WorldObject(5999, 10, 0, 3687, 5107, 0)),
		COAL_ROCK_4(new WorldObject(5999, 10, 1, 3674, 5098, 0)),
		COAL_ROCK_5(new WorldObject(5999, 10, 2, 3664, 5090, 0)),
		COAL_ROCK_6(new WorldObject(5999, 10, 3, 3615, 5090, 0)),
		COAL_ROCK_7(new WorldObject(5999, 10, 1, 3625, 5107, 0)),
		COAL_ROCK_8(new WorldObject(5999, 10, 3, 3647, 5142, 0)),
		GOLD_ROCK_1(new WorldObject(45076, 10, 1, 3667, 5075, 0)),
		GOLD_ROCK_2(new WorldObject(45076, 10, 0, 3637, 5094, 0)),
		GOLD_ROCK_3(new WorldObject(45076, 10, 0, 3677, 5160, 0)),
		GOLD_ROCK_4(new WorldObject(45076, 10, 1, 3629, 5148, 0)),
		
		COAL_DZ(new WorldObject(5999, 10, 0, 3374, 5192, 0), true),
		GOLD_DZ(new WorldObject(45076, 10, 0, 3378, 5192, 0), true),
		GOLD_WILD(new WorldObject(45076, 10, 0, 3172, 3919, 0), true),
		;

		private Rocks(WorldObject rock, boolean spawnEmpty) {
			this.rock = rock;
			this.spawnEmpty = spawnEmpty;
		}
		
		private Rocks(WorldObject rock) {
			this.rock = rock;
		}

		private WorldObject rock;
		private boolean spawnEmpty;

	}

	private LivingRockCavern() {

	}

	private static void respawnRock(final Rocks rock) {
		World.spawnObject(rock.rock);
		GameExecutorManager.slowExecutor.schedule(new Runnable() {

			@Override
			public void run() {
				removeRock(rock);
			}
		}, Utils.random(8) + 3, TimeUnit.MINUTES);
	}

	private static void removeRock(final Rocks rock) {
		if (rock.spawnEmpty)
			World.spawnObject(new WorldObject(45075, rock.rock.getType(), rock.rock.getRotation(), rock.rock));
		else
			World.removeObject(rock.rock);
		GameExecutorManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				respawnRock(rock);
			}

		}, 3, TimeUnit.MINUTES);
	}

	public static void init() {
		for (Rocks rock : Rocks.values())
			respawnRock(rock);
		World.spawnObject(new WorldObject(68973, 10, 3, new WorldTile(3280, 5285, 0)));
	}
}
