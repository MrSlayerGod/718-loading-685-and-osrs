package com.rs.cache.loaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.io.InputStream;
import com.rs.utils.Utils;

public class GraphicDefinitions {

	public short[] aShortArray1435;
	public short[] aShortArray1438;
	public int anInt1440;
	public boolean aBoolean1442;
	public int modelId;
	public int anInt1446;
	public boolean aBoolean1448 = false;
	public int anInt1449;
	public int emoteId;
	public int anInt1451;
	public int graphicsId;
	public int anInt1454;
	public short[] aShortArray1455;
	public short[] aShortArray1456;

	// added
	public byte byteValue;
	// added
	public int intValue;

	private static final ConcurrentHashMap<Integer, GraphicDefinitions> animDefs = new ConcurrentHashMap<Integer, GraphicDefinitions>();

	public static final GraphicDefinitions getAnimationDefinitions(int emoteId) {
		GraphicDefinitions defs = animDefs.get(emoteId);
		if (defs != null)
			return defs;
		byte[] data = Cache.STORE.getIndexes()[21].getFile(emoteId >>> 735411752, emoteId & 0xff);
		defs = new GraphicDefinitions();
		defs.graphicsId = emoteId;
		if (data != null)
			defs.readValueLoop(new InputStream(data));
		animDefs.put(emoteId, defs);
		return defs;
	}

	public static void main3(String... s) {
		try {
			Cache.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		System.out.println(getAnimationDefinitions(24).emoteId);
		System.out.println(AnimationDefinitions.getAnimationDefinitions(366).leftHandItem);
		System.out.println(AnimationDefinitions.getAnimationDefinitions(366).rightHandItem);
	*/
		for (int i = 0; i < 7000; i++) {
			GraphicDefinitions graph = GraphicDefinitions.getAnimationDefinitions(i);
			AnimationDefinitions config = AnimationDefinitions.getAnimationDefinitions(graph.emoteId);
			if (config.rightHandItem == 52804 || config.leftHandItem == 52804)
			System.out.println(i);
		}
	}
	
	
	
	public static int getAnimationBySound(int id, boolean effect2Sound) {
		for (int i = 0; i < Utils.getAnimationDefinitionsSize(); i++) {
			AnimationDefinitions defs =AnimationDefinitions.getAnimationDefinitions(i);
			if (defs == null || defs.handledSounds == null || defs.effect2Sound != effect2Sound)
				continue;
			for(int [] sounds : defs.handledSounds) {
				if(sounds != null )
					for(int s : sounds)
						if(s == id)
							return i;
			}
		}
		return -1;
	}
	
	public static int getGfxByAnimation(int id) {
		for (int i = 0; i < Utils.getGraphicDefinitionsSize(); i++) {
			GraphicDefinitions defs = getAnimationDefinitions(i);
			if (defs == null || defs.emoteId != id)
				continue;
			return i;
		}
		return -1;
	}
	
	
	public static final void main(String... s) {
		try {
			Cache.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//GraphicDefinitions original = GraphicDefinitions.getAnimationDefinitions(5697);
		int model = 236102;// NPCConfig.forID(28615).models[0];
		//int emoteId = original.emoteId;
		
		/*AnimationDefinitions defs = AnimationDefinitions.getAnimationDefinitions(emoteId);
		if(defs.handledSounds != null)
			for(int [] sounds : defs.handledSounds)
			if(sounds != null)
				System.out.print(Arrays.toString(sounds)+", ");
		/*/
		System.out.println(model);
		int offset = 500;
		for (int i = 0; i < Utils.getGraphicDefinitionsSize(); i++) {
			GraphicDefinitions def = GraphicDefinitions.getAnimationDefinitions(i);
			if (def == null)
				continue;
			if ((def.modelId >= model - offset && def.modelId <= model + offset) 
					 /*(def.emoteId >= emoteId - offset2 && def.emoteId <= emoteId + offset2)*/) {
				System.out.println("Possible match [id=" + i + ", model=" + def.modelId + "]."+", "+def.emoteId+", "+def.anInt1446+", "+def.anInt1449+", "+def.anInt1454);
			AnimationDefinitions defs2 = AnimationDefinitions.getAnimationDefinitions(def.emoteId);
			if(defs2.handledSounds != null)  {
				for(int [] sounds : defs2.handledSounds) {
					if(sounds != null )
					System.out.print(Arrays.toString(sounds)+", ");
				}
				System.out.println();
			}
			}
		}
		
	}

	private void readValueLoop(InputStream stream) {
		for (;;) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				break;
			readValues(stream, opcode);
		}
	}

	public void readValues(InputStream stream, int opcode) {
		if (opcode != 1) {
			if ((opcode ^ 0xffffffff) == -3)
				emoteId = stream.readBigSmart();
			else if (opcode == 4)
				anInt1446 = stream.readUnsignedShort();
			else if (opcode != 5) {
				if ((opcode ^ 0xffffffff) != -7) {
					if (opcode == 7)
						anInt1440 = stream.readUnsignedByte();
					else if ((opcode ^ 0xffffffff) == -9)
						anInt1451 = stream.readUnsignedByte();
					else if (opcode != 9) {
						if (opcode != 10) {
							if (opcode == 11) { // added opcode
								// aBoolean1442 = true;
								byteValue = (byte) 1;
							} else if (opcode == 12) { // added opcode
								// aBoolean1442 = true;
								byteValue = (byte) 4;
							} else if (opcode == 13) { // added opcode
								// aBoolean1442 = true;
								byteValue = (byte) 5;
							} else if (opcode == 14) { // added opcode
								// aBoolean1442 = true;
								// aByte2856 = 2;
								byteValue = (byte) 2;
								intValue = stream.readUnsignedByte() * 256;
							} else if (opcode == 15) {
								// aByte2856 = 3;
								byteValue = (byte) 3;
								intValue = stream.readUnsignedShort();
							} else if (opcode == 16) {
								// aByte2856 = 3;
								byteValue = (byte) 3;
								intValue = stream.readInt();
							} else if (opcode != 40) {
								if ((opcode ^ 0xffffffff) == -42) {
									int i = stream.readUnsignedByte();
									aShortArray1455 = new short[i];
									aShortArray1435 = new short[i];
									for (int i_0_ = 0; i > i_0_; i_0_++) {
										aShortArray1455[i_0_] = (short) (stream.readUnsignedShort());
										aShortArray1435[i_0_] = (short) (stream.readUnsignedShort());
									}
								}
							} else {
								int i = stream.readUnsignedByte();
								aShortArray1438 = new short[i];
								aShortArray1456 = new short[i];
								for (int i_1_ = 0; ((i ^ 0xffffffff) < (i_1_ ^ 0xffffffff)); i_1_++) {
									aShortArray1438[i_1_] = (short) stream.readUnsignedShort();
									aShortArray1456[i_1_] = (short) stream.readUnsignedShort();
								}
							}
						} else
							aBoolean1448 = true;
					} else {
						// aBoolean1442 = true;
						byteValue = (byte) 3;
						intValue = 8224;
					}
				} else
					anInt1454 = stream.readUnsignedShort();
			} else
				anInt1449 = stream.readUnsignedShort();
		} else
			modelId = stream.readBigSmart() + (this.graphicsId >= 5000 ? Settings.OSRS_MODEL_OFFSET : 0);
	}

	public GraphicDefinitions() {
		byteValue = 0;
		intValue = -1;
		anInt1446 = 128;
		aBoolean1442 = false;
		anInt1449 = 128;
		anInt1451 = 0;
		emoteId = -1;
		anInt1454 = 0;
		anInt1440 = 0;
	}

}
