package com.rs.game.npc.familiar;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.Summoning.Pouch;
import com.rs.game.player.controllers.Wilderness;

public class Packyak extends Familiar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1397015887332756680L;

	public Packyak(Player owner, Pouch pouch, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(owner, pouch, tile, mapAreaNameHash, false);
	}

	@Override
	public int getSpecialAmount() {
		return 12;
	}

	@Override
	public String getSpecialName() {
		return "Winter Storage";
	}

	@Override
	public String getSpecialDescription() {
		return "Use special move on an item in your inventory to send it to your bank.";
	}

	@Override
	public SpecialAttack getSpecialAttack() {
		return SpecialAttack.ITEM;
	}

	@Override
	public int getBOBSize() {
		return 30;
	}

	@Override
	public boolean isAgressive() {
		return false;
	}

	@Override
	public boolean submitSpecial(Object object) {
		int slotId = (Integer) object;
		Item item = getOwner().getInventory().getItem(slotId);
		if (item == null)
			return false;
		if (!ItemConstants.isTradeable(item)) {
			getOwner().getPackets().sendGameMessage("You can not bank this item.");
			return false;
		}
		if (Wilderness.isAtWild(getOwner()) && Wilderness.getWildLevel(getOwner()) >= 30) {
			getOwner().getPackets().sendGameMessage("A mysterious force prevents you from teleporting this item.");
			return false;
		}
		if (getOwner().getBank().hasBankSpace()) {
			getOwner().getBank().depositItem(slotId, 1, false);
			getOwner().getPackets().sendGameMessage("Your Pack Yak has sent an item to your bank.");
			getOwner().setNextGraphics(new Graphics(1316));
			getOwner().setNextAnimation(new Animation(7660));
			return true;
		}
		return false;
	}
}
