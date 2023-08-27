/**
 * 
 */
package com.rs.game.player.actions.woodcutting;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;

/**
 * @author dragonkk(Alex)
 * Oct 30, 2017
 */
public abstract class WoodcuttingBase extends Action {


	public enum HatchetDefinitions {

		NOVITE(16361, 1, 1, 13118),

		BATHUS(16363, 10, 4, 13119),

		MARMAROS(16365, 20, 5, 13120),

		KRATONITE(16367, 30, 7, 13121),

		FRACTITE(16369, 40, 10, 13122),

		ZEPHYRIUM(16371, 50, 12, 13123),

		ARGONITE(16373, 60, 13, 13124),

		KATAGON(16375, 70, 15, 13125),

		GORGONITE(16377, 80, 17, 13126),

		PROMETHIUM(16379, 90, 19, 13127),

		PRIMAL(16381, 99, 21, 13128),

		BRONZE(1351, 1, 1, 879, 12322),

		IRON(1349, 1, 2, 877, 2847),

		STEEL(1353, 5, 3, 875, 880),

		BLACK(1361, 11, 4, 873, 878),

		MITHRIL(1355, 21, 5, 871, 876),

		ADAMANT(1357, 31, 7, 869, 874),

		RUNE(1359, 41, 10, 867, 872),

		DRAGON(6739, 61, 13, 2846, 870),
		
		_3RD_AGE(50011, 61, 13, 27264),

		INFERNO(13661, 61, 13, 10251, 12323),
		
		INFERNAL(43241, 61, 13, 22117),
		
		CRYSTAL(53673, 71, 15, 28324),
		
		SACRED(14108, 40, 10, 12338, 12324),
		
		VOLATILE(14100, 40, 10, 12338, 12324),
		
		;

		protected int itemId, levelRequried, axeTime, emoteId;
		protected int ivyEmoteID;

		private HatchetDefinitions(int itemId, int levelRequried, int axeTime, int emoteId) {
			this(itemId, levelRequried, axeTime, emoteId, emoteId);
		}
		
		private HatchetDefinitions(int itemId, int levelRequried, int axeTime, int emoteId, int ivyEmoteID) {
			this.itemId = itemId;
			this.levelRequried = levelRequried;
			this.axeTime = axeTime;
			this.emoteId = emoteId;
			this.ivyEmoteID = ivyEmoteID;
		}

		public int getItemId() {
			return itemId;
		}

		public int getLevelRequried() {
			return levelRequried;
		}

		public int getAxeTime() {
			return axeTime;
		}

		public int getEmoteId() {
			return emoteId;
		}
	}
	
	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}

	public static HatchetDefinitions getHatchet(Player player, boolean dungeoneering) {
		for (int i = dungeoneering ? 10 : HatchetDefinitions.values().length - 1; i >= (dungeoneering ? 0 : 11); i--) { //from best to worst
			HatchetDefinitions def = HatchetDefinitions.values()[i];
			if (player.getInventory().containsItemToolBelt(def.itemId) || player.getEquipment().getWeaponId() == def.itemId) {
				if (player.getSkills().getLevel(Skills.WOODCUTTING) >= def.levelRequried)
					return def;
			}
		}
		return null;
	}
	
	public static HatchetDefinitions getHatchet(int id) {
		for (HatchetDefinitions h : HatchetDefinitions.values()) {
			if (h.itemId == id)
				return h;
		}
		return null;
	}

	
}
