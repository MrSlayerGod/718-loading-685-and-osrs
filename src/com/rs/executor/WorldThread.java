package com.rs.executor;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public final class WorldThread extends Thread {

	public static volatile long WORLD_CYCLE;

	protected WorldThread() {
		setPriority(Thread.MAX_PRIORITY);
		setName("World Thread");
	}

	@Override
	public final void run() {
		while (!GameExecutorManager.executorShutdown) {
			WORLD_CYCLE++; // made the cycle update at begin instead of end cuz at end theres 600ms then to
							// next cycle
			long currentTime = Utils.currentTimeMillis();
			// long debug = Utils.currentTimeMillis();
			WorldTasksManager.processTasks();
			for (Player player : World.getPlayers()) {
				try {
					if (!player.hasStarted() || player.hasFinished())
						continue;
					player.processEntity();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			for (NPC npc : World.getNPCs()) {
				try {
					if (npc == null || npc.hasFinished())
						continue;
					npc.processEntity();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			for (Player player : World.getPlayers()) {
				try {
					if (!player.hasStarted() || player.hasFinished())
						continue;
					player.processEntityUpdate();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			for (NPC npc : World.getNPCs()) {
				try {
					if (npc == null || npc.hasFinished())
						continue;
					npc.processEntityUpdate();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			for (Player player : World.getPlayers()) {
				try {
					if (!player.hasStarted() || player.hasFinished())
						continue;
					player.processHitbox();
					player.processProjectiles();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			for (Player player : World.getPlayers()) {
				try {
					if (!player.hasStarted() || player.hasFinished())
						continue;
					player.getPackets().sendLocalPlayersUpdate();
					player.getPackets().sendLocalNPCsUpdate();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			// System.out.print(" ,PLAYER UPDATE: "+(Utils.currentTimeMillis()-debug)+",
			// "+World.getPlayers().size()+", "+World.getNPCs().size());
			// debug = Utils.currentTimeMillis();
			for (Player player : World.getPlayers()) {
				try {
					if (!player.hasStarted() || player.hasFinished())
						continue;
					player.resetMasks();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			for (NPC npc : World.getNPCs()) {
				try {
					if (npc == null || npc.hasFinished())
						continue;
					npc.resetMasks();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}

			for (Player player : World.getPlayers()) {
				try {
					if (!player.hasStarted() || player.hasFinished())
						continue;
					if (player.getSession().getChannel() != null && (!player.hasPinged() || !player.getSession().getChannel().isActive()))
						player.finish(); // requests finish, wont do anything if already requested btw
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			for (Player player : World.getLobbyPlayers()) {
				try {
					if (!player.hasStarted() || player.hasFinished())
						continue;
					if (player.getSession().getChannel() != null && (!player.hasPinged() || !player.getSession().getChannel().isActive()))
						player.finish(); // requests finish, wont do anything if already requested btw
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}

			// //
			// Logger.log(this, "TOTAL: "+(Utils.currentTimeMillis()-currentTime));
			long sleepTime = Settings.WORLD_CYCLE_TIME + currentTime - Utils.currentTimeMillis();
			if (sleepTime <= 0)
				continue;
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				Logger.handle(e);
			}
		}
	}

}
