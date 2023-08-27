package com.rs.game.minigames.lms;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.FloorItem;
import com.rs.game.item.Item;
import com.rs.game.minigames.pktournament.PkTournamentType;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.Consumables;
import com.rs.game.player.content.Drinkables;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.security.InvalidParameterException;

/**
 * @author Simplex
 * created on 2021-02-01
 */
public class LastManStandingGame extends Controller {

    private transient LastManStanding instance;

    @Override
    public void start() {
        if (getArguments() == null || getArguments().length == 0 || !(getArguments()[0] instanceof LastManStanding)) {
            removeControler();
            throw new InvalidParameterException("LMS Game Controller improperly instanced");
        }

        instance = (LastManStanding) getArguments()[0];
        getArguments()[0] = null;
    }

    public void sendInterfaces() {
        boolean started = instance.getGameStartClock().finished();
        player.getInterfaceManager().setOverlay(75, false);
        player.getPackets().sendIComponentText(75, 0,
                "<col=ff981f>Players:<col=ffffff>" + instance.getPlayersInGame().size() + "<br>"
                    + (!started ? "" : "<col=ff981f>Time: <col=ffffff>" + Utils.formatTimeCox(instance.getGameClock().remaining())));
    }

    private boolean countdownFinished() {
        return instance.getGameStartClock().finished();
    }

    @Override
    public boolean canEat(Consumables.Food food) {
        return super.canEat(food);
    }

    @Override
    public boolean canPot(Drinkables.Drink pot) {
        return super.canPot(pot);
    }

    @Override
    public boolean canTakeItem(FloorItem item) {
        return super.canTakeItem(item);
    }

    @Override
    public boolean keepCombating(Entity target) {
        return super.keepCombating(target);
    }

    @Override
    public boolean canEquip(int slotId, int itemId) {
        return super.canEquip(slotId, itemId);
    }

    @Override
    public boolean canRemoveEquip(int slotId, int itemId) {
        return super.canRemoveEquip(slotId, itemId);
    }

    @Override
    public boolean canAttack(Entity target) {
        return super.canAttack(target);
    }

    @Override
    public boolean canPlayerOption1(Player target) {
        return super.canPlayerOption1(target);
    }

    @Override
    public boolean canPlayerOption2(Player target) {
        return super.canPlayerOption2(target);
    }

    @Override
    public boolean canPlayerOption3(Player target) {
        return super.canPlayerOption3(target);
    }

    @Override
    public boolean canPlayerOption4(Player target) {
        return super.canPlayerOption4(target);
    }

    @Override
    public void process() {
        sendInterfaces();
        super.process();
    }

    @Override
    public void magicTeleported(int type) {
        super.magicTeleported(type);
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        return super.processMagicTeleport(toTile);
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        return super.processItemTeleport(toTile);
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        return super.processObjectTeleport(toTile);
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        return super.processObjectClick1(object);
    }

    @Override
    public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        return super.processButtonClick(interfaceId, componentId, slotId, slotId2, packetId);
    }

    @Override
    public boolean processNPCClick1(NPC npc) {
        return super.processNPCClick1(npc);
    }

    @Override
    public boolean processNPCClick2(NPC npc) {
        return super.processNPCClick2(npc);
    }

    @Override
    public boolean processNPCClick3(NPC npc) {
        return super.processNPCClick3(npc);
    }

    @Override
    public boolean processNPCClick4(NPC npc) {
        return super.processNPCClick4(npc);
    }

    @Override
    public boolean processObjectClick2(WorldObject object) {
        return super.processObjectClick2(object);
    }

    @Override
    public boolean processObjectClick3(WorldObject object) {
        return super.processObjectClick3(object);
    }

    @Override
    public boolean processObjectClick4(WorldObject object) {
        return super.processObjectClick4(object);
    }

    @Override
    public boolean processObjectClick5(WorldObject object) {
        return super.processObjectClick5(object);
    }

    @Override
    public boolean sendDeath() {
        Player killer = player.getMostDamageReceivedSourcePlayer();
        player.anim(20836);
        // don't reward final kill
        if(instance.getPlayersInGame().size() != 1) {
            instance.rewardKill(killer);
        }
        killer.sendMessage("You have defeated " + player.getDisplayName() + "!");

        WorldTasksManager.schedule(() -> {

            player.sendMessage("You have been eliminated!");
            // drop all items
            for(Item item : player.getEquipment().getItems().getItems()) {
                if(item != null) {
                    World.addGroundItem(item.clone(), player.clone(), killer, true, 60, 0, 30000);
                }
            }
            for(Item item : player.getInventory().getItems().getItems()) {
                if(item != null) {
                    World.addGroundItem(item.clone(), player.clone(), killer, true, 60, 0, 30000);
                }
            }

            instance.eliminate(player);
            player.anim(-1);
        }, 4);
        return false;
    }

    @Override
    public boolean canMove(int dir) {
        return super.canMove(dir);
    }

    @Override
    public boolean logout() {
        instance.leave(player);
        return super.logout();
    }

    @Override
    public boolean login() {
        PkTournamentType.removeSetup(player);
        player.setNextWorldTile(LastManStanding.OUTSIDE);
        return super.logout();
    }

    @Override
    public boolean canDropItem(Item item) {
        return super.canDropItem(item);
    }

    @Override
    public boolean canSummonFamiliar() {
        return super.canSummonFamiliar();
    }
}
