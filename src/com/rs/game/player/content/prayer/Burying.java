package com.rs.game.player.content.prayer;

import java.util.HashMap;
import java.util.Map;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class Burying {

	public enum Bone {
		NORMAL(526, 4.5),

		BURNT(528, 4.5),

		WOLF(2859, 4.5),

		MONKEY(3183, 5),

		BAT(530, 5),

		BIG(532, 15),

		JOGRE(3125, 15),

		ZOGRE(4812, 22.5),

		SHAIKAHAN(3123, 25),

		BABY(534, 30),

		WYVERN(6812, 50),

		DRAGON(536, 72),

		FAYRG(4830, 84),

		RAURG(4832, 96),

		DAGANNOTH(6729, 125),

		OURG(4834, 140),
		//dung ones
		FROST_DRAGON(18830, 180)
		//real ones
		,FROST_DRAGON_2(18832, 180)
		
		,LAVA_DRAGON(41943, 85)
		
		, SUPERIOR_DRAGON(52124, 150)
		
		, HYDRA(52786, 110)
		
		, DRAKE(52783, 80)
		
		, WYRM(52780, 50)
		
		,IMPIOUS(20264, 4, true)
		
		, ACCURSED(20266, 12.5, true)
		
		,INFERNAL(20268, 62.5, true);

		private int id;
		private double experience;
		private boolean ash;

		private static Map<Integer, Bone> bones = new HashMap<Integer, Bone>();

		static {
			for (Bone bone : Bone.values()) {
				bones.put(bone.getId(), bone);
			}
		}

		public static Bone forId(int id) {
			return bones.get(id);
		}

		private Bone(int id, double experience, boolean ash) {
			this.id = id;
			this.experience = experience;
			this.ash = ash;
		}
		
		private Bone(int id, double experience) {
			this.id = id;
			this.experience = experience;
		}

		public int getId() {
			return id;
		}
		
		public boolean isAsh() {
			return ash;
		}

		public double getExperience() {
			return experience;
		}

		public static final Animation BURY_ANIMATION = new Animation(827);

		public static void bury(final Player player, int inventorySlot) {
			final Item item = player.getInventory().getItem(inventorySlot);
			if (item == null)
				return;
			final Bone bone = Bone.forId(item.getId());
			if(bone == null)
				return;
			if (bone == Bone.SUPERIOR_DRAGON && player.getSkills().getLevelForXp(Skills.PRAYER) < 70) {
				player.getPackets().sendGameMessage("You dont have the required level to bury this bone.");
				return;
			}
			final ItemConfig itemDef = new ItemConfig(item.getId());
			player.lock(2);
			switch(item.getId()) {
			case 20264:
				player.setNextAnimation(new Animation(445));
				player.setNextGraphics(new Graphics(56));
				break;
			case 20266:
				player.setNextAnimation(new Animation(445));
				player.setNextGraphics(new Graphics(47));
				break;
			case 20268:
				player.setNextAnimation(new Animation(445));
				 player.setNextGraphics(new Graphics(40));
				break;
			default:
				player.getPackets().sendSound(2738, 0, 1);
				player.setNextAnimation(BURY_ANIMATION);
				break;
			}
			    
			
			player.getPackets().sendGameMessage(bone.ash ? "You scatter the ashes in the wind." : "You dig a hole in the ground...", true);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					if(!bone.ash)
						player.getPackets().sendGameMessage("You bury the " + itemDef.getName().toLowerCase(), true);
					player.getInventory().deleteItem(item.getId(), 1);
					double xp = bone.getExperience() * player.getAuraManager().getPrayerMultiplier();
					if (bone == Bone.LAVA_DRAGON && isLavaDragonIsle(player))
						xp *= 4;
					
					player.getSkills().addXp(Skills.PRAYER, xp);
					Double lastPrayer = (Double) player.getTemporaryAttributtes().get("current_prayer_xp");
					if (lastPrayer == null) {
						lastPrayer = 0.0;
					}
					double total = xp + lastPrayer;
					int amount = (int) (total / 500);
					if (amount != 0) {
						double restore = player.getAuraManager().getPrayerRestoration() * (player.getSkills().getLevelForXp(Skills.PRAYER) * 10);
						player.getPrayer().restorePrayer((int) (amount * restore));
						total -= amount * 500;
					}
					restorePrayer(player, bone);
					player.getTemporaryAttributtes().put("current_prayer_xp", total);
					stop();
				}

			});
		}
	}
	
	public static void restorePrayer(Player player, Bone bone) {
		if (!bone.ash) {
			int mapID = player.getRegionId();
			int neckID = player.getEquipment().getAmuletId();
			int neckTier = neckID == 52111 || neckID == 52986 ? 2 : neckID >= 19886 && neckID <= 19888 ? (neckID - 19886) : 	
				(mapID == 6556 || mapID == 6557 || mapID == 6812 || mapID == 6813) ? 0 :
				-1;
			if (neckTier >= 0) {
				int boneTier = bone.ordinal() >= Bone.DRAGON.ordinal() ? 2 : bone.ordinal() >= Bone.BIG.ordinal() ? 1 : 0;
				player.getPrayer().restorePrayer(neckID == 52111 || neckID == 52986 ? (10 + (20*(Math.min(neckTier, boneTier)))): (10 * (Math.min(neckTier, boneTier)+1)));
			}
		}
	}
	
	public static boolean isLavaDragonIsle(Player player) {
		return player.getX() >= 3163 && player.getY() <= 3870 && player.getX() <= 3246 && player.getY() >= 3798;
	}
}
