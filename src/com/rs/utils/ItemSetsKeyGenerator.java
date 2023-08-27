package com.rs.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Do not use. Not correct.
 * Blame my lack of knowledge back then.
 */
public class ItemSetsKeyGenerator {

	
	public static final int COLLECTOR_LOG_KEY = 1000; //above 1000 as nextkey is below 999.need a fixed key to do inv
	
	private final static AtomicInteger nextKey = new AtomicInteger(999); // After

	// 400
	// keys
	// uses
	// negative
	// keys

	public static final int generateKey() {
		int key = nextKey.getAndDecrement();
		if (key > 0 && key <= 100)
			nextKey.set(-1); // starts at negative
		return key;
	}
}
