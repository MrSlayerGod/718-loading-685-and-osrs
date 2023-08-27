/**
 * 
 */
package com.rs.tools.cache;

import java.io.IOException;

import com.alex.store.Index;
import com.alex.store.Store;
import com.alex.utils.Constants;
import com.rs.cache.Cache;

/**
 * @author dragonkk(Alex)
 * Sep 5, 2017
 */
public class CopyCache {


	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Cache.init();
		Store cache = Cache.STORE;
		Store newCache = new Store("data/cachec/");
		for(int i = 0; i < cache.getIndexes().length; i++) {
			Index index = cache.getIndexes()[i];
			newCache.addIndex(index.getTable().isNamed(), index.getTable().usesWhirpool(), Constants.GZIP_COMPRESSION);
			newCache.getIndexes()[i].packIndex(cache);
			newCache.getIndexes()[i].getTable().setRevision(cache.getIndexes()[i].getTable().getRevision());
			newCache.getIndexes()[i].rewriteTable();
		}
	}
}
