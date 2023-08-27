package com.rs.game.player.dialogues.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogues.Dialogue;

public class EnchantingOrbsDialogue extends Dialogue {

	private final static int UNCHARGED_ORB = 567;
	public final static int[][] REQUIRED_RUNES =
	{
	{ 564, 3, 556, 30, UNCHARGED_ORB, 1 },
	{ 564, 3, 555, 30, UNCHARGED_ORB, 1 },
	{ 564, 3, 557, 30, UNCHARGED_ORB, 1 },
	{ 564, 3, 554, 30, UNCHARGED_ORB, 1 } };
	public final static int[] LEVELS =
	{ 66, 56, 60, 63 }, ORBS =
	{ 573, 571, 575, 569 }, GRAPHICS =
	{ 150, 149, 151, 152 }, OBJECTS =
	{ 2152, 2151, 29415, 74862 }, COMPONENTS =
	{ 74, 60, 64, 71 };
	private final static double[] EXPERIENCE =
	{ 76, 63, 70, 73 };

	private int index;

	@Override
	public void start() {
		index = (int) parameters[0];
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to make,<br>then click on the item to begin.", 28, new int[]
		{ ORBS[index] }, new ItemNameFilter() {

			@Override
			public String rename(String name) {
				int levelRequired = LEVELS[index];
				if (player.getSkills().getLevel(Skills.CRAFTING) < levelRequired)
					name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + levelRequired;
				return name;
			}
		});
	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.getActionManager().setAction(new Action() {

			int ticks;

			@Override
			public boolean start(final Player player) {
				if (!checkAll(player))
					return false;
				int unchargedAmount = player.getInventory().getAmountOf(UNCHARGED_ORB);
				int ticks = SkillsDialogue.getQuantity(player);
				if (ticks > unchargedAmount)
					ticks = unchargedAmount;
				this.ticks = ticks;
				return true;
			}

			public boolean checkAll(Player player) {
				final int levelRequired = LEVELS[index];
				if (player.getSkills().getLevel(Skills.MAGIC) < levelRequired) {
					player.getPackets().sendGameMessage("You need a Magic level of " + levelRequired + " in order to enchant this type of orb.");
					return false;
				}
				return true;
			}

			@Override
			public boolean process(Player player) {
				return checkAll(player) && ticks > 0;
			}

			@Override
			public int processWithDelay(Player player) {
				ticks--;
				if (!Magic.checkRunes(player, true, REQUIRED_RUNES[index]))
					return -1;
				player.getInventory().addItem(new Item(ORBS[index]));
				player.setNextAnimation(new Animation(726));
				player.setNextGraphics(new Graphics(GRAPHICS[index], 0, 100));
				player.getSkills().addXp(Skills.MAGIC, EXPERIENCE[index]);
				return 4;
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 3);
			}
		});
		end();
	}

	@Override
	public void finish() {

	}
}
