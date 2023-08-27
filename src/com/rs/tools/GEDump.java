package com.rs.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

public class GEDump {

	public static void main(String[] args) throws Throwable {
		Settings.HOSTED = true;
		SerializableFilesManager.init();
		Cache.init(); // needed for ge
		GrandExchange.init(); // load prices
		System.out.println("BEGIN");
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("prices_old.txt")));
		for (int i = 0; i < Utils.getItemDefinitionsSize(); i++) {
			writer.write(i + " - " + GrandExchange.getPrice(i));
			writer.newLine();
			writer.flush();
		}
		writer.close();
		System.out.println("DONE");
	}
}