package com.rs.game.log;

import java.io.Serializable;
import java.util.Arrays;

public class LogEntry implements Serializable {
	/**
	 * Serial number.
	 */
	private static final long serialVersionUID = -9141858878691509737L;
	

	private static final int LOG_TYPE_LOGIN = 0;
	private static final int LOG_TYPE_LOGOUT = 1;
	private static final int LOG_TYPE_ITEMDROP = 2;
	private static final int LOG_TYPE_ITEMTAKE = 3;
	
	/**
	 * Type of the entry.
	 */
	private int type;
	/**
	 * Date of the log.
	 */
	private long date;
	
	/**
	 * Int tags for this log entry.
	 */
	private int[] itags;
	
	/**
	 * String tags for this log entry.
	 */
	private String[] stags;
	
	private LogEntry() {
		
	}
	
	
	/**
	 * Constructs raw log entry from given data.
	 */
	public static LogEntry createRaw(int type, long date, int[] itags, String[] stags) {
		LogEntry entry = new LogEntry();
		entry.type = type;
		entry.date = date;
		entry.itags = itags;
		entry.stags = stags;
		return entry;
	}
	
	
	


	
	
	public int getType() {
		return type;
	}

	public long getDate() {
		return date;
	}
	
	public int[] getIntTags() {
		return itags;
	}
	
	public String[] getStringTags() {
		return stags;
	}


	public String toString() {
		switch (type) {
			case LOG_TYPE_LOGIN:
				return stags[0] + " has logged in. [" + stags[1] + "]";
			case LOG_TYPE_LOGOUT:
				return stags[0] + " has logged out. [" + stags[1] + "]";
			case LOG_TYPE_ITEMDROP:
				return stags[0] + " has dropped Item" + "(" + itags[0] + "," + itags[1] + ").";
			case LOG_TYPE_ITEMTAKE:
				return stags[0] + " has dropped Item" + "(" + itags[0] + "," + itags[1] + ").";
			default:
				return "Generic log entry[" + type + "," + date + "," + Arrays.toString(itags) + "," + Arrays.toString(stags) + "]";
		}
	}
	
	

}
