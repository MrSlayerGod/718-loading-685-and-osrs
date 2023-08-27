package com.rs.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemConfig;
import com.rs.cache.loaders.NPCConfig;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.login.GameWorld;
import com.rs.login.Login;
import com.rs.login.account.Account;
import com.rs.utils.LoginFilesManager;
import com.rs.utils.NPCDrops;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.SerializationUtilities;
import com.rs.utils.Utils;

public class DropAnalyser {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Throwable {
		Cache.init();
		Settings.HOSTED = false;
		Settings.WORLD_ID = 1;
		SerializableFilesManager.init();
		NPCDrops.init();
		Cache.init(); // needed for ge
		Login.init();

		Scanner kbd = new Scanner(System.in);

		System.out.print("Input NPC ID: ");
		int npc = kbd.nextInt(), size = NPCConfig.forID(npc).boundSize;

		GameWorld target = Login.getWorld(1);
		
		final int TEST_AMT = 10000;
		int dLevel = 0;

		Map<String, Integer> dpR = new HashMap<String, Integer>();
		Map<String, Integer> dpP = new HashMap<String, Integer>();
		for (String acc : LoginFilesManager.getAllAccounts()) {
			Account account = (Account) LoginFilesManager.loadAccount(acc.substring("accounts/".length(), acc.length() - ".acc".length()));
			if (account == null)
				continue;
			byte[] data = account.getFile(target.getInformation().getPlayerFilesId());
			if (data == null || data.length == 0)
				continue;
			Player player = (Player) SerializationUtilities.loadObject(data);
			if (player == null)
				continue;
			dLevel = 1;
			Drops drops = NPCDrops.getDrops(npc);
			if (drops == null)
				return;
			drops.setAcessRareTable(false);
			NPC n = World.spawnNPC(npc, new WorldTile(1,1,1), -1, true);
			for (int i = 0; i < TEST_AMT; i++) {
				List<Drop> dropL = drops.generateDrops(player, Settings.DROP_RATE * n.getDropRateFactor());
			
				drops.addCharms(dropL, size);
				for (Drop d : dropL) {
					String itemName = ItemConfig.forID(d.getItemId()).getName();
					if (dpR.containsKey(itemName)) {
						dpP.put(itemName, dpP.get(itemName) + 1);
						dpR.put(itemName, dpR.get(itemName) + Utils.random(d.getMinAmount(), d.getMaxAmount()));
					} else {
						dpP.put(itemName, 1);
						dpR.put(itemName, Utils.random(d.getMinAmount(), d.getMaxAmount()));
					}
				}

			}
		}
		
		File file = new File("./dropt/");
		if (!file.exists())
			file.mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("./dropt/"+NPCConfig.forID(npc).getName()+".txt")));

		writer.write("Kill count: "+TEST_AMT);
		writer.newLine();
		
		writer.write("Difficulty Level [Hard = 1, Med = 2, Easy = 3]: "+dLevel);
		writer.newLine();
		
		writer.write("Format: Item Name, Item Amount, Drop percentage");
		writer.newLine();
		
		writer.newLine();
		
		DecimalFormat df = new DecimalFormat("#.###");
		String[] keys = dpR.keySet().toArray(new String[dpR.size()]);
		Arrays.sort(keys);
		for (String key : keys) {
			int keyCount = dpR.get(key);
			writer.write(key + " : "+keyCount+" : "+(df.format(100 * ((double) dpP.get(key) / TEST_AMT)))+"%");
			writer.newLine();
		}
		writer.flush();
		
		writer.close();
		kbd.close();
	}
}
