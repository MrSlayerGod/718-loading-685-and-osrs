package com.rs.utils;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.cache.loaders.NPCConfig;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.others.GiantMimic;
import com.rs.game.npc.others.Lucien;
import com.rs.game.npc.worldboss.OnyxBoss;
import com.rs.game.player.Player;
import com.rs.game.player.content.AncientEffigies;
import com.rs.game.player.content.NPCKillLog;

public class NPCDrops {

	private final static String PACKED_PATH = "data/npc/packedDrops.d";
	private static HashMap<Integer, Drops> npcDrops = new HashMap<Integer, Drops>();
	
	private static HashMap<Integer, List<String>> itemDrops = new HashMap<Integer, List<String>>();

	public static final void init() {
		loadPackedNPCDrops();
		OnyxBoss.init(); //set drops
		Lucien.init();
		GiantMimic.init();
	}

	public static NPCConfig search(String string) {
		string = string.toLowerCase();
		NPCConfig match = null;

		for(Integer key : npcDrops.keySet()) {
			NPCConfig npc = NPCConfig.forID(key);
			String n = npc.getName().toLowerCase();
			if(n.equals(string))
				return npc;
			if(n.contains(string))
				match = npc;
		}

		return match;
	}

	public static Drops getDrops(int npcId) {
		switch (npcId) {
			case 22095:
			case 22096:
				npcId = 3451;
				break;
		}
		return npcDrops.get(npcId);
	}

	public static void addDrops(int npcId, Drops drops) {
		npcDrops.put(npcId, drops);
	}
	
	
	public static void searchItem(Player player) {
		player.getInterfaceManager().setInterface(true, 752, 7, 389);
		player.getTemporaryAttributtes().put(Key.SEARCH_ITEM_DROP, Boolean.TRUE);
		player.getPackets().sendExecuteScript(570, "Item Drop Search");
		player.getPackets().sendExecuteScript(-22);
	}
	
	public static void showItem(Player player, int id) {
		List<String> names = itemDrops.get(id);
		player.stopAll();
		if (names == null) {
			player.getPackets().sendGameMessage("No drops found for this npc.");
			return;
		}
		player.getPackets().sendGameMessage("Found "+names.size()+" npcs which drop this item.");
		NPCKillLog.sendQuestTab(player, "Item - "+ItemConfig.forID(id).getName(), names.toArray(new String[names.size()]));
		
	}

	private static void loadPackedNPCDrops() {
		try {
			RandomAccessFile in = new RandomAccessFile(PACKED_PATH, "r");
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());

			while (buffer.hasRemaining()) {
				int npcId = buffer.getInt();
				String name = NPCConfig.forID(npcId).getName();
				boolean acessRareTable = buffer.get() == 1;
				Drops drops = new Drops(acessRareTable);
				@SuppressWarnings("unchecked")
				List<Drop>[] dList = new ArrayList[Drops.VERY_RARE + 1];
				int size = (buffer.get() & 0xff);
				for (int i = 0; i < size; i++) {
					int itemId = buffer.getShort() & 0xffff;
					int min = buffer.getInt();
					int max = buffer.getInt();
					int rarity = buffer.get() & 0xff;
					if (
							(itemId == 15128
							|| itemId == 4851
							|| itemId == 18653 || itemId == 10976 || itemId == 10977 || itemId == 15173 || itemId == 15143 ||itemId == 15146 || itemId == 15149 || itemId == 15152 || itemId == 15155 ||itemId == 15158 || itemId == 15161 || itemId == 15164 || itemId == 15167 || itemId == 15170 || itemId == 15179 || itemId == 10995 || itemId == 10996 || itemId == 18639 || itemId == 18640 || itemId == 18641 || itemId == 18642 || itemId == 7812 || itemId == 7908 || itemId == 7815 || itemId == 7818 || itemId == 7821 || itemId == 7854 || itemId == 7824 || itemId == 7827 || itemId == 7830 || itemId == 7833 || itemId == 7836 || itemId == 7839 || itemId == 7842 || itemId == 7845 || itemId == 7848 || itemId == 7851 || itemId == 7857 || itemId == 7860 || itemId == 7863 || itemId == 7866 || itemId == 7872 || itemId == 7875 || itemId == 7878 || itemId == 7881 || itemId == 7884 || itemId == 7887 || itemId == 7893 || itemId == 7896 || itemId == 7899 || itemId == 7902 || itemId == 7905 || itemId == 7911 || itemId == 7914 || itemId == 7890 || itemId == 15196 || itemId == 15193 || itemId == 15199 || itemId == 15190 || itemId == 15202 || itemId == 15211 || itemId == 15188 || itemId == 15205 || itemId == 15208 || itemId == 15182 || itemId == 15185 || itemId == 15176 || itemId == 7869 || itemId == 7890 ||itemId == 14639 || itemId == 24909
							|| (itemId < Settings.OSRS_ITEM_OFFSET && itemId >= 25354
							&& itemId != 25477 && itemId != 25481 
							&& itemId != 25483 && itemId != 25489
									&& itemId != 25739 &&
									itemId != 25760 && itemId != 25761 && itemId != 25762 && itemId != 25502 && itemId != 25587)
							) || (itemId == AncientEffigies.STARVED_ANCIENT_EFFIGY && NPCConfig.forID(npcId).combatLevel < 100)) // DISABLE EOC DROPS
						continue;
					if (dList[rarity] == null)
						dList[rarity] = new ArrayList<Drop>();
					
					if (itemId == 6529) { //coins + tokkul drops x10
						min *= 10;
						max *= 10;
					} else if (itemId == 995) {
						min *= 50; //100 before
						max *= 50; //100 before
					}
					Drop drop = new Drop(itemId, min, max, rarity);
					dList[rarity].add(drop);
					
					ItemConfig config = ItemConfig.forID(itemId);
					if (config.isNoted())
						itemId = config.getCertId();
					List<String> id = itemDrops.get(itemId);
					if (id == null) 
						itemDrops.put(itemId, id = new ArrayList<String>());
					String name2 = name+" : ("+min+"-"+max+") "+(config.isNoted() ? " : Noted" : "");
					if (!id.contains(name2) && !name.equalsIgnoreCase("null"))
						id.add(name2);
				}
				drops.addDrops(dList);
				npcDrops.put(npcId, drops);
			}
			channel.close();
			in.close();
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

}
