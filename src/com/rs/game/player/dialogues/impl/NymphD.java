/**
 * 
 */
package com.rs.game.player.dialogues.impl;

import java.util.ArrayList;
import java.util.List;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.minigames.LavaFlowMine;
import com.rs.game.npc.others.LiquidGoldNymph;
import com.rs.game.player.Equipment;
import com.rs.game.player.Skills;
import com.rs.game.player.content.collectionlog.CategoryType;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Mar 8, 2018
 */
public class NymphD extends Dialogue {
	
	private LiquidGoldNymph nymph;
	private boolean givenItem;

	@Override
	public void start() {
		nymph = (LiquidGoldNymph) parameters[0];
		nymph.setInteracting(true);
		player.setNextAnimation(new Animation(-1));
		if(givenItem) {
			sendPlayerDialogue(9827, "Thank you.");
			stage = 100;
			return;
		}
		sendNPCDialogue(nymph.getId(), 9827, getDialogue());
	}
	
	public String getDialogue() {
		String dialogue = null;
		switch(getGoldAmount()) {
		case 0:
			dialogue = "Oh, darling, that outfit you're wearing is so DREARY. Woudln't you rather wear something STYLISH and GOLD?";
			break;
		case 1:
			dialogue = "You're wearing a LITTLE gold... I suppose that's a START. Would you like another piece of the outfit?";
			break;
		case 2:
			dialogue = "You need more gold, darling! More GOLD! I have another piece of the outfit for you here.";
			break;
		case 3:
			dialogue = "You're starting to look good, darling! You should add more pieces to the gold outfit! Would you like another?";
			break;
		case 4:
			dialogue = "Oh, you're looking VERY good INDEED. There's just one thing missing, though, I think. Would you like the final piece?";
			break;
		case 5:
			boolean wearing = true;
			for(int item : LavaFlowMine.MINING_SUIT)
				if(!player.getEquipment().containsOneItem(item))
					wearing = false;
			if(!wearing)
				dialogue = "I've given you a LOVELY golden outfit, but you're not wearing it! Never mind, never mind.";
			else
				dialogue = "That is a LOVELY golden outfit you're wearing.";
			if(hasPickaxe())
				if(wearing)
					dialogue += " Would you like me to add some gold to your pickaxe to make it complete?";
				else
					dialogue += " Would you like me to add some gold to your pickaxe?";
			else
				dialogue += " Would you like some mining experience?";
			break;
		}
		return dialogue;
	}
	
	public boolean containsItem(int item, boolean bank) {
		if(!player.getInventory().containsItem(item, 1) && !player.getEquipment().containsOneItem(item)) {
			if(!bank)
				return false;
			else
				return player.getBank().containsItem(item);
		}
		return true;
	}
	
	public boolean hasPickaxe() {
		for(int item : LavaFlowMine.PICKAXES)
			if(containsItem(item, false))
				return true;
		return false;
	}
	
	public int getBestPickaxeIndex() {
		int index = 0;
		for(int i = 0; i < LavaFlowMine.PICKAXES.length; i++) {
			int item = LavaFlowMine.PICKAXES[i];
			if(containsItem(item, false))
				index = i;
		}
		return index;
	}
	
	public boolean wearingBestPickaxe() {
		int index = 0;
		for(int i = 0; i < LavaFlowMine.PICKAXES.length; i++) {
			int item = LavaFlowMine.PICKAXES[i];
			if(containsItem(item, false))
				index = i;
		}
		return player.getEquipment().getWeaponId() == LavaFlowMine.PICKAXES[index];
	}
	
	public int getGoldAmount() {
		int amount = 0;
		for(int item : LavaFlowMine.MINING_SUIT)
			if(containsItem(item, true))
				amount++;
		return amount;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if(stage == -1) {
			sendOptionsDialogue("Choose an option", "Yes, please.", "No, thank you.");
			stage = 0;
		} else if(stage == 0) {
			if(componentId == OPTION_1) {
				if(getGoldAmount() == 5 && !hasPickaxe()) {
					player.getSkills().addXp(Skills.MINING, /*250000*/2500);
					sendNPCDialogue(nymph.getId(), 9827, "Let's hope this experience helps you with your mining.");
					givenItem = true;
					stage = 1;
					return;
				} else if(getGoldAmount() == 5 && hasPickaxe()) {
					if(wearingBestPickaxe()) {
						int index = getBestPickaxeIndex();
						player.getEquipment().getItems().set(Equipment.SLOT_WEAPON, new Item(LavaFlowMine.GILDED[index]));
						player.getEquipment().refresh(Equipment.SLOT_WEAPON);
						player.getAppearence().generateAppearenceData();
						sendNPCDialogue(nymph.getId(), 9827, "See, that just looks WONDERFUL!");
						givenItem = true;
						stage = 1;
						return;
					} else {
						int index = getBestPickaxeIndex();
						player.getInventory().deleteItem(LavaFlowMine.PICKAXES[index], 1);
						player.getInventory().addItem(LavaFlowMine.GILDED[index], 1);
						sendNPCDialogue(nymph.getId(), 9827, "See, that just looks WONDERFUL!");
						givenItem = true;
						stage = 1;
						return;
					}
				} else if(getGoldAmount() < 5) {
					if(!player.getInventory().hasFreeSlots()) {
						sendNPCDialogue(nymph.getId(), 9827, "You don't have enough spaces... talk to me later.");
						stage = 2;
						return;
					}
					//for(int item : LavaFlowMine.MINING_SUIT) {
						int item = dropSet();
						if(/*!containsItem(item, true)*/item != -1) {
							player.getInventory().addItem(item, 1);
							player.getCollectionLog().add(CategoryType.MINIGAMES, "Lava Flow Mine", new Item(item));
							World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + ItemConfig.forID(item).getName() + "<col=ff8c38> from <col=cc33ff>lava flow mine<col=ff8c38>!", 1);
							sendNPCDialogue(nymph.getId(), 9827, "You're going to look VERY good in this new gold.");
							givenItem = true;
							stage = 1;
						//	return;
						}
					//}
				}
			} else if(componentId == OPTION_2) {
				sendNPCDialogue(nymph.getId(), 9827, "Your choice. I'll ask again later.");
				stage = 100;
				return;
			}
		} else if(stage == 1) {
			sendPlayerDialogue(9827, "Thank you!");
			stage = 100;
		} else if(stage == 100) {
			nymph.finish();
			end();
		} else
			end();
	}


	private int dropSet() {
		List<Integer> pieces = new ArrayList<Integer>();
		for (int i : LavaFlowMine.MINING_SUIT)
			if (!player.containsItem(i))
				pieces.add(i);
		if (pieces.isEmpty())
			return -1;

		return pieces.get(Utils.random(pieces.size()));
	}
	
	
	
	
	@Override
	public void finish() {
		if(givenItem)
			nymph.finish();
		if(nymph != null && !nymph.hasFinished() && !nymph.isDead())
			nymph.setInteracting(false);
	}

}
