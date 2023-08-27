package com.rs.game;

import java.io.Serializable;

import com.rs.game.Hit.HitLook;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.theatreOfBlood.verzikVitur.NycolasAthanatos;
import com.rs.game.player.Player;
import com.rs.game.player.content.Drinkables;
import com.rs.utils.Utils;

public final class Poison implements Serializable {

	private static final long serialVersionUID = -6324477860776313690L;

	private transient Entity entity;
	private int poisonDamage;
	private int poisonCount;
	private boolean venom;

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}

	public void makeEnvenomed(int startDamage) {
		if (poisonDamage > startDamage && venom)
			return;
		if (entity instanceof Player) {
			Player player = ((Player) entity);
			if (player.getPoisonImmune() > Utils.currentTimeMillis() || player.getEquipment().getShieldId() == 18340 || player.getEquipment().getHatId() == 42931)
				return;
			if (poisonDamage == 0 || !venom)
				player.getPackets().sendGameMessage("<col=00ff00>You are envenomed.");
		} else if (entity instanceof NPC) {
			NPC npc = ((NPC)entity);
			if (npc.isIntelligentRouteFinder() || npc.getCombatLevel() >= 150) { //boss or high lvl mob
				makePoisoned(startDamage);
				return;
			}
			return;
		}
		poisonDamage = startDamage;
		venom = true;
		refresh();
	}
	
	public void makePoisoned(int startDamage) {
		if (poisonDamage > startDamage)
			return;
		if (entity instanceof Player) {
			Player player = ((Player) entity);
			if (player.getPoisonImmune() > Utils.currentTimeMillis() || player.getEquipment().getShieldId() == 18340 || player.getEquipment().getHatId() == 42931)
				return;
			if (poisonDamage == 0)
				player.getPackets().sendGameMessage("<col=00ff00>You are poisoned.");
		} else if (entity instanceof NPC) {
			NPC npc = ((NPC)entity);
			if (npc.isIntelligentRouteFinder() && !(npc instanceof NycolasAthanatos))  //boss
				return;
		}
		poisonDamage = startDamage;
		refresh();
	}

	public boolean healPoison() {
		if (!(entity instanceof Player))
			return false;
		Player player = (Player) entity;
		if (!isPoisoned()) {
			player.getPackets().sendGameMessage("You're not currently in need of curing.");
			return false;
		}
		for (int i = 0; i < 28; i++) {
			Item item = player.getInventory().getItem(i);
			if (item == null || (!Drinkables.Drink.ANTIPOISON_POTION.contains(item.getId()) && !Drinkables.Drink.SUPER_ANTIPOISON.contains(item.getId()) && !Drinkables.Drink.ANTIPOISON_FLASK.contains(item.getId()) && !Drinkables.Drink.SUPER_ANTIPOISON_FLASK.contains(item.getId())))
				continue;
			Drinkables.drink(player, item, i);
			return true;
		}
		player.getPackets().sendGameMessage("You don't have anything to cure the poison.");
		return false;

	}

	public void processPoison() {
		if (!entity.isDead() && isPoisoned()) {
			if (poisonCount > 0) {
				poisonCount--;
				return;
			}
			boolean heal = false;
			if (entity instanceof Player) {
				Player player = ((Player) entity);
				// inter opened we dont poison while inter opened like at rs
				if (player.getInterfaceManager().containsScreenInter())
					return;
				if (player.getAuraManager().hasPoisonPurge())
					heal = true;
			}
			entity.applyHit(new Hit(entity, poisonDamage, heal ? HitLook.HEALED_DAMAGE : HitLook.POISON_DAMAGE));
			if (venom) {
				if (poisonDamage >= 200)
					reset();
				else
					poisonDamage += 2;
			} else
				poisonDamage -= 2;
			if (isPoisoned()) {
				poisonCount = 30;
				return;
			}
			reset();
		}
	}

	public void reset() {
		poisonDamage = 0;
		poisonCount = 0;
		venom = false;
		refresh();
	}

	public void refresh() {
		if (entity instanceof Player) {
			Player player = ((Player) entity);
			player.getVarsManager().sendVar(102, isPoisoned() ? 1 : 0);
		}
	}

	public boolean isPoisoned() {
		return poisonDamage >= 1;
	}
	
	public boolean isEnvenomed() {
		return poisonDamage >= 1 && venom;
	}
	
	public boolean isImmune() {
		if (!(entity instanceof Player))
			return false;
		Player player = (Player) entity;
		return player.getPoisonImmune() > Utils.currentTimeMillis() || player.getEquipment().getShieldId() == 18340 || player.getEquipment().getHatId() == 42931;
	}
}
