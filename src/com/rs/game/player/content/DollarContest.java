package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class DollarContest {

	public static final int FRAGMENT_ID = 25455, KEY_ID = 25456;
	
	public static String winner = "asdasdaada";
	
	public static void repair(Player player) {
		if (player.getInventory().getAmountOf(FRAGMENT_ID) < 100) {
			player.getPackets().sendGameMessage("You need 100 fragments in order to create a key.");
			return;
		}
		if (!player.getInventory().hasFreeSlots()) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return;
		}
		player.lock(1);
		player.gfx(905);
		player.getInventory().deleteItem(FRAGMENT_ID, 100);
		player.getInventory().addItem(KEY_ID, 1);
		int skill;
		while (true) {
			skill = Utils.random(Skills.SKILL_NAME.length);
			boolean combatSkill = skill == Skills.SUMMONING || (skill >= Skills.ATTACK && skill <= Skills.MAGIC);
			if (!combatSkill)
				break;
		}
		player.getPackets().sendGameMessage("The fragments seem to fuse as you push them together!");
		int xp = player.getSkills().getLevelForXp(skill) * 20;
		player.getPackets().sendGameMessage("You receive "+xp+" "+Skills.SKILL_NAME[skill]+" xp.");
		player.getSkills().addXp(skill, xp, true);
	}
	
	public static void open(Player player, WorldObject object) {
		if (player.getInventory().getAmountOf(KEY_ID) < 1) {
			player.getPackets().sendGameMessage("You need a pandora key to attempt this.");
			return;
		}
		player.getInventory().deleteItem(KEY_ID, 1);
		player.lock(7);
		player.setNextAnimation(new Animation(536));
		player.getPackets().sendGameMessage("You unlock the chest with your key.");
		World.sendGraphics(player, new Graphics(2015), new WorldTile(3088, 3500, 0));
		World.sendGraphics(player, new Graphics(2015), new WorldTile(3088, 3496, 0));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getPackets().sendGameMessage("The chest refuses to open and locks itself!");
				if (Utils.random(10) == 0 || winner == null/*|| (winner != null && player.getUsername().equals(winner))*/) {
					player.getPackets().sendGameMessage("The chest starts opening...");
					
					if (true /*|| winner != null && player.getUsername().equals(winner)*/) {
						World.sendNews(player.getDisplayName()+" JUST WON THE PANDORA CONTEST!!!!!!", World.SERVER_NEWS);
						World.removeObject(object);
						winner = "win";
					} else {
						player.getInventory().addItemMoneyPouch(new Item(995, 100000));
						player.useStairs(-1, Settings.START_PLAYER_LOCATION, 5, 6, "The chest reacts and teleports you. Better luck next time!", false);
					}
				} else {
					player.getInventory().addItemMoneyPouch(new Item(995, 50000));
					player.getPackets().sendGameMessage("Better luck next time!");
					player.gfx(1222);
					player.applyHit(new Hit(player, 50, HitLook.REFLECTED_DAMAGE));
				}
				//player.useStairs(-1, Settings.START_PLAYER_LOCATION, 1, 2, "Better luck next time!", false);
			}
		}, 6);
	}
	
	public static void info(Player player) {
		player.getPackets().sendOpenURL("https://onyxftw.com/forums/index.php?/topic/1658-onyxs-100-pandoras-chest-contest/");
	}
}
