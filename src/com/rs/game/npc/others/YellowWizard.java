package com.rs.game.npc.others;

import java.util.ArrayList;
import java.util.List;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.collectionlog.CategoryType;
import com.rs.game.player.content.collectionlog.CollectionLog;
import com.rs.game.player.controllers.RunespanControler;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class YellowWizard extends NPC {

	private RunespanControler controler;
	private long spawnTime;

	public YellowWizard(WorldTile tile, RunespanControler controler) {
		super(15430, tile, -1, true, true);
		spawnTime = Utils.currentTimeMillis();
		this.controler = controler;
	}

	@Override
	public void processNPC() {
		if (spawnTime + 300000 < Utils.currentTimeMillis())
			finish();
	}

	@Override
	public void finish() {
		controler.removeWizard();
		super.finish();
	}

	public static void giveReward(Player player) {
		player.getPackets().sendGameMessage("The wizard rewards you with runecrafting experience.");
		player.getSkills().addXp(Skills.RUNECRAFTING, player.getSkills().getLevel(Skills.RUNECRAFTING)*20);
		if (Utils.random(100) == 0 && !player.containsItem(13629)) {
			player.getPackets().sendGameMessage("Wow! The wizard seems to have given you his staff.");
			player.getInventory().addItemDrop(13642, 1);
			player.getCollectionLog().add(CategoryType.MINIGAMES, "Runespan", new Item(13642));
			World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + ItemConfig.forID(13642).getName() + "<col=ff8c38> from <col=cc33ff>runespan<col=ff8c38>!", 1);

		}
		dropSet(player);
		player.getHintIconsManager().removeUnsavedHintIcon();
	}
	
	public static final Integer[] PIECES = {21484, 21485, 21486, 21487};
	
	private static void dropSet(Player player) {
		List<Integer> pieces = new ArrayList<Integer>();
		for (int i : PIECES)
			if (!player.containsItem(i))
				pieces.add(i);
		if (pieces.isEmpty())
			return;
		int piece = pieces.get(Utils.random(pieces.size()));
		player.getCollectionLog().add(CategoryType.MINIGAMES, "Runespan", new Item(piece));
		player.getPackets().sendGameMessage("You feel your inventory getting heavier.");
		player.getInventory().addItemDrop(piece, 1);
		World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + ItemConfig.forID(piece).getName() + "<col=ff8c38> from <col=cc33ff>runespan<col=ff8c38>!", 1);
	}


	@Override
	public boolean withinDistance(Player tile, int distance) {
		return tile == controler.getPlayer() && super.withinDistance(tile, distance);
	}

}
