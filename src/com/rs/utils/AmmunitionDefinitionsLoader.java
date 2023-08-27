package com.rs.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;

public class AmmunitionDefinitionsLoader {

	private static final String PATH = "data/item/ammo.dat";
	public static AmmunitionDefinition DEFAULT_AMMO_DEFINITION = new AmmunitionDefinition(-1, -1);

	public final static HashMap<Integer, AmmunitionDefinition> ammoDefs = new HashMap<Integer, AmmunitionDefinition>();
	
	private static void loadNext() {
		ammoDefs.put(812, new AmmunitionDefinition(226, -1)); //Bronze dart (p)
		ammoDefs.put(813, new AmmunitionDefinition(227, -1)); //Iron dart (p)
		ammoDefs.put(831, new AmmunitionDefinition(200, -1)); //Bronze javelin (p)
		ammoDefs.put(832, new AmmunitionDefinition(201, -1)); //Iron javelin (p)
		ammoDefs.put(833, new AmmunitionDefinition(202, -1)); //Steel javelin (p)
		ammoDefs.put(834, new AmmunitionDefinition(201, -1)); //Mithril javelin (p)
		ammoDefs.put(835, new AmmunitionDefinition(204, -1)); //Adamant javelin (p)
		ammoDefs.put(836, new AmmunitionDefinition(205, -1)); //Rune javelin (p)
		ammoDefs.put(870, new AmmunitionDefinition(212, -1)); //Bronze knife (p)
		ammoDefs.put(872, new AmmunitionDefinition(214, -1)); //Steel knife (p)
		ammoDefs.put(873, new AmmunitionDefinition(216, -1)); //Mithril knife (p)
		ammoDefs.put(874, new AmmunitionDefinition(215, -1)); //Black knife (p)
		ammoDefs.put(875, new AmmunitionDefinition(217, -1)); //Adamant knife (p)
		ammoDefs.put(876, new AmmunitionDefinition(218, -1)); //Rune knife (p)
		ammoDefs.put(878, new AmmunitionDefinition(27, -1)); //Bronze bolts (p)
		ammoDefs.put(883, new AmmunitionDefinition(10, 19)); //Bronze arrow (p)
		ammoDefs.put(885, new AmmunitionDefinition(9, 18)); //Iron arrow (p)
		ammoDefs.put(887, new AmmunitionDefinition(11, 20)); //Steel arrow (p)
		ammoDefs.put(889, new AmmunitionDefinition(12, 21)); //Mithril arrow (p)
		ammoDefs.put(891, new AmmunitionDefinition(13, 22)); //Adamant arrow (p)
		ammoDefs.put(893, new AmmunitionDefinition(15, 24)); //Rune arrow (p)
		ammoDefs.put(3094, new AmmunitionDefinition(34, -1)); //Black dart (p)
		ammoDefs.put(5616, new AmmunitionDefinition(10, 19)); //Bronze arrow (p+)
		ammoDefs.put(5617, new AmmunitionDefinition(9, 18)); //Iron arrow (p+)
		ammoDefs.put(5618, new AmmunitionDefinition(11, 20)); //Steel arrow (p+)
		ammoDefs.put(5619, new AmmunitionDefinition(12, 21)); //Mithril arrow (p+)
		ammoDefs.put(5620, new AmmunitionDefinition(13, 22)); //Adamant arrow (p+)
		ammoDefs.put(5621, new AmmunitionDefinition(15, 24)); //Rune arrow (p+)
		ammoDefs.put(5622, new AmmunitionDefinition(10, 19)); //Bronze arrow (p++)
		ammoDefs.put(5623, new AmmunitionDefinition(9, 18)); //Iron arrow (p++)
		ammoDefs.put(5624, new AmmunitionDefinition(11, 20)); //Steel arrow (p++)
		ammoDefs.put(5625, new AmmunitionDefinition(12, 21)); //Mithril arrow (p++)
		ammoDefs.put(5626, new AmmunitionDefinition(13, 22)); //Adamant arrow (p++)
		ammoDefs.put(5627, new AmmunitionDefinition(15, 24)); //Rune arrow (p++)
		ammoDefs.put(5632, new AmmunitionDefinition(229, -1)); //Mithril dart (p+)
		ammoDefs.put(5633, new AmmunitionDefinition(230, -1)); //Adamant dart (p+)
		ammoDefs.put(5634, new AmmunitionDefinition(231, -1)); //Rune dart (p+)
		ammoDefs.put(5635, new AmmunitionDefinition(226, -1)); //Bronze dart (p++)
		ammoDefs.put(5636, new AmmunitionDefinition(227, -1)); //Iron dart (p++)
		ammoDefs.put(5637, new AmmunitionDefinition(228, -1)); //Steel dart (p++)
		ammoDefs.put(5638, new AmmunitionDefinition(34, -1)); //Black dart (p++)
		ammoDefs.put(5639, new AmmunitionDefinition(229, -1)); //Mithril dart (p++)
		ammoDefs.put(5640, new AmmunitionDefinition(230, -1)); //Adamant dart (p++)
		ammoDefs.put(5641, new AmmunitionDefinition(231, -1)); //Rune dart (p++)
		ammoDefs.put(5642, new AmmunitionDefinition(200, -1)); //Bronze javelin (p+)
		ammoDefs.put(5643, new AmmunitionDefinition(201, -1)); //Iron javelin (p+)
		ammoDefs.put(5644, new AmmunitionDefinition(202, -1)); //Steel javelin (p+)
		ammoDefs.put(5645, new AmmunitionDefinition(201, -1)); //Mithril javelin (p+)
		ammoDefs.put(5646, new AmmunitionDefinition(204, -1)); //Adamant javelin (p+)
		ammoDefs.put(5647, new AmmunitionDefinition(205, -1)); //Rune javelin (p+)
		ammoDefs.put(5649, new AmmunitionDefinition(201, -1)); //Iron javelin (p++)
		ammoDefs.put(5650, new AmmunitionDefinition(202, -1)); //Steel javelin (p++)
		ammoDefs.put(5651, new AmmunitionDefinition(201, -1)); //Mithril javelin (p++)
		ammoDefs.put(5652, new AmmunitionDefinition(204, -1)); //Adamant javelin (p++)
		ammoDefs.put(5653, new AmmunitionDefinition(205, -1)); //Rune javelin (p++)
		ammoDefs.put(5654, new AmmunitionDefinition(212, -1)); //Bronze knife (p+)
		ammoDefs.put(5656, new AmmunitionDefinition(214, -1)); //Steel knife (p+)
		ammoDefs.put(5657, new AmmunitionDefinition(216, -1)); //Mithril knife (p+)
		ammoDefs.put(5658, new AmmunitionDefinition(215, -1)); //Black knife (p+)
		ammoDefs.put(5659, new AmmunitionDefinition(217, -1)); //Adamant knife (p+)
		ammoDefs.put(5661, new AmmunitionDefinition(212, -1)); //Bronze knife (p++)
		ammoDefs.put(5663, new AmmunitionDefinition(214, -1)); //Steel knife (p++)
		ammoDefs.put(5664, new AmmunitionDefinition(216, -1)); //Mithril knife (p++)
		ammoDefs.put(5665, new AmmunitionDefinition(215, -1)); //Black knife (p++)
		ammoDefs.put(5666, new AmmunitionDefinition(217, -1)); //Adamant knife (p++)
		ammoDefs.put(5667, new AmmunitionDefinition(218, -1)); //Rune knife (p++)
		ammoDefs.put(6061, new AmmunitionDefinition(27, -1)); //Bronze bolts (p+)
		ammoDefs.put(6062, new AmmunitionDefinition(27, -1)); //Bronze bolts (p++)
		ammoDefs.put(9286, new AmmunitionDefinition(27, -1)); //Blurite bolts (p)
		ammoDefs.put(9287, new AmmunitionDefinition(27, -1)); //Iron bolts (p)
		ammoDefs.put(9288, new AmmunitionDefinition(27, -1)); //Steel bolts (p)
		ammoDefs.put(9289, new AmmunitionDefinition(27, -1)); //Mithril bolts (p)
		ammoDefs.put(9290, new AmmunitionDefinition(27, -1)); //Adamant bolts (p)
		ammoDefs.put(9291, new AmmunitionDefinition(27, -1)); //Runite bolts (p)
		ammoDefs.put(9292, new AmmunitionDefinition(27, -1)); //Silver bolts (p)
		ammoDefs.put(9293, new AmmunitionDefinition(27, -1)); //Blurite bolts (p+)
		ammoDefs.put(9294, new AmmunitionDefinition(27, -1)); //Iron bolts (p+)
		ammoDefs.put(9295, new AmmunitionDefinition(27, -1)); //Steel bolts (p+)
		ammoDefs.put(9296, new AmmunitionDefinition(27, -1)); //Mithril bolts (p+)
		ammoDefs.put(9297, new AmmunitionDefinition(27, -1)); //Adamant bolts (p+)
		ammoDefs.put(9298, new AmmunitionDefinition(27, -1)); //Runite bolts (p+)
		ammoDefs.put(9299, new AmmunitionDefinition(27, -1)); //Silver bolts (p+)
		ammoDefs.put(9300, new AmmunitionDefinition(27, -1)); //Blurite bolts (p++)
		ammoDefs.put(9301, new AmmunitionDefinition(27, -1)); //Iron bolts (p++)
		ammoDefs.put(9302, new AmmunitionDefinition(27, -1)); //Steel bolts (p++)
		ammoDefs.put(9303, new AmmunitionDefinition(27, -1)); //Mithril bolts (p++)
		ammoDefs.put(9304, new AmmunitionDefinition(27, -1)); //Adamant bolts (p++)
		ammoDefs.put(9305, new AmmunitionDefinition(27, -1)); //Runite bolts (p++)
		ammoDefs.put(9306, new AmmunitionDefinition(27, -1)); //Silver bolts (p++)
		ammoDefs.put(11227, new AmmunitionDefinition(1120, 1116)); //Dragon arrow (p)
		ammoDefs.put(11228, new AmmunitionDefinition(1120, 1116)); //Dragon arrow (p+)
		ammoDefs.put(11229, new AmmunitionDefinition(1120, 1116)); //Dragon arrow (p++)
		ammoDefs.put(11231, new AmmunitionDefinition(1122, -1)); //Dragon dart (p)
		ammoDefs.put(13880, new AmmunitionDefinition(1837, -1));//Morrigan's javelin (p)
		ammoDefs.put(13881, new AmmunitionDefinition(1837, -1));//Morrigan's javelin (p+)
		ammoDefs.put(13882, new AmmunitionDefinition(1837, -1));//Morrigan's javelin (p++)
		ammoDefs.put(13954, new AmmunitionDefinition(1837, -1));//C. morrigan's javelin (p)
		ammoDefs.put(13955, new AmmunitionDefinition(1837, -1));//C. morrigan's javelin (p+)
		ammoDefs.put(13956, new AmmunitionDefinition(1837, -1));//C. morrigan's javelin (p++)
		ammoDefs.put(15968, new AmmunitionDefinition(2487, 2486)); //Sagittarian arrows (p) (b)
		ammoDefs.put(15969, new AmmunitionDefinition(2467, 2466)); //Novite arrows (p+) (b)
		ammoDefs.put(15970, new AmmunitionDefinition(2469, 2468)); //Bathus arrows (p+) (b)
		ammoDefs.put(15971, new AmmunitionDefinition(2471, 2470)); //Marmaros arrows (p+) (b)
		ammoDefs.put(15972, new AmmunitionDefinition(2473, 2472)); //Kratonite arrows (p+) (b)
		ammoDefs.put(15973, new AmmunitionDefinition(2475, 2474)); //Fractite arrows (p+) (b)
		ammoDefs.put(15974, new AmmunitionDefinition(2477, 2476)); //Zephyrium arrows (p+) (b)
		ammoDefs.put(15975, new AmmunitionDefinition(2479, 2478)); //Argonite arrows (p+) (b)
		ammoDefs.put(15976, new AmmunitionDefinition(2481, 2480)); //Katagon arrows (p+) (b)
		ammoDefs.put(15977, new AmmunitionDefinition(2483, 2482)); //Gorgonite arrows (p+) (b)
		ammoDefs.put(15978, new AmmunitionDefinition(2485, 2484)); //Promethium arrows (p+) (b)
		ammoDefs.put(15979, new AmmunitionDefinition(2487, 2486)); //Sagittarian arrows (p+) (b)
		ammoDefs.put(15980, new AmmunitionDefinition(2467, 2466)); //Novite arrows (p++) (b)
		ammoDefs.put(15981, new AmmunitionDefinition(2469, 2468)); //Bathus arrows (p++) (b)
		ammoDefs.put(15982, new AmmunitionDefinition(2471, 2470)); //Marmaros arrows (p++) (b)
		ammoDefs.put(15983, new AmmunitionDefinition(2473, 2472)); //Kratonite arrows (p++) (b)
		ammoDefs.put(15984, new AmmunitionDefinition(2475, 2474)); //Fractite arrows (p++) (b)
		ammoDefs.put(15985, new AmmunitionDefinition(2477, 2476)); //Zephyrium arrows (p++) (b)
		ammoDefs.put(15986, new AmmunitionDefinition(2479, 2478)); //Argonite arrows (p++) (b)
		ammoDefs.put(15987, new AmmunitionDefinition(2481, 2480)); //Katagon arrows (p++) (b)
		ammoDefs.put(15988, new AmmunitionDefinition(2483, 2482)); //Gorgonite arrows (p++) (b)
		ammoDefs.put(15989, new AmmunitionDefinition(2485, 2484)); //Promethium arrows (p++) (b)
		ammoDefs.put(15990, new AmmunitionDefinition(2487, 2486)); //Sagittarian arrows (p++) (b)
		ammoDefs.put(16482, new AmmunitionDefinition(2467, 2466)); //Novite arrows (p)
		ammoDefs.put(16487, new AmmunitionDefinition(2469, 2468)); //Bathus arrows (p)
		ammoDefs.put(16492, new AmmunitionDefinition(2471, 2470)); //Marmaros arrows (p)
		ammoDefs.put(16497, new AmmunitionDefinition(2473, 2472)); //Kratonite arrows (p)
		ammoDefs.put(16497, new AmmunitionDefinition(2473, -1)); //Kratonite arrows (p)
		ammoDefs.put(16502, new AmmunitionDefinition(2475, 2474)); //Fractite arrows (p)
		ammoDefs.put(16507, new AmmunitionDefinition(2477, 2476)); //Zephyrium arrows (p)
		ammoDefs.put(16642, new AmmunitionDefinition(2487, 2486)); //Sagittarian arrows (p++)
		ammoDefs.put(21712, new AmmunitionDefinition(13, -1)); //Dragonbane arrow (p)
		ammoDefs.put(21719, new AmmunitionDefinition(13, -1)); //Basiliskbane arrow (p)
		ammoDefs.put(21726, new AmmunitionDefinition(13, -1)); //Wallasalkibane arrow (p)
		ammoDefs.put(21733, new AmmunitionDefinition(13, -1)); //Abyssalbane arrow (p)
		
		
		ammoDefs.put(4212, new AmmunitionDefinition(249, 250));//ranged
		ammoDefs.put(4214, new AmmunitionDefinition(249, 250));//ranged
		ammoDefs.put(4215, new AmmunitionDefinition(249, 250));//ranged
		ammoDefs.put(4216, new AmmunitionDefinition(249, 250));//ranged
		ammoDefs.put(4217, new AmmunitionDefinition(249, 250));//ranged
		ammoDefs.put(4218, new AmmunitionDefinition(249, 250));//ranged
		ammoDefs.put(4219, new AmmunitionDefinition(249, 250));//ranged
		ammoDefs.put(4220, new AmmunitionDefinition(249, 250));//ranged
		ammoDefs.put(4221, new AmmunitionDefinition(249, 250));//ranged
		ammoDefs.put(4222, new AmmunitionDefinition(249, 250));//ranged
		ammoDefs.put(24456, new AmmunitionDefinition(249, 250));//ranged
		

		ammoDefs.put(4223, new AmmunitionDefinition(249, 250));//ranged
		
		ammoDefs.put(52550, new AmmunitionDefinition(249, 6611));//ranged
		ammoDefs.put(25544, new AmmunitionDefinition(249, 6611));//ranged
		
		ammoDefs.put(49484, new AmmunitionDefinition(202, -1)); //dragon javelin
		ammoDefs.put(49486, new AmmunitionDefinition(202, -1));
		ammoDefs.put(49488, new AmmunitionDefinition(202, -1));
		ammoDefs.put(49490, new AmmunitionDefinition(202, -1));

		ammoDefs.put(41959, new AmmunitionDefinition(6272, -1));//TODO
		ammoDefs.remove(23043);
		//dragon bolts
		ammoDefs.put(51905, new AmmunitionDefinition(27, -1));
		ammoDefs.put(51924, new AmmunitionDefinition(27, -1));
		ammoDefs.put(51926, new AmmunitionDefinition(27, -1));
		ammoDefs.put(51928, new AmmunitionDefinition(27, -1));
		ammoDefs.put(51932, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51934, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51936, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51938, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51940, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51942, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51944, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51946, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51948, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51950, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51955, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51957, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51959, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51961, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51963, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51965, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51967, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51969, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51971, new AmmunitionDefinition(27, -1));
	ammoDefs.put( 51973, new AmmunitionDefinition(27, -1));
		
		
		
	ammoDefs.put(50849, new AmmunitionDefinition(6319, 6320));
	
	ammoDefs.put(52804, new AmmunitionDefinition(5028, -1));
	ammoDefs.put(52806, new AmmunitionDefinition(5697, -1));
	ammoDefs.put(52808, new AmmunitionDefinition(5697, -1));
	ammoDefs.put(52810, new AmmunitionDefinition(5697, -1));
	
	ammoDefs.put(25575, ammoDefs.get(20171));
	ammoDefs.put(25592, ammoDefs.get(20171));
	ammoDefs.put(25609, ammoDefs.get(20171));
		
	}
	
	public static void loadDefinitions() {
		try {
			RandomAccessFile in = new RandomAccessFile(PATH, "r");
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
			while (buffer.hasRemaining()) {
				int id = buffer.getShort();
				short projectile = buffer.getShort();
				short pullGFX = buffer.getShort();
				ammoDefs.put(id, new AmmunitionDefinition(Math.max(0, projectile), Math.max(0, pullGFX)));
			}
			channel.close();
			in.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		loadNext();
	}

	public static AmmunitionDefinition getAmmoDefinition(int id) {
		AmmunitionDefinition def = ammoDefs.get(id);
		if (def == null)
			def = DEFAULT_AMMO_DEFINITION;
		return def;
	}

	public static class AmmunitionDefinition {
		private final int projectile;
		private final int pullGFX;

		public AmmunitionDefinition(int projectile, int pullGFX) {
			this.projectile = Math.max(0, projectile);
			this.pullGFX = Math.max(0, pullGFX);
		}

		public int getProjectile() {
			return projectile;
		}

		public int getPullGFX() {
			return pullGFX;
		}
	}
}
