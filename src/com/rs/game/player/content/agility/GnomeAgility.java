package com.rs.game.player.content.agility;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Achievements.Task;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class GnomeAgility {

	public static void climbUpTree(final Player player) {
		if (!Agility.hasLevel(player, 85))
			return;
		player.useStairs(828, new WorldTile(2472, 3419, 3), 1, 2, "You climbed the tree succesfully.");
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				removeGnomeStage(player);
				player.getSkills().addXp(Skills.AGILITY, 25 * Agility.getAgilityMultiplier(player));
			}
		}, 1);
	}

	public static void jumpDown(final Player player, WorldObject object) {
		player.lock();
		WorldTasksManager.schedule(new WorldTask() {

			boolean secondLoop;

			@Override
			public void run() {
				if (!secondLoop) {
					player.setNextForceMovement(new ForceMovement(player, 1, new WorldTile(2485, 3434, 3), 2, ForceMovement.NORTH));
					player.setNextAnimation(new Animation(2923));
					secondLoop = true;
				} else {
					player.setNextAnimation(new Animation(2924));
					player.setNextWorldTile(new WorldTile(2485, 3436, 0));
					player.getAchievements().add(Task.GNOME_AGILITY_ADVANCED);
					player.getSkills().addXp(Skills.AGILITY, 630 * Agility.getAgilityMultiplier(player));
					if (Utils.random(50) == 0) {
						player.getPackets().sendGameMessage("You feel your inventory getting heavier.");
						if (!player.containsItem(14938)) {
							player.getInventory().addItemDrop(14938, 1);
							World.sendNews(player, player.getDisplayName() + " has received " + ItemConfig.forID(14938).getName() + " from agility!", 1);
						}
					}
					player.lock(2);
					stop();
				}

			}

		}, 1, 2);
	}

	// advanced

	public static void preSwing(final Player player, final WorldObject object) {
		if (player.getY() >= 3432)
			return;
		player.lock();
		player.setNextAnimation(new Animation(11784));
		final WorldTile toTile = new WorldTile(player.getX(), 3421, object.getPlane());
		player.setNextForceMovement(new ForceMovement(player, 0, toTile, 2, ForceMovement.NORTH));
		WorldTasksManager.schedule(new WorldTask() {
			int stage;

			@Override
			public void run() {
				if (stage == 1) {
					player.setNextWorldTile(toTile);
					player.setNextAnimation(new Animation(11785));
					swing(player, object);
					stop();
				}
				stage++;
			}

		}, 0, 1);
	}

	private static void swing(final Player player, final WorldObject object) {
		final WorldTile toTile = new WorldTile(player.getX(), 3425, object.getPlane());
		player.setNextForceMovement(new ForceMovement(player, 0, toTile, 1, ForceMovement.NORTH));
		WorldTasksManager.schedule(new WorldTask() {
			int stage;

			@Override
			public void run() {
				if (stage == 0) {
					player.setNextAnimation(new Animation(11789));
					player.setNextWorldTile(toTile);
				} else if (stage == 1) {
					swing1(player, object);
					stop();
				}
				stage++;
			}

		}, 0, 1);
	}

	private static void swing1(final Player player, final WorldObject object) {
		final WorldTile NextTile = new WorldTile(player.getX(), 3429, object.getPlane());
		player.setNextForceMovement(new ForceMovement(player, 2, NextTile, 3, ForceMovement.NORTH));
		WorldTasksManager.schedule(new WorldTask() {
			int stage;

			@Override
			public void run() {
				if (stage == 3) {
					player.setNextWorldTile(NextTile);
					swing2(player, object);
					stop();
				}
				stage++;
			}

		}, 0, 1);
	}

	private static void swing2(final Player player, final WorldObject object) {
		player.lock();
		final WorldTile LastTile = new WorldTile(player.getX(), 3432, object.getPlane());
		player.setNextForceMovement(new ForceMovement(player, 0, LastTile, 1, ForceMovement.NORTH));
		WorldTasksManager.schedule(new WorldTask() {
			int stage;

			@Override
			public void run() {
				if (stage == 2) {
					player.getSkills().addXp(Skills.AGILITY, 25 * Agility.getAgilityMultiplier(player));
					player.setNextWorldTile(LastTile);
					stop();
					player.unlock();
				}
				stage++;
			}

		}, 0, 1);
	}

	public static void runGnomeBoard(final Player player, final WorldObject object) {
		if (player.getX() >= 2484)
			return;
		player.lock();
		player.setNextAnimation(new Animation(2922));
		final WorldTile toTile = new WorldTile(2484, 3418, object.getPlane());
		player.setNextForceMovement(new ForceMovement(new WorldTile(2477, 3418, 3), 1, toTile, 3, ForceMovement.EAST));
		player.getPackets().sendGameMessage("You skilfully run across the board.", true);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.unlock();
				player.setNextWorldTile(toTile);
				player.getSkills().addXp(Skills.AGILITY, 25 * Agility.getAgilityMultiplier(player));
			}
		}, 3);
	}

	// gnome course

	public static void walkGnomeLog(final Player player) {
		if (player.getX() != 2474 || player.getY() != 3436)
			return;
		final boolean running = player.getRun();
		player.setRunHidden(false);
		player.lock(8);
		player.addWalkSteps(2474, 3429, -1, false);
		player.getPackets().sendGameMessage("You walk carefully across the slippery log...", true);
		WorldTasksManager.schedule(new WorldTask() {
			boolean secondloop;

			@Override
			public void run() {
				if (!secondloop) {
					secondloop = true;
					player.getAppearence().setRenderEmote(155);
				} else {
					player.getAppearence().setRenderEmote(-1);
					player.setRunHidden(running);
					setGnomeStage(player, 0);
					player.getSkills().addXp(Skills.AGILITY, 7.5 * Agility.getAgilityMultiplier(player));
					player.getPackets().sendGameMessage("... and make it safely to the other side.", true);
					stop();
				}
			}
		}, 0, 6);
	}

	public static void climbGnomeObstacleNet(final Player player) {
		if (player.getY() != 3426)
			return;
		player.getPackets().sendGameMessage("You climb the netting.", true);
		player.useStairs(828, new WorldTile(player.getX(), 3423, 1), 1, 2);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (getGnomeStage(player) == 0)
					setGnomeStage(player, 1);
				player.getSkills().addXp(Skills.AGILITY, 7.5 * Agility.getAgilityMultiplier(player));
			}
		}, 1);
	}

	public static void climbUpGnomeTreeBranch(final Player player) {
		player.getPackets().sendGameMessage("You climb the tree...", true);
		player.useStairs(828, new WorldTile(2473, 3420, 2), 1, 2, "... to the plantaform above.");
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (getGnomeStage(player) == 1)
					setGnomeStage(player, 2);
				player.getSkills().addXp(Skills.AGILITY, 5 * Agility.getAgilityMultiplier(player));
			}
		}, 1);
	}

	public static void walkBackGnomeRope(final Player player) {
		if (player.getX() != 2483 || player.getY() != 3420 || player.getPlane() != 2)
			return;
		final boolean running = player.getRun();
		player.setRunHidden(false);
		player.lock(7);
		player.addWalkSteps(2477, 3420, -1, false);
		WorldTasksManager.schedule(new WorldTask() {
			boolean secondloop;

			@Override
			public void run() {
				if (!secondloop) {
					secondloop = true;
					player.getAppearence().setRenderEmote(155);
				} else {
					player.getAppearence().setRenderEmote(-1);
					player.setRunHidden(running);
					player.getSkills().addXp(Skills.AGILITY, 7 * Agility.getAgilityMultiplier(player));
					player.getPackets().sendGameMessage("You passed the obstacle succesfully.", true);
					stop();
				}
			}
		}, 0, 5);
	}

	public static void walkGnomeRope(final Player player) {
		if (player.getX() != 2477 || player.getY() != 3420 || player.getPlane() != 2)
			return;
		final boolean running = player.getRun();
		player.setRunHidden(false);
		player.lock(7);
		player.addWalkSteps(2483, 3420, -1, false);
		WorldTasksManager.schedule(new WorldTask() {
			boolean secondloop;

			@Override
			public void run() {
				if (!secondloop) {
					secondloop = true;
					player.getAppearence().setRenderEmote(155);
				} else {
					player.getAppearence().setRenderEmote(-1);
					player.setRunHidden(running);
					if (getGnomeStage(player) == 2)
						setGnomeStage(player, 3);
					player.getSkills().addXp(Skills.AGILITY, 7 * Agility.getAgilityMultiplier(player));
					player.getPackets().sendGameMessage("You passed the obstacle succesfully.", true);
					stop();
				}
			}
		}, 0, 5);
	}

	public static void climbDownGnomeTreeBranch(final Player player) {
		player.useStairs(828, new WorldTile(2487, 3421, 0), 1, 2, "You climbed the tree branch succesfully.");
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (getGnomeStage(player) == 3)
					setGnomeStage(player, 4);
				player.getSkills().addXp(Skills.AGILITY, 5 * Agility.getAgilityMultiplier(player));
			}
		}, 1);
	}

	public static void climbGnomeObstacleNet2(final Player player, WorldObject object) {
		if (player.getY() != 3425)
			return;
		final WorldTile toTile = new WorldTile(player.getX(), object.getY() + 1, 0);
		player.lock();
		player.setNextFaceWorldTile(toTile);
		player.getPackets().sendGameMessage("You climb the netting.", true);
		WorldTasksManager.schedule(new WorldTask() {

			int ticks;

			@Override
			public void run() {
				ticks++;

				if (ticks == 1) {
					player.setNextAnimation(new Animation(16356));
				} else if (ticks == 5) {
					player.setNextWorldTile(toTile);
					player.setNextAnimation(new Animation(-1));
				} else if (ticks == 7) {
					if (getGnomeStage(player) == 4)
						setGnomeStage(player, 5);
					player.getSkills().addXp(Skills.AGILITY, 8 * Agility.getAgilityMultiplier(player));
					player.unlock();
					stop();
				}
			}
		}, 0, 0);
	}

	public static void enterGnomePipe(final Player player, final int objectX, final int objectY) {
		player.lock();
		player.resetWalkSteps();
		player.addWalkSteps(objectX, objectY - 1, -1, false);
		player.getPackets().sendGameMessage("You pull yourself through the pipes.", true);
		WorldTasksManager.schedule(new WorldTask() {

			int ticks;

			@Override
			public void run() {
				ticks++;
				if (ticks == 2) {
					player.setNextForceMovement(new ForceMovement(player, 1, new WorldTile(objectX, player.getY() + 3, 0), 4, ForceMovement.NORTH));
				} else if (ticks == 3) {
					player.setNextAnimation(new Animation(10580));
				} else if (ticks == 5) {
					player.setNextWorldTile(new WorldTile(objectX, player.getY() + 3, 0));
				} else if (ticks == 9) {
					player.setNextForceMovement(new ForceMovement(player, 1, new WorldTile(objectX, player.getY() + 4, 0), 5, ForceMovement.NORTH));
				} else if (ticks == 12) {
					player.setNextAnimation(new Animation(10580));
					player.setNextWorldTile(new WorldTile(objectX, player.getY() + 4, 0));
				} else if (ticks == 13) {
					if (getGnomeStage(player) == 5) {
						removeGnomeStage(player);
						player.getAchievements().add(Task.GNOME_AGILITY);
						player.getSkills().addXp(Skills.AGILITY, 39.5 * Agility.getAgilityMultiplier(player));
					}
					player.getSkills().addXp(Skills.AGILITY, 7 * Agility.getAgilityMultiplier(player));
					player.unlock();
					stop();
					return;
				}
			}
		}, 0, 0);
	}

	public static void removeGnomeStage(Player player) {
		player.getTemporaryAttributtes().remove("GnomeCourse");
		LuckyPets.checkPet(player, LuckyPet.GIANT_SQUIRREL);
	}

	public static void setGnomeStage(Player player, int stage) {
		player.getTemporaryAttributtes().put("GnomeCourse", stage);
	}

	public static int getGnomeStage(Player player) {
		Integer stage = (Integer) player.getTemporaryAttributtes().get("GnomeCourse");
		if (stage == null)
			return -1;
		return stage;
	}
}
