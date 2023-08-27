package com.rs.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.grandExchange.Offer;
import com.rs.login.GameWorld;
import com.rs.login.Login;
import com.rs.login.account.Account;
import com.rs.utils.LoginFilesManager;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.SerializationUtilities;
import com.rs.utils.Utils;

public class DupeChecker {

	private static Map<String, Long> moneyCount = new HashMap<String, Long>();

	
	public static void main(String[] args) throws Throwable {
		Settings.HOSTED = true;
		Settings.WORLD_ID = 1;
		SerializableFilesManager.init();
		Cache.init(); // needed for ge
		Login.init();
		GrandExchange.init(); // load prices
		
		GameWorld target = Login.getWorld(1);
		for (String acc : LoginFilesManager.getAllAccounts()) {
			if (!acc.endsWith(".ser"))
				continue;
			Account account = (Account) LoginFilesManager.loadAccount(acc.replace(".ser", ""));
			if (account == null)
				continue;
			if ( account.getRights() == 2) {
				System.out.println("Admin: "+acc);
				continue;
			}
			byte[] data = account.getFile(target.getInformation().getPlayerFilesId());
			if (data == null || data.length == 0) {//they don't play
				continue;
			}
			Player player = (Player) SerializationUtilities.loadObject(data);
			if (player == null) {//nulled
			/*	account.deleteFile(target.getInformation().getPlayerFilesId());
				suspects.write("DELETED: "+account.getUsername());
				suspects.newLine();
				LoginFilesManager.saveAccount(account);*/
				continue;
			}
			
			long value = calculateValue(player);
			moneyCount.put(acc.replace(".ser", ""), value);
		}
		LoginFilesManager.flush();

	      List<String> moneySorted = new ArrayList<String>();
	      moneySorted.addAll(moneyCount.keySet());
	      moneySorted.sort(new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					Long price1 = moneyCount.get(o1);
	            	Long price2 = moneyCount.get(o2);
	                return -price1.compareTo(price2);
				}
	        });
	        
	      
	      long ecoCoins = writeMap("economy.txt", moneySorted, moneyCount);
	      System.out.println("Finished. Eco total:");
			System.out.println("Value count: "+formatMoney(Long.toString(ecoCoins)));
		
	      

	}
	
	private static long writeMap(String pathName, List<String> list, Map<String, Long> map) throws IOException {
		long total = 0;
		File file = new File(pathName); 
		if (file.exists())
			file.delete();
		else
			file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (String key : list) {
			long v = map.get(key);
			total += v;
			writer.append(key+", "+formatMoney(Long.toString(v)));
			writer.newLine();
			writer.flush();
		}
		writer.close();
		return total;
	}
	
	public static String formatMoney(String value) {
		if (value.length() > 15)
			value = value.substring(0, value.length() - 15) + "Q ["+value+"]";
		else if (value.length() > 12)
			value = value.substring(0, value.length() - 12) + "T ["+value+"]";
		else if (value.length() > 9)
			value = value.substring(0, value.length() - 9) + "B ["+value+"]";
		else if (value.length() > 6)
			value = value.substring(0, value.length() - 6) + "M ["+value+"]";
		else if (value.length() > 3)
			value = value.substring(0, value.length() - 3) + "K ["+value+"]";
		return value;
	}

	@SuppressWarnings("unchecked")
	public static long calculateValue(Player player) {
		
		long total = 0;

		total += player.getMoneyPouch().getCoinsAmount();

		ItemsContainer<Item>[] containers = (ItemsContainer<Item>[]) new ItemsContainer[3];
		player.getControlerManager().setPlayer(player);
		player.getControlerManager().r(); //so that doesnt check spawnpk
		player.getInventory().setPlayer(player);
		player.getEquipment().setPlayer(player);
		player.getBank().setPlayer(player);
		containers[0] = player.getInventory().getItems();
		containers[1] = player.getEquipment().getItems();
		if (player.getFamiliar() != null && player.getFamiliar().getBob() != null)
			containers[2] = player.getFamiliar().getBob().getBeastItems();

		for (int s = 0; s < 6; s++) {
			Offer offer = GrandExchange.getOffer(player, s);
			if (offer == null)
				continue;
			if (offer.isBuying())
				total += (long)offer.getAmount() * (long)offer.getPrice();
			else
				total += (long)GrandExchange.getPrice(offer.getId()) * (long)offer.getAmount();
		}

		for (int i = 0; i < containers.length; i++) {
			ItemsContainer<Item> container = containers[i];
			if (container == null)
				continue;

			for (int a = 0; a < container.getItems().length; a++) {
				Item item = container.getItems()[a];
				if (item == null)
					continue;

				long price = GrandExchange.getPrice(item.getId());
				if (price == 0)
					price = 1;
				else if (price < 0)
					price = Integer.MAX_VALUE;
				price *= item.getAmount();
				if (price < 0)
					price = Long.MAX_VALUE;

				total += price;
				if (total < 0) {
					// oh god, we had long overflow, that person must have really big dupe bank if he more than 2^63 coins worth
					return Long.MAX_VALUE;
				}
			}
		}

		Item[] bank = player.getBank().generateContainer();
		for (int a = 0; a < bank.length; a++) {
			Item item = bank[a];
			if (item == null || !ItemConstants.isTradeable(item))
				continue;

			long price = GrandExchange.getPrice(item.getId());
			if (price == 0)
				price = 1;
			else if (price < 0)
				price = Integer.MAX_VALUE;
			price *= item.getAmount();
			if (price < 0)
				price = Long.MAX_VALUE;

			total += price;
			if (total < 0) {
				// oh god, we had long overflow, that person must have really big dupe bank if he more than 2^63 coins worth
				return Long.MAX_VALUE;
			}
		}

		return total;
	}
}