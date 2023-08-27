package com.rs.game.player.content;

import java.util.HashMap;
import java.util.Map;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.controllers.DungeonController;
import com.rs.utils.Utils;

/**
 * 
 * @author Cjay0091
 * 
 */
public class Consumables {

	public static enum Food {

		/**
		 * Fish
		 */
		CRAFISH(13433, 2),

		ANCHOVIE(319, 1),

		CAVIAR(11326, 3),

		ROE(11324, 3),
		
		SHRIMP(315, 3),

		KARAMBWANJI(3151, 3),

		KEBAB(1971, 3),
		
		SARDINE(325, 3),

		BAGUETTE(6961, 6),
		
		STRAWBERRY(5504, 6),
		
		
		POISON_KARAMBWANJI(3146, 0, Effect.POISION_KARMAMWANNJI_EFFECT),

		KARAMBWANI(3144, 18),

		PURPLE_SWEETS(10476, 3, Effect.PURPLE_SWEET),

		BISCUITS(19467, 3, Effect.BISCUITS),

		SLIMY_EEL(3381, 7 + Utils.random(2)),

		RAINBOW_FISH(10136, 11),

		CAVE_EEL(5003, 8 + Utils.random(2)),

		LAVA_EEL(2149, 7 + Utils.random(2)),

		HERRING(347, 5),

		MACKEREL(355, 6),

		TROUT(333, 7),

		COD(339, 7),

		PIKE(351, 8),

		SALMON(329, 9),

		FIELD_RATION(7934, 10),
		
		TUNA(361, 10),

		LOBSTER(379, 12),

		BASS(365, 13),

		SWORDFISH(373, 14),

		MONKFISH(7946, 16),

		SHARK(385, 20),
		
		FURY_SHARK(20429, 23),

		TURTLE(397, 21),

		MANTA(391, 22),

		CAVEFISH(15266, 20),

		ROCKTAIL(15272, 23, 0, null, 10),
		
		ROCKTAIL_SPECIAL(25431, 23, 0, null, 10),
		
		DARK_CRAB(41936, 22),
		
		ANGLERFISH(43441, 22, 0, null, 13),
		
		EASTER_EGG(1961, 25),

		/**
		 * Meats
		 */
		CHICKEN(2140, 3),
		
		GREEN_CHICKEN(4291, 3),

		MEAT(2142, 3), // TODO

		RABIT(3228, 5),

		ROAST_RABIT(7223, 7),

		ROASTED_BIRD_MEAT(9980, 6),

		CRAB_MEAT(7521, 10), // TODO

		ROASTED_BEAST_MEAT(9988, 8),

		CHOMPY(2878, 10),

		JUBBLY(7568, 15),

		OOMILE(2343, 14),

		/**
		 * Pies
		 */
		REDBERRY_PIE_FULL(2325, 5, 2333),

		REDBERRY_PIE_HALF(2333, 5, 2313),

		MEAT_PIE_FULL(2327, 6, 2331),

		MEAT_PIE_HALF(2331, 6, 2313),

		APPLE_PIE_FULL(2323, 7, 2335),

		APPLE_PIE_HALF(2335, 7, 2313),

		GARDEN_PIE_FULL(7178, 6, 7180, Effect.GARDEN_PIE),

		GARDEN_PIE_HALF(7180, 6, 2313, Effect.GARDEN_PIE),

		FISH_PIE_FULL(7188, 6, 7190, Effect.FISH_PIE),

		FISH_PIE_HALF(7188, 6, 2313, Effect.FISH_PIE),

		ADMIRAL_PIE_FULL(7198, 8, 7200, Effect.ADMIRAL_PIE),

		ADMIRAL_PIE_HALF(7200, 8, 2313, Effect.ADMIRAL_PIE),

		WILD_PIE_FULL(7208, 11, 7210, Effect.WILD_PIE),

		WILD_PIE_HALF(7210, 11, 2313, Effect.WILD_PIE),

		SUMMER_PIE_FULL(7218, 11, 7220, Effect.SUMMER_PIE),

		SUMMER_PIE_HALF(7220, 11, 2313, Effect.SUMMER_PIE),

		/**
		 * Stews
		 */

		STEW(2003, 11, 1923),

		SPICY_STEW(7513, 11, 1923, Effect.SPICY_STEW_EFFECT),

		CURRY(2011, 19, 1923),

		/**
		 * Pizzas
		 */
		PLAIN_PIZZA_FULL(2289, 7, 2291),

		PLAIN_PIZZA_HALF(2291, 7),

		MEAT_PIZZA_FULL(2293, 8, 2295),

		MEAT_PIZZA_HALF(2295, 8),

		ANCHOVIE_PIZZA_FULL(2297, 9, 2299),

		ANCHOVIE_PIZZA_HALF(2299, 9),

		PINEAPPLE_PIZZA_FULL(2301, 11, 2303),

		PINEAPPLE_PIZZA_HALF(2303, 11),

		/**
		 * Potato Toppings
		 */
		SPICEY_SAUCE(7072, 2, 1923),

		CHILLI_CON_CARNIE(7062, 14, 1923),

		SCRAMBLED_EGG(7078, 5, 1923),

		EGG_AND_TOMATO(7064, 8, 1923),

		FRIED_ONIONS(7084, 9, 1923),

		MUSHROOM_AND_ONIONS(7066, 11, 1923),

		FRIED_MUSHROOMS(7082, 5, 1923),

		TUNA_AND_CORN(7068, 13, 1923),

		/**
		 * Baked Potato
		 */
		BAKED_POTATO(6701, 4),

		POTATO_WITH_BUTTER(6703, 14),

		CHILLI_POTATO(7054, 14),

		POTATO_WITH_CHEESE(6705, 16),

		EGG_POTATO(7056, 16),

		MUSHROOM_AND_ONION_POTATO(7058, 20),

		TUNA_POTATO(7060, 22),

		/**
		 * Gnome Food
		 */
		TOAD_CRUNCHIES(2217, 8),

		SPICY_CRUNCHIES(2213, 7),

		WORM_CRUNCHIES(2205, 8),

		CHOCOCHIP_CRUNCHIES(9544, 7),

		FRUIT_BATTA(2277, 11),

		TOAD_BATTA(2255, 11),

		WORM_BATTA(2253, 11),

		VEGETABLE_BATTA(2281, 11),

		CHEESE_AND_TOMATO_BATTA(9535, 11),

		WORM_HOLE(2191, 12),

		VEG_BALL(2195, 12),

		PRE_MADE_VEG_BALL(2235, 12),

		TANGLED_TOAD_LEGS(2187, 15),

		CHOCOLATE_BOMB(2185, 15),
		

		CHOCOLATE_BAR(1973, 3),

		/**
		 * Misc
		 */
		
		DWELLBERRIES(2126, 2),
		
		SPINACH_ROLL(1969, 2),

		PUMPKIN(1959, 14, Effect.HALLOWEEN_EVENT),

		CAKE(1891, 4, 1893),

		TWO_THIRDS_CAKE(1893, 4, 1895),

		SLICE_OF_CAKE(1895, 4),

		CHOCOLATE_CAKE(1897, 4, 1899),

		TWO_THIRDS_CHOCOLATE_CAKE(1899, 4, 1901),

		CHOCOLATE_SLICE(1901, 4),

		FISHCAKE(7530, 11),

		BREAD(2309, 5),

		CABBAGE(1965, 1, Effect.CABAGE_MESSAGE),

		ONION(1957, 1, Effect.ONION_MESSAGE),

		EVIL_TURNIP(12134, 1),

		POT_OF_CREAM(2130, 1),

		CHEESE_WHEEL(18789, 2),

		THIN_SNAIL_MEAT(3369, 5 + Utils.random(2)),

		LEAN_SNAIL_MEAT(3371, 8),

		FAT_SNAIL_MEAT(3373, 8 + Utils.random(2)),
		
		BANANA(1963, 2),

		PEACH(6883, 8),

		PYSKK_BAT(50883, 230),


		/**
		 * Dungeoneering Food
		 */

		RAW_CAVE_POTATO(17817, 2, Effect.RAW_CAVE_POTATO),

		CAVE_POTATO(18093, 2),

		GISSEL_POTATO(18095, 6),

		EDICAP_POTATO(18097, 12),

		HEIM_CRAB(18159, 2),

		HEIM_CRAB_POTATO(18099, 5),

		HIEM_CRAB_GISSEL(18119, 8),

		HIEM_CRAB_EDICAP(18139, 14),

		REDEYE(18161, 5),

		REDEYE_POTATO(18101, 8),

		REDEYE_GISSEL(18121, 11),

		REDEYE_EDICAP(18141, 17),

		DUSK_EEL(18163, 7),

		DUSK_EEL_POTATO(18103, 10),

		DUSK_EEL_GISSEL(18123, 13),

		DUSK_EEL_EDICAP(18143, 19),

		GIANT_FLATFISH(18165, 10),

		GIANT_FLATFISH_POTATO(18105, 13),

		GIANT_FLATFISH_GISSEL(18125, 16),

		GIANT_FLATFISH_EDICAP(18145, 22),

		SHORTFINNED_EEL(18167, 12),

		SHORTFINNED_EEL_POTATO(18107, 15),

		SHORTFINNED_EEL_GISSEL(18127, 18),

		SHORTFINNED_EEL_EDICAP(18147, 24),

		WEB_SNIPPER(18169, 15),

		WEB_SNIPPER_EEL_POTATO(18109, 18),

		WEB_SNIPPER_GISSEL(18129, 21),

		WEB_SNIPPER_EDICAP(18149, 27),

		BOULDABASS(18171, 17),

		BOULDABASS_POTATO(18111, 20),

		BOULDABASS_GISSEL(18131, 23),

		BOULDABASS_EDICAP(18151, 29),

		SALVE_EEL(18173, 20),

		SALVE_EEL_POTATO(18113, 23),

		SALVE_EEL_GISSEL(18133, 26),

		SALVE_EEL_EDICAP(18153, 32),

		BLUE_CRAB(18175, 22),

		BLUE_CRAB_POTATO(18115, 25),

		BLUE_CRAB_GISSEL(18135, 28),

		BLUE_CRAB_EDICAP(18155, 34),

		CAVE_MORAY_CRAB(18177, 25),

		CAVE_MORAY_POTATO(18117, 28),

		CAVE_MORAY_GISSEL(18137, 31),

		CAVE_MORAY_EDICAP(18157, 37),

		BANNANA_DUNG(18199, 2),

		BANNANA_DUNG_O(17381, 2);

		/**
		 * The food id
		 */
		private int id;

		/**
		 * The healing health
		 */
		private int heal;

		/**
		 * The new food id if needed
		 */
		private int newId;

		private int extraHP;

		/**
		 * Our effect
		 */
		private Effect effect;

		/**
		 * A map of object ids to foods.
		 */
		private static Map<Integer, Food> foods = new HashMap<Integer, Food>();

		/**
		 * Gets a food by an object id.
		 * 
		 * @param itemId
		 *            The object id.
		 * @return The food, or <code>null</code> if the object is not a food.
		 */
		public static Food forId(int itemId) {
			return foods.get(itemId);
		}

		/**
		 * Populates the tree map.
		 */
		static {
			for (final Food food : Food.values()) {
				foods.put(food.id, food);
			}
		}

		/**
		 * Represents a food being eaten
		 * 
		 * @param id
		 *            The food id
		 * @param health
		 *            The healing health received
		 */
		private Food(int id, int heal) {
			this.id = id;
			this.heal = heal;
		}

		/**
		 * Represents a part of a food item being eaten (example: cake)
		 * 
		 * @param id
		 *            The food id
		 * @param heal
		 *            The heal amount
		 * @param newId
		 *            The new food id
		 */
		private Food(int id, int heal, int newId) {
			this(id, heal, newId, null);
		}

		private Food(int id, int heal, int newId, Effect effect) {
			this(id, heal, newId, effect, 0);
		}

		private Food(int id, int heal, int newId, Effect effect, int extraHP) {
			this.id = id;
			this.heal = heal;
			this.newId = newId;
			this.setEffect(effect);
			this.extraHP = extraHP;
		}

		private Food(int id, int heal, Effect effect) {
			this(id, heal, 0, effect);
		}

		/**
		 * Gets the id.
		 * 
		 * @return The id.
		 */
		public int getId() {
			return id;
		}

		/**
		 * Gets the exp amount.
		 * 
		 * @return The exp amount.
		 */
		public int getHeal() {
			return heal;
		}

		/**
		 * Gets the new food id
		 * 
		 * @return The new food id.
		 */
		public int getNewId() {
			return newId;
		}

		public int getExtraHP() {
			return extraHP;
		}

		public Effect getEffect() {
			return effect;
		}

		public void setEffect(Effect effect) {
			this.effect = effect;
		}
	}

	public static enum Effect {
		SUMMER_PIE {

			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				int runEnergy = (int) (player.getRunEnergy() * 1.1);
				if (runEnergy > 100)
					runEnergy = 100;
				player.setRunEnergy(runEnergy);
				int level = player.getSkills().getLevel(Skills.AGILITY);
				int realLevel = player.getSkills().getLevelForXp(Skills.AGILITY);
				player.getSkills().set(Skills.AGILITY, level >= realLevel ? realLevel + 5 : level + 5);
			}

		},

		GARDEN_PIE {

			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				int level = player.getSkills().getLevel(Skills.FARMING);
				int realLevel = player.getSkills().getLevelForXp(Skills.FARMING);
				player.getSkills().set(Skills.FARMING, level >= realLevel ? realLevel + 3 : level + 3);
			}

		},

		FISH_PIE {

			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				int level = player.getSkills().getLevel(Skills.FISHING);
				int realLevel = player.getSkills().getLevelForXp(Skills.FISHING);
				player.getSkills().set(Skills.FISHING, level >= realLevel ? realLevel + 3 : level + 3);
			}
		},

		ADMIRAL_PIE {
			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				int level = player.getSkills().getLevel(Skills.FISHING);
				int realLevel = player.getSkills().getLevelForXp(Skills.FISHING);
				player.getSkills().set(Skills.FISHING, level >= realLevel ? realLevel + 5 : level + 5);
			}
		},

		WILD_PIE {
			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				int level = player.getSkills().getLevel(Skills.SLAYER);
				int realLevel = player.getSkills().getLevelForXp(Skills.SLAYER);
				player.getSkills().set(Skills.SLAYER, level >= realLevel ? realLevel + 4 : level + 4);
				int level2 = player.getSkills().getLevel(Skills.RANGE);
				int realLevel2 = player.getSkills().getLevelForXp(Skills.RANGE);
				player.getSkills().set(Skills.RANGE, level2 >= realLevel2 ? realLevel2 + 4 : level2 + 4);
			}
		},

		SPICY_STEW_EFFECT {
			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				if (Utils.random(100) > 5) {
					int level = player.getSkills().getLevel(Skills.COOKING);
					int realLevel = player.getSkills().getLevelForXp(Skills.COOKING);
					player.getSkills().set(Skills.COOKING, level >= realLevel ? realLevel + 6 : level + 6);
				} else {
					int level = player.getSkills().getLevel(Skills.COOKING);
					player.getSkills().set(Skills.COOKING, level <= 6 ? 0 : level - 6);
				}
			}

		},

		CABAGE_MESSAGE {
			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				player.getPackets().sendGameMessage("You don't really like it much.", true);
			}
		},

		ONION_MESSAGE {
			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				player.getPackets().sendGameMessage("It hurts to see a grown " + (player.getAppearence().isMale() ? "male" : "female") + " cry.", true);
			}
		},

		POISION_KARMAMWANNJI_EFFECT {
			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				player.applyHit(new Hit(player, 50, HitLook.POISON_DAMAGE));
			}
		},

		BEER_EFFECT {
			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				player.getPackets().sendGameMessage("You drink the beer. You feel slightly reinvigorated...", true);
				player.getPackets().sendGameMessage("...and slightly dizzy too.", true);
				int level = player.getSkills().getLevel(Skills.ATTACK);
				player.getSkills().set(Skills.ATTACK, level <= 0 ? 0 : level - 2);
				level = player.getSkills().getLevel(Skills.STRENGTH);
				int realLevel = player.getSkills().getLevelForXp(Skills.STRENGTH);
				player.getSkills().set(Skills.STRENGTH, level >= realLevel ? realLevel + 1 : level + 1);
			}
		},

		WINE_EFFECT {
			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				player.getPackets().sendGameMessage("You drink the wine. You feel slightly reinvigorated...", true);
				player.getPackets().sendGameMessage("...and slightly dizzy too.", true);
				player.heal(70);
				int level = player.getSkills().getLevel(Skills.ATTACK);
				player.getSkills().set(Skills.ATTACK, level <= 0 ? 0 : level - 4);
			}
		},
		PURPLE_SWEET {
			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				int newRunEnergy = (int) (player.getRunEnergy() * 1.2);
				player.setRunEnergy(newRunEnergy > 100 ? 100 : newRunEnergy);
			}
		},
		BISCUITS {
			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				player.getPrayer().restorePrayer(10);
			}
		},
		HALLOWEEN_EVENT {
			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				player.getPackets().sendGameMessage("Halloween is over kid.");
				/**
				 * player.transform(DropEvent.HALLOWEEN_NPCS[Utils.random(
				 * DropEvent.HALLOWEEN_NPCS.length)], 500); int randomMessage =
				 * Utils.random(DropEvent.HALLOWEEN_MESSAGES.length);
				 * player.getPackets().sendGameMessage("<col=0000FF>"+DropEvent.
				 * HALLOWEEN_MESSAGES[randomMessage]);
				 */
			}
		},
		RAW_CAVE_POTATO {

			@Override
			public void effect(Object object) {
				Player player = (Player) object;
				player.getPackets().sendGameMessage("You must really be hungry.", true);
			}
		};

		public void effect(Object object) {
		}
	}

	private static final Animation EAT_ANIM = new Animation(829);

	public static boolean eat(final Player player, Item item, int slot) {
		Food food = Food.forId(item.getId());
		if (food == null)
			return false;
		if (player.getFoodDelay() > Utils.currentTimeMillis()
				&& food != Food.KARAMBWANI
				/*|| player.getPotDelay() > Utils.currentTimeMillis()*/)
			return true;
		if (food == Food.KARAMBWANI && player.getKaramDelay() > Utils.currentTimeMillis())
			return true;
		if (!player.getControlerManager().canEat(food))
			return true;
		String name = ItemConfig.forID(food.getId()).getName().toLowerCase();
		player.getPackets().sendGameMessage("You eat the " + name + ".", true);
		player.setNextAnimationNoPriority(EAT_ANIM);
		long foodDelay = name.contains("half") ? 600 : 1800;
		if(food == Food.KARAMBWANI)
			player.addKaramDelay(foodDelay);
		else
			player.addFoodDelay(foodDelay);
		player.getActionManager().addActionDelay(food == Food.KARAMBWANI ? 2 : 3);
		//player.getActionManager().setActionDelay(player.getActionManager().getActionDelay() + 3);
		player.getInventory().getItems().set(slot, food.getNewId() == 0 ? item.getAmount() > 1 ? new Item(item.getId(), item.getAmount() - 1) : null : new Item(food.getNewId(), item.getAmount()));
		player.getInventory().refresh(slot);
		int hp = player.getHitpoints();
		int heal = food.getHeal() * 10;
		if(food == Food.FURY_SHARK && !player.isUnderCombat())
			heal += 50;
		if (player.getControlerManager().getControler() instanceof DungeonController)
			player.applyHit(new Hit(player, heal, HitLook.HEALED_DAMAGE));
		else
			player.heal(heal, food.getExtraHP() * 10);
		if (player.getHitpoints() > hp)
			player.getPackets().sendGameMessage("It heals some health.", true);
		player.getInventory().refresh();
		if (food.getEffect() != null) {
			food.getEffect().effect(player);
		}
		return true;
	}
}