package com.rs.game.npc.corp;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

@SuppressWarnings("serial")
public class CorporealBeast extends NPC {

	private DarkEnergyCore core;

	public CorporealBeast(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setLureDelay(3000);
		setIntelligentRouteFinder(true);
		setCapDamage(2000);
		setDropRateFactor(2.4);
	}

	public void spawnDarkEnergyCore() {
		if (core != null)
			return;
		core = new DarkEnergyCore(this);
	}

	public void removeDarkEnergyCore() {
		if (core == null)
			return;
		core.finish();
		core = null;
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (isDead())
			return;
		checkReset();
	}

	public void checkReset() {
		int maxhp = getMaxHitpoints();
		if (maxhp > getHitpoints() && !isUnderCombat() && getPossibleTargets().isEmpty())
			setHitpoints(maxhp);
	}

	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		if (core != null)
			core.sendDeath(source);
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		super.handleIngoingHit(hit);
		reduceHit(hit);
	}
	
	
	public void reduceHit(Hit hit) {
		if (!(hit.getSource() instanceof Player) || (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE))
			return;
		Player from = (Player) hit.getSource();
		int weaponId = from.getEquipment().getWeaponId();
		String name = weaponId == -1 ? "null" : ItemConfig.forID(weaponId).getName().toLowerCase();
		if(hit.getLook() != HitLook.MELEE_DAMAGE || !(name.contains("spear") || name.contains("halberd"))) 
			hit.setDamage(hit.getDamage()/2);
		
	}
	@Override
	public double getMagePrayerMultiplier() {
		return 0.6;
	}

}
