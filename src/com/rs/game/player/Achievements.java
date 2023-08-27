package com.rs.game.player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.Graphics;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * 
 * @author Alex (Dragonkk)
 * Jan 27, 2020
 */
public class Achievements implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7229594266282150201L;

	public static enum Difficulty {
		EASY,
		MEDIUM,
		HARD, 
		ELITE;
	}

	public static enum Task {
		/*
		 * Difficulty.EASY Tasks
		 */
		CHOP_LOGS(Difficulty.EASY, 50) //done
		, SMITH_BRONZE_PLATEBODY(Difficulty.EASY, 20) //done
		, STEAL_FROM_BAKERS_STALL(Difficulty.EASY, 25), //done
		FLETCH_SHORTBOW(Difficulty.EASY, 50) //done
		, MINE_COPPER_AND_TIN(Difficulty.EASY, 100),//done
		GNOME_AGILITY(Difficulty.EASY, 5)//done
		, CUT_UNCUT_SAPPHIRE(Difficulty.EASY, 25)//done
		, MAKE_ATTACK_POTION(Difficulty.EASY, 10)//done
		, FISH_SHRIMP(Difficulty.EASY, 75), //done
		COOK_SARDINE(Difficulty.EASY, 25)//done
		, CRAFT_AIR_RUNE(Difficulty.EASY, 200),//done

		/*
		 * Difficulty.MEDIUM Tasks
		 */
		CHOP_MAPLE_LOGS(Difficulty.MEDIUM, 50) //done
		, SMITH_MITHRIL_SCIMITAR(Difficulty.MEDIUM, 20) //done
		, MINE_MITHRIL_ORE(Difficulty.MEDIUM, 50),//done
		FLETCH_MAPLE_LONGBOW(Difficulty.MEDIUM, 75) //done
		, BARBARIAN_AGILITY(Difficulty.MEDIUM, 10) //done
		, COMPLETE_SLAYER_TASK(Difficulty.MEDIUM, 10) //done
		,	CLEAN_AVANTOE(Difficulty.MEDIUM, 50)  //done
		, CUT_UNCUT_DIAMOND(Difficulty.MEDIUM, 50) //done
		, LIGHT_MAPLE_LOG(Difficulty.MEDIUM, 100),//done
		STEAL_FROM_SILK_STALL(Difficulty.MEDIUM, 50), //done

		/*
		 * Difficulty.HARD Tasks
		 */

		MINE_ADAMANT_ORE(Difficulty.HARD, 100)//done
		, CHOP_YEW_LOGS(Difficulty.HARD, 100) //done
		, FISH_SHARK(Difficulty.HARD, 150) //done
		, CUT_UNCUT_DRAGONSTONE(Difficulty.HARD, 50), //done
		SMITH_ADAMANT_SWORD(Difficulty.HARD, 100) //done
		, FLETCH_YEW_SHORTBOW(Difficulty.HARD, 150) //done
		, MAKE_RANGING_POTION(Difficulty.HARD, 50), //done
		LIGHT_YEW_LOG(Difficulty.HARD, 150),//done

		/*
		 * Difficulty.ELITE Tasks
		 */

		FLETCH_MAGIC_LONGBOW(Difficulty.ELITE, 200) //done
		, CUT_UNCUT_ONYX(Difficulty.ELITE) //done
		, CHOP_MAGIC_LOGS(Difficulty.ELITE, 150), //done
		MAKE_EXTREME_STRENGTH(Difficulty.ELITE, 150)  //done
		, SMITH_ADAMANT_PLATEBODY(Difficulty.ELITE, 100) //done
		, LIGHT_MAGIC_LOG(Difficulty.ELITE, 200),//done
		GNOME_AGILITY_ADVANCED(Difficulty.ELITE, 25) //done
		//, STEAL_FROM_GEM_STALL(Difficulty.ELITE, 150) //done
		, KILL_ABYSSAL_DEMON(Difficulty.ELITE, 100) //done
		, CRAFT_BLOOD_RUNE(Difficulty.ELITE, 500) //done
		, MINE_RUNITE_ORE(Difficulty.ELITE, 150)//done
		, SUMMON_UNICORN_STALLION(Difficulty.ELITE); //done
	
		private Difficulty difficulty;
		private int amount;
		
		private Task(Difficulty difficulty) {
			this(difficulty, 1);
		}
		
		private Task(Difficulty difficulty, int amount) {
			this.difficulty = difficulty;
			this.amount = amount;
		}
		
		public Difficulty getDifficulty() {
			return difficulty;
		}
		
		public int getAmount() {
			return amount;
		}
	}
	
	private static final String[] FINISH_TASK_MESSAGES = { "Nice job!", "Great job!", "Awesome,", "Well done!", "Cool!", "Amazing!" };
	
	
	private Map<Task, Integer> tasks;
	private transient Player player;
	
	public Achievements() {
		tasks = new HashMap<Task, Integer>();
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public void add(Task task) {
		add(task, 1);
	}
	
	public int getTaskProgress(Task task) {
		Integer amount = tasks.get(task);
		return amount == null ? 0 : amount;
	}
	
	public boolean isTaskCompleted(Task task) {
		return getTaskProgress(task) >= task.amount;
	}
	
	public boolean isCompleted(Difficulty d) {
		for (Task task : Task.values())
			if (d == task.difficulty && !isTaskCompleted(task))
				return false;
		return true;
	}
	
	public boolean isCompleted() {
		for (Task task : Task.values())
			if (!isTaskCompleted(task))
				return false;
		return true;
	}
	
	public boolean isTaskStarted(Task task) {
		return tasks.containsKey(task);
	}
	
	public void add(Task task, int amount) {
		if (isTaskCompleted(task))
			return;
		Integer amt = getTaskProgress(task) + amount;
		tasks.put(task, amt);
		if (isTaskCompleted(task)) 
			complete(task);
		 else
			 player.getPackets().sendGameMessage("You have completed a step in the task: "
						 + Utils.formatPlayerNameForDisplay(task.toString()) + ", Step: <col=ff0000>" + amt+"/"+task.amount, true);
	}
	
	public void complete(Task task) {
		player.setNextGraphics(new Graphics(3201, 0, 150));
		player.getInterfaceManager().setFadingInterface(1073);
		player.getPackets().sendIComponentText(1073, 10, "<col=ffc800>Task completed");
		player.getPackets().sendIComponentText(1073, 11, Utils.formatPlayerNameForDisplay(task.toString()));
		player.getPackets().sendGameMessage((FINISH_TASK_MESSAGES[Utils.random(FINISH_TASK_MESSAGES.length)] + " You completed a " + Utils.formatPlayerNameForDisplay(task.difficulty.toString())
								+ " task: " + Utils.formatPlayerNameForDisplay(task.toString())
								+ "! To view what tasks you got left, check achievement diary."), true);
		player.getPackets().sendSound(5527, 0, 1);
		
		
		int reward = task.difficulty == Difficulty.ELITE ? 23716 :  task.difficulty == Difficulty.HARD ? 23715 :  task.difficulty == Difficulty.MEDIUM ? 23714 : 23713;
		player.getInventory().addItemDrop(reward, 1);
		
		if (isCompleted(task.difficulty)) { //all group completed
			reward = task.difficulty == Difficulty.ELITE ? 14632 :
				task.difficulty == Difficulty.HARD ? 52986 :
				task.difficulty == Difficulty.MEDIUM ? 22272 :
					42791;
			player.getInventory().addItemDrop(reward, 1);
			player.getPackets().sendGameMessage("You completed all " + Utils.formatPlayerNameForDisplay(task.difficulty.toString())
			+ " tasks and received a "+ItemConfig.forID(reward).getName()+"!", true);
		}
		
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (player.getInterfaceManager().containsInterface(1073))
					player.getInterfaceManager().closeFadingInterface();
			}
		}, 8);
	}
}
