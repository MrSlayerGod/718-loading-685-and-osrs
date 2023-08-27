package com.rs.game.player;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.utils.MusicHints;
import com.rs.utils.Utils;

public final class MusicsManager implements Serializable {


	public static final Map<Integer, String> OSRS_MUSIC_NAMES = new HashMap<Integer, String>();
	
	private static final long serialVersionUID = 1020415702861567375L;

	private static final int[] CONFIG_IDS = new int[]
	{ 20, 21, 22, 23, 24, 25, 298, 311, 346, 414, 464, 598, 662, 721, 906, 1009, 1104, 1136, 1180, 1202, 1381, 1394, 1434, 1596, 1618, 1619, 1620, -1, 1864, 1865, 2019, 2246, 2430, 2559, 2632 };
	private static final int[] PLAY_LIST_CONFIG_IDS = new int[]
	{ 1621, 1622, 1623, 1624, 1625, 1626 };

	private transient Player player;
	private transient int playingMusic;
	private transient long playingMusicDelay;
	private transient boolean settedMusic;
	private ArrayList<Integer> unlockedMusics;
	private ArrayList<Integer> playList;

	private transient boolean playListOn;
	private transient int nextPlayListMusic;
	private transient boolean shuffleOn;

	public MusicsManager() {
		unlockedMusics = new ArrayList<Integer>();
		playList = new ArrayList<Integer>();
		// auto unlocked musics
		unlockMusics();
	}
	
	private static final int[] AUTO_UNLOCKED_MUSICS = { -1692, 466, 200, 517, 518, 519, 323, 1176, 931, 316, 336, 151, 411, 350, 360, 89, 321, 412, 1177, 377, 150, 1179, 103, 153, 152, 602, 717, 482, 650, 520, 611, 318, 196, 514 };

	private void unlockMusics() {
		for (int id : AUTO_UNLOCKED_MUSICS)
			unlockedMusics.add(id);
		int[] startZoneMusics = World.getRegion(Settings.START_PLAYER_LOCATION.getRegionId()).getMusicIds();
		if (startZoneMusics != null)
			for (int musicId : startZoneMusics)
				if (musicId >= 0)
					unlockedMusics.add(musicId);
	}

	public void passMusics(Player p) {
		for (int musicId : p.getMusicsManager().unlockedMusics) {
			if (!unlockedMusics.contains(musicId))
				unlockedMusics.add(musicId);
		}
	}

	public boolean hasMusic(int id) {
		return unlockedMusics.contains(id);
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void switchShuffleOn() {
		if (shuffleOn) {
			playListOn = false;
			refreshPlayListConfigs();
		}
		shuffleOn = !shuffleOn;
	}

	public void switchPlayListOn() {
		if (playListOn) {
			playListOn = false;
			shuffleOn = false;
			refreshPlayListConfigs();
		} else {
			playListOn = true;
			nextPlayListMusic = 0;
			replayMusic();
		}
	}

	public void clearPlayList() {
		if (playList.isEmpty())
			return;
		playList.clear();
		refreshPlayListConfigs();
	}

	public void addPlayingMusicToPlayList() {
		addToPlayList(playingMusic);
	}
	
	public int getArchiveId(int musicId) {
		return musicId  < -1 ? -musicId : ClientScriptMap.getMap(1351).getIntValue((long) musicId);
	}

	public int getMusicId(int archiveId) {
		return (int) ClientScriptMap.getMap(1351).getKeyForValue(archiveId);
	}
	
	public void addToPlayList(int musicId) {
		if (playList.size() == PLAY_LIST_CONFIG_IDS.length * 2)
			return;
		if (musicId != -1 && unlockedMusics.contains(musicId) && !playList.contains(musicId)) {
			playList.add(musicId);
			if (playListOn)
				switchPlayListOn();
			else
				refreshPlayListConfigs();
		}
	}

	public void removeFromPlayListByIndex(int listIndex) {
		if (listIndex >= PLAY_LIST_CONFIG_IDS.length * 2)
			listIndex -= PLAY_LIST_CONFIG_IDS.length * 2;
		if (listIndex >= playList.size())
			return;
		playList.remove(listIndex);
		if (playListOn)
			switchPlayListOn();
		else
			refreshPlayListConfigs();
	}

	public void removeFromPlayList(int musicId) {
		if (musicId != -1 && unlockedMusics.contains(musicId) && playList.contains(musicId)) {
			playList.remove((Integer) musicId);
			if (playListOn)
				switchPlayListOn();
			else
				refreshPlayListConfigs();
		}
	}

	public void refreshPlayListConfigs() {
		int[] configValues = new int[PLAY_LIST_CONFIG_IDS.length];
		for (int i = 0; i < configValues.length; i++)
			configValues[i] = -1;
		for (int i = 0; i < playList.size(); i += 2) {
			Integer musicId1 = playList.get(i);
			Integer musicId2 = (i + 1) >= playList.size() ? null : playList.get(i + 1);
			if (musicId1 == null && musicId2 == null)
				break;
			int configValue = musicId1 | (musicId2 == null ? -1 : musicId2) << 15;
			configValues[i / 2] = configValue;
		}
		for (int i = 0; i < PLAY_LIST_CONFIG_IDS.length; i++)
			player.getVarsManager().sendVar(PLAY_LIST_CONFIG_IDS[i], configValues[i]);
	}

	public void refreshListConfigs() {
		int[] configValues = new int[CONFIG_IDS.length];
		for (int musicId : unlockedMusics) {
			if (musicId < 0)
				continue;
			int index = getConfigIndex(musicId);
			if (index >= CONFIG_IDS.length)
				continue;
			configValues[index] |= 1 << (musicId - (index * 32));
		}
		for (int i = 0; i < CONFIG_IDS.length; i++) {
			if (CONFIG_IDS[i] != -1)
				player.getVarsManager().sendVar(CONFIG_IDS[i], configValues[i]);
		}
	}

	public void addMusic(int musicId) {
		unlockedMusics.add(musicId);
		refreshListConfigs();
		if (unlockedMusics.size() >= Settings.AIR_GUITAR_MUSICS_COUNT)
			player.getEmotesManager().unlockEmote(41);
	}

	public int getConfigIndex(int musicId) {
		return (musicId + 1) / 32;
	}

	public void unlockMusicPlayer() {
		player.getPackets().sendUnlockIComponentOptionSlots(187, 1, 0, CONFIG_IDS.length * 64, 0, 1, 2, 3);
		player.getPackets().sendUnlockIComponentOptionSlots(187, 9, 0, PLAY_LIST_CONFIG_IDS.length * 4, 0, 1, 2, 3);
	}

	public void init() {
		// unlock music inter all options
		refreshListConfigs();
		refreshPlayListConfigs();
		player.getMusicsManager().checkMusic(World.getRegion(player.getRegionId()).getRandomMusicId());
	}

	private static final long MUSIC_DELAY = 60000 * 30;//180000;

	public boolean musicEnded() {
		return playingMusic != -2 && playingMusicDelay + MUSIC_DELAY < Utils.currentTimeMillis();
	}

	public void replayMusic(int lastMusicIndex) {
		if (playingMusic < 0)
			return;
		int musicIndex = getArchiveId(playingMusic);
		if (musicIndex != lastMusicIndex)
			return;
		replayMusic();
	}
	
	public void replayMusic() {
		if (playListOn && playList.size() > 0) {
			if (shuffleOn)
				playingMusic = playList.get(Utils.random(playList.size()));
			else {
				if (nextPlayListMusic >= playList.size())
					nextPlayListMusic = 0;
				playingMusic = playList.get(nextPlayListMusic++);
			}
		} else if (unlockedMusics.size() > 0 && !World.getRegion(player.getRegionId()).isOsrs()) {// random music
			playingMusic = getGenreMusic(); //unlockedMusics.get(Utils.random(unlockedMusics.size()));
		}
		playMusic(playingMusic);
	}

	private static String[] getHint(int id) {
		String hint = MusicHints.getHint(id).replace(".", "");
		String[] words = hint.split(" ");
		if(words.length == 0)
			return null;
		String s = "";;
		for(String w : words) {
			if(Character.isUpperCase(w.charAt(0)))
				s += w + " ";
		}
		return s.split(" ");
	}
	
	public static void main(String[] test) throws IOException {
		Cache.init();
		MusicHints.init();
		String[] hints = getHint(222);
		System.out.println("from "+Arrays.toString(hints));
		List<Integer> combs = new ArrayList<Integer>();
		for(int id = 0; id < 1000; id++) {
			String[] hint = getHint(id);
			if(hint.length == 0)
				continue;
			l: for(String h : hint) {
				for(String h2 : hints) {
					if(h2.equals(h)) {
						combs.add(id);
						
						int musicIndex = (int) ClientScriptMap.getMap(1351).getKeyForValue(id);
							String musicName = ClientScriptMap.getMap(1345).getStringValue(musicIndex);
						
						System.out.println("found "+id+", "+musicName+", "+MusicHints.getHint(id)+", "+h);
						break l;
					}
				}
			}
		}
	}
	
	/*
	 * might be too slow. if so improve
	 */
	public int getGenreMusic() {
		if(playingMusic > -1) {
			//int currentMusic = playingMusic;
			String[] hints = getHint(playingMusic);
			if(hints.length > 0) {
				List<Integer> combs = new ArrayList<Integer>();
				for(int id : unlockedMusics) {
					String[] hint = getHint(id);
					if(hint.length == 0)
						continue;
					l: for(String h : hint) {
						for(String h2 : hints) {
							if(h2.equals(h)) {
								combs.add(id);
								break l;
							}
						}
					}
				}
				if(combs.size() > 0)
					return combs.get(Utils.random(combs.size()));
			}
		}
		return unlockedMusics.get(Utils.random(unlockedMusics.size()));
			
	}
	
	public void checkMusic(int requestMusicId) {
		if (playListOn || settedMusic && playingMusicDelay + MUSIC_DELAY >= Utils.currentTimeMillis()
				|| player.getCutscenesManager().hasCutscene())
			return;
		settedMusic = false;
		if (playingMusic != requestMusicId)
			playMusic(requestMusicId);
	}

	//if using forceplaymusic do not forget to reset later.
	public void forcePlayMusic(int musicId) {
		settedMusic = true;
		playMusic(musicId);
	}

	public void reset() {
		settedMusic = false;
		player.getMusicsManager().checkMusic(World.getRegion(player.getRegionId()).getRandomMusicId());
	}

	public void sendHint(int musicId) {
		player.getPackets().sendGameMessage("This track " + (unlockedMusics.contains(musicId) ? "was unlocked" : "unlocks") + " " + MusicHints.getHint(musicId));
	}

	public void playAnotherMusicFromPlayListByIndex(int listIndex) {
		if (listIndex >= PLAY_LIST_CONFIG_IDS.length * 2)
			listIndex -= PLAY_LIST_CONFIG_IDS.length * 2;
		if (listIndex >= playList.size())
			return;
		playAnotherMusic(playList.get(listIndex));
	}
	
	public void playAnotherMusic(int musicId) {
		if (musicId != -1 && unlockedMusics.contains(musicId)) {
			settedMusic = true;
			if (playListOn)
				switchPlayListOn();
			playMusic(musicId);
		}
	}

	public void playOSRSMusic(String name) {
		int id = Region.getMusicId(name);//-Cache.STORE.getIndexes()[6].getArchiveId(name.toLowerCase().replace(" ", "_"));
		if (id < -1) {
			if (!OSRS_MUSIC_NAMES.containsKey(id))
				OSRS_MUSIC_NAMES.put(id, name);
			playMusic(id);
		}
	}
	
	public String getMusicName() {
		if (playingMusic == -2)
			return "";
		if (playingMusic < -2) {
			String name = OSRS_MUSIC_NAMES.get(playingMusic);
			return name == null ? "" : name;
		}
		return ClientScriptMap.getMap(1345).getStringValue(playingMusic);
	}

	public void refreshMusicInterface() {
		player.getPackets().sendIComponentText(187, 4, getMusicName());
	}

	public void playMusic(int musicId) {
		if (!player.hasStarted())
			return;
		playingMusicDelay = Utils.currentTimeMillis();
		if (musicId == -2) {
			playingMusic = musicId;
			player.getPackets().sendMusic(-1);
			refreshMusicInterface();
			return;
		}
		playingMusic = musicId;
		int musicIndex = getArchiveId(musicId);
		player.getPackets().sendMusic(musicIndex, playingMusic == -1 ? 0 : 100, 255);
		if (musicIndex != -1) {
			String musicName = getMusicName();
			refreshMusicInterface();
			if (musicName.replace(" ", "").equals(""))
				musicName = null;//Region.getMusicName1(player.getRegionId());
			if (musicName != null && !unlockedMusics.contains(musicId)) {
				addMusic(musicId);
				player.getPackets().sendGameMessage("<col=ff0000>You have unlocked a new music track: " + musicName + ".");
			}
		} else {
			String musicName = null;
			player.getPackets().sendIComponentText(187, 4, musicName != null ? musicName : "");

		}
	}
	
	public int getUnlockedMusicsCount() { 
		return unlockedMusics.size();
	}

}
