package com.rs.game.npc.others.zalcano;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.FloorItem;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.NPCDrops;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simplex
 * @since May 30, 2020
 */
public class Golem extends NPC {
    public Golem(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
    }

    static boolean init = false;

    public static void init() {
        if(!init) {
            Drops drops = new Drops(false);
            @SuppressWarnings("unchecked")
            List<Drop>[] dList = new ArrayList[Drops.VERY_RARE + 1];
            for (int i = 0; i < dList.length; i++)
                dList[i] = new ArrayList<Drop>();
            dList[Drops.ALWAYS].add(new Drop(Zalcano.IMBUED_TEPHRA, 16, 24));
            drops.addDrops(dList);
            NPCDrops.addDrops(Zalcano.GOLEM_ID, drops);
            init = true;
        }
    }

    static FloorItem tephraDropped = null;

    public static void removeDroppedTephra() {
        // need to track in case zalc dies
        if(tephraDropped != null) {
            World.removeGroundItem(tephraDropped);
            tephraDropped = null;
        }
    }

    @Override
    public void drop() {
        Player plr = getMostDamageReceivedSourcePlayer();
        int size = getSize();
        Item tephra = new Item(Zalcano.IMBUED_TEPHRA, Utils.random(16, 24));
        tephraDropped = World.addGroundItem(tephra, new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane()), plr, true, 30);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                removeDroppedTephra();
                stop();
            }
        }, 15, 1);
    }

    @Override
    public void sendDeath(Entity source) {
        Zalcano.finishGolem();
        super.sendDeath(source);
    }
}
