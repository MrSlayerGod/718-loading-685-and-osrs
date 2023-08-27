package com.rs.game.minigames;

import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.construction.HouseConstants;
import com.rs.net.decoders.WorldPacketsDecoder;

public class Sawmill {

	private static final int SAWMILL_OPERATOR = 4250;
	public static final int OVERSEER = 8904;

	public static enum Plank {
		WOOD(HouseConstants.PLANK, 1511, 100), OAK(HouseConstants.OAK_PLANK, 1521, 250), TEAK(HouseConstants.TEAK_PLANK, 6333, 500), MAHOGANY(HouseConstants.MAHOGANY_PLANK, 6332, 1500);
		private int id, logId, cost;

		private Plank(int id, int logId, int cost) {
			this.id = id;
			this.logId = logId;
			this.cost = cost;
		}

		public int getCost() {
			return cost;
		}

		public int getId() {
			return id;
		}
	}

	public static Plank getPlankForLog(int id) {
		for (Plank plank : Plank.values())
			if (plank.logId == id)
				return plank;
		return null;
	}

	public static Plank getPlank(int id) {
		for (Plank plank : Plank.values())
			if (plank.id == id)
				return plank;
		return null;
	}

	public static boolean hasPlanksOrLogs(Player player) {
		for (Item item : player.getInventory().getItems().getItems()) {
			if (item != null && (getPlankForLog(item.getId()) != null || getPlank(item.getId()) != null))
				return true;
		}
		return false;
	}

	public static void openPlanksConverter(Player player) {
		player.getInterfaceManager().sendInterface(403);
	}

	public static void handlePlanksConvertButtons(Player player, int componentId, int packetId) {
		if (componentId >= 12 && componentId <= 15) {
			Plank type = Plank.values()[componentId - 12];
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
				player.getTemporaryAttributtes().put("PlanksConvert", type);
				player.getPackets().sendInputIntegerScript("Enter amount:");
				return;
			}
			int amount = packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET ? 1 : packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET ? 5 : packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET ? 10 : player.getInventory().getAmountOf(type.logId);
			convertPlanks(player, type, amount);
		}
	}

	public static void convertPlanks(Player player, Plank type, int amount) {
		int warning = 0;
		int logsAmt = player.getInventory().getAmountOf(type.logId);
		if (amount > logsAmt) {
			amount = logsAmt;
			warning = 1;
		}
		int cost = amount * type.cost;
		long invCoins = player.getInventory().getCoinsAmount();
		if (cost > invCoins) {
			amount = (int) (invCoins / type.cost);
			cost = amount * type.cost;
			warning = 2;
		}
		if (warning != 0)
			player.getPackets().sendGameMessage("You've run out of " + (warning == 1 ? "logs" : "coins") + ".");
		if (amount > 0) {
			player.getInventory().removeItemMoneyPouch(new Item(995, cost));
			player.getInventory().deleteItem(type.logId, amount);
			player.getInventory().addItem(type.id, amount);
		}
		player.closeInterfaces();
		player.getDialogueManager().startDialogue("SimpleNPCMessage", SAWMILL_OPERATOR, "I've done as many as I could.");
	}

	public static void enter(Player player, WorldObject object) {
		if (player.getSkills().getLevelForXp(Skills.WOODCUTTING) < 80) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", OVERSEER, "Sorry, we don't need inexperienced woodcutters.<br>Come back when you're at least 80 Woodcutting.");
			return;
		}
		if (hasPlanksOrLogs(player)) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", OVERSEER, "Sorry, you can't bring any planks or logs in with you. You might get them muddled with ours.");
			return;
		}
		player.lock(2);
		player.addWalkSteps(object.getX() + 1, object.getY(), 1, false);
		player.getControlerManager().startControler("SawmillController");
	}

}
