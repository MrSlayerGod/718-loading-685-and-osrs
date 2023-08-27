package com.rs.game.player;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Achievements.Task;
import com.rs.game.player.QuestManager.Quests;
import com.rs.game.player.content.NPCKillLog;
import com.rs.game.player.content.Slayer;
import com.rs.game.player.content.Slayer.SlayerMaster;
import com.rs.game.player.content.Slayer.SlayerTask;
import com.rs.game.player.controllers.Wilderness;
import com.rs.net.decoders.handlers.NPCHandler;
import com.rs.utils.Utils;

public class SlayerManager implements Serializable {

	private static final long serialVersionUID = -3935672307271551069L;

	private transient final static Object[][] ABILITY =
		{
		{ 84, 91, 50, "to receive aquantities as a task from the slayer master Kuradal" },
		{ 85, 97, 400, "to use certain items on npcs in order to speed their death rates" },
		{ 86, 98, 2000, "to be assigned Ice Strykewyrms without owning a Fire cape" },
		{ 83, 90, 300, "to attach broad-tips to bolts and arrows by using them together" },
		{ 87, 99, 300, "to make a ring of slaying by joining an enchanted gem, a gold bar, and a ring mold together" },
		{ 88, 100, 400, "to make a full slayer helment by combining a spiny helmet, a pair of earmuffs, a nose peg, a face mask, and a black mask" } };
	public transient final static int BUY_INTERFACE = 164, ABILITIES_INTERFACE = 378, ASSIGNMENT_INTERFACE = 161, AQUANTITIES = 0, QUICK_BLOWS = 1, ICE_STYKE = 2, BROAD_TIPS = 3, RING_OF_SLAYING = 4,
			SLAYER_HELMET = 5;

	private transient Player player;
	private transient Player socialPlayer;
	private SlayerTask[] canceledTasks;
	private SlayerTask currentTask;
	private SlayerTask lastTask;
	
	private SlayerMaster currentMaster;
	protected int completedTasks, slayerPoints, maximumTaskCount, currentTaskCount;
	private boolean[] learnedAbilities;
	
	private String bossTask;
	private String lastBossTask;
	private int bossTaskRemaining;
	private boolean hardBossTask;
	private long lastBossSkip;
	private int bossSkipCount;
	
	private Map<SlayerTask, Integer> killcount;

	public SlayerManager() {
		learnedAbilities = new boolean[7];
		canceledTasks = new SlayerTask[6];
		setCurrentMaster(SlayerMaster.TURAEL);
		killcount = new HashMap<SlayerTask, Integer>();
	}

	public static void init() {
		// register npc clicks for cape exchange / boss task
		NPCHandler.register(27690, 2, (player, npc) ->
				player.getDialogueManager().startDialogue("ExchangeCapeD", npc.getId()));
		NPCHandler.register(27690, 1, (player, npc) ->
				player.getDialogueManager().startDialogue("BossTaskD", SlayerMaster.KURADAL));
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	private void addPoints() {
		player.getAchievements().add(Task.COMPLETE_SLAYER_TASK);
		double pointsIncreased = 0;
		if (completedTasks >= 50 && completedTasks % 50 == 0) {
			pointsIncreased += getCurrentMaster().getPointsRange()[2];
			player.getPackets().sendGameMessage("You have completed "+completedTasks+" tasks in a row!.");
			//resetCompletedTasks();
		} else if (completedTasks >= 10 && completedTasks % 10 == 0) {
			pointsIncreased += getCurrentMaster().getPointsRange()[1];
			player.getPackets().sendGameMessage("You have completed "+completedTasks+" tasks in a row!");
		} else
			pointsIncreased += getCurrentMaster().getPointsRange()[0];
		pointsIncreased *= ((double) currentTaskCount / maximumTaskCount);
		pointsIncreased *= 5;//Settings.getDropQuantityRate();
		slayerPoints += Math.max(1, pointsIncreased);
	}

	private boolean removePoints(int pointsValue) {
		int newPoints = slayerPoints - pointsValue;
		if (newPoints < 0) {
			player.getPackets().sendGameMessage("You don't have enough points to complete this transaction.");
			return false;
		}
		slayerPoints -= pointsValue;
		return true;
	}

	public int getPoints() {
		return slayerPoints;
	}

	private void unlockAbility(int slot) {
		if (learnedAbilities[slot])
			return;
		else if (!removePoints((int) ABILITY[slot][2]))
			return;
		learnedAbilities[slot] = true;
		sendSlayerInterface(ABILITIES_INTERFACE);
		player.getPackets().sendGameMessage("You have unlocked the ability " + ABILITY[slot][3] + "!");
	}

	public void sendSlayerInterface(int interfaceId) {
		if (!player.getBank().hasVerified(9))
			return;
		player.getInterfaceManager().sendInterface(interfaceId);
		sendPoints(interfaceId);
		if (interfaceId == ASSIGNMENT_INTERFACE) {
			player.getPackets().sendHideIComponent(ASSIGNMENT_INTERFACE, 42, true); // useless
			// component
			for (int index = 0; index < 5/*canceledTasks.length*/; index++) {
				SlayerTask task = canceledTasks[index];
				if (task != null) {
					player.getPackets().sendIComponentText(ASSIGNMENT_INTERFACE, 31 + index, task.getName());
				} else
					player.getPackets().sendHideIComponent(ASSIGNMENT_INTERFACE, 37 + index, true);
			}
		} else if (interfaceId == ABILITIES_INTERFACE) {
			for (int index = 0; index < learnedAbilities.length; index++) {
				boolean hasUnlocked = learnedAbilities[index];
				if (hasUnlocked) {
					Object[] data = ABILITY[index];
					player.getPackets().sendIComponentText(ABILITIES_INTERFACE, (int) data[0], "You have already unlocked this ability.");
					player.getPackets().sendIComponentText(ABILITIES_INTERFACE, (int) data[1], "");
					player.getPackets().sendHideIComponent(ABILITIES_INTERFACE, 73 + index, true);
				}
			}
		}
	}

	private void sendPoints(int interfaceId) {
		if (interfaceId == ASSIGNMENT_INTERFACE)
			player.getPackets().sendIComponentText(interfaceId, 19, "" + slayerPoints);
		else if (interfaceId == BUY_INTERFACE)
			player.getPackets().sendIComponentText(interfaceId, 20, "" + slayerPoints);
		else
			player.getPackets().sendIComponentText(interfaceId, 79, "" + slayerPoints);
	}

	public void handleRewardButtons(int interfaceId, int componentId) {
		if (componentId == 15)
			sendSlayerInterface(BUY_INTERFACE);
		else if (componentId == 16)
			sendSlayerInterface(ABILITIES_INTERFACE);
		else if (componentId == 17)
			sendSlayerInterface(ASSIGNMENT_INTERFACE);
		else if (componentId == 14) {
			if (interfaceId == ASSIGNMENT_INTERFACE)
				sendSlayerInterface(ABILITIES_INTERFACE);
			else
				sendSlayerInterface(ASSIGNMENT_INTERFACE); // xp == 2000
		}
		if (interfaceId == BUY_INTERFACE) {
			if (componentId == 24 || componentId == 32) {
				if (removePoints(400)) {
					player.getSkills().addXp(Skills.SLAYER, 2000);// 40k xp is
					// good enough
					player.getPackets().sendGameMessage("You begin to feel wiser and more experienced than before.");
				}
			} else if (componentId == 26 || componentId == 33) {
				if (removePoints(75)) {
					player.getInventory().addItemDrop(13281, 1);
					player.getPackets().sendGameMessage("The master quickly forges you a fully charged ring of slaying.");
				}
			} else if (componentId == 28 || componentId == 36) {
				if (removePoints(35)) {
					player.getInventory().addItemDrop(560, 250);
					player.getInventory().addItemDrop(558, 1000);
					player.getPackets().sendGameMessage("Here are your runes. Use them wisely.");
				}
			} else if (componentId == 37 || componentId == 34) {
				if (removePoints(35)) {
					player.getInventory().addItemDrop(13280, 250);
					player.getPackets().sendGameMessage("Here are your bolts. Use them wisely.");
				}
			} else if (componentId == 39 || componentId == 35) {
				if (removePoints(35)) {
					player.getInventory().addItemDrop(4160, 250);
					player.getPackets().sendGameMessage("Here are your bolts. Use them wisely.");
				}
			}
		} else if (interfaceId == ABILITIES_INTERFACE) {
			if (componentId >= 73 && componentId <= 78)
				unlockAbility(componentId - 73);
		} else if (interfaceId == ASSIGNMENT_INTERFACE) {
			if (componentId == 23 || componentId == 26)
				cancleCurrentTask();
			else if (componentId == 24 || componentId == 27)
				removeCurrentTask();
			else if (componentId >= 37 && componentId <= 41)
				addRemovedTask(componentId - 37);
		}
		sendPoints(interfaceId);
	}

	private void cancleCurrentTask() {
		if (currentTask == null) {
			player.getPackets().sendGameMessage("You don't have an active task to cancel.");
			return;
		} else {
			if (removePoints(30)) {
				skipCurrentTask(false);
				setCurrentTask(true, null);
				player.getPackets().sendGameMessage("Your slayer task has been re-assigned as requested."/*, as a result, your slayer-streak has been reset to 0."*/);
			}
		}
	}

	private void addRemovedTask(int slot) {
		SlayerTask task = canceledTasks[slot];
		if (task == null)
			return;
		canceledTasks[slot] = null;
		sendSlayerInterface(ASSIGNMENT_INTERFACE);
		player.getPackets().sendGameMessage("You have re-added " + task.getName().toLowerCase() + " to the assignment list.");
	}

	private void removeCurrentTask() {
		if (currentTask == null) {
			player.getPackets().sendGameMessage("You don't have an active task to remove.");
			return;
		} else {
			if (slayerPoints >= 100) {
				for (int index = 0; index < 5/*canceledTasks.length*/; index++) {
					SlayerTask task = canceledTasks[index];
					if (task == null) {
						removePoints(100);
						canceledTasks[index] = currentTask;
						player.getPackets().sendGameMessage("You have canceled the task " + currentTask.getName() + " permanently.");
						skipCurrentTask(false);
						sendSlayerInterface(ASSIGNMENT_INTERFACE);
						return;
					}
				}
				player.getPackets().sendGameMessage("You have reached the maximum limit of cancelable tasks, please remove one before continuing.");
			}
		}
	}

	public Object[] calculateTask() {
		List<SlayerTask> tasks = new LinkedList<SlayerTask>(Arrays.asList(getCurrentMaster().getTask()));
		for (SlayerTask task : canceledTasks) {
			if (task != null && tasks.contains(task))
				tasks.remove(task);
		}
		if (lastTask != null)
			tasks.remove(lastTask);
		while (true) {
			SlayerTask task = tasks.get(Utils.random(tasks.size()));
			if (!hasRequirement(player, task) || (socialPlayer != null && !hasRequirement(socialPlayer, task))) {
				continue;
			}
			return new Object[]
					{ task, Utils.random(getCurrentMaster().getTasksRange()[0], getCurrentMaster().getTasksRange()[1]+1)/2};//Math.max(10, (int) Utils.random((int)getCurrentMaster().getTasksRange()[0] * task.getTaskFactor())), (int)Math.max(10, getCurrentMaster().getTasksRange()[1] * task.getTaskFactor()) };
		}
	}

	private static boolean hasRequirement(Player p, SlayerTask task) {
		if (task == SlayerTask.SHADOW_WARRIOR && !p.getQuestManager().completedQuest(Quests.LEGENDS_QUEST) || task == SlayerTask.SKELETAL_WYVERN && !p.getQuestManager().completedQuest(Quests.ELEMENTAL_WORKSHOP_I))
			return false;
	//	if (p.getSlayerManager().getCurrentMaster() == SlayerMaster.KURADAL) {
			if ((task == SlayerTask.ICE_STRYKEWYRM && !p.isCompletedFightCaves() && !p.getSlayerManager().hasLearnedStykes()) || (task == SlayerTask.AQUANITE && !p.getSlayerManager().hasLearnedAquanite()))
				return false;
	//	}
			if (task == SlayerTask.FROST_DRAGON && p.getSkills().getLevelForXp(Skills.DUNGEONEERING) < 85)
				return false;
			
			
		if (p.getSkills().getLevel(Skills.SLAYER) < task.getLevelRequried())
			return false;
		return true;
	}

	public boolean isValidTask(String name) {
		if (currentTask == null)
			return false;
		if (currentMaster == SlayerMaster.KRYSTILIA && !(player.getControlerManager().getControler() instanceof Wilderness))
			return false;
		List<SlayerTask> tasks = new LinkedList<SlayerTask>(Arrays.asList(currentTask.getAlternatives()));
		tasks.add(currentTask);
		name = name.replace("'", "").replace("-", "");
		for (SlayerTask currentTask : tasks) {
			if (name.toLowerCase().contains(currentTask.toString().replace("_", " ").toLowerCase()))
				return true;
		}
		return false;
	}
	
	
	public void addBossKill(int damageAdmitted) {
		bossTaskRemaining--;
		boolean slayerTower = player.getRegionId() == 13623;
		player.getSkills().addXp(Skills.SLAYER, damageAdmitted / 5 * (slayerTower ? 1.1 : 1));
			if (Wilderness.isAtWild(player))
				if (!player.isExtremeDonator() || !player.getInventory().containsItem(41941, 1) || player.getLootingBag().isFull())
					World.addGroundItem(new Item(43307, damageAdmitted/10), new WorldTile(player), player, true, 180);
				else {
					player.getLootingBag().addItemNew(new Item(43307, damageAdmitted/10)); 
					player.getPackets().sendGameMessage("Blood money x"+(damageAdmitted/10)+" has been added to your looting bag.", true);	
				}
			else
				player.getInventory().addItemMoneyPouch(new Item(995, 2 * (damageAdmitted < 1000 ? Math.min(1000, damageAdmitted * 2) : damageAdmitted)));
		if (bossTaskRemaining <= 0) {
			player.getPackets().sendGameMessage("You have finished your boss task, talk to a slayer master for a new one.");
			player.getSkills().addXp(Skills.SLAYER, hardBossTask ? 20000 : 5000);
			player.getInventory().addItemMoneyPouch(new Item(995, hardBossTask ? 4000000 : 2000000));
			slayerPoints += hardBossTask ? 100 : 50;
			bossTask = null;
			hardBossTask = false;
			player.setBossTasksCompleted(player.getBossTasksCompleted()+1);
		} else if (bossTaskRemaining % 10 == 0)
			checkKillsLeft();
	}

	public void checkCompletedTask(int damageAdmitted, int otherDamageAdmitted) {
		currentTaskCount++;
		int otherSocialCount = 0;
		boolean slayerTower = player.getRegionId() == 13623;
		if (socialPlayer != null) {
			if (socialPlayer.withinDistance(player, 16) && otherDamageAdmitted > 0) {
				socialPlayer.getSkills().addXp(Skills.SLAYER, otherDamageAdmitted / 5 * (slayerTower ? 1.1 : 1));
				if (Wilderness.isAtWild(socialPlayer))
					if (!socialPlayer.isExtremeDonator() || !socialPlayer.getInventory().containsItem(41941, 1)
							|| socialPlayer.getLootingBag().isFull())
						World.addGroundItem(new Item(43307, damageAdmitted/10), new WorldTile(socialPlayer), socialPlayer, true, 180);
					else {
						socialPlayer.getLootingBag().addItemNew(new Item(43307, damageAdmitted/10)); 
						socialPlayer.getPackets().sendGameMessage("Blood money x"+(damageAdmitted/10)+" has been added to your looting bag.", true);	
					}
				else
					socialPlayer.getInventory().addItemMoneyPouch(new Item(995, otherDamageAdmitted * 2));
			}
			otherSocialCount = socialPlayer.getSlayerManager().getCurrentTaskCount();
		}
		player.getSkills().addXp(Skills.SLAYER, damageAdmitted / 5 * (slayerTower ? 1.1 : 1));
		if (Wilderness.isAtWild(player))
			if (!player.isExtremeDonator() || !player.getInventory().containsItem(41941, 1) || player.getLootingBag().isFull())
				World.addGroundItem(new Item(43307, damageAdmitted/10), new WorldTile(player), player, true, 180);
			else {
				player.getLootingBag().addItemNew(new Item(43307, damageAdmitted/10)); 
				player.getPackets().sendGameMessage("Blood money x"+(damageAdmitted/10)+" has been added to your looting bag.", true);	
			}
		else
			player.getInventory().addItemMoneyPouch(new Item(995, 2 * (damageAdmitted < 1000 ? Math.min(1000, damageAdmitted * 2) : damageAdmitted)));
		if (currentTaskCount + otherSocialCount >= maximumTaskCount) {
			if (socialPlayer != null)
				socialPlayer.getPackets().sendGameMessage("You have finished your slayer task, talk to a slayer master for a new one.");
			player.getPackets().sendGameMessage("You have finished your slayer task, talk to a slayer master for a new one.");
		/*	if (currentTask == SlayerTask.MEN) {
				player.getAppearence().setTitle(3000);
				World.sendNews(player.getDisplayName()+" has unlocked the \"Genocidal\" title!", World.WORLD_NEWS);
			} else if (currentTask == SlayerTask.UNICORN) {
				player.getAppearence().setTitle(3001);
				World.sendNews(player.getDisplayName()+" has unlocked the \"Unicorn Slayer\" title!", World.WORLD_NEWS);
			}*/
			resetTask(true, true);
			return;
		} else if (currentTaskCount % 10 == 0)
			checkKillsLeft();
	}

	public void checkKillsLeft() {
		if (bossTask != null) 
			player.getPackets().sendGameMessage("Your current boss task assignment is: " + bossTask + "; only " + bossTaskRemaining + " more to go.");
		if (currentTask == null) {
			player.getPackets().sendGameMessage("You currently have no slayer task assigned.");
			return;
		}
		player.getPackets().sendGameMessage("Your current assignment is: " + currentTask.getName() + "; only " + getCount() + " more to go.");
		if (socialPlayer != null) {
			player.getPackets().sendGameMessage("Your partner's current assignment is: " + currentTask.getName() + "; only " + player.getSlayerManager().getCount() + " more to go.");
			int combinedTasksCount = currentTaskCount + socialPlayer.getSlayerManager().getCurrentTaskCount();
			player.getPackets().sendGameMessage("In total you both have killed " + combinedTasksCount + " out of " + maximumTaskCount + " of the task, only " + (maximumTaskCount - combinedTasksCount));
		}
	}

	public int getCount() {
		return Math.max(0, maximumTaskCount - currentTaskCount);
	}

	public void invitePlayer(Player otherPlayer) {
		if (currentTask != null) {
			player.getPackets().sendGameMessage("You need to complete your current task before starting a social slayer group.");
			return;
		} else if (otherPlayer == null || !otherPlayer.withinDistance(player, 7) || player.hasFinished() || otherPlayer.hasFinished()) {
			player.getPackets().sendGameMessage("Your target is no-where to be found.");
			return;
		} else if (otherPlayer.getSlayerManager().getCurrentTask() != null) {
			player.getPackets().sendGameMessage("Your target needs to complete their current task before joining a social slayer group.");
			return;
		} else if (otherPlayer.getSlayerManager().getSocialPlayer() != null) {
			player.getPackets().sendGameMessage("Your target is already in a social slayer group.");
			return;
		} else if (socialPlayer != null) {
			player.getPackets().sendGameMessage("You are already in a social slayer group, leave it in order to start a new one.");
			return;
		}
		if (otherPlayer.getTemporaryAttributtes().get("social_request") == player) {
			player.getTemporaryAttributtes().put("social_request", otherPlayer);
			openSocialInvitation(otherPlayer);
			return;
		}
		player.getTemporaryAttributtes().put("social_request", otherPlayer);
		player.getPackets().sendGameMessage("Sending " + otherPlayer.getDisplayName() + " an invitation...");
		otherPlayer.getPackets().sendMessage(117, "You have received an invitation to join " + player.getDisplayName() + "'s social slayer group.", player);
	}

	private void openSocialInvitation(final Player otherPlayer) {
		player.getInterfaceManager().sendInterface(1310);
		player.getPackets().sendIComponentText(1310, 6, otherPlayer.getDisplayName());
		player.getPackets().sendIComponentText(1310, 8, "" + otherPlayer.getSkills().getLevel(Skills.SLAYER));
		player.getPackets().sendIComponentText(1310, 10, "" + otherPlayer.getSkills().getCombatLevelWithSummoning());
		player.setCloseInterfacesEvent(new Runnable() {

			@Override
			public void run() {
				otherPlayer.getPackets().sendGameMessage("Your invitation has been declined.");
				player.getPackets().sendGameMessage("You have declined the invitation.");
				otherPlayer.getTemporaryAttributtes().remove("social_request");
				player.getTemporaryAttributtes().remove("social_request");
			}
		});
	}

	public void createSocialGroup(boolean initial) {
		Player socialPlayer = (Player) player.getTemporaryAttributtes().remove("social_request");
		if (socialPlayer == null)
			return;
		if (initial) {
			if ((player.isIronman() || player.isUltimateIronman() || player.isHCIronman())
					&& (player.getHcPartner() == null || !player.getHcPartner().equalsIgnoreCase(socialPlayer.getUsername()))) {
				player.getPackets().sendGameMessage("You can't use this feature as an ironman.");
				return;
			}
			if (player.isDungeoneer()) {
				player.getPackets().sendGameMessage("You can't use this feature as a dungeoneer.");
				return;
			}
		/*	if (socialPlayer.isIronman()) {
				player.getPackets().sendGameMessage("You can't use this feature with an ironman.");
				return;
			}*/
			if (socialPlayer.isDungeoneer()) {
				player.getPackets().sendGameMessage("You can't use this feature with a dungeoneer.");
				return;
			}
			
			socialPlayer.getSlayerManager().createSocialGroup(false);
			player.getPackets().sendGameMessage("You have created a social group.");
		} else
			player.getPackets().sendGameMessage("You have just joined " + socialPlayer.getDisplayName() + "'s social group.");
		this.socialPlayer = socialPlayer;
	}

	public void resetSocialGroup(boolean initial) {
		if (socialPlayer != null) {
			if (initial) {
				socialPlayer.getSlayerManager().resetSocialGroup(false);
				player.getPackets().sendGameMessage("You have left the social slayer group.", true);
			} else
				player.getPackets().sendGameMessage("Your social slayer member has left your group.", true);
			socialPlayer = null;
		}
	}

	public void skipCurrentTask(boolean resetCompletedTasks) {
		resetTask(false, true);
		if (resetCompletedTasks)
			resetCompletedTasks();
	}

	private void resetTask(boolean completed, boolean initial) {
		lastTask = currentTask;

		if (completed) {
			completedTasks++;
			addPoints();
		}
		if (initial) {
			if (socialPlayer != null) {
				socialPlayer.getSlayerManager().resetTask(completed, false);
				if (!completed)
					resetSocialGroup(true);
			}
		}
		setCurrentTask(null, 0);
	}

	public boolean hasLearnedBroad() {
		return learnedAbilities[BROAD_TIPS];
	}

	public boolean hasLearnedQuickBlows() {
		return learnedAbilities[QUICK_BLOWS];
	}

	public boolean hasLearnedRing() {
		return learnedAbilities[RING_OF_SLAYING];
	}

	public boolean hasLearnedSlayerHelmet() {
		return learnedAbilities[SLAYER_HELMET];
	}

	public boolean hasLearnedStykes() {
		return learnedAbilities[ICE_STYKE];
	}

	public boolean hasLearnedAquanite() {
		return learnedAbilities[AQUANTITIES];
	}

	private void resetCompletedTasks() {
		if(completedTasks > 0)
			player.sendMessage("<col=ff0000>Your task streak has been reset!");
		completedTasks = 0;
	}
	
	public void setCurrentTask(boolean initial, SlayerMaster master) {
		if (master != null)
			setCurrentMaster(master);
		Object[] futureTask = calculateTask();
		setCurrentTask((SlayerTask) futureTask[0], (int) futureTask[1]);
		checkKillsLeft();
		if (initial) {
			if (socialPlayer != null)
				socialPlayer.getSlayerManager().setCurrentTask((SlayerTask) futureTask[0], (int) futureTask[1]);
		}
	}

	public void setCurrentTask(SlayerTask task, int maximumTaskCount) {
		if (task == null)
			this.currentTaskCount = 0;
		else if (maximumTaskCount <= 0)
			maximumTaskCount = 1;
		this.currentTask = task;
		if (task != null)
			lastTask = task;
		this.maximumTaskCount = maximumTaskCount;
	}
	
	public void setBossTask() {
		bossTaskRemaining = 3 + Utils.random(33);
		
		for (int i = 0; i < 100; i++ ) {
			if(Utils.rollDie(NPCKillLog.BOSS_NAMES.length, 1)) {
				bossTask = "The Horde";
				hardBossTask = true;
				bossTaskRemaining = 1;
				break;
			}
			bossTask = NPCKillLog.BOSS_NAMES[Utils.random(NPCKillLog.BOSS_NAMES.length)].toLowerCase();
			if (Slayer.getLevelRequirement(bossTask) <= player.getSkills().getLevelForXp(Skills.SLAYER)
					 && (lastBossTask == null || !bossTask.equalsIgnoreCase(lastBossTask))
					 && !bossTask.equalsIgnoreCase("Skoll")
					 && !bossTask.startsWith("Enraged"))
					break;
		}

		lastBossTask = bossTask;
		
		if (bossTask.equalsIgnoreCase("Theatre of Blood") || bossTask.equalsIgnoreCase("TzKal-Zuk")
				 || bossTask.equalsIgnoreCase("Har-Aken") || bossTask.equalsIgnoreCase("TzTok-Jad") || bossTask.equalsIgnoreCase("Nex") || bossTask.equalsIgnoreCase("Galvek") || bossTask.equalsIgnoreCase("Bork") 
				 || bossTask.equalsIgnoreCase("Matrix")
				|| bossTask.equalsIgnoreCase("Giant Mimic")
				 || bossTask.equalsIgnoreCase("The Nightmare")
				|| bossTask.equalsIgnoreCase("Callus")
				|| bossTask.equalsIgnoreCase("The Horde")) {
				bossTaskRemaining = 1;
				hardBossTask = true;
		} else
			hardBossTask = false;
	}
	
	public boolean isHardBossTask() {
		return hardBossTask;
	}

	public void setBossTask(String task) { //test purposes
		bossTask = task;
	}
	
	public SlayerTask getCurrentTask() {
		return currentTask;
	}

	private int getCurrentTaskCount() {
		return currentTaskCount;
	}

	public void setPoints(int i) {// admin only
		this.slayerPoints = i;
	}

	public void setCurrentMaster(SlayerMaster currentMaster) {
		if (this.currentMaster != currentMaster)
			resetCompletedTasks();
		this.currentMaster = currentMaster;
	}

	public SlayerMaster getCurrentMaster() {
		if (currentMaster == null)
			currentMaster = SlayerMaster.TURAEL;
		return currentMaster;
	}

	public Player getSocialPlayer() {
		return socialPlayer;
	}

	public void autoComplete() {
		currentTaskCount = maximumTaskCount - 1;
		checkCompletedTask(1, 0);
	}

	/**
	 * @return the killcount
	 */
	public Map<SlayerTask, Integer> getKillcount() {
		if (killcount == null) //temporarly
			killcount = new HashMap<SlayerTask, Integer>();
		return killcount;
	}
	
	public String getBossTask() {
		return bossTask;
	}
	
	public int getBossTaskRemaining() {
		return bossTaskRemaining;
	}

	public long getLastBossSkip() {
		return lastBossSkip;
	}

	public void setLastBossSkip(long lastBossSkip) {
		this.lastBossSkip = lastBossSkip;
	}

	public int getBossSkipCount() {
		return bossSkipCount;
	}

	public void setBossSkipCount(int bossSkipCount) {
		this.bossSkipCount = bossSkipCount;
	}

	public void trySuperiorSpawn(NPC n) {
		if(Utils.random(100) == 0) {
			int superiorNPC = SuperiorSlayer.getSuperior(getCurrentTask(), n.getId());
			if (superiorNPC != -1 && superiorNPC != n.getId()) {
				player.sendMessage("<col=ff0000>A superior foe has appeared...</col>");
				World.spawnNPC(superiorNPC, n, -1, true, true);
			}
		}
	}
}