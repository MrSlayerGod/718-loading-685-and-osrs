
package com.rs.game.player.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.abyssalNexus.AbyssalSire;
import com.rs.game.npc.abyssalNexus.AbyssalSpawn;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.cox.impl.*;
import com.rs.game.npc.dungeonnering.DungeonBoss;
import com.rs.game.npc.dungeonnering.DungeonNPC;
import com.rs.game.npc.dungeonnering.ShadowForgerIhlakhizan;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.familiar.Steeltitan;
import com.rs.game.npc.fightkiln.HarAken;
import com.rs.game.npc.fightkiln.HarAkenTentacle;
import com.rs.game.npc.glacior.Glacyte;
import com.rs.game.npc.godwars.armadyl.GodwarsArmadylFaction;
import com.rs.game.npc.godwars.saradomin.CommanderZilyana;
import com.rs.game.npc.godwars.zaros.NexMinion;
import com.rs.game.npc.grotesque.Dawn;
import com.rs.game.npc.inferno.InfernoNPC;
import com.rs.game.npc.others.ClueNPC;
import com.rs.game.npc.others.DagannothKing;
import com.rs.game.npc.others.Mogre;
import com.rs.game.npc.pest.PestPortal;
import com.rs.game.npc.qbd.QueenBlackDragon;
import com.rs.game.npc.skotizo.Skotizo;
import com.rs.game.npc.slayer.CaveKraken;
import com.rs.game.npc.slayer.Cerberus;
import com.rs.game.npc.slayer.Kraken;
import com.rs.game.npc.slayer.KrakenTentacle;
import com.rs.game.npc.slayer.Vorkath;
import com.rs.game.npc.theatreOfBlood.maiden.Maiden;
import com.rs.game.npc.theatreOfBlood.maiden.NylocasMatomenos;
import com.rs.game.npc.theatreOfBlood.nycolas.NycolasSpawn;
import com.rs.game.npc.theatreOfBlood.verzikVitur.NycolasAthanatos;
import com.rs.game.npc.theatreOfBlood.verzikVitur.NycolasMemetos;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikNycolas;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.npc.wild.Scorpia;
import com.rs.game.player.CombatDefinitions;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.Slayer;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.content.raids.cox.chamber.impl.MysticsChamber;
import com.rs.game.player.controllers.TheHorde;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.AmmunitionDefinitionsLoader;
import com.rs.utils.AmmunitionDefinitionsLoader.AmmunitionDefinition;
import com.rs.utils.Logger;
import com.rs.utils.MapAreas;
import com.rs.utils.Utils;
import com.rs.utils.WeaponTypesLoader;
import com.rs.utils.WeaponTypesLoader.WeaponType;

public class PlayerCombat extends Action {

	private Entity target;
	private int max_hit; // temporary constant
	private double base_mage_xp; // temporary constant
	private int mage_hit_gfx; // temporary constant
	private int magic_sound; // temporary constant
	private int magic_voice; // temporary constant
	private int max_poison_hit; // temporary constant
	private int freeze_time; // temporary constant
	private boolean reduceAttack; // temporary constant
	private boolean blood_spell; // temporary constant
	private boolean sang_spell; 
	private boolean block_tele;
	private int spellcasterGloves;
	private int spell_type = -1;

	public static final int AIR_SPELL = 0, WATER_SPELL = 1, EARTH_SPELL = 2, FIRE_SPELL = 3;

	// finish client routefinder

	public PlayerCombat(Entity target) {
		this.target = target;
	}

	@Override
	public boolean start(Player player) {
		player.setNextFaceEntity(target);
		if (checkAll(player)) {
			return true;
		}
		player.setNextFaceEntity(null);
		return false;
	}

	@Override
	public boolean process(Player player) {
		if (checkAll(player)) {
			if (player.getActionManager().getActionDelay() > 0
					&& player.getEquipment().getWeaponId() == 50849 && player.getCombatDefinitions().isUsingSpecialAttack()) {
				processWithDelay(player);
			}
			return true;
		}
		return false;
	}

	private boolean forceCheckClipAsRange(Entity target) {
		return target instanceof Maiden || target instanceof ShadowForgerIhlakhizan || target instanceof PestPortal || target instanceof NexMinion || target instanceof HarAken || target instanceof HarAkenTentacle || target instanceof QueenBlackDragon || target instanceof Vorkath;
	}

	public static boolean isUsingRange(Player player) {
		return getAttackType(player) == Combat.RANGE_TYPE;
	}
	public static boolean isUsingMagic(Player player) {
		return getAttackType(player) == Combat.MAGIC_TYPE;
	}
	public static boolean isUsingMelee(Player player) {
		return getAttackType(player) == Combat.MELEE_TYPE;
	}
	public static int getAttackType(Player player) {
		int spellId = player.getCombatDefinitions().getSpellId();
		if (spellId < 1) {
			if (hasDFSActive(player))
				spellId = 65536;
			else if (hasPolyporeStaff(player))
				spellId = 65535;
			else if (hasTridentOfSeas(player))
				spellId = 65534;
			else if (hasTridentOfSwamp(player))
				spellId = 65533;
			else if (hasDawnbringer(player))
				spellId = 65532;
			else if (hasSangStaff(player))
				spellId = 65531;
			else if (hasCataclysm(player))
				spellId = 65530;
		}
		if (spellId > 0) {
			return Combat.MAGIC_TYPE;
		} else {
			WeaponType type = WeaponTypesLoader.getWeaponDefinition(player.getEquipment().getWeaponId());
			return type.getType();
		}
	}

	@Override
	public int processWithDelay(Player player) {

		int delay = __processWithDelay(player);
		if(delay > 0) {
			target.onAttack(player);
		}

		return delay;
	}

	public int __processWithDelay(Player player) {
		if (player.getEquipment().getWeaponId() == 51015 && player.getCombatDefinitions().getAttackStyle() >= 2) 
			return -1;
		int spellId = player.getCombatDefinitions().getSpellId();
		if (spellId < 1) {
			if (hasDFSActive(player))
				spellId = 65536;
			else if (hasPolyporeStaff(player))
				spellId = 65535;
			else if (hasTridentOfSeas(player))
				spellId = 65534;
			else if (hasTridentOfSwamp(player))
				spellId = 65533;
			else if (hasDawnbringer(player))
				spellId = 65532;
			else if (hasSangStaff(player))
				spellId = 65531;
			else if (hasCataclysm(player))
				spellId = 65530;
		}
		int maxDistance = calculateAttackRange(player);
		
		if (player.getCombatDefinitions().isUsingSpecialAttack() && isSpellSpecial(player.getEquipment().getWeaponId())	&& spellId <= 0) 
			spellId = 65529; //65529, considered autocast this way doesnt reset
		
		double multiplier = 1.0;
		if (player.getTemporaryAttributtes().get("miasmic_effect") == Boolean.TRUE || player.getTemporaryAttributtes().get(Key.SPORE_INFECTED) != null)
			multiplier = 1.5;
		int size = player.getSize();
		if (!player.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target)))
			return 0;
		if (player.hasWalkSteps())
			maxDistance += player.getRun() ? 2 : 1;
		if (!Utils.isOnRange(player.getX(), player.getY(), size, target.getX(), target.getY(), target.getSize(), maxDistance))
			return 0;
		if (!player.getControlerManager().keepCombating(target))
			return -1;
		addAttackedByDelay(player);
		if (spellId > 0) {
			if (player.getEquipment().getWeaponId() == 51015) {
				player.getPackets().sendGameMessage("Your bulwark gets in the way.");
				return -1;
			}
			boolean manualCast = spellId <= 65529 && spellId >= 256;
			Item gloves = player.getEquipment().getItem(Equipment.SLOT_HANDS);
			spellcasterGloves = gloves != null && (gloves.getDefinitions().getName().contains("Spellcaster glove") || gloves.getId() == 25672) && player.getEquipment().getWeaponId() == -1 && new Random().nextInt(30) == 0 ? spellId : -1;
			int delay = mageAttack(player, manualCast ? spellId - 256 : spellId, !manualCast);
			
			if (player.getCombatDefinitions().getSpellBook() == 192 && delay == 5 && spellId <= 65529 && player.getEquipment().getWeaponId() == 54423)
				delay = 4;
			
			if (player.getNextAnimation() != null && spellcasterGloves > 0) {
				player.setNextAnimation(new Animation(14339));
				spellcasterGloves = -1;
			}
			return player.isDoubleCast() ? delay / 2 : delay;
		} else {
			WeaponType type = WeaponTypesLoader.getWeaponDefinition(player.getEquipment().getWeaponId());
			switch(type.getType()) {
			case Combat.MELEE_TYPE:
			case Combat.MAGIC_TYPE:
			case Combat.ALL_TYPE:
				return (int) (meleeAttack(player) * multiplier);
			case Combat.RANGE_TYPE:
				if (!checkAmmo(player))
					return -1;
				return (int) (rangeAttack(player) * multiplier);
			}
			return -1;
		}
	}

	private static int calculateAttackRange(Player player) {
		if (hasDFSActive(player))
			return 1;
		if (player.getCombatDefinitions().getSpellId() > 0 || hasPolyporeStaff(player) || hasTridentOfSeas(player) || hasTridentOfSwamp(player) || hasDawnbringer(player) || hasSangStaff(player) || hasCataclysm(player)
				|| (player.getCombatDefinitions().isUsingSpecialAttack() && isSpellSpecial(player.getEquipment().getWeaponId())))
			return 9;
		
		Item item = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
		if (item == null)
			return 0;
		if(isTBow(item.getId()))
			return 10;

		WeaponType weaponType = WeaponTypesLoader.getWeaponDefinition(item.getId());
		int type = weaponType.getType();
		String name = item.getName().toLowerCase();
		if (type == Combat.MELEE_TYPE) {
			if (item.getId() == 25591 || name.contains("halberd") || name.contains("polearm"))
				return 1;
			return 0;
		} else if (type == Combat.RANGE_TYPE) {
			boolean longRangeStyle = player.getCombatDefinitions().getAttackStyle() == 2;
			if (name.contains("composite"))
				return longRangeStyle ? 9 : 8;
			else if (name.contains("(sighted)"))
				return 9;
			else if (name.contains("longbow"))
				return 9;
			else if (item.getId() == 25037)
				return longRangeStyle ? 9 : 7;
			else if (name.contains("c'bow") || name.contains("crossbow"))
				return longRangeStyle ? 8 : 6;
			else if (item.getId() == 42926 || item.getId() == 25502)
				return longRangeStyle ? 6 : 4;
			else if (name.contains("knife"))
				return longRangeStyle ? 5 : 3;
			else if (name.contains("dart") || name.contains(" stake"))
				return longRangeStyle ? 4 : 2;
			else if (name.contains("crystal"))
				return 9;
			else if (name.contains("dark bow") || item.getId() == 25539 || item.getId() == 25617)
				return longRangeStyle ? 9 : 8;
			else if (name.contains("chinchompa"))
				return longRangeStyle ? 9 : 8;
			else if (name.contains("javelin"))
				return longRangeStyle ? 6 : 4;
			else if (name.contains("thrownaxe"))
				return longRangeStyle ? 5 : 3;
			else if (name.contains("salamander"))
				return 0;
			else if (name.contains("seercull bow"))
				return longRangeStyle ? 9 : 7;
			else if (name.contains("zaryte bow") || name.contains("hexhunter bow"))
				return 9;
			return longRangeStyle ? 8 : 6;
		}
		return 0;
	}

	private static boolean hasDFSActive(Player player) {
		int shieldId = player.getEquipment().getShieldId();
		if (shieldId != -1 && shieldId != 11283 && shieldId != 52002 && shieldId != 51633)
			return false;
		return player.getTemporaryAttributtes().get("dfs_shield_active") != null;
	}

	private void addAttackedByDelay(Entity player) {
		target.setAttackedBy(player);
		target.setAttackedByDelay(Utils.currentTimeMillis() + 6000); // 8seconds
	}

	public static int getMeleeCombatDelay(Player player, int weaponId) {
		return weaponId == -1 ? 3 : (ItemConfig.forID(weaponId).getAttackSpeed() - 1);
	}

	private int getRangeCombatDelay(int weaponId, int attackStyle) {
		int delay = weaponId == -1 ? 3 : (ItemConfig.forID(weaponId).getAttackSpeed() - 1);
		if ((weaponId == 42926 || weaponId == 25502) && target instanceof Player)
			delay++;
		if (attackStyle == 1)
			delay--;
		else if (attackStyle == 2)
			delay++;
		if ((weaponId == 25539 || weaponId == 25617) && target instanceof NPC)
			delay /= 2;
		return delay;
	}

	public Entity[] getMultiAttackTargets(Player player) {
		return getMultiAttackTargets(player, 1, 9);
	}

	public Entity[] getMultiAttackTargets(Player player, int maxDistance, int maxAmtTargets) {
		List<Entity> possibleTargets = new ArrayList<Entity>();
		possibleTargets.add(target);
		if (target.isAtMultiArea()) {
			y: for (int regionId : target.getMapRegionsIds()) {
				Region region = World.getRegion(regionId);
				if (target instanceof Player) {
					List<Integer> playerIndexes = region.getPlayerIndexes();
					if (playerIndexes == null)
						continue;
					for (int playerIndex : playerIndexes) {
						Player p2 = World.getPlayers().get(playerIndex);
						if (p2 == null || p2 == player || p2 == target || p2.isDead() || !p2.hasStarted() || p2.hasFinished() || !p2.isCanPvp() || !p2.isAtMultiArea() || !Utils.isOnRange(target, p2, maxDistance - 1)/*|| !p2.withinDistance(target, maxDistance)*/ || !player.getControlerManager().canHit(p2) || !player.clipedProjectile(p2, false))
							continue;
						possibleTargets.add(p2);
						if (possibleTargets.size() == maxAmtTargets)
							break y;
					}
				} else {
					List<Integer> npcIndexes = region.getNPCsIndexes();
					if (npcIndexes == null)
						continue;
					for (int npcIndex : npcIndexes) {
						NPC n = World.getNPCs().get(npcIndex);
						if (n == null || n == target || n == player.getFamiliar() || n.isDead() || n.hasFinished() || !n.isAtMultiArea() || !Utils.isOnRange(target, n, maxDistance - 1)/*!n.withinDistance(target, maxDistance)*/ || !n.getDefinitions().hasAttackOption() || !player.getControlerManager().canHit(n) || !player.clipedProjectile(n, false))
							continue;
						possibleTargets.add(n);
						if (possibleTargets.size() == maxAmtTargets)
							break y;
					}
				}
			}
		}
		return possibleTargets.toArray(new Entity[possibleTargets.size()]);
	}
	
	public static boolean isSpellSpecial(int weaponId) {
		return weaponId == 54425 || weaponId == 54424;
	}
	
	public int mageAttack(final Player player, int spellId, boolean autocast) {
		if (!autocast) {
			player.getCombatDefinitions().resetSpells(false);
			player.getActionManager().forceStop();
		}
		if (player.getCombatDefinitions().isUsingSpecialAttack() && spellId != 65532
				&& isSpellSpecial(player.getEquipment().getWeaponId())) {	
			if (!specialExecute(player))
				return 5;
			switch(player.getEquipment().getWeaponId()) {
			case 54425:
				player.setNextAnimation(new Animation(810));
				mage_hit_gfx = 77;
				int damage = getDamage(
						player, 0, 0, 2, (player.getSkills().getLevelForXp(Skills.MAGIC)-75) * 110 / 24 + 390, true, 1D, true
						
						);
				delayMagicHit(1, getMagicHit(player,damage));
				player.getPrayer().restorePrayer(damage/2);
				return 5;
			case 54424:
				player.setNextAnimation(new Animation(810));
				mage_hit_gfx = 78;
				delayMagicHit(1, getMagicHit(player, 
						getDamage(
								player, 0, 0, 2, (player.getSkills().getLevelForXp(Skills.MAGIC)-75) * 160 / 24 + 500, true, 1D, true)));
				return 5;
			default:
				player.getPackets().sendGameMessage("This weapon has no special attack, if you see an attack bar please submit a bug report.");
				return 5;
			}
		}
		
		if (!Magic.checkCombatSpell(player, spellId, -1, true)) {
			if (autocast)
				player.getCombatDefinitions().resetSpells(true);
			if(Settings.DEBUG && player.isAdmin())
				player.sendMessage("Debug: checkCombatSpell fail");
			return -1; // stops
		}
		if (spellId == 65536) {
			int shieldId = player.getEquipment().getShieldId();
			if (shieldId != 11283 && shieldId != 52002 && shieldId != 51633)
				return 0;
			player.setNextFaceEntity(target);
			player.setNextGraphics(new Graphics(1165));
			player.setNextAnimation(new Animation(6696));
			mage_hit_gfx = 1167;
			max_hit = 290;
			delayMagicHit(3, getMagicHit(player, Utils.random(290)));
			player.getCharges().addCharges(shieldId, -1, Equipment.SLOT_SHIELD);
			player.getCombatDefinitions().refreshBonuses();
			player.setDFSDelay(120000); // two minutes
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					World.sendProjectile(player, target, 1166, 60, 32, 50, 50, 0, 0);
				}
			});
			return 4;
		} else if (spellId == 65535) {
			player.setNextFaceEntity(target);
			player.setNextGraphics(new Graphics(2034));
			player.setNextAnimation(new Animation(15448));
			mage_hit_gfx = 2036;
			int maxHit = (5 * player.getSkills().getLevel(Skills.MAGIC)) - 180;
			max_hit = maxHit;
			delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, maxHit)));
			if (player.getEquipment().getWeaponId() == 22494) {
				player.getCharges().addCharges(22496, ItemConstants.getItemDefaultCharges(22496), Equipment.SLOT_WEAPON);
				player.getEquipment().getItem(Equipment.SLOT_WEAPON).setId(22496);
				player.getEquipment().refresh(Equipment.SLOT_WEAPON);
			}
			player.getCharges().addCharges(22496, -1, Equipment.SLOT_WEAPON);
			World.sendProjectile(player, target, 2035, 60, 32, 50, 50, 0, 0);
			return 4;
		} else if (spellId == 65534) {
			player.setNextFaceEntity(target);
			player.setNextGraphics(new Graphics(6251, 0, 100));
			player.setNextAnimation(new Animation(811));
			mage_hit_gfx = 6253;
			int maxHit = (((player.getSkills().getLevel(Skills.MAGIC) - 75) / 3) + 20)*10;
			max_hit = maxHit;
			delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, maxHit)));
			if (player.getEquipment().getWeaponId() == 41905) {
				player.getCharges().addCharges(41907, ItemConstants.getItemDefaultCharges(22496), Equipment.SLOT_WEAPON);
				player.getEquipment().getItem(Equipment.SLOT_WEAPON).setId(41907);
				player.getEquipment().refresh(Equipment.SLOT_WEAPON);
			}
			player.getCharges().addCharges(41907, -1, Equipment.SLOT_WEAPON);
			World.sendProjectile(player, target, 6252, 32, 20, 40, 50, 0, 0);
			return 4;
		} else if (spellId == 65533) {
			player.setNextFaceEntity(target);
			player.setNextGraphics(new Graphics(6251, 0, 100));
			player.setNextAnimation(new Animation(811));
			mage_hit_gfx = 6253;
			int maxHit = (((player.getSkills().getLevel(Skills.MAGIC) - 75) / 3) + 23)*10;
			max_hit = maxHit;
			delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, maxHit)));
			if (Utils.random(4) == 0 && (target instanceof Player || !((NPC)target).isIntelligentRouteFinder()))
				target.getPoison().makeEnvenomed(60);
			player.getCharges().addCharges(42899, -1, Equipment.SLOT_WEAPON);
			World.sendProjectile(player, target, 6252, 32, 20, 40, 50, 0, 0);
			return 4;
		} else if (spellId == 65532) { //dawnbright
			boolean spec = false;
			if (player.getCombatDefinitions().isUsingSpecialAttack()) {
				if (!specialExecute(player))
					return 4;
				spec = true;
			}
			player.setNextFaceEntity(target);
			player.setNextGraphics(new Graphics(6546, 0, 100));
			player.setNextAnimation(new Animation(811));
			mage_hit_gfx = 6548;
			int maxHit = (((player.getSkills().getLevel(Skills.MAGIC) - 75) / 3) + 24)*10;
			if (player.getEquipment().getWeaponId() == 25583)
				maxHit *= 1.1;
			max_hit = maxHit;
			boolean verzik = target instanceof VerzikVitur && ((VerzikVitur)target).getId() == VerzikVitur.PHASE_1;
			Hit hit = getMagicHit(player, spec && verzik ? Utils.random(800, 1501) : getRandomMagicMaxHit(player, maxHit));
			if (verzik)
				hit.setLook(HitLook.BLUE_DAMAGE);
			delayMagicHit(2, hit);
			World.sendProjectile(player, target, 6547, 32, 20, 40, 50, 16, 0);
			return 4;
		} else if (spellId == 65531) { //Sanguinesti staff
			player.setNextFaceEntity(target);
			player.setNextGraphics(new Graphics(6540, 0, 100));
			player.setNextAnimation(new Animation(811));
			mage_hit_gfx = 6541;
			int maxHit = (((player.getSkills().getLevel(Skills.MAGIC) - 75) / 3) + 24)*10;
			max_hit = maxHit;
			if (Utils.random(6) == 0)
				sang_spell = true;
			delayMagicHit(2, getMagicHit(player,  getRandomMagicMaxHit(player, maxHit)));
			if (player.getEquipment().getWeaponId() == 25764)
				delayMagicHit(2, getMagicHit(player,  getRandomMagicMaxHit(player, maxHit)));
			World.sendProjectile(player, target, 6539, 32, 20, 40, 50, 16, 0);
			return 4;
		} else if (spellId == 65530) { //Cataclysm staff
			player.setNextFaceEntity(target);
			player.setNextAnimation(new Animation(14221));

			playVoice(5540, player, target);
			attackTarget(getMultiAttackTargets(player, player.getRights() == 2 ? 16 : 1, 9), new MultiAttack() {

				private boolean nextTarget; //real target is first player on array

				@Override
				public boolean attack() {
					magic_voice = 5531;
					if (Utils.rollDie(20)) {
						freeze_time = 20000;
						mage_hit_gfx = 3388;
					} else {
						mage_hit_gfx = 3202;
					}
					base_mage_xp = 52;
					int maxHit = (((player.getSkills().getLevel(Skills.MAGIC) - 75) / 3) + 24)*10;
					int damage = getRandomMagicMaxHit(player, maxHit);
					Hit hit = player.getRights() == 2 ? getRegularHit(player, damage) : getMagicHit(player, damage);
					delayMagicHit(Utils.getDistance(player, target) > 5 ? 4 : Utils.getDistance(player, target) > 3 ? 3 : 2, hit);
					if(player.distance(target) < 3) {
						World.sendProjectile(player, target, 3388, 0, 0, 10, 40, 0, 5);
					} else
						World.sendProjectile(player, target, 3388, 0, 0, 40, 40, 0, 5);
					if (!nextTarget) {
						if (damage == -1)
							return false;
						nextTarget = true;
					}
					return nextTarget;

				}
			});

			return 4;
		}
		if (player.getCombatDefinitions().getSpellBook() == 192) {
			switch (spellId) {
			case 98: // air rush
				player.setNextAnimation(new Animation(14221));
				mage_hit_gfx = 2700;
				base_mage_xp = 2.7;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 10)));
				World.sendProjectile(player, target, 2699, 18, 18, 50, 50, 0, 0);
				return 5;
			case 25: // air strike
				if(player.isOsrsMagicToggle())
					player.setNextGraphics(new Graphics(Settings.OSRS_GFX_OFFSET + 90, 0, 92));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1163 : 14221));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 92:2708;
				base_mage_xp = 5.5;
				int baseDamage = 20;
				if (player.getEquipment().getGlovesId() == 205) {
					baseDamage = 30;
				}

				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, baseDamage)));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2699, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 91, 43, 31, 50, 50, 16, 64);
				return 5;
			case 28: // water strike
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 93 : 2701, 0, player.isOsrsMagicToggle() ? 92 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1163 : 14221));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 95:2708;
				base_mage_xp = 7.5;
				baseDamage = 40;
				if (player.getEquipment().getGlovesId() == 205) {
					baseDamage = 100;
				}
				int damage = getRandomMagicMaxHit(player, baseDamage);
				delayMagicHit(2, getMagicHit(player, damage));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2703, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 94, 43, 31, 50, 50, 16, 64);
				return 5;
			case 36:// bind
				player.setNextGraphics(new Graphics(177));
				player.setNextAnimation(new Animation(710));
				mage_hit_gfx = 181;
				base_mage_xp = 60.5;
				freeze_time = 5000;
				Hit magicHit = getMagicHit(player, getRandomMagicMaxHit(player, 20));
				delayMagicHit(2, magicHit);
				World.sendProjectile(player, target, 178, 18, 18, 50, 50, 0, 0);
				return 5;
			case 55:// snare
				player.setNextGraphics(new Graphics(177));
				player.setNextAnimation(new Animation(710));
				mage_hit_gfx = 180;
				base_mage_xp = 91.1;
				freeze_time = 10000;
				Hit snareHit = getMagicHit(player, getRandomMagicMaxHit(player, 30));
				delayMagicHit(2, snareHit);
				World.sendProjectile(player, target, 178, 18, 18, 50, 50, 0, 0);
				return 5;
			case 81:// entangle
				player.setNextGraphics(new Graphics(177));
				player.setNextAnimation(new Animation(710));
				mage_hit_gfx = 179;
				base_mage_xp = 91.1;
				freeze_time = 15000;
				Hit entangleHit = getMagicHit(player, getRandomMagicMaxHit(player, 50));
				delayMagicHit(2, entangleHit);
				World.sendProjectile(player, target, 178, 18, 18, 50, 50, 0, 0);
				return 5;
			case 30: // earth strike
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 96 : 2713, 0, player.isOsrsMagicToggle() ? 92 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1163 : 14221));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 98:2723;
				base_mage_xp = 9.5;
				baseDamage = 60;
				if (player.getEquipment().getGlovesId() == 205) {
					baseDamage = 110;
				}
				damage = getRandomMagicMaxHit(player, baseDamage);
				delayMagicHit(2, getMagicHit(player, damage));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2718, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 97, 43, 31, 50, 50, 16, 64);
				return 5;
			case 32: // fire strike
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 99 : 2728, 0, player.isOsrsMagicToggle() ? 92 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1163 : 14221));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 101:2737;
				base_mage_xp = 11.5;
				baseDamage = 80;
				if (player.getEquipment().getGlovesId() == 205) {
					baseDamage = 120;
				}
				if (player.getEquipment().getShieldId() == 50714)
					baseDamage *= 1.5;
				damage = getRandomMagicMaxHit(player, baseDamage);
				delayMagicHit(2, getMagicHit(player, damage));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2729, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 100, 43, 31, 50, 50, 16, 64);
				return 5;
			case 34: // air bolt
				if(player.isOsrsMagicToggle())
					player.setNextGraphics(new Graphics(Settings.OSRS_GFX_OFFSET + 117, 0, 92));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1163 : 14220));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 119:2700;
				base_mage_xp = 13.5;
				baseDamage = 90;
				if (player.getEquipment().getGlovesId() == 777) {
					baseDamage = 120;
				}
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, baseDamage)));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2699, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 118, 43, 31, 50, 50, 16, 64);
				return 5;
			case 39: // water bolt
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 120 : 2707, 0, player.isOsrsMagicToggle() ? 92 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1163 : 14220));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 122:2709;
				base_mage_xp = 16.5;
				baseDamage = 100;
				if (player.getEquipment().getGlovesId() == 777) {
					baseDamage = 130;
				}
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, baseDamage)));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2704, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 121, 43, 31, 50, 50, 16, 64);
				return 5;
			case 42: // earth bolt
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 123 : 2714, 0, player.isOsrsMagicToggle() ? 92 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1163 : 14222));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 125:2724;
				base_mage_xp = 19.5;
				baseDamage = 110;
				if (player.getEquipment().getGlovesId() == 777) {
					baseDamage = 140;
				}
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, baseDamage)));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2719, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 124, 43, 31, 50, 50, 16, 64);
				return 5;
			case 45: // fire bolt
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 126 : 2728, 0, player.isOsrsMagicToggle() ? 92 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1163 : 14223));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 128:2738;
				base_mage_xp = 22.5;
				spell_type = FIRE_SPELL;
				baseDamage = 120;
				if (player.getEquipment().getGlovesId() == 777) {
					baseDamage = 150;
				}
				if (player.getEquipment().getShieldId() == 50714)
					baseDamage *= 1.5;
				damage = getRandomMagicMaxHit(player, baseDamage);
				delayMagicHit(2, getMagicHit(player, damage));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2731, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 127, 43, 31, 50, 50, 16, 64);
				return 5;
			case 49: // air blast
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 132 : 2701, 0, player.isOsrsMagicToggle() ? 92 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1163 : 14221));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 134:2700;
				base_mage_xp = 25.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 130)));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2699, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 133, 43, 31, 50, 50, 16, 64);
				return 5;
			case 47: //crumble undead
				if (!Combat.isUndead(target)) {
					player.getPackets().sendGameMessage("This spell only affects skeletons, zombies, ghosts and shades.");
					return -1;
				}
				player.setNextGraphics(new Graphics(145));
				player.setNextAnimation(new Animation(724));
				mage_hit_gfx = 147;
				base_mage_xp = 24.5;
				damage = target instanceof NPC && ((NPC)target).getId() == 28063 ? target.getHitpoints() : getRandomMagicMaxHit(player, 150);
				delayMagicHit(2, getMagicHit(player, damage));
				World.sendProjectile(player, target, 146, 36, 36, 50, 50, 5, 0);
				return 5;
			case 54: //iban spell 
				player.setNextGraphics(new Graphics(87, 0, 100));
				player.setNextAnimation(new Animation(708));
				mage_hit_gfx = 89;
				base_mage_xp = 30;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 250)));
				World.sendProjectile(player, target, 88, 36, 36, 50, 50, 5, 0);
				return 5;
			case 56: //slayer spell 
				player.setNextGraphics(new Graphics(327, 0, 100));
				player.setNextAnimation(new Animation(1575));
				mage_hit_gfx = 329;
				base_mage_xp = 30;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 
						 (int) (player.getEquipment().getWeaponId() == 51255
						 && target instanceof NPC &&
						 player.getSlayerManager().isValidTask(((NPC) target).getName()) ?
								 (player.getSkills().getLevel(Skills.MAGIC) / 0.6 + 130) :
								 (player.getSkills().getLevel(Skills.MAGIC) + 100)))));
				World.sendProjectile(player, target, 328, 36, 36, 50, 50, 30, 0);
				return 5;
			case 52: // water blast
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 135 : 2701, 0, player.isOsrsMagicToggle() ? 92 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1163 : 14220));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 137:2710;
				base_mage_xp = 31.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 140)));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2705, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 136, 43, 31, 50, 50, 16, 64);
				return 5;
			case 58: // earth blast
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 138 : 2715, 0, player.isOsrsMagicToggle() ? 92 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1163 : 14222));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 140:2725;
				base_mage_xp = 31.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 150)));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2720, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 139, 43, 31, 50, 50, 16, 64);
				return 5;
			case 63: // fire blast
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 129 : 2728, 0, player.isOsrsMagicToggle() ? 92 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1163 : 14223));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 131:2739;
				base_mage_xp = 34.5;
				baseDamage = 160;
				if (player.getEquipment().getShieldId() == 50714)
					baseDamage *= 1.5;
				damage = getRandomMagicMaxHit(player, baseDamage);
				delayMagicHit(2, getMagicHit(player, damage));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2733, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 130, 43, 31, 50, 50, 16, 64);
				return 5;
			case 66:// Saradomin Strike
				player.setNextAnimation(new Animation(811));
				mage_hit_gfx = 76;
				base_mage_xp = 34.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 300)));
				return 5;
			case 67: // Claws of Guthix
				player.setNextAnimation(new Animation(811));
				mage_hit_gfx = 77;
				base_mage_xp = 34.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 300)));
				return 5;
			case 68: // Flames of Zamorak
				player.setNextAnimation(new Animation(811));
				mage_hit_gfx = 78;
				base_mage_xp = 34.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 300)));
				return 5;
			case 70: // air wave
				if(player.isOsrsMagicToggle())
					player.setNextGraphics(new Graphics(Settings.OSRS_GFX_OFFSET + 158, 0, 100));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1167 : 14221));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 160:2700;
				base_mage_xp = 36;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 170)));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2699, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 159, 43, 31, 50, 50, 16, 64);
				return 5;
			case 73: // water wave
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 161 : 2702, 0, player.isOsrsMagicToggle() ? 100 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1167 : 14220));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 163:2710;
				base_mage_xp = 37.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 180)));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2721, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 162, 43, 31, 50, 50, 16, 64);
				return 5;
			case 77: // earth wave
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 164 : 2716, 0, player.isOsrsMagicToggle() ? 100 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1167 : 14222));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 166:2726;
				base_mage_xp = 42.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 190)));

				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2721, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 165, 43, 31, 50, 50, 16, 64);
				return 5;
			case 80: // fire wave
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 155 : 2728, 0, player.isOsrsMagicToggle() ? 100 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1167 : 14223));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 157:2740;
				base_mage_xp = 42.5;
				spell_type = FIRE_SPELL;
				baseDamage = 200;
				if (player.getEquipment().getShieldId() == 50714)
					baseDamage *= 1.5;
				damage = getRandomMagicMaxHit(player, baseDamage);
				delayMagicHit(2, getMagicHit(player, damage));
				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 155, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 156, 43, 31, 50, 50, 16, 64);
				return 5;
			case 86: // teleblock
				if (target instanceof Player && ((Player) target).getTeleBlockDelay() <= Utils.currentTimeMillis()) {
					player.setNextGraphics(new Graphics(1841));
					player.setNextAnimation(new Animation(10503));
					mage_hit_gfx = 1843;
					base_mage_xp = 80;
					block_tele = true;
					Hit hit = getMagicHit(player, getRandomMagicMaxHit(player, 30));
					delayMagicHit(2, hit);
					World.sendProjectile(player, target, 1842, 18, 18, 50, 50, 0, 0);
				} else {
					player.getPackets().sendGameMessage("This player is already effected by this spell.", true);
				}
				return 5;
			case 84:// air surge
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 1455 : 457, 0, player.isOsrsMagicToggle() ? 100 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1165 : 10546));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 1457:2712;
				base_mage_xp = 80;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 210)));
				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 462, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 1456, 43, 31, 50, 50, 16, 64);
				return 5;
			case 87:// water surge
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 1458 : 2701, 0, player.isOsrsMagicToggle() ? 100 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1165 : 10542));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 1460:2712;
				base_mage_xp = 80;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 220)));
				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2707, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 1459, 43, 31, 50, 50, 16, 64);
				return 5;
			case 89:// earth surge
				player.setNextGraphics(new Graphics(
						player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 1461 : 2717, 0, player.isOsrsMagicToggle() ? 100 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1165 : 14209));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 1463:2727;
				base_mage_xp = 80;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 230)));
				if(!player.isOsrsMagicToggle())
					World.sendProjectile(player, target, 2722, 18, 18, 50, 50, 0, 0);
				else
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 1462, 43, 31, 50, 50, 16, 64);
				return 5;
			case 91:// fire surge
				player.setNextGraphics(new Graphics(player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 1464 : 2728, 0, player.isOsrsMagicToggle() ? 100 : 0));
				player.setNextAnimation(new Animation(player.isOsrsMagicToggle() ? Settings.OSRS_ANIMATIONS_OFFSET + 1165 : 2791));
				mage_hit_gfx = player.isOsrsMagicToggle() ? Settings.OSRS_GFX_OFFSET + 1466:2741;
				base_mage_xp = 80;
				spell_type = FIRE_SPELL;
				baseDamage = 240;
				if (player.getEquipment().getShieldId() == 50714)
					baseDamage *= 1.5;
				damage = getRandomMagicMaxHit(player, baseDamage);
				delayMagicHit(2, getMagicHit(player, damage));

				if(!player.isOsrsMagicToggle()) {
					World.sendProjectile(player, target, 2735, 18, 18, 50, 50, 3, 0);
					World.sendProjectile(player, target, 2736, 18, 18, 50, 50, 20, 0);
					World.sendProjectile(player, target, 2736, 18, 18, 50, 50, 110, 0);
				} else {
					World.sendProjectile(player, target, Settings.OSRS_GFX_OFFSET+ 1465, 43, 31, 50, 50, 16, 64);
				}
				return 5;
			case 26: //confuse
				player.setNextAnimation(new Animation(711));
				player.setNextGraphics(new Graphics(102));
				mage_hit_gfx = 104;
				base_mage_xp = 13;
				damage = getRandomMagicMaxHit(player, 30);
				World.sendProjectile(player, target, 103, 39, 18, 55, 70, 5, 0);
				if (damage > 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (target instanceof Player) {
								Player player = (Player) target;
								player.getSkills().set(Skills.ATTACK, (int) (player.getSkills().getLevel(Skills.ATTACK) * .95));
							}
						}
					});
				}
				return 5;
			case 31: //weaken
				player.setNextAnimation(new Animation(716));
				player.setNextGraphics(new Graphics(105));
				mage_hit_gfx = 107;
				base_mage_xp = 21;
				damage = getRandomMagicMaxHit(player, 50);
				World.sendProjectile(player, target, 106, 39, 18, 55, 70, 5, 0);
				if (damage > 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (target instanceof Player) {
								Player player = (Player) target;
								player.getSkills().set(Skills.STRENGTH, (int) (player.getSkills().getLevel(Skills.STRENGTH) * .95));
							}
						}
					});
				}
				return 5;
			case 35: //curse
				player.setNextAnimation(new Animation(724));
				player.setNextGraphics(new Graphics(108));
				mage_hit_gfx = 110;
				base_mage_xp = 29;
				World.sendProjectile(player, target, 109, 39, 18, 55, 70, 5, 0);
				damage = getRandomMagicMaxHit(player, 70);
				if (damage > 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (target instanceof Player) {
								Player player = (Player) target;
								player.getSkills().set(Skills.DEFENCE, (int) (player.getSkills().getLevel(Skills.DEFENCE) * .95));
							}
						}
					});
				}
				return 5;
			case 75: //vulnerability
				player.setNextAnimation(new Animation(729));
				player.setNextGraphics(new Graphics(167));
				mage_hit_gfx = 169;
				base_mage_xp = 76;
				World.sendProjectile(player, target, 168, 39, 18, 55, 70, 5, 0);
				damage = getRandomMagicMaxHit(player, 100);
				if (damage > 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (target instanceof Player) {
								Player player = (Player) target;
								player.getSkills().set(Skills.DEFENCE, (int) (player.getSkills().getLevel(Skills.DEFENCE) * .90));
							}
						}
					});
				}
				return 5;
			case 78: //enfeeble
				player.setNextAnimation(new Animation(729));
				player.setNextGraphics(new Graphics(170));
				mage_hit_gfx = 172;
				base_mage_xp = 83;
				World.sendProjectile(player, target, 171, 39, 18, 55, 70, 5, 0);
				damage = getRandomMagicMaxHit(player, 100);
				if (damage > 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (target instanceof Player) {
								Player player = (Player) target;
								player.getSkills().set(Skills.STRENGTH, (int) (player.getSkills().getLevel(Skills.STRENGTH) * .90));
							}
						}
					});
				}
				return 5;
			case 82: //stun
				player.setNextAnimation(new Animation(729));
				player.setNextGraphics(new Graphics(173));
				mage_hit_gfx = 107;
				base_mage_xp = 90;
				World.sendProjectile(player, target, 174, 39, 18, 55, 70, 5, 0);
				damage = getRandomMagicMaxHit(player, 100);
				if (damage > 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (target instanceof Player) {
								Player player = (Player) target;
								player.getSkills().set(Skills.ATTACK, (int) (player.getSkills().getLevel(Skills.ATTACK) * .90));
							}
						}
					});
				}
				return 5;
			case 99: // Storm of armadyl //Sonic and Tyler dumped
				player.setNextGraphics(new Graphics(457));
				player.setNextAnimation(new Animation(10546));
				mage_hit_gfx = 1019;
				base_mage_xp = 70;
				int boost = (player.getSkills().getLevel(Skills.MAGIC) - 77) * 5;
				if (boost < 0)
					boost = 0;
				int hit = getRandomMagicMaxHit(player, 160 + boost);
				if (hit > 0 && hit < boost)
					hit += boost;
				delayMagicHit(2, getMagicHit(player, hit));
				World.sendProjectile(player, target, 1019, 18, 18, 50, 30, 0, 0);
				return player.getEquipment().getWeaponId() == 21777 || player.getEquipment().getWeaponId() == 25442 ? 4 : 5;
			}
		} else if (player.getCombatDefinitions().getSpellBook() == 950) {
			switch (spellId) {
			case 25:
				player.setNextAnimation(new Animation(14221));
				mage_hit_gfx = 2700;
				base_mage_xp = 5.5;
				int baseDamage = 20;
				if (player.getEquipment().getGlovesId() == 205) {
					baseDamage = 90;
				}
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, baseDamage)));
				World.sendProjectile(player, target, 2699, 18, 18, 50, 50, 0, 0);
				return 5;
			case 27:
				player.setNextGraphics(new Graphics(2701));
				player.setNextAnimation(new Animation(14221));
				mage_hit_gfx = 2708;
				base_mage_xp = 7.5;
				baseDamage = 40;
				if (player.getEquipment().getGlovesId() == 205) {
					baseDamage = 100;
				}
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, baseDamage)));
				World.sendProjectile(player, target, 2703, 18, 18, 50, 50, 0, 0);
				return 5;
			case 28:
				player.setNextGraphics(new Graphics(2713));
				player.setNextAnimation(new Animation(14221));
				mage_hit_gfx = 2723;
				base_mage_xp = 9.5;
				baseDamage = 60;
				if (player.getEquipment().getGlovesId() == 205) {
					baseDamage = 110;
				}
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, baseDamage)));
				World.sendProjectile(player, target, 2718, 18, 18, 50, 50, 0, 0);
				return 5;
			case 30:
				player.setNextGraphics(new Graphics(2728));
				player.setNextAnimation(new Animation(14221));
				mage_hit_gfx = 2737;
				base_mage_xp = 11.5;
				spell_type = FIRE_SPELL;
				baseDamage = 80;
				if (player.getEquipment().getGlovesId() == 205) {
					baseDamage = 120;
				}
				int damage = getRandomMagicMaxHit(player, baseDamage);
				delayMagicHit(2, getMagicHit(player, damage));
				World.sendProjectile(player, target, 2729, 18, 18, 50, 50, 0, 0);
				return 5;
			case 32: // air bolt
				player.setNextAnimation(new Animation(14220));
				mage_hit_gfx = 2700;
				base_mage_xp = 13.5;
				baseDamage = 90;
				if (player.getEquipment().getGlovesId() == 777) {
					baseDamage = 120;
				}
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, baseDamage)));
				World.sendProjectile(player, target, 2699, 18, 18, 50, 50, 0, 0);
				return 5;
			case 36: // water bolt
				player.setNextGraphics(new Graphics(2707, 0, 100));
				player.setNextAnimation(new Animation(14220));
				mage_hit_gfx = 2709;
				base_mage_xp = 16.5;
				baseDamage = 100;
				if (player.getEquipment().getGlovesId() == 777) {
					baseDamage = 130;
				}
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, baseDamage)));
				World.sendProjectile(player, target, 2704, 18, 18, 50, 50, 0, 0);
				return 5;
			case 37: // earth bolt
				player.setNextGraphics(new Graphics(2714));
				player.setNextAnimation(new Animation(14222));
				mage_hit_gfx = 2724;
				base_mage_xp = 19.5;
				baseDamage = 110;
				if (player.getEquipment().getGlovesId() == 777) {
					baseDamage = 140;
				}
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, baseDamage)));
				World.sendProjectile(player, target, 2719, 18, 18, 50, 50, 0, 0);
				return 5;
			case 41: // fire bolt
				player.setNextGraphics(new Graphics(2728));
				player.setNextAnimation(new Animation(14223));
				mage_hit_gfx = 2738;
				base_mage_xp = 22.5;
				spell_type = FIRE_SPELL;
				baseDamage = 120;
				if (player.getEquipment().getGlovesId() == 777) {
					baseDamage = 150;
				}
				damage = getRandomMagicMaxHit(player, baseDamage);
				delayMagicHit(2, getMagicHit(player, damage));
				World.sendProjectile(player, target, 2731, 18, 18, 50, 50, 0, 0);
				return 5;
			case 42: // air blast
				player.setNextAnimation(new Animation(14221));
				mage_hit_gfx = 2700;
				base_mage_xp = 25.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 130)));
				World.sendProjectile(player, target, 2699, 18, 18, 50, 50, 0, 0);
				return 5;
			case 43: // water blast
				player.setNextGraphics(new Graphics(2701));
				player.setNextAnimation(new Animation(14220));
				mage_hit_gfx = 2710;
				base_mage_xp = 31.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 140)));
				World.sendProjectile(player, target, 2705, 18, 18, 50, 50, 0, 0);
				return 5;
			case 45: // earth blast
				player.setNextGraphics(new Graphics(2715));
				player.setNextAnimation(new Animation(14222));
				mage_hit_gfx = 2725;
				base_mage_xp = 31.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 150)));
				World.sendProjectile(player, target, 2720, 18, 18, 50, 50, 0, 0);
				return 5;
			case 47: // fire blast
				player.setNextGraphics(new Graphics(2728));
				player.setNextAnimation(new Animation(14223));
				mage_hit_gfx = 2739;
				base_mage_xp = 34.5;
				spell_type = FIRE_SPELL;
				damage = getRandomMagicMaxHit(player, 160);
				delayMagicHit(2, getMagicHit(player, damage));
				World.sendProjectile(player, target, 2733, 18, 18, 50, 50, 0, 0);
				return 5;
			case 48: // air wave
				player.setNextAnimation(new Animation(14221));
				mage_hit_gfx = 2700;
				base_mage_xp = 36;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 170)));
				World.sendProjectile(player, target, 2699, 18, 18, 50, 50, 0, 0);
				return 5;
			case 49: // water wave
				player.setNextGraphics(new Graphics(2702));
				player.setNextAnimation(new Animation(14220));
				mage_hit_gfx = 2710;
				base_mage_xp = 37.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 180)));
				World.sendProjectile(player, target, 2706, 18, 18, 50, 50, 0, 0);
				return 5;
			case 54: // earth wave
				player.setNextGraphics(new Graphics(2716));
				player.setNextAnimation(new Animation(14222));
				mage_hit_gfx = 2726;
				base_mage_xp = 42.5;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 190)));
				World.sendProjectile(player, target, 2721, 18, 18, 50, 50, 0, 0);
				return 5;
			case 58: // fire wave
				player.setNextGraphics(new Graphics(2728));
				player.setNextAnimation(new Animation(14223));
				mage_hit_gfx = 2740;
				base_mage_xp = 42.5;
				spell_type = FIRE_SPELL;
				damage = getRandomMagicMaxHit(player, 200);
				delayMagicHit(2, getMagicHit(player, damage));
				World.sendProjectile(player, target, 2735, 18, 18, 50, 50, 0, 0);
				return 5;
			case 61:// air surge
				player.setNextGraphics(new Graphics(457));
				player.setNextAnimation(new Animation(10546));
				mage_hit_gfx = 2700;
				base_mage_xp = 80;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 220)));
				World.sendProjectile(player, target, 462, 18, 18, 50, 50, 0, 0);
				return 5;
			case 62:// water surge
				player.setNextGraphics(new Graphics(2701));
				player.setNextAnimation(new Animation(10542));
				mage_hit_gfx = 2712;
				base_mage_xp = 80;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 240)));
				World.sendProjectile(player, target, 2707, 18, 18, 50, 50, 3, 0);
				return 5;
			case 63:// earth surge
				player.setNextGraphics(new Graphics(2717));
				player.setNextAnimation(new Animation(14209));
				mage_hit_gfx = 2727;
				base_mage_xp = 80;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 260)));
				World.sendProjectile(player, target, 2722, 18, 18, 50, 50, 0, 0);
				return 5;
			case 67:// fire surge
				player.setNextGraphics(new Graphics(2728));
				player.setNextAnimation(new Animation(2791));
				mage_hit_gfx = 2741;
				base_mage_xp = 80;
				spell_type = FIRE_SPELL;
				damage = getRandomMagicMaxHit(player, 280);
				delayMagicHit(2, getMagicHit(player, damage));
				World.sendProjectile(player, target, 2735, 18, 18, 50, 50, 3, 0);
				World.sendProjectile(player, target, 2736, 18, 18, 50, 50, 20, 0);
				World.sendProjectile(player, target, 2736, 18, 18, 50, 50, 110, 0);
				return 5;
			case 34:// bind
				player.setNextGraphics(new Graphics(177));
				player.setNextAnimation(new Animation(710));
				mage_hit_gfx = 181;
				base_mage_xp = 60.5;
				freeze_time = 5000;
				Hit magicHit = getMagicHit(player, getRandomMagicMaxHit(player, 20));
				delayMagicHit(2, magicHit);
				World.sendProjectile(player, target, 178, 18, 18, 50, 50, 0, 0);
				return 5;
			case 44:// snare
				player.setNextGraphics(new Graphics(177));
				player.setNextAnimation(new Animation(710));
				mage_hit_gfx = 180;
				base_mage_xp = 91.1;
				freeze_time = 10000;
				Hit snareHit = getMagicHit(player, getRandomMagicMaxHit(player, 30));
				delayMagicHit(2, snareHit);
				World.sendProjectile(player, target, 178, 18, 18, 50, 50, 0, 0);
				return 5;
			case 59:// entangle
				player.setNextGraphics(new Graphics(177));
				player.setNextAnimation(new Animation(710));
				mage_hit_gfx = 179;
				base_mage_xp = 91.1;
				freeze_time = 15000;
				Hit entangleHit = getMagicHit(player, getRandomMagicMaxHit(player, 50));
				delayMagicHit(2, entangleHit);
				World.sendProjectile(player, target, 178, 18, 18, 50, 50, 0, 0);
				return 5;
			case 26: //confuse
				player.setNextAnimation(new Animation(711));
				player.setNextGraphics(new Graphics(102));
				mage_hit_gfx = 104;
				base_mage_xp = 13;
				damage = getRandomMagicMaxHit(player, 30);
				World.sendProjectile(player, target, 103, 39, 18, 55, 70, 5, 0);
				if (damage > 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (target instanceof Player) {
								Player player = (Player) target;
								player.getSkills().set(Skills.ATTACK, (int) (player.getSkills().getLevel(Skills.ATTACK) * .95));
							}
						}
					});
				}
				return 5;
			case 29: //weaken
				player.setNextAnimation(new Animation(716));
				player.setNextGraphics(new Graphics(105));
				mage_hit_gfx = 107;
				base_mage_xp = 21;
				damage = getRandomMagicMaxHit(player, 50);
				World.sendProjectile(player, target, 106, 39, 18, 55, 70, 5, 0);
				if (damage > 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (target instanceof Player) {
								Player player = (Player) target;
								player.getSkills().set(Skills.STRENGTH, (int) (player.getSkills().getLevel(Skills.STRENGTH) * .95));
							}
						}
					});
				}
				return 5;
			case 33: //curse
				player.setNextAnimation(new Animation(724));
				player.setNextGraphics(new Graphics(108));
				mage_hit_gfx = 110;
				base_mage_xp = 29;
				World.sendProjectile(player, target, 109, 39, 18, 55, 70, 5, 0);
				damage = getRandomMagicMaxHit(player, 70);
				if (damage > 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (target instanceof Player) {
								Player player = (Player) target;
								player.getSkills().set(Skills.DEFENCE, (int) (player.getSkills().getLevel(Skills.DEFENCE) * .95));
							}
						}
					});
				}
				return 5;
			case 50: //vulnerability
				player.setNextAnimation(new Animation(729));
				player.setNextGraphics(new Graphics(167));
				mage_hit_gfx = 169;
				base_mage_xp = 76;
				World.sendProjectile(player, target, 168, 39, 18, 55, 70, 5, 0);
				damage = getRandomMagicMaxHit(player, 100);
				if (damage > 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (target instanceof Player) {
								Player player = (Player) target;
								player.getSkills().set(Skills.DEFENCE, (int) (player.getSkills().getLevel(Skills.DEFENCE) * .90));
							}
						}
					});
				}
				return 5;
			case 56: //enfeeble
				player.setNextAnimation(new Animation(729));
				player.setNextGraphics(new Graphics(170));
				mage_hit_gfx = 172;
				base_mage_xp = 83;
				World.sendProjectile(player, target, 171, 39, 18, 55, 70, 5, 0);
				damage = getRandomMagicMaxHit(player, 100);
				if (damage > 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (target instanceof Player) {
								Player player = (Player) target;
								player.getSkills().set(Skills.STRENGTH, (int) (player.getSkills().getLevel(Skills.STRENGTH) * .90));
							}
						}
					});
				}
				return 5;
			case 60: //stun
				player.setNextAnimation(new Animation(729));
				player.setNextGraphics(new Graphics(173));
				mage_hit_gfx = 107;
				base_mage_xp = 90;
				World.sendProjectile(player, target, 174, 39, 18, 55, 70, 5, 0);
				damage = getRandomMagicMaxHit(player, 100);
				if (damage > 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (target instanceof Player) {
								Player player = (Player) target;
								player.getSkills().set(Skills.ATTACK, (int) (player.getSkills().getLevel(Skills.ATTACK) * .90));
							}
						}
					});
				}
				return 5;
			}

		} else if (player.getCombatDefinitions().getSpellBook() == 193) {
			switch (spellId) {
			case 28:// Smoke Rush
				player.setNextAnimation(new Animation(1978));
				mage_hit_gfx = 385;
				base_mage_xp = 30;
				max_poison_hit = 20;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 150)));
				World.sendProjectile(player, target, 386, 18, 18, 50, 50, 0, 0);
				return 5;
			case 32:// Shadow Rush
				player.setNextAnimation(new Animation(1978));
				mage_hit_gfx = 379;
				base_mage_xp = 31;
				reduceAttack = true;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 160)));
				World.sendProjectile(player, target, 380, 18, 18, 50, 50, 0, 0);
				return 5;
			case 36: // Miasmic rush
				player.setNextAnimation(new Animation(10513));
				player.setNextGraphics(new Graphics(1845));
				mage_hit_gfx = 1847;
				base_mage_xp = 35;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 200)));
				World.sendProjectile(player, target, 1846, 43, 22, 51, 50, 16, 0);
				if (target.getTemporaryAttributtes().get("miasmic_immunity") == Boolean.TRUE) {
					return 5;
				}
				if (target instanceof Player) {
					((Player) target).getPackets().sendGameMessage("You feel slowed down.");
				}
				target.getTemporaryAttributtes().put("miasmic_immunity", Boolean.TRUE);
				target.getTemporaryAttributtes().put("miasmic_effect", Boolean.TRUE);
				final Entity t = target;
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						t.getTemporaryAttributtes().remove("miasmic_effect");
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								t.getTemporaryAttributtes().remove("miasmic_immunity");
								stop();
							}
						}, 15);
						stop();
					}
				}, 20);
				return 5;
			case 37: // Miasmic blitz
				player.setNextAnimation(new Animation(10524));
				player.setNextGraphics(new Graphics(1850));
				mage_hit_gfx = 1851;
				base_mage_xp = 48;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 280)));
				World.sendProjectile(player, target, 1852, 43, 22, 51, 50, 16, 0);
				if (target.getTemporaryAttributtes().get("miasmic_immunity") == Boolean.TRUE) {
					return 4;
				}
				if (target instanceof Player) {
					((Player) target).getPackets().sendGameMessage("You feel slowed down.");
				}
				target.getTemporaryAttributtes().put("miasmic_immunity", Boolean.TRUE);
				target.getTemporaryAttributtes().put("miasmic_effect", Boolean.TRUE);
				final Entity t0 = target;
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						t0.getTemporaryAttributtes().remove("miasmic_effect");
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								t0.getTemporaryAttributtes().remove("miasmic_immunity");
								stop();
							}
						}, 15);
						stop();
					}
				}, 60);
				return 5;
			case 38: // Miasmic burst
				player.setNextAnimation(new Animation(10516));
				player.setNextGraphics(new Graphics(1848));
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {
					private boolean nextTarget;

					@Override
					public boolean attack() {
						mage_hit_gfx = 1849;
						base_mage_xp = 42;
						int damage = getRandomMagicMaxHit(player, 240);
						delayMagicHit(2, getMagicHit(player, damage));
						if (target.getTemporaryAttributtes().get("miasmic_immunity") != Boolean.TRUE) {
							if (target instanceof Player) {
								((Player) target).getPackets().sendGameMessage("You feel slowed down.");
							}
							target.getTemporaryAttributtes().put("miasmic_immunity", Boolean.TRUE);
							target.getTemporaryAttributtes().put("miasmic_effect", Boolean.TRUE);
							final Entity t = target;
							WorldTasksManager.schedule(new WorldTask() {
								@Override
								public void run() {
									t.getTemporaryAttributtes().remove("miasmic_effect");
									WorldTasksManager.schedule(new WorldTask() {
										@Override
										public void run() {
											t.getTemporaryAttributtes().remove("miasmic_immunity");
											stop();
										}
									}, 15);
									stop();
								}
							}, 40);
						}
						if (!nextTarget) {
							if (damage == -1) {
								return false;
							}
							nextTarget = true;
						}
						return nextTarget;

					}
				});
				return 5;
			case 39: // Miasmic barrage
				player.setNextAnimation(new Animation(10518));
				player.setNextGraphics(new Graphics(1853));
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {
					private boolean nextTarget;

					@Override
					public boolean attack() {
						mage_hit_gfx = 1854;
						base_mage_xp = 54;
						int damage = getRandomMagicMaxHit(player, 320);
						delayMagicHit(2, getMagicHit(player, damage));
						if (target.getTemporaryAttributtes().get("miasmic_immunity") != Boolean.TRUE) {
							if (target instanceof Player) {
								((Player) target).getPackets().sendGameMessage("You feel slowed down.");
							}
							target.getTemporaryAttributtes().put("miasmic_immunity", Boolean.TRUE);
							target.getTemporaryAttributtes().put("miasmic_effect", Boolean.TRUE);
							final Entity t = target;
							WorldTasksManager.schedule(new WorldTask() {
								@Override
								public void run() {
									t.getTemporaryAttributtes().remove("miasmic_effect");
									WorldTasksManager.schedule(new WorldTask() {
										@Override
										public void run() {
											t.getTemporaryAttributtes().remove("miasmic_immunity");
											stop();
										}
									}, 15);
									stop();
								}
							}, 80);
						}
						if (!nextTarget) {
							if (damage == -1) {
								return false;
							}
							nextTarget = true;
						}
						return nextTarget;

					}
				});
				return 5;
			case 24:// Blood rush
				player.setNextAnimation(new Animation(1978));
				mage_hit_gfx = 373;
				base_mage_xp = 33;
				blood_spell = true;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 170)));
				World.sendProjectile(player, target, 374, 18, 18, 50, 50, 0, 0);
				return 5;
			case 20:// Ice rush
				player.setNextAnimation(new Animation(1978));
				mage_hit_gfx = 361;
				base_mage_xp = 34;
				freeze_time = 5000;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 180)));
				World.sendProjectile(player, target, 362, 18, 18, 50, 50, 0, 0);
				return 5;
				// TODO burst
			case 30:// Smoke burst
				player.setNextAnimation(new Animation(1979));
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {
					private boolean nextTarget; // real target is first player on array

					@Override
					public boolean attack() {
						mage_hit_gfx = 389;
						base_mage_xp = 36;
						max_poison_hit = 40;
						int damage = getRandomMagicMaxHit(player, 190);
						delayMagicHit(2, getMagicHit(player, damage));
						World.sendProjectile(player, target, 388, 18, 18, 50, 50, 0, 0);
						if (!nextTarget) {
							if (damage == -1)
								return false;
							nextTarget = true;
						}
						return nextTarget;

					}
				});
				return 5;
			case 34:// Shadow burst
				player.setNextAnimation(new Animation(1979));
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {
					private boolean nextTarget; // real target is first player on array

					@Override
					public boolean attack() {
						mage_hit_gfx = 382;
						base_mage_xp = 37;
						reduceAttack = true;
						int damage = getRandomMagicMaxHit(player, 200);
						delayMagicHit(2, getMagicHit(player, damage));
						if (!nextTarget) {
							if (damage == -1)
								return false;
							nextTarget = true;
						}
						return nextTarget;

					}
				});
				return 5;
			case 26:// Blood burst
				player.setNextAnimation(new Animation(1979));
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {
					private boolean nextTarget; // real target is first player on array

					@Override
					public boolean attack() {
						mage_hit_gfx = 376;
						base_mage_xp = 39;
						blood_spell = true;
						int damage = getRandomMagicMaxHit(player, 210);
						delayMagicHit(2, getMagicHit(player, damage));
						if (!nextTarget) {
							if (damage == -1)
								return false;
							nextTarget = true;
						}
						return nextTarget;

					}
				});
				return 5;
			case 29:// Smoke Blitz
				player.setNextAnimation(new Animation(1978));
				mage_hit_gfx = 387;
				base_mage_xp = 42;
				max_poison_hit = 60;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 230)));
				World.sendProjectile(player, target, 386, 18, 18, 50, 50, 0, 0);
				return 5;
			case 33:// Shadow Blitz
				player.setNextAnimation(new Animation(1978));
				mage_hit_gfx = 381;
				base_mage_xp = 43;
				reduceAttack = true;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 240)));
				World.sendProjectile(player, target, 380, 18, 18, 50, 50, 0, 0);
				return 5;
			case 25:// Blood Blitz
				player.setNextAnimation(new Animation(1978));
				mage_hit_gfx = 375;
				base_mage_xp = 45;
				blood_spell = true;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 250)));
				World.sendProjectile(player, target, 374, 18, 18, 50, 50, 0, 0);
				return 5;
			case 31:// Smoke barrage
				player.setNextAnimation(new Animation(1979));
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {
					private boolean nextTarget; // real target is first player on array

					@Override
					public boolean attack() {
						mage_hit_gfx = 391;
						base_mage_xp = 48;
						max_poison_hit = 80;
						int damage = getRandomMagicMaxHit(player, 270);
						delayMagicHit(2, getMagicHit(player, damage));
						World.sendProjectile(player, target, 390, 18, 18, 50, 50, 0, 0);
						if (!nextTarget) {
							if (damage == -1)
								return false;
							nextTarget = true;
						}
						return nextTarget;

					}
				});
				return 5;
			case 35:// shadow barrage
				player.setNextAnimation(new Animation(1979));
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {
					private boolean nextTarget; // real target is first player on array

					@Override
					public boolean attack() {
						mage_hit_gfx = 383;
						base_mage_xp = 49;
						reduceAttack = true;
						int damage = getRandomMagicMaxHit(player, 280);
						delayMagicHit(2, getMagicHit(player, damage));
						if (!nextTarget) {
							if (damage == -1)
								return false;
							nextTarget = true;
						}
						return nextTarget;

					}
				});
				return 5;
			case 27:// blood barrage
				player.setNextAnimation(new Animation(1979));
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {
					private boolean nextTarget; // real target is first player on array

					@Override
					public boolean attack() {
						mage_hit_gfx = 377;
						base_mage_xp = 51;
						blood_spell = true;
						int damage = getRandomMagicMaxHit(player, 290);
						delayMagicHit(2, getMagicHit(player, damage));
						if (!nextTarget) {
							if (damage == -1)
								return false;
							nextTarget = true;
						}
						return nextTarget;

					}
				});
				return 5;
			case 23: // ice barrage
				player.setNextAnimation(new Animation(1979));
				playVoice(5540, player, target);
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {

					private boolean nextTarget; //real target is first player on array

					@Override
					public boolean attack() {
						magic_voice = 5531;
						long currentTime = Utils.currentTimeMillis();
						if (/*(target.getSize() >= 2 && !(target instanceof Scorpia))||*/ target.getFreezeDelay() >= currentTime || target.getFrozenBlockedDelay() >= currentTime) {
							mage_hit_gfx = 1677;
						} else {
							mage_hit_gfx = 369;
							freeze_time = 20000;
						}
						base_mage_xp = 52;
						int damage = getRandomMagicMaxHit(player, 300);
						Hit hit = getMagicHit(player, damage);
						delayMagicHit(Utils.getDistance(player, target) > 5 ? 4 : Utils.getDistance(player, target) > 3 ? 3 : 2, hit);
						World.sendProjectile(player, target, 368, 45, 38, 35, 35, 5, 0);//368
						if (!nextTarget) {
							if (damage == -1)
								return false;
							nextTarget = true;
						}
						return nextTarget;

					}
				});
				return 5;
			case 21:// Ice Blitz
				player.setNextGraphics(new Graphics(366));
				player.setNextAnimation(new Animation(1978));
				playVoice(5533, player, target); //notsure if you have playVoice, but voiceid is correct for blitz
				mage_hit_gfx = 367;
				base_mage_xp = 46;
				freeze_time = 15000;
				magic_voice = 5534;
				delayMagicHit(2, getMagicHit(player, getRandomMagicMaxHit(player, 260)));
				return 5;
			case 22:// Ice burst
				player.setNextAnimation(new Animation(1979));
				playVoice(5540, player, target);
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {

					private boolean nextTarget; //real target is first player on array

					@Override
					public boolean attack() {
						mage_hit_gfx = 363;
						base_mage_xp = 40;
						freeze_time = 10000;
						magic_voice = 5537;
						int damage = getRandomMagicMaxHit(player, 220);
						delayMagicHit(Utils.getDistance(player, target) > 5 ? 4 : Utils.getDistance(player, target) > 3 ? 3 : 2, getMagicHit(player, damage));
						World.sendProjectile(player, target, 362, 18, 18, 50, 50, 0, 0);
						if (!nextTarget) {
							if (damage == -1)
								return false;
							nextTarget = true;
						}
						return nextTarget;

					}
				});
				return 5;
			}

		}
		return -1; // stops atm
	}

	public interface MultiAttack {
		public boolean attack();
	}

	public void attackTarget(Entity[] targets, MultiAttack perform) {
		Entity realTarget = target;
		for (Entity t : targets) {
			target = t;
			if (!perform.attack())
				break;
		}
		target = realTarget;
	}

	@Deprecated
	public int getRandomMagicMaxHit(Player player, int baseDamage) {
		return getDamage(player, 0, 0, 2, baseDamage, true, 1D, false);
	}

	public int getMagicMaxHit(Player player, int baseDamage) {
		int max_hit = baseDamage;
		if (target instanceof NPC) {
			double boost = 1 + ((player.getSkills().getLevel(Skills.MAGIC) - player.getSkills().getLevelForXp(Skills.MAGIC)) * 0.03);
			if (boost > 1)
				max_hit *= boost;
		}
		double magicPerc = player.getCombatDefinitions().getBonuses()[CombatDefinitions.MAGIC_DAMAGE];
		if (spellcasterGloves > 0) {
			if (baseDamage > 60 || spellcasterGloves == 28 || spellcasterGloves == 25) {
				magicPerc += 17;
				if (target instanceof Player) {
					Player p = (Player) target;
					p.getSkills().drainLevel(0, p.getSkills().getLevel(0) / 10);
					p.getSkills().drainLevel(1, p.getSkills().getLevel(1) / 10);
					p.getSkills().drainLevel(2, p.getSkills().getLevel(2) / 10);
					p.getPackets().sendGameMessage("Your melee skills have been drained.");
					player.getPackets().sendGameMessage("Your spell weakened your enemy.");
				}
				player.getPackets().sendGameMessage("Your magic surged with extra power.");
			}
		}
		double boost = magicPerc / 100 + 1;
		if (target instanceof NPC) {
			 if ((player.getEquipment().getAmuletId() == 42018 || player.getEquipment().getAmuletId() == 25486  || player.getEquipment().getAmuletId() == 25740) && Combat.isUndead(target))
				 boost += 0.2;
			 else if (player.getEquipment().getHatId() == 15488 || Slayer.hasFullSlayerHelmet(player)) {
				if (player.getSlayerManager().isValidTask(((NPC) target).getName()) || (player.getSlayerManager().getBossTask() != null && player.getSlayerManager().getBossTask().equalsIgnoreCase(target.getName())))
					boost += 0.125;
			}
			 if (player.getEquipment().getWeaponId() == 25541 && Combat.isUndead(target))
			 	if(MysticsChamber.isMysticNPC(target.asNPC().getId()))
			 		boost += 1.15;
			 	else
			 		boost += 1;
		}
		if (!(target instanceof Player))
			max_hit =  (int) Math.floor(max_hit * getUltimateMageEffect(player));
		max_hit *= boost;
		if (player.getEquipment().getWeaponId() == 42000 && player.getCombatDefinitions().getSpellBook() == 192)
			max_hit *= 1.1;
		if (player.getEquipment().getWeaponId() == 52555 && target instanceof NPC && Wilderness.isAtWild(target))
			max_hit *= 1.4;//1.25;

		if (target instanceof NPC)
			max_hit *= player.getAuraManager().getDamageMultiplier();
		 else if (Combat.hasCustomWeaponOnWild(player))
			max_hit /= 2;
		return max_hit;
	}

	private boolean checkAmmo(Player player) {
		if (player.getEquipment().getWeaponId() == 42926) {
			if(player.getBlowpipeDarts() == null) {
				player.getPackets().sendGameMessage("The blowpipe has no darts in it.");
				return false;
			}
			return true;
		}
		if (player.getEquipment().getWeaponId() == 25502) {
			if(player.getInfernalBlowpipeDarts() == null) {
				player.getPackets().sendGameMessage("The infernal blowpipe has no darts in it.");
				return false;
			}
			return true;
		}
		ItemConfig defs = player.getEquipment().getItem(Equipment.SLOT_WEAPON).getDefinitions();
		WeaponType type = WeaponTypesLoader.getWeaponDefinition(defs.getId());
		AmmunitionDefinition ammoDefs = AmmunitionDefinitionsLoader.getAmmoDefinition(defs.getId());
		if (ammoDefs.getProjectile() != 0 && player.getEquipment().getWeaponId() != 25575
				 && player.getEquipment().getWeaponId() != 25592 && player.getEquipment().getWeaponId() != 25609)//Doesn't need any ammo
			return true;
		Item ammo = player.getEquipment().getItem(Equipment.SLOT_ARROWS);
		if (ammo == null) {
			player.getPackets().sendGameMessage("You have no ammo equipped.");
			return false;
		}
		boolean result;
		switch (type.getStyle()) {
		case Combat.ARROW_STYLE:
			result = ammo.getName().toLowerCase().contains("arrow");
			break;
		case Combat.BOLT_STYLE:
			//handcannon exeption
			result = hasBalista(player) ? ammo.getName().contains("javelin") : defs.getId() == 10156 ? (ammo.getId() == 10158 || ammo.getId() == 10159) : defs.getName().toLowerCase().contains("karil's crossbow") ? ammo.getId() == 4740 : 
				
				
				defs.id == 25584 || defs.id == 15241 ? ammo.getId() == 15243 : ammo.getName().contains("bolt");
			break;
		case Combat.THROWN_STYLE: //throwables not supposed to use ammo
		default:
			result = false;
			break;
		}
		if (result) {
			result = defs.getCSOpcode(750) >= ammo.getDefinitions().getCSOpcode(750);
			if (defs.id >= 14192 && defs.id <= 14201)//Stealing creation bows have no lvl right now.
				result = ammo.getId() >= 14202 && ammo.getId() <= 14202 + ((defs.id - 14192) / 2);
			if (defs.id == 23043 || defs.id == 15836 || defs.id == 17295)// Hexhunter exception lvl 98 tier that can use 99 tier arrow
				result = true;
			else if ((defs.id == 24338 || defs.id == 24339))//Royal Crossbow exception
				result = ammo.getId() == 24336;
			else if (defs.id == 24303)//Coral Crossbow exception
				result = ammo.getId() == 24303;
			if (ammo.getId() == 4740)
				result = defs.getName().toLowerCase().contains("karil's crossbow");
			if (ammo.getId() == 8882)
				result = defs.id == 14686 || defs.id == 8880;
			if (hasBalista(player) || defs.getId() == 25441  || defs.getId() == 50997 || defs.getId() == 42424
					|| defs.getId() == 25460 || defs.getId() == 25469 || defs.getId() == 25533 || defs.getId() == 25575
					|| defs.getId() == 25592 || defs.getId() == 25609 || defs.getId() == 25662) //balista and twisted bow exception
				result = true;
		}
		if (!result)
			player.getPackets().sendGameMessage("You can't use that ammunition with your weapon.");
		return result;
	}

	private int rangeAttack(final Player player) {
		final int weaponId = player.getEquipment().getWeaponId();
		final int attackStyle = player.getCombatDefinitions().getAttackStyle();
		int combatDelay = getRangeCombatDelay(weaponId, attackStyle);
		if(player.isQuickShot()) {
			combatDelay /= 2;
		}
		int soundId = getSoundId(weaponId, attackStyle);
		if (player.getCombatDefinitions().isUsingSpecialAttack()) {
			if (!specialExecute(player))
				return combatDelay;
			switch (weaponId) {
			case 19149:// zamorak bow
			case 19151:
				player.setNextAnimation(new Animation(426));
				player.setNextGraphics(new Graphics(97, 0, 96));
				int time = World.sendProjectile(player, target, 100, 41, 39, 70, 42, 8, -1);
				time = delayHitMS(time, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true)));
				break;
			case 19146:
			case 19148:// guthix bow
				player.setNextAnimation(new Animation(426));
				player.setNextGraphics(new Graphics(95, 0, 96));
				time = World.sendProjectile(player, target, 98, 41, 39, 70, 42, 8, -1);
				time = delayHitMS(time, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true)));
				break;
			case 19143:// saradomin bow
			case 19145:
				player.setNextAnimation(new Animation(426));
				player.setNextGraphics(new Graphics(96, 0, 96));
				time = World.sendProjectile(player, target, 99, 41, 39, 70, 42, 8, -1);
				time = delayHitMS(time, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true)));
				break;
			case 859: // magic longbow
			case 861: // magic shortbow
			case 10284: // Magic composite bow
			case 18332: // Magic longbow (sighted)
			case 42788:
				player.setNextAnimation(new Animation(1074));
				int time1 = World.sendProjectile(player, target, 249, 45, 39, 70, 22, 10, 0); //first arrow
				int time2 = World.sendProjectile(player, target, 249, 45, 39, 70, 52, 10, 0);
				delayHitMS(time1, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true)));
				delayHitMS(time2, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true)));
				player.setNextGraphics(new Graphics(256, time1, 100));
				break;
			case 15241: // Hand cannon
				WorldTasksManager.schedule(new WorldTask() {

					int ticks = 0;

					@Override
					public void run() {
						ticks++;
						if ((target.isDead() || player.isDead() || player.hasWalkSteps() || player.getEquipment().getWeaponId() != 15241) && ticks != 4) {
							player.getCombatDefinitions().restoreSpecialAttack(50);
							player.setNextAnimation(new Animation(-1));
							stop();
							return;
						} else if (ticks == 1)
							player.setNextAnimation(new Animation(12175));
						else if (ticks == 4) {//3 seconds, then shoot
							player.setNextAnimationNoPriority(new Animation(12174));
							player.setNextGraphics(new Graphics(2138));
							int time = World.sendProjectile(player, target, 2143, 18, 18, 70, 50, 0, 0);
							time = delayHitMS(time, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true)));
							stop();
							return;
						}
					}
				}, 0, 0);
				combatDelay = 6;
				break;
			case 25609:
			case 25592:
			case 25575:
				int ammoId = player.getEquipment().getAmmoId();
				player.setNextAnimation(new Animation(getWeaponAttackEmote(weaponId, attackStyle)));
				player.setNextGraphics(new Graphics(getPullBackGFX(weaponId, ammoId, -1), 0, 100));
				int dbowbutnotdbowdmg;
				if (ammoId == 11212) {
					dbowbutnotdbowdmg = getRandomMaxHit(player, weaponId, attackStyle, true, true, 1, true);
					/*if (damage < 80)
						damage = 80;*/
					int damage2 = getRandomMaxHit(player, weaponId, attackStyle, true, true, 1, true);
				/*	if (damage2 < 80)
						damage2 = 80;*/

					time1 = World.sendProjectile(player, target, 1099, 41, 30, 50, 42, 8, 0);
					time2 = World.sendProjectile(player, target, 1099, 41, 50, 50, 50, 21, 0);
					delayHitMS(time1, weaponId, attackStyle, getRangeHit(player, dbowbutnotdbowdmg));
					delayHitMS(time1 + 600, weaponId, attackStyle, getRangeHit(player, damage2));
					
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							target.setNextGraphics(new Graphics(1100, 0, 100));
						}
					}, 2);
				} else {
					dbowbutnotdbowdmg = getRandomMaxHit(player, weaponId, attackStyle, true, true, 1, true);
				/*	if (damage < 50)
						damage = 50;*/
					int damage2 = getRandomMaxHit(player, weaponId, attackStyle, true, true, 1, true);
				/*	if (damage2 < 50)
						damage2 = 50;*/
					time1 = World.sendProjectile(player, target, 1101, 41, 30, 70, 42, 8, 0);
					time2 = World.sendProjectile(player, target, 1101, 41, 50, 70, 50, 21, 0);
					delayHitMS(time1, weaponId, attackStyle, getRangeHit(player, dbowbutnotdbowdmg));
					time2 = delayHitMS(time2, weaponId, attackStyle, getRangeHit(player, damage2));
				}

				if(weaponId == 25592 || weaponId == 25609) { // acid
					target.addFreezeDelay(4000);
					if(target.getSize() < 2)
						target.gfx(weaponId == 25592 ? 5369 : 1393 );

					player.heal(dbowbutnotdbowdmg / 2);
					player.getPrayer().restorePrayer((dbowbutnotdbowdmg / 4) * 10);
				}

				if (target instanceof Player) {
					Player targetPlayer = (Player) target;
					if(weaponId == 25575)
						targetPlayer.getSkills().drainLevel(Skills.DEFENCE, (int) (targetPlayer.getSkills().getLevel(Skills.DEFENCE) * 0.3) + 1);
				} else if (target instanceof NPC) {
					NPC targetNPC = (NPC) target;
					targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] * 0.7);
					targetNPC.getBonuses()[CombatDefinitions.CRUSH_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.CRUSH_DEF] * 0.7);
					targetNPC.getBonuses()[CombatDefinitions.SLASH_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.SLASH_DEF] * 0.7);
					targetNPC.getBonuses()[CombatDefinitions.RANGE_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.RANGE_DEF] * 0.7);
					targetNPC.getBonuses()[CombatDefinitions.MAGIC_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.MAGIC_DEF] * 0.7);
				}
				player.getPackets().sendGameMessage("Your special attack reduced enemy defence by 30%.");
				break;
			case 11235: // dark bows
			case 25539:
			case 25617:
			case 25380: //lucky dow
			case 15701:
			case 15702:
			case 15703:
			case 15704:
				ammoId = player.getEquipment().getAmmoId();
				player.setNextAnimation(new Animation(getWeaponAttackEmote(weaponId, attackStyle)));
				player.setNextGraphics(new Graphics(getPullBackGFX(weaponId, ammoId, -1), 0, 100));
				if (ammoId == 11212) {
					int damage = getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.5, true);
					if (damage < 80)
						damage = 80;
					int damage2 = getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.5, true);
					if (damage2 < 80)
						damage2 = 80;

					time1 = World.sendProjectile(player, target, 1099, 41, 30, 50, 42, 8, 0);
					time2 = World.sendProjectile(player, target, 1099, 41, 50, 50, 50, 21, 0);
					delayHitMS(time1, weaponId, attackStyle, getRangeHit(player, damage));
					delayHitMS(time1 + 600, weaponId, attackStyle, getRangeHit(player, damage2));
					
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							target.setNextGraphics(new Graphics(1100, 0, 100));
						}
					}, 2);
				} else {
					int damage = getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.3, true);
					if (damage < 50)
						damage = 50;
					int damage2 = getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.3, true);
					if (damage2 < 50)
						damage2 = 50;
					time1 = World.sendProjectile(player, target, 1101, 41, 30, 70, 42, 8, 0);
					time2 = World.sendProjectile(player, target, 1101, 41, 50, 70, 50, 21, 0);
					delayHitMS(time1, weaponId, attackStyle, getRangeHit(player, damage));
					time2 = delayHitMS(time2, weaponId, attackStyle, getRangeHit(player, damage2));
				}
				break;
			case 14684: // zanik cbow
				player.setNextAnimation(new Animation(getWeaponAttackEmote(weaponId, attackStyle)));
				player.setNextGraphics(new Graphics(1714));
				time = World.sendProjectile(player, target, 2001, 41, 41, 50, 35, 0, 0);
				time = delayHitMS(time, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true) + 30 + Utils.random(120)));
				break;
			case 8880: // Dorgeshuun cbow
				player.setNextAnimation(new Animation(getWeaponAttackEmote(weaponId, attackStyle)));
				time = World.sendProjectile(player, target, 698, 41, 41, 50, 35, 0, 0);
				int damage = getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true);
				time = delayHitMS(time, weaponId, attackStyle, getRangeHit(player, damage));
				if (damage > 10 && target instanceof Player) 
						((Player)target).getSkills().drainLevel(Skills.DEFENCE, damage/10);
				
				break;
			case 13954:// morrigan javelin
			case 12955:
			case 13956:
			case 13879:
			case 13880:
			case 13881:
			case 13882:
				if (target instanceof NPC) {
					player.getPackets().sendGameMessage("You may only use Phantom Strike during PvP situations.");
					return combatDelay;
				}
				player.setNextGraphics(new Graphics(1836));
				player.setNextAnimation(new Animation(10501));
				time = World.sendProjectile(player, target, 1837, 41, 41, 70, 35, 0, 0);
				final int hit = getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true);
				if (hit > 0) {
					final Entity finalTarget = target;
					WorldTasksManager.schedule(new WorldTask() {
						int damage = hit;

						@Override
						public void run() {
							if (finalTarget.isDead() || finalTarget.hasFinished()) {
								stop();
								return;
							}
							if (damage > 50) {
								damage -= 50;
								finalTarget.applyHit(new Hit(player, 50, HitLook.REGULAR_DAMAGE));
							} else {
								finalTarget.applyHit(new Hit(player, damage, HitLook.REGULAR_DAMAGE));
								stop();
							}
						}
					}, 4, 2);
				}
				delayHitMS(time, weaponId, attackStyle, getRangeHit(player, hit));
				break;
			case 50849:
				player.setNextAnimation(new Animation(10501));
				time = World.sendProjectile(player, target, 6318, 41, 41, 50, 35, 16, 0);
				delayHitMS(time, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.2, true)));
				break;
			case 52804: //d knife
			case 52806:
			case 52808:
			case 52810:
				player.setNextAnimation(new Animation(2068));
				time = World.sendProjectile(player, target, weaponId == 52804 ? 5699 : 6629, 41, 41, 50, 20, 16, 32);
				delayHitMS(time, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true)), getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true)));
				break;
			case 13883:
			case 13957:// morigan thrown axe
				if (target instanceof NPC) {
					player.getPackets().sendGameMessage("You may only use Hamstring during PvP situations.");
					return combatDelay;
				}
				player.setNextGraphics(new Graphics(1838));
				player.setNextAnimation(new Animation(10504));
				time = World.sendProjectile(player, target, 1839, 41, 41, 70, 35, 0, 0);
				delayHitMS(time, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.2, true)));
				break;
			case 42926:
			case 25502:
				player.setNextAnimation(new Animation(25061));
				time = World.sendProjectile(player, target, 6043, 41, 36, 60, 30, 0, 64);
				damage =  getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.5, true);
				player.heal(damage/2);
				delayHitMS(time, weaponId, attackStyle, getRangeHit(player, damage));
				break;
			case 49478:
			case 49481:
				ammoId = player.getEquipment().getAmmoId();
				AmmunitionDefinition weaponDef = AmmunitionDefinitionsLoader.getAmmoDefinition(weaponId), ammoDef = AmmunitionDefinitionsLoader.getAmmoDefinition(ammoId);
				int projectileGfx = weaponDef.getProjectile() == 0 ? ammoDef.getProjectile() : weaponDef.getProjectile();
				player.setNextAnimation(new Animation(12175));
				int delay = target.getSize() != 1 || Utils.getDistance(player, target) > 1 ? World.sendProjectile(player, target, projectileGfx, 25, 25, 80, 40, 0, 64) : 600;
				time = delayHitMS(delay, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.25, true)));
				dropAmmo(player, 1, time);
				break;
			case 25037:
				player.setNextAnimation(new Animation(4230));
				time = World.sendProjectile(player, target, 5301, 38, 36, 40, 40, 5, 0);
				time = delayHitMS(time, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1, true)));
				dropAmmo(player, 1, time);
				break;
			case 51902:
			case 25629:
				time = World.sendProjectile(player, target, 698, 38, 36, 40, 40, 5, 0);
				Item ammo = player.getEquipment().getItem(Equipment.SLOT_ARROWS);

				if(weaponId == 25629) {
					player.anim(11359);
					if(ammo.getAmount() >= 2) {
						delayHitMS(World.sendProjectile(player, target, 698, 38, 36, 40, 50, 5, 0), weaponId, attackStyle, getRangeHit(player, (int) ((double)getRandomMaxHit(player, weaponId, attackStyle, true) * 0.3 * (isDragon(target) ? 1.333 : 1.0))));
					}
					if(ammo.getAmount() >= 3) {
						delayHitMS(World.sendProjectile(player, target, 698, 38, 36, 40, 60, 5, 0), weaponId, attackStyle, getRangeHit(player, (int) ((double)getRandomMaxHit(player, weaponId, attackStyle, true) * 0.3 * (isDragon(target) ? 1.333 : 1.0))));
					}
					//delayHitMS(World.sendProjectile(player, target, 698, 38, 36, 40, 60, 5, 0), weaponId, attackStyle, getRangeHit(player, (int) ((double)getRandomMaxHit(player, weaponId, attackStyle, true) * 0.3)));
					dropAmmo(player, 3, time);
				} else {
					player.setNextAnimation(new Animation(4230));
					dropAmmo(player, 1, time);
				}

				int finalDelay = time;
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {

					private boolean nextTarget;

					@Override
					public boolean attack() {
						int damage = getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.2, true);
						delayHitMS(finalDelay, weaponId, attackStyle, getRangeHit(player, damage));
						target.setNextGraphics(new Graphics(2737, finalDelay / 10, 100));
						if (!nextTarget) {
							if (damage == -1)
								return false;
							nextTarget = true;
						}
						return nextTarget;
					}
				});
				break;
			default:
				player.getPackets().sendGameMessage("This weapon has no special attack, if you see an attack bar please message submit a bug report.");
				return combatDelay;
			}
		} else {
			int ammoId = player.getEquipment().getAmmoId();
			int style = WeaponTypesLoader.getWeaponDefinition(weaponId).getStyle();
			AmmunitionDefinition weaponDef = AmmunitionDefinitionsLoader.getAmmoDefinition(weaponId), ammoDef = AmmunitionDefinitionsLoader.getAmmoDefinition(ammoId);
			int projectileGfx = weaponDef.getProjectile() == 0 ? ammoDef.getProjectile() : weaponDef.getProjectile();
			if ((weaponId == 42926) && player.getBlowpipeDarts() != null) {
				AmmunitionDefinition dartDef = AmmunitionDefinitionsLoader.getAmmoDefinition(player.getBlowpipeDarts().getId());
				if (dartDef  != null)
					projectileGfx = dartDef.getProjectile();
			}
			if ((weaponId == 25502) && player.getInfernalBlowpipeDarts() != null) {
				AmmunitionDefinition dartDef = AmmunitionDefinitionsLoader.getAmmoDefinition(player.getInfernalBlowpipeDarts().getId());
				if (dartDef  != null)
					projectileGfx = dartDef.getProjectile();
			}
			ItemConfig defs = ItemConfig.forID(weaponId);
			int gfx = defs.id == 15241 || defs.id == 25584 ? 0 : weaponDef.getPullGFX() > 0 ? weaponDef.getPullGFX() : getPullBackGFX(weaponId, ammoId, ammoDef.getPullGFX());
			Graphics loadGfx = gfx <= 0 ? null : new Graphics(gfx, 0, 105);
			Animation attackAnim = new Animation(getWeaponAttackEmote(weaponId, attackStyle));
			int projectileDelay = attackAnim.getDefinitions().getEmoteTime() / 30;
			player.setNextAnimation(attackAnim);
			playSound(soundId, player, target);
			if (loadGfx != null && style != Combat.THROWN_STYLE && !hasBalista(player))
				player.setNextGraphics(loadGfx);
			String name = defs.getName().toLowerCase();
			switch (style) {
			case Combat.ARROW_STYLE:
				projectileDelay -= 15;
				if (Combat.hasDarkbow(player)) { //darkbow exeption
					delayHitMS(World.sendProjectile(player, target, projectileGfx, 35, 36, 40, projectileDelay, 15, 0), weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true)));
					int time = delayHitMS(World.sendProjectile(player, target, projectileGfx, 45, 46, 40, projectileDelay + 10, 20, 0), weaponId, attackStyle, getRangeHit(player,
							
							(int) (getRandomMaxHit(player, weaponId, attackStyle, true) * (Combat.hasCustomWeapon(player) ? 0.5d : 1d))));
					dropAmmo(player, 2, time);
				} else {
					int damage = getRandomMaxHit(player, weaponId, attackStyle, true);
					int time = delayHitMS(World.sendProjectile(player, target, projectileGfx, 40, 36, 40, projectileDelay, 15, 0), weaponId, attackStyle, getRangeHit(player, damage));
					//Exceptions
					if (name.contains("zaryte") || name.contains("crystal"))
						player.getCharges().addCharges(defs.id, -1, Equipment.SLOT_WEAPON);
					else if (Utils.random(10) == 5) {
						if (weaponId == 19152 || weaponId == 19162 || weaponId == 19157) {
							damage = getRandomMaxHit(player, weaponId, attackStyle, true, false, 1.15, true);
							if (damage > 0) {
								if (ammoId == 19152) {
									mage_hit_gfx = 2708;
									magic_sound = 212;
								} else if (ammoId == 19162) {
									mage_hit_gfx = 2737;
									magic_sound = 161;
								} else {
									mage_hit_gfx = 2723;
									magic_sound = 133;
								}
							} else {
								target.setNextGraphics(new Graphics(85, projectileDelay, 96));
								soundId = 227;
							}
							delayHitMS(projectileDelay, -1, -1, getMagicHit(player, Utils.random(50)));
						}
					}
					checkSwiftGlovesEffect(player, time, attackStyle, weaponId, damage, projectileGfx, 40, 36, 40, projectileDelay, 15, 0);
					if (weaponDef.getProjectile() == 0)
						dropAmmo(player, name.contains("bow") ? 1 : -2, time);
				}
				break;
			case Combat.BOLT_STYLE:
				if (defs.id == 15241 || defs.id == 25584) {
					if (defs.id == 15241 && Utils.random(player.getSkills().getLevel(Skills.FIREMAKING) << 1) == 0) {//handcannon exeption
						player.setNextAnimation(new Animation(12175));
						player.setNextGraphics(new Graphics(2140));
						player.getEquipment().getItems().set(3, null);
						player.getEquipment().refresh(3);
						player.getAppearence().generateAppearenceData();
						player.applyHit(new Hit(player, Utils.random(150) + 10, HitLook.REGULAR_DAMAGE));
						return 0;
					} else {
						
						if (defs.id == 25584) {
							WorldTasksManager.schedule(new WorldTask() {

								int ticks = 0;

								@Override
								public void run() {
									ticks++;
									if ((target.isDead() || player.isDead() || player.hasWalkSteps() || player.getEquipment().getWeaponId() != 25584) && ticks != 4) {
										player.setNextAnimation(new Animation(-1));
										stop();
										return;
									} else if (ticks == 1)
										player.setNextAnimation(new Animation(12175));
									else if (ticks == 4) {//3 seconds, then shoot
										player.setNextAnimationNoPriority(new Animation(12174));
										player.setNextGraphics(new Graphics(2138));
										int time = World.sendProjectile(player, target, 2143, 18, 18, 70, 50, 0, 0);
										time = delayHitMS(time, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true)));
										stop();
										return;
									}
								}
							}, 0, 0);
							combatDelay = 6;
						}
						
						
						player.setNextAnimation(new Animation(combatDelay <= 5 ? 12175 : 12174));
						player.setNextGraphics(new Graphics(2138));
						int time = delayHitMS(World.sendProjectile(player, target, 2143, 25, 25, 55, 30, 0, -1), weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true)));
						dropAmmo(player, -2, time);
						return combatDelay;
					}
				} else if (hasBalista(player)) {
					player.setNextAnimation(new Animation(12175));
					int delay = target.getSize() != 1 || Utils.getDistance(player, target) > 1 ? World.sendProjectile(player, target, projectileGfx, 25, 25, 55, 30, 0, 64) : 600;
					int time = delayHitMS(delay, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true)));
					dropAmmo(player, 1, time);
					return combatDelay;
				} else {
					int damage = 0;
					projectileDelay -= 20;
					if (Utils.random(10) == 5) {
						switch(ammoId) {
						case 9237:
						case 51934:
							target.setNextGraphics(new Graphics(755, projectileDelay, 0));
							if (target instanceof Player) {
								Player p2 = (Player) target;
								p2.stopAll();
							} else {
								NPC n = (NPC) target;
								n.setTarget(null);
							}
							soundId = 2914;
							break;
						case 9242:
						case 51944:
							if (player.getHitpoints() >= 20) {
								max_hit = Short.MAX_VALUE;
								damage = Math.min(2000, (int) (target.getHitpoints() * 0.2));
								target.setNextGraphics(new Graphics(754, projectileDelay, 0));
								player.applyHit(new Hit(target, (int) (player.getHitpoints() * 0.1), HitLook.REFLECTED_DAMAGE));
								soundId = 2912;
							}
							break;
						case 9243:
						case 51946:
							damage = getRandomMaxHit(player, weaponId, attackStyle, true, false, 1.15, true);
							target.setNextGraphics(new Graphics(751, projectileDelay, 0));
							soundId = 2913;
							break;
						case 9244:
						case 51948:
							damage = getRandomMaxHit(player, weaponId, attackStyle, true, false, 1, true)
							+ (Combat.hasAntiDragProtection(target) ? 0 : (player.getSkills().getLevel(Skills.RANGE) * 2));
							target.setNextGraphics(new Graphics(756, projectileDelay, 0));
							soundId = 2915;
							break;
						case 9245:
						case 51950:
							damage = getRandomMaxHit(player, weaponId, attackStyle, true, false, 1.2, true);
							target.setNextGraphics(new Graphics(753, projectileDelay, 0));
							player.heal((int) (damage * 0.25));
							soundId = 2917;
							break;
						}
					}
					if (damage == 0)
						damage = getRandomMaxHit(player, weaponId, attackStyle, true);
					if (Combat.hasRoyalCrossbow(player) && (target instanceof Player)) {
						player.getPackets().sendGameMessage("The royal crossbow feels weak and unresponsive.");
						max_hit = 60;
						damage = Math.min(damage, max_hit);
					}
					int time = delayHitMS(World.sendProjectile(player, target, projectileGfx, 38, 36, 40, projectileDelay, 5, 0), weaponId, attackStyle, getRangeHit(player, damage));
					if (weaponId == 25546 || weaponId == 25639 || weaponId == 25629)
						delayHitMS(World.sendProjectile(player, target, projectileGfx, 38, 36, 40, projectileDelay + 10, 5, 0), weaponId, attackStyle, getRangeHit(player, (int) ((double)getRandomMaxHit(player, weaponId, attackStyle, true) * 0.50)));
					checkSwiftGlovesEffect(player, time, attackStyle, weaponId, damage, projectileGfx, 38, 36, 40, projectileDelay, 5, 0);
					if (weaponDef.getProjectile() == 0)
						dropAmmo(player, (name.contains("c'bow") || name.contains("crossbow") || weaponId == 25546) && ammoId != 4740 ? 1 : -2, time);
				}
				break;
			case Combat.THROWN_STYLE:
				projectileDelay -= attackAnim.getIds()[0] == 10501 ? 10 : 20;
				int delay = 
						
						weaponId == 42926 || weaponId == 25502 ? 	World.sendProjectile(player, target, projectileGfx, 41, 36, 60, 30, 0, 64) :
						World.sendProjectile(player, target, projectileGfx, 41, 36, 40, projectileDelay, 15, 0);
				if (defs.id == 10033 || defs.id == 10034 || defs.id == 41959) { //Chinchompa exeption 
					player.getEquipment().removeAmmo(weaponId, -1);
					final int finalDelay = delay;
					attackTarget(getMultiAttackTargets(player), new MultiAttack() {

						private boolean nextTarget;

						@Override
						public boolean attack() {
							int damage = getRandomMaxHit(player, weaponId, attackStyle, true);
							delayHitMS(finalDelay, weaponId, attackStyle, getRangeHit(player, damage));
							target.setNextGraphics(new Graphics(2737, finalDelay / 10, 100));
							if (!nextTarget) {
								if (damage == -1)
									return false;
								nextTarget = true;
							}
							return nextTarget;
						}
					});
				}
				else {
					int damage = getRandomMaxHit(player, weaponId, attackStyle, true);
					if (weaponId == 21364 && damage > 20) {
						int distance = Utils.getDistance(player, target);
						if (distance > 4)
							distance = 4;
						damage += 20 * distance;
					} else if (weaponId == 21365) {
						long currentTime = Utils.currentTimeMillis();
						if (target.getFrozenBlockedDelay() >= currentTime) {
							player.getPackets().sendGameMessage("Your victim is already being restrained.");
							return combatDelay;
						}
						int bolasFreezeTimer = 15000;
						if (target instanceof Player) {
							Player p = (Player) target;
							Item weapon = p.getEquipment().getItem(3);
							boolean slashBased = weapon != null;
							if (weapon != null) {
								double slash = p.getCombatDefinitions().getBonuses()[CombatDefinitions.SLASH_ATTACK];
								for (int i = 0; i < 5; i++) {
									if (p.getCombatDefinitions().getBonuses()[i] > slash) {
										slashBased = false;
										break;
									}
								}
							}
							if (p.getInventory().containsItemToolBelt(946) || slashBased)
								bolasFreezeTimer /= 2;
							if (p.getPrayer().isRangeProtecting())
								bolasFreezeTimer /= 2;
							if (bolasFreezeTimer < 5000) {
								bolasFreezeTimer = 5000;
							}
						} else if (target instanceof NPC) {
							NPC npc = (NPC) target;
							if (npc.getSize() >= 2)
								bolasFreezeTimer = 0;
							else
								bolasFreezeTimer = 8000;
						}
						target.addFrozenBlockedDelay(bolasFreezeTimer + (4 * 1000));
						target.addFreezeDelay(bolasFreezeTimer, true);
						target.setNextGraphics(new Graphics(469, delay, 96));
					}
					checkSwiftGlovesEffect(player, delay, attackStyle, weaponId, damage, projectileGfx, 41, 36, 40, projectileDelay, attackAnim.getIds()[0] == 10501 ? 0 : 15, 0);
					delay = delayHitMS(delay, weaponId, attackStyle, getRangeHit(player, damage));
				}
				if (weaponId == 42926 || weaponId == 25502 || weaponDef.getProjectile() != 0 && defs.id != 10033 && defs.id != 10034)
					dropAmmo(player, -1, delay);
				break;
			}
		}
		return combatDelay;
	}


	/**
	 * Handles the swift gloves effect.
	 * 
	 * @param player
	 *            The player.
	 * @param hitDelay
	 *            The delay before hitting the target.
	 * @param attackStyle
	 *            The attack style used.
	 * @param weaponId
	 *            The weapon id.
	 * @param hit
	 *            The hit done.
	 * @param gfxId
	 *            The gfx id.
	 * @param startHeight
	 *            The start height of the original projectile.
	 * @param endHeight
	 *            The end height of the original projectile.
	 * @param speed
	 *            The speed of the original projectile.
	 * @param delay
	 *            The delay of the original projectile.
	 * @param curve
	 *            The curve of the original projectile.
	 * @param startDistanceOffset
	 *            The start distance offset of the original projectile.
	 */
	private void checkSwiftGlovesEffect(Player player, int hitDelay, int attackStyle, int weaponId, int hit, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		Item gloves = player.getEquipment().getItem(Equipment.SLOT_HANDS);
		if (gloves == null || !(gloves.getDefinitions().getName().contains("Swift glove") || gloves.getId() == 25672)) {
			return;
		}
		if ((hit != 0 && hit < ((max_hit / 3) * 2)) || new Random().nextInt(hit == 0 ? 30 : 10) != 0) {
			return;
		}
		player.getPackets().sendGameMessage("You fired an extra shot.");
		World.sendProjectile(player, target, gfxId, startHeight - 5, endHeight - 5, speed, delay, curve - 5 < 0 ? 0 : curve - 5, startDistanceOffset);
		delayHit(hitDelay, weaponId, attackStyle, getRangeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true)));
		if (hit > (max_hit - 10)) {
			target.addFreezeDelay(10000, false);
			target.setNextGraphics(new Graphics(181, 0, 96));
		}

	}

	public void dropAmmo(Player player, int quantity, int cycles) {
		dropAmmo(player, quantity, cycles, false);
	}

	public void dropAmmo(final Player player, final int quantity, int cycles, boolean skipAvaCheck) {
		if (player.isSafePk())
			return;
		if (quantity == -2) {
			final int ammoId = player.getEquipment().getAmmoId();
			player.getEquipment().removeAmmo(ammoId, 1);
		} else {
			int slotId = quantity == -1 ? player.getEquipment().getWeaponId() : player.getEquipment().getAmmoId();
			if (slotId == 42926) {
				if (player.getBlowpipeDarts() != null)
					slotId = player.getBlowpipeDarts().getId();
				else
					return;
			}
			if (slotId == 25502) {
				if (player.getInfernalBlowpipeDarts() != null)
					slotId = player.getInfernalBlowpipeDarts().getId();
				else
					return;
			}
			if (slotId != -1) {
				int capeId = player.getEquipment().getCapeId();
				if (!skipAvaCheck && (capeId == 10498 || capeId == 10499 || capeId == 20767 || capeId == 20068 || capeId == 20769 || capeId == 20771
						|| capeId == 25528 || capeId == 25531 || capeId == 25358 || capeId == 9756 || capeId == 9757 || capeId == 52109
						|| capeId == 43329 || capeId == 51285)) {
					if(Utils.random(10) >= (capeId == 52109 ? 8 : 7))
						player.getEquipment().removeAmmo(slotId, quantity);
					return;
				} 
				player.getEquipment().removeAmmo(slotId, quantity);
				if (Utils.random(10) >= 7) //30% chance arrow dissapears instead of dropping without ava
					return;
				final int id = slotId;
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						World.updateGroundItem(new Item(id, quantity), new WorldTile(target.getCoordFaceX(target.getSize()), target.getCoordFaceY(target.getSize()), target.getPlane()), player);
					}
				}, cycles);
			}
		}
	}

	public int getPullBackGFX(int weaponId, int arrowId, int pullGFX) {
		Ammo ammo = getAmmo(weaponId);
		if (ammo == null)
			ammo = getAmmo(arrowId);
		if (ammo == null)
			return pullGFX;
		if (weaponId == 25662 || weaponId == 25592 || weaponId == 25609 || weaponId == 25575 || weaponId == 25617 || weaponId == 25533 || weaponId == 25539 || weaponId == 11235 || weaponId == 25380 || weaponId == 15701 || weaponId == 15702 || weaponId == 15703 || weaponId == 15704) {
			if (arrowId == 19152)
				return 125;
			else if (arrowId == 19157)
				return 124;
			else if (arrowId == 19162)
				return 126;
			return ammo.getStartDoubleGFX();
		}
		return ammo.getStartGFX();
		/*
		if (weaponId == 11235 || weaponId == 15701 || weaponId == 15702 || weaponId == 15703 || weaponId == 15704) {
			if (arrowId == 882)
				return 1104;
			else if (arrowId == 884)
				return 1105;
			else if (arrowId == 886)
				return 1106;
			else if (arrowId == 888)
				return 1107;
			else if (arrowId == 890)
				return 1108;
			else if (arrowId == 892)
				return 1109;
			else if (arrowId == 78)
				return 1110;
			else if (arrowId == 11212)
				return 1111;
			else if (arrowId == 19152)
				return 125;
			else if (arrowId == 19157)
				return 124;
			else if (arrowId == 19162)
				return 126;
		}
		return pullGFX;*/
	}

	private int meleeAttack(final Player player) {
		int weaponId = player.getEquipment().getWeaponId();
		final int attackStyle = player.getCombatDefinitions().getAttackStyle();
		int combatDelay = getMeleeCombatDelay(player, weaponId);
		if(player.isFluidStrikes()) {
			combatDelay /= 2;
		}
		int soundId = getSoundId(weaponId, attackStyle);
		if (weaponId == -1) {
			Item gloves = player.getEquipment().getItem(Equipment.SLOT_HANDS);
			if (gloves != null && (gloves.getDefinitions().getName().contains("Goliath gloves")
					|| gloves.getId() == 25672)) {
				weaponId = -2;
			}
		}
		if (player.getCombatDefinitions().isUsingSpecialAttack()) {
			if (!specialExecute(player))
				return combatDelay;
			switch (weaponId) {
			case 4153: //gmaul
			case 14679:
				player.setNextAnimation(new Animation(10505));
				player.setNextGraphics(new Graphics(340, 0, 96 << 16));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.1, true)));
				break;
			case 15442:// whip start
			case 15443:
			case 15444:
			case 15441:
			case 4151:
			case 23691:
				player.setNextAnimation(new Animation(11971));
				target.setNextGraphics(new Graphics(2108, 0, 100));
				if (target instanceof Player) {
					Player p2 = (Player) target;
					p2.setRunEnergy(p2.getRunEnergy() > 25 ? p2.getRunEnergy() - 25 : 0);
				}
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.2, true)));
				break;
			case 21371:
			case 21372:
			case 21373:
			case 21374:
				player.setNextAnimation(new Animation(11971));
				player.setNextGraphics(new Graphics(476));
				if (target.getSize() < 3)
					target.addFreezeDelay(5000, true);
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.2, true)));
				break;
			case 42006:
				player.setNextAnimation(new Animation(11971));
				target.setNextGraphics(new Graphics(2108, 0, 100));
				if (target.getSize() < 3)
					target.addFreezeDelay(5000, true);
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.2, true)));
				break;
			case 11730: // sara sword
			case 23690:
			case 25504:
			case 25529:
			case 25563:
			case 25588:
			case 25589:
				player.setNextAnimation(new Animation(11993));
				target.setNextGraphics(new Graphics(1194));
				delayNormalHit(weaponId, attackStyle,getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.1, true)),  getMagicHit(player, 1 + Utils.random(160)));
				soundId = 3853;
				break;
			case 42808: // sara sword
			case 42809:
				player.setNextAnimation(new Animation(11993));
				target.setNextGraphics(new Graphics(1194));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.25, true)));
				soundId = 3853;
				break;
			case 1249:// d spear
			case 1263:
			case 3176:
			case 5716:
			case 5730:
			case 13770:
			case 13772:
			case 13774:
			case 13776:
			case 11716:
			case 23683:
			case 41889:
				player.setNextAnimation(new Animation(12017));
				player.stopAll();
				target.setNextGraphics(new Graphics(80, 5, 60));
				if (target instanceof Player/*target.getSize() < 3 && ((target instanceof NPC) || ((NPC) target).isCantFollowUnderCombat())*/) {
					if (!target.addWalkSteps(target.getX() - player.getX() + target.getX(), target.getY() - player.getY() + target.getY(), 1))
						player.setNextFaceEntity(target);
				}
				target.setNextFaceEntity(player);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						target.setNextFaceEntity(null);
						player.setNextFaceEntity(null);
					}
				});
				if (target.getSize() < 3)
					target.addFreezeDelay(3000, true);
				if (target instanceof Player) {
					final Player other = (Player) target;
					other.lock();
					other.addFoodDelay(3000);
					other.setDisableEquip(true);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							other.setDisableEquip(false);
							other.unlock();
						}
					}, 5);
				}
				break;
			case 11698: // sgs
			case 23681:
				player.setNextAnimation(new Animation(12019));
				player.setNextGraphics(new Graphics(2109));
				int sgsdamage = getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.1, true);
				player.heal(sgsdamage / 2);
				player.getPrayer().restorePrayer((sgsdamage / 4) * 10);
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, sgsdamage));
				break;
			case 11696: // bgs
			case 23680:
				player.setNextAnimation(new Animation(11991));
				player.setNextGraphics(new Graphics(2114));
				int damage = getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.21, true);
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, damage));
				if (target instanceof Player) {
					Player targetPlayer = ((Player) target);
					int amountLeft;
					if ((amountLeft = targetPlayer.getSkills().drainLevel(Skills.DEFENCE, damage / 10)) > 0) {
						if ((amountLeft = targetPlayer.getSkills().drainLevel(Skills.STRENGTH, amountLeft)) > 0) {
							if ((amountLeft = targetPlayer.getSkills().drainLevel(Skills.PRAYER, amountLeft)) > 0) {
								if ((amountLeft = targetPlayer.getSkills().drainLevel(Skills.ATTACK, amountLeft)) > 0) {
									if ((amountLeft = targetPlayer.getSkills().drainLevel(Skills.MAGIC, amountLeft)) > 0) {
										if (targetPlayer.getSkills().drainLevel(Skills.RANGE, amountLeft) > 0) {
											break;
										}
									}
								}
							}
						}
					}
				} else if (target instanceof NPC) {
					NPC targetNPC = (NPC) target;
					if(damage == 0 && target instanceof Tekton) {
						player.sendMessage("bgs tekton");
						damage = 100;
					}
					targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] -= damage / 10;
					targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] -= damage / 10;
					targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] -= damage / 10;
					targetNPC.getBonuses()[CombatDefinitions.RANGE_DEF] -= damage / 10;
					targetNPC.getBonuses()[CombatDefinitions.MAGIC_DEF] -= damage / 10;
				}
				break;
			case 11694: // ags
			case 23679:
				player.setNextAnimation(new Animation(11989));
				player.setNextGraphics(new Graphics(2113));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.375, true)));
				break;
			case 25526:
			case 25592:
			case 25609:
				player.setNextAnimation(new Animation(11989));
				player.setNextGraphics(new Graphics(2113));
				player.setNextGraphics(new Graphics(2114));
				player.setNextGraphics(new Graphics(2109));
				player.setNextGraphics(new Graphics(1221));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, sgsdamage = getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.375, true)));
				player.heal(sgsdamage / 2);
				player.getPrayer().restorePrayer((sgsdamage / 4) * 10);
				if (sgsdamage != 0 && target.getSize() <= 1 && (target instanceof Player || !((NPC)target).isIntelligentRouteFinder())) { // freezes
					target.setNextGraphics(new Graphics(2104));
					target.addFreezeDelay(18000); // 18seconds
				}
				break;
			case 11061:// ancient mace
				player.setNextAnimation(new Animation(6147));
				player.setNextGraphics(new Graphics(1052, 3, 10));
				damage = getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.14, true);
				if (target instanceof Player) {
					Player p2 = (Player) target;
					int currentPrayer = player.getPrayer().getPrayerpoints(), newPrayer = currentPrayer + damage;
					if (newPrayer > 1375)//The max CAP.
						newPrayer = 1375;
					p2.getPrayer().drainPrayer(damage);
					player.getPrayer().setPrayerpoints(newPrayer);
				}
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, damage));
				break;
			case 13899: // vls
			case 13901:
				player.setNextAnimation(new Animation(10502));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.25, true)));
				break;
			case 13902: // statius hammer
			case 25630:
			case 13904:
			case 25547:
			case 25640:
				player.setNextAnimation(new Animation(10505));
				player.setNextGraphics(new Graphics(1840));
				damage = getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.25, true);
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, damage));
				if (damage > 0) {
					if (target instanceof Player) {
						Player targetPlayer = (Player) target;
						targetPlayer.getSkills().drainLevel(Skills.DEFENCE, (int) (targetPlayer.getSkills().getLevel(Skills.DEFENCE) * 0.3) + 1);
					} else if (target instanceof NPC) {
						NPC targetNPC = (NPC) target;
						targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] * 0.7);
						targetNPC.getBonuses()[CombatDefinitions.CRUSH_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.CRUSH_DEF] * 0.7);
						targetNPC.getBonuses()[CombatDefinitions.SLASH_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.SLASH_DEF] * 0.7);
						targetNPC.getBonuses()[CombatDefinitions.RANGE_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.RANGE_DEF] * 0.7);
						targetNPC.getBonuses()[CombatDefinitions.MAGIC_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.MAGIC_DEF] * 0.7);
					}
				}
				break;
			case 51742:
				player.setNextAnimation(new Animation(10505));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, 50 + getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.0, true)));
				break;
			case 43576: // dragon hammer
			case 25661:
			case 25591:
				player.setNextAnimation(new Animation(weaponId == 25661 ? 10502 : 10505));

				if(weaponId != 25661)
					player.setNextGraphics(new Graphics(6292));
				else
					target.gfx(1590);

				damage = getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.5, true);
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, damage));

				if (damage > 0) {
					if (target instanceof Player) {
						Player targetPlayer = (Player) target;
						targetPlayer.getSkills().drainLevel(Skills.DEFENCE, (int) (targetPlayer.getSkills().getLevel(Skills.DEFENCE) * 0.3) + 1);
					} else if (target instanceof NPC) {
						NPC targetNPC = (NPC) target;
						targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] * 0.7);
						targetNPC.getBonuses()[CombatDefinitions.CRUSH_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.CRUSH_DEF] * 0.7);
						targetNPC.getBonuses()[CombatDefinitions.SLASH_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.SLASH_DEF] * 0.7);
						targetNPC.getBonuses()[CombatDefinitions.RANGE_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.RANGE_DEF] * 0.7);
						targetNPC.getBonuses()[CombatDefinitions.MAGIC_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.MAGIC_DEF] * 0.7);
					}
				} else if(target instanceof Tekton) {
					// dwh spec drains 5% on tekton on 0 damage
					NPC targetNPC = (NPC) target;
					targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] * 0.95);
					targetNPC.getBonuses()[CombatDefinitions.CRUSH_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.CRUSH_DEF] * 0.95);
					targetNPC.getBonuses()[CombatDefinitions.SLASH_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.SLASH_DEF] * 0.95);
					targetNPC.getBonuses()[CombatDefinitions.RANGE_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.RANGE_DEF] * 0.95);
					targetNPC.getBonuses()[CombatDefinitions.MAGIC_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.MAGIC_DEF] * 0.95);
				}
				break;
			case 13905: // vesta spear
			case 13907:
				player.setNextAnimation(new Animation(10499));
				player.setNextGraphics(new Graphics(1835));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.1, true)));
				break;
			case 19780:
			case 19784: // korasi sword
				player.setNextAnimation(new Animation(14788));
				player.setNextGraphics(new Graphics(1729));
				final double multiplier = 0.5 + Math.random();
				int korasiDamage = getMaxHit(player, weaponId, attackStyle, false, true, 1);
				korasiDamage *= multiplier;
				max_hit = 0; //korasi does crit every time in rs :p
				final int finalKorasiDamage = korasiDamage;
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {

					private int damage = finalKorasiDamage;
					final int weaponId = player.getEquipment().getWeaponId();

					// player on array

					@Override
					public boolean attack() {
						delayHit(0, weaponId, attackStyle, getMagicHit(player, damage));
						damage /= 2;
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								target.setNextGraphics(new Graphics(2795, 0, 100));
							}
						});
						return damage != 0;

					}
				});
				return combatDelay;
			case 11700:
			case 23682:
				int zgsdamage = getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.1, true);
				player.setNextAnimation(new Animation(7070));
				player.setNextGraphics(new Graphics(1221));
				if (zgsdamage != 0 && target.getSize() <= 1 && (target instanceof Player || !((NPC)target).isIntelligentRouteFinder())) { // freezes
							// small
					// npcs
					target.setNextGraphics(new Graphics(2104));
					target.addFreezeDelay(18000); // 18seconds
				}
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, zgsdamage));
				break;
			case 14484: // d claws
			case 23695:
			case 25619:
			case 25633:
				player.setNextAnimation(new Animation(10961));
				player.setNextGraphics(new Graphics(1950));
				int[] hits = new int[]
						{ 0, 1 };
				int hit = getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.0, true);
				if(weaponId == 25633) {
					if(target.isNPC()) {
						if(isDragon(target)) {
							hit *= 1.33;
						}
					}
				}
				if (hit > 0) {
					hits = new int[]
							{ hit, hit / 2, (hit / 2) / 2, (hit / 2) - ((hit / 2) / 2) };
				} else {
					hit = getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.0, true);
					if (hit > 0) {
						hits = new int[]
								{ 0, hit, hit / 2, hit - (hit / 2) };
					} else {
						hit = getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.0, true);
						if (hit > 0) {
							hits = new int[]
									{ 0, 0, hit / 2, (hit / 2) + 10 };
						} else {
							hit = getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.0, true);
							if (hit > 0) {
								hits = new int[]
										{ 0, 0, 0, (int) (hit * 1.5) };
							} else {
								hits = new int[]
										{ 0, 0, 0, Utils.random(7) + 1 };
							}
						}
					}
				}
				for (int i = 0; i < hits.length; i++) {
					if (i > 1) {
						delayHit(1, weaponId, attackStyle, getMeleeHit(player, hits[i]));
					} else {
						delayNormalHit(weaponId, attackStyle, getMeleeHit(player, hits[i]));
					}
				}
				break;
			case 10887: // anchor
				player.setNextAnimation(new Animation(5870));
				player.setNextGraphics(new Graphics(1027));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, false, 1.0, true)));
				break;
			case 7158: //dragon 2h
			case 13430:
				player.setNextAnimation(new Animation(7078));
				player.setNextGraphics(new Graphics(1225));
				attackTarget(getMultiAttackTargets(player), new MultiAttack() {
					
					final int weaponId = player.getEquipment().getWeaponId();
					
					@Override
					public boolean attack() {
						delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.25, true)));
						return true;

					}
				});
				break;
			case 51015:
				//TODO gfx and anim
				attackTarget(getMultiAttackTargets(player, 5, 10), new MultiAttack() {
					
					final int weaponId = player.getEquipment().getWeaponId();
					
					@Override
					public boolean attack() {
						delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, true, true, 1.0, true)));
						return true;

					}
				});
				break;
			case 1305: // dragon long
				player.setNextAnimation(new Animation(12033));
				player.setNextGraphics(new Graphics(2117));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.25, true)));
				break;
			case 51009: //dragon sword
				player.setNextAnimation(new Animation(10502));
				player.setNextGraphics(new Graphics(6369, 0, 100));
				delayNormalHit(weaponId, attackStyle, getRegularHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.25, true)));
				break;
			case 52731: //dragon hasta
			case 52734:
			case 52737:
			case 52740:
			case 52743:
				double specAmt = player.getCombatDefinitions().getSpecialAttackPercentage()+5;
				double dmgMult = specAmt/200 + 1;
			//	double accMult = specAmt/100 + 1;  not used so i set to 100% boost every time
				player.setNextAnimation(new Animation(10502));
				player.setNextGraphics(new Graphics(6369, 0, 100));
				player.getCombatDefinitions().setSpecialAttack(0);
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, dmgMult, true)));
				break;
			case 3204: // d hally
			case 13478:
			case 25476:
			case 25618:
				player.setNextAnimation(new Animation(1203));//1665));
				player.setNextGraphics(new Graphics(282, 0, 100));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.1, true)));
				if (target.getSize() > 1 || ((weaponId == 25618 || weaponId == 25476) && !(target instanceof Player)))
					delayHit(1, weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.1, true)));
				break;
			case 43092: //crystal hally
				player.setNextAnimation(new Animation(1665));
				player.setNextGraphics(new Graphics(6232, 0, 100));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.1, true)));
				if (target.getSize() > 1)
					delayHit(1, weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.1, true)));
				break;
			case 4587: // dragon scimitar
				player.setNextAnimation(new Animation(12031));
				player.setNextGraphics(new Graphics(2118));
				Hit hit1 = getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.0, true));
				if (target instanceof Player) {
					Player p2 = (Player) target;
					if (hit1.getDamage() > 0)
						p2.setPrayerDelay(5000);// 5 seconds
				}
				delayNormalHit(weaponId, attackStyle, hit1);
				soundId = 2540;
				break;
			case 1215: // dragon dagger
			case 5698: // dds
			case 1231:
			case 5680:
				player.setNextAnimation(new Animation(1062));
				player.setNextGraphics(new Graphics(252, 0, 100));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.15, true)), getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.15, true)));
				soundId = 2537;
				break;
			case 43265: //abyssal dagger
			case 43267:
			case 43269:
			case 43271:
				player.setNextAnimation(new Animation(1062));
				player.setNextGraphics(new Graphics(6283));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 0.85, true)), getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 0.85, true)));
				soundId = 2537;
				break;
			case 1434: // dragon mace
				player.setNextAnimation(new Animation(1060));
				player.setNextGraphics(new Graphics(251));
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.45, true)));
				soundId = 2541;
				break;
			case 43263: //bludgeon
				player.setNextAnimation(new Animation(23299));
				target.setNextGraphics(new Graphics(6284));
				double percentage = (player.getSkills().getLevelForXp(Skills.PRAYER)-(player.getPrayer().getPrayerpoints()/10));
				percentage *= 0.005d;
				percentage += 1;
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, getRandomMaxHit(player, weaponId, attackStyle, false, true, percentage, true)));
				break; 
			case 6746: //darklight special
			case 49675:
				player.setNextAnimation(new Animation(2890));
				player.setNextGraphics(new Graphics(483));
				damage = getRandomMaxHit(player, weaponId, attackStyle, false, true, 1.25, true);
				delayNormalHit(weaponId, attackStyle, getMeleeHit(player, damage));
				if (damage > 0) {
					if (target instanceof Player) {
						Player targetPlayer = (Player) target;
						targetPlayer.getSkills().drainLevel(Skills.ATTACK, (int) (targetPlayer.getSkills().getLevel(Skills.ATTACK) * 0.05) + 1);
						targetPlayer.getSkills().drainLevel(Skills.STRENGTH, (int) (targetPlayer.getSkills().getLevel(Skills.STRENGTH) * 0.05) + 1);
						targetPlayer.getSkills().drainLevel(Skills.DEFENCE, (int) (targetPlayer.getSkills().getLevel(Skills.DEFENCE) * 0.05) + 1);
					} else if (target instanceof NPC) {
						NPC targetNPC = (NPC) target;
						targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.STAB_DEF] * 0.95);
						targetNPC.getBonuses()[CombatDefinitions.CRUSH_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.CRUSH_DEF] * 0.95);
						targetNPC.getBonuses()[CombatDefinitions.SLASH_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.SLASH_DEF] * 0.95);
						targetNPC.getBonuses()[CombatDefinitions.RANGE_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.RANGE_DEF] * 0.95);
						targetNPC.getBonuses()[CombatDefinitions.MAGIC_DEF] = (int) (targetNPC.getBonuses()[CombatDefinitions.MAGIC_DEF] * 0.95);
					}
				}
				break;
			default:
				player.getPackets().sendGameMessage("This weapon has no special Attack, if you still see special bar please relogin.");
				return combatDelay;
			}
		} else {
			if (weaponId == -2 && new Random().nextInt(25) == 0) {
				player.setNextAnimation(new Animation(14417));
				final int attack = attackStyle;
				attackTarget(getMultiAttackTargets(player, 5, 9), new MultiAttack() {

					private boolean nextTarget;

					@Override
					public boolean attack() {
						target.addFreezeDelay(10000, true);
						target.setNextGraphics(new Graphics(181, 0, 96));
						final Entity t = target;
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								final int damage = getRandomMaxHit(player, -2, attack, false, false, 0.2, false);
								t.applyHit(new Hit(player, damage, HitLook.REGULAR_DAMAGE));

								stop();
							}
						}, 1);
						if (target instanceof Player) {
							Player p = (Player) target;
							for (int i = 0; i < 7; i++) {
								if (i != 3 && i != 5) {
									p.getSkills().drainLevel(i, 7);
								}
							}
							p.getPackets().sendGameMessage("Your stats have been drained!");
						}
						if (!nextTarget) {
							nextTarget = true;
						}
						return nextTarget;

					}
				});
				return combatDelay;
			}
			int damage = getRandomMaxHit(player, weaponId, attackStyle, false);
			if (damage != 0 && (fullGuthanEquipped(player) && Utils.random(
					
					
					player.getPet() != null
					&& player.getPet().getId() == Pets.GUTHAN.getBabyNpcId() ? 2 : 3) == 0) 
					|| ((player.getEquipment().getWeaponId() == 25618 || player.getEquipment().getWeaponId() == 25476) && Utils.random(5) == 0)
					|| (player.getEquipment().getWeaponId() == 25591 && Utils.random(7) == 0)	) {
				target.setNextGraphics(new Graphics(398));
				player.heal(damage);
			}
			if (damage != 0 && fullAkrisaeEquipped(player) && Utils.random(3) == 0)
				player.getPrayer().restorePrayer(damage);
			delayNormalHit(weaponId, attackStyle, getMeleeHit(player, damage));
			if (weaponId == 52325 || weaponId == 25591 ||  weaponId == 25476 || weaponId == 25618 || weaponId == 25540 || weaponId == 25618) {
				player.setNextGraphics(new Graphics(282, 0, 100));
				if (target.getSize() >= 2 || ((weaponId == 25591 || weaponId == 25476 || weaponId == 25618 || weaponId == 25540) && !(target instanceof Player))) {
					delayNormalHit(weaponId, attackStyle, getMeleeHit(player, damage / 2));
					if (target.getSize() >= 3 || ((weaponId == 25591 || weaponId == 25476 || weaponId == 25618 || weaponId == 25540) && !(target instanceof Player)))
						delayNormalHit(weaponId, attackStyle, getMeleeHit(player, damage / 4));
				}
			}
			player.setNextAnimation(new Animation(getWeaponAttackEmote(weaponId, attackStyle)));
		}
		playSound(soundId, player, target);
		return combatDelay;
	}

	public void playSound(int soundId, Player player, Entity target) {
		if (soundId == -1)
			return;
		player.getPackets().sendSound(soundId, 0, 1);
		if (target instanceof Player) {
			Player p2 = (Player) target;
			p2.getPackets().sendSound(soundId, 0, 1);
		}
	}

	public void playVoice(int soundId, Player player, Entity target) {
		if (soundId == -1)
			return;
		player.getPackets().sendSound(soundId, 0, 2);
		if (target instanceof Player) {
			Player p2 = (Player) target;
			p2.getPackets().sendSound(soundId, 0, 2);
		}
	}

	public static int getSpecialAmmount(int weaponId) {
		switch (weaponId) {
		case 52731:
		case 52734:
		case 52737:
		case 52740:
		case 52743:
			return 5;
		case 8880: // Dorgeshuun cbow
		case 54425:
			return 75;
		case 3204: // d hally
		case 13478:
		case 43092: //crystal halberd
		case 25476: //scythe of vitur u
		case 25618: // hallowed scythe
			return 30;
		case 43265: //abyssal dagger
		case 43267:
		case 43269:
		case 43271:
		case 6746: //darklight
		case 49675:
		case 43263: //bludgeon
		case 42788: //magic shortbow (i)
		case 11700: // zgs
		case 23682:
			return 50;
		case 4587: // dragon sci
		case 859: // magic longbow
		case 861: // magic shortbow
		case 10284: // Magic composite bow
		case 18332: // Magic longbow (sighted)
		case 19149:// zamorak bow
		case 19151:
		case 19143:// saradomin bow
		case 19145:
		case 19146:
		case 19148:// guthix bow
		case 54424:
			
		case 11235: // dark bows
		case 25539:
		case 25617:
		case 25380: //lucky dbow
		case 15701:
		case 15702:
		case 15703:
		case 15704:
			
			return 55;
		case 49478: //balista
		case 49481:
			return 65;
		case 13899: // vls
		case 13901:
		case 1305: // dragon long
		case 1215: // dragon dagger
		case 5698: // dds
		case 1231: // ddp
		case 5680://ddp+
		case 1434: // dragon mace
		case 1249:// d spear
		case 1263:
		case 3176:
		case 5716:
		case 5730:
		case 13770:
		case 13772:
		case 13774:
		case 13776:
		case 11716:
		case 23683:
		case 41889:
		case 50849: //dragon thrownaxe
		case 52804: //d knife
		case 52806:
		case 52808:
		case 52810:
			return 25;
		case 42006:
		case 42926:
		case 25502:
		case 15442:// whip start
		case 15443:
		case 15444:
		case 15441:
		case 4151:
		case 23691:
		case 11698: // sgs
		case 23681:
		case 11694: // ags
		case 25526: //ggs
		case 25592:
		case 25609:
		case 23679:
		case 13905: // vesta spear
		case 13907:
		case 14484: // d claws
		case 25619:
		case 25633:
		case 23695:
		case 10887: // anchor
		case 4153: // granite maul
		case 14679:
		case 14684: // zanik cbow
		case 15241: // hand cannon
		case 13908:
		case 13954:// morrigan javelin
		case 13955:
		case 13956:
		case 13879:
		case 13880:
		case 13881:
		case 13882:
		case 13883:// morigan thrown axe
		case 13957:
		case 11696: // bgs
		case 23680:
		case 43576: //dragon warhammer
		case 25591:
		case 51015: //bulwark
		case 21371:
		case 21372:
		case 21373:
		case 21374:
		case 25629: // dragonshredder
			return 50;
		case 13902: // statius hammer
		case 25630:
		case 25547:
		case 13904:
		case 52516: //dawnbrighter
		case 25583:
		case 25575: //versace bow
			return 35;
		case 11730: // ss
		case 23690:
		case 25504:
		case 25529:
		case 25563:
		case 25588:
		case 25589:
		case 35:// Excalibur
		case 8280:
		case 14632:
		case 1377:// dragon battle axe
		case 13472:
		case 15486:// staff of lights
		case 25379: //lucky staff of light
		case 22207:
		case 22209:
		case 22211:
		case 22213:
		case 11061:
		case 41791:
		case 42902:
		case 25541:
		case 42904:
		case 6739: //dragon axe
		case 13470:
		case 43241:
		case 15259: //dragon pickaxe
		case 20786:
		case 43243:
		case 51028: //dragon harpoon
		case 51031: //infernal harpoon
		case 53673:
		case 53680:
		case 53762:
			return 100;
		case 19784: // korasi sword
		case 19780:
		case 51902: //dragon crossbow
		case 7158: //dragon 2h
		case 13430:
		case 51742: //granite hammer
			return 60;
		case 42808://blessed ss
		case 42809:
			return 65;
		case 25037: //armadyl crossbow
		case 51009: //dragon sword
		case 25640:
		case 25661:
			return 40; 
			
		default:
			return 0;
		}
	}

	public int getRandomMaxHit(Player player, int weaponId, int attackStyle, boolean ranging) {
		return getRandomMaxHit(player, weaponId, attackStyle, ranging, true, 1.0D, false);
	}

/*
 * attack type - 0 melee, 1 range, 2 mage, 
 */
	public int getDamage(Player player, int weaponId, int attackStyle, int attackType, int baseDamage, boolean defenceAffects, double specMultiplier, boolean usingSpec) {
		boolean max = (target instanceof NPC && ((NPC)target).getId() == 16008);
		if (attackType == 2)
			max_hit = getMagicMaxHit(player, baseDamage);
		else
			max_hit = getMaxHit(player, weaponId, attackStyle, attackType == 1, usingSpec, specMultiplier);
		boolean lower = false;
		if (defenceAffects && !max) {
			double effectiveAttack;
			double attackBonus;
			double attackBoost = 1;
			if (attackType == 2) {
				effectiveAttack = 8 + Math.floor((player.getSkills().getLevel(Skills.MAGIC) * player.getPrayer().getMageMultiplier() * player.getAuraManager().getMagicAccurayMultiplier()));
				attackBonus = player.getCombatDefinitions().getBonuses()[CombatDefinitions.MAGIC_ATTACK];
				if (fullVoidEquipped(player, 11663, 11674))
					effectiveAttack *= 1.45;
				if (player.getEquipment().getWeaponId() == 52555 && target instanceof NPC && Wilderness.isAtWild(target))
					effectiveAttack *= 2;
			} else if (attackType == 1) {
				effectiveAttack = 8 + Math.floor((player.getSkills().getLevel(Skills.RANGE) * player.getPrayer().getRangeMultiplier() * player.getAuraManager().getRangeAccurayMultiplier()));
				attackBonus = player.getCombatDefinitions().getBonuses()[CombatDefinitions.RANGE_ATTACK];
				if (fullVoidEquipped(player, (new int[]
						{ 11664, 11675 })))
					effectiveAttack *= 1.1;
				if (weaponId == 51012 && isDragon(target))
					effectiveAttack *= 1.3;
				else if (weaponId == 4214)
					effectiveAttack *= 1d + ((double)getCrystalSetCount(player) * 0.06d);
				if (player.getEquipment().getWeaponId() == 52550 && target instanceof NPC && Wilderness.isAtWild(target))
					effectiveAttack *= 1.7;
				else if (player.getEquipment().getWeaponId() == 25544 && target instanceof NPC)
					effectiveAttack *= 1.7;
			} else {
				effectiveAttack = 8 + Math.floor((player.getSkills().getLevel(Skills.ATTACK) * player.getPrayer().getAttackMultiplier()));
				int bonusStyle = CombatDefinitions.getMeleeBonusStyle(weaponId, attackStyle);
				attackBonus = player.getCombatDefinitions().getBonuses()[bonusStyle];
				if (weaponId == -2) // dont ask me why. was there lol
					attackBonus += 82;
				else if (fullVoidEquipped(player, (new int[]
						{ 11665, 11676 }))  || hasObsidian(player))
					effectiveAttack *= 1.1;
				if (weaponId == 49675 && isDemon(target))
					effectiveAttack *= 3; //1.7
				else if (weaponId == 52978 && isDragon(target))
					effectiveAttack *= 1.3;
				else if (player.getEquipment().getWeaponId() == 52545 && target instanceof NPC && Wilderness.isAtWild(target))
					effectiveAttack *= 1.7;
				
				if (bonusStyle == CombatDefinitions.CRUSH_ATTACK)
					effectiveAttack = Math.floor(effectiveAttack * getNightmareEffect(player));
			}
			if (usingSpec)
				attackBoost = this.getSpecialAccuracyModifier(weaponId);

			if(attackType == 2) {
				attackBoost *= Settings.SERVER_MAGIC_ACCURACY_BUFF;
			}

			double basedefence;
			if (target instanceof Player) {
				double effectiveDefense;
				double defenceBonus;
				double defBoost = 1;
				Player p2 = (Player) target;
				if (attackType == 2) {
					effectiveDefense = 8 + Math.floor((((p2.getSkills().getLevel(Skills.DEFENCE) * 0.3) + (p2.getSkills().getLevel(Skills.MAGIC) * 0.7)) * p2.getPrayer().getDefenceMultiplier()));
					defenceBonus = p2.getCombatDefinitions().getBonuses()[CombatDefinitions.MAGIC_DEF];
				} else {
					effectiveDefense = 8 + Math.floor((p2.getSkills().getLevel(Skills.DEFENCE) * p2.getPrayer().getDefenceMultiplier()));
					defenceBonus = p2.getCombatDefinitions().getBonuses()[attackType == 1 ? CombatDefinitions.RANGE_DEF : CombatDefinitions.getMeleeDefenceBonus(CombatDefinitions.getMeleeBonusStyle(weaponId, attackStyle))];
				}
				if (attackType == 0 && p2.getFamiliar() instanceof Steeltitan)
					defBoost = 1.15;
				if (attackStyle != 2) //nerfing def by  40% for melee & range on pvp. test purposes
					defBoost *= 0.6;
				basedefence = effectiveDefense * (1 + (defenceBonus / 64)) * defBoost;
			} else {
				NPC n = (NPC) target;
				basedefence = n.getBonuses()[attackType == 2 ? CombatDefinitions.MAGIC_DEF : attackType == 1 ? CombatDefinitions.RANGE_DEF : CombatDefinitions.getMeleeDefenceBonus(CombatDefinitions.getMeleeBonusStyle(weaponId, attackStyle))];
				basedefence *= 1.25; //too weak with new formula as players get alot more att than before
				if (basedefence < 0)
					basedefence = 1;
				if (attackType == 0 && n.getId() == 1160 && fullVeracsEquipped(player))
					basedefence *= 0.6;
				if (performHexbow(weaponId, target))
					attackBonus += n.getCombatLevel() / (weaponId == 25441 ? 9 : 10);
			}
			double baseattack = effectiveAttack * (1 + (attackBonus / 64)) * attackBoost;
			if (attackType == 2 && (player.getEquipment().getRingId() == 52975 || player.getEquipment().getRingId() == 25488 || player.getEquipment().getRingId() == 25741) && Utils.random(4) == 0)
				basedefence *= 0.9;
			double chance = (Math.pow(baseattack, 2) / Math.pow(baseattack + basedefence, 2)) * 2;
			if (chance > 0.99 && attackType == 2)
				chance = 0.99D;
			else if (chance > 0.9 && attackType != 2)
				chance = 0.9D;
			else if (chance < 0.01)
				chance = 0.01D;

			if (target instanceof DungeonNPC)
				chance *= getAccuracyPenalty(player);

			/*if (Utils.randomDouble() > chance / 1.5) {
				if (Utils.randomDouble() > chance)
					return attackType == 2 ? -1 : 0;
				lower = true;
			}*/
			if (Utils.randomDouble() > chance)
				return attackType == 2 ? -1 : 0;

			if (Settings.DEBUG)
				Logger.log(this, "new hitchance: " + chance + ", " + baseattack + ", " + basedefence + ", " + lower);
			//dmg += (lower ? Utils.random(maxhit/2) : Utils.random(maxhit))+1;

			/*	if(Utils.randomDouble() > chance)
					return attackType == 2 ? -1 : 0;*/
		}

		//2 but seemed too low so changed to / 1.5
		int damage = max ? max_hit : ((lower ? Utils.random((int) (max_hit / 1.5)) : Utils.random(max_hit)) + 1);//rollHit(max_hit);

		if(attackType == 2 && !target.isPlayer()) {
			damage *= Settings.SERVER_MAGIC_DAMAGE_BUFF;
		}

		if (usingSpec && !max) {
			int halfMaxhit = (int) (max_hit * 0.5);
			double m1 = (0.25 + specMultiplier) / 2;
			// if hit gonna be lower than half of max hit and percentage >
			// random, hit = at least half max hit + random
			if (halfMaxhit > damage && m1 > Math.random() * 2)
				damage = halfMaxhit + Utils.random(halfMaxhit + 1);
		}

		if (player.getAuraManager().usingEquilibrium()) {
			int perc25MaxHit = (int) (max_hit * 0.25);
			damage -= perc25MaxHit;
			max_hit -= perc25MaxHit;
			if (damage < 0)
				damage = 0;
			if (damage < perc25MaxHit)
				damage += perc25MaxHit;

		}
		if (target instanceof NPC) {
			NPC n = (NPC) target;
			if (n.getId() == 9463 && hasFireCape(player))
				damage += 40;
			if (attackType == 2 && spell_type == FIRE_SPELL) {
				if (n.getId() == 9463  //iceverms
						|| (n.getId() >= 14301 && n.getId() <= 14304))  //glacors
					damage *= 2;
			}
		}
		if (Settings.DEBUG)
			Logger.log(this, "new hit: " + damage + "/" + max_hit);
		return damage == 0 && attackType == 2 ? -1 : damage;

	}

	private static final double[] ACCURACY_MODIFIERS = {1.10, 0.95, 0.80, 0.70};

	private double getAccuracyPenalty(Player player) {
		DungeonNPC npc = (DungeonNPC) target;
		WeaponType[][] weak = npc.getWeaknessStyle();
		double penalty = target instanceof DungeonBoss ? 0.70 : 0.65;// Sixty-five percent of current accuracy
		if (weak != null) {
			o : for (int tier = 0; tier < weak.length; tier++) {
				WeaponType[] types = weak[tier];
				for (WeaponType type : types) {
					boolean validMelee = type.getType() == Combat.MELEE_TYPE && CombatDefinitions.getMeleeBonusStyle(player.getEquipment().getWeaponId(), player.getCombatDefinitions().getAttackStyle()) == type.getStyle();
					boolean validMagic = type.getType() == Combat.RANGE_TYPE && player.getCombatDefinitions().getSpellId() > 0 && (type.getStyle() == -1 || spell_type == type.getStyle());
					boolean validRange = player.getCombatDefinitions().getType() == Combat.RANGE_TYPE;
					if (validMelee || validMagic || validRange) {
						penalty = ACCURACY_MODIFIERS[tier];
						break o;
					}
				}
			}
		}
		return penalty;
	}

	@Deprecated
	public int getRandomMaxHit(Player player, int weaponId, int attackStyle, boolean ranging, boolean defenceAffects, double specMultiplier, boolean usingSpec) {
		return getDamage(player, weaponId, attackStyle, ranging ? 1 : 0, 0, defenceAffects, specMultiplier, usingSpec);
	}

	public final int getMaxHit(Player player, int weaponId, int attackStyle, boolean ranging, boolean usingSpec, double specMultiplier) {
		if (!ranging) {
			double strengthLvl = player.getSkills().getLevel(Skills.STRENGTH);
			int xpStyle = CombatDefinitions.getXpStyle(weaponId, attackStyle);
			double styleBonus = xpStyle == Skills.STRENGTH ? 3 : xpStyle == CombatDefinitions.SHARED ? 1 : 0;
			double otherBonus = 1;
			if (fullDharokEquipped(player)) {
				double hp = player.getHitpoints();
				double maxhp = player.getMaxHitpoints();
				double d = Math.min(1, hp / maxhp);
				otherBonus = 2 - d;
			}
			
			if (target instanceof NPC) {
				if (player.getEquipment().getAmuletId() == 4081 && Combat.isUndead(target))
					if(MysticsChamber.isMysticNPC(target.asNPC().getId()))
						otherBonus += 0.15;
					else
						otherBonus += 0.30;
				else if ((player.getEquipment().getAmuletId() == 10588 || player.getEquipment().getAmuletId() == 42018  || player.getEquipment().getAmuletId() == 25486 || player.getEquipment().getAmuletId() == 25740) && Combat.isUndead(target))
					otherBonus += 0.2;
				else {
					int hatId = player.getEquipment().getHatId();
					if ((hatId >= 8901 && hatId <= 8922) || Slayer.hasSlayerHelmet(player)) {
						if (player.getSlayerManager().isValidTask(((NPC) target).getName())
								|| (player.getSlayerManager().getBossTask() != null && player.getSlayerManager().getBossTask().equalsIgnoreCase(target.getName())))
							otherBonus += 0.125;
					}
				}
			}
			double effectiveStrength = 8 + Math.floor((strengthLvl * player.getPrayer().getStrengthMultiplier()) + styleBonus);
			if (fullVoidEquipped(player, 11665, 11676) || hasObsidian(player))
				effectiveStrength = Math.floor(effectiveStrength * (hasEliteVoid(player) ? 1.125 : 1.1));
			if (berskerEffect(player))
				effectiveStrength = Math.floor(effectiveStrength * 1.25);
			if (!(target instanceof Player))
				effectiveStrength =  Math.floor(effectiveStrength * getUltimateMeleeEffect(player));
			
			if (CombatDefinitions.getMeleeBonusStyle(weaponId, attackStyle) == CombatDefinitions.CRUSH_ATTACK)
				effectiveStrength =  Math.floor(effectiveStrength * getNightmareEffect(player));
			
			double strengthBonus = player.getCombatDefinitions().getBonuses()[CombatDefinitions.STRENGTH_BONUS];
			if (weaponId == -2) {
				strengthBonus += 82;
			}
			double baseDamage = 5 + effectiveStrength * (1 + (strengthBonus / 64));
			int maxHit = (int) Math.floor(baseDamage * specMultiplier * otherBonus);
			if (weaponId == 15403 && target instanceof NPC && ((NPC)target).getName().toLowerCase().contains("dagannoth"))
				maxHit *= 2;
			else if ((weaponId == 6746 || weaponId == 49675) && isDemon(target))
				maxHit *= 2;
			else if (weaponId == 52978 && isDragon(target))
				maxHit *= 1.3;
			else if (weaponId == 25529 || weaponId == 25589 || weaponId == 25661 || weaponId == 25563)
				maxHit *= 0.95;
			if ((player.getEquipment().getRingId() == 23643
					|| player.getEquipment().getRingId() == 25488 || player.getEquipment().getRingId() == 25741) && target instanceof NPC && (target.getName().startsWith("Tz") || target.getName().startsWith("Jal")))
				maxHit *= 1.1;
			if (weaponId == 52545 && target instanceof NPC && Wilderness.isAtWild(target))
				maxHit *= 1.4;
			if (player.getControlerManager().getControler() instanceof TheHorde)
				maxHit *= 1.1;
			if (target instanceof NPC)
				maxHit *= player.getAuraManager().getDamageMultiplier();
			 else if (Combat.hasCustomWeaponOnWild(player))
				 maxHit /= 2;
			return maxHit;
		} else {
			if (weaponId == 24338 && target instanceof Player) {
				player.getPackets().sendGameMessage("The royal crossbow feels weak and unresponsive against other players.");
				return 60;
			}
			double rangedLvl = player.getSkills().getLevel(Skills.RANGE);
			double styleBonus = attackStyle == 0 ? 3 : attackStyle == 1 ? 0 : 1;
			double otherBonus = 1;
			if (target instanceof NPC) {
				 if ((player.getEquipment().getAmuletId() == 42018  || player.getEquipment().getAmuletId() == 25486 || player.getEquipment().getAmuletId() == 25740) && Combat.isUndead(target))
						otherBonus += 0.2;
				 else if (player.getEquipment().getHatId() == 15490 || Slayer.hasFullSlayerHelmet(player)) {
					if (player.getSlayerManager().isValidTask(((NPC) target).getName()) || (player.getSlayerManager().getBossTask() != null && player.getSlayerManager().getBossTask().equalsIgnoreCase(target.getName())))
						otherBonus += 0.125;
				}
			}
			double effectiveStrenght = Math.floor(rangedLvl * player.getPrayer().getRangeStrengthMultiplier()) + styleBonus;
			if (fullVoidEquipped(player, 11664, 11675))
				effectiveStrenght = Math.floor(effectiveStrenght * (hasEliteVoid(player) ? 1.125 : 1.1));
			//	effectiveStrenght += Math.floor((player.getSkills().getLevelForXp(Skills.RANGE) / 5) + 1.6);
			if (!(target instanceof Player))
				effectiveStrenght =  Math.floor(effectiveStrenght * getUltimateRangeEffect(player));
			double strengthBonus = player.getCombatDefinitions().getBonuses()[CombatDefinitions.RANGED_STR_BONUS];
			double baseDamage = 5 + (((effectiveStrenght + 8) * (strengthBonus + 64)) / 64);
			int maxHit = (int) Math.floor(baseDamage * specMultiplier * otherBonus);
			if (performHexbow(weaponId, target)) {
				int combatModifier = ((NPC) target).getCombatLevel();
				if (target instanceof DungeonBoss) {
					if (combatModifier > 99)
						combatModifier = 99;
					maxHit *= 2.5 + (combatModifier / 100);// 150% increase
				}	else
					maxHit *= 1d + ((double)combatModifier / 500d);
					//maxHit *= 2 + (combatModifier / 100);
				int cap = weaponId == 25575 || weaponId == 25592 || weaponId == 25609 ? 850 : 810;
				if (maxHit > cap)
					maxHit = cap;
			} else if ((weaponId == 15913 || weaponId == 16337) && target instanceof NPC)
				maxHit *= 2;
			else if (weaponId == 51012 && isDragon(target))
				maxHit *= 1.3;
			else if (weaponId == 4214)
				maxHit *= 1d + ((double)getCrystalSetCount(player) * 0.03d);
			if ((player.getEquipment().getRingId() == 23643
					|| player.getEquipment().getRingId() == 25488 || player.getEquipment().getRingId() == 25741) && target instanceof NPC && (target.getName().startsWith("Tz") || target.getName().startsWith("Jal")))
				maxHit *= 1.1;
			if (weaponId == 52550 && target instanceof NPC && Wilderness.isAtWild(target))
				maxHit *= 1.4;
			else if (weaponId == 25544 && target instanceof NPC && Wilderness.isAtWild(target))
				maxHit *= 1.4;
			if (player.getControlerManager().getControler() instanceof TheHorde)
				maxHit *= 1.1;
			if (target instanceof NPC)
				maxHit *= player.getAuraManager().getDamageMultiplier();
			 else if (Combat.hasCustomWeaponOnWild(player))
				 maxHit /= 2;
			return maxHit;
		}
	}
	
	private static boolean isDemon(Entity target) {
		if (target instanceof NPC) {
			int id = ((NPC)target).getId();
			return target.getName().toLowerCase().contains("demon") || target.getName().toLowerCase().contains("hell") || (target instanceof Skotizo) || (target instanceof Cerberus)
					|| (id >= 6203 && id <= 6208);
		}
		return false;
	}
	
	private static boolean isDragon(Entity target) {
		if (target instanceof GreatOlm || target instanceof GreatOlmLeftClaw)
			return true;
		if (target instanceof NPC) {
			//int id = ((NPC)target).getId();
			String name = target.getName().toLowerCase();
			return name.contains("dragon") || name.contains("wyvern") || name.equals("vorkath")
					|| name.equals("drake") || name.contains("hydra") || name.equals("great olm")
					|| name.equals("wyrm") || name.equals("galvek");
		}
		return false;
	}

	public static boolean isTBow(int weaponId) {
		return (weaponId == 17295 || weaponId == 15836 || weaponId == 25441 || weaponId == 50997 || weaponId == 25460 || weaponId == 25469 || weaponId == 25575 || weaponId == 25592 || weaponId == 25609 || weaponId == 25662 || weaponId == 25533);
	}

	public static boolean performHexbow(int weaponId, Entity target) {
		if (isTBow(weaponId) && target instanceof NPC) {
			int type = ((NPC) target).getCombatDefinitions().getAttackStyle();
			int id = ((NPC) target).getId();
			if (type == NPCCombatDefinitions.MAGE || type == NPCCombatDefinitions.MAGE_FOLLOW || ((NPC) target).getId() == 10024 || ((NPC) target).getId() == 10128 || ((NPC) target).getId() == 11925 || ((NPC) target).getId() == 10744
					|| ((NPC) target).getId() == 10744 || ((NPC) target).getId() == 6247
					 || ((NPC) target).getId() == 6260
							 || id == 6203
							 || id == 28355
			|| target instanceof VasaNistirio
			|| target instanceof MuttadileMother || target instanceof MuttadileChild) { //special npcs that cant attack at distance but use mage(so their att style i set melee but isnt true)
				return true;
			}
		}
		return false;
	}

	private static final int[] OBBY_BOOST_WEAPONS =
		{ 6528, 6527, 6523, 6525, 6526, 51003 };

	private boolean berskerEffect(Player player) {
		if (player.getEquipment().getAmuletId() == 11128) {
			for (int id : OBBY_BOOST_WEAPONS) {
				if (player.getEquipment().getWeaponId() == id) {
					return true;
				}
			}
		}
		return false;
	}

/*
 * public int getRandomMaxHit(Player player, int weaponId, int attackStyle,
 * boolean ranging, boolean defenceAffects, double specMultiplier, boolean
 * usingSpec) { Random r = new Random(); max_hit = getMaxHit(player,
 * weaponId, attackStyle, ranging, usingSpec, specMultiplier); double
 * accuracyMultiplier = 1.0; if (defenceAffects) { accuracyMultiplier =
 * getSpecialAccuracyModifier(player); } if (ranging && defenceAffects) {
 * double accuracy = accuracyMultiplier * getRangeAccuracy(player,
 * attackStyle) + 1; double defence = getRangeDefence(target) + 1; if
 * (r.nextInt((int) accuracy) < r.nextInt((int) defence)) { return 0; } }
 * else if (defenceAffects) { double accuracy = accuracyMultiplier *
 * getMeleeAccuracy(player, attackStyle, weaponId) + 1; double defence =
 * getMeleeDefence(target, attackStyle, weaponId) + 1; if (r.nextInt((int)
 * accuracy) < r.nextInt((int) defence)) { return 0; } } int hit =
 * r.nextInt(max_hit + 1); if (target instanceof NPC) { NPC n = (NPC)
 * target; if (n.getId() == 9463 && hasFireCape(player)) hit += 40; } if
 * (player.getAuraManager().usingEquilibrium()) { int perc25MaxHit = (int)
 * (max_hit * 0.25); hit -= perc25MaxHit; max_hit -= perc25MaxHit; if (hit <
 * 0) hit = 0; if (hit < perc25MaxHit) hit += perc25MaxHit;
 * 
 * } return hit; }
 * 
 * /** Gets the melee accuracy of the player.
 * 
 * @param e The player.
 * 
 * @param attackStyle The attack style.
 * 
 * @param weaponId The weapon id.
 * 
 * @return The melee accuracy.
 */
/*
 * public static double getMeleeAccuracy(Player e, int attackStyle, int
 * weaponId) { int style = attackStyle == 0 ? 3 : attackStyle == 2 ? 1 : 0;
 * int attLvl = e.getSkills().getLevel(Skills.ATTACK); int attBonus =
 * e.getCombatDefinitions
 * ().getBonuses()[CombatDefinitions.getMeleeBonusStyle(weaponId,
 * attackStyle)]; if (weaponId == -2) { attBonus += 82; } double attMult =
 * 1.0 * e.getPrayer().getAttackMultiplier(); double accuracyMultiplier =
 * 1.0; if (fullVoidEquipped(e, 11665, 11676)) { accuracyMultiplier *= 0.15;
 * } double cumulativeAtt = attLvl * attMult + style; return (14 +
 * cumulativeAtt + (attBonus / 8) + ((cumulativeAtt * attBonus) / 64)) *
 * accuracyMultiplier; }
 * 
 * /** Gets the maximum melee hit.
 * 
 * @param e The player.
 * 
 * @param attackStyle The attack style.
 * 
 * @param weaponId The weapon id.
 * 
 * @return The maximum melee hit.
 */
/*
 * public static double getMeleeMaximum(Player e, int attackStyle, int
 * weaponId) { int strLvl = e.getSkills().getLevel(Skills.STRENGTH); int
 * strBonus =
 * e.getCombatDefinitions().getBonuses()[CombatDefinitions.STRENGTH_BONUS];
 * if (weaponId == -2) { strBonus += 82; } double strMult =
 * e.getPrayer().getStrengthMultiplier(); double xpStyle =
 * CombatDefinitions.getXpStyle(weaponId, attackStyle); double style =
 * xpStyle == Skills.STRENGTH ? 3 : xpStyle == CombatDefinitions.SHARED ? 1
 * : 0; int dhp = 0; double dharokMod = 1.0; double hitMultiplier = 1.0; if
 * (fullDharokEquipped(e)) { dhp = e.getMaxHitpoints() - e.getHitpoints();
 * dharokMod = (dhp * 0.001) + 1; } if (fullVoidEquipped(e, 11665, 11676)) {
 * hitMultiplier *= 1.1; } double cumulativeStr = (strLvl * strMult + style)
 * * dharokMod; return (int) ((14 + cumulativeStr + (strBonus / 8) +
 * ((cumulativeStr * strBonus) / 64)) * hitMultiplier); }
 * 
 * /** Gets the melee defence.
 * 
 * @param e The entity.
 * 
 * @param attackStyle The attack style.
 * 
 * @param weaponId The weapon id.
 * 
 * @return The maximum melee defence.
 */
/*
 * public static double getMeleeDefence(Entity e, int attackStyle, int
 * weaponId) { boolean player = e instanceof Player; int style = player ?
 * ((Player) e).getCombatDefinitions().getAttackStyle() : 0; style = style
 * == 2 ? 1 : style == 3 ? 3 : 0; int defLvl = player ? ((Player)
 * e).getSkills().getLevel(Skills.DEFENCE) : (int) (((NPC)
 * e).getCombatLevel() * 0.6); int defBonus = player ? ((Player)
 * e).getCombatDefinitions().getBonuses()[
 * CombatDefinitions.getMeleeDefenceBonus
 * (CombatDefinitions.getMeleeBonusStyle(weaponId, attackStyle))] : 0; if
 * (!player) { defBonus = ((NPC) e).getBonuses() != null ? ((NPC)
 * e).getBonuses()[
 * CombatDefinitions.getMeleeDefenceBonus(CombatDefinitions.
 * getMeleeBonusStyle(weaponId, attackStyle))] : 0; } double defMult = 1.0 *
 * (player ? ((Player) e).getPrayer().getDefenceMultiplier() : 1.0); double
 * cumulativeDef = defLvl * defMult + style; return 14 + cumulativeDef +
 * (defBonus / 8) + ((cumulativeDef * (defBonus)) / 64); }
 * 
 * /** Gets the range accuracy of the player.
 * 
 * @param e The player.
 * 
 * @param attackStyle The attack style.
 * 
 * @return The range accuracy.
 */
/*
 * public static double getRangeAccuracy(Player e, int attackStyle) { int
 * style = attackStyle == 0 ? 3 : attackStyle == 2 ? 1 : 0; int attLvl =
 * e.getSkills().getLevel(Skills.RANGE); int attBonus =
 * e.getCombatDefinitions().getBonuses()[4]; double attMult = 1.0 *
 * e.getPrayer().getRangeMultiplier() *
 * e.getAuraManager().getRangeAccurayMultiplier(); double accuracyMultiplier
 * = 1.05; if (fullVoidEquipped(e, 11664, 11675)) { accuracyMultiplier +=
 * 0.10; } double cumulativeAtt = attLvl * attMult + style; return (14 +
 * cumulativeAtt + (attBonus / 8) + ((cumulativeAtt * attBonus) / 64)) *
 * accuracyMultiplier; }
 * 
 * /** Gets the maximum range hit.
 * 
 * @param e The player.
 * 
 * @param attackStyle The attack style.
 * 
 * @return The maximum range hit.
 */
/*
 * public static double getRangeMaximum(Player e, int attackStyle) { int
 * style = attackStyle == 0 ? 3 : attackStyle == 2 ? 1 : 0; int strLvl =
 * e.getSkills().getLevel(Skills.RANGE); int strBonus =
 * e.getCombatDefinitions
 * ().getBonuses()[CombatDefinitions.RANGED_STR_BONUS]; double strMult = 1.0
 * * e.getPrayer().getRangeMultiplier(); double hitMultiplier = 1.0; if
 * (fullVoidEquipped(e, 11664, 11675)) { hitMultiplier += 0.1; } double
 * cumulativeStr = strLvl * strMult + style; return (14 + cumulativeStr +
 * (strBonus / 8) + ((cumulativeStr * strBonus) / 64)) * hitMultiplier; }
 * 
 * /** Gets the range defence.
 * 
 * @param e The entity.
 * 
 * @return The maximum range defence.
 */
/*
 * public static double getRangeDefence(Entity e) { boolean player = e
 * instanceof Player; int style = player ? ((Player)
 * e).getCombatDefinitions().getAttackStyle() : 0; style = style == 2 ? 1 :
 * style == 3 ? 3 : 0; int defLvl = player ? ((Player)
 * e).getSkills().getLevel(Skills.DEFENCE) : (int) (((NPC)
 * e).getCombatLevel() * 0.6); int defBonus = player ? ((Player)
 * e).getCombatDefinitions().getBonuses()[CombatDefinitions.RANGE_DEF] : 0;
 * if (!player) { defBonus = ((NPC) e).getBonuses() != null ? ((NPC)
 * e).getBonuses()[9] : 0; } double defMult = 1.0 * (player ? ((Player)
 * e).getPrayer().getDefenceMultiplier() : 1.0); double cumulativeDef =
 * defLvl * defMult + style; return 14 + cumulativeDef + (defBonus / 8) +
 * ((cumulativeDef * defBonus) / 64); }
 */

	private double getSpecialAccuracyModifier(int weaponId) {
		String name = ItemConfig.forID(weaponId).getName().toLowerCase();
		if (name.contains("tentacle") || name.contains("whip") || name.contains("dragon longsword") || name.contains("dragon scimitar") || name.contains("dragon dagger") || name.contains("magic shortbow"))
			return 1.1;
		if (weaponId == 54424 || weaponId == 51742)
			return 1.5;
		if (weaponId == 50849)
			return 1.25;
		if (name.contains("anchor") || name.contains("dragon hasta"))
			return 2;
		if (name.contains("armadyl godsword"))
			return 1.85;
		if (name.contains("dragon mace"))
			return 0.95;
		if (name.contains("magic longbow"))
			return 3;
		if (name.contains("dragon claws") || weaponId == 15241) //handcannon
			return 1.5;
		if (name.contains("abyssal dagger") || name.contains("ballista") || name.contains("dragon sword"))
			return 1.25;
		if (name.contains("bulwark"))
			return 1.2;
		if (name.contains("armadyl crossbow"))
			return 2;
		return 1;
	}

	public boolean hasFireCape(Player player) {
		int capeId = player.getEquipment().getCapeId();
		return capeId == 6570 || capeId == 20769 || capeId == 20771
				|| capeId == 43329 || capeId == 51285;
	}

/*
 * public static final int getMaxHit(Player player, int weaponId, int
 * attackStyle, boolean ranging, boolean usingSpec, double specMultiplier) {
 * if (ranging) { return (int) (getRangeMaximum(player, attackStyle) *
 * specMultiplier); } return (int) (getMeleeMaximum(player, weaponId,
 * attackStyle) * specMultiplier); }
 */

	public static final boolean fullVanguardEquipped(Player player) {
		int helmId = player.getEquipment().getHatId();
		int chestId = player.getEquipment().getChestId();
		int legsId = player.getEquipment().getLegsId();
		int weaponId = player.getEquipment().getWeaponId();
		int bootsId = player.getEquipment().getBootsId();
		int glovesId = player.getEquipment().getGlovesId();
		if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1 || bootsId == -1 || glovesId == -1)
			return false;
		return ItemConfig.forID(helmId).getName().contains("Vanguard") && ItemConfig.forID(chestId).getName().contains("Vanguard") && ItemConfig.forID(legsId).getName().contains("Vanguard") && ItemConfig.forID(weaponId).getName().contains("Vanguard") && ItemConfig.forID(bootsId).getName().contains("Vanguard") && ItemConfig.forID(glovesId).getName().contains("Vanguard");
	}
	
	public static int getCrystalSetCount(Player player) {
		int count = 0;
		if (player.getEquipment().getHatId() == 53971)
			count++;
		if (player.getEquipment().getChestId() == 53975)
			count++;
		if (player.getEquipment().getLegsId() == 53979)
			count++;
		return count == 3 ? 5 : count;
	}

	public static final boolean usingGoliathGloves(Player player) {
		String name = player.getEquipment().getItem(Equipment.SLOT_SHIELD) != null ? player.getEquipment().getItem(Equipment.SLOT_SHIELD).getDefinitions().getName().toLowerCase() : "";
		if (player.getEquipment().getItem((Equipment.SLOT_HANDS)) != null) {
			if (player.getEquipment().getItem(Equipment.SLOT_HANDS).getDefinitions().getName().toLowerCase().contains("goliath") && player.getEquipment().getWeaponId() == -1) {
				if (name.contains("defender") && name.contains("dragonfire shield"))
					return true;
				return true;
			}
		}
		return false;
	}

	public static final boolean fullVeracsEquipped(Player player) {
		int helmId = player.getEquipment().getHatId();
		int chestId = player.getEquipment().getChestId();
		int legsId = player.getEquipment().getLegsId();
		int weaponId = player.getEquipment().getWeaponId();
		if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1)
			return false;
		return ItemConfig.forID(helmId).getName().toLowerCase().contains("verac's") && ItemConfig.forID(chestId).getName().toLowerCase().contains("verac's") && ItemConfig.forID(legsId).getName().toLowerCase().contains("verac's") && ItemConfig.forID(weaponId).getName().toLowerCase().contains("verac's");
	}

	public static final boolean fullGuthanEquipped(Player player) {
		int helmId = player.getEquipment().getHatId();
		int chestId = player.getEquipment().getChestId();
		int legsId = player.getEquipment().getLegsId();
		int weaponId = player.getEquipment().getWeaponId();
		if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1)
			return false;
		return ItemConfig.forID(helmId).getName().toLowerCase().contains("guthan's")&& ItemConfig.forID(chestId).getName().toLowerCase().contains("guthan's")&& ItemConfig.forID(legsId).getName().toLowerCase().contains("guthan's")&& ItemConfig.forID(weaponId).getName().toLowerCase().contains("guthan's");
	}

	public static final boolean fullAkrisaeEquipped(Player player) {
		int helmId = player.getEquipment().getHatId();
		int chestId = player.getEquipment().getChestId();
		int legsId = player.getEquipment().getLegsId();
		int weaponId = player.getEquipment().getWeaponId();
		if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1)
			return false;
		return ItemConfig.forID(helmId).getName().toLowerCase().contains("akrisea's")&& ItemConfig.forID(chestId).getName().toLowerCase().contains("akrisea's")&& ItemConfig.forID(legsId).getName().toLowerCase().contains("akrisea's")&& ItemConfig.forID(weaponId).getName().toLowerCase().contains("akrisea's");
	}

	public static final boolean fullDharokEquipped(Player player) {
		int helmId = player.getEquipment().getHatId();
		int chestId = player.getEquipment().getChestId();
		int legsId = player.getEquipment().getLegsId();
		int weaponId = player.getEquipment().getWeaponId();

		if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1)
			return false;
		return ItemConfig.forID(helmId).getName().toLowerCase().contains("dharok")&& ItemConfig.forID(chestId).getName().toLowerCase().contains("dharok")&& ItemConfig.forID(legsId).getName().toLowerCase().contains("dharok")&& ItemConfig.forID(weaponId).getName().toLowerCase().contains("dharok");
	}
	
	public static boolean hasEliteVoid(Player player) {
		int legsId = player.getEquipment().getLegsId();
		int torsoId = player.getEquipment().getChestId();
		return (legsId == 19786 || legsId == 19788 || legsId == 19790) || (torsoId == 19785 || torsoId == 19787 || torsoId == 19789);
		
	}

	
	public static double getNightmareEffect(Player player) {
		int pieces = 0;
		for (Item item : player.getEquipment().getItems().getItems()) {
			if (item != null && 
					((item.getId() >= 54419 && item.getId() <= 54421)
					|| (item.getId() >= 25576 && item.getId() <= 25578)))
				pieces++;
		}
		return 1 + (pieces == 3 ? 0.025 : (pieces * 0.005));
	}
	
	
	public static double getUltimateMeleeEffect(Player player) {
		int pieces = 0;
		for (Item item : player.getEquipment().getItems().getItems()) {
			if(item == null)
				continue;
			if (((((item.getName().toLowerCase().startsWith("ultimate bandos")
					|| item.getName().toLowerCase().startsWith("ultimate torva"))) && item.getName().contains(("(i)")))
					|| (item.getId() >= 25576 && item.getId() <= 25578)))
				pieces++;
		}
		if(player.getPet() != null && player.getPet().getPet() == Pets.ELDER_OLMLET)
			pieces+=2;
		double stat = 1 + pieces * 0.01;
		
		if (player.getEquipment().getAmuletId() == 25740)
			stat += 0.02;
		if (player.getEquipment().getRingId() == 25741)
			stat += 0.02;
		/*pieces = 0;
		for (Item item : player.getEquipment().getItems().getItems()) {
			if (item != null && item.getId() >= 25576 && item.getId() <= 25578)
				pieces++;
		}
		stat += pieces * 0.02;*/
		return stat;
	}
	
	public static double getUltimateRangeEffect(Player player) {
		int pieces = 0;
		for (Item item : player.getEquipment().getItems().getItems()) {
			if(item == null)
				continue;
			if (((item.getName().toLowerCase().startsWith("ultimate armadyl")
					|| item.getName().toLowerCase().startsWith("ultimate pernix"))) && item.getName().contains(("(i)")))
				pieces++;
		}
		if(player.getPet() != null && player.getPet().getPet() == Pets.TWISTED_OLMLET)
			pieces+=2;
		double stat = 1 + pieces * 0.01;
		if (player.getEquipment().getAmuletId() == 25740)
			stat += 0.02;
		if (player.getEquipment().getRingId() == 25741)
			stat += 0.02;
		return stat;
	}

	static final int[] ULT_MAGE_GEAR = new int[] {25695, 25696, 25697, 25698, 44702, 25699,
	25611, 25612, 25613, 25614, 25615, 25620,
	25634, 25635, 25636, 25637, 25638, 25640, 25641};

	public static double getUltimateMageEffect(Player player) {
		int pieces = 0;
		for (Item item : player.getEquipment().getItems().getItems()) {
			if(item == null)
				continue;
			if (((((item.getName().toLowerCase().startsWith("ultimate ") && item.getName().toLowerCase().contains("subjugation")))
					|| item.getName().toLowerCase().startsWith("ultimate virtus"))) && item.getName().contains(("(i)")))
				pieces++;
			else {
				for(int i : ULT_MAGE_GEAR)
					if(item.getId() == i)
						pieces++;
			}
		}

		if(player.getPet() != null && player.getPet().getPet() == Pets.ANCESTRAL_OLMLET)
			pieces+= 2;
		double stat = 1 + pieces * 0.01;
		if (player.getEquipment().getAmuletId() == 25740)
			stat += 0.02;
		if (player.getEquipment().getRingId() == 25741)
			stat += 0.02;
		return stat;
	}
	
	
	public static final boolean fullVoidEquipped(Player player, int... helmid) {
		boolean hasDeflector = player.getEquipment().getShieldId() == 19712;
		if (player.getEquipment().getGlovesId() != 8842) {
			if (hasDeflector)
				hasDeflector = false;
			else
				return false;
		}
		int legsId = player.getEquipment().getLegsId();
		boolean hasLegs = legsId != -1 && (legsId == 8840 || legsId == 19786 || legsId == 19788 || legsId == 19790);
		if (!hasLegs) {
			if (hasDeflector)
				hasDeflector = false;
			else
				return false;
		}
		int torsoId = player.getEquipment().getChestId();
		boolean hasTorso = torsoId != -1 && (torsoId == 8839 || torsoId == 10611 || torsoId == 19785 || torsoId == 19787 || torsoId == 19789);
		if (!hasTorso) {
			if (hasDeflector)
				hasDeflector = false;
			else
				return false;
		}
		if (hasDeflector)
			return true;
		int helmId = player.getEquipment().getHatId();
		if (helmId == -1)
			return false;
		boolean hasHelm = false;
		for (int id : helmid) {
			if (helmId == id) {
				hasHelm = true;
				break;
			}
		}
		if (!hasHelm)
			return false;
		return true;
	}

	public void delayNormalHit(int weaponId, int attackStyle, Hit... hits) {
		delayHit(0, weaponId, attackStyle, hits);
	}
	
	public Hit getRegularHit(Player player, int damage) {
		return new Hit(player, damage, HitLook.REGULAR_DAMAGE);
	}

	public Hit getMeleeHit(Player player, int damage) {
		return new Hit(player, damage, HitLook.MELEE_DAMAGE);
	}

	public Hit getRangeHit(Player player, int damage) {
		return new Hit(player, damage, HitLook.RANGE_DAMAGE);
	}

	public Hit getMagicHit(Player player, int damage) {
		return new Hit(player, damage, HitLook.MAGIC_DAMAGE);
	}

	private void delayMagicHit(int delay, final Hit... hits) {
		delayHit(delay, -1, -1, hits);
	}

	public void resetVariables() {
		base_mage_xp = 0;
		mage_hit_gfx = 0;
		magic_sound = 0;
		max_poison_hit = 0;
		freeze_time = 0;
		reduceAttack = false;
		blood_spell = false;
		sang_spell = false;
		block_tele = false;
	}

	private int delayHitMS(int msTime, final int weaponId, final int attackStyle, final Hit... hits) {
		int cycle = msTime / 600;
		/*if ((msTime - (cycle * 600)) > 0)
			cycle++;*/
		/*int d = (msTime - (cycle * 600)) / 10;
		if(d > 30) 
			cycle++;
		else
			for(Hit hit : hits) 
				hit.setDelay(d);*/
		delayHit(cycle, weaponId, attackStyle, hits);
		return cycle;
	}

	private void delayHit(int delay, final int weaponId, final int attackStyle, final Hit... hits) {
		addAttackedByDelay(hits[0].getSource());
		final Entity target = this.target;
		final int max_hit = this.max_hit;
		final double base_mage_xp = this.base_mage_xp;
		final int mage_hit_gfx = this.mage_hit_gfx;
		final int magic_sound = this.magic_sound;
		final int magic_voice = this.magic_voice;
		final int max_poison_hit = this.max_poison_hit;
		final int freeze_time = this.freeze_time;
		@SuppressWarnings("unused")
		final boolean reduceAttack = this.reduceAttack;
		final boolean blood_spell = this.blood_spell;
		final boolean sang_spell = this.sang_spell;
		final boolean block_tele = this.block_tele;
		resetVariables();
		final int defenceEmote = Combat.getDefenceEmote(target);
		((Player)hits[0].getSource()).setLastTarget(target);
		for (Hit hit : hits) {
			Player player = (Player) hit.getSource();
			if (player.getPrayer().usingPrayer(1, 18) )
				target.sendSoulSplit(hit, player);

			if (Combat.instantProtectPrayer(target)) {
				Player targetP = (Player) target;
				if (targetP.getPrayer().usingPrayer(0, 16) || targetP.getPrayer().usingPrayer(1, 7))
					hit.setDamage((int) (hit.getDamage() * player.getMagePrayerMultiplier()));
				else if (targetP.getPrayer().usingPrayer(0, 17) || targetP.getPrayer().usingPrayer(1, 8))
					hit.setDamage((int) (hit.getDamage() * player.getRangePrayerMultiplier()));
				else if (targetP.getPrayer().usingPrayer(0, 18) || targetP.getPrayer().usingPrayer(1, 9))
					hit.setDamage((int) (hit.getDamage() * player.getMeleePrayerMultiplier()));
			}



			int damage = hit.getDamage() > target.getHitpoints() ? target.getHitpoints() : hit.getDamage();
			if (hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MELEE_DAMAGE) {
				double combatXp = (double)damage / 2.5d;
				player.getAuraManager().checkSuccefulHits(hit.getDamage());
				if (combatXp > 0/* && (!player.isIronman() || !(target instanceof Player))*/ && !(target instanceof NPC && ((NPC)target).getId() == 16008)) {
					if (hit.getLook() == HitLook.RANGE_DAMAGE) {
						if (attackStyle == 2) {
							player.getSkills().addXp(Skills.RANGE, combatXp / 2, target instanceof Player || target instanceof Familiar);
							player.getSkills().addXp(Skills.DEFENCE, combatXp / 2, target instanceof Player || target instanceof Familiar);
						} else
							player.getSkills().addXp(Skills.RANGE, combatXp, target instanceof Player || target instanceof Familiar);

					} else {
						int xpStyle = CombatDefinitions.getXpStyle(weaponId, attackStyle);
						if (xpStyle != CombatDefinitions.SHARED)
							player.getSkills().addXp(xpStyle, combatXp, target instanceof Player || target instanceof Familiar);
						else {
							player.getSkills().addXp(Skills.ATTACK, combatXp / 3, target instanceof Player || target instanceof Familiar);
							player.getSkills().addXp(Skills.STRENGTH, combatXp / 3, target instanceof Player || target instanceof Familiar);
							player.getSkills().addXp(Skills.DEFENCE, combatXp / 3, target instanceof Player || target instanceof Familiar);
						}
					}
					double hpXp = (double)damage / 7.5d;
					if (hpXp > 0)
						player.getSkills().addXp(Skills.HITPOINTS, hpXp, target instanceof Player || target instanceof Familiar);
				}
			} else if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
				if (mage_hit_gfx != 0 && damage > 0) {
					if ((target.getSize() == 1 || target instanceof VerzikNycolas || target instanceof NycolasMemetos || target instanceof NycolasAthanatos || target instanceof NycolasSpawn || target instanceof NylocasMatomenos || target instanceof Scorpia || target instanceof InfernoNPC || target instanceof DagannothKing) && target.getFrozenBlockedDelay() < Utils.currentTimeMillis()
							&& !(target instanceof CommanderZilyana)) {
						if (freeze_time > 0) {
							target.addFreezeDelay(freeze_time, freeze_time == 0);
							if (target instanceof Player)
								((Player) target).stopAll(false);
							target.addFrozenBlockedDelay(freeze_time + (4 * 1000));
						}
					}
				} else if (damage < 0) {
					damage = 0;
				}
				double combatXp = base_mage_xp * 1 + (damage / 5);
				player.getAuraManager().checkSuccefulHits(hit.getDamage());
				if (combatXp > 0 /*&& (!player.isIronman() || !(target instanceof Player))*/  && !(target instanceof NPC && ((NPC)target).getId() == 16008)) {
					if (player.getCombatDefinitions().isDefensiveCasting() || ((hasTridentOfSeas(player) || hasPolyporeStaff(player) || hasTridentOfSwamp(player)  || hasDawnbringer(player) || hasSangStaff(player) || hasCataclysm(player)) && player.getCombatDefinitions().getAttackStyle() == 1)) {
						int defenceXp = (int) (damage / 7.5);
						if (defenceXp > 0) {
							combatXp -= defenceXp;
							player.getSkills().addXp(Skills.DEFENCE, defenceXp, target instanceof Player || target instanceof Familiar);
						}
					}
					player.getSkills().addXp(Skills.MAGIC, combatXp, target instanceof Player || target instanceof Familiar);
					double hpXp = damage / 7.5;
					if (hpXp > 0)
						player.getSkills().addXp(Skills.HITPOINTS, hpXp, target instanceof Player || target instanceof Familiar);
				}
			}
		}

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				for (Hit hit : hits) {
					boolean splash = false;
					Player player = (Player) hit.getSource();
					if (/*player.isDead() || */player.hasFinished() || target.isDead() || target.hasFinished())
						return;
					if (target instanceof NPC) {
						NPC n = (NPC) target;
						if (n.isCantInteract())
							return;
					}
					if (hit.getDamage() > -1) {
						target.applyHit(hit); // also reduces damage if needed,
						// pray
						// and special items affect here
					} else {
						splash = true;
						hit.setDamage(0);
					}
					if (defenceEmote != -1)
						target.setNextAnimationNoPriority(new Animation(defenceEmote, hit.getDelay()));
					int damage = hit.getDamage() > target.getHitpoints() ? target.getHitpoints() : hit.getDamage();
					if ((damage >= max_hit * 1.0) && (hit.getLook() == HitLook.MAGIC_DAMAGE || hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MELEE_DAMAGE))
						hit.setCriticalMark();
					if (hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MELEE_DAMAGE) {
						double combatXp = damage / 2.5;
						if (combatXp > 0) {
							if (hit.getLook() == HitLook.RANGE_DAMAGE) {
								if (weaponId != -1) {
									String name = ItemConfig.forID(weaponId).getName();
									if (name.contains("(p++)")) {
										if (Utils.random(8) == 0 || target instanceof NycolasAthanatos)
											target.getPoison().makePoisoned(48);
									} else if (name.contains("(p+)") ) {
										if (Utils.random(8) == 0 || target instanceof NycolasAthanatos)
											target.getPoison().makePoisoned(38);
									} else if (name.contains("(p)")) {
										if (Utils.random(8) == 0 || target instanceof NycolasAthanatos)
											target.getPoison().makePoisoned(28);
									} else if (((weaponId == 42926 || weaponId == 25502) && Utils.random(4) == 0)
											||  target instanceof NycolasAthanatos)
										target.getPoison().makeEnvenomed(60);
								}
							} else {
								if (weaponId != -1) {
									String name = ItemConfig.forID(weaponId).getName();
									if (name.contains("(p++)")) {
										if (Utils.random(8) == 0 || target instanceof NycolasAthanatos)
											target.getPoison().makePoisoned(68);
									} else if (name.contains("(p+)")) {
										if (Utils.random(8) == 0 || target instanceof NycolasAthanatos)
											target.getPoison().makePoisoned(58);
									} else if (name.contains("(p)") || weaponId == 42006) {
										if (Utils.random(8) == 0 || target instanceof NycolasAthanatos)
											target.getPoison().makePoisoned(48);
									} 
									if (player.getEquipment().getHatId() == 42931 && ((target instanceof NPC && Utils.random(
											weaponId == 42006 ? 1 : 6) == 0) || target instanceof NycolasAthanatos))
										target.getPoison().makeEnvenomed(60);
									if (target instanceof Player) {
										if (((Player) target).getPolDelay() >= Utils.currentTimeMillis())
											target.setNextGraphics(new Graphics(2320));
									}
								}
							}
						}
					} else if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
						if (reduceAttack && target instanceof AbyssalSire)
								((AbyssalSire)target).shadowSpell();
						if (splash) {
							target.setNextGraphics(new Graphics(85, 0, 96));
							playSound(227, player, target);
						} else {
							if (mage_hit_gfx != 0) {
								target.setNextGraphics(new Graphics(mage_hit_gfx, 0, mage_hit_gfx == 78 || mage_hit_gfx == 369 || mage_hit_gfx == 1843 || (mage_hit_gfx > 1844 && mage_hit_gfx < 1855) ? 0 : 96));
								if (blood_spell)
									player.heal(damage / 4);
								if (sang_spell) {
									target.setNextGraphics(new Graphics(6542));
									player.heal(damage / 2);
								}
								if (block_tele) {
									if (target instanceof Player) {
										Player targetPlayer = (Player) target;
										targetPlayer.setTeleBlockDelay((targetPlayer.getPrayer().usingPrayer(0, 16) || targetPlayer.getPrayer().usingPrayer(1, 7) ? 100000 : 300000));
										targetPlayer.getPackets().sendGameMessage("You have been teleblocked.", true);
									}
								}
							}
							if (magic_sound > 0)
								playSound(magic_sound, player, target);
							if (magic_voice > 0)
								playVoice(magic_voice, player, target);
							 if (weaponId == 42904 && Utils.random(4) == 0)
								target.getPoison().makeEnvenomed(60);
						}
					}
					if (max_poison_hit > 0 && Utils.random(10) == 0) {
						if (!target.getPoison().isPoisoned())
							target.getPoison().makePoisoned(max_poison_hit);
					}
					if (target instanceof Player) {
						Player p2 = (Player) target;
						p2.closeInterfaces();
						if (p2.getCombatDefinitions().isAutoRelatie() && !p2.getActionManager().hasAction() && !p2.hasWalkSteps() && !p2.isLocked() && !p2.getEmotesManager().isDoingEmote() && !p2.hasRouteEvent())
							p2.getActionManager().setAction(new PlayerCombat(player));
						int shieldId = p2.getEquipment().getShieldId();
						if (shieldId != -1 && ItemConstants.itemDegradesWhileHit(shieldId))
							p2.getCharges().addCharges(shieldId, -1, Equipment.SLOT_SHIELD);
					} else {
						NPC n = (NPC) target;
						if (!n.isUnderCombat() || n.canBeAttackedByAutoRelatie())
							n.setTarget(player);
					}
				}
			}
		}, delay);
	}

	private int getSoundId(int weaponId, int attackStyle) {
		if (weaponId != -1) {
			String weaponName = ItemConfig.forID(weaponId).getName().toLowerCase();
			if (weaponName.contains("dart") || weaponName.contains("knife") || weaponName.contains(" stake"))
				return 2707;
			if (weaponName.contains("crossbow"))
				return 2708;
		}
		return -1;
	}

	public static int getWeaponAttackEmote(int weaponId, int attackStyle) {
		if (weaponId != -1) {
			if (weaponId == -2) {
				// punch/block:14393 kick:14307 spec:14417
				switch (attackStyle) {
				case 1:
					return 14307;
				default:
					return 14393;
				}
			}
			String weaponName = ItemConfig.forID(weaponId).getName().toLowerCase();
			if (weaponName != null && !weaponName.equals("null")) {
				if (weaponName.contains("c'bow") || weaponName.contains("crossbow") || weaponId == 25546)
					return weaponName.contains("royal crossbow") ? 16929 : weaponName.contains("karil's crossbow") ? 2075 : 4230;
			/*	if (weaponId == 52804)
					return 28194;
				if (weaponId == 52806 || weaponId == 52808 || weaponId == 52810)
					return 28195;*/
				if (weaponId == 42424 || weaponId == 50997 || weaponId == 25460 || weaponId == 25592 || weaponId == 25609 || weaponId == 25575 || weaponId == 25662 || weaponId == 25533 || weaponId == 25469 || weaponId == 42788 || weaponId == 25441 || weaponId == 52550
			|| weaponId == 25544)
					return 20426;
				if (weaponName.contains("bow") || weaponName.contains("decimation") || weaponId == 25539 || weaponId == 25617)
					return 426;
				if (weaponName.contains("chinchompa"))
					return 2779;
				if (weaponName.contains("staff of light") || weaponId == 41791 || weaponId == 25541 || weaponId == 42902 || weaponId == 42904) {
					switch (attackStyle) {
					case 0:
						return 15072;
					case 1:
						return 15071;
					case 2:
						return 414;
					}
				}
				if (weaponName.contains("trident") || weaponName.contains("mindspike") || weaponName.contains("staff") || weaponId == 25699  || weaponName.contains("wand") || weaponName.contentEquals("obliteration"))
					return 419;
				if (weaponName.contains("dart") || weaponName.contains(" stake"))
					return 6600;
				if (weaponName.contains("ballista"))
					return 12175;
				if (weaponName.contains("knife"))
					return 9055;
				if (weaponName.contains("bulwark"))
					return 27516;
				if (weaponName.contains("blade ") || weaponName.contains("scimitar") || weaponName.contains("sabre")  || weaponName.contains("korasi's sword")) {
					switch (attackStyle) {
					case 2:
						return 15072;
					default:
						return 15071;
					}
				}
				if (weaponName.contains("granite mace"))
					return 10662;//400;
				if (weaponId == 52545) 
					return 2062;
				if (weaponName.contains("mace") || weaponName.contains("annihilation") || weaponName.contains("carrot")) {
					switch (attackStyle) {
					case 2:
						return 400;
					default:
						return 10662;//weaponId == 54417 ? 10662 : 401;
					}
				}
				if (weaponName.endsWith(" axe") || weaponName.contains("hatchet") || weaponName.contains("battleaxe")) {
					switch (attackStyle) {
					case 2:
						return 401;
					default:
						return 395;
					}
				}
				if (weaponName.contains("hammers"))
					return 2068;
				if (weaponName.contains("bludgeon") || weaponName.contains(" hammer") || weaponName.contains(" warhammer")/*"warhammer"*/) {
					switch (attackStyle) {
					default:
						return 401;
					}
				}
				if (weaponName.contains("claws") || weaponName.contains("fists")) {
					switch (attackStyle) {
					case 2:
						return 1067;
					default:
						return 393;
					}
				}
				if (weaponName.contains("tentacle") || weaponName.contains("whip")) {
					switch (attackStyle) {
					case 1:
						return 11969;
					case 2:
						return 11970;
					default:
						return 11968;
					}
				}
				if (weaponName.contains("anchor")) {
					switch (attackStyle) {
					default:
						return 5865;
					}
				}
				if (weaponName.contains("tzhaar-ket-em")) {
					switch (attackStyle) {
					default:
						return 401;
					}
				}
				if (weaponName.contains("tzhaar-ket-om")) {
					switch (attackStyle) {
					default:
						return 13691;
					}
				}
				if (weaponName.contains("halberd") || weaponName.contains("polearm")) {
					switch (attackStyle) {
					case 1:
						return 440;
					default:
						return 428;
					}
				}
				if (weaponName.contains("zamorakian spear")
						|| weaponName.contains("zamorakian hasta")
						|| weaponName.contains("dragon hunter lance")) {
					switch (attackStyle) {
					case 1:
						return 12005;
					case 2:
						return 12009;
					default:
						return 12006;
					}
				}
				if (weaponName.contains("spear") || weaponName.contains("hasta") || weaponName.contains("lance")) {
					switch (attackStyle) {
					case 1:
						return 440;
					case 2:
						return 429;
					default:
						return 428;
					}
				}
				if (weaponName.contains("flail")) {
					return 2062;
				}
				if (weaponName.contains("javelin") || weaponName.contains("throwing axe") || weaponName.contains("thrownaxe")) {
					return 10501;
				}
				if (weaponName.contains("pickaxe")) {
					switch (attackStyle) {
					case 2:
						return 400;
					default:
						return 401;
					}
				}
				if (weaponName.contains("dragon dagger")) {
					switch (attackStyle) {
					case 2:
						return 377;
					default:
						return 376;
					}
				}
				if (weaponName.contains("scythe") || weaponId == 25540 || weaponId == 25618) {
					switch (attackStyle) {
					case 1:
						return 428;
					case 2:
						return 2067;
					default:
						return 1203;
					}
				}
				if (weaponName.contains("dagger")) {
					switch (attackStyle) {
					case 2:
						return 377;
					default:
						return 400;
					}
				}
				if (weaponName.contains("2h sword") || weaponName.equals("dominion sword") || weaponName.equals("thok's sword") || weaponName.contains("saradomin sword") || weaponName.contains("blessed sword")) {
					switch (attackStyle) {
					case 2:
						return 7048;
					case 3:
						return 7049;
					default:
						return 7041;
					}
				}
				if (weaponName.contains("longsword") || weaponName.contains("saber") || weaponName.endsWith("light") || weaponName.contains("excalibur")) {
					switch (attackStyle) {
					case 2:
						return 12310;
					default:
						return 12311;
					}
				}
				if (weaponName.contains("rapier") || weaponName.contains("brackish") || weaponName.contains(" sword") || weaponName.contains("dagger") || weaponName.contains("toktz-xil-ek") || weaponName.contains("toktz-xil-ak")
						|| weaponName.contains(" harpoon")) {
					switch (attackStyle) {
					case 2:
						return 13048;
					default:
						return 13049;
					}
				}
				if (weaponName.contains("katana")) {
					switch (attackStyle) {
					case 2:
						return 1882;
					default:
						return 1884;
					}
				}
				if (weaponName.contains("godsword")) {
					switch (attackStyle) {
					case 2:
						return 11980;
					case 3:
						return 11981;
					default:
						return 11979;
					}
				}
				if (weaponName.contains("balmung") || weaponName.contains("greataxe")) {
					switch (attackStyle) {
					case 2:
						return 12003;
					default:
						return 12002;
					}
				}
				if (weaponName.contains("granite maul")) {
					switch (attackStyle) {
					default:
						return 1665;
					}
				}

			}
		}
		switch (weaponId) {
		case 42926:
		case 25502:
			return 25061;
		case 16405:// novite maul
		case 16407:// Bathus maul
		case 16409:// Maramaros maul
		case 16411:// Kratonite maul
		case 16413:// Fractite maul
		case 16415:// Zephyrium maul
		case 16417:// Argonite maul
		case 16419:// Katagon maul
		case 16421:// Gorgonite maul
		case 16423:// Promethium maul
		case 16425:// primal maul
		case 16174:
		case 16175:
		case 16176:
		case 16177:
		case 16178:
		case 16179:
		case 16180:
		case 16181:
		case 16182:
		case 16183:
		case 16184:
			return 2661; // maul
		case 18353:// chaotic maul
		case 51003: //elder maul
			return 13055;
		case 7671:
		case 7673:
			return 3678;
		case 21364:
			return 10501;
		case 15241:
		case 25584:
			return 12174;
		default:
			switch (attackStyle) {
			case 1:
				return 423;
			default:
				return 422; // todo default emote
			}
		}
	}

	@Override
	public void stop(final Player player) {
		player.setNextFaceEntity(null);
	}

	private boolean checkAll(Player player) {
		if (player.isDead() || player.hasFinished() || target.isDead() || target.hasFinished() || player.isCantWalk()) {
			return false;
		}
		int distanceX = player.getX() - target.getX();
		int distanceY = player.getY() - target.getY();
		int size = player.getSize();
		int maxDistance = 16;
		if (player.getPlane() != target.getPlane() || distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance) {
			return false;
		}
		if (target instanceof Player) {
			Player p2 = (Player) target;
			if (!player.isCanPvp() || !p2.isCanPvp())
				return false;

		} else {
			NPC n = (NPC) target;
			if (n.isCantInteract()) {
				return false;
			}
			if (n instanceof Familiar) {
				Familiar familiar = (Familiar) n;
				if (!familiar.canAttack(player))
					return false;
			} else {
				if (!n.canBeAttackFromOutOfArea() && !MapAreas.isAtArea(n.getMapAreaNameHash(), player)) {
					return false;
				}
				int slayerLevel = n.isXmas() ? 1 : n.getName().equals("Hydra") && n.getId() < Settings.OSRS_NPC_OFFSET ? 1 : Slayer.getLevelRequirement(n.getName());
				if (slayerLevel > player.getSkills().getLevel(Skills.SLAYER)) {
					player.getPackets().sendGameMessage("You need a slayer level of " + slayerLevel + " to know how to wound this monster.");
					return false;
				}
				if (n.getId() == 114) {
					Mogre m = (Mogre) target;
					if (m.getOwner() != player) {
						player.getPackets().sendGameMessage("This isn't your target!");
						return false;
					}
					return true;
				}

				if(!n.preAttackCheck(player)) {
					return false;
				}
				if (n.getId() == 879) {
					if (player.getEquipment().getWeaponId() != 2402 && player.getCombatDefinitions().getAutoCastSpell() <= 0 && !hasPolyporeStaff(player) && !hasTridentOfSeas(player) && !hasTridentOfSwamp(player) && !hasDawnbringer(player) && !hasSangStaff(player) && !hasCataclysm(player)) {
						player.getPackets().sendGameMessage("I'd better wield Silverlight first.");
						return false;
					}
				} else if (n.getId() >= 14084 && n.getId() <= 14139) {
					int weaponId = player.getEquipment().getWeaponId();
					if (!((weaponId >= 13117 && weaponId <= 13146) || (weaponId >= 21580 && weaponId <= 21582)) && player.getCombatDefinitions().getAutoCastSpell() <= 0 && !hasPolyporeStaff(player) && !hasTridentOfSeas(player)  && !hasTridentOfSwamp(player)  && !hasDawnbringer(player) && !hasSangStaff(player) && !hasCataclysm(player)) {
						player.getPackets().sendGameMessage("I'd better wield a silver weapon first.");
						return false;
					}
				} else if (n instanceof GodwarsArmadylFaction || n.getId() == 6222 || n.getId() == 6223 || n.getId() == 6225 || n.getId() == 6227) {
					if (!player.getCombatDefinitions().isDistancedStyle()) {
						player.getPackets().sendGameMessage("The Aviansie is flying too high for you to attack using melee.");
						return false;
					}
				} else if (n instanceof Dawn) {
					if (!player.getCombatDefinitions().isDistancedStyle()) {
						player.getPackets().sendGameMessage("Dawn is flying too high for you to attack using melee.");
						return false;
					}
				} else if (n instanceof CaveKraken || n instanceof Kraken || n instanceof KrakenTentacle) {
					if (!player.getCombatDefinitions().isDistancedStyle()) {
						player.getPackets().sendGameMessage("You can't reach this npc with melee.");
						return false;
					}
					if (n instanceof Kraken && !((Kraken) n).isReady()) {
						player.getPackets().sendGameMessage("Maybe you should try to disturbe the smaller whirpools first.");
						return false;
					}
				} else if ((n instanceof AbyssalSire && !((AbyssalSire)n).canAttack(player)) || (n instanceof AbyssalSpawn && !((AbyssalSpawn)n).canAttack(player))) {
					player.getPackets().sendGameMessage("Someone else is already fighting this boss.");
					return false;
				} else if (n.getId() == 14301 || n.getId() == 14302 || n.getId() == 14303 || n.getId() == 14304) {
					if(n.getRegionId() != 9535) {
						Glacyte glacyte = (Glacyte) n;
						if (glacyte.getGlacor().getTargetIndex() != -1 && player.getIndex() != glacyte.getGlacor().getTargetIndex()) {
							player.getPackets().sendGameMessage("This isn't your target.");
							return false;
						}
					}
				} else if (n.getId() == 1007 || n.getId() == 1264 || n.getId() == 5144 || n.getId() == 5145) {
					ClueNPC npc = (ClueNPC) n;
					if (npc.getTarget() != player) {
						player.getPackets().sendGameMessage("This isn't your target.");
						return false;
					}
				}
			}
		}
		if (!(target instanceof NPC && ((NPC) target).isForceMultiAttacked())) {

			if (!target.isAtMultiArea() || !player.isAtMultiArea()) {
				if (player.getAttackedBy() != null && !player.getAttackedBy().isDead() && player.getAttackedBy() != target && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
					player.getPackets().sendGameMessage("You are already in combat.");
					return false;
				}
				if (target.getAttackedBy() != player && target.getAttackedByDelay() > Utils.currentTimeMillis()) {
					player.getPackets().sendGameMessage("That " + (player.getAttackedBy() instanceof Player ? "player" : "npc") + " is already in combat.");
					return false;
				}
			}
		}
		int targetSize = target.getSize();
		if (player.getFreezeDelay() >= Utils.currentTimeMillis()) {
			if (Utils.colides(player.getX(), player.getY(), size, target.getX(), target.getY(), targetSize))// under
				// target
				return false;
			if (!player.getCombatDefinitions().isDistancedStyle() && target.getSize() == 1 && Math.abs(player.getX() - target.getX()) == 1 && Math.abs(player.getY() - target.getY()) == 1 && !target.hasWalkSteps()) // diagonal
				return false;
			return true;
		}
		if (Utils.colides(player.getX(), player.getY(), size, target.getX(), target.getY(), targetSize) && !target.hasWalkSteps()) {
			player.resetWalkSteps();
			if (!player.addWalkSteps(target.getX() + targetSize, player.getY())) {
				player.resetWalkSteps();
				if (!player.addWalkSteps(target.getX() - size, player.getY())) {
					player.resetWalkSteps();
					if (!player.addWalkSteps(player.getX(), target.getY() + targetSize)) {
						player.resetWalkSteps();
						if (!player.addWalkSteps(player.getX(), target.getY() - size)) {
							return false;
						}
					}
				}
			}
			return true;
		} else if (!player.getCombatDefinitions().isDistancedStyle() && target.getSize() == 1 && Math.abs(player.getX() - target.getX()) == 1 && Math.abs(player.getY() - target.getY()) == 1 && !target.hasWalkSteps()) {
			if (!player.addWalkSteps(target.getX(), player.getY(), 1))
				player.addWalkSteps(player.getX(), target.getY(), 1);
			return true;
		}
		maxDistance = calculateAttackRange(player);
		boolean needCalc = !player.hasWalkSteps() || target.hasWalkSteps();
		if ((!player.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) || !Utils.isOnRange(player.getX(), player.getY(), size, target.getX(), target.getY(), target.getSize(), maxDistance)) {
			// if (!player.hasWalkSteps()) {
			if (needCalc) {
				player.resetWalkSteps();
				player.calcFollow(target, player.getRun() ? 2 : 1, true, true);
			}
			// }
			return true;
		} else {
			player.resetWalkSteps();
		}
		if (player.getPolDelay() >= Utils.currentTimeMillis() && !(player.getEquipment().getWeaponId() == 15486 || player.getEquipment().getWeaponId() == 25379 || player.getEquipment().getWeaponId() == 22207 || player.getEquipment().getWeaponId() == 22209 || player.getEquipment().getWeaponId() == 22211 || player.getEquipment().getWeaponId() == 22213
				 || player.getEquipment().getWeaponId() == 25379
				 || player.getEquipment().getWeaponId() == 41791 || player.getEquipment().getWeaponId() == 42902 || player.getEquipment().getWeaponId() == 25541 || player.getEquipment().getWeaponId() == 42904))
			player.setPolDelay(0);
		return true;
	}

	public static boolean specialExecute(Player player) {
		int weaponId = player.getEquipment().getWeaponId();
		player.getCombatDefinitions().switchUsingSpecialAttack();
		int specAmt = getSpecialAmmount(weaponId);
		if (specAmt == 0) {
			player.getPackets().sendGameMessage("This weapon has no special Attack, if you still see special bar please relogin.");
			player.getCombatDefinitions().desecreaseSpecialAttack(0);
			return false;
		}
		if (player.getCombatDefinitions().hasRingOfVigour())
			specAmt *= 0.9;
		if (player.getCombatDefinitions().getSpecialAttackPercentage() < specAmt) {
			player.getPackets().sendGameMessage("You don't have enough power left.");
			player.getCombatDefinitions().desecreaseSpecialAttack(0);
			return false;
		}
		player.getCombatDefinitions().desecreaseSpecialAttack(specAmt);
		return true;
	}

	/**
	 * Checks if the player is wielding polypore staff.
	 * 
	 * @param player
	 *            The player.
	 * @return {@code True} if so.
	 */
	private static boolean hasPolyporeStaff(Player player) {
		int weaponId = player.getEquipment().getWeaponId();
		return weaponId == 22494 || weaponId == 22496;
	}
	
	private static boolean hasTridentOfSeas(Player player) {
		int weaponId = player.getEquipment().getWeaponId();
		return weaponId == 41905 || weaponId == 41907;
	}
	
	private static boolean hasTridentOfSwamp(Player player) {
		int weaponId = player.getEquipment().getWeaponId();
		return weaponId == 42899;
	}
	
	private static boolean hasDawnbringer(Player player) {
		int weaponId = player.getEquipment().getWeaponId();
		return weaponId == 52516 || weaponId == 25583;
	}
	
	private static boolean hasSangStaff(Player player) {
		int weaponId = player.getEquipment().getWeaponId();
		return weaponId == 52323 || weaponId == 25496 || weaponId == 25764;
	}

	private static boolean hasCataclysm(Player player) {
		int weaponId = player.getEquipment().getWeaponId();
		return weaponId == 25699;
	}



	public Entity getTarget() {
		return target;
	}
	
	public Ammo getAmmo(int id) {
		for (Ammo ammo : Ammo.values())
			for(int i : ammo.getIDs())
				if (i == id)
					return ammo;
		return null;
	}
	
	public static enum Ammo {
		//arrows
		BRONZE_ARROW(7, new int[] {882, 883, 5616, 5622}, AmmoType.ARROW, 0, 19, 1104, 10),
		IRON_ARROW(10, new int[] {884, 885, 5617, 5623}, AmmoType.ARROW, 1, 18, 1105, 11),
		STEEL_ARROW(16, new int[] {886, 887, 5618, 5624}, AmmoType.ARROW, 2, 20, 1106, 12),
		MITHRIL_ARROW(22, new int[] {888, 889, 5619, 5625}, AmmoType.ARROW, 3, 21, 1107, 13),
		ADAMANT_ARROW(31, new int[] {890, 891, 5620, 5626}, AmmoType.ARROW, 4, 22, 1108, 14),
		RUNE_ARROW(49, new int[] {892, 893, 5621, 5627}, AmmoType.ARROW, 5, 24, 1109, 15),
		ICE_ARROW(16, new int[] {78}, AmmoType.ARROW, 5, 25, 1110, 16),
		BROAD_ARROW(28, new int[] {4160}, AmmoType.ARROW, 6, 325, 1112, 324),
		DRAGON_ARROW(60, new int[] {11212, 11227, 11228, 11229}, AmmoType.ARROW, 7, 1116, 1111, 1115),
		//bolts
		KEBBIT_BOLT(10, new int[] {10158, 10159}, AmmoType.BOLT, 0, 27),
		BRONZE_BOLT(10, new int[] {877, 878, 6061, 6062}, AmmoType.BOLT, 0, 27),
		OPAL_BOLT(14, new int[] {879, 9236}, AmmoType.GEM_BOLT, 0, 27),
		BLURITE_BOLT(28, new int[] {9139, 9286, 9293, 9300}, AmmoType.BOLT, 1, 27),
		JADE_BOLT(30, new int[] {9237, 9335}, AmmoType.GEM_BOLT, 1, 27),
		IRON_BOLT(46, new int[] {9140, 9287, 9294, 9301}, AmmoType.BOLT, 2, 27),
		PEARL_BOLT(48, new int[] {880, 9238}, AmmoType.GEM_BOLT, 2, 27),
		SILVER_BOLT(36, new int[] {9145, 9292, 9299, 9306}, AmmoType.BOLT, 2, 27), 
		STEEL_BOLT(64, new int[] {9141, 9288, 9295, 9301}, AmmoType.BOLT, 3, 27),
		TOPAZ_BOLT(66, new int[] {9239, 9336}, AmmoType.GEM_BOLT, 3, 27),
		BLACK_BOLT(75, new int[] {13083, 13084, 13085, 13086}, AmmoType.BOLT, 4, 27), 
		MITHRIL_BOLT(82, new int[] {9142, 9289, 9296, 9303}, AmmoType.BOLT, 5, 27),
		SAPPHIRE_BOLT(83, new int[] {9240, 9337}, AmmoType.GEM_BOLT, 5, 27),
		EMERALD_BOLT(85, new int[] {9241, 9338}, AmmoType.GEM_BOLT, 5, 27),
		ADAMANT_BOLT(100, new int[] {9143, 9290, 9297, 9304}, AmmoType.BOLT, 6, 27),
		RUBY_BOLT(103, new int[] {9242, 9339}, AmmoType.GEM_BOLT, 6, 27),
		DIAMOND_BOLT(105, new int[] {9243, 9340}, AmmoType.GEM_BOLT, 6, 27),
		RUNE_BOLT(115, new int[] {9144, 9291, 9298, 9305}, AmmoType.BOLT, 7, 27),
		DRAGON_BOLT(117, new int[] {9244, 9341}, AmmoType.GEM_BOLT, 7, 27),
		ONYX_BOLT(120, new int[] {9245, 9342}, AmmoType.GEM_BOLT, 7, 27),
		BROAD_BOLT(100, new int[] {13280}, AmmoType.BOLT, 7, 27),
		//other bolts
		BONE_BOLT(49, new int[] {8882}, AmmoType.BONE, 0, 696),
		BOLT_RACK(55, new int[] {4740}, AmmoType.RACK, 0, 27),
		//thrownables
		//dart
		BRONZE_DART(1, new int[] {806, 812, 5628, 5635}, AmmoType.DART, 0, 232, 226),
		IRON_DART(3, new int[] {807, 813, 5629, 5636}, AmmoType.DART, 0, 233, 227),
		STEEL_DART(4, new int[] {808, 814, 5630, 5637}, AmmoType.DART, 0, 234, 228),
		BLACK_DART(6, new int[] {3093, 3094, 5631, 5638}, AmmoType.DART, 0, 235, 229),
		MITHRIL_DART(7, new int[] {809, 815, 5632, 5639}, AmmoType.DART, 0, 235, 229),
		ADAMANT_DART(10, new int[] {810, 816, 5633, 5640}, AmmoType.DART, 0, 236, 230),
		RUNE_DART(14, new int[] {811, 817, 5634, 5641}, AmmoType.DART, 0, 237, 231),
		DRAGON_DART(20, new int[] {11230, 11231, 11233, 11234}, AmmoType.DART, 0, 1123, 1122),
		//knives
		BRONZE_KNIFE(3, new int[] {864, 870, 5654, 5661}, AmmoType.KNIFE, 0, 219, 212),
		IRON_KNIFE(4, new int[] {863, 871, 5655, 5662}, AmmoType.KNIFE, 0, 220, 213),
		STEEL_KNIFE(7, new int[] {865, 872, 5656, 5663}, AmmoType.KNIFE, 0, 221, 214),
		BLACK_KNIFE(8, new int[] {869, 874, 5658, 5665}, AmmoType.KNIFE, 0, 222, 215),
		MITHRIL_KNIFE(10, new int[] {866, 873, 5657, 5664}, AmmoType.KNIFE, 0, 223, 216),
		ADAMANT_KNIFE(14, new int[] {867, 875, 5659, 5666}, AmmoType.KNIFE, 0, 224, 217),
		RUNE_KNIFE(24, new int[] {868, 876, 5660, 5667}, AmmoType.KNIFE, 0, 225, 218),
		//javalin
		BRONZE_JAVELIN(25, new int[] {825, 831, 5642, 5648}, AmmoType.JAVELIN, 0, 206, 200),
		IRON_JAVELIN(42, new int[] {826, 832, 5643, 5649}, AmmoType.JAVELIN, 0, 207, 201),
		STEEL_JAVELIN(64, new int[] {827, 833, 5644, 5650}, AmmoType.JAVELIN, 0, 208, 202),
		MITHRIL_JAVELIN(85, new int[] {828, 834, 5645, 5651}, AmmoType.JAVELIN, 0, 209, 203),
		ADAMANT_JAVELIN(107, new int[] {829, 835, 5646, 5652}, AmmoType.JAVELIN, 0, 210, 204),
		RUNE_JAVELIN(124, new int[] {830, 836, 5647, 5653}, AmmoType.JAVELIN, 0, 211, 205),
		DRAGON_JAVELIN(145, new int[] {49484, 49486, 49488, 49490}, AmmoType.JAVELIN, 0, 208, 202),
		MORRIGAN_JAVELIN(145, new int[] {13879, 13880, 13881, 13882, 13953, 13954, 13955, 13956}, AmmoType.JAVELIN, 0, 1837),
		//throwing axes
		BRONZE_THROWING_AXE(5, new int[] {800, }, AmmoType.THROWING_AXE, 0, 43, 36),
		IRON_THROWING_AXE(7, new int[] {801}, AmmoType.THROWING_AXE, 0, 42, 35),
		STEEL_THROWING_AXE(11, new int[] {802}, AmmoType.THROWING_AXE, 0, 44, 37),
		MITHRIL_THROWING_AXE(16, new int[] {803}, AmmoType.THROWING_AXE, 0, 45, 38),
		ADAMANT_THROWING_AXE(23, new int[] {804}, AmmoType.THROWING_AXE, 0, 46, 39),
		RUNE_THROWING_AXE(26, new int[] {805}, AmmoType.THROWING_AXE, 0, 48, 41),
		OBSIDIAN_THROWING_AXE(49, new int[] {6522}, AmmoType.THROWING_AXE, 0, 442),
		MORRIGAN_THROWING_AXE(117, new int[] {13883, 13957}, AmmoType.THROWING_AXE, 0, 1839),
		//bows
		CRYSTAL_BOW(70, new int[] {4212, 4214, 4215, 4216, 4217, 4218, 4219, 4220, 4221, 4222, 4223}, AmmoType.BOW, 0, 250, 249),
		ZARYTE_BOW(115, new int[] {20171}, AmmoType.BOW, 0, 472, 471), //wrong gfx but real one was overriden .. this one almost same
		//handcannon
		HAND_CANNON_SHOT(150, new int[] {15243}, AmmoType.SHOT, 0, 2138, 2143),
		//chimcompa
		CHINCHOMPA(0, new int[] {10033}, AmmoType.CHINCHOMPA, 0, 908), 
		RED_CHINCHOMPA(15, new int[] {10034}, AmmoType.CHINCHOMPA, 0, 909), 
		BLACK_CHINCHOMPA(15, new int[] {41959}, AmmoType.CHINCHOMPA, 0, 6272), 
		;

		private int[] ids;
		private AmmoType type;
		private int rangedStrength, priority, startGFX, startDoubleGFX, projectileGFX;
		
		private Ammo(int rangedStrength, int[] ids, AmmoType type, int priority, int projectileGFX) {
			this(rangedStrength, ids, type, priority, -1, projectileGFX);
		}
		
		private Ammo(int rangedStrength,int[] ids, AmmoType type, int priority, int startGFX, int projectileGFX) {
			this(rangedStrength, ids, type, priority, startGFX, -1, projectileGFX);
		}
		
		private Ammo(int rangedStrength, int[] ids, AmmoType type, int priority, int startGFX, int startDoubleGFX, int projectileGFX) {
			this.rangedStrength = rangedStrength;
			this.ids = ids;
			this.type = type;
			this.priority = priority;
			this.startGFX = startGFX;
			this.startDoubleGFX = startDoubleGFX;
			this.projectileGFX = projectileGFX;
		}
		
		public int getRangedStrength() {
			return rangedStrength;
		}
		
		public int[] getIDs() {
			return ids;
		}
		
		public int getPriority() {
			return priority;
		}
		
		public AmmoType getType() {
			return type;
		}
		
		public int getStartGFX() {
			return startGFX;
		}
		
		public int getStartDoubleGFX() {
			return startDoubleGFX;
		}
		
		public int getProjectileGFX() {
			return projectileGFX;
		}
	}
	
	public enum AmmoType {

		ARROW,
		BOW,
		SHOT(false, false),
		CHINCHOMPA(false, false),
		BOLT,
		GEM_BOLT,
		OGRE,
		BONE,
		RACK(false, true),
		DART,
		KNIFE,
		JAVELIN,
		THROWING_AXE,
		GRENADE(false, false),
		GUN;
		
		private boolean save, drop; //if ava accumulator saves, notice that save and drop settings are ignored by non stackable ammo
		
		private AmmoType() {
			this(true, true);
		}
		
		/**
		 * 
		 * @param save - if ava saves ammo or not. ignored by non stacakbles
		 * @param drop if ammo drops or not. ignored by non stacakbles
		 */
		private AmmoType(boolean save, boolean drop) {
			this.save = save;
			this.drop = drop;
		}
		
		public boolean isSave() {
			return save;
		}
		
		public boolean isDrop() {
			return drop;
		}
		
	}


	public static void unloadBlowpipe(Player player) {
		if (player.getBlowpipeDarts() == null) {
			player.getPackets().sendGameMessage("Your blowpipe is already empty.");
			return;
		}
		if (player.getInventory().getFreeSlots() == 0) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return;
		}
		player.getInventory().addItem(player.getBlowpipeDarts());
		player.setBlowpipeDarts(null);
		player.getPackets().sendGameMessage("You unload the blowpipe.", true);
	}

	public static void unloadInfernalBlowpipe(Player player) {
		if (player.getInfernalBlowpipeDarts() == null) {
			player.getPackets().sendGameMessage("Your infernal blowpipe is already empty.");
			return;
		}
		if (player.getInventory().getFreeSlots() == 0) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return;
		}
		player.getInventory().addItem(player.getInfernalBlowpipeDarts());
		player.setInfernalBlowpipeDarts(null);
		player.getPackets().sendGameMessage("You unload the infernal blowpipe.", true);
	}
	
	
	public static boolean chargeBlowpipe(Player player, Item with, Item used) {
		if (with.getId() != 42926 && used.getId() != 42926)
			return false;
		Item darts = with.getId() == 42926 ? used : with;
		boolean found = false;
		for (int i : DARTS) {
			if (i == darts.getId()) {
				found = true;
				break;
			}
		}
		if (!found)
			return false;
		if (darts.getAmount() > 16383)
			 darts = new Item(darts.getId(), 16383);
		if (player.getBlowpipeDarts() == null /*|| player.getBlowpipeDarts().getId() != darts.getId()*/) {
			player.setBlowpipeDarts(darts);
		} else {
			if (player.getBlowpipeDarts().getId() != darts.getId()) {
				player.getPackets().sendGameMessage("Unload the blowpipe before you add different darts.");
				return true;
			}
			if (player.getBlowpipeDarts().getAmount() + darts.getAmount() > 16383) {
				int amount = 16383 - player.getBlowpipeDarts().getAmount();
				if (amount <= 0)
					return true;
				darts = new Item(darts.getId(), 16383 - player.getBlowpipeDarts().getAmount());
			}
			player.getBlowpipeDarts().setAmount(player.getBlowpipeDarts().getAmount() + darts.getAmount());
		}
		player.getInventory().deleteItem(darts.getId(), darts.getAmount());
		player.getCombatDefinitions().refreshBonuses();
		player.getPackets().sendGameMessage("You load the blowpipe.", true);
		return true;
	}
	public static boolean chargeInfernalBlowpipe(Player player, Item with, Item used) {
		if (with.getId() != 25502 && used.getId() != 25502)
			return false;
		Item darts = with.getId() == 25502 ? used : with;
		boolean found = false;
		for (int i : DARTS) {
			if (i == darts.getId()) {
				found = true;
				break;
			}
		}
		if (!found)
			return false;
		if (darts.getAmount() > 16383)
			darts = new Item(darts.getId(), 16383);
		if (player.getInfernalBlowpipeDarts() == null /*|| player.getInfernalBlowpipeDarts().getId() != darts.getId()*/) {
			player.setInfernalBlowpipeDarts(darts);
		} else {
			if (player.getInfernalBlowpipeDarts().getId() != darts.getId()) {
				player.getPackets().sendGameMessage("Unload the blowpipe before you add different darts.");
				return true;
			}
			if (player.getInfernalBlowpipeDarts().getAmount() + darts.getAmount() > 16383) {
				int amount = 16383 - player.getInfernalBlowpipeDarts().getAmount();
				if (amount <= 0)
					return true;
				darts = new Item(darts.getId(), 16383 - player.getInfernalBlowpipeDarts().getAmount());
			}
			player.getInfernalBlowpipeDarts().setAmount(player.getInfernalBlowpipeDarts().getAmount() + darts.getAmount());
		}
		player.getInventory().deleteItem(darts.getId(), darts.getAmount());
		player.getCombatDefinitions().refreshBonuses();
		player.getPackets().sendGameMessage("You load the infernal blowpipe.", true);
		return true;
	}
	public static final int[] DARTS = new int[] {806, 807, 808, 809, 810, 811, 11230};
	
	public static boolean hasBalista(Player player) {
		int weaponID = player.getEquipment().getWeaponId();
		return weaponID == 49478 || weaponID == 49481;
	}
	
	public static boolean isJavelin(int id) {
		return (id >= 825 && id <= 836) || (id >= 5642 && id <= 5653)
				|| id >= 49484 && id <= 49490;
	}

	public static boolean hasObsidian(Player player) {
		int weaponID = player.getEquipment().getWeaponId();
		return weaponID >= 6522 && weaponID <= 6528 && player.getEquipment().getHatId() == 51298
				&& player.getEquipment().getChestId() == 51301
				&& player.getEquipment().getLegsId() == 51304
				;
	}

	public static void useSnowBall(Player player, Entity target) {
		player.stopAll();
		player.faceEntity(target);
		player.getEquipment().removeAmmo(10501, -1);
		World.sendProjectile(player, target, 1209, 32, 20, 40, 50, 16, 0);
		player.setNextAnimation(new Animation(7530));
		player.lock(2);
	}
}
