package com.rs.game.npc.others;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.TheHorde;
import com.rs.utils.NPCDrops;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Lucien extends NPC {

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

		dList[Drops.RARE].add(new Drop(25765, 1, 1));
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
		NPCDrops.addDrops(14256, drops);
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

	public Lucien(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setLureDelay(0);
		setCapDamage(500);
		setCombatLevel(59999);
	//	setName("Dragonkk's #1 Boss");
		setRun(true);
		setForceMultiAttacked(true);
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public void handleIngoingHit(Hit hit) {
		if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE)
			return;
		super.handleIngoingHit(hit);
		if (hit.getSource() != null) {
			int recoil = (int) (hit.getDamage() * 0.1);
			if (recoil > 0)
				hit.getSource().applyHit(new Hit(this, recoil, HitLook.REFLECTED_DAMAGE));
		}
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
	public Item sendDrop(Player player, Drop drop) {
		int size = getSize();
		Item item = ItemConfig.forID(drop.getItemId()).isStackable() ? new Item(drop.getItemId(), (drop.getMinAmount() * Settings.getDropQuantityRate()) + Utils.getRandom(drop.getExtraAmount() * Settings.getDropQuantityRate())) : new Item(drop.getItemId(), drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount()));
		if (!isOverall(item.getId()))
			World.sendNews(player, "World boss dropped " + item.getAmount() + " x " + item.getName(), 1);
		player.setLootbeam(World.addGroundItem(item, new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane()), player, true, 60));
		return item;
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
}
