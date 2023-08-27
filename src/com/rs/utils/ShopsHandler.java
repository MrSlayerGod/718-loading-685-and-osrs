package com.rs.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.Shop;

public class ShopsHandler {

	private static final HashMap<Integer, Shop> handledShops = new HashMap<Integer, Shop>();

	private static final String PACKED_PATH = "data/item/packedShops.s";
	private static final String UNPACKED_PATH = "data/item/shops.txt";

	private static final int SHOP_QUANTITY_RATE = 10;

	public static void init() {
		loadUnpackedShops();
	}

	public static void forceReload() {
		handledShops.clear();
		loadUnpackedShops();
	}

	private static void loadUnpackedShops() {
		//Logger.log("ShopsHandler", "Packing shops...");
		try {
			@SuppressWarnings("resource")
			BufferedReader in = new BufferedReader(new FileReader(UNPACKED_PATH));
			//DataOutputStream out = new DataOutputStream(new FileOutputStream(PACKED_PATH));
			while (true) {
				String line = in.readLine();
				if (line == null)
					break;
				if (line.startsWith("//"))
					continue;
				String[] splitedLine = line.split(" - ", 3);
				if (splitedLine.length != 3)
					throw new RuntimeException("Invalid list for shop line: " + line);
				String[] splitedInform = splitedLine[0].split(" ", 3);
				if (splitedInform.length != 3)
					throw new RuntimeException("Invalid list for shop line: " + line);
				String[] splitedItems = splitedLine[2].split(" ");
				int key = Integer.valueOf(splitedInform[0]);
				int money = Integer.valueOf(splitedInform[1]);
				boolean generalStore = Boolean.valueOf(splitedInform[2]);
				Item[] items = new Item[splitedItems.length / 2];
				int count = 0;
				for (int i = 0; i < items.length; i++) {
					items[i] = new Item(Integer.valueOf(splitedItems[count++]), Integer.valueOf(splitedItems[count++]), 0, true);
				}
				if (key < 1200) {
					for (int i = 0; i < items.length; i++) {
						int value = items[i].getAmount();
						if (items[i].getDefinitions().isStackable()) //increase amt  by x10 for stackables
							items[i].setAmount(value * SHOP_QUANTITY_RATE);
					}
				} else {
					for (int i = 0; i < items.length; i++) {
						if (items[i].getDefinitions().isStackable() || items[i].getDefinitions().isNoted()) {
							items[i].setAmount(items[i].getId() == 995 ? 2000000000 : 1000000);
						} else {
							items[i].setAmount(5000);
						}
					}
				}

				/*out.writeInt(key);
				writeAlexString(out, splitedLine[1]);
				out.writeShort(money);
				out.writeBoolean(generalStore);
				out.writeByte(items.length);
				for (Item item : items) {
					out.writeShort(item.getId());
					out.writeInt(item.getAmount());
				}*/
				addShop(key, new Shop(key, splitedLine[1], money, items, generalStore));
			//	addShop(key + 10000, new Shop(key, splitedLine[1], money, items.clone(), generalStore)); //extreme
			}
			in.close();
			//out.close();
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

	private static void loadPackedShops() {
		try {
			RandomAccessFile in = new RandomAccessFile(PACKED_PATH, "r");
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
			while (buffer.hasRemaining()) {
				int key = buffer.getInt();
				String name = readAlexString(buffer);
				int money = buffer.getShort() & 0xffff;
				boolean generalStore = buffer.get() == 1;
				Item[] items = new Item[buffer.get() & 0xff];
				for (int i = 0; i < items.length; i++)
					items[i] = new Item(buffer.getShort() & 0xffff, buffer.getInt(), 0, true);
				addShop(key, new Shop(key, name, money, items, generalStore));
				//addShop(key + 10000, new Shop(key, name, money, items.clone(), generalStore)); //extreme
			}
			channel.close();
			in.close();
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

	public static String readAlexString(ByteBuffer buffer) {
		int count = buffer.get() & 0xfff;
		byte[] bytes = new byte[count];
		buffer.get(bytes, 0, count);
		return new String(bytes);
	}

	public static void writeAlexString(DataOutputStream out, String string) throws IOException {
		byte[] bytes = string.getBytes();
		out.writeByte(bytes.length);
		out.write(bytes);
	}

	public static void restoreShops() {
		for (Shop shop : handledShops.values()) {
			if (shop.isGeneralStore())
				shop.restoreItems(true);
			shop.restoreItems(false);
		}
	}

	public static boolean openShop(Player player, int key) {
		/*if (player.isExtreme())
			key += 10000;*/
		Shop shop = getShop(key);
		if (shop == null)
			return false;
		if ((player.isIronman() || player.isUltimateIronman() || player.isHCIronman()) && (key == 914 || key == 913 || key == 900 || key == 902 || key == 903 || key == 904 || key == 907 || key == 908)) {
			player.getPackets().sendGameMessage("You can't use this shop as an ironman.");
			return true;
		}
/*		
		if (player.isHCIronman() && (key == 910 || key == 911 || key == 905)) {
			player.getPackets().sendGameMessage("You can't use this shop as a hardcore ironman.");
			return true;
		}*/
		
		
		if (!player.getBank().hasVerified(10))
			return true;
		shop.addPlayer(player);
		return true;
	}

	public static Shop getShop(int key) {
		return handledShops.get(key);
	}

	public static void addShop(int key, Shop shop) {
		handledShops.put(key, shop);
	}
}
