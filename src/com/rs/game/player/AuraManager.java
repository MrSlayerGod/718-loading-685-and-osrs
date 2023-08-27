package com.rs.game.player;

import java.io.Serializable;
import java.util.HashMap;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.player.content.Combat;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class AuraManager implements Serializable {

	private static final long serialVersionUID = -8860530757819448608L;

	private transient Player player;
	private transient boolean warned;
	private long activation;
	private HashMap<Integer, Long> cooldowns;

	public AuraManager() {
		cooldowns = new HashMap<Integer, Long>();
	}

	protected void setPlayer(Player player) {
		this.player = player;
	}

	public void process() {
		if (!isActivated() || player.tournamentResetRequired())
			return;
		if (activation - Utils.currentTimeMillis() <= 60000 && !warned) {
			player.getPackets().sendGameMessage("Your aura will deplete in 1 minute.");
			warned = true;
			return;
		}
		if (Utils.currentTimeMillis() < activation)
			return;
		desactive();
		player.getAppearence().generateAppearenceData();
	}

	public void removeAura() {
		if (isActivated())
			desactive();
	}

	public void desactive() {
		activation = 0;
		warned = false;
		player.getPackets().sendGameMessage("Your aura has depleted.");
	}

	public long getCoolDown(int aura) {
		Long coolDown = cooldowns.get(aura);
		if (coolDown == null)
			return 0;
		return coolDown;
	}
	
	private static boolean needsCharges(int aura) {
		return aura != 25477 && aura != 25554 && aura != 25622;
	}
	
	private static boolean forceChargeDonators(int aura) {
		return aura == 22300 || aura == 22298;
	}

	public void activate() {
		if (player.getControlerManager().getControler() instanceof FfaZone) {
			player.sendMessage("You can not use auras on spawn pvp!");
			return;
		}
		Item item = player.getEquipment().getItem(Equipment.SLOT_AURA);
		if (item == null)
			return;
		player.stopAll(false);
		int toId = getTransformIntoAura(item.getId());
		if (toId != -1) {
			player.getEquipment().getItem(Equipment.SLOT_AURA).setId(toId);
			player.getEquipment().refresh(Equipment.SLOT_AURA);
			player.getAppearence().generateAppearenceData();
		} else {
			if (activation != 0) {
				player.getPackets().sendGameMessage("Your aura is already activated.");
				return;
			}
			if (Utils.currentTimeMillis() <= getCoolDown(item.getId())  && (forceChargeDonators(item.getId()) || (!player.isVIPDonator() && needsCharges(item.getId())))) {
				player.getPackets().sendGameMessage("Your aura did not recharge yet."); 
				return;
			}
			int tier = getTier(item.getId());
			double multiplier = ((double)player.getDonator())/10 + 1;
			activation = player.isVIPDonator() || !needsCharges(item.getId()) ? Long.MAX_VALUE : (long) (Utils.currentTimeMillis() + (getActivationTime(item.getId()) * multiplier) * 1000);
			cooldowns.put(item.getId(), activation + getCooldown(item.getId()) * 1000);
			player.setNextAnimation(new Animation(2231));
			player.setNextGraphics(new Graphics(getActiveGraphic(tier)));
			player.getAppearence().generateAppearenceData();
		}
	}

	public int getTransformIntoAura(int aura) {
		switch (aura) {
		case 23896: // infernal
			return 23880;
		case 23880: // infernal
			return 23896;
		case 23898: // serene
			return 23882;
		case 23882: // serene
			return 23898;
		case 23900: // vernal
			return 23884;
		case 23884: // vernal
			return 23900;
		case 23902: // nocturnal
			return 23886;
		case 23886: // nocturnal
			return 23902;
		case 23904: // mystical
			return 23888;
		case 23888: // mystical
			return 23904;
		case 23906: // blazing
			return 23890;
		case 23890: // blazing
			return 23906;
		case 23908: // abyssal
			return 23892;
		case 23892: // abyssal
			return 23908;
		case 23910: // divine
			return 23894;
		case 23894: // divine
			return 23910;
		default:
			return -1;
		}
	}

	public void sendAuraRemainingTime() {
		if (!isActivated()) {
			long cooldown = getCoolDown(player.getEquipment().getAuraId());
			if (Utils.currentTimeMillis() <= cooldown && (forceChargeDonators(player.getEquipment().getAuraId()) || (!player.isVIPDonator() && needsCharges(player.getEquipment().getAuraId())))) {
				player.getPackets().sendGameMessage("Currently recharging. <col=ff0000>" + getFormatedTime((cooldown - Utils.currentTimeMillis()) / 1000) + " remaining.");
				return;
			}
			player.getPackets().sendGameMessage("Currently inactive. It is ready to use.");
			return;
		}
		player.getPackets().sendGameMessage("Currently active. <col=00ff00>" + getFormatedTime((activation - Utils.currentTimeMillis()) / 1000) + " remaining");
	}

	public String getFormatedTime(long seconds) {
		long minutes = seconds / 60;
		long hours = minutes / 60;
		minutes -= hours * 60;
		seconds -= (hours * 60 * 60) + (minutes * 60);
		String minutesString = (minutes < 10 ? "0" : "") + minutes;
		String secondsString = (seconds < 10 ? "0" : "") + seconds;
		return hours + ":" + minutesString + ":" + secondsString;
	}

	public void sendTimeRemaining(int aura) {
		long cooldown = getCoolDown(aura);
		if (cooldown < Utils.currentTimeMillis() || (!forceChargeDonators(aura) && (player.isVIPDonator() || !needsCharges(aura)))) {
			player.getPackets().sendGameMessage("The aura has finished recharging. It is ready to use.");
			return;
		}
		player.getPackets().sendGameMessage("Currently recharging. <col=ff0000>" + getFormatedTime((cooldown - Utils.currentTimeMillis()) / 1000) + " remaining.");
	}

	public boolean isActivated() {
		return activation != 0;
	}

	/*
	 * 16449 - Corruption 16464 - Greater corruption 16429 - SlayerMasterD corruption
	 * 68615 - Supreme corruption
	 * 
	 * 16465 - Salvation 16524 - Greater salvation 16450 - SlayerMasterD salvation
	 * 68611 - supreme salvation.
	 * 
	 * 68605 - Harmony. 68610 - Greater harmony. 68607 - SlayerMasterD harmony. 68613 -
	 * Supreme harmony.
	 */
	public int getAuraModelId2() {
		int aura = player.getEquipment().getAuraId();
		switch (aura) {
		case 22905: // Corruption.
			return 16449;
		case 22899: // Salvation.
			return 16465;
		case 23848: // Harmony.
			return 68605;
		case 22907: // Greater corruption.
			return 16464;
		case 22901: // Greater salvation.
			return 16524;
		case 23850: // Greater harmony.
			return 68610;
		case 22909: // SlayerMasterD corruption.
			return 16429;
		case 22903: // SlayerMasterD salvation.
			return 16450;
		case 23852: // SlayerMasterD harmony.
			return 68607;
		case 23874: // Supreme corruption.
		case 25477:
		case 25622:
		case 25554:
			return 68615;
		case 23876: // Supreme salvation.
			return 68611;
		case 23854: // Supreme harmony.
			return 68613;
		default:
			Logger.log("AurasManager", "Unknown wings: " + aura);
			return -1;
		}
	}

	public int getAuraModelId() {
		Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
		if (weapon == null)
			return 8719;
		String name = weapon.getDefinitions().getName().toLowerCase();
		if (name.contains("dagger"))
			return 8724;
		if (name.contains("whip"))
			return 8725;
		if (name.contains("2h sword") || name.contains("godsword"))
			return 8773;
		if (name.contains("sword") || name.contains("scimitar") || name.contains("korasi"))
			return 8722;
		return 8719;
	}

	public int getActiveGraphic(int tier) {
		if (tier == 2)
			return 1764;
		if (tier >= 3)
			return 1763;
		return 370; // default gold
	}

	public boolean hasPoisonPurge() {
		if (!isActivated())
			return false;
		int aura = player.getEquipment().getAuraId();
		return aura == 20958 || aura == 22268 || aura == 22917
				|| aura == 23862;
	}

	public double getMagicAccurayMultiplier() {
		if (!isActivated() || player.isCanPvp())
			return 1;
		int aura = player.getEquipment().getAuraId();
		if (aura == 20962)
			return 1.03;
		if (aura == 22270)
			return 1.05;
		if (aura == 22919)
			return 1.07;
		if (aura == 23864)
			return 1.1;
		return 1;
		
	}

	public double getDamageMultiplier() {
		if (!isActivated() || player.isCanPvp())
			return 1;
		int aura = player.getEquipment().getAuraId();
		if (aura == 25554)
			return 1.05;
		return 1;
	}
	
	public double getRangeAccurayMultiplier() {
		if (!isActivated() || player.isCanPvp())
			return 1;
		int aura = player.getEquipment().getAuraId();
		if (aura == 20967)
			return 1.03;
		if (aura == 22272)
			return 1.05;
		if (aura == 22921)
			return 1.07;
		if (aura == 23866)
			return 1.1;
		return 1;
	}

	public double getWoodcuttingAccurayMultiplier() {
		if (!isActivated())
			return 1;
		int aura = player.getEquipment().getAuraId();
		if (aura == 22280)
			return 1.03;
		if (aura == 22282)
			return 1.05;
		if (aura == 22915)
			return 1.07;
		if (aura == 23860)
			return 1.1;
		return 1;
	}

	public double getMininingAccurayMultiplier() {
		if (!isActivated())
			return 1;
		int aura = player.getEquipment().getAuraId();
		if (aura == 22284)
			return 1.03;
		if (aura == 22286)
			return 1.05;
		if (aura == 22913)
			return 1.07;
		if (aura == 23858)
			return 1.1;
		return 1;
	}

	public double getFishingAccurayMultiplier() {
		if (!isActivated())
			return 1;
		int aura = player.getEquipment().getAuraId();
		if (aura == 20966)
			return 1.03;
		if (aura == 22274)
			return 1.05;
		if (aura == 22923)
			return 1.07;
		if (aura == 23868)
			return 1.1;
		return 1;
	}

	public double getPrayerPotsRestoreMultiplier() {
		if (!isActivated())
			return 1;
		int aura = player.getEquipment().getAuraId();
		if (aura == 20965)
			return 1.03;
		if (aura == 22276)
			return 1.05;
		if (aura == 22925)
			return 1.07;
		if (aura == 23870)
			return 1.1;
		return 1;
	}

	public double getThievingAccurayMultiplier() {
		if (!isActivated())
			return 1;
		int aura = player.getEquipment().getAuraId();
		if (aura == 22288)
			return 1.03;
		if (aura == 22290)
			return 1.05;
		if (aura == 22911)
			return 1.07;
		if (aura == 23856)
			return 1.1;
		return 1;
	}

	public double getChanceNotDepleteMN_WC() {
		if (!isActivated())
			return 1;
		int aura = player.getEquipment().getAuraId();
		if (aura == 22292)
			return 1.1;
		return 1;
	}

	public boolean usingEquilibrium() {
		if (!isActivated() || player.isCanPvp())
			return false;
		int aura = player.getEquipment().getAuraId();
		return aura == 22294;
	}

	public boolean usingPenance() {
		if (!isActivated())
			return false;
		int aura = player.getEquipment().getAuraId();
		return aura == 22300;
	}

	/**
	 * Gets the prayer experience multiplier.
	 * 
	 * @return The prayer experience multiplier.
	 */
	public double getPrayerMultiplier() {
		if (!isActivated())
			return 1;
		int aura = player.getEquipment().getAuraId();
		switch (aura) {
		case 22905: // Corruption.
		case 22899: // Salvation.
		case 23848: // Harmony.
			return 1.01;
		case 22907: // Greater corruption.
		case 22901: // Greater salvation.
		case 23850: // Greater harmony.
			return 1.015;
		case 22909: // SlayerMasterD corruption.
		case 22903: // SlayerMasterD salvation.
		case 23852: // SlayerMasterD harmony.
			return 1.02;
		case 23874: // Supreme corruption.
		case 23876: // Supreme salvation.
		case 23854: // Supreme harmony.
			return 1.025;
		}
		return 1.0;
	}

	
	
	public double getDropMultiplier() {
		if (!isActivated())
			return 1;
		int aura = player.getEquipment().getAuraId();
		if (aura == 25477)
			return 1.1;
		if (aura == 25622)
			return 1.12;
		return 1;
	}
	
	/**
	 * Gets the amount of prayer points to restore (when getting 500 prayer
	 * experience).
	 * 
	 * @return The prayer restoration multiplier.
	 */
	public double getPrayerRestoration() {
		if (!isActivated())
			return 0;
		int aura = player.getEquipment().getAuraId();
		switch (aura) {
		case 22905: // Corruption.
		case 22899: // Salvation.
		case 23848: // Harmony.
			return 0.03;
		case 22907: // Greater corruption.
		case 22901: // Greater salvation.
		case 23850: // Greater harmony.
			return 0.05;
		case 22909: // SlayerMasterD corruption.
		case 22903: // SlayerMasterD salvation.
		case 23852: // SlayerMasterD harmony.
			return 0.07;
		case 23874: // Supreme corruption.
		case 23876: // Supreme salvation.
		case 23854: // Supreme harmony.
			return 0.1;
		}
		return 0;
	}

	public void checkSuccefulHits(int damage) {
		if (!isActivated() || player.isCanPvp() || damage == 0)
			return;
		int aura = player.getEquipment().getAuraId();
		if (aura == 22296)
			useInspiration();
		else if (aura == 22298)
			useVampyrism(damage);
	}

	public void useVampyrism(int damage) {
		int heal = (int) (damage * (Combat.hasCustomWeapon(player) ? 0.03 : 0.05));
		if (heal > 0)
			player.heal(heal);
	}

	public void useInspiration() {
		Integer atts = (Integer) player.getTemporaryAttributtes().get("InspirationAura");
		if (atts == null)
			atts = 0;
		atts++;
		if (atts == 5) {
			atts = 0;
			player.getCombatDefinitions().restoreSpecialAttack(1);
		}
		player.getTemporaryAttributtes().put("InspirationAura", atts);
	}

	public boolean usingWisdom() {
		if (!isActivated())
			return false;
		int aura = player.getEquipment().getAuraId();
		return aura == 22302;
	}

	/**
	 * Checks if the aura worn is a winged aura.
	 * 
	 * @return {@code True}.
	 */
	public boolean isWingedAura(int aura) {
		switch (aura) {
		case 22905: // Corruption.
		case 22899: // Salvation.
		case 23848: // Harmony.
		case 22907: // Greater corruption.
		case 22901: // Greater salvation.
		case 23850: // Greater harmony.
		case 22909: // SlayerMasterD corruption.
		case 22903: // SlayerMasterD salvation.
		case 23852: // SlayerMasterD harmony.
		case 23874: // Supreme corruption.
		case 25477: // Wings of wealth
		case 25622: // Supreme Wings of wealth
		case 25554:
		case 23876: // Supreme salvation.
		case 23854: // Supreme harmony.
			return true;
		}
		return false;
	}

	/*
	 * return seconds
	 */
	public static int getActivationTime(int aura) {
		/*switch (aura) {
		case 20958:
			return 600; // 10minutes
		case 22268:
			return 1200; // 20minutes
		case 22302:
			return 1800; // 30minutes
		case 22294:
			return 7200; // 2hours
		case 20959:
			return 10800; // 3hours
		default:
			return 3600; // default 1hour
		}*/
		return aura == -1 ? 0 : (int) (ItemConfig.forID(aura).getCSOpcode(1430) * 0.6);
	}

	public static int getCooldown(int aura) {
		/*switch (aura) {
		case 20962:
		case 22270:
		case 20967:
		case 22272:
		case 22280:
		case 22282:
		case 22284:
		case 22286:
		case 20966:
		case 22274:
		case 20965:
		case 22276:
		case 22288:
		case 22290:
		case 22292:
		case 22296:
		case 22298:
		case 22300:
			return 10800; // 3hours
		case 22294:
			return 14400; // 4hours
		case 20959:
		case 22302:
			return 86400; // 24hours
		default:
			return 10800; // default 3 hours - stated on
			// www.runescape.wikia.com/wiki/Aura
		}*/
		return aura == -1 ? 0 : (int) (ItemConfig.forID(aura).getCSOpcode(1429) * 0.6);
	}

	public static GeneralRequirementMap getAuraData(int aura) {
		//normal auras
		for (Object id : ClientScriptMap.getMap(5182).getValues().values()) {
			GeneralRequirementMap data = GeneralRequirementMap.getMap((int) id);
			if (data.getIntValue(1935) == aura)
				return data;
		}
		//cosmetic auras
		for (Object id : ClientScriptMap.getMap(5724).getValues().values()) {
			GeneralRequirementMap data = GeneralRequirementMap.getMap((int) id);
			if (data.getIntValue(1935) == aura)
				return data;
		}
		return null;
		
	}
	

	public static int getPrice(int aura) {
		if (aura == -1)
			return 0;
		GeneralRequirementMap data = getAuraData(aura);
		if (data != null)
			return data.getIntValue(1932);
		return 1;
	}
	
	public static int getTier(int aura) {
		if (aura == -1)
			return 1;
		GeneralRequirementMap data = getAuraData(aura);
		if (data != null)
			return data.getIntValue(1993);
		String name = ItemConfig.forID(aura).getName().toLowerCase();
		if (name.startsWith("supreme" ))
			return 4;
		if (name.startsWith("master "))
			return 3;
		if (name.startsWith("greater "))
			return 2;
		switch (aura) {
		case 25477:
		case 25622:
		case 25554:
		case 23874:
		case 23876:
		case 23854:
			return 4;
		case 22302:
		case 22909:
		case 22903:
		case 23852:
			return 3;
		case 22907:
		case 22901:
		case 23850:
		case 20959:
		case 22270:
		case 22272:
		case 22282:
		case 22286:
		case 22274:
		case 22276:
		case 22290:
		case 22292:
		case 22294:
		case 22296:
		case 22298:
		case 22300:
			return 2;
		default:
			return 1; // default 1
		}
	}
}
