package com.rs.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rs.Settings;
import com.rs.game.log.LogEntry;
import com.rs.login.DisplayNames;
import com.rs.login.Offences;
import com.rs.login.account.Account;

public class LoginFilesManager {

	/**
	 * Path of the folder where account files are stored.
	 */
	private static final String ACCOUNTS_PATH = "account/";
	private static final String ACCOUNTS_BACKUP_PATH = "accountB/";
	/**
	 * Path of the folder where log files are stored.
	 */
	private static final String LOGS_PATH = "log/";
	/**
	 * Path where offences are stored.
	 */
	private static final String OFFENCES = "offences.ser";
	/**
	 * Path where display names are stored.
	 */
	private static final String DISPLAY_NAMES = "displayNames.ser";

	/**
	 * Filesystem, into which files are stored, might be null.
	 */
//	private static MiniFS filesystem;

	public static synchronized void init() {
		/*File f = new File(ACCOUNTS_BACKUP_PATH);
		try {
			if (Settings.HOSTED)
				filesystem = MiniFS.open(Settings.LOGIN_DATA_PATH);
			else {
				if (new File(Settings.LOGIN_DATA_PATH + "_" + System.getProperty("user.name") + ".data").exists()) {
					filesystem = MiniFS.open(Settings.LOGIN_DATA_PATH + "_" + System.getProperty("user.name"));
				} else {
					Utils.copyFile(new File(Settings.LOGIN_DATA_PATH + "_Admin.data"), new File(Settings.LOGIN_DATA_PATH + "_" + System.getProperty("user.name") + ".data"));
					filesystem = MiniFS.open(Settings.LOGIN_DATA_PATH + "_" + System.getProperty("user.name"));
				}
			}
		} catch (Throwable t) {
			Logger.handle(t);
			throw new Error("Failed to load file system.");
		}*/
	}

	public static synchronized void flush() {
		/*try {
			boolean ok = filesystem.flush();
			if (!ok)
				throw new RuntimeException("Couldn't flush fs.");
		} catch (Throwable t) {
			Logger.handle(t);
		}*/
	}

	public synchronized static String[] getAllAccounts() {
		return listFiles(ACCOUNTS_PATH);
	//	return filesystem.listFiles(ACCOUNTS_PATH);
	}

	public synchronized static boolean containsAccount(String username) {
		return fileExists(ACCOUNTS_PATH + username + ".ser");
		//return filesystem.fileExists(ACCOUNTS_PATH + username + ".acc");
	}
	
	private synchronized static String[] listFiles(String path) {
		return new File(Settings.LOGIN_DATA_PATH + path).list();
	}
	
	private synchronized static boolean fileExists(String path) {
		return new File(Settings.LOGIN_DATA_PATH + path).exists();
	}
	

	public synchronized static Account loadAccount(String username) {
		Account account = null;
		try {
			account = (Account) loadObject(ACCOUNTS_PATH + username + ".ser");
		} catch (Throwable e) {
			Logger.handle(e);
		}
		if (account == null) { //tries to recover if fails to load
			try {
				account = (Account) loadObject(ACCOUNTS_BACKUP_PATH + username + ".ser");
			} catch (Throwable e) {
				Logger.handle(e);
			}
			
		} else {
			try {
				storeObject(account, ACCOUNTS_BACKUP_PATH + username + ".ser");
			} catch (Throwable e) {
				Logger.handle(e);
			}
		}
		return account;
	}

	public synchronized static void saveAccount(Account account) {
		try {
			/*if (account.isMasterLogin())
				return;*/
			storeObject(account, ACCOUNTS_PATH + account.getUsername() + ".ser");
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}
	
	
	public synchronized static boolean containsLog(int worldprefx, int day, int log) {
		return /*filesystem.*/fileExists(LOGS_PATH + worldprefx + "/" + day + "/" + log + ".ser");
	}
	
	@SuppressWarnings("unchecked")
	public synchronized static List<LogEntry> loadLogsFile(int worldprefx, int day, int log) {
		try {
			return (List<LogEntry>) loadObject(LOGS_PATH + worldprefx + "/" + day + "/" + log + ".ser");
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
	
	public static synchronized void saveLogsFile(List<LogEntry> logs, int worldprefx, int day, int log) {
		try {
			storeObject((Serializable) logs, LOGS_PATH + worldprefx + "/" + day + "/" + log + "ser");
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}

	public static synchronized Offences loadOffences() {
		if (/*filesystem.*/fileExists(OFFENCES)) {
			try {
				Offences off = (Offences) loadObject(OFFENCES);
				off.onLoad();
				return off;
			} catch (Throwable t) {
				Logger.handle(t);
				return null;
			}
		} else {
			return new Offences();
		}
	}

	public static synchronized void saveOffences(Offences offences) {
		try {
			storeObject(offences, OFFENCES);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}

	public static synchronized DisplayNames loadDisplayNames() {
		if (/*filesystem.*/fileExists(DISPLAY_NAMES)) {
			try {
				return (DisplayNames) loadObject(DISPLAY_NAMES);
			} catch (Throwable t) {
				Logger.handle(t);
				return null;
			}
		} else {
			return new DisplayNames();
		}
	}

	public static synchronized void saveDisplayNames(DisplayNames displayNames) {
		try {
			storeObject(displayNames, DISPLAY_NAMES);
		} catch (Throwable t) {
			Logger.handle(t);
		}
	}


	private static final Object SER_LOCK = new Object();
	
	private static synchronized Object loadObject(String f) throws IOException, ClassNotFoundException {
		synchronized (SER_LOCK) {
			//	byte[] data = filesystem.getFile(f);
			if (!fileExists(f))
				return null;
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(Settings.LOGIN_DATA_PATH + f));
			Object object = in.readObject();
			in.close();
			return object;
		}
	}

	private static synchronized void storeObject(Serializable o, String f) throws IOException {
		synchronized (SER_LOCK) {
			//ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Settings.LOGIN_DATA_PATH + f));
			out.writeObject(o);
			out.flush();
		/*	boolean ok = filesystem.putFile(f, baos.toByteArray());
			if (!ok)
				throw new RuntimeException("Couldn't put file");*/
			out.close();

		}
	}

	public static void main(String args[]){
		try {

			// Encode using basic encoder
			// I assume you replace this with actual data...
			System.out.println(new File("data/login/account/test.ser").getAbsolutePath());
			byte[] data = Files.readAllBytes(new File("C:\\Users\\Alex\\IdeaProjects\\onyx-server\\onyx-server\\data\\login\\account/test.ser").toPath());
		System.out.println(data);

			// Decode
			byte[] base64decodedBytes = Base64.getDecoder().decode(data);

			InputStream in = new ByteArrayInputStream(base64decodedBytes);
			ObjectInputStream obin = new ObjectInputStream(in);
			Object object = obin.readObject();
			System.out.println("Deserialised data: \n" + object.toString());

			// You could also try...
			System.out.println("Object class is " + object.getClass().toString());

			// Don't do this!! The original data was not a string!
			//System.out.println("Original String: " + new     String(base64decodedBytes, "utf-8"));


		}catch(ClassNotFoundException | IOException e){
			System.out.println("Error :" + e.getMessage());
		}
	}

}
