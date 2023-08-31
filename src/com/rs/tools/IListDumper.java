package com.rs.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemConfig;
import com.rs.utils.Utils;

public class IListDumper {

	public static void main(String[] args) {
		try {
			new IListDumper();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public IListDumper() throws IOException {
		Cache.init();
		File file = new File("itemList.txt"); // = new
		// File("information/itemlist.txt");
		if (file.exists())
			file.delete();
		else
			file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		// writer.append("//Version = 709\n");
		writer.append("//Version = 742/osrs/custom\n");
		writer.flush();
		System.out.println("SIZE: "+Utils.getItemDefinitionsSize());
		for (int id = 0; id < Utils.getItemDefinitionsSize(); id++) {
			ItemConfig def = ItemConfig.forID(id);
			/*
			 * if (def.getName().equals("null")) continue;
			 */
			writer.append(id + " - " + def.getName());
			writer.newLine();
			writer.flush();
		}
		writer.close();
	}

	public static int convertInt(String str) {
		try {
			int i = Integer.parseInt(str);
			return i;
		} catch (NumberFormatException e) {
		}
		return 0;
	}

}
