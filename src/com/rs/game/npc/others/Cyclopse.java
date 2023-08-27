package com.rs.game.npc.others;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.minigames.WarriorsGuild;
import com.rs.game.npc.NPC;
import com.rs.game.npc.godwars.bandos.GodwarsBandosFaction;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Cyclopse extends GodwarsBandosFaction {

	public Cyclopse(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
	}

	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		if (source instanceof Player) {
			WarriorsGuild.killedCyclopses++;
			final NPC npc = this;
			final Player player = (Player) source;
			Controller controler = player.getControlerManager().getControler();
			final int id = WarriorsGuild.getBestDefender(player);
			if ((player.getRegionId() != 11319 && (controler == null || !(controler instanceof WarriorsGuild))) || Utils.random(id == 20072 ? 40 : 20) != 0)
				return;
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					player.setLootbeam(World.addGroundItem(new Item(id), new WorldTile(getCoordFaceX(npc.getSize()), getCoordFaceY(npc.getSize()), getPlane()), player, true, 60));
					if (id == 20072)
						World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + ItemConfig.forID(id).getName() + "<col=ff8c38> drop!", 1);
					
				}
			}, getCombatDefinitions().getDeathDelay());
		}
	}
}
