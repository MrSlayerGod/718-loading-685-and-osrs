package com.rs.game.player;

import java.util.HashMap;
import java.util.Map;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.VarBitDefinitions;

public class VarsManager {

	//custom vars. keep track here to know
	public static final int VARS_COUNT = 0; //customs included.
	
	private static final int[] masklookup = new int[32];

	static {
		int i = 2;
		for (int i2 = 0; i2 < 32; i2++) {
			masklookup[i2] = i - 1;
			i += i;
		}
	}

	private int[] values;
	private Player player;
	
	private Map<Integer, Integer> osrsVars;

	public VarsManager(Player player) {
		this.player = player;
		values = new int[Math.max(VARS_COUNT, Cache.STORE.getIndexes()[2].getLastFileId(16) + 1)];
		osrsVars = new HashMap<Integer, Integer>();
	}

	public void sendVar(int id, int value) {
		sendVar(id, value, false);
	}

	public void forceSendVar(int id, int value) {
		sendVar(id, value, true);
	}

	private void sendVar(int id, int value, boolean force) {
		if (id >= Settings.OSRS_OBJECTS_OFFSET) {
			Integer oldValue = osrsVars.get(id);
			if (oldValue == null)
				oldValue = 0;
			if (!force && oldValue == value)
				return;
			osrsVars.put(id, value);
			sendOSRSObjectVar(id);
			return;
		}
		if (id < 0 || id >= values.length) // temporarly
			return;
		if (!force && values[id] == value)
			return;
		setVar(id, value);
		sendClientVarp(id);
	}

	public void setVar(int id, int value) {
		if (id == -1) // temporarly
			return;
		values[id] = value;
	}

	public int getValue(int id) {
		if (id >= Settings.OSRS_OBJECTS_OFFSET) {
			Integer value = osrsVars.get(id);
			return value == null ? 0 : value;
		}
		return values[id];
	}

	public void forceSendVarBit(int id, int value) {
		setVarBit(id, value, 0x1 | 0x2);
	}

	public void sendVarBit(int id, int value) {
		setVarBit(id, value, 0x1);
	}

	public void setVarBit(int id, int value) {
		setVarBit(id, value, 0);
	}

	public int getBitValue(int id) {
		VarBitDefinitions defs = VarBitDefinitions.getClientVarpBitDefinitions(id);
		return values[defs.baseVar] >> defs.startBit & masklookup[defs.endBit - defs.startBit];
	}

	private void setVarBit(int id, int value, int flag) {
		if (id == -1) // temporarly
			return;
		VarBitDefinitions defs = VarBitDefinitions.getClientVarpBitDefinitions(id);
		int mask = masklookup[defs.endBit - defs.startBit];
		if (value < 0 || value > mask)
			value = 0;
		mask <<= defs.startBit;
		int varpValue = (values[defs.baseVar] & (mask ^ 0xffffffff) | value << defs.startBit & mask);
		if ((flag & 0x2) != 0 || varpValue != values[defs.baseVar]) {
			setVar(defs.baseVar, varpValue);
			if ((flag & 0x1) != 0)
				sendClientVarp(defs.baseVar);
		}
	}
	private void sendOSRSObjectVar(int id) {
		Integer value = osrsVars.get(id);
		player.getPackets().sendExecuteScript(-13, id, (value == null ? 0 : value));
	}
	
	@SuppressWarnings("deprecation")
	private void sendClientVarp(int id) {
		player.getPackets().sendVar(id, values[id]);
	}
}
