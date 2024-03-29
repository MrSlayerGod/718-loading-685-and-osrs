package com.rs.game.player.dialogues.impl;

import com.rs.game.WorldObject;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Smelting;
import com.rs.game.player.actions.Smelting.SmeltingBar;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogues.Dialogue;

public class SmeltingD extends Dialogue {

	private WorldObject object;

	@Override
	public void start() {
		object = (WorldObject) parameters[0];
		int[] ids = new int[SmeltingBar.NOVITE.ordinal()];
		for (int i = 0; i < ids.length; i++)
			ids[i] = SmeltingBar.values()[i].getProducedBar().getId();
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many bars would you like to smelt?<br>Choose a number, then click the bar to begin.", 28, ids, new ItemNameFilter() {
			int count = 0;

			@Override
			public String rename(String name) {
				SmeltingBar bar = SmeltingBar.values()[count++];
				if (player.getSkills().getLevel(Skills.SMITHING) < bar.getLevelRequired())
					name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + bar.getLevelRequired();
				return name;

			}
		});
	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.getActionManager().setAction(new Smelting(SmeltingBar.getBar(SkillsDialogue.getItemSlot(componentId)), object, SkillsDialogue.getQuantity(player)));
		end();
	}

	@Override
	public void finish() {
	}
}
