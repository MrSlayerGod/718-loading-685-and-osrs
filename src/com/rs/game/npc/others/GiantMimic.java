package com.rs.game.npc.others;

import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.worldboss.OnyxBoss;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.MimicFightController;
import com.rs.game.player.controllers.TheHorde;
import com.rs.utils.DropTable;
import com.rs.utils.DropTable.DropCategory;
import com.rs.utils.DropTable.ItemDrop;
import com.rs.utils.NPCDrops;
import com.rs.utils.Utils;

import java.util.*;

@SuppressWarnings("serial")
/**
 * @author Simplex
 * @since Sep 10, 2020
 */
public class GiantMimic extends NPC {

    // npcs
    public static final int MIMIC_ID = 21230, WARRIOR_ID = 28635, RANGER_ID = 28636, MAGE_ID = 28637;

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

    public NPC[] minions = new NPC[3];

    public int chaseTargetTicks;

    private MimicFightController instance;

    public static Item[] OVERALL_REWARDS =
            new Item[] { new Item(200, 100),
                    new Item(202, 100),
                    new Item(204, 100),
                    new Item(206, 100),
                    new Item(208, 100),
                    new Item(210, 100),
                    new Item(212, 100),
                    new Item(214, 100),
                    new Item(216, 100),
                    new Item(218, 100),
                    new Item(220, 100),
                    new Item(232, 100),
                    new Item(224, 100),
                    new Item(1120, 100),
                    new Item(5973, 100),
                    new Item(10819, 100),
                    new Item(2, 1000),
                    new Item(995, 3000000)};

    public static void init() {
        Drops drops = new Drops(true);
        @SuppressWarnings("unchecked")
        List<Drop>[] dList = new ArrayList[Drops.VERY_RARE + 1];
        for (int i = 0; i < dList.length; i++)
            dList[i] = new ArrayList<Drop>();
        dList[Drops.ALWAYS].add(new Drop(995, 10000000, 10000000));

        dList[Drops.RARE].add(new Drop(25478, 1, 1));
        dList[Drops.RARE].add(new Drop(25470, 1, 1));
		/*dList[Drops.RARE].add(new Drop(1038, 1, 1));
		dList[Drops.RARE].add(new Drop(1040, 1, 1));
		dList[Drops.RARE].add(new Drop(1042, 1, 1));
		dList[Drops.RARE].add(new Drop(1044, 1, 1));
		dList[Drops.RARE].add(new Drop(1046, 1, 1));
		dList[Drops.RARE].add(new Drop(1050, 1, 1));
		dList[Drops.RARE].add(new Drop(1053, 1, 1));
		dList[Drops.RARE].add(new Drop(1055, 1, 1));
		dList[Drops.RARE].add(new Drop(1057, 1, 1));
		//
		dList[Drops.RARE].add(new Drop(18349, 1, 1));
		dList[Drops.RARE].add(new Drop(18351, 1, 1));
		dList[Drops.RARE].add(new Drop(18353, 1, 1));
		dList[Drops.RARE].add(new Drop(18355, 1, 1));
		dList[Drops.RARE].add(new Drop(18357, 1, 1));
		dList[Drops.RARE].add(new Drop(49544, 1, 1));
		dList[Drops.RARE].add(new Drop(49547, 1, 1));
		dList[Drops.RARE].add(new Drop(49550, 1, 1));
		dList[Drops.RARE].add(new Drop(49553, 1, 1));
		dList[Drops.RARE].add(new Drop(25453, 1, 1));*/

        dList[Drops.UNCOMMON].add(new Drop(23715, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(23716, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(4151, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(15486, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(11235, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(995, 30000000, 30000000));
        dList[Drops.UNCOMMON].add(new Drop(21371, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(42006, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(41905, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(11286, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(6199, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(6585, 1, 1));

        dList[Drops.UNCOMMON].add(new Drop(18349, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(18351, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(18353, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(18355, 1, 1));
        dList[Drops.UNCOMMON].add(new Drop(18357, 1, 1));

        //dList[Drops.UNCOMMON].add(new Drop(25436, 1, 1));

        for (Item item : OVERALL_REWARDS)
            dList[Drops.COMMOM].add(new Drop(item.getId(), item.getAmount(), item.getAmount()));
        drops.addDrops(dList);
        NPCDrops.addDrops(21230, drops);
    }


    public static boolean isOverall(int drop)  {
        for (Item i : OVERALL_REWARDS)
            if (i.getId() == drop)
                return true;
        for (int i : Drops.CHARMS)
            if (i == drop)
                return true;
        return drop == 995;
    }


    public GiantMimic(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, true);
        // uninstanced fight
        // can use this for events
       // this.setHitpoints(getMaxHitpoints() * 5); // x5 for boss masses
        this.instance = null;
        setup();
    }

    @Override
    public int getMaxHitpoints() {
        int hp = super.getMaxHitpoints() * 2;

        if (getRegionId() != 6214 && getRegionId() != 6470)
            return hp;

        double pc = World.getPlayerCount();
        double mult = 1 + (pc * 0.03); //3% increase per player
        return (int) (hp * mult);
    }

    public GiantMimic(MimicFightController instance) {
        super(MIMIC_ID, instance.getMap().getTile(MIMIC_SPAWN), -1, true, true);
        this.instance = instance;
        setup();
    }

    public void setup() {
        setLureDelay(0);
        setCapDamage(500);
        setForceMultiArea(true);
        setForceMultiAttacked(true);
        setForceTargetDistance(64);
        setIntelligentRouteFinder(true);
        setRun(true);
        setForceLootshare(true);
        setDropRateFactor(9);
        setForceAgressive(true);
    }

    @Override
    public void drop() {
        for (Entity entity : getReceivedDamageSources()) {
            if (entity instanceof Player) {
                int damage = getDamageReceived(entity);
                if (((Player) entity).getControlerManager().getControler() instanceof TheHorde && getReceivedDamageSources().size() == 1)
                    return;
                if (damage >= 500) {
                    Player player = (Player) entity;

                    if (!player.withinDistance(this))
                        continue;
                    player.getPackets().sendGameMessage("You receive a reward for your participation in the world boss event.");
                    Item reward = OVERALL_REWARDS[Utils.random(OVERALL_REWARDS.length)];
                    player.getInventory().addItemDrop(reward.getId(), reward.getAmount());
                    player.getInventory().addItemDrop(995, 1000000);
                }
            }
        }
        super.drop();
    }

    @Override
    public List<Player> getForceLootSharingPeople() {
        List<Player> players = super.getForceLootSharingPeople();
        for (Player player : players.toArray(new Player[players.size()]))
            if (getDamageReceived(player) < 500)
                players.remove(player);
        return players;
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
