package com.rs.game.player.content.agility;

import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class Agility {

    public static boolean hasLevel(Player player, int level) {
        if (player.getSkills().getLevel(Skills.AGILITY) < level) {
            player.getPackets().sendGameMessage("You need an Agility level of " + level + " to use this obstacle.");
            return false;
        }
        return true;
    }

    public static boolean startObstacle(Player player, WorldTile start, Runnable obTraversalEvent) {
        if(!start.matches(player)) {
            player.addWalkSteps(start.getX(), start.getY(), -1, false);
            WorldTasksManager.schedule(() ->  {
                int distToStart = Utils.getDistance(player, start);

                if(distToStart > 1 || start.getPlane() != player.getPlane()) {
                    player.unlock();
                    return;
                }

                obTraversalEvent.run();
            }, 1);

            return false;
        } else {
            obTraversalEvent.run();
            return true;
        }
    }

    public static int tightropeWalk(final Player player, WorldTile start, WorldTile dest, int xp) {
        int tiles = Utils.getDistance(start, dest);
        player.getStopwatch().delay(3);
        startObstacle(player, start, () -> {
            player.getStopwatch().delay(tiles-1);
            final boolean running = player.getRun();
            player.setRunHidden(false);
            player.lock(tiles + 2);
            player.addWalkSteps(dest.getX(), dest.getY(), -1, false);
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
                        player.getSkills().addXp(Skills.AGILITY, xp * Agility.getAgilityMultiplier(player));
                        player.getPackets().sendGameMessage("You cross the tightrope.", true);
                        if(player.getPlane() != dest.getPlane()) {
                            player.setNextWorldTile(dest.clone());
                        }
                        stop();
                    }
                }
            }, 0, tiles-3);
        });
        return tiles-2;
    }

    public static void leapDown(Player player, WorldTile start, WorldTile dest, int xp) {
        player.getStopwatch().delay(3);
        player.setNextAnimation(new Animation(2586));
        player.getAppearence().setRenderEmote(-1);
        player.setNextFaceWorldTile(dest);
        WorldTasksManager.schedule(() -> {
                player.setNextWorldTile(dest);
                player.setNextAnimation(new Animation(2588));
            player.getSkills().addXp(Skills.AGILITY, xp * Agility.getAgilityMultiplier(player));
        });
    }

    public static void leapAcross(Player player, WorldTile start, WorldTile dest, int xp) {
        player.getStopwatch().delay(3);
        player.setNextAnimation(new Animation(21603));
        player.getAppearence().setRenderEmote(-1);
        player.setNextFaceWorldTile(dest);

        WorldTasksManager.schedule(() -> {
            player.setNextWorldTile(dest);
            player.setNextAnimation(new Animation(-1));
            player.getSkills().addXp(Skills.AGILITY, xp * Agility.getAgilityMultiplier(player));
        });
    }

    public static double getAgilityMultiplier(Player player) {
        double mult = 1;
        if (player.getEquipment().getChestId() == 14936)
            mult += 0.01;
        if (player.getEquipment().getLegsId() == 14939)
            mult += 0.01;
        if (player.getEquipment().getChestId() == 14936 && player.getEquipment().getLegsId() == 14939)
            mult += 0.01;
        return mult;
    }


    public static void faladorCrumbledWall(Player player, WorldObject object) {
        if (player.getX() == 2936 && player.getY() == 3355) {
            player.faceObject(object);
            player.setNextAnimation(new Animation(12915, 1));
            final WorldTile toTile = new WorldTile(object.getX() - 1, object.getY(), object.getPlane());
            player.setNextForceMovement(new ForceMovement(player, 1, toTile, 3, ForceMovement.WEST));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.setNextWorldTile(toTile);
                }
            }, 2);
            return;
        }
        player.getStopwatch().delay(5);
        player.faceObject(object);
        player.setNextAnimation(new Animation(12916, 1));
        final WorldTile toTile = new WorldTile(object.getX() + 1, object.getY(), object.getPlane());
        player.setNextForceMovement(new ForceMovement(player, 1, toTile, 3, ForceMovement.EAST));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.setNextWorldTile(toTile);
            }
        }, 2);
    }


    public static void faladorGrappleWall(final Player player, final WorldObject object) {
        if (player.getSkills().getLevel(Skills.AGILITY) < 11
                || player.getSkills().getLevel(Skills.RANGE) < 19 ||
                player.getSkills().getLevel(Skills.STRENGTH) < 37) {
            player.getDialogueManager().startDialogue("You need a Agility level of 11, Ranged level of 19 and a Strength level of 37",
                    "respectively in order to use this obstacle.");
            return;
        }
        player.getStopwatch().delay(13);
        player.setRunHidden(false);
        player.lock(13);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.setNextAnimation(new Animation(442));
                player.setNextGraphics(new Graphics(23));

            }
        }, 1);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.getSkills().addXp(Skills.AGILITY, 2.5);
                if (player.getX() == 3005 && player.getY() == 3392) {
                    player.setNextWorldTile(new WorldTile(3005, 3393, 1));
                    return;
                }

                player.setNextWorldTile(new WorldTile(3006, 3394, 1));

            }
        }, 12);


    }


    public static void faladorTunnel(final Player player, final WorldObject object) {
        if (player.getSkills().getLevel(Skills.AGILITY) < 26) {
            player.getPackets().sendGameMessage("You need a Agility level of 26 in order to use this obstacle.");
            return;
        }
        WorldTasksManager.schedule(new WorldTask() {

            int ticks = 0;
            int id = object.getId();

            @Override
            public void run() {
                boolean withinFalador = id == 9310;
                WorldTile tile = withinFalador ? new WorldTile(2948, 3310, 0) : new WorldTile(2948, 3312, 0);
                player.lock();
                player.getStopwatch().delay(6);
                ticks++;
                if (ticks == 1) {
                    player.setNextAnimation(new Animation(2589));
                    player.setNextForceMovement(new ForceMovement(object, 1, withinFalador ? ForceMovement.SOUTH : ForceMovement.NORTH));
                } else if (ticks == 3) {
                    player.setNextWorldTile(new WorldTile(2948, 3311, 0));
                    player.setNextAnimation(new Animation(2590));
                } else if (ticks == 5) {
                    player.setNextAnimation(new Animation(2591));
                    player.setNextWorldTile(tile);
                } else if (ticks == 6) {
                    player.setNextWorldTile(new WorldTile(tile.getX(), tile.getY() + (withinFalador ? -1 : 1), tile.getPlane()));
                    player.unlock();
                    stop();
                }
            }
        }, 0, 0);
    }

    public static void faladorJumpDown(final Player player, final WorldObject object) {
        player.faceObject(object);
        player.setNextAnimation(new Animation(21477));
        final boolean running = player.getRun();
        player.setRunHidden(false);
        player.lock(2);
        player.getStopwatch().delay(2);
        FadingScreen.fade(player, new Runnable() {

            @Override
            public void run() {
                player.unlock();
                player.setNextAnimation(new Animation(-1));
                if (player.getX() == 3006 && player.getY() == 3394) {
                    player.setNextWorldTile(new WorldTile(3006, 3396, 0));
                    return;
                }
                player.setNextWorldTile(new WorldTile(3005, 3391, 0));
            }
        });
        player.getSkills().addXp(Skills.AGILITY, 0.5);

    }

}
