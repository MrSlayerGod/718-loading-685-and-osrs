package com.rs.cache.loaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.alex.utils.Constants;
import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.custom.CustomItems;
import com.rs.io.InputStream;
import com.rs.utils.Logger;
import com.rs.utils.OsrsEquipment;
import com.rs.utils.Utils;

@SuppressWarnings("unused")
public final class ItemConfig {

	private static final ConcurrentHashMap<Integer, ItemConfig> itemsDefinitions = new ConcurrentHashMap<Integer, ItemConfig>();

	public int id;
	public boolean loaded;

	public int model;
	public String name;

	// model size information
	public int scale;
	public int pitch;
	public int roll;
	public int translateX;
	public int translateY;

	// extra information
	public int stackable;
	public int value;
	public boolean membersOnly;

	// wearing model information
	public int maleEquip1;
	public int maleOffset;
	public int femaleEquip1;
	public int femaleOffset;
	public int maleEquip2;
	public int femaleEquip2;

	// options
	public String[] groundOptions;
	public String[] inventoryOptions;

	// model information
	public int[] originalModelColors;
	public int[] modifiedModelColors;
	public short[] originalTextureColors;
	public short[] modifiedTextureColors;
	public byte[] unknownArray1;
	public byte[] unknownArray3;
	public int[] unknownArray2;
	// extra information, not used for newer items
	public boolean tradeable;

	public int maleModel3;
	public int femaleModel3;
	public int maleHeadModel1;
	public int femaleHeadModel1;
	public int maleHeadModel2;
	public int femaleHeadModel2;
	public int offsetX;
	public int unknownInt6;
	public int cert;
	public int certTemplate;
	public int[] stackIds;
	public int[] stackAmounts;
	public int scaleX;
	public int scaleY;
	public int scaleZ;
	public int shadow;
	public int lightness;
	public int team;
	public int lendId;
	public int lendTemplateId;

	public int lastManStandingId;
	public int lastManStandingTemplateId;
	public boolean lastManStanding;

	public int unknownInt12;
	public int unknownInt13;
	public int unknownInt14;
	public int unknownInt15;
	public int unknownInt16;
	public int unknownInt17;
	public int unknownInt18;
	public int unknownInt19;
	public int unknownInt20;
	public int unknownInt21;
	public int unknownInt22;
	public int unknownInt23;
	public int equipSlot;
	public int equipType;

	// extra added
	public boolean noted;
	public boolean lended;

	public HashMap<Integer, Object> clientScriptData;
	public HashMap<Integer, Integer> itemRequiriments;
	public int[] unknownArray5;
	public int[] unknownArray4;
	public byte[] unknownArray6;

	public byte[] data;
	
	private int hp;

	public static final ItemConfig forID(int itemId) {
		ItemConfig def = itemsDefinitions.get(itemId);
		if (def == null)
			itemsDefinitions.put(itemId, def = new ItemConfig(itemId));
		return def;
	}

	public static ItemConfig forName(String n) {
		int bestMatch = Integer.MAX_VALUE;
		ItemConfig bestMatchItem = null;

		for(ItemConfig i : itemsDefinitions.values()) {
			if(i != null && i.name != null) {
				int dist = Utils.calcLevenshteinDistance(n, i.name);
				if(dist < bestMatch) {
					bestMatch = dist;
					bestMatchItem = i;
				}
			}
		}

		return bestMatchItem;
	}

	private void setStackable(boolean stackable) {
		this.stackable = stackable ? 1 : 0;
	}

	private void setName(String name) {
		this.name = name;
	}
	
	public static void main(String[] args) throws IOException {
		Cache.init();
		
		ItemConfig config = forID(23679);
		System.out.println(config.getAttackSpeed());
		System.out.println(config.getRenderAnimId());
		System.out.println(config.clientScriptData);
	}

	public static final void clearItemsDefinitions() {
		itemsDefinitions.clear();
	}

	public ItemConfig(int id) {
		this.id = id;
		setDefaultsVariableValues();
		setDefaultOptions();
		loadItemDefinitions();
	}

	public boolean isLoaded() {
		return loaded;
	}

	public final void loadItemDefinitions() {
		data = Cache.STORE.getIndexes()[Constants.ITEM_DEFINITIONS_INDEX].getFile(getArchiveId(), getFileId());
		if (data != null) {
			// System.out.println("Failed loading Item " + id+".");
		//	return;
			readOpcodeValues(new InputStream(data));
		}
		if (certTemplate != -1)
			toNote();
		if (lendTemplateId != -1)
			toLend();
		if (this.lastManStandingTemplateId != -1)
			lastManStanding = true;
		if (bindTemplateId != -1)
			toBind();
		if (id >= Settings.OSRS_ITEM_OFFSET)
			setEquipmentData();
		CustomItems.modify(this);
		loaded = true;
	}
	
	//osrs
	public void setEquipmentData() {
		if (!containsInventoryOption(1, "wield") && !containsInventoryOption(1, "wear"))
			return;
		equipSlot = OsrsEquipment.getItemSlot(this);
		if (equipSlot == Equipment.SLOT_WEAPON && OsrsEquipment.isTwoHandedWeapon(this))
			equipType = 5;
		else if (equipSlot == Equipment.SLOT_CHEST && /*&& OsrsEquipment.isFullBody(this)*/ (this.maleEquip2 != -1 || this.femaleEquip2 != -1))
			equipType = 6;
		else if (equipSlot == Equipment.SLOT_HAT && OsrsEquipment.isFullHat(this))
			equipType = 8;
	}

	public byte[] getData() {
		return data;
	}

	public void toNote() {
		// ItemDefinitions noteItem; //certTemplateId
		ItemConfig realItem = forID(cert);
		membersOnly = realItem.membersOnly;
		value = realItem.value;
		name = realItem.name;
		stackable = 1;
		noted = true;
		clientScriptData = realItem.clientScriptData;
	}

	public void toBind() {
		// ItemDefinitions lendItem; //lendTemplateId
		ItemConfig realItem = forID(bindId);
		originalModelColors = realItem.originalModelColors;
		maleModel3 = realItem.maleModel3;
		femaleModel3 = realItem.femaleModel3;
		team = realItem.team;
		value = 0;
		membersOnly = realItem.membersOnly;
		name = realItem.name;
		inventoryOptions = new String[5];
		groundOptions = realItem.groundOptions;
		if (realItem.inventoryOptions != null)
			for (int optionIndex = 0; optionIndex < 4; optionIndex++)
				inventoryOptions[optionIndex] = realItem.inventoryOptions[optionIndex];
		inventoryOptions[4] = "Destroy";
		maleEquip1 = realItem.maleEquip1;
		maleEquip2 = realItem.maleEquip2;
		femaleEquip1 = realItem.femaleEquip1;
		femaleEquip2 = realItem.femaleEquip2;
		clientScriptData = realItem.clientScriptData;
		equipSlot = realItem.equipSlot;
		equipType = realItem.equipType;
	}

	public void toLend() {
		// ItemDefinitions lendItem; //lendTemplateId
		ItemConfig realItem = forID(lendId);
		originalModelColors = realItem.originalModelColors;
		maleModel3 = realItem.maleModel3;
		femaleModel3 = realItem.femaleModel3;
		team = realItem.team;
		value = 0;
		membersOnly = realItem.membersOnly;
		name = realItem.name;
		inventoryOptions = new String[5];
		groundOptions = realItem.groundOptions;
		if (realItem.inventoryOptions != null)
			for (int optionIndex = 0; optionIndex < 4; optionIndex++)
				inventoryOptions[optionIndex] = realItem.inventoryOptions[optionIndex];
		inventoryOptions[4] = "Discard";
		maleEquip1 = realItem.maleEquip1;
		maleEquip2 = realItem.maleEquip2;
		femaleEquip1 = realItem.femaleEquip1;
		femaleEquip2 = realItem.femaleEquip2;
		clientScriptData = realItem.clientScriptData;
		equipSlot = realItem.equipSlot;
		equipType = realItem.equipType;
		lended = true;
	}

	public int getArchiveId() {
		return getId() >>> 8;
	}

	public int getFileId() {
		return 0xff & getId();
	}

	public boolean isDestroyItem() {
		if (inventoryOptions == null)
			return false;
		for (String option : inventoryOptions) {
			if (option == null)
				continue;
			if (option.equalsIgnoreCase("destroy"))
				return true;
		}
		return false;
	}

	public boolean isBindItem() {
		if (inventoryOptions == null)
			return false;
		for (String option : inventoryOptions) {
			if (option == null)
				continue;
			if (option.equalsIgnoreCase("bind"))
				return true;
		}
		return false;
	}

	public boolean containsOption(int i, String option) {
		if (inventoryOptions == null || inventoryOptions.length <= i || inventoryOptions[i] == null)
			return false;
		return inventoryOptions[i].equalsIgnoreCase(option);
	}

	public boolean containsOption(String option) {
		if (inventoryOptions == null)
			return false;
		for (String o : inventoryOptions) {
			if (o == null || !o.equals(option))
				continue;
			return true;
		}
		return false;
	}
	
	public boolean isWearItem() {
		return equipSlot != -1;
	}

	public boolean isWearItem(boolean male) {
		if (equipSlot < Equipment.SLOT_RING && (male ? getMaleWornModelId1() == -1 : getFemaleWornModelId1() == -1))
			return false;

		if (!containsInventoryOption(1, "wield") && !containsInventoryOption(1, "wear"))
			return false;
		return equipSlot != -1;
	}

	public boolean containsInventoryOption(int i, String option) {
		if (inventoryOptions == null || inventoryOptions[i] == null || inventoryOptions.length <= i)
			return false;
		return inventoryOptions[i].equalsIgnoreCase(option);
	}

	public int getStageOnDeath() {
		if (clientScriptData == null)
			return 0;
		Object protectedOnDeath = clientScriptData.get(1397);
		if (protectedOnDeath != null && protectedOnDeath instanceof Integer)
			return (Integer) protectedOnDeath;
		return 0;
	}

	public boolean hasSpecialBar() {
		if (clientScriptData == null)
			return false;
		Object specialBar = clientScriptData.get(687);
		if (specialBar != null && specialBar instanceof Integer)
			return (Integer) specialBar == 1;
		return false;
	}

	/**
	 * @author dragonkk(Alex)
	 * Oct 28, 2017
	 * @param i
	 */
	public void setAttackSpeed(int i) {
		this.setData(14, i);
	}
	
	public int getAttackSpeed() {
		if (clientScriptData == null)
			return 4;
		Object attackSpeed = clientScriptData.get(14);
		if (attackSpeed != null && attackSpeed instanceof Integer)
			return (int) attackSpeed;
		return 4;
	}
	
	public void setHPOld() {
		
			if (id == 20135 || id == 20137// torva
			|| id == 20147 || id == 20149// pernix
			|| id == 20159 || id == 20161// virtus
			)
				hp = 66;

		 else 
			if (id == 20139 || id == 20141// torva
			|| id == 20151 || id == 20153// pernix
			|| id == 20163 || id == 20165// virtus
			)
				hp = 200;
		else
			if (id == 20143 || id == 20145// torva
			|| id == 20155 || id == 20157// pernix
			|| id == 20167 || id == 20169// virtus
			)
				hp = 134;
		else if (id == 24450)
				hp = 40;
			else if (id == 24451)
				hp = 75;
			else if (id == 24452)
				hp = 120;
			else if (id == 24453)
				hp = 140;
			else if (id == 24454)
				hp = 155;
			else if (id == 24977// torva
			|| id == 24980// virtus
			|| id == 24974// pernix
			)
				hp = 24;
		else 
			if (id == 24983 || id == 24984// torva
			|| id == 24989 || id == 24990// pernix
			|| id == 24986 || id == 24987 // virtus
			)
				hp = 33;
		
	}
	
	public void setHP(int hp) {
		this.hp = hp;
	}
	
	public int getHP() {
		return hp;
	}
	
	public void setStabAttack(int bonus) {
		setData(0, bonus);
	}

	
	public int getStabAttack() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(0);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}
	
	public void setSlashAttack(int bonus) {
		setData(1, bonus);
	}


	public int getSlashAttack() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(1);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}
	
	public void setCrushAttack(int bonus) {
		setData(2, bonus);
	}

	public int getCrushAttack() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(2);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}
	
	public void setMagicAttack(int bonus) {
		setData(3, bonus);
	}


	public int getMagicAttack() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(3);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public double getDungShopValueMultiplier() {
		if (clientScriptData == null)
			return 1;
		Object value = clientScriptData.get(1046);
		if (value != null && value instanceof Integer)
			return ((Integer) value).doubleValue() / 100;
		return 1;
	}
	
	public void setRangeAttack(int bonus) {
		setData(4, bonus);
	}


	public int getRangeAttack() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(4);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public void setData(int key, int value) {
		if (clientScriptData == null)
			clientScriptData = new HashMap<Integer, Object>();
		clientScriptData.put(key, value);
	}
	
	
	public void setStabDef(int bonus) {
		setData(5, bonus);
	}
	
	public int getStabDef() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(5);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}
	
	public void setSlashDef(int bonus) {
		setData(6, bonus);
	}

	public int getSlashDef() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(6);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}
	
	public void setCrushDef(int bonus) {
		setData(7, bonus);
	}
	
	public void addAtt(double multiplier) {
		setStabAttack((int) (getStabAttack() * multiplier));
		setSlashAttack((int) (getSlashAttack() * multiplier));
		setCrushAttack((int) (getCrushAttack() * multiplier));
		setRangeAttack((int) (getRangeAttack() * multiplier));
		setMagicAttack((int) (getMagicAttack() * multiplier));
	}
	
	public void addHP(double multiplier) {
		hp *= multiplier;
	}
	
	public void addDef(double multiplier) {
		setStabDef((int) (getStabDef() * multiplier));
		setSlashDef((int) (getSlashDef() * multiplier));
		setCrushDef((int) (getCrushDef() * multiplier));
		setRangeDef((int) (getRangeDef() * multiplier));
		setMagicDef((int) (getMagicDef() * multiplier));
	}

	public int getCrushDef() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(7);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}
	
	public void setMagicDef(int bonus) {
		setData(8, bonus);
	}

	public int getMagicDef() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(8);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}
	
	public void setRangeDef(int bonus) {
		setData(9, bonus);
	}

	public int getRangeDef() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(9);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}
	
	public void setSummoningDef(int bonus) {
		setData(417, bonus);
	}

	public int getSummoningDef() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(417);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getAbsorveMeleeBonus() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(967);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getAbsorveMageBonus() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(969);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getAbsorveRangeBonus() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(968);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}
	
	
	public void setStrengthBonus(int bonus) {
		setData(641, bonus * 10);
	}
	
	public int getStrengthBonus() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(641);
		if (value != null && value instanceof Integer)
			return (int) value / 10;
		return 0;
	}

	public void setRangedStrBonus(int bonus) {
		setData(643, bonus * 10);
	}
	
	public int getRangedStrBonus() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(643);
		if (value != null && value instanceof Integer)
			return (int) value / 10;
		return 0;
	}

	public void setMagicDamage(int damage) {
		setData(685, damage);
	}
	
	public int getMagicDamage() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(685);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public void setPrayerBonus(int bonus) {
		setData(11, bonus);
	}
	
	public int getPrayerBonus() {
		if (clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(11);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}
	
	public GeneralRequirementMap getCombatMap() {
		int csMapOpcode = getCSOpcode(686);
		if(csMapOpcode != 0) 
			return GeneralRequirementMap.getMap(csMapOpcode, true);
		return null;
	}
	
	public int getCombatOpcode(int opcode) {
		if (clientScriptData == null)
			return 0;
		Integer value = (Integer) clientScriptData.get(opcode);
		if(value != null)
			return value;
		GeneralRequirementMap map = getCombatMap();
		return map == null ? 0 : map.getIntValue(opcode);
	}

	public void setStance(int id) {
		setData(644, id);
	}
	
	public int getRenderAnimId() {
		if (clientScriptData == null)
			return 1426;
		Object animId = clientScriptData.get(644);
		if (animId != null && animId instanceof Integer)
			return (Integer) animId;
		return 1426;
	}

	public boolean hasShopPriceAttributes() {
		if (clientScriptData == null)
			return false;
		if (clientScriptData.get(258) != null && ((Integer) clientScriptData.get(258)).intValue() == 1)
			return true;
		if (clientScriptData.get(259) != null && ((Integer) clientScriptData.get(259)).intValue() == 1)
			return true;
		return false;
	}

	public int getModelZoom() {
		return scale;
	}

	public int getModelOffset1() {
		return translateX;
	}

	public int getModelOffset2() {
		return translateY;
	}

	public int getQuestId() {
		if (clientScriptData == null)
			return -1;
		Object questId = clientScriptData.get(861);
		if (questId != null && questId instanceof Integer)
			return (Integer) questId;
		return -1;
	}

	public List<Item> getCreateItemRequirements(boolean infusingScroll) {
		if (clientScriptData == null)
			return null;
		List<Item> items = new ArrayList<Item>();
		int requiredId = -1;
		int requiredAmount = -1;
		for (int key : clientScriptData.keySet()) {
			Object value = clientScriptData.get(key);
			if (value instanceof String)
				continue;
			if (key >= 536 && key <= 770) {
				if (key % 2 == 0)
					requiredId = (Integer) value;
				else
					requiredAmount = (Integer) value;
				if (requiredId != -1 && requiredAmount != -1) {
					if (infusingScroll) {
						requiredId = getId();
						requiredAmount = 1;
					}
					if (items.size() == 0 && !infusingScroll)
						items.add(new Item(requiredAmount, 1));
					else
						items.add(new Item(requiredId, requiredAmount));
					requiredId = -1;
					requiredAmount = -1;
					if (infusingScroll) {
						break;
					}
				}
			}
		}
		return items;
	}

	public HashMap<Integer, Object> getClientScriptData() {
		return clientScriptData;
	}

	public HashMap<Integer, Integer> getWearingSkillRequiriments() {
		if (itemRequiriments == null && clientScriptData == null)
			return null;
		if (itemRequiriments == null) {
			HashMap<Integer, Integer> skills = new HashMap<Integer, Integer>();
			for (int i = 0; i < 10; i++) {
				Integer skill = (Integer) clientScriptData.get(749 + (i * 2));
				if (skill != null) {
					Integer level = (Integer) clientScriptData.get(750 + (i * 2));
					if (level != null)
						skills.put(skill, level);
				}
			}
			Integer maxedSkill = (Integer) clientScriptData.get(277);
			if (maxedSkill != null)
				skills.put(maxedSkill, getId() == 19709 ? 120 : 99);
			itemRequiriments = skills;
			
			if (name.toLowerCase().contains("berserker helm"))
				itemRequiriments.put(Skills.DEFENCE, 45);
			else if (name.toLowerCase().contains("helm of neitiznot"))
				itemRequiriments.put(Skills.DEFENCE, 55);
			else if (name.toLowerCase().startsWith("body "))
				itemRequiriments.put(Skills.DEFENCE, 33);
			else if (name.toLowerCase().startsWith("cosmic "))
				itemRequiriments.put(Skills.DEFENCE, 40);
			else if (name.toLowerCase().startsWith("chaos "))
				itemRequiriments.put(Skills.DEFENCE, 50);
			switch (getId()) {
			case 21371:
			case 21372:
			case 21373:
			case 21374:
			case 21375:
				itemRequiriments.put(Skills.ATTACK, 75);
				break;
			case 10887:
				itemRequiriments.put(Skills.ATTACK, 60);
				itemRequiriments.put(Skills.STRENGTH, 40);
				itemRequiriments.put(Skills.PRAYER, 50);
				break;
			case 7456: //steel
				itemRequiriments.put(Skills.DEFENCE, 1);
			//	itemRequiriments.put(Skills.COOKING, 5);
				break;
			case 7457: //black
				itemRequiriments.put(Skills.DEFENCE, 1);
				//itemRequiriments.put(Skills.COOKING, 10);
				break;
			case 7458: //mit
				itemRequiriments.put(Skills.DEFENCE, 1);
				//itemRequiriments.put(Skills.COOKING, 20);
				break;
			case 7459:
				itemRequiriments.put(Skills.DEFENCE, 13);
				//itemRequiriments.put(Skills.COOKING, 30);
				break;
			case 7460:
				itemRequiriments.put(Skills.DEFENCE, 34);
				//itemRequiriments.put(Skills.COOKING, 40);
				break;
			case 7461:
				itemRequiriments.put(Skills.DEFENCE, 41);
				//itemRequiriments.put(Skills.COOKING, 60);
				break;
			case 7462:
				itemRequiriments.put(Skills.DEFENCE, 41);
				//itemRequiriments.put(Skills.COOKING, 70);
				break;
			case 12674:
			case 12675:
				itemRequiriments.put(Skills.DEFENCE, 45);
				break;
			case 12680:
			case 12681:
				itemRequiriments.put(Skills.DEFENCE, 55);
				break;
			case 10828:
				/*itemRequiriments.put(Skills.CONSTRUCTION, 20);
				itemRequiriments.put(Skills.WOODCUTTING, 54);
				itemRequiriments.put(Skills.CRAFTING, 46);
				itemRequiriments.put(Skills.AGILITY, 40);*/
				break;
			case 2412:
			case 2413:
			case 2414:
				itemRequiriments.put(Skills.MAGIC, 60);
				break;
			case 19784:
			case 22401:
			case 19780: // Korasi
				itemRequiriments.put(Skills.ATTACK, 78);
				itemRequiriments.put(Skills.STRENGTH, 78);
				itemRequiriments.put(Skills.MAGIC, 80);
				itemRequiriments.put(Skills.DEFENCE, 10);
				itemRequiriments.put(Skills.SUMMONING, 55);
				break;
			case 20822:
			case 20823:
			case 20824:
			case 20825:
			case 20826:
				itemRequiriments.put(Skills.DEFENCE, 99);
				break;
			case 1377:
			case 1434:
				itemRequiriments.put(Skills.DEFENCE, 28);
				break;
			case 8846:
				itemRequiriments.put(0, 5);
				itemRequiriments.put(1, 5);
				break;
			case 8847:
				itemRequiriments.put(Skills.ATTACK, 10);
				itemRequiriments.put(Skills.DEFENCE, 10);
				break;
			case 8848:
				itemRequiriments.put(Skills.ATTACK, 20);
				itemRequiriments.put(Skills.DEFENCE, 20);
				break;
			case 8849:
				itemRequiriments.put(Skills.ATTACK, 30);
				itemRequiriments.put(Skills.DEFENCE, 30);
				break;
			case 8850:
				itemRequiriments.put(Skills.ATTACK, 40);
				itemRequiriments.put(Skills.DEFENCE, 40);
				break;
			case 20072:
				itemRequiriments.put(Skills.ATTACK, 60);
				itemRequiriments.put(Skills.DEFENCE, 60);
				break;
			//case 19172:
			//itemRequiriments.put(Skills.PRAYER, 22);
			case 8839:
			case 8840:
			case 8841:
			case 8842:
			case 11663:
			case 11664:
			case 11665:
			case 11674:
			case 11675:
			case 11676:
				itemRequiriments.put(Skills.DEFENCE, 42);
				itemRequiriments.put(Skills.HITPOINTS, 42);
				itemRequiriments.put(Skills.RANGE, 42);
				itemRequiriments.put(Skills.ATTACK, 42);
				itemRequiriments.put(Skills.MAGIC, 42);
				itemRequiriments.put(Skills.STRENGTH, 42);
				break;
			case 19785:
			case 19786:
			case 19787:
			case 19788:
			case 19789:
			case 19790:
				itemRequiriments.put(Skills.ATTACK, 78);
				itemRequiriments.put(Skills.STRENGTH, 78);
				itemRequiriments.put(Skills.MAGIC, 80);
				itemRequiriments.put(Skills.HITPOINTS, 42);
				itemRequiriments.put(Skills.RANGE, 42);
				itemRequiriments.put(Skills.PRAYER, 22);
				break;
			}
		}

		return itemRequiriments;
	}

	public void setDefaultOptions() {
		groundOptions = new String[]
		{ null, null, "take", null, null };
		inventoryOptions = new String[]
		{ null, null, null, null, "drop" };
	}

	public void setDefaultsVariableValues() {
		name = "null";
		maleEquip1 = -1;
		maleEquip2 = -1;
		femaleEquip1 = -1;
		femaleEquip2 = -1;
		scale = 2000;
		lendId = -1;
		lendTemplateId = -1;
		lastManStandingId = -1;
		lastManStandingTemplateId = -1;
		cert = -1;
		certTemplate = -1;
		scaleZ = 128;
		value = 1;
		maleModel3 = -1;
		femaleModel3 = -1;
		bindTemplateId = -1;
		bindId = -1;
		team = -1;
		equipSlot = -1;
		equipType = -1;
	}
	
	public final void readValuesOSRS(InputStream stream, int opcode) {
		if (opcode == 1)
			model = stream.readUnsignedShort() + Settings.OSRS_MODEL_OFFSET;
		else if (opcode == 2)
			name = stream.readString();
		else if (opcode == 4)
			scale = stream.readUnsignedShort();
		else if (opcode == 5)
			pitch = stream.readUnsignedShort();
		else if (opcode == 6)
			roll = stream.readUnsignedShort();
		else if (opcode == 7) {
			translateX = stream.readUnsignedShort();
			if (translateX > 32767)
				translateX -= 65536;
			translateX <<= 0;
		} else if (opcode == 8) {
			translateY = stream.readUnsignedShort();
			if (translateY > 32767)
				translateY -= 65536;
			translateY <<= 0;
		} else if (opcode == 11)
			stackable = 1;
		else if (opcode == 12)
			value = stream.readInt();
		else if (opcode == 16) {
			membersOnly = true;
		} else if (opcode == 23) {
			maleEquip1 = stream.readUnsignedShort() + Settings.OSRS_MODEL_OFFSET;
			maleOffset = stream.readUnsignedByte();
		} else if (opcode == 24)
			maleEquip2 = stream.readUnsignedShort() + Settings.OSRS_MODEL_OFFSET;
		else if (opcode == 25) {
			femaleEquip1 = stream.readUnsignedShort() + Settings.OSRS_MODEL_OFFSET;
			femaleOffset = stream.readUnsignedByte();
		} else if (opcode == 26)
			femaleEquip2 = stream.readUnsignedShort() + Settings.OSRS_MODEL_OFFSET;
		else if (opcode >= 30 && opcode < 35)
			groundOptions[opcode - 30] = stream.readString();
		else if (opcode >= 35 && opcode < 40)
			inventoryOptions[opcode - 35] = stream.readString();
		else if (opcode == 40) {
			int length = stream.readUnsignedByte();
			originalModelColors = new int[length];
			modifiedModelColors = new int[length];
			for (int index = 0; index < length; index++) {
				originalModelColors[index] = stream.readUnsignedShort();
				modifiedModelColors[index] = stream.readUnsignedShort();
			}
		} else if (opcode == 41) {
			int length = stream.readUnsignedByte();
			originalTextureColors = new short[length];
			modifiedTextureColors = new short[length];
			for (int index = 0; index < length; index++) {
				originalTextureColors[index] = (short) stream.readUnsignedShort();
				modifiedTextureColors[index] = (short) stream.readUnsignedShort();
			}
		} else if (opcode == 42) {
			team = stream.readByte();
		} else if (opcode == 65)
			tradeable = true;
		else if (opcode == 78)
			maleModel3 = stream.readUnsignedShort() + Settings.OSRS_MODEL_OFFSET;
		else if (opcode == 79)
			femaleModel3 = stream.readUnsignedShort() + Settings.OSRS_MODEL_OFFSET;
		else if (opcode == 90)
			maleHeadModel1 = stream.readUnsignedShort() + Settings.OSRS_MODEL_OFFSET;
		else if (opcode == 91)
			femaleHeadModel1 = stream.readUnsignedShort() + Settings.OSRS_MODEL_OFFSET;
		else if (opcode == 92)
			maleHeadModel2 = stream.readUnsignedShort() + Settings.OSRS_MODEL_OFFSET;
		else if (opcode == 93)
			femaleHeadModel2 = stream.readUnsignedShort() + Settings.OSRS_MODEL_OFFSET;
		else if (opcode == 95)
			offsetX = stream.readUnsignedShort();
		else if (opcode == 97)
			cert = stream.readUnsignedShort() + Settings.OSRS_ITEM_OFFSET;
		else if (opcode == 98)
			certTemplate = stream.readUnsignedShort() + Settings.OSRS_ITEM_OFFSET;
		else if (opcode >= 100 && opcode < 110) {
			if (stackIds == null) {
				stackIds = new int[10];
				stackAmounts = new int[10];
			}
			stackIds[opcode - 100] = stream.readUnsignedShort();
			stackAmounts[opcode - 100] = stream.readUnsignedShort();
		} else if (opcode == 110)
			scaleX = stream.readUnsignedShort();
		else if (opcode == 111)
			scaleY = stream.readUnsignedShort();
		else if (opcode == 112)
			scaleZ = stream.readUnsignedShort();
		else if (opcode == 113)
			shadow = stream.readByte();
		else if (opcode == 114)
			lightness = stream.readByte() * 5;
		else if (opcode == 115)
			team = stream.readUnsignedByte();
		else if (opcode == 139)
			lendId = stream.readUnsignedShort() + Settings.OSRS_ITEM_OFFSET;
		else if (opcode == 140)
			lendTemplateId = stream.readUnsignedShort() + Settings.OSRS_ITEM_OFFSET;
		else if (opcode == 148) {
			lastManStandingId = stream.readUnsignedShort() + Settings.OSRS_ITEM_OFFSET;
		} else if (opcode == 149) {
			lastManStandingTemplateId = stream.readUnsignedShort() + Settings.OSRS_ITEM_OFFSET;
		} else if (opcode == 249) {
			int length = stream.readUnsignedByte();
			if (clientScriptData == null)
				clientScriptData = new HashMap<Integer, Object>(length);
			for (int index = 0; index < length; index++) {
				boolean stringInstance = stream.readUnsignedByte() == 1;
				int key = stream.read24BitInt();
				Object value = stringInstance ? stream.readString() : stream.readInt();
				clientScriptData.put(key, value);
			}
		} else {
			Logger.log(this, "Wrong opcode (osrs): "+opcode+", "+id);
		}
	}


	public final void readValues(InputStream stream, int opcode) {
		if (opcode == 1)
			model = stream.readBigSmart();
		else if (opcode == 2)
			name = stream.readString();
		else if (opcode == 4)
			scale = stream.readUnsignedShort();
		else if (opcode == 5)
			pitch = stream.readUnsignedShort();
		else if (opcode == 6)
			roll = stream.readUnsignedShort();
		else if (opcode == 7) {
			translateX = stream.readUnsignedShort();
			if (translateX > 32767)
				translateX -= 65536;
			translateX <<= 0;
		} else if (opcode == 8) {
			translateY = stream.readUnsignedShort();
			if (translateY > 32767)
				translateY -= 65536;
			translateY <<= 0;
		} else if (opcode == 11)
			stackable = 1;
		else if (opcode == 12)
			value = stream.readInt();
		else if (opcode == 13) {
			equipSlot = stream.readUnsignedByte();
		} else if (opcode == 14) {
			equipType = stream.readUnsignedByte();
		} else if (opcode == 16)
			membersOnly = true;
		else if (opcode == 18) { // added
			stream.readUnsignedShort();
		} else if (opcode == 23)
			maleEquip1 = stream.readBigSmart();
		else if (opcode == 24)
			maleEquip2 = stream.readBigSmart();
		else if (opcode == 25)
			femaleEquip1 = stream.readBigSmart();
		else if (opcode == 26)
			femaleEquip2 = stream.readBigSmart();
		else if (opcode == 27)
			stream.readUnsignedByte();
		else if (opcode >= 30 && opcode < 35)
			groundOptions[opcode - 30] = stream.readString();
		else if (opcode >= 35 && opcode < 40)
			inventoryOptions[opcode - 35] = stream.readString();
		else if (opcode == 40) {
			int length = stream.readUnsignedByte();
			originalModelColors = new int[length];
			modifiedModelColors = new int[length];
			for (int index = 0; index < length; index++) {
				originalModelColors[index] = stream.readUnsignedShort();
				modifiedModelColors[index] = stream.readUnsignedShort();
			}
		} else if (opcode == 41) {
			int length = stream.readUnsignedByte();
			originalTextureColors = new short[length];
			modifiedTextureColors = new short[length];
			for (int index = 0; index < length; index++) {
				originalTextureColors[index] = (short) stream.readUnsignedShort();
				modifiedTextureColors[index] = (short) stream.readUnsignedShort();
			}
		} else if (opcode == 42) {
			int length = stream.readUnsignedByte();
			unknownArray1 = new byte[length];
			for (int index = 0; index < length; index++)
				unknownArray1[index] = (byte) stream.readByte();
		} else if (opcode == 44) {
			int length = stream.readUnsignedShort();
			int arraySize = 0;
			for (int modifier = 0; modifier > 0; modifier++) {
				arraySize++;
				unknownArray3 = new byte[arraySize];
				byte offset = 0;
				for (int index = 0; index < arraySize; index++) {
					if ((length & 1 << index) > 0) {
						unknownArray3[index] = offset;
					} else {
						unknownArray3[index] = -1;
					}
				}
			}
		} else if (45 == opcode) {
			int i_97_ = (short) stream.readUnsignedShort();
			int i_98_ = 0;
			for (int i_99_ = i_97_; i_99_ > 0; i_99_ >>= 1)
				i_98_++;
			unknownArray6 = new byte[i_98_];
			byte i_100_ = 0;
			for (int i_101_ = 0; i_101_ < i_98_; i_101_++) {
				if ((i_97_ & 1 << i_101_) > 0) {
					unknownArray6[i_101_] = i_100_;
					i_100_++;
				} else
					unknownArray6[i_101_] = (byte) -1;
			}
		} else if (opcode == 65)
			tradeable = true;
		else if (opcode == 78)
			maleModel3 = stream.readBigSmart();
		else if (opcode == 79)
			femaleModel3 = stream.readBigSmart();
		else if (opcode == 90)
			maleHeadModel1 = stream.readBigSmart();
		else if (opcode == 91)
			femaleHeadModel1 = stream.readBigSmart();
		else if (opcode == 92)
			maleHeadModel2 = stream.readBigSmart();
		else if (opcode == 93)
			femaleHeadModel2 = stream.readBigSmart();
		else if (opcode == 94) {// new
			int anInt7887 = stream.readUnsignedShort();
		}else if (opcode == 95)
			offsetX = stream.readUnsignedShort();
		else if (opcode == 96)
			unknownInt6 = stream.readUnsignedByte();
		else if (opcode == 97)
			cert = stream.readUnsignedShort();
		else if (opcode == 98)
			certTemplate = stream.readUnsignedShort();
		else if (opcode >= 100 && opcode < 110) {
			if (stackIds == null) {
				stackIds = new int[10];
				stackAmounts = new int[10];
			}
			stackIds[opcode - 100] = stream.readUnsignedShort();
			stackAmounts[opcode - 100] = stream.readUnsignedShort();
		} else if (opcode == 110)
			scaleX = stream.readUnsignedShort();
		else if (opcode == 111)
			scaleY = stream.readUnsignedShort();
		else if (opcode == 112)
			scaleZ = stream.readUnsignedShort();
		else if (opcode == 113)
			shadow = stream.readByte();
		else if (opcode == 114)
			lightness = stream.readByte() * 5;
		else if (opcode == 115)
			team = stream.readUnsignedByte();
		else if (opcode == 121)
			lendId = stream.readUnsignedShort();
		else if (opcode == 122)
			lendTemplateId = stream.readUnsignedShort();
		else if (opcode == 125) {
			unknownInt12 = stream.readByte() << 2;
			unknownInt13 = stream.readByte() << 2;
			unknownInt14 = stream.readByte() << 2;
		} else if (opcode == 126) {
			unknownInt15 = stream.readByte() << 2;
			unknownInt16 = stream.readByte() << 2;
			unknownInt17 = stream.readByte() << 2;
		} else if (opcode == 127) {
			unknownInt18 = stream.readUnsignedByte();
			unknownInt19 = stream.readUnsignedShort();
		} else if (opcode == 128) {
			unknownInt20 = stream.readUnsignedByte();
			unknownInt21 = stream.readUnsignedShort();
		} else if (opcode == 129) {
			unknownInt20 = stream.readUnsignedByte();
			unknownInt21 = stream.readUnsignedShort();
		} else if (opcode == 130) {
			unknownInt22 = stream.readUnsignedByte();
			unknownInt23 = stream.readUnsignedShort();
		} else if (opcode == 132) {
			int length = stream.readUnsignedByte();
			unknownArray2 = new int[length];
			for (int index = 0; index < length; index++)
				unknownArray2[index] = stream.readUnsignedShort();
		} else if (opcode == 134) {
			int unknownValue = stream.readUnsignedByte();
		} else if (opcode == 139) {
			bindId = stream.readUnsignedShort();
		} else if (opcode == 140) {
			bindTemplateId = stream.readUnsignedShort();
		} else if (opcode >= 142 && opcode < 147) {
			if (unknownArray4 == null) {
				unknownArray4 = new int[6];
				Arrays.fill(unknownArray4, -1);
			}
			unknownArray4[opcode - 142] = stream.readUnsignedShort();
		} else if (opcode >= 150 && opcode < 155) {
			if (null == unknownArray5) {
				unknownArray5 = new int[5];
				Arrays.fill(unknownArray5, -1);
			}
			unknownArray5[opcode - 150] = stream.readUnsignedShort();
		}else if (opcode == 156) { //new
			
		} else if (157 == opcode) {//new
			boolean aBool7955 = true;
		} else if (161 == opcode) {//new
			int anInt7904 = stream.readUnsignedShort();
		} else if (162 == opcode) {//new
			int anInt7923 = stream.readUnsignedShort();
		} else if (163 == opcode) {//new
			int anInt7939 = stream.readUnsignedShort();
		} else if (164 == opcode) {//new coinshare shard
			String aString7902 = stream.readString();
		} else if (opcode == 165) {//new
			stackable = 2;
		} else if (opcode == 242) {
			int oldInvModel = stream.readBigSmart();
		} else if (opcode == 243) {
			int oldMaleEquipModelId3 = stream.readBigSmart();
		} else if (opcode == 244) {
			int oldFemaleEquipModelId3 = stream.readBigSmart();
		} else if (opcode == 245) {
			int oldMaleEquipModelId2 = stream.readBigSmart();
		} else if (opcode == 246) {
			int oldFemaleEquipModelId2 = stream.readBigSmart();
		} else if (opcode == 247) {
			int oldMaleEquipModelId1 = stream.readBigSmart();
		} else if (opcode == 248) {
			int oldFemaleEquipModelId1 = stream.readBigSmart();
		} else if (opcode == 251) {
			int length = stream.readUnsignedByte();
			int[] oldoriginalModelColors = new int[length];
			int[] oldmodifiedModelColors = new int[length];
			for (int index = 0; index < length; index++) {
				oldoriginalModelColors[index] = stream.readUnsignedShort();
				oldmodifiedModelColors[index] = stream.readUnsignedShort();
			}
		} else if (opcode == 252) {
			int length = stream.readUnsignedByte();
			short[] oldoriginalTextureColors = new short[length];
			short[] oldmodifiedTextureColors = new short[length];
			for (int index = 0; index < length; index++) {
				oldoriginalTextureColors[index] = (short) stream.readUnsignedShort();
				oldmodifiedTextureColors[index] = (short) stream.readUnsignedShort();
			}
		} else if (opcode == 249) {
			int length = stream.readUnsignedByte();
			if (clientScriptData == null)
				clientScriptData = new HashMap<Integer, Object>(length);
			for (int index = 0; index < length; index++) {
				boolean stringInstance = stream.readUnsignedByte() == 1;
				int key = stream.read24BitInt();
				Object value = stringInstance ? stream.readString() : stream.readInt();
				clientScriptData.put(key, value);
			}
		} else {
			Logger.log(this, "Wrong opcode (rs2): "+opcode);
		}
	}

	public final void readValues685(InputStream stream, int opcode) {
		if (opcode == 1)
			model = stream.readBigSmart() + Settings._685_MODEL_OFFSET;
		else if (opcode == 2)
			name = stream.readString();
		else if (opcode == 4)
			scale = stream.readUnsignedShort();
		else if (opcode == 5)
			pitch = stream.readUnsignedShort();
		else if (opcode == 6)
			roll = stream.readUnsignedShort();
		else if (opcode == 7) {
			translateX = stream.readUnsignedShort();
			if (translateX > 32767)
				translateX -= 65536;
			translateX <<= 0;
		} else if (opcode == 8) {
			translateY = stream.readUnsignedShort();
			if (translateY > 32767)
				translateY -= 65536;
			translateY <<= 0;
		} else if (opcode == 11)
			stackable = 1;
		else if (opcode == 12)
			value = stream.readInt();
		else if (opcode == 13) {
			equipSlot = stream.readUnsignedByte();
		} else if (opcode == 14) {
			equipType = stream.readUnsignedByte();
		} else if (opcode == 16)
			membersOnly = true;
		else if (opcode == 18) { // added
			stream.readUnsignedShort();
		} else if (opcode == 23)
			maleEquip1 = stream.readBigSmart() + Settings._685_MODEL_OFFSET;
		else if (opcode == 24)
			maleEquip2 = stream.readBigSmart() + Settings._685_MODEL_OFFSET;
		else if (opcode == 25)
			femaleEquip1 = stream.readBigSmart() + Settings._685_MODEL_OFFSET;
		else if (opcode == 26)
			femaleEquip2 = stream.readBigSmart() + Settings._685_MODEL_OFFSET;
		else if (opcode == 27)
			stream.readUnsignedByte();
		else if (opcode >= 30 && opcode < 35)
			groundOptions[opcode - 30] = stream.readString();
		else if (opcode >= 35 && opcode < 40)
			inventoryOptions[opcode - 35] = stream.readString();
		else if (opcode == 40) {
			int length = stream.readUnsignedByte();
			originalModelColors = new int[length];
			modifiedModelColors = new int[length];
			for (int index = 0; index < length; index++) {
				originalModelColors[index] = stream.readUnsignedShort();
				modifiedModelColors[index] = stream.readUnsignedShort();
			}
		} else if (opcode == 41) {
			int length = stream.readUnsignedByte();
			originalTextureColors = new short[length];
			modifiedTextureColors = new short[length];
			for (int index = 0; index < length; index++) {
				originalTextureColors[index] = (short) stream.readUnsignedShort();
				modifiedTextureColors[index] = (short) stream.readUnsignedShort();
			}
		} else if (opcode == 42) {
			int length = stream.readUnsignedByte();
			unknownArray1 = new byte[length];
			for (int index = 0; index < length; index++)
				unknownArray1[index] = (byte) stream.readByte();
		} else if (opcode == 44) {
			int length = stream.readUnsignedShort();
			int arraySize = 0;
			for (int modifier = 0; modifier > 0; modifier++) {
				arraySize++;
				unknownArray3 = new byte[arraySize];
				byte offset = 0;
				for (int index = 0; index < arraySize; index++) {
					if ((length & 1 << index) > 0) {
						unknownArray3[index] = offset;
					} else {
						unknownArray3[index] = -1;
					}
				}
			}
		} else if (45 == opcode) {
			int i_97_ = (short) stream.readUnsignedShort();
			int i_98_ = 0;
			for (int i_99_ = i_97_; i_99_ > 0; i_99_ >>= 1)
				i_98_++;
			unknownArray6 = new byte[i_98_];
			byte i_100_ = 0;
			for (int i_101_ = 0; i_101_ < i_98_; i_101_++) {
				if ((i_97_ & 1 << i_101_) > 0) {
					unknownArray6[i_101_] = i_100_;
					i_100_++;
				} else
					unknownArray6[i_101_] = (byte) -1;
			}
		} else if (opcode == 65)
			tradeable = true;
		else if (opcode == 78)
			maleModel3 = stream.readBigSmart() + Settings._685_MODEL_OFFSET;
		else if (opcode == 79)
			femaleModel3 = stream.readBigSmart() + Settings._685_MODEL_OFFSET;
		else if (opcode == 90)
			maleHeadModel1 = stream.readBigSmart()+ Settings._685_MODEL_OFFSET;
		else if (opcode == 91)
			femaleHeadModel1 = stream.readBigSmart() + Settings._685_MODEL_OFFSET;
		else if (opcode == 92)
			maleHeadModel2 = stream.readBigSmart()+ Settings._685_MODEL_OFFSET;
		else if (opcode == 93)
			femaleHeadModel2 = stream.readBigSmart()+ Settings._685_MODEL_OFFSET;
		else if (opcode == 94) {// new
			int anInt7887 = stream.readUnsignedShort();
		}else if (opcode == 95)
			offsetX = stream.readUnsignedShort();
		else if (opcode == 96)
			unknownInt6 = stream.readUnsignedByte();
		else if (opcode == 97)
			cert = stream.readUnsignedShort() + Settings._685_ITEM_OFFSET;
		else if (opcode == 98)
			certTemplate = stream.readUnsignedShort() + Settings._685_ITEM_OFFSET;
		else if (opcode >= 100 && opcode < 110) {
			if (stackIds == null) {
				stackIds = new int[10];
				stackAmounts = new int[10];
			}
			stackIds[opcode - 100] = stream.readUnsignedShort();
			stackAmounts[opcode - 100] = stream.readUnsignedShort();
		} else if (opcode == 110)
			scaleX = stream.readUnsignedShort();
		else if (opcode == 111)
			scaleY = stream.readUnsignedShort();
		else if (opcode == 112)
			scaleZ = stream.readUnsignedShort();
		else if (opcode == 113)
			shadow = stream.readByte();
		else if (opcode == 114)
			lightness = stream.readByte() * 5;
		else if (opcode == 115)
			team = stream.readUnsignedByte();
		else if (opcode == 121)
			lendId = stream.readUnsignedShort() + Settings._685_ITEM_OFFSET;
		else if (opcode == 122)
			lendTemplateId = stream.readUnsignedShort()+ Settings._685_ITEM_OFFSET;
		else if (opcode == 125) {
			unknownInt12 = stream.readByte() << 2;
			unknownInt13 = stream.readByte() << 2;
			unknownInt14 = stream.readByte() << 2;
		} else if (opcode == 126) {
			unknownInt15 = stream.readByte() << 2;
			unknownInt16 = stream.readByte() << 2;
			unknownInt17 = stream.readByte() << 2;
		} else if (opcode == 127) {
			unknownInt18 = stream.readUnsignedByte();
			unknownInt19 = stream.readUnsignedShort();
		} else if (opcode == 128) {
			unknownInt20 = stream.readUnsignedByte();
			unknownInt21 = stream.readUnsignedShort();
		} else if (opcode == 129) {
			unknownInt20 = stream.readUnsignedByte();
			unknownInt21 = stream.readUnsignedShort();
		} else if (opcode == 130) {
			unknownInt22 = stream.readUnsignedByte();
			unknownInt23 = stream.readUnsignedShort();
		} else if (opcode == 132) {
			int length = stream.readUnsignedByte();
			unknownArray2 = new int[length];
			for (int index = 0; index < length; index++)
				unknownArray2[index] = stream.readUnsignedShort();
		} else if (opcode == 134) {
			int unknownValue = stream.readUnsignedByte();
		} else if (opcode == 139) {
			bindId = stream.readUnsignedShort()+ Settings._685_ITEM_OFFSET;
		} else if (opcode == 140) {
			bindTemplateId = stream.readUnsignedShort()+ Settings._685_ITEM_OFFSET;
		} else if (opcode >= 142 && opcode < 147) {
			if (unknownArray4 == null) {
				unknownArray4 = new int[6];
				Arrays.fill(unknownArray4, -1);
			}
			unknownArray4[opcode - 142] = stream.readUnsignedShort();
		} else if (opcode >= 150 && opcode < 155) {
			if (null == unknownArray5) {
				unknownArray5 = new int[5];
				Arrays.fill(unknownArray5, -1);
			}
			unknownArray5[opcode - 150] = stream.readUnsignedShort();
		}else if (opcode == 156) { //new
			
		} else if (157 == opcode) {//new
			boolean aBool7955 = true;
		} else if (161 == opcode) {//new
			int anInt7904 = stream.readUnsignedShort();
		} else if (162 == opcode) {//new
			int anInt7923 = stream.readUnsignedShort();
		} else if (163 == opcode) {//new
			int anInt7939 = stream.readUnsignedShort();
		} else if (164 == opcode) {//new coinshare shard
			String aString7902 = stream.readString();
		} else if (opcode == 165) {//new
			stackable = 2;
		} else if (opcode == 242) {
			int oldInvModel = stream.readBigSmart();
		} else if (opcode == 243) {
			int oldMaleEquipModelId3 = stream.readBigSmart();
		} else if (opcode == 244) {
			int oldFemaleEquipModelId3 = stream.readBigSmart();
		} else if (opcode == 245) {
			int oldMaleEquipModelId2 = stream.readBigSmart();
		} else if (opcode == 246) {
			int oldFemaleEquipModelId2 = stream.readBigSmart();
		} else if (opcode == 247) {
			int oldMaleEquipModelId1 = stream.readBigSmart();
		} else if (opcode == 248) {
			int oldFemaleEquipModelId1 = stream.readBigSmart();
		} else if (opcode == 251) {
			int length = stream.readUnsignedByte();
			int[] oldoriginalModelColors = new int[length];
			int[] oldmodifiedModelColors = new int[length];
			for (int index = 0; index < length; index++) {
				oldoriginalModelColors[index] = stream.readUnsignedShort();
				oldmodifiedModelColors[index] = stream.readUnsignedShort();
			}
		} else if (opcode == 252) {
			int length = stream.readUnsignedByte();
			short[] oldoriginalTextureColors = new short[length];
			short[] oldmodifiedTextureColors = new short[length];
			for (int index = 0; index < length; index++) {
				oldoriginalTextureColors[index] = (short) stream.readUnsignedShort();
				oldmodifiedTextureColors[index] = (short) stream.readUnsignedShort();
			}
		} else if (opcode == 249) {
			int length = stream.readUnsignedByte();
			if (clientScriptData == null)
				clientScriptData = new HashMap<Integer, Object>(length);
			for (int index = 0; index < length; index++) {
				boolean stringInstance = stream.readUnsignedByte() == 1;
				int key = stream.read24BitInt();
				Object value = stringInstance ? stream.readString() : stream.readInt();
				clientScriptData.put(key, value);
			}
		} else {
			Logger.log(this, "Wrong opcode (rs2): "+opcode);
		}
	}

	public int bindTemplateId;
	public int bindId;

	public final void readOpcodeValues(InputStream stream) {
		while (true) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				break;
			if (id >= Settings._685_ITEM_OFFSET)
				readValues685(stream, opcode);
			else if (id >= Settings.OSRS_ITEM_OFFSET)
				readValuesOSRS(stream, opcode);
			else
				readValues(stream, opcode);
		}
	}

	public boolean isBinded() {
		return (id >= 15775 && id <= 16272) || (id >= 19865 && id <= 19866);
	}

	public String getName() {
		return name;
	}

	public int getFemaleWornModelId1() {
		return femaleEquip1;
	}

	public int getFemaleWornModelId2() {
		return femaleEquip2;
	}

	public int getFemaleWornModelId3() {
		return femaleModel3;
	}

	public int getMaleWornModelId1() {
		return maleEquip1;
	}

	public int getMaleWornModelId2() {
		return maleEquip2;
	}

	public int getMaleWornModelId3() {
		return maleModel3;
	}

	public boolean isOverSized() {
		return scale > 5000;
	}

	public boolean isLended() {
		return lended;
	}

	public boolean isLastManStanding() {
		return lastManStanding;
	}
	
	public boolean isSCItem() {
		return id >= 14122 && id <= 14432;
	}
	public boolean isDungItem() {
		return id == 18830 || (id >= 19653 && id <= 19668) || id  == 18829 || id == 21332 || id >= 15753 && id <= 18329 || id >= 20822 && id <= 20882 || id >= 18511 && id <= 18570;
	}

	public boolean isCustomItem() {
		return (id >= 25354 && id < Settings.OSRS_ITEM_OFFSET);
	}

	public boolean isOsrsRepeated() {
		return (id >= Settings.OSRS_ITEM_OFFSET && id < 40500);
	}

	public boolean isMembersOnly() {
		return membersOnly;
	}

	public boolean isStackable() {
		return stackable == 1 || id == 0;
	}

	public boolean isNoted() {
		return noted;
	}

	public int getLendId() {
		return lendId;
	}

	public int getCertId() {
		return cert;
	}

	public int getValue() {
		return value;
	}

	public int getId() {
		return id;
	}

	public int getEquipSlot() {
		return equipSlot;
	}

	public int getEquipType() {
		return equipType;
	}

	public void getShopStats(Player player, Item item) {
		if (item.getDefinitions().name.contains("sword") || item.getDefinitions().name.contains("dagger")
				|| item.getDefinitions().name.contains("scimitar") || item.getDefinitions().name.contains("whip")
				|| item.getDefinitions().name.contains("spear") || item.getDefinitions().name.contains("mace")
				|| item.getDefinitions().name.contains("battleaxe") || item.getDefinitions().name.contains("staff")
				|| item.getDefinitions().name.contains("hatchet") || item.getDefinitions().name.contains("pickaxe")
				|| item.getDefinitions().name.contains("plate") || item.getDefinitions().name.contains("body")
				|| item.getDefinitions().name.contains("robe top") || item.getDefinitions().name.contains("top")
				|| item.getDefinitions().name.contains("jacket") || item.getDefinitions().name.contains("tabard")

				|| item.getDefinitions().name.contains("shirt") || item.getDefinitions().name.contains("apron")
				|| item.getDefinitions().name.contains("chest") || item.getDefinitions().name.contains("gloves")
				|| item.getDefinitions().name.contains("gauntlets") || item.getDefinitions().name.contains("vambraces")
				|| item.getDefinitions().name.contains("boots") || item.getDefinitions().name.contains("necklace")
				|| item.getDefinitions().name.contains("amulet") || item.getDefinitions().name.contains("skirt")
				|| item.getDefinitions().name.contains("kilt") || item.getDefinitions().name.contains("leggings")
				|| item.getDefinitions().name.contains("chaps") || item.getDefinitions().name.contains("pants")
				|| item.getDefinitions().name.contains("shorts") || item.getDefinitions().name.contains("legs")
				|| item.getDefinitions().name.contains("helm") || item.getDefinitions().name.contains("cap")
				|| item.getDefinitions().name.contains("hood") || item.getDefinitions().name.contains("coif")
				|| item.getDefinitions().name.contains("fez") || item.getDefinitions().name.contains("mask")
				|| item.getDefinitions().name.contains("paint") || item.getDefinitions().name.contains("visor")
				|| item.getDefinitions().name.contains("cavalier") || item.getDefinitions().name.contains("hat")
				|| item.getDefinitions().name.contains("shield") || item.getDefinitions().name.contains("book")
				|| item.getDefinitions().name.contains("shield") || item.getDefinitions().name.contains("2h")
				|| item.getDefinitions().name.contains("maul") || item.getDefinitions().name.contains("claws")
				|| item.getDefinitions().name.contains("cape") || item.getDefinitions().name.contains("ava's")
				|| item.getDefinitions().name.contains("cloak") || item.getDefinitions().name.contains("Cape")
				|| item.getDefinitions().name.contains("arrow") || item.getDefinitions().name.contains("bolt")
				|| item.getDefinitions().name.contains("ball") || item.getDefinitions().name.contains("chinchompa")
				|| item.getDefinitions().name.contains("dart") || item.getDefinitions().name.contains("knife")
				|| item.getDefinitions().name.contains("javelin") || item.getDefinitions().name.contains("holy water")
				|| item.getDefinitions().name.contains("bow") || item.getDefinitions().name.contains("Staff")
				|| item.getDefinitions().name.contains("staff") || item.getDefinitions().name.contains("wand")) {
			player.getPackets().sendVar(1876, 0);
		} else {
			player.getPackets().sendVar(1876, -1);
		}
	}

	public String getEquipType(Item item) {
		if (item.getDefinitions().name.contains("sword") || item.getDefinitions().name.contains("dagger")
				|| item.getDefinitions().name.contains("scimitar") || item.getDefinitions().name.contains("whip")
				|| item.getDefinitions().name.contains("spear") || item.getDefinitions().name.contains("mace")
				|| item.getDefinitions().name.contains("battleaxe") || item.getDefinitions().name.contains("staff")
				|| item.getDefinitions().name.contains("hatchet") || item.getDefinitions().name.contains("pickaxe")) {
			return "wielded in the right hand";
		}

		if (item.getDefinitions().name.contains("plate") || item.getDefinitions().name.contains("body")
				|| item.getDefinitions().name.contains("robe top") || item.getDefinitions().name.contains("top")
				|| item.getDefinitions().name.contains("jacket") || item.getDefinitions().name.contains("tabard")
				|| item.getDefinitions().name.contains("shirt") || item.getDefinitions().name.contains("apron")
				|| item.getDefinitions().name.contains("chest")) {
			return "worn on the torso";
		}
		if (item.getDefinitions().name.contains("gloves") || item.getDefinitions().name.contains("gauntlets")
				|| item.getDefinitions().name.contains("vambraces")) {
			return "worn on the hands";
		}
		if (item.getDefinitions().name.contains("boots")) {
			return "worn on the feet";
		}
		if (item.getDefinitions().name.contains("necklace") || item.getDefinitions().name.contains("amulet")) {
			return "worn on the neck";
		}
		if (item.getDefinitions().name.contains("skirt") || item.getDefinitions().name.contains("kilt")
				|| item.getDefinitions().name.contains("leggings") || item.getDefinitions().name.contains("chaps")
				|| item.getDefinitions().name.contains("pants") || item.getDefinitions().name.contains("shorts")
				|| item.getDefinitions().name.contains("legs")) {
			return "worn on the legs";
		}
		if (item.getDefinitions().name.contains("helm") || item.getDefinitions().name.contains("cap")
				|| item.getDefinitions().name.contains("hood") || item.getDefinitions().name.contains("coif")
				|| item.getDefinitions().name.contains("fez") || item.getDefinitions().name.contains("mask")
				|| item.getDefinitions().name.contains("paint") || item.getDefinitions().name.contains("visor")
				|| item.getDefinitions().name.contains("cavalier") || item.getDefinitions().name.contains("hat")) {
			return "worn on the head";
		}
		if (item.getDefinitions().name.contains("shield") || item.getDefinitions().name.contains("book")) {
			return "held in the left hand";
		}
		if (item.getDefinitions().name.contains("shield") || item.getDefinitions().name.contains("2h")
				|| item.getDefinitions().name.contains("maul") || item.getDefinitions().name.contains("claws")) {
			return "wielded in both hands";
		}
		if (item.getDefinitions().name.contains("cape") || item.getDefinitions().name.contains("ava's")
				|| item.getDefinitions().name.contains("cloak") || item.getDefinitions().name.contains("Cape")) {
			return "worn on the back";
		}
		return "an item";
	}

	public String getItemType(Item item) {
		if (item.getDefinitions().name.contains("sword") || item.getDefinitions().name.contains("dagger")
				|| item.getDefinitions().name.contains("scimitar") || item.getDefinitions().name.contains("maul")
				|| item.getDefinitions().name.contains("whip") || item.getDefinitions().name.contains("claws")
				|| item.getDefinitions().name.contains("spear") || item.getDefinitions().name.contains("mace")
				|| item.getDefinitions().name.contains("cane") || item.getDefinitions().name.contains("hasta")
				|| item.getDefinitions().name.contains("brackish blade")
				|| item.getDefinitions().name.contains("battleaxe")) {
			return "a melee weapon";
		}
		if (item.getDefinitions().name.contains("Staff") || item.getDefinitions().name.contains("wand")) {
			return "a weapon for mages";
		}
		if (item.getDefinitions().name.contains("body") || item.getDefinitions().name.contains("legs")
				|| item.getDefinitions().name.contains("robe") || item.getDefinitions().name.contains("priest")
				|| item.getDefinitions().name.contains("helm")) {
			return "a piece of apparel";
		}
		if (item.getDefinitions().name.contains("shield")) {
			return "a shield";
		}
		if (item.getDefinitions().name.contains("hatchet")) {
			return "a hatchet";
		}
		if (item.getDefinitions().name.contains("arrow") || item.getDefinitions().name.contains("bolt")
				|| item.getDefinitions().name.contains("ball")) {
			return "ammunition for a ranged weapon";
		}
		if (item.getDefinitions().name.contains("chinchompa") || item.getDefinitions().name.contains("dart")
				|| item.getDefinitions().name.contains("knife") || item.getDefinitions().name.contains("javelin")
				|| item.getDefinitions().name.contains("holy water") || item.getDefinitions().name.contains("bow")) {
			return "a ranged weapon";
		}
		return "an item";
	}

	public void applyEquipData() {
		String name = this.name.toLowerCase();

		if (name.contains(" chestplate") || name.contains("body")
				|| name.contains(" brassard") || name.contains(" top")
				|| name.contains(" jacket") || name.contains(" shirt")
				|| name.contains(" apron") || name.contains(" coat")
				|| name.contains(" blouse") || name.contains(" hauberk")
				|| name.contains(" chestguard") || name.contains(" torso")
				|| name.contains(" garb") || name.contains(" tunic")
				|| name.contains(" armour")) {
			equipSlot = 4;
			equipType = 6;
		}

		if (name.contains("legs") || name.contains("skirt")
				|| name.contains("robe bottom") || name.contains(" chaps")
				|| name.contains(" leggings") || name.contains(" tassets")
				|| name.contains("slacks") || name.contains(" bottoms")
				|| name.contains(" trousers") || name.contains(" greaves")
				|| name.contains("Shorts")) {
			equipSlot = 7;
		}

		if (name.contains("mask") || name.contains("helm")
				|| name.contains("hat") || name.contains(" hood")
				|| name.contains("coif") || name.contains("mitre")
				|| name.contains("eyepatch") || name.contains("mask")
				|| name.contains(" boater") || name.contains(" beret")
				|| name.contains(" snelm") || name.contains(" tiara")
				|| name.contains(" ears") || name.contains(" head")
				|| name.contains(" cavalier") || name.contains(" wreath")
				|| name.contains("fedora") || name.contains("fez")
				|| name.contains(" headband") || name.contains(" headgear")
				|| name.contains(" faceguard")) {
			if (name.contains("mask"))
				equipType = 8;
			equipSlot = 0;
		}

		if (name.contains("ring")) {
			equipSlot = 12;
		}

		if (name.contains("amulet") || name.contains("necklace")
				|| name.contains("pendant") || name.contains("scarf")
				|| name.contains("stole") || name.contains(" symbol")) {
			equipSlot = 2;
		}

		if (name.contains("boots") || name.contains("feet")
				|| name.contains("shoes") || name.contains("sandals")) {
			equipSlot = 10;
		}

		if (name.contains("gloves") || name.contains(" vamb")
				|| name.contains("bracelet") || name.contains("bracers")
				|| name.contains("gauntlets") || name.contains(" cuffs")
				|| name.contains("hands") || name.contains("armband")) {
			equipSlot = 9;
		}

		if (name.contains("cape") || name.contains("cloak") || name.contains("ava's")) {
			equipSlot = 1;
		}

		if (name.contains("shield") || name.contains("defender")
				|| name.contains("book") || name.contains(" ward")
				|| name.equalsIgnoreCase("tome of fire") || name.equalsIgnoreCase("toktz-ket-xil")
				|| name.contains("satchel")) {
			equipSlot = 5;
		}

		if (name.contains(" arrow") || name.contains(" bolts")
				|| name.contains(" brutal") || name.contains(" arrows")
				|| name.contains(" tar") || name.contains(" blessing")
				|| name.contains(" javelin") || name.contains(" grapple")) {
			equipSlot = 13;
		}

		if (name.contains("sword") || name.contains("whip")
				|| name.contains("halbard") || name.contains("claws")
				|| name.contains("spear") || name.contains("anchor")
				|| name.contains("lance")
				|| name.contains("dagger") || name.contains("bow")
				|| name.contains("bulwark") || name.contains(" axe")
				|| name.contains(" maul") || name.contains(" ballista")
				|| name.contains(" club") || name.contains("katana")
				|| name.contains("scythe") || name.contains("staff")
				|| name.contains("sceptre")
				|| name.contains("wand") || name.contains("lizard")
				|| name.contains("salamander") || name.contains("flail")
				|| name.contains("mjolnir") || name.contains("mace")
				|| name.contains("hammer") || name.contains("arclight")
				|| name.contains("crozier") || name.contains("banner ")
				|| name.contains("dart") || name.contains("cane")
				|| name.contains("chinchompa") || name.contains("knife")
				|| name.contains("scimitar") || name.contains("hasta")
				|| name.contains("rapier") || name.contains("sickle")
				|| name.contains("greegree") || name.contains("machete")
				|| name.contains("blackjack") || name.contains("blowpipe")
				|| name.contains("trident") || name.contains("pickaxe")
				|| name.contains("harpoon") || name.contains("bludgeon")) {
			equipSlot = 3;
			if (name.contains("2h") || name.contains("halbard")
					|| name.contains("spear") || name.contains("bulwark")
					|| name.contains("maul") || name.contains(" ballista")
					|| name.contains("salamander") || name.contains("lizard")
					|| (name.contains("bow") && (!name.contains("cross") && !name.contains("karil's")))
					|| name.contains("club") || name.contains("claws")
					|| name.contains("blowpipe") || name.contains("anchor")
					|| name.contains("greataxe") || name.contains("godsword") || name.contains("bludgeon")) {
				equipType = 5;
			}
		}
	}


	public int getCSOpcode(int opcode) {
		if (clientScriptData == null)
			return 0;
		Integer cs = (Integer) clientScriptData.get(opcode);
		if (cs == null)
			return 0;
		return cs;
	}
	
	public int getAttackStyle() {
		return getCSOpcode(686);
	}
	
	public void addLevel(int add) {
		HashMap<Integer, Integer> levels = getWearingSkillRequiriments();
		if (levels == null)
			return;
		for (int skill : levels.keySet()) {
			int level = levels.get(skill);
			if (level > 1)
				levels.put(skill, level + add);
		}
	}
	
	public void setLevel(int skill, int level) {
		getWearingSkillRequiriments();
		if (itemRequiriments == null)
			itemRequiriments = new HashMap<Integer, Integer>();
		itemRequiriments.put(skill, level);
		if (skill == Skills.RANGE)
			setData(750, level);
	}

	public void setAttackStyle(int i) {
		setData(686, i);
	}

}