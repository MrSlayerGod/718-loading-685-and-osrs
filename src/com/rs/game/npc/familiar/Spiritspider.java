package com.rs.game.npc.familiar;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.Summoning.Pouch;
import com.rs.utils.Utils;

public class Spiritspider extends Familiar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5995661005749498978L;

	public Spiritspider(Player owner, Pouch pouch, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	}

	@Override
	public String getSpecialName() {
		return "Egg Spawn";
	}

	@Override
	public String getSpecialDescription() {
		return "Spawns a random amount of red eggs around the familiar.";
	}

	@Override
	public int getBOBSize() {
		return 0;
	}

	@Override
	public int getSpecialAmount() {
		return 6;
	}

	@Override
	public SpecialAttack getSpecialAttack() {
		return SpecialAttack.CLICK;
	}

	@Override
	public boolean submitSpecial(Object object) {
		Player player = (Player) object;
		setNextAnimation(new Animation(8267));
		player.setNextAnimation(new Animation(7660));
		player.setNextGraphics(new Graphics(1316));
		WorldTile tile = this;
		// attemps to randomize tile by 4x4 area
		for (int trycount = 0; trycount < Utils.random(8)+1; trycount++) {
			tile = new WorldTile(this, 2);
			if (World.isTileFree(this.getPlane(), tile.getX(), tile.getY(), player.getSize()))
				continue;
			World.sendGraphics(player, new Graphics(1342), tile);
			World.addGroundItem(new Item(223, 1), tile, player, true, 120);
		}
		return true;
	}
}
