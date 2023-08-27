package com.rs.cache.loaders;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.game.player.content.custom.CustomObjects;
import com.rs.game.player.content.dungeoneering.DungeonUtils;
import com.rs.io.InputStream;
import com.rs.utils.Utils;

@SuppressWarnings("unused")
public class ObjectConfig {

	private static final ConcurrentHashMap<Integer, ObjectConfig> objectDefinitions = new ConcurrentHashMap<Integer, ObjectConfig>();

	private short[] originalColors;
	public int[] toObjectIds;
	static int anInt3832;
	int[] animations = null;
	private int anInt3834;
	int anInt3835;
	static int anInt3836;
	private byte aByte3837;
	int objectIconID = -1;
	boolean reverse;
	private int lightness;
	private int scaleY;
	static int anInt3842;
	static int anInt3843;
	int anInt3844;
	boolean aBoolean3845;
	static int anInt3846;
	private byte aByte3847;
	private byte aByte3849;
	int anInt3850;
	int mapIconID;
	public boolean notDecoration;
	public boolean aBoolean3853;
	int opcode75V;
	public boolean ignoreClipOnAlternativeRoute;
	int anInt3857;
	private byte[] aByteArray3858;
	int[] opcode79A;
	int ambientSoundID;
	public String[] options;
	public int configFileId;
	private short[] modifiedColors;
	int anInt3865;
	boolean aBoolean3866;
	boolean updateFaces;
	public boolean solid;
	private int[] anIntArray3869;
	boolean aBoolean3870;
	public int sizeY;
	boolean opcode64V;
	boolean aBoolean3873;
	public int opcode23V;
	private int anInt3875;
	public int animation;
	private int anInt3877;
	private int shadow;
	public int cliped;
	private int anInt3881;
	public int anInt3882;
	private int opcode70V;
	Object loader;
	private int opcode71V;
	public int sizeX;
	public boolean aBoolean3891;
	int opcode28V;
	public int optionType;
	boolean aBoolean3894;
	boolean aBoolean3895;
	int anInt3896;
	int configId;
	public byte[] modelTypes;
	int opcode79V1;
	public String name;
	private int scaleX;
	int opcode78V2;
	int opcode79V2;
	boolean aBoolean3906;
	int[] anIntArray3908;
	public byte updateVertices;
	int anInt3913;
	private byte aByte3914;
	private int opcode72V;
	public int[][] modelIDs;
	private int scaleZ;
	/**
	 * Object anim shit 1
	 */
	private short[] modifiedTextures;
	/**
	 * Object anim shit 2
	 */
	private short[] originalTextures;
	int anInt3921;
	private HashMap<Integer, Object> parameters;
	boolean aBoolean3923;
	boolean aBoolean3924;
	int anInt3925;
	public int id;

	private int[] anIntArray4534;

	private byte[] unknownArray4;

	private byte[] unknownArray3;

	public int acessBlockFlag;

	public int[] collapseAllIds() {
		List<Integer> ids = new ArrayList<>();
		Arrays.stream(modelIDs).filter(Objects::nonNull).forEach(
				ints -> Arrays.stream(ints).filter(i->i>0).forEach(ids::add)
		);
		int[] idsArr = new int[ids.size()];
		for(int i = 0; i < ids.size(); i++)
			idsArr[i] = ids.get(i);
		return idsArr;
	}

	public static void main2(String[] args) throws IOException {
		Cache.init();
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < 12000; i++ ) {
			ObjectConfig o = forID(i);
			ObjectConfig os = forID(i + Settings.OSRS_OBJECTS_OFFSET);
			if (/*!o.name.equals("null") && !os.name.equals("null")
					&& o.name.equals(os.name)*/  o.name.equalsIgnoreCase(os.name)) {
				if (o.objectIconID != -1 && os.objectIconID != -1) {
				//	System.out.println(o.name+", "+o.mapIconID+", "+os.mapIconID);
					Integer icon = map.get(os.objectIconID);
					if (icon != null && icon != o.objectIconID) {
						System.out.println("wrong icon "+os.objectIconID+", to "+icon+" not, "+o.objectIconID);
						break;
					}else {
						if (icon != null)
							continue;
						map.put(os.objectIconID, o.objectIconID);
						System.out.println("map.put("+os.objectIconID+", "+o.objectIconID+");");
					}
				}
			}
		}
		//System.out.println(map);
	}
	
	//29521, 29578
	public static void main(String[] args) throws IOException {
		Cache.init();
		/*for (int i = 132700; i <= 134000; i++) {
			ObjectConfig defs = forID(i);
			if (defs.modelTypes != null && defs.modelTypes[0] == 22)
				System.out.println(i);
		}*/
		//5771
		System.out.println(forID(137739).animation);
		System.out.println(Arrays.toString(forID(137741).animations));///.modelIDs[0]));
	/*	System.out.println(Utils.getObjectDefinitionsSize());
		ObjectConfig defs = forID(11434);
		for (int i = 0; i < defs.options.length; i++)
			if (defs.options[i] != null)
				System.out.println("[" + i + "]:" + defs.options[i]);
		System.out.println(defs.name);
		System.out.println(Arrays.toString(defs.modelIDs[0]));
		System.out.println(defs.animation);
		System.out.println(defs.configId);
		System.out.println("Var config: " + defs.configFileId);
		System.out.println("SizeX: "+defs.sizeX+", SizeY: "+defs.sizeY);
		System.out.println(Arrays.toString(defs.toObjectIds));
		System.out.println(Arrays.toString(defs.modelIDs));
		System.out.println(defs.toObjectIds != null ? defs.toObjectIds.length : 0);
		System.out.println(Arrays.toString(defs.animations));
		System.out.println(Arrays.toString(defs.modelTypes));*/
		
	}

	public static int[] findDoorPairs(int openDoorId) {
		ObjectConfig open = forID(openDoorId);
		if (open == null || open.modelIDs == null)
			return new int[0];
		int length = 0;
		for (int i = 0; i < open.modelIDs.length; i++)
			length += open.modelIDs[i].length;

		int[] allModels = new int[length];

		int[] pairs = new int[100];
		int count = 0;

		main: for (int x = 0; x < Utils.getObjectDefinitionsSize(); x++) {
			ObjectConfig other = forID(x);
			if (other == null || other == open || other.modelIDs == null)
				continue;

			if (open.modelIDs.length != other.modelIDs.length)
				continue;

			for (int a = 0; a < open.modelIDs.length; a++) {
				if (open.modelIDs[a].length != other.modelIDs[a].length)
					continue main;

				int[] a1 = open.modelIDs[a];
				int[] a2 = other.modelIDs[a];

				for (int b = 0; b < a1.length; b++)
					if (a1[b] != a2[b])
						continue main;
			}

			pairs[count++] = x;
		}

		if (count == pairs.length)
			return pairs;

		int[] rebuff = new int[count];
		System.arraycopy(pairs, 0, rebuff, 0, count);
		return rebuff;

	}

	public String getFirstOption() {
		if (options == null || options.length < 1)
			return "";
		return options[0];
	}

	public String getSecondOption() {
		if (options == null || options.length < 2)
			return "";
		return options[1];
	}

	public String getOption(int option) {
		if (options == null || options.length < option || option == 0 || options[option - 1] == null)
			return "";
		return options[option - 1];
	}

	public String getThirdOption() {
		if (options == null || options.length < 3)
			return "";
		return options[2];
	}

	public boolean containsOption(int i, String option) {
		if (options == null || options[i] == null || options.length <= i)
			return false;
		return options[i].equalsIgnoreCase(option);
	}

	public boolean containsOption(String o) {
		if (options == null)
			return false;
		for (String option : options) {
			if (option == null)
				continue;
			if (option.equalsIgnoreCase(o))
				return true;
		}
		return false;
	}

	public boolean osrsVarEnabled;
	
	public ObjectConfig toObject(Player player) {
		if (toObjectIds == null)
			return this;
		int index = configFileId != -1 ? player.getVarsManager().getBitValue(configFileId) : configId != -1 ? player.getVarsManager().getValue(configId) : (toObjectIds.length - 1);
		if (index == -1 && id >= Settings.OSRS_OBJECTS_OFFSET) {
			Integer value = player.getVarsManager().getValue(index);
			index = value != null ? value : osrsVarEnabled ? 0 : (toObjectIds.length - 2); // last one
		}
		if (index >= toObjectIds.length || index < 0)
			index = toObjectIds.length - 1;
		return forID(toObjectIds[index]);
	}
	
	public String getToObjectName(Player player) {
		ObjectConfig config = toObject(player); 
		return config == null ? name : config.name;
	}
	
	private void readValues(InputStream stream, int opcode) {
		// System.out.println(opcode);
		if (opcode != 1 && opcode != 5) {
			if (opcode != 2) {
				if (opcode != 14) {
					if (opcode != 15) {
						if (opcode == 17) { // nocliped
							solid = false;
							cliped = 0;
						} else if (opcode != 18) {
							if (opcode == 19)
								optionType = stream.readUnsignedByte();
							else if (opcode == 21)
								updateVertices = (byte) 1;
							else if (opcode != 22) {
								if (opcode != 23) {
									if (opcode != 24) {
										if (opcode == 27) // cliped, no idea
											// diff between 2
											// and 1
											cliped = 1;
										else if (opcode == 28)
											opcode28V = (stream.readUnsignedByte() << 2);
										else if (opcode != 29) {
											if (opcode != 39) {
												if (opcode < 30 || opcode >= 35) {
													if (opcode == 40) {
														int i_53_ = (stream.readUnsignedByte());
														originalColors = new short[i_53_];
														modifiedColors = new short[i_53_];
														for (int i_54_ = 0; i_53_ > i_54_; i_54_++) {
															originalColors[i_54_] = (short) (stream.readUnsignedShort());
															modifiedColors[i_54_] = (short) (stream.readUnsignedShort());
														}
													} else if (44 == opcode) {
														int i_86_ = (short) stream.readUnsignedShort();
														int i_87_ = 0;
														for (int i_88_ = i_86_; i_88_ > 0; i_88_ >>= 1)
															i_87_++;
														unknownArray3 = new byte[i_87_];
														byte i_89_ = 0;
														for (int i_90_ = 0; i_90_ < i_87_; i_90_++) {
															if ((i_86_ & 1 << i_90_) > 0) {
																unknownArray3[i_90_] = i_89_;
																i_89_++;
															} else
																unknownArray3[i_90_] = (byte) -1;
														}
													} else if (opcode == 45) {
														int i_91_ = (short) stream.readUnsignedShort();
														int i_92_ = 0;
														for (int i_93_ = i_91_; i_93_ > 0; i_93_ >>= 1)
															i_92_++;
														unknownArray4 = new byte[i_92_];
														byte i_94_ = 0;
														for (int i_95_ = 0; i_95_ < i_92_; i_95_++) {
															if ((i_91_ & 1 << i_95_) > 0) {
																unknownArray4[i_95_] = i_94_;
																i_94_++;
															} else
																unknownArray4[i_95_] = (byte) -1;
														}
													} else if (opcode != 41) { // object
														// anim
														if (opcode != 42) {
															if (opcode != 62) {
																if (opcode != 64) {
																	if (opcode == 65)
																		scaleX = stream.readUnsignedShort();
																	else if (opcode != 66) {
																		if (opcode != 67) {
																			if (opcode == 69)
																				acessBlockFlag = stream.readUnsignedByte();
																			else if (opcode != 70) {
																				if (opcode == 71)
																					opcode71V = stream.readShort() << 2;
																				else if (opcode != 72) {
																					if (opcode == 73)
																						notDecoration = true;
																					else if (opcode == 74)
																						ignoreClipOnAlternativeRoute = true;
																					else if (opcode != 75) {
																						if (opcode != 77 && opcode != 92) {
																							if (opcode == 78) {
																								ambientSoundID = stream.readUnsignedShort();
																								opcode78V2 = stream.readUnsignedByte();
																							} else if (opcode != 79) {
																								if (opcode == 81) {
																									updateVertices = (byte) 2;
																									anInt3882 = 256 * stream.readUnsignedByte();
																								} else if (opcode != 82) {
																									if (opcode == 88)
																										aBoolean3853 = false;
																									else if (opcode != 89) {
																										if (opcode == 90)
																											aBoolean3870 = true;
																										else if (opcode != 91) {
																											if (opcode != 93) {
																												if (opcode == 94)
																													updateVertices = (byte) 4;
																												else if (opcode != 95) {
																													if (opcode != 96) {
																														if (opcode == 97)
																															aBoolean3866 = true;
																														else if (opcode == 98)
																															aBoolean3923 = true;
																														else if (opcode == 99) {
																															anInt3857 = stream.readUnsignedByte();
																															anInt3835 = stream.readUnsignedShort();
																														} else if (opcode == 100) {
																															anInt3844 = stream.readUnsignedByte();
																															anInt3913 = stream.readUnsignedShort();
																														} else if (opcode != 101) {
																															if (opcode == 102)
																																objectIconID = stream.readUnsignedShort();
																															else if (opcode == 103)
																																opcode23V = 0;
																															else if (opcode != 104) {
																																if (opcode == 105)
																																	aBoolean3906 = true;
																																else if (opcode == 106) {
																																	int i_55_ = stream.readUnsignedByte();
																																	anIntArray3869 = new int[i_55_];
																																	animations = new int[i_55_];
																																	for (int i_56_ = 0; i_56_ < i_55_; i_56_++) {
																																		animations[i_56_] = stream.readBigSmart();
																																		int i_57_ = stream.readUnsignedByte();
																																		anIntArray3869[i_56_] = i_57_;
																																		anInt3881 += i_57_;
																																	}
																																} else if (opcode == 107)
																																	mapIconID = stream.readUnsignedShort();
																																else if (opcode >= 150 && opcode < 155) {
																																	options[opcode + -150] = stream.readString();
																																} else if (opcode != 160) {
																																	if (opcode == 162) {
																																		updateVertices = (byte) 3;
																																		anInt3882 = stream.readInt();
																																	} else if (opcode == 163) {
																																		aByte3847 = (byte) stream.readByte();
																																		aByte3849 = (byte) stream.readByte();
																																		aByte3837 = (byte) stream.readByte();
																																		aByte3914 = (byte) stream.readByte();
																																	} else if (opcode != 164) {
																																		if (opcode != 165) {
																																			if (opcode != 166) {
																																				if (opcode == 167)
																																					anInt3921 = stream.readUnsignedShort();
																																				else if (opcode != 168) {
																																					if (opcode == 169) {
																																						aBoolean3845 = true;
																																						// added
																																						// opcode
																																					} else if (opcode == 170) {
																																						int anInt3383 = stream.readUnsignedSmart();
																																						// added
																																						// opcode
																																					} else if (opcode == 171) {
																																						int anInt3362 = stream.readUnsignedSmart();
																																						// added
																																						// opcode
																																					} else if (opcode == 173) {
																																						int anInt3302 = stream.readUnsignedShort();
																																						int anInt3336 = stream.readUnsignedShort();
																																						// added
																																						// opcode
																																					} else if (opcode == 177) {
																																						boolean ub = true;
																																						// added
																																						// opcode
																																					} else if (opcode == 178) {
																																						int db = stream.readUnsignedByte();
																																					} else if (opcode == 189) {
																																						boolean bloom = true;
																																					} else if (opcode >= 190 && opcode < 196) {
																																						if (anIntArray4534 == null) {
																																							anIntArray4534 = new int[6];
																																							Arrays.fill(anIntArray4534, -1);
																																						}
																																						anIntArray4534[opcode - 190] = stream.readUnsignedShort();
																																					} else if (opcode == 249) {
																																						int length = stream.readUnsignedByte();
																																						if (parameters == null)
																																							parameters = new HashMap<Integer, Object>(length);
																																						for (int i_60_ = 0; i_60_ < length; i_60_++) {
																																							boolean bool = stream.readUnsignedByte() == 1;
																																							int i_61_ = stream.read24BitInt();
																																							if (!bool)
																																								parameters.put(i_61_, stream.readInt());
																																							else
																																								parameters.put(i_61_, stream.readString());

																																						}
																																					}
																																				} else
																																					aBoolean3894 = true;
																																			} else
																																				anInt3877 = stream.readShort();
																																		} else
																																			anInt3875 = stream.readShort();
																																	} else
																																		anInt3834 = stream.readShort();
																																} else {
																																	int i_62_ = stream.readUnsignedByte();
																																	anIntArray3908 = new int[i_62_];
																																	for (int i_63_ = 0; i_62_ > i_63_; i_63_++)
																																		anIntArray3908[i_63_] = stream.readUnsignedShort();
																																}
																															} else
																																anInt3865 = stream.readUnsignedByte();
																														} else
																															anInt3850 = stream.readUnsignedByte();
																													} else
																														aBoolean3924 = true;
																												} else {
																													updateVertices = (byte) 5;
																													anInt3882 = stream.readShort();
																												}
																											} else {
																												updateVertices = (byte) 3;
																												anInt3882 = stream.readUnsignedShort();
																											}
																										} else
																											aBoolean3873 = true;
																									} else
																										aBoolean3895 = false;
																								} else
																									aBoolean3891 = true;
																							} else {
																								opcode79V1 = stream.readUnsignedShort();
																								opcode79V2 = stream.readUnsignedShort();
																								opcode78V2 = stream.readUnsignedByte();
																								int i_64_ = stream.readUnsignedByte();
																								opcode79A = new int[i_64_];
																								for (int i_65_ = 0; i_65_ < i_64_; i_65_++)
																									opcode79A[i_65_] = stream.readUnsignedShort();
																							}
																						} else {
																							configFileId = stream.readUnsignedShort();
																							if (configFileId == 65535)
																								configFileId = -1;
																							configId = stream.readUnsignedShort();
																							if (configId == 65535)
																								configId = -1;
																							int i_66_ = -1;
																							if (opcode == 92) {
																								i_66_ = stream.readBigSmart();
																							}
																							int i_67_ = stream.readUnsignedByte();
																							toObjectIds = new int[i_67_ - -2];
																							for (int i_68_ = 0; i_67_ >= i_68_; i_68_++) {
																								toObjectIds[i_68_] = stream.readBigSmart();
																							}
																							toObjectIds[i_67_ + 1] = i_66_;
																						}
																					} else
																						opcode75V = stream.readUnsignedByte();
																				} else
																					opcode72V = stream.readShort() << 2;
																			} else
																				opcode70V = stream.readShort() << 2;
																		} else
																			scaleZ = stream.readUnsignedShort();
																	} else
																		scaleY = stream.readUnsignedShort();
																} else
																	// 64
																	opcode64V = false;
															} else
																reverse = true;
														} else {
															int i_69_ = (stream.readUnsignedByte());
															aByteArray3858 = (new byte[i_69_]);
															for (int i_70_ = 0; i_70_ < i_69_; i_70_++)
																aByteArray3858[i_70_] = (byte) (stream.readByte());
														}
													} else { 
														int i_71_ = (stream.readUnsignedByte());
														originalTextures = new short[i_71_];
														modifiedTextures = new short[i_71_];
														for (int i_72_ = 0; i_71_ > i_72_; i_72_++) {
															originalTextures[i_72_] = (short) (stream.readUnsignedShort());
															modifiedTextures[i_72_] = (short) (stream.readUnsignedShort());
														}
													}
												} else {
													options[-30 + opcode] = (stream.readString());
												}
											} else
												// 39
												lightness = (stream.readByte() * 5);
										} else {// 29
											shadow = stream.readByte();
										}
									} else {
										animation = stream.readBigSmart();
									}
								} else
									opcode23V = 1;
							} else
								updateFaces = true;
						} else
							solid = false;
					} else
						// 15
						sizeY = stream.readUnsignedByte();
				} else
					// 14
					sizeX = stream.readUnsignedByte();
			} else {
				name = stream.readString();
			}
		} else {
			boolean aBoolean1162 = false;
			if (opcode == 5 && aBoolean1162)
				skipReadModelIds(stream);
			int i_73_ = stream.readUnsignedByte();
			modelIDs = new int[i_73_][];
			modelTypes = new byte[i_73_];
			for (int i_74_ = 0; i_74_ < i_73_; i_74_++) {
				modelTypes[i_74_] = (byte) stream.readByte();
				int i_75_ = stream.readUnsignedByte();
				modelIDs[i_74_] = new int[i_75_];
				for (int i_76_ = 0; i_75_ > i_76_; i_76_++)
					modelIDs[i_74_][i_76_] = stream.readBigSmart();
			}
			if (opcode == 5 && !aBoolean1162)
				skipReadModelIds(stream);
		}
	}
	

	private void decodeOSRS(InputStream buffer) {
		for(;;) {
			int opcode = buffer.readUnsignedByte();
			if(opcode == 0)
				break;
			if(opcode == 1) {
				int length = buffer.readUnsignedByte();
				int[] ids = new int[length];
				int [] types = new int[length];
				for (int i = 0; i < length; i++) {
					ids[i] = buffer.readUnsignedShort();
					types[i] = buffer.readUnsignedByte();
				}
				if (modelIDs == null) {
					modelIDs = new int[ids.length][];
					modelTypes = new byte[ids.length];
					for (int i = 0; i < ids.length; i++) {
						modelIDs[i] = new int[1];
						modelIDs[i][0] = ids[0];
						modelTypes[i] = (byte) types[i];
					}
				}
			} else if (opcode == 2) 
				name = buffer.readString();
			else if (opcode == 5) {
				int length = buffer.readUnsignedByte();
				int[] ids = new int[length];
				for (int i = 0; i < length; i++) 
					ids[i] = buffer.readUnsignedShort();
				if (modelIDs == null) { //skip low detail
					modelTypes = new byte[23];
					modelIDs = new int[23][];
					for (int i = 0; i <= 22; i++) {
						modelTypes[i] = (byte) i;
						modelIDs[i] = ids;
					}
				}
			} else if (opcode == 14)
				sizeX = buffer.readUnsignedByte();
			else if (opcode == 15)
				sizeY = buffer.readUnsignedByte();
			else if (opcode == 17) {
				cliped = 0;
				solid = false;
			} else if (opcode == 18) 
				solid = false;
			else if (opcode == 19)
				optionType = buffer.readUnsignedByte();
			else if (opcode == 21)
				updateVertices = 0;
			else if (opcode == 22)
				updateFaces = true;
			else if (opcode == 23)
				opcode23V = 1;
			else if (opcode == 24) {
				animation = buffer.readUnsignedShort();
				if (animation == 65535)
					animation = -1;
			} else if (opcode == 27)
				cliped = 1;
			else if (opcode == 28)
				opcode28V = buffer.readUnsignedByte();
			else if (opcode == 29)
				shadow = buffer.readByte();
			else if (opcode == 39)
				lightness = buffer.readByte() * 25;
			else if (opcode >= 30 && opcode < 35) {
				options[opcode - 30] = buffer.readString();
				if (options[opcode - 30].equalsIgnoreCase("Hidden")) 
					options[opcode - 30] = null;
			} else if (opcode == 40) {
				int i_6_ = buffer.readUnsignedByte();
				((ObjectConfig) this).originalColors = new short[i_6_];
				modifiedColors = new short[i_6_];
				for (int i_7_ = 0; i_7_ < i_6_; i_7_++) {
					((ObjectConfig) this).originalColors[i_7_] = (short) buffer.readUnsignedShort();
					modifiedColors[i_7_] = (short) buffer.readUnsignedShort();
				}
			} else if (opcode == 41) {
				int i_8_ = buffer.readUnsignedByte();
				((ObjectConfig) this).originalTextures = new short[i_8_];
				modifiedTextures = new short[i_8_];
				for (int i_9_ = 0; i_9_ < i_8_; i_9_++) {
					((ObjectConfig) this).originalTextures[i_9_] = (short) buffer.readUnsignedShort();
					modifiedTextures[i_9_] = (short) buffer.readUnsignedShort();
				}
			} else if (opcode == 62)
				reverse = true;
			else if (opcode == 64) 
				opcode64V = false;
			else if (opcode == 65)
				scaleX = buffer.readUnsignedShort();
			else if (opcode == 66)
				scaleY = buffer.readUnsignedShort();
			else if (opcode == 67)
				scaleZ = buffer.readUnsignedShort();
			else if (opcode == 68)  //osrs worldmap sprite. we dont use done
				objectIconID = buffer.readUnsignedShort();
			else if (opcode == 69)
				acessBlockFlag = buffer.readUnsignedByte();
			else if (opcode == 70)
				opcode70V = buffer.readShort();
			else if (opcode == 71)
				opcode71V = buffer.readShort();
			else if (opcode == 72)
				opcode72V = buffer.readShort();
			else if (opcode == 73)
				notDecoration = true;
			else if (opcode == 74)
				ignoreClipOnAlternativeRoute = true;
			else if (opcode == 75)
				opcode75V = buffer.readUnsignedByte();
			else if (opcode == 78) {
				ambientSoundID = buffer.readUnsignedShort();
				opcode78V2 = buffer.readUnsignedByte();
			} else if (opcode == 79) {
				opcode79V1 = buffer.readUnsignedShort();
				opcode79V2 = buffer.readUnsignedShort();
				opcode78V2 = buffer.readUnsignedByte();
				int length = buffer.readUnsignedByte();
				opcode79A = new int[length];
				for (int i = 0; i < length; i++) 
					this.opcode79A[i] = buffer.readUnsignedShort();
			} else if (opcode == 81) 
				updateVertices = (byte) (buffer.readUnsignedByte() * 256);
			else if (opcode == 77 || opcode == 92) {
				configFileId = buffer.readUnsignedShort();
				if (configFileId == 65535 || true)
					configFileId = -1;
				configId = buffer.readUnsignedShort();
				if (configId == 65535 || true)
					configId = -1;
				int defaultObjectID = -1;
				if (opcode == 92) {
					defaultObjectID = buffer.readUnsignedShort();
					if (defaultObjectID == 65535)
						defaultObjectID = -1;
					else
						defaultObjectID += Settings.OSRS_OBJECTS_OFFSET;
				}
				int length = buffer.readUnsignedByte();
				toObjectIds = new int[length + 2];
				for (int i = 0; i <= length; i++) {
					toObjectIds[i] = buffer.readUnsignedShort();
					if (toObjectIds[i] == 65535)
						toObjectIds[i] = -1;
					else
						toObjectIds[i] += Settings.OSRS_OBJECTS_OFFSET;
				}
				toObjectIds[1 + length] = defaultObjectID;
			} else if (82 == opcode) { //osrs map icon id. new update.
				mapIconID = buffer.readUnsignedShort();
			} else if (opcode == 249) {
				int length = buffer.readUnsignedByte();
				if (parameters == null)
					parameters = new HashMap<Integer, Object>(length);
				for (int i_60_ = 0; i_60_ < length; i_60_++) {
					boolean bool = buffer.readUnsignedByte() == 1;
					int i_61_ = buffer.read24BitInt();
					if (!bool)
						parameters.put(i_61_, buffer.readInt());
					else
						parameters.put(i_61_, buffer.readString());

				}
			} else 
				System.out.println("Error loading osrs object. Missing opcode: "+opcode);
		}
	}

	private void skipReadModelIds(InputStream stream) {
		int length = stream.readUnsignedByte();
		for (int index = 0; index < length; index++) {
			stream.skip(1);
			int length2 = stream.readUnsignedByte();
			for (int i = 0; i < length2; i++)
				stream.readBigSmart();
		}
	}

	private void readValueLoop(InputStream stream) {
		if (id >= Settings.OSRS_OBJECTS_OFFSET) {
			decodeOSRS(stream);
			return;
		}
		for (;;) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0) {
				// System.out.println("Remaining: "+stream.getRemaining());
				break;
			}
			readValues(stream, opcode);
		}
	}

	private ObjectConfig() {
		anInt3835 = -1;
		ambientSoundID = -1;
		configFileId = -1;
		aBoolean3866 = false;
		mapIconID = -1;
		anInt3865 = 255;
		aBoolean3845 = false;
		updateFaces = false;
		anInt3850 = 0;
		anInt3844 = -1;
		anInt3881 = 0;
		anInt3857 = -1;
		opcode64V = true;
		anInt3882 = -1;
		anInt3834 = 0;
		options = new String[5];
		anInt3875 = 0;
		reverse = false;
		anIntArray3869 = null;
		sizeY = 1;
		opcode23V = -1;
		opcode70V = 0;
		aBoolean3895 = true;
		lightness = 0;
		aBoolean3870 = false;
		opcode71V = 0;
		aBoolean3853 = true;
		notDecoration = false;
		cliped = 2;
		solid = true;
		ignoreClipOnAlternativeRoute = false;
		opcode75V = -1;
		shadow = 0;
		opcode78V2 = 0;
		sizeX = 1;
		animation = -1;
		aBoolean3891 = false;
		opcode79V2 = 0;
		name = "null";
		anInt3913 = -1;
		aBoolean3906 = false;
		aBoolean3873 = false;
		aByte3914 = (byte) 0;
		opcode72V = 0;
		opcode79V1 = 0;
		optionType = -1;
		aBoolean3894 = false;
		updateVertices = (byte) 0;
		anInt3921 = 0;
		scaleX = 128;
		configId = -1;
		anInt3877 = 0;
		anInt3925 = 0;
		opcode28V = 64;
		aBoolean3923 = false;
		aBoolean3924 = false;
		scaleY = 128;
		scaleZ = 128;
	}

	final void method3287() {
		if (optionType == -1) {
			optionType = 0;
			if (modelTypes != null && modelTypes.length == 1 && modelTypes[0] == 10)
				optionType = 1;
			for (int i_13_ = 0; i_13_ < 5; i_13_++) {
				if (options[i_13_] != null) {
					optionType = 1;
					break;
				}
			}
		}
		if (opcode75V == -1)
			opcode75V = cliped != 0 ? 1 : 0;
	}

	private static int getArchiveId(int i_0_) {
		return i_0_ >>> -1135990488;
	}

	public static ObjectConfig forID(int id) {
		ObjectConfig def = objectDefinitions.get(id);
		if (def == null) {
			def = new ObjectConfig();
			def.id = id;
			byte[] data = Cache.STORE.getIndexes()[16].getFile(getArchiveId(id), id & 0xff);
			if (data == null) {
				// System.out.println("Failed loading Object " + id + ".");
			} else
				def.readValueLoop(new InputStream(data));
			def.method3287();
			/*
			if(def.name.equalsIgnoreCase("bank booth") || def.name.equalsIgnoreCase("counter")) {
			def.notCliped = false;
			def.projectileCliped = true;
			if (def.clipType == 0)
			    def.clipType = 1;
			} else if (DungeonUtils.isDoor(id) || DungeonUtils.isBossDoor(id)) {
			def.notCliped = false;
			def.projectileCliped = true;
			if (def.clipType == 0)
			    def.clipType = 1;
			}
			if (def.notCliped) {
			def.projectileCliped = false;
			def.clipType = 0;
			}*/
			if (DungeonUtils.isClipped(id)) {
				def.ignoreClipOnAlternativeRoute = false;
				def.solid = true;
				def.cliped = 1;
			}
			CustomObjects.modify(def);
			objectDefinitions.put(id, def);
		}
		return def;
	}

	public int getClipType() {
		return cliped;
	}

	public boolean isProjectileCliped() {
		return solid;
	}

	public int getSizeX() {
		return sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public int getAccessBlockFlag() {
		return acessBlockFlag;
	}

	public static void clearObjectDefinitions() {
		objectDefinitions.clear();
	}

	/**
	 * Prints all fields in this class.
	 */
	public void printFields() {
		for (Field field : getClass().getDeclaredFields()) {
			if ((field.getModifiers() & 8) != 0) {
				continue;
			}
			try {
				System.out.println(field.getName() + ": " + getValue(field));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		System.out.println("-- end of " + getClass().getSimpleName() + " fields --");
	}

	private Object getValue(Field field) throws Throwable {
		field.setAccessible(true);
		Class<?> type = field.getType();
		if (type == int[][].class) {
			return Arrays.toString((int[][]) field.get(this));
		} else if (type == int[].class) {
			return Arrays.toString((int[]) field.get(this));
		} else if (type == byte[].class) {
			return Arrays.toString((byte[]) field.get(this));
		} else if (type == short[].class) {
			return Arrays.toString((short[]) field.get(this));
		} else if (type == double[].class) {
			return Arrays.toString((double[]) field.get(this));
		} else if (type == float[].class) {
			return Arrays.toString((float[]) field.get(this));
		} else if (type == Object[].class) {
			return Arrays.toString((Object[]) field.get(this));
		}
		return field.get(this);
	}

	public boolean hasOption(String name) {
		for (String s : options)
			if (s != null && s.equalsIgnoreCase(name))
				return true;
		return false;

	}

}
