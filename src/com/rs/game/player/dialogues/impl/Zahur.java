package com.rs.game.player.dialogues.impl;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.HerbCleaning;
import com.rs.game.player.actions.Herblore;
import com.rs.game.player.content.Drinkables;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.net.decoders.handlers.NPCHandler;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Optional;

import static com.rs.game.player.content.Drinkables.Drink.*;

public class Zahur extends Dialogue {

	private int npcId;
	private DecimalFormat formatter = new DecimalFormat("#,###");

	private static final int PRICE_PER_POT = 1000;
	private static final int PRICE_PER_GRIMY_HERB = 200;
	private static final int ZAHUR_ID = 24753;

	private static final Drinkables.Drink[][] POTION_TO_FLASK = {
			{SARADOMIN_BREW_POTION, SARADOMIN_BREW_FLASK},
			{SUPER_RESTORE_POTION, SUPER_RESTORE_FLASK},
			{OVERLOAD_POTION, OVERLOAD_FLASK},
			{SANFEW_SERUM_POTION, SANFEW_SERUM_FLASK},
			{PRAYER_RENEWAL_POTION, PRAYER_RENEWAL_FLASK},
			{PRAYER_POTION, PRAYER_FLASK},
			{SUPER_PRAYER_POTION, SUPER_PRAYER_FLASK},
			{EXTREME_ATTACK_POTION, EXTREME_ATTACK_FLASK},
			{EXTREME_STRENGTH_POTION, EXTREME_STRENGTH_FLASK},
			{EXTREME_DEFENCE_POTION, EXTREME_DEFENCE_FLASK},
			{EXTREME_MAGIC_POTION, EXTREME_MAGIC_FLASK},
			{EXTREME_RANGING_POTION, EXTREME_RANGING_FLASK},
			{EXTREME_ATTACK_POTION, EXTREME_MAGIC_FLASK},
			{ATTACK_POTION, ATTACK_FLASK},
			{STRENGTH_POTION, STRENGTH_FLASK},
			{DEFENCE_POTION, DEFENCE_FLASK},
			{RANGING_POTION, RANGING_FLASK},
			{MAGIC_POTION, MAGIC_FLASK},
			{ANTIFIRE_FLASK, ANTIFIRE_FLASK},
			{SUPER_ANTIPOISON, ANTIPOISON_PLUS_FLASK},
			{ANTIPOISON_DOUBLE_PLUS, ANTIPOISON_DOUBLE_PLUS_Flask},
			{RECOVER_SPECIAL_POTION, RECOVER_SPECIAL_FLASK},
			{SUPER_ENERGY_POTION, SUPER_ENERGY_FLASK},
			{ZAMORAK_BREW, ZAMORAK_BREW_FLASK},
			{SUPER_STRENGTH_POTION, SUPER_STRENGTH_FLASK},
			{SUPER_ATTACK_POTION, SUPER_ATTACK_FLASK},
			{SUPER_DEFENCE_POTION, SUPER_DEFENCE_FLASK},
			{COMBAT_POTION, COMBAT_FLASK}
	};

	public static void init() {
		NPCHandler.register(ZAHUR_ID, 1, ((p, n) -> {
			// talk-to
			p.getDialogueManager().startDialogue("Zahur", ZAHUR_ID);
		}));
		NPCHandler.register(ZAHUR_ID, 2, ((p, n) -> {
			// decant
			//Drinkables.decantPotsInv(p); // only deal with 4 dose
			potsToFlasks(p);
		}));
		NPCHandler.register(ZAHUR_ID, 3, ((p, n) -> {
			// clean
			cleanHerbs(p);
		}));
		NPCHandler.register(ZAHUR_ID, 4, ((p, n) -> {
			// make unfinished
			createUnfinished(p);
		}));
	}

	private static void createUnfinished(Player p) {
		int vials = p.getInventory().getAmountOf(Herblore.VIAL)
				+ p.getInventory().getAmountOf(Herblore.VIAL + 1);
		int vialsReq = 0;
		int cost = 0;
		long coins = (long) p.getMoneyPouch().getCoinsAmount() + p.getInventory().getAmountOf(995);
		Herblore.Ingredients ingredient;
		boolean success = true;

		for(Item item : p.getInventory().getItems().getItems()) {
			if(item != null && ((ingredient = Herblore.Ingredients.forIdOrNoted(item.getId())) != null)) {
				ItemConfig unf = ItemConfig.forID(ingredient.getRewards()[0]);
				if(!unf.getName().contains("(unf)"))
					continue;
				if(vials < vialsReq + item.getAmount()) {
					success = false;
					break;
				}
				if(coins >= cost + PRICE_PER_POT * item.getAmount()) {
					cost += PRICE_PER_POT * item.getAmount();
					vialsReq += item.getAmount();
					item.setId(item.isNoted() ? unf.cert : unf.id);
				} else {
					success = false;
					break;
				}
			}
		}

		if(cost > 0) {
			p.getDialogueManager().startDialogue("SimpleNPCMessage", ZAHUR_ID,
					success ? "There, all done."
							: "You don't have enough vials or coins for me to create these potions.");
			p.getInventory().removeItemMoneyPouch(new Item(995, cost));
			int unnotedRem = p.getInventory().getAmountOf(Herblore.VIAL);
			p.getInventory().deleteItem(Herblore.VIAL, vialsReq);
			if(unnotedRem < vialsReq)
				p.getInventory().deleteItem(new Item(Herblore.VIAL + 1, vialsReq - unnotedRem));
		} else {
			p.getDialogueManager().startDialogue("SimpleNPCMessage", ZAHUR_ID, "I didn't find anything that I could clean.");
		}

		p.getInventory().refresh();
	}

	private static void cleanHerbs(Player p) {
		int cost = 0;
		long coins = (long) p.getMoneyPouch().getCoinsAmount() + p.getInventory().getAmountOf(995);
		HerbCleaning.Herbs herb;
		boolean success = true;

		for(Item item : p.getInventory().getItems().getItems()) {
			if(item != null && ((herb = HerbCleaning.getHerbOrNoted(item.getId())) != null)) {
				if(coins >= cost + PRICE_PER_GRIMY_HERB * item.getAmount()) {
					cost += PRICE_PER_GRIMY_HERB * item.getAmount();
					item.setId(item.isNoted() ? ItemConfig.forID(herb.getCleanId()).cert : herb.getCleanId());
				} else {
					success = false;
					break;
				}
			}
		}

		if(cost > 0) {
			p.getDialogueManager().startDialogue("SimpleNPCMessage", ZAHUR_ID,
					success ? "There, all done."
							: "You don't have enough coins for me to clean these herbs.");
			p.getInventory().removeItemMoneyPouch(new Item(995, cost));
		} else {
			p.getDialogueManager().startDialogue("SimpleNPCMessage", ZAHUR_ID, "I didn't find anything that I could clean.");
		}

		p.getInventory().refresh();
	}

	public static Drinkables.Drink[] getPotToFlask(int id) {
		Drinkables.Drink find = Drinkables.getDrink(id);
		if(find == null)
			return null;
		Optional<Drinkables.Drink[]> found = Arrays.stream(POTION_TO_FLASK).filter(drinks -> drinks[0] == find).findAny();
		return found.isPresent() ? found.get() : null;
	}

	/**
	 * returns if player has enough flasks to complete transaction
	 */
	private static boolean potsToFlasks(Player p) {
		double flasksReq = 0.0;
		int flasks = p.getInventory().getAmountOf(Drinkables.FLASK)
				+ p.getInventory().getAmountOf(Drinkables.FLASK + 1);
		Drinkables.Drink pot[];
		boolean success = true;

		for(Item item : p.getInventory().getItems().getItems()) {
			if(item == null)
				continue;

			int id = item.isNoted() && item.getNotedId() != -1 ? item.getNotedId() : item.getId();
			if((pot = getPotToFlask(id)) != null) {
				int doses = 1;
				// find amt of doses
				for(int i = 0; i < pot[0].getId().length; i++) {
					if(pot[0].getId()[i] == id) {
						doses = 4-i;
						break;
					}
				}
				id = pot[1].getId()[pot[1].getId().length - doses];

				double req = item.getAmount() == 1 ? 0.66667 : (int) Math.ceil((double)(item.getAmount() * doses) * 0.16667);
				if(flasks >= flasksReq + req) {
					if(item.isNoted())
						id = ItemConfig.forID(id).getCertId();
					item.setId(id);
					flasksReq+=req;
				} else {
					success = false;
					break;
				}
			}
		}

		if(flasksReq > 0) {
			int unnotedRem = p.getInventory().getAmountOf(Drinkables.FLASK);
			p.getInventory().deleteItem(Drinkables.FLASK, (int) flasksReq);
			if(unnotedRem < flasksReq)
				p.getInventory().deleteItem(new Item(Drinkables.FLASK + 1, (int) (flasksReq - unnotedRem)));
		} else {
			p.getDialogueManager().startDialogue("SimpleNPCMessage", ZAHUR_ID, "I didn't find anything that I could clean.");
		}

		Drinkables.decantPotsInv(p);

		p.getDialogueManager().startDialogue("SimpleNPCMessage", ZAHUR_ID,
				success ? "There, all done."
						: "You don't have enough potion flasks for me to fill with your normal potions.");
		return success;
	}

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, HAPPY, "Hello, can I help you at all?");
	}

	@Override
	public void run(int interfaceId, int componentId) {

		if (stage < 1) {
			sendOptionsDialogue("Select an Option", "Decant potions", "Make unfinished potions (" + PRICE_PER_POT +" gp each)", "Clean grimy herbs (" + PRICE_PER_GRIMY_HERB + " gp each)");
			stage = 1;
		} else {
			if (componentId == OPTION_1) {
				potsToFlasks(player);
			} else if (componentId == OPTION_2) {
				createUnfinished(player);
			} else {
				cleanHerbs(player);
			}
		}
	}

	@Override
	public void finish() {

	}
}
