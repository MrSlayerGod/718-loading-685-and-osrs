package com.rs.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ObjectConfig;
import com.rs.utils.Utils;

public class ObjectListDumper {

	public static void main(String[] args) throws IOException {
		Cache.init();
		File file = new File("objectlist.txt");
		if (file.exists())
			file.delete();
		else
			file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.append("//Version = 742\n");
		writer.flush();
		for (int id = 0; id < Utils.getObjectDefinitionsSize(); id++) {
			ObjectConfig def = ObjectConfig.forID(id);
			// writer.append("FORMAT1"+id+"FORMAT2"+def.name.replaceAll("`",
			// "")+"FORMAT3\n");
			writer.append(id + " - " + def.name);
			writer.newLine();
			System.out.println(id + " - " + def.name);
			writer.flush();
		}
		writer.close();
	}

}
