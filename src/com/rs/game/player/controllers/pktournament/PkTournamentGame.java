package com.rs.game.player.controllers.pktournament;

import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.item.Item;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class PkTournamentGame extends Controller {

    Player target;

    private int time;

    @Override
    public void start() {
        target = (Player) getArguments()[0];
        sendInterfaces();
    }

    @Override
    public boolean canDropItem(Item item) {
        player.sendMessage("You cannot drop items in PK Tournaments.");
        return false;
    }

    @Override
    public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        switch (interfaceId) {
            case 182:
                player.sendMessage("You can not logout during a pvp tournament!");
                return false;
            case 271:
            case 749:
                if (componentId == 4 || (componentId == 8 && slotId >= 16 && slotId <= 18)) {
                    if (PkTournament.getType().prayerDisabled()) {
                        player.sendMessage("Protection prayers are disabled in " + PkTournament.getType().getFormattedName() + " PK Tournaments!");
                        return false;
                    }
                }
                break;
        }


        return true;
    }

    @Override
    public boolean canAttack(Entity target) {
        if (target != this.target)
            return false;
        time = 0;
        return true;
    }

    @Override
    public boolean canHit(Entity target) {
        if (target != this.target)
            return false;
        time = 0;
        return true;
    }


    @Override
    public void process() {
        if (player.isDead() || target.isDead()) {
            time = 0;
            return;
        }
        time++;

        if (player.hasHits() || target.hasHits())
            return;


        if (time >= 500) {
            if ((time - 500) % 25 == 0) //250damage every 15secs after 10min to prevent long fights.
                player.applyHit(new Hit(target, 250, HitLook.REGULAR_DAMAGE));
        } else if (time >= 400) {
            if ((time - 400) % 25 == 0) //200dmg every 15secs after 8min to prevent long fights.
                player.applyHit(new Hit(target, 200, HitLook.REGULAR_DAMAGE));
        } else if (time >= 300) {
            if ((time - 300) % 25 == 0) //150dmg every 15secs after 6min to prevent long fights.
                player.applyHit(new Hit(target, 150, HitLook.REGULAR_DAMAGE));
        } else if (time >= 200) {
            if ((time - 200) % 25 == 0) //100dmg every 15secs after 4min to prevent long fights.
                player.applyHit(new Hit(target, 100, HitLook.REGULAR_DAMAGE));
        } else if (time >= 100) {
            if ((time - 100) % 25 == 0) //50dmg every 15secs after 2min to prevent long fights.
                player.applyHit(new Hit(target, 50, HitLook.REGULAR_DAMAGE));
        }

    }

    @Override
    public boolean processNPCClick2(NPC n) {
        return true;
    }

    @Override
    public boolean canRemoveEquip(int slot, int item) {
        return true;
    }

    public void leave() {
        player.getInterfaceManager().removeOverlay(false);
        PkTournament.handleRemoval(player);
    }

    @Override
    public void sendInterfaces() {
        player.getInterfaceManager().setOverlay(1194, false);
        player.getPackets().sendIComponentText(1194, 7, player.getDisplayName());
        player.getPackets().sendIComponentText(1194, 6, target == null ? "Unknown" : target.getDisplayName());

        player.getPackets().sendIComponentText(1194, 8,
                "Round " + (PkTournament.round + 1));
    }


    @Override
    public boolean sendDeath() {
        player.lock(8);
        player.stopAll();
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    player.setNextAnimation(new Animation(836));
                } else if (loop == 1) {
                    player.getPackets().sendGameMessage("Oh dear, you have died.");
                } else if (loop == 3) {
                    PkTournament.endBracket(target, player, false);
                } else if (loop == 4) {
                    player.getPackets().sendMusicEffect(90);
                    player.anim(-1);
                    player.unlock();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
        return false;
    }

    @Override
    public boolean logout() {
        player.setLocation(new WorldTile(3086, 3498, 0));
        PkTournament.handleRemoval(player);
        return true;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
        return false;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
        return false;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
        return false;
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        int id = object.getId();

        if(id == 132755) {
            player.sendMessage("You can not leave the fight! Kill your opponent or die trying!");
            return false;
        }

        return true;
    }

    @Override
    public boolean processObjectClick2(WorldObject object) {
        return true;
    }

    @Override
    public void magicTeleported(int type) {
        removeControler();
        leave();
    }

    @Override
    public void forceClose() {
        leave();
    }

    public Player getTarget() {
        return target;
    }
}
