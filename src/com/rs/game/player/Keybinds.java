package com.rs.game.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simplex
 * @since May 05, 2020
 */
public class Keybinds implements Serializable {

    private static transient final long serialVersionUID = -5688544433624132221L;
    
    public static final int ID = 3040;
    private static final int FIRST_BTN_COMPONENT = 135;

    // save only the binds they've manually set, else client uses defaults
    private HashMap<Tab,Integer> keybinds = new HashMap<Tab,Integer>();
    private boolean escToExit = true;
    
    private transient int setTab = -1;

    public Keybinds() {
        setDefaults();
    }

    public void setDefaults() {
        keybinds = new HashMap<Tab,Integer>();
        for(Tab tab : Tab.values()) {
            keybinds.put(tab, tab.getDefaultKey().ordinal());
        }
    }

    public void open(Player player) {
        player.stopAll();
        writeKeybinds(player);
        writeStrings(player);
        // set esc to exit sprite
        player.getPackets().sendIComponentSprite(ID, 444, player.getKeyBinds().escToExit ? 181 : 180);
        player.getInterfaceManager().sendInterface(3040);
        
        player.setCloseInterfacesEvent(new Runnable() {
        	
        	@Override
        	public void run() {
        		if (setTab >= 0)
        			setTab(player, -1);
        	}
        });
    }

    private void writeStrings(Player player) {
        for(Tab tab : Tab.values()) {
            int key = keybinds.get(tab) != null ? keybinds.get(tab) : tab.defaultKey.ordinal();

            player.getPackets().sendIComponentText(ID, FIRST_BTN_COMPONENT + (tab.getComponent() * 5) - 1, Key.values()[key].name());
        }
    }

    public void writeKeybinds(Player player) {
        ArrayList<Object> payload = new ArrayList<Object>();
        payload.add(escToExit ? 1 : 0);

        if(keybinds == null)
            setDefaults();

        for(Map.Entry<Tab, Integer> keybind : keybinds.entrySet()) {
            if(keybind == null) {
                System.err.println("keybind null in map");
            } else {
                // System.out.println(keybind.getKey()+", "+keybind.getValue());
                payload.add(keybind.getValue());
                payload.add(keybind.getKey().ordinal());
            }
        }

        //encoder writes payload in reverse
        Collections.reverse(payload);

        // write custom key binds
        player.getPackets().sendExecuteScript(-17, payload.toArray());
    }

    public static void buttonClick(Player player, int buttonComponentId) {

        if(buttonComponentId >= FIRST_BTN_COMPONENT && buttonComponentId <= FIRST_BTN_COMPONENT + Tab.values().length * 5) {
            int componentIndex = (buttonComponentId - FIRST_BTN_COMPONENT) / 5;
            player.getPackets().sendIComponentText(ID, buttonComponentId-1, "Enter key..");
            player.getKeyBinds().setTab(player, componentIndex);
          /*  player
                    .sendMessage("Settab " + componentIndex);*/
        } else if(buttonComponentId == 24) {
            player.getKeyBinds().setDefaults();
            player.getKeyBinds().open(player);
            player.sendMessage("Keybinds have been restored to default settings.");
        } else if(buttonComponentId == 21) {
            player.getKeyBinds().escToExit = !player.getKeyBinds().escToExit;
            player.getPackets().sendIComponentSprite(ID, 444, player.getKeyBinds().escToExit ? 181 : 180);
            player.sendMessage("Interfaces will " + (player.getKeyBinds().escToExit ? "" : "no longer ") + "close when pressing the ESC key.");
            player.getKeyBinds().writeKeybinds(player);
        }
    }

    public void setTab(Player player, int tab) {
    	setTab = tab;
    	player.getPackets().sendExecuteScript(-18, tab >= 0 ? 1 : 0);
    }
    
    public void create(Player player, int fkey) {
    	if (fkey >= Key.values().length || setTab == -1 || !player.getInterfaceManager().containsInterface(ID))
    		return;
    	int component = setTab;
        Key key = Key.values()[fkey];
        Tab t = null;

        // find Tab for client component (button)
        for (Tab tab : Tab.values())
            if (tab != null && tab.getComponent() == component)
                t = tab;

        if(t == null) {
            System.err.println("Error: could not find Tab for " + component +"!");
            return;
        }
        setTab = -1;
        if (key.ordinal() == keybinds.get(t)) {
        	setTab(player, -1);
        	writeStrings(player);
        	return;
        }
        if(keybinds.containsKey(t)) {
            Tab existingTab = null;
            // find existing tab bound to this key
            for (Tab tab : keybinds.keySet())
                if (keybinds.get(tab) == key.ordinal())
                    existingTab = tab;
            if (existingTab != null) {
            	keybinds.put(existingTab, Key.NONE.ordinal());
            	// player.sendMessage(existingTab+ " Removing keybind");
            }
        }
        keybinds.put(t, key.ordinal());
       // player.sendMessage("Set tab " + t + " to " + key);
        player.getPackets().sendIComponentText(ID, FIRST_BTN_COMPONENT + (t.getComponent() * 5) - 1, key.name());
        writeKeybinds(player);
        writeStrings(player);
       // player.sendMessage(keybinds.toString());
    }

    enum Key {
        F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, ESC, NONE
    }

    enum Tab {
        COMBAT(0, 0, Key.F1), TELES(1, 14, Key.F2), SKILLS(2, 1, Key.F3), QUESTS(3, 2, Key.F4), INV(4, 3, Key.F5), GEAR(5, 4, Key.F6), PRAYER(6, 5, Key.F7), MAGIC(7, 6, Key.F8),
        SQUEAL(8, -1, Key.NONE), FRIENDS(9, 7, Key.F10), C_CHAT(11, 8, Key.F11), F_CHAT(10, 9, Key.F12), NOTES(15, 10, Key.NONE), SETTINGS(12, 11, Key.NONE), EMOTES(12, 12, Key.NONE), MUSIC(14, 13, Key.NONE),  LOGOUT(16, 16, Key.NONE);

        int tabId = 0;
        int component = 1;
        Key defaultKey = Key.NONE;

        Tab(int tab, int component, Key defaultKey) {
            this.tabId = tab;
            this.component = component;
            this.defaultKey = defaultKey;
        }

        public Key getDefaultKey() {
            return defaultKey;
        }

        public int getTabId() {
            return tabId;
        }

        public int getComponent() {
            return component;
        }
    }
}

