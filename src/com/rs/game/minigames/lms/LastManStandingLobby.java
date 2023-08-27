package com.rs.game.minigames.lms;

import com.rs.game.Entity;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.minigames.pktournament.PkTournamentType;
import com.rs.game.npc.NPC;
import com.rs.game.player.controllers.Controller;
import com.rs.utils.Utils;

import java.security.InvalidParameterException;

/**
 * @author Simplex
 * created on 2021-02-01
 */
public class LastManStandingLobby extends Controller {

    private transient LastManStanding instance;

    @Override
    public void start() {
        if (getArguments() == null || getArguments().length == 0 || !(getArguments()[0] instanceof LastManStanding)) {
            removeControler();
            throw new InvalidParameterException("LMS Lobby Controller improperly instanced");
        }

        instance = (LastManStanding) getArguments()[0];
        getArguments()[0] = null;

        sendInterfaces();
    }

    @Override
    public boolean login() {
        PkTournamentType.removeSetup(player);
        player.setNextWorldTile(LastManStanding.OUTSIDE);
        return super.logout();
    }

    public void sendInterfaces() { //TODO
        player.getInterfaceManager().setOverlay(75, false);
        player.getPackets().sendIComponentText(75, 0, "<col=ff981f>Lobby: <col=ffffff>" + Utils.formatTime(instance.getLobbyClock().remaining()));
    }

    @Override
    public boolean processNPCClick2(NPC n) {
        return true;
    }

    @Override
    public boolean canRemoveEquip(int slot, int item) {
        return true;
    }

    @Override
    public boolean canAttack(Entity target) {
        return canHit(target);
    }

    @Override
    public boolean canSummonFamiliar() {
        return false;
    }

    @Override
    public boolean canDropItem(Item item) {
        player.sendMessage("You cannot drop items in PK Tournaments.");
        return false;
    }


    public void leave() {
        removeControler();
        player.getInterfaceManager().removeOverlay(false);
        instance.leave(player);
    }

    @Override
    public void process() {
        sendInterfaces();
    }


    @Override
    public boolean logout() {
        leave();
        return true;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        leave();
        return true;
    }
    @Override
    public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        return true;
    }
    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        leave();
        return true;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        leave();
        return true;
    }

    @Override
    public boolean sendDeath() {
        player.reset();
        player.setNextWorldTile(LastManStanding.LOBBY);
        return false;
    }


    public boolean processObjectClick1(WorldObject object) {
        return true;
    }

    @Override
    public boolean processObjectClick2(WorldObject object) {
        int id = object.getId();
        return true;
    }

    @Override
    public void magicTeleported(int type) {
        leave();
    }

    @Override
    public void forceClose() {
        leave();
    }
}
