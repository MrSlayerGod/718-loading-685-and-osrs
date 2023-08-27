/**
 * 
 */
package com.rs.game.player.content;

import java.util.LinkedList;
import java.util.List;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;

/**
 * @author dragonkk(Alex)
 * Nov 6, 2017
 */
public class TeleportMenu {

	
	private static class MenuOption extends Option {
		
		private Option[] options;
		
		private MenuOption(String name, Option... options) {
			super(name);
			this.options = options;
		}
	}
	
	private static class TeleportOption extends Option {
		
		private WorldTile tile;
		
		private TeleportOption(String name, WorldTile tile) {
			super(name);
			this.tile = tile;
		}
	}
	
	private static class Option {
		
		public Option(String name) {
			this.name = name;
		}
		
		private String name;
	}
	
	static {
		MenuOption mainMenu = new MenuOption("Teleports", 
				
				new MenuOption("Combat"
						, new TeleportOption("Cows", new WorldTile(3257, 3267, 0))
						, new TeleportOption("Rock Crabs", new WorldTile(2675, 3712, 0))
						, new TeleportOption("Yaks", new WorldTile(2323, 3794, 0))
						, new TeleportOption("Fountain of Rune (WILD)", new WorldTile(3371, 3893, 0))
						),
				new MenuOption("Skills"
						, new MenuOption("Prayer"
								, new TeleportOption("Ectofuntus", new WorldTile(3659, 3526, 0))
								, new TeleportOption("Green dragons (WILD)", new WorldTile(2978, 3616, 0))
								, new TeleportOption("Lava Dragons (WILD)", new WorldTile(3201, 3852, 0))
								, new TeleportOption("Dungeoneering Altar", new WorldTile(3449, 3718, 0))
								, new TeleportOption("House Altar", new WorldTile(1541, 3602, 0))
								)
						, new MenuOption("Cooking"
								, new TeleportOption("The Rogues' Den", new WorldTile(3029, 4958, 0))
								, new TeleportOption("Cooks' Guild", new WorldTile(3143, 3443, 0))
								)
						, new MenuOption("Woodcutting"
								, new TeleportOption("Normal + Willow trees", new WorldTile(3092, 3228, 0))
								, new TeleportOption("Teak + Mahogany trees", new WorldTile(2824, 3083, 0))
								, new TeleportOption("Maple trees", new WorldTile(2729, 3500, 0))
								, new TeleportOption("Yew trees", new WorldTile(2711, 3463, 0))
								, new TeleportOption("Magic trees", new WorldTile(2702, 3397, 0))
								, new TeleportOption("Ivy", new WorldTile(3233, 3461, 0))
								, new TeleportOption("Resource Area (Wilderness)", new WorldTile(3173, 3935, 0))
								, new TeleportOption("Sawmill", new WorldTile(3309, 3491, 0))
								)
						, new MenuOption("Fishing"
								, new TeleportOption("Shrimps + Sardines", new WorldTile(3093, 3229, 0))
								, new TeleportOption("Trouts + Salmons", new WorldTile(2843, 2969, 0))
								, new TeleportOption("Fishing Guild (Lobsters/Swordfish/Sharks)", new WorldTile(2604, 3416, 0))
								, new TeleportOption("Monkfish", new WorldTile(2311, 3700, 0))
								, new TeleportOption("Dark Crabs (Wilderness)", new WorldTile(3173, 3935, 0))
								, new TeleportOption("Cave Fish + Rocktails", new WorldTile(3656, 5114, 0))
								)
						, new MenuOption("Firemaking"
								, new TeleportOption("Grand Exchange", new WorldTile(3164, 3472, 0))
								, new TeleportOption("Fist of Guthix", new WorldTile(1681, 5600, 0))
								)
						, new MenuOption("Crafting"
								, new TeleportOption("Cow Hide", new WorldTile(3257, 3267, 0))
								, new TeleportOption("Yak Hide", new WorldTile(2323, 3794, 0))
								, new TeleportOption("Green Hide (Wilderness)", new WorldTile(2978, 3616, 0))
								, new TeleportOption("Blue Hide", new WorldTile(2908, 9803, 0))
								, new TeleportOption("Red Hide", new WorldTile(2711, 9514, 0))
								, new TeleportOption("Black Hide", new WorldTile(2834, 9825, 0))
								, new TeleportOption("Royal Hide", new WorldTile(1195, 6504, 0))
								, new TeleportOption("Tanning", new WorldTile(3273, 3197, 0))
								, new TeleportOption("Crafting Guild", new WorldTile(2933, 3292, 0))
								)
						, new TeleportOption("Smithing", new WorldTile(1530, 3598, 0))
						, new MenuOption("Mining"
								, new TeleportOption("Copper + Tin + Iron ore", new WorldTile(3285, 3366, 0))
								, new TeleportOption("Coal ore", new WorldTile(2583, 3480, 0))
								, new TeleportOption("Sandstone + granite", new WorldTile(3170, 2912, 0))
								, new TeleportOption("Mithril + Adamant + Rune ore (Wilderness)", new WorldTile(3173, 3935, 0))
								, new TeleportOption("Coal + Gold (LRC)", new WorldTile(3656, 5114, 0))
								, new TeleportOption("Mining Guild", new WorldTile(3042, 9743, 0))
								)
						, new MenuOption("Herblore"
								, new TeleportOption("Chaos druids (Wilderness)", new WorldTile(3115, 9929, 0))
								, new TeleportOption("Elder Chaos druids (Wilderness)", new WorldTile(3234, 3621, 0))
								)
						, new MenuOption("Agility"
								, new TeleportOption("Gnome normal + advanced course", new WorldTile(2470, 3439, 0))
								, new TeleportOption("Barbarian normal + advanced course", new WorldTile(2552, 3562, 0))
								, new TeleportOption("Wilderness course", new WorldTile(2998, 3915, 0))
								, new TeleportOption("Prifddinas course", new WorldTile(2222, 3357, 0))
								)
						, new MenuOption("Thieving"
								, new TeleportOption("Men Pickpocketing", new WorldTile(3099, 3510, 0))
								, new TeleportOption("Guard + Paladin + Hero Pickpocketing + Stalls", new WorldTile(2661, 3307, 0))
								, new TeleportOption("Elf Pickpocketing", new WorldTile(2332, 3171, 0))
								, new TeleportOption("Sorceress garden", new WorldTile(3322, 3139, 0))
								)
						, new MenuOption("Slayer"
								, new TeleportOption("Turael", new WorldTile(2910, 3422, 0))
								, new TeleportOption("Krystilia (Wilderness)", new WorldTile(3096, 3504, 0))
								, new TeleportOption("Mazchna", new WorldTile(3509, 3509, 0))
								, new TeleportOption("Vannaka", new WorldTile(3145, 9914, 0))
								, new TeleportOption("Chaeldar", new WorldTile(2447, 4431, 0))
								, new TeleportOption("Sumona", new WorldTile(3359, 2993, 0))
								, new TeleportOption("Duradel", new WorldTile(2869, 2982, 1))
								, new TeleportOption("Kuradal", new WorldTile(1739, 5313, 1))
								)
						, new MenuOption("Farming"
								, new TeleportOption("Falador Farm", new WorldTile(3053, 3305, 0))
								, new TeleportOption("Catherbey Farm", new WorldTile(2804, 3464, 0))
								, new TeleportOption("Ardougne Farm", new WorldTile(2669, 3377, 0))
								)
						, new MenuOption("Runecrafting"
								, new TeleportOption("Air Altar", new WorldTile(2846, 4835, 0))
								, new TeleportOption("Body Altar", new WorldTile(2784, 4840, 0))
								, new TeleportOption("Earth Altar", new WorldTile(2660, 4839, 0))
								)
						),
				new MenuOption("Bosses + Dungeons"),
				new MenuOption("Minigames"),
				new MenuOption("Wilderness"),
				new MenuOption("Gambling"),
				new MenuOption("Event")
				);
		
	}
	
	public static void open(Player player) {
		player.getInterfaceManager().sendInterface(1174);
	}
}
