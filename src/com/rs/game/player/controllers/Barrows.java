package com.rs.game.player.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.rs.cache.Cache;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.BarrowsBrother;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.NPCKillLog;
import com.rs.game.player.content.Shop;
import com.rs.game.player.content.collectionlog.CategoryType;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public final class Barrows extends Controller {

	private static final short[][] TUNNEL_CONFIG =
	{
	{ 470, 479, 482, 476, 474 },
	{ 479, 477, 478, 480, 472 },
	{ 477, 471, 472, 476, 475, 478, 480, 477 } };
	private static final int[] CRYPT_NPCS =
	{ 1243, 1244, 1245, 1246, 1247, 1618, 2031, 2032, 2033, 2034, 2035, 2036, 2037, 4920, 4921, 5381, 5422, 7637 };

	//dont enable, not finished
	private static final boolean ENABLE_AKRISAE = false;
	private BarrowsBrother target;

	// they have to be handled by correct order
	// AHRIM
	// DHAROK
	// GUTHAN
	// KARIL
	// TORAG
	// VERAC
	private static enum Hills {
		//old verac 3578, 9706, 3
		AHRIM_HILL(new WorldTile(3564, 3287, 0), new WorldTile(3557, 9703, 3)),
		DHAROK_HILL(new WorldTile(3573, 3296, 0), new WorldTile(3556, 9718, 3)),
		GUTHAN_HILL(new WorldTile(3574, 3279, 0), new WorldTile(3534, 9704, 3)),
		KARIL_HILL(new WorldTile(3563, 3276, 0), new WorldTile(3546, 9684, 3)),
		TORAG_HILL(new WorldTile(3553, 3281, 0), new WorldTile(3568, 9683, 3)),
		VERAC_HILL(new WorldTile(3556, 3296, 0), ENABLE_AKRISAE ? new WorldTile(4077, 5710, 0) : new WorldTile(3578, 9706, 3));

		private WorldTile outBound;
		private WorldTile inside;

		// out bound since it not a full circle

		private Hills(WorldTile outBound, WorldTile in) {
			this.outBound = outBound;
			inside = in;
		}
	}

	public static boolean digIntoGrave(final Player player) {
		for (Hills hill : Hills.values()) {
			if (player.getPlane() == hill.outBound.getPlane() && player.getX() >= hill.outBound.getX() && player.getY() >= hill.outBound.getY() && player.getX() <= hill.outBound.getX() + 3 && player.getY() <= hill.outBound.getY() + 3) {
				player.useStairs(-1, hill.inside, 1, 2, "You've broken into a crypt.");
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						player.getControlerManager().startControler("Barrows");
					}
				});
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canAttack(Entity target) {
		if (target instanceof BarrowsBrother && target != this.target) {
			player.getPackets().sendGameMessage("This isn't your target.");
			return false;
		}
		return true;
	}

	private void exit(WorldTile outside) {
		player.setNextWorldTile(outside);
		leave(false);
	}

	private void leave(boolean logout) {
		if (target != null) {
			target.finish(); // target also calls removing hint icon at remove
			player.getPackets().sendGameMessage("We'll finish this later.......");
		}
		if (!logout) {
			for (int varBit : TUNNEL_CONFIG[getTunnelIndex()])
				player.getVarsManager().sendVarBit(varBit, 0);
			player.getVarsManager().sendVar(1270, 0);
			player.getPackets().sendBlackOut(0); // unblacks minimap
			if (player.getHiddenBrother() == -1)
				player.getPackets().sendStopCameraShake();
			else
				player.getInterfaceManager().removeOverlay(false);
			removeControler();
		}
	}

	private int getTunnelIndex() {
		if (getArguments() == null || getArguments().length == 0)
			return 0;
		return (int) this.getArguments()[0];
	}

	@Override
	public boolean sendDeath() {
		leave(false);
		player.lock(8);
		player.stopAll();

		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					player.getControlerManager().forceStop();
					player.getControlerManager().startControler("DeathEvent", new WorldTile(3565, 3303, 0), player.hasSkull());
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
		//return true;
	}

	@Override
	public void magicTeleported(int type) {
		leave(false);
	}

	public int getRandomBrother() {
		List<Integer> bros = new ArrayList<Integer>();
		for (int i = 0; i < Hills.values().length; i++) {
			if (player.getKilledBarrowBrothers()[i] || player.getHiddenBrother() == i)
				continue;
			bros.add(i);
		}
		if (bros.isEmpty())
			return -1;
		return bros.get(Utils.random(bros.size()));

	}

	private static final Item[] COMMUM_REWARDS =
	{ new Item(558, 1795 * 2), new Item(562, 773 * 2), new Item(560, 391 * 2), new Item(565, 164 * 2), new Item(4740, 188) };

	@SuppressWarnings("unused")
	// once i add the ring effect
	private static final Item[] RING_OF_WEALTH_REWARDS =
	{ new Item(165, 1), new Item(159, 1), new Item(141, 1), new Item(129, 1), new Item(385, 4) };
	private static final Item[] RARE_REWARDS =
	{ new Item(1149, 1), new Item(987, 1), new Item(985, 1) };
	public static final Item[] BARROW_REWARDS =
	{
		new Item(4708, 1),
		new Item(4710, 1),
		new Item(4712, 1),
		new Item(4714, 1),
		new Item(4716, 1),
		new Item(4718, 1),
		new Item(4720, 1),
		new Item(4722, 1),
		new Item(4724, 1),
		new Item(4726, 1),
		new Item(4728, 1),
		new Item(4730, 1),
		new Item(4732, 1),
		new Item(4734, 1),
		new Item(4736, 1),
		new Item(4738, 1),
		new Item(4745, 1),
		new Item(4747, 1),
		new Item(4749, 1),
		new Item(4751, 1),
		new Item(4753, 1),
		new Item(4755, 1),
		new Item(4757, 1),
		new Item(4759, 1),
		new Item(21736, 1),
		new Item(21744, 1),
		new Item(21752, 1),
		new Item(21760, 1) };

	public void drop(Item item) {
		//not increasing by drop rate since its already easy enough :p
		//new Item(item.getId(), Utils.random(item.getDefinitions().isStackable() ? item.getAmount() * Settings.getDropQuantityRate() : item.getAmount()) + 1);
		player.getInventory().addItemDrop(item.getId(), item.getAmount());

	}
	
	
	public static void main(String[] args) throws IOException {
		Cache.init();
		int money = 0;
		for (int run = 0; run < 1000; run++) {
			double percentage = 7 * 1.5d;
			int count = 7;
			percentage += 2.5; //before too op
			percentage += 0; //10k would increase to 90% lol
			if (percentage > 90)
				percentage = 90;
			
			boolean ringOfWealth = true;
			if (ringOfWealth)
				percentage += 1;
			if (percentage / 2 >= Math.random() * 100) { //1 in 20
				Item item = RARE_REWARDS[Utils.random(RARE_REWARDS.length)];
				money += item.getDefinitions().getValue() * item.getAmount();
			}
			for (int i = 0; i < count; i++)
				if (percentage*3 >= Math.random() * 100) { //boost
					Item item = COMMUM_REWARDS[Utils.random(COMMUM_REWARDS.length)];
					money += item.getDefinitions().getValue() * item.getAmount();
				}
			if (count >= 2)  {
				money += Utils.random(5307 * 10); // money reward at least always, coins boosted by 10

			}
			
			if (percentage >= Math.random() * 100 && count > 3) {
				//reard barrows
				Item item = BARROW_REWARDS[Utils.random(BARROW_REWARDS.length)];
			//	drop(item);
			//	World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + item.getName() + "<col=ff8c38> drop!", 1);
				money += Shop.getBarrowsPrice(item);
			}	
		}
		System.out.println(Utils.getFormattedNumber(money));
	}

	public void sendReward() {
		double percentage = 0;
		int count = 0;
		for (boolean died : player.getKilledBarrowBrothers()) {
			if (died) {
				percentage += 1.75; //before 3% too op
				count++;
			}
		}
		percentage += (double)player.getDonator() / 2; //before too op
		percentage += (player.getBarrowsKillCount() / 100d); //10k would increase to 90% lol
		if (percentage > 90)
			percentage = 90;
		
		boolean ringOfWealth = Combat.hasRingOfWealth(player);
		if (ringOfWealth)
			percentage += 1;
		if (percentage / 2 >= Math.random() * 100) { //1 in 20
			drop(RARE_REWARDS[Utils.random(RARE_REWARDS.length)]);
			player.getPackets().sendGameMessage("<col=ff7000>Your ring of wealth shines more brightly!", true);
		}
		for (int i = 0; i < count; i++)
			if (percentage*3 >= Math.random() * 100) //boost
				drop(COMMUM_REWARDS[Utils.random(COMMUM_REWARDS.length)]);
		if (count >= 2)  {
			drop(new Item(995, Utils.random(5307 * 10)));
			if (count >= 6)
				drop(new Item(25458, 300000));// money reward at least always, coins boosted by 10
			NPCKillLog.addKill(player, "Barrows Chests"); //exception
			if (player.getSlayerManager().getBossTask() != null && player.getSlayerManager().getBossTask().equalsIgnoreCase("Barrows Chests"))
				player.getSlayerManager().addBossKill(count*990);
		}
		
		if (percentage >= Math.random() * 100 && count > 3) {
			//reard barrows
			Item item = BARROW_REWARDS[Utils.random(BARROW_REWARDS.length)];
			drop(item);
			World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + item.getName() + "<col=ff8c38> drop!", 1);
			player.getCollectionLog().add(CategoryType.MINIGAMES, "Barrows", item);
		}
		
	}
	
	

	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() >= 6702 && object.getId() <= 6707) {
			WorldTile out = Hills.values()[object.getId() - 6702].outBound;
			// cant make a perfect middle since 3/ 2 wont make a real integer
			// number or wahtever u call it..
			exit(new WorldTile(out.getX() + 1, out.getY() + 1, out.getPlane()));
			return false;
		} else if (object.getId() == 10284) {
			if (player.getHiddenBrother() == -1) {// reached chest
				player.getPackets().sendGameMessage("You found nothing.");
				return false;
			}
			if (!player.getKilledBarrowBrothers()[player.getHiddenBrother()])
				sendTarget(player.getHiddenBrother() == 6 ? 14297 : 2025 + player.getHiddenBrother(), player);
			if (target != null) {
				player.getPackets().sendGameMessage("You found nothing.");
				return false;
			}
			sendReward();
			player.getPackets().sendCameraShake(3, 12, 25, 12, 25);
			player.getInterfaceManager().removeOverlay(false);
			player.getPackets().sendAddObject(new WorldObject(6775, 10, 0, 3551, 9695, 0));
			player.resetBarrows();
			return false;
		} else if (object.getId() == 6711) {
			player.useStairs(828, new WorldTile(3565, 3312, 0), 1, 2);
			leave(false);
			return false;
		} else if (object.getId() >= 6716 && object.getId() <= 6749) {
			boolean inBetween = player.getTemporaryAttributtes().get("between_barrow_door") != null;
			if (inBetween)
				player.getTemporaryAttributtes().remove("between_barrow_door");
			else
				player.getTemporaryAttributtes().put("between_barrow_door", true);
			WorldTile walkTo;
			if (object.getRotation() == 0)
				walkTo = new WorldTile(object.getX() + (inBetween ? -1 : 1), object.getY(), 0);
			else if (object.getRotation() == 1)
				walkTo = new WorldTile(object.getX(), object.getY() - (inBetween ? -1 : 1), 0);
			else if (object.getRotation() == 2)
				walkTo = new WorldTile(object.getX() - (inBetween ? -1 : 1), object.getY(), 0);
			else
				walkTo = new WorldTile(object.getX(), object.getY() + (inBetween ? -1 : 1), 0);
			if (!World.isFloorFree(walkTo.getPlane(), walkTo.getX(), walkTo.getY()))
				return false;
			WorldObject opened = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1, object.getX(), object.getY(), object.getPlane());
			World.spawnObjectTemporary(opened, 600);
			player.addWalkSteps(walkTo.getX(), walkTo.getY(), -1, false);
			player.lock(3);
			player.getVarsManager().sendVar(1270, inBetween ? 0 : 1);
			if (player.getHiddenBrother() != -1) {
				int brother = getRandomBrother();
				if (brother != -1)
					sendTarget(2025 + brother, walkTo);
			}
			return false;
		} else {
			int sarcoId = getSarcophagusId(object.getId());
			if (sarcoId != -1) {
				if (sarcoId == player.getHiddenBrother())
					player.getDialogueManager().startDialogue("BarrowsD");
				else if (target != null || player.getKilledBarrowBrothers()[sarcoId])
					player.getPackets().sendGameMessage("You found nothing.");
				else
					sendTarget(sarcoId == 6 ? 14297 : 2025 + sarcoId, player);
				return false;
			}
		}
		return true;
	};

	public int getSarcophagusId(int objectId) {
		switch (objectId) {
		case 66017:
			return 0;
		case 63177:
			return 1;
		case 66020:
			return 2;
		case 66018:
			return 3;
		case 66019:
			return 4;
		case 66016:
			return 5;
		case 61189:
			return 6;
		default:
			return -1;
		}
	}

	public void targetDied() {
		player.getHintIconsManager().removeUnsavedHintIcon();
		setBrotherSlained(target.getId() >= 14297 ? 6 : target.getId() - 2025);
		target = null;

	}

	public void targetFinishedWithoutDie() {
		player.getHintIconsManager().removeUnsavedHintIcon();
		target = null;
	}

	public void setBrotherSlained(int index) {
		player.getKilledBarrowBrothers()[index] = true;
		sendBrotherSlain(index, true);
	}

	public void sendTarget(int id, WorldTile tile) {
		if (target != null)
			target.disapear();
		target = new BarrowsBrother(id, tile, this);
		target.setTarget(player);
		target.setNextForceTalk(new ForceTalk("You dare disturb my rest!"));
		player.getHintIconsManager().addHintIcon(target, 1, -1, false);
	}

	public Barrows() {

	}

	// component 9, 10, 11

	private int headComponentId;
	private int timer;

	public int getAndIncreaseHeadIndex() {
		Integer head = (Integer) player.getTemporaryAttributtes().remove("BarrowsHead");
		if (head == null || head == player.getKilledBarrowBrothers().length - 1)
			head = 0;
		player.getTemporaryAttributtes().put("BarrowsHead", head + 1);
		return player.getKilledBarrowBrothers()[head] ? head : -1;
	}

	@Override
	public void process() {
		if (timer > 0) {
			timer--;
			return;
		}
		if (headComponentId == 0) {
			if (player.getHiddenBrother() == -1) {
				player.applyHit(new Hit(player, Utils.random(50) + 1, HitLook.REGULAR_DAMAGE));
				resetHeadTimer();
				return;
			}
			int headIndex = getAndIncreaseHeadIndex();
			if (headIndex == -1) {
				resetHeadTimer();
				return;
			}
			headComponentId = 9 + Utils.random(2);
			player.getPackets().sendItemOnIComponent(24, headComponentId, 4761 + headIndex, 0);
			player.getPackets().sendIComponentAnimation(9810, 24, headComponentId);
			int activeLevel = player.getPrayer().getPrayerpoints();
			if (activeLevel > 0) {
				int level = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
				player.getPrayer().drainPrayer(level / 6);
			}
			timer = 3;
		} else {
			player.getPackets().sendItemOnIComponent(24, headComponentId, -1, 0);
			headComponentId = 0;
			resetHeadTimer();
		}
	}

	public void resetHeadTimer() {
		timer = 20 + Utils.random(6);
	}

	@Override
	public void sendInterfaces() {
		if (player.getHiddenBrother() != -1)
			player.getInterfaceManager().setOverlay(24, false);
	}

	public void loadData() {
		resetHeadTimer();
		for (int i = 0; i < player.getKilledBarrowBrothers().length; i++)
			sendBrotherSlain(i, player.getKilledBarrowBrothers()[i]);
		sendCreaturesSlainCount();
		player.getPackets().sendBlackOut(2); // blacks minimap
		for (int varBit : TUNNEL_CONFIG[getTunnelIndex()])
			player.getVarsManager().sendVarBit(varBit, 1);
		player.getVarsManager().sendVarBit(467, 1);
	}

	public void sendBrotherSlain(int index, boolean slain) {
		if (index == 6)
			return;
		player.getVarsManager().sendVarBit(457 + index, slain ? 1 : 0);
	}

	public void sendCreaturesSlainCount() {
		player.getVarsManager().sendVarBit(464, player.getBarrowsKillCount());
	}

	@Override
	public void start() {
		if (player.getHiddenBrother() == -1 || player.getHiddenBrother() == 6)
			player.setHiddenBrother(Utils.random(ENABLE_AKRISAE ? player.getKilledBarrowBrothers().length : 6));
		setArguments(new Object[]
		{ (int) Utils.random(TUNNEL_CONFIG.length), 0 });
		loadData();
		sendInterfaces();
	}

	@Override
	public void processNPCDeath(NPC npc) {
	/*	for (int crypt_npc : CRYPT_NPCS) {
			if (npc.getId() == crypt_npc) {
				player.setBarrowsKillCount(player.getBarrowsKillCount()+1);
				sendCreaturesSlainCount(player.getBarrowsKillCount());
			}
		}*/
		player.setBarrowsKillCount(player.getBarrowsKillCount()+1);
		sendCreaturesSlainCount();
	}

	@Override
	public boolean login() {
		if (player.getHiddenBrother() == -1)
			player.getPackets().sendCameraShake(3, 25, 50, 25, 50);
		loadData();
		sendInterfaces();
		return false;
	}

	@Override
	public boolean logout() {
		leave(true);
		this.setArguments(new Object[]
		{ getTunnelIndex() });
		return false;
	}

	@Override
	public void forceClose() {
		leave(true);
	}

}
