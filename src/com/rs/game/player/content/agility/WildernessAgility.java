package com.rs.game.player.content.agility;

import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Direction;
import com.rs.utils.Utils;

public class WildernessAgility {

	public static void swingOnRopeSwing(final Player player, WorldObject object) {
		if (!Agility.hasLevel(player, 52))
			return;
		else if (player.getY() != 3953) {
			player.addWalkSteps(player.getX(), 3953);
			player.getPackets().sendGameMessage("You'll need to get closer to make this jump.");
			return;
		}
		player.lock(4);
		player.setNextAnimation(new Animation(751));
		World.sendObjectAnimation(player, object, new Animation(497));
		final WorldTile toTile = new WorldTile(object.getX(), 3958, object.getPlane());
		player.setNextForceMovement(new ForceMovement(player, 1, toTile, 3, ForceMovement.NORTH));
		player.getSkills().addXp(Skills.AGILITY, 20 * Agility.getAgilityMultiplier(player));
		player.getPackets().sendGameMessage("You skilfully swing across.", true);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(toTile);
				if (getStage(player) != 1)
					removeStage(player);
				else
					setStage(player, 2);
			}
		}, 1);
	}

	public static void walkAcrossLogBalance(final Player player, final WorldObject object) {
		if (!Agility.hasLevel(player, 52))
			return;
		if (player.getY() != object.getY()) {
			player.addWalkSteps(3001, 3945, -1, false);
			player.lock(2);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					walkAcrossLogBalanceEnd(player, object);
				}
			}, 1);
		} else
			walkAcrossLogBalanceEnd(player, object);
	}

	private static void walkAcrossLogBalanceEnd(final Player player, WorldObject object) {
		player.getPackets().sendGameMessage("You walk carefully across the slippery log...", true);
		player.lock();
		player.setNextAnimation(new Animation(9908));
		final WorldTile toTile = new WorldTile(2994, object.getY(), object.getPlane());
		player.setNextForceMovement(new ForceMovement(toTile, 12, ForceMovement.WEST));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextAnimation(new Animation(-1));
				player.setNextWorldTile(toTile);
				player.unlock();
				player.getSkills().addXp(Skills.AGILITY, 20 * Agility.getAgilityMultiplier(player));
				player.getPackets().sendGameMessage("... and make it safely to the other side.", true);
				if (getStage(player) != 3)
					removeStage(player);
				else
					setStage(player, 4);
			}
		}, 11);
	}

	public static void jumpSteppingStones(final Player player, final WorldObject object) {
		if (player.getY() != object.getY())
			return;
		if(player.isLocked())
			return;
		player.getStopwatch().delay(12);
		player.getPackets().sendGameMessage("You carefully start crossing the stepping stones...");
		WorldTile toTile = new WorldTile(3001, 3960, player.getPlane());
		WorldTasksManager.schedule(event -> {
			event.setCancelCondition(() -> player.isDead());

			for(int i = 0; i < 6; i++) {
				final int X_OFFSET = i;
				event.add(() -> {
					toTile.setLocation(3001 - X_OFFSET, 3960, player.getPlane());
					player.setNextForceMovement(new ForceMovement(toTile, 1, ForceMovement.WEST));
					player.setNextAnimation(new Animation(741));
				});
				event.add(() -> {
					player.setNextWorldTile(toTile.clone());
				});
			}

			event.add(() -> {
				player.getSkills().addXp(Skills.AGILITY, 20 * Agility.getAgilityMultiplier(player));
				if (getStage(player) != 2)
					removeStage(player);
				else
					setStage(player, 3);
				player.getPackets().sendGameMessage("... and reach the other side safely.", true);
			});
		});
	}

	public static void climbUpWall(final Player player, final WorldObject object) {
		if (!Agility.hasLevel(player, 52))
			return;
		Agility.startObstacle(player, new WorldTile(2994, 3939, 0), () -> {
			// delay next action 4 ticks
			if(!player.getStopwatch().finished())
				return;
			player.getStopwatch().delay(6);

			WorldTasksManager.schedule(event -> {
				event.add(() -> {
					player.setDirection(Direction.SOUTH, true);
					player.setNextAnimation(new Animation(3378));
				});
				event.delay(1);
				event.add(() -> {
					player.setNextAnimation(new Animation(-1));
					player.setNextWorldTile(new WorldTile(2994, 3935, 0));
				});
				event.add(() -> {
					if (getStage(player) != 4)
						removeStage(player);
					else {
						player.getSkills().addXp(Skills.AGILITY, 498.9 * Agility.getAgilityMultiplier(player));
						LuckyPets.checkPet(player, LuckyPet.GIANT_SQUIRREL);
						setStage(player, 0);
					}
				});
			});
		});
	}

	public static void enterWildernessCourse(final Player player) {
		Agility.startObstacle(player, new WorldTile(2998, 3916, 0), () -> {
			enterCourse(player, false);
		});
	}

	public static void leaveWildernessCourse(final Player player) {
		Agility.startObstacle(player, new WorldTile(2998, 3916, 0), () -> {
			enterCourse(player, true);
		});
	}

	private static void enterCourse(Player player, boolean leave) {
		if (!Agility.hasLevel(player, 52))
			return;
		WorldObject firstGate = new WorldObject(65365, 10, 1, 2998, 3916, 0);
		final WorldObject secondGate = new WorldObject(65367, 10, 1, 2997, 3930, 0);

		if(leave)
			World.spawnObjectTemporary(secondGate.copy().setId(65368).setRotation(1), 1200);
		else
			World.spawnObjectTemporary(firstGate.copy().setId(65366).setRotation(1), 1200);

		//player.setNextWorldTile(new WorldTile(firstGate.getX(), firstGate.getY() + 1, 0));
		player.setNextForceMovement(new ForceMovement(leave ? firstGate : secondGate.relative(1,0), 9, leave ? ForceMovement.SOUTH : ForceMovement.NORTH));
		player.setNextAnimation(new Animation(9908));
		player.lock(10);
		player.getPackets().sendGameMessage("You go through the gate and try to edge over the ridge...");

		boolean run = player.isRunning();
		player.setRun(false);
		WorldTasksManager.schedule(() -> {
			if(leave)
				World.spawnObjectTemporary(firstGate.copy().setId(65366).setRotation(1), 1200);
			else
				World.spawnObjectTemporary(secondGate.copy().setId(65368).setRotation(1), 1200);
		},6);

		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.setNextAnimation(new Animation(-1));
				player.setNextWorldTile(leave ? firstGate.clone() : secondGate.relative(1,1));
				player.setRun(run);
			}
		}, 8);
	}
	public static void exitWildernessCourse(final Player player) {
		enterCourse(player, true);
	}

	public static void enterWildernessPipe(final Player player, int objectX, int objectY) {
		player.lock();
		player.resetWalkSteps();
		player.addWalkSteps(3004, 3938, -1, false);
		WorldTasksManager.schedule(new WorldTask() {

			private int ticks;

			@Override
			public void run() {
				ticks++;
				if (ticks == 2) {
					player.setNextForceMovement(new ForceMovement(player, 0, new WorldTile(3004, 3941, 0), 4, ForceMovement.NORTH));
					player.setNextAnimation(new Animation(10580));
				} else if (ticks == 4) {
					player.setNextWorldTile(new WorldTile(3004, 3941, 0));
				} else if (ticks == 8) {
					player.setNextWorldTile(new WorldTile(3004, 3946, 0));
				} else if (ticks == 9) {
					player.setNextForceMovement(new ForceMovement(player, 0, new WorldTile(3004, 3949, 0), 2, ForceMovement.NORTH));
					player.setNextAnimation(new Animation(10580));
				} else if (ticks == 11) {
					player.setNextWorldTile(new WorldTile(3004, 3949, 0));
				} else if (ticks == 13) {
					player.getSkills().addXp(Skills.AGILITY, 12.5 * Agility.getAgilityMultiplier(player));
					player.unlock();
					stop();
					setStage(player, 1);
					return;
				}
			}
		}, 0, 0);
	}

	public static void removeStage(Player player) {
		player.getTemporaryAttributtes().remove("WildernessCourse");
	}

	public static void setStage(Player player, int stage) {
		player.getTemporaryAttributtes().put("WildernessCourse", stage);
	}

	public static int getStage(Player player) {
		Integer stage = (Integer) player.getTemporaryAttributtes().get("WildernessCourse");
		if (stage == null)
			return -1;
		return stage;
	}
}
