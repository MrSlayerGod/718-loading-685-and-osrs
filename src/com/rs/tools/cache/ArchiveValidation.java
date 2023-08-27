/**
 * 
 */
package com.rs.tools.cache;

import java.io.IOException;

import com.alex.store.Archive;
import com.alex.store.ArchiveReference;
import com.alex.store.Index;
import com.rs.cache.Cache;

/**
 * @author dragonkk(Alex)
 * Sep 5, 2017
 */
public class ArchiveValidation {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Cache.init();
		for(int i = 0; i < Cache.STORE.getIndexes().length; i++) {
			if(i == 5)
				continue;
			Index index = Cache.STORE.getIndexes()[i];
			System.out.println("checking index: "+i);
			for(int archiveId : index.getTable().getValidArchiveIds()) {
				Archive archive = index.getArchive(archiveId);
				if(archive == null) {
					System.out.println("Missing:: "+i+", "+archiveId);
					continue;
				}
				ArchiveReference reference = index.getTable().getArchives()[archiveId];
				if(archive.getRevision() != reference.getRevision() ) {
					System.out.println("corrupted: "+i+", "+archiveId);
				}
			}
		}
	}
	
}
