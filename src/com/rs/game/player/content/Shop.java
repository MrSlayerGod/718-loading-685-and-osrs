package com.rs.game.player.content;

import java.util.concurrent.CopyOnWriteArrayList;

import com.rs.Settings;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemConfig;
import com.rs.discord.Bot;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.utils.ItemExamines;
import com.rs.utils.ItemWeights;
import com.rs.utils.Utils;
import com.rs.utils.WeaponTypesLoader;

public class Shop {

    private static final int GENERAL_STOCK_ITEMS_KEY = 139, MAIN_STOCK_ITEMS_KEY = 661;

    private static final int MAX_SHOP_ITEMS = 200;
    public static final int COINS = 995, TOKKUL = 6529, AURA = 25447, BARROW = 25458, BLOOD = 43307, PKP = 30000, VOTE = 25459,
            DONATOR = 25472, MOLCH_PEARL = 52820, SLAYER = 4149, LOYAL = 25434,
            EASTER_EGG = 1961;

    private String name;
    private Item[] mainStock;
    private int[] defaultQuantity;
    private Item[] generalStock;
    private int money;
    private CopyOnWriteArrayList<Player> viewingPlayers;
    private int key;

    public Shop(int key, String name, int money, Item[] mainStock, boolean isGeneralStore) {
        viewingPlayers = new CopyOnWriteArrayList<Player>();
        this.key = key;
        this.name = name;
        this.money = money;
        this.mainStock = mainStock;
        defaultQuantity = new int[mainStock.length];
        for (int i = 0; i < defaultQuantity.length; i++)
            defaultQuantity[i] = mainStock[i].getAmount();
        if (isGeneralStore && mainStock.length < MAX_SHOP_ITEMS)
            generalStock = new Item[MAX_SHOP_ITEMS - mainStock.length];
    }

    public Item[] getMainStock() {
        return mainStock;
    }

    public boolean isGeneralStore() {
        return generalStock != null;
    }


    private void removePlayer(Player player) {
        if (money == PKP) {
            player.getInterfaceManager().sendCombatStyles();
            player.getInterfaceManager().sendTaskSystem();
            player.getInterfaceManager().sendSkills();
            player.getInterfaceManager().sendEquipment();
            player.getInterfaceManager().sendPrayerBook();
            player.getInterfaceManager().sendMagicBook();
            player.getInterfaceManager().sendEmotes();
            player.getInterfaceManager().openGameTab(4);
        }
        viewingPlayers.remove(player);
        player.getTemporaryAttributtes().remove("Shop");
        player.getTemporaryAttributtes().remove("shop_transaction");
        player.getTemporaryAttributtes().remove("isShopBuying");
        player.getTemporaryAttributtes().remove("ShopSelectedSlot");
        player.getTemporaryAttributtes().remove("ShopSelectedInventory");
    }

    public void addPlayer(final Player player) {
        viewingPlayers.add(player);
        player.getTemporaryAttributtes().put("Shop", this);
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                player.getPackets().setShopPrices();
                removePlayer(player);
            }
        });


        if (key == 914 || money == LOYAL || money == SLAYER || money == BARROW || money == BLOOD || money == VOTE || money == DONATOR || money == MOLCH_PEARL
                || money == EASTER_EGG) {

            Object[] pricesData = new Object[mainStock.length * 2];
            for (int i = 0; i < mainStock.length; i++) {
                Item item = mainStock[i];
                if (item.getDefinitions().isNoted())
                    item = new Item(item.getDefinitions().getCertId(), item.getAmount());
                pricesData[i * 2] = item.getId();
                pricesData[i * 2 + 1] = getBuyPrice(item);
            }
            player.getPackets().setShopPrices(pricesData);

            //	player.getPackets().sendHideIComponent(1265, 205, true);
        }

        player.refreshVerboseShopDisplayMode();
        player.getVarsManager().sendVar(118, generalStock != null ? GENERAL_STOCK_ITEMS_KEY : MAIN_STOCK_ITEMS_KEY);
        player.getVarsManager().sendVar(1496, -1); // sample items container id (TODO: add support for it)
        player.getVarsManager().sendVar(532, money);
        resetSelected(player);
        //	player.getPackets().sendCSVarString(336, customcs2configstring(player)); // makes my hand crafted cs2 to set prices work
        sendStore(player);
        player.getInterfaceManager().sendInterface(1265); // opens shop

        if ((money == COINS && key >= 1200)) {
            // since vote prices are custom
            // the least we can do is disable price displaying on items
            //player.getPackets().sendHideIComponent(1265, 23, true);
            //player.getPackets().sendHideIComponent(1265, 24, true);

            player.getPackets().sendGameMessage("Warning! All oracle items are based in grand exchange prices but 40% more expensive so you're recommended to use g.e. instead.");
        }
        if (money == SLAYER)
            player.getPackets().sendGameMessage("You currently have " + player.getSlayerManager().getPoints() + " slayer points.");

        resetTransaction(player);
        setBuying(player, true);
        if (generalStock != null)
            player.getPackets().sendHideIComponent(1265, 19, false); // unlocks general store icon
        player.getPackets().sendUnlockIComponentOptionSlots(1265, 20, 0, getStoreSize(), 0, 1, 2, 3, 4, 5, 6); // unlocks stock slots
        sendInventory(player);
        player.getPackets().sendIComponentText(1265, 85, name);
    }

    public void resetTransaction(Player player) {
        setTransaction(player, 1);
    }

    public void increaseTransaction(Player player, int amount) {
        setTransaction(player, getTransaction(player) + amount);
    }

    public int getTransaction(Player player) {
        Integer transaction = (Integer) player.getTemporaryAttributtes().get("shop_transaction");
        return transaction == null ? 1 : transaction;
    }

    public void stats(Player player) {
        Integer selectedSlot = (Integer) player.getTemporaryAttributtes().get("ShopSelectedSlot");
        Boolean inventory = (Boolean) player.getTemporaryAttributtes().get("ShopSelectedInventory");
        if (selectedSlot == null || inventory == null)
            return;
        Item item = inventory ? player.getInventory().getItem(selectedSlot) : selectedSlot >= mainStock.length ? generalStock[selectedSlot - mainStock.length] : mainStock[selectedSlot];
        if (item == null)
            return;
        if (item.getDefinitions().isNoted())
            item = new Item(item.getDefinitions().getCertId(), item.getAmount());
        if (!item.getDefinitions().isWearItem())
            return;
        Item wearSlot = player.getEquipment().getItem(item.getDefinitions().getEquipSlot());


        player.getPackets().sendIComponentText(1265, 101, "Stats for " + item.getName() + ":");

        player.getPackets().sendIComponentText(1265, 107, "<col=FFFFFF>" + item.getDefinitions().getSlashAttack());
        player.getPackets().sendIComponentText(1265, 108, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getSlashAttack())) + ")");
        player.getPackets().sendIComponentText(1265, 115, "<col=FFFFFF>" + item.getDefinitions().getStabAttack());
        player.getPackets().sendIComponentText(1265, 116, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getStabAttack())) + ")");
        player.getPackets().sendIComponentText(1265, 121, "<col=FFFFFF>" + item.getDefinitions().getCrushAttack());
        player.getPackets().sendIComponentText(1265, 122, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getCrushAttack())) + ")");
        player.getPackets().sendIComponentText(1265, 127, "<col=FFFFFF>" + item.getDefinitions().getRangeAttack());
        player.getPackets().sendIComponentText(1265, 128, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getRangeAttack())) + ")");
        player.getPackets().sendIComponentText(1265, 133, "<col=FFFFFF>" + item.getDefinitions().getMagicAttack());
        player.getPackets().sendIComponentText(1265, 134, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getMagicAttack())) + ")");

        player.getPackets().sendIComponentText(1265, 112, "<col=FFFFFF>" + item.getDefinitions().getSlashDef());
        player.getPackets().sendIComponentText(1265, 113, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getSlashDef())) + ")");
        player.getPackets().sendIComponentText(1265, 118, "<col=FFFFFF>" + item.getDefinitions().getStabDef());
        player.getPackets().sendIComponentText(1265, 119, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getStabDef())) + ")");
        player.getPackets().sendIComponentText(1265, 124, "<col=FFFFFF>" + item.getDefinitions().getCrushDef());
        player.getPackets().sendIComponentText(1265, 125, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getCrushDef())) + ")");
        player.getPackets().sendIComponentText(1265, 130, "<col=FFFFFF>" + item.getDefinitions().getRangeDef());
        player.getPackets().sendIComponentText(1265, 131, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getRangeDef())) + ")");
        player.getPackets().sendIComponentText(1265, 136, "<col=FFFFFF>" + item.getDefinitions().getMagicDef());
        player.getPackets().sendIComponentText(1265, 137, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getMagicDef())) + ")");
        player.getPackets().sendIComponentText(1265, 139, "<col=FFFFFF>" + item.getDefinitions().getSummoningDef());
        player.getPackets().sendIComponentText(1265, 140, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getSummoningDef())) + ")");

        player.getPackets().sendIComponentText(1265, 142, "<col=FFFFFF>" + item.getDefinitions().getStrengthBonus());
        player.getPackets().sendIComponentText(1265, 143, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getStrengthBonus())) + ")");
        player.getPackets().sendIComponentText(1265, 145, "<col=FFFFFF>" + item.getDefinitions().getRangedStrBonus());
        player.getPackets().sendIComponentText(1265, 146, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getRangedStrBonus())) + ")");
        player.getPackets().sendIComponentText(1265, 148, "<col=FFFFFF>" + item.getDefinitions().getMagicDamage());
        player.getPackets().sendIComponentText(1265, 149, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getMagicDamage())) + ")");
        player.getPackets().sendIComponentText(1265, 165, "<col=FFFFFF>" + item.getDefinitions().getAttackSpeed());
        player.getPackets().sendIComponentText(1265, 166, "<col=FFFFFF>(" + (wearSlot == null ? "4" : ("" + wearSlot.getDefinitions().getAttackSpeed())) + ")");

        player.getPackets().sendIComponentText(1265, 151, "<col=FFFFFF>" + item.getDefinitions().getAbsorveMeleeBonus());
        player.getPackets().sendIComponentText(1265, 152, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getAbsorveMeleeBonus())) + ")");
        player.getPackets().sendIComponentText(1265, 154, "<col=FFFFFF>" + item.getDefinitions().getAbsorveRangeBonus());
        player.getPackets().sendIComponentText(1265, 155, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getAbsorveRangeBonus())) + ")");
        player.getPackets().sendIComponentText(1265, 157, "<col=FFFFFF>" + item.getDefinitions().getAbsorveMageBonus());
        player.getPackets().sendIComponentText(1265, 158, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getAbsorveMageBonus())) + ")");

        player.getPackets().sendIComponentText(1265, 160, "<col=FFFFFF>" + item.getDefinitions().getPrayerBonus());
        player.getPackets().sendIComponentText(1265, 161, "<col=FFFFFF>(" + (wearSlot == null ? "0" : ("" + wearSlot.getDefinitions().getPrayerBonus())) + ")");

        player.getPackets().sendIComponentText(1265, 163, "<col=FFFFFF>" + ItemWeights.getWeight(item, true));
    }

    public void pay(Player player) {
        Integer selectedSlot = (Integer) player.getTemporaryAttributtes().get("ShopSelectedSlot");
        Boolean inventory = (Boolean) player.getTemporaryAttributtes().get("ShopSelectedInventory");
        if (selectedSlot == null || inventory == null)
            return;
        int amount = getTransaction(player);
        if (inventory)
            sell(player, selectedSlot, amount);
        else
            buy(player, selectedSlot, amount);
    }

    public int getSelectedMaxAmount(Player player) {
        Integer selectedSlot = (Integer) player.getTemporaryAttributtes().get("ShopSelectedSlot");
        Boolean inventory = (Boolean) player.getTemporaryAttributtes().get("ShopSelectedInventory");
        if (selectedSlot == null || inventory == null)
            return 1;
        if (inventory) {
            Item item = player.getInventory().getItem(selectedSlot);
            if (item == null)
                return 1;
            return player.getInventory().getAmountOf(item.getId());
        } else {
            if (selectedSlot >= getStoreSize())
                return 1;
            Item item = selectedSlot >= mainStock.length ? generalStock[selectedSlot - mainStock.length] : mainStock[selectedSlot];
            if (item == null)
                return 1;
            return item.getAmount();
        }
    }

    public void setTransaction(Player player, int amount) {
        int max = getSelectedMaxAmount(player);
        if (amount > max)
            amount = max;
        else if (amount < 1)
            amount = 1;
        player.getTemporaryAttributtes().put("shop_transaction", amount);
        player.getVarsManager().sendVar(2564, amount);
    }

    public static void setBuying(Player player, boolean buying) {
        player.getTemporaryAttributtes().put("isShopBuying", buying);
        player.getVarsManager().sendVar(2565, buying ? 0 : 1);
    }

    public static boolean isBuying(Player player) {
        Boolean isBuying = (Boolean) player.getTemporaryAttributtes().get("isShopBuying");
        return isBuying != null && isBuying;
    }


    public void buyAll(Player player, int slotId) {
        if (slotId >= getStoreSize())
            return;
        Item item = slotId >= mainStock.length ? generalStock[slotId - mainStock.length] : mainStock[slotId];
        buy(player, slotId, item.getAmount());
    }

    public void buy(Player player, int slotId, int quantity) {
        if (slotId >= getStoreSize())
            return;
        Item item = slotId >= mainStock.length ? generalStock[slotId - mainStock.length] : mainStock[slotId];
        if (item == null)
            return;
        if (item.getAmount() == 0) {
            player.getPackets().sendGameMessage("There is no stock of that item at the moment.");
            return;
        }

        if ((player.isIronman() || player.isUltimateIronman()) && slotId >= mainStock.length) {
            player.getPackets().sendGameMessage("You can't buy other players stock as an ironman.");
            return;
        }

        @SuppressWarnings("unused")
        int dq = slotId >= mainStock.length ? 0 : defaultQuantity[slotId];
        int price = getBuyPrice(item);
        long amountCoins = money == SLAYER ? player.getSlayerManager().getPoints() : money == PKP ? player.getPkPoints() : money == COINS ? player.getInventory().getCoinsAmount() : player.getInventory().getItems().getNumberOf(money);
        int maxQuantity = (int) Math.min(Integer.MAX_VALUE, amountCoins / price);
        int buyQ = item.getAmount() > quantity ? quantity : item.getAmount();

        boolean enoughCoins = maxQuantity >= buyQ;
        if (!enoughCoins) {
            player.getPackets().sendGameMessage("You don't have enough " + (money == SLAYER ? "Slayer Points" : ItemConfig.forID(money).getName().toLowerCase()) + ".");
            buyQ = maxQuantity;
        } else if (quantity > buyQ)
            player.getPackets().sendGameMessage("The shop has run out of stock.");
        if (item.getDefinitions().isStackable()) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.getPackets().sendGameMessage("Not enough space in your inventory.");
                return;
            }
        } else {
            int freeSlots = player.getInventory().getFreeSlots();
            if (buyQ > freeSlots) {
                buyQ = freeSlots;
                player.getPackets().sendGameMessage("Not enough space in your inventory.");
            }
        }
        if (buyQ != 0) {
            int totalPrice = price * buyQ;
            if (money == SLAYER ? player.getSlayerManager().getPoints() - totalPrice >= 0 : money == PKP ? player.getPkPoints() - totalPrice >= 0 : player.getInventory().removeItemMoneyPouch(new Item(money, totalPrice))) {
                if (money == PKP)
                    player.setPkPoints(player.getPkPoints() - totalPrice);
                else if (money == SLAYER) {
                    player.getSlayerManager().setPoints(player.getSlayerManager().getPoints() - totalPrice);
                    player.getPackets().sendGameMessage("You currently have " + player.getSlayerManager().getPoints() + " slayer points.");
                }

                if (money != 995)
                    player.getCollectionLog().shopPurchase(item);

                player.getInventory().addItem(item.getId(), buyQ);
                item.setAmount(item.getAmount() - buyQ);
                if (item.getAmount() <= 0 && slotId >= mainStock.length)
                    generalStock[slotId - mainStock.length] = null;
                refreshShop();
                resetSelected(player);
                Bot.sendLog(Bot.SELL_BUY_CHANNEL, "[type=BUY][name=" + player.getUsername() + "][item=" + item.getName() + "(" + item.getId() + ")x" + Utils.getFormattedNumber(buyQ) + "][price=" + ItemConfig.forID(money).getName() + "(" + money + ")x" + Utils.getFormattedNumber(totalPrice) + "]");
            }
        }
    }

    public void restoreItems(boolean general) {
        Item[] stock = general ? generalStock : mainStock;
        int[] restoreQuantity = getPercentOfStock(stock, 0.05, general);//ten percent
        for (int idx = 0; idx < stock.length; idx++) {
            Item item = stock[idx];
            if (item == null || !general && item.getAmount() == defaultQuantity[idx])
                continue;
            boolean subtraction = general || stock[idx].getAmount() > defaultQuantity[idx];
            int quantity = (general ? 1 : restoreQuantity[idx]);
            item.setAmount(Math.max(0, (item.getAmount() + (subtraction ? -quantity : quantity))));
            if (subtraction) {
                if (item.getAmount() <= 0 && general)
                    stock[idx] = null;
            } else if (item.getAmount() > defaultQuantity[idx])
                item.setAmount(defaultQuantity[idx]);
        }
        refreshShop();
    }

    private int[] getPercentOfStock(Item[] stock, double percent, boolean general) {
        int[] percentArray = new int[stock.length];
        for (int idx = 0; idx < stock.length; idx++) {
            Item item = stock[idx];
            if (item == null)
                continue;
            percentArray[idx] = (int) Math.max(1, (general ? item.getAmount() : defaultQuantity[idx]) * percent);
        }
        return percentArray;
    }

    private boolean addItem(int itemId, int quantity) {
        for (Item item : mainStock) {
            if (item.getId() == itemId) {
                item.setAmount(item.getAmount() + quantity);
                refreshShop();
                return true;
            }
        }
        if (generalStock != null) {
            for (Item item : generalStock) {
                if (item == null)
                    continue;
                if (item.getId() == itemId) {
                    item.setAmount(item.getAmount() + quantity);
                    refreshShop();
                    return true;
                }
            }
            for (int i = 0; i < generalStock.length; i++) {
                if (generalStock[i] == null) {
                    generalStock[i] = new Item(itemId, quantity);
                    refreshShop();
                    return true;
                }
            }
        }
        return false;
    }

    public void sell(Player player, int slotId, int quantity) {
        if (player.getInventory().getItemsContainerSize() < slotId)
            return;
        Item item = player.getInventory().getItem(slotId);
        if (item == null)
            return;
        if (player.isBeginningAccount()) {
            player.getPackets().sendGameMessage("Starter accounts cannot sell to any shop for the first hour of playing time.");
            return;
        }

        int originalId = item.getId();
        if (item.getDefinitions().isNoted() && item.getDefinitions().getCertId() != -1)
            item = new Item(item.getDefinitions().getCertId(), item.getAmount());
        if ((!ItemConstants.isTradeable(item) && money != AURA) || money == Settings.VOTE_TOKENS_ITEM_ID || money == PKP || item.getId() == money
                || item.getDefinitions().isDungItem() || item.getDefinitions().isSCItem() || money == 25434 /*|| money == 43307*/
                || money == VOTE || money == DONATOR || money == SLAYER || money == EASTER_EGG
                || (money == BARROW && (item.getId() == COINS || item.getId() == 12183 || item.getId() == 43204))) {
            player.getPackets().sendGameMessage("You can't sell this item.");
            return;
        }
        int dq = getDefaultQuantity(item.getId());
        if (dq == -1 && generalStock == null) {
            player.getPackets().sendGameMessage("You can't sell this item to this shop.");
            return;
        }
        int price = getSellPrice(item);
        int numberOff = player.getInventory().getItems().getNumberOf(originalId);
        if (quantity > numberOff)
            quantity = numberOff;
        if (!addItem(item.getId(), quantity)) {
            player.getPackets().sendGameMessage("Shop is currently full.");
            return;
        }
        player.getInventory().deleteItem(originalId, quantity);
        refreshShop();
        resetSelected(player);
        if (price == 0)
            return;
        Item moneyI = new Item(money, price * quantity);
        player.getInventory().addItemMoneyPouch(moneyI);
        Bot.sendLog(Bot.SELL_BUY_CHANNEL, "[type=SELL][name=" + player.getUsername() + "][item=" + ItemConfig.forID(originalId).getName() + "(" + originalId + ")x" + Utils.getFormattedNumber(quantity) + "][price=" + moneyI.getName() + "(" + moneyI.getId() + ")x" + Utils.getFormattedNumber(moneyI.getAmount()) + "]");
    }

    public void sendValue(Player player, int slotId) {
        if (player.getInventory().getItemsContainerSize() < slotId)
            return;
        Item item = player.getInventory().getItem(slotId);
        if (item == null)
            return;
        if (item.getDefinitions().isNoted())
            item = new Item(item.getDefinitions().getCertId(), item.getAmount());
        if ((!ItemConstants.isTradeable(item) && money != AURA) || money == Settings.VOTE_TOKENS_ITEM_ID || money == PKP || item.getId() == money
                || item.getDefinitions().isDungItem() || item.getDefinitions().isSCItem() || money == 25434 || /*money == 43307 ||*/ money == VOTE || money == DONATOR || money == SLAYER || money == EASTER_EGG || (money == BARROW && (item.getId() == COINS || item.getId() == 12183 || item.getId() == 43204))) {
            player.getPackets().sendGameMessage("You can't sell this item.");
            return;
        }
        int dq = getDefaultQuantity(item.getId());
        if (dq == -1 && generalStock == null) {
            player.getPackets().sendGameMessage("You can't sell this item to this shop.");
            return;
        }
        int price = getSellPrice(item);
        player.getPackets().sendGameMessage(item.getDefinitions().getName() + ": shop will buy for: " + Utils.getFormattedNumber(price) + " " + ItemConfig.forID(money).getName().toLowerCase() + ". Right-click the item to sell.");
    }

    public int getDefaultQuantity(int itemId) {
        for (Item item : mainStock) {
            if (itemId == item.getId())
                return item.getAmount();
        }
        return -1;
    }

    public void resetSelected(Player player) {
        player.getTemporaryAttributtes().remove("ShopSelectedSlot");
        player.getVarsManager().sendVar(2563, -1);
    }

    public void sendInfo(Player player, int slotId, boolean inventory) {
        if (!inventory && slotId >= getStoreSize())
            return;
        Item item = inventory ? player.getInventory().getItem(slotId) : slotId >= mainStock.length ? generalStock[slotId - mainStock.length] : mainStock[slotId];
        if (item == null)
            return;
        if (item.getDefinitions().isNoted())
            item = new Item(item.getDefinitions().getCertId(), item.getAmount());
        if (inventory && ((!ItemConstants.isTradeable(item) && money != AURA) || money == Settings.VOTE_TOKENS_ITEM_ID || money == PKP || item.getId() == money
                || item.getDefinitions().isDungItem() || item.getDefinitions().isSCItem() || money == 25434/* || money == 43307*/ || money == VOTE || money == DONATOR)) {
            player.getPackets().sendGameMessage("You can't sell this item.");
            resetSelected(player);
            return;
        }


        resetTransaction(player);
        player.getTemporaryAttributtes().put("ShopSelectedSlot", slotId);
        player.getTemporaryAttributtes().put("ShopSelectedInventory", inventory);
        player.getVarsManager().sendVar(2561, inventory ? 93 : generalStock != null ? GENERAL_STOCK_ITEMS_KEY : MAIN_STOCK_ITEMS_KEY); // inv key
        player.getVarsManager().sendVar(2562, item.getId());
        player.getVarsManager().sendVar(2563, slotId);
        player.getPackets().sendCSVarString(362, ItemExamines.getExamine(item));
        player.getPackets().sendCSVarInteger(1876, getGearDescription(item));
        int price = inventory ? getSellPrice(item) : getBuyPrice(item);
        player.getPackets().sendGameMessage(item.getDefinitions().getName() + ": shop will " + (inventory ? "buy" : "sell") + " for: " + Utils.getFormattedNumber(price) + " " + (money == SLAYER ? "Slayer Points" : Utils.formatPlayerNameForDisplay(ItemConfig.forID(money).getName())));
    }

    private int getGearDescription(Item item) {
        ItemConfig config = item.getDefinitions();
        if (!config.isWearItem())
            return -1;
        if (config.getEquipSlot() == Equipment.SLOT_HAT)
            return 0;
        if (config.getEquipSlot() == Equipment.SLOT_CAPE)
            return 1;
        if (config.getEquipSlot() == Equipment.SLOT_AMULET)
            return 2;
        if (config.getEquipSlot() == Equipment.SLOT_CHEST)
            return 3;
        if (config.getEquipSlot() == Equipment.SLOT_LEGS)
            return 4;
        if (config.getEquipSlot() == Equipment.SLOT_HANDS)
            return config.getName().toLowerCase().contains("bracelet") ? 7 : 5;
        if (config.getEquipSlot() == Equipment.SLOT_FEET)
            return 6;
        if (config.getEquipSlot() == Equipment.SLOT_ARROWS)
            return 8;
        if (config.getEquipSlot() == Equipment.SLOT_SHIELD)
            return 9;
        if (config.getEquipSlot() == Equipment.SLOT_WEAPON)
            return WeaponTypesLoader.getWeaponDefinition(config.getId()).getType() == Combat.RANGE_TYPE ? (Equipment.isTwoHandedWeapon(item) ? 15 : 12) :
                    WeaponTypesLoader.getWeaponDefinition(config.getId()).getType() == Combat.MAGIC_TYPE ? 13 : (Equipment.isTwoHandedWeapon(item) ? 14 : 11);
        return 10;
		/*
		10 nothing
		11 melee weap
		12 range weap
		13 mage weap
		14 melee 2h weap
		15 range 2h weap*/
    }

    public int getBuyPrice(Item item) {
        //fixes getting noted items price
        if (item.getDefinitions().isNoted() && item.getDefinitions().getCertId() != -1)
            item = new Item(item.getDefinitions().getCertId(), item.getAmount());
        switch (money) {
            case Settings.VOTE_TOKENS_ITEM_ID:
                if (item.getId() < 0 || item.getId() >= Settings.VOTE_SHOP_ITEM_PRICES.length)
                    return 1;
                return Settings.VOTE_SHOP_ITEM_PRICES[item.getId()];
            case PKP:
                return Settings.PKP_SHOP_ITEM_PRICES[item.getId()];
            case 24444: // TROHPY
                if (item.getId() >= 24450 && item.getId() <= 24454)
                    return 30 + (item.getId() - 24450) * 5;
                if (item.getId() >= 24455 && item.getId() <= 24457)
                    return 1500;
                break;
            case BARROW:
                return getBarrowsPrice(item);
            case BLOOD:
                return getBloodPrice(item);
            case VOTE:
                return getVotePrice(item);
            case DONATOR:
                return getDonatorPrice(item);
            case MOLCH_PEARL:
                return getMolchPearlPrice(item);
            case SLAYER:
                return getSlayerPrice(item);
            case LOYAL:
                return getLoyalPrice(item);
            case EASTER_EGG:
                return getEasterPrice(item);
            default:
                if (key == 914)
                    return getSecondariesPrice(item) * 3;
                if (money == COINS && key >= 1200 && ItemConstants.isTradeable(item)) {
                    int price = (int) (GrandExchange.getPrice(item.getId()) * 1.4);
                    return price == 0 ? 1 : price;
                }
                int price = ClientScriptMap.getMap(731).getIntValue(item.getId());
                if (money == TOKKUL && price > 0)
                    return price;
                price = ClientScriptMap.getMap(733).getIntValue(item.getId());
                if (price > 0)
                    return price;
                if (item.getDefinitions().hasShopPriceAttributes())
                    return 99000;
                price = item.getDefinitions().getValue();
                if (money == TOKKUL) //50% more
                    price = (price * 3) / 2;
                return Math.max(price, 1);

        }
        return 1;
    }

    private int getSlayerPrice(Item item) {
        switch (item.getId()) {
            case 6:
                return 200;
            case 8:
                return 200;
            case 10:
                return 200;
            case 12:
                return 200;
            case 2:
                return 2;
            case 989:
                return 100;
            case 25763:
                return 250;
            case 15488:
                return 300;
            case 15490:
                return 300;
            case 18337:
                return 450;
            case 19675:
                return 500;
            case 10551:
                return 1500;
            case 15017:
                return 150;
            case 4151:
                return 250;
            case 11716:
                return 4500;
            case 51902:
                return 2000;
            case 24388:
                return 1000;
            case 24382:
                return 1000;
            case 24379:
                return 1000;
            case 6914:
                return 375;
            case 6889:
                return 750;
            case 6918:
                return 250;
            case 6916:
                return 250;
            case 6924:
                return 250;
            case 6922:
                return 250;
            case 6920:
                return 1500;
            case 2577:
                return 3000;
            case 11732:
                return 200;
            case 8921:
                return 200;
            case 25500:
                return 8000;
            default:
                return Integer.MAX_VALUE;
        }
    }

    private int getDonatorPrice(Item item) {
        int price = getDollarPrice(item) * 200;
        if (price >= 150 * 200)
            price *= 1.2;
        else if (price >= 100 * 200)
            price *= 1.15;
        else if (price >= 50 * 200)
            price *= 1.1;
        else if (price >= 25 * 200)
            price *= 1.05;
        return price;
    }


    /**
     * Shard dollar values
     */
    public static int getDollarPrice(Item item) {
        switch (item.getId()) {
            case 25763: return 3;
            case 25526:
                return 80;
            case 25504:
                return 150;
            case 25588:
                return 150;
            case 52326:
                return 30;
            case 52327:
            case 52328:
                return 45;
            case 52322:
                return 45;
            case 25425:
                return 15;
            case 25426:
                return 40;
            case 25427:
                return 90;
            case 25428:
                return 200;
            case 25429:
                return 499;
            case 25493:
                return 750;
            case 25494:
                return 250;
            case 25699:
                return 450;
            case 25437:
                return 25;
            case 25438:
                return 50;
            case 25439:
                return 110;
            case 25440:
                return 300;
            case 25503:
                return 4;
            case 6199:
                return 6;
            case 25436:
                return 8;
            case 27004:
                return 20;
            case 25453:
                return 10;
            case 22298:
                return 4;
            case 22300:
                return 4;
            case 23876:
                return 6;
            case 23854:
                return 6;
            case 23874:
                return 6;
            case 25477:
                return 45;
            case 23866:
                return 10;
            case 4151:
                return 4;
            case 21371:
                return 20;
            case 43576:
                return 40;
            case 14484:
                return 15;
            case 18349:
                return 40;
            case 18351:
                return 40;
            case 18353:
                return 40;
            case 42924:
                return 30;
            case 18357:
                return 40;
            case 15486:
                return 5;
            case 18355:
                return 40;
            case 21777:
                return 60;
            case 50997:
                return 300;
            case 25460:
                return 300;
            case 25469:
                return 300;
            case 25476:
                return 380;
            case 51006:
                return 50;
            case 52325:
                return 205;
            case 42929:
                return 10;
            case 10551:
                return 5;
            case 11665:
                return 5;
            case 11664:
                return 5;
            case 11663:
                return 5;
            case 19785:
                return 10;
            case 19786:
                return 8;
            case 8842:
                return 3;
            case 4716:
                return 2;
            case 4720:
                return 4;
            case 4722:
                return 3;
            case 4718:
                return 5;
            case 4708:
                return 2;
            case 4712:
                return 4;
            case 4714:
                return 3;
            case 4710:
                return 5;
            case 4732:
                return 2;
            case 4736:
                return 4;
            case 4738:
                return 3;
            case 4734:
                return 5;
            case 4724:
                return 2;
            case 4728:
                return 4;
            case 4730:
                return 3;
            case 4726:
                return 5;
            case 22358:
                return 7;
            case 22363:
                return 7;
            case 22369:
                return 7;
            case 11724:
                return 20;
            case 11726:
                return 25;
            case 11718:
                return 15;
            case 11720:
                return 20;
            case 11722:
                return 20;
            case 24992:
                return 15;
            case 24995:
                return 20;
            case 24998:
                return 20;
            case 21472:
                return 76;
            case 21473:
                return 114;
            case 21474:
                return 95;
            case 21475:
                return 20;
            case 21476:
                return 20;
            case 21467:
                return 76;
            case 21468:
                return 114;
            case 21469:
                return 95;
            case 21470:
                return 20;
            case 21471:
                return 20;
            case 21462:
                return 76;
            case 21463:
                return 114;
            case 21464:
                return 95;
            case 21465:
                return 20;
            case 21466:
                return 20;
            case 51018:
                return 50;
            case 51021:
                return 50;
            case 51024:
                return 50;
            case 43237:
                return 30;
            case 43235:
                return 20;
            case 43239:
                return 25;
            case 20072:
                return 5;
            case 11283:
                return 15;
            case 51000:
                return 12;
            case 13744:
                return 35;
            case 13738:
                return 45;
            case 13742:
                return 55;
            case 13740:
                return 70;
            case 25495:
                return 150;
            case 6585:
                return 5;
            case 42002:
                return 4;
            case 25484:
                return 20;
            case 49553:
                return 25;
            case 49547:
                return 30;
            case 6570:
                return 5;
            case 25378:
                return 25;
            case 51295:
                return 50;
            case 52109:
                return 5;
            case 51795:
                return 10;
            case 15220:
                return 7;
            case 15018:
                return 7;
            case 15019:
                return 7;
            case 42785:
                return 10;
            case 25470:
                return 35;
            case 25486:
                return 115;
            case 25488:
                return 90;
            case 11858:
                return 50;
            case 11860:
                return 50;
            case 11862:
                return 50;
            case 1050:
                return 40;
            case 43343:
                return 150;
            case 1057:
                return 50;
            case 1053:
                return 50;
            case 1055:
                return 50;
            case 1042:
                return 100;
            case 1044:
                return 100;
            case 1038:
                return 100;
            case 1046:
                return 100;
            case 1040:
                return 100;
            case 1048:
                return 100;
            case 41862:
                return 250;
            case 42649:
                return 40;
            case 42650:
                return 40;
            case 42651:
                return 40;
            case 42652:
                return 40;
            case 25446:
                return 60;
            case 25444:
                return 80;
            case 25467:
                return 100;
            case 25523:
                return 50;
            case 25574:
                return 4;
        }
        return item.getDefinitions().getValue();
    }

    public int getSellPrice(Item item) {
        //fixes getting noted items price
        if (item.getDefinitions().isNoted() && item.getDefinitions().getCertId() != -1)
            item = new Item(item.getDefinitions().getCertId(), item.getAmount());
        int price = ClientScriptMap.getMap(732).getIntValue(item.getId());
        if (money == TOKKUL && price > 0)
            return price;
        price = ClientScriptMap.getMap(1441).getIntValue(item.getId());
        if (price > 0)
            return price;
        if (money == TOKKUL)
            return (int) (item.getDefinitions().getValue() * 0.1);
        if (money == AURA)
            return (int) (item.getDefinitions().getValue() * 0.2);
        if (money == BARROW)
            return (int) (getBarrowsPrice(item) * 0.5);
        if (money == BLOOD)
            return (int) (getBloodPrice(item) * 0.1);
        if (money == VOTE)
            return (int) (getVotePrice(item) * 0.5);
        if (money == DONATOR)
            return (int) (getDonatorPrice(item) * 0.5);
        if (money == SLAYER)
            return (int) (getSlayerPrice(item) * 0.5);
        if (money == EASTER_EGG)
            return (int) (getEasterPrice(item) * 0.5);
        if (money == MOLCH_PEARL)
            return (int) (getMolchPearlPrice(item) * 0.1);
        return Math.max(1, money != COINS ? ((item.getDefinitions().getValue() * 30) / 100) : ItemConstants.getHighAlchValue(item)); //30%
    }

    private int getEasterPrice(Item item) {
        switch (item.getId()) {
            case 25435:
                return 1;
            case 23713:
            case 989:
                return 3;
            case 24149:
            case 24150:
                return 5;
            case 11021:
            case 11020:
            case 11022:
            case 11019:
                return 10;
            case 1037:
                return 50;
            case 53448:
                return 75;
            case 43663:
            case 43664:
                return 20;
            case 4566:
                return 30;
            case 14728:
            case 24145:
                return 40;
            case 4084:
            case 7927:
                return 100;
        }
        return Integer.MAX_VALUE;
    }

    private int getSecondariesPrice(Item item) {
        switch (item.getId()) {
            case 221:
                return 500;
            case 235:
                return 750;
            case 225:
                return 1000;
            case 1939:
                return 1500;
            case 223:
                return 2000;
            case 1975:
                return 2500;
            case 239:
                return 3000;
            case 231:
                return 4000;
            case 2970:
                return 5000;
            case 241:
                return 6000;
            case 2859:
                return 1000;
            case 2138:
                return 1250;
            case 6291:
                return 1500;
            case 440:
                return 1750;
            case 10033:
                return 2000;
            case 6010:
                return 2250;
            case 1635:
                return 2500;
            case 2132:
                return 2750;
            case 9978:
                return 3000;
            case 1937:
                return 3250;
            case 9736:
                return 3500;
            case 6287:
                return 3750;
            case 8431:
                return 4000;
            case 2150:
                return 4250;
            case 7939:
                return 4500;
            case 1933:
                return 4750;
            case 10117:
                return 5000;
            case 1963:
                return 5250;
            case 2462:
                return 5500;
            case 1442:
                return 5750;
            case 10149:
                return 6000;
            case 237:
                return 7000;
            case 10818:
                return 8000;
            case 1115:
                return 9000;
            case 1119:
                return 10000;
        }
        return item.getDefinitions().getValue();
    }

    private int getMolchPearlPrice(Item item) {
        switch (item.getId()) {
            case 52838:
                return 1000;
            case 52840:
                return 2000;
            case 24427:
                return 100;
            case 24428:
                return 100;
            case 24429:
                return 100;
            case 24430:
                return 100;
        }
        return item.getDefinitions().getValue();
    }

    private int getVotePrice(Item item) {
        switch (item.getId()) {
            case 27004:
                return 500;
            case 18338:
                return 50;
            case 18339:
                return 30;
            case 25435:
                return 1;
            case 989:
                return 1;
            case 25763: return 3;
            case 15441:
                return 10;
            case 15701:
                return 15;
            case 22209:
                return 120;
            case 10551:
                return 50;
            case 8850:
                return 30;
            case 42791:
                return 35;
            case 41941:
                return 10;
            case 6:
                return 5;
            case 8:
                return 5;
            case 10:
                return 5;
            case 12:
                return 5;
            case 6739:
                return 75;
            case 15259:
                return 75;
            case 51028:
                return 75;
            case 4084:
                return 250;
            case 22216:
                return 50;
            case 24433:
                return 100;
            case 42389:
                return 50;
            case 3486:
                return 50;
            case 3481:
                return 50;
            case 3483:
                return 50;
            case 3488:
                return 50;
            case 42391:
                return 50;
            case 42785:
                return 50;
            case 6199:
                return 125;
            case 25453:
                return 250;
            case 25432:
                return 250;
            case 49730:
                return 1000;

        }
        return item.getDefinitions().getValue();
    }

    private int getLoyalPrice(Item item) {
        switch (item.getId()) {
            case 24154:
                return 5;
            case 24108:
                return 50;
            case 24110:
                return 50;
            case 24112:
                return 50;
            case 24114:
                return 50;
            case 21258:
                return 75;
            case 21260:
                return 75;
            case 21262:
                return 75;
            case 21264:
                return 75;
            case 7003:
                return 10;
            case 19747:
                return 25;
            case 43344:
                return 100;
            case 42399:
                return 250;
            case 24317:
                return 65;
            case 51314:
                return 40;
            case 7534:
                return 30;
            case 7535:
                return 30;
            case 7537:
                return 30;
            case 24431:
                return 50;
            case 6335:
                return 20;
            case 6337:
                return 20;
            case 6339:
                return 20;
            case 6341:
                return 20;
            case 6343:
                return 20;
            case 6345:
                return 20;
            case 6347:
                return 20;
            case 6349:
                return 20;
            case 6392:
                return 25;
            case 6394:
                return 25;
            case 6396:
                return 25;
            case 6398:
                return 25;
            case 50838:
                return 50;
            case 50840:
                return 50;
            case 50842:
                return 50;
            case 50846:
                return 50;
            case 2643:
                return 100;
            case 10400:
                return 100;
            case 10402:
                return 100;
            case 42457:
                return 150;
            case 42458:
                return 150;
            case 42459:
                return 150;
            case 13672:
                return 250;
            case 13673:
                return 250;
            case 13674:
                return 250;
            case 13675:
                return 250;
            case 7671:
                return 500;
            case 7673:
                return 500;
            case 7927:
                return 750;
            case 6583:
                return 750;
            case 747:
                return 150;
            case 24418:
                return 50;
            case 24419:
                return 50;
            case 5608:
                return 150;
            case 5609:
                return 150;
            case 5607:
                return 150;
            case 4566:
                return 250;
            case 50368:
                return 400;
            case 50370:
                return 400;
            case 50372:
                return 400;
            case 50374:
                return 400;
            case 20821:
                return 1000;
            case 50590:
                return 750;
            case 20765:
                return 100;
            case 20766:
                return 50;
            case 20763:
                return 500;
            case 20764:
                return 150;
            case 24709:
                return 1000;
            case 24710:
                return 250;
            case 4084:
                return 2000;
            case 23030:
                return 1000;

        }
        return item.getDefinitions().getValue();
    }


    private int getBloodPrice(Item item) {
        switch (item.getId()) {
            case 25433:
                return 300;
            case 20800:
                return 500000;
            case 20799:
                return 200000;
            case 20798:
                return 100000;
            case 20797:
                return 50000;
            case 20796:
                return 10000;
            case 20795:
                return 1000;
            case 24455:
                return 3000000;
            case 24456:
                return 3000000;
            case 24457:
                return 3000000;
            case 25470:
                return 1000000;
            case 25527:
                return 500000;
            case 52545:
                return 100000;
            case 52550:
                return 100000;
            case 52555:
                return 100000;
            case 42006:
                return 50000;
            case 42808:
                return 50000;
            case 42788:
                return 2500;
            case 41924:
                return 50000;
            case 41926:
                return 50000;
            case 24454:
                return 100000;
            case 13899:
                return 100000;
            case 13905:
                return 100000;
            case 13887:
                return 100000;
            case 13893:
                return 100000;
            case 13902:
                return 100000;
            case 13896:
                return 100000;
            case 13884:
                return 125000;
            case 13890:
                return 125000;
            case 13879:
                return 1000;
            case 13883:
                return 1000;
            case 13876:
                return 50000;
            case 13870:
                return 75000;
            case 13873:
                return 75000;
            case 13867:
                return 50000;
            case 13864:
                return 50000;
            case 13858:
                return 75000;
            case 13861:
                return 75000;
            case 49707:
                return 20000;
            case 52557:
                return 25000;
            case 42785:
                return 25000;
            case 42791:
                return 10000;

            case 13908:
                return 12500;
            case 13911:
                return 10000;
            case 13914:
                return 12500;
            case 13917:
                return 10000;
            case 13920:
                return 10000;
            case 13923:
                return 10000;
            case 13926:
                return 10000;
            case 13929:
                return 10000;
            case 13932:
                return 7500;
            case 13935:
                return 7500;
            case 13938:
                return 5000;
            case 13941:
                return 5000;
            case 13944:
                return 7500;
            case 13947:
                return 7500;
            case 13950:
                return 5000;
        }
        return item.getDefinitions().getValue();
    }


    public static int getBarrowsPrice(Item item) {
        switch (item.getId()) {
            case 4708:
            case 4716:
            case 4724:
            case 4732:
            case 4745:
            case 4753:
            case 21736:
                return 3000000; //helm
            case 4710:
            case 4718:
            case 4726:
            case 4734:
            case 4747:
            case 4755:
            case 21744:
                return 6000000; //weapon
            case 4712:
            case 4720:
            case 4728:
            case 4736:
            case 4749:
            case 4757:
            case 21752:
                return 5000000; //body
            case 4714:
            case 4722:
            case 4730:
            case 4738:
            case 4751:
            case 4759:
            case 21760:
                return 4000000; //legs
            //lucky
            case 25385:
            case 25389:
            case 25393:
            case 25397:
            case 25401:
            case 25405:
            case 25409:
                return 4500000; //helm
            case 25386:
            case 25390:
            case 25394:
            case 25398:
            case 25402:
            case 25406:
            case 25410:
                return 9000000; //weapon
            case 25387:
            case 25391:
            case 25395:
            case 25399:
            case 25403:
            case 25407:
            case 25411:
                return 7500000; //body
            case 25388:
            case 25392:
            case 25396:
            case 25400:
            case 25404:
            case 25408:
            case 25412:
                return 6000000; //legs
        }
        return item.getDefinitions().getValue();

    }

    public void sendExamine(Player player, int slotId) {
        if (slotId >= getStoreSize())
            return;
        Item item = slotId >= mainStock.length ? generalStock[slotId - mainStock.length] : mainStock[slotId];
        if (item == null)
            return;
        player.getPackets().sendGameMessage(ItemExamines.getExamine(item));
    }

    public void refreshShop() {
        for (Player player : viewingPlayers) { //prevent glitching
            Shop shop = (Shop) player.getTemporaryAttributtes().get("Shop");
            if (shop != this) {
                player.stopAll();
                removePlayer(player);
                continue;
            }
            //	player.getPackets().sendCSVarString(336, customcs2configstring(player)); // makes my hand crafted cs2 to set prices work
            sendStore(player);
            player.getPackets().sendIComponentSettings(620, 25, 0, getStoreSize() * 6, 1150);
        }
    }

    private String customcs2configstring(Player player) {
        String str = "";

        for (int i = 0; i < player.getInventory().getItemsContainerSize(); i++) {
            Item item = player.getInventory().getItem(i);
            if (item == null) {
                String id = Integer.toString(-1, 16);
                while (id.length() < 8)
                    id = "0" + id;
                String price = Integer.toString(0, 16);
                while (price.length() < 8)
                    price = "0" + price;

                str += id + price;
                continue;
            }

            String id = Integer.toString(item.getId(), 16);
            while (id.length() < 8)
                id = "0" + id;
            String price = Integer.toString(getSellPrice(item), 16);
            while (price.length() < 8)
                price = "0" + price;

            str += id + price;
        }

        int written = 28;
        for (int i = 0; i < mainStock.length; i++) {
            if (written >= 100)
                break;

            Item item = mainStock[i];
            if (item == null)
                continue;

            String id = Integer.toString(item.getId(), 16);
            while (id.length() < 8)
                id = "0" + id;
            String price = Integer.toString(getBuyPrice(item), 16);
            while (price.length() < 8)
                price = "0" + price;

            str += id + price;
            written++;
        }

        if (generalStock != null) {
            for (int i = 0; i < generalStock.length; i++) {
                if (written >= 100)
                    break;

                Item item = generalStock[i];
                if (item == null)
                    continue;

                String id = Integer.toString(item.getId(), 16);
                while (id.length() < 8)
                    id = "0" + id;
                String price = Integer.toString(item.getId(), 16);
                while (price.length() < 8)
                    price = "0" + price;

                str += id + price;
                written++;
            }
        }

        return str.toUpperCase();


    }

    public int getStoreSize() {
        return mainStock.length + (generalStock != null ? generalStock.length : 0);
    }

    public void sendInventory(Player player) {
        player.getInterfaceManager().sendInventoryInterface(1266);
        player.getPackets().sendItems(93, player.getInventory().getItems());
        player.getPackets().sendUnlockIComponentOptionSlots(1266, 0, 0, 27, 0, 1, 2, 3, 4, 5);
        player.getPackets().sendInterSetItemsOptionsScript(1266, 0, 93, 4, 7, "Value", "Sell 1", "Sell 5", "Sell 10", "Sell 50", "Examine");
    }


    public void sendStore(Player player) {
        Item[] stock = new Item[mainStock.length + (generalStock != null ? generalStock.length : 0)];
        System.arraycopy(mainStock, 0, stock, 0, mainStock.length);
        if (generalStock != null)
            System.arraycopy(generalStock, 0, stock, mainStock.length, generalStock.length);
        player.getPackets().sendItems(generalStock != null ? GENERAL_STOCK_ITEMS_KEY : MAIN_STOCK_ITEMS_KEY, stock);
    }

}