package com.rs.executor;

import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class GameExecutorManager {

	public static volatile boolean executorShutdown;
	public static WorldThread worldThread;
	public static PlayerHandlerThread playerHandlerThread;
	public static Timer fastExecutor;
	public static ScheduledExecutorService slowExecutor;

	public static void init() {
		worldThread = new WorldThread();
		playerHandlerThread = new PlayerHandlerThread();
		fastExecutor = new Timer("Fast Executor");
		slowExecutor = Executors.newSingleThreadScheduledExecutor(new SlowThreadFactory());
		worldThread.start();
		playerHandlerThread.start();
	}

	public static void initLoginServer() {
		slowExecutor = Executors.newSingleThreadScheduledExecutor(new SlowThreadFactory());
	}

	public static void shutdown(boolean await) {
		executorShutdown = true;
		fastExecutor.cancel();
		slowExecutor.shutdownNow();
		if (await) {
			while (true) {
				try {
					worldThread.join();
					break;
				} catch (InterruptedException e) {
				}
			}
			while (true) {
				try {
					playerHandlerThread.join();
					break;
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
