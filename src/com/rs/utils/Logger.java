package com.rs.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;

import com.rs.Settings;
import com.rs.executor.GameExecutorManager;

public final class Logger {

	private static BufferedWriter globallogs;

	static {
		try {
			if (!Settings.DEBUG) {
				Calendar c = Calendar.getInstance();
				globallogs = new BufferedWriter(new FileWriter("data/log/global/log." + ((c.get(Calendar.MONTH)) + 1) + "." + c.get(Calendar.DATE) + "." + c.get(Calendar.YEAR) + "." + Settings.WORLD_ID + ".txt", true));
			}
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

	
	public static void globalLog(String ip, String name, Object o) {
			if (!Settings.DEBUG) {
				
				GameExecutorManager.slowExecutor.execute(new Runnable() {//io operations can be slow

					@Override
					public void run() {
						try {
							
							String message = Thread.currentThread().getName() + ", " + "[" + Utils.currentTime("hh:mm:ss z") + "]" + "" + name + " - " + ip + " [ " + o + " ]";
							
							globallogs.write(message);
							globallogs.newLine();
							globallogs.flush();
							
							BufferedWriter writer = new BufferedWriter(new FileWriter("data/log/players/" + ip + ".txt", true));
							writer.write(message);
							writer.newLine();
							writer.flush();
							writer.close();
							
							writer = new BufferedWriter(new FileWriter("data/log/ips/" + name + ".txt", true));
							writer.write(message);
							writer.newLine();
							writer.flush();
							writer.close();
							
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				});
			}
	}

	public static void handle(Throwable throwable) {
		System.out.println("ERROR! THREAD NAME: " + Thread.currentThread().getName());
		throwable.printStackTrace();
	}

	public static void log(Class<?> classInstance, Object message) {
		log(classInstance.getSimpleName(), message);
	}

	public static void log(Object classInstance, Object message) {
		log(classInstance.getClass().getSimpleName(), message);
	}

	public static void log(String className, Object message) {
		String text = "[" + className + "]" + " " + message.toString();
		System.out.println(text);
	}

}
