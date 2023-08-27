package com.rs.game.player;

import com.rs.game.player.actions.Action;

public final class ActionManager {

	private Player player;
	private Action action;
	private int actionDelay;

	public ActionManager(Player player) {
		this.player = player;
	}

	public void process() {
		if (action != null && !action.process(player))
			forceStop();
		if (actionDelay > 0) {
			actionDelay--;
			return;
		}
		if (action == null)
			return;
		int delay = action.processWithDelay(player);
		if (delay == -1) {
			forceStop();
			return;
		}
		actionDelay += delay;
	}

	public boolean setAction(Action skill) {
		forceStop();
		if (!skill.start(player))
			return false;
		this.action = skill;
		return true;
	}

	public void forceStop() {
		if (action == null)
			return;
		action.stop(player);
		action = null;
	}

	public int getActionDelay() {
		return actionDelay;
	}

	public void addActionDelay(int skillDelay) {
		this.actionDelay += skillDelay;
	}

	public void setActionDelay(int skillDelay) {
		this.actionDelay = skillDelay;
	}

	public boolean hasAction() {
		return action != null;
	}

	public Action getAction() {
		return action;
	}

	/**
	 * Creates an empty task to be used with interruptable skilling actions,
	 */
    public void createSkillingLock(Runnable interruptAction) {
		setAction(new Action() {
			@Override
			public boolean start(Player player) { return true; }

			@Override
			public boolean process(Player player) { return true; }

			@Override
			public int processWithDelay(Player player) { return 0; }

			@Override
			public void stop(Player player) {
				if(interruptAction != null) {
					interruptAction.run();
				}
			}
		});
    }
}
