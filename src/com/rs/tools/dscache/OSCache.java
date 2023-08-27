/**
 * 
 */
package com.rs.tools.dscache;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import com.alex.store.Index;
import com.alex.store.Store;
import com.alex.utils.Constants;

/**
 * @author dragonkk(Alex)
 * Jul 18, 2017
 */
public class OSCache {
	
	public static int MODELS_FOLDER = 0, OBJECTS_FOLDER = 1, MAPS_FOLDER = 2, SKELETONS_FOLDER = 3, SKINS_FOLDER = 4, ANIMATIONS_FOLDER = 5, GRAPHICS_FOLDER = 6, NPCS_FOLDER = 7, ITEMS_FOLDER = 8, FLOOR_FOLDER = 9;
	
	//to make sure no conflicts with rs2/rs3/customs models
	public static final int MODEL_OFFSET = 200000;
	public static final int OBJECTS_OFFSET = 100000;
	public static final int ANIMATIONS_OFFSET = 30000;
	public static final int GRAPHICS_OFFSET = 5000;
	public static final int FRAMES_OFFSET = 30000;
	public static final int ITEMS_OFFSET = 30000;
	public static final int NPCS_OFFSET = 30000;
	
	public static boolean ENABLE_OSRS = true;
	
	private static RandomAccessFile[] idxRaf;
	private static RandomAccessFile dataF;
	
	public static void init() {
		if (!ENABLE_OSRS) {
			return;
		}
		try {
			idxRaf = new RandomAccessFile[10];
			for (int i = 0; i < idxRaf.length; i++) {
				idxRaf[i] = new RandomAccessFile("dscache/main_file_cache.idx"+i, "rw");
			}
			dataF = new RandomAccessFile("dscache/main_file_cache.dat", "r");
		} catch (FileNotFoundException e) {
			ENABLE_OSRS = false;
			e.printStackTrace();
		}
	}
	

	private final static HashMap<Integer, int[]> keys = new HashMap<Integer, int[]>();
	
	public static final void loadUnpackedKeys() {
		try {
			File unpacked = new File("data/map/keys/keysOSRS/");
			File[] xteasFiles = unpacked.listFiles();
			for (File region : xteasFiles) {
				String name = region.getName();
				if (!name.contains(".txt")) {
					continue;
				}
				int regionId = Short.parseShort(name.replace(".txt", ""));
				if (regionId <= 0) {
					continue;
				}
				BufferedReader in = new BufferedReader(new FileReader(region));
				final int[] xteas = new int[4];
				for (int index = 0; index < 4; index++) 
					xteas[index] = Integer.parseInt(in.readLine());
				keys.put(regionId, xteas);
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		init();
		loadUnpackedKeys();
		Store osrsData = new Store(
				"C:\\Users\\alex\\Downloads\\OSRSCD_1.0.3\\data\\");
		
		byte[][][] cacheData = new byte[10][][];
		for (int i = 0; i < cacheData.length; i++) {
			System.out.println("loading: "+i);
			cacheData[i] = new byte[/*getFilesCount(i)*/200000][];
			for (int i2 = 0; i2 < cacheData[i].length; i2++)
				cacheData[i][i2] = getFileD(i, i2);
		}
		System.out.println("loading models.");
		Index osrsModels = osrsData.getIndexes()[Constants.MODELS_INDEX];
		for (int i = 0; i < osrsModels.getLastArchiveId() + 1; i++) {
			byte[] data = osrsModels.getFile(i);
			if (data != null)
				cacheData[MODELS_FOLDER][i] = data;
		}
		System.out.println("loading objects.");
		Index osrsConfig = osrsData.getIndexes()[2]; 
		for (int i = 0; i <= osrsConfig.getLastFileId(6); i++) {
			byte[] data = osrsConfig.getFile(6, i);
			if (data != null)
				cacheData[OBJECTS_FOLDER][i] = data;
		}
		System.out.println("loading maps.");
		
		for (int i = 0; i < 65535; i++) {
			int fromMapX = (i >> 8);
			int fromMapY = (i & 0xff);
			int mapArchiveID = osrsData.getIndexes()[5].getArchiveId("m"+fromMapX+"_"+fromMapY);
			int landscapeArchiveID = osrsData.getIndexes()[5].getArchiveId("l"+fromMapX+"_"+fromMapY);
			if (mapArchiveID == -1 || landscapeArchiveID == -1) 
				continue;
			byte[] mapSettingsData = osrsData.getIndexes()[5].getFile(mapArchiveID);
			byte[] objectsData = osrsData.getIndexes()[5].getFile(landscapeArchiveID, 0, keys.get(i));
			if (mapSettingsData == null || objectsData == null)
				continue;
			cacheData[MAPS_FOLDER][i * 2] = mapSettingsData;
			cacheData[MAPS_FOLDER][(i * 2) + 1] = objectsData;
		}
		
		System.out.println("loading skeletons.");
		Index osrsSkeletons = osrsData.getIndexes()[0];
		for (int i = 0; i <= osrsSkeletons.getLastArchiveId(); i++) {
			if (!osrsSkeletons.archiveExists(i))
				continue;
			ByteBuffer buffer = ByteBuffer.allocate(1000000);
			buffer.putShort((short) (osrsSkeletons.getLastFileId(i)+1));
			buffer.putShort((short) osrsSkeletons.getValidFilesCount(i));
			for (int i2 : osrsSkeletons.getTable().getArchives()[i].getValidFileIds()) {
				buffer.putShort((short) i2);
				buffer.put(osrsSkeletons.getFile(i, i2));
			}
			byte [] data = Arrays.copyOf(buffer.array(), buffer.position());
			cacheData[SKELETONS_FOLDER][i] = data;
		}
		System.out.println("loading skins.");
		Index osrsSkins = osrsData.getIndexes()[1];
		for (int i = 0; i <= osrsSkins.getLastArchiveId(); i++) {
			byte[] data = osrsSkins.getFile(i);
			if (data != null)
				cacheData[SKINS_FOLDER][i] = data;
		}
		System.out.println("loading animations.");
		for (int i = 0; i <= osrsConfig.getLastFileId(12); i++) {
			byte[] data = osrsConfig.getFile(12, i);
			if (data != null)
				cacheData[ANIMATIONS_FOLDER][i] = data;
		}
		System.out.println("loading graphics.");
		for (int i = 0; i <= osrsConfig.getLastFileId(13); i++) {
			byte[] data = osrsConfig.getFile(13, i);
			if (data != null)
				cacheData[GRAPHICS_FOLDER][i] = data;
		}
		System.out.println("loading npcs.");
		for (int i = 0; i <= osrsConfig.getLastFileId(9); i++) {
			byte[] data = osrsConfig.getFile(9, i);
			if (data != null)
				cacheData[NPCS_FOLDER][i] = data;
		}
		System.out.println("loading items.");
		for (int i = 0; i <= osrsConfig.getLastFileId(10); i++) {
			byte[] data = osrsConfig.getFile(10, i);
			if (data != null)
				cacheData[ITEMS_FOLDER][i] = data;
		}
		System.out.println("loading floor.");
		for (int i = 0; i <= osrsConfig.getLastFileId(4); i++) {
			byte[] data = osrsConfig.getFile(4, i);
			if (data != null)
				cacheData[FLOOR_FOLDER][i] = data;
		}
		
		try {
			RandomAccessFile[] idxRaf = new RandomAccessFile[10];
			for (int i = 0; i < idxRaf.length; i++) {
				idxRaf[i] = new RandomAccessFile("dscache/n/main_file_cache.idx"+i, "rw");
			}
			RandomAccessFile dataF = new RandomAccessFile("dscache/n/main_file_cache.dat", "rw");
			/*idxRaf[partition].seek(pos);
			pos = idxRaf[partition].readInt();
			int size = idxRaf[partition].readUnsignedShort();*/
			
			for (int i = 0; i < cacheData.length; i++) {
				System.out.println("packing: "+i);
				for (int i2 = 0; i2 < cacheData[i].length; i2++) {
					if (cacheData[i][i2] == null)
						continue;
					byte[] data = gzipCompress(cacheData[i][i2]);
					idxRaf[i].seek(i2 * 8);
					idxRaf[i].writeInt((int) dataF.getFilePointer());
					idxRaf[i].writeInt(data.length);
					dataF.write(data);
				}
				idxRaf[i].close();
			}
			dataF.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static byte[] getFileD(int partition, int id) {
		byte[] data = getFile(partition, id);
		return data == null ? null : decompress(data);
	}
	
	public static int getFilesCount(int partition) {
		if (!ENABLE_OSRS) {
			return 0;
		}
		try {
			return (int) (idxRaf[partition].length() / 8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static boolean containsFile(int partition, int id) {
		if (!ENABLE_OSRS) {
			return false;
		}
		try {
			synchronized (dataF) {
			int pos = id * 8;
			if (idxRaf[partition].length() < pos + 8) {
				return false;
			}
			idxRaf[partition].seek(pos);
			pos = idxRaf[partition].readInt();
			int size = idxRaf[partition].readInt();
			if (size == 0 || dataF.length() < pos + size) {
				return false;
			}
			return true;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static byte[] getFile(int partition, int id) {
		if (!ENABLE_OSRS) {
			return null;
		}
		try {
			synchronized (dataF) {
			int pos = id * 8;
			if (idxRaf[partition].length() < pos + 8) {
				return null;
			}
			idxRaf[partition].seek(pos);
			pos = idxRaf[partition].readInt();
			int size = idxRaf[partition].readInt();
			if (size == 0 || dataF.length() < pos + size) {
				return null;
			}
			byte[] data = new byte[size];
			dataF.seek(pos);
			dataF.readFully(data);
			return data;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static byte[] gzipCompress(byte[] uncompressedData) {
        byte[] result = new byte[]{};
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(uncompressedData.length);
             GZIPOutputStream gzipOS = new GZIPOutputStream(bos)) {
            gzipOS.write(uncompressedData);
            gzipOS.close();
            result = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
	
	class MyGZIPOutputStream 
    extends GZIPOutputStream {

    public MyGZIPOutputStream( OutputStream out ) throws IOException {
        super( out );
        def.setLevel(Deflater.BEST_COMPRESSION);
    } 
}
	
	public static byte[] decompress(byte[] data) {
		try {
			java.io.ByteArrayInputStream bytein = new java.io.ByteArrayInputStream(data);
			java.util.zip.GZIPInputStream gzin = new java.util.zip.GZIPInputStream(bytein);
			java.io.ByteArrayOutputStream byteout = new java.io.ByteArrayOutputStream();

			int res = 0;
			byte buf[] = new byte[1024];
			while (res >= 0) {
				res = gzin.read(buf, 0, buf.length);
				if (res > 0) {
					byteout.write(buf, 0, res);
				}
			}
			byte uncompressed[] = byteout.toByteArray();
			return uncompressed;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}
