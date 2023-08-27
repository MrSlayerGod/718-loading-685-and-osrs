package com.rs.game.npc.familiar;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Summoning.Pouch;
import com.rs.game.player.content.dungeoneering.DungeonConstants;
import com.rs.utils.Utils;

public class Naivehoardstalker extends Familiar {

	private static final long serialVersionUID = -7037718748109234870L;
	private int forageTicks;

	public Naivehoardstalker(Player owner, Pouch pouch, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		forageTicks++;
		if (forageTicks == 300) {
			forageTicks = 0;
			getBob().getBeastItems().add(new Item(DungeonConstants.HOARDSTALKER_ITEMS[2][Utils.random(5)], 1));
		}
	}

	@Override
	public String getSpecialName() {
		return "Aptitude";
	}

	@Override
	public String getSpecialDescription() {
		return "Boosts all of your non-combat skills by 3.";
	}

	@Override
	public int getBOBSize() {
		return 30;
	}

	@Override
	public int getSpecialAmount() {
		return 20;
	}

	@Override
	public SpecialAttack getSpecialAttack() {
		return SpecialAttack.CLICK;
	}

	@Override
	public boolean submitSpecial(Object object) {
		Player player = (Player) object;
		player.setNextGraphics(new Graphics(1300));
		player.setNextAnimation(new Animation(7660));
		for (int skill = 0; skill < Skills.SKILL_NAME.length; skill++) {
			if (skill == Skills.SUMMONING || skill == Skills.ATTACK || skill == Skills.DEFENCE || skill == Skills.STRENGTH || skill == Skills.RANGE || skill == Skills.MAGIC || skill == Skills.PRAYER || skill == Skills.DUNGEONEERING)
				continue;
			player.getSkills().set(skill, player.getSkills().getLevelForXp(skill) + 3);
		}
		return true;
	}
}
