package com.rs.game.player.content;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.item.Item;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.raids.cox.ChambersHerblore;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.controllers.CrucibleControler;
import com.rs.game.player.controllers.TheHorde;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.Arrays;

public final class Drinkables {

	public static final int VIAL = 229, DUNGEONEERING_VIAL = 17490, FLASK = 23191;

	public static enum Drink {

		ATTACK_POTION(new int[]
		{ 2428, 121, 123, 125 }, Effects.ATTACK_POTION),

		STRENGTH_POTION(new int[]
		{ 113, 115, 117, 119 }, Effects.STRENGTH_POTION),

		DEFENCE_POTION(new int[]
		{ 2432, 133, 135, 137 }, Effects.DEFENCE_POTION),

		RANGING_POTION(new int[]
		{ 2444, 169, 171, 173 }, Effects.RANGE_POTION),

		MAGIC_POTION(new int[]
		{ 3040, 3042, 3044, 3046 }, Effects.MAGIC_POTION),

		MAGIC_FLASK(new int[]
		{ 23423, 23425, 23427, 23429, 23431, 23433 }, Effects.MAGIC_POTION),

		ANTIPOISON_POTION(new int[]
		{ 2446, 175, 177, 179 }, Effects.ANTIPOISON),

		SUPER_ANTIPOISON(new int[]
		{ 2448, 181, 183, 185 }, Effects.SUPER_ANTIPOISON),

		ANTIPOISION_PLUS(new int[]
		{ 5943, 5945, 5947, 5949 }, Effects.ANTIPOISON_PLUS),

		ANTIPOISON_DOUBLE_PLUS(new int[]
		{ 5952, 5954, 5956, 5958 }, Effects.ANTIPOISON_DOUBLE_PLUS),

		ANTIPOISON_PLUS_FLASK(new int[]
		{ 23579, 23581, 23583, 23585, 23587, 23589 }, Effects.ANTIPOISON_PLUS),

		ANTIPOISON_DOUBLE_PLUS_Flask(new int[]
		{ 23591, 23593, 23595, 23597, 23599, 23601 }, Effects.ANTIPOISON_DOUBLE_PLUS),

		ZAMORAK_BREW(new int[]
		{ 2450, 189, 191, 193 }, Effects.ZAMORAK_BREW),

		ZAMORAK_BREW_FLASK(new int[]
		{ 23339, 23341, 23343, 23345, 23347, 23349 }, Effects.ZAMORAK_BREW),

		PRAYER_POTION(new int[]
		{ 2434, 139, 141, 143 }, Effects.PRAYER_POTION),

		SUPER_COMBAT_POTION(new int[]
				{ 42695, 42697, 42699, 42701 }, Effects.SUPER_COMBAT_POTION),
		
		SUPER_ATTACK_POTION(new int[]
		{ 2436, 145, 147, 149 }, Effects.SUPER_ATT_POTION),

		SUPER_STRENGTH_POTION(new int[]
		{ 2440, 157, 159, 161 }, Effects.SUPER_STR_POTION),

		SUPER_DEFENCE_POTION(new int[]
		{ 2442, 163, 165, 167 }, Effects.SUPER_DEF_POTION),

		ENERGY_POTION(new int[]
		{ 3008, 3010, 3012, 3014 }, Effects.ENERGY_POTION),

		SUPER_ENERGY_POTION(new int[]
		{ 3016, 3018, 3020, 3022 }, Effects.SUPER_ENERGY),

		EXTREME_ATTACK_POTION(new int[]
		{ 15308, 15309, 15310, 15311 }, Effects.EXTREME_ATT_POTION),

		EXTREME_STRENGTH_POTION(new int[]
		{ 15312, 15313, 15314, 15315 }, Effects.EXTREME_STR_POTION),

		EXTREME_DEFENCE_POTION(new int[]
		{ 15316, 15317, 15318, 15319 }, Effects.EXTREME_DEF_POTION),

		EXTREME_MAGIC_POTION(new int[]
		{ 15320, 15321, 15322, 15323 }, Effects.EXTREME_MAG_POTION),

		KODAI_MINUS_POTION(new int[]
				{ 50940, 50939, 50938, 50937 }, Effects.REGULAR_MAGIC_POTION),
		KODAI_POTION(new int[]
				{ 50944, 50943, 50942, 50941 }, Effects.STRONG_MAGIC_POTION),
		KODAI_PLUS_POTION(new int[]
				{ 50948, 50947, 50946, 50945 }, Effects.EXTREME_MAG_POTION),

		TWISTED_MINUS_POTION(new int[]
				{ 50928, 50927, 50926, 50925 }, Effects.REGULAR_RANGE_POTION),
		TWISTED_POTION(new int[]
				{ 50932, 50931, 50930, 50929 }, Effects.STRONG_RANGE_POTION),
		TWISTED_PLUS_POTION(new int[]
				{ 50936, 50935, 50934, 50933 }, Effects.EXTREME_RAN_POTION),

		ELDER_MINUS_POTION(new int[]
				{ 50916, 50915, 50914, 50913 }, Effects.COMBAT_POTION),
		ELDER_POTION(new int[]
				{ 50920, 50919, 50918, 50917 }, Effects.SUPER_COMBAT_POTION),
		ELDER_PLUS_POTION(new int[]
				{ 50924, 50923, 50922, 50921 }, Effects.ELDER_PLUS),

		EXTREME_RANGING_POTION(new int[]
		{ 15324, 15325, 15326, 15327 }, Effects.EXTREME_RAN_POTION),

		RESTORE_POTION(new int[]
		{ 2430, 127, 129, 131 }, Effects.RESTORE_POTION),

		SUPER_RESTORE_POTION(new int[]
		{ 3024, 3026, 3028, 3030 }, Effects.SUPER_RESTORE),

		SARADOMIN_BREW_POTION(new int[]
		{ 6685, 6687, 6689, 6691 }, Effects.SARADOMIN_BREW),

		XERICS_AID_MINUS_POTION(new int[]
				{ 50976, 50975, 50974, 50973 }, Effects.XERICS_AID_MINUS),
		XERICS_AID_POTION(new int[]
				{ 50980, 50979, 50978, 50977 }, Effects.XERICS_AID),
		XERICS_AID_PLUS_POTION(new int[]
				{ 50984, 50983, 50982, 50981 }, Effects.XERICS_AID_PLUS),

		RECOVER_SPECIAL_POTION(new int[]
		{ 15300, 15301, 15302, 15303 }, Effects.RECOVER_SPECIAL),

		SUPER_PRAYER_POTION(new int[]
		{ 15328, 15329, 15330, 15331 }, Effects.SUPER_PRAYER),

		REVITALISATION_MINUS_POTION(new int[]
				{ 50952, 50951, 50950, 50949 }, Effects.REVITALISATION_MINUS),
		REVITALISATION_POTION(new int[]
				{ 50956, 50955, 50954, 50953 }, Effects.REVITALISATION),
		REVITALISATION_PLUS_POTION(new int[]
				{ 50960, 50959, 50958, 50957 }, Effects.REVITALISATION_PLUS),

		OVERLOAD_POTION(new int[]
				{ 15332, 15333, 15334, 15335 }, Effects.OVERLOAD),

		OVERLOAD_RAID_PLUS_POTION(new int[]
				{ 50996, 50995, 50994, 50993 }, Effects.OVERLOAD),
		OVERLOAD_RAID_POTION(new int[]
			{ 50992, 50991, 50990, 50989 }, Effects.OVERLOAD),
		OVERLOAD_RAID_MINUS_POTION(new int[]
				{ 50988, 50987, 50986, 50985 }, Effects.OVERLOAD),
		
		OVERLOAD_POTION_SPECIAL(new int[]
		{ 25430, 25430, 25430, 25430 }, Effects.OVERLOAD),

		ANTIFIRE_POTION(new int[]
		{ 2452, 2454, 2456, 2458 }, Effects.ANTI_FIRE),

		EXTENDED_ANTIFIRE_POTION(new int[]
		{ 41951, 41953, 41955, 41957 }, Effects.EXTENDED_ANTI_FIRE),

		SUPER_ANTIFIRE_POTION(new int[]
		{ 15304, 15305, 15306, 15307 }, Effects.SUPER_ANTI_FIRE),

		SUMMONING_POTION(new int[]
		{ 12140, 12142, 12144, 12146 }, Effects.SUMMONING_POT),

		SUMMONING_FLASK(new int[]
		{ 23621, 23623, 23625, 23627, 23629, 23631 }, Effects.SUMMONING_POT),

		SANFEW_SERUM_POTION(new int[]
		{ 10925, 10927, 10929, 10931 }, Effects.SANFEW_SERUM),

		PRAYER_RENEWAL_POTION(new int[]
		{ 21630, 21632, 21634, 21636 }, Effects.PRAYER_RENEWAL),

		PRAYER_ENHANCE_POTION_PLUS_RAID(new int[]
				{ 50963, 50962, 50961, 50960 }, Effects.PRAYER_ENHANCE_PLUS),

		PRAYER_ENHANCE_POTION_RAID(new int[]
				{ 50968, 50967, 50966, 50965 }, Effects.PRAYER_ENHANCE),

		PRAYER_ENHANCE_POTION_MINUS_RAID(new int[]
				{ 50972, 50971, 50970, 50969 }, Effects.PRAYER_ENHANCE_MINUS),

		PRAYER_RENEWAL_FLASK(new int[]
		{ 23609, 23611, 23613, 23615, 23617, 23619 }, Effects.PRAYER_RENEWAL),

		ATTACK_FLASK(new int[]
		{ 23195, 23197, 23199, 23201, 23203, 23205 }, Effects.ATTACK_POTION),

		STRENGTH_FLASK(new int[]
		{ 23207, 23209, 23211, 23213, 23215, 23217 }, Effects.STRENGTH_POTION),

		RESTORE_FLASK(new int[]
		{ 23219, 23221, 23223, 23225, 23227, 23229 }, Effects.RESTORE_POTION),

		DEFENCE_FLASK(new int[]
		{ 23231, 23233, 23235, 23237, 23239, 23241 }, Effects.DEFENCE_POTION),

		PRAYER_FLASK(new int[]
		{ 23243, 23245, 23247, 23249, 23251, 23253 }, Effects.PRAYER_POTION),

		SUPER_ATTACK_FLASK(new int[]
		{ 23255, 23257, 23259, 23261, 23263, 23265 }, Effects.SUPER_ATT_POTION),

		FISHING_FLASK(new int[]
		{ 23267, 23269, 23271, 23273, 23275, 23277 }, Effects.FISHING_POTION),

		FISHING_POTION(new int[]
		{ 2438, 151, 153, 155 }, Effects.FISHING_POTION),

		AGILITY_POTION(new int[]
		{ 3032, 3034, 3036, 3038 }, Effects.AGILITY_POTION),

		AGILITY_FLASK(new int[]
		{ 23411, 23413, 23415, 23417, 23419, 23421 }, Effects.AGILITY_POTION),

		COMBAT_POTION(new int[]
		{ 9740, 9742, 9744, 9746 }, Effects.COMBAT_POTION),

		COMBAT_FLASK(new int[]
		{ 23447, 23449, 23451, 23453, 23455, 23457 }, Effects.COMBAT_POTION),

		CRAFTING_POTION(new int[]
		{ 14838, 14840, 14842, 14844 }, Effects.CRAFTING_POTION),

		CRAFTING_FLASK(new int[]
		{ 23459, 23461, 23463, 23465, 23467, 23469 }, Effects.CRAFTING_POTION),

		HUNTER_POTION(new int[]
		{ 9998, 10000, 10002, 10004 }, Effects.HUNTER_POTION),

		HUNTER_FLASK(new int[]
		{ 23435, 23437, 23439, 23441, 23443, 23435 }, Effects.HUNTER_POTION),

		FLETCHING_POTION(new int[]
		{ 14846, 14848, 14850, 14852 }, Effects.FLETCHING_POTION),

		FLETCHING_FLASK(new int[]
		{ 23471, 23473, 23475, 23477, 23479, 23481 }, Effects.FLETCHING_POTION),

		SUPER_STRENGTH_FLASK(new int[]
		{ 23279, 23281, 23283, 23285, 23287, 23289 }, Effects.SUPER_STR_POTION),

		SUPER_DEFENCE_FLASK(new int[]
		{ 23291, 23293, 23295, 23297, 23299, 23301 }, Effects.SUPER_DEF_POTION),

		RANGING_FLASK(new int[]
		{ 23303, 23305, 23307, 23309, 23311, 23313 }, Effects.RANGE_POTION),

		ANTIPOISON_FLASK(new int[]
		{ 23315, 23317, 23319, 23321, 23323, 23325 }, Effects.ANTIPOISON),

		SUPER_ANTIPOISON_FLASK(new int[]
		{ 23327, 23329, 23331, 23333, 23335, 23337 }, Effects.SUPER_ANTIPOISON),

		SARADOMIN_BREW_FLASK(new int[]
		{ 23351, 23353, 23355, 23357, 23359, 23361 }, Effects.SARADOMIN_BREW),

		ANTIFIRE_FLASK(new int[]
		{ 23363, 23365, 23367, 23369, 23371, 23373 }, Effects.ANTI_FIRE),

		SUPER_ANTIFIRE_FLASK(new int[]
		{ 23489, 23490, 23491, 23492, 23493, 23494 }, Effects.SUPER_ANTI_FIRE),

		ENERGY_FLASK(new int[]
		{ 23375, 23377, 23379, 23381, 23383, 23385 }, Effects.ENERGY_POTION),

		SUPER_ENERGY_FLASK(new int[]
		{ 23387, 23389, 23391, 23393, 23395, 23397 }, Effects.SUPER_ENERGY),

		SUPER_RESTORE_FLASK(new int[]
		{ 23399, 23401, 23403, 23405, 23407, 23409 }, Effects.SUPER_RESTORE),

		RECOVER_SPECIAL_FLASK(new int[]
		{ 23483, 23484, 23485, 23486, 23487, 23488 }, Effects.RECOVER_SPECIAL),

		EXTREME_ATTACK_FLASK(new int[]
		{ 23495, 23496, 23497, 23498, 23499, 23500 }, Effects.EXTREME_ATT_POTION),

		EXTREME_STRENGTH_FLASK(new int[]
		{ 23501, 23502, 23503, 23504, 23505, 23506 }, Effects.EXTREME_STR_POTION),

		EXTREME_DEFENCE_FLASK(new int[]
		{ 23507, 23508, 23509, 23510, 23511, 23512 }, Effects.EXTREME_DEF_POTION),

		EXTREME_MAGIC_FLASK(new int[]
		{ 23513, 23514, 23515, 23516, 23517, 23518 }, Effects.EXTREME_MAG_POTION),

		EXTREME_RANGING_FLASK(new int[]
		{ 23519, 23520, 23521, 23522, 23523, 23524 }, Effects.EXTREME_RAN_POTION),

		SUPER_PRAYER_FLASK(new int[]
		{ 23525, 23526, 23527, 23528, 23529, 23530 }, Effects.SUPER_PRAYER),

		OVERLOAD_FLASK(new int[]
		{ 23531, 23532, 23533, 23534, 23535, 23536 }, Effects.OVERLOAD),

		JUG(new int[]
		{ 1993, 1935 }, Effects.WINE),

		SANFEW_SERUM_FLASK(new int[]
		{ 23567, 23569, 23571, 23573, 23575, 23577 }, Effects.SANFEW_SERUM),

		BEER(new int[]
		{ 1917, 1919 }, Effects.BEER),

		TANKARD(new int[]
		{ 3803 }, Effects.TANKARD),

		GREENMANS_ALE(new int[]
		{ 1909 }, Effects.GREENMANS_ALE),

		GREENMANS_ALE_KEG(new int[]
		{ 5793, 5791, 5789, 5787 }, Effects.GREENMANS_ALE),

		AXEMANS_ALE(new int[]
		{ 5751 }, Effects.AXEMANS),

		AXEMANS_ALE_KEG(new int[]
		{ 5825, 5823, 5821, 5819 }, Effects.AXEMANS),

		SLAYER_RESPITE(new int[]
		{ 5759 }, Effects.SLAYER_RESPITE),

		SLAYER_RESPITE_KEG(new int[]
		{ 5841, 5839, 5837, 5835 }, Effects.SLAYER_RESPITE),

		RANGERS_AID(new int[]
		{ 15119 }, Effects.RANGERS_AID),

		MOONLIGHT_MEAD(new int[]
		{ 2955 }, Effects.MOONLIGHT_MEAD),

		MOONLIGHT_MEAD_KEG(new int[]
		{ 5817, 5815, 5813, 5811 }, Effects.MOONLIGHT_MEAD),

		DRAGON_BITTER(new int[]
		{ 1911 }, Effects.DRAGON_BITTER),

		DRAGON_BITTER_KEG(new int[]
		{ 5809, 5807, 5805, 5803 }, Effects.DRAGON_BITTER),

		ASGARNIAN_ALE(new int[]
		{ 1905 }, Effects.ASGARNIAN_ALE),

		ASGARNIAN_ALE_KEG(new int[]
		{ 5785, 5783, 5781, 5779 }, Effects.ASGARNIAN_ALE),

		CHEF_DELIGHT(new int[]
		{ 5755 }, Effects.CHEF_DELIGHT),

		CHEF_DELIGHT_KEG(new int[]
		{ 5833, 5831, 5829, 5827 }, Effects.CHEF_DELIGHT),

		CIDER(new int[]
		{ 5763 }, Effects.CIDER),

		CIDER_KEG(new int[]
		{ 5849, 5847, 5845, 5843 }, Effects.CIDER),

		WIZARD_MIND_BOMB(new int[]
		{ 1907 }, Effects.WIZARD_MIND_BOMB),

		DWARVEN_STOUT(new int[]
		{ 1913 }, Effects.DWARVEN_STOUT),

		DWARVEN_STOUT_KEG(new int[]
		{ 5777, 5775, 5773, 5771 }, Effects.DWARVEN_STOUT),

		GROG(new int[]
		{ 1915 }, Effects.GROG),

		KEG_OF_BEER(new int[]
		{ 3801 }, Effects.KEG_OF_BEER),

		BANDIT_BREW(new int[]
		{ 4627 }, Effects.BANDIT_BREW),

		WEAK_MELEE_POTION(new int[]
		{ 17560 }, Effects.WEAK_MELEE_POTION),

		WEAK_MAGIC_POTION(new int[]
		{ 17556 }, Effects.WEAK_MAGIC_POTION),

		WEAK_RANGE_POTION(new int[]
		{ 17558 }, Effects.WEAK_RANGE_POTION),

		WEAK_DEFENCE_POTION(new int[]
		{ 17562 }, Effects.WEAK_DEFENCE_POTION),

		WEAK_STAT_RESTORE(new int[]
		{ 17564 }, Effects.WEAK_STAT_RESTORE_POTION),
		
		WEAK_ANTIPOISON_POTION(new int[]
				{ 17566 }, Effects.ANTIPOISON),

		WEAK_CURE_POTION(new int[]
		{ 17568 }, Effects.WEAK_STAT_CURE_POTION),

		WEAK_REJUVINATION_POTION(new int[]
		{ 17570 }, Effects.WEAK_REJUVINATION_POTION),

		WEAK_GATHERS_POTION(new int[]
		{ 17574 }, Effects.WEAK_GATHERER_POTION),

		WEAK_ARTISTAN_POTION(new int[]
		{ 17576 }, Effects.WEAK_ARTISTIANS_POTION),

		WEAK_NATURALIST_POTION(new int[]
		{ 17578 }, Effects.WEAK_NATURALISTS_POTION),

		WEAK_SURVIVALIST_POTION(new int[]
		{ 17580 }, Effects.WEAK_SURVIVALISTS_POTION),

		REGULAR_MELEE_POTION(new int[]
		{ 17586 }, Effects.REGULAR_MELEE_POTION),

		REGULAR_MAGIC_POTION(new int[]
		{ 17582 }, Effects.REGULAR_MAGIC_POTION),

		REGULAR_RANGE_POTION(new int[]
		{ 17584 }, Effects.REGULAR_RANGE_POTION),

		REGULAR_DEFENCE_POTION(new int[]
		{ 17588 }, Effects.REGULAR_DEFENCE_POTION),

		REGULAR_STAT_RESTORE(new int[]
		{ 17590 }, Effects.REGULAR_STAT_RESTORE_POTION),

		REGULAR_CURE_POTION(new int[]
		{ 17592 }, Effects.REGULAR_STAT_CURE_POTION),

		REGULAR_REJUVINATION_POTION(new int[]
		{ 17594 }, Effects.REGULAR_REJUVINATION_POTION),

		REGULAR_GATHERS_POTION(new int[]
		{ 17598 }, Effects.REGULAR_GATHERER_POTION),

		REGULAR_ARTISTAN_POTION(new int[]
		{ 17600 }, Effects.REGULAR_ARTISTIANS_POTION),

		REGULAR_NATURALIST_POTION(new int[]
		{ 17602 }, Effects.REGULAR_NATURALISTS_POTION),

		REGULAR_SURVIVALIST_POTION(new int[]
		{ 17604 }, Effects.REGULAR_SURVIVALISTS_POTION),

		STRONG_MELEE_POTION(new int[]
		{ 17610 }, Effects.STRONG_MELEE_POTION),

		STRONG_MAGIC_POTION(new int[]
		{ 17606 }, Effects.STRONG_MAGIC_POTION),

		STRONG_RANGE_POTION(new int[]
		{ 17608 }, Effects.STRONG_RANGE_POTION),

		STRONG_DEFENCE_POTION(new int[]
		{ 17612 }, Effects.STRONG_DEFENCE_POTION),

		STRONG_STAT_RESTORE(new int[]
		{ 17614 }, Effects.STRONG_STAT_RESTORE_POTION),

		STRONG_CURE_POTION(new int[]
		{ 17616 }, Effects.STRONG_STAT_CURE_POTION),

		STRONG_REJUVINATION_POTION(new int[]
		{ 17618 }, Effects.STRONG_REJUVINATION_POTION),

		STRONG_GATHERS_POTION(new int[]
		{ 17622 }, Effects.STRONG_GATHERER_POTION),

		STRONG_ARTISTAN_POTION(new int[]
		{ 17624 }, Effects.STRONG_ARTISTIANS_POTION),

		STRONG_NATURALIST_POTION(new int[]
		{ 17626 }, Effects.STRONG_NATURALISTS_POTION),

		STRONG_SURVIVALIST_POTION(new int[]
		{ 17628 }, Effects.STRONG_SURVIVALISTS_POTION),

		;

		private int[] id;
		private Effects effect;

		private Drink(int[] id, Effects effect) {
			this.id = id;
			this.effect = effect;
		}

		public boolean isFlask() {
			return getMaxDoses() == 6;
		}

		public boolean isGourd() {
			return id.length > 0 && id[0] >= 50913 && id[0] <= 50996;
		}

		public boolean isPotion() {
			return getMaxDoses() == 4;
		}

		public boolean isDungVial() {
			String name = toString();
			return name.contains("WEAK_") || name.contains("REGULAR_") || name.contains("STRONG_");
		}

		public int getMaxDoses() {
			return id.length;
		}

		public int[] getId() {
			return id;
		}

		public int getIdForDoses(int doses) {
			return id[getMaxDoses() - doses];
		}

		public boolean contains(int i) {
			for (int i2 : id)
				if (i2 == i)
					return true;
			return false;
		}
		
		public Effects getEffect() {
			return effect;
		}
	}

	public enum Effects {
		WEAK_MELEE_POTION(Skills.ATTACK, Skills.STRENGTH) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 2 + (realLevel * 0.07));
			}
		},

		WEAK_MAGIC_POTION(Skills.MAGIC) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 2 + (realLevel * 0.07));
			}
		},

		WEAK_RANGE_POTION(Skills.RANGE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 2 + (realLevel * 0.07));
			}
		},

		WEAK_DEFENCE_POTION(Skills.DEFENCE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 2 + (realLevel * 0.07));
			}
		},

		WEAK_STAT_RESTORE_POTION(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE, Skills.AGILITY, Skills.COOKING, Skills.CRAFTING, Skills.FARMING, Skills.FIREMAKING, Skills.FISHING, Skills.FLETCHING, Skills.HERBLORE, Skills.MINING, Skills.RUNECRAFTING, Skills.SLAYER, Skills.SMITHING, Skills.THIEVING, Skills.WOODCUTTING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int boost = (int) (realLevel * 0.12) + 5;
				if (actualLevel > realLevel)
					return actualLevel;
				if (actualLevel + boost > realLevel)
					return realLevel;
				return actualLevel + boost;
			}
		},

		WEAK_STAT_CURE_POTION() {
			@Override
			public void extra(Player player) {
				long time = 300000;
				player.addFireImmune(time, true);
				player.addPoisonImmune(time);
			}
		},

		WEAK_REJUVINATION_POTION(Skills.SUMMONING, Skills.PRAYER) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int restore = (int) (Math.floor(player.getSkills().getLevelForXp(skillId) * 0.08) + 4);
				if (actualLevel + restore > realLevel)
					return realLevel;
				return actualLevel + restore;
			}

			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer((int) (40 + (player.getSkills().getLevelForXp(Skills.PRAYER) * 10) * .08));
			}
		},

		WEAK_GATHERER_POTION(Skills.WOODCUTTING, Skills.MINING, Skills.FISHING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 3 + (realLevel * 0.02));
			}
		},

		WEAK_ARTISTIANS_POTION(Skills.SMITHING, Skills.CRAFTING, Skills.FLETCHING, Skills.CONSTRUCTION, Skills.FIREMAKING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 3 + (realLevel * 0.02));
			}
		},

		WEAK_NATURALISTS_POTION(Skills.COOKING, Skills.FARMING, Skills.HERBLORE, Skills.RUNECRAFTING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 3 + (realLevel * 0.02));
			}
		},

		WEAK_SURVIVALISTS_POTION(Skills.AGILITY, Skills.HUNTER, Skills.THIEVING, Skills.SLAYER) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 3 + (realLevel * 0.1));
			}
		},

		REGULAR_MELEE_POTION(Skills.ATTACK, Skills.STRENGTH) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 3 + (realLevel * 0.11));
			}
		},

		REGULAR_MAGIC_POTION(Skills.MAGIC) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 3 + (realLevel * 0.11));
			}
		},

		REGULAR_RANGE_POTION(Skills.RANGE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 3 + (realLevel * 0.11));
			}
		},

		REGULAR_DEFENCE_POTION(Skills.DEFENCE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 3 + (realLevel * 0.11));
			}
		},

		REGULAR_STAT_RESTORE_POTION(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE, Skills.AGILITY, Skills.COOKING, Skills.CRAFTING, Skills.FARMING, Skills.FIREMAKING, Skills.FISHING, Skills.FLETCHING, Skills.HERBLORE, Skills.MINING, Skills.RUNECRAFTING, Skills.SLAYER, Skills.SMITHING, Skills.THIEVING, Skills.WOODCUTTING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int boost = (int) (realLevel * 0.17) + 7;
				if (actualLevel > realLevel)
					return actualLevel;
				if (actualLevel + boost > realLevel)
					return realLevel;
				return actualLevel + boost;
			}
		},

		REGULAR_STAT_CURE_POTION() {
			@Override
			public void extra(Player player) {
				long time = 600000;
				player.addFireImmune(time, true);
				player.addPoisonImmune(time);
			}
		},

		REGULAR_REJUVINATION_POTION(Skills.SUMMONING, Skills.PRAYER) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int restore = (int) (Math.floor(player.getSkills().getLevelForXp(skillId) * 0.15) + 7);
				if (actualLevel + restore > realLevel)
					return realLevel;
				return actualLevel + restore;
			}

			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer((int) (70 + (player.getSkills().getLevelForXp(Skills.PRAYER) * 10) * .15));
			}
		},

		REGULAR_GATHERER_POTION(Skills.WOODCUTTING, Skills.MINING, Skills.FISHING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 4 + (realLevel * 0.04));
			}
		},

		REGULAR_ARTISTIANS_POTION(Skills.SMITHING, Skills.CRAFTING, Skills.FLETCHING, Skills.CONSTRUCTION, Skills.FIREMAKING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 4 + (realLevel * 0.04));
			}
		},

		REGULAR_NATURALISTS_POTION(Skills.COOKING, Skills.FARMING, Skills.HERBLORE, Skills.RUNECRAFTING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 4 + (realLevel * 0.04));
			}
		},

		REGULAR_SURVIVALISTS_POTION(Skills.AGILITY, Skills.HUNTER, Skills.THIEVING, Skills.SLAYER) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 4 + (realLevel * 0.04));
			}
		},

		STRONG_MELEE_POTION(Skills.ATTACK, Skills.STRENGTH) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 6 + (realLevel * 0.20));
			}
		},

		STRONG_MAGIC_POTION(Skills.MAGIC) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 6 + (realLevel * 0.20));
			}
		},

		STRONG_RANGE_POTION(Skills.RANGE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 6 + (realLevel * 0.20));
			}
		},

		STRONG_DEFENCE_POTION(Skills.DEFENCE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 6 + (realLevel * 0.20));
			}
		},

		STRONG_STAT_RESTORE_POTION(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE, Skills.AGILITY, Skills.COOKING, Skills.CRAFTING, Skills.FARMING, Skills.FIREMAKING, Skills.FISHING, Skills.FLETCHING, Skills.HERBLORE, Skills.MINING, Skills.RUNECRAFTING, Skills.SLAYER, Skills.SMITHING, Skills.THIEVING, Skills.WOODCUTTING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int boost = (int) (realLevel * 0.24) + 10;
				if (actualLevel > realLevel)
					return actualLevel;
				if (actualLevel + boost > realLevel)
					return realLevel;
				return actualLevel + boost;
			}
		},

		STRONG_STAT_CURE_POTION() {
			@Override
			public void extra(Player player) {
				long time = 1200000;
				player.addFireImmune(time, true);
				player.addPoisonImmune(time);
			}
		},

		STRONG_REJUVINATION_POTION(Skills.SUMMONING, Skills.PRAYER) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int restore = (int) (Math.floor(player.getSkills().getLevelForXp(skillId) * 0.22) + 10);
				if (actualLevel + restore > realLevel)
					return realLevel;
				return actualLevel + restore;
			}

			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer((int) (100 + (player.getSkills().getLevelForXp(Skills.PRAYER) * 10) * 22));
			}
		},

		STRONG_GATHERER_POTION(Skills.WOODCUTTING, Skills.MINING, Skills.FISHING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 6 + (realLevel * 0.06));
			}
		},

		STRONG_ARTISTIANS_POTION(Skills.SMITHING, Skills.CRAFTING, Skills.FLETCHING, Skills.CONSTRUCTION, Skills.FIREMAKING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 6 + (realLevel * 0.06));
			}
		},

		STRONG_NATURALISTS_POTION(Skills.COOKING, Skills.FARMING, Skills.HERBLORE, Skills.RUNECRAFTING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 6 + (realLevel * 0.06));
			}
		},

		STRONG_SURVIVALISTS_POTION(Skills.AGILITY, Skills.HUNTER, Skills.THIEVING, Skills.SLAYER) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 6 + (realLevel * 0.06));
			}
		},

		ATTACK_POTION(Skills.ATTACK) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 3 + (realLevel * 0.1));
			}
			
			@Override
			public void extra(Player player) {
				player.setAttackPotionTimer();
			}
		},
		AGILITY_POTION(Skills.AGILITY) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (level + 3);
			}
		},
		COMBAT_POTION(Skills.ATTACK, Skills.STRENGTH) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 1 + (realLevel * 0.08));
			}
		},
		ELDER_PLUS(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 6 + (realLevel * 0.16));
			}
		},
		FISHING_POTION(Skills.FISHING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (level + 3);
			}
		},
		CRAFTING_POTION(Skills.CRAFTING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (level + 3);
			}
		},
		HUNTER_POTION(Skills.HUNTER) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (level + 3);
			}
		},
		FLETCHING_POTION(Skills.FLETCHING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (level + 3);
			}
		},
		ZAMORAK_BREW(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE) {

			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.DEFENCE) {
					int newLevel = (int) ((actualLevel + 1) * 0.92);
					if (newLevel < 1)
						newLevel = 1;
					return newLevel;
				} else {
					int boost = (int) (realLevel * 0.25);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				}
			}

			@Override
			public boolean canDrink(Player player) {
				int reflectedDmg = (int) (player.getHitpoints() * 0.12);
				if (player.getHitpoints() - reflectedDmg < 0) {
					player.getPackets().sendGameMessage("You need more hitpoints in order to survive the effects of the zamorak brew.");
					return false;
				}
				player.applyHit(new Hit(player, reflectedDmg, HitLook.REFLECTED_DAMAGE));
				player.getPrayer().restorePrayer((int) ((player.getPrayer().getPrayerpoints() + 20) * 1.08));
				return true;
			}
		},
		SANFEW_SERUM(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE, Skills.AGILITY, Skills.COOKING, Skills.CRAFTING, Skills.FARMING, Skills.FIREMAKING, Skills.FISHING, Skills.FLETCHING, Skills.HERBLORE, Skills.MINING, Skills.RUNECRAFTING, Skills.SLAYER, Skills.SMITHING, Skills.THIEVING, Skills.WOODCUTTING, Skills.SUMMONING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int boost = (int) (realLevel * 0.33);
				if (actualLevel > realLevel)
					return actualLevel;
				if (actualLevel + boost > realLevel)
					return realLevel;
				return actualLevel + boost;
			}

			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer((int) ((int) (player.getSkills().getLevelForXp(Skills.PRAYER) * 0.33 * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
				player.addPoisonImmune(180000);
				// TODO DISEASE HEALING
				
				if (player.getTemporaryAttributtes().remove(Key.BIG_PARATISE) != null) 
					player.getPackets().sendGameMessage("<col=00FF00>The parasite within you has been weakened.");
			}

		},
		SUMMONING_POT(Skills.SUMMONING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int restore = (int) (Math.floor(player.getSkills().getLevelForXp(skillId) * 0.25) + 7);
				if (actualLevel + restore > realLevel)
					return realLevel;
				return actualLevel + restore;
			}

			@Override
			public void extra(Player player) {
				Familiar familiar = player.getFamiliar();
				if (familiar != null)
					familiar.restoreSpecialAttack(15);
			}
		},
		ANTIPOISON() {
			@Override
			public void extra(Player player) {
				player.addPoisonImmune(86000);//1 min and half
				player.getPackets().sendGameMessage("You are now immune to poison.");
			}
		},
		ANTIPOISON_PLUS() {
			@Override
			public void extra(Player player) {
				player.addPoisonImmune(518000);//like 8 minutes and half
				player.getPackets().sendGameMessage("You are now immune to poison.");
			}
		},
		ANTIPOISON_DOUBLE_PLUS() {
			@Override
			public void extra(Player player) {
				player.addPoisonImmune(720000);//12 mins
				player.getPackets().sendGameMessage("You are now immune to poison.");
			}
		},
		SUPER_ANTIPOISON() {
			@Override
			public void extra(Player player) {
				player.addPoisonImmune(346000);
				player.getPackets().sendGameMessage("You are now immune to poison.");
			}
		},
		ENERGY_POTION() {
			@Override
			public void extra(Player player) {
				int restoredEnergy = player.getRunEnergy() + 20;
				player.setRunEnergy(restoredEnergy > 100 ? 100 : restoredEnergy);
			}
		},
		SUPER_ENERGY() {
			@Override
			public void extra(Player player) {
				int restoredEnergy = player.getRunEnergy() + 40;
				player.setRunEnergy(restoredEnergy > 100 ? 100 : restoredEnergy);
			}
		},
		ANTI_FIRE() {
			@Override
			public void extra(final Player player) {
				useAntifire(player, false, false);
			}
		},
		EXTENDED_ANTI_FIRE() {
			@Override
			public void extra(final Player player) {
				useAntifire(player, false, true);
			}
		},
		SUPER_ANTI_FIRE() {
			@Override
			public void extra(final Player player) {
				useAntifire(player, true, false);
			}
		},
		STRENGTH_POTION(Skills.STRENGTH) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 3 + (realLevel * 0.1));
			}
			
			@Override
			public void extra(Player player) {
				player.setStrengthPotionTimer();
			}
		},
		DEFENCE_POTION(Skills.DEFENCE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 3 + (realLevel * 0.1));
			}
			
			@Override
			public void extra(Player player) {
				player.setDefencePotionTimer();
			}
		},
		RANGE_POTION(Skills.RANGE) {

			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 5 + (realLevel * 0.1));
			}
			
			@Override
			public void extra(Player player) {
				player.setRangePotionTimer();
			}
		},
		MAGIC_POTION(Skills.MAGIC) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return level + 5;
			}
			
			@Override
			public void extra(Player player) {
				player.setMagicPotionTimer();
			}
		},
		PRAYER_POTION() {
			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer((int) ((int) (Math.floor(player.getSkills().getLevelForXp(Skills.PRAYER) * 2.5) + 70) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
			}
		},
		SUPER_STR_POTION(Skills.STRENGTH) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 5 + (realLevel * 0.15));
			}
			@Override
			public void extra(Player player) {
				player.setStrengthPotionTimer();
			}
		},
		SUPER_DEF_POTION(Skills.DEFENCE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 5 + (realLevel * 0.15));
			}
			@Override
			public void extra(Player player) {
				player.setDefencePotionTimer();
			}
		},
		SUPER_ATT_POTION(Skills.ATTACK) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 5 + (realLevel * 0.15));
			}
			@Override
			public void extra(Player player) {
				player.setAttackPotionTimer();
			}
		},
		SUPER_COMBAT_POTION(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 5 + (realLevel * 0.15));
			}
			@Override
			public void extra(Player player) {
				player.setCombatPotionTimer();
			}
		},
		EXTREME_STR_POTION(Skills.STRENGTH) {

			@Override
			public boolean canDrink(Player player) {
				if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleControler || FfaZone.isOverloadChanged(player)) {
					player.getPackets().sendGameMessage("You cannot drink this potion here.");
					return false;
				}
				return true;
			}

			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 5 + (realLevel * 0.22));
			}
			
			@Override
			public void extra(Player player) {
				player.setStrengthPotionTimer();
			}
		},
		EXTREME_DEF_POTION(Skills.DEFENCE) {

			@Override
			public boolean canDrink(Player player) {
				if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleControler || FfaZone.isOverloadChanged(player)) {
					player.getPackets().sendGameMessage("You cannot drink this potion here.");
					return false;
				}
				return true;
			}

			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 5 + (realLevel * 0.22));
			}
			
			@Override
			public void extra(Player player) {
				player.setDefencePotionTimer();
			}
		},
		EXTREME_ATT_POTION(Skills.ATTACK) {

			@Override
			public boolean canDrink(Player player) {
				if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleControler || FfaZone.isOverloadChanged(player)) {
					player.getPackets().sendGameMessage("You cannot drink this potion here.");
					return false;
				}
				return true;
			}

			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 5 + (realLevel * 0.22));
			}
			
			@Override
			public void extra(Player player) {
				player.setAttackPotionTimer();
			}
		},
		EXTREME_RAN_POTION(Skills.RANGE) {

			@Override
			public boolean canDrink(Player player) {
				if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleControler || FfaZone.isOverloadChanged(player)) {
					player.getPackets().sendGameMessage("You cannot drink this potion here.");
					return false;
				}
				return true;
			}

			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return (int) (level + 4 + (Math.floor(realLevel / 5.2)));
			}
			
			@Override
			public void extra(Player player) {
				player.setRangePotionTimer();
			}
		},
		EXTREME_MAG_POTION(Skills.MAGIC) {

			@Override
			public boolean canDrink(Player player) {
				if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleControler || FfaZone.isOverloadChanged(player)) {
					player.getPackets().sendGameMessage("You cannot drink this potion here.");
					return false;
				}
				return true;
			}

			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int level = actualLevel > realLevel ? realLevel : actualLevel;
				return level + 7;
			}
			
			@Override
			public void extra(Player player) {
				player.setMagicPotionTimer();
			}
		},
		RECOVER_SPECIAL() {

			@Override
			public boolean canDrink(Player player) {
				if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleControler || FfaZone.isOverloadChanged(player)) {
					player.getPackets().sendGameMessage("You cannot drink this potion here.");
					return false;
				}
				Long time = (Long) player.getTemporaryAttributtes().get("Recover_Special_Pot");
				if (time != null && Utils.currentTimeMillis() - time < 30000) {
					player.getPackets().sendGameMessage("You may only use this pot every 30 seconds.");
					return false;
				}
				return true;
			}

			@Override
			public void extra(Player player) {
				player.getTemporaryAttributtes().put("Recover_Special_Pot", Utils.currentTimeMillis());
				player.setSpecialRecoverTimer(30000);
				player.getCombatDefinitions().restoreSpecialAttack(25);
			}
		},
		SARADOMIN_BREW("You drink some of the foul liquid.", Skills.ATTACK, Skills.DEFENCE, Skills.STRENGTH, Skills.MAGIC, Skills.RANGE) {

			@Override
			public boolean canDrink(Player player) {
				return true;
			}

			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.DEFENCE) {
					int boost = (int) (realLevel * 0.25);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel * 0.90);
				}
			}

			@Override
			public void extra(Player player) {
				int hitpointsModification = (int) (player.getMaxHitpoints() * 0.15);
				player.heal(hitpointsModification + 20, hitpointsModification);
			}
		},
		XERICS_AID_PLUS("You drink some of the Xeric's aid.", Skills.ATTACK, Skills.DEFENCE, Skills.STRENGTH, Skills.MAGIC, Skills.RANGE) {

			@Override
			public boolean canDrink(Player player) {
				return true;
			}

			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.DEFENCE) {
					int boost = (int) (realLevel * 0.25);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel * 0.90);
				}
			}

			@Override
			public void extra(Player player) {
				int hitpointsModification = (int) (player.getMaxHitpoints() * 0.15);
				player.heal(hitpointsModification + 50, hitpointsModification);
			}
		},

		XERICS_AID("You drink some of the Xeric's aid.", Skills.ATTACK, Skills.DEFENCE, Skills.STRENGTH, Skills.MAGIC, Skills.RANGE) {

			@Override
			public boolean canDrink(Player player) {
				return true;
			}

			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.DEFENCE) {
					int boost = (int) (realLevel * 0.22);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel * 0.90);
				}
			}

			@Override
			public void extra(Player player) {
				int hitpointsModification = (int) (player.getMaxHitpoints() * 0.15);
				player.heal(hitpointsModification + 30, hitpointsModification);
			}
		},
		XERICS_AID_MINUS("You drink some of the Xeric's aid", Skills.ATTACK, Skills.DEFENCE, Skills.STRENGTH, Skills.MAGIC, Skills.RANGE) {

			@Override
			public boolean canDrink(Player player) {
				return true;
			}

			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.DEFENCE) {
					int boost = (int) (realLevel * 0.18);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel * 0.90);
				}
			}

			@Override
			public void extra(Player player) {
				int hitpointsModification = (int) (player.getMaxHitpoints() * 0.15);
				player.heal(hitpointsModification + 10, hitpointsModification);
			}
		},
		OVERLOAD() {

			@Override
			public boolean canDrink(Player player) {
				/*if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleControler || FfaZone.isOverloadChanged(player)) {
					player.getPackets().sendGameMessage("You cannot drink this potion here.");
					return false;
				}*/
				/*
				 * if (player.getOverloadDelay() > 0) {
				 * player.getPackets().sendGameMessage(
				 * "You may only use this potion every five minutes."); return
				 * false; }
				 */
				if (player.getHitpoints() <= 500 || player.getOverloadDelay() > (player.isVIPDonator() ? 980 : 480)) {
					player.getPackets().sendGameMessage("You need more than 500 life points to survive the power of overload.");
					return false;
				}
				return true;
			}

			@Override
			public void extra(final Player player) {
				player.setOverloadDelay(player.isVIPDonator() ? 1001 : 501);
				WorldTasksManager.schedule(new WorldTask() {
					int count = 4;

					@Override
					public void run() {
						if (count == 0)
							stop();
						if(ChambersOfXeric.getRaid(player) != null) {
							player.applyHit(new Hit(null, 100, HitLook.REGULAR_DAMAGE, 0));
						} else {
							player.applyHit(new Hit(null, player.isSupremeVIPDonator() ? 10 : player.isVIPDonator() ? 50 : player.isLegendaryDonator() ? 75 : 100, HitLook.REGULAR_DAMAGE, 0));
						}
						player.setNextAnimation(new Animation(3170));
						player.setNextGraphics(new Graphics(560));
						count--;
					}
				}, 0, 2);
			}
		},
		REVITALISATION_MINUS(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int boost = (int) (realLevel * 0.33);
				if (actualLevel > realLevel)
					return actualLevel;
				if (actualLevel + boost > realLevel)
					return realLevel;
				return actualLevel + boost;
			}
			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer(((int) (70 + (player.getSkills().getLevelForXp(Skills.PRAYER) * 2.50))));
			}
		},
		REVITALISATION(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int boost = (int) (realLevel * 0.33);
				if (actualLevel > realLevel)
					return actualLevel;
				if (actualLevel + boost > realLevel)
					return realLevel;
				return actualLevel + boost;
			}

			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer(((int) (90 + (player.getSkills().getLevelForXp(Skills.PRAYER) * 2.75))));
			}

		},
		REVITALISATION_PLUS(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int boost = (int) (realLevel * 0.33);
				if (actualLevel > realLevel)
					return actualLevel;
				if (actualLevel + boost > realLevel)
					return realLevel;
				return actualLevel + boost;
			}
			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer(((int) (110 + (player.getSkills().getLevelForXp(Skills.PRAYER) * 3.00))));
			}
		},
		SUPER_PRAYER() {
			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer((int) ((int) (70 + (player.getSkills().getLevelForXp(Skills.PRAYER) * 3.43)) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
			}
		},

		PRAYER_RENEWAL() {
			@Override
			public void extra(Player player) {
				player.setPrayerRenewalDelay((player.isLegendaryDonator() ? 2 : 1) * 501);
			}
		},
		PRAYER_ENHANCE_PLUS() {
			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer((int) ((int) (Math.floor(player.getSkills().getLevelForXp(Skills.PRAYER) * 2.5) + 70) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
				player.setPrayerRenewalDelay(672);
			}
		},
		PRAYER_ENHANCE() {
			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer((int) ((int) (Math.floor(player.getSkills().getLevelForXp(Skills.PRAYER) * 2.5) + 70) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
				player.setPrayerRenewalDelay(504);
			}
		},
		PRAYER_ENHANCE_MINUS() {
			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer((int) ((int) (Math.floor(player.getSkills().getLevelForXp(Skills.PRAYER) * 2.5) + 70) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
				player.setPrayerRenewalDelay(840);
			}
		},
		SUPER_RESTORE(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE, Skills.AGILITY, Skills.COOKING, Skills.CRAFTING, Skills.FARMING, Skills.FIREMAKING, Skills.FISHING, Skills.FLETCHING, Skills.HERBLORE, Skills.MINING, Skills.RUNECRAFTING, Skills.SLAYER, Skills.SMITHING, Skills.THIEVING, Skills.WOODCUTTING, Skills.SUMMONING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int boost = (int) (realLevel * 0.33);
				if (actualLevel > realLevel)
					return actualLevel;
				if (actualLevel + boost > realLevel)
					return realLevel;
				return actualLevel + boost;
			}

			@Override
			public void extra(Player player) {
				player.getPrayer().restorePrayer((int) ((int) (player.getSkills().getLevelForXp(Skills.PRAYER) * 0.33 * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
			}

		},
		RESTORE_POTION(Skills.ATTACK, Skills.STRENGTH, Skills.MAGIC, Skills.RANGE, Skills.PRAYER) {

			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				int boost = (int) (realLevel * 0.33);
				if (actualLevel > realLevel)
					return actualLevel;
				if (actualLevel + boost > realLevel)
					return realLevel;
				return actualLevel + boost;
			}
		},
		BEER("You drink a glass of beer.", Skills.ATTACK, Skills.STRENGTH) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.STRENGTH) {
					int boost = (int) (realLevel * 0.04);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel * 0.93);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(20);
			}
		},
		TANKARD("You quaff the beer. You feel slightly reinvigorated... but very dizzy too.", Skills.ATTACK, Skills.STRENGTH) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.STRENGTH) {
					int boost = (int) (4);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel > 9 && actualLevel != 0 ? actualLevel - 9 : actualLevel >> 9);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(10);
			}
		},
		KEG_OF_BEER("You chug the keg. You feel reinvigorated... ...but extremely drunk, too.", Skills.ATTACK, Skills.STRENGTH) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.STRENGTH) {
					int boost = (int) (10);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel > 40 && actualLevel != 0 ? actualLevel - 40 : actualLevel >> 40);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(20);

			}
		},
		MOONLIGHT_MEAD("You drink a glass of Moonlight Mead.") {
			@Override
			public void extra(Player player) {
				player.heal(40);
				player.getPackets().sendGameMessage("It tastes like something died in your mouth.");
			}
		},
		GROG("You drink a glass of grog.", Skills.ATTACK, Skills.STRENGTH) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.STRENGTH) {
					int boost = (int) (3);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel > 6 && actualLevel != 0 ? actualLevel - 6 : actualLevel >> 6);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(30);
			}
		},
		CIDER("You drink a glass of cider.", Skills.ATTACK, Skills.STRENGTH, Skills.FARMING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.FARMING) {
					int boost = (int) (1);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel > 2 && actualLevel != 0 ? actualLevel - 2 : actualLevel >> 2);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(20);
			}
		},
		GREENMANS_ALE("You drink a glass of Greenman's Ale.", Skills.ATTACK, Skills.STRENGTH, Skills.HERBLORE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.HERBLORE) {
					int boost = (int) (1);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel > 2 && actualLevel != 0 ? actualLevel - 2 : actualLevel >> 2);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(10);
			}
		},
		DWARVEN_STOUT("You drink a glass of Dwarven Stout.", Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MINING, Skills.SMITHING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.MINING || skillId == Skills.SMITHING) {
					int boost = (int) (1);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel > 2 && actualLevel != 0 ? actualLevel - 2 : actualLevel >> 2);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(10);
			}
		},
		DRAGON_BITTER("You drink a glass of Dragon Bitter.", Skills.ATTACK, Skills.STRENGTH) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.STRENGTH) {
					int boost = (int) (2);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel > 1 && actualLevel != 0 ? actualLevel - 1 : actualLevel >> 1);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(10);
			}
		},
		RANGERS_AID("You drink a glass of Ranger's Aid.", Skills.RANGE, Skills.DEFENCE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.RANGE) {
					int boost = (int) (2);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel > 5 && actualLevel != 0 ? actualLevel - 5 : actualLevel >> 5);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(10);
			}
		},
		SLAYER_RESPITE("You drink a glass of Slayer's Respite.", Skills.ATTACK, Skills.STRENGTH, Skills.SLAYER) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.SLAYER) {
					int boost = (int) (1);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel > 2 && actualLevel != 0 ? actualLevel - 2 : actualLevel >> 2);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(10);
			}
		},
		WIZARD_MIND_BOMB("You drink a glass of Wizard's mind bomb.", Skills.ATTACK, Skills.STRENGTH, Skills.MAGIC, Skills.DEFENCE) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.MAGIC) {

					int boost = (int) (player.getSkills().getLevelForXp(Skills.MAGIC) >= 50 ? 3 : 2);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else if (skillId == Skills.ATTACK) {
					return (int) (actualLevel > 4 && actualLevel != 0 ? actualLevel - 4 : actualLevel >> 4);
				} else {
					return (int) (actualLevel > 3 && actualLevel != 0 ? actualLevel - 3 : actualLevel >> 3);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(10);
			}
		},
		CHEF_DELIGHT("You drink a glass of Chef's Delight", Skills.ATTACK, Skills.STRENGTH, Skills.COOKING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.COOKING) {
					int boost = (int) (realLevel * 0.05);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel * 0.97);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(10);
			}
		},
		BANDIT_BREW("You drink a glass of Bandit's brew.", Skills.ATTACK, Skills.STRENGTH, Skills.THIEVING, Skills.DEFENCE) {

			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.ATTACK && skillId == Skills.THIEVING) {
					int boost = (int) (1);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else if (skillId == Skills.STRENGTH) {
					return (int) (actualLevel > 1 && actualLevel != 0 ? actualLevel - 1 : actualLevel >> 1);
				} else {
					return (int) (actualLevel > 6 && actualLevel != 0 ? actualLevel - 6 : actualLevel >> 6);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(20);
			}
		},
		AXEMANS("You drink a glass of Axeman's Folly", Skills.ATTACK, Skills.STRENGTH, Skills.WOODCUTTING) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.WOODCUTTING) {
					int boost = (int) (1);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel > 3 && actualLevel != 0 ? actualLevel - 3 : actualLevel >> 3);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(10);
			}
		},
		ASGARNIAN_ALE("You drink a glass of asgarnian ale.", Skills.ATTACK, Skills.STRENGTH) {
			@Override
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				if (skillId == Skills.STRENGTH) {
					int boost = (int) (2);
					int level = actualLevel > realLevel ? realLevel : actualLevel;
					return level + boost;
				} else {
					return (int) (actualLevel > 4 && actualLevel != 0 ? actualLevel - 4 : actualLevel >> 4);
				}
			}

			@Override
			public void extra(Player player) {
				player.heal(10);
			}
		},
		WINE(Skills.ATTACK) {
			public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
				return (int) (actualLevel * 0.90);
			}

			@Override
			public void extra(Player player) {
				player.getPackets().sendGameMessage("You drink the wine. You feel slightly reinvigorated...");
				player.getPackets().sendGameMessage("...and slightly dizzy too.");
				player.heal(70);
			}
		};

		private int[] affectedSkills;
		private String drinkMessage;

		private Effects(int... affectedSkills) {
			this(null, affectedSkills);
		}

		private Effects(String drinkMessage, int... affectedSkills) {
			this.drinkMessage = drinkMessage;
			this.affectedSkills = affectedSkills;
		}

		public int getAffectedSkill(Player player, int skillId, int actualLevel, int realLevel) {
			return actualLevel;
		}

		public boolean canDrink(Player player) {
			// usualy unused
			return true;
		}

		public void extra(Player player) {
			// usualy unused
		}
	}

	public static Drink getDrink(int id) {
		for (Drink pot : Drink.values())
			for (int potionId : pot.id) {
				if (id == potionId)
					return pot;
			}
		return null;
	}

	public static int getDoses(Drink pot, Item item) {
		for (int i = pot.id.length - 1; i >= 0; i--) {
			if (pot.id[i] == item.getId())
				return pot.id.length - i;
		}
		return 0;
	}

	public static int[] VIALS = new int[] {VIAL, /*FLASK,*/ ChambersHerblore.EMPTY_GOURD_VIAL};

	public static boolean isVial(int i) {
		return Arrays.stream(VIALS).anyMatch(id->i==id);
	}

	public static int mixPot(Player player, Item fromItem, Item toItem, int fromSlot, int toSlot, boolean single) {
		if (single) {
			if (isVial(toItem.getId()) || isVial(fromItem.getId())) {
				Drink pot = getDrink(isVial(fromItem.getId()) ? toItem.getId() : fromItem.getId());
				if (pot == null || pot.isFlask())
					return -1;
				if((pot.isFlask() || pot.isGourd()) && (toItem.getId() == VIAL || fromItem.getId() == VIAL))
					return -1;
				int doses = getDoses(pot, isVial(fromItem.getId()) ? toItem : fromItem);
				if (doses == 1) {
					player.getInventory().switchItem(fromSlot, toSlot);
					if (single)
						player.getPackets().sendGameMessage("You pour from one container into the other.", true);
					return 1;
				}
				int vialDoses = doses / 2;
				doses -= vialDoses;
				player.getInventory().getItems().set(isVial(fromItem.getId()) ? toSlot : fromSlot, new Item(pot.getIdForDoses(doses), 1));
				player.getInventory().getItems().set(isVial(fromItem.getId()) ? fromSlot : toSlot, new Item(pot.getIdForDoses(vialDoses), 1));
				player.getInventory().refresh(fromSlot);
				player.getInventory().refresh(toSlot);
				if (single)
					player.getPackets().sendGameMessage("You split the potion between the two vials.", true);
				return 2;
			}
		}
		Drink pot = getDrink(fromItem.getId());
		if (pot == null)
			return -1;
		int doses2 = getDoses(pot, toItem);
		if (doses2 == 0 || doses2 == pot.getMaxDoses()) // not same pot type or
			// full already
			return -1;
		int doses1 = getDoses(pot, fromItem);
		if (doses1 == 0 || doses1 == pot.getMaxDoses())
			return -1;
		doses2 += doses1;
		doses1 = doses2 > pot.getMaxDoses() ? doses2 - pot.getMaxDoses() : 0;
		doses2 -= doses1;
		if (doses1 == 0 && (pot.isFlask() || pot.isGourd()))
			player.getInventory().deleteItem(fromSlot, fromItem);
		else {
			player.getInventory().getItems().set(fromSlot, new Item(doses1 > 0 ? pot.getIdForDoses(doses1) : VIAL, 1));
			player.getInventory().refresh(fromSlot);
		}
		player.getInventory().getItems().set(toSlot, new Item(pot.getIdForDoses(doses2), 1));
		player.getInventory().refresh(toSlot);
		if (single)
			player.getPackets().sendGameMessage("You pour from one container into the other" + ((pot.isFlask() || pot.isGourd()) && doses1 == 0 ? " and the glass shatters to pieces." : "."));
		return 3;
	}
	
	private static int mixPotNoted(Player player, Item fromItem, Item toItem) {
		Drink pot = getDrink(fromItem.getDefinitions().getCertId());
		if (pot == null)
			return -1;
		int doses2 = getDoses(pot, new Item(toItem.getDefinitions().getCertId()));
		if (doses2 == 0 || doses2 == pot.getMaxDoses()) // not same pot type or
			// full already
			return -1;
		int doses1 = getDoses(pot, new Item(fromItem.getDefinitions().getCertId()));
		if (doses1 == 0 || doses1 == pot.getMaxDoses())
			return -1;
		doses2 += doses1;
		doses1 = doses2 > pot.getMaxDoses() ? doses2 - pot.getMaxDoses() : 0;
		doses2 -= doses1;
		player.getInventory().getItems().remove(new Item(fromItem.getId()));
		player.getInventory().getItems().remove(new Item(toItem.getId()));
		player.getInventory().getItems().add(new Item(ItemConfig.forID(pot.getIdForDoses(doses2)).getCertId(), 1));
		if (doses1 != 0 || !pot.isFlask()) {
			int id = doses1 > 0 ? pot.getIdForDoses(doses1) : VIAL;
			player.getInventory().getItems().add(new Item(ItemConfig.forID(id).getCertId(), 1));
		}
		return 3;
	}

	public static boolean emptyPot(Player player, Item item, int slot) {
		Drink pot = getDrink(item.getId());
		if (pot == null || pot.isFlask())
			return false;
		item.setId(VIAL);
		player.getInventory().refresh(slot);
		player.getPackets().sendGameMessage("You empty the vial.", true);
		return true;
	}

	public static boolean drink(Player player, Item item, int slot) {
		Drink drink = getDrink(item.getId());
		if (drink == null)
			return false;
		if (drink == Drink.OVERLOAD_POTION_SPECIAL && !(player.getControlerManager().getControler() instanceof FfaZone
				|| player.getControlerManager().getControler() instanceof TheHorde) && !player.isDungeoneer()) {
			player.getInventory().deleteItem(item);
			return  true;
		}
		if (player.getPotDelay() > Utils.currentTimeMillis())
			return true;
		if (!player.getControlerManager().canPot(drink))
			return true;
		if (!drink.effect.canDrink(player))
			return true;
		if (drink.isDungVial() && !player.getDungManager().isInside())
			return false;
		player.addPotDelay(1075);
		drink.effect.extra(player);
		int dosesLeft = getDoses(drink, item) - 1;
		
		if (drink != Drink.OVERLOAD_POTION_SPECIAL) {
			if (dosesLeft == 0/* && drink.isFlask()*/)
				player.getInventory().deleteItem(slot, item);
			else {
				player.getInventory().getItems().set(slot, new Item(dosesLeft > 0 ? drink.getIdForDoses(dosesLeft) : drink.isDungVial() ? DUNGEONEERING_VIAL : drink.isPotion() ? VIAL : getReplacedId(drink), 1));
				player.getInventory().refresh(slot);
			}
		}
		for (int skillId : drink.effect.affectedSkills)
			player.getSkills().set(skillId, drink.effect.getAffectedSkill(player, skillId, player.getSkills().getLevel(skillId), player.getSkills().getLevelForXp(skillId)));
		player.setNextAnimationNoPriority(new Animation(829));
		player.getPackets().sendSound(4580, 0, 1);
		if (drink.isFlask() || drink.isPotion()) {
			player.getPackets().sendGameMessage(drink.effect.drinkMessage != null ? drink.effect.drinkMessage : "You drink some of your " + item.getDefinitions().getName().toLowerCase().replace(" (1)", "").replace(" (2)", "").replace(" (3)", "").replace(" (4)", "").replace(" (5)", "").replace(" (6)", "") + ".", true);
			if (drink != Drink.OVERLOAD_POTION_SPECIAL)
				player.getPackets().sendGameMessage(dosesLeft == 0 ? "You have finished your " + (drink.isFlask() ? "flask and the glass shatters to pieces." : drink.isPotion() ? "potion and the glass shatters to pieces." : item.getName().toLowerCase() + ".") : "You have " + dosesLeft + " dose of potion left.", true);
		}
		return true;
	}

	public static void decantPotsInv(Player player) {//Cheepo Hax way, probs terrible performance but whatever xD
		int count = 0;
		outLoop: for (int fromSlot = 0; fromSlot < 28; fromSlot++) {
			Item fromItem = player.getInventory().getItem(fromSlot);
			if (fromItem == null)
				continue outLoop;
			innerLoop: for (int toSlot = 0; toSlot < 28; toSlot++) {
				Item toItem = player.getInventory().getItem(toSlot);
				if (toItem == null)
					continue innerLoop;
				
				if (fromItem.getDefinitions().isNoted() && toItem.getDefinitions().isNoted()) {
					notedLoop: for (int i = 0; i < Math.min(fromItem.getAmount(), toItem.getAmount()); i++) {
						Item fromItemR = player.getInventory().getItem(fromSlot); //may change
						if (fromItemR == null || fromItemR.getId() != fromItem.getId())
							continue outLoop;
						Item toItemR = player.getInventory().getItem(toSlot);
						if (toItemR == null || (fromSlot == toSlot && fromItemR.getAmount() == 1)
								 || toItemR.getId() != toItem.getId())
							continue innerLoop;
						if (mixPotNoted(player, fromItem, toItem) != -1) {
							fromSlot = 0;
							toSlot = 0;
							if (count++ >= 10000) //limit of 10k pots
								break outLoop;  
						} else
							break notedLoop;
					}
				} else {
					if (fromSlot == toSlot)
						continue innerLoop;
					if (mixPot(player, fromItem, toItem, fromSlot, toSlot, false) != -1) {
						if (count++ >= 1000)
							break outLoop;  
						break innerLoop;
					}
				}
			}
		}

		if (count != 0) {
			player.getInventory().getItems().shift();
			player.getInventory().refresh();
		}
	}

	@SuppressWarnings("incomplete-switch")
	private static int getReplacedId(Drink drink) {
		switch (drink) {
		case JUG:
			return 1935;
		case BEER:
		case TANKARD:
		case GREENMANS_ALE:
		case AXEMANS_ALE:
		case SLAYER_RESPITE:
		case RANGERS_AID:
		case MOONLIGHT_MEAD:
		case DRAGON_BITTER:
		case ASGARNIAN_ALE:
		case CHEF_DELIGHT:
		case CHEF_DELIGHT_KEG:
		case CIDER:
		case CIDER_KEG:
		case WIZARD_MIND_BOMB:
		case DWARVEN_STOUT:
		case GROG:
		case BANDIT_BREW:
			return 1919;
		case GREENMANS_ALE_KEG:
		case AXEMANS_ALE_KEG:
		case SLAYER_RESPITE_KEG:
		case MOONLIGHT_MEAD_KEG:
		case DRAGON_BITTER_KEG:
		case ASGARNIAN_ALE_KEG:
		case DWARVEN_STOUT_KEG:
		case KEG_OF_BEER:
			return 10885;
		}
		return 0;
	}

	private static void useAntifire(final Player player, boolean sup, boolean extend) {
		player.addFireImmune((player.isExtremeDonator() ? 2 : 1) * (extend ? 3 : 1) * 360000, sup);
		final long current = player.getFireImmune();
		player.getPackets().sendGameMessage("You are now immune to dragonfire.");
		WorldTasksManager.schedule(new WorldTask() {
			boolean stop = false;

			@Override
			public void run() {
				if (current != player.getFireImmune() || player.hasFinished()) {
					stop();
					return;
				}
				if (!stop) {
					player.getPackets().sendGameMessage("<col=480000>Your resistance to dragonfire is about to run out.</col>");
					stop = true;
				} else {
					stop();
					player.getPackets().sendGameMessage("<col=480000>Your resistance to dragonfire has run out.</col>");
				}
			}
		}, 500, 100);
	}

	public static void resetOverLoadEffect(Player player) {
		if (!player.isDead()) {
			int actualLevel = player.getSkills().getLevel(Skills.ATTACK);
			int realLevel = player.getSkills().getLevelForXp(Skills.ATTACK);
			if (actualLevel > realLevel)
				player.getSkills().set(Skills.ATTACK, realLevel);
			actualLevel = player.getSkills().getLevel(Skills.STRENGTH);
			realLevel = player.getSkills().getLevelForXp(Skills.STRENGTH);
			if (actualLevel > realLevel)
				player.getSkills().set(Skills.STRENGTH, realLevel);
			actualLevel = player.getSkills().getLevel(Skills.DEFENCE);
			realLevel = player.getSkills().getLevelForXp(Skills.DEFENCE);
			if (actualLevel > realLevel)
				player.getSkills().set(Skills.DEFENCE, realLevel);
			Long vecnaSkullImbuedHeart = (Long) player.getTemporaryAttributtes().get("VecnaSkullDelay");
			if(vecnaSkullImbuedHeart == null) {
				actualLevel = player.getSkills().getLevel(Skills.MAGIC);
				realLevel = player.getSkills().getLevelForXp(Skills.MAGIC);
				if (actualLevel > realLevel)
					player.getSkills().set(Skills.MAGIC, realLevel);
			}
			actualLevel = player.getSkills().getLevel(Skills.RANGE);
			realLevel = player.getSkills().getLevelForXp(Skills.RANGE);
			if (actualLevel > realLevel)
				player.getSkills().set(Skills.RANGE, realLevel);
			player.heal(500);
		}
		player.setOverloadDelay(0);
		player.getPackets().sendGameMessage("<col=480000>The effects of overload have worn off and you feel normal again.");
	}

	public static void applyOverLoadEffect(Player player) {

		ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
		if (raid != null || player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleControler || FfaZone.isOverloadChanged(player)) {
			int meleeCombatAdd = 5;

			int actualLevel = player.getSkills().getLevel(Skills.ATTACK);
			int realLevel = player.getSkills().getLevelForXp(Skills.ATTACK);
			int level = actualLevel > realLevel ? realLevel : actualLevel;
			player.getSkills().set(Skills.ATTACK, (int) (level + meleeCombatAdd + (realLevel * 0.16)));

			actualLevel = player.getSkills().getLevel(Skills.STRENGTH);
			realLevel = player.getSkills().getLevelForXp(Skills.STRENGTH);
			level = actualLevel > realLevel ? realLevel : actualLevel;
			player.getSkills().set(Skills.STRENGTH, (int) (level + meleeCombatAdd + (realLevel * 0.16)));

			actualLevel = player.getSkills().getLevel(Skills.DEFENCE);
			realLevel = player.getSkills().getLevelForXp(Skills.DEFENCE);
			level = actualLevel > realLevel ? realLevel : actualLevel;
			player.getSkills().set(Skills.DEFENCE, (int) (level + meleeCombatAdd + (realLevel * 0.16)));

			Long vecnaSkullImbuedHeart = (Long) player.getTemporaryAttributtes().get("VecnaSkullDelay");
			if(vecnaSkullImbuedHeart == null) {
				actualLevel = player.getSkills().getLevel(Skills.MAGIC);
				realLevel = player.getSkills().getLevelForXp(Skills.MAGIC);
				level = actualLevel > realLevel ? realLevel : actualLevel;
				if(raid != null && raid.isOsrsRaid())
					player.getSkills().set(Skills.MAGIC, (int) (level + meleeCombatAdd + (realLevel * 0.16)));
				else
					player.getSkills().set(Skills.MAGIC, level + meleeCombatAdd);

			}

			actualLevel = player.getSkills().getLevel(Skills.RANGE);
			realLevel = player.getSkills().getLevelForXp(Skills.RANGE);
			level = actualLevel > realLevel ? realLevel : actualLevel;
			player.getSkills().set(Skills.RANGE, (int) (level + 5 + (realLevel * 0.1)));
		} else {
			int actualLevel = player.getSkills().getLevel(Skills.ATTACK);
			int realLevel = player.getSkills().getLevelForXp(Skills.ATTACK);
			int level = actualLevel > realLevel ? realLevel : actualLevel;
			player.getSkills().set(Skills.ATTACK, (int) (level + 5 + (realLevel * 0.22)));

			actualLevel = player.getSkills().getLevel(Skills.STRENGTH);
			realLevel = player.getSkills().getLevelForXp(Skills.STRENGTH);
			level = actualLevel > realLevel ? realLevel : actualLevel;
			player.getSkills().set(Skills.STRENGTH, (int) (level + 5 + (realLevel * 0.22)));

			actualLevel = player.getSkills().getLevel(Skills.DEFENCE);
			realLevel = player.getSkills().getLevelForXp(Skills.DEFENCE);
			level = actualLevel > realLevel ? realLevel : actualLevel;
			player.getSkills().set(Skills.DEFENCE, (int) (level + 5 + (realLevel * 0.22)));

			Long vecnaSkullImbuedHeart = (Long) player.getTemporaryAttributtes().get("VecnaSkullDelay");
			if(vecnaSkullImbuedHeart == null) {
				actualLevel = player.getSkills().getLevel(Skills.MAGIC);
				realLevel = player.getSkills().getLevelForXp(Skills.MAGIC);
				level = actualLevel > realLevel ? realLevel : actualLevel;
				player.getSkills().set(Skills.MAGIC, level + 7);
			}

			actualLevel = player.getSkills().getLevel(Skills.RANGE);
			realLevel = player.getSkills().getLevelForXp(Skills.RANGE);
			level = actualLevel > realLevel ? realLevel : actualLevel;
			player.getSkills().set(Skills.RANGE, (int) (level + 4 + (Math.floor(realLevel / 5.2))));
		}
	}

	private Drinkables() {

	}
}
