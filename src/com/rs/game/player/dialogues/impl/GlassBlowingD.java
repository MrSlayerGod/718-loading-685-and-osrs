package com.rs.game.player.dialogues.impl;

import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogues.Dialogue;

public class GlassBlowingD extends Dialogue {

	private static final int[][] LEVELS =
	{
	{ 1, 4, 12, 33, 42, 45, 49, 87 },
	{ 89 } };
	private static final double[][] EXPERIENCE =
	{
	{ 17.5, 19, 25, 35, 42.5, 52.5, 55, 70 },
	{ 100 } };
	private static final int[][] PRODUCTS =
	{
	{ 1919, 4527, 4522, 7534, 567, 4542, 10973 },
	{ 23191 } };

	private int index;

	@Override
	public void start() {
		index = (int) parameters[0];
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to make,<br>then click on the item to begin.", 28, PRODUCTS[index], new ItemNameFilter() {

			int count = 0;

			@Override
			public String rename(String name) {
				int levelRequired = LEVELS[index][count++];
				if (player.getSkills().getLevel(Skills.CRAFTING) < levelRequired)
					name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + levelRequired;
				return name;
			}
		});
	}

	@Override
	public void run(int interfaceId, int componentId) {
		final int componentIndex = SkillsDialogue.getItemSlot(componentId);
		if (componentIndex > PRODUCTS[index].length) {
			end();
			return;
		}
		player.getActionManager().setAction(new Action() {

			int ticks;

			@Override
			public boolean start(final Player player) {
				if (!checkAll(player))
					return false;
				int moltenGlassCount = player.getInventory().getAmountOf(index == 0 ? 1775 : 23193);
				int requestedAmount = SkillsDialogue.getQuantity(player);
				if (requestedAmount > moltenGlassCount)
					requestedAmount = moltenGlassCount;
				this.ticks = requestedAmount;
				return true;
			}

			public boolean checkAll(Player player) {
				final int levelReq = LEVELS[index][componentIndex];
				if (player.getSkills().getLevel(Skills.CRAFTING) < levelReq) {
					player.getPackets().sendGameMessage("You need a Crafting level of " + levelReq + " to create this.");
					return false;
				} else if (!player.getInventory().containsItemToolBelt(1785)) {
					player.getPackets().sendGameMessage("You need a glassblowing pipe in order to create glass items.");
					return false;
				}
				return true;
			}

			@Override
			public boolean process(Player player) {
				return checkAll(player);
			}

			@Override
			public int processWithDelay(Player player) {
				ticks--;
				player.getInventory().deleteItem(new Item(index == 0 ? 1775 : 23193, 1));
				player.getInventory().addItem(new Item(PRODUCTS[index][componentIndex]));
				player.getSkills().addXp(Skills.CRAFTING, EXPERIENCE[index][componentIndex]);
				if (ticks % 2 == 0)
					player.setNextAnimation(new Animation(884));
				if (ticks > 0)
					return 2;
				return -1;
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
