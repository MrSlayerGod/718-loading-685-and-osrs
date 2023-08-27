package com.rs.game.player.content.collectionlog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.rs.cache.loaders.NPCConfig;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.utils.ItemSetsKeyGenerator;

/**
 * @author Simplex
 * @since May 07, 2020
 */
public class CollectionLog implements Serializable {
    private static final long serialVersionUID = 4602692016264458065L;

    public static final int ID = 3031;

    public static CollectionLog MASTER = new CollectionLog();

    public static void init() {
    	CollectionLogBuilder.build(MASTER);
    }

    // Components
    private static final int CAT_SPRITE_START = 19, TAB_BTN_START = 51, COLLECTED_ITEM_CONTAINER = 45,
                            BOSS_NAME_STRING = 40, OBTAINED_STRING = 41, KILLS_STRING = 42;

    // maximum tabs in the category
    private static final int MAX_TABS = 53;

    public static final int ITEM_CONTAINER_KEY = ItemSetsKeyGenerator.COLLECTOR_LOG_KEY;
    public static final int ITEM_W = 6, ITEM_H = 20;

    private transient int tabId = 0;
    private transient CategoryType category = CategoryType.BOSSES;

    // used when clicking a tab to get tab name
    private transient ArrayList<String> tabs = new ArrayList<String>();

    // the categories available on interface
    private LogCategory bosses, clues, minigames, others, raids;

    public LogCategory getBosses() { return bosses; }

    public LogCategory getClues() { return clues; }

    public LogCategory getMinigames() { return minigames; }

    public LogCategory getOthers() { return others; }

    public LogCategory getRaids() { return raids; }

    private transient Player player = null;

    public CollectionLog() {
        bosses = new LogCategory(CategoryType.BOSSES);
        clues = new LogCategory(CategoryType.CLUES);
        minigames = new LogCategory(CategoryType.MINIGAMES);
        others = new LogCategory(CategoryType.OTHERS);
        raids = new LogCategory(CategoryType.RAIDS);
    }

    public void init(Player player) {
        this.player = player;
        this.category = CategoryType.BOSSES;
        tabs = new ArrayList<String>();
    }

    public void open() {
        sendComponentOps();
        writeCategory();
        player.getInterfaceManager().sendInterface(ID);

        player.setCloseInterfacesEvent(() -> {
            tabId = 0;
            category = CategoryType.BOSSES;
        });
    }


    public void buttonClick(Player player, int componentId) {
        CategoryType previousCategory = category;
        int previousTab = tabId;

        if(componentId > TAB_BTN_START && componentId < TAB_BTN_START + MAX_TABS*3) {
            if((componentId + 1 - TAB_BTN_START) % 3 == 0) {
                tabId = (componentId - TAB_BTN_START) / 3;
                if(tabId != previousTab)
                    writeCategory();
                return;
            }
        }

        switch(componentId) {
            case 18:
                category = CategoryType.BOSSES;
                break;
            case 21:
                category = CategoryType.RAIDS;
                break;
            case 24:
                category = CategoryType.CLUES;
                break;
            case 27:
                category = CategoryType.MINIGAMES;
                break;
            case 30:
                category = CategoryType.OTHERS;
                break;
        }

        if(previousCategory != category) {
            tabId = 0;
            writeCategory();
        }
    }

    private void writeCategory() {
        writeTabs();
        writeCollectedItems();
        writeDetails();

        for(int i = 0; i < CategoryType.values().length; i++) {
            player.getPackets().sendIComponentSprite(ID, CAT_SPRITE_START + i*3, category == CategoryType.values()[i] ? 952 : 953);
        }
    }

    private void writeDetails() {
        String key = tabs.get(tabId);
        String kills  = getKills(key);
        player.getPackets().sendIComponentText(ID, BOSS_NAME_STRING, key);
        player.getPackets().sendIComponentText(ID, KILLS_STRING, kills == null ? "" : getKillString(key, kills));
        player.getPackets().sendIComponentText(ID, OBTAINED_STRING, getCompletion(category, key));
        player.getPackets().sendIComponentText(ID, BOSS_NAME_STRING, tabs.get(tabId));
    }

    /*
     * null hides string
     */
    private String getKills(String key) {
        Integer lookup;

        switch(category) {
            case BOSSES:
                lookup = player.getBossKillcount().get(key.toLowerCase());
                return "" + (lookup == null ? 0 : lookup);
            case RAIDS:
                switch(key) {
                    case "Theatre of Blood":
                        lookup = player.getBossKillcount().get("Theatre of Blood".toLowerCase());
                        return "" + (lookup == null ? 0 : lookup);
                    case "Chambers of Xeric":
                        return "" + player.getChambersCompletions() + player.getOsrsChambersCompletions();
                }
                break;
            case CLUES:
                switch(key) {
                    case "Easy Clues":
                        return "" + player.getTreasureTrailsManager().getCluesCompleted();
                    case "Medium Clues":
                        return "" + player.getTreasureTrailsManager().getMedCluesCompleted();
                    case "Hard Clues":
                        return "" + player.getTreasureTrailsManager().getHardCluesCompleted();
                    case "Elite Clues":
                        return "" + player.getTreasureTrailsManager().getEliteCluesCompleted();
                }
                break;
            case MINIGAMES:
                switch(key) {
                    case "Barrows":
                        lookup = player.getBossKillcount().get("Barrows Chests".toLowerCase());
                        return "" + (lookup == null ? 0 : lookup);
                    case "Castle Wars":
                        return "" + player.getFinishedCastleWars();
                    case "Crucible":
                        return null;//"" + player.getCrucibleHighScore();
                    case "Dominion Tower":
                        return "" + player.getDominionTower().getTotalScore();
                    case "Fight Caves":
                        return "" + player.getFightCavesCompletions();
                    case "Fight Kiln":
                        return "" + player.getFightKilnCompletions();
                    case "The Inferno":
                        return "" + player.getInfernoCompletions();
                    case "Lava Flow Mine":
                        return null;
                    case "Pest Control":
                        return "" + player.getPestGames();
                    case "Puro-Puro":
                        return null;
                    case "Runespan":
                        return null;
                    case "Sorceress Garden":
                        return "" + player.getSorceressGardenCompletions();
                    case "Sawmill":
                        return "" + player.getSawmillJobsComplete();

                }
                break;
            //case OTHERS:
            //  return null;
		default:
			break;
        }

        return null;
    }

    public String getCompletion(CategoryType category, String key) {
        Map<Integer, Integer> lootTab = getCategory(category).obtainedDrops.get(key);
        Map<Integer, Integer> masterTab = MASTER.getCategory(category).obtainedDrops.get(key);

        int completion = lootTab == null ? 0 : lootTab.size();

        String prefix = "";
        if(completion == masterTab.size())
            // completed tab = numbers green
            prefix = "<col=00ff00>";
        else if (completion == 0)
        	 prefix = "<col=FF0000>";
        else
        	prefix = "<col=FFFF00>";

        return "Obtained: " + prefix + completion + " / " + masterTab.size();
    }

    /**
     * Checks if a type and key has been complete or not
     * @param type
     * @param key
     * @return
     */
    private boolean hasComplete(CategoryType type, String key) {
        Map<Integer, Integer> lootTab = getCategory(category).obtainedDrops.get(key);
        Map<Integer, Integer> masterTab = MASTER.getCategory(category).obtainedDrops.get(key);
        int completion = lootTab == null ? 0 : lootTab.size();

        if (masterTab == null) return false;

        if(completion == masterTab.size()) {
            return true;
        }

        return false;
    }


    private void sendComponentOps() {
        player.getPackets().sendUnlockIComponentOptionSlots(ID, COLLECTED_ITEM_CONTAINER, 0, ITEM_W*ITEM_H-1, 0, 1);
        player.getPackets().sendInterSetItemsOptionsScript(ID, COLLECTED_ITEM_CONTAINER, ITEM_CONTAINER_KEY, ITEM_W, ITEM_H, "Examine");
    }

    private void writeCollectedItems() {
        ItemsContainer<Item> masterLog = MASTER.getCategory(category).getCollectionList(tabs.get(tabId));
        ItemsContainer<Item> log = getCategory(category).getCollectionList(tabs.get(tabId));
        ItemsContainer<Item> mergedLogs = new ItemsContainer<>(masterLog.getSize(), true);
        // merge
        for(Item item : masterLog.getItems())
            if(item != null)
                mergedLogs.forceAdd(new Item(item)); // forceadd as they are 0 stacks
        mergedLogs.addAll(log.getItems());
        player.getPackets().sendItems(ITEM_CONTAINER_KEY, mergedLogs);
    }

    public LogCategory getCategory(CategoryType type) {
        switch(type) {
            default:
            case BOSSES:
                return getBosses();
            case CLUES:
                return getClues();
            case MINIGAMES:
                return getMinigames();
            case OTHERS:
                return getOthers();
            case RAIDS:
                return getRaids();
        }
    }

    private void writeTabs() {
        tabs.clear();

        // get tab names to display
        for(String s : MASTER.getCategory(category).obtainedDrops.keySet())
            if(tabs.size() == MAX_TABS)
                System.err.println("Error: Too many tabs in " + category.name + " category, cannot display " + s + "!");
            else
                tabs.add(s);

        Collections.sort(tabs);

        String tabName;
        int component = TAB_BTN_START;
        for(int i = 0; i < MAX_TABS; i++, component += 3) {
            boolean hidden = i >= tabs.size();
            tabName = !hidden ? tabs.get(i) : "";

            // Here we check if it has been complete or not
            if (hasComplete(category, tabName)) {
                tabName = "<col=00ff00>" + tabName;
            } else if(i == tabId) {
                tabName = "<col=f85515>" + tabName;
            } else {
                tabName = "<col=ff981f>" + tabName;
            }
            player.getPackets().sendHideIComponent(ID, component, hidden);
            player.getPackets().sendIComponentText(ID, component+2, tabName);
        }
    }

    public String getKillString(String tabName, String kills) {
        if(category.killString == null)
            return "";
        return "<col=ff981f>" +tabName + " " + category.killString + " <col=FFFFFF>" + kills;
    }

    
    public void add(String tab, Item item) {
    	for (CategoryType type : CategoryType.values()) {
    		LogCategory log = MASTER.getCategory(type);
    		for (String name : log.getDrops().keySet()) {
    			if (name.equalsIgnoreCase(tab)) {
    				add(type, name, item);
    				break;
    			}
    		}
    	}
    }
    
    public void add(CategoryType category, String tab, Item item) {
        getCategory(category).addToLog(tab, item);
    }
    public void npcDrop(int npc, Item item) {
        String npcName = NPCConfig.forID(npc).name;
        if(npcName == null)
            return;
        add(CategoryType.BOSSES, npcName, item);
    }
    public void npcDrop(int npc, int id, int amount) {
        npcDrop(npc, new Item(id, amount));
    }

    // generally used by minigame reward shops
    public void shopPurchase(Item item) {
        for(Map.Entry<String, Map<Integer, Integer>> tab : getMinigames().getDrops().entrySet()) {
            if(tab.getValue().keySet().contains(item.getId())) {
                // this tab has the item from the shop, increment collected
                add(CategoryType.MINIGAMES, tab.getKey(), item);
            }
        }
    }
}

/**
 * Holds all data for category
 */
class LogCategory implements Serializable {

    private static long serialVersionUID = 248848120918481361L;

    /**
     * Tab<Name, Rewards<itemId, amount>
     */
    Map<String, Map<Integer, Integer>> obtainedDrops;

    CategoryType categoryType;

    public LogCategory(CategoryType category) {
        this.categoryType = category;
        obtainedDrops = new LinkedHashMap<>();
    }

    /**
     * Belongs to CollectionLog.MASTER
     */
    public boolean isMaster() {
        return CollectionLog.MASTER.getCategory(categoryType) == this;
    }

    /**
     * Call to add a drop to the log (ex. boss drops, clue reward, etc)
     */
    public void addToLog(String key, Item value) {
        Map<Integer, Integer> lootTab = obtainedDrops.get(key);

        if (lootTab == null) {
            // if tab doesn't exist, create
            lootTab = new HashMap<Integer, Integer>();
            obtainedDrops.put(key, lootTab);
        }
        //System.out.println("Add " + amt + 'x' + value.getName()+" to " + key);

        lootTab.merge(value.getId(), value.getAmount(), Integer::sum);
  //      System.out.println("Value of "+ value.getName()+" = " + lootTab.get(value.getId()));
    }


    /**
     * Init a list of items
     */
    public void init(String key, Item[] val) {
        for(Item i : val) init(key, i.getId());
    }

    /**
     * Init a list of items
     */
    public void init(String key, Integer[] val) {
        for(int i : val) init(key, i);
    }

    /**
     * Init a list of items
     */
    public void init(String key, int[] val) {
        for(int i : val) init(key, i);
    }

    /**
     * On login init all drops, if more are added in the future
     * the log will rebuilt with missing indexes
     */
    public void init(String key, int value) {
        Map<Integer, Integer> lootTab = obtainedDrops.get(key);

        if (lootTab == null) {
            // if tab doesn't exist, create
            lootTab = new LinkedHashMap<Integer, Integer>();
            obtainedDrops.put(key, lootTab);
        }

        lootTab.putIfAbsent(value, new Integer(0));
    }

    /**
     * Used when merging MASTER list and personal list
     */
    public ItemsContainer<Item> getCollectionList(String lootTabKey) {
        Map<Integer, Integer> lootTab;
        ItemsContainer<Item> con = new ItemsContainer<Item>(CollectionLog.ITEM_W * CollectionLog.ITEM_H,true);

        try {
            lootTab = obtainedDrops.get(lootTabKey);

            if (lootTab == null && isMaster()) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException arg) {
            lootTab = null;
            System.err.println("Could not find loot table! category=" + categoryType + " tab=" + lootTabKey);
            arg.printStackTrace();
        }

        if(lootTab == null) {
            // player hasn't saved any items from this loot tab yet
            return con;
        }

        lootTab.forEach((item, amt) -> con.add(new Item(item, amt, -1, true)));
        return con;
    }

    public Map<String, Map<Integer, Integer>> getDrops() {
        return obtainedDrops;
    }
}


