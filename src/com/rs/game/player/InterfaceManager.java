package com.rs.game.player;

import java.util.concurrent.ConcurrentHashMap;

import com.rs.Settings;
import com.rs.game.player.content.Deadman;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.questTab.QuestTab;

public class InterfaceManager {

	public static final int FIXED_WINDOW_ID = 548;
	public static final int RESIZABLE_WINDOW_ID = 746;
	public static final int CHAT_BOX_COMPONENT = 13;
	public static final int FIXED_SCREEN_COMPONENT_ID = 27;
	public static final int RESIZABLE_SCREEN_COMPONENT_ID = 28;
	public static final int FIXED_INV_COMPONENT_ID = 166;
	public static final int RESIZABLE_INV_COMPONENT_ID = 108;
	private Player player;

	private final ConcurrentHashMap<Integer, Integer> openedinterfaces = new ConcurrentHashMap<Integer, Integer>();

	private boolean resizableScreen;
	private int rootInterface;
	private int currentTab;

	public InterfaceManager(Player player) {
		this.player = player;
	}

	public void setWindowInterface(int componentId, int interfaceId) {
		setWindowInterface(true, componentId, interfaceId);
	}

	public void setWindowInterface(boolean walkable, int componentId, int interfaceId) {
		setInterface(walkable, resizableScreen ? RESIZABLE_WINDOW_ID : FIXED_WINDOW_ID, componentId, interfaceId);
	}

	public void removeWindowInterface(int componentId) {
		removeInterfaceByParent(resizableScreen ? RESIZABLE_WINDOW_ID : FIXED_WINDOW_ID, componentId);
	}

	public void sendChatBoxInterface(int interfaceId) {
		setInterface(true, 752, CHAT_BOX_COMPONENT, interfaceId);
		if (player.isOsrsGameframe() && resizableScreen)
			player.getPackets().sendHideIComponent(752, 2, true);
	}

	public void closeChatBoxInterface() {
		boolean removed = removeInterfaceByParent(752, CHAT_BOX_COMPONENT);
		if (removed && player.isOsrsGameframe() && resizableScreen)
			player.getPackets().sendHideIComponent(752, 2, false);
	}

	public boolean containsChatBoxInter() {
		return containsInterfaceAtParent(752, CHAT_BOX_COMPONENT);
	}

	public void setOverlay(int interfaceId, boolean fullScreen) {
		setWindowInterface(resizableScreen ? fullScreen ? 1 : 11 : 0, interfaceId);
	}

	public void removeOverlay(boolean fullScreen) {
		removeWindowInterface(resizableScreen ? fullScreen ? 1 : 11 : 0);
	}

	public void sendSquealOverlay() {
		setWindowInterface(resizableScreen ? 0 : 10, 1252); // TODO not working for fixed
	}

	public void closeSquealOverlay() {
		removeWindowInterface(resizableScreen ? 0 : 10);
	}

	public void sendInterface(int interfaceId) {
		sendInterface(interfaceId, false);
	}
	
	public void sendInterface(int interfaceId, boolean clickThrough) {
		setInterface(clickThrough, resizableScreen ? RESIZABLE_WINDOW_ID : FIXED_WINDOW_ID, resizableScreen ? RESIZABLE_SCREEN_COMPONENT_ID : FIXED_SCREEN_COMPONENT_ID, interfaceId);
	}

	private long lastNotification;
	
	public long getLastNotification() {
		return lastNotification;
	}
	
	public void sendNotification(String title, String text) {
		if (!player.isNotifications())
			return;
		/*sendPMInterface(798);
		player.getPackets().sendIComponentText(798, 2, title);
		player.getPackets().sendIComponentText(798, 3, text);
		lastNotification = Utils.currentTimeMillis();*/
		player.getPackets().sendNotification(title, text);
	}

	public boolean containsPMInterface() {
		return containsWindowInterfaceAtParent(resizableScreen ? 25 : 26);
	}

	
	public void sendPMInterface(int interfaceId) {
		setInterface(true, resizableScreen ? RESIZABLE_WINDOW_ID : FIXED_WINDOW_ID, resizableScreen ? 25 : 26, interfaceId);
	}
	
	public void removePMInterface() {
		removeWindowInterface(resizableScreen ? 25 : 26);
	}
	
	public void sendInventoryInterface(int interfaceId) {
		setInterface(false, resizableScreen ? RESIZABLE_WINDOW_ID : FIXED_WINDOW_ID, resizableScreen ? RESIZABLE_INV_COMPONENT_ID : FIXED_INV_COMPONENT_ID, interfaceId);
	}

	/*
	 * rs calls it so
	 */
	public void setExtras(int interfaceId) {
		setWindowInterface(resizableScreen ? 128 : 188, interfaceId);
	}

	public void removeExtras() {
		removeWindowInterface(resizableScreen ? 128 : 188);
	}

	public final void sendInterfaces() {
		if (player.getDisplayMode() == 2 || player.getDisplayMode() == 3) {
			resizableScreen = true;
			sendFullScreenInterfaces();
		} else {
			resizableScreen = false;
			sendFixedInterfaces();
		}
		//sendSquealOverlay();
		player.getSkills().sendInterfaces();
		player.getCombatDefinitions().sendUnlockAttackStylesButtons();
		player.getMusicsManager().unlockMusicPlayer();
		player.getEmotesManager().unlockEmotesBook();
		player.getInventory().unlockInventoryOptions();
		player.getPrayer().unlockPrayerBookButtons();
		ClansManager.unlockBanList(player);
		if (player.getFamiliar() != null && player.isRunning())
			player.getFamiliar().unlock();
		player.getPackets().sendHideIComponent(182, 2, true); //logout to lobby
		player.getMoneyPouch().refreshCoins();
		player.getTasksManager().refreshInterface();
		player.getControlerManager().sendInterfaces();
		Deadman.sendInterfaces(player);
	}

	public boolean containsReplacedChatBoxInter() {
		return containsInterfaceAtParent(752, 11);
	}

	public void replaceRealChatBoxInterface(int interfaceId) {
		setInterface(true, 752, 11, interfaceId);
		if (player.isOsrsGameframe() && resizableScreen)
			player.getPackets().sendHideIComponent(752, 2, true);
	}

	public void closeReplacedRealChatBoxInterface() {
		boolean removed = removeInterfaceByParent(752, 11);
		if (removed && player.isOsrsGameframe() && resizableScreen)
			player.getPackets().sendHideIComponent(752, 2, false);
	}

	public void setDefaultRootInterface() {
		setRootInterface(resizableScreen ? 746 : 548, false);
	}
	
	public void sendChatBottomInterface() {
		setWindowInterface(resizableScreen ? 22 : 37, 751);
		player.getPackets().sendIComponentText(751, 16, "Request HELP");
	}

	public void sendChatBoxInterface() {
		setWindowInterface(resizableScreen ? 21 : 161, 752);
	}

	public void sendFullScreenInterfaces() {
		setDefaultRootInterface();
		sendChatBoxInterface();
		sendChatBottomInterface();
		setWindowInterface(15, 745);
		setWindowInterface(24, 754);
		setWindowInterface(195, 748);
		setWindowInterface(196, 749);
		setWindowInterface(197, 750);
		setWindowInterface(198, 747);
		setInterface(true, 752, 9, 137);
		sendCombatStyles();
		sendTaskSystem();
		sendSkills();
		sendQuestTab();
		sendInventory();
		sendEquipment();
		sendPrayerBook();
		sendMagicBook();
		setWindowInterface(120, 550); // friend list
		setWindowInterface(121, 1109); // 551 ignore now friendchat
		setWindowInterface(122, 1110); // 589 old clan chat now new clan chat
		sendSettings();
		sendEmotes();
		setWindowInterface(125, 187); // music
		setWindowInterface(126, 34); // notes
		setWindowInterface(129, 182); // logout*/
		sendSquealOfFortuneTab();
		sendXPPopup();
	}

	public void sendFixedInterfaces() {
		setDefaultRootInterface();
		sendChatBoxInterface();
		sendChatBottomInterface();
		setWindowInterface(23, 745);
		setWindowInterface(25, 754);
		setWindowInterface(155, 747);
		setWindowInterface(151, 748);
		setWindowInterface(152, 749);
		setWindowInterface(153, 750);
		setInterface(true, 752, 9, 137);
		sendMagicBook();
		sendPrayerBook();
		sendEquipment();
		sendInventory();
		sendQuestTab();// quest
		setWindowInterface(181, 1109);// 551 ignore now friendchat
		setWindowInterface(182, 1110);// 589 old clan chat now new clan chat
		setWindowInterface(180, 550);// friend list
		setWindowInterface(185, 187);// music
		setWindowInterface(186, 34); // notes
		setWindowInterface(189, 182);
		sendSkills();
		sendEmotes();
		sendSettings();
		sendTaskSystem();
		sendCombatStyles();
		sendSquealOfFortuneTab();
		sendXPPopup();
	}

	public void sendQuestTab() {
		sendQuestTab(3002);//Settings.HOSTED ? 190 : 930); //190
		QuestTab.refresh(player, true);
	}

	public void sendQuestTab(int interfaceId) {
		setWindowInterface(resizableScreen ? 114 : 174, interfaceId);
	}

	public void sendXPPopup() {
		setWindowInterface(resizableScreen ? 38 : 10, 1213); // xp
	}
	
	

	public void sendXPDisplay() {
		sendXPDisplay(1215); // xp counter
	}

	public void sendXPDisplay(int interfaceId) {
		setWindowInterface(resizableScreen ? 27 : 29, interfaceId); // xp counter
	}

	public void closeXPPopup() {
		removeWindowInterface(resizableScreen ? 38 : 10);
	}

	public void closeXPDisplay() {
		removeWindowInterface(resizableScreen ? 27 : 29);
	}

	public void sendEquipment() {
		setWindowInterface(resizableScreen ? 116 : 176, 387);
	}

	public void closeEquipment() {
		removeWindowInterface(resizableScreen ? 116 : 176);
	}

	public void sendInventory() {
		setWindowInterface(resizableScreen ? 115 : 175, Inventory.INVENTORY_INTERFACE);
	}

	public void closeInventory() {
		removeWindowInterface(resizableScreen ? 115 : 175);
	}

	public void closeSkills() {
		removeWindowInterface(resizableScreen ? 113 : 206);
	}

	public void closeCombatStyles() {
		removeWindowInterface(resizableScreen ? 111 : 204);
	}

	public void closeTaskSystem() {
		removeWindowInterface(resizableScreen ? 112 : 205);
	}

	public void sendCombatStyles() {
		setWindowInterface(resizableScreen ? 111 : 171, 884);
	}

	public void sendTaskSystem() {
		setWindowInterface(resizableScreen ? 112 : 172, 3207);//1056);
	}

	public void sendSkills() {
		setWindowInterface(resizableScreen ? 113 : 173, 320);
	}

	public void sendSettings() {
		sendSettings(261);
	}

	public void sendSettings(int interfaceId) {
		setWindowInterface(resizableScreen ? 123 : 183, interfaceId);
	}

	public void sendPrayerBook() {
		setWindowInterface(resizableScreen ? 117 : 177, 271);
	}

	public void closePrayerBook() {
		removeWindowInterface(resizableScreen ? 117 : 210);
	}

	public void sendMagicBook() {
		setWindowInterface(resizableScreen ? 118 : 178, player.getCombatDefinitions().getSpellBook());
	}

	public void closeMagicBook() {
		removeWindowInterface(resizableScreen ? 118 : 211);
	}

	public void sendSquealOfFortuneTab() {
		//player.getSquealOfFortune().sendSpinCounts();
		player.getPackets().sendCSVarInteger(823, 1); // this config used in cs2 to tell current extra tab type (0 - none, 1 - sof, 2 - armies minigame tab)
		setWindowInterface(resizableScreen ? 119 : 179, 3206);//1330);//1056);
		//player.getPackets().sendIComponentText(1330, 5, "<shad=ff0000>HOME"/*(player.isDiamondDonator() ? "" : "<col=ff0000>")+ "Switch Prayers"*/);
		//player.getPackets().sendIComponentText(1330, 17, "<shad=ff0000>TELEPORTS"/*(player.isDiamondDonator() ? "" : "<col=ff0000>")+ "Switch Spellbook"*/);
		//player.getPackets().sendIComponentText(1330, 20, "<shad=ff0000>SLAYER TASK");
		//player.getPackets().sendIComponentText(1330, 23, "<img=10> "+(player.isDonator() ? "" : "<col=ff0000>")+ "<shad=ff0000>DONATOR ZONE <img=10>     ");
		//player.getPackets().sendIComponentText(1330, 26, "<img=13> "+(player.isLegendaryDonator() ? "" : "<col=ff0000>")+ "<shad=ff0000>VIP ZONE <img=13>     ");

	}

	public void closeSquealOfFortuneTab() {
		removeWindowInterface(resizableScreen ? 119 : 179);
		player.getPackets().sendCSVarInteger(823, 0); // this config used in cs2 to tell current extra tab type (0 - none, 1 - sof, 2 - armies minigame tab)
	}

	public void sendEmotes() {
		setWindowInterface(resizableScreen ? 124 : 184, 590);
	}

	public void closeEmotes() {
		removeWindowInterface(resizableScreen ? 124 : 217);
	}

	public void setInterface(boolean clickThrought, int parentInterfaceId, int parentInterfaceComponentId, int interfaceId) {
		if (Settings.DEBUG) {
			if (parentInterfaceId != rootInterface && !containsInterface(parentInterfaceId))
				System.out.println("The parent interface isnt setted so where are u trying to set it? " + parentInterfaceId + ", " + parentInterfaceComponentId + ", " + interfaceId);
			/* if(containsInterface(interfaceId))
			     System.out.println("Already have "+interfaceId+" in another component.");*/
		}
		//even so lets set it for now
		int parentUID = getComponentUId(parentInterfaceId, parentInterfaceComponentId);
		Integer oldInterface = openedinterfaces.get(parentUID);
		if (oldInterface != null)
			clearChilds(oldInterface);
		openedinterfaces.put(parentUID, interfaceId); //replaces inter if theres one in that component already
		player.getPackets().sendInterface(clickThrought, parentUID, interfaceId);
	}

	public boolean removeInterfaceByParent(int parentInterfaceId, int parentInterfaceComponentId) {
		return removeInterfaceByParent(getComponentUId(parentInterfaceId, parentInterfaceComponentId));
	}

	public boolean removeInterfaceByParent(int parentUID) {
		Integer removedInterface = openedinterfaces.remove(parentUID);
		if (removedInterface != null) {
			clearChilds(removedInterface);
			player.getPackets().closeInterface(parentUID);
			return true;
		}
		return false;
	}

	private void clearChilds(int parentInterfaceId) {
		for (int key : openedinterfaces.keySet()) {
			if (key >> 16 == parentInterfaceId)
				openedinterfaces.remove(key);
		}
	}

	public void removeInterface(int interfaceId) {
		int parentUID = getInterfaceParentId(interfaceId);
		if (parentUID == -1)
			return;
		removeInterfaceByParent(parentUID);
	}

	public void setRootInterface(int rootInterface, boolean gc) {
		this.rootInterface = rootInterface;
		player.getPackets().sendRootInterface(rootInterface, gc ? 3 : 0);
	}

	public static int getComponentUId(int interfaceId, int componentId) {
		return interfaceId << 16 | componentId;
	}

	public int getInterfaceParentId(int interfaceId) {
		if (interfaceId == rootInterface)
			return -1;
		for (int key : openedinterfaces.keySet()) {
			int value = openedinterfaces.get(key);
			if (value == interfaceId)
				return key;
		}
		return -1;
	}

	public boolean containsInterfaceAtParent(int parentInterfaceId, int parentInterfaceComponentId) {
		return openedinterfaces.containsKey(getComponentUId(parentInterfaceId, parentInterfaceComponentId));
	}

	public boolean containsInterface(int interfaceId) {
		if (interfaceId == rootInterface)
			return true;
		for (int value : openedinterfaces.values())
			if (value == interfaceId)
				return true;
		return false;
	}

	public void removeAll() {
		openedinterfaces.clear();
	}

	public boolean containsWindowInterfaceAtParent(int componentId) {
		return containsInterfaceAtParent(resizableScreen ? RESIZABLE_WINDOW_ID : FIXED_WINDOW_ID, componentId);
	}

	public boolean containsScreenInter() {
		return containsWindowInterfaceAtParent(resizableScreen ? RESIZABLE_SCREEN_COMPONENT_ID : FIXED_SCREEN_COMPONENT_ID);
	}

	public void removeScreenInterface() {
		removeWindowInterface(resizableScreen ? RESIZABLE_SCREEN_COMPONENT_ID : FIXED_SCREEN_COMPONENT_ID);
	}

	public boolean containsInventoryInter() {
		return containsWindowInterfaceAtParent(resizableScreen ? RESIZABLE_INV_COMPONENT_ID : FIXED_INV_COMPONENT_ID);
	}

	public void removeInventoryInterface() {
		removeWindowInterface(resizableScreen ? RESIZABLE_INV_COMPONENT_ID : FIXED_INV_COMPONENT_ID);
	}

	public void setFadingInterface(int backgroundInterface) {
		setWindowInterface(hasRezizableScreen() ? 12 : 11, backgroundInterface);
	}

	public void closeFadingInterface() {
		removeWindowInterface(hasRezizableScreen() ? 12 : 11);
	}

	public void setScreenInterface(int backgroundInterface, int interfaceId) {
		setScreenInterface(true, backgroundInterface, interfaceId);
	}
	
	public boolean containsScreenInterface() {
		return containsWindowInterfaceAtParent(hasRezizableScreen() ? 41 : 201);
	}

	public void setScreenInterface(boolean walkable, int backgroundInterface, int interfaceId) {
		removeScreenInterface();
		setWindowInterface(walkable, hasRezizableScreen() ? 40 : 200, backgroundInterface);
		setWindowInterface(walkable, hasRezizableScreen() ? 41 : 201, interfaceId);

		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
				removeScreenInterfaceBG();
			}
		});
	}

	public void removeScreenInterfaceBG() {
		removeWindowInterface(hasRezizableScreen() ? 40 : 200);
		removeWindowInterface(hasRezizableScreen() ? 41 : 201);
	}

	public boolean hasRezizableScreen() {
		return resizableScreen;
	}

	public void setWindowsPane(int windowsPane) {
		this.rootInterface = windowsPane;
	}

	public int getWindowsPane() {
		return rootInterface;
	}

	public void gazeOrbOfOculus() {
		player.stopAll();
		setRootInterface(475, false);
		setInterface(true, 475, 57, 751);
		setInterface(true, 475, 55, 752);
		player.setCloseInterfacesEvent(new Runnable() {

			@Override
			public void run() {
				setDefaultRootInterface();
				player.getPackets().sendResetCamera();
			}

		});
	}

	/*
	 * returns lastGameTab
	 */
	public int openGameTab(int tabId) {
		player.getPackets().sendCSVarInteger(168, tabId);
		int lastTab = currentTab; 
		setCurrentTab(tabId);
		return lastTab;
	}
	
	public int getCurrentTab() {
		return currentTab;
	}
	
	public void setCurrentTab(int tab) {
		this.currentTab = tab;
		if (tab == 3 && containsInterface(3002))
			QuestTab.refresh(player, false);
		if (Settings.DEBUG)
			System.out.println("Current tab: "+tab);
	}
	
	public boolean isResizable() {
		return resizableScreen;
	}

}
