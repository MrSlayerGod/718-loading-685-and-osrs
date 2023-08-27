package com.rs.game.player.actions;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.Achievements.Task;
import com.rs.game.player.actions.woodcutting.Woodcutting.TreeDefinitions;

public class Fletching extends Action {

	public static final int KNIFE = 946, CHISLE = 1755, BOW_STRING = 1777, CROSSBOW_STRING = 9438;
	public static final int DUNGEONEERING_KNIFE = 17754, DUNGEEONEERING_BOW_STRING = 17752, DUNGEONEERING_HEADLESS = 17747;

	public static enum Fletch {

		/**
		 * (u)'s and completed bows
		 */

		REGULAR_BOW(1511, 946, new int[]
		{ 52, 49584, 50, 48, 9440 }, new int[]
		{ 1, 3, 5, 10, 9 }, new double[]
		{ 5, 5, 5, 10, 6 }, new Animation(6702)),

		STRUNG_SHORT_BOW(50, 1777, new int[]
		{ 841 }, new int[]
		{ 5 }, new double[]
		{ 5 }, new Animation(6678)),

		STRUNG_LONG_BOW(48, 1777, new int[]
		{ 839 }, new int[]
		{ 10 }, new double[]
		{ 10 }, new Animation(6684)),

		OAK_BOW(1521, 946, new int[]
		{ 52, 54, 56, 9442, 52251 }, new int[]
		{ 15, 20, 25, 24, 27 }, new double[]
		{ 10, 16.5, 25, 16, 50 }, new Animation(6702)),

		STRUNG_OAK_SHORT_BOW(54, 1777, new int[]
		{ 843 }, new int[]
		{ 20 }, new double[]
		{ 16.5 }, new Animation(6679)),

		STRUNG_OAK_LONG_BOW(56, 1777, new int[]
		{ 845 }, new int[]
		{ 25 }, new double[]
		{ 25 }, new Animation(6685)),

		WILLOW_BOW(1519, 946, new int[]
		{ 52, 60, 58, 9444, 52254 }, new int[]
		{ 30, 35, 40, 39, 42 }, new double[]
		{ 15, 33.3, 41.5, 22, 83 }, new Animation(6702)),
		
		TEAK_BOW(6333, 946, new int[]
		{ 9446 }, new int[]
		{ 46 }, new double[]
		{ 27 }, new Animation(6702)),
		
		MAHOGANY_BOW(6332, 946, new int[]
		{ 9450 }, new int[]
		{ 61 }, new double[]
		{ 41 }, new Animation(6702)),

		STRUNG_WILLOW_SHORT_BOW(60, 1777, new int[]
		{ 849 }, new int[]
		{ 35 }, new double[]
		{ 33.3 }, new Animation(6680)),

		STRUNG_WILLOW_LONG_BOW(58, 1777, new int[]
		{ 847 }, new int[]
		{ 40 }, new double[]
		{ 41.5 }, new Animation(6686)),

		MAPLE_BOW(1517, 946, new int[]
		{ 52, 64, 62, 9448, 52257 }, new int[]
		{ 45, 50, 55, 54, 57 }, new double[]
		{ 20, 50, 58.3, 32, 116.5 }, new Animation(6702)),

		STRUNG_MAPLE_SHORT_BOW(64, 1777, new int[]
		{ 853 }, new int[]
		{ 50 }, new double[]
		{ 50 }, new Animation(6681)),

		STRUNG_MAPLE_LONG_BOW(62, 1777, new int[]
		{ 851 }, new int[]
		{ 55 }, new double[]
		{ 58.3 }, new Animation(6687)),

		YEW_BOW(1515, 946, new int[]
		{ 52, 68, 66, 9452, 52260 }, new int[]
		{ 60, 65, 70, 69, 72 }, new double[]
		{ 25, 75, 77, 50, 150 }, new Animation(6702)),

		STRUNG_YEW_SHORT_BOW(68, 1777, new int[]
		{ 857 }, new int[]
		{ 65 }, new double[]
		{ 67.5 }, new Animation(6682)),

		STRUNG_YEW_LONG_BOW(66, 1777, new int[]
		{ 855 }, new int[]
		{ 70 }, new double[]
		{ 75 }, new Animation(6688)),

		MAGIC_BOW(1513, 946, new int[]
		{ 52, 72, 70, 51952, 52263 }, new int[]
		{ 75, 80, 85, 78, 87 }, new double[]
		{ 30, 83.25, 91.5, 70, 183 }, new Animation(6702)),

		STRUNG_MAGIC_SHORT_BOW(72, 1777, new int[]
		{ 861 }, new int[]
		{ 80 }, new double[]
		{ 83.25 }, new Animation(6683)),

		STRUNG_MAGIC_LONG_BOW(70, 1777, new int[]
		{ 859 }, new int[]
		{ 85 }, new double[]
		{ 91.5 }, new Animation(6689)),
		
		REDWOOD(49669, 946, new int[]
		{ 52, 52266 }, new int[]
		{ 90, 92}, new double[]
		{ 35, 215}, new Animation(6702)),

		/**
		 * Crossbows
		 */

		U_BRONZE_CBOW(9440, 9420, new int[]
		{ 9454 }, new int[]
		{ 9 }, new double[]
		{ 6 }, new Animation(-1)),

		U_IRON_CBOW(9444, 9423, new int[]
		{ 9457 }, new int[]
		{ 39 }, new double[]
		{ 22 }, new Animation(-1)),

		U_BLURITE_CBOW(9442, 9422, new int[]
		{ 9456 }, new int[]
		{ 24 }, new double[]
		{ 16 }, new Animation(-1)),

		U_STEEL_CBOW(9446, 9425, new int[]
		{ 9459 }, new int[]
		{ 46 }, new double[]
		{ 27 }, new Animation(-1)),

		U_MITHRIL_CBOW(9448, 9427, new int[]
		{ 9461 }, new int[]
		{ 54 }, new double[]
		{ 32 }, new Animation(-1)),

		U_ADAMANT_CBOW(9450, 9429, new int[]
		{ 9463 }, new int[]
		{ 61 }, new double[]
		{ 41 }, new Animation(-1)),

		U_RUNITE_CBOW(9452, 9431, new int[]
		{ 9465 }, new int[]
		{ 69 }, new double[]
		{ 50 }, new Animation(-1)),
		
		U_DRAGON_CBOW(51952, 51918, new int[]
		{ 51921 }, new int[]
		{ 78 }, new double[]
		{ 70 }, new Animation(-1)),

		BRONZE_CBOW(9454, 9438, new int[]
		{ 9174 }, new int[]
		{ 9 }, new double[]
		{ 6.0 }, new Animation(6671)),

		IRON_CBOW(9457, 9438, new int[]
		{ 9177 }, new int[]
		{ 39 }, new double[]
		{ 22 }, new Animation(6673)),

		STEEL_CBOW(9459, 9438, new int[]
		{ 9179 }, new int[]
		{ 46 }, new double[]
		{ 27 }, new Animation(6674)),

		BLURITE_CBOW(9456, 9438, new int[]
		{ 9176 }, new int[]
		{ 24 }, new double[]
		{ 16 }, new Animation(6672)),

		MITHRIL_CBOW(9461, 9438, new int[]
		{ 9181 }, new int[]
		{ 52 }, new double[]
		{ 32 }, new Animation(6675)),

		ADAMANT_CBOW(9463, 9438, new int[]
		{ 9183 }, new int[]
		{ 61 }, new double[]
		{ 41 }, new Animation(6676)),

		RUNITE_CBOW(9465, 9438, new int[]
		{ 9185 }, new int[]
		{ 69 }, new double[]
		{ 50 }, new Animation(6677)),
		
		DRAGON_CBOW(51921, 9438, new int[]
		{ 51902 }, new int[]
		{ 78 }, new double[]
		{ 70 }, new Animation(6677)),

		/**
		 * Arrows
		 */
		HEADLESS_ARROWS(52, 314, new int[]
		{ 53 }, new int[]
		{ 1 }, new double[]
		{ 1 }, new Animation(-1)),

		BRONZE_ARROWS(39, 53, new int[]
		{ 882 }, new int[]
		{ 1 }, new double[]
		{ 0.4 }, new Animation(-1)),

		IRON_ARROWS(40, 53, new int[]
		{ 884 }, new int[]
		{ 15 }, new double[]
		{ 3.8 }, new Animation(-1)),

		STEEL_ARROWS(41, 53, new int[]
		{ 886 }, new int[]
		{ 30 }, new double[]
		{ 6.3 }, new Animation(-1)),

		MITHRIL_ARROWS(42, 53, new int[]
		{ 888 }, new int[]
		{ 45 }, new double[]
		{ 8.8 }, new Animation(-1)),

		ADAMANT_ARROWS(43, 53, new int[]
		{ 890 }, new int[]
		{ 60 }, new double[]
		{ 11.3 }, new Animation(-1)),

		RUNITE_ARROWS(44, 53, new int[]
		{ 892 }, new int[]
		{ 75 }, new double[]
		{ 13.8 }, new Animation(-1)),

		DRAGON_ARROWS(11237, 53, new int[]
		{ 11212 }, new int[]
		{ 90 }, new double[]
		{ 16.3 }, new Animation(-1)),
		
		/*
		 * Javelins
		 */
		BRONZE_JAVELIN(49570, 49584, new int[]
		{ 825 }, new int[]
		{ 3 }, new double[]
		{ 0.8 }, new Animation(-1)),
		IRON_JAVELIN(49572, 49584, new int[]
		{ 826 }, new int[]
		{ 17 }, new double[]
		{ 2 }, new Animation(-1)),
		STEEL_JAVELIN(49574, 49584, new int[]
		{ 827 }, new int[]
		{ 32 }, new double[]
		{ 2.5 }, new Animation(-1)),
		MITHRIL_JAVELIN(49576, 49584, new int[]
		{ 828 }, new int[]
		{ 47 }, new double[]
		{ 8 }, new Animation(-1)),
		ADAMANT_JAVELIN(49578, 49584, new int[]
		{ 829 }, new int[]
		{ 62 }, new double[]
		{ 10 }, new Animation(-1)),
		RUNE_JAVELIN(49580, 49584, new int[]
		{ 830 }, new int[]
		{ 77 }, new double[]
		{ 13 }, new Animation(-1)),
		DRAGON_JAVELIN(49582, 49584, new int[]
		{ 49484 }, new int[]
		{ 92 }, new double[]
		{ 17.3 }, new Animation(-1)),
		/**
		 * Bolts
		 */
		BRONZE_BOLT(9375, 314, new int[]
		{ 877 }, new int[]
		{ 9 }, new double[]
		{ 0.5 }, new Animation(-1)),

		IRON_BOLT(9377, 314, new int[]
		{ 9140 }, new int[]
		{ 39 }, new double[]
		{ 1.5 }, new Animation(-1)),

		STEEL_BOLT(9378, 314, new int[]
		{ 9141 }, new int[]
		{ 46 }, new double[]
		{ 3.5 }, new Animation(-1)),

		MITHRIL_BOLT(9379, 314, new int[]
		{ 9142 }, new int[]
		{ 54 }, new double[]
		{ 5 }, new Animation(-1)),

		ADAMANT_BOLT(9380, 314, new int[]
		{ 9143 }, new int[]
		{ 61 }, new double[]
		{ 7 }, new Animation(-1)),

		RUNITE_BOLT(9381, 314, new int[]
		{ 9144 }, new int[]
		{ 69 }, new double[]
		{ 10 }, new Animation(-1)),
		
		DRAGON_DRAGON_BOLTS(51930, 314, new int[]
				{ 51905 }, new int[]
				{ 84 }, new double[]
				{ 12 }, new Animation(-1)),

		OPAL_BOLTS(45, 877, new int[]
		{ 879 }, new int[]
		{ 11 }, new double[]
		{ 1.6 }, new Animation(-1)),

		BLURITE_BOLTS(9376, 314, new int[]
		{ 9139 }, new int[]
		{ 24 }, new double[]
		{ 1 }, new Animation(-1)),

		JADE_BOLTS(9139, 9187, new int[]
		{ 9335 }, new int[]
		{ 26 }, new double[]
		{ 2.4 }, new Animation(-1)),

		PEARL_BOLTS(9140, 46, new int[]
		{ 880 }, new int[]
		{ 41 }, new double[]
		{ 3.2 }, new Animation(-1)),

		SILVER_BOLTS(9382, 314, new int[]
		{ 9145 }, new int[]
		{ 43 }, new double[]
		{ 2.5 }, new Animation(-1)),

		RED_TOPAZ_BOLTS(9188, 9141, new int[]
		{ 9336 }, new int[]
		{ 48 }, new double[]
		{ 3.9 }, new Animation(-1)),

		BARBED_BOLTS(47, 877, new int[]
		{ 881 }, new int[]
		{ 51 }, new double[]
		{ 9.5 }, new Animation(-1)),

		SAPPHIRE_BOLTS(9189, 9142, new int[]
		{ 9337 }, new int[]
		{ 56 }, new double[]
		{ 2.4 }, new Animation(-1)),

		EMERALD_BOLTS(9142, 9190, new int[]
		{ 9338 }, new int[]
		{ 26 }, new double[]
		{ 4.7 }, new Animation(-1)),

		RUBY_BOLTS(9191, 9143, new int[]
		{ 9339 }, new int[]
		{ 63 }, new double[]
		{ 6.3 }, new Animation(-1)),

		DIAMOND_BOLTS(9143, 9192, new int[]
		{ 9340 }, new int[]
		{ 65 }, new double[]
		{ 7 }, new Animation(-1)),

		DRAGON_BOLTS(9193, 9144, new int[]
		{ 9341 }, new int[]
		{ 71 }, new double[]
		{ 8.2 }, new Animation(-1)),

		ONYX_BOLTS(9194, 9144, new int[]
		{ 9342 }, new int[]
		{ 73 }, new double[]
		{ 9.4 }, new Animation(-1)),

		/**
		 * Darts
		 */
		BRONZE_DART(819, 314, new int[]
		{ 806 }, new int[]
		{ 1 }, new double[]
		{ 0.8 }, new Animation(-1)),

		IRON_DART(820, 314, new int[]
		{ 807 }, new int[]
		{ 22 }, new double[]
		{ 1 }, new Animation(-1)),

		STEEL_DART(821, 314, new int[]
		{ 808 }, new int[]
		{ 37 }, new double[]
		{ 1.7 }, new Animation(-1)),

		MITHRIL_DART(822, 314, new int[]
		{ 809 }, new int[]
		{ 52 }, new double[]
		{ 4 }, new Animation(-1)),

		ADAMANT_DART(823, 314, new int[]
		{ 810 }, new int[]
		{ 67 }, new double[]
		{ 7.6 }, new Animation(-1)),

		RUNITE_DART(824, 314, new int[]
		{ 811 }, new int[]
		{ 81 }, new double[]
		{ 12.2 }, new Animation(-1)),

		DRAGON_DART(11232, 314, new int[]
		{ 11230 }, new int[]
		{ 95 }, new double[]
		{ 18.4 }, new Animation(-1)),

		/**
		 * Bolt tips
		 */
		OPAL_BOLTS_TIPS(1609, CHISLE, new int[]
		{ 45 }, new int[]
		{ 11 }, new double[]
		{ 1.5 }, new Animation(886)),

		JADE_BOLT_TIPS(1611, CHISLE, new int[]
		{ 9187 }, new int[]
		{ 26 }, new double[]
		{ 2 }, new Animation(886)),

		PEARL_BOLT_TIPS(411, CHISLE, new int[]
		{ 46 }, new int[]
		{ 41 }, new double[]
		{ 2 }, new Animation(886)),

		TOPAZ_BOLT_TIPS(1613, CHISLE, new int[]
		{ 9188 }, new int[]
		{ 48 }, new double[]
		{ 3.9 }, new Animation(887)),

		SAPPHIRE_BOLT_TIPS(1607, CHISLE, new int[]
		{ 9189 }, new int[]
		{ 56 }, new double[]
		{ 4 }, new Animation(888)),

		EMERALD_BOLT_TIPS(1605, CHISLE, new int[]
		{ 9190 }, new int[]
		{ 58 }, new double[]
		{ 5.5 }, new Animation(889)),

		RUBY_BOLT_TIPS(1603, CHISLE, new int[]
		{ 9191 }, new int[]
		{ 63 }, new double[]
		{ 6.3 }, new Animation(887)),

		DIAMOND_BOLT_TIPS(1601, CHISLE, new int[]
		{ 9192 }, new int[]
		{ 65 }, new double[]
		{ 7 }, new Animation(890)),

		DRAGON_BOLT_TIPS(1615, CHISLE, new int[]
		{ 9193 }, new int[]
		{ 71 }, new double[]
		{ 8.2 }, new Animation(885)),

		ONYX_BOLT_TIPS(6573, CHISLE, new int[]
		{ 9194 }, new int[]
		{ 73 }, new double[]
		{ 9.4 }, new Animation(2717)),

		DRAMEN_BRANCH(771, KNIFE, new int[]
		{ 772 }, new int[]
		{ 1 }, new double[]
		{ 0 }, new Animation(6702)),

		SAGIE_SHAFTS(21351, KNIFE, new int[]
		{ 21353 }, new int[]
		{ 83 }, new double[]
		{ 50 }, new Animation(3007)),

		SAGIE(21353, 21358, new int[]
		{ 21364 }, new int[]
		{ 83 }, new double[]
		{ 50 }, new Animation(3113)),

		BOLAS(21359, 21358, new int[]
		{ 21365 }, new int[]
		{ 87 }, new double[]
		{ 50 }, new Animation(3113)),

		/**
		 * Dungeoneering
		 */

		HEADLESS_ARROW(17742, 17796, new int[]
		{ 17747 }, new int[]
		{ 1 }, new double[]
		{ .3 }, new Animation(-1)),

		NOVITE_ARROW(17885, DUNGEONEERING_HEADLESS, new int[]
		{ 16427 }, new int[]
		{ 1 }, new double[]
		{ 19.5 }, new Animation(1248)),

		BATHUS_ARROW(17890, DUNGEONEERING_HEADLESS, new int[]
		{ 16432 }, new int[]
		{ 11 }, new double[]
		{ 37.5 }, new Animation(1248)),

		MARMAROS_ARROW(17895, DUNGEONEERING_HEADLESS, new int[]
		{ 16437 }, new int[]
		{ 22 }, new double[]
		{ 75 }, new Animation(1248)),

		KRATONITE_ARROW(17900, DUNGEONEERING_HEADLESS, new int[]
		{ 16442 }, new int[]
		{ 33 }, new double[]
		{ 112.5 }, new Animation(1248)),

		FRACTITE_ARROW(17905, DUNGEONEERING_HEADLESS, new int[]
		{ 16447 }, new int[]
		{ 44 }, new double[]
		{ 150.0 }, new Animation(1248)),

		ZEPHYRIUM_ARROW(17910, DUNGEONEERING_HEADLESS, new int[]
		{ 16452 }, new int[]
		{ 55 }, new double[]
		{ 187.5 }, new Animation(1248)),

		AGRONITE_ARROWS(17915, DUNGEONEERING_HEADLESS, new int[]
		{ 16457 }, new int[]
		{ 66 }, new double[]
		{ 225 }, new Animation(1248)),

		KATAGON_ARROWS(17920, DUNGEONEERING_HEADLESS, new int[]
		{ 16462 }, new int[]
		{ 77 }, new double[]
		{ 262.5 }, new Animation(1248)),

		GORGONITE_ARROWS(17925, DUNGEONEERING_HEADLESS, new int[]
		{ 16467 }, new int[]
		{ 88 }, new double[]
		{ 300 }, new Animation(1248)),

		PROMETHIUM_ARROWS(17930, DUNGEONEERING_HEADLESS, new int[]
		{ 16472 }, new int[]
		{ 99 }, new double[]
		{ 337.5 }, new Animation(1248)),

		TANGLE_SHORT_BOW(17702, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16867 }, new int[]
		{ 1 }, new double[]
		{ 5 }, new Animation(13225)),

		SEEPING_SHORT_BOW(17704, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16869 }, new int[]
		{ 11 }, new double[]
		{ 9 }, new Animation(13226)),

		BLOOD_SHORT_BOW(17706, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16871 }, new int[]
		{ 21 }, new double[]
		{ 15 }, new Animation(13227)),

		UTUKU_SHORT_BOW(17708, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16873 }, new int[]
		{ 31 }, new double[]
		{ 23 }, new Animation(13228)),

		SPINEBEAM_SHORT_BOW(17710, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16875 }, new int[]
		{ 41 }, new double[]
		{ 33 }, new Animation(13229)),

		BOVISTRANGLER_SHORT_BOW(17712, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16877 }, new int[]
		{ 51 }, new double[]
		{ 45 }, new Animation(13230)),

		THIGAT_SHORT_BOW(17714, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16879 }, new int[]
		{ 61 }, new double[]
		{ 59 }, new Animation(13231)),

		CORPSETHORN_SHORT_BOW(17716, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16881 }, new int[]
		{ 71 }, new double[]
		{ 75 }, new Animation(13232)),

		ENTGALLOW_SHORT_BOW(17718, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16883 }, new int[]
		{ 81 }, new double[]
		{ 93 }, new Animation(13233)),

		GRAVE_CREEPER_SHORT_BOW(17720, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16885 }, new int[]
		{ 91 }, new double[]
		{ 113 }, new Animation(13234)),

		TANGLE_LONG_BOW(17722, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16317 }, new int[]
		{ 6 }, new double[]
		{ 5.7 }, new Animation(13235)),

		SEEPING_LONG_BOW(17724, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16319 }, new int[]
		{ 16 }, new double[]
		{ 10.3 }, new Animation(13236)),

		BLOOD_LONG_BOW(17726, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16321 }, new int[]
		{ 26 }, new double[]
		{ 17.2 }, new Animation(13237)),

		UTUKU_LONG_BOW(17728, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16323 }, new int[]
		{ 36 }, new double[]
		{ 26.4 }, new Animation(13238)),

		SPINEBEAM_LONG_BOW(17730, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16325 }, new int[]
		{ 46 }, new double[]
		{ 37.9 }, new Animation(13239)),

		BOVISTRANGLER_LONG_BOW(17732, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16327 }, new int[]
		{ 56 }, new double[]
		{ 51.7 }, new Animation(13240)),

		THIGAT_LONG_BOW(17734, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16329 }, new int[]
		{ 66 }, new double[]
		{ 67.8 }, new Animation(13241)),

		CORPSETHORN_LONG_BOW(17736, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16331 }, new int[]
		{ 76 }, new double[]
		{ 86.2 }, new Animation(13242)),

		ENTGALLOW_LONG_BOW(17738, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16333 }, new int[]
		{ 86 }, new double[]
		{ 106.9 }, new Animation(13243)),

		GRAVE_CREEPER_LONG_BOW(17740, DUNGEEONEERING_BOW_STRING, new int[]
		{ 16335 }, new int[]
		{ 96 }, new double[]
		{ 129.9 }, new Animation(13244)),

		TANGLE_GUM_UNSTRUNG(17682, DUNGEONEERING_KNIFE, new int[]
		{ 17702, 17722, 17742, 16977, 17756 }, new int[]
		{ 1, 6, 1, 8, 3 }, new double[]
		{ 5, 5.7, 4.5, 8, 12 }, new Animation(1248)),

		SEEPING_ELM_UNSTRUNG(17684, DUNGEONEERING_KNIFE, new int[]
		{ 17704, 17724, 17742, 16979, 17758 }, new int[]
		{ 11, 16, 10, 18, 13 }, new double[]
		{ 9, 10.3, 6.3, 12, 21.6 }, new Animation(1248)),

		BLOOD_SPINDLE_UNSTRUNG(17686, DUNGEONEERING_KNIFE, new int[]
		{ 17706, 17726, 17742, 16981, 17760 }, new int[]
		{ 21, 26, 20, 28, 23 }, new double[]
		{ 15, 17.2, 7.8, 32, 36 }, new Animation(1248)),

		UTUKU_UNSTRUNG(17688, DUNGEONEERING_KNIFE, new int[]
		{ 17708, 17728, 17742, 16983, 17762 }, new int[]
		{ 31, 36, 30, 38, 33 }, new double[]
		{ 23, 26.4, 9.6, 43.2, 55.2 }, new Animation(1248)),

		SPINEBEAM_UNSTRUNG(17690, DUNGEONEERING_KNIFE, new int[]
		{ 17710, 17730, 17742, 16985, 17764 }, new int[]
		{ 41, 46, 40, 48, 43 }, new double[]
		{ 33, 37.9, 11.1, 76.8, 79.2 }, new Animation(1248)),

		BOVISTRANGLER_UNSTRUNG(17692, DUNGEONEERING_KNIFE, new int[]
		{ 17712, 17732, 17742, 16987, 17766 }, new int[]
		{ 51, 56, 50, 58, 53 }, new double[]
		{ 45, 51.7, 12.9, 93.3, 108 }, new Animation(1248)),

		THIGAT_UNSTRUNG(17694, DUNGEONEERING_KNIFE, new int[]
		{ 17714, 17734, 17742, 16989, 17768 }, new int[]
		{ 61, 66, 1, 68, 63 }, new double[]
		{ 59, 67.8, 14.4, 113.4, 141.6 }, new Animation(1248)),

		CORPSETHORN_UNSTRUNG(17696, DUNGEONEERING_KNIFE, new int[]
		{ 17716, 17736, 17742, 16991, 17770 }, new int[]
		{ 71, 76, 70, 78, 73 }, new double[]
		{ 75, 86.2, 16.2, 137.7, 180 }, new Animation(1248)),

		ENTGALLOW_UNSTRUNG(17698, DUNGEONEERING_KNIFE, new int[]
		{ 17718, 17738, 17742, 16993, 17772 }, new int[]
		{ 81, 86, 80, 88, 83 }, new double[]
		{ 93, 106.9, 17.7, 167.4, 223.2 }, new Animation(1248)),

		GRAVE_CREEPER_UNSTRUNG(17700, DUNGEONEERING_KNIFE, new int[]
		{ 17720, 17740, 17742, 16995, 17774 }, new int[]
		{ 91, 96, 90, 98, 93 }, new double[]
		{ 113, 129.9, 19.5, 203.4, 271.2 }, new Animation(1248)),

		BROAD_BOLTS(13279, 314, new int[]
		{ 13280 }, new int[]
		{ 55 }, new double[]
		{ 3 }, new Animation(-1)),

		BROAD_ARROWS(13278, 53, new int[]
		{ 4160 }, new int[]
		{ 52 }, new double[]
		{ 10 }, new Animation(-1));

		public static Fletch forId(int id, int selected) {
			for (Fletch fletch : Fletch.values()) {
				if (id == fletch.getId() && selected == fletch.getSelected())
					return fletch;
			}
			return null;
		}

		private int[] product, level;
		private int id, selected;
		private double[] xp;
		private Animation anim;

		private Fletch(int id, int selected, int[] product, int level[], double[] xp, Animation anim) {
			this.id = id;
			this.product = product;
			this.selected = selected;
			this.xp = xp;
			this.anim = anim;
			this.level = level;
		}

		public int getId() {
			return id;
		}

		public int getSelected() {
			return selected;
		}

		public int[] getProduct() {
			return product;
		}

		public int[] getLevel() {
			return level;
		}

		public double[] getXp() {
			return xp;
		}

		public Animation getAnim() {
			return anim;
		}
	}

	private Fletch fletch;
	private int option, ticks;

	public Fletching(Fletch fletch, int option, int ticks) {
		this.fletch = fletch;
		this.option = option;
		this.ticks = ticks;
		if (ticks > 1 && fletch.getProduct()[option] >= 52251 && fletch.getProduct()[option] <= 52266)
			this.ticks /= 2;
	}

	@Override
	public boolean start(Player player) {
		if (option >= fletch.getProduct().length)
			return false;

		if (fletch.name().toLowerCase().contains("dart")) {
			processWithDelay(player);
			return false;
		}

		if (!process(player))
			return false;

		if (!player.getSlayerManager().hasLearnedBroad()) {
			if (fletch == Fletch.BROAD_ARROWS || fletch == Fletch.BROAD_BOLTS) {
				player.getPackets().sendGameMessage("You lack the knowledge to create a broad accessory, perhaps a Slayer Master could assist you.");
				return false;
			}
		}

		player.getPackets().sendGameMessage("You attempt to create a " + new Item(fletch.getProduct()[option]).getDefinitions().getName().replace("(u)", "") + "...", true);
		return true;
	}

	@Override
	public boolean process(Player player) {
		if (ticks <= 0)
			return false;
		boolean stackable1 = ItemConfig.forID(fletch.getSelected()).isStackable();
		int amount = stackable1 ? 15 : 1;
		int amount2 = fletch.getSelected() != Fletching.DUNGEONEERING_KNIFE && ItemConfig.forID(fletch.getId()).isStackable() ? 15 : (fletch.getProduct()[option] >= 52251 && fletch.getProduct()[option] <= 52266 ? 2 : 1);
		if (stackable1) {
			int amt1 = player.getInventory().getAmountOf(fletch.getSelected());
			int amt2 = player.getInventory().getAmountOf(fletch.getId());
			if (amt1 < amount) {
				amount = amt1;
				amount2 = amt1;
			}
			if (amt2 < amount2) {
				amount = amt2;
				amount2 = amt2;
			}
			if (amt1 == 0 || amt2 == 0)
				return false;
		}
		if (amount2 > 28) {
			if (ticks > 10)
				ticks = 10;
		}
		if (
				!((fletch.getSelected() == KNIFE && (player.getInventory().containsItem(14111, amount)
						|| player.getInventory().containsItem(14103, amount))) || 
				
				player.getInventory().containsItemToolBelt(fletch.getSelected(), amount)) || !player.getInventory().containsItemToolBelt(fletch.getId(), amount2)) {
			player.getPackets().sendGameMessage("You don't have the items required to do this.");
			return false;
		}
		if (player.getSkills().getLevel(Skills.FLETCHING) < fletch.getLevel()[option]) {
			player.getPackets().sendGameMessage("You need a level of " + fletch.getLevel()[option] + " to fletch this.");
			return false;
		}
		return true;
	}

	public static boolean maxMakeQuantityTen(Fletch fletch) {
		return ItemConfig.forID(fletch.getId()).isStackable() && (fletch.getSelected() == CHISLE || ItemConfig.forID(fletch.getSelected()).isStackable());
	}

	@Override
	public int processWithDelay(Player player) {
		ticks--;
		player.setNextAnimation(fletch.getAnim());
		boolean stackable1 = ItemConfig.forID(fletch.getSelected()).isStackable();
		int amount = stackable1 ? 15 : 1;
		int amount2 = fletch.getSelected() != Fletching.DUNGEONEERING_KNIFE && ItemConfig.forID(fletch.getId()).isStackable() ? 15 
				: (fletch.getProduct()[option] >= 52251 && fletch.getProduct()[option] <= 52266 ? 2 : 1);
		int amount3 = ItemConfig.forID(fletch == Fletch.SAGIE_SHAFTS ? 5 : fletch.getProduct()[option]).isStackable() ? (fletch == Fletch.REDWOOD ? 105 : fletch == Fletch.MAGIC_BOW ? 90 : fletch == Fletch.YEW_BOW ? 75 : fletch == Fletch.MAPLE_BOW ? 60 : fletch == Fletch.WILLOW_BOW ? 45 : fletch == Fletch.OAK_BOW ? 30 : 15) : 1;
		if (stackable1) {
			int amt1 = player.getInventory().getAmountOf(fletch.getSelected());
			int amt2 = player.getInventory().getAmountOf(fletch.getId());
			if (amt1 < amount) {
				amount = amt1;
				amount2 = amt1;
				amount3 = amt1;
			}
			if (amt2 < amount2) {
				amount = amt2;
				amount2 = amt2;
				amount3 = amt2;
			}
		}
		
		player.getInventory().deleteItem(fletch.getId(), amount2);
		if (fletch.getSelected() != KNIFE && fletch.getSelected() != DUNGEONEERING_KNIFE && fletch.getSelected() != CHISLE)
			player.getInventory().deleteItem(fletch.getSelected(), amount);

		
		if (fletch.getProduct()[option] == 50)
			player.getAchievements().add(Task.FLETCH_SHORTBOW);
		else if (fletch.getProduct()[option] == 62)
			player.getAchievements().add(Task.FLETCH_MAPLE_LONGBOW);
		else if (fletch.getProduct()[option] == 68)
			player.getAchievements().add(Task.FLETCH_YEW_SHORTBOW);
		else if (fletch.getProduct()[option] == 70)
			player.getAchievements().add(Task.FLETCH_MAGIC_LONGBOW);
		
		player.getSkills().addXp(Skills.FLETCHING, fletch.getXp()[option] * amount);
		if (amount3 > 1)
			amount3 *= Settings.getCraftRate();
		player.getInventory().addItem(fletch.getProduct()[option], amount3);
		player.getPackets().sendGameMessage("You successfully create a " + new Item(fletch.getProduct()[option]).getDefinitions().getName().replace("(u)", "") + ".", true);
		player.getPackets().sendGameMessage("You attempt to create a " + new Item(fletch.getProduct()[option]).getDefinitions().getName().replace("(u)", ""), true);

		if(fletch.name().toLowerCase().contains("dart"))
			return 0;

		return fletch.name().endsWith("_STRUNG") || fletch.name().endsWith("BOW") ? 3 : 2;
	}

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 3);
	}

	public static Fletch isFletching(Item first, Item second) {
		
		if (first.getId() == 14111 || first.getId() == 14103)
			first = new Item(KNIFE);
		else if (second.getId() == 14111 || second.getId() == 14103)
			second = new Item(KNIFE);
		
		Fletch fletch = Fletch.forId(first.getId(), second.getId());
		int selected = second.getId();
		if (fletch == null) {
			fletch = Fletch.forId(second.getId(), first.getId());
			selected = first.getId();
		}
		return fletch != null && fletch.getSelected() == selected ? fletch : null;
	}
}
