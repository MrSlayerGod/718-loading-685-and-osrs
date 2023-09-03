package com.rs.game;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.rs.Settings;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.discord.Bot;
import com.rs.executor.GameExecutorManager;
import com.rs.game.item.FloorItem;
import com.rs.game.item.Item;
import com.rs.game.map.MapUtils;
import com.rs.game.map.MapUtils.Structure;
import com.rs.game.minigames.ZarosGodwars;
import com.rs.game.minigames.clanwars.ClanWarRequestController;
import com.rs.game.minigames.duel.DuelControler;
import com.rs.game.minigames.lms.LastManStandingGame;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.npc.NPC;
import com.rs.game.npc.abyssalNexus.AbyssalSire;
import com.rs.game.npc.combat.impl.superiorslayer.GreaterAbyssalDemon;
import com.rs.game.npc.corp.CorporealBeast;
import com.rs.game.npc.dragons.Elvarg;
import com.rs.game.npc.dragons.FrostDragon;
import com.rs.game.npc.dragons.KingBlackDragon;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.glacior.Glacor;
import com.rs.game.npc.godwars.GodWarMinion;
import com.rs.game.npc.godwars.armadyl.GodwarsArmadylFaction;
import com.rs.game.npc.godwars.armadyl.KreeArra;
import com.rs.game.npc.godwars.bandos.GeneralGraardor;
import com.rs.game.npc.godwars.bandos.GodwarsBandosFaction;
import com.rs.game.npc.godwars.saradomin.CommanderZilyana;
import com.rs.game.npc.godwars.saradomin.GodwarsSaradominFaction;
import com.rs.game.npc.godwars.zammorak.GodwarsZammorakFaction;
import com.rs.game.npc.godwars.zammorak.KrilTstsaroth;
import com.rs.game.npc.godwars.zaros.Nex;
import com.rs.game.npc.godwars.zaros.NexMinion;
import com.rs.game.npc.godwars.zaros.ZarosMinion;
import com.rs.game.npc.gorrilas.DemonicGorilla;
import com.rs.game.npc.gorrilas.TorturedGorilla;
import com.rs.game.npc.holiday.EvilSanta;
import com.rs.game.npc.nomad.FlameVortex;
import com.rs.game.npc.nomad.Nomad;
import com.rs.game.npc.others.*;
import com.rs.game.npc.others.EconomyManager;
import com.rs.game.npc.others.zalcano.Zalcano;
import com.rs.game.npc.slayer.CaveKraken;
import com.rs.game.npc.slayer.Cerberus;
import com.rs.game.npc.slayer.Drake;
import com.rs.game.npc.slayer.Hydra;
import com.rs.game.npc.slayer.Kraken;
import com.rs.game.npc.slayer.ThermonuclearSmokeDevil;
import com.rs.game.npc.slayer.Wyrn;
import com.rs.game.npc.wild.Archaeologist;
import com.rs.game.npc.wild.Callisto;
import com.rs.game.npc.wild.ChaosElemental;
import com.rs.game.npc.wild.Galvek;
import com.rs.game.npc.wild.Scorpia;
import com.rs.game.npc.wild.Vetion;
import com.rs.game.npc.worldboss.CallusFrostborne;
import com.rs.game.npc.worldboss.OnyxBoss;
import com.rs.game.player.Equipment;
import com.rs.game.player.OwnedObjectManager;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.TrapAction.HunterNPC;
import com.rs.game.player.content.*;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.questTab.QuestTab;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.seasonalEvents.*;
import com.rs.game.player.content.surpriseevents.EventArena;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.route.Flags;
import com.rs.net.LoginClientChannelManager;
import com.rs.net.Session;
import com.rs.net.encoders.LoginChannelsPacketEncoder;
import com.rs.utils.*;

import javax.swing.text.Position;

public final class World {
    private static final EntityList<Player> players = new EntityList<Player>(Settings.PLAYERS_LIMIT, true);
    private static final List<Player> lobbyPlayers = new ArrayList<Player>(Settings.PLAYERS_LIMIT);
    private static final EntityList<NPC> npcs = new EntityList<NPC>(Settings.NPCS_LIMIT, false);
    private static final Map<Integer, Region> regions = Collections.synchronizedMap(new HashMap<Integer, Region>());

    private static final List<Player> onlineStaff = new LinkedList<Player>();

    private static int skillOfTheDay;
    private static int bossOfTheDay;
    private static long wishingWell;

    public static final void init() {
        setSkillOfTheDay();
        setBossOfTheDay();
        //	addDonationAnnouncmentTask();
        addWorldAnnouncementTask();
        //dont, i made it refresh when u open tab
        //	addQuestTabUpdateTask();w
        addRestoreRunEnergyTask();
        addDrainPrayerTask();
        addRestoreHitPointsTask();
        addRestoreNPCBonusesTask();
        addRestoreSkillsTask();
        addRestoreSpecialAttackTask();
        addRestoreShopItemsTask();
        addOwnedObjectsTask();
        addOnlineTokensTask();
        addTileMessages();
        if (Settings.XP_BONUS_ENABLED)
            addIncreaseElapsedBonusMinutesTak();
        addUpdateDealsTimer();
        setBotsTask();

        /** PUT FUTURE INITS IN ContentInitializer class! */
        ContentInitializer.init();
    }

    public static final void addTileMessages() {
        GameExecutorManager.slowExecutor.scheduleAtFixedRate(() -> {
            for (Player player : World.getPlayers()) {
                if (player.getRegionId() == 8503 && player.hasMessageHovers) {
					player.getPackets().sendTileMessage("Shops / Slayer Masters", new WorldTile(2148, 3553, 0), Color.white.getRGB());
					player.getPackets().sendTileMessage("Teleportation", new WorldTile(3628, 2655, 0), Color.white.getRGB());
					player.getPackets().sendTileMessage("Skilling Zone", new WorldTile(3631, 2673, 0), Color.white.getRGB());
					player.getPackets().sendTileMessage("Spirit Tree / Fairy Rings", new WorldTile(3625, 2662, 0), Color.white.getRGB());
					player.getPackets().sendTileMessage("Spellbooks & Prayers", new WorldTile(3622, 2655, 0), Color.white.getRGB());
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    //announcements every 30 min or gets annoying.
    private static void addWorldAnnouncementTask() {
        if (Settings.ANNOUNCEMENT_TEXTS.length > 0) {
            GameExecutorManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        World.sendNews(Settings.ANNOUNCEMENT_TEXTS[Utils.random(Settings.ANNOUNCEMENT_TEXTS.length)], World.GAME_NEWS);
                    } catch (Throwable e) {
                        Logger.handle(e);
                    }
                }
            }, 0, 15, TimeUnit.MINUTES);
        }

		/*for (Player player : World.getPlayers()) {
			if (player == null || player.isLobby() || !player.isRunning() || player.isBeginningAccount())
				continue;
			GrandExchange.removeExpiredStock(player);
		}*/
    }

    private static void addDonationAnnouncmentTask() {
        if (Settings.ANNOUNCEMENT_TEXTS.length > 0) {
            GameExecutorManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (Player player : World.getPlayers()) {
                            if (player == null || player.isLobby() || !player.isRunning() || player.getInterfaceManager().containsReplacedChatBoxInter() || player.hasVotedInLast24Hours())
                                continue;
                            if (Settings.SPAWN_WORLD) {
                                player.getPackets().sendGameMessage("<img=7><col=D80000>Your ability to wear prod items is currently disabled. You can enable it right now by voting for atleast " + (Settings.VOTE_MIN_AMOUNT / 1000) + "k tokens. Type ::vote to get started.");
                            } else {
                                player.getPackets().sendGameMessage("<img=7><col=D80000>Your 25% Bonus XP and drop rate boost " + "is currently disabled. You can enable it right now by voting for atleast " + (Settings.VOTE_MIN_AMOUNT / 1000) + "k tokens. Type ::vote to get started.");
                            }
                        }
                    } catch (Throwable e) {
                        Logger.handle(e);
                    }
                }
            }, 0, 60 * 7, TimeUnit.SECONDS);
        }
    }

    public static final void addOnlineTokensTask() {
        GameExecutorManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    for (Player player : getPlayers()) {
                        if (player == null || !player.isRunning() || player.getTotalOnlineTime() < 60 * 1000 * 30 || player.isUltimateIronman() || player.isHCIronman())
                            continue;
                        player.getBank().addItem(25434, 1, false);
                        player.getPackets().sendGameMessage("A loyalty token has been added to your bank due to your activity.");
                    }
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    private static void addRestoreShopItemsTask() {
        GameExecutorManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    ShopsHandler.restoreShops();
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, 0, 20, TimeUnit.SECONDS);
    }

    public static final void addIncreaseElapsedBonusMinutesTak() {
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!Settings.XP_BONUS_ENABLED) {
                        this.cancel();
                        return;
                    }
                    for (Player player : getPlayers()) {
                        if (player == null || !player.isRunning())
                            continue;
                        player.getSkills().increaseElapsedBonusMinues();
                    }
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, 0, 60000);
    }

    private static void addOwnedObjectsTask() {
        GameExecutorManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    OwnedObjectManager.processAll();
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    private static final void addRestoreSpecialAttackTask() {

        GameExecutorManager.fastExecutor.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (Player player : getPlayers()) {
                        if (player == null || player.isDead() || !player.isRunning()
                                || player.getInterfaceManager().containsScreenInter())
                            continue;
                        player.getCombatDefinitions().restoreSpecialAttack();
                    }
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, 0, 30000);
    }

    private static final void addRestoreHitPointsTask() {
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {

            private long cycle;

            @Override
            public void run() {
                try {
                    for (Player player : getPlayers()) {
                        if(player.getHealRestoreRate() == 0)
                            continue;
                        if (player == null || player.isDead() || !player.isRunning() || cycle % player.getHealRestoreRate() != 0)
                            continue;
                        player.restoreHitPoints();
                    }
                    for (NPC npc : npcs) {
                        if(npc.getHealRestoreRate() == 0)
                            continue;
                        if (npc == null || npc.isDead() || npc.hasFinished() || cycle % npc.getHealRestoreRate() != 0)
                            continue;
                        npc.restoreHitPoints();
                    }
                    cycle++;
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, 0, 600);
    }

    private static final void addUpdateDealsTimer() {
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (Player player : getPlayers()) {
                        if (player == null || player.isDead() || !player.isRunning())
                            continue;
                        player.getDeals().process();
                        if (player.getInterfaceManager().getCurrentTab() == 3 && player.getInterfaceManager().containsInterface(3002))
                            QuestTab.refresh(player, false);
                    }
                    checkBots();
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, 0, 1000);
    }

    private static final void addRestoreRunEnergyTask() {
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {

            private long cycle;

            @Override
            public void run() {
                try {
                    for (Player player : getPlayers()) {

                        if (player == null || player.isDead() || !player.isRunning() || player.tournamentResetRequired())
                            continue;
                        if (player.getNextRunDirection() == -1) {
                            int r = player.isResting() ? 3 : ((180 - player.getSkills().getLevel(Skills.AGILITY)) / 10);
                            if (cycle != 0 && cycle % r != 0)
                                continue;
                            player.restoreRunEnergy();
                        } else {
                            double weight = player.getWeight();
                            int r = weight >= 270 ? 1 : (int) (10 - (weight / (weight < 0 ? 10 : 30)));
                            if (cycle % r != 0)
                                continue;
                            player.drainRunEnergy();
                        }
                    }
                    cycle++;
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, 0, 100);
    }

    private static final void addDrainPrayerTask() {
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (Player player : getPlayers()) {
                        if (player == null || player.isDead() || !player.isRunning())
                            continue;
                        player.getPrayer().processPrayerDrain();
                    }
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, 0, 600);
    }

    private static final void addRestoreNPCBonusesTask() {
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (NPC npc : npcs) {
                        if (npc == null || npc.isDead() || npc.hasFinished())
                            continue;
                        npc.restoreBonuses();
                    }
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, 0, 3000);
    }

    private static final void addRestoreSkillsTask() {
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (Player player : getPlayers()) {
                        if (player == null || !player.isRunning())
                            continue;
                        int ammountTimes = player.getPrayer().usingPrayer(0, 8) ? 2 : 1;
                        if (player.isResting())
                            ammountTimes += 1;
                        boolean berserker = player.getPrayer().usingPrayer(1, 5);
                        b:
                        for (int skill = 0; skill < 25; skill++) {
                            if (skill == Skills.SUMMONING)
                                continue b;
                            c:
                            for (int time = 0; time < ammountTimes; time++) {
                                int currentLevel = player.getSkills().getLevel(skill);
                                int normalLevel = player.getSkills().getLevelForXp(skill);

                                // don't decay stats below tourney type skill set while in tourney;
                                // but do decay stats if the player used potions in tourney
                                boolean tournamentThreshold = player.tournamentResetRequired() && currentLevel <= (PkTournament.getType() == null ? 99 : PkTournament.getType().getLevelForSkill(skill));

                                if (!tournamentThreshold && currentLevel > normalLevel && time == 0) {
                                    if (skill == Skills.ATTACK || skill == Skills.STRENGTH || skill == Skills.DEFENCE || skill == Skills.RANGE || skill == Skills.MAGIC) {
                                        if (berserker && Utils.random(100) <= 15)
                                            continue c;
                                    }
                                    if (player.getPrayer().usingPrayer(0, 24) && Utils.random(2) == 0) //preserve
                                        continue c;
                                    player.getSkills().set(skill, currentLevel - 1);
                                } else if (currentLevel < normalLevel)
                                    player.getSkills().set(skill, currentLevel + 1);
                                else
                                    break c;
                            }
                        }
                    }
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, 0, 60000);

    }

    public static final Map<Integer, Region> getRegions() {
        return regions;
    }

    public static final Region getRegion(int id) {
        return getRegion(id, false);
    }

    public static final Region getRegion(int id, boolean load) {
        Region region = regions.get(id);
        if (region == null) {
            region = new Region(id);
            regions.put(id, region);
        }
        if (load)
            region.checkLoadMap();
        return region;
    }

    public static final void addPlayer(Player player) {
        if (players.contains(player)) //already contains shouldnt happen
            return;
        players.add(player);
        if (player.getRights() > 0 || player.isSupporter())
            onlineStaff.add(player);
        Bot.sendLog(Bot.LOGIN_LOGOUT_CHANNEL, "[type=LOGIN][name=" + player.getUsername() + "][ip=" + player.getSession().getIP() + "][mac=" + player.getLastGameMAC() + "][play_time=" + Utils.longFormat(player.getTotalOnlineTime()) + "]");
        PlayersOnline.updateCount();
    }

    public static void addLobbyPlayer(Player player) {
        synchronized (lobbyPlayers) {
            lobbyPlayers.add(player);
        }
    }

    public static void removeLobbyPlayer(Player player) {
        synchronized (lobbyPlayers) {
            lobbyPlayers.remove(player);
        }
    }

    public static void removePlayer(Player player) {
        players.remove(player);
        //	if (player.getRights() > 0 || player.isSupporter())
        onlineStaff.remove(player);
        Bot.sendLog(Bot.LOGIN_LOGOUT_CHANNEL, "[type=LOGOUT][name=" + player.getUsername() + "][ip=" + player.getLastGameIp() + "][mac=" + player.getLastGameMAC() + "][session_time=" + Utils.longFormat(player.getSessionTime()) + "]");
        PlayersOnline.updateCount();
    }

    public static final void addNPC(NPC npc) {
        if (npcs.contains(npc)) //already contains shouldnt happen
            return;
        npcs.add(npc);
    }

    public static final void removeNPC(NPC npc) {
        npcs.remove(npc);
    }

    public static List<Player> getOnlineStaff() {
        return onlineStaff;
    }

    public static final NPC spawnNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
        NPC n = null;
        if (id == 15151)
            n = new ForceTalkNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, "Donate to activate the well!", "I heard if you throw coins into this well something cool happens.", "Throw coins into the well to benefit everyone!", "Thank you for sponsoring my gear!");
        else if (id == 15147)
            n = new ForceTalkNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, "Kill Galvek to earn 5m coins instantly and many other rewards!", "LOl then KILL people to earn blood money!", "LOL!", "You can trade your blood coins at ::shop!", "I heard revenants are a great way to get filthy rich.");
        else if (id == 28323) {
            n = new ForceTalkNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, "Prove your worth in the Theatre!", "Glory awaits those who enter the Theatre!");
            n.setNextFaceWorldTile(tile.transform(1, 2, 0));
        } else if (id >= 5533 && id <= 5558)
            n = new Elemental(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 2880)
            n = new DagannothFledeling(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 2881 || id == 2882 || id == 2883)
            n = new DagannothKing(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 2440 || id == 2443 || id == 2446)
            n = new DoorSupport(id, tile);
        else if(id == 16018) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setNextFaceRectanglePrecise(n.transform(-1, 0, 0), 1, 1);
        } else if (id == 7010)
            n = new HunterTrapNPC(HunterNPC.GRENWALL, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 28633)
            n = new Mimic(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 21230)
            n = new GiantMimic(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 5079)
            n = new HunterTrapNPC(HunterNPC.GREY_CHINCHOMPA, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 5080)
            n = new HunterTrapNPC(HunterNPC.RED_CHINCHOMPA, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 22912)
            n = new HunterTrapNPC(HunterNPC.BLACK_CHINCHOMPA, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == Easter2021.BUNNY_2021_ID)
            n = new Easter2021NPC(id, tile);
        else if (id == 5081)
            n = new HunterTrapNPC(HunterNPC.FERRET, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 6916)
            n = new HunterTrapNPC(HunterNPC.GECKO, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 7272)
            n = new HunterTrapNPC(HunterNPC.MONKEY, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 7272)
            n = new HunterTrapNPC(HunterNPC.RACCOON, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 1552)
            n = new EvilSanta(tile);
        else if (id == 5073)
            n = new HunterTrapNPC(HunterNPC.CRIMSON_SWIFT, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 5075)
            n = new HunterTrapNPC(HunterNPC.GOLDEN_WARBLER, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 5076)
            n = new HunterTrapNPC(HunterNPC.COPPER_LONGTAIL, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 5074)
            n = new HunterTrapNPC(HunterNPC.CERULEAN_TWITCH, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 5072)
            n = new HunterTrapNPC(HunterNPC.TROPICAL_WAGTAIL, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 7031)
            n = new HunterTrapNPC(HunterNPC.WIMPY_BIRD, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 5088)
            n = new HunterTrapNPC(HunterNPC.BARB_TAILED_KEBBIT, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 1926 || id == 1931)
            n = new BanditCampBandits(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 5585)
            n = new SkillAlchemistNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        else if (id == 6078 || id == 6079 || id == 4292 || id == 4291 || id == 6080 || id == 6081)
            n = new Cyclopse(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        else if (id == 9441)
            n = new FlameVortex(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 8832 && id <= 8834)
            n = new LivingRock(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 26594 || id == 27234)
            n = new Ent(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 13465 && id <= 13481)
            n = new Revenant(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 1158 || id == 1160)
            n = new KalphiteQueen(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 8528 && id <= 8532)
            n = new Nomad(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 13456 || id == 13457 || id == 13458 || id == 13459)
            n = new ZarosMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        else if (id == 6261 || id == 6263 || id == 6265)
            /*n = GodWarsBosses.graardorMinions[(id - 6261) / 2] = */
            n = new GodWarMinion(id, 0, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 6260)
            n = new GeneralGraardor(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 6222)
            n = new KreeArra(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 6223 || id == 6225 || id == 6227)
            /*n = GodWarsBosses.armadylMinions[(id - 6223) / 2] = */
            n = new GodWarMinion(id, 1, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 6203)
            n = new KrilTstsaroth(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 6204 || id == 6206 || id == 6208)
            /*n = GodWarsBosses.zamorakMinions[(id - 6204) / 2] = */
            n = new GodWarMinion(id, 3, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 6248 || id == 6250 || id == 6252)
            /*n = GodWarsBosses.commanderMinions[(id - 6248) / 2] = */
            n = new GodWarMinion(id, 2, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 6247)
            n = new CommanderZilyana(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 6210 && id <= 6221)
            n = new GodwarsZammorakFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 6254 && id <= 6259)
            n = new GodwarsSaradominFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 6268 && id <= 6283)
            n = new GodwarsBandosFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 6228 && id <= 6246)
            n = new GodwarsArmadylFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 1615 || id == 27241)
            n = new AbyssalDemon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == GreaterAbyssalDemon.ID)
            n = new GreaterAbyssalDemon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 2058)
            n = new HoleInTheWall(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        else if (id == 50 || id == 2642)
            n = new KingBlackDragon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 28097)
            n = new Galvek(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 9462 && id <= 9467)
            n = new Strykewyrm(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 3200)
            n = new ChaosElemental(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 6026 && id <= 6045)
            n = new Werewolf(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 27800 || id == 27207 || id == 25936 || id == 1266 || id == 1268 || id == 2453 || id == 2886
                || id == 27267)
            n = new RockCrabs(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 8133)
            n = new CorporealBeast(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == Zalcano.ZALCANO_ID)
            n = Zalcano.ZALCANO_INSTANCE = new Zalcano(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 13447)
            n = ZarosGodwars.nex = new Nex(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        else if (id == 13451)
            n = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 13452)
            n = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 13453)
            n = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 13454)
            n = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 14256)
            n = new Lucien(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 1282) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setLocked(true);
        } else if (id == 43 || (id >= 5156 && id <= 5164) || id == 5156 || id == 1765)
            n = new Sheep(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        else if (id == 51)
            n = new FrostDragon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 8335)
            n = new MercenaryMage(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 8349 || id == 8450 || id == 8451)
            n = new TormentedDemon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 15149)
            n = new MasterOfFear(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 1631 || id == 1632)
            n = new ConditionalDeath(4161, "The rockslug shrivels and dies.", true, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 14301)
            n = new Glacor(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 1610)
            n = new ConditionalDeath(new int[]{4162, 51742}, "The gargoyle breaks into peices as you slam the hammer onto its head.", false, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 14849)
            n = new ConditionalDeath(23035, null, false, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 1627 || id == 1628 || id == 1629 || id == 1630)
            n = new ConditionalDeath(new int[]{13290, 4158}, null, false, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 2803 && id <= 2808)
            n = new ConditionalDeath(6696, null, true, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 1609 || id == 1610 || id == 1611 || id == 1623 || (id >= 1626 && id <= 1630))
            n = new Kurask(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        else if (id == 3153)
            n = new HarpieBug(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        else if (id == 3344 || id == 3345 || id == 3346 || id == 3347)
            n = new MutatedZygomites(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        else if (id == 13820 || id == 13821 || id == 13822)
            n = new Jadinko(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 14688 && id <= 14701)
            n = new PolyporeCreature(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 15184 && id <= 15186)
            n = new OnyxBoss(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id >= 21212 && id <= 21212)
            n = new CallusFrostborne(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 16031)
            n = new GrimReaper(id, tile);
        else if (id == 16032)
            n = new EvilSnowman(id, tile);
        else if (com.rs.game.player.content.EconomyManager.isEconomyManagerNpc(id))
            n = new EconomyManager(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 2417)
            n = new WildyWyrm(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 26611)
            n = new Vetion(tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 26503)
            n = new Callisto(tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 26615)
            n = new Scorpia(tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 1131 || id == 1132 || id == 1133 || id == 1134) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setForceAgressive(true);
        } else if (id == 20499)
            n = new ThermonuclearSmokeDevil(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 20493)
            n = new CaveKraken(tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 28610)
            n = new Wyrn(tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 28612)
            n = new Drake(tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 28609)
            n = new Hydra(tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 20496)
            n = new Kraken(tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 25886)
            n = new AbyssalSire(tile);
        else if (id == 25863)
            n = new Cerberus(tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 27144)
            n = new DemonicGorilla(tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 27150)
            n = new TorturedGorilla(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 26607 || (id >= 26914 && id <= 26919)) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setDropRateFactor(2);
        } else if (id == 26619 || id == 26504) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setDropRateFactor(3);
            //	n.setIntelligentRouteFinder(true);
        } else if (id == 26766 || id == 28565 || id == 16025 || id == 29293) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setIntelligentRouteFinder(true);
        } else if (id == 26618 || id == 27806) {
            n = new Archaeologist(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 912 && id <= 914) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setDropRateFactor(3);
        } else if (id >= 6747 && id <= 6749) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setForceMultiAttacked(true);
            n.setForceMultiArea(true);
            n.setCantSetTargetAutoRelatio(true);
            n.setIntelligentRouteFinder(true);
        } else if (id == 26504) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setDropRateFactor(7);
        } else if (id == 3340) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setDropRateFactor(2);
        } else if (id == 16000 || id == 16001 || id == 16008)
            n = new CombatDummy(id, tile, spawned);
        else if (id >= 27792 && id <= 27794) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setDropRateFactor(0.7);
        } else if (id >= 27273 && id <= 27274) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setDropRateFactor(0.2);
        } else if (id == 28030 || id == 1591 || id == 1592 || id == 3068) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setDropRateFactor(0.5);
        } else if (id == 5666) {
            n = new Barrelchest(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 8549) {
            n = new Phoenix(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 742) {
            n = new Elvarg(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 795) {
            n = new IceQueen(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 13460 || id == 14836) {
            n = new Hati(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 5627 || id == 5628) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            n.setForceAgressive(true);
        } else
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);

        if(faceNorth(tile)) {
            n.setNextFaceWorldTile(new WorldTile(n.getX(), n.getY() + 1, 0)); // face north
        } else if(faceEast(tile)) {
            n.setRandomWalk(0);
            n.setNextFaceWorldTile(new WorldTile(n.getX() + 1, n.getY(), 0)); // face east
        } else if(faceSouth(tile)) {
            n.setRandomWalk(0);
            n.setNextFaceWorldTile(new WorldTile(n.getX(), n.getY() - 1, 0)); // face south
        } else if(faceWest(tile)) {
            n.setRandomWalk(0);
            n.setNextFaceWorldTile(new WorldTile(n.getX() - 1, n.getY(), 0)); // face west
        }
        return n;
    }

    private static WorldTile[] FACE_NORTH = new WorldTile[] {
            new WorldTile(3083, 3484, 0), //Turadel (North)
            new WorldTile(3081, 3484, 0), //Vannaka (North)
            new WorldTile(3079, 3484, 0), //Nieve (North)
            new WorldTile(3109, 3504, 0), //Shopkeeper (north)
            new WorldTile(3114, 3504, 0), //Shopkeeper (north)
            new WorldTile(3102, 3489, 0), // ADD GE Clerk (north)

    };
    private static WorldTile[] FACE_EAST = new WorldTile[] {
            new WorldTile(3078, 3486, 0), //TzHaar-Ket-Keh (East)
            new WorldTile(3078, 3489, 0), //Krystilia (East)
            new WorldTile(3103, 3487, 0), // ADD Banker (East)
            new WorldTile(3103, 3488, 0), // ADD Banker (East)
            new WorldTile(3086, 3484, 0), //Combat Dummy (east)
            new WorldTile(3086, 3491, 0), //Combat Dummy (east)
            new WorldTile(3088, 3507, 0), //mandrith in shops (east)
            new WorldTile(3088, 3509, 0), //Onyx Guide (East)
            new WorldTile(3088, 3506, 0), //Donation Guide (East)
            new WorldTile(3088, 3508, 0), //Max (East)
            new WorldTile(3088, 3507, 0), //Mandrith (East)
            new WorldTile(3086, 3496, 0), //Larxus (East)
            new WorldTile(3086, 3476, 0), //Lisa (East)
    };
    private static WorldTile[] FACE_SOUTH = new WorldTile[] {
            new WorldTile(3314, 3240, 0), // Lisa SOUTH
            new WorldTile(3091, 3471, 0), // Zahur SOUTH
            new WorldTile(3096, 3471, 0), //Bob Barter (South)
            new WorldTile(3102, 3486, 0), //GE Clerk (south)
            new WorldTile(3101, 3489, 0), //ADD GE Clerk (north)
            new WorldTile(3101, 3486, 0), // ADD GE Clerk (south)
            new WorldTile(3104, 3505, 0), //Mr Ex (south)
            new WorldTile(3083, 3491, 0), //Kuradal (South)
            new WorldTile(3081, 3491, 0), //Duradel (South)
            new WorldTile(3079, 3491, 0), //Konar (South)
            new WorldTile(3089, 3510, 0), //Xuan (South)
            new WorldTile(3090, 3510, 0), //Loyal Dan (South)
            new WorldTile(3091, 3510, 0), //Pure Guide (South)
            new WorldTile(3092, 3510, 0), //Gear Guide (South)
            new WorldTile(3093, 3510, 0), //Skilling Guide (South)
            new WorldTile(3094, 3510, 0), //Skilling Secondaries Guide (South)
            new WorldTile(3095, 3510, 0), //Fremennik Guide (South)
            new WorldTile(3096, 3510, 0), //Voting Guide (South)
    };
    private static WorldTile[] FACE_WEST = new WorldTile[] {
            new WorldTile(3117, 3467, 0), //Father Aereck (West)
            new WorldTile(3126, 3494, 0), //The Collector (west)
            new WorldTile(3100, 3487, 0), // ADD Banker (West)
            new WorldTile(3100, 3488, 0), // ADD Banker (West)
            new WorldTile(3126, 3481, 0), //Mandrith (west)
            new WorldTile(3126, 3491, 0), // Onyx Guide (West)
            new WorldTile(3126, 3484, 0), // Makeover Mage (West)
            new WorldTile(3097, 3509, 0), //Strange Old Man (West)
            new WorldTile(3097, 3508, 0), //*TzHaar-Hur-Tel (West)
            new WorldTile(3097, 3507, 0), //Fidelio (West)
            new WorldTile(3097, 3506, 0), //Wise Old Man (West)
    };
    public static boolean faceNorth(WorldTile tile) {
        return Arrays.stream(FACE_NORTH).anyMatch(worldTile -> worldTile.matches(tile));
    }
    public static boolean faceEast(WorldTile tile) {
        return Arrays.stream(FACE_EAST).anyMatch(worldTile -> worldTile.matches(tile));
    }
    public static boolean faceSouth(WorldTile tile) {
        return Arrays.stream(FACE_SOUTH).anyMatch(worldTile -> worldTile.matches(tile));
    }
    public static boolean faceWest(WorldTile tile) {
        return Arrays.stream(FACE_WEST).anyMatch(worldTile -> worldTile.matches(tile));
    }
    public static final NPC spawnNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
        return spawnNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
    }

    public static final void updateEntityRegion(Entity entity) {
        if (entity.hasFinished()) {
            if (entity instanceof Player)
                getRegion(entity.getLastRegionId()).removePlayerIndex(entity.getIndex());
            else
                getRegion(entity.getLastRegionId()).removeNPCIndex(entity.getIndex());
            return;
        }
        int regionId = entity.getRegionId();
        if (entity.getLastRegionId() != regionId) { // map region entity at
            // changed
            if (entity instanceof Player) {
                if (entity.getLastRegionId() > 0)
                    getRegion(entity.getLastRegionId()).removePlayerIndex(entity.getIndex());
                Region region = getRegion(regionId);
                region.addPlayerIndex(entity.getIndex());
                Player player = (Player) entity;
                int musicId = region.getRandomMusicId();
                if (musicId != -1)
                    player.getMusicsManager().checkMusic(musicId);
                player.getControlerManager().moved();
                if (player.hasStarted()) {
                    checkControlersAtMove(player);
                    Deadman.check(player);
                }
            } else {
                if (entity.getLastRegionId() > 0)
                    getRegion(entity.getLastRegionId()).removeNPCIndex(entity.getIndex());
                getRegion(regionId).addNPCIndex(entity.getIndex());
            }
            entity.checkMultiArea();
            entity.setLastRegionId(regionId);
        } else {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.getControlerManager().moved();
                if (player.hasStarted()) {
                    checkControlersAtMove(player);
                    Deadman.check(player);
                }
            }
            entity.checkMultiArea();
        }
    }


    public static boolean isBank(Player player) {
        int mapID = player.getRegionId();
        return player.withinArea(3341, 3265, 3387, 3281) //duel arena
                || player.withinArea(2828, 5085, 2866, 5109) //party room
                || player.withinArea(3144, 3468, 3186, 3516) //ge
                || player.withinArea(3091, 3488, 3098, 3499) //edgeville bank
                || player.withinArea(3082, 3488, 3098, 3506) //edgeville home
                || mapID == 7473 //soulwars bank
                ;
    }

    private static void checkControlersAtMove(Player player) {
        if (player.getControlerManager().getControler() == null) {
            String control = null;
            if (ClanWarRequestController.inWarRequest(player))
                control = "clan_wars_request";
            else if (DuelControler.isAtDuelArena(player))
                control = "DuelControler";
            if (control != null)
                player.getControlerManager().startControler(control);
        }
    }

    /*
     * checks clip
     */
    public static boolean isRegionLoaded(int regionId) {
        Region region = getRegion(regionId);
        if (region == null)
            return false;
        return region.getLoadMapStage() == 2;
    }

    public static boolean isTileFree(WorldTile t, int size) {
        return isTileFree(t.getPlane(), t.getX(), t.getY(), size);
    }

    public static boolean isTileFree(int plane, int x, int y, int size) {
        for (int tileX = x; tileX < x + size; tileX++)
            for (int tileY = y; tileY < y + size; tileY++)
                if (!isFloorFree(plane, tileX, tileY) || !isWallsFree(plane, tileX, tileY))
                    return false;
        return true;
    }

    public static boolean isFloorFree(int plane, int x, int y, int size) {
        for (int tileX = x; tileX < x + size; tileX++)
            for (int tileY = y; tileY < y + size; tileY++)
                if (!isFloorFree(plane, tileX, tileY))
                    return false;
        return true;
    }

    public static boolean isFloorFree(int plane, int x, int y) {
        return (getMask(plane, x, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ)) == 0;
    }

    public static boolean isWallsFree(int plane, int x, int y) {
        return (getMask(plane, x, y) & (Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST | Flags.WALLOBJ_EAST | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST)) == 0;
    }

    public static int getMask(int plane, int x, int y) {
        WorldTile tile = new WorldTile(x, y, plane);
        Region region = getRegion(tile.getRegionId());
        if (region == null)
            return -1;
        return region.getMask(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
    }

    private static int getClipedOnlyMask(int plane, int x, int y) {
        WorldTile tile = new WorldTile(x, y, plane);
        Region region = getRegion(tile.getRegionId());
        if (region == null)
            return -1;
        return region.getMaskClipedOnly(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
    }

    public static final boolean checkProjectileStep(int plane, int x, int y, int dir, int size) {
        int xOffset = Utils.DIRECTION_DELTA_X[dir];
        int yOffset = Utils.DIRECTION_DELTA_Y[dir];
        /*
         * int rotation = getRotation(plane,x+xOffset,y+yOffset); if(rotation !=
         * 0) { dir += rotation; if(dir >= Utils.DIRECTION_DELTA_X.length) dir =
         * dir - (Utils.DIRECTION_DELTA_X.length-1); xOffset =
         * Utils.DIRECTION_DELTA_X[dir]; yOffset = Utils.DIRECTION_DELTA_Y[dir];
         * }
         */
        if (size == 1) {
            int mask = getClipedOnlyMask(plane, x + Utils.DIRECTION_DELTA_X[dir], y + Utils.DIRECTION_DELTA_Y[dir]);
            if (xOffset == -1 && yOffset == 0)
                return (mask & 0x42240000) == 0;
            if (xOffset == 1 && yOffset == 0)
                return (mask & 0x60240000) == 0;
            if (xOffset == 0 && yOffset == -1)
                return (mask & 0x40a40000) == 0;
            if (xOffset == 0 && yOffset == 1)
                return (mask & 0x48240000) == 0;
            if (xOffset == -1 && yOffset == -1) {
                return (mask & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x - 1, y) & 0x42240000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x40a40000) == 0;
            }
            if (xOffset == 1 && yOffset == -1) {
                return (mask & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 1, y) & 0x60240000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x40a40000) == 0;
            }
            if (xOffset == -1 && yOffset == 1) {
                return (mask & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x - 1, y) & 0x42240000) == 0 && (getClipedOnlyMask(plane, x, y + 1) & 0x48240000) == 0;
            }
            if (xOffset == 1 && yOffset == 1) {
                return (mask & 0x78240000) == 0 && (getClipedOnlyMask(plane, x + 1, y) & 0x60240000) == 0 && (getClipedOnlyMask(plane, x, y + 1) & 0x48240000) == 0;
            }
        } else if (size == 2) {
            if (xOffset == -1 && yOffset == 0)
                return (getClipedOnlyMask(plane, x - 1, y) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4e240000) == 0;
            if (xOffset == 1 && yOffset == 0)
                return (getClipedOnlyMask(plane, x + 2, y) & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y + 1) & 0x78240000) == 0;
            if (xOffset == 0 && yOffset == -1)
                return (getClipedOnlyMask(plane, x, y - 1) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x + 1, y - 1) & 0x60e40000) == 0;
            if (xOffset == 0 && yOffset == 1)
                return (getClipedOnlyMask(plane, x, y + 2) & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x + 1, y + 2) & 0x78240000) == 0;
            if (xOffset == -1 && yOffset == -1)
                return (getClipedOnlyMask(plane, x - 1, y) & 0x4fa40000) == 0 && (getClipedOnlyMask(plane, x - 1, y - 1) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x63e40000) == 0;
            if (xOffset == 1 && yOffset == -1)
                return (getClipedOnlyMask(plane, x + 1, y - 1) & 0x63e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y - 1) & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y) & 0x78e40000) == 0;
            if (xOffset == -1 && yOffset == 1)
                return (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4fa40000) == 0 && (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x, y + 2) & 0x7e240000) == 0;
            if (xOffset == 1 && yOffset == 1)
                return (getClipedOnlyMask(plane, x + 1, y + 2) & 0x7e240000) == 0 && (getClipedOnlyMask(plane, x + 2, y + 2) & 0x78240000) == 0 && (getClipedOnlyMask(plane, x + 1, y + 1) & 0x78e40000) == 0;
        } else {
            if (xOffset == -1 && yOffset == 0) {
                if ((getClipedOnlyMask(plane, x - 1, y) & 0x43a40000) != 0 || (getClipedOnlyMask(plane, x - 1, -1 + (y + size)) & 0x4e240000) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
                    if ((getClipedOnlyMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0)
                        return false;
            } else if (xOffset == 1 && yOffset == 0) {
                if ((getClipedOnlyMask(plane, x + size, y) & 0x60e40000) != 0 || (getClipedOnlyMask(plane, x + size, y - (-size + 1)) & 0x78240000) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
                    if ((getClipedOnlyMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0)
                        return false;
            } else if (xOffset == 0 && yOffset == -1) {
                if ((getClipedOnlyMask(plane, x, y - 1) & 0x43a40000) != 0 || (getClipedOnlyMask(plane, x + size - 1, y - 1) & 0x60e40000) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
                    if ((getClipedOnlyMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0)
                        return false;
            } else if (xOffset == 0 && yOffset == 1) {
                if ((getClipedOnlyMask(plane, x, y + size) & 0x4e240000) != 0 || (getClipedOnlyMask(plane, x + (size - 1), y + size) & 0x78240000) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
                    if ((getClipedOnlyMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0)
                        return false;
            } else if (xOffset == -1 && yOffset == -1) {
                if ((getClipedOnlyMask(plane, x - 1, y - 1) & 0x43a40000) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
                    if ((getClipedOnlyMask(plane, x - 1, y + (-1 + sizeOffset)) & 0x4fa40000) != 0 || (getClipedOnlyMask(plane, sizeOffset - 1 + x, y - 1) & 0x63e40000) != 0)
                        return false;
            } else if (xOffset == 1 && yOffset == -1) {
                if ((getClipedOnlyMask(plane, x + size, y - 1) & 0x60e40000) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
                    if ((getClipedOnlyMask(plane, x + size, sizeOffset + (-1 + y)) & 0x78e40000) != 0 || (getClipedOnlyMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0)
                        return false;
            } else if (xOffset == -1 && yOffset == 1) {
                if ((getClipedOnlyMask(plane, x - 1, y + size) & 0x4e240000) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
                    if ((getClipedOnlyMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0 || (getClipedOnlyMask(plane, -1 + (x + sizeOffset), y + size) & 0x7e240000) != 0)
                        return false;
            } else if (xOffset == 1 && yOffset == 1) {
                if ((getClipedOnlyMask(plane, x + size, y + size) & 0x78240000) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
                    if ((getClipedOnlyMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0 || (getClipedOnlyMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0)
                        return false;
            }
        }
        return true;
    }

    public static final boolean checkWalkStep(int plane, int x, int y, int dir, int size) {
        return checkWalkStep(plane, x, y, Utils.DIRECTION_DELTA_X[dir], Utils.DIRECTION_DELTA_Y[dir], size);
    }

    public static final boolean checkWalkStep(int plane, int x, int y, int xOffset, int yOffset, int size) {
        if (size == 1) {
            int mask = getMask(plane, x + xOffset, y + yOffset);
            if (xOffset == -1 && yOffset == 0)
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST)) == 0;
            if (xOffset == 1 && yOffset == 0)
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_WEST)) == 0;
            if (xOffset == 0 && yOffset == -1)
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH)) == 0;
            if (xOffset == 0 && yOffset == 1)
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH)) == 0;
            if (xOffset == -1 && yOffset == -1)
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST)) == 0 && (getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH)) == 0;
            if (xOffset == 1 && yOffset == -1)
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) == 0 && (getMask(plane, x + 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_WEST)) == 0 && (getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH)) == 0;
            if (xOffset == -1 && yOffset == 1)
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST)) == 0 && (getMask(plane, x, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH)) == 0;
            if (xOffset == 1 && yOffset == 1)
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) == 0 && (getMask(plane, x + 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_WEST)) == 0 && (getMask(plane, x, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH)) == 0;
        } else if (size == 2) {
            if (xOffset == -1 && yOffset == 0)
                return (getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x - 1, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) == 0;
            if (xOffset == 1 && yOffset == 0)
                return (getMask(plane, x + 2, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) == 0 && (getMask(plane, x + 2, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
            if (xOffset == 0 && yOffset == -1)
                return (getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x + 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) == 0;
            if (xOffset == 0 && yOffset == 1)
                return (getMask(plane, x, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x + 1, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
            if (xOffset == -1 && yOffset == -1)
                return (getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x - 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) == 0;
            if (xOffset == 1 && yOffset == -1)
                return (getMask(plane, x + 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x + 2, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) == 0 && (getMask(plane, x + 2, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
            if (xOffset == -1 && yOffset == 1)
                return (getMask(plane, x - 1, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x - 1, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
            if (xOffset == 1 && yOffset == 1)
                return (getMask(plane, x + 1, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) == 0 && (getMask(plane, x + 2, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) == 0 && (getMask(plane, x + 1, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
        } else {
            if (xOffset == -1 && yOffset == 0) {
                if ((getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) != 0 || (getMask(plane, x - 1, -1 + (y + size)) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
                    if ((getMask(plane, x - 1, y + sizeOffset) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) != 0)
                        return false;
            } else if (xOffset == 1 && yOffset == 0) {
                if ((getMask(plane, x + size, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) != 0 || (getMask(plane, x + size, y - (-size + 1)) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
                    if ((getMask(plane, x + size, y + sizeOffset) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) != 0)
                        return false;
            } else if (xOffset == 0 && yOffset == -1) {
                if ((getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) != 0 || (getMask(plane, x + size - 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
                    if ((getMask(plane, x + sizeOffset, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) != 0)
                        return false;
            } else if (xOffset == 0 && yOffset == 1) {
                if ((getMask(plane, x, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) != 0 || (getMask(plane, x + (size - 1), y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
                    if ((getMask(plane, x + sizeOffset, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) != 0)
                        return false;
            } else if (xOffset == -1 && yOffset == -1) {
                if ((getMask(plane, x - 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
                    if ((getMask(plane, x - 1, y + (-1 + sizeOffset)) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) != 0 || (getMask(plane, sizeOffset - 1 + x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) != 0)
                        return false;
            } else if (xOffset == 1 && yOffset == -1) {
                if ((getMask(plane, x + size, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
                    if ((getMask(plane, x + size, sizeOffset + (-1 + y)) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) != 0 || (getMask(plane, x + sizeOffset, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) != 0)
                        return false;
            } else if (xOffset == -1 && yOffset == 1) {
                if ((getMask(plane, x - 1, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
                    if ((getMask(plane, x - 1, y + sizeOffset) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) != 0 || (getMask(plane, -1 + (x + sizeOffset), y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) != 0)
                        return false;
            } else if (xOffset == 1 && yOffset == 1) {
                if ((getMask(plane, x + size, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) != 0)
                    return false;
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
                    if ((getMask(plane, x + sizeOffset, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) != 0 || (getMask(plane, x + size, y + sizeOffset) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) != 0)
                        return false;
            }
        }
        return true;
    }

    public static final boolean containsPlayer(String username) {
        for (Player p2 : players) {
            if (p2 == null)
                continue;
            if (p2.getUsername().equals(username))
                return true;
        }
        return false;
    }

    public static final boolean containsLobbyPlayer(String username) {
        synchronized (lobbyPlayers) {
            for (Player p2 : lobbyPlayers) {
                if (p2 == null)
                    continue;
                if (p2.getUsername().equals(username))
                    return true;
            }
            return false;
        }
    }

    public static Player getPlayer(String username) {
        for (Player player : getPlayers()) {
            if (player == null)
                continue;
            if (player.getUsername().equals(username))
                return player;
        }
        return null;
    }

    public static Player getPlayerWithDisplay(String display) {
        for (Player player : getPlayers()) {
            if (player == null)
                continue;
            if (player.getDisplayName().equalsIgnoreCase(display))
                return player;
        }
        return null;
    }

    public static Player getLobbyPlayer(String username) {
        synchronized (lobbyPlayers) {
            for (Player player : lobbyPlayers) {
                if (player == null)
                    continue;
                if (player.getUsername().equals(username))
                    return player;
            }
            return null;
        }
    }

    public static final Player getPlayerByDisplayName(String username) {
        String formatedUsername = Utils.formatPlayerNameForDisplay(username);
        for (Player player : getPlayers()) {
            if (player == null)
                continue;
            if (player.getUsername().equalsIgnoreCase(formatedUsername) || player.getDisplayName().equalsIgnoreCase(formatedUsername))
                return player;
        }
        return null;
    }

    public static final Player getPlayerByDisplayNameAll(String username) {
        String formatedUsername = Utils.formatPlayerNameForDisplay(username);
        for (Player player : getPlayers()) {
            if (player == null)
                continue;
            if (player.getUsername().equalsIgnoreCase(formatedUsername) || player.getDisplayName().equalsIgnoreCase(formatedUsername))
                return player;
        }
        synchronized (lobbyPlayers) {
            for (Player player : lobbyPlayers) {
                if (player == null)
                    continue;
                if (player.getUsername().equalsIgnoreCase(formatedUsername) || player.getDisplayName().equalsIgnoreCase(formatedUsername))
                    return player;
            }
        }
        return null;
    }

    public static final EntityList<Player> getPlayers() {
        return players;
    }

    public static final List<Player> getLobbyPlayers() {
        synchronized (lobbyPlayers) {
            return new ArrayList<Player>(lobbyPlayers);
        }
    }

    public static final EntityList<NPC> getNPCs() {
        return npcs;
    }

    private World() {

    }

    public static final boolean isSpawnedObject(WorldObject object) {
        return getRegion(object.getRegionId()).getSpawnedObjects().contains(object);
    }

    public static final void spawnObject(WorldObject object) {
        getRegion(object.getRegionId()).spawnObject(object, object.getPlane(), object.getXInRegion(), object.getYInRegion(), false);
    }

    public static final void unclipTile(WorldTile tile) {
        getRegion(tile.getRegionId()).unclip(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
    }

    public static final void addFloor(WorldTile tile) {
        getRegion(tile.getRegionId()).addFloor(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
    }

    public static final void removeFloor(WorldTile tile) {
        getRegion(tile.getRegionId()).removeFloor(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
    }


    public static final void removeObject(WorldObject object) {
        if (object == null)
            return;
        getRegion(object.getRegionId()).removeObject(object, object.getPlane(), object.getXInRegion(), object.getYInRegion());
    }

    public static final void spawnObjectTemporary(final WorldObject object, long time) {
        spawnObjectTemporary(object, time, false, false);
    }

    public static final void spawnObjectTemporary(final WorldObject object, long time, final boolean checkObjectInstance, boolean checkObjectBefore) {
        spawnObjectTemporary(object, time, checkObjectInstance, checkObjectBefore, null);
    }

    public static final void spawnObjectTemporary(final WorldObject object, long time, final boolean checkObjectInstance, boolean checkObjectBefore, Runnable remove) {
        final WorldObject before = checkObjectBefore ? World.getObjectWithType(object, object.getType()) : null;
        spawnObject(object);
        GameExecutorManager.slowExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    if (checkObjectInstance && World.getObjectWithId(object, object.getId()) != object)
                        return;
                    if (remove != null)
                        remove.run();

                    if (before != null)
                        spawnObject(before);
                    else
                        removeObject(object); //this method allows to remove object with just tile and type actualy so the removing object may be diferent and still gets removed
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }

        }, time, TimeUnit.MILLISECONDS);
    }

    public static final boolean removeObjectTemporary(final WorldObject object, long time) {
        removeObject(object);
        GameExecutorManager.slowExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    spawnObject(object);
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }

        }, time, TimeUnit.MILLISECONDS);
        return true;
    }

    public static final void spawnTempGroundObject(final WorldObject object, final int replaceId, long time) {
        spawnObject(object);
        GameExecutorManager.slowExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    removeObject(object);
                    addGroundItem(new Item(replaceId), object, null, false, 180);
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, time, TimeUnit.MILLISECONDS);
    }

    public static final WorldObject getStandartObject(WorldTile tile) {
        return getRegion(tile.getRegionId()).getStandartObject(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
    }

    public static final WorldObject getObjectWithType(WorldTile tile, int type) {
        return getRegion(tile.getRegionId()).getObjectWithType(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), type);
    }

    public static final WorldObject getObjectWithSlot(WorldTile tile, int slot) {
        return getRegion(tile.getRegionId()).getObjectWithSlot(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), slot);
    }

    public static final WorldObject getRealObject(WorldTile tile, int slot) {
        return getRegion(tile.getRegionId()).getRealObject(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), slot);
    }

    public static final boolean containsObjectWithId(WorldTile tile, int id) {
        return getRegion(tile.getRegionId()).containsObjectWithId(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), id);
    }

    public static final WorldObject getObjectWithId(WorldTile tile, int id) {
        return getRegion(tile.getRegionId()).getObjectWithId(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), id);
    }

    public static final void addGroundItem(final Item item, final WorldTile tile) {
        // adds item, not invisible, no owner, no time to disapear
        addGroundItem(item, tile, null, false, -1, 2, -1);
    }

    public static final FloorItem addGroundItem(final Item item, final WorldTile tile, final Player owner/*
     * null
     * for
     * default
     */, boolean invisible, long hiddenTime/*
     * default
     * 3
     * minutes
     */) {
        return addGroundItem(item, tile, owner, invisible, hiddenTime, 2, 150);
    }

    public static final FloorItem addCoxFloorItem(final Item item, final WorldTile tile, final Player owner) {
        FloorItem i = addGroundItem(item, tile, owner, false, -1, 2, 30 * 60000);
        i.setChambers();
        return i;
    }

    public static final FloorItem addCoxFloorItemNPCDrop(final Item item, final WorldTile tile, final Player owner) {
        FloorItem i = addGroundItem(item, tile, owner, true, 10, 2, 30 * 60000);
        i.setChambers();
        return i;
    }

    public static final FloorItem addGroundItem(final Item item, final WorldTile tile, final Player owner/*
     * null
     * for
     * default
     */, boolean invisible, long hiddenTime/*
     * default
     * 3
     * minutes
     */, int type) {
        return addGroundItem(item, tile, owner, invisible, hiddenTime, type, 150);
    }

    public static final boolean containsItem(FloorItem floorItem) {
        return getRegion(floorItem.getTile().getRegionId()).getGroundItemsSafe().contains(floorItem);
    }

    public static final void turnPublic(FloorItem floorItem, int publicTime) {
        if (!floorItem.isInvisible() || floorItem.isChambers())
            return;
        int regionId = floorItem.getTile().getRegionId();
        final Region region = getRegion(regionId);
        if (!region.getGroundItemsSafe().contains(floorItem))
            return;
        Player realOwner = floorItem.hasOwner() ? World.getPlayer(floorItem.getOwner()) : null;
        if (!ItemConstants.isTradeable(floorItem)) {
            if(floorItem.isChambers()) // removed at end of raid, don't turn to coins
                return; // players store items on the floor
            if (realOwner != null && realOwner.getMapRegionsIds().contains(regionId))
                realOwner.getPackets().sendRemoveGroundItem(floorItem);


            int degradeId = -1;
            for (int i = 0; i < 10; i++) {
                int nextId = ItemConstants.getItemDegrade(degradeId != -1 ? degradeId : floorItem.getId());
                if (nextId == -1)
                    break;
                degradeId = nextId;
            }
            if (degradeId != -1 && ItemConstants.isTradeable(new Item(degradeId))) {
                floorItem.setId(degradeId);
            } else {
                //int price = floorItem.getDefinitions().getValue();
                //if (price <= 1) {
                    region.getGroundItemsSafe().remove(floorItem);
                    removeGroundItem(floorItem);
                    return;
                //}

                //floorItem.setId(995);
                //floorItem.setAmount(price);
            }
            if (realOwner != null && realOwner.getMapRegionsIds().contains(regionId))
                realOwner.getPackets().sendGroundItem(floorItem);
        }

        floorItem.setInvisible(false);
        for (Player player : getPlayers()) {
            if (player == null || player == realOwner || !player.hasStarted() || player.hasFinished() || !player.getMapRegionsIds().contains(regionId))
                continue;
            player.getPackets().sendGroundItem(floorItem);
        }
        // disapears after this time
        if (publicTime != -1)
            removeGroundItem(floorItem, publicTime);
    }

    @Deprecated
    public static final void addGroundItemForever(Item item, final WorldTile tile) {
        int regionId = tile.getRegionId();
        final FloorItem floorItem = new FloorItem(item, tile, true);
        final Region region = getRegion(tile.getRegionId());
        region.getGroundItemsSafe().add(floorItem);
        for (Player player : getPlayers()) {
            if (player == null || !player.hasStarted() || player.hasFinished() || !player.getMapRegionsIds().contains(regionId))
                continue;
            player.getPackets().sendGroundItem(floorItem);
        }
    }

    /*
     * type 0 - gold if not tradeable
     * type 1 - gold if destroyable
     * type 2 - no gold
     */
    public static final FloorItem addGroundItem(final Item item, final WorldTile tile, final Player owner, boolean invisible, long hiddenSeconds/*
     * default
     * 3
     * minutes






     */, int type, final int publicTime) {

        final FloorItem floorItem = new FloorItem(item, tile, owner, owner != null, invisible);
        if (type != 2) {

            if ((type == 0 && !ItemConstants.isTradeable(item)) || type == 1 && ItemConstants.isDestroy(item)) {
                int price = item.getDefinitions().getValue();
                if (price <= 1)
                    return null;
                floorItem.setId(995);
                floorItem.setAmount(price);
            }
        }
        final Region region = getRegion(tile.getRegionId());

        if(region.getRegionId() == 23961 || (owner != null && owner.tournamentResetRequired() && !(owner.getControlerManager().getControler() instanceof LastManStandingGame))) {
            // don't drop items in tournament
            return null;
        }
        region.getGroundItemsSafe().add(floorItem);
        if (invisible) {
            if (owner != null)
                owner.getPackets().sendGroundItem(floorItem);
            // becomes visible after x time
            if (hiddenSeconds != -1) {
                GameExecutorManager.slowExecutor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            turnPublic(floorItem, publicTime);
                        } catch (Throwable e) {
                            Logger.handle(e);
                        }
                    }
                }, hiddenSeconds, TimeUnit.SECONDS);
            }
        } else {
            // visible
            int regionId = tile.getRegionId();
            for (Player player : getPlayers()) {
                if (player == null || !player.hasStarted() || player.hasFinished() || !player.getMapRegionsIds().contains(regionId))
                    continue;
                player.getPackets().sendGroundItem(floorItem);
            }
            // disapears after this time
            if (publicTime != -1)
                removeGroundItem(floorItem, publicTime);
        }
        return floorItem;
    }

    public static final void updateGroundItem(Item item, final WorldTile tile, final Player owner) {
        final FloorItem floorItem = World.getRegion(tile.getRegionId()).getGroundItem(item.getId(), tile, owner);
        if (floorItem == null) {
            addGroundItem(item, tile, owner, true, 360);
            return;
        }
        floorItem.setAmount(floorItem.getAmount() + item.getAmount());
        owner.getPackets().sendRemoveGroundItem(floorItem);
        owner.getPackets().sendGroundItem(floorItem);

    }

    private static final void removeGroundItem(final FloorItem floorItem, long publicTime) {
        GameExecutorManager.slowExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    int regionId = floorItem.getTile().getRegionId();
                    Region region = getRegion(regionId);
                    if (!region.getGroundItemsSafe().contains(floorItem))
                        return;
                    region.getGroundItemsSafe().remove(floorItem);
                    for (Player player : World.getPlayers()) {
                        if (player == null || !player.hasStarted() || player.hasFinished() || !player.getMapRegionsIds().contains(regionId))
                            continue;
                        player.getPackets().sendRemoveGroundItem(floorItem);
                    }
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }
        }, publicTime, TimeUnit.SECONDS);
    }

    public static final boolean removeGroundItem(Player player, FloorItem floorItem) {
        return removeGroundItem(player, floorItem, true);
    }

    /*
     * used for dung
     */
    public static final boolean removeGroundItem(final FloorItem floorItem) {
        int regionId = floorItem.getTile().getRegionId();
        Region region = getRegion(regionId);
        if (!region.getGroundItemsSafe().contains(floorItem))
            return false;
        region.getGroundItemsSafe().remove(floorItem);
        for (Player player : World.getPlayers()) {
            if (player == null || !player.hasStarted() || player.hasFinished() || !player.getMapRegionsIds().contains(regionId))
                continue;
            player.getPackets().sendRemoveGroundItem(floorItem);
        }
        return true;
    }

    public static final int getGroundStackValue(Player player, WorldTile tile) {
    	int value = 0;
        int regionId = tile.getRegionId();
        Region region = getRegion(regionId);
        for (FloorItem item : region.getGroundItemsSafe()) {
        	if (item.getTile().matches(tile) && (!item.isInvisible() || (item.getOwner() != null  && item.getOwner().equalsIgnoreCase(player.getUsername())))) 
        		value += GrandExchange.getPrice(item.getId())*item.getAmount();
        }
		return value;
    }
    public static final FloorItem getFloorItem(Player player, WorldTile tile) {
        int regionId = tile.getRegionId();
        Region region = getRegion(regionId);
        for (FloorItem item : region.getGroundItemsSafe()) {
        	if (item.getTile().matches(tile) && (!item.isInvisible() || (item.getOwner() != null  && item.getOwner().equalsIgnoreCase(player.getUsername())))) 
        		return item;
        }
		return null;
    }
    
    public static final boolean removeGroundItem(Player player, final FloorItem floorItem, boolean add) {
        int regionId = floorItem.getTile().getRegionId();
        Region region = getRegion(regionId);
        if (!region.getGroundItemsSafe().contains(floorItem))
            return false;
        if (add && (
        		!(floorItem.getId() == 995 && !player.isCanPvp() && (player.getMoneyPouch().getCoinsAmount() + floorItem.getAmount()) > 0)
        		&&
        		player.getInventory().getFreeSlots() == 0 && (!floorItem.getDefinitions().isStackable() || !player.getInventory().containsItem(floorItem.getId(), 1)))) {
        	
        	player.getPackets().sendGameMessage("Not enough space in your inventory.");
            return false;
        }
        region.getGroundItemsSafe().remove(floorItem);
        if (add) {
            if (player.isCanPvp())
                player.getInventory().addItem(new Item(floorItem.getId(), floorItem.getAmount()));
            else
                player.getInventory().addItemMoneyPouch(new Item(floorItem.getId(), floorItem.getAmount()));
        }
        if (floorItem.isInvisible()) {
            player.getPackets().sendRemoveGroundItem(floorItem);
            return true;
        } else {
            for (Player p2 : World.getPlayers()) {
                if (p2 == null || !p2.hasStarted() || p2.hasFinished() || !p2.getMapRegionsIds().contains(regionId))
                    continue;
                p2.getPackets().sendRemoveGroundItem(floorItem);
            }
            if (floorItem.isForever()) {
                GameExecutorManager.slowExecutor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            addGroundItemForever(floorItem, floorItem.getTile());
                        } catch (Throwable e) {
                            Logger.handle(e);
                        }
                    }
                }, 10, TimeUnit.SECONDS);
            }
            return true;
        }
    }

    public static final void sendObjectAnimation(WorldObject object, Animation animation) {
        sendObjectAnimation(null, object, animation);
    }

    public static final void sendObjectAnimation(Entity creator, WorldObject object, Animation animation) {
        if (object == null)
            return;
        if (creator == null) {
            for (Player player : World.getPlayers()) {
                if (player == null || !player.hasStarted() || player.hasFinished() || !player.withinDistance(object, player.hasLargeSceneView() ? 64 : 14))
                    continue;
                player.getPackets().sendObjectAnimation(object, animation);
            }
        } else {
            for (int regionId : creator.getMapRegionsIds()) {
                List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
                if (playersIndexes == null)
                    continue;
                for (Integer playerIndex : playersIndexes) {
                    Player player = players.get(playerIndex);
                    if (player == null || !player.hasStarted() || player.hasFinished() || !player.withinDistance(object))
                        continue;
                    player.getPackets().sendObjectAnimation(object, animation);
                }
            }
        }
    }

    public static List<Entity> getNearbyPlayers(Entity target, boolean checkFamiliars) {
        return getNearbyPlayers(target, checkFamiliars, 14);
    }
    public static List<Entity> getNearbyPlayers(Entity target, boolean checkFamiliars, int dist) {
        List<Entity> entities = new LinkedList<Entity>();
        for (int regionId : target.getMapRegionsIds()) {
            List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
            if (playersIndexes == null)
                continue;
            for (Integer playerIndex : playersIndexes) {
                Player player = players.get(playerIndex);
                if (player == null || !player.hasStarted() || player.isDead() || player.hasFinished() || player.getAppearence().isHidden() || !player.withinDistance(target, dist))
                    continue;
                entities.add(player);
                if (checkFamiliars) {
                    Familiar familiar = player.getFamiliar();
                    if (familiar == null || familiar.isDead() || familiar.isFinished() || !familiar.isAtMultiArea())
                        continue;
                    entities.add(familiar);
                }
            }
        }
        return entities;
    }
    public static List<Player> getLocalPlayers(Entity target) {
        List<Player> entities = new LinkedList<Player>();
        for (int regionId : target.getMapRegionsIds()) {
            List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
            if (playersIndexes == null)
                continue;
            for (Integer playerIndex : playersIndexes) {
                Player player = players.get(playerIndex);
                if (player == null || !player.hasStarted() || player.isDead() || player.hasFinished() || player.getAppearence().isHidden() || !player.withinDistance(target))
                    continue;
                entities.add(player);
            }
        }
        return entities;
    }

    public static final void sendGraphics(int gfx, WorldTile tile) {
        sendGraphics(null, new Graphics(gfx), tile);
    }

    public static final void sendGraphics(Entity creator, Graphics graphics, WorldTile tile) {
        if (creator == null) {
            for (Player player : World.getPlayers()) {
                if (player == null || !player.hasStarted() || player.hasFinished() || !player.withinDistance(tile, player.hasLargeSceneView() ? 64 : 14))
                    continue;
                player.getPackets().sendGraphics(graphics, tile);
            }
        } else {
            for (int regionId : creator.getMapRegionsIds()) {
                List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
                if (playersIndexes == null)
                    continue;
                for (Integer playerIndex : playersIndexes) {
                    Player player = players.get(playerIndex);
                    if (player == null || !player.hasStarted() || player.hasFinished() || !player.withinDistance(tile, player.hasLargeSceneView() ? 64 : 14))
                        continue;
                    player.getPackets().sendGraphics(graphics, tile);
                }
            }
        }
    }

    public static final int sendProjectile(Entity shooter, WorldTile startTile, WorldTile receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
        Projectile projectile = new Projectile(startTile, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset);
        for (int regionId : shooter.getMapRegionsIds()) {
            List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
            if (playersIndexes == null)
                continue;
            for (Integer playerIndex : playersIndexes) {
                Player player = players.get(playerIndex);
                if (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter,  player.hasLargeSceneView() ? 64 : 14) && !player.withinDistance(receiver,  player.hasLargeSceneView() ? 64 : 14)))
                    continue;
                player.addProjectileToQueue(projectile);
                //player.getPackets().sendProjectile(null, startTile, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, 0);
            }
        }
        return Utils.getProjectileTime(shooter, receiver, startHeight, endHeight, speed / 10, delay, curve, startDistanceOffset);
    }

    public static final int sendProjectile(WorldTile shooter, Entity receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
        Projectile projectile = new Projectile(shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset);
        for (int regionId : receiver.getMapRegionsIds()) {
            List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
            if (playersIndexes == null)
                continue;
            for (Integer playerIndex : playersIndexes) {
                Player player = players.get(playerIndex);
                if (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver,  player.hasLargeSceneView() ? 64 : 14)))
                    continue;
                player.addProjectileToQueue(projectile);
                //player.getPackets().sendProjectile(receiver, shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, 0);
            }
        }
        return Utils.getProjectileTime(shooter, receiver, startHeight, endHeight, speed / 10, delay, curve, startDistanceOffset);
    }

    public static final int sendProjectile(Entity shooter, WorldTile receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
        Projectile projectile = new Projectile(shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset);
        for (int regionId : shooter.getMapRegionsIds()) {
            List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
            if (playersIndexes == null)
                continue;
            for (Integer playerIndex : playersIndexes) {
                Player player = players.get(playerIndex);
                if (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter, player.hasLargeSceneView() ? 64 : 14) && !player.withinDistance(receiver, player.hasLargeSceneView() ? 64 : 14)))
                    continue;
                player.addProjectileToQueue(projectile);
                //	player.getPackets().sendProjectile(null, shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, shooter.getSize());
            }
        }
        return Utils.getProjectileTime(shooter, receiver, startHeight, endHeight, speed / 10, delay, curve, startDistanceOffset);
    }

    public static final int sendProjectile(Entity shooter, Entity receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
        return sendProjectile(shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, true);
    }

    public static final int raidProjectile(ChambersOfXeric raid, Entity shooter, Entity receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
        Projectile projectile = new Projectile(shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset);
        raid.getTeam().stream().filter(Objects::nonNull).forEach(player -> {
            boolean outOfViewport = (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter, player.hasLargeSceneView() ? 64 : 14) && !player.withinDistance(receiver, player.hasLargeSceneView() ? 64 : 14)));
            if(!outOfViewport)
                player.addProjectileToQueue(projectile);
        });

        return Utils.getProjectileTime(shooter, receiver, startHeight, endHeight, speed / 10, delay, curve, startDistanceOffset, true);
    }

    public static final int sendProjectile(Entity shooter, Entity receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset, boolean old) {
        int size = shooter.getSize();
        Projectile projectile = new Projectile(shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset);
        for (int regionId : shooter.getMapRegionsIds()) {
            List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
            if (playersIndexes == null)
                continue;
            for (Integer playerIndex : playersIndexes) {
                Player player = players.get(playerIndex);
                if (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter, player.hasLargeSceneView() ? 64 : 14) && !player.withinDistance(receiver, player.hasLargeSceneView() ? 64 : 14)))
                    continue;
                player.addProjectileToQueue(projectile);
                //	player.getPackets().sendProjectile(receiver, shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, size);
            }
        }
        return Utils.getProjectileTime(shooter, receiver, startHeight, endHeight, speed / 10, delay, curve, startDistanceOffset, true);
    }

    public static final boolean isMultiArea(WorldTile tile) {
        int destX = tile.getX();
        int destY = tile.getY();
        int regionId = tile.getRegionId(); // try to avoid using it unless area
        // is uses the whole region and
        // nothing else
        return (destX >= 3462 && destX <= 3511 && destY >= 9481 && destY <= 9521 && tile.getPlane() == 0) // kalphite
                // queen
                // lair

                || (destX >= 3806 && destX <= 3817 && destY >= 2840 && destY <= 2848) //barrelchest

                || (destX >= 4540 && destX <= 4799 && destY >= 5052 && destY <= 5183 && tile.getPlane() == 0) // thzaar
                // city
                || (destX >= 1721 && destX <= 1791 && destY >= 5123 && destY <= 5249) // mole
                || (destX >= 3029 && destX <= 3374 && destY >= 3759 && destY <= 3903)// wild
                || (destX >= 2250 && destX <= 2280 && destY >= 4670 && destY <= 4720) || (destX >= 3198 && destX <= 3380 && destY >= 3904 && destY <= 3970) || (destX >= 3191 && destX <= 3326 && destY >= 3510 && destY <= 3759) || (destX >= 2987 && destX <= 3006 && destY >= 3912 && destY <= 3937) || (destX >= 2245 && destX <= 2295 && destY >= 4675 && destY <= 4720) || (destX >= 3070 && destX <= 3290 && destY >= 9821 && destY <= 10003) || (destX >= 3006 && destX <= 3071 && destY >= 3602 && destY <= 3710) || (destX >= 3134 && destX <= 3192 && destY >= 3519 && destY <= 3646) || (destX >= 2815 && destX <= 2966 && destY >= 5240 && destY <= 5375)// wild
                || (destX >= 2840 && destX <= 2950 && destY >= 5188 && destY <= 5230) // godwars
                || (destX >= 3547 && destX <= 3555 && destY >= 9690 && destY <= 9699) // zaros
                || (destX >= 1490 && destX <= 1515 && destY >= 4696 && destY <= 4714) // chaos dwarf battlefield
                // godwars
                || KingBlackDragon.atKBD(tile) // King Black Dragon lair
                || TormentedDemon.atTD(tile) // Tormented demon's area
                || (destX >= 2970 && destX <= 3000 && destY >= 4365 && destY <= 4400)// corp
                || (destX >= 3195 && destX <= 3327 && destY >= 3520 && destY <= 3970 || (destX >= 2376 && 5127 >= destY && destX <= 2422 && 5168 <= destY)) || (destX >= 2374 && destY >= 5129 && destX <= 2424 && destY <= 5168) // pits
                || (destX >= 2622 && destY >= 5696 && destX <= 2573 && destY <= 5752) // torms
                || (destX >= 2368 && destY >= 3072 && destX <= 2431 && destY <= 3135) // castlewars
                // out
                || (destX >= 2365 && destY >= 9470 && destX <= 2436 && destY <= 9532) // castlewars
                || (destX >= 2948 && destY >= 5537 && destX <= 3071 && destY <= 5631) // Risk
                // ffa.
                || (destX >= 2756 && destY >= 5537 && destX <= 2879 && destY <= 5631) // Safe
                // ffa
                || (tile.getX() >= 3011 && tile.getX() <= 3132 && tile.getY() >= 10052 && tile.getY() <= 10175 && (tile.getY() >= 10066 || tile.getX() >= 3094)) // fortihrny																		 // dungeon
                //workshop
                || (destX >= 2691 && destX <= 2743 && destY >= 9863 && destY <= 9914)
                // bandit camp
                || (destX >= 3155 && destX <= 3191 && destY >= 2964 && destY <= 2993)
                // strongholf of security first floor
                || (destX >= 1853 && destX <= 1919 && destY >= 5184 && destY <= 5250)
                // barbarian vilage
                || (destX >= 3066 && destX <= 3101 && destY >= 3403 && destY <= 3464)
                // abbys dimension
                || (destX >= 3002 && destX <= 3066 && destY >= 4804 && destY <= 4866)
                // alkarid palace guards
                || (destX >= 3281 && destX <= 3305 && destY >= 3148 && destY <= 3177) || regionId == 10140 // light
                // house
                // tair lair terror dogs
                || (destX >= 3134 && destX <= 3164 && destY >= 4640 && destY <= 4669)
                // trolls
                || (destX >= 2815 && destX <= 2949 && destY >= 3576 && destY <= 3727)
                // godwars dungeon entrance
                || (destX >= 2901 && destX <= 2938 && destY >= 3720 && destY <= 3756)
                // choas tunnels
                || (destX >= 3136 && destX <= 3327 && destY >= 5443 && destY <= 5571)
                // poision waste dungeon
                || (destX >= 1986 && destX <= 2045 && destY >= 4162 && destY <= 4286) || regionId == 16729 //glacors
                //falador east multi
                || (destX >= 2936 && destY >= 3361 && destX <= 2981 && destY <= 3410)
                //falador east multi
                || (destX >= 2936 && destY >= 3361 && destX <= 2981 && destY <= 3410)
                //ape toll
                || (destX >= 2685 && destX <= 2820 && destY >= 2683 && destY <= 2813)
                //waterbird island
                || regionId == 9886 || regionId == 10142 || regionId == 11589
                || regionId == 7236 || regionId == 7492 || regionId == 7748
                || regionId == 12961 //scorpia pit
                || regionId == 12958 //wild godwars
                || regionId == 9619 || regionId == 9363 //smoke devils
                || regionId == 9116 //kraken cove
                || regionId == 6966 || regionId == 7479 //sand crabs
                || regionId == 6198 || regionId == 6454 //arround wc guild
                || regionId == 6457 //home
                || regionId == 13138
                || regionId == 10061 || regionId == 10317
                || regionId == 8755 || regionId == 8756 || regionId == 9012 || regionId == 9011
                || regionId == 14161 //phoenix lair
                || regionId == 11414 // elvarg
                || regionId == 11419 //ice queen lair
                || regionId == 10808 //hati
                || regionId == 9551 //world boss new
                || regionId == 6201 || regionId == 5945 || regionId == 5689//shaman
                || regionId == 14650 || regionId == 14651 || regionId == 14652 || regionId == 14906 || regionId == 14907 || regionId == 14908 || regionId == 15162 || regionId == 15163 || regionId == 15164 //fossil island
                || regionId == 12106 || regionId == 11850 || regionId == 11851 || regionId == 12362 || regionId == 12363
                || ((regionId == 14932 || regionId == 15188) && !(destX >= 3752 && destY >= 5382 && destX <= 3787 && destY <= 5411))

                || ((regionId == 6557 || regionId == 6556 || regionId == 6813 || regionId == 6812)
                && !(destX >= 1607 && destX <= 1637 && destY >= 10068 && destY <= 10106))
                //	|| (destX >= 2939 && destY >= 10121 && destX <= 2554 && destY <= 10142)
                // event arena
                || EventArena.isAtMultiArena(destX, destY)
                || (destX >= 3705 && destX <= 3716 && destY >= 5561 && destY <= 5571) //vip dummies
                || (destX >= 3741 && destX <= 3759 && destY >= 5516 && destY <= 5534) //vip green drags
                || regionId == 9891 || /*regionId == 9890 ||*/ regionId == 9635/* || regionId == 9634*/ //jormungand bridge
                || regionId == 16209 || regionId == 16465//halloween event
                || regionId == 10841//xmas event
                || regionId == 12126//zalcano
                || regionId == 9535//wb2
                || regionId == 13367//mimic
                || regionId == 11807//new onyx map
                ;
        // in

        // multi
    }


    public static final boolean isPvpArea(WorldTile tile) {
        return Wilderness.isAtWild(tile);
    }

    public static void sendWorldMessage(String message, boolean forStaff) {
        for (Player p : World.getPlayers()) {
            if (p == null || !p.isRunning() || p.isYellOff() || (forStaff && p.getRights() == 0) || p.getInterfaceManager().containsReplacedChatBoxInter())
                continue;
            p.getPackets().sendGameMessage(message);
        }
    }

    public static final int WORLD_NEWS = 0, SERVER_NEWS = 1, FRIEND_NEWS = 2, GAME_NEWS = 3;

    public static void sendNews(String message, int type) {
        sendNews(null, message, type); //dont use type 2(FRIEND_NEWS) with this one
    }

    /*
     * 0 - all worlds
     * 1 - just this world
     * 2 - friend
     * 3 - game news
     */
    public static void sendNews(Player from, String message, int type) {
        String m = "<shad=000>News: " + message + "</shad></col>";
        if (type == 0 || type == 4 || type == 5)
            m = "<img=7><col=D80000>" + m;
        else if (type == 1)
            m = "<img=6><col=ff8c38>" + m;
        else if (type == 2)
            m = "<img=5><col=45b247>" + m;
        else if (type == 3)
            m = "<img=7><col=FFFF00>" + m;

        for (Player p : World.getPlayers()) {
            if (p == null || !p.isRunning() || p.getInterfaceManager().containsReplacedChatBoxInter()/* || (type == 2 && p != from && !p.getFriendsIgnores().isFriend(from.getDisplayName()))*/)
                continue;
            p.getPackets().sendGameMessage(m, type == 4 || type == 5);
        }
        if (type != 4)
            Bot.sendMessage(Bot.INGAME_CHANNEL, "News: " + message);
    }

    public static void sendIgnoreableWorldMessage(Player sender, String message, boolean forStaff) {
        for (Player p : World.getPlayers()) {
            if (p == null || !p.isRunning() || p.isYellOff() || (forStaff && p.getRights() == 0 && !p.isSupporter()) || p.getFriendsIgnores().isIgnore(sender.getDisplayName()) || p.getInterfaceManager().containsReplacedChatBoxInter())
                continue;
            p.getPackets().sendGameMessage(message);
        }
        Bot.sendMessage(Bot.INGAME_CHANNEL, "Yell: " + message);
    }

    @SuppressWarnings("deprecation")
    public static final int sendProjectile(WorldObject object, WorldTile startTile, WorldTile endTile, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startOffset) {
        for (Player pl : getPlayers()) {
            if (pl == null || !pl.withinDistance(object, pl.hasLargeSceneView() ? 64 : 20))
                continue;
            pl.getPackets().sendProjectile(null, startTile, endTile, gfxId, startHeight, endHeight, speed, delay, curve, startOffset, 1);
        }

        return Utils.getProjectileTime(startTile, endTile, startHeight, endHeight, speed / 10, delay, curve, startOffset);
    }
    public static final int sendProjectile(WorldTile startTile, WorldTile endTile, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startOffset) {
        for (Player pl : getPlayers()) {
            pl.getPackets().sendProjectile(null, startTile, endTile, gfxId, startHeight, endHeight, speed, delay, curve, startOffset, 1);
        }

        return Utils.getProjectileTime(startTile, endTile, startHeight, endHeight, speed / 10, delay, curve, startOffset);
    }
    public static void executeAfterLoadRegion(final int regionId, final Runnable event) {
        executeAfterLoadRegion(regionId, 0, event);
    }

    public static void executeAfterLoadRegion(final int regionId, long startTime, final Runnable event) {
        executeAfterLoadRegion(regionId, startTime, 10000, event);
    }


    public static void executeAfterLoadRegion(final int fromRegionX, final int fromRegionY, final int toRegionX, final int toRegionY, long startTime, final long expireTime, final Runnable event) {
        final long start = Utils.currentTimeMillis();
        for (int x = fromRegionX; x <= toRegionX; x++) {
            for (int y = fromRegionY; y <= toRegionY; y++) {
                int regionId = MapUtils.encode(Structure.REGION, x, y);
                World.getRegion(regionId, true); //forces check load if not loaded
            }
        }
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    for (int x = fromRegionX; x <= toRegionX; x++) {
                        for (int y = fromRegionY; y <= toRegionY; y++) {
                            int regionId = MapUtils.encode(Structure.REGION, x, y);
                            if (!World.isRegionLoaded(regionId) && Utils.currentTimeMillis() - start < expireTime)
                                return;
                        }
                    }
                    event.run();
                    cancel();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        }, startTime, 600);
    }

    /*
     *TODO make this use code from above to save lines lo, they do same
     */
    public static void executeAfterLoadRegion(final int regionId, long startTime, final long expireTime, final Runnable event) {
        final long start = Utils.currentTimeMillis();
        World.getRegion(regionId, true); //forces check load if not loaded
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    if (!World.isRegionLoaded(regionId) && Utils.currentTimeMillis() - start < expireTime)
                        return;
                    event.run();
                    cancel();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        }, startTime, 600);
    }

    public static boolean isWeekend() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    public static void setBotsTask() {
    	if (!Settings.HOSTED)
    		return;
        int fake = getPlayerCount();
        int real = fake - BOTS.size();
        int goal = (int) (real * BOT_MULT);
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    checkBots();
                    int fake = getPlayerCount();
                    int real = fake - BOTS.size();
                    int goal = (int) (real * BOT_MULT);
                    if (fake < goal)
                        addBot();
                    else if (fake > goal)
                        removeBot();
                    setBotsTask();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        }, 5000 + Utils.random(goal - fake > 10 ? 5000 : 120000));
    }

    public static List<Player> BOTS = new ArrayList<Player>();
    public static List<String> oldBotNames = new ArrayList<String>();
    public static String BOT_FC = "help";
    public static double BOT_MULT = 1.9;

    public static WorldTile getTrainSpot() {
        if (true == true || Utils.random(100) < 70) //30% chance
            return new WorldTile(5000, 5000, 0);
        List<NPC> npcs = new ArrayList<NPC>();
        a:
        for (NPC npc : World.getNPCs()) {
            if (((npc.getId() >= 1265 && npc.getId() <= 1267) || npc.getId() == 25935 || npc.getId() == 25936
                    || npc.getId() == 27799 || npc.getId() == 27800)
                    && !npc.isUnderCombat()
                    && (npc.getRegionId() == 10553 || npc.getRegionId() == 10554 || npc.getRegionId() == 7479
                    || npc.getRegionId() == 7223
                    || npc.getRegionId() == 14908)) {
                for (Player bot : BOTS)
                    if (bot.withinDistance(npc.getRespawnTile(), 2))
                        continue a;
                npcs.add(npc);
            }
        }
        if (npcs.isEmpty())
            return new WorldTile(5000, 5000, 0);
        NPC npc = npcs.get(Utils.random(npcs.size()));
        for (int i = 0; i < 10; i++) {
            WorldTile tile = npc.getRespawnTile().transform(Utils.random(3) - 1, Utils.random(3) - 1, npc.getPlane());
            if (World.isFloorFree(tile.getPlane(), tile.getX(), tile.getY()))
                return tile;
        }
        return npc.getRespawnTile();

    }

    public static String getBotName() {
        File[] files = new File("data/login/account/").listFiles();
        int tries = 0;
        while (tries++ < 10000) {
            String name = files[Utils.random(files.length)].getName().replace(".ser", "");
            if (World.getPlayer(Utils.formatPlayerNameForProtocol(name)) != null)
                continue;
            if (name.length() <= 4 || name.length() > 12)
                continue;
            name = name.substring(0, 4 + Utils.random(name.length() - 4));
			/* for (int i = 0; i < Utils.random(2); i++)
				 name += Utils.random(9);*/
            if (Utils.random(2) == 0) {
                Random r = new Random();
                char c = (char) (r.nextInt(26) + 'a');
                name = c + name.substring(1);
            }
            name = Utils.formatPlayerNameForProtocol(name);
            if (Character.isDigit(name.charAt(0)) || Utils.invalidAccountName(name) || World.getPlayer(name) != null || new File("data/login/account/" + name + ".ser").exists())
                continue;
            return name;
        }
        return null;
    }

    private static void checkBots() {
        for (Player bot : new ArrayList<Player>(BOTS))
            if (bot.hasFinished() || !World.getPlayers().contains(bot))
                removeBot(bot);
    }

    public static void removeBot() {
        if (BOTS.isEmpty())
            return;
        removeBot(BOTS.get(Utils.random(BOTS.size())));
    }

    public static void removeBot(Player bot) {
        bot.disconnect(true, false);
        bot.realFinish();
        BOTS.remove(bot);
        for (Player player : World.getPlayers())
            if (player.hasStarted() && !player.hasFinished() && player.getFriendsIgnores().isFriend(bot.getDisplayName())) {
                player.getFriendsIgnores().beginFriendsUpdate();
                player.getFriendsIgnores().updateFriend(true, bot.getDisplayName(), null, 0, 0, null);
                player.getFriendsIgnores().endFriendsUpdate();
            }
        FriendsChat.detach(bot);
        LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendsChatRefreshRequest(Utils.formatPlayerNameForDisplay(BOT_FC)).trim());
    }

    //0 fake, 1 login, 2 logout
    public static void addBot() {
	/*-	int real = lobbyPlayers.size() + players.size();
		int expected = real * 3;//(int) (real * 1.5);
		if (type == 1)
			playerCount++; //increase fake count by 1 when someone logs in
		else if (type == 2)
			playerCount = Math.max(expected, playerCount-1); //dont let it drop under real amt
		else {
			if (playerCount < expected)
				playerCount++;
			else if (playerCount > expected)
				playerCount--;
		}*/
        String username = getBotName();
        if (username == null)
            return;
        Player bot = new Player();
        String displayname = Utils.formatPlayerNameForDisplay(username);
        int gameMode = Utils.random(3);


        bot.init(new Session(null), false, username, displayname, "00-00-00-00-00", "", 0, gameMode == 0 ? Player.NORMAL : gameMode == 1 ? Player.IRONMAN : Player.DEADMAN, 0, false, 0, false, false, false, false, 0, 0, 742, 503, null, new IsaacKeyPair(new int[4]));
        bot.getSession().setDecoder(3, bot);
        bot.getSession().setEncoder(2, bot);
        bot.getControlerManager().removeControlerWithoutCheck();

        if (Utils.random(7) == 0)
            bot.getAppearence().female();

        int weap = Utils.random(5);
        bot.getEquipment().getItems().set(Equipment.SLOT_WEAPON, new Item(weap == 4 ? 841 : weap == 3 ? 4151 : weap == 2 ? 4587 : weap == 1 ? 1333 : 1323));
        bot.getEquipment().getItems().set(Equipment.SLOT_ARROWS, new Item(884, 10000));
        int gear = weap == 4 && Utils.random(3) != 0 ? 20 : Utils.random(4);
        if (!((gear == 3 || gear == 20) && Utils.random(2) == 0))
            bot.getEquipment().getItems().set(Equipment.SLOT_HAT, new Item(gear == 1 ? 1163 : 1153));
        bot.getEquipment().getItems().set(Equipment.SLOT_CHEST, new Item(gear == 3 ? 577 : gear == 20 ? 1129 : gear == 1 ? 10564 : 1115));
        bot.getEquipment().getItems().set(Equipment.SLOT_LEGS, new Item(gear == 3 ? 1011 : gear == 20 ? 1079 : gear == 1 ? (bot.getAppearence().isMale() ? 1079 : 1093) : (bot.getAppearence().isMale() ? 1067 : 1081)));

        if (Utils.random(4) != 0)
            bot.getEquipment().getItems().set(Equipment.SLOT_AMULET, new Item(1704));
        if (Utils.random(4) != 0)
            bot.getEquipment().getItems().set(Equipment.SLOT_CAPE, new Item(1052));
        if (Utils.random(4) != 0)
            bot.getEquipment().getItems().set(Equipment.SLOT_HANDS, new Item(7455));
        if (Utils.random(4) != 0)
            bot.getEquipment().getItems().set(Equipment.SLOT_FEET, new Item(3105));
        if (Utils.random(4) != 0 && bot.getEquipment().getItem(Equipment.SLOT_WEAPON).getDefinitions().getEquipType() != 5)
            bot.getEquipment().getItems().set(Equipment.SLOT_SHIELD, new Item(1540));

        bot.getAppearence().generateAppearenceData();

        WorldTile tile = getTrainSpot();
        if (tile.getX() == 5000 && tile.getY() == 5000)
            bot.getAppearence().setHidden(true);
        bot.setNextWorldTile(tile);
        bot.setNextFaceWorldTile(bot.transform(Utils.random(3) - 1, Utils.random(3) - 1, 0));

        int hp = 0;
        Integer level = bot.getEquipment().getItem(Equipment.SLOT_WEAPON).getDefinitions().getWearingSkillRequiriments().get(Skills.ATTACK);
        if (level == null)
            level = 1;
        level = level + Utils.random(5);
        hp = Math.max(level, hp);
        bot.getSkills().set(Skills.ATTACK, level);
        bot.getSkills().setXp(Skills.ATTACK, Skills.getXPForLevel(level));
        level = Utils.random(80) + 20;
        level = level + Utils.random(99 - level);
        hp = Math.max(level, hp);
        bot.getSkills().set(Skills.STRENGTH, level);
        bot.getSkills().setXp(Skills.STRENGTH, Skills.getXPForLevel(level));
        level = bot.getEquipment().getItem(Equipment.SLOT_CHEST).getDefinitions().getWearingSkillRequiriments().get(Skills.DEFENCE);
        if (level == null)
            level = 1;
        level = level + Utils.random(5);
        bot.getSkills().set(Skills.DEFENCE, level);
        bot.getSkills().setXp(Skills.DEFENCE, Skills.getXPForLevel(level));
        bot.getSkills().set(Skills.HITPOINTS, hp);
        bot.getSkills().setXp(Skills.HITPOINTS, Skills.getXPForLevel(hp));

        bot.setTotalOnlineTime(Utils.random(180 * 60 * 1000) + 10 * 60 * 1000);

        bot.getCombatDefinitions().setAttackStyle(Utils.random(3));
        bot.reset();
        bot.start();
        BOTS.add(bot);
        if (!oldBotNames.contains(bot.getUsername()))
            oldBotNames.add(bot.getUsername());

        for (int i = 0; i < 20; i++) {
            if (!FriendsChat.isFull(BOT_FC+" "+(i+1))) {
                FriendsChat.attach(bot, BOT_FC + " " + (i+1));
                break;
            }
        }
        LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder.encodePlayerFriendsChatRefreshRequest(Utils.formatPlayerNameForDisplay(BOT_FC)).trim());
        bot.setClientHasLoadedMapRegion();
    }

    public static int getPlayerCount() {
        return lobbyPlayers.size() + players.size();//(int) ((lobbyPlayers.size() + players.size()) * 1.5); //+ (Settings.HOSTED ? 10 : 0);
    }

    public static int getPlayersOnWildernessCount() {
        int count = 0;
        for (Player player : getPlayers()) {
            if (!player.hasFinished() && player.getControlerManager().getControler() instanceof Wilderness)
                count++;
        }
        return count;
    }

    public static int getIPCount(String ip, String mac) {
        int count = 0;
        for (Player player : World.getPlayers())
            if (player != null && player.getLastGameIp() != null && player.getLastGameIp().equalsIgnoreCase(ip)
                   && player.getLastGameMAC() != null && player.getLastGameMAC().equals(mac))
                count++;
        return count;
    }

    public static int getSkillOfTheDay() {
        return skillOfTheDay;
    }

    public static void setSkillOfTheDay() {
        int skill = -1;
        int tries = 0;
        while ((skill == -1 || (skill >= Skills.ATTACK && skill <= Skills.RANGE) || skill == Skills.MAGIC) & tries++ < 1000)
            skill = Utils.random(Skills.SKILL_NAME.length);
        skillOfTheDay = skill;
    }

    public static int getBossOfTheDay() {
        return bossOfTheDay;
    }

    public static void setBossOfTheDay() {
        int boss = -1;
        int tries = 0;
        while ((boss == -1 || NPCKillLog.BOSS_NAMES[boss].equals("TzTok-Jad")
                || NPCKillLog.BOSS_NAMES[boss].equals("Har-Aken")
                || NPCKillLog.BOSS_NAMES[boss].equals("TzKal-Zuk")
                || NPCKillLog.BOSS_NAMES[boss].equals("Matrix")
                || NPCKillLog.BOSS_NAMES[boss].equals("Bork")
                || NPCKillLog.BOSS_NAMES[boss].startsWith("Enraged ")
        ) & tries++ < 1000)
            boss = Utils.random(NPCKillLog.BOSS_NAMES.length);
        bossOfTheDay = boss;
    }

    public static void addWishingWell(Player player, long time, boolean donated) {
        boolean active = isWishingWellActive();
        wishingWell = Math.max(wishingWell, Utils.currentTimeMillis()) + time;
        if (!active || !donated)
            World.sendNews("Everyone hail " + player.getName() + "! <col=FFD700>Wishing well is active for the next " + (Utils.longFormat(World.getWishingWellRemaining()).toLowerCase() + "! 15% XP Boost & 5% Drop rate Boost!"), WORLD_NEWS);
    }

    public static boolean isWishingWellActive() {
        return wishingWell >= Utils.currentTimeMillis();
    }

    public static long getWishingWellRemaining() {
        return wishingWell - Utils.currentTimeMillis();
    }

    public static WorldObject spawnObject(int id, int x, int y, int z, int rotation, int type) {
        WorldObject obj = new WorldObject(id, rotation, type, new WorldTile(x, y, z));
        spawnObject(obj);
        return obj;
    }

    public static int getAnimTicks(int emote) {
        AnimationDefinitions def = AnimationDefinitions.getAnimationDefinitions(emote);
        if(def != null) return def.getEmoteTime() / 600;
        else return 1;
    }
}
