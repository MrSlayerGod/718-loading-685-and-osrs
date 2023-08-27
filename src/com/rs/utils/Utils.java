package com.rs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.rs.cache.Cache;
import com.rs.executor.WorldThread;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import vpn.detection.Response;
import vpn.detection.VPNDetection;

public final class Utils {

	private static final Object ALGORITHM_LOCK = new Object();

	private static final long INIT_MILLIS = System.currentTimeMillis();
	private static final long INIT_NANOS = System.nanoTime();

	private static long millisSinceClassInit() {
		return (System.nanoTime() - INIT_NANOS) / 1000000;
	}

	public static long currentTimeMillis() {
		return INIT_MILLIS + millisSinceClassInit();
	}

	private static final SecureRandom random = new SecureRandom();

	public static String aOrAn(String word) {
		if(word == null || word.length() == 0)
			return "a";
		char c = word.charAt(0);
		return c=='a' || c=='e' || c=='i' || c== 'o' || c=='u' ? "an" : "a";
	}



	private static VPNDetection vpnDetection = new VPNDetection();

	public static boolean checkVPN(String ip) {
		//System.out.println("proxy check");
		String ipToLookup = ip;

		//new Thread(() -> {
		try {
			vpnDetection.set_api_key("hXBsHMS4gwGn7VMHAL3PhSTeQv86ZF");
			Response api_response = vpnDetection.getResponse(ipToLookup);

			if(api_response.status.equals("success")) {
				//System.out.println("Package: " + api_response.getPackage);
				if(api_response.getPackage.equals("Free")) {
					System.out.println("Remaining Requests: " + api_response.remaining_requests);
				}
				//System.out.println("IP Address: " + api_response.ipaddress);
				// System.out.println("Is this IP a VPN or Hosting Network? " + api_response.hostip);
				// System.out.println("Organisation: " + api_response.org);
				// if(api_response.country != null) {
					//System.out.println("Country: " + api_response.country.name);
				// }

				return  api_response.hostip;
			} else {
				System.out.println("VPN check error: " + api_response.msg);
			}

		} catch (IOException ex) {
			System.out.println("Error checking VPN: " + ex.getMessage());
		}
		//}).start();

		return false;
	}

	public static boolean containsIgnoreCase(String str, String searchStr)     {
		if(str == null || searchStr == null) return false;

		final int length = searchStr.length();
		if (length == 0)
			return true;

		for (int i = str.length() - length; i >= 0; i--) {
			if (str.regionMatches(true, i, searchStr, 0, length))
				return true;
		}
		return false;
	}

	public static <T extends Enum<?>> T randomEnum(Class<T> clazz){
		int x = random.nextInt(clazz.getEnumConstants().length);
		return clazz.getEnumConstants()[x];
	}

	public static char[] stringToCharArr(String s) {
		char[] c = new char[s.length()];
		for(int i = 0; i < s.length(); i++)
			c[i] = s.charAt(i);
		return c;
	}

	public static int calcLevenshteinDistance( String string1, String string2 ) {
		char[] s1 = stringToCharArr(string1);
		char[] s2 = stringToCharArr(string2);

		// memoize only previous line of distance matrix
		int[] prev = new int[ s2.length + 1 ];

		for( int j = 0; j < s2.length + 1; j++ ) {
			prev[ j ] = j;
		}

		for( int i = 1; i < s1.length + 1; i++ ) {

			// calculate current line of distance matrix
			int[] curr = new int[ s2.length + 1 ];
			curr[0] = i;

			for( int j = 1; j < s2.length + 1; j++ ) {
				int d1 = prev[ j ] + 1;
				int d2 = curr[ j - 1 ] + 1;
				int d3 = prev[ j - 1 ];
				if ( s1[ i - 1 ] != s2[ j - 1 ] ) {
					d3 += 1;
				}
				curr[ j ] = Math.min( Math.min( d1, d2 ), d3 );
			}

			// define current line of distance matrix as previous
			prev = curr;
		}
		return prev[ s2.length ];
	}
	/**
	 * Split long item names into 2 lines
	 */
	public static String splitString(String s, int minLen) {
		if(s.length() <= minLen) return s;

		int split = -1, h = (int) ((double)s.length()/2);

		for(int i = h, j = h; i < s.length() || j > -1; i++, j--) {
			if (i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '-')) {
				split = i;
				break;
			}
			if (j > -1 && (s.charAt(j) == ' ' || s.charAt(j) == '-')) {
				split = j;
				break;
			}
		}
		return s.substring(0, split+1) + "<br>" + s.substring(split);
	}

	public static <T> T[] concatenate(T[] a, T[] b) {
		int aLen = a.length;
		int bLen = b.length;

		@SuppressWarnings("unchecked")
		T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);

		return c;
	}

	/*
	 * world cycles, each is 600ms :). its 100% safe to use :p example of usage
	 * well doesnt save with restarts it should work fine for disabled.
	 *  its bad dont use for things that save
	 *  ofc good for stuff that doesnt save such as temporary args and delays
	 */
	public static long currentWorldCycle() {
		return WorldThread.WORLD_CYCLE;
	}

	/*
	 * private static long timeCorrection; private static long lastTimeUpdate;
	 *
	 * public static synchronized long currentTimeMillis() { long l =
	 * System.currentTimeMillis(); if (l < lastTimeUpdate) timeCorrection +=
	 * lastTimeUpdate - l; lastTimeUpdate = l; return l + timeCorrection; }
	 */

	public static boolean intOverflow(int int1, int int2) {
		return  ((long) int1 + int2) > Integer.MAX_VALUE;
	}

	private static final DecimalFormat dFormatter = new DecimalFormat("#,###,###,###");

	public static String getFormattedNumber(long amount) {
		return dFormatter.format(amount);
	}

	public static String formatTime(long time) {
		long seconds = time / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		seconds = seconds % 60;
		minutes = minutes % 60;
		hours = hours % 24;
		StringBuilder string = new StringBuilder();
		string.append(hours > 9 ? hours : ("0" + hours));
		string.append(":" + (minutes > 9 ? minutes : ("0" + minutes)));
		string.append(":" + (seconds > 9 ? seconds : ("0" + seconds)));
		return string.toString();
	}

	/**
	 * append hours when > 1, always show minutes (no 00)
	 */
	public static String formatTimeCox(long time) {
		long seconds = time / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		seconds = seconds % 60;
		minutes = minutes % 60;
		hours = hours % 24;
		StringBuilder string = new StringBuilder();
		if(hours != 0)
			string.append(hours + ":");
		string.append(minutes + ":");
		string.append((seconds > 9 ? seconds : ("0" + seconds)));
		return string.toString();
	}

	public static String formatTime2(long time) {
		long seconds = time / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		seconds = seconds % 60;
		minutes = minutes % 60;
		hours = hours % 24;
		StringBuilder string = new StringBuilder();
		if(hours>0)
			string.append(hours > 9 ? hours : ("" + hours) + " hours").append(" ");
		if(minutes>0)
			string.append((minutes > 9 ? minutes : ("" + minutes)) + " minutes").append(" ");
		string.append((seconds > 9 ? seconds : ("" + seconds)) + " seconds");
		return string.toString().trim();
	}
	public static String formatTimeShorthand(long time) {
		long seconds = time / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		seconds = seconds % 60;
		minutes = minutes % 60;
		hours = hours % 24;
		StringBuilder string = new StringBuilder();
		if(hours>0)
			string.append(hours > 9 ? hours : ("" + hours) + " h").append(" ");
		if(minutes>0)
			string.append((minutes > 9 ? minutes : ("" + minutes)) + " m").append(" ");
		string.append((seconds > 9 ? seconds : ("" + seconds)) + " s");
		return string.toString().trim();
	}
	public static String longFormat(long time) {
		int minutes = (int) ((time / (1000 * 60)) % 60);
		int hours = (int) ((time / (1000 * 60 * 60)) % 24);
		int days = (int) ((time / (1000 * 60 * 60 * 24)));
		String duration = "";
		if (days > 0) {
			if (days == 1) {
				duration = "1 Day ";
			} else {
				duration = days + " Days ";
			}
		}
		boolean showMin = (minutes > 0 && days == 0) || hours == 0;
		if (hours > 0) {
			if (hours == 1) {
				duration += "1 Hour";
			} else {
				duration += hours + " Hours";
			}
			if (showMin)
				duration += " ";
		}
		if (showMin) {
			if (minutes == 1) {
				duration += "1 Minute";
			} else {
				duration += minutes + " Minutes";
			}
		}
		return duration;
	}

	public static String longFormatS(long time) {
		int minutes = (int) ((time / (1000 * 60)) % 60);
		int hours = (int) ((time / (1000 * 60 * 60)) % 24);
		int days = (int) ((time / (1000 * 60 * 60 * 24)));
		String duration = "";
		if (days > 0) {
			if (days == 1) {
				duration = "1 D ";
			} else {
				duration = days + " D ";
			}
		}
		boolean showMin = (minutes > 0 && days == 0) || hours == 0;
		if (hours > 0) {
			if (hours == 1) {
				duration += "1 H";
			} else {
				duration += hours + " H";
			}
			if (showMin)
				duration += " ";
		}
		if (showMin) {
			if (minutes == 1) {
				duration += "1 M";
			} else {
				duration += minutes + " M";
			}
		}
		return duration;
	}

	public static String getPunishmentMessage(long time) {
		return "You have been punished for breaking a rule, this will expire: " + (time > Integer.MAX_VALUE ? "NEVER" : longFormat(time) + ".");
	}

	public static boolean isValidQc(int qcFileId) {
		boolean idx24 = (qcFileId & 0x8000) == 0;
		return Cache.STORE.getIndexes()[idx24 ? 24 : 25].fileExists(1, idx24 ? qcFileId : (qcFileId & 0x7FFFF));
	}

	public static void shuffle(int[] array) {
		int count = array.length;
		for (int i = count; i > 1; i--)
			swap(array, i - 1, new Random().nextInt(i));
	}

	private static void swap(int[] array, int i, int j) {
		int temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	public static final int[] DEFAULT_LOBBY_CONFIGS = new int[]
	{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -476379337, -1634348113, 919975670, 100098016, -941649356, 33599227, 0, 0, 0, 2, 0, 10, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 52776083, 0, 2, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 50, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 1,
		1, 0, 0, 0, 0, 0, 130, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 557858, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 1000, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 2, 0, 0, 0,
		0, 0, 272, 0, 1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 58720640, 0, 24596, 0, 0, 0, 0, 2048, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 269026962, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0,
		1073742208, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 262177, 0, 256, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1076961290, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1073741824, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1409286144, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 99, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2147483648, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34079488, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 272, 0, 0, 0, 0, 0, 0, 0, 1073741824, 0, 0, 0, 0, 8192, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 407044218, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 536870912, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 134217730, 0, 2049, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 201326625, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4224, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 33554432, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 4194304, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1073741824, 0, 0, 4, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 134232576, 32768, 0, 0, 0, 0, 0, 4194304, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0,
		262144, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2147483648, 0, 0, 0, -1, -1, -1, -1, -1, -1, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 256, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 268435456, 0, 0, 0, 0, 0, 0, 0, 0, 537133056, 2048, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 262273,
		536877056, 0, 393216, 32, 0, 4096, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 1073742848, 65536, 268435456, 0, 0, 0, 512, 0, 0, 0, 0, 0, 291912, 5672, 260092, 0, 60248,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 508, 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2048, -2130681854, 0, -1, -1, -1, -1, 0, 10,
		0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 64, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1, 0, 0, 60, 0, 0, 470286336, 132096, 1, 1276, 0, 0, 0, 0, 0, 0, 0, 0, 0, 536870912, -1, 0, 54332549,
		0, 0, 0, -1, 0, 67174408, -872415232, 256, 8388608, 0, 0, 0, 4353, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 590848, 0, 0, 0, 0, 0, 0, -1, 0, 1001, 0, 0, 0, 0, 82663,
		1025, 0, 134217728, 14, 0, 0, 0, 0, 0, 557057, 1476461568, 16384, 4096, 3721, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 525312, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 22271787, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12582912, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, -1, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0,
		0, 0, 65536, 0, -1879048192, 0, 16384, 1, 16384, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1, 0, -2147483648, 0, 0, -2147483648, 0, 0, 0, 0, 0, 0, 512, 0, 0, 0, 0, 0, 16777216, 0, 0, 0, 25165824, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 67108864, 0, 0, 0, 268435456, 0, 136347657, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2144919536, 0, 0, -1, 65540, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 511305630, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1073744067, 0, 65536, 0, 0, 0, 0, 0, 0, 536870912, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 65535, 65535, 65535, 1073741823,
		2147483647, 1073741823, 1073741823, 1073741823, 1073741823, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 134218080, 0, 0, 0, 0, 545260028, 0, 0, 0, 0, 0, 0, 0, 0, 262144, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2080374784, 0, 0, 0, 0, 0, 0,
		-2147483648, 279019520, -2147483648, 134217728, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 751720, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, -1, 268437504, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 4194304, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100673544, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 677729578, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 956430392, 25171979, 151589, 3, 4718466, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 393216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8388608, 33554432, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 67108864, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41476, 0, 0, 0, 0, 32, 536870912, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		40632322, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 819320, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 4194304, 0, 0, 0, 0, 0, 0, 0, 0, 0, 256,
		0, 0, 0, 0, 0, 0, 0, 1048559, 589721, -1, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 589855, 0, 0, -1, 0, 0, 402655232, 98353,
		0, 0, 0, -1137689604, 1073807344, 0, 0, 0, 10, 0, 0, 0, 8386561, 0, 0, -1, -1, -1, -1, -1, -1, -1, 1536, 8192, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 77, 1733, -16912800, 4594664,
		5359015, 3721, 0, 0, 0, -2147483648, 1310720, -1, 0, 0, 0, 0, 0, 0, 0, 1342177408, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 16777216, 0, 0, 32289, -1, -1, 0, 0, 0, 77, 52, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, };

	public static String getFormattedNumber(double amount, char seperator) {
		String str = new DecimalFormat("#,###,###").format(amount);
		char[] rebuff = new char[str.length()];
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c >= '0' && c <= '9')
				rebuff[i] = c;
			else
				rebuff[i] = seperator;
		}
		return new String(rebuff);
	}

	public static byte[] cryptRSA(byte[] data, BigInteger exponent, BigInteger modulus) {
		return new BigInteger(data).modPow(exponent, modulus).toByteArray();
	}

	public static final byte[] encryptUsingMD5(byte[] buffer) {
		// prevents concurrency problems with the algorithm
		synchronized (ALGORITHM_LOCK) {
			try {
				MessageDigest algorithm = MessageDigest.getInstance("MD5");
				algorithm.update(buffer);
				byte[] digest = algorithm.digest();
				algorithm.reset();
				return digest;
			} catch (Throwable e) {
				Logger.handle(e);
			}
			return null;
		}
	}

	public static boolean inCircle(WorldTile location, WorldTile center, int radius) {
		return getDistance(center, location) < radius;
	}

	public static WorldTile getFreeTile(WorldTile center, int distance) {
		WorldTile tile = center;
		for (int i = 0; i < 10; i++) {
			tile = new WorldTile(center, distance);
			if (World.isTileFree(tile.getPlane(), tile.getX(), tile.getY(), 1))
				return tile;
		}
		return center;
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	@SuppressWarnings(
	{ "rawtypes" })
	public static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile().replaceAll("%20", " ")));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	@SuppressWarnings("rawtypes")
	private static List<Class> findClasses(File directory, String packageName) {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert!file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				try {
					classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
				} catch (Throwable e) {

				}
			}
		}
		return classes;
	}

	public static final int getDistance(WorldTile t1, WorldTile t2) {
		return getDistance(t1.getX(), t1.getY(), t2.getX(), t2.getY());
	}

	public static final int getDistance(int coordX1, int coordY1, int coordX2, int coordY2) {
		int deltaX = coordX2 - coordX1;
		int deltaY = coordY2 - coordY1;
		return ((int) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
	}

	public static final int getMoveDirection(int xOffset, int yOffset) {
		if (xOffset < 0) {
			if (yOffset < 0)
				return 5;
			else if (yOffset > 0)
				return 0;
			else
				return 3;
		} else if (xOffset > 0) {
			if (yOffset < 0)
				return 7;
			else if (yOffset > 0)
				return 2;
			else
				return 4;
		} else {
			if (yOffset < 0)
				return 6;
			else if (yOffset > 0)
				return 1;
			else
				return -1;
		}
	}

	public static final String[] NUMBERS =
	{ "one", "two", "three", "four", "five" };

	public static final byte[] DIRECTION_DELTA_X = new byte[]
	{ -1, 0, 1, -1, 1, -1, 0, 1 };
	public static final byte[] DIRECTION_DELTA_Y = new byte[]
	{ 1, 1, 1, 0, 0, -1, -1, -1 };

	private static final byte[][] ANGLE_DIRECTION_DELTA =
	{
		{ 0, -1 },
		{ -1, -1 },
		{ -1, 0 },
		{ -1, 1 },
		{ 0, 1 },
		{ 1, 1 },
		{ 1, 0 },
		{ 1, -1 } };

	public static int getNpcMoveDirection(int dd) {
		return getNpcMoveDirection(DIRECTION_DELTA_X[dd], DIRECTION_DELTA_Y[dd]);
	}

	public static byte[] getDirection(int angle) {
		int v = angle >> 11;
		return ANGLE_DIRECTION_DELTA[v % ANGLE_DIRECTION_DELTA.length];
	}

	public static final int getAngle(int xOffset, int yOffset) {
		return ((int) (Math.atan2(-xOffset, -yOffset) * 2607.5945876176133)) & 0x3fff;
	}

	public static int getNpcMoveDirection(int dx, int dy) {
		if (dx == 0 && dy > 0)
			return 0;
		if (dx > 0 && dy > 0)
			return 1;
		if (dx > 0 && dy == 0)
			return 2;
		if (dx > 0 && dy < 0)
			return 3;
		if (dx == 0 && dy < 0)
			return 4;
		if (dx < 0 && dy < 0)
			return 5;
		if (dx < 0 && dy == 0)
			return 6;
		if (dx < 0 && dy > 0)
			return 7;
		return -1;
	}

	public static final int[][] getCoordOffsetsNear(int size) {
		int[] xs = new int[4 + (4 * size)];
		int[] xy = new int[xs.length];
		xs[0] = -size;
		xy[0] = 1;
		xs[1] = 1;
		xy[1] = 1;
		xs[2] = -size;
		xy[2] = -size;
		xs[3] = 1;
		xy[3] = -size;
		for (int fakeSize = size; fakeSize > 0; fakeSize--) {

			xs[(4 + ((size - fakeSize) * 4))] = -fakeSize + 1;
			xy[(4 + ((size - fakeSize) * 4))] = 1;

			xs[(4 + ((size - fakeSize) * 4)) + 1] = -size;
			xy[(4 + ((size - fakeSize) * 4)) + 1] = -fakeSize + 1;

			xs[(4 + ((size - fakeSize) * 4)) + 2] = 1;
			xy[(4 + ((size - fakeSize) * 4)) + 2] = -fakeSize + 1;

			xs[(4 + ((size - fakeSize) * 4)) + 3] = -fakeSize + 1;
			xy[(4 + ((size - fakeSize) * 4)) + 3] = -size;
		}
		return new int[][]
		{ xs, xy };
	}

	public static final int getGraphicDefinitionsSize() {
		int lastArchiveId = Cache.STORE.getIndexes()[21].getLastArchiveId();
		return lastArchiveId * 256 + Cache.STORE.getIndexes()[21].getValidFilesCount(lastArchiveId);
	}

	public static final int getAnimationDefinitionsSize() {
		int lastArchiveId = Cache.STORE.getIndexes()[20].getLastArchiveId();
		return lastArchiveId * 128 + Cache.STORE.getIndexes()[20].getValidFilesCount(lastArchiveId);
	}

	public static final int getConfigDefinitionsSize() {
		int lastArchiveId = Cache.STORE.getIndexes()[22].getLastArchiveId();
		return lastArchiveId * 256 + Cache.STORE.getIndexes()[22].getValidFilesCount(lastArchiveId);
	}

	public static final int getObjectDefinitionsSize() {
		int lastArchiveId = Cache.STORE.getIndexes()[16].getLastArchiveId();
		return lastArchiveId * 256 + Cache.STORE.getIndexes()[16].getValidFilesCount(lastArchiveId);
	}

	public static final int getNPCDefinitionsSize() {
		int lastArchiveId = Cache.STORE.getIndexes()[18].getLastArchiveId();
		return lastArchiveId * 128 + Cache.STORE.getIndexes()[18].getValidFilesCount(lastArchiveId);
	}

	public static final int getItemDefinitionsSize() {
		int lastArchiveId = Cache.STORE.getIndexes()[19].getLastArchiveId();
		return (lastArchiveId * 256 + Cache.STORE.getIndexes()[19].getValidFilesCount(lastArchiveId));
	}

	public static boolean itemExists(int id) {
		if (id >= getItemDefinitionsSize()) // setted because of custom items
			return false;
		return true;//Cache.STORE.getIndexes()[19].fileExists(id >>> 8, 0xff & id);
	}

	public static final int getInterfaceDefinitionsSize() {
		return Cache.STORE.getIndexes()[3].getLastArchiveId() + 1;
	}

	public static final int getInterfaceDefinitionsComponentsSize(int interfaceId) {
		return Cache.STORE.getIndexes()[3].getLastFileId(interfaceId) + 1;
	}

	/*
	 * Use random instead
	 */
	@Deprecated
	public static final int getRandom(int maxValue) {
		return (int) (random.nextDouble() * (maxValue + 1));
	}

	/*
	 * Use random instead
	 */
	@Deprecated
	public static final double getRandomDouble(double maxValue) {
		return (random.nextDouble() * (maxValue + 1));
	}

	public static final int random(int min, int max) {
		final int n = Math.abs(max - min);
		return Math.min(min, max) + (n == 0 ? 0 : random(n));
	}

	public static final double random(double min, double max) {
		final double n = Math.abs(max - min);
		return Math.min(min, max) + (n == 0 ? 0 : random(n));
	}

	public static final int next(int max, int min) {
		return min + (int) (random.nextDouble() * ((max - min) + 1));
	}

	public static final int random(int maxValue) {
		if (maxValue <= 0)
			return 0;
		return new Random().nextInt(maxValue);
	}

	public static <T> T randomFrom(final T... items) {
		return items[random(items.length)];
	}

	public static <T> T randomFrom(final List<T> items) {
		return items.get(random(items.size()));
	}

	/**
	 * Get a random number inclusive of the range provided. (5 = 0-5)
	 * @param maxRange
	 * @return
	 */
	public static int get(int maxRange) {
		return (int) (Math.random() * (maxRange + 1D));
	}

	public static int random(int... t) {
		return t[random(t.length)];
	}

	public static <T> T random(T... t) {
		return t[random(t.length)];
	}

	public static final double random(double maxValue) {
		return new Random().nextDouble() * maxValue;
	}

	public static final long random(long maxValue) {
		return (long) (new Random().nextDouble() * maxValue);
	}


	public static final double randomDouble() {
		return new Random().nextDouble();
	}

	public static double increaseByPercent(double initialValue, double percentageIncrease) {
		var newValue = initialValue;
		newValue *= (percentageIncrease / 100);
		return initialValue + newValue;
	}

	public static final char[] VALID_CHARS =
	{ '_', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	public static boolean invalidAccountName(String name) {
		return name.length() < 2 || name.length() > 12 || name.startsWith("_") || name.endsWith("_") || name.contains("__") || containsInvalidCharacter(name);
	}

	public static boolean containsInvalidCharacter(char c) {
		for (char vc : VALID_CHARS) {
			if (vc == c)
				return false;
		}
		return true;
	}

	public static boolean containsInvalidCharacter(String name) {
		for (char c : name.toCharArray()) {
			if (containsInvalidCharacter(c))
				return true;
		}
		return false;
	}

	public static final long stringToLong(String s) {
		long l = 0L;
		for (int i = 0; i < s.length() && i < 12; i++) {
			char c = s.charAt(i);
			l *= 37L;
			if (c >= 'A' && c <= 'Z')
				l += (1 + c) - 65;
			else if (c >= 'a' && c <= 'z')
				l += (1 + c) - 97;
			else if (c >= '0' && c <= '9')
				l += (27 + c) - 48;
		}
		while (l % 37L == 0L && l != 0L) {
			l /= 37L;
		}
		return l;
	}

	public static final String longToString(long l) {
		if (l <= 0L || l >= 0x5b5b57f8a98a5dd1L)
			return null;
		if (l % 37L == 0L)
			return null;
		int i = 0;
		char ac[] = new char[12];
		while (l != 0L) {
			long l1 = l;
			l /= 37L;
			ac[11 - i++] = VALID_CHARS[(int) (l1 - l * 37L)];
		}
		return new String(ac, 12 - i, i);
	}

	public static final int getNameHash(String name) {
		name = name.toLowerCase();
		int hash = 0;
		for (int index = 0; index < name.length(); index++)
			hash = method1258(name.charAt(index)) + ((hash << 5) - hash);
		return hash;
	}

	public static final byte method1258(char c) {
		byte charByte;
		if (c > 0 && c < '\200' || c >= '\240' && c <= '\377') {
			charByte = (byte) c;
		} else if (c != '\u20AC') {
			if (c != '\u201A') {
				if (c != '\u0192') {
					if (c == '\u201E') {
						charByte = -124;
					} else if (c != '\u2026') {
						if (c != '\u2020') {
							if (c == '\u2021') {
								charByte = -121;
							} else if (c == '\u02C6') {
								charByte = -120;
							} else if (c == '\u2030') {
								charByte = -119;
							} else if (c == '\u0160') {
								charByte = -118;
							} else if (c == '\u2039') {
								charByte = -117;
							} else if (c == '\u0152') {
								charByte = -116;
							} else if (c != '\u017D') {
								if (c == '\u2018') {
									charByte = -111;
								} else if (c != '\u2019') {
									if (c != '\u201C') {
										if (c == '\u201D') {
											charByte = -108;
										} else if (c != '\u2022') {
											if (c == '\u2013') {
												charByte = -106;
											} else if (c == '\u2014') {
												charByte = -105;
											} else if (c == '\u02DC') {
												charByte = -104;
											} else if (c == '\u2122') {
												charByte = -103;
											} else if (c != '\u0161') {
												if (c == '\u203A') {
													charByte = -101;
												} else if (c != '\u0153') {
													if (c == '\u017E') {
														charByte = -98;
													} else if (c != '\u0178') {
														charByte = 63;
													} else {
														charByte = -97;
													}
												} else {
													charByte = -100;
												}
											} else {
												charByte = -102;
											}
										} else {
											charByte = -107;
										}
									} else {
										charByte = -109;
									}
								} else {
									charByte = -110;
								}
							} else {
								charByte = -114;
							}
						} else {
							charByte = -122;
						}
					} else {
						charByte = -123;
					}
				} else {
					charByte = -125;
				}
			} else {
				charByte = -126;
			}
		} else {
			charByte = -128;
		}
		return charByte;
	}

	public static String formatPlayerNameForProtocol(String name) {
		if (name == null)
			return "";
		name = name.replaceAll(" ", "_");
		name = name.toLowerCase();
		return name;
	}

	public static String formatPlayerNameForDisplay(String name) {
		if (name == null)
			return "";
		name = name.replaceAll("_", " ");
		name = name.toLowerCase();
		StringBuilder newName = new StringBuilder();
		boolean wasSpace = true;
		for (int i = 0; i < name.length(); i++) {
			if (wasSpace) {
				newName.append(("" + name.charAt(i)).toUpperCase());
				wasSpace = false;
			} else {
				newName.append(name.charAt(i));
			}
			if (name.charAt(i) == ' ') {
				wasSpace = true;
			}
		}
		return newName.toString();
	}

	private static final char[] UNICODE_TABLE =
	{ '\u20ac', '\0', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020', '\u2021', '\u02c6', '\u2030', '\u0160', '\u2039', '\u0152', '\0', '\u017d', '\0', '\0', '\u2018', '\u2019', '\u201c', '\u201d',
		'\u2022', '\u2013', '\u2014', '\u02dc', '\u2122', '\u0161', '\u203a', '\u0153', '\0', '\u017e', '\u0178' };

	public static char method2782(byte value) {
		int byteChar = 0xff & value;
		if (byteChar == 0)
			throw new IllegalArgumentException("Non cp1252 character 0x" + Integer.toString(byteChar, 16) + " provided");
		if ((byteChar ^ 0xffffffff) <= -129 && byteChar < 160) {
			int i_4_ = UNICODE_TABLE[-128 + byteChar];
			if ((i_4_ ^ 0xffffffff) == -1)
				i_4_ = 63;
			byteChar = i_4_;
		}
		return (char) byteChar;
	}

	public static int getHashMapSize(int size) {
		size--;
		size |= size >>> -1810941663;
		size |= size >>> 2010624802;
		size |= size >>> 10996420;
		size |= size >>> 491045480;
		size |= size >>> 1388313616;
		return 1 + size;
	}

	/**
	 * Walk dirs 0 - South-West 1 - South 2 - South-East 3 - West 4 - East 5 -
	 * North-West 6 - North 7 - North-East
	 */
	public static int getPlayerWalkingDirection(int dx, int dy) {
		if (dx == -1 && dy == -1) {
			return 0;
		}
		if (dx == 0 && dy == -1) {
			return 1;
		}
		if (dx == 1 && dy == -1) {
			return 2;
		}
		if (dx == -1 && dy == 0) {
			return 3;
		}
		if (dx == 1 && dy == 0) {
			return 4;
		}
		if (dx == -1 && dy == 1) {
			return 5;
		}
		if (dx == 0 && dy == 1) {
			return 6;
		}
		if (dx == 1 && dy == 1) {
			return 7;
		}
		return -1;
	}

	public static int getPlayerRunningDirection(int dx, int dy) {
		if (dx == -2 && dy == -2)
			return 0;
		if (dx == -1 && dy == -2)
			return 1;
		if (dx == 0 && dy == -2)
			return 2;
		if (dx == 1 && dy == -2)
			return 3;
		if (dx == 2 && dy == -2)
			return 4;
		if (dx == -2 && dy == -1)
			return 5;
		if (dx == 2 && dy == -1)
			return 6;
		if (dx == -2 && dy == 0)
			return 7;
		if (dx == 2 && dy == 0)
			return 8;
		if (dx == -2 && dy == 1)
			return 9;
		if (dx == 2 && dy == 1)
			return 10;
		if (dx == -2 && dy == 2)
			return 11;
		if (dx == -1 && dy == 2)
			return 12;
		if (dx == 0 && dy == 2)
			return 13;
		if (dx == 1 && dy == 2)
			return 14;
		if (dx == 2 && dy == 2)
			return 15;
		return -1;
	}

	public static String fixChatMessage(String message) {
		StringBuilder newText = new StringBuilder();
		boolean wasSpace = true;
		boolean exception = false;
		for (int i = 0; i < message.length(); i++) {
			if (!exception) {
				if (wasSpace) {
					newText.append(("" + message.charAt(i)).toUpperCase());
					if (!String.valueOf(message.charAt(i)).equals(" "))
						wasSpace = false;
				} else {
					newText.append(("" + message.charAt(i)).toLowerCase());
				}
			} else {
				newText.append(("" + message.charAt(i)));
			}
			if (String.valueOf(message.charAt(i)).contains(":"))
				exception = true;
			else if (String.valueOf(message.charAt(i)).contains(".") || String.valueOf(message.charAt(i)).contains("!") || String.valueOf(message.charAt(i)).contains("?"))
				wasSpace = true;
		}
		return newText.toString().replace(" i ", " I ");
	}

	public static final int[] DOOR_ROTATION_DIR_X =
	{ -1, 0, 1, 0 };

	public static final int[] DOOR_ROTATION_DIR_Y =
	{ 0, 1, 0, -1 };

	private Utils() {

	}

	public static String currentTime(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}

	public static boolean colides(int x1, int y1, int size1, int x2, int y2, int size2) {
		int distanceX = x1 - x2;
		int distanceY = y1 - y2;
		return distanceX < size2 && distanceX > -size1 && distanceY < size2 && distanceY > -size1;
	}

	public static boolean isOnRange(Entity entity1, Entity entity2, int maxDistance) {
		return isOnRange(entity1.getX(), entity1.getY(), entity1.getSize(), entity2.getX(), entity2.getY(), entity2.getSize(), maxDistance);
	}

	public static boolean isOnRange(int x1, int y1, int size1, int x2, int y2, int size2, int maxDistance) {
		int distanceX = x1 - x2;
		int distanceY = y1 - y2;
		if (distanceX > size2 + maxDistance || distanceX < -size1 - maxDistance || distanceY > size2 + maxDistance || distanceY < -size1 - maxDistance)
			return false;
		return true;
	}

	/*
	 * dont use this one
	 */
	public static boolean isOnRange(int x1, int y1, int x2, int y2, int sizeX, int sizeY) {
		int distanceX = x1 - x2;
		int distanceY = y1 - y2;
		if (distanceX > sizeX || distanceX < -1 || distanceY > sizeY || distanceY < -1)
			return false;
		return true;
	}

	public static int getProjectileTime(WorldTile startTile, WorldTile endTile, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		return getProjectileTime(startTile, endTile, startHeight, endHeight, speed, delay, curve, startDistanceOffset, true);
	}

	public static boolean rollDie(int maxRoll) {
		return rollDie(maxRoll, 1);
	}

	public static boolean rollDie(int sides, int chance) {
		return rng(1, sides) <= chance;
	}

	public static double rng() {
		return ThreadLocalRandom.current().nextDouble();
	}

	public static <T> T get(T[] values) {
		return values[rng(values.length - 1)];
	}

	public static <T> T get(List<T> list) {
		if(list.size() == 0)
			return null;
		return list.get(rng(list.size() - 1));
	}
	
	public static int rng(int maxRange) {
		return (int) (rng() * (maxRange + 1D));
	}

	public static int rng(int minRange, int maxRange) {
		return minRange + rng(maxRange - minRange);
	}

	public static boolean rollPercent(int percent) {
		return rng() <= (percent * 0.01);
	}

	public static int getProjectileTime(WorldTile startTile, WorldTile endTile, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset, boolean old) {
	//	double distance = Utils.getDistance(startTile, endTile) + 1;
		double startSize = startTile instanceof Entity ? ((Entity)startTile).getSize() : 1;
		double endSize = endTile instanceof Entity ? ((Entity)endTile).getSize() : 1;
		double deltaX = (startTile.getX() + startSize/2d) - (endTile.getX() + endSize/2d);
		double deltaY = (startTile.getY() + startSize/2d) - (endTile.getY() + endSize/2d);
		double distance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
//System.out.println("123");
		if (speed == 0) //cant be 0, happens cuz method wrong and so /10 needed so may round to 0
			speed = 1;
		//distance = 1 tile = 30ms.
		//600 * distance = 600 / 20.
		//distance = 20.
	//	System.out.println(distance);


	/*	if (old)
			return delay * 20 + (int) ((delay * 10d) + (distance * ((30d / speed) * 10d) * Math.cos(Math.toRadians(curve))));
		*/
		//return (int) (delay * 20d + (distance * 600d / (speed * 1000)));/* ((30d / speed) * 10d)))*///));
		return

				(delay * 20) + (int) ((distance * 30 / ((speed ) < 1 ? 1 : (speed ))) * 20);

				//delay * 20 + (int) ((delay * 10d) + (distance * ((30d / speed) * 10d))); // * Math.cos(Math.toRadians(curve))));
	}

	private static final String[] NUMBER_NAMES =
	{ "Zero", "One", "Two", "Three", "Four" };

	public static String toNumString(int i) {
		return NUMBER_NAMES[i];
	}

	public static boolean collides(Entity entity, Entity target) {
		return entity.getPlane() == target.getPlane() && colides(entity.getX(), entity.getY(), entity.getSize(), target.getX(), target.getY(), target.getSize());
	}

	public static boolean collides(int x1, int y1, int size1, int x2, int y2, int size2) {
		int distanceX = x1 - x2;
		int distanceY = y1 - y2;
		return distanceX < size2 && distanceX > -size1 && distanceY < size2 && distanceY > -size1;
	}

	public static int getMapArchiveId(int regionX, int regionY) {
		return regionX | regionY << 7;
	}

	public static <T extends Comparable<T>> T clamp(T val, T min, T max) {
		if (val.compareTo(min) < 0) return min;
		else if (val.compareTo(max) > 0) return max;
		else return val;
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}
