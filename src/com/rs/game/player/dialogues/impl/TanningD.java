package com.rs.game.player.dialogues.impl;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.item.Item;
import com.rs.game.player.Skills;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogues.Dialogue;

public class TanningD extends Dialogue {

	public static final int[][] TANNING_PRICES =
		{
		{ 0, 3, 15, 20, 20, 20, 20, 20, 20 },
		{ 2, 5, 25, 45, 45, 45, 45, 45, 45 } };
	public static final int[] INGREDIENT =
		{ 1739, 1739, 6287, 1753, 1751, 1749, 1747, 24372 };
	public static final int[] PRODUCT =
		{ 1741, 1743, 6289, 1745, 2505, 2507, 2509, 24374 };
	public static final int[] LEVELS =
		{ 1, 28, 45, 45, 57, 66, 73, 79, 87 };

	private boolean isCanfis;
	private int npcId;

	@Override
	public void start() {
		npcId = (int) parameters[0];
		isCanfis = npcId == 1041;
		stage = 0;
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many hides would you like to tan?<br>Choose a number, then click the hide to begin.", 28, PRODUCT, new ItemNameFilter() {
			int count = 0;

			@Override
			public String rename(String name) {
				int levelRequired = LEVELS[count++];
				if (player.getSkills().getLevel(Skills.CRAFTING) < levelRequired)
					name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + levelRequired;
				return name;
			}
		});
	}

	@Override
	public void run(int interfaceId, final int componentId) {
		if (stage == 0) {
			final int componentIndex = SkillsDialogue.getItemSlot(componentId);
			if (componentIndex > INGREDIENT.length) {
				end();
				return;
			}
			int price = TANNING_PRICES[isCanfis ? 1 : 0][componentIndex];
			int leatherAmount = player.getInventory().getAmountOf(INGREDIENT[componentIndex]);
			if (leatherAmount == 0) {
				sendNPCDialogue(npcId, NORMAL, "Ahhh... novice mistake, you must bring me at least one " + ItemConfig.forID(INGREDIENT[componentIndex]).getName().toLowerCase() + " in order to tan " + ItemConfig.forID(PRODUCT[componentIndex]).getName().toLowerCase() + ".");
				stage = 1;
				return;
			}
			final int levelReq = LEVELS[componentIndex];
			if (player.getSkills().getLevel(Skills.CRAFTING) < levelReq) {
				sendNPCDialogue(npcId, NORMAL, "Young traveler! You aren't skilled enough to make THIS yet. Come back when you have a Crafting level of at least " + levelReq + ".");
				stage = 1;
				return;
			}
			int requestedAmount = SkillsDialogue.getQuantity(player);
			long maximumQuantity = price == 0 ? requestedAmount : player.getInventory().getCoinsAmount() / price;
			if (requestedAmount > leatherAmount)
				requestedAmount = leatherAmount;
			if (requestedAmount > maximumQuantity)
				requestedAmount = (int) maximumQuantity;
			if (requestedAmount == 0) {
				player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId, "You don't have enough coins to cover the costs, return to me once you've obtained the proper amount.");
				stage = 1;
				return;
			}
			player.getInventory().deleteItem(new Item(INGREDIENT[componentIndex], requestedAmount));
			player.getInventory().addItem(new Item(PRODUCT[componentIndex], requestedAmount));
			price = TANNING_PRICES[isCanfis ? 1 : 0][componentIndex] * requestedAmount;
			if (price != 0)
				player.getInventory().removeItemMoneyPouch(new Item(995, price));
			end();
		} else
			end();
	}

	@Override
	public void finish() {
	}
}
