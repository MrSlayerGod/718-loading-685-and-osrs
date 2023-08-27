package com.rs.game.player.content.seasonalEvents;

import com.rs.Settings;
import com.rs.executor.GameExecutorManager;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.CombatDummy;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.NewPlayerController;
import com.rs.game.player.controllers.pktournament.PkTournamentGame;
import com.rs.utils.DropTable;
import com.rs.utils.DropTable.*;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.TimerTask;

/**
 * @author Simplex
 * created on 2021-03-19
 */
public class Easter2021 {

    private static NPC easterBunny;
    private static long nextSpawnTime;
    public static boolean ENABLED = false;

    public static int BUNNY_2018_ID = 13651, BUNNY_2021_ID = 9687;
    public static int CRACKABLE_EGGS_TO_DROP = 50;

    public static final void init() {
        // hween pet dialog
        if (!ENABLED)
            return;
        for (NPC n : World.getNPCs()) {
            if(n != null && n.getId() == BUNNY_2018_ID) {
                n.finish();
            }
        }

        Easter2021NPC.init();
        // spawn 2018 bunny at home
        World.spawnNPC(BUNNY_2018_ID, new WorldTile(3097, 3502, 0), -1, false, true);
        World.sendNews("<col=00ffff><shad=ffff00>Easter 2021 is here! Eggs will be dropped while playing and a special event will spawn from time to time - check your quest time for the next event time!", 1);
        World.sendNews("<col=00ffff><shad=ffff00>Bring your eggs to the Easter Bunny at home for limited time rewards!", 1);
        setSpawnTask();
    }

    public static boolean isEventActive() {
        return easterBunny != null && !easterBunny.hasFinished() && !easterBunny.isDead();
    }

    public static long getNextSpawnTime() {
        return nextSpawnTime- Utils.currentTimeMillis();
    }

    public static void login(Player player) {
        if (easterBunny != null && !easterBunny.hasFinished() && !(player.getControlerManager().getControler() instanceof NewPlayerController))
            player.getPackets().sendGameMessage("<img=7><col=D80000><img=2>The Easter Bunny has appeared! Head ::home to participate in the event!");
    }

    public static boolean forceSpawn() {
        if(isEventActive())
            return false;
        if (easterBunny != null && !easterBunny.hasFinished())
            easterBunny.finish();
        World.sendNews("<img=7><col=cc33ff><img=2>The Easter Bunny has appeared! Head ::home to participate in the event!", 1);
        for (Player player : World.getPlayers()) {
            if (!player.hasStarted() || player.hasFinished())
                continue;
            player.getInterfaceManager().sendNotification("WARNING", "The Easter Bunny just appeared at home!");
        }
        easterBunny = World.spawnNPC(BUNNY_2021_ID, new WorldTile(3113, 3500, 0), -1, true, true);
        return true;
    }

    private static void setSpawnTask() {
        long timeLeft = easterBunny == null ?
                !Settings.HOSTED ? 0 : Utils.random(60000 * 5) : Utils.random(3600000 * 2, 3600000 * 4);
        nextSpawnTime = Utils.currentTimeMillis() + timeLeft;
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    if(!ENABLED) {
                        // manually disabled
                        cancel();
                        return;
                    }
                    if (easterBunny != null && !easterBunny.hasFinished())
                        easterBunny.finish();
                    World.sendNews("<img=7><col=cc33ff><img=2>The Easter Bunny has appeared! Head ::home to participate in the event!", 1);
                    for (Player player : World.getPlayers()) {
                        if (!player.hasStarted() || player.hasFinished())
                            continue;
                        player.getInterfaceManager().sendNotification("WARNING", "The Easter Bunny just appeared at home!");
                    }
                    easterBunny = World.spawnNPC(BUNNY_2021_ID, new WorldTile(3113, 3500, 0), -1, true, true);
                    setSpawnTask();
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }

        }, timeLeft); //every 6-18h
    }

    public static int EGG_DROP_DMG = 5000;

    public static void checkEggDrop(Player p, int damage, Entity target) {
        if(target instanceof NPC) {
            NPC n = target.asNPC();
            if(n instanceof CombatDummy) {
                return;
            }
        }

        int dmg = (int) p.getTemporaryAttributtes().getOrDefault("EASTER_DMG_COUNTER", 0);
        dmg += damage;
        if(dmg + damage >= EGG_DROP_DMG) {
            dmg -= EGG_DROP_DMG;
            // every 500 hp dealt roll for an easter egg
            if(!Controller.check(p, PkTournamentGame.class) && !PkTournament.inMinigame(p)) {
                if(Utils.rollDie(3, 1)) {
                    p.setLootbeam(World.addGroundItem(new Item(1961), new WorldTile(p), p, true, 60));
                } else {
                    p.sendMessage("<col=ffff00><shad=00ffff><img=1>Happy Easter!");
                    World.addGroundItem(new Item(1961), new WorldTile(p), p, true, 60);
                }
            }
        }
        p.getTemporaryAttributtes().put("EASTER_DMG_COUNTER", dmg);
    }

    public static DropTable CRACKABLE_EGG_TABLE = new DropTable(

        // 1/300 (gear is 3x more common than weps)
        new DropTable.DropCategory("GEAR", 1, true,
                new ItemDrop(25579, 1, 1, 3), // chicken gear
                new ItemDrop(25580, 1, 1, 3), // chicken gear
                new ItemDrop(25581, 1, 1, 3), // chicken gear
                new ItemDrop(25582, 1, 1, 3), // chicken gear
                new ItemDrop(25583, 1, 1, 1), // chicken wep
                new ItemDrop(25584, 1, 1, 1), // chicken wep
                new ItemDrop(25585, 1,1, 1)), // chicken wep

        new DropTable.DropCategory("COMMON", 299,
            new ItemDrop(995, 1000000, 3000000, 20), // Coins
            new ItemDrop(23713, 1,1, 20), // Small XP lamp
            new ItemDrop(23714, 1,1, 10), // Medium XP lamp
            new ItemDrop(23715, 1,1, 3), // Large XP lamp
            new ItemDrop(3140, 1,1, 3), // dragon chainbody
            new ItemDrop(4087, 1,1, 5), // dragon platelegs
            new ItemDrop(11732, 1,1, 7), // dragon boots
            new ItemDrop(1149, 1,1, 15), // draon helm
            new ItemDrop(15259, 1,1, 2),  // dragon pickaxe
            new ItemDrop(6739, 1,1, 2),  // Dragon hatchet
            new ItemDrop(6914, 1,1, 1), // Master Wand
            new ItemDrop(6916, 1,1, 2), // Infinity top
            new ItemDrop(6918, 1,1, 2),// Infinity hat
            new ItemDrop(6920, 1,1, 2), // Infinity boots
            new ItemDrop(6922, 1,1, 2), // Infinity gloves
            new ItemDrop(6924, 1,1, 2), // Infinity bottoms
            new ItemDrop(1037, 1,1, 5), // Bunny ears
            new ItemDrop(24145, 1,1, 5), // Eggsterminator
            new ItemDrop(24149, 1,1, 3), // Egg on face mask
            new ItemDrop(24150, 1,1, 5), // Chocolate egg on face mask
            new ItemDrop(24144, 1,1, 5),  // Peahat
            new ItemDrop(43182, 1,1, 5),  // Bunny feet
            new ItemDrop(43663, 1,1, 5), // Bunny Top
			new ItemDrop(43664, 1,1, 5), // Bunny Legs
            new ItemDrop(11019, 1,1, 5),  // Chicken feet
            new ItemDrop(11020, 1,1, 5),  // Chicken wings
            new ItemDrop(11021, 1,1, 5), // Chicken head
            new ItemDrop(11022, 1,1, 5), // Chicken legs
            new ItemDrop(7927, 1,1, 5) ,// Easter ring
            new ItemDrop(4566, 1,1, 5), // Rubber chicken
            new ItemDrop(51214, 1,1, 5), // Easter Egg Helm
            new ItemDrop(52351, 1,1, 5), // Eggshell platebody
            new ItemDrop(52353, 1,1, 5), // Eggshell platelegs
            new ItemDrop(53448, 1,1, 5), // Bunnyman mask
            new ItemDrop(4708, 1,1, 2), // Barrows Start
            new ItemDrop(4710, 1,1, 2), //
            new ItemDrop(4712, 1,1, 2),  //
            new ItemDrop(4714, 1,1, 2),  //
            new ItemDrop(4716, 1,1, 2), //
            new ItemDrop(4718, 1,1, 2), //
            new ItemDrop(4720, 1,1, 2), //
            new ItemDrop(4722, 1,1, 2), //
            new ItemDrop(4724, 1,1, 2),  //
            new ItemDrop(4726, 1,1, 2),  //
            new ItemDrop(4728, 1,1, 2), //
            new ItemDrop(4730, 1,1, 2), //
            new ItemDrop(4732, 1,1, 2) ,//
            new ItemDrop(4734, 1,1, 2), //
            new ItemDrop(4736, 1,1, 2),  //
            new ItemDrop(4738, 1,1, 2),  //
            new ItemDrop(4745, 1,1, 2), //
            new ItemDrop(4747, 1,1, 2), //
            new ItemDrop(4749, 1,1, 2) ,//
            new ItemDrop(4751, 1,1, 2), //
            new ItemDrop(4753, 1,1, 2),  //
            new ItemDrop(4755, 1,1, 2),  //
            new ItemDrop(4757, 1,1, 2), //
            new ItemDrop(4759, 1,1, 2))// Barrows End
    );
}
