package com.rs.game.minigames.duel;

import com.rs.executor.GameExecutorManager;
import com.rs.game.Entity;
import com.rs.game.TemporaryAtributtes;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.game.player.content.Gambling;
import com.rs.game.player.controllers.Controller;
import com.rs.utils.Logger;

import java.util.TimerTask;

public class DuelControler extends Controller {

	@Override
	public void start() {
		sendInterfaces();
		player.getPackets().sendPlayerOption("Duel-Challenge", 1, false);
		player.getPackets().sendPlayerOption("Gamble-Challenge", 3, false);

	}

	@Override
	public boolean login() {
		start();
		return false;
	}


	@Override
	public boolean logout() {
		return false;
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (interfaceId == Gambling.INTERFACE) {
			if (player.getGamblingSession() != null)
				player.getGamblingSession().handleButtons(player, componentId);
			return false;
		}
		return true;
	}

	@Override
	public void forceClose() {
		remove();
	}

	@Override
	public void magicTeleported(int type) {
		removeControler();
		remove();
	}

	@Override
	public void moved() {
		if (!isAtDuelArena(player)) {
			removeControler();
			remove();
		}
	}


	@Override
	public boolean canAttack(Entity target) {
		if (target instanceof Player || target instanceof Familiar)
			return false;
		return true;
	}

	@Override
	public boolean canPlayerOption5(final Player target) {
		player.stopAll();
		if (target.getInterfaceManager().containsScreenInter() || target.isLocked()) {
			player.getPackets().sendGameMessage("The other player is busy.");
			return false;
		}
		if (player.isBeginningAccount() || target.isBeginningAccount()) {
			player.getPackets().sendGameMessage("Starter accounts cannot duel or be dueled until after at least one hour of playing time.");
			return false;
		}
		if (!player.getBank().hasVerified(10))
			return false;
		if (player.getGamblingSession() != null || target.getGamblingSession() != null)
			return false;
		player.getTemporaryAttributtes().put(TemporaryAtributtes.Key.GAMBLING, target);
		GameExecutorManager.fastExecutor.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					player.getPackets().sendInputIntegerScript("Enter coins amount you wish to gamble: ");
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}

		}, 600);
		return false;
	}

	@Override
	public boolean canPlayerOption1(final Player target) {
		player.stopAll();
		if (target.getInterfaceManager().containsScreenInter() || target.isLocked()) {
			player.getPackets().sendGameMessage("The other player is busy.");
			return false;
		}
		if (player.isBeginningAccount() || target.isBeginningAccount()) {
			player.getPackets().sendGameMessage("Starter accounts cannot duel or be dueled until after at least one hour of playing time.");
			return false;
		}
		if (!player.getBank().hasVerified(10))
			return false;
		if (player.getGamblingSession() != null || target.getGamblingSession() != null)
			return false;
		if (target.getTemporaryAttributtes().get(TemporaryAtributtes.Key.GAMBLING_AMOUNT) != null && target.getTemporaryAttributtes().get(TemporaryAtributtes.Key.GAMBLING) == player) {
			if ((player.isIronman() || player.isUltimateIronman() || player.isHCIronman())) {
				player.getPackets().sendGameMessage("You can't stack duel as an ironman.");
				return false;
			}
			if (target.isIronman() || target.isUltimateIronman() || player.isHCIronman()) {
				player.getPackets().sendGameMessage("You can't stake against an ironman.");
				return false;
			}

			target.getTemporaryAttributtes().remove(TemporaryAtributtes.Key.GAMBLING);
			Gambling.start(player, target, (Integer) target.getTemporaryAttributtes().remove(TemporaryAtributtes.Key.GAMBLING_AMOUNT));
			return false;
		}
		if (target.getTemporaryAttributtes().get("DuelChallenged") == player) {
			
			Boolean friendly = (Boolean) target.getTemporaryAttributtes().get("DuelFriendly");
			if (friendly == null)
				friendly = false;
			
			if (!friendly) {
				if ((player.isIronman() || player.isUltimateIronman() || player.isHCIronman())) {
					player.getPackets().sendGameMessage("You can't stack duel as an ironman.");
					return false;
				}
				if (target.isIronman() || target.isUltimateIronman() || player.isHCIronman()) {
					player.getPackets().sendGameMessage("You can't stake against an ironman.");
					return false;
				}
				/*if (player.isExtreme() != target.isExtreme()) {
					player.getPackets().sendGameMessage("Both users need to be extreme in order to duel.");
					return false;
				}*/
			}
			
			player.getControlerManager().removeControlerWithoutCheck();
			target.getControlerManager().removeControlerWithoutCheck();
			target.getTemporaryAttributtes().remove("DuelChallenged");
			player.setLastDuelRules(new DuelRules(player, target));
			target.setLastDuelRules(new DuelRules(target, player));
			player.getControlerManager().startControler("DuelArena", target, friendly);
			target.getControlerManager().startControler("DuelArena", player, friendly);
			return false;
		}
		player.getTemporaryAttributtes().put("DuelTarget", target);
		player.getInterfaceManager().sendInterface(640);
		player.getTemporaryAttributtes().put("WillDuelFriendly", true);
		player.getVarsManager().sendVar(283, 67108864);
		return false;
	}

	public static void challenge(Player player) {
		player.closeInterfaces();
		Boolean friendly = (Boolean) player.getTemporaryAttributtes().remove("WillDuelFriendly");
		if (friendly == null)
			return;
		Player target = (Player) player.getTemporaryAttributtes().remove("DuelTarget");
		if (target == null || target.hasFinished() || !target.withinDistance(player, 14) || !(target.getControlerManager().getControler() instanceof DuelControler)) {
			player.getPackets().sendGameMessage("Unable to find " + (target == null ? "your target" : target.getDisplayName()));
			return;
		}
		player.getTemporaryAttributtes().put("DuelChallenged", target);
		player.getTemporaryAttributtes().put("DuelFriendly", friendly);
		player.getPackets().sendGameMessage("Sending " + target.getDisplayName() + " a request...");
		target.getPackets().sendDuelChallengeRequestMessage(player, friendly);
	}

	public void remove() {
		player.getInterfaceManager().removeOverlay(false);
		player.getPackets().sendPlayerOption("null", 1, false);
		player.getPackets().sendPlayerOption("null", 3, false);
	}

	@Override
	public void sendInterfaces() {
		player.getInterfaceManager().setOverlay(638, false);
	}

	public static boolean isAtDuelArena(Player player) {
		return player.withinArea(3341, 3265, 3387, 3281)
				|| player.withinArea(2828, 5085, 2866, 5109);
				
			//	|| player.withinArea(3036, 3371, 3055, 3385); //dice room
	}
}
