package com.rs.game.player;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.impl.superiorslayer.*;
import com.rs.game.player.content.Slayer;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.utils.DropTable;
import com.rs.utils.Utils;

import static com.rs.utils.DropTable.*;

/**
 * @author Simplex
 * @since Sep 24, 2020
 */
public class SuperiorSlayer {

    public static final int[] SUPERIOR_CREATURES = {
            KingKurask.ID, AbhorrentSpectre.ID, BasiliskSentinel.ID, CaveAbomination.ID,
            ChasmCrawler.ID, ChokeDevil.ID, Cockathrice.ID, ColossalHydra.ID, CrushingHand.ID,
            FlamingPyrelord.ID,  GiantRockslug.ID, GreaterAbyssalDemon.ID, GuardianDrake.ID,
            InsatiableBloodveld.ID, KingKurask.ID, MalevolentMage.ID, MarbleGargoyle.ID,
            MonstrousBasilisk.ID, Nechryarch.ID, NightBeast.ID, NuclearSmokeDevil.ID,
            ScreamingBanshee.ID, SpikedTuroth.ID, VitreousJelly.ID
    };

    public static void dropItems(Player player, NPC npc) {
        ItemDrop drop = superiorDropTable.roll();
        Item item = drop.get();
        WorldTile dropTile = new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane());

        int bones = npc.getId() == MalevolentMage.ID || npc.getId() == ScreamingBanshee.ID ? 592 : 532;
        npc.sendDrop(player, new Drop(bones, 1, 1));

        LuckyPets.checkPet(player, LuckyPets.LuckyPet.SS_PET);

        if(drop.isAnnounceDrop()) {
            World.sendNews("<col=ffff00><shad=0>" + player.getDisplayName() + " received drop: <col=ff981f>" + Utils.getFormattedNumber(item.getAmount()) + " <col=00ffff>x <col=ff981f>" + item.getName(), 1);
            player.setLootbeam(World.addGroundItem(item, dropTile, player, true, 60));
        } else {
            //World.addGroundItem(item, dropTile, player, true, 60);
            npc.sendDrop(player, new Drop(item.getId(), item.getAmount(), item.getAmount()));
        }
    }

    public static DropTable superiorDropTable = new DropTable(
            new DropCategory("Common", 101, false,
                    new ItemDrop(27001),
                    new ItemDrop(13278, 5000, 5000, 1),
                    new ItemDrop(2, 250, 250, 1),
                    new ItemDrop(6694, 50, 100, 1),
                    new ItemDrop(1754, 50, 100, 1),
                    new ItemDrop(208, 25, 100, 1),
                    new ItemDrop(3052, 25, 100, 1),
                    new ItemDrop(3050, 25, 100, 1),
                    new ItemDrop(20269, 50, 100, 1),
                    new ItemDrop(43440, 50, 100, 1),
                    new ItemDrop(384, 100, 100, 1),
                    new ItemDrop(232, 100, 100, 1),
                    new ItemDrop(1622, 50, 100, 1),
                    new ItemDrop(1624, 50, 100, 1),
                    new ItemDrop(1516, 100, 100, 1)),
            new DropCategory("Uncommon", 16, false,
                    new ItemDrop(27002, 1, 1, 1),
                    new ItemDrop(2362, 75, 75, 1),
                    new ItemDrop(5298, 20, 20, 1),
                    new ItemDrop(43442, 50, 50, 1),
                    new ItemDrop(386, 100, 200, 1),
                    new ItemDrop(11212, 500, 1000, 1),
                    new ItemDrop(11230, 500, 1000, 1),
                    new ItemDrop(5303, 20, 20, 1),
                    new ItemDrop(212, 25, 50, 1),
                    new ItemDrop(218, 25, 50, 1),
                    new ItemDrop(2486, 25, 50, 1),
                    new ItemDrop(220, 25, 50, 1),
                    new ItemDrop(1514, 100, 200, 1),
                    new ItemDrop(5295, 20, 20, 1),
                    new ItemDrop(2364, 30, 30, 1),
                    new ItemDrop(5300, 20, 20, 1),
                    new ItemDrop(5296, 20, 20, 1),
                    new ItemDrop(5304, 10, 10, 1),
                    new ItemDrop(1618, 25, 50, 1),
                    new ItemDrop(1620, 25, 50, 1)),

            new DropCategory("Rare", 8, false,
                    new ItemDrop(53184, 10, 1, 1),
                    new ItemDrop(52125, 30, 30, 1),
                    new ItemDrop(990, 50, 50, 1),
                    new ItemDrop(23531, 5, 5, 1),
                    new ItemDrop(23352, 50, 50, 1),
                    new ItemDrop(23400, 50, 50, 1),
                    new ItemDrop(1632, 20, 20, 1)),

            new DropCategory("Very Rare", 1, true,
                    new ItemDrop(6571),
                    new ItemDrop(50724))
    );

    public static int getSuperior(Slayer.SlayerTask task, int npc) {
        if(task == null) return -1;

        switch(task) {
            case BASILISK:
                return MonstrousBasilisk.ID;
            case COCKATRICE:
                return Cockathrice.ID;
            case BANSHEE:
                //if(npc == 27272)
                return ScreamingBanshee.ID;
            //return 27390;
            case ABERRANT_SPECTRE:
                //if(npc == 27279)
                return AbhorrentSpectre.ID;
            //return 27402;
            case ABYSSAL_DEMON:
                return GreaterAbyssalDemon.ID;
            case BLOODVELD:
                //if(npc == 27276)
                return InsatiableBloodveld.ID;
            //return 27397;
            case CAVE_CRAWLER:
                return ChasmCrawler.ID;
            case CAVE_HORROR:
                return CaveAbomination.ID;
            case CRAWLING_HAND:
                return CrushingHand.ID;
            case DUST_DEVIL:
                return ChokeDevil.ID;
            case GARGOYLE:
                return MarbleGargoyle.ID;
            case JELLY:
                //if(npc == 27277)
                return VitreousJelly.ID;
            //return 27399;
            case KURASK:
                return KingKurask.ID;
            case NECHRYAEL:
                return Nechryarch.ID;
            case SMOKE_DEVIL:
                return NuclearSmokeDevil.ID;
            case ROCKSLUG:
                return GiantRockslug.ID;
            case DARK_BEAST:
                return NightBeast.ID;
            case PYREFIEND:
                return FlamingPyrelord.ID;
            default:
                return -1;
        }
    }
}
