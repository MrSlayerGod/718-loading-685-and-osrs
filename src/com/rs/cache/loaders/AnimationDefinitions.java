package com.rs.cache.loaders;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.game.World;
import com.rs.io.InputStream;

public class AnimationDefinitions {

	private int id;
	public int opcode8V;
	public int anInt2137;
	public int[] frameIDs;
	public int opcode9V;
	public boolean aBoolean2141 = false;
	public int priority;
	public int leftHandItem;
	public int rightHandItem = -1;
	public int[][] handledSounds;
	public boolean[] array3;
	public int[] anIntArray2151;
	public boolean aBoolean2152;
	public int[] frameCycles;
	public int opcode11V;
	public boolean aBoolean2158;
	public boolean aBoolean2159;
	public int resetOnMovement;
	public int opcode2V;

	// added
	public int[] soundMinDelay;
	public int[] soundMaxDelay;
	public int[] anIntArray1362;
	public boolean effect2Sound;
	public HashMap<Integer, Object> clientScriptData;
	
	private static final ConcurrentHashMap<Integer, AnimationDefinitions> animDefs = new ConcurrentHashMap<Integer, AnimationDefinitions>();

	
	public static void main(String[] args) throws IOException {
		Cache.init();
		/*for (int i = 0; i < 30000; i++) {
			AnimationDefinitions config = getAnimationDefinitions(i);
			if (config.rightHandItem == 11950 || config.leftHandItem == 11950)
			System.out.println(i);
		}*/
		   // emote = 20000 + 7478 + 10;  //(isEnraged() ? 10 : 0);
		   System.out.println(World.getAnimTicks(28440));
		   System.out.println((AnimationDefinitions.getAnimationDefinitions(16859).getEmoteClientCycles() / 30) - 1);
		  System.out.println(NPCConfig.forID(15510).boundSize);
			
	}
	
	public static final AnimationDefinitions getAnimationDefinitions(int emoteId) {
		try {
			AnimationDefinitions defs = animDefs.get(emoteId);
			if (defs != null)
				return defs;
			byte[] data = Cache.STORE.getIndexes()[20].getFile(emoteId >>> 7, emoteId & 0x7f);
			defs = new AnimationDefinitions(emoteId);
			if (data != null)
				defs.readValueLoop(new InputStream(data));
			defs.method2394();
			animDefs.put(emoteId, defs);
			return defs;
		} catch (Throwable t) {
			return null;
		}
	}

	private void readValueLoop(InputStream stream) {
		for (;;) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				break;
			if (id >= Settings.OSRS_ANIMATIONS_OFFSET)
				readValuesOSRS(stream, opcode);
			else
				readValues(stream, opcode);
		}
	}

	public int getEmoteTime() {
		if (frameCycles == null)
			return 0;
		int ms = 0;
		for (int i : frameCycles)
			ms += i;
		return ms * 30;
	}
	
	public int getEmoteClientCycles() {
		if (frameCycles == null)
			return 0;
		int r = 0;
		for (int i = 0; i < frameCycles.length - 3; i++) {
			r += frameCycles[i];
		}
		return r;
	}
	

	private void readValuesOSRS(InputStream stream, int opcode) {
		if (opcode == 1) {
			int i = stream.readUnsignedShort();
			frameCycles = new int[i];
			for (int i_16_ = 0; (i ^ 0xffffffff) < (i_16_ ^ 0xffffffff); i_16_++)
				frameCycles[i_16_] = stream.readUnsignedShort();
			frameIDs = new int[i];
			for (int i_17_ = 0; (i_17_ ^ 0xffffffff) > (i ^ 0xffffffff); i_17_++)
				frameIDs[i_17_] = stream.readUnsignedShort();
			for (int i_18_ = 0; i_18_ < i; i_18_++)
				frameIDs[i_18_] = ((stream.readUnsignedShort() << 16) + frameIDs[i_18_]);
		} else if ((opcode ^ 0xffffffff) != -3) {
			if ((opcode ^ 0xffffffff) != -4) {
				if ((opcode ^ 0xffffffff) == -5)
					aBoolean2152 = true;
				else if (opcode == 5)
					priority = stream.readUnsignedByte();
				else if (opcode != 6) {
					if ((opcode ^ 0xffffffff) == -8)
						leftHandItem = stream.readUnsignedShort() + Settings.OSRS_ITEM_OFFSET - 512;
					else if ((opcode ^ 0xffffffff) != -9) {
						if (opcode != 9) {
							if ((opcode ^ 0xffffffff) != -11) {
								if ((opcode ^ 0xffffffff) == -12)
									opcode11V = stream.readUnsignedByte();
								else if (opcode == 12) {
									int i = stream.readUnsignedByte();
									anIntArray2151 = new int[i];
									for (int i_19_ = 0; ((i_19_ ^ 0xffffffff) > (i ^ 0xffffffff)); i_19_++)
										anIntArray2151[i_19_] = stream.readUnsignedShort();
									for (int i_20_ = 0; i > i_20_; i_20_++)
										anIntArray2151[i_20_] = ((stream.readUnsignedShort() << 16)
												+ anIntArray2151[i_20_]);
								} else if ((opcode ^ 0xffffffff) != -14) {
									if (opcode != 14) {
										if (opcode != 15) {
											if (opcode == 16)
												aBoolean2158 = true;
											// added opcode
											else if (opcode == 17) {
												@SuppressWarnings("unused")
												int anInt2145 = stream.readUnsignedByte();
												// added opcode
											} else if (opcode == 18) {
												effect2Sound = true;
											} else if (opcode == 19 || opcode == 119) {
												if (anIntArray1362 == null) {
													anIntArray1362 = new int[handledSounds.length];
													for (int index = 0; index < handledSounds.length; index++)
														anIntArray1362[index] = 255;
												}
												int index;
												if (19 == opcode) {
													index = stream.readUnsignedByte();
												} else {
													index = stream.readUnsignedShort();
												}
												anIntArray1362[index] = stream.readUnsignedByte();
												// added opcode
											} else if (opcode == 20 || opcode == 120) {
												if ((soundMaxDelay == null) || (soundMinDelay == null)) {
													soundMaxDelay = (new int[handledSounds.length]);
													soundMinDelay = (new int[handledSounds.length]);
													for (int i_34_ = 0; (i_34_ < handledSounds.length); i_34_++) {
														soundMaxDelay[i_34_] = 256;
														soundMinDelay[i_34_] = 256;
													}
												}
												int index;
												if (opcode == 20) {
													index = stream.readUnsignedByte();
												} else {
													index = stream.readUnsignedShort();
												}
												soundMaxDelay[index] = stream.readUnsignedShort();
												soundMinDelay[index] = stream.readUnsignedShort();
											} else if (22 == opcode)
												stream.readUnsignedByte();
											else if (23 == opcode)
												stream.readUnsignedShort();
											else if (24 == opcode)
												stream.readUnsignedShort();
											else if (opcode == 249) {
												int length = stream.readUnsignedByte();
												if (clientScriptData == null)
													clientScriptData = new HashMap<Integer, Object>(length);
												for (int index = 0; index < length; index++) {
													boolean stringInstance = stream.readUnsignedByte() == 1;
													int key = stream.read24BitInt();
													Object value = stringInstance ? stream.readString() : stream.readInt();
													clientScriptData.put(key, value);
												}
											}
										} else
											aBoolean2159 = true;
									} else
										aBoolean2141 = true;
								} else {
									// opcode 13
									int i = 1;//stream.readUnsignedShort();
									handledSounds = new int[i][];
									for (int i_21_ = 0; i_21_ < i; i_21_++) {
										int i_22_ = stream.readUnsignedByte();
										if ((i_22_ ^ 0xffffffff) < -1) {
											handledSounds[i_21_] = new int[i_22_];
										//	handledSounds[i_21_][0] = stream.read24BitInt();
											for (int i_23_ = 0; ((i_22_ ^ 0xffffffff) < (i_23_
													^ 0xffffffff)); i_23_++) {
												handledSounds[i_21_][i_23_] = stream.read24BitInt();
											}
										}
									}
								}
							} else
								resetOnMovement = stream.readUnsignedByte();
						} else
							opcode9V = stream.readUnsignedByte();
					} else
						opcode8V = stream.readUnsignedByte();
				} else
					rightHandItem = stream.readUnsignedShort() + Settings.OSRS_ITEM_OFFSET - 512;
			} else {
				array3 = new boolean[256];
				int i = stream.readUnsignedByte();
				for (int i_24_ = 0; (i ^ 0xffffffff) < (i_24_ ^ 0xffffffff); i_24_++)
					array3[stream.readUnsignedByte()] = true;
			}
		} else
			opcode2V = stream.readUnsignedShort();
	}


	private void readValues(InputStream stream, int opcode) {
		if (opcode == 1) {
			int i = stream.readUnsignedShort();
			frameCycles = new int[i];
			for (int i_16_ = 0; (i ^ 0xffffffff) < (i_16_ ^ 0xffffffff); i_16_++)
				frameCycles[i_16_] = stream.readUnsignedShort();
			frameIDs = new int[i];
			for (int i_17_ = 0; (i_17_ ^ 0xffffffff) > (i ^ 0xffffffff); i_17_++)
				frameIDs[i_17_] = stream.readUnsignedShort();
			for (int i_18_ = 0; i_18_ < i; i_18_++)
				frameIDs[i_18_] = ((stream.readUnsignedShort() << 16) + frameIDs[i_18_]);
		} else if ((opcode ^ 0xffffffff) != -3) {
			if ((opcode ^ 0xffffffff) != -4) {
				if ((opcode ^ 0xffffffff) == -5)
					aBoolean2152 = true;
				else if (opcode == 5)
					priority = stream.readUnsignedByte();
				else if (opcode != 6) {
					if ((opcode ^ 0xffffffff) == -8)
						leftHandItem = stream.readUnsignedShort();
					else if ((opcode ^ 0xffffffff) != -9) {
						if (opcode != 9) {
							if ((opcode ^ 0xffffffff) != -11) {
								if ((opcode ^ 0xffffffff) == -12)
									opcode11V = stream.readUnsignedByte();
								else if (opcode == 12) {
									int i = stream.readUnsignedByte();
									anIntArray2151 = new int[i];
									for (int i_19_ = 0; ((i_19_ ^ 0xffffffff) > (i ^ 0xffffffff)); i_19_++)
										anIntArray2151[i_19_] = stream.readUnsignedShort();
									for (int i_20_ = 0; i > i_20_; i_20_++)
										anIntArray2151[i_20_] = ((stream.readUnsignedShort() << 16)
												+ anIntArray2151[i_20_]);
								} else if ((opcode ^ 0xffffffff) != -14) {
									if (opcode != 14) {
										if (opcode != 15) {
											if (opcode == 16)
												aBoolean2158 = true;
											// added opcode
											else if (opcode == 17) {
												@SuppressWarnings("unused")
												int anInt2145 = stream.readUnsignedByte();
												// added opcode
											} else if (opcode == 18) {
												effect2Sound = true;
											} else if (opcode == 19 || opcode == 119) {
												if (anIntArray1362 == null) {
													anIntArray1362 = new int[handledSounds.length];
													for (int index = 0; index < handledSounds.length; index++)
														anIntArray1362[index] = 255;
												}
												int index;
												if (19 == opcode) {
													index = stream.readUnsignedByte();
												} else {
													index = stream.readUnsignedShort();
												}
												anIntArray1362[index] = stream.readUnsignedByte();
												// added opcode
											} else if (opcode == 20 || opcode == 120) {
												if ((soundMaxDelay == null) || (soundMinDelay == null)) {
													soundMaxDelay = (new int[handledSounds.length]);
													soundMinDelay = (new int[handledSounds.length]);
													for (int i_34_ = 0; (i_34_ < handledSounds.length); i_34_++) {
														soundMaxDelay[i_34_] = 256;
														soundMinDelay[i_34_] = 256;
													}
												}
												int index;
												if (opcode == 20) {
													index = stream.readUnsignedByte();
												} else {
													index = stream.readUnsignedShort();
												}
												soundMaxDelay[index] = stream.readUnsignedShort();
												soundMinDelay[index] = stream.readUnsignedShort();
											} else if (22 == opcode)
												stream.readUnsignedByte();
											else if (23 == opcode)
												stream.readUnsignedShort();
											else if (24 == opcode)
												stream.readUnsignedShort();
											else if (opcode == 249) {
												int length = stream.readUnsignedByte();
												if (clientScriptData == null)
													clientScriptData = new HashMap<Integer, Object>(length);
												for (int index = 0; index < length; index++) {
													boolean stringInstance = stream.readUnsignedByte() == 1;
													int key = stream.read24BitInt();
													Object value = stringInstance ? stream.readString() : stream.readInt();
													clientScriptData.put(key, value);
												}
											}
										} else
											aBoolean2159 = true;
									} else
										aBoolean2141 = true;
								} else {
									// opcode 13
									int i = stream.readUnsignedShort();
									handledSounds = new int[i][];
									for (int i_21_ = 0; i_21_ < i; i_21_++) {
										int i_22_ = stream.readUnsignedByte();
										if ((i_22_ ^ 0xffffffff) < -1) {
											handledSounds[i_21_] = new int[i_22_];
											handledSounds[i_21_][0] = stream.read24BitInt();
											for (int i_23_ = 1; ((i_22_ ^ 0xffffffff) < (i_23_
													^ 0xffffffff)); i_23_++) {
												handledSounds[i_21_][i_23_] = stream.readUnsignedShort();
											}
										}
									}
								}
							} else
								resetOnMovement = stream.readUnsignedByte();
						} else
							opcode9V = stream.readUnsignedByte();
					} else
						opcode8V = stream.readUnsignedByte();
				} else
					rightHandItem = stream.readUnsignedShort();
			} else {
				array3 = new boolean[256];
				int i = stream.readUnsignedByte();
				for (int i_24_ = 0; (i ^ 0xffffffff) < (i_24_ ^ 0xffffffff); i_24_++)
					array3[stream.readUnsignedByte()] = true;
			}
		} else
			opcode2V = stream.readUnsignedShort();
	}

	public void method2394() {
		if (opcode9V == -1) {
			if (array3 == null)
				opcode9V = 0;
			else
				opcode9V = 2;
		}
		if (resetOnMovement == -1) {
			if (array3 == null)
				resetOnMovement = 0;
			else
				resetOnMovement = 2;
		}
	}

	public AnimationDefinitions(int emoteId) {
		this.id = emoteId;
		opcode8V = 99;
		leftHandItem = -1;
		opcode9V = -1;
		aBoolean2152 = false;
		priority = 5;
		aBoolean2159 = false;
		opcode2V = -1;
		opcode11V = 2;
		aBoolean2158 = false;
		resetOnMovement = -1;
	}

}
