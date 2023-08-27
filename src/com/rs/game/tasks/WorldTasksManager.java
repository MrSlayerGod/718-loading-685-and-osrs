package com.rs.game.tasks;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class WorldTasksManager {

	private static final List<WorldTaskInformation> tasks = Collections.synchronizedList(new LinkedList<WorldTaskInformation>());

	public static void processTasks() {
		for (WorldTaskInformation taskInformation : tasks.toArray(new WorldTaskInformation[tasks.size()])) {
			if (taskInformation.continueCount > 0) {
				taskInformation.continueCount--;
				continue;
			}
			try {
				taskInformation.task.run();
			} catch (Throwable e) {
				Logger.handle(e);
			}
			if (taskInformation.task.needRemove)
				tasks.remove(taskInformation);
			else
				taskInformation.continueCount = taskInformation.continueMaxCount;
		}
	}

	public static void schedule(WorldTask task, int delayCount, int periodCount) {
		if (task == null || delayCount < 0 || periodCount < 0)
			return;
		tasks.add(new WorldTaskInformation(task, delayCount, periodCount));
	}

	public static void schedule(WorldTask task, int delayCount) {
		if (task == null || delayCount < 0)
			return;
		tasks.add(new WorldTaskInformation(task, delayCount, -1));
	}

	public static void schedule(Runnable runnable) {
		schedule(new WorldTask() {
			@Override
			public void run() {
				runnable.run();
				stop();
			}
		});
	}

	/**
	 * Should not be used, processes on a 2t delay
	 * Use #schedule(Consumer<WorldTaskList> abc)
	 */
	@Deprecated
	public static void scheduleRevolving(Consumer<WorldTaskList> abc) {
		WorldTaskList list = new WorldTaskList();
		abc.accept(list);
		list.execute2t();
	}


	public static void schedule(Consumer<WorldTaskList> abc) {
		WorldTaskList list = new WorldTaskList();
		abc.accept(list);
		list.execute();
	}

	public static void executeTaskList(WorldTaskList taskList) {
		schedule(new WorldTask() {
			WorldTaskList.ChainedTask currentTask = taskList.remove();
			@Override
			public void run() {
				if(taskList.checkCancelCondition()) {
					currentTask.cancel();
					taskList.cancel();
					return;
				}

				if(currentTask.isFinishedExecuting() && !taskList.finished()) {
					currentTask = taskList.remove();
					//return;
				}

				if(currentTask.getTask() != null && !currentTask.hasExecuted()) {
					currentTask.execute();
					return;
				}

				if(!currentTask.isFinishedExecuting()) {
					return;
				}

				if(taskList.finished()) {
					stop();
				}
			}
		}, 0, 0);
	}

	/**
	 * Use executeTaskList
	 */
	@Deprecated
	public static void executeTaskList2TickInterval(WorldTaskList taskList) {
		schedule(new WorldTask() {
			WorldTaskList.ChainedTask currentTask = taskList.remove();
			@Override
			public void run() {
				if(taskList.checkCancelCondition()) {
					currentTask.cancel();
					taskList.cancel();
					return;
				}

				if(currentTask.isFinishedExecuting() && !taskList.finished()) {
					currentTask = taskList.remove();
					//return;
				}

				if(currentTask.getTask() != null && !currentTask.hasExecuted()) {
					currentTask.execute();
					return;
				}

				if(!currentTask.isFinishedExecuting()) {
					return;
				}

				if(taskList.finished()) {
					stop();
				}
			}
		}, 0, 0);
	}

	public static class WorldTaskList {
		Queue<ChainedTask> taskList = new LinkedList<>();
		Supplier<Boolean> cancelCondition = null;

		public boolean checkCancelCondition() {
			if (cancelCondition != null && cancelCondition.get()) {
				return true;
			} else {
				return false;
			}
		}

		public void delay(int delay) {
			taskList.add(new ChainedTask(delay));
		}

		public void add(Runnable runnable, int delay) {
			taskList.add(new ChainedTask(runnable, delay));
		}

		public void add(WorldTask runnable) {
			taskList.add(new ChainedTask(runnable));
		}

		public void add(WorldTask runnable, int delay) {
			taskList.add(new ChainedTask(runnable, delay));
		}

		public void add(Runnable runnable) {
			taskList.add(new ChainedTask(runnable));
		}

		public ChainedTask remove() {
			return taskList.remove();
		}

		private boolean executed = false;

		/**
		 * Use execute
		 */
		@Deprecated
		public void execute2t() {
			if(!finished()) {
				WorldTasksManager.executeTaskList2TickInterval(this);
			}
			executed = true;
		}

		public void execute() {
			if(!finished()) {
				WorldTasksManager.executeTaskList(this);
			}
			executed = true;
		}

		public boolean finished() {
			return taskList.size() == 0;
		}

		public void setCancelCondition(Supplier<Boolean> cancelCondition) {
			this.cancelCondition = cancelCondition;
		}

		public void cancel() {
			taskList.forEach(ChainedTask::cancel);
			taskList.clear();
		}

		public boolean isExecuted() {
			return executed;
		}

		class ChainedTask {
			int delay;
			WorldTask worldTask;
			boolean executed;

			public boolean hasExecuted() {
				return executed;
			}

			public boolean isFinishedExecuting() {
				return hasExecuted() && taskCompleted();
			}

			private boolean taskCompleted() {
				return worldTask == null || worldTask.isStopped();
			}

			public void setExecuted(boolean executed) {
				this.executed = executed;
			}

			public ChainedTask(WorldTask runnable, int delay) {
				this.delay = delay;
				this.worldTask = runnable;
			}

			public ChainedTask(WorldTask runnable) {
				this.delay = 0;
				this.worldTask = runnable;
			}

			public ChainedTask(Runnable runnable, int delay) {
				this.delay = delay;
				this.worldTask = createTask(runnable);
			}

			private WorldTask createTask(Runnable runnable) {
				WorldTask task = new WorldTask() {
					@Override
					public void run() {
						runnable.run();
						stop();
					}
				};
				return task;
			}

			public ChainedTask(int delay) {
				this.delay = delay;
				this.worldTask = createTask(()->{});
			}

			public ChainedTask(Runnable runnable) {
				this.worldTask = createTask(runnable);
				this.delay = 0;
			}

			public void execute() {
				if(this.worldTask != null) {
					WorldTaskInformation info = new WorldTaskInformation(worldTask, this.delay, 0);
					if(this.delay != 0)
						tasks.add(info);
					else {
						info.task.run();
						if(!info.task.isStopped())
							tasks.add(info);
					}

				} else {
					System.err.println("Error: null runnable attempted execution");
				}
				setExecuted(true);
			}

			public Runnable getTask() {
				return this.worldTask;
			}

			public void cancel() {
				this.worldTask.stop();
			}
		}
	}

	public static void schedule(Runnable runnable, int delay) {
		schedule(new WorldTask() {
			@Override
			public void run() {
				runnable.run();
				stop();
			}
		}, delay);
	}

	public static void schedule(Runnable runnable, int delay, int periodCount) {
		schedule(new WorldTask() {
			@Override
			public void run() {
				runnable.run();
				stop();
			}
		}, delay, periodCount);
	}
	public static void schedule(WorldTask task) {
		if (task == null)
			return;
		tasks.add(new WorldTaskInformation(task, 0, -1));
	}

	public static int getTasksCount() {
		return tasks.size();
	}

	private static final class WorldTaskInformation {

		private WorldTask task;
		private int continueMaxCount;
		private int continueCount;

		public WorldTaskInformation(WorldTask task, int continueCount, int continueMaxCount) {
			this.task = task;
			this.continueCount = continueCount;
			this.continueMaxCount = continueMaxCount;
			if (continueMaxCount == -1)
				task.needRemove = true;
		}
	}

}
