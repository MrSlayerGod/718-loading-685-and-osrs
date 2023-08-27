package com.rs.game.player.dialogues.impl;

import com.rs.Settings;
import com.rs.game.minigames.ShootingStars;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class StarSpriteD extends Dialogue {


	@Override
	public void start() {
		if(player.getInventory().containsOneItem(ShootingStars.STARDUST) && (Utils.currentTimeMillis() - player.getLastStarSprite()) > (/*(player.isDonator() ? player.isRubyDonator() ? 1 : 12 : 24)**/60*60*1000)) {
			sendNPCDialogue(ShootingStars.SPRITE, NORMAL, "Thank you for helping me out of here.");
			stage = 0;
		}else{
			sendNPCDialogue(ShootingStars.SPRITE, NORMAL, "I'm a star sprite! I was in my star in the sky, when it lost control and crashed into the ground. With half my star sticking into the ground, I became stuck. Fortunately, I was mined out by the kind creatures of");
			stage = 1;
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case 0:
			int stardust = player.getInventory().getAmountOf(ShootingStars.STARDUST);
			if(stardust > 400)
				stardust = 400;
			player.getInventory().deleteItem(ShootingStars.STARDUST, stardust);
			int amtCosmic = stardust*152 / 200 * Settings.getDropQuantityRate();
			int amtAstral = stardust*52 / 200 * Settings.getDropQuantityRate();
			int amtGold = stardust*20 / 200 * Settings.getDropQuantityRate();
			int amtCoins = stardust*50002 / 200 * 10; //10x coin reward
			player.getInventory().addItemDrop(564, amtCosmic);
			player.getInventory().addItemDrop(9075, amtAstral);
			player.getInventory().addItemDrop(445, amtGold);
			player.getInventory().addItemDrop(995, amtCoins);
			player.setLastStarSprite(Utils.currentTimeMillis());
			sendNPCDialogue(ShootingStars.SPRITE, NORMAL, "I have rewarded you by mine extra ore for the next 15 minutes. Also, have "+amtCosmic+" cosmic runes, "+amtAstral+" astral runes, "+amtGold+" gold ores and "+amtCoins+" coins.");
			stage = -2;
			break;
		case 1:
			sendNPCDialogue(ShootingStars.SPRITE, NORMAL, "your race.");
			stage = 2;
			break;
		case 2:
			sendPlayerDialogue(NORMAL, "Well, I'm glad you're okay.");
			stage = -2;
			break;
		default:
			end();
			break;
		}
	}

	@Override
	public void finish() {

	}
}
