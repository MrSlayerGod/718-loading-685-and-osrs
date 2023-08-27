package com.rs.login;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rs.utils.Utils;

public class Offences implements Serializable {

	/**
	 * Our serial UID.
	 */
	private static final long serialVersionUID = 8210363743646992498L;

	/**
	 * All ip bans
	 */
	private List<Metadata> ipBans;
	/**
	 * All ip mutes
	 */
	private List<Metadata> ipMutes;
	/**
	 * All bans
	 */
	private List<Metadata> bans;
	/**
	 * All mutes
	 */
	private List<Metadata> mutes;
	
	/**
	 * All ip bans
	 */
	private List<Metadata> archivedIpBans;
	/**
	 * All ip mutes
	 */
	private List<Metadata> archivedIpMutes;
	/**
	 * All bans
	 */
	private List<Metadata> archivedBans;
	/**
	 * All mutes
	 */
	private List<Metadata> archivedMutes;

	public Offences() {
		ipBans = new ArrayList<Metadata>();
		ipMutes = new ArrayList<Metadata>();
		bans = new ArrayList<Metadata>();
		mutes = new ArrayList<Metadata>();
		archivedIpBans = new ArrayList<Metadata>();
		archivedIpMutes = new ArrayList<Metadata>();
		archivedBans = new ArrayList<Metadata>();
		archivedMutes = new ArrayList<Metadata>();
	}
	
	/**
	 * Happens when offences are loaded.
	 */
	public synchronized void onLoad() {
		// first, we need to assign uids.
		for (Metadata m : ipBans)
			m.uid = Metadata.uidCounter++;
		for (Metadata m : ipMutes)
			m.uid = Metadata.uidCounter++;
		for (Metadata m : bans)
			m.uid = Metadata.uidCounter++;
		for (Metadata m : mutes)
			m.uid = Metadata.uidCounter++;
		
		if (archivedIpBans == null)
			archivedIpBans = new ArrayList<Metadata>();
		if (archivedIpMutes == null)
			archivedIpMutes = new ArrayList<Metadata>();
		if (archivedBans == null)
			archivedBans = new ArrayList<Metadata>();
		if (archivedMutes == null)
			archivedMutes = new ArrayList<Metadata>();
		
		for (Metadata m : archivedIpBans)
			m.uid = -1;
		for (Metadata m : archivedIpMutes)
			m.uid = -1;
		for (Metadata m : archivedBans)
			m.uid = -1;
		for (Metadata m : archivedMutes)
			m.uid = -1;
			
	}

	/**
	 * Whether this ip is banned.
	 */
	public synchronized boolean isIpBanned(String ip) {
		for (Metadata m : ipBans)
			if (m.getIp() != null && m.getIp().equals(ip) && !m.hasExpired())
				return true;
		return false;
	}
	
	public synchronized boolean isMacBanned(String mac) {
		if (mac == null || mac.equalsIgnoreCase("00-00-00-00-00"))
			return false;
		for (Metadata m : ipBans)
			if (m.getMac() != null && m.getMac().equals(mac) && !m.hasExpired())
				return true;
		return false;
	}

	/**
	 * Whether this ip is muted.
	 */
	public synchronized boolean isIpMuted(String ip) {
		for (Metadata m : ipMutes)
			if (m.getIp().equals(ip) && !m.hasExpired())
				return true;
		return false;
	}
	
	public synchronized boolean isMacMuted(String mac) {
		if (mac == null || mac.equalsIgnoreCase("00-00-00-00-00"))
			return false;
		for (Metadata m : ipMutes)
			if (m.getMac() != null && m.getMac().equals(mac) && !m.hasExpired())
				return true;
		return false;
	}

	/**
	 * Whether this user is banned.
	 */
	public synchronized boolean isBanned(String username) {
		if (username == null)
			return false;
		for (Metadata m : bans)
			if (m.getUsername() != null && m.getUsername().equals(username) && !m.hasExpired())
				return true;
		for (Metadata m : ipBans)
			if (m.getUsername() != null && m.getUsername().equals(username) && !m.hasExpired())
				return true;
		return false;
	}

	/**
	 * Whether this user is banned.
	 */
	public synchronized boolean isMuted(String username) {
		if (username == null)
			return false;
		for (Metadata m : mutes)
			if (m.getUsername() != null && m.getUsername().equals(username) && !m.hasExpired())
				return true;
		for (Metadata m : ipMutes)
			if (m.getUsername() != null && m.getUsername().equals(username) && !m.hasExpired())
				return true;
		return false;
	}
	
	public synchronized long getExpirationTime(String username) {
		if (username == null)
			return -1;
		for (Metadata m : mutes)
			if (m.getUsername() != null && m.getUsername().equals(username) && !m.hasExpired())
				return m.getExpires();
		for (Metadata m : ipMutes)
			if (m.getUsername() != null && m.getUsername().equals(username) && !m.hasExpired())
				return m.getExpires();
		return -1;
	}
	
	
	/**
	 * Find's offence by it's uid.
	 */
	public synchronized Metadata findOffence(int uid) {
		// TODO better search
		for (Metadata m : ipBans)
			if (m.uid == uid)
				return m;
		for (Metadata m : ipMutes)
			if (m.uid == uid)
				return m;
		for (Metadata m : bans)
			if (m.uid == uid)
				return m;
		for (Metadata m : mutes)
			if (m.uid == uid)
				return m;
		return null;
	}

	/**
	 * Find's all offences made by given username's or ip's.
	 */
	public synchronized Map<Integer, List<Metadata>> findAllOffences(List<String> usernames, List<String> ips) {
		Map<Integer, List<Metadata>> offences = new HashMap<Integer, List<Metadata>>();
		for (int i = 0; i < 4; i++)
			offences.put(i, new ArrayList<Metadata>());
		for (Metadata m : ipBans)
			if (usernames.contains(m.getUsername()) || ips.contains(m.getIp()))
				offences.get(0).add(m);
		for (Metadata m : archivedIpBans)
			if (usernames.contains(m.getUsername()) || ips.contains(m.getIp()))
				offences.get(0).add(m);
		for (Metadata m : ipMutes)
			if (usernames.contains(m.getUsername()) || ips.contains(m.getIp()))
				offences.get(1).add(m);
		for (Metadata m : archivedIpMutes)
			if (usernames.contains(m.getUsername()) || ips.contains(m.getIp()))
				offences.get(1).add(m);
		for (Metadata m : bans)
			if (usernames.contains(m.getUsername()) || ips.contains(m.getIp()))
				offences.get(2).add(m);
		for (Metadata m : archivedBans)
			if (usernames.contains(m.getUsername()) || ips.contains(m.getIp()))
				offences.get(2).add(m);
		for (Metadata m : mutes)
			if (usernames.contains(m.getUsername()) || ips.contains(m.getIp()))
				offences.get(3).add(m);
		for (Metadata m : archivedMutes)
			if (usernames.contains(m.getUsername()) || ips.contains(m.getIp()))
				offences.get(3).add(m);
		return offences;
	}

	/**
	 * Add's ip ban with given details.
	 */
	public synchronized void ipBan(String username, String ip, String mac, String moderator, String reason, long expires) {
		ipBans.add(new Metadata(username, ip, mac, moderator, reason, expires));
	}

	/**
	 * Add's ip mute with given details.
	 */
	public synchronized void ipMute(String username, String ip, String mac, String moderator, String reason, long expires) {
		ipMutes.add(new Metadata(username, ip, mac, moderator, reason, expires));
	}

	/**
	 * Add's ban with given details.
	 */
	public synchronized void ban(String username, String ip, String mac, String moderator, String reason, long expires) {
		bans.add(new Metadata(username, ip, mac, moderator, reason, expires));
	}

	/**
	 * Add's mute with given details.
	 */
	public synchronized void mute(String username, String ip, String mac, String moderator, String reason, long expires) {
		mutes.add(new Metadata(username, ip, mac, moderator, reason, expires));
	}

	/**
	 * Remove's all bans for specific user.
	 */
	public synchronized int unbanByUser(String username) {
		if (username == null)
			return 0;
		int count = 0;
		Iterator<Metadata> it$ = ipBans.iterator();
		while (it$.hasNext()) {
			Metadata m = it$.next();
			if (m.getUsername() != null && m.getUsername().equals(username)) {
				it$.remove();
				count++;
			}
		}

		it$ = bans.iterator();
		while (it$.hasNext()) {
			Metadata m = it$.next();
			if (m.getUsername() != null && m.getUsername().equals(username)) {
				it$.remove();
				count++;
			}
		}

		return count;
	}

	/**
	 * Remove's all mutes for specific user.
	 */
	public synchronized int unmuteByUser(String username) {
		if (username == null)
			return 0;
		int count = 0;
		Iterator<Metadata> it$ = ipMutes.iterator();
		while (it$.hasNext()) {
			Metadata m = it$.next();
			if (m.getUsername() != null && m.getUsername().equals(username)) {
				it$.remove();
				count++;
			}
		}

		it$ = mutes.iterator();
		while (it$.hasNext()) {
			Metadata m = it$.next();
			if (m.getUsername() != null && m.getUsername().equals(username)) {
				it$.remove();
				count++;
			}
		}

		return count;
	}

	/**
	 * Clean's up by removing expired offences.
	 */
	public synchronized void cleanup() {
		cleanup(ipBans, archivedIpBans);
		cleanup(ipMutes, archivedIpMutes);
		cleanup(bans, archivedBans);
		cleanup(mutes, archivedMutes);
	}

	/**
	 * Clean's up specific offences list.
	 */
	private void cleanup(List<Metadata> list, List<Metadata> archive) {
		Iterator<Metadata> it$ = list.iterator();
		while (it$.hasNext()) {
			Metadata metadata = it$.next();
			if (metadata.hasExpired()) {
				metadata.uid = -1;
				archive.add(metadata);
				it$.remove();
			}
		}

	}

	/**
	 * Metadata for offence. None of the fields inside are guaranteed to be
	 * filled.
	 */
	public static class Metadata implements Serializable {
		/**
		 * Our serial version UID.
		 */
		private static final long serialVersionUID = -5082795333477460692L;
		/**
		 * Contains uid counter.
		 */
		private static int uidCounter;

		/**
		 * Unique id of this offence.
		 */
		private transient int uid;
		/**
		 * Target username who was taken action against. (Even if it was ipban
		 * or ipmute)
		 */
		private String username;
		/**
		 * Time when this offence was added.
		 */
		private long time;
		/**
		 * Ip of the offender.
		 */
		private String ip;
		private String mac;
		/**
		 * Username of moderator who did action.
		 */
		private String moderator;
		/**
		 * Moderator reasonining.
		 */
		private String reason;
		/**
		 * Time when this offence expires.
		 */
		private long expires;
		
		/**
		 * State of this offence:
		 * 0 - Appeal available.
		 * 1 - Appeal submitted.
		 * 2 - Appeal denied.
		 * 3 - Appeal accepted.
		 * 4 - Offence reversed.
		 */
		private int state;
		/**
		 * Appeal text.
		 */
		private String appeal;
		/**
		 * Appeal response text.
		 */
		private String response;

		public Metadata(String username, String ip, String mac, String moderator, String reason, long expires) {
			this.uid = uidCounter++;
			this.time = Utils.currentTimeMillis();
			this.username = username;
			this.ip = ip;
			this.mac = mac;
			this.moderator = moderator;
			this.reason = reason;
			this.expires = expires;
			
			this.state = 0;
			this.appeal = null;
			this.response = null;
		}
		
		
		/**
		 * Reset's appeal data.
		 */
		public void resetAppeal() {
			if (this.uid == -1)
				return;
			this.state = 0;
			this.appeal = null;
			this.response = null;
		}
		
		/**
		 * Initiate's appeal for this offence.
		 */
		public void initiateAppeal(String appeal) {
			if (this.uid == -1)
				return;
			this.state = 1;
			this.appeal = appeal;
			this.response = null;
		}
		
		/**
		 * Update's appeal data.
		 */
		public void updateAppeal(int state, String response) {
			if (this.uid == -1 || this.state != 1)
				return;
			this.state = state;
			this.response = response;
		}
		
		/**
		 * Reverse's this offence.
		 */
		public void reverse() {
			if (this.uid == -1)
				return;
			this.appeal = null;
			this.response = null;
			this.state = 4;
		}
		
		
		public int getUid() {
			return uid;
		}

		public boolean hasExpired() {
			return state == 3 || state == 4 || Utils.currentTimeMillis() >= expires;
		}

		public long getTime() {
			return time;
		}

		public String getUsername() {
			return username;
		}

		public String getIp() {
			return ip;
		}
		
		public String getMac() {
			return mac;
		}

		public String getModerator() {
			return moderator;
		}

		public String getReason() {
			return reason;
		}

		public long getExpires() {
			return expires;
		}

		public int getState() {
			return state;
		}

		public String getAppeal() {
			return appeal;
		}

		public String getResponse() {
			return response;
		}


	}
	
	

}
