package com.rs.grab;

import java.util.ArrayDeque;
import java.util.Queue;

import com.rs.cache.Cache;
import com.rs.net.Session;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

public class Grab {
	//can only send 2 archive at a time atm 
	//done so that while archive1 is being sent, archieve2 gets loaded and cached into stream
	//and viceversa (for maximum perfomance)
	//cached at 2 and not more so that the stream doesnt get flooded(meaning priority files would take longer)
	private static final int SENDING_LIMIT = 2;
	private Queue<Integer> priorityArchives, nonPriorityArchives;
	private int sendingCount, encryptionValue;
	
	private boolean initialized;
	private boolean isLoggedIn;

	private Session session;

	public Grab(Session session) {
		this.session = session;
		priorityArchives = new ArrayDeque<Integer>(50);
		nonPriorityArchives = new ArrayDeque<Integer>(20);
	}

	public void requestArchive(int index, int archive, boolean priority) {
		if (!initialized) {
			finish();
			return;
		}
		if (index == 255 && archive != 255) {
			if (archive < 0 || archive >= Cache.STORE.getIndexes().length || Cache.STORE.getIndexes()[archive] == null)
				return;
		}
		else if (index != 255) {
			if (index < 0 || index >= Cache.STORE.getIndexes().length || Cache.STORE.getIndexes()[index] == null || 
				archive < 0 || !Cache.STORE.getIndexes()[index].archiveExists(archive))
				return;
		}
		int archiveUID = index << 24 | archive;
		//turns non priority into priority and caps at 1 per archive
		if(nonPriorityArchives.contains(archiveUID)) {
			if(priority)
				nonPriorityArchives.remove(archiveUID);
			else 
				return;
		}else if (priorityArchives.contains(archiveUID))
			return;
		if((priority ? priorityArchives.size() : nonPriorityArchives.size()) > (priority ? 50 : 20)) 
			return;
		if(sendingCount < SENDING_LIMIT) {
			sendArchive(index, archive, priority);
			return;
		}
		if(priority)
			priorityArchives.offer(archiveUID);
		else
			nonPriorityArchives.offer(archiveUID);
		trySendArchive();
	}
	
	public void trySendArchive() {
		if(sendingCount >= SENDING_LIMIT || !initialized)
			return;
		boolean priority = true;
		Integer archiveUID = priorityArchives.poll();
		if(archiveUID == null) {
			archiveUID = nonPriorityArchives.poll();
			priority = false;
		}
		if(archiveUID == null)
			return;
		sendArchive((archiveUID >> 24) & 0xff, archiveUID & 0xffffff, priority);
	}
	
	@SuppressWarnings("unchecked")
	public void sendArchive(int folderID, int archiveID, boolean priority) {
		sendingCount++;
		Future future = session.getGrabPackets().sendArchive(folderID, archiveID, priority, encryptionValue);
		if(future == null) { //means dced 
			sendingCount--;
			return;
		}
		future.addListener(new FutureListener() {
			@Override
			public void operationComplete(Future future) throws Exception {
				sendingCount--;
				//update server closed or something
				if(!future.isSuccess())
					return;
				trySendArchive();
			}
		});
	}
	
	

	public void init() {
		if (initialized) {
			finish();
			return;
		}
		initialized = true;
	}

	/*
	 * force close update server due to either error from client or hack attempt
	 */
	public void finish() {
		if (session.getChannel() != null)
			session.getChannel().close();
		if (!initialized)
			return;
		initialized = false;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean clientLoggedIn) {
		if (!initialized) {
			finish();
			return;
		}
		this.isLoggedIn = clientLoggedIn;
	}

	public Session getSession() {
		return session;
	}
	
	/**
	 * @return the encryptionValue
	 */
	public int getEncryptionValue() {
		return encryptionValue;
	}

	/**
	 * @param encryptionValue the encryptionValue to set
	 */
	public void setEncryptionValue(int encryptionValue) {
		this.encryptionValue = encryptionValue;
	}

}
