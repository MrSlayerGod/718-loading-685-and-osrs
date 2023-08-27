package com.rs.game.player.content;

import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.WorldTile;
import com.rs.game.minigames.*;
import com.rs.game.minigames.lms.LastManStanding;
import com.rs.game.npc.NPC;
import com.rs.game.npc.OSRSDropTables;
import com.rs.game.npc.others.Pet;
import com.rs.game.npc.others.zalcano.Zalcano;
import com.rs.game.npc.worldboss.CallusFrostborne;
import com.rs.game.player.Player;
import com.rs.game.player.SlayerManager;
import com.rs.game.player.actions.runecrafting.SiphionActionNodes;
import com.rs.game.player.actions.thieving.HalloweenStallThieving;
import com.rs.game.player.content.agility.PriffdinasAgility;
import com.rs.game.player.content.box.MysteryBox;
import com.rs.game.player.content.collectionlog.CollectionLog;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.seasonalEvents.DropEvent;
import com.rs.game.player.content.seasonalEvents.Easter2021;
import com.rs.game.player.content.seasonalEvents.HalloBoss;
import com.rs.game.player.content.seasonalEvents.XmasBoss;
import com.rs.game.player.content.teleportation.TeleportationInterface;
import com.rs.game.player.content.track.TrackPC;
import com.rs.game.player.controllers.JadinkoLair;
import com.rs.game.player.controllers.SkeletalHorrorController;
import com.rs.game.player.controllers.TheNightmareInstance;
import com.rs.game.player.dialogues.impl.UpgradeItemOption;
import com.rs.game.player.dialogues.impl.Zahur;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.NPCHandler;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Direction;
import com.rs.utils.NPCSpawns;

/**
 * @author Simplex
 * created on 2021-01-31
 *
 * Used as a routing class to avoid cluttering World class.
 */
public class ContentInitializer {
    public static void init() {
        LastManStanding.init();
        LivingRockCavern.init();
        SiphionActionNodes.init();
        PuroPuro.initPuroImplings();
        WarriorsGuild.init();
        Zalcano.init();
        JadinkoLair.init();
        ShootingStars.init();
        EvilTrees.init();
        WildernessBoss.init();
        VoteWorldBoss.init();
        WorldBosses.init();
        HalloBoss.init();
        Easter2021.init();
        XmasBoss.init();
        LavaFlowMine.init();
        Reaction.init();
        Pet.init();
        Trivia.init();
        DropEvent.init();
        TheNightmareInstance.init();
        CollectionLog.init();
        NPCSpawns.preloadSpawns();
        ChambersOfXeric.init();
        CallusFrostborne.init();
        PriffdinasAgility.init();
        SlayerBox.init();
        MysteryBox.init();
        SlayerManager.init();
        OSRSDropTables.init();
        HalloweenStallThieving.init();
        com.rs.game.player.content.EconomyManager.init();
        UpgradeItemOption.init();
        TeleportationInterface.init();
        MiscObjects.init();
        //SlaughterFieldsControler.load();
        TrackPC.setTask();
        Zahur.init();
        SkeletalHorrorController.init();

        NPCHandler.register(5182, 1, ContentInitializer::followElkoy);
        NPCHandler.register(5182, 2, ContentInitializer::followElkoy);

        ObjectHandler.register(2186, 1, ((player, object) -> {
            player.setDirection(Direction.EAST, true);
            WorldTasksManager.schedule(() -> {
                boolean enter = player.getY() < object.getY();
                player.setNextAnimation(new Animation(12260));
                final WorldTile toTile = new WorldTile(object.getX(), object.getY() + (!enter ? -1 : 0), object.getPlane());
                player.setNextForceMovement(new ForceMovement(player, 0, toTile, 2, ForceMovement.EAST));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        player.anim(-1);
                        player.setNextWorldTile(toTile);
                    }
                }, 1);
            });
        }));
    }

    private static void followElkoy(Player player, NPC npc) {
        final WorldTile to;
        final String str;

        if(npc.getY() > 3161) {
            to = new WorldTile(2515, 3161, 0);
            str = "into";
        } else {
            to = new WorldTile(2503, 3191, 0);
            str = "out of";
        }

        player.sendMessage("Elkoy leads you " + str + " the Tree Gnome Village..");
        player.lock(3);
        final long time = FadingScreen.fade(player);
        FadingScreen.unfade(player, time, () -> player.setNextWorldTile(to));
    }
}
