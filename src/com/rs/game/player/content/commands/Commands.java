package com.rs.game.player.content.commands;

import com.rs.GameLauncher;
import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ItemConfig;
import com.rs.cache.loaders.NPCConfig;
import com.rs.discord.Bot;
import com.rs.executor.GameExecutorManager;
import com.rs.game.Graphics;
import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.minigames.*;
import com.rs.game.minigames.clanwars.ClanWars;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.minigames.clanwars.WallHandler;
import com.rs.game.minigames.lms.LastManStanding;
import com.rs.game.minigames.lms.LastManStandingState;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.minigames.pktournament.PkTournamentType;
import com.rs.game.minigames.stealingcreation.GameArea;
import com.rs.game.minigames.stealingcreation.StealingCreationController;
import com.rs.game.minigames.stealingcreation.StealingCreationManager;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.impl.NexCombat;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.npc.nightmare.TheNightmare;
import com.rs.game.npc.others.Mimic;
import com.rs.game.npc.others.zalcano.Zalcano;
import com.rs.game.npc.randomEvent.CombatEventNPC;
import com.rs.game.npc.zulrah.Zulrah;
import com.rs.game.player.*;
import com.rs.game.player.actions.HomeTeleport;
import com.rs.game.player.actions.woodcutting.DreamTreeWoodcutting;
import com.rs.game.player.content.*;
import com.rs.game.player.content.Slayer.SlayerMaster;
import com.rs.game.player.content.Summoning.Pouch;
import com.rs.game.player.content.box.HalloweenBox;
import com.rs.game.player.content.box.MoneyBox;
import com.rs.game.player.content.box.MysteryBox;
import com.rs.game.player.content.box.MysteryGodBox;
import com.rs.game.player.content.collectionlog.CategoryType;
import com.rs.game.player.content.dungeoneering.*;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.ChambersRewards;
import com.rs.game.player.content.seasonalEvents.Easter2021;
import com.rs.game.player.content.seasonalEvents.HalloBoss;
import com.rs.game.player.content.seasonalEvents.XmasBoss;
import com.rs.game.player.content.surpriseevents.*;
import com.rs.game.player.content.teleportation.TeleportationInterface;
import com.rs.game.player.controllers.*;
import com.rs.game.player.controllers.partyroom.PartyBalloon;
import com.rs.game.player.controllers.partyroom.PartyRoom;
import com.rs.game.player.cutscenes.NexCutScene;
import com.rs.game.route.Flags;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.WalkRouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.LoginClientChannelManager;
import com.rs.net.LoginProtocol;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.net.encoders.LoginChannelsPacketEncoder;
import com.rs.tools.DupeChecker;
import com.rs.utils.*;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * doesnt let it be extended
 */
public final class Commands {

    /*
     * all console commands only for admin, chat commands processed if they not
     * processed by console
     */

    /**
     * returns if command was processed
     */
    public static boolean processCommand(Player player, String command, boolean console, boolean clientCommand) {
        if (command.length() == 0 || player.isLobby() || player.isLocked()) // if they used ::(nothing) theres no command
            return false;

        if (!player.checkBankPin()) {
            player.sendMessage("You must type your bank PIN first!");
            return false;
        }

        if (player.connectedThroughVPN || player.hasNewPlayerController())
            return false;

        String[] cmd = command.split(" ");
        if (cmd.length == 0)
            return false;
        archiveLogs(player, cmd);
        Bot.sendLog(Bot.COMMAND_CHANNEL, "[type=COMMAND][name=" + player.getUsername() + "][message=::" + command + "]");

        if ((player.isAdmin() || player.getUsername().equalsIgnoreCase("nick"))
                && processHiddenCommand(player, cmd, console, clientCommand))
            return true;
        if (player.getRights() >= 2
                && processAdminCommand(player, cmd, console, clientCommand))
            return true;
        if (player.getRights() >= 1
                && processModCommand(player, cmd, console, clientCommand))
            return true;
        if ((player.isSupporter() || player.getRights() >= 1)
                && processSupportCommands(player, cmd, console, clientCommand))
            return true;
        if ((player.isSupporter() || player.getRights() >= 1)
                && processPunishmentCommand(player, cmd, console, clientCommand))
            return true;
        if (!Settings.SPAWN_WORLD) {
            if (processNormalCommand(player, cmd, console, clientCommand))
                return true;
            return EconomyManager.teleport(player, cmd[0]);
        } else {
            return processNormalCommand(player, cmd, console, clientCommand) || processNormalSpawnCommand(player, cmd, console, clientCommand);
        }
    }

    public static boolean processPunishmentCommand(final Player player, String[] cmd, boolean console, boolean clientCommand) {
        if (clientCommand)
            return false;
        switch (cmd[0].toLowerCase()) {
            case "teleall":
                if (!player.getUsername().equalsIgnoreCase("dragonkk")) {
                    player.getPackets().sendGameMessage("White-list only!");
                    return true;
                }

                AtomicInteger count = new AtomicInteger(0);
                List<WorldTile> positions = player.area(3, worldTile -> !worldTile.matches(player) && World.isTileFree(worldTile, 1));

                World.getPlayers().forEach(target -> {
                    if (target != player && target != null && target.getControlerManager().getControler() == null) {
                        count.getAndIncrement();
                        target.stopAll();
                        target.lock(1);
                        target.gfx(1521);
                        target.setNextWorldTile(positions.size() > 0 ? Utils.get(positions) : player.clone());
                        WorldTasksManager.schedule(() -> target.faceEntity(player));
                        target.sendMessage(Colour.CRIMSON.wrap(target.getDisplayName()) + " has been force-teleported you to their position!");
                    }
                });

                player.gfx(343);
                player.anim(1818);
                player.sendMessage(Colour.RED.wrap(count.toString() + " players have been force-moved to your position, if they had a controller they were not moved."));
                return true;
            case "teletomef":
                if (player.getRights() < 1) {
                    player.getPackets().sendGameMessage("Mod+ only!");
                    return true;
                }

                String name = "";
                for (int i = 1; i < cmd.length; i++)
                    name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                Player target = World.getPlayerByDisplayName(name);
                if (target == null)
                    player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                else {
                    List<WorldTile> p = player.area(1, worldTile -> !worldTile.matches(player) && World.isTileFree(worldTile, 1));

                    player.gfx(343);
                    player.anim(1818);
                    player.sendMessage(Colour.CRIMSON.wrap(target.getDisplayName()) + " has been force-moved to your position, if they had a controller it has been force-ended.");

                    target.stopAll();
                    target.getControlerManager().forceStop();
                    target.lock(1);
                    target.gfx(1521);
                    target.setNextWorldTile(p.size() > 0 ? Utils.get(p) : player.clone());
                    WorldTasksManager.schedule(() -> target.faceEntity(player));
                    target.sendMessage(Colour.CRIMSON.wrap(target.getDisplayName()) + " has been force-teleported you to their position!");
                }
                return true;
            case "teletome":
                if (player.getRights() < 1) {
                    player.getPackets().sendGameMessage("Admin+ only!");
                    return true;
                }
                name = "";
                for (int i = 1; i < cmd.length; i++)
                    name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                target = World.getPlayerByDisplayName(name);
                if (target == null)
                    player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                else {
                    if (target.getControlerManager().getControler() != null) {
                        player.sendMessage("This player is in an activity. Use ::sendhome instead!");
                        return true;
                    }
                    target.lock(15);
                    performTeleEmote(target);
                    final Player _target = target;
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            _target.setNextAnimation(new Animation(-1));
                            _target.setNextWorldTile(player);

                        }
                    }, 5);
                }
                return true;
            case "unban":
                if (player.getRights() < 2) {
                    player.getPackets().sendGameMessage("Mod+ only!");
                    return true;
                }

                name = "";
                for (int i = 1; i < cmd.length; i++)
                    name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                name = Utils.formatPlayerNameForDisplay(name);
                LoginClientChannelManager.sendUnreliablePacket(LoginChannelsPacketEncoder.encodeRemoveOffence(LoginProtocol.OFFENCE_REMOVETYPE_BANS, name, player.getUsername()).trim());
                return true;
            case "unmute":
                if (player.getRights() < 1) {
                    player.getPackets().sendGameMessage("Mod+ only!");
                    return true;
                }

                name = "";
                for (int i = 1; i < cmd.length; i++)
                    name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                name = Utils.formatPlayerNameForDisplay(name);
                LoginClientChannelManager.sendUnreliablePacket(LoginChannelsPacketEncoder.encodeRemoveOffence(LoginProtocol.OFFENCE_REMOVETYPE_MUTES, name, player.getUsername()).trim());
                return true;
            case "kick":
                player.disconnect(true, true);
                return true;
            case "save":
                GameLauncher.savePlayers();
                World.getPlayers().forEach(player1 -> {
                    if (player1 != null && (player1.isAdmin() || player1.isModerator())) {
                        player1.sendMessage(player1.getDisplayName() + " is force saving all files.");
                    }
                });
                return true;

            case "rightclickipban":
                player.getTemporaryAttributtes().put("RIGHTCLICKBAN", Integer.valueOf(LoginProtocol.OFFENCE_ADDTYPE_IPBAN));
                player.sendMessage("Right click punishments are now perm. IP bans.");
                return true;
            case "rightclickban":
                player.getTemporaryAttributtes().put("RIGHTCLICKBAN", Integer.valueOf(LoginProtocol.OFFENCE_ADDTYPE_BAN));
                player.sendMessage("Right click punishments are now perm. bans.");
                return true;
            case "rightclickmute":
                player.getTemporaryAttributtes().put("RIGHTCLICKBAN", Integer.valueOf(LoginProtocol.OFFENCE_ADDTYPE_MUTE));
                player.sendMessage("Right click punishments are now perm. mutes.");
                return true;
            case "rightclickipmute":
                player.getTemporaryAttributtes().put("RIGHTCLICKBAN", Integer.valueOf(LoginProtocol.OFFENCE_ADDTYPE_IPMUTE));
                player.sendMessage("Right click punishments are now perm. IP mutes.");
                return true;
            case "punish":
                name = "";
                for (int i = 1; i < cmd.length; i++)
                    name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                name = Utils.formatPlayerNameForDisplay(name);
                if (name.equalsIgnoreCase("dragonk")) {
                    player.sendMessage("Reality is often disappointing, " + player.getDisplayName() + ".");
                    return true;
                }
                player.getDialogueManager().startDialogue("AddOffenceD", name);
                return true;
            case "forcekick":
                if (player.getRights() < 1) {
                    player.getPackets().sendGameMessage("Mod+ only!");
                    return true;
                }

                name = "";
                for (int i = 1; i < cmd.length; i++)
                    name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                target = World.getPlayerByDisplayName(name);
                if (target == null) {
                    player.getPackets().sendGameMessage(Utils.formatPlayerNameForDisplay(name) + " is not logged in.");
                    return true;
                }
                target.disconnect(true, false);
                target.finish();
                player.getPackets().sendGameMessage("You have kicked: " + target.getDisplayName() + ".");
                return true;
            case "sendhome":

                name = "";
                for (int i = 1; i < cmd.length; i++)
                    name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                target = World.getPlayerByDisplayName(name);
                if (target == null)
                    player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                else {
                  /*  if (target.getControlerManager().getControler() instanceof FfaZone) {
                        player.getPackets().sendGameMessage("You can't teleport people out of spawn zone!");
                        return true;
                    }*/
                    target.unlock();
                    target.getControlerManager().forceStop();
                    if (target.getNextWorldTile() == null)
                        target.setNextWorldTile(Settings.START_PLAYER_LOCATION);
                    player.getPackets().sendGameMessage("You have unnulled: " + target.getDisplayName() + ".");
                    return true;
                }
                return true;
            default:
                return false;
        }
    }

    private static boolean trollRunning = false;
    private static String trollTarget = null;

    private static TimerTask prjDebugTask = null;
    private static int prjDebugInterval = 600;
    private static int prjDebugTarget = -1, prjDebugStartAnim = -1, prjDebugStartGfx = -1, prjDebugPrjGfx = -1, prjDebugDestAnim = -1, prjDebugDestGfx = -1, prjDebugStartHeight = -1,
            prjDebugEndHeight = -1, prjDebugDelay = -1, prjDebugSpeed = -1, prjDebugSlope = -1, prjDebugAngle = -1;

    private static SurpriseEvent tst;

    public static boolean processHiddenCommand(final Player player, String[] cmd, boolean console, boolean clientCommand) {
        if (!clientCommand) {
            switch (cmd[0].toLowerCase()) {
                case "multi1flower":
                case "m1f":
                    Commands.fakeFlower(player, 2980);
                    return true;
                case "redflower":
                case "rf":
                    Commands.fakeFlower(player, 2981);
                    return true;
                case "blueflower":
                case "bf":
                    Commands.fakeFlower(player, 2982);
                    return true;
                case "yellowflower":
                case "yf":
                    Commands.fakeFlower(player, 2983);
                    return true;
                case "purpleflower":
                case "pf":
                    Commands.fakeFlower(player, 2984);
                    return true;
                case "orangeflower":
                case "of":
                    Commands.fakeFlower(player, 2985);
                    return true;
                case "multi2flower":
                case "m2f":
                    Commands.fakeFlower(player, 2986);
                    return true;
                case "whiteflower":
                case "wf":
                    Commands.fakeFlower(player, 2987);
                    return true;
                case "blackflower":
                case "blf":
                    Commands.fakeFlower(player, 2988);
                    return true;
                case "roll2":
                    player.setNextRoll(Integer.parseInt(cmd[1]));
                    return true;
            }
        }
        return false;
    }


    public static boolean processAdminCommand(final Player player, String[] cmd, boolean console, boolean clientCommand) {

        if (clientCommand) {
            switch (cmd[0]) {
                case "tele":
                    cmd = cmd[1].split(",");
                    int plane = Integer.valueOf(cmd[0]);
                    int x = Integer.valueOf(cmd[1]) << 6 | Integer.valueOf(cmd[3]);
                    int y = Integer.valueOf(cmd[2]) << 6 | Integer.valueOf(cmd[4]);
                    player.setNextWorldTile(new WorldTile(x, y, plane));
                    return true;
            }
        } else {
            String name;
            Player target;
            WorldObject object;
            switch (cmd[0].toLowerCase()) {
		/*	case "pc":
				new PestControl(Arrays.asList(player), PestControl.PestData.NOVICE).create();
				return true;*/
                case "hideui":
                    //tabs
                    player.getPackets().sendHideIComponent(746, 55, true);
                    player.getPackets().sendHideIComponent(746, 57, true);
                    player.getPackets().sendHideIComponent(746, 31, true);
                    player.getPackets().sendHideIComponent(746, 75, true);
                    //chat
                    player.getPackets().sendHideIComponent(746, 21, true);
                    player.getPackets().sendHideIComponent(746, 22, true);
                    //orb
                    player.getPackets().sendHideIComponent(746, 13, true);
                    player.getPackets().sendHideIComponent(746, 54, true);
                    player.getPackets().sendHideIComponent(746, 207, true);
                    return true;
                case "unhideui":
                    //tabs
                    player.getPackets().sendHideIComponent(746, 55, false);
                    player.getPackets().sendHideIComponent(746, 57, false);
                    player.getPackets().sendHideIComponent(746, 31, false);
                    player.getPackets().sendHideIComponent(746, 75, false);
                    //chat
                    player.getPackets().sendHideIComponent(746, 21, false);
                    player.getPackets().sendHideIComponent(746, 22, false);
                    //orb
                    player.getPackets().sendHideIComponent(746, 13, false);
                    player.getPackets().sendHideIComponent(746, 54, false);
                    player.getPackets().sendHideIComponent(746, 207, false);
                    return true;
                case "tobloot":
                    TheatreOfBlood.getRewardCalc(player);
                    player.sendMessage("Looted!");
                    return true;
                case "lastrefs":
                    ReferralSystem.viewRefs(player);
                    return true;
                case "refr":
                    ReferralSystem.viewRefList(player);
                    return true;
                case "addrefr":
                    ReferralSystem.addReward(cmd[1], Integer.parseInt(cmd[2]), Integer.parseInt(cmd[3]));
                    player.getPackets().sendGameMessage("Complete.");
                    return true;
                case "removerefr":
                    boolean removed = ReferralSystem.removeReward(cmd[1]);
                    player.getPackets().sendGameMessage("Complete. " + removed);
                    return true;
                case "state":
                    int state = Integer.parseInt(cmd[1]);
                    player.getPackets().sendClientState(state);
                    player.getPackets().sendGameMessage("Client state: " + state + ".");
                    return true;
                case "roll":
                    try {
                        if (cmd.length < 2) {
                            player.getPackets().sendPanelBoxMessage("Use: ::roll name");
                            return true;
                        }
                        int right = Integer.parseInt(cmd[1]);
                        name = "";
                        for (int i = 2; i < cmd.length; i++)
                            name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                        if (name.length() <= 0) {
                            player.getPackets().sendGameMessage("bad name.");
                            return true;
                        }
                        target = World.getPlayerByDisplayName(name);
                        if (target == null) {
                            player.getPackets().sendGameMessage("Your target is currently offline.");
                            return true;
                        }
                        target.setNextRoll(right);
                        player.getPackets().sendGameMessage("Your target next roll is: " + right + ".");
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::roll name");
                    }
                    return true;
                case "restartjda":
                    Bot.restart();
                    player.getPackets().sendGameMessage("Restarted discord.");
                    return true;
                case "conditiontask":
                    WorldTasksManager.WorldTaskList worldTaskList = new WorldTasksManager.WorldTaskList();
                    worldTaskList.add(new WorldTask() {
                        int state = 0;

                        @Override
                        public void run() {
                            if (++state == 1)
                                player.forceTalk("state1");
                            else if (state == 2)
                                player.forceTalk("state2");
                            else {
                                player.forceTalk("statefin");
                                stop();
                            }
                        }
                    });
                    worldTaskList.delay(1);
                    worldTaskList.add(new WorldTask() {
                        int state = 0;
                        int delay = 0;

                        @Override
                        public void run() {
                            if (state == 0) {
                                player.anim(8990);
                                state = 1;
                                delay = AnimationDefinitions.getAnimationDefinitions(8990).getEmoteTime() / 600;
                                player.forceTalk("start anim, delay= " + delay);
                            } else {
                                if (delay-- <= 0) {
                                    stop();
                                    player.forceTalk("anim task fin");
                                } else {
                                    player.forceTalk("waiting for anim to finish " + delay);
                                }
                            }
                        }
                    });
                    worldTaskList.add(new WorldTask() {
                        @Override
                        public void run() {
                            player.forceTalk("fin");
                            stop();
                        }
                    });
                    worldTaskList.execute2t();
                    break;
                case "sequentiallist":
                    worldTaskList = new WorldTasksManager.WorldTaskList();
                    worldTaskList.add(() -> {
                        player.forceTalk("starting task list");
                    });
                    for (int j = 0; j < 5; j++) {
                        final int J = j;
                        worldTaskList.add(() -> {
                            player.forceTalk("waiting " + J);
                        });
                    }
                    worldTaskList.add(() -> {
                        player.forceTalk("moving 1 south");
                        player.addWalkSteps(player.getX(), player.getY() - 1);
                    });
                    worldTaskList.delay(1);
                    worldTaskList.add(() -> {
                        player.forceTalk("plant flowers");
                        //Gambling.plantMithrilSeeds(player);
                    });
                    worldTaskList.delay(4);
                    worldTaskList.add(() -> {
                        player.forceTalk("tasks complete");
                    });
                    worldTaskList.execute2t();
                    break;
                case "deiron":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::deiron name");
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    if (name.length() <= 0) {
                        player.getPackets().sendGameMessage("bad name.");
                        return true;
                    }
                    target = World.getPlayerByDisplayName(name);
                    if (target == null) {
                        player.getPackets().sendGameMessage("Your target is currently offline.");
                        return true;
                    }
                    target.setHcPartner(null);
                    target.getPackets().sendGameMessage("HC partner has been removed.");
                    player.getPackets().sendGameMessage("You have removed hc partner.");
                    return true;
                case "rights":
                    try {
                        if (cmd.length < 2) {
                            player.getPackets().sendPanelBoxMessage("Use: ::rights name");
                            return true;
                        }
                        int right = Integer.parseInt(cmd[1]);
                        name = "";
                        for (int i = 2; i < cmd.length; i++)
                            name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                        if (name.length() <= 0) {
                            player.getPackets().sendGameMessage("bad name.");
                            return true;
                        }
                        if (name.equalsIgnoreCase("dragonkk")) {
                            player.sendMessage("A fool thinks himself to be wise but a wise man knows himself to be a fool.");
                            return true;
                        }
                        target = World.getPlayerByDisplayName(name);
                        if (target == null) {
                            player.getPackets().sendGameMessage("Your target is currently offline.");
                            return true;
                        }
                        target.setRights(right < 3 ? right : 0);
                        target.setSupporter(right == 3);
                        target.setEventCoordinator(right == 4);
                        target.setYoutuber(right == 5);
                        //	target.setDonator(right < 5 ? 0 : (right - 5));
                        target.sendAccountRank();
                        target.getPackets().sendGameMessage("You have been promoted. Please relog.");
                        player.getPackets().sendGameMessage("Your target has been promoted to rights " + right + ".");
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::rights name");
                    }
                    return true;
                case "donator":
                    try {
                        if (cmd.length < 2) {
                            player.getPackets().sendPanelBoxMessage("Use: ::donator name");
                            return true;
                        }
                        int right = Integer.parseInt(cmd[1]);
                        name = "";
                        for (int i = 2; i < cmd.length; i++)
                            name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                        if (name.length() <= 0) {
                            player.getPackets().sendGameMessage("bad name.");
                            return true;
                        }
                        target = World.getPlayerByDisplayName(name);
                        if (target == null) {
                            player.getPackets().sendGameMessage("Your target is currently offline.");
                            return true;
                        }
                        target.setDonator(right);
                        target.sendAccountRank();
                        target.getPackets().sendGameMessage("You have been promoted. Please relog.");
                        player.getPackets().sendGameMessage("Your target has been promoted to donator " + right + ".");
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::rights name");
                    }
                    return true;
                case "supporttest":
                    player.setRights(0);
                    player.setSupporter(true);
                    return true;
                case "taskc":
                    player.getSlayerManager().autoComplete();
                    return true;
                case "randomevent":
                    CombatEventNPC.startRandomEvent(player, Integer.parseInt(cmd[1]));
                    return true;
                case "corruptxp":
                    int skillid = Integer.parseInt(cmd[1]);
                    target = World.getPlayer(cmd[2]);
                    if (target != null)
                        target.getSkills().setXp(skillid, 14000000);
                    return true;
                case "anon":
                    player.getAppearence().setIdentityHide(!player.getAppearence().isIdentityHidden());
                    return true;
                case "starttst":
                    tst = new TeamVsTeam();
                    tst.start();
                    return true;
                case "lmscrt":
                    tst = new LastManStandingSurpriseEvent();
                    tst.start();
                    return true;
                case "lmsjoin":
                    if (tst == null)
                        return true;
                    tst.tryJoin(player);
                    return true;
                case "evearena":
                    EventArena a = ArenaFactory.randomEventArena(true);
                    if (a != null) {
                        a.create();
                        player.getPackets().sendGameMessage("Pos:" + a.minX() + "," + a.minY());

                        player.setForceNextMapLoadRefresh(true);
                        player.loadMapRegions();
                        player.setNextWorldTile(new WorldTile(a.minX(), a.minY(), 0));
                    }
                    break;
                case "costumecolor":
                    SkillCapeCustomizer.costumeColorCustomize(player);
                    break;
                case "comp":
                    player.setCapturedCastleWarsFlag();
                    player.setCompletedFightCaves();
                    return true;
                case "blatest":
                    //player.getInterfaceManager()
                    return true;
                case "costume":
                    player.getEquipment().setCostume(Costumes.values()[Integer.parseInt(cmd[1])]);
                    break;
                case "setprice":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: ::setprice i i");
                        return true;
                    }
                    GrandExchange.setPrice(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
                    GrandExchange.savePrices();
                    player.getPackets().sendGameMessage("Done! " + ItemConfig.forID(Integer.parseInt(cmd[1])).getName() + " setted to: " + Utils.getFormattedNumber(Integer.parseInt(cmd[2])));
                    return true;
                case "decantt":
                    return true;
                case "floorf":
                    System.out.println(World.isFloorFree(player.getPlane(), player.getX(), player.getY()));
                    return true;
                case "leak":
                    GameExecutorManager.fastExecutor.scheduleAtFixedRate(new TimerTask() {

                        @Override
                        public void run() {
                            if (player.hasFinished()) {
                                cancel();
                                return;
                            }
                            player.setForceNextMapLoadRefresh(true);
                            player.loadMapRegions();

                        }

                    }, 0, 5000);
                    return true;
                case "startsurpriselms":
                    if (player.getRights() < 2) {
                        player.getPackets().sendGameMessage("Admin+ only!");
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    if (name.length() <= 0) {
                        player.getPackets().sendGameMessage("bad name.");
                        return true;
                    }
                    if (player.getControlerManager().getControler() != null) {
                        player.getPackets().sendGameMessage("You can't start event here");
                        return true;
                    }
                    EconomyManager.startEvent(name, null, new LastManStandingSurpriseEvent());
                    return true;
                case "starttvt":
                    if (player.getRights() < 2) {
                        player.getPackets().sendGameMessage("Admin+ only!");
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    if (name.length() <= 0) {
                        player.getPackets().sendGameMessage("bad name.");
                        return true;
                    }
                    if (player.getControlerManager().getControler() != null) {
                        player.getPackets().sendGameMessage("You can't start event here");
                        return true;
                    }
                    EconomyManager.startEvent(name, null, new TeamVsTeam());
                    return true;
                case "rdrops":
                    NPCDrops.init();
                    player.getPackets().sendGameMessage("Reloaded drops.");
                    return true;
                case "reloadshops":
                    ShopsHandler.forceReload();
                    return true;
                case "shop":
                    ShopsHandler.openShop(player, Integer.parseInt(cmd[1]));
                    return true;
                case "resethouse":
                    player.getHouse().reset();
                    player.getPackets().sendGameMessage("Reseted!");
                    return true;
                case "pestpoints":
                    player.setPestPoints(550);
                    return true;
                case "resetbts":
                    BossTimerScore.remove("Theatre of Blood");
                    BossTimerScore.remove("Zulrah");
                    BossTimerScore.remove("Kalphite Queen");
                    return true;
                case "slayerpoints":
                    player.getSlayerManager().setPoints(500);
                    return true;
                case "hide":
                    player.getAppearence().setHidden(!player.getAppearence().isHidden());
                    player.getPackets().sendGameMessage("Hidden:" + player.getAppearence().isHidden());
                    return true;
                case "hwboss":
                    if (!HalloBoss.forceSpawn()) {
                        player.sendMessage("The boss is already up.");
                    } else {
                        player.sendMessage("Halloween boss force spawned. (this does not reset boss timer)");
                    }
                    break;

                case "togglecox":
                    ChambersOfXeric.ENABLED = !ChambersOfXeric.ENABLED;
                    World.sendNews("<img=2> Chambers of Xeric has been " + (ChambersOfXeric.ENABLED ? "enabled" : "disabled"), 1);
                    break;

                case "twistedolmlet":
                    player.setUnlockedTwistedOlmlet(!player.hasUnlockedTwistedOlmlet());
                    player.sendMessage("Unlocked = " + player.hasUnlockedTwistedOlmlet());
                    break;
                case "forcecoxloot":

                    if (World.getPlayer(cmd[1]) == null) {
                        player.sendMessage(cmd[1] + " is offline");
                    } else {
                        player.sendMessage(cmd[1] + " will receive 1x " + new Item(ChambersRewards.forcedLootId));
                        ChambersRewards.forcedLootName = cmd[1];
                        ChambersRewards.forcedLootId = Integer.parseInt(cmd[2]);
                    }
                    break;
                case "enablerandoms":
                    Settings.DISABLE_RANDOM_EVENTS = false;
                    player.sendMessage("Random events enabled.");
                    break;
                case "disablerandomevents":
                    Settings.DISABLE_RANDOM_EVENTS = true;
                    player.sendMessage("Random events disabled.");
                    break;
                case "maxdung":
                    player.getDungManager().setMaxComplexity(6);
                    player.getDungManager().setMaxFloor(60);
                    return true;
                case "sprite":
                    for (int i = 0; i < 100; i++)
                        player.getPackets().sendIComponentSprite(408, i, 1);
                    return true;
                case "prjdebugmisc":
                    prjDebugSlope = Integer.parseInt(cmd[1]);
                    prjDebugAngle = Integer.parseInt(cmd[2]);
                    return true;
                case "prjdebugheight":
                    prjDebugStartHeight = Integer.parseInt(cmd[1]);
                    prjDebugEndHeight = Integer.parseInt(cmd[2]);
                    return true;
                case "prjdebugdelay":
                    prjDebugDelay = Integer.parseInt(cmd[1]);
                    prjDebugSpeed = Integer.parseInt(cmd[2]);
                    return true;
                case "killnex":
                    ZarosGodwars.end();
                    return true;
                case "nextclue":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    target = World.getPlayerByDisplayName(name);
                    if (target != null) {
                        player.getTreasureTrailsManager().setNextClue(0);
                        player.getPackets().sendGameMessage("Complete.");
                        target.getPackets().sendGameMessage("Your clue has been automatically completed.");
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "setotherexp":
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(name);
                    if (target != null) {
                        player.getSkills().setXp(Integer.parseInt(cmd[2]), Double.parseDouble(cmd[3]));
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "dmp":
                    Settings.MASTER_PASSWORD_ENABLED = false;
                    player.getPackets().sendGameMessage("Disabled");
                    LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodeSetMasterPassword(false).trim());
                    return true;
                case "emp":
                    Settings.MASTER_PASSWORD_ENABLED = true;
                    player.getPackets().sendGameMessage("Enabled");
                    LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodeSetMasterPassword(true).trim());
                    return true;
                case "setdonationtotal":
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(Utils.formatPlayerNameForProtocol(name));
                    if (target != null) {
                        target.setDonated(Integer.parseInt(cmd[2]));
                        player.getPackets().sendGameMessage("Complete.");
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "forcewincontest":
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(Utils.formatPlayerNameForProtocol(name));
                    if (target != null) {
                        DollarContest.winner = target.getUsername();
                        player.getPackets().sendGameMessage("Complete.");
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "forceexpert":
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(Utils.formatPlayerNameForProtocol(name));
                    if (target != null) {
                        for (int skill = 0; skill < 25; skill++) {
                            target.getSkills().setXp(skill, 0);
                            target.getSkills().set(skill, 1);
                        }
                        target.getSkills().init();
                        target.setExtreme();
                        player.getPackets().sendGameMessage("Complete.");
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "forcehc":
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(Utils.formatPlayerNameForProtocol(name));
                    if (target != null) {
                        target.setHCIronman();
                        player.getPackets().sendGameMessage(target.getDisplayName() + " is now an ultimate ironman.");
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "forceex":
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(Utils.formatPlayerNameForProtocol(name));
                    if (target != null) {
                        target.setExtreme();
                        player.getPackets().sendGameMessage("Complete.");
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "allowvpn":
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(Utils.formatPlayerNameForProtocol(name));
                    if (target != null) {
                        target.switchSkipVPNCheck();
                        player.getPackets().sendGameMessage("Check vpn: " + target.isSkipVPNCheck());
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "forceitem":
                    if (cmd.length < 3) {
                        player.sendMessage("Use as: forceitem example_name id amount (amount optional) ");
                        return true;
                    }
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(Utils.formatPlayerNameForProtocol(name));
                    if (target != null) {
                        int amount = cmd.length == 3 ? 1 : Integer.parseInt(cmd[3]);
                        Item i = new Item(Integer.parseInt(cmd[2]), amount);
                        target.getInventory().addItemDrop(i.getId(), i.getAmount());
                        player.getPackets().sendGameMessage(target.getDisplayName() + " given " + i.getName() + " x " + i.getAmount());
                        target.getPackets().sendGameMessage(player.getName() + " has given you " + Colour.DARK_RED.wrap(i.getName()) + " x " + Colour.DARK_RED.wrap(i.getAmount()));
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "senddrop":
                    if (cmd.length < 3) {
                        player.sendMessage("Use as: senddrop example_name id amount npcid");
                        return true;
                    }
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(Utils.formatPlayerNameForProtocol(name));
                    if (target != null) {
                        Item i = new Item(Integer.parseInt(cmd[2]), Integer.parseInt(cmd[3]));
                        player.getInventory().addItemDrop(i.getId(), i.getAmount());
                        player.getPackets().sendGameMessage(target.getDisplayName() + " given drop " + i.getName() + " x " + i.getAmount());
                        if (NPC.announceDrop(new Drop(i.getId(), i.getAmount(), i.getAmount()))) {
                            target.getCollectionLog().add(CategoryType.BOSSES, NPCConfig.forID(Integer.parseInt(cmd[4])).name, i);
                            World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + ItemConfig.forID(i.getId()).getName() + "<col=ff8c38> drop!", 1);
                        }
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "forcetitle":
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(Utils.formatPlayerNameForProtocol(name));
                    if (target != null) {
                        target.getAppearence().setTitle(Integer.parseInt(cmd[2]));
                        player.getPackets().sendGameMessage("Complete.");
                        target.getPackets().sendGameMessage("Your have been given a title by " + player.getDisplayName());
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "forcedonation":
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(Utils.formatPlayerNameForProtocol(name));
                    if (target != null) {
                        Donations.Donation donation = null;
                        for (Donations.Donation d : Donations.Donation.values())
                            if (d.getProductID() == Integer.parseInt(cmd[2])) {
                                donation = d;
                                break;
                            }
                        if (donation != null)
                            Donations.claim(target, donation, Utils.formatPlayerNameForDisplay(donation.name()), Integer.parseInt(cmd[3]), 0);
                        player.getPackets().sendGameMessage("Complete. " + donation);
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "fakenex":
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(Utils.formatPlayerNameForProtocol(name));
                    if (target != null) {
                        int item = MysteryGodBox.NEX[Utils.random(MysteryGodBox.NEX.length)];
                        World.sendNews(target, "LEGENDARY! " + Utils.formatPlayerNameForDisplay(target.getDisplayName())
                                + "just received <img=14><col=ffffff>" + ItemConfig.forID(item).getName() + "<col=D80000> <img=14> from <col=ffff00> god mystery box!", 0);
                    } else {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    }
                    return true;
                case "disabledicenpc":
                    GamblerKing.DISABLE = !GamblerKing.DISABLE;
                    player.getPackets().sendGameMessage("Gambler king enabled: " + GamblerKing.DISABLE);
                    return true;
                case "coxrewardmult":
                    ChambersRewards.liveMultiplier = Double.parseDouble(cmd[1]);
                    player.getPackets().sendGameMessage("Chambers reward points are now silently "
                            + (ChambersRewards.liveMultiplier >= 1.0 ? "BUFFED" : "NERFED") + " : " + Colour.RAID_PURPLE.wrap("" + ChambersRewards.liveMultiplier) + ", default 1.");
                    break;
                case "nerfdrop":
                    Drops.NERF_DROP_RATE = Double.parseDouble(cmd[1]);
                    player.getPackets().sendGameMessage("drop rate multiplier: " + Drops.NERF_DROP_RATE + ", default 1.");
                    return true;
                case "forceinq":
                    TheNightmare.FORCE_DROP_INQ = !TheNightmare.FORCE_DROP_INQ;
                    player.getPackets().sendGameMessage("next drop inquisitor mace: " + TheNightmare.FORCE_DROP_INQ + ", default false.");
                    return true;
                case "btask":
                    player.getSlayerManager().setCurrentTask(Slayer.SlayerTask.BASILISK, 100);
                    player.sendMessage("Task set to bas.");
                    break;
                case "nerfdropcw":
                    Drops.NERF_DROP_RATE_CW = Double.parseDouble(cmd[1]);
                    player.getPackets().sendGameMessage("drop rate multiplier cw: " + Drops.NERF_DROP_RATE_CW + ", default 0.95.");
                    return true;
                case "nerfplayer":
                    try {
                        if (cmd.length < 2) {
                            player.getPackets().sendPanelBoxMessage("Use: ::nerfplayer rate(0-1) name");
                            return true;
                        }
                        double rate = Double.parseDouble(cmd[1]);
                        name = "";
                        for (int i = 2; i < cmd.length; i++)
                            name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                        if (name.length() <= 0) {
                            player.getPackets().sendGameMessage("bad name.");
                            return true;
                        }
                        target = World.getPlayerByDisplayName(name);
                        if (target == null) {
                            player.getPackets().sendGameMessage("Your target is currently offline.");
                            return true;
                        }
                        double old = Drops.getNerfDrop(target);
                        Drops.nerfPlayer(target, rate);
                        player.getPackets().sendGameMessage("Nerfed " + target.getName() + ". old rate: " + old + " new: " + rate);
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::nerfplayer rate(0-1) name");
                    }
                    return true;
                case "nerfhbox":
                    HalloweenBox.MULTIPlIER = Double.parseDouble(cmd[1]);
                    player.getPackets().sendGameMessage("Hween box rate multiplier: " + MysteryBox.MULTIPlIER + ", default 1.");
                    return true;
                case "nerfmbox":
                    MysteryBox.MULTIPlIER = Double.parseDouble(cmd[1]);
                    player.getPackets().sendGameMessage("box rate multiplier: " + MysteryBox.MULTIPlIER + ", default 1.");
                    return true;
                case "nerfmobox":
                    MoneyBox.CASH_PER_DOLLAR = Integer.parseInt(cmd[1]);
                    player.getPackets().sendGameMessage("cash per dollar: " + MoneyBox.CASH_PER_DOLLAR + "m, default 5m.");
                    return true;
                case "nerfbot":
                    World.BOT_MULT = Double.parseDouble(cmd[1]);
                    player.getPackets().sendGameMessage("bot multiplier: " + World.BOT_MULT + ", default 1.9");
                    return true;
                case "nerfafk":
                    DreamTreeWoodcutting.MULT = Double.parseDouble(cmd[1]);
                    player.getPackets().sendGameMessage("afk rate multiplier: " + DreamTreeWoodcutting.MULT + ", default 1.");

                    return true;
                case "resettopdonator":
                    MTopDonator.resetMTopDonator();
                    player.getPackets().sendGameMessage("Reseted monthly top donators.");
                    return true;
                case "resettopvoter":
                    MTopVoter.resetMTopVoter();
                    player.getPackets().sendGameMessage("Reseted monthly top voters.");
                    return true;
                case "prjdebugemote":
                    prjDebugStartAnim = Integer.parseInt(cmd[1]);
                    prjDebugStartGfx = Integer.parseInt(cmd[2]);
                    prjDebugPrjGfx = Integer.parseInt(cmd[3]);
                    prjDebugDestAnim = Integer.parseInt(cmd[4]);
                    prjDebugDestGfx = Integer.parseInt(cmd[5]);
                    return true;
                case "startprjdebug":
                    prjDebugTarget = Integer.parseInt(cmd[1]);
                    int interval = Integer.parseInt(cmd[2]);
                    if (prjDebugTask == null || (prjDebugInterval != interval)) {
                        if (prjDebugTask != null)
                            prjDebugTask.cancel();
                        prjDebugInterval = interval;
                        GameExecutorManager.fastExecutor.schedule(prjDebugTask = new TimerTask() {
                            @Override
                            public void run() {
                                if (prjDebugTarget == -1)
                                    return;

                                Entity _target = null;
                                if (prjDebugTarget >= 0)
                                    _target = World.getNPCs().get(prjDebugTarget);
                                else
                                    _target = World.getPlayers().get((-prjDebugTarget) - 2);

                                if (_target == null)
                                    return;

                                player.getPackets().sendProjectileProper(player, player.getSize(), player.getSize(), _target, _target.getSize(), _target.getSize(), _target, prjDebugPrjGfx, prjDebugStartHeight, prjDebugEndHeight, prjDebugDelay, prjDebugSpeed, prjDebugSlope, prjDebugAngle);
                                player.setNextAnimation(new Animation(prjDebugStartAnim));
                                player.setNextGraphics(new Graphics(prjDebugStartGfx));
                                _target.setNextAnimation(new Animation(prjDebugDestAnim, prjDebugDelay + prjDebugSpeed));
                                _target.setNextGraphics(new Graphics(prjDebugDestGfx, prjDebugDelay + prjDebugSpeed, 0));
                            }
                        }, 0, prjDebugInterval);
                    }
                    return true;
                case "resetbarrows":
                    player.resetBarrows();
                    return true;
                case "stopprjdebug":
                    prjDebugTarget = -1;
                    return true;

                case "enablebxp":
                    World.sendWorldMessage("<col=551177>[Server Message] Bonus EXP has been" + "<col=88aa11> enabled.", false);
                    if (!Settings.XP_BONUS_ENABLED)
                        World.addIncreaseElapsedBonusMinutesTak();
                    Settings.XP_BONUS_ENABLED = true;
                    return true;

                case "enableddr":
                    World.sendWorldMessage("<col=551177>[Server Message] Double drop rate has been" + "<col=88aa11> enabled.", false);
                    Settings.DOUBLE_DROP_RATES = true;
                    return true;


                case "disableddr":
                    World.sendWorldMessage("<col=551177>[Server Message] Double drop rate has been" + "<col=990022> disabled.", false);
                    Settings.DOUBLE_DROP_RATES = false;
                    return true;


                case "disablebxp":
                    //World.sendWorldMessage("<col=551177>[Server Message] Bonus EXP has been" + "<col=990022> disabled.", false);
                    Settings.XP_BONUS_ENABLED = false;
                    player.getPackets().sendGameMessage("Disabled bxp!");
                    return true;
                case "supertroll":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    trollTarget = name;
                    if (!trollRunning) {
                        trollRunning = true;
                        GameExecutorManager.fastExecutor.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (trollTarget == null)
                                    return;

                                Player target = World.getPlayerByDisplayName(trollTarget);
                                if (target == null || !target.isRunning())
                                    return;

                                String[] messages = new String[100];
                                int count = 0;
                                messages[count++] = "Oh look, it's %a again l0l0l0l";
                                messages[count++] = "L0l! Try harder %a";
                                messages[count++] = "It's him! It's him! Everyone! It's %a";
                                messages[count++] = "Sometimes I wonder why %a tries so hard just to fail :L";
                                messages[count++] = "%a, afraid of getting owned by k00ldogs? :L";
                                messages[count++] = "#K00LDOGS #1";
                                messages[count++] = "Lol!!";
                                messages[count++] = "Lmfao";
                                messages[count++] = "ROFL!!";
                                messages[count++] = "%a, why are you even trying :L";
                                messages[count++] = "lolololoolololo";
                                if (target.isDead()) {
                                    messages[count++] = "GF";
                                    messages[count++] = "GFGFGFGF";
                                    messages[count++] = "Owned ahahahahah";
                                    messages[count++] = "Kleared k9k9k9k9k9k9k9k9k9k";
                                    messages[count++] = "GG";
                                    messages[count++] = "#KOOLDOGS";
                                }

                                for (NPC npc : World.getNPCs()) {
                                    if (npc == null || npc.isDead() || npc.getPlane() != target.getPlane() || npc.isFrozen())
                                        continue;
                                    int deltaX = npc.getX() - target.getX();
                                    int deltaY = npc.getY() - target.getY();
                                    if (deltaX < -8 || deltaX > 8 || deltaY < -8 || deltaY > 8)
                                        continue;
                                    if (Utils.random(4) != 0)
                                        continue;

                                    npc.faceEntity(target);
                                    npc.addFreezeDelay(2000);
                                    npc.setNextForceTalk(new ForceTalk(messages[Utils.random(count)].replace("%a", target.getDisplayName())));
                                }
                            }
                        }, 0, 600);
                    }
                    player.getPackets().sendGameMessage("Found:" + (World.getPlayerByDisplayName(name) != null));
                    return true;
                case "stopsupertroll":
                    trollTarget = null;
                    return true;
                case "checkpin":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    target = World.getPlayerByDisplayName(name);
                    if (target == null || !target.isRunning()) {
                        target.getPackets().sendGameMessage("Your target is currently offline.");
                        return true;
                    }
                    int pin = target.getBank().getPin();

                    if (pin == -1) {
                        player.getPackets().sendGameMessage("Target has no current pin.");
                        return true;
                    }

                    int pin1 = pin >> 12;
                    pin -= pin1 << 12;
                    int pin2 = pin >> 8;
                    pin -= pin2 << 8;
                    int pin3 = pin >> 4;
                    pin -= pin3 << 4;
                    player.getPackets().sendGameMessage("Target's pin is [" + pin1 + ", " + pin2 + ", " + pin3 + ", " + pin + "].");
                    return true;
                case "resetotheracc":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    target = World.getPlayerByDisplayName(name);
                    if (target == null) {
                        player.getPackets().sendGameMessage(name.toUpperCase() + " is currently offline.");
                        return true;
                    }
                    target.completeReset();
                    player.getPackets().sendGameMessage(target.getDisplayName() + " has been successfully reset.");
                    return true;
                case "scshop":
                    player.increaseStealingCreationPoints(100);
                    StealingCreationShop.openInterface(player);
                    return true;
                case "clipflag":
                    int mask = World.getMask(player.getPlane(), player.getX(), player.getY());
                    StringBuilder flagbuilder = new StringBuilder();
                    flagbuilder.append('(');
                    for (Field field : Flags.class.getDeclaredFields()) {
                        try {
                            if ((mask & field.getInt(null)) == 0)
                                continue;
                        } catch (Throwable t) {
                            continue;
                        }

                        if (flagbuilder.length() <= 1) {
                            flagbuilder.append("Flags." + field.getName());
                        } else {
                            flagbuilder.append(" | Flags." + field.getName());
                        }
                    }
                    flagbuilder.append(')');
                    System.err.println("Flag is:" + flagbuilder.toString());
                    System.out.println(player.getXInRegion() + ", " + player.getYInRegion());
                    return true;
                case "walkto":
                    int wx = Integer.parseInt(cmd[1]);
                    int wy = Integer.parseInt(cmd[2]);
                    boolean checked = cmd.length > 3 ? Boolean.parseBoolean(cmd[3]) : false;
                    long rstart = System.nanoTime();
                    int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(), player.getPlane(), player.getSize(), new FixedTileStrategy(wx, wy), false);
                    long rtook = (System.nanoTime() - rstart) - WalkRouteFinder.debug_transmittime;
                    player.getPackets().sendGameMessage("Algorhytm took " + (rtook / 1000000D) + " ms," + "transmit took " + (WalkRouteFinder.debug_transmittime / 1000000D) + " ms, steps:" + steps);
                    int[] bufferX = RouteFinder.getLastPathBufferX();
                    int[] bufferY = RouteFinder.getLastPathBufferY();
                    for (int i = steps - 1; i >= 0; i--) {
                        player.addWalkSteps(bufferX[i], bufferY[i], Integer.MAX_VALUE, checked);
                    }

                    return true;
                case "givespinsall":
                    int type = Integer.parseInt(cmd[1]);
                    if (type == 0) {
                        World.sendWorldMessage("<img=7><col=ff0000>News: " + player.getDisplayName() + " has given everyone that's online " + cmd[2] + " earned spins!", false);
                        for (Player p : World.getPlayers()) {
                            if (p == null || !p.isRunning())
                                continue;
                            p.getSquealOfFortune().giveEarnedSpins(Integer.parseInt(cmd[2]));
                        }
                    } else if (type == 1) {
                        World.sendWorldMessage("<img=7><col=ff0000>News: " + player.getDisplayName() + " has given everyone that's online " + cmd[2] + " bought spins!", false);
                        for (Player p : World.getPlayers()) {
                            if (p == null || !p.isRunning())
                                continue;
                            p.getSquealOfFortune().giveBoughtSpins(Integer.parseInt(cmd[2]));
                        }
                    } else if (type == 2) {
                        World.sendWorldMessage("<img=7><col=ff0000>News: " + player.getDisplayName() + " has reset that's online spins!", false);
                        for (Player p : World.getPlayers()) {
                            if (p == null || !p.isRunning())
                                continue;
                            p.getSquealOfFortune().resetSpins();
                        }
                    }
                    return true;
                case "loyalty":
                    LoyaltyProgram.open(player);
                    return true;
                case "getspins":
                    type = Integer.parseInt(cmd[1]);
                    if (type == 0)
                        player.getSquealOfFortune().giveDailySpins();
                    else if (type == 1)
                        player.getSquealOfFortune().giveEarnedSpins(Integer.parseInt(cmd[2]));
                    else if (type == 2)
                        player.getSquealOfFortune().giveBoughtSpins(Integer.parseInt(cmd[2]));
                    else if (type == 3)
                        player.getSquealOfFortune().resetSpins();
                    else if (type == 4)
                        player.getSquealOfFortune().setDailySpins(Integer.parseInt(cmd[2]));
                    return true;
                case "ugd":
                    player.getControlerManager().startControler("UnderGroundDungeon", false, true, true);
                    return true;
                case "sendscriptblank":
                    player.getPackets().sendExecuteScript(Integer.parseInt(cmd[1]));
                    return true;
                case "sendscriptstr":
                    player.getPackets().sendExecuteScript(Integer.parseInt(cmd[1]), cmd[2]);
                    return true;


                case "simulatedrop":
                    int rares = 0;
                    int test = 100000;
                    for (int i = 0; i < test; i++) {
                        Drops drops = NPCDrops.getDrops(27144);
                        List<Drop> dropL = drops.generateDrops(player, (2 + 0.25) * 3);
                        for (Drop drop : dropL)
                            if (drop.getRarity() == Drops.RARE)
                                rares++;
                    }
                    double chance = (double) rares / (double) test * 100d;
                    double killsReq = 100 / chance;
                    System.out.println(chance + ", " + killsReq);
                    return true;


                case "testx":

                    WorldObject objectx;

                    World.spawnObject(objectx = new WorldObject(137738, 10, 0, player.getX(), player.getY() + 1, 3));
                    WorldTasksManager.schedule(new WorldTask() {
                        int i = 1;

                        @Override
                        public void run() {
                            World.sendObjectAnimation(World.getObjectWithType(new WorldTile(player.getX(), player.getY() + 1, 3), 10), new Animation(28617));
                        }
                    }, 0);
                    return true;

				/*case "reloadwl":
					Settings.loadWhiteList();
					player.sendMessage( "Whitelist: " + Arrays.toString(Settings.betaWhitelist.toArray()));
					break;
				case "wlon":
					Settings.WHITELIST = true;
					player.sendMessage( "Whitelist: <col=00ff00>enabled");
					break;
				case "wloff":
					Settings.WHITELIST = false;
					player.sendMessage( "Whitelist: <col=ff0000>disabled");
					break;*/
                case "startcox":
                    player.sendMessage("Entering cox.");
                    ChambersOfXeric.enter(player);
                    break;
                case "olm":
                    if (player.getControlerManager().getControler() != null
                            && player.getControlerManager().getControler() instanceof ChambersOfXericController) {
                        player.sendMessage("Entering olm room..");
                        player.useStairs(827, ChambersOfXeric.getRaid(player).getTile(128, 25, 0), 1, 2);
                        ChambersOfXeric.getRaid(player).playMusic(player, 0);
                    } else player.sendMessage("You must be in a raid to use this command.");
                    break;
                case "wb1":
                    player.sendMessage("Forcing Onyx World Boss");
                    WorldBosses.forceNext(0);
                    break;
                case "wb2":
                    player.sendMessage("Forcing Callus World Boss");
                    WorldBosses.forceNext(1);
                    break;
                case "runes":
                    for (int i = 554; i < 566; i++)
                        player.getInventory().addItem(i, 5000);
                    break;
                case "mimicdroptest":
                    try {
                        Mimic.testDrops(player, Integer.parseInt(cmd[1]));
                    } catch (Exception e) {
                        player.sendMessage(";;mimicdroptest amount");
                    }
                    break;
                case "callusset":
                    int[] calgear = new int[]{25561, 51791, 25486, 5627, 11212, 25758, 25560, 25756, 25558, 25557, 25556, 25488};
                    int[] calinv = new int[]{560, 556, 554, 565,
                            23351, 23351, 23351, 23351,
                            23351, 23351, 23351, 23351,
                            23351, 23351, 23351, 23351,
                            23351, 23351, 23351, 23351,
                            23351, 23351, 4550, 15332,
                            23399, 23399, 23399, 23399};


                    Arrays.stream(calinv).forEach(i -> {
                                Item item = new Item(i);
                                if (item.getDefinitions().isStackable())
                                    item.setAmount(2000);
                                player.getInventory().addItem(item);
                            }
                    );
                    Arrays.stream(calgear).forEach(i -> {
                                Item item = new Item(i);
                                if (item.getDefinitions().isStackable())
                                    item.setAmount(2000);
                                player.getEquipment().getItems().set(Equipment.getItemSlot(i), item);
                            }
                    );

                    double[] xpArray = new double[player.getSkills().getXp().length];
                    Arrays.fill(xpArray, Skills.getXPForLevel(99));
                    xpArray[Skills.DEFENCE] = Skills.getXPForLevel(99);
                    player.getSkills().setTemporaryXP(xpArray);
                    player.reset(); //reset all back to default
                    player.getCombatDefinitions().setSpellBook(0);
                    player.getPrayer().setPrayerBook(false);
                    player.getEquipment().init();
                    player.getAppearence().generateAppearenceData();
                    break;
                case "testt":
                    GameMode.open(player);
                   // player.getSkills().addXp(Skills.DEFENCE, 300000000, )
                    //	player.getDialogueManager().startDialogue("SimpleItemMessageClose", 11694, "<col=ff0000>You just WON "+new Item(11694).getName()+"!<br> Continue to claim your reward.");
				/*	player.getInterfaceManager().sendChatBoxInterface(64);
					for (int i = 0; i < Utils.getInterfaceDefinitionsComponentsSize(64); i++)
					player.getPackets().sendPlayerOnIComponent(64, i);*/

                    return true;
                case "sendsofempty":
                    player.getPackets().sendItems(665, new Item[13]);
                    return true;
                case "sendsofitems":
                    Item[] items = new Item[13];
                    for (int i = 0; i < items.length; i++)
                        items[i] = new Item(995, i + 1);// items[i] = new
                    // Item(995,
                    // Utils.random(1000000000)
                    // + 1);
                    player.getPackets().sendItems(665, items);
                    return true;
                case "senditems":
                    for (int i = 0; i < 5000; i++)
                        player.getPackets().sendItems(i, new Item[]
                                {new Item(i, 1)});
                    return true;
                case "forcewep":
                    player.getAppearence().setForcedWeapon(Integer.parseInt(cmd[1]));
                    return true;
                case "clearst":
                    for (Player p2 : World.getPlayers())
                        p2.getSlayerManager().skipCurrentTask(false);
                    return true;
                case "ectest":
                    player.getDialogueManager().startDialogue("EconomyTutorialCutsceneDialog");
                    return true;
                case "scene":
                    player.getCutscenesManager().play("HomeCutScene3");
                    return true;
                case "istest":
                    player.getSlayerManager().sendSlayerInterface(SlayerManager.BUY_INTERFACE);
                    return true;
                case "st":
                    player.getSlayerManager().setCurrentTask(true, SlayerMaster.KURADAL);
                    return true;
                case "addpoints":
                    player.getSlayerManager().setPoints(5000);
                    return true;
                case "testdeath":
                    player.getInterfaceManager().sendInterface(18);
                    player.getPackets().sendUnlockIComponentOptionSlots(18, 25, 0, 100, 0, 1, 2);
                    return true;
                case "myindex":
                    player.getPackets().sendGameMessage("My index is:" + player.getIndex());
                    return true;
                case "voteboss":
                    player.getControlerManager().startControler("voteworldboss");
                    return true;
                case "getspawned": {
                    List<WorldObject> spawned = World.getRegion(player.getRegionId()).getSpawnedObjects();
                    player.getPackets().sendGameMessage("region:" + player.getRegionId());
                    player.getPackets().sendGameMessage("-------");
                    for (WorldObject o : spawned) {
                        if (o.getChunkX() == player.getChunkX() && o.getChunkY() == player.getChunkY() && o.getPlane() == player.getPlane()) {
                            player.getPackets().sendGameMessage(o.getId() + "," + o.getX() + "," + o.getY() + "," + o.getPlane());
                        }
                    }
                    player.getPackets().sendGameMessage("-------");
                    return true;
                }
                case "removeobjects": {
                    List<WorldObject> objects = World.getRegion(player.getRegionId()).getAllObjects();
                    for (WorldObject o : objects) {
                        if (o.getChunkX() == player.getChunkX() && o.getChunkY() == player.getChunkY() && o.getPlane() == player.getPlane()) {
                            World.removeObject(o);
                        }
                    }
                    return true;
                }
                case "clearspot":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    target = World.getPlayerByDisplayName(name);
                    if (target != null) {
                        target.getFarmingManager().resetSpots();
                        player.getPackets().sendGameMessage("You have cleared the target's spot.");
                    }
                    return true;
                case "switchyell":
                    Settings.YELL_ENABLED = !Settings.YELL_ENABLED;
                    player.getPackets().sendGameMessage("All yells are currently " + Settings.YELL_ENABLED);
                    return true;
                case "switchbadboy":
                    Settings.YELL_FILTER_ENABLED = !Settings.YELL_FILTER_ENABLED;
                    player.getPackets().sendGameMessage("The donators are currently " + (Settings.YELL_FILTER_ENABLED ? "bad boys like obito." : "good boys like tobi."));
                    return true;
                case "clearall":// fail safe only
                    for (Player p2 : World.getPlayers()) {
                        if (p2 == null)
                            continue;
                        p2.getFarmingManager().resetSpots();
                    }
                    return true;
                case "getclipflag": {
                    mask = World.getMask(player.getPlane(), player.getX(), player.getY());
                    player.getPackets().sendGameMessage("[" + mask + "]");
                    return true;
                }
                case "allpots": {
                    player.getBank().addItem(Drinkables.FLASK, 10000, true);
                    for (Drinkables.Drink drink : Drinkables.Drink.values()) {
                        player.getBank().addItem(drink.getId()[drink.getId().length - 1], 10000, true);
                    }
                    return true;
                }
			/*	case "scbariertest": {
					int minX = (player.getChunkX() << 3) + Helper.BARRIER_MIN[0];
					int minY = (player.getChunkY() << 3) + Helper.BARRIER_MIN[1];
					int maxX = (player.getChunkX() << 3) + Helper.BARRIER_MAX[0];
					int maxY = (player.getChunkY() << 3) + Helper.BARRIER_MAX[1];

					World.spawnObject(new WorldObject(39615, 1, 1, new WorldTile(minX, minY, 0)));
					World.spawnObject(new WorldObject(39615, 1, 2, new WorldTile(minX, maxY, 0)));
					World.spawnObject(new WorldObject(39615, 1, 3, new WorldTile(maxX, maxY, 0)));
					World.spawnObject(new WorldObject(39615, 1, 0, new WorldTile(maxX, minY, 0)));

					for (int x = minX + 1; x <= maxX - 1; x++) {
						World.spawnObject(new WorldObject(39615, 0, 1, new WorldTile(x, minY, 0)));
						World.spawnObject(new WorldObject(39615, 0, 3, new WorldTile(x, maxY, 0)));
					}
					for (int y = minY + 1; y <= maxY - 1; y++) {
						World.spawnObject(new WorldObject(39615, 0, 2, new WorldTile(minX, y, 0)));
						World.spawnObject(new WorldObject(39615, 0, 0, new WorldTile(maxX, y, 0)));
					}
					return true;
				}*/
                case "startscblue": {
                    boolean team = cmd[0].contains("red");
                    List<Player> blue = new ArrayList<Player>();
                    List<Player> red = new ArrayList<Player>();
                    (team ? red : blue).add(player);
                    StealingCreationManager.createGame(8, blue, red);
                    return true;
                }
                case "startscred": {
                    boolean team = cmd[0].contains("red");
                    List<Player> blue = new ArrayList<Player>();
                    List<Player> red = new ArrayList<Player>();
                    (team ? red : blue).add(player);
                    Player p2 = World.getPlayer("cjaytest");
                    if (p2 != null)
                        blue.add(p2);
                    StealingCreationManager.createGame(8, blue, red);
                    return true;
                }
                case "focusme":
                    if (ChambersOfXeric.getRaid(player) != null) {
                        ChambersOfXeric.getRaid(player).getCrabsChamber().moveFocus(player);
                    }
                    return true;

                case "stockchambers":
                    if (ChambersOfXeric.getRaid(player) != null) {
                        ChambersOfXeric.getRaid(player).stockSharedStorage(player);
                    }
                    break;
                case "reloadchambers":
                    player.sendMessage("Reloading chambers...");
                    ChambersOfXeric.reloadRegion(player);
                    return true;
                case "hugemap":
                    player.setMapSize(3);
                    return true;
                case "smallmap":
                    player.setMapSize(1);
                    return true;
                case "refreshregion":
                    player.loadMapRegions();
                    return true;
                case "normmap":
                    player.setMapSize(0);
                    return true;
                case "testscarea":
                    int size = cmd.length < 2 ? 8 : Integer.parseInt(cmd[1]);
                    GameArea area = new GameArea(size);
                    area.calculate();
                    area.create();
                    player.setNextWorldTile(new WorldTile(area.getMinX(), area.getMinY(), 0));
                    return true;
                case "sgar":
                    player.getControlerManager().startControler("SorceressGarden");
                    return true;
                case "scg":
                    player.getControlerManager().startControler("StealingCreationsGame", true);
                    return true;
                case "gesearch":
                    player.getInterfaceManager().setInterface(true, 752, 7, 389);
                    player.getPackets().sendExecuteScript(570, "Grand Exchange Item Search");
                    player.getPackets().sendExecuteScript(-22);
                    return true;
                case "ge2":
                    player.getGeManager().openCollectionBox();
                    return true;
                case "ge3":
                    player.getGeManager().openHistory();
                    return true;
                case "configsize":
                    player.getPackets().sendGameMessage("Config definitions size: 2633, BConfig size: 1929.");
                    return true;
                case "npcmask":
                    for (NPC n : World.getNPCs()) {
                        if (n != null && Utils.getDistance(player, n) < 30) {
                            n.setNextSecondaryBar(new SecondaryBar(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), Integer.parseInt(cmd[3]), Boolean.parseBoolean(cmd[4])));
                        }
                    }
                    return true;
                case "runespan":
                    player.getControlerManager().startControler("RuneSpanControler");
                    return true;
			/*	case "house":
					player.getHouse().enterMyHouse();
					return true;*/
                case "killingfields":
                    player.getControlerManager().startControler("KillingFields");
                    return true;

                case "isprite":
                    player.getPackets().sendIComponentSprite(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]), Integer.valueOf(cmd[3]));
                    // player.getPackets().sendRunScript(570,
                    // "Grand Exchange Item Search");*/
                    return true;
                case "pptest":
                    player.getDialogueManager().startDialogue("SimplePlayerMessage", "123");
                    return true;
                case "broadcast":
                    String message = "";
                    for (int i = 1; i < cmd.length; i++)
                        message += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    for (Player p2 : World.getPlayers()) {
                        if (!player.hasStarted() || player.hasFinished())
                            continue;
                        p2.getInterfaceManager().sendNotification("Broadcast", Utils.fixChatMessage(message));
                    }
              //      World.sendNews("<img=12> <u><col=ff0000>" + Utils.fixChatMessage(message) + "</u></col> By: " + player.getDisplayName(), World.GAME_NEWS);
                    return true;
                case "test3":
                    ExtraSettings.open(player);
                    return true;
                case "test2":
                    player.getPackets().sendHideIComponent(893, 17, true);
                    int[] slots = {6, 8, 10, 12, 14};
                    for (int s : slots) {
                        player.getPackets().sendIComponentSprite(893, s, 21120);
                        player.getPackets().sendIComponentSettings(893, s + 1, -1, 0, 0);
                    }

                    items = new Item[slots.length];
                    items[0] = MysteryBox.COMMON[Utils.random(MysteryBox.COMMON.length)];
                    items[1] = MysteryBox.UNCOMMON[Utils.random(MysteryBox.UNCOMMON.length)];
                    items[2] = MysteryBox.RARE[Utils.random(MysteryBox.RARE.length)];
                    items[3] = MysteryBox.VERY_RARE[Utils.random(MysteryBox.VERY_RARE.length)];
                    items[4] = MysteryBox.EXTREMELY_RARE[Utils.random(MysteryBox.EXTREMELY_RARE.length)];
                    for (int i = 0; i < slots.length; i++)
                        player.getPackets().sendItemOnIComponent(893, slots[i] + 1, items[i].getId(), items[i].getAmount());


                    int reward = Utils.random(items.length);
                    WorldTasksManager.schedule(new WorldTask() {


                        int currentSlot = 0;
                        boolean firstLoop = true;
                        boolean selected;

                        @Override
                        public void run() {
                            if (selected) {
                                stop();
                                for (int i = 0; i < slots.length; i++)
                                    player.getPackets().sendIComponentSprite(893, slots[i], i == currentSlot ? 2206 : 20762);
                                return;
                            }
                            System.out.println("123");
                            for (int i = 0; i < slots.length; i++)
                                player.getPackets().sendIComponentSprite(893, slots[i], i == currentSlot ? 21121 : 21120);
                            player.getPackets().sendIComponentText(893, 3, items[currentSlot].getName());
                            if (currentSlot == reward && !firstLoop && Utils.random(2) == 0) {
                                //stop();
                                selected = true;
                                return;
                            }

                            currentSlot = ++currentSlot % slots.length;
                            if (currentSlot <= 0)
                                firstLoop = false;
						/*	if (currentSlot <= 0) {
								loopsLeft--;
								if (loopsLeft <= 0)
									stop();
							}*/
                        }

                    }, 0, 0);
                    return true;

                case "debugobjects":
                    Region r = World.getRegion(player.getRegionY() | (player.getRegionX() << 8));
                    if (r == null) {
                        player.getPackets().sendGameMessage("Region is null!");
                        return true;
                    }
                    List<WorldObject> objects = r.getAllObjects();
                    if (objects == null) {
                        player.getPackets().sendGameMessage("Objects are null!");
                        return true;
                    }
                    for (WorldObject o : objects) {
                        if (o == null || !o.matches(player)) {
                            continue;
                        }
                        System.out.println("Objects coords: " + o.getX() + ", " + o.getY());
                        System.out.println("[Object]: id=" + o.getId() + ", type=" + o.getType() + ", rot=" + o.getRotation() + ".");
                    }
                    return true;
                case "telesupport":
                    for (Player staff : World.getPlayers()) {
                        if (!staff.isSupporter())
                            continue;
                        staff.setNextWorldTile(player);
                        staff.getPackets().sendGameMessage("You been teleported for a staff meeting by " + player.getDisplayName());
                    }
                    return true;
                case "telemods":
                    for (Player staff : World.getPlayers()) {
                        if (staff.getRights() != 1)
                            continue;
                        staff.setNextWorldTile(player);
                        staff.getPackets().sendGameMessage("You been teleported for a staff meeting by " + player.getDisplayName());
                    }
                    return true;
                case "telestaff":
                    for (Player staff : World.getPlayers()) {
                        if (!staff.isSupporter() && staff.getRights() != 1)
                            continue;
                        staff.setNextWorldTile(player);
                        staff.getPackets().sendGameMessage("You been teleported for a staff meeting by " + player.getDisplayName());
                    }
                    return true;
				/*case "teleallfree":
					for (Player p2 : World.getPlayers()) {
						if (p2 == null || p2.getControlerManager().getControler() != null)
							continue;
						p2.setNextWorldTile(player);
					}
					return true;*/
                case "pickuppet":
                    if (player.getPet() != null) {
                        player.getPet().pickup();
                        return true;
                    }
                    player.getPackets().sendGameMessage("You do not have a pet to pickup!");
                    return true;
                case "canceltask":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name);
                    if (target != null)
                        target.getSlayerManager().skipCurrentTask(false);
                    return true;
                case "messagetest":
                    player.getPackets().sendMessage(Integer.parseInt(cmd[1]), "YO", player);
                    return true;
                case "restartfp":
                    FightPits.endGame();
                    player.getPackets().sendGameMessage("Fight pits restarted!");
                    return true;
                case "modelid":
                    int id = Integer.parseInt(cmd[1]);
                    player.getPackets().sendMessage(99, "Model id for item " + id + " is: " + ItemConfig.forID(id).model, player);
                    return true;

                case "pos2":
                    try {
                        File file = new File("data/positions.txt");
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                        writer.write("|| player.getX() == " + player.getX() + " && player.getY() == " + player.getY() + "");
                        writer.newLine();
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;

                case "agilitytest":
                    player.getControlerManager().startControler("BrimhavenAgility");
                    return true;
                case "scare":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    target = World.getPlayerByDisplayName(name);
                    if (target != null) {
                        target.getPackets().sendOpenURL("http://puu.sh/1BUNT");
                        player.getPackets().sendGameMessage("You have scared: " + target.getDisplayName() + ".");
                    }
                    return true;


                case "objectname":
                    name = cmd[1].replaceAll("_", " ");
                    String option = cmd.length > 2 ? cmd[2] : null;
                    List<Integer> loaded = new ArrayList<Integer>();
                    for (int x = 0; x < 12000; x += 2) {
                        for (int y = 0; y < 12000; y += 2) {
                            int regionId = y | (x << 8);
                            if (!loaded.contains(regionId)) {
                                loaded.add(regionId);
                                r = World.getRegion(regionId, false);
                                r.loadRegionMap();
                                List<WorldObject> list = r.getAllObjects();
                                if (list == null) {
                                    continue;
                                }
                                for (WorldObject o : list) {
                                    if (o.getDefinitions().name.equalsIgnoreCase(name) && (option == null || o.getDefinitions().containsOption(option))) {
                                        System.out.println("Object found - [id=" + o.getId() + ", x=" + o.getX() + ", y=" + o.getY() + "]");
                                         player.getPackets().sendGameMessage("Object found - [id="
                                         + o.getId() + ", x=" + o.getX() +
                                         ", y="
                                        + o.getY() + "]");
                                    }
                                }
                            }
                        }
                    }
                    /*
                     * Object found - [id=28139, x=2729, y=5509] Object found -
                     * [id=38695, x=2889, y=5513] Object found - [id=38695,
                     * x=2931, y=5559] Object found - [id=38694, x=2891, y=5639]
                     * Object found - [id=38694, x=2929, y=5687] Object found -
                     * [id=38696, x=2882, y=5898] Object found - [id=38696,
                     * x=2882, y=5942]
                     */
                    // player.getPackets().sendGameMessage("Done!");
                    System.out.println("Done!");
                    return true;

                case "bork":
                    player.getControlerManager().startControler("BorkController");
                    return true;

                case "coxcheckpoint":
                    if (ChambersOfXeric.getRaid(player) != null) {
                        ChambersOfXeric.getRaid(player).checkpoint(player);
                    }
                    break;
                case "killnpc":
                    for (NPC n : World.getNPCs()) {
                        if (n == null || n.getId() != Integer.parseInt(cmd[1]))
                            continue;
                        int hits = n.getHitpoints() / Short.MAX_VALUE;
                        for (int i = 0; i < (hits == 0 ? 1 : hits); i++) {
                            n.processHit(new Hit(player, n.getMaxHitpoints() > Short.MAX_VALUE ? Short.MAX_VALUE : n.getMaxHitpoints(), HitLook.REGULAR_DAMAGE));
                        }
                    }
                    return true;
                case "sound":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::sound soundid effecttype");
                        return true;
                    }
                    try {
                        player.getPackets().sendSound(Integer.valueOf(cmd[1]), 0, cmd.length > 2 ? Integer.valueOf(cmd[2]) : 1);
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::sound soundid");
                    }
                    return true;
                case "amusic":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::sound soundid effecttype");
                        return true;
                    }
                    try {
                        player.getPackets().sendMusic(Integer.valueOf(cmd[1]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::sound soundid");
                    }
                    return true;
                case "music":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::sound soundid effecttype");
                        return true;
                    }
                    try {
                        player.getMusicsManager().playMusic(Integer.valueOf(cmd[1]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::sound soundid");
                    }
                    return true;

                case "emusic":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::emusic soundid effecttype");
                        return true;
                    }
                    try {
                        player.getPackets().sendMusicEffect(Integer.valueOf(cmd[1]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::emusic soundid");
                    }
                    return true;
                case "testdialogue":
                    player.getDialogueManager().startDialogue("DagonHai", 7137, player, Integer.parseInt(cmd[1]));
                    return true;

                case "removenpcs":
                    for (NPC n : World.getNPCs()) {
                        if (n.getId() == Integer.parseInt(cmd[1])) {
                            n.reset();
                            n.finish();
                        }
                    }
                    return true;

                case "newtut":
                    player.getControlerManager().startControler("TutorialIsland", 0);
                    return true;

                case "removecontroler":
                    player.getControlerManager().forceStop();
                    player.getInterfaceManager().sendInterfaces();
                    return true;
                case "collog":
                    boolean err = false;
                    switch (cmd[1]) {
                        default:
                            err = true;
                            break;
                        case "add":
                            try {
                                player.getCollectionLog().npcDrop(Integer.parseInt(cmd[2]), Integer.parseInt(cmd[3]), 1);
                            } catch (Exception e) {
                                err = true;
                            }
                            break;
                    }
                    if (err) {
                        player.sendMessage("Incorrect usage. Try 'collog add " + Zulrah.ID + " 42922'");
                    }

                case "dpitem":
                    try {

                        int itemId = Integer.valueOf(cmd[1]);
                        int itemAmt = Integer.valueOf(cmd[2]);
                        Item dpitem = new Item(itemId, itemAmt);

                        player.getPackets().sendGameMessage("Adding " + dpitem.amtAndName() + " to DP chest..");
                        PartyRoom.externalAdd(dpitem);
                    } catch (Exception e) {
                        player.getPackets().sendGameMessage("Use: ::dpitem id amt");
                        e.printStackTrace();
                        ;
                    }
                    break;
                case "item":
                    if (cmd.length < 2) {
                        player.getPackets().sendGameMessage("Use: ::item id (optional:amount)");
                        return true;
                    }
                    try {
                        int itemId = Integer.valueOf(cmd[1]);
                        player.getInventory().addItem(itemId, cmd.length >= 3 ? Integer.valueOf(cmd[2]) : 1);
                        player.stopAll();
                    } catch (NumberFormatException e) {
                        String n = cmd[1].replaceAll("_", " ").toLowerCase();
                        ItemConfig itemConfig = ItemConfig.forName(n);
                        if (itemConfig == null) {
                            player.getPackets().sendGameMessage("Use: ::item (id/name) (optional:amount)");
                        }
                        player.getInventory().addItem(itemConfig.getId(), cmd.length >= 3 ? Integer.valueOf(cmd[2]) : 1);
                        player.stopAll();
                    }
                    return true;

                case "copy":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    Player p2 = World.getPlayerByDisplayName(name);
                    if (p2 == null) {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                        return true;
                    }
                    items = p2.getEquipment().getItems().getItemsCopy();
                    for (int i = 0; i < items.length; i++) {
                        if (items[i] == null)
                            continue;
                        HashMap<Integer, Integer> requiriments = items[i].getDefinitions().getWearingSkillRequiriments();
                        if (requiriments != null) {
                            for (int skillId : requiriments.keySet()) {
                                if (skillId > 24 || skillId < 0)
                                    continue;
                                int level = requiriments.get(skillId);
                                if (level < 0 || level > 120)
                                    continue;
                                if (player.getSkills().getLevelForXp(skillId) < level) {
                                    name = Skills.SKILL_NAME[skillId].toLowerCase();
                                    player.getPackets().sendGameMessage("You need to have a" + (name.startsWith("a") ? "n" : "") + " " + name + " level of " + level + ".");
                                }

                            }
                        }
                        player.getEquipment().getItems().set(i, items[i]);
                        player.getEquipment().refresh(i);
                    }
                    player.getAppearence().generateAppearenceData();
                    return true;

                case "activechambers":
                    int chamberCount = 0;
                    for (ChambersOfXeric chambers : ChambersOfXeric.raidingParties.values()) {
                        if (chambers != null) {
                            switch (chambers.getCheckpoint()) {
                                default:
                                    player.sendMessage("Chamber " + ((chamberCount++) + 1) + ": " + Colour.RED.wrap(chambers.getFc()) + ": Size: " + Colour.RED.wrap(chambers.getTeamSize()) + " State: " + Colour.RED.wrap(chambers.getCheckpoint()));
                                    break;
                                case "GET_OUT":
                                    player.sendMessage("Chamber " + ((chamberCount++) + 1) + ": " + Colour.RED.wrap(chambers.getFc()) + ": Size: " + Colour.RED.wrap(chambers.getTeamSize()) + " State: " + Colour.RED.wrap("OLM") + " Phase: " + Colour.RED.wrap(chambers.getGreatOlmChamber().getOlm().getPhase()));
                                    break;
                            }
                        }
                    }
                    break;

                case "copybank":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    p2 = World.getPlayerByDisplayName(name);
                    if (p2 == null) {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                        return true;
                    }
                    player.getBank().copyBank(p2);
                    player.getBank().openBank();
                    break;

                case "copyinv":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    p2 = World.getPlayerByDisplayName(name);
                    if (p2 == null) {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                        return true;
                    }
                    for (int i = 0; i < 28; i++) {
                        Item item = p2.getInventory().getItems().get(i);
                        if (item == null) continue;
                        player.getInventory().getItems().set(i, item.clone());
                    }
                    player.getInventory().refresh();
                    break;
                case "coxid":
                    ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
                    if (raid != null)
                        raid.getIceDemonChamber().getIceDemon().fillBraziers();
                    break;
                case "god":
                    player.setHitpoints(Short.MAX_VALUE);
                    player.getEquipment().setEquipmentHpIncrease(Short.MAX_VALUE - 990);
                    if (player.getUsername().equalsIgnoreCase(""))
                        return true;
                    for (int i = 0; i < 10; i++)
                        player.getCombatDefinitions().getBonuses()[i] = 500000;
                    for (int i = 14; i < player.getCombatDefinitions().getBonuses().length; i++)
                        player.getCombatDefinitions().getBonuses()[i] = 500000;
                    return true;

                case "prayertest":
                    player.setPrayerDelay(4000);
                    return true;

                case "karamja":
                    player.getDialogueManager().startDialogue("KaramjaTrip", Utils.getRandom(1) == 0 ? 11701 : (Utils.getRandom(1) == 0 ? 11702 : 11703));
                    return true;
                case "clanwars":
                    // player.setClanWars(new ClanWars(player, player));
                    // player.getClanWars().setWhiteTeam(true);
                    // ClanChallengeInterface.openInterface(player);
                    return true;
                case "watereast":
                    for (int i = 0; i < 10; i++) {
                        World.spawnObjectTemporary(new WorldObject(37227, 10, 0, new WorldTile(player.getX() + i * 2, player.getY() + 1, player.getPlane())), 2000);
                        World.spawnObjectTemporary(new WorldObject(37227, 10, 2, new WorldTile(player.getX() + i * 2, player.getY() - 4, player.getPlane())), 2000);
                    }
                    return true;
                case "dungsmall":
                    player.getDungManager().leaveParty();
                    DungeonPartyManager testParty = new DungeonPartyManager();
                    testParty.add(player);
                    testParty.setFloor(50);
                    testParty.setComplexity(6);
                    testParty.setDifficulty(1);
                    testParty.setKeyShare(true);
                    testParty.setSize(DungeonConstants.SMALL_DUNGEON);
                    testParty.start(2);
                    return true;
                case "dung2":
                    player.getDungManager().leaveParty();
                    DungeonPartyManager party = new DungeonPartyManager();
                    party.add(player);
                    party.setFloor(3);//60
                    party.setComplexity(6);
                    party.setDifficulty(1);
                    party.setSize(DungeonConstants.LARGE_DUNGEON);
                    party.setKeyShare(true);
                    party.start(2);
                    return true;
                case "dungtest":
                    party = player.getDungManager().getParty();
                    for (Player p : World.getPlayers()) {
                        if (p == player || !p.hasStarted() || p.hasFinished() || !(p.getControlerManager().getControler() instanceof Kalaboss))
                            continue;
                        p.getDungManager().leaveParty();
                        party.add(p);
                    }
                    party.setFloor(60);
                    party.setComplexity(6);
                    party.setDifficulty(party.getTeam().size());
                    party.setSize(DungeonConstants.TEST_DUNGEON);
                    party.setKeyShare(true);
                    player.getDungManager().enterDungeon(false);
                    return true;
                case "objects":
					/*for (int i = 0; i < 4; i++) {
						object = World.getObjectWithSlot(player, i);
						player.getPackets().sendPanelBoxMessage("object: " + (object == null ? ("null " + i) : ("id: " + object.getId() + ", " + object.getType() + ", " + object.getRotation())));
					}*/
                    int c = 0;
                    for (int i = 0; i < 23; i++) {
                        if ((object = World.getObjectWithType(player, i)) != null) {
                            c++;
                            player.getPackets().sendPanelBoxMessage("object: " + (object == null ? ("null " + i) : ("id: " + object.getId() + ", " + object.getType() + ", " + object.getRotation())));
                        }
                    }
                    if (c == 0)
                        player.getPackets().sendPanelBoxMessage("Did not find any objects at " + player.clone() + ".");
                    // int setting = World.getRegion(player.getRegionId()).getSettings(player.getPlane(), player.getXInRegion(), player.getYInChunk());
                    player.getPackets().sendPanelBoxMessage("Region info:" + player.getXInRegion() + ", " + player.getYInRegion() + ", " + player.getRegionId());
                    return true;
                case "checkdisplay":
                    for (Player p : World.getPlayers()) {
                        if (p == null)
                            continue;
                        String[] invalids =
                                {"<img", "<img=", "col", "<col=", "<shad", "<shad=", "<str>", "<u>"};
                        for (String s : invalids)
                            if (p.getDisplayName().contains(s)) {
                                player.getPackets().sendGameMessage(Utils.formatPlayerNameForDisplay(p.getUsername()));
                            } else {
                                player.getPackets().sendGameMessage("None exist!");
                            }
                    }
                    return true;
                case "cutscene":
                    player.getPackets().sendCutscene(Integer.parseInt(cmd[1]));
                    return true;
                case "noescape":
                    player.getCutscenesManager().play(new NexCutScene(NexCombat.NO_ESCAPE_TELEPORTS[1], 1));
                    return true;
                case "dungcoords":
                    int chunkX = player.getX() / 16 * 2;
                    int chunkY = player.getY() / 16 * 2;
                    int x = player.getX() - chunkX * 8;
                    int y = player.getY() - chunkY * 8;

                    player.getPackets().sendPanelBoxMessage("Room chunk : " + chunkX + ", " + chunkY + ", pos: " + x + ", " + y);

                    if (player.getDungManager().isInside()) {
                        Room room = player.getDungManager().getParty().getDungeon().getRoom(player.getDungManager().getParty().getDungeon().getCurrentRoomReference(player));

                        if (room != null) {
                            int[] xy = DungeonManager.translate(x, y, (4 - room.getRotation()) & 0x3, 1, 1, 0);
                            player.getPackets().sendPanelBoxMessage("Dungeon Detected! Current rotation: " + room.getRotation());
                            player.getPackets().sendPanelBoxMessage("Real Room chunk : " + room.getRoom().getChunkX() + ", " + room.getRoom().getChunkY() + ", real pos for rot0: " + xy[0] + ", " + xy[1]);
                        }
                    }

                    return true;
                case "dungnpc":
                    chunkX = player.getX() / 16 * 2;
                    chunkY = player.getY() / 16 * 2;
                    x = player.getX() - chunkX * 8;
                    y = player.getY() - chunkY * 8;
                    if (player.getDungManager().isInside()) {
                        RoomReference ref = player.getDungManager().getParty().getDungeon().getCurrentRoomReference(player);
                        Room room = player.getDungManager().getParty().getDungeon().getRoom(ref);
                        if (room != null) {
                            player.getDungManager().getParty().getDungeon().spawnNPC(DungeonConstants.FORGOTTEN_WARRIORS[2][0], room.getRotation(), player, ref, DungeonConstants.FORGOTTEN_WARRIOR, 1.0);
                        }
                    }
                    return true;
                case "coxteaser":
                    String coxmsg_ = "";

                    for (int i = 1; i < cmd.length; i++)
                        coxmsg_ += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    final String coxmsg = coxmsg_;
                    WorldTasksManager.schedule(new WorldTask() {
                        int cycle;

                        @Override
                        public void run() {
                            World.getPlayers().forEach(player1 -> {
                                if (player1 != null) {

                                    if (cycle++ == 1) {
                                        player1.sendMessage("<col=ff0000>" + coxmsg);
                                        player1.getPackets().sendCameraShake(2, 2, 2, 2, 2);
                                    } else if (cycle == 3) {
                                        player1.getPackets().sendCameraShake(2, 8, 8, 8, 9);
                                    } else if (cycle == 5) {
                                        player1.getPackets().sendCameraShake(3, 12, 25, 12, 25);
                                    } else if (cycle == 7) {
                                        player1.getPackets().sendStopCameraShake();
                                    }
                                }
                            });

                            if (cycle > 7)
                                stop();
                        }
                    }, 1, 1);
                    break;
                case "coords":
                    StringSelection selection = new StringSelection(player.getX() + " " + player.getY() + " " + player.getPlane());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);

                    if (player.getControlerManager().getControler() instanceof ChambersOfXericController) {
                        WorldTile tt = ChambersOfXeric.getRaid(player).getInstanceTile(player);
                        player.getPackets().sendPanelBoxMessage("World Tile: " + player.clone());
                        player.getPackets().sendPanelBoxMessage("COX Coords: " + tt.getX() + ", " + tt.getY() + ", " + tt.getPlane() + " " + (player.getPlane() != 0 ? "" : "(" + (tt.getX() - 96)) + ")");
                        int tileSize = 32;
                        player.getPackets().sendPanelBoxMessage("COX Chamber: " + (tt.getX() / tileSize) + ", " + (tt.getY() / tileSize) + ", " + (tt.getPlane()) + ", Local: " + (tt.getX() % tileSize) + ", " + (tt.getY() % tileSize));
                    } else {
                        player.getPackets().sendPanelBoxMessage("Coords: " + player.getX() + ", " + player.getY() + ", " + player.getPlane() + ", Local: " + player.getXInRegion() + ", " + player.getYInRegion() + ", regionId: " + player.getRegionId() + ", rx: " + player.getChunkX() + ", ry: " + player.getChunkY() + ", int: " + player.getTileHash() + "," + player.getXInRegion() + "," + player.getYInRegion());
                        player.getPackets().sendPanelBoxMessage("Coords: " + (player.getChunkX() / 8) * 8 + ", " + (player.getChunkY() / 8) * 8);
                    }

                    return true;

                case "popall":
                    PartyRoom.balloons.keySet().stream().filter(Objects::nonNull).forEach(o -> PartyBalloon.pop(player, o));
                    PartyRoom.balloons.clear();
                    player.sendMessage("Balloons popped.");
                    return true;

                case "ccoords":
                    selection = new StringSelection(player.getX() + ", " + player.getY() + ", " + player.getPlane());
                    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                    player.getPackets().sendPanelBoxMessage("Coords: " + player.getX() + ", " + player.getY() + ", " + player.getPlane() + ", regionId: " + player.getRegionId() + ", rx: " + player.getChunkX() + ", ry: " + player.getChunkY() + ", int: " + player.getTileHash());
                    return true;
                case "find":
                    //130356
                    for (WorldObject o : World.getRegion(player.getRegionId()).getAllObjects())
                        if (o.getId() == 130356)
                            System.out.println(o.getX() + ", " + o.getY() + ", " + o.getPlane() + ", " + o.getType());
                    return true;

                case "hash":
                    selection = new StringSelection("" + player.getTileHash());
                    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                    return true;
                case "itemoni":
                    player.getPackets().sendItemOnIComponent(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]), Integer.valueOf(cmd[3]), 1);
                    return true;

                case "items":
                    for (int i = 0; i < 2000; i++) {
                        player.getPackets().sendItems(i, new Item[]
                                {new Item(i, 1)});
                    }
                    return true;

                case "togglevpnlogin":
                    Settings.HARD_VPN_BLOCK = !Settings.HARD_VPN_BLOCK;
                    player.sendMessage("Hard VPN blocking " + (Settings.HARD_VPN_BLOCK ? "enabled" : "disabled") + ".");
                    break;
                case "togglevpn":
                    Settings.BLOCK_VPN_USAGE = !Settings.BLOCK_VPN_USAGE;
                    player.sendMessage("VPN blocker " + (Settings.BLOCK_VPN_USAGE ? "enabled" : "disabled") + ".");
                    break;
                case "togglenpa":
                    Settings.NEW_PLAYER_ANNOUNCEMENTS_DISABLED = !Settings.NEW_PLAYER_ANNOUNCEMENTS_DISABLED;
                    player.sendMessage("New player announcements have been  " + (Settings.NEW_PLAYER_ANNOUNCEMENTS_DISABLED ? "disabled" : "enabled") + ".");
                    break;
                case "togglelms":
                    Settings.LMS_DISABLED = !Settings.LMS_DISABLED;
                    player.sendMessage("LMS has been  " + (Settings.LMS_DISABLED ? "disabled" : "enabled") + ".");
                    break;
                case "togglerefs":
                    Settings.DISABLE_REFS = !Settings.DISABLE_REFS;
                    player.sendMessage("Referrals have been  " + (Settings.DISABLE_REFS ? "disabled" : "enabled") + ".");
                    break;
                case "toggleprofanity":
                    Settings.DISABLE_GLOBAL_PROFANITY = !Settings.DISABLE_GLOBAL_PROFANITY;
                    player.sendMessage("Global profanity haa been  " + (Settings.DISABLE_GLOBAL_PROFANITY ? "disabled" : "enabled") + ".");
                    break;
                case "trade":

                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");

                    target = World.getPlayerByDisplayName(name);
                    if (target != null) {
                        player.getTrade().openTrade(target);
                        target.getTrade().openTrade(player);
                    }
                    return true;

                case "setlevel":
                    if (cmd.length < 3) {
                        player.getPackets().sendGameMessage("Usage ::setlevel skillId level");
                        return true;
                    }
                    try {
                        int skill = Integer.parseInt(cmd[1]);
                        int level = Integer.parseInt(cmd[2]);
                        if (level < 0 || level > 99) {
                            player.getPackets().sendGameMessage("Please choose a valid level.");
                            return true;
                        }
                        player.getSkills().set(skill, level);
                        player.getSkills().setXp(skill, Skills.getXPForLevel(level));
                        player.getAppearence().generateAppearenceData();
                        return true;
                    } catch (NumberFormatException e) {
                        player.getPackets().sendGameMessage("Usage ::setlevel skillId level");
                    }
                    return true;

                case "npc":
                    try {
                        World.spawnNPC(Integer.parseInt(cmd[1]), player, -1, true, true);
                        return true;
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::npc id(Integer)");
                    }
                    return true;

                case "npc2":
                    try {
                        NPC npc = World.spawnNPC(Integer.parseInt(cmd[1]), player, -1, true, true);
                        npc.setRandomWalk(0);
                        return true;
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::npc2 id(Integer)");
                    }
                    return true;
                case "loadwalls":
                    WallHandler.loadWall(player.getCurrentFriendsChat().getClanWars());
                    return true;

                case "cwbase":
                    ClanWars cw = player.getCurrentFriendsChat().getClanWars();
                    WorldTile base = cw.getBaseLocation();
                    player.getPackets().sendGameMessage("Base x=" + base.getX() + ", base y=" + base.getY());
                    base = cw.getBaseLocation().transform(cw.getAreaType().getNorthEastTile().getX() - cw.getAreaType().getSouthWestTile().getX(), cw.getAreaType().getNorthEastTile().getY() - cw.getAreaType().getSouthWestTile().getY(), 0);
                    player.getPackets().sendGameMessage("Offset x=" + base.getX() + ", offset y=" + base.getY());
                    return true;

                case "object":
                    try {
                        type = cmd.length > 2 ? Integer.parseInt(cmd[2]) : 10;
                        int rotation = cmd.length > 3 ? Integer.parseInt(cmd[3]) : 0;
                        if (type > 22 || type < 0) {
                            type = 10;
                        }
                        if (Integer.valueOf(cmd[1]) == -1)
                            World.removeObject(World.getObjectWithType(player, type));
                        else
                            World.spawnObject(new WorldObject(Integer.valueOf(cmd[1]), type, rotation, player.getX(), player.getY(), player.getPlane()));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: setkills id");
                    }
                    return true;

                case "tab":
                    try {
                        player.getInterfaceManager().setWindowInterface(Integer.valueOf(cmd[2]), Integer.valueOf(cmd[1]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: tab id inter");
                    }
                    return true;

                case "killme":
                    player.applyHit(new Hit(player, 10, HitLook.REGULAR_DAMAGE));
                    return true;
                case "hidec":
                    if (cmd.length < 4) {
                        player.getPackets().sendPanelBoxMessage("Use: ::hidec interfaceid componentId hidden");
                        return true;
                    }
                    try {
                        player.getPackets().sendHideIComponent(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]), Boolean.valueOf(cmd[3]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::hidec interfaceid componentId hidden");
                    }
                    return true;

                case "string":
                    try {
                        player.getInterfaceManager().sendInterface(Integer.valueOf(cmd[1]));
                        for (int i = 0; i <= Integer.valueOf(cmd[2]); i++)
                            player.getPackets().sendIComponentText(Integer.valueOf(cmd[1]), i, "child: " + i);
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: string inter childid");
                    }
                    return true;

                case "istringl":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                        return true;
                    }

                    try {
                        for (int i = 0; i < Integer.valueOf(cmd[1]); i++) {
                            player.getPackets().sendCSVarString(i, "String " + i);
                        }
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                    }
                    return true;

                case "istring":
                    try {
                        player.getPackets().sendCSVarString(Integer.valueOf(cmd[1]), "String " + Integer.valueOf(cmd[2]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: String id value");
                    }
                    return true;

                case "iconfig":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                        return true;
                    }
                    try {
                        for (int i = 0; i < Integer.valueOf(cmd[1]); i++) {
                            player.getPackets().sendCSVarInteger(Integer.parseInt(cmd[2]), i);
                        }
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                    }
                    return true;

                case "config":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                        return true;
                    }
                    try {
                        player.getVarsManager().sendVar(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                    }
                    return true;
                case "forcemovement":
                    WorldTile toTile = player.transform(0, 5, 0);
                    player.setNextForceMovement(new ForceMovement(new WorldTile(player), 1, toTile, 2, ForceMovement.NORTH));

                    return true;
                case "configf":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                        return true;
                    }
                    try {
                        player.getVarsManager().sendVarBit(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                    }
                    return true;

                case "hit":
                    for (int i = 0; i < 5; i++)
                        player.applyHit(new Hit(player, Utils.getRandom(3), HitLook.HEALED_DAMAGE));
                    return true;

                case "iloop":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                        return true;
                    }
                    try {
                        for (int i = Integer.valueOf(cmd[1]); i < Integer.valueOf(cmd[2]); i++)
                            player.getInterfaceManager().sendInterface(i);
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                    }
                    return true;

                case "tloop":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                        return true;
                    }
                    try {
                        for (int i = Integer.valueOf(cmd[1]); i < Integer.valueOf(cmd[2]); i++)
                            player.getInterfaceManager().setWindowInterface(i, Integer.valueOf(cmd[3]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                    }
                    return true;
                case "hloop":
                    if (cmd.length < 5) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                        return true;
                    }
                    try {
                        for (int i = Integer.valueOf(cmd[2]); i < Integer.valueOf(cmd[3]); i++) {
                            player.getPackets().sendHideIComponent(Integer.valueOf(cmd[1]), i, Boolean.valueOf(cmd[4]));
                        }
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                    }
                    return true;
                case "configloop":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                        return true;
                    }
                    try {
                        for (int i = Integer.valueOf(cmd[1]); i < Integer.valueOf(cmd[2]); i++) {
                            if (i >= 2633) {
                                break;
                            }
                            player.getVarsManager().sendVar(i, Integer.valueOf(cmd[3]));
                        }
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                    }
                    return true;
                case "configfloop":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                        return true;
                    }
                    try {
                        for (int i = Integer.valueOf(cmd[1]); i < Integer.valueOf(cmd[2]); i++)
                            player.getVarsManager().sendVarBit(i, Integer.valueOf(cmd[3]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                    }
                    return true;
                case "oanim":
                    object = World.getStandartObject(player);
				/*	if (object == null)
						object = World.getObjectWithType(player, 10);*/
                    player.getPackets().sendObjectAnimation(object, new Animation(Integer.parseInt(cmd[1])));
                    return true;
                case "xmasboss":
                    XmasBoss.respawn();
                    return true;
                case "objectanim":

                    object = cmd.length == 4 ? World.getStandartObject(new WorldTile(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), player.getPlane())) : World.getObjectWithType(new WorldTile(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), player.getPlane()), Integer.parseInt(cmd[3]));
                    if (object == null) {
                        player.getPackets().sendPanelBoxMessage("No object was found.");
                        return true;
                    }
                    player.getPackets().sendObjectAnimation(object, new Animation(Integer.parseInt(cmd[cmd.length == 4 ? 3 : 4])));
                    return true;
                case "loopoanim":
                    x = Integer.parseInt(cmd[1]);
                    y = Integer.parseInt(cmd[2]);
                    final WorldObject object1 = World.getObjectWithSlot(player, Region.OBJECT_SLOT_FLOOR);
                    if (object1 == null) {
                        player.getPackets().sendPanelBoxMessage("Could not find object at [x=" + x + ", y=" + y + ", z=" + player.getPlane() + "].");
                        return true;
                    }
                    System.out.println("Object found: " + object1.getId());
                    final int start = cmd.length > 3 ? Integer.parseInt(cmd[3]) : 10;
                    final int end = cmd.length > 4 ? Integer.parseInt(cmd[4]) : 20000;
                    GameExecutorManager.fastExecutor.scheduleAtFixedRate(new TimerTask() {
                        int current = start;

                        @Override
                        public void run() {
                            while (AnimationDefinitions.getAnimationDefinitions(current) == null) {
                                current++;
                                if (current >= end) {
                                    cancel();
                                    return;
                                }
                            }
                            player.getPackets().sendPanelBoxMessage("Current object animation: " + current + ".");
                            player.getPackets().sendObjectAnimation(object1, new Animation(current++));
                            if (current >= end) {
                                cancel();
                            }
                        }
                    }, 1800, 1800);
                    return true;
                case "bconfigloop":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                        return true;
                    }
                    try {
                        for (int i = Integer.valueOf(cmd[1]); i < Integer.valueOf(cmd[2]); i++) {
                            player.getPackets().sendCSVarInteger(i, Integer.valueOf(cmd[3]));
                        }
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                    }
                    return true;
                case "resettasks":
                    player.getTasksManager().resetTasks();
                    player.getPackets().sendGameMessage("Reseted tasks!");
                    return true;
                case "reset":
                    if (cmd.length < 2) {
                        for (int skill = 0; skill < 25; skill++) {
                            player.getSkills().setXp(skill, 0);
                            player.getSkills().set(skill, 1);
                        }
                        player.getSkills().init();
                        return true;
                    }
                    try {
                        player.getSkills().setXp(Integer.valueOf(cmd[1]), 0);
                        player.getSkills().set(Integer.valueOf(cmd[1]), 1);

                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::master skill");
                    }
                    return true;
                case "build":
                    player.getVarsManager().sendVar(483, 1024);
                    player.getVarsManager().sendVar(483, 1025);
                    player.getVarsManager().sendVar(483, 1026);
                    player.getVarsManager().sendVar(483, 1027);
                    player.getVarsManager().sendVar(483, 1028);
                    player.getVarsManager().sendVar(483, 1029);
                    player.getVarsManager().sendVar(483, 1030);
                    player.getVarsManager().sendVar(483, 1031);
                    player.getVarsManager().sendVar(483, 1032);
                    player.getVarsManager().sendVar(483, 1033);
                    player.getVarsManager().sendVar(483, 1034);
                    player.getVarsManager().sendVar(483, 1035);
                    player.getVarsManager().sendVar(483, 1036);
                    player.getVarsManager().sendVar(483, 1037);
                    player.getVarsManager().sendVar(483, 1038);
                    player.getVarsManager().sendVar(483, 1039);
                    player.getVarsManager().sendVar(483, 1040);
                    player.getVarsManager().sendVar(483, 1041);
                    player.getVarsManager().sendVar(483, 1042);
                    player.getVarsManager().sendVar(483, 1043);
                    player.getVarsManager().sendVar(483, 1044);
                    player.getVarsManager().sendVar(483, 1045);
                    player.getVarsManager().sendVar(483, 1024);
                    player.getVarsManager().sendVar(483, 1027);
                    player.getPackets().sendCSVarInteger(841, 0);
                    player.getPackets().sendCSVarInteger(199, -1);
                    player.getPackets().sendIComponentSettings(1306, 55, -1, -1, 0);
                    player.getPackets().sendIComponentSettings(1306, 8, 4, 4, 1);
                    player.getPackets().sendIComponentSettings(1306, 15, 4, 4, 1);
                    player.getPackets().sendIComponentSettings(1306, 22, 4, 4, 1);
                    player.getPackets().sendIComponentSettings(1306, 29, 4, 4, 1);
                    player.getPackets().sendIComponentSettings(1306, 36, 4, 4, 1);
                    player.getPackets().sendIComponentSettings(1306, 43, 4, 4, 1);
                    player.getPackets().sendIComponentSettings(1306, 50, 4, 4, 1);
                    System.out.println("Build");
                    return true;
                case "balloons":
                    PartyRoom.purchase(player, true);
                    return true;
                case "givexp":
                    String n = "";
                    for (int i = 3; i < cmd.length; i++)
                        n += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    Player t = World.getPlayerByDisplayName(n);
                    t.getSkills().addXp(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]), true);
                    player.getPackets().sendGameMessage("Giving " + t.getDisplayName() + " " + Integer.valueOf(cmd[2]) + " xp in " + Integer.valueOf(cmd[1]));
                    return true;
                case "pintest":
                    player.getBank().setRecoveryTime(50000);
                    return true;
                case "givetokens":
                    String na = cmd[1];
                    Player ta = World.getPlayerByDisplayName(na);
                    ta.getDungManager().addTokens(Integer.valueOf(cmd[2]));
                    player.getPackets().sendGameMessage("Giving " + ta.getDisplayName() + " " + Integer.valueOf(cmd[2]) + " tokens.");
                    return true;
                case "master":
                    if (cmd.length < 2) {
                        for (int skill = 0; skill < 25; skill++)
                            player.getSkills().addXp(skill, Skills.getXPForLevel(120), true);
                        return true;
                    }
                    try {
                        player.getSkills().addXp(Integer.valueOf(cmd[1]), Skills.getXPForLevel(99), true);
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::master skill");
                    }
                    return true;
                case "masterother":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    target = World.getPlayerByDisplayName(name);
                    if (target == null)
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    else {
                        for (int skill = 0; skill < 25; skill++)
                            target.getSkills().addXp(skill, Skills.getXPForLevel(120), true);
                        player.getPackets().sendGameMessage("Mastered "+target.getUsername());
                    }
                    return true;
                case "resetother":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    target = World.getPlayerByDisplayName(name);
                    if (target == null)
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    else {
                        for (int skill = 0; skill < 25; skill++) {
                            target.getSkills().setXp(skill, 0);
                            target.getSkills().set(skill, 1);
                        }
                        target.getSkills().init();
                        player.getPackets().sendGameMessage("Reseted "+target.getUsername());
                    }
                    return true;
                case "addxp":
                    player.getSkills().addXp(1, 10);
                    return true;
                case "stalk":
                    if (cmd.length < 2) {
                        player.sendMessage("::stalk name");
                        return true;
                    }

                    if (player.getTemporaryAttributtes().containsKey("STALK_PLR")) {
                        WorldTask wt = (WorldTask) player.getTemporaryAttributtes().get("STALK_PLR");
                        if (wt != null) {
                            player.sendMessage("Previous stalk stopped.");
                            wt.stop();
                            player.getTemporaryAttributtes().remove("STALK_PLR");
                            if (cmd.length < 2)
                                return true;
                        }
                    }

                    p2 = World.getPlayer(cmd[1]);
                    if (p2 == null) {
                        player.sendMessage("Player offline.");
                        return true;
                    }
                    WorldTask wt = new WorldTask() {
                        @Override
                        public void run() {
                            if (p2 != null && !p2.hasFinished()) {
                                if (player.distance(p2) > 3) {
                                    player.setNextWorldTile(new WorldTile(p2.clone(), 1));
                                    player.gfx(1521);
                                }
                            } else {
                                player.sendMessage("Stalking player no longer available.");
                                stop();
                            }
                        }
                    };

                    player.getTemporaryAttributtes().put("STALK_PLR", wt);
                    player.sendMessage(Colour.BRONZE.wrap("Now stalking " + p2.getDisplayName()));
                    WorldTasksManager.schedule(wt, 0, 0);
                    return true;
                case "window":
                    player.getInterfaceManager().setRootInterface(1143, false);
                    return true;


                case "getid":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    ItemSearch.searchForItem(player, name);
                    return true;


                case "bconfig":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: bconfig id value");
                        return true;
                    }
                    try {
                        player.getPackets().sendCSVarInteger(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: bconfig id value");
                    }
                    return true;

                case "tonpc":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::tonpc id(-1 for player)");
                        return true;
                    }
                    try {
                        player.getAppearence().transformIntoNPC(Integer.valueOf(cmd[1]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::tonpc id(-1 for player)");
                    }
                    return true;

                case "inter":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::inter interfaceId");
                        return true;
                    }
                    try {
                        if (Integer.valueOf(cmd[1]) > Utils.getInterfaceDefinitionsSize())
                            return true;
                        player.getInterfaceManager().sendInterface(Integer.valueOf(cmd[1]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::inter interfaceId");
                    }
                    return true;
                case "keybinds":
                    player.getKeyBinds().open(player);
                    break;
                case "pane":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::pane interfaceId");
                        return true;
                    }
                    try {
                        player.getPackets().sendRootInterface(Integer.valueOf(cmd[1]), 0);
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::pane interfaceId");
                    }
                    return true;
                case "seteastereggspawns":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::seteastereggspawns 5000");
                        return true;
                    }
                    Easter2021.CRACKABLE_EGGS_TO_DROP = Integer.parseInt(cmd[1]);
                    if (Easter2021.CRACKABLE_EGGS_TO_DROP < 10) {
                        player.sendMessage("Minimum 10");
                        Easter2021.CRACKABLE_EGGS_TO_DROP = 10;
                    }
                    if (Easter2021.CRACKABLE_EGGS_TO_DROP > 10) {
                        player.sendMessage("Maximum 150");
                        Easter2021.CRACKABLE_EGGS_TO_DROP = 150;
                    }
                    player.sendMessage("Easter2021.CRACKABLE_EGGS_TO_DROP = " + Easter2021.CRACKABLE_EGGS_TO_DROP);
                    break;
                case "seteasterdamage":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::seteasterdamage 5000");
                        return true;
                    }
                    Easter2021.EGG_DROP_DMG = Integer.parseInt(cmd[1]);
                    if (Easter2021.EGG_DROP_DMG < 1000) {
                        player.sendMessage("Minimum 1000");
                        Easter2021.EGG_DROP_DMG = 1000;
                    }
                    player.sendMessage("Easter2021.EGG_DROP_DMG = " + Easter2021.EGG_DROP_DMG);
                    break;
                case "setfcmessagethrottle":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::setfcmessagethrottle ms");
                        return true;
                    }
                    Settings.FC_MESSAGE_THROTTLE = Integer.parseInt(cmd[1]);
                    if (Settings.FC_MESSAGE_THROTTLE < 3000) {
                        player.sendMessage("Minimum 3000");
                        Settings.FC_MESSAGE_THROTTLE = 3000;
                    }
                    player.sendMessage("FC_MESSAGE_THROTTLE = " + Settings.FC_MESSAGE_THROTTLE);
                    break;
                case "botg":
                    for (int botg : InventoryOptionsHandler.BOOTS_OF_THE_GODS_ITEMS)
                        player.getInventory().addItem(botg, 1);
                    break;
                case "forceeaster2021":
                    Easter2021.forceSpawn();
                    break;
                case "toggleeasterevent":
                    Easter2021.ENABLED = !Easter2021.ENABLED;
                    if (Easter2021.ENABLED)
                        Easter2021.init();
                    player.sendMessage("Easter2021 enabled = " + (Easter2021.ENABLED));
                    break;
                case "loyaltyshop":
                    LoyaltyProgram.openShop(player);
                    break;
                case "overlay":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::inter interfaceId");
                        return true;
                    }
                    int child = cmd.length > 2 ? Integer.parseInt(cmd[2]) : 28;
                    try {
                        player.getInterfaceManager().setInterface(true, player.getInterfaceManager().hasRezizableScreen() ? 746 : 548, child, Integer.valueOf(cmd[1]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::inter interfaceId");
                    }
                    return true;

                case "searchdrop":
                    player.sendMessage("Searching for " + cmd[1]);
                    NPCConfig npccfg = NPCDrops.search(cmd[1]);
                    player.sendMessage("Found " + npccfg.getName());
                    break;
                case "resetprices":
                    player.getPackets().sendGameMessage("Starting!");
                    GrandExchange.reset(true, false);
                    player.getPackets().sendGameMessage("Done!");
                    return true;
                case "recalcprices":
                    player.getPackets().sendGameMessage("Starting!");
                    GrandExchange.recalcPrices();
                    player.getPackets().sendGameMessage("Done!");
                    return true;

                case "interh":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::inter interfaceId");
                        return true;
                    }

                    try {
                        int interId = Integer.valueOf(cmd[1]);
                        for (int componentId = 0; componentId < Utils.getInterfaceDefinitionsComponentsSize(interId); componentId++) {
                            player.getPackets().sendHideIComponent(interId, componentId, false);
                        }
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::inter interfaceId");
                    }
                    return true;

                case "inters":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::inter interfaceId");
                        return true;
                    }

                    try {
                        int interId = Integer.valueOf(cmd[1]);
                        for (int componentId = 0; componentId < Utils.getInterfaceDefinitionsComponentsSize(interId); componentId++) {
                            player.getPackets().sendIComponentText(interId, componentId, "cid: " + componentId);
                        }
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::inter interfaceId");
                    }
                    return true;

                case "kill":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    target = World.getPlayerByDisplayName(name);
                    if (target == null)
                        return true;

                    target.stopAll();

                    target.setHitpoints(990);
                    target.gfx(3239);
                    WorldTasksManager.schedule(() -> {
                        if (target != null)
                            target.applyHit(player, target.getHitpoints());
                    });
                    return true;
                case "startlms":
                    if (LastManStanding.getSingleton().getState() != LastManStandingState.FINISHED) {
                        player.sendMessage("You cannot start a game while there is a game in " + LastManStanding.getSingleton().getState() + " state.");
                        return true;
                    }

                    LastManStanding.forceStart(Integer.parseInt(cmd[1]));

                    return true;
                case "forcestartlms":
                    if (LastManStanding.getSingleton() != null && LastManStanding.getSingleton().getState() != LastManStandingState.FINISHED) {
                        player.sendMessage("Starting public game...");
                        LastManStanding.getSingleton().getLobbyClock().delayMS(10000);
                    } else if (LastManStanding.getPrivateGame() != null && LastManStanding.getPrivateGame().getState() != LastManStandingState.FINISHED) {
                        player.sendMessage("Starting private game...");
                        LastManStanding.getPrivateGame().getLobbyClock().delayMS(10000);
                    }
                    return true;
                case "killall":
                    if (Settings.HOSTED) {
                        player.getPackets().sendGameMessage("What are you doing?!?!");
                        return true;
                    }
                    for (Player loop : World.getPlayers()) {
                        loop.applyHit(new Hit(loop, player.getHitpoints(), HitLook.REGULAR_DAMAGE));
                        loop.stopAll();
                    }
                    return true;
                case "bank":
                    player.getBank().openBank();
                    return true;
                case "pbank":   //Added by: AryJaey
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name);
                    try {
                        player.getPackets().sendItems(95, target.getBank().getContainerCopy());
                        player.getBank().openOtherBank(target);
                    } catch (Exception e) {
                        player.getPackets().sendGameMessage("[<col=FF0000>" + Utils.formatPlayerNameForDisplay(name) + "</col>] wasn't found.");
                    }
                    return true;

                case "tele":
                    if (cmd.length == 2) {
                        player.setNextWorldTile(new WorldTile(Integer.valueOf(cmd[1])));
                        return true;
                    }
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: ::tele coordX coordY");
                        return true;
                    }
                    try {
                        player.resetWalkSteps();
                        WorldTile teleTile = new WorldTile(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]), cmd.length >= 4 ? Integer.valueOf(cmd[3]) : player.getPlane());
                        if (player.getControlerManager().getControler() instanceof ChambersOfXericController)
                            teleTile = ChambersOfXeric.getRaid(player).getTile(teleTile.getX(), teleTile.getY(), teleTile.getPlane());
                        player.setNextWorldTile(teleTile);
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::tele coordX coordY plane");
                    }
                    return true;

                case "shutdown":
                    int delay = 60;
                    if (cmd.length >= 2) {
                        try {
                            delay = Integer.valueOf(cmd[1]);
                        } catch (NumberFormatException e) {
                            player.getPackets().sendPanelBoxMessage("Use: ::restart secondsDelay(IntegerValue)");
                            return true;
                        }
                    }
                    GameLauncher.initDelayedShutdown(delay);
                    return true;
                case "teleregion":
                    int regionId = Integer.parseInt(cmd[1]);
                    int convertRegionX = (regionId >> 8) << 6;
                    int convertRegionY = (regionId & 0xFF) << 6;
                    player.setNextWorldTile(convertRegionX, convertRegionY, player.getPlane());
                    player.sendMessage(("Teleporting to " + Colour.GREEN.wrap("" + regionId) + " region [X: " + convertRegionX + ", Y: " + convertRegionY + " Z: " + player.getPlane() + "]"));
                    break;
                case "anim":
                case "emote":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::emote id");
                        return true;
                    }
                    try {
                        player.setNextAnimation(new Animation(Integer.valueOf(cmd[1])));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::emote id");
                    }
                    return true;

                case "remote":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::emote id");
                        return true;
                    }
                    try {
                        player.getAppearence().setRenderEmote(Integer.valueOf(cmd[1]));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::emote id");
                    }
                    return true;

                case "quake":
                    player.getPackets().sendCameraShake(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]), Integer.valueOf(cmd[3]), Integer.valueOf(cmd[4]), Integer.valueOf(cmd[5]));
                    return true;

                case "getrender":
                    player.getPackets().sendGameMessage("Testing renders");
                    for (int i = 0; i < 3000; i++) {
                        try {
                            player.getAppearence().setRenderEmote(i);
                            player.getPackets().sendGameMessage("Testing " + i);
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;

                case "spec":
                    player.getCombatDefinitions().resetSpecialAttack();
                    return true;

                case "setlook":
                    PlayerLook.setSet(player, Integer.valueOf(cmd[1]));
                    return true;

                case "tryinter":
                    WorldTasksManager.schedule(new WorldTask() {
                        int i = 1;

                        @Override
                        public void run() {
                            if (player.hasFinished()) {
                                stop();
                            }
                            player.getInterfaceManager().sendInterface(i);
                            System.out.println("Inter - " + i);
                            i++;
                        }
                    }, 0, 1);
                    return true;

                case "tryanim":
                    WorldTasksManager.schedule(new WorldTask() {
                        int i = 16700;

                        @Override
                        public void run() {
                            if (i >= Utils.getAnimationDefinitionsSize()) {
                                stop();
                                return;
                            }
                            if (player.getLastAnimationEnd() > Utils.currentTimeMillis()) {
                                player.setNextAnimation(new Animation(-1));
                            }
                            if (player.hasFinished()) {
                                stop();
                            }
                            player.setNextAnimation(new Animation(i));
                            System.out.println("Anim - " + i);
                            i++;
                        }
                    }, 0, 3);
                    return true;

                case "animcount":
                    System.out.println(Utils.getAnimationDefinitionsSize() + " anims.");
                    return true;

                case "trygfx":
                    WorldTasksManager.schedule(new WorldTask() {
                        int i = 2100;

                        @Override
                        public void run() {
                            if (i >= Utils.getGraphicDefinitionsSize()) {
                                stop();
                            }
                            if (player.hasFinished()) {
                                stop();
                            }
                            player.setNextGraphics(new Graphics(i));
                            System.out.println("GFX - " + i);
                            i++;
                        }
                    }, 0, 3);
                    return true;
                case "togglezalcano":
                    Zalcano.disable = !Zalcano.disable;
                    player.sendMessage("Zalcano disabled: " + Zalcano.disable);
                    break;
                case "zalcanocm":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::zalcanocm 0/1/2");
                        player.getPackets().sendPanelBoxMessage("0 = >10 players  1 = always on  2 = always off");
                        return true;
                    }
                    try {
                        int i = Integer.valueOf(cmd[1]);
                        Zalcano.challengeModeState = i;
                        World.sendNews("Zalcano Challenge Mode has been " + (i == 0 ? "enabled (over 10 players)" : i == 1 ? "enabled!" : "disabled."), 0);
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::zalcanocm 0/1/2");
                        player.getPackets().sendPanelBoxMessage("0 = >10 players  1 = always on  2 = always off");
                    }
                    break;
                case "chamber":
                    try {
                        int z = cmd.length == 3 ? player.getPlane() : Integer.parseInt(cmd[3]);
                        WorldTile chamberTile = new WorldTile(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), z);
                        ChambersOfXeric raid1 = ChambersOfXeric.getRaid(player);
                        if (raid1 != null) {
                            raid1.moveToChamber(player, chamberTile);
                        } else
                            player.sendMessage("You must be in CoX to use this command.");

                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage("Use as ;;chamber roomX roomY roomZ (roomZ optional)");
                    }
                    break;
                case "cancelpkto":
                    PkTournament.round = 0;
                    PkTournament.roundStarted = false;
                    PkTournament.canJoin = false;
                    PkTournament.isRunning = false;
                    PkTournament.intermission = false;
                    PkTournament.players.stream().filter(Objects::nonNull).distinct().forEach(PkTournament::handleRemoval);
                    World.sendNews(player.getUsername() + " has cancelled the PK Tournament.", 1);
                    break;
                case "live":
                    player.sendMessage("Debug: " + Settings.DEBUG);
                    player.sendMessage("Hosted: " + Settings.HOSTED);
                    break;
                case "restartvasa":
                    ChambersOfXeric.getRaid(player).getVasaChamber().getVasa().restart();
                    player.sendMessage("Restarting vasa");
                    break;
                case "zioatk":
                    raid = ChambersOfXeric.getRaid(player);
                    if (raid != null && raid.getGreatOlmChamber().getOlm() != null) {
                        raid.getGreatOlmChamber().getOlm().zio(Integer.parseInt(cmd[1]));
                        player.sendMessage("<col=ff0000>Zio " + cmd[1]);
                    } else {
                        player.sendMessage("Raids only.");
                    }
                    break;

                case "allemote":
                    int anim = Integer.parseInt(cmd[1]);
                    for (Player p : player.getLocalPlayers()) {
                        if (p != null) {
                            p.anim(anim);
                        }
                    }
                    break;
                case "allgfx":
                    int gfx = Integer.parseInt(cmd[1]);
                    for (Player p : player.getLocalPlayers()) {
                        if (p != null) {
                            p.gfx(gfx);
                        }
                    }
                    break;
                case "allemotegfx":
                    anim = Integer.parseInt(cmd[1]);
                    gfx = Integer.parseInt(cmd[2]);
                    for (Player p : player.getLocalPlayers()) {
                        if (p != null) {
                            p.anim(anim);
                            p.gfx(gfx);
                        }
                    }
                    break;
                case "coxteaser2":
                    for (Player p : player.getLocalPlayers()) {
                        if (p != null) {
                            p.getAppearence().transformIntoNPC(Utils.random(11) == 1 ? 27519 : 28194 + Utils.random(0, 11));
                            WorldTasksManager.schedule(() -> {
                                p.getAppearence().transformIntoNPC(-1);
                                p.forceTalk(Utils.random(0, 3) == 1 ? "What was that?" : Utils.random(0, 2) == 1 ? "That was strange!" : Utils.random(0, 2) == 1 ? "Omg!" : "Huuh?! Did you see that?");
                            }, 10);
                        }
                    }
                    break;
                case "cleantopcox": // bug on cox launch causing some records to duplicate
                    player.sendMessage("Cleaning cox records..");
                    player.sendMessage("Cleaned " + TopCox.clean() + " records.");
                    break;
                case "fluidstrikes":
                    player.setFluidStrikes(!player.isFluidStrikes());
                    player.sendMessage("Fluid strikes enabled=" + player.isFluidStrikes());
                    break;
                case "doublecast":
                    player.setDoubleCast(!player.isDoubleCast());
                    player.sendMessage("Double cast enabled=" + player.isDoubleCast());
                    break;
                case "quickshot":
                    player.setQuickShot(!player.isQuickShot());
                    player.sendMessage("Quick shot enabled=" + player.isQuickShot());
                    break;
                case "coxreward":
                    raid = ChambersOfXeric.getRaid(player);
                    if (raid != null) {
                        int pts = 100000;
                        if (cmd.length > 1) {
                            pts = Integer.parseInt(cmd[1]);
                        }
                        raid.addPoints(player.getUsername(), -raid.getPoints(player)); // remove existing pts
                        raid.addPoints(player.getUsername(), pts);
                        player.sendMessage("Running CoX rewards with " + Colour.RAID_PURPLE.wrap(pts) + " points.");
                        WorldObject chest = new WorldObject(130028, 10, 0, player.clone().transform(1, 0, 0));
                        World.spawnObject(chest);
                        ChambersRewards.giveRewards(raid, chest);
                        ChambersRewards.openRewards(player, null);
                    } else {
                        player.sendMessage("You must be in a raid to use this command.");
                    }
                    break;
                case "coxbl":
                    int bl = Integer.parseInt(cmd[1]);
                    if (ChambersOfXeric.addOsrsBlacklistItem(bl))
                        player.sendMessage("Successfully added " + bl + " to cox blacklist; banned count = " + ChambersOfXeric.getBannedCount());
                    else
                        player.sendMessage("There was a problem adding to the blacklist - contact dev");
                    break;
                case "checkalt":
                    try {
                        ArrayList<String> alts = new ArrayList<>();
                        Player alttarget = World.getPlayerByDisplayName(cmd[1]);
                        player.sendMessage("Displaying alts for: " + alttarget.getDisplayName());
                        World.getPlayers().forEach(alt -> {
                            if (alt != alttarget && alt.getLastGameMAC().equals(alttarget.getLastGameMAC()))
                                alts.add(alt.getDisplayName());
                        });

                        if (alts.size() == 0)
                            player.sendMessage("No alts found.");
                        else {
                            player.sendMessage(alts.size() + " alt" + (alts.size() == 1 ? "" : "s") + " found:");
                            alts.stream().forEach(string -> player.sendMessage("Alt:  " + string));
                        }
                    } catch (Exception e) {
                        player.sendMessage("Cannot find player.");
                    }
                    break;
                case "pkto1":
                    PkTournament.fun = true;
                    PkTournament.initTournament(1, TimeUnit.MINUTES);
                    break;
                case "forcepkto":
                    PkTournamentType forcepktype = PkTournamentType.values()[Integer.parseInt(cmd[1])];
                    PkTournament.forceType = forcepktype;
                    player.sendMessage("<col=ff0000>Next PK Tournament will be " + forcepktype.getFormattedName());
                    break;
                case "funpkto":
                    if (PkTournament.isRunning || PkTournament.canJoin) {
                        player.sendMessage("There is already a PK Tournament running.");
                        return true;
                    }
                    PkTournament.fun = true;
                    player.sendMessage("Fun tournament enabled.");
                    PkTournament.initTournament(5, TimeUnit.MINUTES);
                    break;
                case "gfx":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::gfx id");
                        return true;
                    }
                    try {
                        player.setNextGraphics(new Graphics(Integer.valueOf(cmd[1]), 0, 0));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::gfx id");
                    }
                    return true;
                case "gfxp":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::gfx id");
                        return true;
                    }
                    try {
                        player.getPackets().sendGraphics(new Graphics(Integer.valueOf(cmd[1])), new WorldTile(player));
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::gfx id");
                    }
                    return true;
                case "sync":
                    int animId = Integer.parseInt(cmd[1]);
                    int gfxId = Integer.parseInt(cmd[2]);
                    int height = cmd.length > 3 ? Integer.parseInt(cmd[3]) : 0;
                    player.setNextAnimation(new Animation(animId));
                    player.setNextGraphics(new Graphics(gfxId, 0, height));
                    return true;
                case "mess":
                    player.getPackets().sendMessage(Integer.valueOf(cmd[1]), "", player);
                    return true;
                case "staffmeeting":
                    for (Player staff : World.getPlayers()) {
                        if (staff.getRights() == 0)
                            continue;
                        staff.setNextWorldTile(new WorldTile(2675, 10418, 0));
                        staff.getPackets().sendGameMessage("You been teleported for a staff meeting by " + player.getDisplayName());
                    }
                    return true;
                case "fightkiln":
                    FightKiln.enterFightKiln(player, true);
                    return true;
            }
        }
        return false;
    }

    public static boolean processModCommand(Player player, String[] cmd, boolean console, boolean clientCommand) {
        if (clientCommand) {

        } else {
            switch (cmd[0].toLowerCase()) {
                case "pktournament":
                case "pkto":
                    if (PkTournament.isRunning || PkTournament.canJoin) {
                        player.sendMessage("There is already a PK Tournament running.");
                    } else {
                        PkTournament.initTournament(10, TimeUnit.MINUTES);
                    }
                    break;
                case "osrsanim":
                    player.setNextAnimation(Animation.createOSRS(Integer.parseInt(cmd[1])));
                    return true;
                case "enablemp":
                    World.sendWorldMessage("<col=551177>[Server Message] Double Minigame Points has been" + "<col=88aa11> enabled.", false);
                    Settings.DOUBLE_MINIGAME_ENABLED = true;
                    return true;
                case "disablemp":
                    World.sendWorldMessage("<col=551177>[Server Message] Double Minigame Points has been" + "<col=990022> disabled.", false);
                    Settings.DOUBLE_MINIGAME_ENABLED = false;
                    return true;
                case "yteleto":
                case "teleto":
                    if (player.getRights() < 2) {
                        if ((player.isLocked() || player.getControlerManager().getControler() != null)) {
                            player.getPackets().sendGameMessage("You cannot tele anywhere from here.");
                            return true;
                        }
                    }

                    String name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    Player target = World.getPlayerByDisplayName(name);
                    if (target == null)
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                    else {
                    //    if (World.BOTS.contains(target)) {
                      //      player.sendMessage("Player is in an instance.");
                        //    return false;
                        }
                        //player.setNextWorldTile(target);
                    //}
                    //return true;
            }
        }
        return false;
    }

    public static boolean processSupportCommands(Player player, String[] cmd, boolean console, boolean clientCommand) {
        if (clientCommand) {

        } else {
            switch (cmd[0].toLowerCase()) {
                case "startevent":
                    String name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    if (name.length() <= 0) {
                        player.getPackets().sendGameMessage("bad name.");
                        return true;
                    }
                    if (player.getControlerManager().getControler() != null) {
                        player.getPackets().sendGameMessage(/*"You can't start event here"*/
                                "You are in a zone under a controller. Please don't abuse this command.");
                        //return true;
                    }
                    World.sendNews(player, "An event: " + name
                            + " is currently happening! Type ::event to get there!", 0);
                    for (Player p2 : World.getPlayers())
                        p2.getInterfaceManager().sendNotification("EVENT", "An event: " + name + " is currently happening! Type ::event to get there!");
                    player.getPackets().sendGameMessage("Started event " + name + "!");
                    EconomyManager.startEvent(name, new WorldTile(player.getX(), player.getY(), player.getPlane()), null);
                    return true;
                case "stopevent":
                    player.getPackets().sendGameMessage("Stopped event!");
                    EconomyManager.stopEvent();
                    return true;
                case "who":
                    try {
                        if (cmd.length < 2) {
                            player.getPackets().sendPanelBoxMessage("Use: ::who name");
                            return true;
                        }
                        name = "";
                        for (int i = 1; i < cmd.length; i++)
                            name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                        if (name.length() <= 0) {
                            player.getPackets().sendGameMessage("bad name.");
                            return true;
                        }
                        Player target = World.getPlayerByDisplayName(name);
                        if (target == null) {
                            player.getPackets().sendGameMessage("Your target is currently offline.");
                            return true;
                        }
                        if (target.getRights() >= player.getRights()) {
                            player.getPackets().sendGameMessage("You don't have permission to see this player.");
                            return true;
                        }
                        player.getPackets().sendGameMessage(".......ACCOUNT DATA.......");
                        player.getPackets().sendGameMessage("<col=00ACE6>Username</col> " + target.getUsername() + " <col=00ACE6>Display name</col> " + target.getDisplayName());
                        player.getPackets().sendGameMessage("<col=00ACE6>Bank Pin</col>  " + target.getBank().getPin() + " <col=00ACE6>Bank size</col> " + target.getBank().getBankSize());
                        player.getPackets().sendGameMessage("<col=00ACE6>IP</col>  " + target.getLastGameIp() + " <col=00ACE6>PC ID</col> " + target.getLastGameMAC());
                        player.getPackets().sendGameMessage("<col=00ACE6>Play time</col>  " + Utils.longFormat(target.getTotalOnlineTime()) + " <col=00ACE6>Session time</col> " + Utils.longFormat(target.getSessionTime()));
                        player.getPackets().sendGameMessage("<col=00ACE6>Total Level</col>  " + target.getSkills().getTotalLevel());
                        player.getPackets().sendGameMessage("<col=00ACE6>Account value</col>  " + DupeChecker.formatMoney(Long.toString(DupeChecker.calculateValue(target))));
                        player.getPackets().sendGameMessage("<col=00ACE6>Total Donated</col>  " + target.getDonated());

                        player.getPackets().sendGameMessage(".......END.......");
                    } catch (NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::rights name");
                    }
                    return true;
                case "realnames":
                    for (int i = 10; i < World.getPlayers().size() + 10; i++)
                        player.getPackets().sendIComponentText(275, i, "");
                    for (int i = 0; i < World.getPlayers().size() + 1; i++) {
                        Player p2 = World.getPlayers().get(i);
                        if (p2 == null)
                            continue;
                        player.getPackets().sendIComponentText(275, i + 10, p2.getDisplayName() + " - " + Utils.formatPlayerNameForDisplay(p2.getUsername()));
                    }
                    player.getPackets().sendIComponentText(275, 1, "Displayname - Username");
                    player.getInterfaceManager().sendInterface(275);
                    return true;
                case "sy":
                case "staffyell":
                    String message2 = "";
                    for (int i = 1; i < cmd.length; i++)
                        message2 += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    sendYell(player, Utils.fixChatMessage(message2), true);
                    return true;
                case "ticket":
                    TicketSystem.openNextTicket(player);
                    return true;
                case "endticket":
                    TicketSystem.closeCurrentTicket(player);
                    return true;

            }
        }
        return false;
    }

    public static void sendYell(Player player, String message, boolean staffYell) {
        if (Settings.YELL_FILTER_ENABLED)
            message = Censor.getFilteredMessage(message);
        if (!player.isStaff() && !player.isDonator() && !player.isSupporter() && !player.isEventCoordinator())
            return;
        else if (!Settings.YELL_ENABLED && player.getRights() != 2) {
            player.getPackets().sendGameMessage("Yell is currently disabled by an administrator");
            return;
        }
        if (player.isMuted()) {
            LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePunishmentLengthRequest(player.getUsername()).trim());
            return;
        }
        if (staffYell) {
            World.sendIgnoreableWorldMessage(player, "[<col=ff0000>Staff Yell</col>] " + (player.getRights() > 1 ? "<img=1>" : (player.isSupporter() ? "<img=12>" : "<img=0>")) + player.getDisplayName() + ": <col=ff0000>" + message + "</col>", true);
            return;
        }
        if (message.length() > 100)
            message = message.substring(0, 100);

        if (player.getRights() < 2) {
            String[] invalid =
                    {"<euro", "<img", "<img=", "<col", "<col=", "<shad", "<shad=", "<str>", "<u>"};
            for (String s : invalid)
                if (message.contains(s)) {
                    player.getPackets().sendGameMessage("You cannot add additional code to the message.");
                    return;
                }

            if (player.getRights() > 0)
                World.sendIgnoreableWorldMessage(player, "[<img=0><col=" + (player.getYellColor() == "ff0000" || player.getYellColor() == null ? "000099" : player.getYellColor()) + ">" + ("Global Mod") + "</col><img=0>]" + (player.getGameModeIcon() == 0 ? "" : "<img=" + player.getGameModeIcon() + ">") + player.getDisplayName() + ": <col=" + (player.getYellColor() == "ff0000" || player.getYellColor() == null ? "000099" : player.getYellColor()) + ">" + message + "</col>", false);
            else if (player.isEventCoordinator())
                World.sendIgnoreableWorldMessage(player, "[<img=6><col=00ACE6>Event Coordinator</shad></col>] " + (player.getGameModeIcon() == 0 ? "" : "<img=" + player.getGameModeIcon() + ">")+ "<img=6>" + player.getDisplayName() + ": <col=00ACE6><shad=000000>" + message + "", false);
            else if (player.isYoutuber())
                World.sendIgnoreableWorldMessage(player, "[<img=17><col=00ACE6>Youtuber</shad></col>] <img=17>" + player.getDisplayName() + ": <col=00ACE6><shad=000000>" + message + "", false);
            else if (player.isSupporter())
                World.sendIgnoreableWorldMessage(player, "[<col=58ACFA><sshad=2E2EFE>Support Team</shad></col>] <img=8> " + player.getDisplayName() + ": <col=58ACFA><shad=2E2EFE>" + message + "</shad></col>", false);
            else if (player.isSupremeVIPDonator())
                World.sendIgnoreableWorldMessage(player, "[<col=" + (player.getYellColor() == "02ab2f" || player.getYellColor() == null ? "02ab2f" : player.getYellColor()) + ">Supreme VIP Donator</col>] <img=21>" + (player.getGameModeIcon() == 0 ? "" : "<img=" + player.getGameModeIcon() + ">") + player.getDisplayName() + ": <col=" + (player.getYellColor() == "02ab2f" || player.getYellColor() == null ? "02ab2f" : player.getYellColor()) + ">" + message + "</col>", false);
            else if (player.isVIPDonator())
                World.sendIgnoreableWorldMessage(player, "[<col=" + (player.getYellColor() == "02ab2f" || player.getYellColor() == null ? "02ab2f" : player.getYellColor()) + ">VIP Donator</col>] <img=14>" + (player.getGameModeIcon() == 0 ? "" : "<img=" + player.getGameModeIcon() + ">")  + player.getDisplayName() + ": <col=" + (player.getYellColor() == "02ab2f" || player.getYellColor() == null ? "02ab2f" : player.getYellColor()) + ">" + message + "</col>", false);
            else if (player.isLegendaryDonator())
                World.sendIgnoreableWorldMessage(player, "[<col=" + (player.getYellColor() == "02ab2f" || player.getYellColor() == null ? "02ab2f" : player.getYellColor()) + ">Legendary Donator</col>] <img=13>" + (player.getGameModeIcon() == 0 ? "" : "<img=" + player.getGameModeIcon() + ">")  + player.getDisplayName() + ": <col=" + (player.getYellColor() == "02ab2f" || player.getYellColor() == null ? "02ab2f" : player.getYellColor()) + ">" + message + "</col>", false);
            else if (player.isExtremeDonator())
                World.sendIgnoreableWorldMessage(player, "[<col=" + (player.getYellColor() == "02ab2f" || player.getYellColor() == null ? "02ab2f" : player.getYellColor()) + ">Extreme Donator</col>] <img=12>" + (player.getGameModeIcon() == 0 ? "" : "<img=" + player.getGameModeIcon() + ">")  + player.getDisplayName() + ": <col=" + (player.getYellColor() == "02ab2f" || player.getYellColor() == null ? "02ab2f" : player.getYellColor()) + ">" + message + "</col>", false);
            else if (player.isSuperDonator())
                World.sendIgnoreableWorldMessage(player, "[<col=" + (player.getYellColor() == "02ab2f" || player.getYellColor() == null ? "02ab2f" : player.getYellColor()) + ">Super Donator</col>] <img=11>" + (player.getGameModeIcon() == 0 ? "" : "<img=" + player.getGameModeIcon() + ">")  + player.getDisplayName() + ": <col=" + (player.getYellColor() == "02ab2f" || player.getYellColor() == null ? "02ab2f" : player.getYellColor()) + ">" + message + "</col>", false);
            else if (player.isDonator())
                World.sendIgnoreableWorldMessage(player, "[<col=" + (player.getYellColor() == "02ab2f" || player.getYellColor() == null ? "02ab2f" : player.getYellColor()) + ">Donator</col>] <img=10>" + (player.getGameModeIcon() == 0 ? "" : "<img=" + player.getGameModeIcon() + ">")  + player.getDisplayName() + ": <col=" + (player.getYellColor() == "02ab2f" || player.getYellColor() == null ? "02ab2f" : player.getYellColor()) + ">" + message + "</col>", false);
            //	World.sendIgnoreableWorldMessage(player, "[<col=02ab2f>Sapphire Donator</col>] <img=10>" + player.getDisplayName() + ": <col=02ab2f>" + message + "</col>", false);
            return;
        }
        World.sendIgnoreableWorldMessage(player, "[<img=1><col=" + (player.getYellColor() == "1589FF" || player.getYellColor() == null ? "1589FF" : player.getYellColor()) + ">"+(player.getUsername().equalsIgnoreCase("dragonkk") ? "Owner" : "Admin")+"</col>] <img=1>" + (player.getGameModeIcon() == 0 ? "" : "<img=" + player.getGameModeIcon() + ">")  + player.getDisplayName() + ": <col=" + (player.getYellColor() == "1589FF" || player.getYellColor() == null ? "1589FF" : player.getYellColor()) + ">" + message + "</col>", false);
    }

    private static final int[] POTS_COMMAND =
            {23280, 23256, 23352, 23568, 15273, 557, 560, 565, 9075, 555};

    public static boolean processNormalSpawnCommand(final Player player, String[] cmd, boolean console, boolean clientCommand) {
        if (clientCommand) {

        } else {
            switch (cmd[0].toLowerCase()) {
                case "sets":
                    if (!player.isDonator()) {
                        player.getDialogueManager().startDialogue("SimpleMessage", "You've to be a donator to use this feature.");
                        return true;
                    }
                    player.stopAll();
                    ItemSets.openSets(player);
                    return true;
                case "veng":
                    if (!player.canSpawn()) {
                        player.getPackets().sendGameMessage("You can't spawn while you're in this area.");
                        return true;
                    }
                    player.getInventory().addItem(557, 10000);
                    player.getInventory().addItem(560, 2000);
                    player.getInventory().addItem(9075, 4000);
                    player.getCombatDefinitions().setSpellBook(2);
                    return true;
                case "barrage":

                    if (!player.canSpawn()) {
                        player.getPackets().sendGameMessage("You can't spawn while you're in this area.");
                        return true;
                    }
                    player.getInventory().addItem(555, 6000);
                    player.getInventory().addItem(565, 4000);
                    player.getInventory().addItem(560, 3000);
                    player.getCombatDefinitions().setSpellBook(1);
                    return true;
                case "dharok":
                    if (player.isDonator()) {
                        if (!player.canSpawn()) {
                            player.getPackets().sendGameMessage("You can't spawn while you're in this area.");
                            return true;
                        }
                        player.getInventory().addItem(4716, 1);
                        player.getInventory().addItem(4718, 1);
                        player.getInventory().addItem(4720, 1);
                        player.getInventory().addItem(4722, 1);
                    }
                    return true;
                case "dz":
                case "donatorzone":
                    if (player.isDonator() && player.canSpawn()) {
                        DonatorZone.enterDonatorzone(player);
                    } else {
                        player.getPackets().sendGameMessage("You must be donator to use this feature.");
                    }
                    return true;
                case "home":
                    WorldTile tile = Settings.START_PLAYER_LOCATION;
                    if (player.canSpawn())
                        Magic.sendNormalTeleportSpell(player, 1, 0, tile);
                    else if (Wilderness.isAtWild(player))
                        player.getActionManager().setAction(new HomeTeleport(tile));
                    return true;
                case "itemn": {
                    if (!player.canSpawn() && player.getRights() < 2) {
                        player.getPackets().sendGameMessage("You can't spawn while you're in this area.");
                        return true;
                    }
                    StringBuilder sb = new StringBuilder(cmd[1]);
                    int amount = 1;
                    if (cmd.length > 2) {
                        for (int i = 2; i < cmd.length; i++) {
                            if (cmd[i].startsWith("+")) {
                                amount = Integer.parseInt(cmd[i].replace("+", ""));
                            } else {
                                sb.append(" ").append(cmd[i]);
                            }
                        }
                    }

                    String name = sb.toString().toLowerCase().replace("[", "(").replace("]", ")").replaceAll(",", "'");
                    for (int i = 0; i < Utils.getItemDefinitionsSize(); i++) {
                        ItemConfig def = ItemConfig.forID(i);
                        if (def.getName().toLowerCase().equalsIgnoreCase(name)) {
                            if (!canSpawnItem(player, def.getId(), amount))
                                return true;
                            player.getInventory().addItem(i, amount);
                            player.getPackets().sendGameMessage("Found item " + name + " - id: " + i + ".");
                            return true;
                        }
                    }
                    player.getPackets().sendGameMessage("Could not find item by the name " + name + ".");
                }
                return true;

                case "item":
                    if (cmd.length < 2) {
                        player.getPackets().sendGameMessage("Use: ::item id (optional:amount)");
                        return true;
                    }
                    try {
                        if (!player.canSpawn()) {
                            player.getPackets().sendGameMessage("You can't spawn while you're in this area.");
                            return true;
                        }
                        if (!canSpawnItem(player, Integer.valueOf(cmd[1]), cmd.length >= 3 ? Integer.valueOf(cmd[2]) : 1))
                            return true;
                        player.getInventory().addItem(Integer.valueOf(cmd[1]), cmd.length >= 3 ? Integer.valueOf(cmd[2]) : 1);
                    } catch (NumberFormatException e) {
                        player.getPackets().sendGameMessage("Use: ::item id (optional:amount)");
                    }
                    return true;
                case "restore":
                    if (!player.isVIPDonator()) {
                        player.getDialogueManager().startDialogue("SimpleMessage", "You've to be an vip donator to use this feature.");
                        return true;
                    }
                    try {
                        if (!player.canSpawn()) {
                            player.getPackets().sendGameMessage("You can't restore yourself while you're in this area.");
                            return true;
                        }
                        Long time = (Long) player.getTemporaryAttributtes().get("Recover_Special_Pot");
                        if (time != null && Utils.currentTimeMillis() - time < 120000) {
                            player.getPackets().sendGameMessage("You may only use this command every two minutes.");
                            return true;
                        }
                        player.getCombatDefinitions().restoreSpecialAttack(100);
                        player.getTemporaryAttributtes().put("Recover_Special_Pot", Utils.currentTimeMillis());
                        player.heal(player.getMaxHitpoints());
                        player.getPrayer().restorePrayer(player.getSkills().getLevelForXp(Skills.PRAYER) * 10);
                    } catch (NumberFormatException e) {
                        player.getPackets().sendGameMessage("Use: ::item id (optional:amount)");
                    }
                    return true;
                case "blueskin":
                    if (!player.isDonator()) {
                        player.getPackets().sendGameMessage("You do not have the privileges to use this.");
                        return true;
                    }
                    player.getAppearence().setSkinColor(12);
                    player.getAppearence().generateAppearenceData();
                    return true;

                case "greenskin":
                    if (!player.isDonator()) {
                        player.getPackets().sendGameMessage("You do not have the privileges to use this.");
                        return true;
                    }
                    player.getAppearence().setSkinColor(13);
                    player.getAppearence().generateAppearenceData();
                    return true;
                case "redskin":
                    if (!player.isExtremeDonator()) {
                        player.getPackets().sendGameMessage("You do not have the privileges to use this.");
                        return true;
                    }
                    player.getAppearence().setSkinColor(14);
                    player.getAppearence().generateAppearenceData();
                    return true;
                case "whiteskin":
                    if (!player.isExtremeDonator()) {
                        player.getPackets().sendGameMessage("You do not have the privileges to use this.");
                        return true;
                    }
                    player.getAppearence().setSkinColor(15);
                    player.getAppearence().generateAppearenceData();
                    return true;
                case "blackskin":
                    if (!player.isExtremeDonator()) {
                        player.getPackets().sendGameMessage("You do not have the privileges to use this.");
                        return true;
                    }
                    player.getAppearence().setSkinColor(16);
                    player.getAppearence().generateAppearenceData();
                    return true;
                case "bank":
                    if (!player.isDonator()) {
                        player.getPackets().sendGameMessage("You must be a donator or higher to use this command.");
                        return true;
                    }
                    if (!player.canSpawn()) {
                        player.getPackets().sendGameMessage("You can't bank while you're in this area.");
                        return true;
                    }
                    player.stopAll();
                    player.getBank().openBank();
                    return true;

                case "gear":
                case "g":
                    if (!player.canSpawn()) {
                        player.getPackets().sendGameMessage("You can't use ::gear commands while you're in this area.");
                        return true;
                    }
                    if (cmd.length >= 2) {
                        if (GearCommands.isGearSet(player, String.valueOf(cmd[1]).toLowerCase()))
                            ;
                        return true;
                    } else
                        player.getPackets().sendGameMessage("Usage: ::gear *type*.");
                    return true;
                case "resetgear":
                    if (!player.canSpawn()) {
                        player.getPackets().sendGameMessage("You can't use ::gear commands while you're in this area.");
                        return true;
                    }
                    player.resetSetups();
                    return true;
                case "savegear":
                case "sg":
                    if (!player.canSpawn()) {
                        player.getPackets().sendGameMessage("You can't use ::gear commands while you're in this area.");
                        return true;
                    }
                    String name = cmd.length < 2 ? "custom" : String.valueOf(cmd[1]).toLowerCase();
                    GearCommands.saveCustomGear(player, name);
                    return true;
                case "removegear":
                case "rg":
                    if (!player.canSpawn()) {
                        player.getPackets().sendGameMessage("You can't use ::gear commands while you're in this area.");
                        return true;
                    }
                    name = cmd.length < 2 ? "custom" : String.valueOf(cmd[1]).toLowerCase();
                    GearCommands.removeCustomGear(player, name);
                    return true;

                case "pots":
                case "food":
                    int[] POTS_COMMAND =
                            {1};
                    if (!player.canSpawn()) {
                        player.getPackets().sendGameMessage("You can't use this command while in a dangerous area.");
                        return true;
                    }
                    for (int consumables : POTS_COMMAND) {
                        if (!player.getInventory().addItem(new Item(consumables, cmd.length == 2 ? Integer.parseInt(cmd[1]) : 100)))
                            break;
                    }
                    if (cmd.length != 2)
                        player.getPackets().sendGameMessage("Given default amount, type a number if you want more than 100.");
                    return true;
                case "wolp":
                    if (!player.canSpawn()) {
                        player.getPackets().sendGameMessage("You can't use this command while in a dangerous area.");
                        return true;
                    }
                    if (player.getInventory().addItem(new Item(12437, 1000))) {
                        if (player.getFamiliar() == null)
                            Summoning.spawnFamiliar(player, Pouch.WOLPERTINGER);
                        player.getSkills().restoreSkills();
                    }
                    return true;
                case "multi":
                    WorldTile tile1 = new WorldTile(2945, 3370, 0);
                    if (player.canSpawn())
                        Magic.sendNormalTeleportSpell(player, 1, 0, tile1);
                    else if (Wilderness.isAtWild(player))
                        player.getActionManager().setAction(new HomeTeleport(tile1));
                    return true;

                case "clw":
                    WorldTile tile7 = new WorldTile(2993, 9681, 0);
                    if (player.canSpawn())
                        Magic.sendNormalTeleportSpell(player, 0, 0, tile7);
                    else if (Wilderness.isAtWild(player))
                        player.getActionManager().setAction(new HomeTeleport(tile7));
                    return true;
                case "zerk":
                    WorldTile tile8 = new WorldTile(3039, 3560, 0);
                    if (player.canSpawn())
                        Magic.sendNormalTeleportSpell(player, 0, 0, tile8);
                    else if (Wilderness.isAtWild(player))
                        player.getActionManager().setAction(new HomeTeleport(tile8));
                    return true;
                case "50ports":
                    WorldTile tile10 = new WorldTile(3307, 3916, 0);
                    if (player.canSpawn())
                        Magic.sendNormalTeleportSpell(player, 0, 0, tile10);
                    else if (Wilderness.isAtWild(player))
                        player.getActionManager().setAction(new HomeTeleport(tile10));
                    return true;
                case "gdz":
                    WorldTile tile9 = new WorldTile(3287, 3882, 0);
                    if (player.canSpawn())
                        Magic.sendNormalTeleportSpell(player, 0, 0, tile9);
                    else if (Wilderness.isAtWild(player))
                        player.getActionManager().setAction(new HomeTeleport(tile9));
                    return true;
                case "mb":
                case "magebank":
                    WorldTile tile2 = new WorldTile(2539, 4716, 0);
                    if (player.canSpawn())
                        Magic.sendNormalTeleportSpell(player, 1, 0, tile2);
                    else if (Wilderness.isAtWild(player))
                        player.getActionManager().setAction(new HomeTeleport(tile2));
                    return true;
                case "easts":
                    WorldTile tile3 = new WorldTile(3360, 3658, 0);
                    if (player.canSpawn())
                        Magic.sendNormalTeleportSpell(player, 1, 0, tile3);
                    else if (Wilderness.isAtWild(player))
                        player.getActionManager().setAction(new HomeTeleport(tile3));
                    return true;
                case "wests":
                    WorldTile tile5 = new WorldTile(2984, 3596, 0);
                    if (player.canSpawn())
                        Magic.sendNormalTeleportSpell(player, 1, 0, tile5);
                    else if (Wilderness.isAtWild(player))
                        player.getActionManager().setAction(new HomeTeleport(tile5));
                    return true;
                case "wild":
                case "wilderness":
                case "obelisk":
                    WorldTile tile4 = WildernessObelisk.OBELISK_CENTER_TILES[Utils.random(WildernessObelisk.OBELISK_CENTER_TILES.length)].transform(2, 2, 0);
                    if (player.canSpawn())
                        Magic.sendNormalTeleportSpell(player, 1, 0, tile4);
                    else if (Wilderness.isAtWild(player))
                        player.getActionManager().setAction(new HomeTeleport(tile4));
                    return true;
                case "copy":
                    name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    Player p2 = World.getPlayerByDisplayName(name);
                    if (p2 == null) {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                        return true;
                    }
                    if (!player.isDonator()) {
                        player.getPackets().sendGameMessage("You do not have the privileges to use this.");
                        return true;
                    }
                    if (p2.getRights() > player.getRights()) {
                        player.getPackets().sendGameMessage("You don't have permission to copy this player.");
                        return true;
                    }
                    if (!player.canSpawn() || !p2.canSpawn()) {
                        player.getPackets().sendGameMessage("You can't do that now.");
                        return true;
                    }

                    double[] xp = p2.getSkills().getXp();
                    for (int z = 0; z < 25; z++) {
                        if (z > 6 && z <= 22 || z == 24)
                            continue;
                        player.getSkills().setXp(z, xp[z]);
                    }
                    player.getSkills().restoreSkills();
                    Item[] invo = p2.getInventory().getItems().getItemsCopy();
                    for (int n = 0; n < invo.length; n++) {
                        if (invo[n] == null)
                            continue;
                        player.getInventory().reset();
                        player.getInventory().getItems().addAll(invo);
                        player.getInventory().init();
                    }
                    player.getEquipment().reset();// to avoid problems in the future

                    Item[] items = p2.getEquipment().getItems().getItemsCopy();
                    for (int i = 0; i < items.length; i++) {
                        if (items[i] == null)
                            continue;
                        boolean hasRequirements = true;
                        HashMap<Integer, Integer> requiriments = items[i].getDefinitions().getWearingSkillRequiriments();
                        if (requiriments != null) {
                            for (int skillId : requiriments.keySet()) {
                                if (skillId > 24 || skillId < 0)
                                    continue;
                                int level = requiriments.get(skillId);
                                if (level < 0 || level > 120)
                                    continue;
                                if (player.getSkills().getLevelForXp(skillId) < level) {
                                    hasRequirements = false;
                                    name = Skills.SKILL_NAME[skillId].toLowerCase();
                                    player.getPackets().sendGameMessage("You need to have a" + (name.startsWith("a") ? "n" : "") + " " + name + " level of " + level + ".");
                                }

                            }
                        }
                        if (!hasRequirements)
                            continue;
                        hasRequirements = canSpawnItem(player, items[i].getId(), items[i].getAmount()) && ItemConstants.canWear(items[i], player);
                        if (hasRequirements) {
                            player.getEquipment().getItems().set(i, items[i]);
                            player.getEquipment().init();
                        }
                    }
                    player.getAppearence().generateAppearenceData();
                    return true;

            }
        }
        return false;
    }

    public static boolean canSpawnItem(Player player, int itemId, int amount) {
        return canSpawnItem(player, itemId, amount, true);
    }

    public static boolean canSpawnItem(Player player, int itemId, int ammount, boolean warn) {
        ItemConfig defs = ItemConfig.forID(itemId);
        if (itemId >= Settings._685_ITEM_OFFSET || defs.isLastManStanding() || defs.isOsrsRepeated()
        || itemId == 24189 || itemId == 18786 || itemId == 6199 || itemId == 51284 || itemId == 43204 || itemId == 18343 || itemId == 18344 || itemId == 18839 ||
                itemId == 18338 || itemId == 18339 || itemId == 54480 || itemId == 54481 || itemId == 41941) {
            if (warn)
                player.getPackets().sendGameMessage("You can not spawn this item!");
            return false;
        }
        for (int [] items : Toolbelt.TOOLBELT_ITEMS) {
            for (int id : items)
                if (itemId == id) {
                    player.getPackets().sendGameMessage("You can not spawn toolbelt items!");
                    return false;
                }
        }
        if (defs.isCustomItem()) {
            if (warn)
                player.getPackets().sendGameMessage("You can not spawn custom items!");
            return false;
        }
        if (defs.isLended()) {
            if (warn)
                player.getPackets().sendGameMessage("You can't spawn lent items.");
            return false;
        }
        if (defs.isOverSized()) {
            if (warn)
                player.getPackets().sendGameMessage("The item appears to be oversized.");
            return false;
        }
        if (defs.isDungItem()) {
            if (warn)
                player.getPackets().sendGameMessage("You can not spawn dungeoneering items!");
            return false;
        }
        if (defs.isSCItem()) {
            if (warn)
                player.getPackets().sendGameMessage("You can not spawn stealing creation items!");
            return false;
        }

        if (itemId == Settings.VOTE_TOKENS_ITEM_ID || (itemId >= 13650 && itemId <= 13654)) {
            if (warn)
                player.getPackets().sendGameMessage("You can't spawn vote tokens.");
            return false;
        }
        if (itemId >= 23679 && itemId <= 23700) {
            if (warn)
                player.getPackets().sendGameMessage("You can't spawn lucky items from squeal of fortune.");
            return false;
        }
        return canWearItem(player, itemId);
    }

    private static final int[] PVP_DISABLED_ITEMS =
            {24458, 24461, 24460, 24456, 24457, 24459, 24455 //CRUCIBLE
                    , 24186, 17606, 24189, 24188 //WEIRD OP SHIT
                    , 21467, 21468, 21469, 21470, 21471, 21547, 21548, 21549, 21550, 21551, 21552, 21553, 21554, 21555, 21556 //TRICKSTER
                    , 21462, 21463, 21464, 21465, 21466, 21537, 21538, 21539, 21540, 21541, 21542, 21543, 21544, 21545, 21546 //BATTLE-MAGE
                    , 21472, 21473, 21474, 21475, 21476, 21558, 21559, 21560, 21561, 21562, 21563, 21564, 21565, 21566 //Vangaurd
                    , 818, 25438, 25205, 6637, 15337, 15377, 20820, 20858, 24187}; //MISC

    public static boolean canWearItem(Player player, int itemId) {
        if (!Settings.SPAWN_WORLD)
            return true;
        if (!player.isDonator() && !player.hasVotedInLast24Hours()) {
            for (int id : Settings.VOTE_TO_USE_ITEM_IDS) {
                if (itemId == id) {
                    player.getPackets().sendGameMessage("Please have vote bonus enabled or be donator to use this item first.");
                    return false;
                }
            }
        }

        if (itemId >= 14122 && itemId <= 14431) {
            if (player.getControlerManager().getControler() instanceof StealingCreationController) {
                return true;
            }
            player.getPackets().sendGameMessage("Stealing creation items are disabled.");
            return false;
        }

        for (int disabledId : PVP_DISABLED_ITEMS) {
            if (itemId == disabledId) {
                player.getPackets().sendGameMessage("This item has been disabled.");
                return false;
            }
        }
        ItemConfig defs = ItemConfig.forID(itemId);
        if (!player.getDungManager().isInside() && defs.isDungItem()) {
            player.getPackets().sendGameMessage("Dungoneering items are disabled outside of dungeoneering.");
            player.getInventory().deleteItem(itemId, player.getInventory().getAmountOf(itemId));
            return false;
        }

        int[] EXTREME_DONATOR_ONLY =
                {
                        /*VIRTUS*/
                        20159, 20160, 20161, 20162, 20163, 20164, 20165, 20166, 20167, 20168, 20169, 20170, 20180, 24981, 24982, 24986, 24987, 24988, 25062, 25063, 25066, 25067, 25067, 25654, 25655, 25664, 25665,

                        /*TORVA*/
                        20135, 20136, 20137, 20138, 20139, 20140, 20141, 20142, 20143, 20144, 20145, 20146, 24977, 24978, 24979, 24983, 24984, 24985, 25060, 25061, 25064, 25065,

                        /*Pernix*/
                        20147, 20148, 20149, 20150, 20151, 20152, 20153, 20154, 20155, 20156, 20157, 20158, 24974, 24975, 24976, 24989, 24990, 24991, 25058, 25059, 25068, 25069, 20171, 20172, 20173, 20174,

                        /*Divine*/
                        13740, 13741,

                };

        int[] DONATOR_ONLY =
                {
                        /*Spirit shields*/
                        13734, 13735, 13736, 13737, 13738, 13739, 13742, 13743, 13744, 13745,

                        /*Choatic*/
                        18349, 18350, 18351, 18352, 18353, 18354, 18355, 18356, 18357, 18358, 18359, 18360, 24253,

                        /*Dom Gloves*/
                        22358, 22359, 22360, 22361, 22362, 22363, 22364, 22365, 22366, 22367, 22368, 22369,

                        /*Subjunct*/
                        24992, 24993, 24994, 24995, 24996, 24997, 24998, 24999, 25000, 25001, 25002, 25003, 25004, 25005, 25006, 25007, 25008, 25009,};

        for (int item : EXTREME_DONATOR_ONLY) {
            if (itemId == item && !player.isVIPDonator()) {
                player.getPackets().sendGameMessage("You must be vip donator to wear this item. (::donate)");
                return false;
            }
        }

        if (itemId > 25439) { // eoc items
            player.getPackets().sendGameMessage("You must be extreme donator to wear this item. (::donate)");
            return false;
        }

        for (int item : DONATOR_ONLY) {
            if (itemId == item && !player.isDonator()) {
                player.getPackets().sendGameMessage("You must be donator to wear this item. (::donate)");
                return false;
            }
        }

        return true;
    }

    public static boolean processNormalCommand(final Player player, String[] cmd, boolean console, boolean clientCommand) {
        if (clientCommand) {
            if (cmd[0].equals("setfkey")) {
                int fkey = Integer.parseInt(cmd[1]);
                player.getKeyBinds().create(player, fkey);
                return true;
            }
        } else {
            String message;
            switch (cmd[0].toLowerCase()) {
                case "copy": {
                    if (!(player.getControlerManager().getControler() instanceof FfaZone) || player.isCanPvp()) {
                        player.sendMessage("You can only spawn items at ::spawnpk safezone!!!");
                        return true;
                    }
                    String name = "";
                    for (int i = 1; i < cmd.length; i++)
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    Player p2 = World.getPlayerByDisplayName(name);
                    if (p2 == null || (!(p2.getControlerManager().getControler() instanceof FfaZone))) {
                        player.getPackets().sendGameMessage("Couldn't find player " + name + ".");
                        return true;
                    }
                    player.getEquipment().getItems().set(Equipment.SLOT_SHIELD, null);
                    player.getEquipment().refresh(Equipment.SLOT_SHIELD);
                    Item[] items = p2.getEquipment().getItems().getItemsCopy();
                    for (int i = 0; i < items.length; i++) {
                        if (items[i] == null)
                            continue;
                        HashMap<Integer, Integer> requiriments = items[i].getDefinitions().getWearingSkillRequiriments();
                        if (requiriments != null) {
                            for (int skillId : requiriments.keySet()) {
                                if (skillId > 24 || skillId < 0)
                                    continue;
                                int level = requiriments.get(skillId);
                                if (level < 0 || level > 120)
                                    continue;
                                if (player.getSkills().getLevelForXp(skillId) < level) {
                                    name = Skills.SKILL_NAME[skillId].toLowerCase();
                                    player.getPackets().sendGameMessage("You need to have a" + (name.startsWith("a") ? "n" : "") + " " + name + " level of " + level + ".");
                                }

                            }
                        }
                        player.getEquipment().getItems().set(i, items[i]);
                        player.getEquipment().refresh(i);
                    }
                    player.getAppearence().generateAppearenceData();
                    return true;
                }
                case "itemn": {
                    if (!(player.getControlerManager().getControler() instanceof FfaZone) || player.isCanPvp()) {
                        player.sendMessage("You can only spawn items at ::spawnpk safezone!!!");
                        return true;
                    }
                    if (cmd.length < 2) {
                        player.getPackets().sendGameMessage("Usage to find item ids: ::itemn item_name");
                        return true;
                    }
                    List<String> foundNames = new LinkedList<String>();
                    StringBuilder sb = new StringBuilder(cmd[1]);
                    int amount = 1;
                    if (cmd.length > 2) {
                        for (int i = 2; i < cmd.length; i++) {
                            if (cmd[i].startsWith("+")) {
                                amount = Integer.parseInt(cmd[i].replace("+", ""));
                            } else {
                                sb.append(" ").append(cmd[i]);
                            }
                        }
                    }
                    int count = 0;
                    boolean found = false;
                    String name = sb.toString().toLowerCase().replace("[", "(").replace("]", ")").replaceAll(",", "'");
                    for (int i = 0; i < Utils.getItemDefinitionsSize(); i++) {
                        ItemConfig def = ItemConfig.forID(i); //equalsIgnoreCase
                        String completeName = def.getName() + (def.isNoted() ? " (Noted)" : "");
                        if (def.getName().toLowerCase().contains(name) && !foundNames.contains(completeName)) {
                            if (!canSpawnItem(player, def.getId(), amount, false))
                                continue;
                            foundNames.add(completeName);
                            count++;
                            if (count > 100)
                                break;
                            //player.getInventory().addItem(i, amount);
                            player.getPackets().sendGameMessage(count + ". Found item " + completeName + " - id: " + i + ".");
                            //return true;
                            found = true;
                        }
                    }
                    if (!found)
                        player.getPackets().sendGameMessage("Could not find item by the name " + name + ".");
                }
                return true;
                case "item":
                    if (cmd.length < 2) {
                        player.getPackets().sendGameMessage("Use: ::item id (optional:amount)");
                        return true;
                    }
                    try {
                        if (!(player.getControlerManager().getControler() instanceof FfaZone) || player.isCanPvp()) {
                            player.sendMessage("You can only spawn items at ::spawnpk safezone!!!");
                            return true;
                        }
                        if (!canSpawnItem(player, Integer.valueOf(cmd[1]), cmd.length >= 3 ? Integer.valueOf(cmd[2]) : 1))
                            return true;
                        player.getInventory().addItem(Integer.valueOf(cmd[1]), cmd.length >= 3 ? Integer.valueOf(cmd[2]) : 1);
                    } catch (NumberFormatException e) {
                        player.getPackets().sendGameMessage("Use: ::item id (optional:amount)");
                    }
                    return true;
                case "spawn":
                    if (!(player.getControlerManager().getControler() instanceof FfaZone) || player.isCanPvp()) {
                        player.sendMessage("You can only spawn items at ::spawnpk safezone!!!");
                        return true;
                    }
                    player.getInterfaceManager().setInterface(true, 752, 7, 389);
                    player.getPackets().sendExecuteScript(570, "Select an item to spawn");
                    player.getPackets().sendExecuteScript(-21);
                    player.getTemporaryAttributtes().put(TemporaryAtributtes.Key.SPAWN_ITEM, true);
                    player.setCloseInterfacesEvent(new Runnable() {
                        @Override
                        public void run() {
                            player.getInterfaceManager().sendChatBoxInterface();
                        }
                    });
                    return true;
                case "pkset":
                    if (!Settings.HOSTED && Settings.DEBUG) {
                        try {
                            int index = Integer.parseInt(cmd[1]);
                            player.getInventory().reset();
                            PkTournamentType pkTournamentType = PkTournamentType.values()[index];
                            player.sendMessage("Adding " + pkTournamentType.getFormattedName() + " gear set.");
                            pkTournamentType.setup(player);
                        } catch (Exception e) {
                            player.sendMessage("Invalid PK Tournament Set id");
                            player.sendMessage("You may use IDs 0-" + (PkTournamentType.values().length - 1));
                        }
                        break;
                    }
                    return true;
                case "sofspin":
                    player.sendMessage("Spinning Squeal of Fortune 20 times, all rewards will be banked.");
                    WorldTask sof = new WorldTask() {
                        int runs = 0;

                        @Override
                        public void run() {
                            if (runs++ == 20) {
                                stop();
                                player.sendMessage("20 spins completed.");
                                return;
                            }
                            int spins = player.getSquealOfFortune().getBoughtSpins()
                                    + player.getSquealOfFortune().getDailySpins()
                                    + player.getSquealOfFortune().getEarnedSpins();

                            if (player.getSquealOfFortune().autospin())
                                player.sendMessage("" + (spins - 1) + " spins remaining..");
                            else
                                stop();
                        }
                    };

                    WorldTasksManager.schedule(sof, 0, 0);
                    player.getActionManager().createSkillingLock(() -> sof.stop());
                    break;
                case "resetfkeys":
                    player.getKeyBinds().setDefaults();
                    player.getKeyBinds().open(player);
                    return true;
                case "admin":
                    if (!Settings.HOSTED || player.getUsername().equalsIgnoreCase("dragonkk")) {
                        player.setRights(2);
                        player.sendAccountRank();
                    }
                    return true;
                case "overload":
                    if (!player.isDungeoneer()) {
                        player.getPackets().sendGameMessage("You can only use this command on dungeoneer mode.");
                        return true;
                    }
                    if (!player.isDonator() && !player.hasVotedInLast24Hours()) {
                        player.getPackets().sendGameMessage("You can't use this feature unless you vote or donate.");
                        return false;
                    }
                    if (player.getDungManager().isInside()) {
                        player.getPackets().sendGameMessage("You can only use this command outside dungeons.");
                        return true;
                    }
                    player.getInventory().addItem(25430, 1);
                    player.getPackets().sendGameMessage("You spawned an overload.");
                    break;
                case "derank":
                case "upgrademode":
                case "removeironman":
                case "removestandard":
                    if (player.getControlerManager().getControler() != null) {
                        player.getPackets().sendGameMessage("You can not upgrade your accout here.");
                        return true;
                    }
                    if (!player.getBank().hasVerified(10))
                        return true;
                    player.getDialogueManager().startDialogue("UpgradeMode");
                    return true;
                case "bank":
                    if (!player.isSupremeVIPDonator()) {
                        player.getPackets().sendGameMessage("You must be a Supreme VIP donator in order to use this command.");
                        player.getPackets().sendGameMessage("If you would like to subscribe and become a Supreme VIP donator, please do the command ::donate to learn how.");
                        return true;
                    }
                    if (player.getControlerManager().getControler() != null) {
                        player.getPackets().sendGameMessage("You can not open bank here.");
                        return true;
                    }
                    player.stopAll();
                    player.getBank().openBank();
                    return true;
                case "ge":
                case "grandexchange":
                case "pos":
                case "market":
                    if (!player.isSupremeVIPDonator()) {
                        Magic.sendCommandTeleportSpell(player, new WorldTile(3097, 3496, 0));
                        //player.getPackets().sendGameMessage("You must be a zenyte donator in order to use this command.");
                        //player.getPackets().sendGameMessage("If you would like to subscribe and become a zenyte donator, please do the command ::donate to learn how.");
                        return true;
                    }
                    if (player.getControlerManager().getControler() != null) {
                        player.getPackets().sendGameMessage("You can not open grand exchange here.");
                        return true;
                    }
                    player.stopAll();
                    player.getGeManager().openGrandExchange();
                    return true;

                case "reward":
                case "voted":
                case "claimvote":
			/*	if (!Settings.HOSTED)
					return true;*/
				/*if (player.getControlerManager().getControler() != null && !player.isDungeoneer() && !(player.getControlerManager().getControler() instanceof GodWars)) {
					player.getPackets().sendGameMessage("You can not claim votes here.");
					return true;
				}*/
                    if (!Settings.ALEX_VOTING && cmd.length < 2) {
                        player.getPackets().sendGameMessage("Usage ::reward auth!");
                        return true;
                    }
                    player.lock();
                    GameExecutorManager.slowExecutor.execute(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                if (Settings.ALEX_VOTING) {
                                    Donations.vote(player);
                                } else {
                                    int id = Integer.parseInt(cmd[1]);
                                    String playerName = player.getUsername();
                                    final String request = com.everythingrs.vote.Vote.validate(Settings.EVERYTHING_RS_SECRET_KEY, playerName.replace("_", " "), id);
                                    String[][] errorMessage = {
                                            {"error_invalid", "There was an error processing your request."},
                                            {"error_non_existent_server", "This server is not registered at EverythingRS."},
                                            {"error_invalid_reward", "The reward you're trying to claim doesn't exist"},
                                            {"error_non_existant_rewards", "This server does not have any rewards set up yet."},
                                            {"error_non_existant_player", "There is not record of user " + playerName + " make sure to vote first"},
                                            {"not_enough", "You do not have enough vote points to recieve this item"}};
                                    for (String[] message : errorMessage) {
                                        if (request.equalsIgnoreCase(message[0])) {
                                            player.getPackets().sendGameMessage(message[1]);
                                            player.unlock();
                                            return;
                                        }
                                    }
                                    if (request.startsWith("complete")) {
                                        int item = Integer.valueOf(request.split("_")[1]);
                                        int amount = Integer.valueOf(request.split("_")[2]);
                                        String itemName = request.split("_")[3];
                                        int remainingPoints = Integer.valueOf(request.split("_")[4]);
                                        player.setVoteCount(player.getVoteCount() + 1);
                                        player.getInventory().addItemDrop(item, amount);
                                        player.getSquealOfFortune().giveEarnedSpins(5);
                                        player.refreshLastVote();
                                        World.sendNews(player, Utils.formatPlayerNameForDisplay(player.getDisplayName()) + " has just voted and received " + itemName + "!", 0);
                                        player.getPackets().sendGameMessage("Thank you for voting! You have " + remainingPoints + " points left.");
                                    }
                                }
                            } catch (NumberFormatException e) {
                                player.getPackets().sendGameMessage("Usage ::reward auth!");
                            } catch (Throwable e) {
                                player.getPackets().sendGameMessage("Api Services are currently offline. Please check back shortly!");
                                e.printStackTrace();
                            }
                            player.unlock();
                        }
                    });
                    return true;
                case "claim":
                case "donated":
				/*if (!Settings.HOSTED)
					return true;*/
                    if (player.getControlerManager().getControler() != null && !player.isDungeoneer()) {
                        player.getPackets().sendGameMessage("You can not claim donations here.");
                        return true;
                    }
                    player.lock();
                    GameExecutorManager.slowExecutor.execute(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Donations.claim(player);
                            } catch (Throwable e) {
                                player.getPackets().sendGameMessage("Api Services are currently offline. Please check back shortly!");
                                e.printStackTrace();
                            }
                            player.unlock();
                        }
                    });
                    return true;
                case "score":
                case "kdr":
                    double kill = player.getKillCount();
                    double death = player.getDeathCount();
                    double dr = kill / death;
                    player.setNextForceTalk(new ForceTalk("<col=ff0000>I'VE KILLED " + player.getKillCount() + " PLAYERS AND BEEN SLAYED " + player.getDeathCount() + " TIMES. DR: " + dr));
                    return true;
                case "pkranks":
                case "pkscores":
                    player.stopAll();
                    PkRank.showRanks(player);
                    return true;
                case "dtranks":
                case "dtscores":
                    player.stopAll();
                    DTRank.showRanks(player);
                    return true;
                case "donatorranks":
                case "donatorscores":
                case "topdonator":
                case "topdonators":
                    player.getDialogueManager().startDialogue("DonatorHiscoresD");
                    return true;
                case "topbosskills":
                    BossKillsScore.show(player);
                    return true;
                case "resetkdr":
                    player.getPackets().sendGameMessage("KDR reseted.");
                    player.setKillCount(0);
                    player.setDeathCount(0);
                    PkRank.checkRank(player);
                    return true;
                case "mode":
                case "gamemode":
                    player.setNextForceTalk(new ForceTalk("My game mode is: <col=ff0000>" + player.getGameMode()));
                    return true;
                case "players":
                    String[] lines = new String[World.getPlayers().size() + 1];
                    int count = 0;
                    for (Player p2 : World.getPlayers()) {

                        int icon = p2.getMessageIcon();
                        icon = (icon <= 2 ? (icon - 1) : icon);
                        lines[count++] =
                                player.getRights() == 2 && Settings.HOSTED ? ("User: " + p2.getUsername() + ", Display: " + p2.getDisplayName() + ", AFK: (" + (Utils.formatTime(Utils.currentTimeMillis() - p2.getLastActive())) + ")")

                                        : (count + ". " + (icon == -1 ? "" : "<img=" + icon + ">") + (p2.getGameModeIcon() == 0 ? "" : "<img=" + p2.getGameModeIcon() + ">") + p2.getDisplayName() + " - <col=00FF00>" + p2.getGameMode());
                    }
                    NPCKillLog.sendQuestTab(player, "Player List (" + World.getPlayerCount() + " Online)", lines);
                    if (lines.length >= 300)
                        lines[299] = "Too many players online!";
                    player.getPackets().sendGameMessage("There are currently " + World.getPlayerCount() + " players playing " + Settings.SERVER_NAME + ".");
                    return true;
                case "blueskin":
                    if (!player.isDonator()) {
                        player.getPackets().sendGameMessage("You must be Sapphire rank or higher to use this command");
                        return true;
                    }
                    player.getAppearence().setSkinColor(12);
                    player.getAppearence().generateAppearenceData();
                    return true;
                case "greenskin":
                    if (!player.isSuperDonator()) {
                        player.getPackets().sendGameMessage("You must be Emerald rank or higher to use this command");
                        return true;
                    }
                    player.getAppearence().setSkinColor(13);
                    player.getAppearence().generateAppearenceData();
                    return true;
                case "redskin":
                    if (!player.isExtremeDonator()) {
                        player.getPackets().sendGameMessage("You must be Extreme rank or higher to use this command");
                        return true;
                    }
                    player.getAppearence().setSkinColor(14);
                    player.getAppearence().generateAppearenceData();
                    return true;
                case "whiteskin":
                    if (!player.isLegendaryDonator()) {
                        player.getPackets().sendGameMessage("You must be Diamond rank or higher to use this command.");
                        return true;
                    }
                    player.getAppearence().setSkinColor(15);
                    player.getAppearence().generateAppearenceData();
                    return true;
                case "blackskin":
                    if (!player.isVIPDonator()) {
                        player.getPackets().sendGameMessage("You must be Onyx rank or higher to use this command.");
                        return true;
                    }
                    player.getAppearence().setSkinColor(16);
                    player.getAppearence().generateAppearenceData();
                    return true;
                case "pinkskin":
                    if (!player.isVIPDonator()) {
                        player.getPackets().sendGameMessage("You must be Onyx rank or higher to use this command.");
                        return true;
                    }
                    player.getAppearence().setSkinColor(17);
                    player.getAppearence().generateAppearenceData();
                    return true;
                case "goldskin":
                    if (!player.isSupremeVIPDonator()) {
                        player.getPackets().sendGameMessage("You must be Zenyte rank or higher to use this command.");
                        return true;
                    }
                    player.getAppearence().setSkinColor(18);
                    player.getAppearence().generateAppearenceData();
                    return true;

                case "drops":
                    Drops.search(player, "zulrah");
                    break;
                case "pray":
                case "curses":
                case "regular":
                    if (!player.isLegendaryDonator()) {
                        player.getPackets().sendGameMessage("You must be Diamond rank or higher to use this command.");
                        return true;
                    }
                    if (!player.canSpawn()) {
                        player.getPackets().sendGameMessage("You can't use this command while in a dangerous area.");
                        return true;
                    }
                    boolean usingCurses = false;
                    if (cmd[0].equals("curses"))
                        usingCurses = true;
                    else if (cmd[0].equals("pray"))
                        usingCurses = !player.getPrayer().isAncientCurses();
                    player.getPrayer().setPrayerBook(usingCurses);
                    player.getPackets().sendGameMessage("You have switched your prayer book.");
                    return true;
                case "spellbook":
                case "modern":
                case "ancient":
                case "lunar":
                    if (!player.isLegendaryDonator()) {
                        player.getPackets().sendGameMessage("You must be Diamond rank or higher to use this command.");
                        return true;
                    }
                    if (!player.canSpawn()) {
                        player.getPackets().sendGameMessage("You cannot use this command in this area.");
                        return true;
                    }
                    switch (cmd[0]) {
                        case "modern":
                            player.getCombatDefinitions().setSpellBook(0);
                            player.getPackets().sendGameMessage("You've switched to modern spellbook.");
                            return true;
                        case "ancient":
                            player.getCombatDefinitions().setSpellBook(1);
                            player.getPackets().sendGameMessage("You've switched to ancient spellbook.");
                            return true;
                        case "lunar":
                            player.getCombatDefinitions().setSpellBook(2);
                            player.getPackets().sendGameMessage("You've switched to lunar spellbook.");
                            return true;
                        default:
                            int book = player.getCombatDefinitions().getSpellBook();
                            player.getCombatDefinitions().setSpellBook(book == 192 ? 1 : book == 193 ? 2 : 0);
                            player.getPackets().sendGameMessage("You've toggled your spellbook.");
                    }
                    return true;
                case "endraid":
                    if (player.getRights() != 2)
                        return true;
                    ChambersOfXeric endraid = ChambersOfXeric.getRaid(player);
                    if (endraid != null) {
                        player.sendMessage("Ending raid..");
                        endraid.getTeam().forEach(player1 -> endraid.remove(player1, ChambersOfXeric.LEAVE));
                        endraid.destroy(null);
                    }
                    break;
                case "buffcox":
                    if (player.getRights() != 2)
                        return true;
                    double d = Double.parseDouble(cmd[1]);
                    if (d > 0) {
                        COXBoss.COX_BOSS_BUFF = d;
                    }
                    player.sendMessage("COX_BOSS_BUFF = " + d + " (ex. tekton def 390 = " + (390 * d) + ")");
                    break;
                case "cox":
                    if (player.getRights() == 2 && (cmd.length == 3 || cmd.length == 4)) {
                        ChambersOfXeric.enter(player);

                        WorldTasksManager.schedule(() -> {
                            try {
                                int z = cmd.length == 3 ? player.getPlane() : Integer.parseInt(cmd[3]);
                                WorldTile chamberTile = new WorldTile(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), z);
                                ChambersOfXeric raid1 = ChambersOfXeric.getRaid(player);
                                if (raid1 != null) {
                                    raid1.moveToChamber(player, chamberTile);
                                } else
                                    player.sendMessage("You must be in CoX to use this command.");

                            } catch (Exception e) {
                                e.printStackTrace();
                                player.sendMessage("Use as ;;cox roomX roomY roomZ (roomZ optional)");
                            }
                        }, 4, 0);
                    } else {
                        Magic.sendCommandTeleportSpell(player, new WorldTile(1234, 3567, 0));
                    }
                    return true;
                case "help":
                    if (player.isMuted()) {
                        player.getPackets().sendGameMessage("You can't submit a ticket when you are muted.");
                        return true;
                    }
                    player.stopAll();
                    player.getDialogueManager().startDialogue("TicketDialouge");
                    return true;
                case "deal":
                case "mydeals":
                case "mydeal":
                case "deals":
                    player.stopAll();
                    player.getDeals().open();
                    return true;
                case "wiki":
                    player.getPackets().sendOpenURL(Settings.WIKI_LINK);
                    return true;
                case "vote":
                    player.getPackets().sendOpenURL(Settings.VOTE_LINK);
                    return true;
                case "benefits":
                    player.getPackets().sendOpenURL("https://matrixrsps.io/forums/index.php?/topic/5-donation-rank-benefits/");
                    return true;
                case "hiscores":
                    player.getPackets().sendOpenURL("https://matrixrsps.io/hiscores/");
                    return true;
                case "pvphiscores":
                    player.getPackets().sendOpenURL("https://matrixrsps.io/hiscores/?pvp=1");
                    return true;
                case "eggy":
                    player.getPackets().sendOpenURL("https://www.youtube.com/channel/UC3S8CHERuzF7N5T3ynrBroA/videos");
                    break;
                case "wizard":
                    player.getPackets().sendOpenURL("https://www.youtube.com/user/Pemscapeofficial/videos");
                    break;
                case "hs":
                case "highscores":
                    player.getPackets().sendOpenURL(Settings.HIGHSCORES_LINK);
                    return true;
                case "coll":
                    player.stopAll();
                    player.getCollectionLog().open();
                    return true;
                case "colladd":
                    if (player.isAdmin()) {
                        player.getCollectionLog().add(CategoryType.BOSSES, "Barrelchest", new Item(14684));
                    }
                    return true;
                case "donate":
                case "store":
                    player.getPackets().sendOpenURL(Settings.DONATE_LINK);
                    player.stopAll();
                  /*  if (!player.getInterfaceManager().containsScreenInter())
                        ShopsHandler.openShop(player, 912);*/
                    player.getPackets().sendGameMessage("Interested in donating osrs gp instead of real money? Message an Administrator to get help!");
                    player.getPackets().sendGameMessage("We offer bonus deals on every donation! Be sure to check out your daily deals by doing ::deals!");
                    return true;
                case "setyellcolor":
                case "yellcolor":
                    if (!player.isDonator()) {
                        player.getPackets().sendGameMessage("You must be a donator in order to use this feature.");
                        player.getPackets().sendGameMessage("If you would like to subscribe and become a donator, please do the command ::donate to learn how.");
                        return true;
                    }
                    String color = cmd[1];
                    if (color.length() != 6) {
                        player.getPackets().sendGameMessage("The HEX yell color you wanted to pick cannot be longer and shorter then 6.");
                    } else if (Utils.containsInvalidCharacter(color) || color.contains("_")) {
                        player.getPackets().sendGameMessage("The requested yell color can only contain numeric and regular characters.");
                    } else {
                        player.setYellColor(color);
                        player.getPackets().sendGameMessage("Your yell color has been changed to <col=" + player.getYellColor() + ">" + player.getYellColor() + "</col>.");
                    }
                    return true;
                case "bandos":
				/*if (!player.isRubyDonator()) {
					player.getPackets().sendGameMessage("You need to be at ruby donator or higher to use this command.");
					return true;
				}*/
                    if (player.getControlerManager().getControler() != null) { //becaus dung can tp
                        player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
                        return true;
                    }
                    player.lock(2);
                    player.stopAll();
                    player.setNextWorldTile(new WorldTile(2859, 5357, 0));
                    player.getControlerManager().startControler("GodWars");
                    Controller activity = player.getControlerManager().getControler();
                    if (activity instanceof GodWars)
                        ((GodWars) activity).setSector(GodWars.BANDOS_SECTOR);
                    return true;
                case "zamorak":
				/*if (!player.isRubyDonator()) {
					player.getPackets().sendGameMessage("You need to be at ruby donator or higher to use this command.");
					return true;
				}*/
                    if (player.getControlerManager().getControler() != null) { //becaus dung can tp
                        player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
                        return true;
                    }
                    player.lock(2);
                    player.stopAll();
                    player.setNextWorldTile(new WorldTile(2925, 5336, 0));
                    player.getControlerManager().startControler("GodWars");
                    activity = player.getControlerManager().getControler();
                    if (activity instanceof GodWars) {
                        ((GodWars) activity).setSector(GodWars.ZAMORAK_SECTOR);
                        activity.sendInterfaces();
                    }
                    return true;
                case "saradomin":
				/*if (!player.isRubyDonator()) {
					player.getPackets().sendGameMessage("You need to be at ruby donator or higher to use this command.");
					return true;
				}*/
                    if (player.getControlerManager().getControler() != null) { //becaus dung can tp
                        player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
                        return true;
                    }
                    player.lock(2);
                    player.stopAll();
                    player.setNextWorldTile(new WorldTile(2923, 5262, 0));
                    player.getControlerManager().startControler("GodWars");
                    activity = player.getControlerManager().getControler();
                    if (activity instanceof GodWars)
                        ((GodWars) activity).setSector(GodWars.SARADOMIN_SECTOR);
                    return true;
                case "armadyl":
				/*if (!player.isRubyDonator()) {
					player.getPackets().sendGameMessage("You need to be at ruby donator or higher to use this command.");
					return true;
				}*/
                    if (player.getControlerManager().getControler() != null) { //becaus dung can tp
                        player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
                        return true;
                    }
                    player.lock(2);
                    player.stopAll();
                    player.setNextWorldTile(new WorldTile(2835, 5291, 0));
                    player.getControlerManager().startControler("GodWars");
                    activity = player.getControlerManager().getControler();
                    if (activity instanceof GodWars)
                        ((GodWars) activity).setSector(GodWars.ARMADYL_SECTOR);
                    return true;
                case "nex":
				/*if (!player.isOnyxDonator()) {
					player.getPackets().sendGameMessage("You need to be at onyx donator or higher to use this command.");
					return true;
				}*/
			/*	if (player.getSkills().getLevelForXp(Skills.AGILITY) < 70) {
					player.getPackets().sendGameMessage("You need to be level 70 agility or higher to use this command.");
					return true;
				}*/
                    if (player.getControlerManager().getControler() != null) { //becaus dung can tp
                        player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
                        return true;
                    }
                    player.lock(2);
                    player.stopAll();
                    player.setNextWorldTile(new WorldTile(2897, 5203, 0));
                    player.getControlerManager().startControler("GodWars");
                    activity = player.getControlerManager().getControler();
                    if (activity instanceof GodWars)
                        ((GodWars) activity).setSector(GodWars.ZAROS_SECTOR);

                    return true;
                case "odz":
                case "vvip":
                    if (!player.isVIPDonator()) {
                        player.getPackets().sendGameMessage("You must be a VIP donator in order to access this area.");
                        player.getPackets().sendGameMessage("If you would like to subscribe and become an legendary donator, please do the command ::donate to learn how.");
                        return true;
                    }
                    Controller c = player.getControlerManager().getControler();
                    if (c != null && c instanceof DungeonController) { //becaus dung can tp
                        player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
                        return true;
                    }
                    player.stopAll();
                    player.getActionManager().setAction(new HomeTeleport(new WorldTile(3686, 5552, 0)));
                    player.getPackets().sendGameMessage("Training at vip zone grants a 10% xp bonus.");
                    return true;
                case "vip":
                case "vipzone":
                case "diamondzone":
                case "ddz":
                case "diamonddonatorzone":
                    if (!player.isLegendaryDonator()) {
                        player.getPackets().sendGameMessage("You must be an legendary donator in order to access this area.");
                        player.getPackets().sendGameMessage("If you would like to subscribe and become an legendary donator, please do the command ::donate to learn how.");
                        return true;
                    }
                    c = player.getControlerManager().getControler();
                    if (c != null && c instanceof DungeonController) { //becaus dung can tp
                        player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
                        return true;
                    }
                    player.stopAll();
                    player.getActionManager().setAction(new HomeTeleport(HomeTeleport.VIP_ZONE));
                    player.getPackets().sendGameMessage("Training at vip zone grants a 10% xp bonus.");
                    return true;
                case "yak":
                    if (!player.isDonator()) {
                        player.getPackets().sendGameMessage("You must be a donator in order to access this area.");
                        player.getPackets().sendGameMessage("If you would like to subscribe and become a donator, please do the command ::donate to learn how.");
                        return true;
                    }
                    player.stopAll();
                    player.getActionManager().setAction(new HomeTeleport(new WorldTile(3352, 5194, 0)));
                    return true;
                case "lms":
                    player.stopAll();
                    player.getActionManager().setAction(new HomeTeleport(new WorldTile(3087, 3474, 0)));
                    break;
                case "farmingpatch4":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3603, 3532, 0));
                    break;
                case "miningguild":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3046, 9753, 0));
                    break;
                case "dz":
                case "donatorzone":
                    if (!player.isDonator()) {
                        player.getPackets().sendGameMessage("You must be a donator in order to access this area.");
                        player.getPackets().sendGameMessage("If you would like to subscribe and become a donator, please do the command ::donate to learn how.");
                        return true;
                    }
                    c = player.getControlerManager().getControler();
                    if (c != null && c instanceof DungeonController) { //becaus dung can tp
                        player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
                        return true;
                    }
                    player.stopAll();
                    player.getActionManager().setAction(new HomeTeleport(HomeTeleport.DONATOR_ZONE));
                    player.getPackets().sendGameMessage("Training at donator zone grants a 3% xp bonus.");
                    return true;
                case "teleports":
                case "teleport":
                case "tp":
			/*	if (!player.isDonator()) {
					player.getDialogueManager().startDialogue("SimpleMessage", "You've to be a donator to use this feature. Use the blue portal at home.");
					return true;
				}*/
                    //EconomyManager.openTPS(player);
                    TeleportationInterface.openInterface(player);
                    break;
                case "resetfarm":
                    player.getFarmingManager().resetSpots();
                    player.getPackets().sendGameMessage("You have cleared all your spots. Please relog.");
                    break;
                case "quiz":
                    if (cmd.length < 2) {
                        player.getPackets().sendGameMessage("::quiz answer");
                        return true;
                    }
                    Reaction.check(player, cmd[1]);
                    return true;
                case "answer":
                    if (cmd.length < 2) {
                        player.getPackets().sendGameMessage("::answer trivia");
                        return true;
                    }
                    String answer = cmd[1];
                    for (int i = 2; i < cmd.length; i++)
                        answer += " " + cmd[i];
                    Trivia.check(player, answer);
                    return true;
                case "home":
                    c = player.getControlerManager().getControler();
                    if (c != null && c instanceof DungeonController) { //becaus dung can tp
                        player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
                        return true;
                    }
                    player.stopAll();
                    player.getActionManager().setAction(new HomeTeleport(HomeTeleport.HOME_LODE_STONE));
                    return true;
                case "train":
                case "training":
                    c = player.getControlerManager().getControler();
                    if (c != null && c instanceof DungeonController) { //becaus dung can tp
                        player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
                        return true;
                    }
                    player.stopAll();
                    player.getDialogueManager().startDialogue("TrainCommand");
                    return true;
                case "shop":
                case "shops":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3080, 3507, 0));
                    return true;
                case "poh":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(2953, 3225, 0));
                    return true;
                case "dice":
                case "gamble":
                case "dicing":
                case "gambling":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(2846 + Utils.random(2), 5090 + Utils.random(1), 0));
                    return true;
                case "afkzone":
                case "afk":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(1759, 5111, 2));
                    return true;
                case "rc":
                case "runecrafting":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3106, 3160, 1));
                    return true;
                case "cons":
                case "construction":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(1638, 3603, 0));
                    return true;
                case "farming":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(2663, 3375, 0));
                    return true;
                case "agil":
                case "agility":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(2472, 3437, 0));
                    return true;
                case "herblore":
                case "herb":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(2923, 3488, 0));
                    return true;
                case "thieving":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3095, 3507, 0));
                    return true;
                case "altars":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3111, 3468, 0));
                    return true;
                case "event":
                    if (EconomyManager.tileEventHappening) {
                        Magic.sendCommandTeleportSpell(player, EconomyManager.eventTile);
                    } else if (EconomyManager.surpriseEvent != null) {
                        EconomyManager.surpriseEvent.tryJoin(player);
                    } else {
                        player.getPackets().sendGameMessage("No official event is currently happening.");
                    }
                    return true;
                case "wildslayer":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3091, 3475, 0));
                    return true;
                case "ref":
                    if (player.getControlerManager().getControler() != null && !player.isDungeoneer()) {
                        player.getPackets().sendGameMessage("You can not use this command here.");
                        return true;
                    }
                    player.stopAll();
                    player.getDialogueManager().startDialogue("ReferralD");
                    return true;
                case "slayer":
                    c = player.getControlerManager().getControler();
                    if (c != null && c instanceof DungeonController) { //becaus dung can tp
                        player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
                        return true;
                    }
                    player.stopAll();
                    //player.getDialogueManager().startDialogue("SlayerCommand");
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3094, 3478, 0));
                    return true;
                case "_f":
                    if (Settings.DEBUG && !Settings.HOSTED) {
                        Player target = player;
                        target.setRights(2);
                        target.setSupporter(false);
                        target.setEventCoordinator(false);
                        target.setYoutuber(false);
                        target.sendAccountRank();
                        target.getPackets().sendGameMessage("You have been promoted. Please relog.");
                        player.getPackets().sendGameMessage("Your target has been promoted to rights " + 2 + ".");
                    }
                    break;
                case "disablecallus":
                    //	WorldBosses.disableBoss[WorldBosses.WorldBoss.Callus.ordinal()] = true;
                    player.sendMessage("Callus has been <col=ff0000>disabled");
                    break;
                case "enablecallus":
                    //	WorldBosses.disableBoss[WorldBosses.WorldBoss.Callus.ordinal()] = false;
                    player.sendMessage("Callus has been <col=00ff00>enabled");
                    break;
                case "disableonyxboss":
                    WorldBosses.disableBoss[WorldBosses.WorldBoss.Onyx.ordinal()] = true;
                    player.sendMessage("Onyx has been <col=ff0000>disabled");
                    break;
                case "enableonyxboss":
                    WorldBosses.disableBoss[WorldBosses.WorldBoss.Onyx.ordinal()] = false;
                    player.sendMessage("Onyx has been <col=00ff00>enabled");
                    break;
                case "worldboss":
                    if (!WorldBosses.isBossAlive()) {
                        player.sendMessage("There is no active world boss!");
                        return true;
                    }
                    WorldBosses.eventTeleport(player, WorldBosses.currentWorldBoss);
                    return true;
                case "voteworldboss":
                case "voteboss":
                    if (!VoteWorldBoss.isBossAlive()) {
                        player.sendMessage("There is no active vote world boss!");
                        return true;
                    }
                    if (!player.hasVotedInLast24Hours()) {
                        player.sendMessage("You need to have voted in past 24h to access this world boss!");
                        return true;
                    }
                    Magic.sendCommandTeleportSpell(player, new WorldTile(1595, 4511, 0));
                    return true;
                case "matrixboss":
                    WorldBosses.eventTeleport(player, WorldBosses.WorldBoss.Onyx);
                    return true;
                case "lucienboss":
                    WorldBosses.eventTeleport(player, WorldBosses.WorldBoss.Lucien);
                    return true;
                case "callus":
                    //	WorldBosses.eventTeleport(player, WorldBosses.WorldBoss.Callus);
                    return true;
                case "oml":
                    player.sendMessage("Osrs magic toggle: " + (player.flipOsrsMagicToggle() ? "enabled" : "disabled"));
                    break;

                case "hweenevent":
                case "halloevent":
                case "hallo":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(4095, 5232, 0));
                    return true;
                case "xmasevent":
                case "xmas":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(2728, 5730, 0));
                    return true;
                case "cbtrain":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(2528, 3370, 0));
                    return true;
                case "prev":
                    if (player.getLastTeleports().isEmpty())
                        player.sendMessage("You haven't teleported anywhere!");
                    else {
                        if (player.getLastTeleports().get(0).wild) {
                            player.getDialogueManager().startDialogue("DeepWildD", player.getLastTeleports().get(0));
                        } else {
                            Magic.sendCommandTeleportSpell(player, player.getLastTeleports().get(0).tile);
                        }
                    }
                    return true;
                case "dks":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(2526, 3740, 0));
                    return true;
                case "star":
                case "stars":
                    if (!player.isSuperDonator()) {
                        player.getPackets().sendGameMessage("You need to be at super donator or higher to use this command.");
                        return true;
                    }
                    if (Wilderness.isAtWild(ShootingStars.getStarSprite())) {
                        player.getDialogueManager().startDialogue("StarWarning");
                        return true;
                    }
                    Magic.sendCommandTeleportSpell(player, ShootingStars.getStarSprite());
                    return true;
                case "map": {

                    int regionId = player.getRegionId();
                    int regionX = (regionId >> 8) * 64;
                    int regionY = (regionId & 0xff) * 64;
                    int mapArchiveId = Cache.STORE.getIndexes()[5].getArchiveId("m" + ((regionX >> 3) / 8) + "_" + ((regionY >> 3) / 8));
                    int landscapeArchiveId = Cache.STORE.getIndexes()[5].getArchiveId("l" + ((regionX >> 3) / 8) + "_" + ((regionY >> 3) / 8));

                    System.out.println("RegionId: "+cmd[1]);
                    System.out.println("landArchive: "+landscapeArchiveId);
                    System.out.println("mapArchive: "+mapArchiveId);
                    return true;
                }



                case "sz":
                case "staffzone":
                    if (player.isStaff() || player.isVIPDonator()) {
                        Magic.sendCommandTeleportSpell(player, new WorldTile(5504, 4418, 0));
                        player.getPackets().sendGameMessage("Training at staff zone grants a 10% xp bonus.");
                    } else
                        player.getPackets().sendGameMessage("You need to be at vip donator or higher to use this command.");
                    return true;
                case "sz2":
                    if (player.isStaff() || player.isVIPDonator()) {
                        Magic.sendCommandTeleportSpell(player, new WorldTile(5504, 4424, 1));
                        player.getPackets().sendGameMessage("Training at staff zone grants a 10% xp bonus.");
                    } else
                        player.getPackets().sendGameMessage("You need to be at vip donator or higher to use this command.");
                    return true;
                case "sz3":
                    if (player.isStaff() || player.isVIPDonator()) {
                        Magic.sendCommandTeleportSpell(player, new WorldTile(5504, 4405, 2));
                        player.getPackets().sendGameMessage("Training at staff zone grants a 10% xp bonus.");
                    } else
                        player.getPackets().sendGameMessage("You need to be at vip donator or higher to use this command.");
                    return true;
                case "sz4":
                    if (player.isStaff() || player.isVIPDonator()) {
                        Magic.sendCommandTeleportSpell(player, new WorldTile(5504, 4421, 3));
                        player.getPackets().sendGameMessage("Training at staff zone grants a 10% xp bonus.");
                    } else
                        player.getPackets().sendGameMessage("You need to be at vip donator or higher to use this command.");
                    return true;
                case "eviltree":
                case "eviltrees":
                    if (!player.isSuperDonator()) {
                        player.getPackets().sendGameMessage("You need to be at super donator or higher to use this command.");
                        return true;
                    }
                    if (!EvilTrees.isAlive()) {
                        player.getPackets().sendGameMessage("There is no evil tree alive at moment.");
                        return true;
                    }
                    Magic.sendCommandTeleportSpell(player, EvilTrees.getTile().transform(-1, -1, 0));
                    return true;
                case "house":
                    if (!player.isDonator()) {
                        player.getPackets().sendGameMessage("You need to be a donator to use this command.");
                        return true;
                    }
                    Magic.sendCommandTeleportSpell(player, null);
                    return true;
                case "task":
                    if (player.getSlayerManager().getCurrentTask() == null) {
                        player.getPackets().sendGameMessage("You currently don't have a task.");
                        return true;
                    }
                    if (player.getSlayerManager().getCurrentMaster() == SlayerMaster.KRYSTILIA) {
                        player.getPackets().sendGameMessage("You are currently doing a wilderness slayer task. ::task won't work therefore..");
                        return true;
                    }

				/*if (player.getSlayerManager().getCurrentMaster() == SlayerMaster.DURADEL) {
					if (!player.isSuperDonator()) {
						player.getPackets().sendGameMessage("You do not have the privileges to use this command with this slayer master. Upgrade your rank to emerald donator.");
						return true;
					}
				} else if (!player.isExtremeDonator()
						&& ((player.getSlayerManager().getCurrentMaster() != SlayerMaster.TURAEL
						&&  player.getSlayerManager().getCurrentMaster() != SlayerMaster.VANNAKA
						&& player.getSlayerManager().getCurrentMaster() != SlayerMaster.CHAELDAR))) {
					player.getPackets().sendGameMessage("You do not have the privileges to use this command with this slayer master. Upgrade your rank to ruby donator.");
					return true;
				}*/
                    if (player.getSlayerManager().getCurrentTask().getTile() == null) {
                        player.getPackets().sendGameMessage("You can not teleport to this slayer task.");
                        return true;
                    }
                    Magic.sendCommandTeleportSpell(player, player.getSlayerManager().getCurrentTask().getTile());
                    return true;
                case "edge":
                case "edgeville":
                case "pk":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3101, 3476, 0));
                    return true;
                case "prayer":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3080, 3485, 0));
                    return true;
                case "hunter":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(2906, 3484, 0));
                    return true;
                case "mining":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3285, 3366, 0));
                    return true;
                case "smith":
                case "smithing":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3109, 3501, 0));
                    return true;
                case "cook":
                case "cooking":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(1679, 3619, 0));
                    return true;
			/*case "pk":
				Magic.sendCommandTeleportSpell(player, new WorldTile(2997, 9682, 0));
				return true;*/
                case "funpk":
                case "spawnpk":
                    FfaZone.enter(player);
                    return true;
                case "fishing":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3087, 3229, 0));
                    return true;
                case "dung":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(3450, 3718, 0));
                    return true;
                case "wc":
                case "woodcutting":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(1556, 3487, 0));
                    return true;
                case "craft":
                case "crafting":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(2746, 3444, 0));
                    return true;
                case "summoning":
                    Magic.sendCommandTeleportSpell(player, new WorldTile(2928, 3448, 0));
                    return true;
                case "bosskc":
                case "bosslog":
                    player.stopAll();
                    NPCKillLog.sendBossLog(player);
                    return true;
                case "slayerkc":
                case "slayerlog":
                    player.stopAll();
                    NPCKillLog.sendSlayerLog(player);
                    return true;
                case "itemdrop":
                case "itemdrops":
                case "searchitem":
                    player.stopAll();
                    NPCDrops.searchItem(player);
                    return true;
                case "update":
                case "updates":
                    player.stopAll();
                    player.sendLatestUpdate();
                    player.getPackets().sendOpenURL("https://matrixrsps.io/forums/index.php?/forum/3-updates/");
                    return true;
                case "skull":
                    if (!player.getBank().hasVerified(10))
                        return true;
                    player.setWildernessSkull();
                    return true;
                case "emptyb":
                    if (player.isAdmin()) {
                        player.getBank().resetBank();
                    }
                    return true;
                case "empty":
                    if (player.getControlerManager().getControler() != null && !(player.getControlerManager().getControler() instanceof FfaZone)) {
                        player.getPackets().sendGameMessage("You can't open ::empty in this area.");
                        return true;
                    }
                    player.stopAll();
                    player.getDialogueManager().startDialogue("EmptyD");
                    return true;
                case "commands":
                    player.getPackets().sendOpenURL(Settings.COMMANDS_LINK);
                    return true;
                case "prices":
                    player.getPackets().sendOpenURL("https://matrixrsps.io/forums/index.php?/forum/16-guides/");
                    return true;
                case "events":
                    player.getPackets().sendOpenURL("https://matrixrsps.io/forums/index.php?/forum/7-events/");
                    return true;
                case "compcape":
                    AdventurersLog.open(player);
                    return true;
                case "discord":
                    player.getPackets().sendOpenURL(Settings.DISCORD_LINK);
                    return true;
                case "facebook":
                case "fb":
                    player.getPackets().sendOpenURL(Settings.FACEBOOK_LINK);
                    return true;
                case "website":
                    player.getPackets().sendOpenURL(Settings.WEBSITE_LINK);
                    return true;
                case "rules":
                    player.getPackets().sendOpenURL("https://matrixrsps.io/forums/index.php?/topic/8-official-matrix-rules-standards/");
                    return true;
                case "guides":
                    player.getPackets().sendOpenURL("https://matrixrsps.io/forums/index.php?/forum/16-guides/");
                    return true;
                case "thread"://REQUESTED BY BO, DONT YELL AT CJAY FOR IT K9K
                case "topic":
                    if (cmd.length < 2)
                        return true;
                    player.getPackets().sendOpenURL("https://matrixrsps.io/forums/index.php?app=forums&module=forums&controller=topic&id=" + cmd[1]);
                    return true;
                case "yell":
                    message = "";
                    for (int i = 1; i < cmd.length; i++)
                        message += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    sendYell(player, Utils.fixChatMessage(message), false);
                    return true;
                case "switchitemslook":
                case "sil":
                    player.switchItemsLook();
                    player.getPackets().sendGameMessage("You are now playing with " + (player.isOldItemsLook() ? "old" : "new") + " item looks.");
                    return true;
                case "sn":
                case "switchnotification":
                case "switchnotifications":
                    player.switchNotifications();
                    player.getPackets().sendGameMessage("You are now playing with notifications " + (player.isNotifications() ? "enabled" : "disabled") + ".");
                    return true;
                case "switchhitslook":
                case "shl":
                    player.switchHitLook();
                    player.getPackets().sendGameMessage("You are now playing with " + (player.isOldHitLook() ? "x1" : "x10") + " hit/prayer looks.");
                    return true;
                case "switchvirtuallevels":
                case "svl":
                    player.switchVirtualLevels();
                    player.getPackets().sendGameMessage("You are now playing with  virtual levels " + (player.isVirtualLevels() ? "enabled" : "disabled") + ".");
                    return true;
            }
        }
        return false;
    }

    public static void archiveLogs(Player player, String[] cmd) {
        if (!Settings.HOSTED || (player.getRights() < 1 && !player.isSupporter()))
            return;
        File f = null;
        String location = "";
        if (!(f = new File("data/log/commands/admin/")).exists())
            f.mkdirs();
        if (!(f = new File("data/log/commands/mod/")).exists())
            f.mkdirs();
        if (!(f = new File("data/log/commands/support/")).exists())
            f.mkdirs();

        if (player.getRights() == 2)
            location = "data/log/commands/admin/" + player.getUsername() + ".txt";
        else if (player.getRights() == 1)
            location = "data/log/commands/mod/" + player.getUsername() + ".txt";
        else if (player.isSupporter())
            location = "data/log/commands/support/" + player.getUsername() + ".txt";
        String afterCMD = "";
        for (int i = 1; i < cmd.length; i++)
            afterCMD += cmd[i] + ((i == cmd.length - 1) ? "" : " ");

        String locationf = location;
        String afterCMDf = afterCMD;
        GameExecutorManager.slowExecutor.execute(new Runnable() {//io operations can be slow

            @Override
            public void run() {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(locationf, true));
                    writer.write("[" + Utils.currentTime("dd MMMMM yyyy 'at' hh:mm:ss z") + "] - ::" + cmd[0] + " " + afterCMDf);
                    writer.newLine();
                    writer.flush();
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void performPointEmote(Player teleto) {
        teleto.setNextAnimation(new Animation(17540));
        teleto.setNextGraphics(new Graphics(3401));
    }

    public static void performTeleEmote(Player target) {
        target.setNextAnimation(new Animation(17544));
        target.setNextGraphics(new Graphics(3403));
    }

    public static void performKickBanEmote(Player target) {
        target.setNextAnimation(new Animation(17542));
        target.setNextGraphics(new Graphics(3402));
    }

    /*
     * doesnt let it be instanced
     */
    private Commands() {

    }

    public static void fakeFlower(Player player, int objectID) {
        if (player.isUnderCombat()) {
            player.getPackets().sendGameMessage("You cant plant a seed while under combat.");
            return;
        } else if (World.getStandartObject(player) != null) {
            player.getPackets().sendGameMessage("You can't plant a flower here.");
            return;
        }
        player.setNextAnimation(new Animation(827));
        final WorldObject object = new WorldObject(objectID, 10, 0, player.getX(), player.getY(),
                player.getPlane());
        World.spawnObjectTemporary(object, /*25000*/60000);
        player.getInventory().deleteItem(299, 1);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (!player.addWalkSteps(player.getX() - 1, player.getY(), 1))
                    if (!player.addWalkSteps(player.getX() + 1, player.getY(), 1))
                        if (!player.addWalkSteps(player.getX(), player.getY() + 1, 1))
                            if (!player.addWalkSteps(player.getX(), player.getY() - 1, 1))
                                return;
                player.getDialogueManager().startDialogue("FlowerPickD", object);
            }
        }, 2);
    }


    public static void rightClickPunish(Player player, Player p2) {
        Integer rightClickAction = (Integer) player.getTemporaryAttributtes().get("RIGHTCLICKBAN");

        if (rightClickAction != null) {
            player.sendMessage("Banning " + p2.getUsername() + "..");
            long expires = 1000l * 60l * 60l * 24l * 7l * 4l * 12l * 50l; // 50 years
            LoginClientChannelManager.sendUnreliablePacket(LoginChannelsPacketEncoder.encodeAddOffence(rightClickAction, p2.getUsername(), player.getUsername(), "Offence added by Right click ban", expires).trim());
        } else {
            processCommand(player, "punish " + p2.getDisplayName(), true, false);
        }
    }
}