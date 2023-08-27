package com.rs.game.player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.item.Item;
import com.rs.game.minigames.stealingcreation.StealingCreationController;
import com.rs.game.player.actions.PlayerCombat;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.controllers.DTControler;
import com.rs.utils.WeaponTypesLoader;
import com.rs.utils.WeaponTypesLoader.WeaponType;

public final class CombatDefinitions implements Serializable {

	private static final long serialVersionUID = 2102201264836121104L;
	public static final int STAB_ATTACK = 0, SLASH_ATTACK = 1, CRUSH_ATTACK = 2, RANGE_ATTACK = 4, MAGIC_ATTACK = 3;
	public static final int STAB_DEF = 5, SLASH_DEF = 6, CRUSH_DEF = 7, RANGE_DEF = 9, MAGIC_DEF = 8, SUMMONING_DEF = 10;
	public static final int STRENGTH_BONUS = 14, RANGED_STR_BONUS = 15, MAGIC_DAMAGE = 17, PRAYER_BONUS = 16;
	public static final int ABSORVE_MELEE_BONUS = 11, ABSORVE_RANGE_BONUS = 13, ABSORVE_MAGE_BONUS = 12;

	public static final int SHARED = -1;
	private transient Player player;
	private transient boolean usingSpecialAttack;
	private transient double[] bonuses;

	// saving stuff

	
	private Map<Integer, Integer> savedAttackStyles;
	private Map<Integer, Integer> savedAutoCastSpells;
	
	@Deprecated
	private byte attackStyle; //use savedAttackStyles instead
	
	private byte specialAttackPercentage;
	private boolean autoRelatie;
	private byte sortSpellBook;
	private boolean showCombatSpells;
	private boolean showSkillSpells;
	private boolean showMiscallaneousSpells;
	private boolean showTeleportSpells;
	private boolean defensiveCasting;
	private transient boolean instantAttack;
	private transient boolean dungeonneringSpellBook;
	private byte spellBook;
	@Deprecated
	private byte autoCastSpell;

	public int getSpellId() {
		Integer tempCastSpell = (Integer) player.getTemporaryAttributtes().get("tempCastSpell");
		if (tempCastSpell != null)
			return tempCastSpell + 256;
		return getAutoCastSpell();
	}

	public void resetSpells(boolean removeAutoSpell) {
		player.getTemporaryAttributtes().remove("tempCastSpell");
		if (removeAutoSpell) 
			setAutoCastSpell(0);
		
	}

	public void setAutoCastSpell(int id) {
		setAutoCastSpellN(id);
		refreshAutoCastSpell();
	}

	public void refreshAutoCastSpell() {
		refreshAttackStyle();
		player.getVarsManager().sendVar(108, getSpellAutoCastConfigValue());
	}

	public int getSpellAutoCastConfigValue() {
		if (dungeonneringSpellBook) {
			switch (getAutoCastSpell()) {
			case 25:
				return 103;
			case 27:
				return 105;
			case 28:
				return 107;
			case 30:
				return 109;
			case 32: // air bolt
				return 111;
			case 36: // water bolt
				return 113;
			case 37: // earth bolt
				return 115;
			case 41: // fire bolt
				return 117;
			case 42: // air blast
				return 119;
			case 43: // water blast
				return 121;
			case 45: // earth blast
				return 123;
			case 47: // fire blast
				return 125;
			case 48: // air wave
				return 127;
			case 49: // water wave
				return 129;
			case 54: // earth wave
				return 131;
			case 58: // fire wave
				return 133;
			case 61:// air surge
				return 135;
			case 62:// water surge
				return 137;
			case 63:// earth surge
				return 139;
			case 67:// fire surge
				return 141;
			default:
				return 0;
			}
		}
		if (spellBook == 0) {
			switch (getAutoCastSpell()) {
			case 98:
				return 143;
			case 25:
				return 3;
			case 28:
				return 5;
			case 30:
				return 7;
			case 32:
				return 9;
			case 34:
				return 11; // air bolt
			case 39:
				return 13;// water bolt
			case 42:
				return 15;// earth bolt
			case 45:
				return 17; // fire bolt
			case 49:
				return 19;// air blast
			case 47:
				return 35;// crmuble dead
			case 56:
				return 37;// magic dart
			case 54:
				return 45;// iban's blast
			case 52:
				return 21;// water blast
			case 58:
				return 23;// earth blast
			case 63:
				return 25;// fire blast
			case 66: // Saradomin Strike
				return 41;
			case 67:// Claws of Guthix
				return 39;
			case 68:// Flames of Zammorak
				return 43;
			case 70:
				return 27;// air wave
			case 73:
				return 29;// water wave
			case 77:
				return 31;// earth wave
			case 80:
				return 33;// fire wave
			case 84:
				return 47;
			case 87:
				return 49;
			case 89:
				return 51;
			case 91:
				return 53;
			case 99:
				return 145;
			default:
				return 0;
			}
		} else if (spellBook == 1) {
			switch (getAutoCastSpell()) {
			case 28:
				return 63;
			case 32:
				return 65;
			case 24:
				return 67;
			case 20:
				return 69;
			case 30:
				return 71;
			case 34:
				return 73;
			case 26:
				return 75;
			case 22:
				return 77;
			case 29:
				return 79;
			case 33:
				return 81;
			case 25:
				return 83;
			case 21:
				return 85;
			case 31:
				return 87;
			case 35:
				return 89;
			case 27:
				return 91;
			case 23:
				return 93;
			case 36:
				return 95;
			case 37:
				return 99;
			case 38:
				return 97;
			case 39:
				return 101;
			default:
				return 0;
			}
		} else {
			return 0;
		}
	}

	public CombatDefinitions() {
		specialAttackPercentage = 100;
		autoRelatie = true;
		showCombatSpells = true;
		showSkillSpells = true;
		showMiscallaneousSpells = true;
		showTeleportSpells = true;
		savedAttackStyles = new HashMap<Integer, Integer>();
		savedAutoCastSpells  = new HashMap<Integer, Integer>();
	}

	public void setSpellBook(int id) {
		if (id == 3)
			dungeonneringSpellBook = true;
		else
			spellBook = (byte) id;
		refreshSpellBookScrollBar_DefCast();
		player.getInterfaceManager().sendMagicBook();
	}

	public void refreshSpellBookScrollBar_DefCast() {
		player.getVarsManager().sendVar(439, (dungeonneringSpellBook ? 3 : spellBook) + (!defensiveCasting ? 0 : 1 << 8));
	}

	public int getSpellBook() {
		if (dungeonneringSpellBook)
			return 950; // dung book
		else {
			if (spellBook == 0)
				return 192; // normal
			else if (spellBook == 1)
				return 193; // ancients
			else
				return 430; // lunar
		}
	}
	
	public int getSpellBookID() {
		return spellBook;
	}

	public void switchShowCombatSpells() {
		showCombatSpells = !showCombatSpells;
		refreshSpellBook();
	}

	public void switchShowSkillSpells() {
		showSkillSpells = !showSkillSpells;
		refreshSpellBook();
	}

	public void switchShowMiscallaneousSpells() {
		showMiscallaneousSpells = !showMiscallaneousSpells;
		refreshSpellBook();
	}

	public void switchShowTeleportSkillSpells() {
		showTeleportSpells = !showTeleportSpells;
		refreshSpellBook();
	}

	public void switchDefensiveCasting() {
		defensiveCasting = !defensiveCasting;
		refreshSpellBookScrollBar_DefCast();
	}

	public void setSortSpellBook(int sortId) {
		this.sortSpellBook = (byte) sortId;
		refreshSpellBook();
	}

	public boolean isDefensiveCasting() {
		return defensiveCasting;
	}

	public void refreshSpellBook() {
		if (dungeonneringSpellBook)
			player.getVarsManager().sendVar(1376, sortSpellBook << 3 | (showCombatSpells ? 0 : 1 << 16) | (showTeleportSpells ? 0 : 1 << 17));
		else if (spellBook == 0) {
			player.getVarsManager().sendVar(1376, sortSpellBook | (showCombatSpells ? 0 : 1 << 9) | (showSkillSpells ? 0 : 1 << 10) | (showMiscallaneousSpells ? 0 : 1 << 11) | (showTeleportSpells ? 0 : 1 << 12));
		} else if (spellBook == 1) {
			player.getVarsManager().sendVar(1376, sortSpellBook << 3 | (showCombatSpells ? 0 : 1 << 16) | (showTeleportSpells ? 0 : 1 << 17));
		} else if (spellBook == 2) {
			player.getVarsManager().sendVar(1376, sortSpellBook << 6 | (showCombatSpells ? 0 : 1 << 13) | (showMiscallaneousSpells ? 0 : 1 << 14) | (showTeleportSpells ? 0 : 1 << 15));
		}
	}

	public static final int getMeleeDefenceBonus(int bonusId) {
		if (bonusId == STAB_ATTACK)
			return STAB_DEF;
		if (bonusId == SLASH_ATTACK)
			return SLASH_DEF;
		return CRUSH_DEF;
	}

	public static final int getMeleeBonusStyle(int weaponId, int attackStyle) {
		if (weaponId != -1) {
			if (weaponId == -2) {
				return CRUSH_ATTACK;
			}
			String weaponName = ItemConfig.forID(weaponId).getName().toLowerCase();
			if (weaponName.contains("tentacle") || weaponName.contains("whip"))
				return SLASH_ATTACK;
			if (weaponName.contains("scythe") || weaponId == 25540 || weaponId == 25618) {
				switch (attackStyle) {
				case 1:
					return STAB_ATTACK;
				case 2:
					return CRUSH_ATTACK;
				default:
					return SLASH_ATTACK;
				}
			}
			if (weaponName.contains("staff of light")) {
				switch (attackStyle) {
				case 0:
					return STAB_ATTACK;
				case 1:
					return SLASH_ATTACK;
				default:
					return CRUSH_ATTACK;
				}
			}
			if (weaponName.contains("bludgeon") || weaponName.contains("trident") || weaponName.contains("mindspike") || weaponName.contains("staff") || weaponId == 41708 || weaponName.contains("granite mace") || weaponName.contains("hammer") || weaponName.contains("tzhaar-ket-em") || weaponName.contains("tzhaar-ket-om") || weaponName.contains("maul") || weaponName.endsWith("bulwark"))
				return CRUSH_ATTACK;
			if (weaponName.contains("balmung") || weaponName.contains("godsword") || weaponName.contains("greataxe") || weaponName.contains("2h sword") || weaponName.contains("saradomin sword") || weaponName.contains("blessed sword") || weaponName.contains("battleaxe")) {
				switch (attackStyle) {
				case 2:
					return CRUSH_ATTACK;
				default:
					return SLASH_ATTACK;
				}
			}
			if (weaponName.endsWith(" axe") || weaponName.contains("blade ") ||  weaponName.contains("scimitar") || weaponName.contains("sabre") || weaponName.contains("hatchet") || weaponName.contains("claws") || weaponName.contains("fists") || weaponName.contains("longsword")) {
				switch (attackStyle) {
				case 2:
					return STAB_ATTACK;
				default:
					return SLASH_ATTACK;
				}
			}
			if (weaponName.contains("mace") || weaponName.contains("anchor") || weaponName.contains("annihilation")) {
				switch (attackStyle) {
				case 2:
					return STAB_ATTACK;
				default:
					return CRUSH_ATTACK;
				}
			}
			if (weaponName.contains("halberd") || weaponName.contains("polearm")) {
				switch (attackStyle) {
				case 1:
					return SLASH_ATTACK;
				default:
					return STAB_ATTACK;
				}
			}
			if (weaponName.contains("lance") || weaponName.contains("hasta") || weaponName.contains("spear")) {
				switch (attackStyle) {
				case 1:
					return SLASH_ATTACK;
				case 2:
					return CRUSH_ATTACK;
				default:
					return STAB_ATTACK;
				}
			}
			if (weaponName.contains("pickaxe")) {
				switch (attackStyle) {
				case 2:
					return CRUSH_ATTACK;
				default:
					return STAB_ATTACK;
				}
			}

			if (weaponName.contains("dagger") || weaponName.contains("rapier") || weaponName.contains(" sword") || weaponName.contains("harpoon")) {
				switch (attackStyle) {
				case 2:
					return SLASH_ATTACK;
				default:
					return STAB_ATTACK;
				}
			}

		}
		switch (weaponId) {
		default:
			return CRUSH_ATTACK;
		}
	}

	public static final int getXpStyle(int weaponId, int attackStyle) {
		if (weaponId != -1 && weaponId != -2) {
			String weaponName = ItemConfig.forID(weaponId).getName().toLowerCase();
			if (weaponName.contains("tentacle") || weaponName.contains("whip")) {
				switch (attackStyle) {
				case 0:
					return Skills.ATTACK;
				case 1:
					return SHARED;
				case 2:
				default:
					return Skills.DEFENCE;
				}
			}
			if (weaponName.contains("halberd") || weaponName.contains("polearm")) {
				switch (attackStyle) {
				case 0:
					return SHARED;
				case 1:
					return Skills.STRENGTH;
				case 2:
				default:
					return Skills.DEFENCE;
				}
			}
			if (weaponName.contains("lance") || weaponName.contains("hasta") || weaponName.contains("spear")) {
				switch (attackStyle) {
				case 3:
					return Skills.DEFENCE;
				default:
					return SHARED;
				}
			}
			if (weaponName.contains("bludgeon"))
				return Skills.STRENGTH;
			if (weaponName.contains("carrot") || weaponName.contains("mindspike") || weaponName.contains("staff") || weaponId == 25699 || weaponName.contains("granite mace") || weaponName.contains("hammer") || weaponName.contains("tzhaar-ket-em") || weaponName.contains("tzhaar-ket-om") || weaponName.contains("maul") || weaponName.endsWith("bulwark")) {
				switch (attackStyle) {
				case 0:
					return Skills.ATTACK;
				case 1:
					return Skills.STRENGTH;
				case 2:
				default:
					return Skills.DEFENCE;
				}
			}
			if (weaponId == 25540 || weaponName.contains("scythe") || weaponName.contains("dagger") || weaponName.contains("godsword") || weaponName.contains("sword") || weaponName.contains("2h") || weaponName.contains("harpoon")) {
				switch (attackStyle) {
				case 0:
					return Skills.ATTACK;
				case 1:
					return Skills.STRENGTH;
				case 2:
					return Skills.STRENGTH;
				case 3:
				default:
					return Skills.DEFENCE;
				}
			}
		}
		switch (weaponId) {
		case -1:
		case -2:
			switch (attackStyle) {
			case 0:
				return Skills.ATTACK;
			case 1:
				return Skills.STRENGTH;
			case 2:
			default:
				return Skills.DEFENCE;
			}
		default:
			switch (attackStyle) {
			case 0:
				return Skills.ATTACK;
			case 1:
				return Skills.STRENGTH;
			case 2:
				return SHARED;
			case 3:
			default:
				return Skills.DEFENCE;
			}
		}
	}

	public void setPlayer(Player player) {
		this.player = player;
		bonuses = new double[18];
		if (this.savedAttackStyles == null) {//TODO remove
			savedAttackStyles = new HashMap<Integer, Integer>();
			setAttackStyleN(attackStyle);
		}
		if (this.savedAutoCastSpells == null) {
			savedAutoCastSpells = new HashMap<Integer, Integer>();
			setAutoCastSpellN(autoCastSpell);
		}
	}

	public double[] getBonuses() {
		return bonuses;
	}

	public void refreshBonuses() {
		bonuses = new double[18];
		int weapon = player.getEquipment().getWeaponId();
		int weaponRangedStr = weapon == -1 ? 0 : ItemConfig.forID(weapon).getRangedStrBonus();
		int slot = -1;
		for (Item item : player.getEquipment().getItems().getItems()) {
			slot++;
			if (item == null)
				continue;
			// dominion weapons work only in tower
			if ((item.getId() >= 22346 && item.getId() <= 22348 && !(player.getControlerManager().getControler() instanceof DTControler)) || (item.getId() >= 25434 && item.getId() <= 25439 && Settings.SPAWN_WORLD))
				continue;
			if (item.getDefinitions().isDungItem() && !player.getDungManager().isInside()) //dung items no stats outside dung
				continue;
			if (item.getDefinitions().isSCItem() && !(player.getControlerManager().getControler() instanceof StealingCreationController)) //sc items no stats outside instance
				continue;
			if (item.getId() == 11283 || item.getId() == 52002 || item.getId() == 51633) {
				int charges = player.getCharges().getCharges(item.getId());
				bonuses[STAB_DEF] += charges;
				bonuses[SLASH_DEF] += charges;
				bonuses[CRUSH_DEF] += charges;
				bonuses[RANGE_DEF] += charges;
			}
			
			ItemConfig defs = item.getDefinitions();
			bonuses[STAB_ATTACK] += defs.getStabAttack();
			bonuses[SLASH_ATTACK] += defs.getSlashAttack();
			bonuses[CRUSH_ATTACK] += defs.getCrushAttack();
			bonuses[MAGIC_ATTACK] += defs.getMagicAttack();
			bonuses[RANGE_ATTACK] += defs.getRangeAttack();
			bonuses[STAB_DEF] += defs.getStabDef();
			bonuses[SLASH_DEF] += defs.getSlashDef();
			bonuses[CRUSH_DEF] += defs.getCrushDef();
			bonuses[MAGIC_DEF] += defs.getMagicDef();
			bonuses[RANGE_DEF] += defs.getRangeDef();
			bonuses[SUMMONING_DEF] += defs.getSummoningDef();
			bonuses[ABSORVE_MELEE_BONUS] += 0;//defs.getAbsorveMeleeBonus();
			bonuses[ABSORVE_MAGE_BONUS] += 0;//defs.getAbsorveMageBonus();
			bonuses[ABSORVE_RANGE_BONUS] += 0;//defs.getAbsorveRangeBonus();
			bonuses[STRENGTH_BONUS] += defs.getStrengthBonus();
			if (slot != Equipment.SLOT_ARROWS || (weaponRangedStr == 0 && weapon != 13720 && weapon != 25202) || weapon == 25592 || weapon == 25617 || weapon == 25609 || weapon == 25575 || weapon == 25533 || weapon == 25662 || weapon == 49481 || weapon == 50997 || weapon == 25441|| weapon == 25460 || weapon == 25469 || weapon == 25539 || weapon == 25618
					|| weapon == 25546|| weapon == 25639 || weapon == 25629) {
				if (item.getId() == 19152 || item.getId() == 19157 || item.getId() == 19162) {
					int rangeLevel = player.getSkills().getLevel(Skills.RANGE);
					bonuses[RANGED_STR_BONUS]--;
					bonuses[RANGED_STR_BONUS] += rangeLevel >= 70 ? 50 : rangeLevel < 11 ? 0 : rangeLevel - 10;
				}
				bonuses[RANGED_STR_BONUS] += defs.getRangedStrBonus();
				if ((item.getId() == 42926) && player.getBlowpipeDarts() != null)
					bonuses[RANGED_STR_BONUS] += player.getBlowpipeDarts().getDefinitions().getRangedStrBonus();
				if ((item.getId() == 25502) && player.getInfernalBlowpipeDarts() != null)
					bonuses[RANGED_STR_BONUS] += player.getInfernalBlowpipeDarts().getDefinitions().getRangedStrBonus();
			}
			bonuses[PRAYER_BONUS] += defs.getPrayerBonus();
			bonuses[MAGIC_DAMAGE] += defs.getMagicDamage();
		}
		if (PlayerCombat.fullVoidEquipped(player, 11663, 11674) && PlayerCombat.hasEliteVoid(player)) 
			bonuses[CombatDefinitions.MAGIC_DAMAGE] += 2.5;
		//else if (player.getEquipment().getWeaponId() == 51006 && player.getEquipment().getLegsId() == 51024 && player.getEquipment().getChestId() == 51021 &&player.getEquipment().getHatId() == 51018)
		//	bonuses[CombatDefinitions.MAGIC_DAMAGE] += 10;
		if (player.getEquipment().getWeaponId() == 52324 && player.getEquipment().getShieldId() == 52322)
			bonuses[CombatDefinitions.STRENGTH_BONUS] += 15;
		if (player.getPet() != null
				&& player.getPet().getId() == Pets.BABY_MOLE.getBabyNpcId())
			bonuses[CombatDefinitions.STRENGTH_BONUS] += 1;
		else if (player.getPet() != null
				&& (player.getPet().getId() == Pets.JAL_NIB_REK.getBabyNpcId()
				|| player.getPet().getId() == Pets.TZREK_ZUK.getBabyNpcId())) {

			bonuses[CombatDefinitions.STAB_ATTACK] += 1;
			bonuses[CombatDefinitions.SLASH_ATTACK] += 1;
			bonuses[CombatDefinitions.CRUSH_ATTACK] += 1;
			bonuses[CombatDefinitions.RANGE_ATTACK] += 1;
			bonuses[CombatDefinitions.MAGIC_ATTACK] += 1;

			bonuses[CombatDefinitions.STAB_DEF] += 1;
			bonuses[CombatDefinitions.SLASH_DEF] += 1;
			bonuses[CombatDefinitions.CRUSH_DEF] += 1;
			bonuses[CombatDefinitions.RANGE_DEF] += 1;
			bonuses[CombatDefinitions.MAGIC_DEF] += 1;
			
		} else if (player.getPet() != null
				&& player.getPet().getId() == Pets.SHRIMPY.getBabyNpcId()) {
			bonuses[CombatDefinitions.STAB_DEF] += 1;
			bonuses[CombatDefinitions.SLASH_DEF] += 1;
			bonuses[CombatDefinitions.CRUSH_DEF] += 1;
			bonuses[CombatDefinitions.RANGE_DEF] += 1;
			bonuses[CombatDefinitions.MAGIC_DEF] += 1;
		} else if (player.getPet() != null
				&& (player.getPet().getId() == Pets.KALHPITE_PRINCESS.getBabyNpcId()
				||  player.getPet().getId() == Pets.KALHPITE_PRINCESS_2.getBabyNpcId())) {
			bonuses[CombatDefinitions.STAB_ATTACK] += 1;
			bonuses[CombatDefinitions.SLASH_ATTACK] += 1;
			bonuses[CombatDefinitions.CRUSH_ATTACK] += 1;
			bonuses[CombatDefinitions.RANGE_ATTACK] += 1;
			bonuses[CombatDefinitions.MAGIC_ATTACK] += 1;
		} else if (player.getPet() != null
				&& (player.getPet().getId() == Pets.OLMLET.getBabyNpcId())) {
			bonuses[CombatDefinitions.STAB_ATTACK] += 2;
			bonuses[CombatDefinitions.SLASH_ATTACK] += 2;
			bonuses[CombatDefinitions.CRUSH_ATTACK] += 2;
			bonuses[CombatDefinitions.RANGE_ATTACK] += 2;
			bonuses[CombatDefinitions.MAGIC_ATTACK] += 2;
		} else if (player.getPet() != null
				&& (player.getPet().getId() == Pets.PET_DAGANNOTH_PRIME.getBabyNpcId())) 
			bonuses[CombatDefinitions.MAGIC_ATTACK] += 3;
		else if (player.getPet() != null
				&& (player.getPet().getId() == Pets.PET_DAGANNOTH_SUPREME.getBabyNpcId())) 
			bonuses[CombatDefinitions.RANGE_ATTACK] += 3;
		else if (player.getPet() != null
				&& (player.getPet().getId() == Pets.PET_DAGANNOTH_REX.getBabyNpcId())) { 
			bonuses[CombatDefinitions.STAB_ATTACK] += 3;
			bonuses[CombatDefinitions.SLASH_ATTACK] += 3;
			bonuses[CombatDefinitions.CRUSH_ATTACK] += 3;
		} else if (player.getPet() != null
				&& player.getPet().getId() == Pets.PET_GENERAL_GRAARDOR.getBabyNpcId())
			bonuses[CombatDefinitions.STRENGTH_BONUS] += 2;
		else if (player.getPet() != null
				&& player.getPet().getId() == Pets.PET_KRIL_TSUTAROTH.getBabyNpcId()) {
			bonuses[CombatDefinitions.STAB_ATTACK] += 3;
			bonuses[CombatDefinitions.SLASH_ATTACK] += 3;
			bonuses[CombatDefinitions.CRUSH_ATTACK] += 3;
			bonuses[CombatDefinitions.PRAYER_BONUS] += 1;
		}else if (player.getPet() != null
				&& player.getPet().getId() == Pets.PET_KREE_ARRA.getBabyNpcId()) {
			bonuses[CombatDefinitions.RANGE_ATTACK] += 3;
			bonuses[CombatDefinitions.RANGED_STR_BONUS] += 1;
			bonuses[CombatDefinitions.PRAYER_BONUS] += 1;
		}else if (player.getPet() != null
				&& player.getPet().getId() == Pets.PET_ZILYANA.getBabyNpcId()) {
			bonuses[CombatDefinitions.MAGIC_ATTACK] += 3;
			bonuses[CombatDefinitions.PRAYER_BONUS] += 1;
		}else if (player.getPet() != null
				&& player.getPet().getId() == Pets.LILZIK.getBabyNpcId()) {
			bonuses[CombatDefinitions.STRENGTH_BONUS] += 2;
			bonuses[CombatDefinitions.RANGED_STR_BONUS] += 1;
			bonuses[CombatDefinitions.PRAYER_BONUS] += 2;
		}else if (player.getPet() != null
				&& (player.getPet().getId() == Pets.NEXTERMINATOR.getBabyNpcId()
				|| player.getPet().getId() == Pets.LITTLE_NIGHTMARE.getBabyNpcId())) {
			bonuses[CombatDefinitions.STRENGTH_BONUS] += 2;
			bonuses[CombatDefinitions.STAB_ATTACK] += 3;
			bonuses[CombatDefinitions.SLASH_ATTACK] += 3;
			bonuses[CombatDefinitions.CRUSH_ATTACK] += 3;
			bonuses[CombatDefinitions.PRAYER_BONUS] += 1;
			bonuses[CombatDefinitions.RANGE_ATTACK] += 3;
			bonuses[CombatDefinitions.RANGED_STR_BONUS] += 1;
			bonuses[CombatDefinitions.MAGIC_ATTACK] += 3;
		}else if (player.getPet() != null
				&& player.getPet().getId() == Pets.QBD.getBabyNpcId()) 
			bonuses[CombatDefinitions.RANGED_STR_BONUS] += 2;
		else if (player.getPet() != null
				&& player.getPet().getId() == Pets.AHRIM.getBabyNpcId()) 
			bonuses[CombatDefinitions.MAGIC_ATTACK] += 1;
		else if (player.getPet() != null
				&& player.getPet().getId() == Pets.DHAROK.getBabyNpcId()) 
			bonuses[CombatDefinitions.STRENGTH_BONUS] += 1;
		else if (player.getPet() != null
				&& player.getPet().getId() == Pets.KARIL.getBabyNpcId()) 
			bonuses[CombatDefinitions.RANGE_ATTACK] += 1;
		else if (player.getPet() != null
				&& player.getPet().getId() == Pets.TORAG.getBabyNpcId()) {
			bonuses[CombatDefinitions.STAB_DEF] += 1;
			bonuses[CombatDefinitions.SLASH_DEF] += 1;
			bonuses[CombatDefinitions.CRUSH_DEF] += 1;
			bonuses[CombatDefinitions.RANGE_DEF] += 1;
		}	else if (player.getPet() != null
				&& player.getPet().getId() == Pets.VERAC.getBabyNpcId()) 
			bonuses[CombatDefinitions.PRAYER_BONUS] += 1;
		
	}

	public void resetSpecialAttack() {
		desecreaseSpecialAttack(0);
		specialAttackPercentage = 100;
		refreshSpecialAttackPercentage();
	}

	public void setSpecialAttack(int special) {
		desecreaseSpecialAttack(0);
		specialAttackPercentage = (byte) special;
		refreshSpecialAttackPercentage();
	}

	public void restoreSpecialAttack() {
		if (player.getFamiliar() != null)
			player.getFamiliar().restoreSpecialAttack(15);
		if (specialAttackPercentage == 100)
			return;
		restoreSpecialAttack(10);
		if (specialAttackPercentage == 100 || specialAttackPercentage == 50)
			player.getPackets().sendGameMessage("<col=00FF00>Your special attack energy is now " + specialAttackPercentage + "%.");
	}

	public void restoreSpecialAttack(int percentage) {
		if (specialAttackPercentage >= 100 || player.getInterfaceManager().containsScreenInter())
			return;
		specialAttackPercentage += specialAttackPercentage > (100 - percentage) ? 100 - specialAttackPercentage : percentage;
		refreshSpecialAttackPercentage();
	}

	public void init() {
		refreshUsingSpecialAttack();
		refreshSpecialAttackPercentage();
		refreshAutoRelatie();
		refreshAttackStyle();
		refreshSpellBook();
		refreshAutoCastSpell();
		refreshSpellBookScrollBar_DefCast();
	}

	public void checkAttackStyle() {
		/*if (autoCastSpell == 0)
			setAttackStyle(getAttackStyle());*/
		refreshAttackStyle();
		refreshAutoCastSpell();
	}

	public void setAttackStyle(int style) {
		//int maxSize = 3;
		//int weaponId = player.getEquipment().getWeaponId();
		/*String name = weaponId == -1 ? "" : ItemConfig.forID(weaponId).getName().toLowerCase();
		if (weaponId == -1 || getType() == Combat.RANGE_TYPE || name.contains("tentacle") || name.contains("whip") || name.contains("halberd") || name.contains("polearm"))
			maxSize = 2;
		if (style > maxSize)
			style = maxSize;*/
		int attackStyle = this.getAttackStyle();
		if (style != attackStyle) {
			//attackStyle = (byte) style;
			setAttackStyleN(style);
			if (getAutoCastSpell() > 1)
				resetSpells(true);
			else
				refreshAttackStyle();
		} else if (getAutoCastSpell() > 1)
			resetSpells(true);
	}

	public void refreshAttackStyle() {
		player.getVarsManager().sendVar(43, getAutoCastSpell() > 0 ? 4 : getAttackStyle());
	}

	public void sendUnlockAttackStylesButtons() {
		for (int componentId = 7; componentId <= 10; componentId++)
			player.getPackets().sendUnlockIComponentOptionSlots(884, componentId, -1, 0, 0);
	}

	public void switchUsingSpecialAttack() {
		usingSpecialAttack = !usingSpecialAttack;
		refreshUsingSpecialAttack();
	}

	public void desecreaseSpecialAttack(int ammount) {
		usingSpecialAttack = false;
		refreshUsingSpecialAttack();
		if (ammount > 0) {
			specialAttackPercentage -= ammount;
			refreshSpecialAttackPercentage();
		}
	}

	public boolean hasRingOfVigour() {
		return player.getEquipment().getRingId() == 19669 || player.getEquipment().getRingId() == 25488  || player.getEquipment().getRingId() == 25741;
	}

	public int getSpecialAttackPercentage() {
		return specialAttackPercentage;
	}

	public void refreshUsingSpecialAttack() {
		player.getVarsManager().sendVar(301, usingSpecialAttack ? 1 : 0);
	}

	public void refreshSpecialAttackPercentage() {
		player.getVarsManager().sendVar(300, specialAttackPercentage * 10);
	}

	public void switchAutoRelatie() {
		autoRelatie = !autoRelatie;
		refreshAutoRelatie();
	}

	public void refreshAutoRelatie() {
		player.getVarsManager().sendVar(172, autoRelatie ? 0 : 1);
	}

	public boolean isUsingSpecialAttack() {
		return usingSpecialAttack;
	}

	public int getAttackStyle() {
		
		int weap = ItemConfig.forID(player.getEquipment().getWeaponId()).getAttackStyle();
		Integer style = savedAttackStyles.get(weap);
		
		return style == null ? 0 : style;//attackStyle;
	}
	
	private void setAttackStyleN(int style) {
		int weap = ItemConfig.forID(player.getEquipment().getWeaponId()).getAttackStyle();
		if (style == 0)
			savedAttackStyles.remove(weap);
		else	
			savedAttackStyles.put(weap, style);
	}
	
	public int getAutoCastSpell() {
		
		int weap = ItemConfig.forID(player.getEquipment().getWeaponId()).getAttackStyle();
		Integer style = savedAutoCastSpells.get(weap);
		
		return style == null ? 0 : style;//attackStyle;
	}
	
	private void setAutoCastSpellN(int style) {
		int weap = ItemConfig.forID(player.getEquipment().getWeaponId()).getAttackStyle();
		if (style == 0)
			savedAutoCastSpells.remove(weap);
		else	
			savedAutoCastSpells.put(weap, style);
	}

	public boolean isAutoRelatie() {
		return autoRelatie;
	}

	public void setAutoRelatie(boolean autoRelatie) {
		this.autoRelatie = autoRelatie;
	}

	public boolean isDungeonneringSpellBook() {
		return dungeonneringSpellBook;
	}

	public void removeDungeonneringBook() {
		if (dungeonneringSpellBook) {
			dungeonneringSpellBook = false;
			player.getInterfaceManager().sendMagicBook();
		}
	}

	public boolean isInstantAttack() {
		return instantAttack;
	}

	public void setInstantAttack(boolean instantAttack) {
		this.instantAttack = instantAttack;
	}

	public int getType() {
		Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
		WeaponType type = WeaponTypesLoader.getWeaponDefinition(weapon == null ? -1 : weapon.getId());
		int spell = getSpellId();
		if (spell > 0)
			return Combat.MAGIC_TYPE;
		return type.getType();
	}
	
	public boolean isDistancedStyle() {
		int type = getType();
		return type == Combat.RANGE_TYPE || type == Combat.MAGIC_TYPE;
	}
}
