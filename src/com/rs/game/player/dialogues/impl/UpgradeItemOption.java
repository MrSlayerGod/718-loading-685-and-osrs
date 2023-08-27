package com.rs.game.player.dialogues.impl;

import com.rs.cache.loaders.ItemConfig;
import com.rs.discord.Bot;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ButtonHandler;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Encrypt;
import com.rs.utils.Utils;

import java.util.Arrays;

public class UpgradeItemOption extends Dialogue {

	public static final int GEM_ID = 25523;

	public static int INTERFACE_ID = 3072;

	public static void init() {
		ObjectHandler.register(133114, 1, (Player player, WorldObject obj)
				-> openInterface(player, 0));
		ButtonHandler.register(INTERFACE_ID, 25, 1, (Player player, int s, int s2, int action)
				-> openInterface(player, 0));
		ButtonHandler.register(INTERFACE_ID, 30, 1, (Player player, int s, int s2, int action)
				-> openInterface(player, 1));
		ButtonHandler.register(INTERFACE_ID, 35, 1, (Player player, int s, int s2, int action)
				-> openInterface(player, 2));
		ButtonHandler.register(INTERFACE_ID, 40, 1, (Player player, int s, int s2, int action)
				-> openInterface(player, 3));
	}

	public static void openInterface(Player player, int catId) {
		player.getInterfaceManager().sendInterface(INTERFACE_ID);
		//player.getPackets().sendHideIComponent(INTERFACE_ID, component, hidden);
		player.getPackets().sendIComponentText(INTERFACE_ID, 29, (catId == 0 ? "<col=ff981f>" : "") + "Weapons");
		player.getPackets().sendIComponentText(INTERFACE_ID, 34, (catId == 1 ? "<col=ff981f>" : "") + "GWD");
		player.getPackets().sendIComponentText(INTERFACE_ID, 39, (catId == 2 ? "<col=ff981f>" : "") + "Nex");
		player.getPackets().sendIComponentText(INTERFACE_ID, 44, (catId == 3 ? "<col=ff981f>" : "") + "Callus");
		player.getPackets().sendHideIComponent(INTERFACE_ID, 45, true);
		//player.getPackets().sendIComponentText(INTERFACE_ID, 49, "");
		player.getPackets().sendHideIComponent(INTERFACE_ID, 50, true);
		player.getPackets().sendHideIComponent(INTERFACE_ID, 40, true);
		//player.getPackets().sendIComponentText(INTERFACE_ID, 54, "");

		UpgradeCat cat = UpgradeCat.values()[catId];
		int idx = 0;
		for(int c = 67; c < 485; c += 14) {
			Upgrade u = cat.getUpgrades().length > idx ? cat.getUpgrades()[idx] : null;
			player.getPackets().sendHideIComponent(INTERFACE_ID, c+1, u == null);
			if(u != null) {
				player.getPackets().sendItemOnIComponent(INTERFACE_ID, c+8, u.from, 1);
				player.getPackets().sendItemOnIComponent(INTERFACE_ID, c+9, u.to, 1);
				player.getPackets().sendIComponentText(INTERFACE_ID, c+10, Utils.splitString(ItemConfig.forID(u.from).name, 20));
				player.getPackets().sendIComponentText(INTERFACE_ID, c+11, Utils.splitString(ItemConfig.forID(u.to).name, 20));
				player.getPackets().sendHideIComponent(INTERFACE_ID, c+12, false);
				int perc = (int)(100d/(double)u.getChance() * (u.isProperRate() ? 1 : 1.5d));
				player.getPackets().sendIComponentText(INTERFACE_ID, c+14, perc + "%");
				//player.sendMessage("perc  " + c + " = " + (perc) + " " + (c + 14));
			}
			idx++;
		}
		//player.setCloseInterfacesEvent(() -> {
		//});
	}

    public static void checkAllUpgrades(Player player) {
		for (Item item : player.getInventory().getItems().getItems()) {
			if (item != null) {
				Upgrade upgrade = UpgradeItemOption.getUpgrade(item);
				if (upgrade != null) {
					player.getDialogueManager().startDialogue("UpgradeItemOption", upgrade, false);
					return;
				}
			}
		}
		player.getPackets().sendGameMessage("You don't have any item to upgrade.");
    }

	public enum UpgradeCat {
		Weapons(Upgrade.TOB_RAPIER, /*Upgrade.LIGHT_RAPIER,*/ /*Upgrade.TEMPLAR_MACE,*/ Upgrade.BLOWPIPE_CHARGED,
				/*Upgrade.TWISTED_BOW,*/ Upgrade.AGS, Upgrade.BGS, Upgrade.SGS, Upgrade.ZGS),

		GWD_Armour(
				Upgrade.B1, Upgrade.B2, Upgrade.B3, Upgrade.B4,
				Upgrade.A1, Upgrade.A2, Upgrade.A3, Upgrade.A4,
				Upgrade.S1, Upgrade.S2, Upgrade.S3, Upgrade.S4,
				Upgrade.AGS, Upgrade.BGS, Upgrade.SGS, Upgrade.ZGS),

		Nex(Upgrade.T1, Upgrade.T2, Upgrade.T3, Upgrade.T4,
				Upgrade.P1, Upgrade.P2, Upgrade.P3, Upgrade.P4,
				Upgrade.V1, Upgrade.V2, Upgrade.V3, Upgrade.V4),
;
		/*Callus_Armour(Upgrade.CATALYST_HAT,
				Upgrade.CATALYST_ROBE_TOP,
				Upgrade.CATALYST_ROBE_BOTTOM,
				Upgrade.CATALYST_GLOVES,
				Upgrade.CATALYST_BOOTS);*/

		UpgradeCat(Upgrade... upgrades) {
			this.upgrades = upgrades;
		}

		public Upgrade[] getUpgrades() {
			return upgrades;
		}

		Upgrade[] upgrades;
	}

	public static enum Upgrade {
        TOB_RAPIER(52324, 25504, 3, 2, 125000, null, false),
      //  LIGHT_RAPIER(25504, 25529, 3, 4, 185000, null, false),
        //TEMPLAR_MACE(25588, 25589, 3, 4, 185000, null, false),
        BLOWPIPE_CHARGED(42926, 25502, 8, 2, 96500, null, false),
		BLOWPIPE_UNCHARGED(42924, 25502, 8, 2, 96500, null, false),
       // TWISTED_BOW(50997, 25533, 3, 4, 185000, null, false),

        B1(11724, 25505, 6, 1, 75000, null, false),
        B2(11726, 25506, 6, 1, 75000, null, false),
        B3(25022, 25507, 6, 1, 55000, null, false),
        B4(25025, 25508, 6, 1, 33000, null, false),
        B5(11728, 25509, 6, 1, 33000, null, false),
        B6(25019, 25510, 6, 1, 33000, null, false),
        A1(11718, 25511, 6, 1, 55000, null, false),
        A2(11720, 25512, 6, 1, 75000, null, false),
        A3(11722, 25513, 6, 1, 75000, null, false),
        A4(25016, 25514, 6, 1, 33000, null, false),
        A5(25010, 25515, 6, 1, 33000, null, false),
        A6(25013, 25516, 6, 1, 33000, null, false),
        S1(24992, 25517, 6, 1, 55000, null, false),
        S2(24995, 25518, 6, 1, 75000, null, false),
        S3(24998, 25519, 6, 1, 75000, null, false),
        S4(25001, 25520, 6, 1, 33000, null, false),
        S5(25004, 25521, 6, 1, 33000, null, false),
        S6(25007, 25522, 6, 1, 33000, null, false),

        T1(20135, 25573, 6, 1, 92500, null, false),
        T2(20139, 25572, 6, 1, 111000, null, false),
        T3(20143, 25570, 6, 1, 111000, null, false),
        T4(24977, 25569, 6, 1, 55000, null, false),
        T5(24983, 25568, 6, 1, 55000, null, false),
        P1(20147, 25567, 6, 1, 92500, null, false),
        P2(20151, 25566, 6, 1, 111000, null, false),
        P3(20155, 25564, 6, 1, 111000, null, false),
        P4(24974, 25563, 6, 1, 55000, null, false),
        P5(24989, 25562, 6, 1, 55000, null, false),
        V1(20159, 25561, 6, 1, 92500, null, false),
        V2(20163, 25560, 6, 1, 111000, null, false),
        V3(20167, 25558, 6, 1, 111000, null, false),
        V4(24980, 25557, 6, 1, 55000, null, false),
        V5(24986, 25556, 6, 1, 55000, null, false),
        AGS(11694, 25526, 30, 1, 33000, null, false),
        BGS(11696, 25526, 30, 1, 33000, null, false),
        SGS(11698, 25526, 30, 1, 33000, null, false),
        ZGS(11700, 25526, 30, 1, 33000, null, false);
		/*CATALYST_HAT (51018, 44702, 6, 1, 50000, null, true),
		CATALYST_ROBE_TOP (51021, 25695, 6, 1, 75000, null, true),
		CATALYST_ROBE_BOTTOM (51024, 25696, 6, 1, 75000, null, true),
		CATALYST_GLOVES (6922, 25697, 6, 1, 30000, new Item[]{new Item(49544, 1)}, true),
		CATALYST_BOOTS (6920, 25698, 8, 1, 15000, new Item[]{new Item(21793, 1)}, true);*/

		private int from, to, chance, gem, fragments;
		private Item requiredItems[] = null;
		private boolean proper;

		private Upgrade(int from, int to, int chance, int gem, int fragments, Item[] requiredItems, boolean properRate) {
			this.from = from;
			this.to = to;
			this.chance = chance;
			this.gem = gem;
			this.fragments = fragments;
			this.requiredItems = requiredItems;
			this.proper = properRate;
		}

		public int getFrom() {
			return from;
		}

		public int getTo() {
			return to;
		}

		public int getChance() {
			return chance;
		}

		public boolean isProperRate() {
			return proper;
		}
	}
	
	
	public static Upgrade getUpgrade(Item item) {
		for (Upgrade u : Upgrade.values())
			if (u.from == item.getId())
				return u;
		return null;
	}

	private Upgrade upgrade;
	private  boolean gem;
	
	@Override
	public void start() {
		upgrade = (Upgrade) this.parameters[0];
		gem = (boolean) parameters[1];

		if(upgrade.requiredItems != null) {
			for (Item req : upgrade.requiredItems) {
				if (!player.getInventory().containsItem(req)) {
					player.sendMessage("You must infuse " + req.getAmount() + " x " + req.getName() + " to upgrade this item.");
					return;
				}
			}
		}
		
		player.getInterfaceManager().sendChatBoxInterface(1183);
		player.getPackets().sendIComponentText(1183, 7, ItemConfig.forID(upgrade.from).getName()+" - > "+ItemConfig.forID(upgrade.to).getName());
		player.getPackets().sendIComponentText(1183, 12, gem ? "<col=FF0040>This requires "+upgrade.gem+" gem to upgrade!" : "<col=FF0040>If you fail to upgrade this item it will be destroyed!");
		player.getPackets().sendItemOnIComponent(1183, 13, upgrade.to, 1);
		player.getPackets().sendIComponentText(1183, 22, "Are you sure you want to upgrade this item?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 1) {
			end();
			return;
		}
		if (interfaceId == 1183 && componentId == 9) {
			if (gem && !player.getInventory().containsItem(GEM_ID, upgrade.gem)) {
				player.getPackets().sendGameMessage("You don't have enough upgrade gems to upgrade this item!");
				return;
			}

			stage = 1;
			if (!gem)
				player.setNextAnimation(new Animation(536));
			player.getPackets().sendGameMessage("<col=FF0040>Upgrading...");
			sendDialogue("<col=FF0040>Upgrading...");
			player.lock();
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					player.lock(2);
					player.getInventory().deleteItem(upgrade.from, 1);

					if(upgrade.requiredItems != null) {
						Arrays.stream(upgrade.requiredItems).forEach(item -> player.getInventory().deleteItem(item));
					}

					player.increaseUpgradeAttemptCount(upgrade.to);
					String n = new Item(upgrade.to).getName();
					if (gem)
						player.getInventory().deleteItem(GEM_ID, upgrade.gem);
					if (!gem && Utils.random(upgrade.chance) != 0) {
						player.getInventory().addItem(25587, upgrade.fragments);
						player.getCharges().degradeCompletly(new Item(upgrade.from));
						player.getPackets().sendSound(4500, 0, 1);
						player.getPackets().sendGameMessage("<col=FF0040>FAILED!");
						sendDialogue("<col=FF0040>FAILED!");
						Bot.sendLog(Bot.ALL_CHANNEL, "[type=UPGRADE FAIL][name="+player.getUsername()+"]"+"[upgrade=" + n +"] " + " [QF:" + Encrypt.encryptSHA1(n + "upgfail") + "]");
					 player.setNextAnimation(new Animation(860));
					} else {
						if(upgrade == Upgrade.BLOWPIPE_CHARGED) {
							int charges = player.getCharges().getCharges(Upgrade.BLOWPIPE_CHARGED.from);
							if(charges > 0)
								player.getInventory().addItemDrop(42934, charges);
							if(player.getBlowpipeDarts() != null && player.getBlowpipeDarts().getAmount() > 0) {
								player.getInventory().addItemDrop(player.getBlowpipeDarts().getId(), player.getBlowpipeDarts().getAmount());
							}
							player.setBlowpipeDarts(null);
							player.getCharges().resetCharges(Upgrade.BLOWPIPE_CHARGED.from);
						}
						player.getInventory().addItem(upgrade.to, 1);
						player.getPackets().sendSound(4501, 0, 1);
						player.getPackets().sendGameMessage("<col=00FF00>SUCCESS!");
						sendDialogue("<col=00FF00>SUCCESS!");
						player.setNextAnimation(new Animation(862));
						World.sendNews(player, player.getDisplayName() + " has succeeded in upgrading <col=ffff00>" + ItemConfig.forID(upgrade.to).getName() + "<col=ff8c38>!", 1);
						player.setUgradedItem(true);
						if(gem)
							Bot.sendLog(Bot.ALL_CHANNEL, "[type=UPGRADE GEM UPGRADE][name="+player.getUsername()+"]"+"[upgrade=" + n +"] " + " [QF:" + Encrypt.encryptSHA1(n + "upgsucc") + "]");
						else
							Bot.sendLog(Bot.ALL_CHANNEL, "[type=UPGRADE SUCCESS][name="+player.getUsername()+"]"+"[upgrade=" + n +"] " + " [QF:" + Encrypt.encryptSHA1(n + "upgsucc") + "]");

					}
				}
				
			}, 4);
		} else
			end();
	}

	@Override
	public void finish() {

	}

}
