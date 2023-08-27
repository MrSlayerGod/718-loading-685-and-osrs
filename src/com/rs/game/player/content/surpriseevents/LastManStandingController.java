package com.rs.game.player.content.surpriseevents;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.Consumables.Food;
import com.rs.game.player.content.Drinkables.Drink;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class LastManStandingController extends Controller {

	private transient LastManStandingSurpriseEvent game;
	private transient boolean spectator;
	private transient Player[] hint_targets;

	@Override
	public void start() {
		game = (LastManStandingSurpriseEvent) getArguments()[0];
		hint_targets = new Player[7];
		setArguments(null);
		sendInterfaces();
	}

	@Override
	public boolean logout() {
		player.setLocation(Settings.START_PLAYER_LOCATION);
		player.getControlerManager().forceStop();
		return true;
	}

	
	@Override
	public void sendInterfaces() {
		player.getInterfaceManager().setOverlay(730, false);
		player.setLargeSceneView(true);
	}

	@Override
	public void process() {
		if (player.isFrozen() && (player.getFreezeDelay() - Utils.currentTimeMillis()) <= 15000) {
			player.setFreezeDelay(0);
			player.addFrozenBlockedDelay(4000);
		}
		
		
		if (game == null || (getPlayer().getX() < game.getArena().minX() || getPlayer().getX() > game.getArena().maxX() || player.getY() < game.getArena().minY() || player.getY() > game.getArena().maxY())) {
			getPlayer().getPackets().sendGameMessage("An error has occured, please submit bug report.");
			player.getControlerManager().forceStop();
			return;
		}
		
		synchronized (game.getLock()) {
			player.getPackets().sendHideIComponent(730, 18, true);
			player.getPackets().sendIComponentText(730, 17, "<col=FF0000>Last man standing</col>");
			player.getPackets().sendIComponentText(730, 26, "Alive players:" + (game.getPhase() == 2 ? game.getPlayers().size() : game.getAlive().size()));
		}
		
		if (game.getPhase() == 2) {
			long minsleft = Math.max(0, (game.startTime() - Utils.currentTimeMillis()) / (1000 * 60));
			player.getPackets().sendIComponentText(730, 7, "Event starts in: " + (minsleft < 1 ? "<lt>1 min." : (minsleft + " min.")));
			player.getVarsManager().sendVar(1215, 0);
			

		}
		else if (game.getPhase() == 3) {
			player.getPackets().sendHideIComponent(730, 7, true);
			double percent = 1d - ((double)Math.max(0, game.endTime() - Utils.currentTimeMillis()) / (double)(LastManStandingSurpriseEvent.GAME_MINS * 60 * 1000));
			player.getVarsManager().sendVar(1215, (int)(percent * 1000d));
			
			synchronized (game.getLock()) {
				for (int i = 0; i < hint_targets.length; i++) {
					if (hint_targets[i] != null && !game.getAlive().contains(hint_targets[i])) {
						hint_targets[i] = null;
						player.getHintIconsManager().removeHintIcon(i);
					}
				}
				
				main: for (Player pl : game.getAlive()) {
					if (pl == player || Utils.random(2) == 0)
						continue;
					
					int free_slot = -1;
					for (int i = 0; i < hint_targets.length; i++) {
						if (free_slot == -1 && hint_targets[i] == null)
							free_slot = i;
						
						if (hint_targets[i] == pl)
							continue main;
					}
					
					if (free_slot == -1)
						break;
					
					player.getHintIconsManager().addHintIcon(free_slot, hint_targets[free_slot] = pl, 9, -1, true);
				}
			}
		}
	}


	@Override
	public boolean canAttack(Entity target) {
		if (!(target instanceof Player))
			return true;
		if (game == null || game.getPhase() != 3 || spectator)
			return false;
		synchronized (game.getLock()) {
			if (!game.getAlive().contains(target)) {
				player.getPackets().sendGameMessage("You cannot attack this player.");
				return false;
			}
		}
		return true;
	}
	
	public boolean canHit(Entity target) {
		if (!(target instanceof Player))
			return true;
		if (game == null || game.getPhase() != 3 || spectator)
			return false;
		synchronized (game.getLock()) {
			if (!game.getAlive().contains(target))
				return false;
		}
		return true;
	}

	@Override
	public boolean canPlayerOption2(Player target) {
		return false;
	}
	
	@Override
	public boolean canPlayerOption3(Player target) {
		return false;
	}
	
	@Override
	public boolean canPlayerOption4(Player target) {
		return false;
	}

	@Override
	public boolean processMagicTeleport(WorldTile tile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
		return false;
	}
	

	public boolean processItemTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
		return false;
	}

	public boolean processObjectTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
		return false;
	}

	@Override
	public void magicTeleported(int type) {
		player.getControlerManager().forceStop();
	}
	
	
	@Override
	public void processIngoingHit(final Hit hit) {
		if (hit.getSource() != null && hit.getSource() != null && player.isFrozen() && (hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MAGIC_DAMAGE)) {
			final int reflectdmg = Math.max(10, Math.min((int)((double)hit.getDamage() * 0.6d), 350));
			if (Utils.getDistance(player, hit.getSource()) >= 2) {
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						hit.getSource().applyHit(new Hit(player, reflectdmg, HitLook.REFLECTED_DAMAGE));
					}
				});
			}		
		}
		
		
		if (hit.getDamage() < 100)
			return;
		
		if (hit.getLook() == HitLook.MELEE_DAMAGE && player.getPrayer().isMeleeProtecting()) {
			player.getPrayer().closeProtectionPrayer(player.getPrayer().isAncientCurses() ? 9 : 19);
			player.getPackets().sendGameMessage("Your god stops protecting you from melee.");
		}
		else if (hit.getLook() == HitLook.RANGE_DAMAGE && player.getPrayer().isRangeProtecting()) {
			player.getPrayer().closeProtectionPrayer(player.getPrayer().isAncientCurses() ? 8 : 18);
			player.getPackets().sendGameMessage("Your god stops protecting you from ranged.");
		}
		else if (hit.getLook() == HitLook.MAGIC_DAMAGE && player.getPrayer().isMageProtecting()) {
			player.getPrayer().closeProtectionPrayer(player.getPrayer().isAncientCurses() ? 7 : 17);
			player.getPackets().sendGameMessage("Your god stops protecting you from magic.");
		}
	}
	
	public boolean canSummonFamiliar() {
		player.getPackets().sendGameMessage("Familiars not allowed in this event.");
		return false;
	}
	
	private transient long last_eat = 0;
	private transient int eat_amt = 0;
	
	public boolean canEat(Food food) {
		if ((Utils.currentTimeMillis() - last_eat) > 10000)
			eat_amt = 0;
		
		eat_amt += food.getHeal() * 10;
		if (eat_amt > (int)((double)player.getMaxHitpoints() * 1.1d)) {
			player.getPackets().sendGameMessage("You ate too much already, wait before eating more.");
			return false;
		}
		
		last_eat = Utils.currentTimeMillis();
		return true;
	}

	public boolean canPot(Drink pot) {
		if (pot.isFlask()) {
			player.getPackets().sendGameMessage("Flasks not allowed.");
			return false;
		}
		
		if (pot == Drink.SARADOMIN_BREW_POTION) {
			player.getPackets().sendGameMessage("Saradomin brew not allowed.");
			return false;
		}
		return true;
	}


	@Override
	public boolean sendDeath() {
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
					player.getPackets().sendGameMessage("Oh dear, you have died! Entering spectator mode...");
				} else if (loop == 3) {
					Player killer = player.getMostDamageReceivedSourcePlayer();
					if (killer != null) {
						killer.reduceDamage(player);
						

						boolean target = false;
						Controller controller = killer.getControlerManager().getControler();
						if (controller != null && controller instanceof LastManStandingController) {
							 Player[] targets = ((LastManStandingController)controller).hint_targets;
							 if (targets != null) {
								 for (int i = 0; i < targets.length; i++)
									 if (targets[i] == player) {
										 target = true;
										 break;
									 }
							 }
						}
						
						if (target) {
							killer.getPackets().sendGameMessage("You have killed your target, congratulations!");
							//Helper.dropDeathReward(killer, player, LastManStanding.REWARD_DROP_MOD_PER_KILL, LastManStanding.REWARD_CASH_PER_KILL);
						}
						else {
							killer.getPackets().sendGameMessage("You have scored a kill, however it wasn't your target.");
						}
						
					}
					
					player.stopAll();
					player.reset();
					for (int i = 0; i < hint_targets.length; i++)
						if (hint_targets[i] != null)
							player.getHintIconsManager().addHintIcon(i, hint_targets[i], 9, -1, true);
					player.setNextAnimation(new Animation(-1));			
					player.getAppearence().setHidden(true);
					
					

				} else if (loop == 4) {
					spectator = true;
					player.resetWalkSteps();
					if (game != null) {
						synchronized (game.getLock()) {
							game.getAlive().remove(player);
						}
					}
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public void forceClose() {
		player.getInterfaceManager().removeOverlay(false);
		player.getHintIconsManager().removeAll();
		player.setLargeSceneView(false);
		
		if (game != null)
			game.forceleave(player);
		else {
			player.setCanPvp(false);
			player.stopAll();
			player.reset();
			player.getAppearence().setHidden(false);
			player.getAppearence().setIdentityHide(false);
			player.useStairs(-1, Settings.START_PLAYER_LOCATION, 0, 1);
		}
	}
}
