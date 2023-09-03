package com.rs.tools.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;

import com.alex.store.Index;
import com.alex.store.Store;
import com.alex.utils.Constants;

/**
 * Created at: Apr 22, 2017 4:07:39 PM
 * 
 * @author Walied-Yassen A.k.A Cody
 */
public class Rs3toRs2 {

	private static final DecimalFormat format = new DecimalFormat("#.##");
	private static final int START_ITEM_ID = 25010;
	private static final int END_ITEM_ID = 25027;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		System.out.println("Loading RS3 cache..");
		// Store source_cache = new
		// Store("E:\\Users\\Connor\\Desktop\\Games\\RSPS\\dataNEW881\\");
		Store source_cache = new Store("D:\\servers\\Decay-718-Github\\Decay-Server-Build\\data\\cache\\");
		System.out.println("Loading 718 cache..");
		Store target_cache = new Store("C:\\Users\\mrsla\\OneDrive\\Documents\\GitHub\\718-loading-685-and-osrs\\data\\cache\\");
		System.out.println("Started!");// this is pretty much it, can you change these ^ and backup the cache? i got a
										// bu on my vp[s but ye, i dont have an 885 cache, well, you need one to pack
										// from err sec
		// transport_index(source_cache, 0, target_cache, 0); // bases
		// transport_index(source_cache, 1, target_cache, 1); // frames
		// transport_index(source_cache, 5, target_cache, 5); // maps//ill fix the rest
		// of it later, dont ahve much time now
		// transport_index(source_cache, 9, target_cache, 9);// models
		// transport_index(source_cache, 8, target_cache, 8); //sprites
		// transport_index(source_cache, 16, target_cache, 16); // locs
		// transport_index(source_cache, 18, target_cache, 18);// npcs
		// transport_index(source_cache, 19, target_cache, 19); // objs/items try
		// transport_index(source_cache, 20, target_cache, 20); // seqs
		 //transport_index(source_cache, 26, target_cache, 26); // materials never use
		// tk2, its directx
		// transport_index(source_cache, 21, target_cache, 21); // spotanims
		// transport_index(source_cache, 27, target_cache, 27); // particles
		 //transport_index(source_cache, 28, target_cache, 28); // world map info
		 //transport_index(source_cache, 29, target_cache, 29); // billboards
		 //transport_index(source_cache, 53, target_cache, 9);// diffuse textures png
		 //transport_index(source_cache, 54, target_cache, 37);// hdr textures png
		 transport_archive(source_cache, 2, target_cache, 2, 1);// flos
		 transport_archive(source_cache, 2, target_cache, 2, 4);// flus
		 //transport_archive(source_cache, 2, target_cache, 2, 32);// skyboxes
		// transport_archive(source_cache, 2, target_cache, 2, 33); // skybox faces
		//transport_index(source_cache, 31, target_cache, 31); // shaders
		//transport_index(source_cache, 4, target_cache, 4); // SOUND EFFECTS
		//transport_index(source_cache, 14, target_cache, 14); // SOUND EFFECTS
		//transport_index(source_cache, 15, target_cache, 15); // SOUND EFFECTS
		 //transport_items_index(source_cache, 19, target_cache, 19);

	}
	// oh well, you might need to do it, ill leave textures for the last for now

	private static void transport_enum(Store source_cache, Store target_cache, int enumId) {// lmk when ready
		System.out.println("Attempting to transport enum id " + enumId);
		int group = getConfigGroup(enumId, 8);
		int file = getConfigFile(enumId, 8);
		target_cache.getIndexes()[17].putFile(group, file, source_cache.getIndexes()[17].getFile(group, file));
	}

	private static void transport_struct(Store source_cache, Store target_cache, int structId, boolean rebuild) {
		if (rebuild) {
			System.out.println("Attempting to transport struct id " + structId);
		} else {
			System.out.println("^\tAttempting to transport struct id " + structId);
		}
		byte[] data = source_cache.getIndexes()[22].getFile(getConfigGroup(structId, 5), getConfigFile(structId, 5));
		if (data != null) {
			if (rebuild) {
				target_cache.getIndexes()[2].putFile(26, structId, data);
			} else {
				target_cache.getIndexes()[2].putFile(26, structId, Constants.GZIP_COMPRESSION, data, null, false, false, -1, -1);
			}
		}
	}

	private static void transport_archive(Store source_cache, int source_id, Store target_cache, int target_id, int group_id) throws IOException {
		transport_archive(source_cache, source_id, target_cache, target_id, group_id, false);
	}

	private static void transport_archive(Store source_cache, int source_id, Store target_cache, int target_id, int group_id, boolean rewrite) throws IOException {
		Index target_index = target_cache.getIndexes()[target_id];
		System.out.println("Attempting to transport group of id " + group_id + "..");
		if (source_cache.getIndexes()[source_id].archiveExists(group_id)) {
			target_index.putArchive(source_id, group_id, source_cache, false, false);
		}
		if (rewrite) {
			System.out.println("\t ^ Rewriting table..");
			target_index.rewriteTable();
		}
		System.out.println("\t ^ Finished!");
		System.out.println();

	}

	private static void transport_index(Store source_cache, int source_id, Store target_cache, int target_id) throws IOException {
		System.out.println("Attempting to transport index from source id of " + source_id + " and target id of " + target_id);
		Index source_index = source_cache.getIndexes()[source_id];
		if (target_cache.getIndexes().length <= target_id) {
			if (target_cache.getIndexes().length != target_id) {
				throw new IllegalStateException("The cache has more than one gap between the source_index and the target_index!");
			}
			target_cache.addIndex(source_index.getTable().isNamed(), source_index.getTable().usesWhirpool(), Constants.GZIP_COMPRESSION);
			System.out.println("\t ^ Index was created!");
		}

		Index target_index = target_cache.getIndexes()[target_id];
		int num_groups = source_index.getLastArchiveId() + 1;
		System.out.println("\t ^ Attempting to pack " + num_groups + " group(s)..");

		double last = 0.0D;
		for (int group_id = 0; group_id < num_groups; group_id++) {
			if (source_index.archiveExists(group_id)) {
				target_index.putArchive(source_id, group_id, source_cache, false, false);
				double percentage = (double) group_id / (double) num_groups * 100D;
				if (group_id == num_groups - 1 || percentage - last >= 1.0D) {
					System.out.println("\t ^ Percentage Completed: " + format.format(percentage) + "%");
					last = percentage;
				}
			}
		}
		System.out.println("\t ^ Rewriting table..");
		target_index.rewriteTable();
		System.out.println("\t ^ Finished!");
		System.out.println();

	}

	private static int getConfigGroup(int id, int bits) {
		return id >> bits;
	}

	private static int getConfigFile(int id, int bits) {
		return id & (1 << bits) - 1;
	}

	private static void transport_items_index(Store source_cache, int source_id, Store target_cache, int target_id) throws IOException {
		for (int id = START_ITEM_ID; id <= END_ITEM_ID; id++) {
			byte[] data = source_cache.getIndexes()[19].getFile(id >>> 8, 0xff & id);
			target_cache.getIndexes()[19].putFile(id >>> 8, 0xff & id, Constants.GZIP_COMPRESSION, data, null, false, false, -1, -1);
		}

		System.out.println("\t ^ Rewriting table..");
		target_cache.getIndexes()[19].rewriteTable();
		System.out.println("\t ^ Finished!");
		System.out.println();

	}

}