package com.rs.game.player;

import com.rs.game.player.QuestManager.Quests;
import com.rs.utils.Utils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DailyTasksManager implements Serializable {

	private static final long serialVersionUID = 6424942817765324155L;

	private static Map<Integer, TaskData[]> TASKS_THRESHHOLD;

	/**
	 * The level of difficulty assigned with the task.
	 */
	private static final int EASY = 0, MEDIUM = 1, HARD = 2, EXTREME = 3, FIXED = 4;

	static {
		/**
		 * Populate the list with enabled tasks.
		 */
		TASKS_THRESHHOLD = new HashMap<Integer, TaskData[]>();

		/**
		 * Separate tasks by type here.
		 */
		TASKS_THRESHHOLD.put(EASY, new TaskData[]
		{
				TaskData.GAIN_10K_HERBLORE_EXP,
				TaskData.GAIN_10K_FLETCHING_EXP,
				TaskData.GAIN_10K_MINING_EXP,
				TaskData.GAIN_10K_PRAYER_EXP,
				TaskData.GAIN_10K_WOODCUTTING_EXP,
				TaskData.GAIN_10K_FISHING_EXP });

		TASKS_THRESHHOLD.put(MEDIUM, new TaskData[]
				{ TaskData.KILL_1_PLAYER,
						TaskData.GAIN_100K_HERBLORE_EXP,
						TaskData.GAIN_100K_FLETCHING_EXP,
						TaskData.GAIN_100K_MINING_EXP,
						TaskData.GAIN_100K_PRAYER_EXP,
						TaskData.GAIN_100K_WOODCUTTING_EXP,
						TaskData.GAIN_100K_FISHING_EXP });


		TASKS_THRESHHOLD.put(HARD, new TaskData[]
				{ TaskData.KILL_10_PLAYERS,
						TaskData.GAIN_500K_HERBLORE_EXP,
						TaskData.GAIN_500K_FLETCHING_EXP,
						TaskData.GAIN_500K_MINING_EXP,
						TaskData.GAIN_500K_PRAYER_EXP,
						TaskData.GAIN_500K_WOODCUTTING_EXP,
						TaskData.GAIN_500K_FISHING_EXP,
						TaskData.COMPLETE_FIGHT_CAVES,
						TaskData.COMPLETE_FIGHT_KILN

				});

		TASKS_THRESHHOLD.put(EXTREME, new TaskData[]
				{ TaskData.KILL_30_PLAYERS,
						TaskData.GAIN_1000K_PRAYER_EXP,
						TaskData.GAIN_1000K_WOODCUTTING_EXP,
						TaskData.GAIN_1000K_FISHING_EXP,

				});

	}

	/**
	 * The type of task we are assigning.
	 */
	public static final byte EXPERIENCE = 0, SKILL = 1, PVP = 2, PVM = 3, MINIGAME = 4;

	private class SpecialTask implements Serializable {

		private static final long serialVersionUID = 1L;

		private final TaskData task;

		private boolean completed;
		private int currentCount;
		private double[] experienceTracker;

		public SpecialTask(TaskData task) {
			this.task = task;
		}

		public int getCurrentCount() {
			return currentCount;
		}

		public void setCurrentCount(int currentCount) {
			if (currentCount > getMaximumCount())
				currentCount = getMaximumCount();
			this.currentCount = currentCount;
		}

		public double[] getExperienceTracker() {
			return experienceTracker;
		}

		public void setExperienceTracker(double[] experienceTracker) {
			this.experienceTracker = experienceTracker;
		}

		public int getMaximumCount() {
			return task.maximumCount;
		}

		public byte getTaskType() {
			return task.taskType;
		}

		public Object[] getParamaters() {
			return task.paramaters;
		}

		public void setCompleted(boolean completed) {
			this.completed = completed;
		}

		public boolean isCompleted() {
			return completed;
		}

		public String getName() {
			return Utils.formatPlayerNameForDisplay(task.toString());
		}

		public String getDifficulty() {
			return contains(TASKS_THRESHHOLD.get(EASY), task) ? "Easy" : contains(TASKS_THRESHHOLD.get(MEDIUM), task) ? "Medium" : contains(TASKS_THRESHHOLD.get(HARD), task) ? "Hard" : "Extreme";
		}

		public String getProgress() {
			if (getTaskType() == EXPERIENCE) {
				int skill = (int) getParamaters()[0];
				int experienceCap = getMaximumCount();
				return Utils.getFormattedNumber((getExperienceTracker() == null ? 0 : (long) getExperienceTracker()[0])) +"/" +Utils.getFormattedNumber(experienceCap)+" xp";
			}
			return getCurrentCount() +"/" +getMaximumCount();
		}
	}

	public static boolean contains( DailyTasksManager.TaskData[] data, TaskData task) {
		for (TaskData t : data)
			if (t == task)
				return true;
			return false;
	}

	public static int FIGHT_CAVE = 0, FIGHT_KILN = 1;
	/**
	 * Parameters change based off type.
	 * 
	 * @author Khaled
	 * Continued by alex
	 * 
	 */
	public enum TaskData {
		/**
		 * Experience format: new int[] skills[], new double experience[]
		 */
		GAIN_10K_PRAYER_EXP(EXPERIENCE, 10000, Skills.PRAYER),
		GAIN_100K_PRAYER_EXP(EXPERIENCE, 100000, Skills.PRAYER),
		GAIN_500K_PRAYER_EXP(EXPERIENCE, 500000, Skills.PRAYER),
		GAIN_1000K_PRAYER_EXP(EXPERIENCE, 1000000, Skills.PRAYER),

		GAIN_10K_WOODCUTTING_EXP(EXPERIENCE, 10000, Skills.WOODCUTTING),
		GAIN_100K_WOODCUTTING_EXP(EXPERIENCE, 100000, Skills.WOODCUTTING),
		GAIN_500K_WOODCUTTING_EXP(EXPERIENCE, 500000, Skills.WOODCUTTING),
		GAIN_1000K_WOODCUTTING_EXP(EXPERIENCE, 1000000, Skills.WOODCUTTING),

		GAIN_10K_MINING_EXP(EXPERIENCE, 10000, Skills.MINING),
		GAIN_100K_MINING_EXP(EXPERIENCE, 100000, Skills.MINING),
		GAIN_500K_MINING_EXP(EXPERIENCE, 500000, Skills.MINING),

		GAIN_10K_FLETCHING_EXP(EXPERIENCE, 10000, Skills.FLETCHING),
		GAIN_100K_FLETCHING_EXP(EXPERIENCE, 100000, Skills.FLETCHING),
		GAIN_500K_FLETCHING_EXP(EXPERIENCE, 500000, Skills.FLETCHING),

		GAIN_10K_HERBLORE_EXP(EXPERIENCE, 10000, Skills.HERBLORE),
		GAIN_100K_HERBLORE_EXP(EXPERIENCE, 100000, Skills.HERBLORE),
		GAIN_500K_HERBLORE_EXP(EXPERIENCE, 500000, Skills.HERBLORE),

		GAIN_10K_FISHING_EXP(EXPERIENCE, 10000, Skills.FISHING),
		GAIN_100K_FISHING_EXP(EXPERIENCE, 100000, Skills.FISHING),
		GAIN_500K_FISHING_EXP(EXPERIENCE, 500000, Skills.FISHING),
		GAIN_1000K_FISHING_EXP(EXPERIENCE, 1000000, Skills.FISHING),

		COMPLETE_FIGHT_CAVES(MINIGAME, 1, FIGHT_CAVE),
		COMPLETE_FIGHT_KILN(MINIGAME, 1, FIGHT_KILN),

		KILL_30_PLAYERS(PVP, 30),
		KILL_10_PLAYERS(PVP, 10),
		KILL_1_PLAYER(PVP, 1);

		private int maximumCount;
		private byte taskType;
		private Object[] paramaters;

		private TaskData(byte taskType, int maximumCount, Object... paramaters) {
			this.maximumCount = maximumCount;
			this.taskType = taskType;
			this.paramaters = paramaters;
		}

		@Override
		public String toString() {//TODO task names that are funny, separate with $ as sentinel
			return name().replace("_", " ");
		}
	}

	private transient Player player;

	private SpecialTask[] dailyTasks;
	private int completedTasks, consecutiveTasks;
	private long lastTaskAssignTime;

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void init() {
		//TODO set tasks every day
		Calendar previous = Calendar.getInstance();
		previous.setTimeInMillis(lastTaskAssignTime);
		Calendar current = Calendar.getInstance();
		if (dailyTasks == null || previous.get(Calendar.DAY_OF_YEAR) != current.get(Calendar.DAY_OF_YEAR))
			resetTasks();
	}

	public void resetTasks() {
		lastTaskAssignTime = Utils.currentTimeMillis();
		dailyTasks = new SpecialTask[3];

		int index = 0;
		for (int i = 0; i < dailyTasks.length; i++)
			dailyTasks[index++] = new SpecialTask(generateTask(player, Utils.random(EASY, EXTREME)));
		refreshInterface();
		player.getPackets().sendGameMessage("<shad=000><col=EAC117> Daily Task Update: You have new tasks waiting you.", true);

	}

	public void refreshInterface() {
		if (dailyTasks == null)
			return;
		/*int index = 4;
		for (int i = 0; i < dailyTasks.length; i++) {
			player.getPackets().sendIComponentText(3205, index++, dailyTasks[i].getName());
			player.getPackets().sendIComponentText(3205, index++, dailyTasks[i].getDifficulty());
			player.getPackets().sendIComponentText(3205, index++, dailyTasks[i].getProgress());
		}*/
		player.getPackets().sendIComponentText(3207, 4, dailyTasks[0].getName());
		player.getPackets().sendIComponentText(3207, 5, dailyTasks[0].getDifficulty());
		player.getPackets().sendIComponentText(3207, 6, dailyTasks[0].getProgress());

		player.getPackets().sendIComponentText(3207, 12, dailyTasks[1].getName());
		player.getPackets().sendIComponentText(3207, 13, dailyTasks[1].getDifficulty());
		player.getPackets().sendIComponentText(3207, 14, dailyTasks[1].getProgress());

		player.getPackets().sendIComponentText(3207, 18, dailyTasks[2].getName());
		player.getPackets().sendIComponentText(3207, 19, dailyTasks[2].getDifficulty());
		player.getPackets().sendIComponentText(3207, 20, dailyTasks[2].getProgress());
	}

	public void checkForProgression(int type, Object... paramaters) {
		l:for (SpecialTask task : dailyTasks) {
			if (task == null || task.isCompleted() || task.getTaskType() != type)
				continue;
			if (type == EXPERIENCE) {
				int skill = (int) paramaters[0];
				double experience = (int) paramaters[1];
				double experienceCap = task.getMaximumCount();
				if (task.getExperienceTracker() == null)
					task.setExperienceTracker(new double[1]);
				for (int idx = 0; idx < task.getExperienceTracker().length; idx++) {
					if ((int)task.getParamaters()[0] == skill) {
						task.getExperienceTracker()[idx] += experience;
						if (task.getExperienceTracker()[idx] > experienceCap)
							task.getExperienceTracker()[idx] = experienceCap;
						refreshInterface();
						player.getPackets().sendGameMessage("<shad=000><col=EAC117> Daily Task Update: " + (int)task.getExperienceTracker()[0] + " / " + (int)experienceCap + " for skill: " + Skills.SKILL_NAME[skill] + ".", true);
						break;
					}
				}
				for (int idx = 0; idx < task.getExperienceTracker().length; idx++) {// We only check this here cuz of exp tracker
					if (task.getExperienceTracker()[idx] != experienceCap)
						continue l;
				}
				setCompletedTask(task);
			} else {
				if (type == SKILL) {
					int skill = (int) paramaters[0];//which skill are we using
					Object skillExtention = paramaters[1];
					int increment = (int) paramaters[2];
					if (skill != (int) task.getParamaters()[0] || skillExtention != task.getParamaters()[1])//cuz peas.
						continue l;
					sendTaskUpdate(type, increment, task);
				} else if (type == PVP) {
					sendTaskUpdate(type, 1, task);
				} else if (type == PVM) {
					Object identifier = task.getParamaters()[0];
					if (identifier instanceof String) {
						String actualName = (String) identifier;
						String name = (String) paramaters[0];
						if (actualName.contains(name))
							sendTaskUpdate(type, 1, task);
					} else {
						Class<?> clazz = (Class<?>) identifier;
						Class<?> returnedClazz = (Class<?>) paramaters[0];
						if (clazz == returnedClazz)
							sendTaskUpdate(type, 1, task);
					}
				} else if (type == MINIGAME) {
					int minigame = (int) paramaters[0];
					if (minigame == (int) task.getParamaters()[0])
						sendTaskUpdate(type, 1, task);
				}
				if (task.getCurrentCount() == task.getMaximumCount())
					setCompletedTask(task);
			}
		}
	}

	private void sendTaskUpdate(int type, int increment, SpecialTask task) {
		String message = "<shad=000><col=EAC117>Daily Task Update: ";//Default color
		if (type == SKILL)
			message += "Completed " + task.getCurrentCount() + " " + task.getName() + " out of " + task.getMaximumCount() + ".";
		else if (type == PVP)
			message += "Completed " + task.getCurrentCount() + " out of " + task.getMaximumCount() + " PVP kills.";
		else if (type == PVM)
			message += "Completed " + task.getCurrentCount() + " out of " + task.getMaximumCount() + " " + task.getName() + " PVM kills";
		else if (type == MINIGAME)
			message += "Completed " + task.getCurrentCount() + " out of " + task.getMaximumCount() + " " + task.getName() + " minigame wins.";
		player.getPackets().sendGameMessage(message);
		task.setCurrentCount(task.getCurrentCount() + increment);
		refreshInterface();
	}

	private void setCompletedTask(SpecialTask task) {
		consecutiveTasks++;
		completedTasks++;
		task.setCompleted(true);
		//player.getInterfaceManager().setOverlay(199, false);
		//player.getPackets().sendIComponentText(199, "Your task '");
		player.getPackets().sendGameMessage("<shad=000><col=EAC117> Daily Task Update: Your task has been completed, you receive a xp lamp!");
		player.getInventory().addItemDrop(task.getDifficulty().equalsIgnoreCase("easy") ? 23713 : 23714, 1);
		refreshInterface();
	}

	public int getConsecutiveTasks() {
		return consecutiveTasks;
	}

	public int getCompletedTasks() {
		return completedTasks;
	}

	public void setConsecutiveTasks(int consecutiveTasks) {
		this.consecutiveTasks = consecutiveTasks;
	}

	public static TaskData generateTask(Player player, int difficulty) {
		final TaskData[] POSSIBLE_TASKS = TASKS_THRESHHOLD.get(difficulty);

		t: while (true) {
			TaskData task = POSSIBLE_TASKS[Utils.random(POSSIBLE_TASKS.length)];

			for (SpecialTask t2 : player.getTasksManager().dailyTasks) {
				if (t2 != null && t2.task == task)
					continue t;
			}

			if (task != null && hasRequirements(player, task))
				return task;
		}
	}

	public static boolean hasRequirements(Player player, TaskData task) {
		if (task.taskType != SKILL)
			return true;
		final int[] REQUIREMENTS = (int[]) task.paramaters[2];
		for (int idx = 0; idx < REQUIREMENTS.length; idx += 2) {
			int skill = REQUIREMENTS[idx], level = REQUIREMENTS[idx + 1];
			if (player.getSkills().getLevelForXp(skill) < level)
				return false;
		}
		if (task.paramaters.length == 3) {
			Quests[] quests = (Quests[]) task.paramaters[3];//Does our skill need any quests?
			for (Quests quest : quests) {
				if (!player.getQuestManager().completedQuest(quest))
					return false;
			}
		}
		return true;
	}
}
