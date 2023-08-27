package com.rs.game.npc.others;

import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.MimicFightController;
import com.rs.utils.DropTable;
import com.rs.utils.DropTable.DropCategory;
import com.rs.utils.DropTable.ItemDrop;
import com.rs.utils.Utils;

import java.util.*;

@SuppressWarnings("serial")
/**
 * @author Simplex
 * @since Sep 10, 2020
 */
public class Mimic extends NPC {

    // npcs
    public static final int MIMIC_ID = 28633, WARRIOR_ID = 28635, RANGER_ID = 28636, MAGE_ID = 28637;

    // anims
    public static final Animation CANDY_ATTACK_ANIM = new Animation(28309);

    // gfxs
    public static final Graphics PURP_CANDY_PROJ = new Graphics(6670), GREEN_CANDY_PROJ = new Graphics(6671),
            ORANGE_CANDY_PROJ = new Graphics(6672), CYAN_CANDY_PROJ = new Graphics(6673),
            PURP_CANDY_OPEN = new Graphics(6674), GREEN_CANDY_OPEN = new Graphics(6675),
            RED_CANDY_OPEN = new Graphics(6676), CYAN_CANDY_OPEN = new Graphics(6677);

    // items
    public static final int MIMIC_CASKET = 53184;

    // tiles
    public static WorldTile MIMIC_SPAWN = new WorldTile(3613, 9503, 1),
            TELE_TILE = new WorldTile(3615, 9498, 1);

    private static final int REGION_ID = 14484;

    public NPC[] minions = new NPC[3];

    public int chaseTargetTicks;

    private MimicFightController instance;

    public Mimic(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, true);
        // uninstanced fight
        // can use this for events
        this.setHitpoints(getMaxHitpoints() * 5); // x5 for boss masses
        this.instance = null;
        setup();
    }

    public Mimic(MimicFightController instance) {
        super(MIMIC_ID, instance.getMap().getTile(MIMIC_SPAWN), -1, true, true);
        this.instance = instance;
        setup();
    }

    public void setup() {
        setCapDamage(500);
        setCombatLevel(186);
        setForceMultiArea(true);
        setForceMultiAttacked(true);
        setIntelligentRouteFinder(true);
        setForceTargetDistance(64);
        setForceAgressive(true);
    }

    public static void openCasket(Player player) {
        player.getControlerManager().startControler(new MimicFightController());
    }

    @Override
    public void drop() {
        Player player = getMostDamageReceivedSourcePlayer();
        List<Item> drops = new ArrayList<>();

        // always drops
        drops.add(new Item(995, 1000000 * Utils.random(1, 5)));
        drops.add(new Item(25459, 3));

        for(Item drop : drops)
            //World.addGroundItem(drop, new WorldTile(getCoordFaceX(getSize()), getCoordFaceY(getSize()), getPlane()), player, true, 60);
            sendDrop(player, new Drop(drop.getId(), drop.getAmount(), drop.getAmount()));

        ItemDrop itemDrop = dropGenerator.rollCategory().rollDrop();
        Item item = itemDrop.get();

        //if(itemDrop.isAnnounceDrop())
        //    World.sendNews("<col=ffff00><shad=0>" + player.getDisplayName() + " received drop: <col=ff981f>" + Utils.getFormattedNumber(item.getAmount()) + " <col=00ffff>x <col=ff981f>" + item.getName() + "</col> from the Mimic!", 1);

        sendDrop(player, itemDrop);
        /*if(itemDrop.isAnnounceDrop())
            World.sendNews(player, player.getDisplayName() + " received " + item.getAmount() + " x " + item.getName() + " from the Mimic!", 1);
            player.setLootbeam(World.addGroundItem(item, new WorldTile(getCoordFaceX(1), getCoordFaceY(1), getPlane()), player, true, 60));
        } else {
            World.addGroundItem(item, new WorldTile(getCoordFaceX(getSize()), getCoordFaceY(getSize()), getPlane()), player, true, 60);
        }*/
    }

    public static void testDrops(Player player, int i) {
        ArrayList<Item> drops = new ArrayList<>();

        for(int d = 0; d <= i; d++) {
            ItemDrop roll = dropGenerator.roll();
            Item drop = roll.get();
            boolean found = false;
            int amt = drop.getAmount();
            for(Item dropList : drops) {
                if(dropList.getId() == drop.getId()) {
                    dropList.setAmount(dropList.getAmount()+amt);
                    found = true;
                    break;
                }
            }
            if(!found)
                drops.add(drop);
            player.sendMessage(drop.getName() + '(' + drop.getId() + ')' + "  x  " + drop.getAmount() + " = " + roll.getParent().getName());
        }

        Collections.sort(drops, Comparator.comparingInt(Item::getAmount));

        player.getBank().resetBank();
        for(Item item : drops) {
            player.getBank().addItem(item.getId(), item.getAmount(), false);
        }
        player.getBank().refreshViewingTab();
        player.getBank().refreshTabs();
        player.getBank().openBank();
    }

    @Override
    public void processNPC() {
        if (isDead()) return;
        super.processNPC();

        // mimic can walk over the player to cause damage
        if (chaseTargetTicks-- > 0) {
            Entity target = getCombat().getTarget();
            if (target == null || target.getRegionId() != getRegionId() || target.collides(this)) {
                chaseTargetTicks = 0;
            } else if (target != null) {
                int x = getX() + (int) -Math.signum(getX() - target.getX());
                int y = getY() + (int) -Math.signum(getY() - target.getY());
                addWalkSteps(x, y);
                return;
            }
        }

        // damage players under mimic
        getPossibleTargets().forEach(entity -> {
            if (Utils.collides(entity, this)) {
                entity.applyHit(this, 60);
            }
        });
    }

    @Override
    public void sendDeath(Entity killer) {
        Arrays.stream(minions).filter(Objects::nonNull).forEach(npc -> npc.sendDeath(this));
        minions = new NPC[3];
        super.sendDeath(killer);
    }

    public void spawnMinion(WorldTile candyTile, int index) {
        if(this.isDead() || this.hasFinished())
            return;
        int id = index == 0 ? WARRIOR_ID : index == 1 ? RANGER_ID : MAGE_ID;

        if (minions[index] != null && (!minions[index].isDead() || !minions[index].hasFinished()))
            return;
        NPC minion = World.spawnNPC(id, candyTile, -1, true, true);
        minion.setForceTargetDistance(64);
        minion.setForceAgressive(true);
        minion.setForceMultiArea(true);
        minion.setForceMultiAttacked(true);
        minion.getCombat().setTarget(getCombat().getTarget());
        minion.setHitpoints(minion.getMaxHitpoints());
        minions[index] = minion;
    }

    public static final String VERY_RARE_TABLE = "Very rare";

    private static DropCategory[] categories = {
            new DropCategory("Common", 375,
                    new ItemDrop(9075, 2000, 1),
                    new ItemDrop(533, 150, 1),
                    new ItemDrop(565, 750, 1),
                    new ItemDrop(1778, 2000, 1),
                    new ItemDrop(13278, 10000, 1),
                    new ItemDrop(562, 2500, 1),
                    new ItemDrop(454, 500, 1),
                    new ItemDrop(560, 100, 1),
                    new ItemDrop(11212, 300, 1),
                    new ItemDrop(11230, 300, 1),
                    new ItemDrop(2358, 500, 1),
                    new ItemDrop(445, 500, 1),
                    new ItemDrop(12539, 100, 1),
                    new ItemDrop(208, 50, 1),
                    new ItemDrop(3052, 50, 1),
                    new ItemDrop(2352, 1000, 1),
                    new ItemDrop(441, 1000, 1),
                    new ItemDrop(4698, 200, 1),
                    new ItemDrop(561, 2000, 1),
                    new ItemDrop(8779, 500, 1),
                    new ItemDrop(15332, 3, 1),
                    new ItemDrop(224, 100, 1),
                    new ItemDrop(6686, 100, 1),
                    new ItemDrop(232, 100, 1),
                    new ItemDrop(566, 500, 1),
                    new ItemDrop(3025, 50, 1),
                    new ItemDrop(1622, 20, 1),
                    new ItemDrop(1624, 20, 1),
                    new ItemDrop(25459, 1, 5, 1),
                    new ItemDrop(1516, 750, 1)),

            new DropCategory("Uncommon", 100,
                    new ItemDrop(2362, 100, 1),
                    new ItemDrop(450, 100, 1),
                    new ItemDrop(1712, 1, 1),
                    new ItemDrop(4675, 1, 1),
                    new ItemDrop(2497, 1, 1),
                    new ItemDrop(2503, 1, 1),
                    new ItemDrop(4101, 1, 1),
                    new ItemDrop(4103, 1, 1),
                    new ItemDrop(1752, 20, 1),
                    new ItemDrop(995, 5000000, 1),
                    new ItemDrop(11212, 500, 1),
                    new ItemDrop(537, 30, 1),
                    new ItemDrop(11230, 500, 1),
                    new ItemDrop(1149, 1, 1),
                    new ItemDrop(4087, 1, 1),
                    new ItemDrop(22358, 1, 1),
                    new ItemDrop(1746, 30, 1),
                    new ItemDrop(212, 50, 1),
                    new ItemDrop(218, 50, 1),
                    new ItemDrop(2486, 50, 1),
                    new ItemDrop(220, 50, 1),
                    new ItemDrop(1514, 500, 1),
                    new ItemDrop(8836, 500, 1),
                    new ItemDrop(8783, 300, 1),
                    new ItemDrop(2360, 200, 1),
                    new ItemDrop(448, 200, 1),
                    new ItemDrop(15332, 10, 1),
                    new ItemDrop(25205, 1, 1),
                    new ItemDrop(21631, 30, 1),
                    new ItemDrop(49670, 500, 1),
                    new ItemDrop(1127, 1, 1),
                    new ItemDrop(1079, 1, 1),
                    new ItemDrop(2364, 50, 1),
                    new ItemDrop(452, 50, 1),
                    new ItemDrop(22366, 1, 1),
                    new ItemDrop(22362, 1, 1),
                    new ItemDrop(8781, 300, 1),
                    new ItemDrop(1618, 20, 1),
                    new ItemDrop(1620, 20, 1),
                    new ItemDrop(25459, 10, 1)),

            new DropCategory("Rare", 6,
                    new ItemDrop(43307, 10000, 1),
                    new ItemDrop(6739, 1, 1),
                    new ItemDrop(11732, 1, 1),
                    new ItemDrop(2513, 1, 1),
                    new ItemDrop(51028, 1, 1),
                    new ItemDrop(15259, 1, 1),
                    new ItemDrop(25481, 1, 1),
                    new ItemDrop(25503, 1, 1),
                    new ItemDrop(25503, 1, 1),
                    new ItemDrop(25587, 50000, 1),
                    new ItemDrop(25459, 25, 1),
                    new ItemDrop(10350, 1, 1),
                    new ItemDrop(10348, 1, 1),
                    new ItemDrop(10346, 1, 1),
                    new ItemDrop(53242, 1, 1),
                    new ItemDrop(10352, 1, 1),
                    new ItemDrop(10334, 1, 1),
                    new ItemDrop(10330, 1, 1),
                    new ItemDrop(10332, 1, 1),
                    new ItemDrop(10336, 1, 1),
                    new ItemDrop(10342, 1, 1),
                    new ItemDrop(10338, 1, 1),
                    new ItemDrop(10340, 1, 1),
                    new ItemDrop(10344, 1, 1),
                    new ItemDrop(19314, 1, 1),
                    new ItemDrop(19317, 1, 1),
                    new ItemDrop(19320, 1, 1),
                    new ItemDrop(19311, 1, 1),
                    new ItemDrop(42437, 1, 1),
                    new ItemDrop(42426, 1, 1),
                    new ItemDrop(42424, 1, 1),
                    new ItemDrop(42422, 1, 1),
                    new ItemDrop(53342, 1, 1),
                    new ItemDrop(50014, 1, 1),
                    new ItemDrop(50011, 1, 1)),

            new DropCategory(VERY_RARE_TABLE, 1, true, // 1/500
                    new ItemDrop(25470, 1, 1))
    };

    public static DropTable dropGenerator = new DropTable(categories);
}
