package com.rs.game.npc.worldboss;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.NPCKillLog;
import com.rs.game.player.controllers.TheHorde;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.NPCDrops;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class OnyxBoss extends NPC {

	public static final String[] TEXTS = new String[]
	{
		"Your soul belongs to me!",
		"Do you fear the dark abyss?",
		"Weak fools!",
		"The power of darkness is unstoppable!",
		"No one can stop me!",
		"I eat chaotics for lunch!",
		"Give me your twisted bow!",
		"Your foolish prayers make me stronger!"
	};

	/**
	 * Phase of the boss. 0 - Melee 1 - Ranged 2 - Magic 3 - Ultimate
	 */
	private int phase = 0;

	/**
	 * List of minions
	 */
	private List<NPC> minnions = new ArrayList<NPC>();

	
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
		NPCDrops.addDrops(15184, drops);
		NPCDrops.addDrops(15185, drops);
		NPCDrops.addDrops(15186, drops);
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
	
	public OnyxBoss(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setLureDelay(0);
		setCapDamage(500);
		setCombatLevel(2760);
	//	setName("Onyx");
		setIntelligentRouteFinder(true);
		setRun(true);
		setForceMultiAttacked(true);
		setForceMultiArea(true);
		setForceAgressive(true);
		this.setForceLootshare(true);
		setDropRateFactor(9);
		
		//
		this.setCantFollowUnderCombat(true);
	}

	@Override
	public void processNPC() {
		if (isDead())
			return;

		Iterator<NPC> it$ = minnions.iterator();
		while (it$.hasNext()) {
			NPC npc = it$.next();
			if (npc != null && npc.hasFinished())
				it$.remove();
		}

		if (getRespawnTile() != null) {
			int deltaX = getX() - getRespawnTile().getX();
			int deltaY = getY() - getRespawnTile().getY();
			if (deltaX < -30 || deltaX > 30 || deltaY < -30 || deltaY > 30) {
				setNextWorldTile(getRespawnTile().transform(0, 0, 0));
			}
		}

		if (!getCombat().process()) {
			checkAgressivity();
			/*if (!checkAgressivity() && phase != 0 && getHitpoints() >= getMaxHitpoints()) {
				phase = 0;
				setNextNPCTransformation(15186);
			}*/
		}
		doRegenTransform();

	}

	public void doRegenTransform() {
		int hp = getHitpoints() + calculateRegenPower();
		if (hp > getMaxHitpoints())
			hp = getMaxHitpoints();
		setHitpoints(hp);

		if (phase == 0 && getId() != 15186)
			setNextNPCTransformation(15186);
		else if (phase == 1 && getId() != 15184)
			setNextNPCTransformation(15184);
		else if (phase == 2 && getId() != 15185)
			setNextNPCTransformation(15185);
		else if (phase == 3 && getId() != 15185)
			setNextNPCTransformation(15185);
	}

	public int calculateRegenPower() {
		/*int hp_percent = (getHitpoints() * 100) / getMaxHitpoints();
		if (hp_percent >= 90)
			return getHitpoints() / 500;
		else if (hp_percent >= 60)
			return getHitpoints() / 300;
		else if (hp_percent >= 30)
			return getHitpoints() / 200;
		else
			return getHitpoints() / 100;*/
		return 1;
	}

	public int calculateAttackSpeed(int base) {
		/*int hp_percent = (getHitpoints() * 100) / getMaxHitpoints();
		if (hp_percent >= 90)
			return base;
		else if (hp_percent >= 60)
			return base - 1;
		else if (hp_percent >= 30)
			return base - 2;
		else
			return base - 3;*/
		return base;
	}

	public int calculateMaxHit(int base) {
		int hp_percent = (getHitpoints() * 100) / getMaxHitpoints();
		if (hp_percent >= 90)
			return base + 50;
		else if (hp_percent >= 60)
			return base + 100;
		else if (hp_percent >= 30)
			return base + 150;
		else
			return base + 200;
	}

	public int calculateRecoilDamage(int dmg) {
		int hp_percent = (getHitpoints() * 100) / getMaxHitpoints();
		if (hp_percent >= 90)
			return 0;
		else if (hp_percent >= 60)
			return dmg / 20; //4
		else
			return dmg / 10; //2
	}

	@Override
	public ArrayList<Entity> getPossibleTargets(boolean checkPlayers, boolean checkNpcs) {
		ArrayList<Entity> list = super.getPossibleTargets(true, true);
		for (NPC minnion : minnions)
			list.remove(minnion);
		return list;
	}
	
	@Override
	public void drop() {
		for (Entity entity : getReceivedDamageSources()) {
			if (entity instanceof Player) {
				int damage = getDamageReceived(entity);
				if (((Player) entity).getControlerManager().getControler() instanceof TheHorde && getReceivedDamageSources().size() == 1)
					return;
				if (damage >= 1) {
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
	public Item sendDrop(Player player, Drop drop) {
		int size = getSize();
		Item item = ItemConfig.forID(drop.getItemId()).isStackable() ? new Item(drop.getItemId(), (drop.getMinAmount() * Settings.getDropQuantityRate()) + Utils.getRandom(drop.getExtraAmount() * Settings.getDropQuantityRate())) : new Item(drop.getItemId(), drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount()));
		if (!isOverall(item.getId())) 
			World.sendNews(player, "World boss dropped " + item.getAmount() + " x " + item.getName(), 1);
		player.setLootbeam(World.addGroundItem(item, new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane()), player, true, 60));
		return item;
	}

	@Override
	public void sendDeath(final Entity source) {
		for (NPC npc : minnions)
			npc.finish();
		minnions.clear();

		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		setNextGraphics(new Graphics(2929));

		if (phase == 0) {
			setNextForceTalk(new ForceTalk("Uhh.. This is just the beginning..."));
		} else if (phase == 1) {
			setNextForceTalk(new ForceTalk("It's getting annoying, no one will dare to stop me!"));
		} else if (phase == 2) {
			setNextForceTalk(new ForceTalk("THIS IS IT! FACE MY ULTIMATE POWER!!"));
		} else {
			setNextForceTalk(new ForceTalk("Impossible..."));
		}

		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (phase == 0) {
					phase = 1;
					setHitpoints(getMaxHitpoints());
					setNextNPCTransformation(15184);
					for (Entity trg : getPossibleTargets(true, true)) {
						if (trg == OnyxBoss.this || !trg.withinDistance(OnyxBoss.this, 3))
							continue;
						trg.applyHit(new Hit(OnyxBoss.this, Utils.getRandom(50) + 100, HitLook.DESEASE_DAMAGE));
					}
				} else if (phase == 1) {
					phase = 2;
					setHitpoints(getMaxHitpoints());
					setNextNPCTransformation(15185);
					for (Entity trg : getPossibleTargets(true, true)) {
						if (trg == OnyxBoss.this || !trg.withinDistance(OnyxBoss.this, 3))
							continue;
						trg.applyHit(new Hit(OnyxBoss.this, Utils.getRandom(200) + 100, HitLook.DESEASE_DAMAGE));
					}
					Player killer = getMostDamageReceivedSourcePlayer();
					if (killer != null) {
						NPCKillLog.addKill(killer, getName());
						resetReceivedDamage();
					}
				} else if (phase == 2) {
					phase = 3;
					setHitpoints(getMaxHitpoints());
					for (Entity trg : getPossibleTargets(true, true)) {
						if (trg == OnyxBoss.this || !trg.withinDistance(OnyxBoss.this, 3))
							continue;
						trg.applyHit(new Hit(OnyxBoss.this, Utils.getRandom(600) + 100, HitLook.DESEASE_DAMAGE));
					}
				} else {
					for (Entity trg : getPossibleTargets(true, true)) {
						if (trg == OnyxBoss.this || !trg.withinDistance(OnyxBoss.this, 3))
							continue;
						trg.applyHit(new Hit(OnyxBoss.this, Utils.getRandom(800) + 100, HitLook.DESEASE_DAMAGE));
					}
					World.sendNews("World boss has been killed!", World.WORLD_NEWS);
					OnyxBoss.super.sendDeath(source);
					return;
				}
			}
		}, 3);
	}

	public void registerMinnion(NPC minnion) {
		minnions.add(minnion);
	}

	public List<NPC> getMinnions() {
		return minnions;
	}

	public int getPhase() {
		return phase;
	}

	
	@Override
	public int getMaxHitpoints() {
		int hp = super.getMaxHitpoints();
		
		if (getRegionId() != 11807)
			return hp;
		
		//double pc = World.getPlayerCount();
		//double mult = 1; //+ (pc * 0.03); //3% increase per player
		return hp;//(int) (hp * mult);
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (minnions.size() > 0) {
			int totalHP = 0;
			for (NPC minnion : minnions)
				totalHP += minnion.getHitpoints();
			if (totalHP > 0) {
				hit.setHealHit();
				super.handleIngoingHit(hit);
				return;
			}
		}

		super.handleIngoingHit(hit);

		if (hit.getSource() != null) {
			int recoil = calculateRecoilDamage(hit.getDamage());
			if (recoil > 0)
				hit.getSource().applyHit(new Hit(this, recoil, HitLook.REFLECTED_DAMAGE));
		}
	}

	@Override
	public double getMagePrayerMultiplier() {
		return phase + 1.5;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return phase + 1.5;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return phase + 1.5;
	}
}
