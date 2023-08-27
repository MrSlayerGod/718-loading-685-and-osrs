package com.rs.tools;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCConfig;
import com.rs.utils.Utils;

public class ItemDropsPacker {

	public static class NPCDrop {

		private int itemId, minAmount, maxAmount, rarity;

		public NPCDrop(int itemId, int minAmount, int maxAmount, int rarity) {
			if (itemId == 617)
				itemId = 995;
			if (itemId == 2513)
				itemId = 3140;
			if (itemId == 14472 || itemId == 14474 || itemId == 14476)
				itemId = 14479;
			if ((itemId == 18778 || itemId == 24154) && rarity < 3)
				rarity = 3;
			if (itemId >= 9007 && itemId <= 9012)
				itemId = 9013;
			this.itemId = itemId;
			this.minAmount = minAmount;
			this.maxAmount = maxAmount;
			this.rarity = rarity;
		}

		public int getMinAmount() {
			return minAmount;
		}

		public int getExtraAmount() {
			return maxAmount - minAmount;
		}

		public int getMaxAmount() {
			return maxAmount;
		}

		public int getItemId() {
			return itemId;
		}

		public int getRarity() {
			return rarity;
		}

	}

	public static final void main3(String[] args) throws IOException {
		Cache.init();
		RandomAccessFile in = new RandomAccessFile("data/npc/packedDrops.d", "r");
		FileChannel channel = in.getChannel();
		ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
		while (buffer.hasRemaining()) {
			int npcId = buffer.getShort() & 0xffff;

			File file = new File("data/npc/drops/" + npcId + ".txt");
			BufferedWriter writer = new BufferedWriter(new BufferedWriter(new FileWriter(file)));

			String name = NPCConfig.forID(npcId).getName();
			boolean acessRareTable = buffer.get() == 1;
			writer.write("RareDropTable="+acessRareTable);
			writer.newLine();
			int size = (buffer.get() & 0xff);
			for (int i = 0; i < size; i++) {
				int itemId = buffer.getShort() & 0xffff;
				int min = buffer.getInt();
				int max = buffer.getInt();
				int rarity = buffer.get() & 0xff;
				writer.write(itemId+", "+min+", "+max+", "+rarity);
				writer.newLine();
			}
			writer.flush();
			writer.close();
		}
	}

	public static final void main(String[] args) throws IOException {
		Cache.init();
		DataOutputStream out = new DataOutputStream(new FileOutputStream("data/npc/packedDrops.d"));
		for (int npcId = -Utils.getNPCDefinitionsSize(); npcId < Utils.getNPCDefinitionsSize(); npcId++) {
			File file = new File("data/npc/drops/" + npcId + ".txt");
			if (file.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				boolean rareDropTable = false;
				List<NPCDrop> drops = new ArrayList<NPCDrop>();
				while (true) {
					String line = reader.readLine();
					if (line == null)
						break;
					if (line.contains("RareDropTable=")) {
						rareDropTable = line.contains("true");
						continue;
					}
					String[] l = line.split(", ");
					int id = Integer.parseInt(l[0]);
					if (id == -1 || id == Settings.VOTE_TOKENS_ITEM_ID || id == 18757
							|| (id >= 13845 && id <= 13857)
							|| (id >= 25040 && id <= 25044)) //lore books)
						continue;
					if (NPCConfig.forID(npcId).name.equalsIgnoreCase("skeleton") && id == 532)
						continue;
					if (NPCConfig.forID(npcId).name.equalsIgnoreCase("terror dog") && id == 526)
						continue;
					drops.add(new NPCDrop(id, Integer.parseInt(l[1]), Integer.parseInt(l[2]), Integer.parseInt(l[3])));
				}
				reader.close();
				out.writeInt(npcId);
				out.writeBoolean(rareDropTable);
				out.writeByte(drops.size());
				for (NPCDrop drop : drops) {
					out.writeShort(drop.getItemId());
					out.writeInt(drop.getMinAmount());
					out.writeInt(drop.getMaxAmount());
					out.writeByte(drop.getRarity());
				}
			}
		}
		out.flush();
		out.close();
	}

}
