package com.rs.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;

import com.rs.game.npc.others.zalcano.Zalcano;
import com.rs.game.player.content.Combat;

public class WeaponTypesLoader {
	
	private static final String PATH = "data/item/weapons.dat";
	private static WeaponType DEFAULT_WEAPON_DEFINITION = new WeaponType(0, 0);

	private final static HashMap<Integer, WeaponType> weaponDefs = new HashMap<Integer, WeaponType>();
	
	private static void loadNext() {
		weaponDefs.put(812, new WeaponType(1, 10)); //Bronze dart (p)
		weaponDefs.put(813, new WeaponType(1, 10)); //Iron dart (p)
		weaponDefs.put(832, new WeaponType(1, 10)); //Iron javelin (p)
		weaponDefs.put(833, new WeaponType(1, 10)); //Steel javelin (p)
		weaponDefs.put(834, new WeaponType(1, 10)); //Mithril javelin (p)
		weaponDefs.put(835, new WeaponType(1, 10)); //Adamant javelin (p)
		weaponDefs.put(836, new WeaponType(1, 10)); //Rune javelin (p)
		weaponDefs.put(870, new WeaponType(1, 10)); //Bronze knife (p)
		weaponDefs.put(872, new WeaponType(1, 10)); //Steel knife (p)
		weaponDefs.put(873, new WeaponType(1, 10)); //Mithril knife (p)
		weaponDefs.put(874, new WeaponType(1, 10)); //Black knife (p)
		weaponDefs.put(875, new WeaponType(1, 10)); //Adamant knife (p)
		weaponDefs.put(876, new WeaponType(1, 10)); //Rune knife (p)
		weaponDefs.put(1233, new WeaponType(0, 5)); //Black dagger (p)
		weaponDefs.put(1251, new WeaponType(0, 5)); //Bronze spear (p)
		weaponDefs.put(1253, new WeaponType(0, 5)); //Iron spear (p)
		weaponDefs.put(1255, new WeaponType(0, 5)); //Steel spear (p)
		weaponDefs.put(1257, new WeaponType(0, 5)); //Mithril spear (p)
		weaponDefs.put(1259, new WeaponType(0, 5)); //Adamant spear (p)
		weaponDefs.put(1261, new WeaponType(0, 5)); //Rune spear (p)
		weaponDefs.put(1263, new WeaponType(0, 5)); //Dragon spear (p)
		weaponDefs.put(3094, new WeaponType(1, 10)); //Black dart (p)
		weaponDefs.put(4582, new WeaponType(0, 5)); //Black spear (p)
		weaponDefs.put(5628, new WeaponType(1, 10)); //Bronze dart (p+)
		weaponDefs.put(5629, new WeaponType(1, 10)); //Iron dart (p+)
		weaponDefs.put(5630, new WeaponType(1, 10)); //Steel dart (p+)
		weaponDefs.put(5632, new WeaponType(1, 10)); //Mithril dart (p+)
		weaponDefs.put(5633, new WeaponType(1, 10)); //Adamant dart (p+)
		weaponDefs.put(5634, new WeaponType(1, 10)); //Rune dart (p+)
		weaponDefs.put(5635, new WeaponType(1, 10)); //Bronze dart (p++)
		weaponDefs.put(5636, new WeaponType(1, 10)); //Iron dart (p++)
		weaponDefs.put(5637, new WeaponType(1, 10)); //Steel dart (p++)
		weaponDefs.put(5639, new WeaponType(1, 10)); //Mithril dart (p++)
		weaponDefs.put(5640, new WeaponType(1, 10)); //Adamant dart (p++)
		weaponDefs.put(5641, new WeaponType(1, 10)); //Rune dart (p++)
		weaponDefs.put(5642, new WeaponType(1, 10)); //Bronze javelin (p+)
		weaponDefs.put(5643, new WeaponType(1, 10)); //Iron javelin (p+)
		weaponDefs.put(5644, new WeaponType(1, 10)); //Steel javelin (p+)
		weaponDefs.put(5645, new WeaponType(1, 10)); //Mithril javelin (p+)
		weaponDefs.put(5646, new WeaponType(1, 10)); //Adamant javelin (p+)
		weaponDefs.put(5647, new WeaponType(1, 10)); //Rune javelin (p+)
		weaponDefs.put(5649, new WeaponType(1, 10)); //Iron javelin (p++)
		weaponDefs.put(5650, new WeaponType(1, 10)); //Steel javelin (p++)
		weaponDefs.put(5651, new WeaponType(1, 10)); //Mithril javelin (p++)
		weaponDefs.put(5652, new WeaponType(1, 10)); //Adamant javelin (p++)
		weaponDefs.put(5653, new WeaponType(1, 10)); //Rune javelin (p++)
		weaponDefs.put(5654, new WeaponType(1, 10)); //Bronze knife (p+)
		weaponDefs.put(5655, new WeaponType(1, 10)); //Iron knife (p+)
		weaponDefs.put(5656, new WeaponType(1, 10)); //Steel knife (p+)
		weaponDefs.put(5657, new WeaponType(1, 10)); //Mithril knife (p+)
		weaponDefs.put(5658, new WeaponType(1, 10)); //Black knife (p+)
		weaponDefs.put(5659, new WeaponType(1, 10)); //Adamant knife (p+)
		weaponDefs.put(5660, new WeaponType(1, 10)); //Rune knife (p+)
		weaponDefs.put(5661, new WeaponType(1, 10)); //Bronze knife (p++)
		weaponDefs.put(5662, new WeaponType(1, 10)); //Iron knife (p++)
		weaponDefs.put(5663, new WeaponType(1, 10)); //Steel knife (p++)
		weaponDefs.put(5664, new WeaponType(1, 10)); //Mithril knife (p++)
		weaponDefs.put(5665, new WeaponType(1, 10)); //Black knife (p++)
		weaponDefs.put(5666, new WeaponType(1, 10)); //Adamant knife (p++)
		weaponDefs.put(5667, new WeaponType(1, 10)); //Rune knife (p++)
		weaponDefs.put(5668, new WeaponType(0, 5)); //Iron dagger (p+)
		weaponDefs.put(5670, new WeaponType(0, 5)); //Bronze dagger (p+)
		weaponDefs.put(5672, new WeaponType(0, 5)); //Steel dagger (p+)
		weaponDefs.put(5674, new WeaponType(0, 5)); //Mithril dagger (p+)
		weaponDefs.put(5676, new WeaponType(0, 5)); //Adamant dagger (p+)
		weaponDefs.put(5678, new WeaponType(0, 5)); //Rune dagger (p+)
		weaponDefs.put(5678, new WeaponType(0, 5)); //Rune dagger (p+)
		weaponDefs.put(5680, new WeaponType(0, 5)); //Dragon dagger (p+)
		weaponDefs.put(5682, new WeaponType(0, 5)); //Black dagger (p+)
		weaponDefs.put(5686, new WeaponType(0, 5)); //Iron dagger (p++)
		weaponDefs.put(5688, new WeaponType(0, 5)); //Bronze dagger (p++)
		weaponDefs.put(5690, new WeaponType(0, 5)); //Steel dagger (p++)
		weaponDefs.put(5692, new WeaponType(0, 5)); //Mithril dagger (p++)
		weaponDefs.put(5694, new WeaponType(0, 5)); //Adamant dagger (p++)
		weaponDefs.put(5696, new WeaponType(0, 5)); //Rune dagger (p++)
		weaponDefs.put(5696, new WeaponType(0, 5)); //Rune dagger (p++)
		weaponDefs.put(5698, new WeaponType(0, 5)); //Dragon dagger (p++)
		weaponDefs.put(5700, new WeaponType(0, 5)); //Black dagger (p++)
		weaponDefs.put(5704, new WeaponType(0, 5)); //Bronze spear (p+)
		weaponDefs.put(5706, new WeaponType(0, 5)); //Iron spear (p+)
		weaponDefs.put(5708, new WeaponType(0, 5)); //Steel spear (p+)
		weaponDefs.put(5710, new WeaponType(0, 5)); //Mithril spear (p+)
		weaponDefs.put(5712, new WeaponType(0, 5)); //Adamant spear (p+)
		weaponDefs.put(5714, new WeaponType(0, 5)); //Rune spear (p+)
		weaponDefs.put(5714, new WeaponType(0, 5)); //Rune spear (p+)
		weaponDefs.put(5716, new WeaponType(0, 5)); //Dragon spear (p+)
		weaponDefs.put(5716, new WeaponType(0, 5)); //Dragon spear (p+)
		weaponDefs.put(5718, new WeaponType(0, 5)); //Bronze spear (p++)
		weaponDefs.put(5720, new WeaponType(0, 5)); //Iron spear (p++)
		weaponDefs.put(5722, new WeaponType(0, 5)); //Steel spear (p++)
		weaponDefs.put(5724, new WeaponType(0, 5)); //Mithril spear (p++)
		weaponDefs.put(5726, new WeaponType(0, 5)); //Adamant spear (p++)
		weaponDefs.put(5728, new WeaponType(0, 5)); //Rune spear (p++)
		weaponDefs.put(5728, new WeaponType(0, 5)); //Rune spear (p++)
		weaponDefs.put(5730, new WeaponType(0, 5)); //Dragon spear (p++)
		weaponDefs.put(5730, new WeaponType(0, 5)); //Dragon spear (p++)
		weaponDefs.put(5734, new WeaponType(0, 5)); //Black spear (p+)
		weaponDefs.put(5736, new WeaponType(0, 5)); //Black spear (p++)
		weaponDefs.put(6593, new WeaponType(0, 5)); //White dagger (p)
		weaponDefs.put(6595, new WeaponType(0, 5)); //White dagger (p+)
		weaponDefs.put(6597, new WeaponType(0, 5)); //White dagger (p++)
		weaponDefs.put(8874, new WeaponType(0, 5)); //Bone dagger (p)
		weaponDefs.put(10582, new WeaponType(0, 5)); //Keris (p)
		weaponDefs.put(10583, new WeaponType(0, 5)); //Keris (p+)
		weaponDefs.put(10584, new WeaponType(0, 5)); //Keris (p++)
		weaponDefs.put(11231, new WeaponType(1, 10)); //Dragon dart (p)
		weaponDefs.put(1231, new WeaponType(0, 5));//Dragon dagger (p)
		weaponDefs.put(13880, new WeaponType(1, 10));//Morrigan's javelin (p)
		weaponDefs.put(13881, new WeaponType(1, 10));//Morrigan's javelin (p+)
		weaponDefs.put(13882, new WeaponType(1, 10));//Morrigan's javelin (p++)
		weaponDefs.put(13954, new WeaponType(1, 10));//C. morrigan's javelin (p)
		weaponDefs.put(13955, new WeaponType(1, 10));//C. morrigan's javelin (p+)
		weaponDefs.put(13956, new WeaponType(1, 10));//C. morrigan's javelin (p++)
		weaponDefs.put(13466, new WeaponType(0, 5)); //Dragon dagger (p)
		weaponDefs.put(13467, new WeaponType(0, 5)); //Dragon dagger (p+)
		weaponDefs.put(13468, new WeaponType(0, 5)); //Dragon dagger (p++)
		weaponDefs.put(13766, new WeaponType(0, 5)); //Rune dagger (p)
		weaponDefs.put(13767, new WeaponType(0, 5)); //Rune dagger (p+)
		weaponDefs.put(13768, new WeaponType(0, 5)); //Rune dagger (p++)
		weaponDefs.put(13771, new WeaponType(0, 5)); //Rune spear (p)
		weaponDefs.put(13772, new WeaponType(0, 5)); //Dragon spear (p)
		weaponDefs.put(13773, new WeaponType(0, 5)); //Rune spear (p+)
		weaponDefs.put(13774, new WeaponType(0, 5)); //Dragon spear (p+)
		weaponDefs.put(13775, new WeaponType(0, 5)); //Rune spear (p++)
		weaponDefs.put(13776, new WeaponType(0, 5)); //Dragon spear (p++)
		weaponDefs.put(15849, new WeaponType(0, 5)); //Novite dagger (p) (b)
		weaponDefs.put(15853, new WeaponType(0, 5)); //Bathus dagger (p) (b)
		weaponDefs.put(15889, new WeaponType(0, 5)); //Primal dagger (p) (b)
		weaponDefs.put(16219, new WeaponType(0, 5)); //Novite spear (p) (b)
		weaponDefs.put(16220, new WeaponType(0, 5)); //Novite spear (p+) (b)
		weaponDefs.put(16221, new WeaponType(0, 5)); //Novite spear (p++) (b)
		weaponDefs.put(16223, new WeaponType(0, 5)); //Bathus spear (p) (b)
		weaponDefs.put(16224, new WeaponType(0, 5)); //Bathus spear (p+) (b)
		weaponDefs.put(16225, new WeaponType(0, 5)); //Bathus spear (p++) (b)
		weaponDefs.put(16228, new WeaponType(0, 5)); //Marmaros spear (p+) (b)
		weaponDefs.put(16229, new WeaponType(0, 5)); //Marmaros spear (p++) (b)
		weaponDefs.put(16236, new WeaponType(0, 5)); //Fractite spear (p+) (b)
		weaponDefs.put(16237, new WeaponType(0, 5)); //Fractite spear (p++) (b)
		weaponDefs.put(16240, new WeaponType(0, 5)); //Zephyrium spear (p+) (b)
		weaponDefs.put(16241, new WeaponType(0, 5)); //Zephyrium spear (p++) (b)
		weaponDefs.put(16243, new WeaponType(0, 5)); //Argonite spear (p) (b)
		weaponDefs.put(16244, new WeaponType(0, 5)); //Argonite spear (p+) (b)
		weaponDefs.put(16245, new WeaponType(0, 5)); //Argonite spear (p++) (b)
		weaponDefs.put(16247, new WeaponType(0, 5)); //Katagon spear (p) (b)
		weaponDefs.put(16251, new WeaponType(0, 5)); //Gorgonite spear (p) (b)
		weaponDefs.put(16252, new WeaponType(0, 5)); //Gorgonite spear (p+) (b)
		weaponDefs.put(16253, new WeaponType(0, 5)); //Gorgonite spear (p++) (b)
		weaponDefs.put(16255, new WeaponType(0, 5)); //Promethium spear (p) (b)
		weaponDefs.put(16259, new WeaponType(0, 5)); //Primal spear (p) (b)
		weaponDefs.put(16759, new WeaponType(0, 5)); //Novite dagger (p)
		weaponDefs.put(16761, new WeaponType(0, 5)); //Novite dagger (p+)
		weaponDefs.put(16763, new WeaponType(0, 5)); //Novite dagger (p++)
		weaponDefs.put(16767, new WeaponType(0, 5)); //Bathus dagger (p)
		weaponDefs.put(16801, new WeaponType(0, 5)); //Zephyrium dagger (p+)
		weaponDefs.put(16803, new WeaponType(0, 5)); //Zephyrium dagger (p++)
		weaponDefs.put(16807, new WeaponType(0, 5)); //Argonite dagger (p)
		weaponDefs.put(16815, new WeaponType(0, 5)); //Katagon dagger (p)
		weaponDefs.put(16823, new WeaponType(0, 5)); //Gorgonite dagger (p)
		weaponDefs.put(16831, new WeaponType(0, 5)); //Promethium dagger (p)
		weaponDefs.put(16833, new WeaponType(0, 5)); //Promethium dagger (p+)
		weaponDefs.put(16835, new WeaponType(0, 5)); //Promethium dagger (p++)
		weaponDefs.put(17073, new WeaponType(0, 5)); //Bathus spear (p)
		weaponDefs.put(17075, new WeaponType(0, 5)); //Bathus spear (p+)
		weaponDefs.put(17077, new WeaponType(0, 5)); //Bathus spear (p++)
		weaponDefs.put(17089, new WeaponType(0, 5)); //Kratonite spear (p)
		weaponDefs.put(17091, new WeaponType(0, 5)); //Kratonite spear (p+)
		weaponDefs.put(17093, new WeaponType(0, 5)); //Kratonite spear (p++)
		weaponDefs.put(17105, new WeaponType(0, 5)); //Zephyrium spear (p)
		weaponDefs.put(17107, new WeaponType(0, 5)); //Zephyrium spear (p+)
		weaponDefs.put(17109, new WeaponType(0, 5)); //Zephyrium spear (p++)
		weaponDefs.put(17137, new WeaponType(0, 5)); //Promethium spear (p)
		weaponDefs.put(17139, new WeaponType(0, 5)); //Promethium spear (p+)
		weaponDefs.put(17141, new WeaponType(0, 5)); //Promethium spear (p++)
		weaponDefs.put(25110, new WeaponType(0, 5)); //Royal court lance (spear)
		weaponDefs.put(25112, new WeaponType(0, 5)); //Royal court lance (rapier)	
		
		

		weaponDefs.put(4214, new WeaponType(1, Combat.ARROW_STYLE));//ranged
		weaponDefs.put(4212, new WeaponType(1, Combat.ARROW_STYLE));//ranged
		weaponDefs.put(4215, new WeaponType(1, Combat.ARROW_STYLE));//ranged
		weaponDefs.put(4216, new WeaponType(1, Combat.ARROW_STYLE));//ranged
		weaponDefs.put(4217, new WeaponType(1, Combat.ARROW_STYLE));//ranged
		weaponDefs.put(4218, new WeaponType(1, Combat.ARROW_STYLE));//ranged
		weaponDefs.put(4219, new WeaponType(1, Combat.ARROW_STYLE));//ranged
		weaponDefs.put(4220, new WeaponType(1, Combat.ARROW_STYLE));//ranged
		weaponDefs.put(4221, new WeaponType(1, Combat.ARROW_STYLE));//ranged
		weaponDefs.put(4222, new WeaponType(1, Combat.ARROW_STYLE));//ranged
		weaponDefs.put(4223, new WeaponType(1, Combat.ARROW_STYLE));//ranged
		weaponDefs.put(25380, new WeaponType(1, Combat.ARROW_STYLE));//lucky dbow
		weaponDefs.put(25539, new WeaponType(1, Combat.ARROW_STYLE));//hallo dbow
		weaponDefs.put(25617, new WeaponType(1, Combat.ARROW_STYLE));//hallo(u) dbow
		weaponDefs.put(41959, new WeaponType(1, Combat.THROWN_STYLE));//black chincompa
		weaponDefs.put(25398, new WeaponType(1, Combat.BOLT_STYLE));//ranged
		weaponDefs.put(42926, new WeaponType(1, Combat.THROWN_STYLE));//blowpipe
		weaponDefs.put(25502, new WeaponType(1, Combat.THROWN_STYLE));//blowpipe
		weaponDefs.put(23043, new WeaponType(1, Combat.ARROW_STYLE));//quickbow
		weaponDefs.put(50849, new WeaponType(1, Combat.THROWN_STYLE));//dragon thrownaxe
		weaponDefs.put(41905, new WeaponType(Combat.MAGIC_TYPE, -1));
		weaponDefs.put(41907, new WeaponType(Combat.MAGIC_TYPE, -1));
		weaponDefs.put(42899, new WeaponType(Combat.MAGIC_TYPE, -1));
		weaponDefs.put(52516, new WeaponType(Combat.MAGIC_TYPE, -1));
		weaponDefs.put(25583, new WeaponType(Combat.MAGIC_TYPE, -1));
		weaponDefs.put(25620, new WeaponType(Combat.MAGIC_TYPE, -1));

		weaponDefs.put(25699, new WeaponType(Combat.MAGIC_TYPE, -1));
		weaponDefs.put(52323, new WeaponType(Combat.MAGIC_TYPE, -1));
		weaponDefs.put(25496, new WeaponType(Combat.MAGIC_TYPE, -1));
		weaponDefs.put(51006, new WeaponType(Combat.MAGIC_TYPE, -1));
		weaponDefs.put(49478, new WeaponType(1, Combat.BOLT_STYLE));//ranged
		weaponDefs.put(49481, new WeaponType(1, Combat.BOLT_STYLE));//ranged
		
		weaponDefs.put(25441, new WeaponType(1, Combat.ARROW_STYLE));//twisted bow
		weaponDefs.put(50997, new WeaponType(1, Combat.ARROW_STYLE));//twisted bow
		weaponDefs.put(25460, new WeaponType(1, Combat.ARROW_STYLE));//twisted bow
		weaponDefs.put(25469, new WeaponType(1, Combat.ARROW_STYLE));//twisted bow
		weaponDefs.put(25533, new WeaponType(1, Combat.ARROW_STYLE));//twisted bow
		weaponDefs.put(25662, new WeaponType(1, Combat.ARROW_STYLE));//twisted bow
		weaponDefs.put(25575, new WeaponType(1, Combat.ARROW_STYLE));//twisted bow
		weaponDefs.put(25592, new WeaponType(1, Combat.ARROW_STYLE));//twisted bow
		weaponDefs.put(25609, new WeaponType(1, Combat.ARROW_STYLE));//twisted bow
		
		weaponDefs.put(42424, new WeaponType(1, Combat.ARROW_STYLE));//3rd age bow
		
		weaponDefs.put(52550, new WeaponType(1, Combat.ARROW_STYLE));//craw bow
		weaponDefs.put(25544, new WeaponType(1, Combat.ARROW_STYLE));//craw bow
		
		weaponDefs.put(51902, new WeaponType(1, Combat.BOLT_STYLE));//dragon crossbow
		weaponDefs.put(51012, new WeaponType(1, Combat.BOLT_STYLE));//dragon hunter crossbow
		weaponDefs.put(25546, new WeaponType(1, Combat.BOLT_STYLE));//xmas crossbow
		weaponDefs.put(25639, new WeaponType(1, Combat.BOLT_STYLE));//infernal xmas crossbow
		weaponDefs.put(25629, new WeaponType(1, Combat.BOLT_STYLE));//infernal xmas crossbow

		weaponDefs.put(42788, weaponDefs.get(861)); //magic shortbow
		
		weaponDefs.put(52804, new WeaponType(1, Combat.THROWN_STYLE)); //dragon knife
		weaponDefs.put(52806, new WeaponType(1, Combat.THROWN_STYLE)); //dragon knife
		weaponDefs.put(52808, new WeaponType(1, Combat.THROWN_STYLE)); //dragon knife
		weaponDefs.put(52810, new WeaponType(1, Combat.THROWN_STYLE)); //dragon knife

		weaponDefs.put(Zalcano.IMBUED_TEPHRA, new WeaponType(1, Combat.THROWN_STYLE));
		
		weaponDefs.put(25584, weaponDefs.get(15241));
	}
	
	
	
	
	public static void loadDefinitions() {
		try {
			RandomAccessFile in = new RandomAccessFile(PATH, "r");
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
			while (buffer.hasRemaining()) {
				int id = buffer.getShort() & 0xffff;
				short type = buffer.getShort();
				short style = buffer.getShort();
				weaponDefs.put(id, new WeaponType(Math.max(0, type), Math.max(0, style)));
			}
			channel.close();
			in.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		loadNext();
	}
	
	public static WeaponType getWeaponDefinition(int id) {
		WeaponType def = weaponDefs.get(id);
		if (def == null)
			def = DEFAULT_WEAPON_DEFINITION;
		return def;
	}
	
	public static class WeaponType {
		private final int type;
		private final int style;
		
		public WeaponType(int type, int style) {
			this.type = type;
			this.style = style;
		}

		public int getType() {
			return type;
		}

		public int getStyle() {
			return style;
		}
	}
}
