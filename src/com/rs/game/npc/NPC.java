package com.rs.game.npc;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemConfig;
import com.rs.cache.loaders.NPCConfig;
import com.rs.executor.GameExecutorManager;
import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.item.FloorItem;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombat;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.nomad.Nomad;
import com.rs.game.npc.others.Dreadnip;
import com.rs.game.npc.slayer.AlchemicalHydra;
import com.rs.game.player.*;
import com.rs.game.player.Achievements.Task;
import com.rs.game.player.actions.HerbCleaning;
import com.rs.game.player.actions.HerbCleaning.Herbs;
import com.rs.game.player.content.*;
import com.rs.game.player.content.Slayer.SlayerMaster;
import com.rs.game.player.content.collectionlog.CategoryType;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.content.prayer.Burying;
import com.rs.game.player.content.prayer.Burying.Bone;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.impl.ScavengerChamber;
import com.rs.game.player.content.seasonalEvents.Hallowen2018;
import com.rs.game.player.controllers.DTControler;
import com.rs.game.player.controllers.NomadsRequiem;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.DropTable.ItemDrop;
import com.rs.utils.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NPC extends Entity implements Serializable {

	public static int NORMAL_WALK = 0x2, WATER_WALK = 0x4, FLY_WALK = 0x8;

	private static final long serialVersionUID = -4794678936277614443L;

	private int id;
	private CombatScript customCombatScript = null;
	private WorldTile respawnTile;
	private int mapAreaNameHash;
	private boolean canBeAttackFromOutOfArea;
	private int walkType;
	private double[] bonusesD; // 0 stab, 1 slash, 2 crush,3 mage, 4 range, 5 stab
	// def, blahblah till 9
	private boolean spawned;
	private transient NPCCombat combat;
	public WorldTile forceWalk;

	private long lastAttackedByTarget;
	private boolean cantInteract;
	private int capDamage;
	private int lureDelay;
	private boolean cantFollowUnderCombat;
	private boolean forceAgressive;
	private int forceTargetDistance;
	private boolean forceFollowClose;
	private boolean forceMultiAttacked;
	private boolean noDistanceCheck;
	private boolean intelligentRouteFinder;

	// npc masks
	private transient Transformation nextTransformation;
	private transient SecondaryBar nextSecondaryBar;
	// name changing masks
	private String name;
	private transient boolean changedName;
	private int combatLevel;
	private transient boolean changedCombatLevel;
	private transient boolean locked;
	private transient double dropRateFactor;
	private transient boolean cantSetTargetAutoRelatio;

	private transient BossInstance bossInstance; //if its a instance npc
	
	private transient long stopRandomWalk;
	
	private transient long fightStartTime;
	private transient int fightStartBy;
	private boolean canWalkNPC = false;

	public NPC() {
		super(new WorldTile(-1, -1, -1));
	}

	public NPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		this(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
	}

	/*
	 * creates and adds npc
	 */
	public NPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(tile);
		this.id = id;
		this.respawnTile = new WorldTile(tile);
		this.mapAreaNameHash = mapAreaNameHash;
		this.canBeAttackFromOutOfArea = canBeAttackFromOutOfArea;
		this.spawned = spawned;
		combatLevel = -1;
		dropRateFactor = 1;
		setHitpoints(getMaxHitpoints());
		setDirection(getRespawnDirection());
		//int walkType = t(id);
		setRandomWalk(getDefinitions().movementCapabilities);
		setBonuses();
		combat = new NPCCombat(this);
		capDamage = -1;
		lureDelay = 12000;
		// npc is inited on creating instance
		initEntity();
		World.addNPC(this);
		World.updateEntityRegion(this);
		// npc is started on creating instance
		loadMapRegions();
		checkMultiArea();
	}

	public CombatScript getCustomCombatScript() {
		return customCombatScript;
	}

	public void setCustomCombatScript(CombatScript customCombatScript) {
		this.customCombatScript = customCombatScript;
	}

	@Override
	public void addReceivedDamage(Entity source, int amount) {
		super.addReceivedDamage(source, amount);
		if (source == null || (!(source instanceof Player) && !(source instanceof Familiar) && !(source instanceof Dreadnip)))
			return;
		int hashcode = source instanceof Familiar ? ((Familiar)source).getOwner().hashCode() : source.hashCode();
		if (fightStartTime == 0) {
			fightStartTime = Utils.currentTimeMillis();
			fightStartBy = source.hashCode();
		} else if (fightStartBy >= 0 && fightStartBy != hashcode)
			fightStartBy = -1;
	}
	
	public void setBonuses() {
		/*double[] b = getDefaultBonuses();
		if (b == null) {
			bonusesD = new double[10];
			int level = getCombatLevel();
			for (int i = 0; i < bonusesD.length; i++) {
				bonusesD[i] = i >= 5 ? level/**2*//* : (level / 2);
			}
		}else
			bonusesD = b.clone();*/
		bonusesD = getDefaultBonuses();
	}

	public void setBonus(int slot, double d) {
		bonusesD[slot] = d;
	}
	
	public void restoreBonuses() {
		double[] b = getDefaultBonuses();
		if(b == null)
			return;
		for(int i = 0; i < b.length; i++) {
			if(b[i] > bonusesD[i])
				bonusesD[i]++;
			else if(b[i] < bonusesD[i])
				bonusesD[i]--;
		}
	}

	@Override
	public boolean needMasksUpdate() {
		return super.needMasksUpdate() || nextSecondaryBar != null || nextTransformation != null || getCustomName() != null || getCustomCombatLevel() >= 0 /*									        * changedName
		 */;
	}

	public void setNextNPCTransformation(int id) {
		setNPC(id);
		nextTransformation = new Transformation(id);
		if (getCustomCombatLevel() != -1)
			changedCombatLevel = true;
		if (getCustomName() != null)
			changedName = true;
	}

	public void setNPC(int id) {
		this.id = id;
		setBonuses();
	}

	@Override
	public void resetMasks() {
		super.resetMasks();
		nextTransformation = null;
		changedCombatLevel = false;
		changedName = false;
		nextSecondaryBar = null;
	}

	public int getMapAreaNameHash() {
		return mapAreaNameHash;
	}

	public void setCanBeAttackFromOutOfArea(boolean b) {
		canBeAttackFromOutOfArea = b;
	}

	public boolean canBeAttackFromOutOfArea() {
		return canBeAttackFromOutOfArea;
	}

	public NPCConfig getDefinitions() {
		return NPCConfig.forID(id);
	}

	public NPCCombatDefinitions getCombatDefinitions() {
		return NPCCombatDefinitionsL.getNPCCombatDefinitions(id);
	}

	
	private double difficultyMultiplier; //extreme mode setting
	
	public double getDifficultyMultiplier() {
		return difficultyMultiplier;
	}
	
	public NPC setDifficultyMultiplier(double mult) {
		if (mult != 0) {
			difficultyMultiplier = mult;
			setHitpoints(getMaxHitpoints());
			setBonuses();
			setCombatLevel((int) (getDefinitions().combatLevel * difficultyMultiplier));
			setName("Enraged "+getDefinitions().getName());
		}
		return this;
	}
	
	public boolean isHardMode() {
		return this.bossInstance != null && bossInstance.getSettings().isHardMode();
	}
	
	
	@Override
	public int getMaxHitpoints() {
		int hp = Math.max(1, getCombatDefinitions().getHitpoints());
		if (difficultyMultiplier != 0)
			hp *= difficultyMultiplier;
		return hp;
	}
	
	public double[] getDefaultBonuses() {
		double[] b = NPCBonuses.getBonuses(id);
		if (b == null) {
			b = new double[10];
			int level = getCombatLevel();
			for (int i = 0; i < b.length; i++) {
				b[i] = i >= 5 ? level/**2*/ : (level / 2);
			}
		} else
			b = b.clone();
		if (difficultyMultiplier != 0) {
			for (int i = 0; i < b.length; i++) 
				b[i] *= difficultyMultiplier;
		}
		return b;
	}
	

	public int getId() {
		return id;
	}
	
	public void randomWalk() {
		if (getFreezeDelay() < Utils.currentTimeMillis()) {
			if (!hasWalkSteps() && (walkType & NORMAL_WALK) != 0) {
				boolean can = stopRandomWalk < Utils.currentTimeMillis() && Math.random() > 0.9;
				if (can) {
					boolean fly = (walkType & FLY_WALK) != 0;
					int moveX = Utils.random(0, fly ? 10 : 5);
					int moveY = Utils.random(0, fly ? 10 : 5);
					if(Utils.random(2) == 0)
						moveX = -moveX;
					if(Utils.random(2) == 0)
						moveY = -moveY;
					resetWalkSteps();
					if (getMapAreaNameHash() != -1) {
						if (!MapAreas.isAtArea(getMapAreaNameHash(), this)) {
							forceWalkRespawnTile();
							return;
						}
						//fly walk noclips for now, nothing uses it anyway
						if((walkType & FLY_WALK) != 0)
							addWalkSteps(getX() + moveX, getY() + moveY, 10, false);
						else
							Entity.findBasicRoute(this, new WorldTile(getX() + moveX, getY() + moveY, getPlane()), 10, true);
					} else
						if((walkType & FLY_WALK) != 0)
							addWalkSteps(respawnTile.getX() + moveX, respawnTile.getY() + moveY, 7, false);
						else
							Entity.findBasicRoute(this, respawnTile.transform(moveX, moveY, 0), 7, true);
					//	addWalkSteps(respawnTile.getX() + moveX, respawnTile.getY() + moveY, 5, (walkType & FLY_WALK) == 0);
				}

			}
		}
	}

	public void processNPC() {
		if (isDead() || locked)
			return;
		
		
		if (!combat.process()) { // if not under combat
			if (!isForceWalking()) {// combat still processed for attack delay
				// go down
				// random walk
				if (!cantInteract) {
					if (!checkAgressivity()) {
						randomWalk();
					}
				}
			}
		}
		if (isForceWalking()) {
			if (getFreezeDelay() < Utils.currentTimeMillis()) {
				if (getX() != forceWalk.getX() || getY() != forceWalk.getY()) {
					if (!hasWalkSteps()) {
						if((walkType & FLY_WALK) != 0)
							addWalkSteps(forceWalk.getX(), forceWalk.getY(), 2, false);
						else {
							int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), new FixedTileStrategy(forceWalk.getX(), forceWalk.getY()), true);
							int[] bufferX = RouteFinder.getLastPathBufferX();
							int[] bufferY = RouteFinder.getLastPathBufferY();
							for (int i = steps - 1; i >= 0; i--) {
								if (!addWalkSteps(bufferX[i], bufferY[i], 25, true))
									break;
							}
						}
					}
					if (!hasWalkSteps()) { // failing finding route
						setNextWorldTile(new WorldTile(forceWalk)); // force
						// tele
						// to
						// the
						// forcewalk
						// place
						forceWalk = null; // so ofc reached forcewalk place
					}
				} else
					// walked till forcewalk place
					forceWalk = null;
			}
		}
	}

	@Override
	public void processEntity() {
		super.processEntity();
		processNPC();
	}

	public int getRespawnDirection() {
		NPCConfig definitions = getDefinitions();
		if (definitions.contrast << 32 != 0 && definitions.respawnDirection > 0 && definitions.respawnDirection <= 8)
			return (4 + definitions.respawnDirection) << 11;
		return 0;
	}

	/*
	 * forces npc to random walk even if cache says no, used because of fake
	 * cache information
	 */
	/*  private static int walkType(int npcId) {
	switch (npcId) {
	    case 11226:
		return RANDOM_WALK;
	    case 3341:
	    case 3342:
	    case 3343:
		return RANDOM_WALK;
	    default:
		return -1;
	}
	  }*/

	public void sendSoulSplit(final Hit hit, final Entity user) {
		final NPC target = this;
		if (hit.getDamage() > 0)
			World.sendProjectile(user, this, 2263, 11, 11, 20, 5, 0, 0);
		user.heal(hit.getDamage() / 5);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				setNextGraphics(new Graphics(2264));
				if (hit.getDamage() > 0)
					World.sendProjectile(target, user, 2263, 11, 11, 20, 5, 0, 0);
			}
		}, 1);
	}
	
	@Override
	public void handleIngoingHit(final Hit hit) {
		if (capDamage != -1 && hit.getDamage() > capDamage)
			hit.setDamage(capDamage);

		if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE)
			return;
		Entity source = hit.getSource();
		if (source == null)
			return;
		if (source instanceof Player) {
			((Player) source).getPrayer().handleHitPrayers(this, hit);
			((Player) source).getControlerManager().processIncommingHit(hit, this);
		}

	}

	@Override
	public void reset() {
		super.reset();
		setDirection(getRespawnDirection());
		combat.reset();
		setBonuses(); // back to real bonuses
		forceWalk = null;
		fightStartTime = 0;
	}

	@Override
	public void finish() {
		if (hasFinished())
			return;
		setFinished(true);
		World.updateEntityRegion(this);
		World.removeNPC(this);
	}

	public void setRespawnTask() {
		if(bossInstance != null && bossInstance.isFinished())
			return;
		if (!hasFinished()) {
			reset();
			setLocation(respawnTile);
			finish();
		}
		long respawnDelay = getCombatDefinitions().getRespawnDelay() * 600;
		if(bossInstance != null) 
			respawnDelay /= bossInstance.getSettings().getSpawnSpeed();
		GameExecutorManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					if(bossInstance != null && bossInstance.isFinished()) //instance bosses shouldnt respawn when instance over
						return;
					spawn();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, respawnDelay, TimeUnit.MILLISECONDS);
	}
	
	public void setRespawnTile(WorldTile respawnTile) {
		this.respawnTile = respawnTile;
	}

	public void deserialize() {
		if (combat == null)
			combat = new NPCCombat(this);
		spawn();
	}

	public void onSpawn() {}

	public void spawn() {
		if (bonusesD == null) //due to change
			setBonuses();
		onSpawn();
		setFinished(false);
		World.addNPC(this);
		setLastRegionId(0);
		World.updateEntityRegion(this);
		loadMapRegions();
		checkMultiArea();
	}

	public NPCCombat getCombat() {
		return combat;
	}

	@Override
	public void sendDeath(final Entity source) {
		final NPCCombatDefinitions defs = getCombatDefinitions();
		resetWalkSteps();
		combat.removeTarget();
		setNextAnimation(null);
		if (!isDead())
			setHitpoints(0);
		int deathDelay2 = ScavengerChamber.isScav(getId()) || getId() == AlchemicalHydra.ENRAGE_ID ? 5 : getId() == 5666 || getId() == 27286 ? 7 : defs.getDeathDelay() - ( getId() == 50 ? 2 : 1);
		final int deathDelay = deathDelay2;
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= deathDelay) {
					if (source instanceof Player) {
						Player player = (Player) source;
						player.getTasksManager().checkForProgression(DailyTasksManager.PVM, getName());
						player.getControlerManager().processNPCDeath(NPC.this);
					}
					drop();
					reset();
					setLocation(respawnTile);
					finish();
					if (!isSpawned())
						setRespawnTask();
					if (source != null && source.getAttackedBy() == NPC.this) { //no need to wait after u kill
						source.setAttackedByDelay(0);
						source.setAttackedBy(null);
						source.setFindTargetDelay(0);
					}
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}
	
	public void addKill(Player killer) {
		long time = fightStartTime == 0 ? 0 : Utils.currentTimeMillis() - fightStartTime;
		boolean multi = fightStartTime == 0 || fightStartBy != killer.hashCode();
		String name = getId() == 14836 ? "Skoll" : getName();

		NPCKillLog.addKill(killer, name, time, multi);
	}
	
	public void checkSlayer(Player killer) {
		
		if (getId() == 1615 || getId() == 27241)
			killer.getAchievements().add(Task.KILL_ABYSSAL_DEMON);

		addKill(killer);
		Player otherPlayer = killer.getSlayerManager().getSocialPlayer();
		SlayerManager manager = killer.getSlayerManager();

		if (manager.isValidTask(getName())) {
			manager.checkCompletedTask((int) (getDamageReceived(killer) * (otherPlayer != null ? 1.25 : 1)), (int) (otherPlayer != null ? (getDamageReceived(otherPlayer) * 1.25) : 0));
			manager.trySuperiorSpawn(this);
			SlayerBox.roll(killer, this);
		}

		if (manager.getBossTask() != null && manager.getBossTaskRemaining() > 0) {
			String name = getName();
			if (name.equalsIgnoreCase("dusk")) //grotesque guardians exception
				name = "Grotesque Guardians";
			if(getId() == 14836) // skoll
				name = "Hati";
			if (name.equalsIgnoreCase(manager.getBossTask())) {
				manager.addBossKill(getDamageReceived(killer));
				SlayerBox.roll(killer, this);
			}
		}
			
	}
	
	public static void main(String[] args) throws IOException {
		Cache.init();
		NPCDrops.init();
		Drops drops = NPCDrops.getDrops(3340);
		
		int id = 10551;
		int count = 0;
		int kills = 3000000;
		for (int i = 0; i < kills; i++) { //350
			List<Drop> kill = drops.generateDrops(null, 1
					//* 0.9 //easy
					* 1.1 //vote
					* 1.1 //week day
					* 1.1 //sap
					* 1.03 //bankpin
					* 7 //boss multiplier
					);
			for (Drop drop : kill) {
				if (drop.getItemId() == id)
					count++;
			}
		}
		System.out.println(count);
		System.out.println("drops every "+(kills/count));
	}
	
	private boolean forceLootshare;

	public Drops getDrops() {
		return NPCDrops.getDrops(id);
	}

	public Drops getHardDrops() {
		return NPCDrops.getDrops(-id);
	}

	public void drop() {
		drop(this.getDifficultyMultiplier() > 1.0 ? getHardDrops() : getDrops());
	}

	public void drop(Drops drops) {
		if (getCombatDefinitions() == NPCCombatDefinitionsL.DEFAULT_DEFINITION || getMaxHitpoints() == 1
				|| (bossInstance != null && (bossInstance.isFinished() || bossInstance.getSettings().isPractiseMode())))
			return;
		Player killer = getMostDamageReceivedSourcePlayer();
		if (killer == null || killer.getControlerManager().getControler() instanceof DTControler)
			return;

		if(OSRSDropTables.dropItems(killer, this))
			return;

		checkSlayer(killer);
		LuckyPets.checkBossPet(killer, this);
		if (drops == null)
			return;
		List<Player> players = this.isForceLootshare() ? getForceLootSharingPeople() : FriendsChat.getLootSharingPeople(killer, this);
		
		double dropRate = 0;

		boolean inRaid = ChambersOfXeric.getRaid(killer) != null;

		if (players == null || players.size() <= 1) 
			dropRate = killer.getDropRateMultiplier();
		else{ //to be fair
			for (Player p2 : players) 
				dropRate += p2.getDropRateMultiplier();
			dropRate /= players.size();
		}

		// If we have double drop rates enabled, double drop loots
		if (Settings.DOUBLE_DROP_RATES) {
			dropRate *= 2;
		}

		List<Drop> dropL = drops.generateDrops(killer, dropRate * dropRateFactor);
		if(!inRaid)
			drops.addCharms(dropL, getId() == 5361 ? 3 : getSize());
		int mapID = getRegionId();
		if ((mapID == 6557 || mapID == 6556 || mapID == 6813 || mapID == 6812) && getMaxHitpoints() >= 500 && Utils.random(50) == 0)
			dropL.add(new Drop(49677, 1, 1));
		else if ((mapID == 12738 || mapID == 12993 || mapID == 12994) && getMaxHitpoints() >= 500 && Utils.random(10) == 0)
			dropL.add(new Drop(53962, 1, 1));
		
		SlayerManager manager = killer.getSlayerManager();
		if ((manager.getCurrentMaster() == SlayerMaster.KURADAL || manager.getCurrentMaster() == SlayerMaster.KONAR_QUO_MATEN) && manager.isValidTask(getName())) {
			int level = Math.min(350, getCombatLevel());
			int chance = (int) (level >= 100 ? (-0.2 * level + 120) : (0.2 * Math.pow((level - 100), 2) + 100));
			if (Utils.random(chance) == 0)
				dropL.add(new Drop(53083, 1, 1));
		}
		/*else if (Combat.isUndead(this)) { 
			if (Utils.random(100 + Math.max(0, 200 - getCombatLevel())) == 0)
				dropL.add(new Drop(Hallowen2018.getItemDrop(killer), 1, 1));
			if (Utils.random(25) == 0)
				dropL.add(new Drop(1959, 1, 1));
		}*/
		
		if (players == null || players.size() <= 1) {
			boolean hasBonecrusher = killer.getInventory().containsOneItem(18337)
					|| killer.getEquipment().getAmuletId() == 52986;
			//gotta add option to turn it on and off
			boolean hasHerbicide = killer.getInventory().containsOneItem(19675);

			
			
			for (Drop drop : dropL) {
				if(hasBonecrusher) {
					Bone bone = Bone.forId(drop.getItemId());
					if(bone != null && !bone.isAsh()) {
						killer.getSkills().addXp(Skills.PRAYER, bone.getExperience());
						Burying.restorePrayer(killer, bone);
						continue;
					}
				}
				if(hasHerbicide) {
					final Herbs herb = HerbCleaning.getHerb(drop.getItemId());
					if(herb != null && killer.getSkills().getLevel(Skills.HERBLORE) >= herb.getLevel()) {
						killer.getSkills().addXp(Skills.HERBLORE, herb.getExperience()*2);
						continue;
					}
				}
				if (killer.getTreasureTrailsManager().isScroll(drop.getItemId())) {
					if (killer.getTreasureTrailsManager().hasClueScrollItem())
						continue;
					killer.getTreasureTrailsManager().resetCurrentClue();
				}
				sendDrop(killer, drop);
			}
			WorldTile dropTile = getDropTile();
			FloorItem floorItem = World.getFloorItem(killer, dropTile);
			int stackValue = World.getGroundStackValue(killer, dropTile);
			//System.out.println(floorItem);
			if (stackValue > 0 && floorItem != null) {
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						killer.getPackets().sendTileMessage(0, 15263739, floorItem, "Stack value: <col=00FF00>"+MoneyPouch.formatMoney(""+stackValue));
						
						
					}
					
				}, 0);
			}
			} else {
			for (Drop drop : dropL) {
				Player luckyPlayer = players.get(Utils.random(players.size()));
				if (luckyPlayer.getTreasureTrailsManager().isScroll(drop.getItemId())) {
					if (luckyPlayer.getTreasureTrailsManager().hasClueScrollItem())
						continue;
					luckyPlayer.getTreasureTrailsManager().resetCurrentClue();
				}
				Item item = sendDrop(luckyPlayer, drop);
				luckyPlayer.getPackets().sendGameMessage("<col=00FF00>You received: " + item.getAmount() + " " + item.getName() + ".");
				for (Player p2 : players) {
					if (p2 == luckyPlayer)
						continue;
					p2.getPackets().sendGameMessage("<col=66FFCC>" + luckyPlayer.getDisplayName() + "</col> received: " + item.getAmount() + " " + item.getName() + ".");
					p2.getPackets().sendGameMessage("Your chance of receiving loot has improved.");
				}
			}
			for (Player player : players) {
				WorldTile dropTile = getDropTile();
				FloorItem floorItem = World.getFloorItem(player, dropTile);
				int stackValue = World.getGroundStackValue(player, dropTile);
				if (stackValue > 0 && floorItem != null) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							player.getPackets().sendTileMessage(0, 15263739, floorItem, "Stack value: <col=ffffff>"+MoneyPouch.formatMoney(""+stackValue));
					
						}
					}, 0);
				}
			}
		}
	}

	public static boolean announceDrop(Drop drop) {
		return announceDrop(-1, drop);
	}

	public static boolean announceDrop(int id, Drop drop) {
		if ((drop.getItemId() >= 20135 && drop.getItemId() <= 20174) //nex piece
				|| (drop.getItemId() >= 24974 && drop.getItemId() <= 24991) //nex gloves/boots
				|| (drop.getItemId() >= 13746 && drop.getItemId() <= 13754) //sigil
				|| drop.getItemId() == 11335 //dragon full helm
				|| drop.getItemId() == 53908 || drop.getItemId() == 53953
				)
			return true;
		else if ((drop.getItemId() >= 21787 && drop.getItemId() <= 21795) //glacor boots
				|| (drop.getItemId() >= 11702 && drop.getItemId() <= 11709) //godsword hilts
				|| (drop.getItemId() >= 11716 && drop.getItemId() <= 11731) //godwars gear
				|| (drop.getItemId() >= 24992 && drop.getItemId() <= 25039) //godwars gear
				|| (drop.getItemId() >= 41924 && drop.getItemId() <= 41933) //wild wards
				|| (drop.getItemId() >= 25316 && drop.getItemId() <= 25318) //kbd dragon rider
				|| (drop.getItemId() >= 21580 && drop.getItemId() <= 21582) //blisterwood
				|| drop.getItemId() == 41990 //fedora
				|| drop.getItemId() == 14484  //dragon claws
				|| drop.getItemId() == 15259  //dragon pickaxe
				|| drop.getItemId() == 11286  //draconic visage
				|| drop.getItemId() == 24352 
				|| drop.getItemId() == 24338 
				|| drop.getItemId() == 23465 
				
				
				/*|| drop.getItemId() == 13902 //status warhammer
				|| drop.getItemId() == 13899 //vesta's longsword*/
				|| (drop.getItemId() >= 13858 && drop.getItemId() <= (id == 28097 ? 13907 : 13957)) //pvp gear
				|| (drop.getItemId() >= 24455 && drop.getItemId() <= 24457) //87 weaps
				//custom
				|| drop.getItemId() == 6739 //d axe
				|| drop.getItemId() == 6731 || drop.getItemId() == 6733 || drop.getItemId() == 6735 || drop.getItemId() == 6737 //dagannoth rings
			//	||  drop.getItemId() >= 10547 && drop.getItemId() <= 10555 //fightter torso
				|| drop.getItemId() == 10551
				||  drop.getItemId() == 14479 //dragon platebody
				|| drop.getItemId() == 43265 //abyssal dagger
				|| drop.getItemId() == 42601 || drop.getItemId() == 42603 || drop.getItemId() == 42605  //ring of the gods
				|| drop.getItemId() == 42927 || drop.getItemId() == 42932 || drop.getItemId() == 42922 || drop.getItemId() == 6571  //zulrah drops	
				|| drop.getItemId() == 41791
				|| drop.getItemId() == 42004 || drop.getItemId() == 41905  || drop.getItemId() == 41908
				|| drop.getItemId() == 43273
				|| drop.getItemId() == 43227 || drop.getItemId() == 43229 || drop.getItemId() == 43231 || drop.getItemId() == 43233 //cerberus
				|| drop.getItemId() == 49496 || drop.getItemId() == 49481 || drop.getItemId() == 49478
				|| drop.getItemId() == 4151 || drop.getItemId() == 21369
				|| drop.getItemId() == 11235 || drop.getItemId() == 15486
				|| drop.getItemId() == 42002//oblivion neck
				|| drop.getItemId() == 22498//polypore stick
				|| drop.getItemId() == 20667 //full helm
				|| drop.getItemId() == 50727 //leaf battleaxe
				|| drop.getItemId() == 51918 // dragon limb
				|| drop.getItemId() == 24365 // dragon kiteshield
				|| drop.getItemId() == 43576 //dragon warhammer
				|| drop.getItemId() == 51637 //visage
				|| drop.getItemId() == 51730 //black tourmaline core
				|| drop.getItemId() == 50714 //tome of fire
				|| drop.getItemId() == 14684 || drop.getItemId() == 10887 //barrel
				|| drop.getItemId() == 52111 || drop.getItemId() == 52006 //dragon neck & skeletal visage
				|| drop.getItemId() == 52545 || drop.getItemId() == 52550 || drop.getItemId() == 52555 || drop.getItemId() == 52557 //revenant weaps
				
				|| drop.getItemId() == 52804 //d knive
				|| drop.getItemId() == 50849 //d thrownaxes
				|| drop.getItemId() == 51028 //d harpoon
				|| drop.getItemId() == 52954 //devout boots
				|| drop.getItemId() == 52951 //boot of brimestone
				|| drop.getItemId() == 52975 //brimstone ring
				|| drop.getItemId() == 52988 //Hydra tail
				|| drop.getItemId() == 52981 //ferocious gloves
				|| drop.getItemId() == 52966 //hydra claw
				|| drop.getItemId() == 25483  //soul stone
				|| drop.getItemId() == 25477 //wings of wealth
				|| drop.getItemId() == 25622 //supreme wings of wealth
				|| drop.getItemId() == 44702//callus items v
				|| drop.getItemId() == 25695
				|| drop.getItemId() == 25696
				|| drop.getItemId() == 25697
				|| drop.getItemId() == 25698
				|| drop.getItemId() == 25700
				|| drop.getItemId() == 25701//callus items ^
				
				|| (drop.getItemId() >= 6914 && drop.getItemId() <= 6924)
				
						|| (drop.getItemId() >= 12477 && drop.getItemId() <= 12480) //dragon eggs
				|| (drop.getItemId() >= 14876 && drop.getItemId() <= 14881) //statuetes
				|| Hallowen2018.isEventItem(drop.getItemId()) //hallowen event
				|| drop.getItemId() == 54268 //basilisk jaw
				|| (drop.getItemId() >= 54417 && drop.getItemId() <= 54422) //nightmare
				|| (drop.getItemId() >= 54511 && drop.getItemId() <= 54517) //nightmare
				|| drop.getItemId() == 25739 //infinity imbue scroll
				|| (drop.getItemId() >= 25760 && drop.getItemId() <= 25762) //gwd imbue scrolls
				|| drop.getItemId() == 25502 // infernal blowpipe
				)
		return true;
		return false;
	}

	public void sendDrop(Player player, ItemDrop drop) {
		Item item = drop.get();
		if(drop.isAnnounceDrop())
			World.sendNews(player, player.getDisplayName() + " received " + item.getAmount() + " x " + item.getName() + " from "+getName()+"!", 1);

		sendDrop(player, new Drop(drop.getId(), item.getAmount(), item.getAmount()));
	}

	public Item sendDrop(Player player, Drop drop) {
		int size = getSize();
		boolean stackable = ItemConfig.forID(drop.getItemId()).isStackable();
		Item item = new Item(drop.getItemId(), drop.getMinAmount() + Utils.random(drop.getExtraAmount() * + 1));

		// O
		if (announceDrop(drop)) {
			String name = getName();
			if(getId() == 51742) {
				//dusk
				name = "Grotesque Guardians";
			}
			player.getCollectionLog().add(CategoryType.BOSSES, name, item);

			int killCount = player.getBossKillcount(getName());

			// Here we will tell the kc when a BOSS is killed
			if (killCount > 0) {
				World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + ItemConfig.forID(drop.getItemId()).getName() + "<col=ff8c38> drop at " + Utils.getFormattedNumber(killCount) + " KC!", 1);
			} else {
				World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + ItemConfig.forID(drop.getItemId()).getName() + "<col=ff8c38> drop!", 1);
			}


		}
		
		if (item.getId() == 995 && player.getPet() != null && (player.getPet().getId() == Pets.CHUNGUS.getBabyNpcId() )) 
			item.setAmount((int) (item.getAmount() * 1.1));

		boolean inRaid = ChambersOfXeric.getRaid(player) != null;
		if (player.getEquipment().getAmuletId() == 52557 && (player.getRegionId() == 12445 || player.getRegionId() == 12446 || player.getRegionId() == 12190) && !item.getDefinitions().isNoted() && item.getDefinitions().getCertId() != -1)
			item.setId(item.getDefinitions().getCertId());
		/*if ((getId() == 28030 || getId() == 28031) && item.getId() == 536 && Utils.random(getId() == 28031 ? 3 : 5) == 0)
			item.setId(52124);*/
		if (!player.isUltimateIronman() && (item.getId() == 995 || item.getId() == 43204) && (player.isDonator() || Combat.hasRingOfWealth(player)) && !player.isCanPvp()  && player.isActive(300000))
			player.getInventory().addItemMoneyPouch(item);
		else if (!player.isUltimateIronman() && !inRaid && (Combat.hasRingOfWealth(player) && item.getId() == Shop.TOKKUL) || (player.isSuperDonator() && (Drops.isCharm(item) || Drops.isSeedHerb(item))) || (player.isExtremeDonator() && (item.getId() == 43307 || Drops.isBone(item)))
				 /*&& !player.isCanPvp()*/ && player.isActive(300000)) {
			player.getBank().addItem(item.getId(), item.getAmount(), false);
			player.getPackets().sendGameMessage(item.getName() +" x"+item.getAmount()+" has been added to your bank.", true);
		} else if (!player.isUltimateIronman() && !inRaid && ItemConstants.isLooters(player.getEquipment().getAmuletId())
				&& !player.isCanPvp() && !player.isDisableAutoLoot(drop.getRarity()) && player.isActive(300000)) {
			player.getBank().addItem(item.getId(), item.getAmount(), false);
			player.getPackets().sendGameMessage( 
					(drop.getRarity() >= Drops.RARE ?
							"<col=E89002>" : "")  +
					item.getName() +" x"+item.getAmount()+" has been added to your bank.", true);	
		} else {
			FloorItem floorItem = null;
			WorldTile tile = getDropTile();
			if (!World.isFloorFree(tile.getPlane(), tile.getX(), tile.getY()))
				tile = new WorldTile(player);
			if (!stackable && item.getAmount() > 1) {
				for (int i = 0; i < item.getAmount(); i++) {
					if(inRaid) {
						floorItem = World.addCoxFloorItemNPCDrop(new Item(item.getId(), 1), tile, player);
					} else
						floorItem = World.addGroundItem(new Item(item.getId(), 1), tile, player, true, 60);
				}
			} else {
				if (inRaid) {
					floorItem = World.addCoxFloorItemNPCDrop(item, tile, player);
				} else {
					floorItem = World.addGroundItem(item, tile, player, true, 60);
				}
			}
			if (floorItem != null /*&& drop.getRarity() >= Drops.RARE
					&& drop.getItemId() != 24154 && drop.getItemId() != 24155*/
					&& announceDrop(drop)) {
				player.setLootbeam(floorItem);
			}
		}
		
		return item;
	}
	
	public WorldTile getDropTile() {
		int size = getSize();
		return this instanceof Nomad ?  NomadsRequiem.OUTSIDE : new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane());
	}

	@Override
	public int getSize() {
		return getDefinitions().boundSize;
	}

	public int getMaxHit() {
		return getCombatDefinitions().getMaxHit();
	}

	@Override
	public double[] getBonuses() {
		return bonusesD;
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0;
	}

	public WorldTile getRespawnTile() {
		return respawnTile;
	}

	public boolean isUnderCombat() {
		return combat.underCombat();
	}

	@Override
	public void setAttackedBy(Entity target) {
		super.setAttackedBy(target);
		if (target == combat.getTarget() && !(combat.getTarget() instanceof Familiar))
			lastAttackedByTarget = Utils.currentTimeMillis();
	}

	public void setLastAttackByTargetInfinite() {
		lastAttackedByTarget = Long.MAX_VALUE;
	}
	
	public boolean canBeAttackedByAutoRelatie() {
		return Utils.currentTimeMillis() - lastAttackedByTarget > lureDelay;
	}

	public boolean isForceWalking() {
		return forceWalk != null;
	}

	public void setTarget(Entity entity) {
		if (isForceWalking() || cantInteract) // if force walk not gonna get target
			return;
		combat.setTarget(entity);
		lastAttackedByTarget = Utils.currentTimeMillis();
	}

	public void removeTarget() {
		if (combat.getTarget() == null)
			return;
		combat.removeTarget();
	}

	public void forceWalkRespawnTile() {
		setForceWalk(respawnTile);
	}

	public void setForceWalk(WorldTile tile) {
		resetWalkSteps();
		forceWalk = tile;
	}

	public boolean hasForceWalk() {
		return forceWalk != null;
	}

	public List<Entity> getAllTargets() {
		return getPossibleTargets(true, true);
	}

	public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
		int size = getSize();
		int agroRatio = getCombatDefinitions().getAgroRatio();
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (int regionId : getMapRegionsIds()) {
			if (checkPlayers) {
				List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
				if (playerIndexes != null) {
					for (int playerIndex : playerIndexes) {
						Player player = World.getPlayers().get(playerIndex);
						if (player == null || player.getCutscenesManager().hasCutscene() || !player.clientHasLoadedMapRegion() || player.getPlane() != getPlane() || player.isDead() || player.hasFinished() || !player.isRunning() || player.getAppearence().isHidden() || !Utils.isOnRange(getX(), getY(), size, player.getX(), player.getY(), player.getSize(), forceTargetDistance > 0 ? forceTargetDistance : agroRatio) || (!forceMultiAttacked && (!isAtMultiArea() || !player.isAtMultiArea()) && (player.getAttackedBy() != this && (player.getAttackedByDelay() > Utils.currentTimeMillis() || player.getFindTargetDelay() > Utils.currentTimeMillis()))) || !clipedProjectile(player, false) || (!forceAgressive && !(/*Wilderness.isAtWild(this) &&*/ player.getControlerManager().getControler() instanceof Wilderness) && player.getSkills().getCombatLevelWithSummoning() > getCombatLevel() * 2)) {
							continue;
						}
						
						possibleTarget.add(player);
						if (checkNPCs) {
							Familiar familiar = player.getFamiliar();
							if (familiar == null || familiar.isDead() || familiar.isFinished() || !familiar.isAtMultiArea() || !Utils.isOnRange(getX(), getY(), size, familiar.getX(), familiar.getY(), familiar.getSize(), forceTargetDistance > 0 ? forceTargetDistance : agroRatio) || !clipedProjectile(familiar, false))
								continue;
							possibleTarget.add(familiar);
						}
					}
				}
			}
			if (checkNPCs) {
				List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
				if (npcsIndexes != null) {
					for (int npcIndex : npcsIndexes) {
						NPC npc = World.getNPCs().get(npcIndex);
						if (npc == null || npc instanceof Familiar || npc.getPlane() != getPlane() || npc == this || npc.isDead() || npc.hasFinished() || !Utils.isOnRange(getX(), getY(), size, npc.getX(), npc.getY(), npc.getSize(), forceTargetDistance > 0 ? forceTargetDistance : agroRatio) || (!npc.getDefinitions().hasAttackOption()) || ((!isAtMultiArea() || !npc.isAtMultiArea()) && npc.getAttackedBy() != this && npc.getAttackedByDelay() > Utils.currentTimeMillis()) || !clipedProjectile(npc, false) || npc.isCantInteract())
							continue;
						possibleTarget.add(npc);
					}
				}
			}
		}
		return possibleTarget;
	}

	public ArrayList<Entity> getPossibleTargets() {
		return getPossibleTargets(false, true);
	}

	public boolean checkAgressivity() {
		if (!(getId() != 22912 && Wilderness.isAtWild(this) && getDefinitions().hasAttackOption())) {
			//non wild
				if (!forceAgressive) {
					NPCCombatDefinitions defs = getCombatDefinitions();
					if (defs.getAgressivenessType() == NPCCombatDefinitions.PASSIVE)
						return false;
			}
		}
		ArrayList<Entity> possibleTarget = getPossibleTargets();
		if (!possibleTarget.isEmpty()) {
			Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
			
			if (!isForceAgressive() && target instanceof Player && !((Player) target).isActive(600000) && !Wilderness.isAtWild(this)) 
				return false;
			
			setTarget(target);
			target.setAttackedBy(target);
			target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
			return true;
		}
		return false;
	}

	public boolean isCantInteract() {
		return cantInteract;
	}

	public void setCantInteract(boolean cantInteract) {
		this.cantInteract = cantInteract;
		if (cantInteract)
			combat.reset();
	}

	public int getCapDamage() {
		return capDamage;
	}

	public void setCapDamage(int capDamage) {
		this.capDamage = capDamage;
	}

	public int getLureDelay() {
		return lureDelay;
	}

	public void setLureDelay(int lureDelay) {
		this.lureDelay = lureDelay;
	}

	public boolean isCantFollowUnderCombat() {
		return cantFollowUnderCombat;
	}

	public void setCantFollowUnderCombat(boolean canFollowUnderCombat) {
		if (cantFollowUnderCombat)
			resetWalkSteps();
		this.cantFollowUnderCombat = canFollowUnderCombat;
	}

	public Transformation getNextTransformation() {
		return nextTransformation;
	}

	@Override
	public String toString() {
		return getDefinitions().getName() + " - " + id + " - " + getX() + " " + getY() + " " + getPlane();
	}

	public boolean isForceAgressive() {
		return forceAgressive;
	}

	public void setForceAgressive(boolean forceAgressive) {
		this.forceAgressive = forceAgressive;
	}

	public int getForceTargetDistance() {
		return forceTargetDistance;
	}

	public void setForceTargetDistance(int forceTargetDistance) {
		this.forceTargetDistance = forceTargetDistance;
	}

	public boolean isForceFollowClose() {
		return forceFollowClose;
	}

	public void setForceFollowClose(boolean forceFollowClose) {
		this.forceFollowClose = forceFollowClose;
	}

	public boolean isForceMultiAttacked() {
		return forceMultiAttacked;
	}

	public void setForceMultiAttacked(boolean forceMultiAttacked) {
		this.forceMultiAttacked = forceMultiAttacked;
	}

	public void setRandomWalk(int forceRandomWalk) {
		this.walkType = forceRandomWalk;
	}

	public String getCustomName() {
		return name;
	}

	public void setName(String string) {
		this.name = getDefinitions().getName().equals(string) ? null : string;
		changedName = true;
	}

	public int getCustomCombatLevel() {
		return combatLevel;
	}

	public int getCombatLevel() {
		return combatLevel >= 0 ? combatLevel : getDefinitions().combatLevel;
	}

	public String getName() {
		return name != null ? name : getDefinitions().getName();
	}

	public void setCombatLevel(int level) {
		combatLevel = getDefinitions().combatLevel == level ? -1 : level;
		changedCombatLevel = true;
	}

	public boolean hasChangedName() {
		return changedName;
	}

	public boolean hasChangedCombatLevel() {
		return changedCombatLevel;
	}

	public boolean isSpawned() {
		return spawned;
	}

	public void setSpawned(boolean spawned) {
		this.spawned = spawned;
	}

	public boolean isNoDistanceCheck() {
		return noDistanceCheck;
	}

	public void setNoDistanceCheck(boolean noDistanceCheck) {
		this.noDistanceCheck = noDistanceCheck;
	}

	public boolean withinDistance(Player tile, int distance) {
		return super.withinDistance(tile, distance);
	}

	/**
	 * Gets the locked.
	 * 
	 * @return The locked.
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * Sets the locked.
	 * 
	 * @param locked
	 *            The locked to set.
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isIntelligentRouteFinder() {
		return intelligentRouteFinder;
	}

	public void setIntelligentRouteFinder(boolean intelligentRouteFinder) {
		this.intelligentRouteFinder = intelligentRouteFinder;
	}

	public double getDropRateFactor() {
		return dropRateFactor;
	}

	public void setDropRateFactor(double dropRateFactor) {
		this.dropRateFactor = dropRateFactor;
	}

	public SecondaryBar getNextSecondaryBar() {
		return nextSecondaryBar;
	}

	public void setNextSecondaryBar(SecondaryBar secondaryBar) {
		this.nextSecondaryBar = secondaryBar;
	}

	public boolean isCantSetTargetAutoRelatio() {
		return cantSetTargetAutoRelatio;
	}

	public void setCantSetTargetAutoRelatio(boolean cantSetTargetAutoRelatio) {
		this.cantSetTargetAutoRelatio = cantSetTargetAutoRelatio;
	}

	@Override
	public boolean canMove(int dir) {
		return true;
	}

	public void setStopRandomWalk() {
		stopRandomWalk = Utils.currentTimeMillis() + 2000;
	}

	@Override
	public int getHitbarSprite(Player player) {
		int maxHP = getMaxHitpoints();
		if (!player.isOsrsHitbars()) {
			if (maxHP  >= 4000)
				return 1509;
			if (maxHP >= 2000)
				return 1771;
		} else { //osrs hitbars
			if (maxHP  >= 4000)
				return 22191;
			if (maxHP >= 3000)
				return 22189;
			if (maxHP  >= 2000)
				return 22181;
			if (maxHP >= 1000)
				return 22179;
		}
		return super.getHitbarSprite(player); //493
	}

	public boolean isForceLootshare() {
		return 	(EconomyManager.tileEventHappening && EconomyManager.eventTile.withinDistance(this, 64)) || forceLootshare;
	}

	public void setForceLootshare(boolean forceLootshare) {
		this.forceLootshare = forceLootshare;
	}
	
	public NPC setBossInstance(BossInstance instance) {
		bossInstance = instance;
		setForceMultiArea(true); //for now all instanced bosses multi
		return this;
	}
	
	public BossInstance getBossInstance() {
		return bossInstance;
	}

    public boolean preAttackCheck(Player attacker) {
		return true;
    }

	public void lock() {
		setLocked(true);
	}

	public void unlock() {
		setLocked(false);
	}

	private int originalId = 0;

	public void hideNPC() {
		originalId = getId();
		setNextNPCTransformation(26800); // invisible npc no options/minimap icon
	}
	public void unhideNPC() {
		setNextNPCTransformation(originalId);
	}

	public boolean canWalkNPC() {
		return canWalkNPC;
	}

	/**
	 * Can walk ontop of other NPCs
	 */
	public void setCanWalkNPC(boolean canWalkNPC) {
		this.canWalkNPC = canWalkNPC;
	}

	private boolean undeadNPC;

	public void setUndeadNPC(boolean undeadNPC) {
		this.undeadNPC = undeadNPC;
	}

	public boolean isUndead() {
		if(undeadNPC)
			return true;
		String name = getDefinitions().getName().toLowerCase();
		// should move this to constructor
		return name.contains("skeleton") || name.contains("aberrant spectre") || name.contains("zombi") || name.contains("ankou") || name.contains("crawling hand") || name.contains("ghost") || name.contains("ghast") || name.contains("mummy") || name.contains("revenant") || name.contains("shade") || getId() == 8125 || (getId() >= 2044 && getId() <= 2057) || name.contains("undead") || (getId() >= 26611 && getId() <= 26614)
				|| getId() == 28061 //vorkath
				|| getId() == 28359;
	}

	public boolean isHMInstance() {
		return bossInstance != null && bossInstance.getSettings().isHardMode();
	}
	
}
