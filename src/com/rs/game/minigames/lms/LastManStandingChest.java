package com.rs.game.minigames.lms;

import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.DropTable;
import com.rs.utils.Utils;

/**
 * @author Simplex
 * created on 2021-02-01
 */
public class LastManStandingChest {

    public static void init() {


        // LMS chest
        ObjectHandler.register(129069, 1, LastManStandingChest::open);
    }

    private static void open(Player player, WorldObject obj) {

        DropTable dropTable;

        if(player.getInventory().containsOneItem(LastManStanding.BLOODIER_KEY)) {
            player.getInventory().deleteItem(LastManStanding.BLOODIER_KEY, 1);
            dropTable = UPGRADED;
        } else if(player.getInventory().containsOneItem(LastManStanding.BLOODY_KEY)) {
            player.getInventory().deleteItem(LastManStanding.BLOODY_KEY, 1);
            dropTable = Utils.rollPercent(50) ? OFFENSIVE : DEFENSIVE;
        } else {
            player.sendMessage("The chest is locked.");
            return;
        }


        player.sendMessage("You open the chest.");
        player.anim(536);

        obj.updateId(129070);

        WorldTasksManager.schedule(() -> {
            Item drop =  dropTable.roll().get();
            if(drop.getDefinitions().stackable != 0)
                drop.setAmount(100);
            player.sendMessage("Inside the chest you find "+Utils.aOrAn(drop.getName())+ " " + drop.getName() + "!");
            player.getInventory().addItemDrop(drop.getId(), drop.getAmount());

            if(drop.getId() == 11235)
                player.getInventory().addItemDrop(11212, 500);
            if(drop.getId() == 49481)
                player.getInventory().addItemDrop(49484, 500);

            obj.updateId(129069);
        });

    }

    public static final DropTable OFFENSIVE = new DropTable(
            new DropTable.ItemDrop(25037), // Armadyl Crossbow
            new DropTable.ItemDrop(11694), // Armadyl Godsword
            new DropTable.ItemDrop(11235), // Dark Bow MUST ADD 11212 - Dragon arrow TO AUTOBOTS
            new DropTable.ItemDrop(14484), // Dragon Claws
            new DropTable.ItemDrop(51003), // Elder Maul
            new DropTable.ItemDrop(52324), // Ghrazi Rapier
            new DropTable.ItemDrop(4153), // Granite Maul
            new DropTable.ItemDrop(49481), // Heavy Ballista MUST ADD 49484 - Dragon javelin TO AUTOBOTS
            new DropTable.ItemDrop(51295), // Infernal Cape
            new DropTable.ItemDrop(36889), // Mage's Book
            new DropTable.ItemDrop(42002), // Occult Necklace
            new DropTable.ItemDrop(51006), // Kodai Wand
            new DropTable.ItemDrop(41770), // Seers Ring (i)
            new DropTable.ItemDrop(41791) // Staff of the Dead
    );
    public static final DropTable DEFENSIVE = new DropTable(
            new DropTable.ItemDrop(4712), // Ahrim's Robetop
            new DropTable.ItemDrop(4714), // Ahrim's Robeskirt
            new DropTable.ItemDrop(6585), // Amulet of Fury
            new DropTable.ItemDrop(11726), // Bandos Tassets
            new DropTable.ItemDrop(13736), // Blessed Spirit Shield
            new DropTable.ItemDrop(4716), // Dharok's Helm
            new DropTable.ItemDrop(4722), // Dharok's platelegs
            new DropTable.ItemDrop(43235), // Eternal boots
            new DropTable.ItemDrop(4724), // Guthan's helm
            new DropTable.ItemDrop(4736), // Karil's top
            new DropTable.ItemDrop(4745), // Torag's helm
            new DropTable.ItemDrop(4751), // Torag's platelegs
            new DropTable.ItemDrop(4753), // Verac's helm
            new DropTable.ItemDrop(4759) // Verac's plateskirt
    );
    public static final DropTable UPGRADED = new DropTable(
            new DropTable.ItemDrop(13899), // Vesta's longsword
            new DropTable.ItemDrop(13902), // Statius's warhammer
            new DropTable.ItemDrop(13867) // Zuriel's staff
    );
}
