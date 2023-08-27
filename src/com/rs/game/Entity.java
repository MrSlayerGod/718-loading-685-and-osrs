package com.rs.game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.rs.Settings;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ObjectConfig;
import com.rs.executor.WorldThread;
import com.rs.game.Hit.HitLook;
import com.rs.game.item.Item;
import com.rs.game.minigames.lms.LastManStanding;
import com.rs.game.minigames.lms.LastManStandingGame;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.minigames.stealingcreation.StealingCreationController;
import com.rs.game.npc.NPC;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.npc.cox.impl.*;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.inferno.Pillar;
import com.rs.game.npc.inferno.TzalZuk;
import com.rs.game.npc.nightmare.Totem;
import com.rs.game.npc.nomad.Nomad;
import com.rs.game.npc.others.Pet;
import com.rs.game.npc.qbd.TorturedSoul;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.Magic;
import com.rs.game.npc.cox.impl.VasaCrystal;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.seasonalEvents.Easter2021;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.events.DeathEvent;
import com.rs.game.player.controllers.pktournament.PkTournamentGame;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.EntityStrategy;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Direction;
import com.rs.utils.Utils;

public abstract class Entity extends WorldTile {

	private static final long serialVersionUID = -3372926325008880753L;
	private final static AtomicInteger hashCodeGenerator = new AtomicInteger();

	// transient stuff
	private transient int index;
	private transient int lastRegionId; // the last region the entity was at
	private transient WorldTile lastLoadedMapRegionTile;
	private transient long forceMovementCycle;
	private transient CopyOnWriteArrayList<Integer> mapRegionsIds; // called by
	// more than
	// 1thread
	// so
	// concurent
	private transient int direction;
	private transient WorldTile lastWorldTile;
	private transient WorldTile nextWorldTile;
	private transient int nextWalkDirection;
	private transient int nextRunDirection;
	private transient Rectangle nextFaceWorldTile;
	private transient boolean teleported;
	private transient ConcurrentLinkedQueue<Object[]> walkSteps;// called by more
	// than 1thread
	// so concurent
	private transient ConcurrentLinkedQueue<Hit> receivedHits;
	private transient Map<Entity, Integer> receivedDamage;
	private transient boolean finished; // if removed
	private transient long freezeDelay;
	// entity masks
	private transient Animation nextAnimation;
	private transient Graphics nextGraphics1;
	private transient Graphics nextGraphics2;
	private transient Graphics nextGraphics3;
	private transient Graphics nextGraphics4;
	private transient ArrayList<Hit> nextHits;
	private transient boolean refreshHitbars; //to change hitbar look
	private transient ForceMovement nextForceMovement;
	private transient ForceTalk nextForceTalk;
	private transient int nextFaceEntity;
	private transient int lastFaceEntity;
	private transient boolean refreshClanIcon;


	private transient Entity attackedBy; // whos attacking you, used for single
	private transient long attackedByDelay; // delay till someone else can
	// attack you
	private transient boolean multiArea;
	private transient boolean isAtDynamicRegion;
	private transient long lastAnimationEnd;
	private transient boolean forceMultiArea;
	private transient long frozenBlocked;
	private transient long findTargetDelay;
	private transient ConcurrentHashMap<Object, Object> temporaryAttributes;
	private transient int hashCode;

	// saving stuff
	private int hitpoints;
	private int mapSize; // default 0, can be setted other value usefull on
	// static maps
	private boolean run;
	private Poison poison;
	public WorldTile previousWalkTile = null;

	// creates Entity and saved classes
	public Entity(WorldTile tile) {
		super(tile);
		poison = new Poison();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	public final void initEntity() {
		hashCode = hashCodeGenerator.getAndIncrement();
		mapRegionsIds = new CopyOnWriteArrayList<Integer>();
		walkSteps = new ConcurrentLinkedQueue<Object[]>();
		receivedHits = new ConcurrentLinkedQueue<Hit>();
		receivedDamage = new ConcurrentHashMap<Entity, Integer>();
		temporaryAttributes = new ConcurrentHashMap<Object, Object>();
		nextHits = new ArrayList<Hit>();
		nextWalkDirection = nextRunDirection - 1;
		lastFaceEntity = -1;
		nextFaceEntity = -2;
		poison.setEntity(this);
	}

	public int getReceivedDamage(Entity source) {
		int receivedDamage = 0;
		for (Hit hit : receivedHits) {
			if (hit.getSource() != source)
				continue;
			receivedDamage += hit.getDamage();
		}
		return receivedDamage;
	}

	public int getClientIndex() {
		return index + (this instanceof Player ? 32768 : 0);
	}

	public void applyHit(Hit hit) {
		if (isDead() || isTeleporting()/* || (isPlayer() && asPlayer().isLocked())*/)
			return;
		Entity source = hit.getSource();
		if (source != null) 
			hit = source.handleOutgoingHit(hit, this);
		// todo damage for who gets drop
		receivedHits.add(hit); // added hit first because, soaking added after,
		// if applyhit used right there shouldnt be any
		// problem
		handleIngoingHit(hit);
		if(hit.getSource() != null && hit.getSource().isPlayer()) {
			Player p = hit.getSource().asPlayer();
			if(Easter2021.ENABLED && hit.getLook() != HitLook.HEALED_DAMAGE) {
				Easter2021.checkEggDrop(p, hit.getDamage(), this);
			}
			if(hit.getDamage() > 0) {
				if (this instanceof COXBoss && source instanceof Player) {
					ChambersOfXeric.addDamagePoints(source.asPlayer(), (COXBoss) this, hit.getDamage());
				}
			}
		}
	}

	/**
	 * Where the hit came from.
	 * @param hit The hit.
	 */
	@Deprecated
	public Hit handleOutgoingHit(Hit hit, Entity target) {
		//EMPTY
		return hit;
	}

	public abstract void handleIngoingHit(Hit hit);

	public void reset(boolean attributes) {
		setHitpoints(getMaxHitpoints());
		receivedHits.clear();
		resetCombat();
		walkSteps.clear();
		poison.reset();
		resetReceivedDamage();
		setAttackedBy(null);
		setAttackedByDelay(0);
		if (attributes)
			temporaryAttributes.clear();
	}

	public void reset() {
		reset(true);
	}

	public void resetCombat() {
		attackedBy = null;
		attackedByDelay = 0;
		freezeDelay = 0;
	}

	public void processReceivedHits() {
		if (this instanceof Player) {
			if (((Player) this).getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis())
				return;
		}
		Hit hit;
		int count = 0;
		while ((hit = receivedHits.poll()) != null && count++ < 10)
			processHit(hit);
	}

	public void processHit(Hit hit) {
		if (isDead())
			return;
		if (hit.getLook() == HitLook.HEALED_DAMAGE) {
			if (getHitpoints() + hit.getDamage() > getMaxHitpoints())
				hit.setDamage(getMaxHitpoints() - getHitpoints());
			if (hit.getDamage() <= 0)
				return;
		}
		if (hit.getSource() != null && !hit.getSource().withinDistance(this, 64)) 
			return;
		removeHitpoints(hit);
		nextHits.add(hit);
	}
	
	public boolean hasHits() {
		return !receivedHits.isEmpty();
	}

	public void resetReceivedHits() {
		nextHits.clear();
		receivedHits.clear();
	}

	public void removeHitpoints(final Hit hit) {
		if (isDead() || hit.getLook() == HitLook.ABSORB_DAMAGE)
			return;
		if (hit.getLook() == HitLook.HEALED_DAMAGE) {
			heal(hit.getDamage());
			return;
		}
		if (hit.getDamage() > hitpoints)
			hit.setDamage(hitpoints);
		addReceivedDamage(hit.getSource(), hit.getDamage());
		setHitpoints(hitpoints - hit.getDamage());
		if (hitpoints <= 0)
			sendDeath(hit.getSource());
		else if (this instanceof Player) {
			final Player player = (Player) this;
			if (player.getEquipment().getRingId() == 25741 || player.getEquipment().getRingId() == 25488 || player.getEquipment().getRingId() == 2550 || player.getEquipment().getRingId() == 49550 || player.getEquipment().getRingId() == 49710) {
				if (hit.getSource() != null && hit.getSource() != player && hit.getDamage() > 0 && (hit.getLook() == HitLook.MELEE_DAMAGE || hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MAGIC_DAMAGE)) {
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							hit.getSource().applyHit(new Hit(player, (int) (Math.ceil(hit.getDamage() * 0.01) * 10), HitLook.REGULAR_DAMAGE));
						}
					});
				}
			}
			if (player.getPrayer().hasPrayersOn()) {
				if ((hitpoints < player.getMaxHitpoints() * 0.1) && player.getPrayer().usingPrayer(0, 22)) {
					setNextGraphics(new Graphics(436));
					setHitpoints((int) (hitpoints + player.getSkills().getLevelForXp(Skills.PRAYER) * 2.5));
					player.getSkills().set(Skills.PRAYER, 0);
					player.getPrayer().setPrayerpoints(0);
				}
			}
			if (player.getEquipment().getAmuletId() == 11090 && player.getHitpoints() <= player.getMaxHitpoints() * 0.2) {// priority
				// over
				// ring
				// of
				// life
				player.heal((int) (player.getMaxHitpoints() * 0.3));
				resetReceivedHits();
				player.getEquipment().deleteItem(11090, 1);
				player.getPackets().sendGameMessage("Your pheonix necklace heals you, but is destroyed in the process.");
			}else if (player.getEquipment().getAmuletId() != 11090 && player.getEquipment().getRingId() == 2570 && player.getHitpoints() <= player.getMaxHitpoints() * 0.1) {
				Magic.sendNormalTeleportSpell(player, 1, 0, DeathEvent.getRespawnHub(player));
				player.getEquipment().deleteItem(2570, 1);
				player.getPackets().sendGameMessage("Your ring of life saves you, but is destroyed in the process.");
			}
		} 
	}

	public void resetReceivedDamage() {
		receivedDamage.clear();
	}

	public void reduceDamage(Entity entity) {
		int dmg = getDamageReceived(entity);
		if(dmg != -1)
			receivedDamage.put(entity, dmg / 5);
	}

	public Set<Entity> getReceivedDamageSources() {
		return receivedDamage.keySet();
	}

	public List<Player> getForceLootSharingPeople() {
		List<Player> players = new ArrayList<Player>();
		for (Entity source : receivedDamage.keySet()) {
			if (!(source instanceof Player))
				continue;
			Integer d = receivedDamage.get(source);
			if (d == null || (this instanceof Player && source.hasFinished())) {
				receivedDamage.remove(source);
				continue;
			}
			if (!players.contains((Player) source)) //prevent glitch
				players.add((Player) source);
		}
		return players;
	}
	
	public Player getMostDamageReceivedSourcePlayer() {
		Player player = null;
		int damage = -1;
		for (Entity source : receivedDamage.keySet()) {
			if (!(source instanceof Player) || (this instanceof Player && !source.withinDistance(this, 64)))
				continue;
			Integer d = receivedDamage.get(source);
			if (d == null || (this instanceof Player && source.hasFinished())) {
				receivedDamage.remove(source);
				continue;
			}
			if (d > damage) {
				player = (Player) source;
				damage = d;
			}
		}
		return player;
	}

	public int getDamageReceived(Entity source) {
		Integer d = receivedDamage.get(source);
		if (d == null ||  (this instanceof Player && source.hasFinished())) {
			receivedDamage.remove(source);
			return -1;
		}
		return d;
	}

	public void processReceivedDamage() {
		if (isDead())
			return;
		for (Entity source : receivedDamage.keySet()) {
			Integer damage = receivedDamage.get(source);
			if (damage == null ||  (this instanceof Player && source.hasFinished())) {
				receivedDamage.remove(source);
				continue;
			}
			damage--;
			if (damage == 0) {
				receivedDamage.remove(source);
				continue;
			}
			receivedDamage.put(source, damage);
		}
	}

	public void addReceivedDamage(Entity source, int amount) {
		if (source == null || source == this)
			return;
		if (source instanceof Familiar)
			source = ((Familiar)source).getOwner();
		Integer damage = receivedDamage.get(source);
		damage = damage == null ? amount : damage + amount;
		if (damage < 0)
			receivedDamage.remove(source);
		else
			receivedDamage.put(source, damage);
	}

	public void heal(int ammount) {
		heal(ammount, 0);
	}

	public void heal(int ammount, int extra) {
		if (isDead()) //called late for a dead target
			return;
		int hp = (hitpoints + ammount) >= (getMaxHitpoints() + extra) ? (getMaxHitpoints() + extra) : (hitpoints + ammount);
		if (hitpoints > hp)
			return;
		setHitpoints((hitpoints + ammount) >= (getMaxHitpoints() + extra) ? (getMaxHitpoints() + extra) : (hitpoints + ammount));
	}

	public boolean hasWalkSteps() {
		return !walkSteps.isEmpty();
	}

	public abstract void sendDeath(Entity source);

	public void processMovement() {
		lastWorldTile = new WorldTile(this);
		if (lastFaceEntity >= 0) {
			Entity target = lastFaceEntity >= 32768 ? World.getPlayers().get(lastFaceEntity - 32768) : World.getNPCs().get(lastFaceEntity);
			if (target != null) {
				int size = target.getSize();
				updateAngle(target, size, size);
			}
			//direction = Utils.getFaceDirection(target.getCoordFaceX(target.getSize()) - getCoordFaceX(getSize()), target.getCoordFaceY(target.getSize()) - getCoordFaceY(getSize()));
		}
		nextWalkDirection = nextRunDirection = -1;
		if (nextWorldTile != null) {
			// int lastPlane = getPlane();
			setLocation(nextWorldTile);
			nextWorldTile = null;
			teleported = true;
			//not required anymore due to new gpi change
			/*	if (this instanceof Player && ((Player) this).getTemporaryMoveType() == -1)
				((Player) this).setTemporaryMoveType(Player.TELE_MOVE_TYPE);*/
			World.updateEntityRegion(this);
		/*	if (needMapUpdate() && forceMovementCycle < WorldThread.WORLD_CYCLE)
				loadMapRegions();*/
			/*   else if (this instanceof Player && lastPlane != getPlane())
			((Player) this).setClientHasntLoadedMapRegion();*/
			resetWalkSteps();
			return;
		}
		teleported = false;
		if (walkSteps.isEmpty())
			return;
		if (this instanceof Player) { //emotes are special on rs, when using one u will walk once emote done
			if (((Player) this).getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis())
				return;
		}
		if (this instanceof TorturedSoul) { //waste of process power personaly but meh.
			if (((TorturedSoul) this).switchWalkStep()) {
				return;
			}
		}
		if (this instanceof Player && ((Player) this).getRunEnergy() <= 0)
			setRun(false);

		skippedRunStep = null;
		previousWalkTile = clone();

		for (int stepCount = 0; stepCount < (run ? 2 : 1); stepCount++) {
			Object[] nextStep = getNextWalkStep();
			if (nextStep == null)
				break;
			int dir = (int) nextStep[0];
			if (((boolean) nextStep[3] && !World.checkWalkStep(getPlane(), getX(), getY(), dir, getSize())) || (this instanceof NPC && !canWalkNPC(getX() + Utils.DIRECTION_DELTA_X[dir], getY() + Utils.DIRECTION_DELTA_Y[dir])) || !canMove(dir)) {
				resetWalkSteps();
				break;
			}
			if (stepCount == 0)
				nextWalkDirection = dir;
			else
				nextRunDirection = dir;
			moveLocation(Utils.DIRECTION_DELTA_X[dir], Utils.DIRECTION_DELTA_Y[dir], 0);
			if(stepCount == 0 && run) {
				skippedRunStep = this.clone();
			}
			if (run && stepCount == 0) { //fixes impossible steps
				Object[] previewStep = previewNextWalkStep();
				if (previewStep == null)
					break;
				int previewDir = (int) previewStep[0];
				if (Utils.getPlayerRunningDirection(Utils.DIRECTION_DELTA_X[dir] + Utils.DIRECTION_DELTA_X[previewDir], Utils.DIRECTION_DELTA_Y[dir] + Utils.DIRECTION_DELTA_Y[previewDir]) == -1)
					break;
			}
		}
		
		if (this instanceof Player && run && nextWalkDirection != -1 && nextRunDirection == -1) { //fix 1 step run
			Player player = (Player) this;
			player.setTemporaryMoveType(Player.WALK_MOVE_TYPE);
		}
		
		World.updateEntityRegion(this);
	}

	public WorldTile skippedRunStep = new WorldTile(0,0,0);
	
	public void processMapLoad() {
		if (needMapUpdate() && forceMovementCycle < WorldThread.WORLD_CYCLE)
			loadMapRegions();
	}

	public abstract boolean canMove(int dir);

	@Override
	public void moveLocation(int xOffset, int yOffset, int planeOffset) {
		super.moveLocation(xOffset, yOffset, planeOffset);
		direction = Utils.getAngle(xOffset, yOffset);
	}

	private boolean needMapUpdate() {
		return needMapUpdate(lastLoadedMapRegionTile);
	}

	public boolean needMapUpdate(WorldTile tile) {
		if (tile == null)
			return false;
		int lastMapRegionX = tile.getChunkX();
		int lastMapRegionY = tile.getChunkY();
		int regionX = getChunkX();
		int regionY = getChunkY();
		int size = ((Settings.MAP_SIZES[mapSize] >> 3) / 2) - 1;
		return Math.abs(lastMapRegionX - regionX) >= size || Math.abs(lastMapRegionY - regionY) >= size;
	}

	//normal walk steps method
	public boolean addWalkSteps(int destX, int destY) {
		return addWalkSteps(destX, destY, -1);
	}

	public boolean clipedProjectile(WorldTile tile, boolean checkClose) {
		if (tile instanceof Pillar || tile instanceof TzalZuk || tile instanceof VerzikVitur
				|| tile instanceof Totem || tile instanceof GreatOlmRightClaw || tile instanceof GreatOlmLeftClaw || tile instanceof GreatOlm
				|| tile instanceof ChambersGuardian || this instanceof ChambersGuardian || tile instanceof VespulaGrub
				|| tile instanceof VasaCrystal || this instanceof VespulaGrub || this instanceof MuttadileMother
		|| (tile instanceof NPC && ((NPC) tile).getId() == 27316)) {
			return true;
		}
		/*if (tile instanceof Entity) {
			Entity e = (Entity) tile;
			WorldTile me = this;
			if (tile instanceof NPC) {
				NPC n = (NPC) tile;
				tile = n.getMiddleWorldTile();
				System.out.println(n.getSize()+", "+n.getName());
			} else if (this instanceof NPC) {
				NPC n = (NPC) this;
				me = n.getMiddleWorldTile();
			}
			return clipedProjectile(tile, checkClose, 1) || e.clipedProjectile(me, checkClose, 1);
		}*/
		if (tile instanceof Entity) {
			Entity target = (Entity) tile;
			return clipedProjectile(tile, checkClose, 1) || target.clipedProjectile(this, checkClose, 1);
		}
		return clipedProjectile(tile, checkClose, 1);
	}

	public boolean clipedProjectile(WorldTile tile, boolean checkClose, int size) {
		int myX = getX();
		int myY = getY();
		if (this instanceof NPC) {
			NPC n = (NPC) this;
			WorldTile thist = n.getMiddleWorldTile();
			myX = thist.getX();
			myY = thist.getY();
		} 
		if (tile instanceof NPC) {
			NPC n = (NPC) tile;
			if (n.getSize() % 2 == 0 && clipedProjectile(tile.transform(1, 1, 0), checkClose, size)) //mid too
				return true;
			tile = n.getMiddleWorldTile();
		}
		int destX = tile.getX();
		int destY = tile.getY();
		if (myX == destX && destY == myY)
			return true;
		int lastTileX = myX;
		int lastTileY = myY;
		while (true) {
			if (myX < destX)
				myX++;
			else if (myX > destX)
				myX--;
			if (myY < destY)
				myY++;
			else if (myY > destY)
				myY--;
			int dir = Utils.getMoveDirection(myX - lastTileX, myY - lastTileY);
			if (dir == -1)
				return false;
			if (checkClose) {
				if (!World.checkWalkStep(getPlane(), lastTileX, lastTileY, dir, size))
					return false;
			} else if (!World.checkProjectileStep(getPlane(), lastTileX, lastTileY, dir, size))
				return false;
			lastTileX = myX;
			lastTileY = myY;
			if (lastTileX == destX && lastTileY == destY)
				return true;
		}
	}

	public boolean calcFollow(WorldTile target, boolean inteligent) {
		return calcFollow(target, -1, true, inteligent);
	}

	//checks collisions
	public boolean canWalkNPC(int toX, int toY) {
		//stucking nomad is part of strategy
		if (this instanceof Pet || this instanceof Familiar || this instanceof Nomad || ((NPC) this).isIntelligentRouteFinder() || ((NPC) this).isForceWalking()
				|| !((NPC) this).getDefinitions().drawMapdot)
			// use npc.setCanWalkNPC(true) instead for single cases
			return true;
		if(this instanceof NPC && ((NPC) this).canWalkNPC()) {
			return true;
		}
		int size = getSize();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> npcIndexes = World.getRegion(regionId).getNPCsIndexes();
			if (npcIndexes != null/* && npcIndexes.size() < 100*/) {
				for (int npcIndex : npcIndexes) {
					NPC target = World.getNPCs().get(npcIndex);
					if (target == null || target == this || target.isDead() || target.hasFinished() || target.getPlane() != getPlane() || target instanceof Familiar
							|| !target.getDefinitions().drawMapdot || target instanceof Pet)
						continue;
					int targetSize = target.getSize();
					//npc is under this target so skip checking it
					if (Utils.collides(this, target))
						continue;
					WorldTile tile = new WorldTile(target);
					//has to be checked aswell, cuz other one assumes npc will manage to move no matter what
					if (Utils.colides(toX, toY, size, tile.getX(), tile.getY(), targetSize))
						return false;
					if (target.getNextWalkDirection() != -1) {
						tile.moveLocation(Utils.DIRECTION_DELTA_X[target.getNextWalkDirection()], Utils.DIRECTION_DELTA_Y[target.getNextWalkDirection()], 0);
						if (target.getNextRunDirection() != -1)
							tile.moveLocation(Utils.DIRECTION_DELTA_X[target.getNextRunDirection()], Utils.DIRECTION_DELTA_Y[target.getNextRunDirection()], 0);
						//target is at x,y
						if (Utils.colides(toX, toY, size, tile.getX(), tile.getY(), targetSize))
							return false;
					}
				}
			}
		}
		return true;
	}

	private int getPreviewNextWalkStep() {
		Object[] step = walkSteps.poll();
		if (step == null)
			return -1;
		return (int) step[0];
	}

	private Object[] previewNextWalkStep() {
		Object[] step = walkSteps.peek();
		if (step == null)
			return null;
		return step;
	}

	//checks collisions
	public boolean canWalkNPC(int toX, int toY, boolean checkUnder) {
		if (!isAtMultiArea() /*
		 * || (!checkUnder && !canWalkNPC(getX(), getY(),
		 * true))
		 */)
			return true;
		int size = getSize();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> npcIndexes = World.getRegion(regionId).getNPCsIndexes();
			if (npcIndexes != null && npcIndexes.size() < 50) {
				for (int npcIndex : npcIndexes) {
					NPC target = World.getNPCs().get(npcIndex);
					if (target == null || target == this || target.isDead() || target.hasFinished() || target.getPlane() != getPlane() || !target.isAtMultiArea() || (!(this instanceof Familiar) && target instanceof Familiar))
						continue;
					int targetSize = target.getSize();
					if (!checkUnder && target.getNextWalkDirection() == -1) { // means
						// the
						// walk
						// hasnt
						// been
						// processed
						// yet
						int previewDir = getPreviewNextWalkStep();
						if (previewDir != -1) {
							WorldTile tile = target.transform(Utils.DIRECTION_DELTA_X[previewDir], Utils.DIRECTION_DELTA_Y[previewDir], 0);
							if (Utils.colides(tile.getX(), tile.getY(), targetSize, getX(), getY(), size))
								continue;

							if (Utils.colides(tile.getX(), tile.getY(), targetSize, toX, toY, size))
								return false;
						}
					}
					if (Utils.colides(target.getX(), target.getY(), targetSize, getX(), getY(), size))
						continue;
					if (Utils.colides(target.getX(), target.getY(), targetSize, toX, toY, size))
						return false;
				}
			}
		}
		return true;
	}

	public WorldTile getMiddleWorldTile() {
		int size = getSize();
		return size == 1 ? this : new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane());
	}

	public static boolean findBasicRoute(Entity src, WorldTile dest, int maxStepsCount, boolean calculate) {
		int[] srcPos = src.getLastWalkTile();
		int[] destPos =
			{ dest.getX(), dest.getY() };
		int srcSize = src.getSize();
		//set destSize to 0 to walk under it else follows
		int destSize = dest instanceof Entity ? ((Entity) dest).getSize() : 1;
		int[] destScenePos =
			{ destPos[0] + destSize - 1, destPos[1] + destSize - 1 };//Arrays.copyOf(destPos, 2);//destSize == 1 ? Arrays.copyOf(destPos, 2) : new int[] {WorldTile.getCoordFaceX(destPos[0], destSize, destSize, -1), WorldTile.getCoordFaceY(destPos[1], destSize, destSize, -1)};
		while (maxStepsCount-- != 0) {
			int[] srcScenePos =
				{ srcPos[0] + srcSize - 1, srcPos[1] + srcSize - 1 };//srcSize == 1 ? Arrays.copyOf(srcPos, 2) : new int[] { WorldTile.getCoordFaceX(srcPos[0], srcSize, srcSize, -1), WorldTile.getCoordFaceY(srcPos[1], srcSize, srcSize, -1)};
			if (!Utils.isOnRange(srcPos[0], srcPos[1], srcSize, destPos[0], destPos[1], destSize, 0)) {
				if (srcScenePos[0] < destScenePos[0] && srcScenePos[1] < destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] + 1, srcPos[1] + 1)) && src.addWalkStep(srcPos[0] + 1, srcPos[1] + 1, srcPos[0], srcPos[1], true)) {
					srcPos[0]++;
					srcPos[1]++;
					continue;
				}
				if (srcScenePos[0] > destScenePos[0] && srcScenePos[1] > destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] - 1, srcPos[1] - 1)) && src.addWalkStep(srcPos[0] - 1, srcPos[1] - 1, srcPos[0], srcPos[1], true)) {
					srcPos[0]--;
					srcPos[1]--;
					continue;
				}
				if (srcScenePos[0] < destScenePos[0] && srcScenePos[1] > destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] + 1, srcPos[1] - 1)) && src.addWalkStep(srcPos[0] + 1, srcPos[1] - 1, srcPos[0], srcPos[1], true)) {
					srcPos[0]++;
					srcPos[1]--;
					continue;
				}
				if (srcScenePos[0] > destScenePos[0] && srcScenePos[1] < destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] - 1, srcPos[1] + 1)) && src.addWalkStep(srcPos[0] - 1, srcPos[1] + 1, srcPos[0], srcPos[1], true)) {
					srcPos[0]--;
					srcPos[1]++;
					continue;
				}
				if (srcScenePos[0] < destScenePos[0] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] + 1, srcPos[1])) && src.addWalkStep(srcPos[0] + 1, srcPos[1], srcPos[0], srcPos[1], true)) {
					srcPos[0]++;
					continue;
				}
				if (srcScenePos[0] > destScenePos[0] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] - 1, srcPos[1])) && src.addWalkStep(srcPos[0] - 1, srcPos[1], srcPos[0], srcPos[1], true)) {
					srcPos[0]--;
					continue;
				}
				if (srcScenePos[1] < destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0], srcPos[1] + 1)) && src.addWalkStep(srcPos[0], srcPos[1] + 1, srcPos[0], srcPos[1], true)) {
					srcPos[1]++;
					continue;
				}
				if (srcScenePos[1] > destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0], srcPos[1] - 1)) && src.addWalkStep(srcPos[0], srcPos[1] - 1, srcPos[0], srcPos[1], true)) {
					srcPos[1]--;
					continue;
				}
				return false;
			}
			break; 	//for now nothing between break and return
		}
		return true;
	}

	//used for normal npc follow int maxStepsCount, boolean calculate used to save mem on normal path
	public boolean calcFollow(WorldTile target, int maxStepsCount, boolean calculate, boolean inteligent) {
		if (inteligent) {
			int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), target instanceof WorldObject ? new ObjectStrategy((WorldObject) target) : target instanceof Entity ? new EntityStrategy((Entity) target) : new FixedTileStrategy(target.getX(), target.getY()), true);
			if (steps == -1)
				return false;
			if (steps == 0)
				return true;
			int[] bufferX = RouteFinder.getLastPathBufferX();
			int[] bufferY = RouteFinder.getLastPathBufferY();
			for (int step = steps - 1; step >= 0; step--) {
				if (!addWalkSteps(bufferX[step], bufferY[step], 25, true))
					break;
			}
			return true;
		}
		return findBasicRoute(this, target, maxStepsCount, true);
		/*else if (true == true) { //just keeping old code
		    System.out.println("test");
		    return findBasicRoute(this, (Entity) target, maxStepsCount, true);
		}

		int[] lastTile = getLastWalkTile();
		int myX = lastTile[0];
		int myY = lastTile[1];
		int stepCount = 0;
		int size = getSize();
		int destX = target.getX();
		int destY = target.getY();
		int sizeX = target instanceof WorldObject ? ((WorldObject)target).getDefinitions().getSizeX() : ((Entity)target).getSize();
		int sizeY = target instanceof WorldObject ? ((WorldObject)target).getDefinitions().getSizeY() : sizeX;
		while (true) {
		    stepCount++;
		    int myRealX = myX;
		    int myRealY = myY;
		    if (Utils.isOnRange(myX, myY, size, destX, destY, sizeX, 0))
			return true;
		    if (myX < destX)
			myX++;
		    else if (myX > destX)
			myX--;
		    if (myY < destY)
			myY++;
		    else if (myY > destY)
			myY--;
		    if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastTile[0], lastTile[1], true)) {
			if (!calculate)
			    return false;
			myX = myRealX;
			myY = myRealY;
			int[] myT = calculatedStep(myRealX, myRealY, destX, destY, lastTile[0], lastTile[1], sizeX, sizeY);
			if (myT == null)
			    return false;
			myX = myT[0];
			myY = myT[1];
		    }
		    if (stepCount == maxStepsCount)
			return true;
		    lastTile[0] = myX;
		    lastTile[1] = myY;
		    if (lastTile[0] == destX && lastTile[1] == destY)
			return true;
		}*/
	}

	//used for normal npc follow
	@SuppressWarnings("unused")
	private int[] calculatedStep(int myX, int myY, int destX, int destY, int lastX, int lastY, int sizeX, int sizeY) {
		if (myX < destX) {
			myX++;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true))
				myX--;
			else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[]
						{ myX, myY };
			}
		} else if (myX > destX) {
			myX--;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true))
				myX++;
			else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[]
						{ myX, myY };
			}
		}
		if (myY < destY) {
			myY++;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true))
				myY--;
			else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[]
						{ myX, myY };
			}
		} else if (myY > destY) {
			myY--;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true)) {
				myY++;
			} else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[]
						{ myX, myY };
			}
		}
		if (myX == lastX || myY == lastY)
			return null;
		return new int[]
				{ myX, myY };
	}

	/*
	 * return added all steps
	 */
	public boolean addWalkSteps(final int destX, final int destY, int maxStepsCount) {
		return addWalkSteps(destX, destY, -1, true);
	}

	/*
	 * return added all steps
	 */
	public boolean addWalkSteps(final int destX, final int destY, int maxStepsCount, boolean check) {
		int[] lastTile = getLastWalkTile();
		int myX = lastTile[0];
		int myY = lastTile[1];
		int stepCount = 0;
		while (true) {
			stepCount++;
			if (myX < destX)
				myX++;
			else if (myX > destX)
				myX--;
			if (myY < destY)
				myY++;
			else if (myY > destY)
				myY--;
			if (!addWalkStep(myX, myY, lastTile[0], lastTile[1], check))
				return false;
			if (stepCount == maxStepsCount)
				return true;
			lastTile[0] = myX;
			lastTile[1] = myY;
			if (lastTile[0] == destX && lastTile[1] == destY)
				return true;
		}
	}


	public abstract double[] getBonuses();

	private int[] getLastWalkTile() {
		Object[] objects = walkSteps.toArray();
		if (objects.length == 0)
			return new int[]
					{ getX(), getY() };
		Object step[] = (Object[]) objects[objects.length - 1];
		return new int[]
				{ (int) step[1], (int) step[2] };
	}

	public boolean addWalkStep(int nextX, int nextY, int lastX, int lastY, boolean check) {
		int dir = Utils.getMoveDirection(nextX - lastX, nextY - lastY);
		if (dir == -1)
			return false;
		if (check && !World.checkWalkStep(getPlane(), lastX, lastY, dir, getSize()))//double check must be done sadly cuz of npc under check, can be improved later to only check when we want
			return false;
		if (this instanceof Player) {
			if (!((Player) this).getControlerManager().addWalkStep(lastX, lastY, nextX, nextY))
				return false;
		}
		walkSteps.add(new Object[]
				{ dir, nextX, nextY, check });
		return true;
	}

	public ConcurrentLinkedQueue<Object[]> getWalkSteps() {
		return walkSteps;
	}

	public void resetWalkSteps() {
		walkSteps.clear();
	}

	private Object[] getNextWalkStep() {
		Object[] step = walkSteps.poll();
		if (step == null)
			return null;
		return step;
	}

	public boolean restoreHitPoints() {
		int maxHp = getMaxHitpoints();
		if (hitpoints > maxHp) {
			if (this instanceof Player) {
				Player player = (Player) this;
				if (player.getPrayer().usingPrayer(1, 5) && Utils.random(100) <= 15)
					return false;
			}
			setHitpoints(hitpoints - 1);
			return true;
		} else if (hitpoints < maxHp) {
			setHitpoints(hitpoints + 1);
			//done differently now
			/*	if (this instanceof Player) {
				Player player = (Player) this;
				if (player.getPrayer().usingPrayer(0, 10) && hitpoints < maxHp)
					setHitpoints(hitpoints + 1);
				else if (player.getPrayer().usingPrayer(0, 26) && hitpoints < maxHp)
					setHitpoints(hitpoints + (hitpoints + 4 > maxHp ? maxHp - hitpoints : 4));

			}*/
			return true;
		}
		return false;
	}


	public void setHealRestoreRate(int healRestoreRate) {
		this.healRestoreRate = healRestoreRate;
	}

	public int healRestoreRate = 10;

	public int getHealRestoreRate() {
		return healRestoreRate;
	}

	public boolean needMasksUpdate() {
		return nextFaceEntity != -2 || nextAnimation != null || nextGraphics1 != null || nextGraphics2 != null || nextGraphics3 != null || nextGraphics4 != null || (nextWalkDirection == -1 && nextRunDirection == -1 && nextFaceWorldTile != null) || !nextHits.isEmpty() || nextForceMovement != null || nextForceTalk != null || refreshClanIcon || refreshHitbars;
	}

	public boolean isDead() {
		return hitpoints == 0;
	}

	public void resetMasks() {
		nextAnimation = null;
		nextGraphics1 = null;
		nextGraphics2 = null;
		nextGraphics3 = null;
		nextGraphics4 = null;
		if (nextWalkDirection == -1)
			nextFaceWorldTile = null;
		nextForceMovement = null;
		nextForceTalk = null;
		nextFaceEntity = -2;
		nextHits.clear();
		refreshClanIcon = false;
		refreshHitbars = false;
	}

	public abstract void finish();

	public abstract int getMaxHitpoints();

	public void processEntityUpdate() {
		processMovement();
		processMapLoad();
		processReceivedHits();
		processReceivedDamage();
	}

	public void processEntity() {
		poison.processPoison();
	}

	public void loadMapRegions() {
		mapRegionsIds.clear();
		isAtDynamicRegion = false;
		int chunkX = getChunkX();
		int chunkY = getChunkY();
		int mapHash = Settings.MAP_SIZES[mapSize] >> 4;
		int minRegionX = (chunkX - mapHash) / 8;
		int minRegionY = (chunkY - mapHash) / 8;
		for (int xCalc = minRegionX < 0 ? 0 : minRegionX; xCalc <= ((chunkX + mapHash) / 8); xCalc++)
			for (int yCalc = minRegionY < 0 ? 0 : minRegionY; yCalc <= ((chunkY + mapHash) / 8); yCalc++) {
				int regionId = yCalc + (xCalc << 8);
				if (World.getRegion(regionId, this instanceof Player) instanceof DynamicRegion)
					isAtDynamicRegion = true;
				mapRegionsIds.add(regionId);
			}
		lastLoadedMapRegionTile = new WorldTile(this); // creates a immutable
		// copy of this
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public int getHitpoints() {
		return hitpoints;
	}

	public void setHitpoints(int hitpoints) {
		this.hitpoints = hitpoints;
	}

	public void setLastRegionId(int lastRegionId) {
		this.lastRegionId = lastRegionId;
	}

	public int getLastRegionId() {
		return lastRegionId;
	}

	public int getMapSize() {
		return mapSize;
	}

	public void setMapSize(int size) {
		this.mapSize = size;
		loadMapRegions();
	}

	public CopyOnWriteArrayList<Integer> getMapRegionsIds() {
		return mapRegionsIds;
	}

	public List<NPC> getLocalNPCs() {
		List<NPC> l = new ArrayList<>();

		for(int regionId : getMapRegionsIds()) {
			List<Integer> npcIndexes = World.getRegion(regionId).getNPCsIndexes();
			if (npcIndexes != null) {
				for (int npcIndex : npcIndexes) {
					NPC target = World.getNPCs().get(npcIndex);
					if(target != null)
						l.add(target);
				}
			}
		}

		return l;
	}
	public List<Player> getLocalPlayers() {
		List<Player> l = new ArrayList<>();

		for(int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes != null) {
				for (int playerIndex : playerIndexes) {
					Player target = World.getPlayers().get(playerIndex);
					if(target != null)
						l.add(target);
				}
			}
		}

		return l;
	}

	public void anim(int id) { //simplier api to save time.
		setNextAnimation(new Animation(id));
	}

	public void resetAnim() {
		this.nextAnimation = new Animation(-1);
	}
	
	public void setNextAnimation(Animation nextAnimation) {
		if (nextAnimation != null && nextAnimation.getIds()[0] >= 0)
			lastAnimationEnd = Utils.currentTimeMillis() + AnimationDefinitions.getAnimationDefinitions(nextAnimation.getIds()[0]).getEmoteTime();
		this.nextAnimation = nextAnimation;
	}

	public void setNextAnimationNoPriority(Animation nextAnimation) {
		if (lastAnimationEnd > Utils.currentTimeMillis())
			return;
		setNextAnimation(nextAnimation);
	}

	public Animation getNextAnimation() {
		return nextAnimation;
	}

	public void gfx(int id) { //simplier api to save time.
		setNextGraphics(new Graphics(id));
	}
	
	public void setNextGraphics(Graphics nextGraphics) {
		if (nextGraphics == null) {
			if (nextGraphics4 != null)
				nextGraphics4 = null;
			else if (nextGraphics3 != null)
				nextGraphics3 = null;
			else if (nextGraphics2 != null)
				nextGraphics2 = null;
			else
				nextGraphics1 = null;
		} else {
			if (nextGraphics.equals(nextGraphics1) || nextGraphics.equals(nextGraphics2) || nextGraphics.equals(nextGraphics3) || nextGraphics.equals(nextGraphics4))
				return;
			if (nextGraphics1 == null)
				nextGraphics1 = nextGraphics;
			else if (nextGraphics2 == null)
				nextGraphics2 = nextGraphics;
			else if (nextGraphics3 == null)
				nextGraphics3 = nextGraphics;
			else
				nextGraphics4 = nextGraphics;
		}
	}

	public Graphics getNextGraphics1() {
		return nextGraphics1;
	}

	public Graphics getNextGraphics2() {
		return nextGraphics2;
	}

	public Graphics getNextGraphics3() {
		return nextGraphics3;
	}

	public Graphics getNextGraphics4() {
		return nextGraphics4;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public void setDirection(Direction direction, boolean turn) {
		setDirection(Utils.getAngle(direction.deltaX, direction.deltaY));
		if(turn) {
			setNextFaceRectanglePrecise(clone().transform(direction.deltaX * getSize(), direction.deltaY * getSize(), 0), getSize(), getSize());
		}
	}

	public int getDirection() {
		return direction;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean hasFinished() {
		return finished;
	}

	public void setNextWorldTile(WorldTile nextWorldTile) {
		this.nextWorldTile = nextWorldTile;
	}

	public WorldTile getNextWorldTile() {
		return nextWorldTile;
	}

	public boolean hasTeleported() {
		return teleported;
	}

	public WorldTile getLastLoadedMapRegionTile() {
		return lastLoadedMapRegionTile;
	}

	public int getNextWalkDirection() {
		return nextWalkDirection;
	}

	public int getNextRunDirection() {
		return nextRunDirection;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

	public boolean getRun() {
		return run;
	}

	public Rectangle getNextFaceWorldTile() {
		return nextFaceWorldTile;
	}

	//@Deprecated used to face simply a tiel
	//"use setNextFaceRectanglePrecise(tile, 1, 1)
	public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
		// who the hell made setNextFaceworldTile() ??!!! fucking incorrect calcs.....
		// also the thing you guys call direction is actually angle
		setNextFaceRectanglePrecise(nextFaceWorldTile, 1, 1);
	}

	public void setNextFaceRectanglePrecise(WorldTile base, int sizeX, int sizeY) {
		if (nextFaceWorldTile != null && nextFaceWorldTile.getX() == base.getX() && nextFaceWorldTile.getY() == base.getY() && nextFaceWorldTile.getSizeX() == sizeX && nextFaceWorldTile.getSizeY() == sizeY)
			return;
		nextFaceWorldTile = new Rectangle(base.getX(), base.getY(), sizeX, sizeY);
		updateAngle(base, sizeX, sizeY);
	}

	/*
	 * avoid using it :p
	 */
	@Deprecated
	public void updateAngle(WorldTile base, int sizeX, int sizeY) {
		WorldTile from = nextWorldTile != null ? nextWorldTile : this;
		int srcX = (from.getX() * 512) + (getSize() * 256);
		int srcY = (from.getY() * 512) + (getSize() * 256);
		int dstX = (base.getX() * 512) + (sizeX * 256);
		int dstY = (base.getY() * 512) + (sizeY * 256);
		int deltaX = srcX - dstX;
		int deltaY = srcY - dstY;
		direction = deltaX != 0 || deltaY != 0 ? (int) (Math.atan2((double) deltaX, (double) deltaY) * 2607.5945876176133) & 0x3FFF : 0;
	}

	public abstract int getSize();

	public void cancelFaceEntityNoCheck() {
		nextFaceEntity = -2;
		lastFaceEntity = -1;
	}

	public void setNextFaceEntity(Entity entity) {
		if (entity == null) {
			nextFaceEntity = -1;
			lastFaceEntity = -1;
		} else {
			nextFaceEntity = entity.getClientIndex();
			lastFaceEntity = nextFaceEntity;
		}
	}

	public int getNextFaceEntity() {
		return nextFaceEntity;
	}

	public long getFreezeDelay() {
		return freezeDelay; // 2500 delay
	}

	public boolean isFrozen() {
		return freezeDelay >= Utils.currentTimeMillis();
	}

	public int getLastFaceEntity() {
		return lastFaceEntity;
	}

	public long getFrozenBlockedDelay() {
		return frozenBlocked;
	}

	public void setFrozeBlocked(int time) {
		this.frozenBlocked = time;
	}

	public void setFreezeDelay(int time) {
		this.freezeDelay = time;
	}

	public void freeze(long time) {
		this.freezeDelay = time;
	}

	public void addFrozenBlockedDelay(int time) {
		frozenBlocked = time + Utils.currentTimeMillis();
	}

	public void addFreezeDelay(long time) {
		addFreezeDelay(time, false);
	}

	public void addFreezeDelay(long time, boolean entangleMessage) {
		long currentTime = Utils.currentTimeMillis();
		if (currentTime > freezeDelay) {
			if (this instanceof Player) {
				Player p = (Player) this;
				if (!entangleMessage)
					p.getPackets().sendGameMessage("You have been frozen.");
				if (p.getControlerManager().getControler() != null && p.getControlerManager().getControler() instanceof StealingCreationController)
					time /= 2;
			}
		}
		resetWalkSteps();
		freezeDelay = time + currentTime;
	}

	public abstract double getMagePrayerMultiplier();

	public abstract double getRangePrayerMultiplier();

	public abstract double getMeleePrayerMultiplier();

	public Entity getAttackedBy() {
		return attackedBy;
	}

	public void setAttackedBy(Entity attackedBy) {
		this.attackedBy = attackedBy;
	}

	public long getAttackedByDelay() {
		return attackedByDelay;
	}

	public void setAttackedByDelay(long attackedByDelay) {
		this.attackedByDelay = attackedByDelay;
	}

	public void checkMultiArea() {
		multiArea = forceMultiArea ? true : World.isMultiArea(this);
	}

	public boolean isAtMultiArea() {
		return multiArea;
	}

	public void setAtMultiArea(boolean multiArea) {
		this.multiArea = multiArea;
	}

	public boolean isAtDynamicRegion() {
		return isAtDynamicRegion;
	}

	public ForceMovement getNextForceMovement() {
		return nextForceMovement;
	}

	public void setNextForceMovement(ForceMovement nextForceMovement) {
		if (nextForceMovement != null) {
			if (needMapUpdate()) 
				loadMapRegions();
			forceMovementCycle = WorldThread.WORLD_CYCLE + (nextForceMovement.getFirstTileTicketDelay() + nextForceMovement.getSecondTileTicketDelay());
		} else
			forceMovementCycle = 0;
		this.nextForceMovement = nextForceMovement;
	}

	public Poison getPoison() {
		return poison;
	}

	public ForceTalk getNextForceTalk() {
		return nextForceTalk;
	}

	public void setNextForceTalk(ForceTalk nextForceTalk) {
		this.nextForceTalk = nextForceTalk;
	}

	public void forceTalk(String s) {
		setNextForceTalk(new ForceTalk(s));
	}

	public void faceEntity(Entity target) {
		//setNextFaceWorldTile(new WorldTile(target.getCoordFaceX(target.getSize()), target.getCoordFaceY(target.getSize()), target.getPlane()));
		setNextFaceRectanglePrecise(new WorldTile(target.getX(), target.getY(), target.getPlane()), target.getSize(), target.getSize());
	}
	
	public void faceEntity2(Entity target) {
		//setNextFaceWorldTile(new WorldTile(target.getCoordFaceX(target.getSize()), target.getCoordFaceY(target.getSize()), target.getPlane()));
		setNextFaceRectanglePrecise(new WorldTile(target.getLastWorldTile().getX(), target.getLastWorldTile().getY(), target.getPlane()), target.getSize(), target.getSize());
	}

	public void faceObject(WorldObject object) {
		ObjectConfig def = object.getDefinitions();
		int x = -1, y = -1;
		int sizeX = 1, sizeY = 1;
		if (object.getType() == 0) { // wall
			if (object.getRotation() == 0) { // west
				x = object.getX() - 1;
				y = object.getY();
			} else if (object.getRotation() == 1) { // north
				x = object.getX();
				y = object.getY() + 1;
			} else if (object.getRotation() == 2) { // east
				x = object.getX() + 1;
				y = object.getY();
			} else if (object.getRotation() == 3) { // south
				x = object.getX();
				y = object.getY() - 1;
			}
		} else if (object.getType() == 1 || object.getType() == 2) { // corner and cornerwall
			if (object.getRotation() == 0) { // nw
				x = object.getX() - 1;
				y = object.getY() + 1;
			} else if (object.getRotation() == 1) { // ne
				x = object.getX() + 1;
				y = object.getY() + 1;
			} else if (object.getRotation() == 2) { // se
				x = object.getX() + 1;
				y = object.getY() - 1;
			} else if (object.getRotation() == 3) { // sw
				x = object.getX() - 1;
				y = object.getY() - 1;
			}
		} else if (object.getType() == 3) { // inverted corner
			if (object.getRotation() == 0) { // se
				x = object.getX() + 1;
				y = object.getY() - 1;
			} else if (object.getRotation() == 1) { // sw
				x = object.getX() - 1;
				y = object.getY() - 1;
			} else if (object.getRotation() == 2) { // nw
				x = object.getX() - 1;
				y = object.getY() + 1;
			} else if (object.getRotation() == 3) { // ne
				x = object.getX() + 1;
				y = object.getY() + 1;
			}
		} else if (object.getType() < 10) { // walldeco's
			if (object.getRotation() == 0) { // west
				x = object.getX() - 1;
				y = object.getY();
			} else if (object.getRotation() == 1) { // north
				x = object.getX();
				y = object.getY() + 1;
			} else if (object.getRotation() == 2) { // east
				x = object.getX() + 1;
				y = object.getY();
			} else if (object.getRotation() == 3) { // south
				x = object.getX();
				y = object.getY() - 1;
			}
		} else if (object.getType() == 10 || object.getType() == 11 || object.getType() == 22) { // multisized rect objs
			if (object.getRotation() == 0 || object.getRotation() == 2) {
				x = object.getX();
				y = object.getY();
				sizeX = def.getSizeX();
				sizeY = def.getSizeY();
			} else {
				x = object.getX();
				y = object.getY();
				sizeX = def.getSizeY();
				sizeY = def.getSizeX();
			}
		} else {
			// rest
			x = object.getX();
			y = object.getY();
		}

		setNextFaceRectanglePrecise(new WorldTile(x, y, getPlane()), sizeX, sizeY);
	}

	public long getLastAnimationEnd() {
		return lastAnimationEnd;
	}

	public ConcurrentHashMap<Object, Object> getTemporaryAttributtes() {
		return temporaryAttributes;
	}

	public boolean isForceMultiArea() {
		return forceMultiArea;
	}

	public void setForceMultiArea(boolean forceMultiArea) {
		this.forceMultiArea = forceMultiArea;
		checkMultiArea();
	}

	public WorldTile getLastWorldTile() {
		return lastWorldTile;
	}

	public ArrayList<Hit> getNextHits() {
		return nextHits;
	}

	public void playSound(int soundId, int type) {
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes != null) {
				for (int playerIndex : playerIndexes) {
					Player player = World.getPlayers().get(playerIndex);
					if (player == null || !player.isRunning() || !withinDistance(player))
						continue;
					player.getPackets().sendSound(soundId, 0, type);
				}
			}
		}
	}

	public long getFindTargetDelay() {
		return findTargetDelay;
	}

	public void setFindTargetDelay(long findTargetDelay) {
		this.findTargetDelay = findTargetDelay;
	}

	public void sendSoulSplit(final Hit hit, final Entity user) {
		final Entity target = this;
		if (hit.getDamage() > 0)
			World.sendProjectile(user, this, 2263, 11, 11, 20, 5, 0, 0);
		
		
		user.heal(hit.getDamage() /
				(user instanceof Player && Combat.hasCustomWeapon((Player) user) ? 10 : 5));
		if (target instanceof Player)
			((Player) target).getPrayer().drainPrayer(hit.getDamage() / 5);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				setNextGraphics(new Graphics(2264));
				if (hit.getDamage() > 0)
					World.sendProjectile(target, user, 2263, 11, 11, 20, 5, 0, 0);
			}
		}, 0);
	}

	public boolean isRefreshClanIcon() {
		return refreshClanIcon;
	}

	public void setRefreshClanIcon(boolean refreshClanIcon) {
		this.refreshClanIcon = refreshClanIcon;
	}

	public abstract String getName();
	
	public abstract boolean isUnderCombat();
	
	
	public boolean isXmas() {
		return getRegionId() == 10841;
	}

	public boolean isRefreshHitbars() {
		return refreshHitbars;
	}

	public void setRefreshHitbars(boolean refreshHitbars) {
		this.refreshHitbars = refreshHitbars;
	}
	
	public int getHitbarSprite(Player player) {
		return player.isOsrsHitbars() ? 22177 : -1;
	}

	public void applyHit(Entity entity, int i) {
		applyHit(new Hit(entity, i, HitLook.REGULAR_DAMAGE));
	}
	public void applyHit(Entity entity, int i, HitLook look) {
		applyHit(new Hit(entity, i, look));
	}

    public boolean isPlayer() {
		return this instanceof Player;
    }

    public Optional<Player> toPlayer() {
		return Optional.ofNullable(isPlayer() ? (Player) this : null);
	}

	public Player asPlayer() {
		if(isPlayer())
			return (Player) this;
		else return null;
	}

	public boolean isNPC() {
		return this instanceof NPC;
	}

	public NPC asNPC() {
		if(isNPC())
			return (NPC) this;
		else return null;
	}
	public boolean collides(Entity entity) {
		return Utils.collides(this, entity);
	}

	private boolean teleporting = false;

	public boolean isTeleporting() {
		return teleporting;
	}

	public void setTeleporting(int delay) {
		this.teleporting = true;
		WorldTasksManager.schedule(() -> setTeleporting(false), delay);
	}

	public void setTeleporting(boolean teleporting) {
		this.teleporting = teleporting;
	}

	// when the attack is fired - ex. when arrow is fired/anim starts
	public void onAttack(Player player) {
	}

    public boolean canTakeDamage() {
		return !isDead() && !hasFinished() && !isTeleporting();
    }

	public void forceTeleport(WorldTile checkpointTile) {
		setTeleporting(1);
		if(this instanceof Player) {
			this.asPlayer().lock(1);
		}
		setNextWorldTile(checkpointTile.clone());
	}
}