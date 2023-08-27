package com.rs.game.player.content.grandExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.rs.cache.loaders.ItemConfig;
import com.rs.discord.Bot;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.GrandExchangeManager;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.VirtualValues;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

public class GrandExchange {

	private static final Object LOCK = new Object();
	// offer uid
	private static HashMap<Long, Offer> OFFERS;
	private static HashMap<String, ExchangeStock> STOCK;
	//private static ArrayList<OfferHistory> OFFERS_TRACK;
	private static ArrayList<OfferHistory> OFFERS_TRACK_OLD;
//	private static HashMap<Integer, Integer> PRICES;
	private static HashMap<Integer, ItemPrice> PRICES;

	private static boolean edited;

	public static void init() {
		OFFERS = SerializableFilesManager.loadGEOffers();
		STOCK = SerializableFilesManager.loadGEStock();
		//OFFERS_TRACK = SerializableFilesManager.loadGEHistory();
		PRICES = SerializableFilesManager.loadGEPrices();
		OFFERS_TRACK_OLD = new ArrayList<OfferHistory>();

		// register actions
		ObjectHandler.register(new int[] {110060}, 2, ((player, obj) ->
				player.getBank().openBank()));
		ObjectHandler.register(new int[] {110061, 110060}, 1, ((player, obj) ->
				player.getGeManager().openGrandExchange()));
		ObjectHandler.register(new int[] {110061, 110060}, 3, ((player, obj) ->
				player.getGeManager().openCollectionBox()));
		ObjectHandler.register(new int[] {110061, 110060, 110644}, 4, ((player, obj) ->
				player.getDialogueManager().startDialogue("ViewGETransactions")));

	}

	public static void reset(boolean track, boolean price) {
		if (track) {
			for (ItemPrice i : PRICES.values())
				i.transactions.clear();
		}
		if (price)
			PRICES.clear();
		recalcPrices();
	}

	public static void recalcPrices() {
		for (ItemPrice i : PRICES.values()) 
			i.updateValue();
		VirtualValues.setValues();
		savePrices();
	}

	public static void saveStock() {
		SerializableFilesManager.saveGEStock(new HashMap<String, ExchangeStock>(STOCK));
	}

	public static void savePrices() {
		SerializableFilesManager.saveGEPrices(new HashMap<Integer, ItemPrice>(PRICES));
	}

	public static final void save() {
		if (!edited)
			return;
		SerializableFilesManager.saveGEOffers(new HashMap<Long, Offer>(OFFERS));
		saveStock();
		savePrices();
	//	saveOffersTrack();
		edited = false;
	}

	public static void linkOffers(Player player) {
		boolean itemsWaiting = false;
		for (int slot = 0; slot < player.getGeManager().getOfferUIds().length; slot++) {
			Offer offer = getOffer(player, slot);
			if (offer == null)
				continue;
			offer.link(slot, player);
			offer.update();
			if (!itemsWaiting && offer.hasItemsWaiting()) {
				itemsWaiting = true;
				player.getPackets().sendGameMessage("You have items from the Grand Exchange waiting in your collection box.");
			}
		}
	}

	public static Offer getOffer(Player player, int slot) {
		synchronized (LOCK) {
			long uid = player.getGeManager().getOfferUIds()[slot];
			if (uid == 0)
				return null;
			Offer offer = OFFERS.get(uid);
			if (offer == null) {
				player.getGeManager().getOfferUIds()[slot] = 0; // offer
				// disapeared
				// within time
				return null;
			}
			return offer;
		}

	}

	public static void sendOffer(Player player, int slot, int itemId, int amount, int price, boolean buy) {
		Bot.sendLog(Bot.SELL_BUY_CHANNEL, "[type="+ (buy ? "BUY" : "SELL" )+"-GE][name="+player.getUsername()+"][item="+ItemConfig.forID(itemId).getName()+"("+itemId+")x"+Utils.getFormattedNumber(amount)+"][price="+ItemConfig.forID(995).getName()+"("+995+")x"+Utils.getFormattedNumber(price)+"]");
		synchronized (LOCK) {
			Offer offer = new Offer(itemId, amount, price, buy);
			player.getGeManager().getOfferUIds()[slot] = createOffer(offer);
			offer.link(slot, player);
			findBuyerSeller(player, offer);
		}
	}

	public static void abortOffer(Player player, int slot) {
		synchronized (LOCK) {
			Offer offer = getOffer(player, slot);
			if (offer == null)
				return;
			edited = true;
			if (offer.cancel() && offer.forceRemove())
				deleteOffer(player, slot); // shouldnt here happen anyway
		}
	}

	public static void collectItems(Player player, int slot, int invSlot, int option) {
		synchronized (LOCK) {
			Offer offer = getOffer(player, slot);
			if (offer == null)
				return;
			edited = true;
			if (offer.collectItems(invSlot, option) && offer.forceRemove()) {
				deleteOffer(player, slot); // should happen after none left and
				// offer completed
				if (offer.getTotalAmmountSoFar() != 0) {
					
					Bot.sendLog(Bot.SELL_BUY_CHANNEL, "[type="+ (offer.isBuying() ? "BUY" : "SELL" )+"-GE-UPDATE][name="+player.getUsername()+"][item="+ItemConfig.forID(offer.getId()).getName()+"("+offer.getId()+")x"+Utils.getFormattedNumber(offer.getTotalAmmountSoFar())+"][price="+ItemConfig.forID(995).getName()+"("+995+")x"+Utils.getFormattedNumber(offer.getTotalPriceSoFar())+"]");
					
					OfferHistory o = new OfferHistory(offer.getId(), offer.getTotalAmmountSoFar(), offer.getTotalPriceSoFar(), offer.isBuying());
					player.getGeManager().addOfferHistory(o);
					OFFERS_TRACK_OLD.add(new OfferHistory(player.getUsername(), offer.getId(), offer.getTotalAmmountSoFar(), offer.getTotalPriceSoFar(), offer.isBuying()));
				}
			}
		}
	}

	private static void deleteOffer(Player player, int slot) {
		player.getGeManager().cancelOffer(); // sends back to original screen if
		// seeing an offer
		OFFERS.remove(player.getGeManager().getOfferUIds()[slot]);
		player.getGeManager().getOfferUIds()[slot] = 0;
	}

	//player who buy or sell in this case when updates
	private static void findBuyerSeller(Player player, Offer offer) {
		int autoSellPrice = ItemConstants.getHighAlchValue(new Item(offer.getId()));
		while (!offer.isCompleted()) {
			Offer bestOffer = null;
			for (Offer o : OFFERS.values()) {
				//owner is null when not logged in but u online its on so works
				if (o.getOwner() == offer.getOwner() || o.isBuying() == offer.isBuying() || o.getId() != offer.getId() || o.isCompleted() || (offer.isBuying() && o.getPrice() > offer.getPrice()) || (!offer.isBuying() && o.getPrice() < offer.getPrice()) || offer.isOfferTooHigh(o))
					continue;
				
				if (bestOffer == null || (offer.isBuying() && o.getPrice() < bestOffer.getPrice()) || (!offer.isBuying() && o.getPrice() > bestOffer.getPrice()))
					bestOffer = o;
			}
			if (autoSellPrice > 0 && !offer.isBuying() && offer.getPrice() <= autoSellPrice //no best offer but can sell at auto
					&&  (bestOffer == null || bestOffer.getPrice() < autoSellPrice)) { //best offer worse than auto so sell to auto
				bestOffer = new Offer(offer.getId(), offer.getAmount()-offer.getTotalAmmountSoFar(), autoSellPrice, true);
			} else if (GrandExchangeManager.isCurrency(new Item(offer.getId())) && autoSellPrice > 0 && offer.isBuying() && offer.getPrice() >= autoSellPrice //no best offer but can sell at auto
					&&  (bestOffer == null || bestOffer.getPrice() > autoSellPrice)) { //best offer worse than auto so sell to auto
				bestOffer = new Offer(offer.getId(), offer.getAmount()-offer.getTotalAmmountSoFar(), autoSellPrice, false);
			}
			
			if (bestOffer == null) {
				offer.updateStockOffer();
				break;
			}
			offer.updateOffer(bestOffer);
			GrandExchange.addTransaction(player, offer.getId(), bestOffer.getPrice());
		}
		offer.update();
	}

	public static Offer getBestOffer(Player player, int id, boolean buying) {
		Offer bestOffer = null;
		for (Offer o : OFFERS.values()) {
			if (o.getOwner() == player || o.isBuying() == buying || o.getId() != id || o.isCompleted())
				continue;
			//owner is null when not logged in but u online its on so works
			if (bestOffer == null || (buying && o.getPrice() < bestOffer.getPrice()) || (!buying && o.getPrice() > bestOffer.getPrice()))
				bestOffer = o;
		}
		return bestOffer;
	}

	private static long createOffer(Offer offer) {
		edited = true;
		long uid = getUId();
		OFFERS.put(uid, offer);
		return uid;
	}

	private static long getUId() {
		while (true) {
			long uid = Utils.currentTimeMillis()*10 + Utils.random(1000);
			if (OFFERS.containsKey(uid))
				continue;
			return uid;
		}
	}

	// in order not to keep player saved on memory in offers after player leaves
	// <.<
	public static void unlinkOffers(Player player) {
		for (int slot = 0; slot < player.getGeManager().getOfferUIds().length; slot++) {
			Offer offer = getOffer(player, slot);
			if (offer == null)
				continue;
			offer.unlink();
		}
	}

	public static ExchangeStock getStock(Player player, int id) {
		ExchangeStock stock = STOCK.get(player.getUsername()+":"+player.getSession().getIP()+":"+player.getLastGameMAC()+":"+id);
		if (stock != null)
			return stock;
		Iterator<Entry<String, ExchangeStock>> it$ = STOCK.entrySet().iterator();
		while (it$.hasNext()) {
			Entry<String, ExchangeStock> e = (Entry<String, ExchangeStock>) it$.next();
			String[] identifiers = ((String) e.getKey()).split(":");
			stock = (ExchangeStock) e.getValue();
			if (stock.getId() == id && (identifiers[0].equals(player.getUsername()) || identifiers[1].equals(player.getSession().getIP()) || identifiers[2].equals(player.getLastGameMAC())))
				return stock;
		}
		return null;
	}

	public static void removeExpiredStock(Player player) {
		Iterator<Entry<String, ExchangeStock>> it$ = STOCK.entrySet().iterator();
		while (it$.hasNext()) {
			Entry<String, ExchangeStock> e = (Entry<String, ExchangeStock>) it$.next();
			String[] identifiers = ((String) e.getKey()).split(":");
			ExchangeStock stock = (ExchangeStock) e.getValue();
			if (stock.getTimer() < Utils.currentTimeMillis() && (identifiers[0].equals(player.getUsername()) || identifiers[1].equals(player.getSession().getIP()) || identifiers[2].equals(player.getLastGameMAC())))
				it$.remove();
		}
		//Required so stock can update when it resets.
		for (long uid : player.getGeManager().getOfferUIds()) {
			if (uid == 0) continue;
			findBuyerSeller(player, OFFERS.get(uid));
		}
	}

	public static void addStock(Player player, ExchangeStock e) {
		STOCK.put(player.getUsername()+":"+player.getSession().getIP()+":"+player.getLastGameMAC()+":"+e.getId(), e);
	}
	
	public static List<OfferHistory> getHistoryOld() {
		synchronized (LOCK) {
			return OFFERS_TRACK_OLD;
		}
	}

	public static HashMap<Long, Offer> getOffers() {
		synchronized (LOCK) {
			return OFFERS;
		}
	}
	
	public static void addTransaction(Player player, int id, int value) {
		ItemConfig defs = ItemConfig.forID(id);
		if (defs.isNoted())
			id = defs.getCertId();
		else if (defs.isLended())
			id = defs.getLendId();
		synchronized (LOCK) {
			ItemPrice item = PRICES.get(id);
			if (item == null) 
				PRICES.put(id, item = new ItemPrice(defs.value)); //itemconfig.value originally
			item.addTransaction(player, value);
		}
	}
	
	public static void setPrice(int id, int value) {
		ItemConfig defs = ItemConfig.forID(id);
		if (defs.isNoted())
			id = defs.getCertId();
		else if (defs.isLended())
			id = defs.getLendId();
		synchronized (LOCK) {
			ItemPrice item = PRICES.get(id);
			if (item == null)
				PRICES.put(id, item = new ItemPrice(defs.value)); // itemconfig.value originally
			else
				item.transactions.clear(); // clear all transactions to ensure no manipulation
			item.value = value;
		}
	}
	
	public static int getPrice(int id) {
		ItemConfig defs = ItemConfig.forID(id);
		if (defs.isNoted())
			id = defs.getCertId();
		else if (defs.isLended())
			id = defs.getLendId();
		ItemPrice item = PRICES.get(id);
		return item == null || item.value == 0 ? defs.value : item.value; //0 would have been itemconfig.value
	}
	
}
