package com.rs.game.player.controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.others.GraveStone;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.thieving.Thieving;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.controllers.events.DeathEvent;
import com.rs.game.player.dialogues.impl.Mandrith_Nastroth;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.PkRank;
import com.rs.utils.Utils;

public class Wilderness extends Controller {

	private boolean showingSkull;
	//private transient boolean hotspot;
//	private transient List<String> ragList;

	@Override
	public void start() {
		checkBoosts(player);
	/*	ragList = new LinkedList<String>();
		ragList.addAll(0, player.getFriendsIgnores().getIgnores());*/
		player.getPackets().sendGameMessage("<col=ff0000>Warning: items lost on death on wilderness won't be refunded!");
		player.getPackets().sendGameMessage("<col=ff0000>Untradeables above wilderness level 20 will turn into coins upon death.");
		boolean skulled = player.hasSkull();
		Integer[][] slots = GraveStone.getItemSlotsKeptOnDeath(player, true, skulled, player.getPrayer().isProtectingItem());
		Item[][] items = GraveStone.getItemsKeptOnDeath(player, slots);
		long riskedWealth = 0;
		long carriedWealth = 0;
		for (Item item : items[1])
			carriedWealth = riskedWealth += GrandExchange.getPrice(item.getId()) * item.getAmount();
		for (Item item : items[0])
			carriedWealth += GrandExchange.getPrice(item.getId()) * item.getAmount();
		
		player.getPackets().sendGameMessage("Risked wealth: <col=ff0000>"+Utils.getFormattedNumber((int) riskedWealth)+"</col> Carried Wealth: <col=ff0000>"+Utils.getFormattedNumber((int) carriedWealth));
	
		if (Combat.hasCustomWeaponOnWild(player)) {
			player.getPackets().sendGameMessage("<col=ff0000>Warning: You are wearing a custom weapon.");
			player.getPackets().sendGameMessage("<col=ff0000>Warning: Your pvp damage will be reduced by 50% while doing so.");
		}
	}
	
	@Override
	public void processNPCDeath(NPC id) {
		if (player.getRegionId() == 12958 && Utils.random(30) == 0) //key
			player.setLootbeam(World.addGroundItem(new Item(41942), new WorldTile(id.getCoordFaceX(id.getSize()), id.getCoordFaceY(id.getSize()), id.getPlane()), player, true, 60));
		if (Utils.random(30) == 0 && !player.containsItem(41941)) //Looting bag
			World.addGroundItem(new Item(41941), new WorldTile(id.getCoordFaceX(id.getSize()), id.getCoordFaceY(id.getSize()), id.getPlane()), player, true, 60);
		if (Utils.random(200) == 0) //pk teleport
			World.addGroundItem(new Item(25433), new WorldTile(id.getCoordFaceX(id.getSize()), id.getCoordFaceY(id.getSize()), id.getPlane()), player, true, 60);
	}

	public static void checkBoosts(Player player) {
		boolean changed = false;
		int level = player.getSkills().getLevelForXp(Skills.ATTACK);
		int maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(Skills.ATTACK)) {
			player.getSkills().set(Skills.ATTACK, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.STRENGTH);
		maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(Skills.STRENGTH)) {
			player.getSkills().set(Skills.STRENGTH, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.DEFENCE);
		maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(Skills.DEFENCE)) {
			player.getSkills().set(Skills.DEFENCE, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.RANGE);
		maxLevel = (int) (level + 5 + (level * 0.1));
		if (maxLevel < player.getSkills().getLevel(Skills.RANGE)) {
			player.getSkills().set(Skills.RANGE, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.MAGIC);
		maxLevel = level + 5;
		if (maxLevel < player.getSkills().getLevel(Skills.MAGIC)) {
			player.getSkills().set(Skills.MAGIC, maxLevel);
			changed = true;
		}
		if (changed)
			player.getPackets().sendGameMessage("Your extreme potion bonus has been reduced.");
	}

	@Override
	public boolean login() {
		start();
		moved();
		Region map = World.getRegion(player.getRegionId());
		if (map.getLoadMapStage() == 2 && map.isLoadedObjectSpawns() && !World.isFloorFree(player.getPlane(), player.getX(), player.getY())) 
			player.useStairs(-1, new WorldTile(3113, 3523, 0), 0, 1);
		return false;
	}
	
	
	private static final Map<String, Long> skullTimers = new HashMap<String, Long>();
	
	
	public static boolean isSkulled(Player player, Player target) {
		Long time = skullTimers.get(player.getUsername()+"_"+target.getUsername());
		return time != null && time >= Utils.currentTimeMillis();
	}
	
	
	public static void checkSkull(Player player, Player target) {
		if (!isSkulled(target, player)) { //if target not skulled means this is first attacking
			player.setWildernessSkull();
			skullTimers.put(player.getUsername()+"_"+target.getUsername(), Utils.currentTimeMillis() + (3 * 60000));
		} else { //to ensure it doesnt expire while being attacked
			skullTimers.put(target.getUsername()+"_"+player.getUsername(), Utils.currentTimeMillis() + (3 * 60000));
		}
	}
	

	@Override
	public boolean keepCombating(Entity target) {
		if (target instanceof Familiar)
			target = ((Familiar)target).getOwner();
		if (!canAttack(target))
			return false;
		if (player.getCombatDefinitions().getSpellId() <= 0 && Utils.inCircle(new WorldTile(3105, 3933, 0), target, 24)) {
			player.getPackets().sendGameMessage("You can only use magic in the arena.");
			return false;
		}
			
		if (target instanceof NPC)
			return true;
		 checkSkull(player, (Player) target);
		/*if (target.getAttackedBy() != player && player.getAttackedBy() != target
				&& ((Player)target).getLastTarget() != player)
			player.setWildernessSkull();*/
		return true;
	}

	/*@Override
	public boolean canAttack(Entity target) {
		if (target instanceof Familiar)
			target = ((Familiar)target).getOwner();
		if (target instanceof Player) {
			Player p2 = (Player) target;
			if (player.isCanPvp() && !p2.isCanPvp()) {
				player.getPackets().sendGameMessage("That player is not in the wilderness.");
				return false;
			}
			if (canHit(target))
				return true;
			return false;
		}
		return true;
	}*/
	@Override
	public boolean canAttack(Entity target) {
		if (target instanceof Familiar)
			target = ((Familiar)target).getOwner();
		if (target instanceof Player) {
			Player p2 = (Player) target;
			if (player.isCanPvp() && !p2.isCanPvp()) {
				player.getPackets().sendGameMessage("That player is not in the wilderness.");
				return false;
			}
			if (Math.abs(player.getSkills().getCombatLevel() - p2.getSkills().getCombatLevel()) > getWildLevel(player)) {
				player.getPackets().sendGameMessage("The difference between your Combat level and the Combat level of " + p2.getDisplayName() + " is too great.");
				player.getPackets().sendGameMessage("He needs to move deeper into the Wilderness before you can attack him.");
				return false;
			}
			if (Settings.SPAWN_WORLD && (player.getFriendsIgnores().isIgnore(p2.getDisplayName()) || p2.getFriendsIgnores().isIgnore(player.getDisplayName()))) {
				player.getPackets().sendGameMessage("You can't attack this player as you added him, or are part of his ignore list.");
				return false;
			}
			return canHit(target);
		}
		return true;
	}

	@Override
	public boolean canHit(Entity target) {
		if (target instanceof Familiar)
			target = ((Familiar)target).getOwner();
		if (target instanceof NPC)
			return true;
		Player p2 = (Player) target;
		if (Math.abs(player.getSkills().getCombatLevel() - p2.getSkills().getCombatLevel()) > getWildLevel(player))
			return false;
	/*	if (player.isBeginningAccount() || p2.isBeginningAccount()) {
			player.getPackets().sendGameMessage("Starter acccounts cannot attack or be attacked for the first hour of playing time.");
			return false;
		}
		Controller c = p2.getControlerManager().getControler();
		if (c != null) {
			Wilderness w = ((Wilderness) c);
			if (w != null) {
				if(Settings.SPAWN_WORLD && !p2.isAtMultiArea() && (ragList.contains(p2.getDisplayName()) || w.getRagList().contains(player.getDisplayName()))) {
					player.getPackets().sendGameMessage("You can't attack this player as you added him, or are part of his ignore list.");
					return false;
				}
			}
		}*/
		return true;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		if (getWildLevel(player) > 20) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		if (player.getTeleBlockDelay() > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		return true;

	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		if (getWildLevel(player) > 30) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		if (player.getTeleBlockDelay() > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		if (player.getTeleBlockDelay() > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		return true;
	}

	public static final Map<String, Set<Long>> killNames = new ConcurrentHashMap<String, Set<Long>>();
	public static final Map<String, Set<Long>> killIPS = new ConcurrentHashMap<String, Set<Long>>();
	public static final Map<String, Set<Long>> killMACS = new ConcurrentHashMap<String, Set<Long>>();

	public boolean dropArtefact(Player killer) {
	/*	int receivedPKP = isHotSpot() ? 2 : 1;
		killer.setPkPoints(killer.getPkPoints() + receivedPKP);
		killer.getPackets().sendGameMessage("You have killed " + player.getDisplayName() + " and received " + receivedPKP + " PKP.");
		*/
		if (getRiskedWealth(player) < 15000 || getRiskedWealth(killer) < 15000)
			return false;
		String[] keys = new String[]
				{ killer.getUsername() + "@" + player.getUsername(), killer.getSession().getIP() + "@" + player.getSession().getIP(), killer.getLastGameMAC() + "@" + player.getLastGameMAC() };

		@SuppressWarnings("unchecked")
		Set<Long>[] times = new Set[]
				{ killNames.get(keys[0]), killIPS.get(keys[1]), killMACS.get(keys[2]) };

		if (times[0] == null || times[1] == null || times[2] == null) {
			times[0] = new HashSet<Long>();
			times[1] = new HashSet<Long>();
			times[2] = new HashSet<Long>();
			killNames.put(keys[0], times[0]);
			killIPS.put(keys[1], times[1]);
			killMACS.put(keys[2], times[2]);
		}

		long current = Utils.currentTimeMillis();
		for (int i = 0; i < times.length; i++) {
			int count = 0;
			for (Iterator<Long> iterator = times[i].iterator(); iterator.hasNext();) {
				long time = iterator.next();
				if (current < time + 10 * 60 * 1000) { //5min
					//expired
					iterator.remove();
				} else
					count++;
			}
			if (count >= 1 && Settings.HOSTED)
				return false;
		}

		int cb1 = player.getSkills().getCombatLevel();
		if (cb1 < 50)
			return false;
		int cb2 = killer.getSkills().getCombatLevel();
		if (cb2 < 50)
			return false;
		for (Set<Long> set : times) {
			set.add(current);
		}
		if (Utils.currentTimeMillis() < killer.getLastArtefactTime() + 3 * 60 * 1000)
			return true;
		if (Math.abs(cb1 - cb2) > 30)
			return false;
		int rareChance = 10 - player.getDonator();
		if (killer.isAtMultiArea())
			rareChance--;
		if (getWildLevel(killer) >= 30)
			rareChance--;
		int artefact = Mandrith_Nastroth.ARTEFACTS[Utils.random(Mandrith_Nastroth.ARTEFACTS.length - 3) + (Utils.random(rareChance) != 0 ? 3 : 0)];
		killer.setLastArtefactTime(current);
		killer.setLootbeam(World.addGroundItem(new Item(artefact), new WorldTile(player), killer, true, 60));
		
		if (artefact >= 14876 && artefact <= 14881) 
			World.sendNews(killer, killer.getDisplayName() + " has received <col=ffff00>" + ItemConfig.forID(artefact).getName() + "<col=ff8c38> from <col=cc33ff>pvp<col=ff8c38>!", 1);
		return true;
	}

	public static long getRiskedWealth(Player player) {
		Integer[][] slots = GraveStone.getItemSlotsKeptOnDeath(player, true, player.hasSkull(), player.getPrayer().isProtectingItem());
		Item[][] items = GraveStone.getItemsKeptOnDeath(player, slots);
		if (items.length <= 1) //risking just 1 item or 0
			return 0;
		long riskedWealth = 0;
		for (Item item : items[1])
			riskedWealth += GrandExchange.getPrice(item.getId()) * item.getAmount();
		return riskedWealth;
	}

	public void showSkull() {
		player.getInterfaceManager().setOverlay(381, false);
	//	sendUpdatePvpIcons();
	}

	private void sendUpdatePvpIcons() {
		player.getPackets().sendHideIComponent(745, 6, !isHotSpot());
	}

	private boolean isHotSpot() {
		return player.isCanPvp() && (player.getX() >= 3035 && player.getX() <= 3125 && player.getY() >= 3525 && player.getY() <= 3555);
	}

	public void refreshRange() {
		if (player.isCanPvp()) {
			int lvl = player.getSkills().getCombatLevel();
			int range = Wilderness.getWildLevel(player);
			int sub = (lvl - range);
			player.getPackets().sendIComponentText(player.getInterfaceManager().hasRezizableScreen() ? 746 : 548, player.getInterfaceManager().hasRezizableScreen() ? 17 : 12, (sub < 3 ? 3 : sub) + " - " + (lvl + range));
		} else {
			player.getPackets().sendIComponentText(player.getInterfaceManager().hasRezizableScreen() ? 746 : 548, player.getInterfaceManager().hasRezizableScreen() ? 17 : 12, "");
		}
	}

	public static boolean isDitch(int id) {
		return id >= 1440 && id <= 1444 || id >= 65076 && id <= 65087;
	}

	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (isDitch(object.getId())) {
			player.lock();
			player.setNextAnimation(new Animation(6132));
			final WorldTile toTile = new WorldTile(object.getRotation() == 1 || object.getRotation() == 3 ? object.getX() + 2 : player.getX(), object.getRotation() == 0 || object.getRotation() == 2 ? object.getY() - 1 : player.getY(), object.getPlane());

			player.setNextForceMovement(new ForceMovement(new WorldTile(player), 1, toTile, 2, object.getRotation() == 0 || object.getRotation() == 2 ? ForceMovement.SOUTH : ForceMovement.EAST));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setNextWorldTile(toTile);
					player.faceObject(object);
					removeIcon();
					removeControler();
					player.resetReceivedDamage();
					player.unlock();
				}
			}, 2);
			return false;
		} else if (object.getId() == 2557 || object.getId() == 65717) {
			player.getPackets().sendGameMessage("It seems it is locked, maybe you should try something else.");
			return false;
		} else if ((object.getId() == 29319 ||object.getId() == 29320) && object.getY() == 9918) {
			player.lock(2);
			player.addWalkSteps(object.getX(), object.getY()-1, 1, false);
			removeIcon();
			removeControler();
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick2(final WorldObject object) {
		if (object.getId() == 2557 || object.getId() == 65717) {
			Thieving.pickDoor(player, object);
			return false;
		}
		return true;
	}

	@Override
	public void sendInterfaces() {
		if (isAtWild(player))
			showSkull();
	}
	
	public static final String[] KILL_MESSAGES = {"It's all over for @name@.", "@name@ falls before your might."
			, "Can anyone defeat you? Certainly not @name@.", "You were clearly a better fighter than @name@."
			, "@name@ has fallen before your mighty mightiness."
			, "Well done, you've pwned @name@.", "Let all warriors learn from the fate of @name@ and fear you."
			, "It's official: you are far more awesome than @name@ is.", "You have wiped the floor with @name@."
			, "Ooh, @name@ just dropped dead, and it's all thanks to you!", "You have proven your superiority over @name@.",
			"You have killed @name@."};

	@Override
	public boolean sendDeath() {
		player.lock(8);
		player.stopAll();
		if (player.getFamiliar() != null)
			player.getFamiliar().sendDeath(player);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					Player killer = player.getMostDamageReceivedSourcePlayer();
					if (killer != null) {
						killer.reduceDamage(player);
						killer.getPackets().sendGameMessage(KILL_MESSAGES[Utils.random(KILL_MESSAGES.length)].replace("@name@", player.getDisplayName()));
						if (killer.canIncreaseKillCount(player)) {
							killer.increaseKillCount(player);
							boolean drop = dropArtefact(killer);
							World.addGroundItem(new Item(43307, drop ? Utils.random(1000, 6000) : Utils.random(100, 400)), new WorldTile(player), killer, true, 60);
						}
						
						killer.setAttackedByDelay(Utils.currentTimeMillis() + 8000); // imunity
						if (killer.getAttackedBy() == player) 
							killer.setAttackedBy(null);
					}
					player.sendItemsOnDeath(killer);
					player.revokeHC();
					player.reset();
					player.setNextWorldTile(DeathEvent.HUBS[2]); // edgevile
					player.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					removeIcon();
					removeControler();
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public void magicTeleported(int teleType) {
		if (!isAtWild(player.getNextWorldTile())) {
			player.setCanPvp(false);
			removeIcon();
			removeControler();
		}
	}

	@Override
	public void moved() {
	/*	refreshRange();
		if (!hotspot && isHotSpot()) {
			hotspot = true;
			sendUpdatePvpIcons();
		} else if (hotspot && !isHotSpot()) {
			hotspot = false;
			sendUpdatePvpIcons();
		}*/
		boolean isAtWild = isAtWild(player);
		boolean isAtWildSafe = isAtWildSafe(player);
		if (!showingSkull && isAtWild && !isAtWildSafe) {
			showingSkull = true;
			player.setCanPvp(true);
			showSkull();
			player.getAppearence().generateAppearenceData();
		} else if (showingSkull && (isAtWildSafe || !isAtWild)) {
			removeIcon();
		} else if (!isAtWildSafe && !isAtWild) {
			player.setCanPvp(false);
			removeIcon();
			removeControler();
		} else if ((player.getX() == 3386 || player.getX() == 3387) && player.getY() == 3615) {
			removeIcon();
			player.setCanPvp(false);
			removeControler();
			player.getControlerManager().startControler("Kalaboss");
		}
	}

	public void removeIcon() {
		if (showingSkull) {
			showingSkull = false;
			player.setCanPvp(false);
			player.getInterfaceManager().removeOverlay(false);
			player.getAppearence().generateAppearenceData();
			player.getEquipment().refresh(null);
		/*	sendUpdatePvpIcons();
			refreshRange();*/
		}
	}

	@Override
	public boolean logout() {
		return false; // so doesnt remove script
	}

	@Override
	public void forceClose() {
		removeIcon();
	}

	public static final boolean isAtWild(WorldTile tile) {// TODO fix this
		int mapID = tile.getRegionId();
		return (tile.getX() >= 3011 && tile.getX() <= 3132 && tile.getY() >= 10052 && tile.getY() <= 10175) // fortihrny
				// dungeon
				|| (tile.getX() >= 2940 && tile.getX() <= 3395 && tile.getY() >= 3525 && tile.getY() <= 4000) || (tile.getX() >= 3264 && tile.getX() <= 3279 && tile.getY() >= 3279 && tile.getY() <= 3672) || (tile.getX() >= 2756 && tile.getX() <= 2875 && tile.getY() >= 5512 && tile.getY() <= 5627) || (tile.getX() >= 3158 && tile.getX() <= 3181 && tile.getY() >= 3679 && tile.getY() <= 3697) || (tile.getX() >= 3280 && tile.getX() <= 3183 && tile.getY() >= 3885 && tile.getY() <= 3888) || (tile.getX() >= 3012 && tile.getX() <= 3059 && tile.getY() >= 10303 && tile.getY() <= 10351)
				|| mapID == 12961 //scorpia pit;
				|| mapID == 12958 //wild godwars dung
				|| mapID == 12192 //wild lava maze
				|| (mapID >= 12442 && mapID <= 12444); //wild edge
				
	}

	public static boolean isAtWildSafe(WorldTile tile) {
		int mapID = tile.getRegionId();
		return (tile.getX() >= 2940 && tile.getX() <= 3395 && tile.getY() <= 3524 && tile.getY() >= 3523)
				|| (tile.getY() >= 9917 && tile.getX() <= 9922
				&& (mapID >= 12442 && mapID <= 12444 && tile.getY() >= 9917 && tile.getY() <= 9922));
	} //12343 12344

	public static int getWildLevel(WorldTile tile) {
		int wildLevel = tile.getY() > 9900 ? ((tile.getY() - 9912) / 8 + 1) : ((tile.getY() - 3520) / 8 + 1);
		return /*wildLevel < 7 ? 7 :*/ wildLevel;
		/**
		 * if (tile.getY() > 9900) return (tile.getY() - 9912) / 8 + 1; return
		 * (tile.getY() - 3520) / 8 + 1;
		 */
	}

	/*public List<String> getRagList() {
		return ragList;
	}*/

}
