package com.rs.game.npc.others;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.grotesque.Dusk;
import com.rs.game.player.Player;

@SuppressWarnings("serial")
public class ConditionalDeath extends NPC {

	private int[] requiredItems;
	private String deathMessage;
	private boolean checkInventory, lastLegs;

	public ConditionalDeath(int[] requiredItem, String deathMessage, boolean checkInventory, int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		this.requiredItems = requiredItem;
		this.deathMessage = deathMessage;
		this.checkInventory = checkInventory;
	}

	public ConditionalDeath(int requiredItem, String deathMessage, boolean checkInventory, int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		this(new int[]
		{ requiredItem }, deathMessage, checkInventory, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	public int[] getRequiredItem() {
		return requiredItems;
	}

	private boolean removeItem(Player player) {
		if (!checkInventory)
			return true;
		for (int requiredItem : requiredItems) {
			if (player.getInventory().containsItem(requiredItem, 1)) {
				ItemConfig defs = ItemConfig.forID(requiredItem);
				if (checkInventory && defs.isStackable())
					player.getInventory().deleteItem(requiredItem, 1);
				return true;
			}
		}
		return false;
	}

	public boolean useHammer(Player player) {
		if (isDead() || !lastLegs)
			return false;
		if (removeItem(player)) {
			if (deathMessage != null)
				player.getPackets().sendGameMessage(deathMessage, true);
			if (getId() == 14849)
				player.setNextAnimation(new Animation(15845));
			setHitpoints(0);
			if (this instanceof Dusk) //exception
				((Dusk)this).sendDeath2(this);
			else
				super.sendDeath(player);
			lastLegs = false;
			return true;
		}
		return false;
	}

	@Override
	public void processHit(Hit hit) {
		super.processHit(hit);
		if (getHitpoints() < (getMaxHitpoints() * 0.1) && (!(this instanceof Dusk) || getId() == Dusk.ID_PHASE_4)) {
			Entity source = hit.getSource();
			if (source instanceof Player) {
				Player player = (Player) source;
				if (!lastLegs) {
					lastLegs = true;
					player.getPackets().sendGameMessage((getId() == Dusk.ID_PHASE_4 ? "Dusk" : "The " + getName()) + " is on its last legs! Finish it quickly!", true);
				}
				boolean canContinue = player.getSlayerManager().hasLearnedQuickBlows() || checkInventory;
				if (!canContinue) {
					for (int requiredItem : requiredItems) {
						if (player.getEquipment().getWeaponId() == requiredItem || player.getEquipment().getGlovesId() == requiredItem) {
							canContinue = true;
							break;
						}
					}
					if (getName().equalsIgnoreCase("Turoth")) {
						int ammoId = player.getEquipment().getAmmoId();
						if (hit.getLook() == HitLook.RANGE_DAMAGE && (ammoId == 13280 || ammoId == 4160))
							canContinue = true;
					}
				}
				if (canContinue && useHammer(player))
					return;
			}
		}
	}

	@Override
	public void sendDeath(Entity source) {
		setHitpoints(1);
	}
}
