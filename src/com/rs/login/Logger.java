package com.rs.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rs.game.log.LogEntry;
import com.rs.utils.LoginFilesManager;

/**
 * A class for managing huge amount of log entries.
 * The way entries are stored is optimized for our file system so that 
 * a lot of space and loading time is saved.
 */
public class Logger {
	
	/**
	 * World for which we are logging.
	 */
	private GameWorld world;
	
	/**
	 * Contains loaded logs.
	 */
	private Map<Integer, List<LogEntry>> loadcache;
	
	/**
	 * Contains buffered logs to be saved.
	 */
	private Map<Integer, List<LogEntry>> buffer;


	public Logger(GameWorld world) {
		this.world = world;
		this.loadcache = new HashMap<Integer, List<LogEntry>>();
		this.buffer = new HashMap<Integer, List<LogEntry>>();
	}
	
	
	/**
	 * Log's specific entry.
	 */
	public synchronized void log(LogEntry entry) {
		int day = (int)(entry.getDate() / 1000L / 60L / 60L / 24L);
		if (!buffer.containsKey(day)) 
			buffer.put(day, new ArrayList<LogEntry>());
		buffer.get(day).add(entry);
		if (loadcache.containsKey(day))
			loadcache.get(day).add(entry);
	}
	
	/**
	 * Save's all logs.
	 */
	public synchronized void save() {
		for (Integer day : buffer.keySet()) {
			List<LogEntry> entries = buffer.get(day);
			if (entries.size() < 1)
				continue;
			
			int fileid = 0;
			while (LoginFilesManager.containsLog(world.getId() + (world.getInformation().getPlayerFilesId() << 16), day, fileid))
				fileid++;
			
			LoginFilesManager.saveLogsFile(entries, world.getId() + (world.getInformation().getPlayerFilesId() << 16), day, fileid);
			if (loadcache.containsKey(day)) {
				List<LogEntry> cache = loadcache.get(day);
				for (LogEntry entry : entries)
					if (!cache.contains(entry))
						cache.add(entry);
			}
		}
		
		buffer.clear();
	}
	
	
	/**
	 * Queries all log entries from specific start to end date.
	 */
	public synchronized List<LogEntry> query(long start, long end) {
		int dstart = (int)(start / 1000L / 60L / 60L / 24L);
		int dend = (int)(end / 1000L / 60L / 60L / 24L);
	
		List<LogEntry> entries = new ArrayList<LogEntry>();
		for (int day = dstart; day <= dend; day++) {
			ensureLoad(day);	
			for (LogEntry dentry : loadcache.get(day)) {
				if (dentry.getDate() < start || dentry.getDate() > end)
					continue;
				entries.add(dentry);
			}
		}
		
		return entries;
	}
	 
	
	/**
	 * Load's and caches logs for specific day.
	 */
	private void ensureLoad(int day) {
		if (loadcache.containsKey(day))
			return;
		
		loadcache.put(day, new ArrayList<LogEntry>());
		if (buffer.containsKey(day))
			loadcache.get(day).addAll(buffer.get(day));
		
		for (int fileid = 0; LoginFilesManager.containsLog(world.getId() + (world.getInformation().getPlayerFilesId() << 16), day, fileid); fileid++)
			loadcache.get(day).addAll(LoginFilesManager.loadLogsFile(world.getId() + (world.getInformation().getPlayerFilesId() << 16), day, fileid));
	}
	
	

}
