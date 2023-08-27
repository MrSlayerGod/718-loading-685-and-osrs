package com.rs.game.player.content;

import java.util.LinkedList;
import java.util.List;

import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.player.Player;

public class ExtraSettings {
	
	private static interface OptionValue {
		
		public boolean get(Player player);
		
		public void set(Player player);
		
	}
	
	private static final int MAIN = 0, PLUGINS = 1, MESSAGES = 2;
	private static enum Option {
		HITS(MAIN, "X10 Hits/Prayer", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return !player.isOldHitLook();
			}

			@Override
			public void set(Player player) {
				player.switchHitLook();
			}
		
		}),
		OSRS_GAMEFRAME(MAIN, "OSRS Gameframe", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return player.isOsrsGameframe();
			}

			@Override
			public void set(Player player) {
				player.switchGameframe();
			}
		
		}),
		OSRS_HITBARS(MAIN, "OSRS Health Bars", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return player.isOsrsHitbars();
			}

			@Override
			public void set(Player player) {
				player.switchOsrsHitbars();
			}
		
		}),
		ITEMS(MAIN, "OSRS Items Appearence", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return player.isOldItemsLook();
			}

			@Override
			public void set(Player player) {
				player.switchItemsLook();
			}
		
		}),
		NPCS(MAIN, "OSRS Bosses Appearence", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return player.isOldNPCLooks();
			}

			@Override
			public void set(Player player) {
				player.switchOldNPCLooks();
			}
		
		}),
		Spells(MAIN, "OSRS Spells", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return player.isOsrsMagicToggle();
			}

			@Override
			public void set(Player player) {
				player.flipOsrsMagicToggle();
			}
		
		}),
		COSMETIC_OVERRIDES(MAIN, "Cosmetic Overrides", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return !player.isDisableCosmeticOverrides();
			}

			@Override
			public void set(Player player) {
				player.switchCosmeticOverrides();
			}
		
		}),
		RIGHT_CLICK_ENTITIES(MAIN, "Hide-attack option", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return player.isHideAttackOption();
			}

			@Override
			public void set(Player player) {
				player.switchHideAttackOption();
			}
		
		}),
		LEVELS(MAIN, "Virtual Levels", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return player.isVirtualLevels();
			}

			@Override
			public void set(Player player) {
				player.switchVirtualLevels();
			}
		
		}),
		HEALTH_P(PLUGINS, "Health Bar Plugin", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return !player.isDisableHealthPlugin();
			}

			@Override
			public void set(Player player) {
				player.switchHealthPlugin();
			}
		
		}),
		POTION_P(PLUGINS, "Potion Timers Plugin", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return !player.isDisablePotionTimersPlugin();
			}

			@Override
			public void set(Player player) {
				player.switchPotionTimersPlugin();
			}
		
		}),
		ITEM_NAMES(PLUGINS, "Ground Item Names Plugin", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return !player.isDisableGroundItemNames();
			}

			@Override
			public void set(Player player) {
				player.switchGroundItemNames();
			}
		
		}),
		/*XP(MAIN, "XP Lock", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return player.isXpLocked();
			}

			@Override
			public void set(Player player) {
				player.setXpLocked(!player.isXpLocked());
			}
		
		}),*/
		NOTIFICATIONS(MESSAGES, "Broadcast", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return player.isNotifications();
			}

			@Override
			public void set(Player player) {
				player.switchNotifications();
			}
		
		}),
		YELL(MESSAGES, "Yell Chat", new OptionValue() {

			@Override
			public boolean get(Player player) {
				return !player.isYellOff();
			}

			@Override
			public void set(Player player) {
				player.setYellOff(!player.isYellOff());
			}
		
		});

		
		;
		
		private String name;
		private OptionValue value;
		private int type;
		private Option(int type, String name, OptionValue value) {
			this.type = type;
			this.name = name;
			this.value = value;
		}
	}
	
	public static void refreshOption(Player player, int index, Option[] options) {
		int c = 154 + index;
		if (options.length <= index) {
			player.getPackets().sendHideIComponent(1297, c, true);
			return;
		}
		Option option = options[index];
		player.getPackets().sendIComponentText(1297, c, option.value == null ? "??" : option.value.get(player) ? "On" : "Off");
		player.getPackets().sendHideIComponent(1297, c, false);
	}
	
	public static void switchOption(Player player, int index) {
		Option[] options = getOptions(player);
		if (options.length <= index)
			return;
		Option option = options[index];
		if (option != null)
			option.value.set(player);
		refreshOption(player, index, options);
	}
	
	public static int getPage(Player player) {
		Integer page = (Integer) player.getTemporaryAttributtes().get(Key.GAME_SETTINGS_PAGE);
		return page == null ? 0 : page;
	}
	
	public static void setPage(Player player, int page) {
		if (page == getPage(player))
			return;
		if (page == 0)
			player.getTemporaryAttributtes().remove(Key.GAME_SETTINGS_PAGE);
		else
			player.getTemporaryAttributtes().put(Key.GAME_SETTINGS_PAGE, page);
		refreshOptions(player);
	}
	
	
	public static Option[] getOptions(Player player) {
		int page = getPage(player);
		List<Option> options = new LinkedList<Option>();
		for (Option option : Option.values())
			if (option.type == page)
				options.add(option);
		return options.toArray(new Option[options.size()]);
		
	}

	public static void open(Player player) {
		player.getInterfaceManager().sendInterface(1297);
		player.getPackets().sendIComponentText(1297, 249, "Game Settings");
		player.getPackets().sendIComponentText(1297, 124, "Option");
		player.getPackets().sendIComponentText(1297, 125, "Value");
		
		player.getPackets().sendIComponentText(1297, 273, "Gameframe");
		player.getPackets().sendIComponentText(1297, 266, "Plugins");
		player.getPackets().sendIComponentText(1297, 259, "Announcements");
		
		for (int i = 165; i <= 174; i++)
			player.getPackets().sendHideIComponent(1297, i, true);
	/*	for (int i = 126; i <= 128; i++)
			player.getPackets().sendHideIComponent(1297, i, true);*///246
		player.getPackets().sendHideIComponent(1297, 241, true); //scroll bar
		
		if (getPage(player) != 0)
			setPage(player, 0);
		else
			refreshOptions(player);
	}
	
	public static void refreshOptions(Player player) {
		Option[] options = getOptions(player);
		for (int i = 0; i < 11; i++) {
			if (options.length <= i) {
				player.getPackets().sendHideIComponent(1297, i == 0 ? 275 : (i == 1 ? 113 : i == 2 ? 112 : (111 + i)), true);
				player.getPackets().sendHideIComponent(1297,i == 0 ? 88 : (143 + i), true);
				player.getPackets().sendHideIComponent(1297, i + 53, true);
			} else {
				player.getPackets().sendIComponentText(1297, i == 0 ? 275 : (i == 1 ? 113 : i == 2 ? 112 : (111 + i)), "Switch");
				player.getPackets().sendIComponentText(1297, i == 0 ? 88 : (143 + i), options[i].name);
				
				player.getPackets().sendHideIComponent(1297, i == 0 ? 275 : (i == 1 ? 113 : i == 2 ? 112 : (111 + i)), false);
				player.getPackets().sendHideIComponent(1297,i == 0 ? 88 : (143 + i), false);
				player.getPackets().sendHideIComponent(1297, i + 53, false);
			}
			refreshOption(player, i, options);
		}
	}
}
