package com.rs.game.player.controllers.partyroom;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Simplex
 * @since May 01, 2020
 */
public class PartyBalloon {

    Item prize;

    boolean popped = false;

    public PartyBalloon(Item prize) {
        this.prize = prize;
    }

    public static boolean pop(Player player, WorldObject object) {
        PartyBalloonType balloonType = forId(object.getId());

        if(balloonType == null)
            return false;

        player.anim(10017);

        final PartyBalloon balloon = PartyRoom.balloons.get(object);
        // multiple players may get to this point, same as rs
        // only reward the first (PID ftw)
        if(balloon == null || balloon.popped) {
            player.sendMessage("You weren't quick enough!");
        } else {
            balloon.popped = true;
            PartyRoom.balloons.remove(object);

            if(balloon.prize != null) {
                World.addGroundItem(balloon.prize, new WorldTile(object.getX(), object.getY(), 0), player, true, 60);
            } else {
                player.sendMessage("There was nothing in the balloon!");
            }
        }

        World.removeObject(object);

        World.spawnObjectTemporary(new WorldObject(balloonType.popId, object.getType(), object.getRotation(),
                object.getX(), object.getY(), object.getPlane()),1200);
        return true;
    }

    public static PartyBalloonType forId(int id) {
        for (PartyBalloonType balloon : PartyBalloonType.values()) {
            if (balloon.balloonId == id) {
                return balloon;
            }
        }
        return null;
    }

    enum PartyBalloonType {
        YELLOW(115, 123), RED(116, 124), BLUE(117, 125), GREEN(118, 126), PURPLE(119, 127), WHITE(120, 128), GREEN_BLUE(121, 129), TRI(122, 130);

        private int balloonId;

        private int popId;


        PartyBalloonType(int balloonId, int popId) {
            this.balloonId = balloonId;
            this.popId = popId;
        }

        public static PartyBalloonType getRandom() {
            return values()[Utils.random(values().length-1)];
        }

        public int getBalloonId() {
            return balloonId;
        }
    }
}