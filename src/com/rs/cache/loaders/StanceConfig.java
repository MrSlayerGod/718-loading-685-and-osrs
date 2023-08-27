package com.rs.cache.loaders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.io.InputStream;
import com.rs.utils.Utils;

public class StanceConfig {

    public boolean aBool7015;
    public int[] loopAnimations;
    public int[] loopAnimDurations;
    int anInt7018;
    public int anInt7019;
    public int moveType1Anim;
    public int runRotate90CounterAnimation;
    public int rotate180Animation;
    public int rotate90Animation;
    public int rotate90CounterAnimation;
    public int hitbarSpriteID, hitbarSpriteID2;
    public int runAnimation;
    public int[] anIntArray7026;
    public int runRotate90Animation;
    public int anInt7028;
    public int standAnimation;
    public int type1_180;
    public int type1_90;
    public int type1_90_counter;
    public int anInt7033;
    public int anInt7034;
    public int anInt7035;
    public int anInt7036;
    public int anInt7037;
    public int anInt7038;
    public int anInt7039;
    public int anInt7040;
    public int anInt7041;
    public int anInt7042;
    public int[][] anIntArrayArray7043;
    public int[][] anIntArrayArray7044;
    public int anInt7046;
    public int[] anIntArray7047;
    public int anInt7048;
    public int walkAnimation;
    public int walkUpwardsAnimation;
    public int anInt7051;
    public int anInt7052;
    public int anInt7053;
    public int anInt7054;
    public int runRotate180Animation;
    public int anInt7056;
    public int anInt7057;
    public int anInt7058;

    private static final ConcurrentHashMap<Integer, StanceConfig> renderAimDefs = new ConcurrentHashMap<Integer, StanceConfig>();

    public static final StanceConfig forID(int emoteId) {
	StanceConfig defs = renderAimDefs.get(emoteId);
	if (defs != null)
	    return defs;
	if (emoteId == -1)
	    return null;
	byte[] data = Cache.STORE.getIndexes()[2].getFile(32, emoteId);
	defs = new StanceConfig();
	if (data != null)
	    defs.readValueLoop(new InputStream(data));
	renderAimDefs.put(emoteId, defs);
	return defs;
    }

    public int[] anIntArray1246;
    public int[][] anIntArrayArray1217;

    private void readValueLoop(InputStream stream) {
	for (;;) {
	    int opcode = stream.readUnsignedByte();
	    if (opcode == 0)
		break;
	    readValues(stream, opcode);
	}
    }

    private void readValues(InputStream buffer, int opcode) {
	if (1 == opcode) {
	    standAnimation = buffer.readBigSmart();
	    walkAnimation = buffer.readBigSmart();
	} else if (2 == opcode) {
	    moveType1Anim = buffer.readBigSmart();
	} else if (3 == opcode) {
	    type1_180 = buffer.readBigSmart();
	} else if (4 == opcode) {
	    type1_90 = buffer.readBigSmart();
	} else if (opcode == 5) {
	    type1_90_counter = buffer.readBigSmart();
	} else if (opcode == 6) {
	    runAnimation = buffer.readBigSmart();
	} else if (opcode == 7) {
	    runRotate180Animation = buffer.readBigSmart();
	} else if (opcode == 8) {
	    runRotate90Animation = buffer.readBigSmart();
	} else if (opcode == 9) {
	    runRotate90CounterAnimation = buffer.readBigSmart();
	} else if (opcode == 26) {
	    anInt7039 = (short) (buffer.readUnsignedByte() * 4);
	    anInt7040 = (short) (buffer.readUnsignedByte() * 4);
	} else if (opcode == 27) {
	    int count = buffer.readUnsignedByte();
	    if (null == anIntArrayArray7043) {
		anIntArrayArray7043 = new int[1 + count][];
	    } else if (count >= anIntArrayArray7043.length) {
		anIntArrayArray7043 = Arrays.copyOf(anIntArrayArray7043, count + 1);
	    }
	    anIntArrayArray7043[count] = new int[6];
	    for (int index = 0; index < 6; index++) {
		anIntArrayArray7043[count][index] = buffer.readShort();
	    }
	} else if (opcode == 28) {
	    int count = buffer.readUnsignedByte();
	    anIntArray7047 = new int[count];

	    for (int index = 0; index < count; index++) {
		anIntArray7047[index] = buffer.readUnsignedByte();
		if (255 == anIntArray7047[index]) {
		    anIntArray7047[index] = -1;
		}
	    }
	} else if (opcode == 29) {
	    anInt7048 = buffer.readUnsignedByte();
	} else if (30 == opcode) {
	    anInt7028 = buffer.readUnsignedShort();
	} else if (31 == opcode) {
	    anInt7054 = buffer.readUnsignedByte();
	} else if (32 == opcode) {
	    anInt7037 = buffer.readUnsignedShort();
	} else if (33 == opcode) {
	    anInt7019 = buffer.readShort();
	} else if (opcode == 34) {
	    anInt7053 = buffer.readUnsignedByte();
	} else if (35 == opcode) {
	    anInt7051 = buffer.readUnsignedShort();
	} else if (36 == opcode) {
	    anInt7052 = buffer.readShort();
	} else if (opcode == 37) {
	    anInt7056 = buffer.readUnsignedByte();
	} else if (opcode == 38) {
	    walkUpwardsAnimation = buffer.readBigSmart();
	} else if (opcode == 39) {
	    anInt7046 = buffer.readBigSmart();
	} else if (40 == opcode) {
	    rotate180Animation = buffer.readBigSmart();
	} else if (opcode == 41) {
	    rotate90Animation = buffer.readBigSmart();
	} else if (42 == opcode) {
	    rotate90CounterAnimation = buffer.readBigSmart();
	} else if (opcode == 43) {
		hitbarSpriteID = buffer.readUnsignedShort();
	} else if (opcode == 44) {
		hitbarSpriteID2  = buffer.readUnsignedShort();
	} else if (opcode == 45) {
	    anInt7057 = buffer.readUnsignedShort();
	} else if (opcode == 46) {
	    anInt7033 = buffer.readBigSmart();
	} else if (47 == opcode) {
	    anInt7034 = buffer.readBigSmart();
	} else if (48 == opcode) {
	    anInt7035 = buffer.readBigSmart();
	} else if (49 == opcode) {
	    anInt7036 = buffer.readBigSmart();
	} else if (opcode == 50) {
	    anInt7041 = buffer.readBigSmart();
	} else if (51 == opcode) {
	    anInt7038 = buffer.readBigSmart();
	} else if (opcode == 52) {
	    int count = buffer.readUnsignedByte();
	    loopAnimations = new int[count];
	    loopAnimDurations = new int[count];

	    for (int index = 0; index < count; index++) {
		loopAnimations[index] = buffer.readBigSmart();
		int i_8_ = buffer.readUnsignedByte();

		loopAnimDurations[index] = i_8_;
		anInt7018 += i_8_;
	    }
	} else if (53 == opcode) {
	    aBool7015 = false;
	} else if (54 == opcode) {
	    anInt7058 = (buffer.readUnsignedByte() << 6);
	    anInt7042 = (buffer.readUnsignedByte() << 6);
	} else if (55 == opcode) {
	    int index = buffer.readUnsignedByte();
	    if (null == anIntArray7026) {
		anIntArray7026 = new int[index + 1];
	    } else if (index >= anIntArray7026.length) {
		anIntArray7026 = Arrays.copyOf(anIntArray7026, 1 + index);
	    }

	    anIntArray7026[index] = buffer.readUnsignedShort();
	} else if (opcode == 56) {
	    int count = buffer.readUnsignedByte();
	    if (null == anIntArrayArray7044) {
		anIntArrayArray7044 = new int[count + 1][];
	    } else if (count >= anIntArrayArray7044.length) {
		anIntArrayArray7044 = Arrays.copyOf(anIntArrayArray7044, count + 1);
	    }

	    anIntArrayArray7044[count] = new int[3];
	    for (int index = 0; index < 3; index++) {
		anIntArrayArray7044[count][index] = buffer.readShort();
	    }
	}
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

    public static void main(String[] args) throws IOException {
	Cache.init();
/*
	File file = new File("./r2anims.txt");
	BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	for (int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
	    StanceConfig defs = StanceConfig
		    .forID(NPCConfig.forID(i).renderEmote);
	    if (defs != null) {
		writer.write(i + ", run: " + defs.runAnimation + ", walk: " + defs.walkAnimation + ", stand: " +defs.standAnimation+ ", loop: "
			+ Arrays.toString(defs.loopAnimations));
		writer.newLine();
		writer.flush();
	    }
	}
	writer.close();
	*/
/*	for (int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
	    StanceConfig defs = StanceConfig
		    .forID(NPCConfig.forID(i).renderEmote);
	    if (defs != null) {
		if (defs.walkAnimation == 3830)
		    System.out.println(NPCConfig.forID(i).renderEmote);
	    }
	}*/
for (int i = 0; i < 100000; i++) {
	   StanceConfig config = StanceConfig
			    .forID(i);
	   if (config.hitbarSpriteID != -1 || config.hitbarSpriteID2 != -1)
		   System.out.println(i+", "+config.hitbarSpriteID+", "+config.hitbarSpriteID2);
	   
}
    }

    public StanceConfig() {
	walkUpwardsAnimation = -1;
	anInt7046 = 0;
	walkAnimation = -1;
	rotate180Animation = -1;
	rotate90Animation = -1;
	rotate90CounterAnimation = -1;
	runAnimation = -1;
	runRotate180Animation = 0;
	runRotate90Animation = 0;
	runRotate90CounterAnimation = 0;
	moveType1Anim = 0;
	type1_180 = 0;
	type1_90 = 0;
	type1_90_counter = 0;
	anInt7033 = 0;
	anInt7034 = 0;
	anInt7035 = 0;
	anInt7036 = 0;
	anInt7041 = 0;
	anInt7038 = 0;
	anInt7056 = 0;
	anInt7057 = 0;
	hitbarSpriteID = -1;
	hitbarSpriteID2 = -1;
    }

}
