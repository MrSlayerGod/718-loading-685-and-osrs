/**
 * 
 */
package com.rs.tools.cache;

import java.io.IOException;

import com.rs.cache.Cache;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Sep 7, 2017
 */
public class PackAnims {

	public static void main(String[] args) throws IOException {
	/*	ReferenceTable.NEW_PROTOCOL = true;
		Store rs3Cache = new Store("C:\\Users\\alex_\\Downloads\\830_cache\\830_cache\\");
		ReferenceTable.NEW_PROTOCOL = false;
		Cache.init();
		Cache.STORE.getIndexes()[0].packIndex(rs3Cache);
		Cache.STORE.getIndexes()[1].packIndex(rs3Cache);
		Cache.STORE.getIndexes()[20].packIndex(rs3Cache);*/
		Cache.init();
		System.out.println(Utils.getAnimationDefinitionsSize());
		System.out.println(Utils.getGraphicDefinitionsSize());
		System.out.println(Utils.getNPCDefinitionsSize());
	}
	
	/*
	 * 17812
		3448
	 */
	
	
}
