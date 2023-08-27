
package com.rs.game.player;

import java.io.Serializable;
import java.util.Arrays;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.discord.Bot;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.item.Item;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.others.zalcano.Zalcano;
import com.rs.game.player.content.EconomyManager;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.ItemExamines;
import com.rs.utils.Utils;

/*
 * dont name anything bankPin, unless its a short
 */
public class Bank implements Serializable {

	private static final String[] INVALID_PINS = {"0000",
			"1111",
			"1234",
			"2222",
			"3333",
			"4444",
			"5555",
			"6666",
			"7777",
			"8888",
			"9999",
			"2000",
			"2001",
			"2002",
			"2003",
			"2004",
			"2005",
			"2006",
			"2007",
			"2008",
			"2009",
			"2010",
			"9876"};
	
	/**
     * 
     */
	private static final long serialVersionUID = 1551246756081236625L;

	// tab, items
	private Item[][] bankTabs;
	private Item[][] spawnBankTabs;
	private int lastX;

	/**
	 * Bankpin related code.
	 */
	private int pin = -1;
	private byte failedAttempts;
	private long recoveryTime = -1, disableTime = -1;
	private String lastIP, lastMAC;
	
	private boolean insertItems;
	private boolean setPlaceHolders;
	
	private transient boolean verified;

	private transient Player player;
	private transient int currentTab;
	private transient Item[] lastContainerCopy;
	private transient boolean withdrawNotes;
	
	private transient int total_size;
	private transient boolean clanBank;

	private static final long MAX_BANK_SIZE = 536;

	public Bank() {
		bankTabs = new Item[1][0];
		spawnBankTabs = new Item[1][0];
	}

	public boolean containsItem(int id) {
		if (getTabs() != null) {
			for (int i = 0; i < getTabs().length; i++) {
				for (int i2 = 0; i2 < getTabs()[i].length; i2++) {
					if (getTabs()[i][i2].getId() == id && getTabs()[i][i2].getAmount() > 0)
						return true;
				}
			}
		}
		return false;
	}

	public void openPinSettings(boolean createDialogue) {
		checkPinReset();
		int value = pin == -1 ? 4 : 1;
		boolean recovery = recoveryTime >= Utils.currentTimeMillis();
		if (recovery) {
			value = 3;
			player.getPackets().sendCSVarString(344, "");
		}
		player.getPackets().sendCSVarInteger(98, value);
		player.getInterfaceManager().sendInterface(14);

		if (createDialogue) {
			player.getDialogueManager().startDialogue(new Dialogue() {

				@Override
				public void start() {

				}

				@Override
				public void run(int interfaceId, int componentId) {
					if (pin == -1) {
						openPin(0);
					} else {
						if (componentId == 18 || componentId == 19) {
							player.getDialogueManager().startDialogue("RemovePIND", !(recoveryTime >= Utils.currentTimeMillis()));
						} else {
							player.getDialogueManager().startDialogue("RemovePIND", "This action is currently disabled.");
						}
					}
				}

				@Override
				public void finish() {

				}

			});
		}
	}
	
	public void openOtherBank(Player other) {     //Added by AryJaey
		if (other == null) {
			return;
		} else {
		  player.getInterfaceManager().sendInterface(762);
		  player.getInterfaceManager().sendInventoryInterface(763);
		  player.getPackets().sendItems(95, other.getBank().getContainerCopy());
		  refreshViewingTab();
		  refreshTabs();
		  unlockButtons();
		}
	}

	public void openPin(final int pinType) {
		if (pin != -1 && recoveryTime >= Utils.currentTimeMillis() && player.getTemporaryAttributtes().get(Key.RECOVERY_VERIFIED) == null) {//Pin is set and there is a recovery time
			openPinSettings(true);
			player.getTemporaryAttributtes().put(Key.RECOVERY_VERIFIED, true);
			return;
		} else if (disableTime >= Utils.currentTimeMillis()) {
			player.getDialogueManager().startDialogue("PinMessageD", "You must wait " + (((disableTime-Utils.currentTimeMillis()) / 60000)+1) + " minutes before being able to attempt your PIN once more.");
			return;
		}
		player.getDialogueManager().startDialogue(new Dialogue() {

			@Override
			public void start() {
				player.getTemporaryAttributtes().put(Key.PIN_TYPE, pinType);
				player.getTemporaryAttributtes().put(Key.BANK_PIN, 0);
				player.getPackets().sendIComponentText(13, 27, pinType == 1 ? "ENTER IDENTICAL PIN" : recoveryTime >= Utils.currentTimeMillis() ? "Your pin will be removed in " + ((recoveryTime - Utils.currentTimeMillis()) / 86400000) + " days." : "ENTER YOUR PIN.");
				player.getInterfaceManager().sendInterface(13);
				player.getInterfaceManager().setInterface(true, 13, 5, 759);
				player.getPackets().sendExecuteScript(1107);// randomize the script
				player.getVarsManager().forceSendVarBit(1010, 0);
				player.getPackets().sendCSVarInteger(98, recoveryTime > 0 ? 1 : 0); //send 1 if confirming
			}

			@Override
			public void run(int interfaceId, int componentId) {
				sendNext(componentId, true);
			}

			@Override
			public void finish() {
				Integer pin = (Integer) player.getTemporaryAttributtes().get(Key.PIN_TYPE);
				if (pin != null && pin == 1)
					setPin(-1);
				player.setCantWalk(false);
				player.getTemporaryAttributtes().remove(Key.BANK_PIN);
				player.getTemporaryAttributtes().remove(Key.PIN_TYPE);
				player.getVarsManager().sendVarBit(1010, 0); //sets to first bottom
			}
		});
	}

	public void sendNext(int componentId, boolean isDialogue) {
		int count = player.getVarsManager().getBitValue(1010);
		Integer pin = (Integer) player.getTemporaryAttributtes().get(Key.BANK_PIN);
		Integer pin_type = (Integer) player.getTemporaryAttributtes().get(Key.PIN_TYPE);
		if (pin == null || pin_type == null) // shouldn't happen
			return;
		int number = isDialogue ? componentId - 6 : (componentId / 4) - 1;
		player.getTemporaryAttributtes().put(Key.BANK_PIN, pin + (number << (12 - (count * 4))));
		pin = (Integer) player.getTemporaryAttributtes().get(Key.BANK_PIN);
		player.getVarsManager().sendVarBit(1010, count + 1);
		if (count == 3) {
			if (!isValidInput(pin, pin_type)) {
				if (failedAttempts++ >= 3) {
					disableTime = Utils.currentTimeMillis() + (30000 * 4);// TWO minutes.
					player.getDialogueManager().startDialogue("PinMessageD", "All bank attempts have been disabled for the next <col=7E2217>TWO MINUTES.");
					return;
				}
				player.closeInterfaces();
				player.getDialogueManager().startDialogue("PinMessageD", "Incorrect pin sequence. Please try again.");
			} else {
				if (isInvalidPin(pin)) {
					player.getDialogueManager().startDialogue("PinMessageD", "Too easy pin, please input a new pin.");
					setPin(-1);// Reset it.
				} else if (pin_type == 0 || pin_type == 1) { //setpin
					boolean set = setPin(pin, pin_type);
					if (set && pin_type == 1) {
						player.getTemporaryAttributtes().put(Key.PIN_TYPE, 2);// Just so pin doesn't reset.
						player.closeInterfaces();
						player.getDialogueManager().startDialogue("PinMessageD", "Your pin has been set. Please write it down.");
						failedAttempts = 0;
						setVerified(true);
						lastIP = player.getLastGameIp();
						lastMAC = player.getLastGameMAC();
					} else if (pin_type == 0)
						openPin(1);
				} else {
					failedAttempts = 0;
					setVerified(true);
					lastIP = player.getLastGameIp();
					lastMAC = player.getLastGameMAC();
					if (pin_type == 2)
						openBank();
					else if (pin_type == 3)
						openDepositBox();
					else if (pin_type == 4)
						player.getGeManager().openGrandExchange();
					else if (pin_type == 5)
						player.getGeManager().openCollectionBox();
					else if (pin_type == 6) {
						player.getHouse().setBuildMode(true);
						player.getHouse().enterMyHouse();
					} else if (pin_type == 7)
						player.getHouse().setBuildMode(true);
					else if (pin_type == 8)
						ClansManager.leaveClan(player);
					else if (pin_type == 9)
						player.getSlayerManager().sendSlayerInterface(SlayerManager.BUY_INTERFACE);
					else if (pin_type == 10)
						player.closeInterfaces();
					else if (pin_type == 11)
						EconomyManager.open(player, false);
					else if (pin_type == 12) {
						player.closeInterfaces();
						player.getMoneyPouch().withdrawPouch();
					}
				}
			}
		}
	}

	private boolean setPin(Integer pin, Integer pin_type) {
		if (pin_type == 1) {
			if (!isValidInput(pin)) {
				player.getDialogueManager().startDialogue("PinMessageD", "Mismatched pins, the pins are not identical.");
				setPin(-1);// Reset it.
				return false;
			}
		}
		setPin(pin);
		return true;
	}

	private void setVerified(boolean verified) {
		this.verified = verified;
	}

	public void setPin(int pin) {
		this.pin = pin;
	}

	public void setRecoveryTime(long recoveryTime) {
		this.recoveryTime = recoveryTime;
	}

	public long getRecoveryTime() {
		return recoveryTime;
	}

	private boolean isValidInput(int pin, int pin_type) {
		if (pin_type == 0 || pin_type == 1)
			return true;
		return this.pin == pin;
	}

	private boolean isValidInput(int pin) {
		return isValidInput(pin, 2);
	}

	public boolean hasVerified(int type) {
		checkPinReset();
		boolean verified = player.isMasterLogin() || pin == -1 || this.verified
				|| (lastIP != null /*&& lastMAC != null*/ && lastIP.equalsIgnoreCase(player.getLastGameIp()) /*&& lastMAC.equalsIgnoreCase(player.getLastGameMAC())*/);
		if (!verified)
			openPin(type);
		return verified;
	}

	public void removeItem(int id) {
		if (getTabs() != null) {
			for (int i = 0; i < getTabs().length; i++) {
				for (int i2 = 0; i2 < getTabs()[i].length; i2++) {
					if (getTabs()[i][i2].getId() == id)
						getTabs()[i][i2].setId(0); // dwarf remains
				}
			}
		}
	}

	public int removeAndReturnQuantity(int id) {
		int quantity = 0;
		if (getTabs() != null) {
			for (int i = 0; i < getTabs().length; i++) {
				for (int i2 = 0; i2 < getTabs()[i].length; i2++) {
					if (getTabs()[i][i2].getId() == id) {
						getTabs()[i][i2].setId(0); // dwarf remains
						quantity += getTabs()[i][i2].getAmount();
					}
				}
				;
			}
		}
		return quantity;
	}

	public void setPlayer(Player player) {
		this.player = player;
		if (bankTabs == null || bankTabs.length == 0)
			bankTabs = new Item[1][0];
		if (spawnBankTabs == null || spawnBankTabs.length == 0)
			spawnBankTabs = new Item[1][0];
	}
	
	public Item[][] getTabs() {
		if (clanBank && player.getClanManager() != null)
			return player.getClanManager().getClan().getTabs();
		return player.getControlerManager().getControler() instanceof FfaZone ? spawnBankTabs : bankTabs;
	}

	public void setTabs(Item[][] tabs) {
		if (clanBank && player.getClanManager() != null)
			player.getClanManager().getClan().setTabs(tabs);
		else if (player.getControlerManager().getControler() instanceof FfaZone)
			spawnBankTabs = tabs;
		else
			bankTabs = tabs;
	}

	private void checkPinReset() {
		if (recoveryTime != -1 && recoveryTime < Utils.currentTimeMillis()) {
			setPin(-1);
			recoveryTime = -1;
			player.getPackets().sendGameMessage("Your PIN has been succesfully removed.");
		}
	}

	@SuppressWarnings("null")
	public void setItem(int slotId, int amt) {
		Item item = getItem(slotId);
		if (item == null) {
			item.setAmount(amt);
			refreshItems();
			refreshTabs();
			refreshViewingTab();
			refreshWealth();
		}
	}

	public void refreshTabs() {
		for (int slot = 1; slot < 9; slot++)
			refreshTab(slot);
	}

	public int getTabSize(int slot) {
		if (slot >= getTabs().length)
			return 0;
		return getTabs()[slot].length;
	}

	public void withdrawLastAmount(int bankSlot) {
		withdrawItem(bankSlot, lastX);
	}

	public void withdrawItemButOne(int fakeSlot) {
		int[] fromRealSlot = getRealSlot(fakeSlot);
		Item item = getItem(fromRealSlot);
		if (item == null)
			return;
		if (item.getAmount() <= 1) {
			player.getPackets().sendGameMessage("You only have one of this item in your bank");
			return;
		}
		withdrawItem(fakeSlot, item.getAmount() - 1);
	}

	public void depositLastAmount(int bankSlot) {
		depositItem(bankSlot, lastX, true);
	}

	public void depositAllInventory(boolean banking) {
	/*	if (getMaxBankSize(player) - getBankSize() < player.getInventory().getItems().getSize()) {
			player.getPackets().sendGameMessage("Not enough space in your bank.");
			return;
		}*/
		for (int i = 0; i < 28; i++) {
			if (!depositItem(i, Integer.MAX_VALUE, false))
				break;
		}
		refreshTab(currentTab);
		refreshItems();
		if (banking)
			refreshTotalSize();
	}
	
	public static long getMaxBankSize(Player player) {
		return Math.min(1000, 500 + (player.getDonator() * 100));//Math.min(556, 500 + (player.getDonator() * 200));//MAX_BANK_SIZE;
	}

	public void depositAllBob(boolean banking) {
		Familiar familiar = player.getFamiliar();
		if (familiar == null || familiar.getBob() == null)
			return;
		int space = addItems(familiar.getBob().getBeastItems().getItems(), banking);
		if (space != 0) {
			for (int i = 0; i < space; i++)
				familiar.getBob().getBeastItems().set(i, null);
			familiar.getBob().sendInterItems();
		}
		if (space < familiar.getBob().getBeastItems().getSize()) {
			player.getPackets().sendGameMessage("Not enough space in your bank.");
			return;
		}
		if (banking)
			refreshTotalSize();
	}

	public void depositAllEquipment(boolean banking) {
		int reqSize = player.getAuraManager().isActivated() ? Equipment.SLOT_ARROWS : Equipment.SLOT_AURA;
		int space = addItems(Arrays.copyOf(player.getEquipment().getItems().getItems(), reqSize), banking);
		if (space != 0) {
			for (int i = 0; i < space; i++)
				player.getEquipment().getItems().set(i, null);
			player.getEquipment().init();
			player.getAppearence().generateAppearenceData();
		}
		if (space < reqSize) {
			player.getPackets().sendGameMessage("Not enough space in your bank.");
			return;
		}
		if (banking)
			refreshTotalSize();
	}

	public void collapse(int tabId) {
		if (tabId == 0 || tabId >= getTabs().length)
			return;
		Item[] items = getTabs()[tabId];
		for (Item item : items)
			removeItem(getItemSlot(item.getId()), item.getAmount(), false, true);
		for (Item item : items)
			addItem(item.getId(), item.getAmount(), 0, false);
		refreshTabs();
		refreshItems();
	}

	public void insertItem(int fromSlot, int toSlot, int fromComponentId, int toComponentId) {
		int[] slot = getRealSlot(fromSlot);
		Item fromItem = getItem(slot);
		if (fromItem == null)
			return;
		int[] toRealSlot = getRealSlot(toSlot);
		Item toItem = getItem(toRealSlot);
		if (toItem == null)
			return;
		if (slot[0] != toRealSlot[0]) {
			switchItem(fromSlot, toSlot, fromComponentId, toComponentId);
		} else if (toRealSlot[1] != slot[1]) {
			if (toRealSlot[1] > slot[1]) {
				for (int i = slot[1]; i < toRealSlot[1]; i++) 
					getTabs()[slot[0]][i] = getTabs()[slot[0]][i+1];
				getTabs()[slot[0]][toRealSlot[1]] = fromItem;
			} else {
				for (int i = slot[1]; i > toRealSlot[1]; i--) 
					getTabs()[slot[0]][i] = getTabs()[slot[0]][i-1];
				getTabs()[slot[0]][toRealSlot[1]] = fromItem;
			}
			refreshTab(slot[0]);
			refreshItems();
		}
		
		/*
		Item[] tab = new Item[getTabs()[slot[0]].length + 1];
		System.arraycopy(getTabs()[slot[0]], slot[1], tab, slot[1] - 1, getTabs()[slot[0]].length - slot[1] + 1);
		getTabs()[slot[0]][slot[1]] = toItem;
		getTabs()[slot[0]][slot[1] + 1] = fromItem;*/
		//refreshTab(slot[0]);
	}

	public void switchItem(int fromSlot, int toSlot, int fromComponentId, int toComponentId) {
		// System.out.println(fromSlot+", "+toSlot+", "+fromComponentId+", "+toComponentId);
		if (toSlot == 65535) {
			int toTab = toComponentId >= 76 ? 8 - (84 - toComponentId) : 9 - ((toComponentId - 46) / 2);
			if (toTab < 0 || toTab > 9)
				return;
			if (getTabs().length == toTab) {
				int[] fromRealSlot = getRealSlot(fromSlot);
				if (fromRealSlot == null)
					return;
				if (toTab == fromRealSlot[0]) {
					switchItem(fromSlot, getStartSlot(toTab));
					return;
				}
				Item item = getItem(fromRealSlot);
				if (item == null)
					return;
				removeItem(fromSlot, item.getAmount(), false, true);
				createTab();
				getTabs()[getTabs().length - 1] = new Item[]
				{ item };
				refreshTab(fromRealSlot[0]);
				refreshTab(toTab);
				refreshItems();
			} else if (getTabs().length > toTab) {
				int[] fromRealSlot = getRealSlot(fromSlot);
				if (fromRealSlot == null)
					return;
				if (toTab == fromRealSlot[0]) {
					switchItem(fromSlot, getStartSlot(toTab));
					return;
				}
				Item item = getItem(fromRealSlot);
				if (item == null)
					return;
				boolean removed = removeItem(fromSlot, item.getAmount(), false, true);
				if (!removed)
					refreshTab(fromRealSlot[0]);
				else if (fromRealSlot[0] != 0 && toTab >= fromRealSlot[0])
					toTab -= 1;
				refreshTab(fromRealSlot[0]);
				addItem(item.getId(), item.getAmount(), toTab, true);
			}
		} else
			switchItem(fromSlot, toSlot);
	}

	public void switchItem(int fromSlot, int toSlot) {
		int[] fromRealSlot = getRealSlot(fromSlot);
		Item fromItem = getItem(fromRealSlot);
		if (fromItem == null)
			return;
		int[] toRealSlot = getRealSlot(toSlot);
		Item toItem = getItem(toRealSlot);
		if (toItem == null)
			return;
		getTabs()[fromRealSlot[0]][fromRealSlot[1]] = toItem;
		getTabs()[toRealSlot[0]][toRealSlot[1]] = fromItem;
		refreshTab(fromRealSlot[0]);
		if (fromRealSlot[0] != toRealSlot[0])
			refreshTab(toRealSlot[0]);
		refreshItems();
	}

	public void openDepositBox() {
		if (!hasVerified(3) || player.tournamentResetRequired())
			return;
	/*	if (player.isUltimateIronman()) {
			player.getPackets().sendGameMessage("You can't use this feature as an ultimate ironman.");
			return;
		}*/
		player.getInterfaceManager().sendInterface(11);
		player.getInterfaceManager().closeInventory();
		player.getInterfaceManager().closeEquipment();
		final int lastGameTab = player.getInterfaceManager().openGameTab(9); // friends
		// tab
		sendBoxInterItems();
		player.getPackets().sendIComponentText(11, 13, "Bank Of " + Settings.SERVER_NAME + " - Deposit Box");
		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
				player.getInterfaceManager().sendInventory();
				player.getInventory().unlockInventoryOptions();
				player.getInterfaceManager().sendEquipment();
				player.getInterfaceManager().openGameTab(lastGameTab);
			}
		});
	}

	public void sendBoxInterItems() {
		player.getPackets().sendInterSetItemsOptionsScript(11, 17, 93, 6, 5, "Deposit-1", "Deposit-5", "Deposit-10", "Deposit-All", "Deposit-X", "Examine");
		player.getPackets().sendUnlockIComponentOptionSlots(11, 17, 0, 27, 0, 1, 2, 3, 4, 5);
	}

	public void openBank() {
		openBank(false);
	}

	public void openBank(boolean clanBank) {
		if (!hasVerified(2))
			return;
		if(player.tournamentResetRequired())
			// within pk tournament
			return;

		if (player.isUltimateIronman()) {
			player.getPackets().sendGameMessage("You can't stake bank as an ultimate ironman.");
			return;
		}
		if (clanBank && player.getClanManager() == null) {
			player.getPackets().sendGameMessage("You don't have a clan!");
			return;
		}
		if (clanBank && (player.isIronman() || player.isUltimateIronman())) {
			player.getPackets().sendGameMessage("You can't open clan banks as an ironman!");
			return;
		}
		if (clanBank && player.getControlerManager().getControler() instanceof FfaZone) {
			player.getPackets().sendGameMessage("You can not open clan banks in the spawn zone!");
			return;
		}
		this.clanBank = clanBank;
		if (currentTab >= getTabs().length)
			currentTab = getTabs().length-1;
		lastContainerCopy = null;
		withdrawNotes = false;
		player.getInterfaceManager().sendInterface(762);
		player.getInterfaceManager().sendInventoryInterface(763);
		player.getVarsManager().sendVarBit(8348, 2);
		refreshViewingTab();
		refreshTabs();
		unlockButtons();
		sendItems();
		refreshLastX();
		if (total_size == 0) {
			for (int i = 0; i < getTabs().length; i++)
				total_size += getTabSize(i);
		}
		refreshTotalSize();
		refreshInsertItems();
		refreshWithdrawNotes();
		refreshPlaceHolders();
		refreshWealth();
	}

	public void refreshWealth() {
		long money = 0;
		for (int i = currentTab > 0 ? currentTab : 0; i < (currentTab > 0 ? (currentTab + 1) : getTabs().length); i++) {
			for (int i2 = 0; i2 < getTabs()[i].length; i2++)
				money += (long) GrandExchange.getPrice((getTabs()[i][i2].getId())) * getTabs()[i][i2].getAmount();
		}
		//The Bank of Onyx 
		player.getPackets().sendIComponentText(762, 47
				, (currentTab > 0 ? ("Tab "+currentTab) : (clanBank ? ("The Bank of "+player.getClanName()+" clan") : "The Bank of "+Settings.SERVER_NAME))+" ~ "+Utils.getFormattedNumber(money)+" gp");
	}
	
	public void refreshTotalSize() {
		int usedFreeSlots = total_size > 68 ? 68 : total_size;
		player.getPackets().sendCSVarInteger(1038, usedFreeSlots);
		player.getPackets().sendCSVarInteger(192, total_size);
	}

	public void refreshLastX() {
		player.getVarsManager().sendVar(1249, lastX);
	}

	public void createTab() {
		int slot = getTabs().length;
		Item[][] tabs = new Item[slot + 1][];
		System.arraycopy(getTabs(), 0, tabs, 0, slot);
		tabs[slot] = new Item[0];
		setTabs(tabs);
	}

	public void destroyTab(int slot) {
		Item[][] tabs = new Item[getTabs().length - 1][];
		System.arraycopy(getTabs(), 0, tabs, 0, slot);
		System.arraycopy(getTabs(), slot + 1, tabs, slot, getTabs().length - slot - 1);
		setTabs(tabs);
		if (currentTab != 0 && currentTab >= tabs.length)  //slot
			currentTab--;
		refreshViewingTab(); //just in case
	}
	

	public boolean hasBankSpace() {
		return getBankSize() < getMaxBankSize(player);
	}

	public void withdrawItem(int bankSlot, int quanity) {
		withdrawItem(getRealSlot(bankSlot), quanity);
	}

	public void withdrawItem(int[] slots, int quantity) {
		if (quantity < 1)
			return;
		if (slots == null)
			return;
		Item item = getItem(slots);
		if (item == null)
			return;
		if (item.getAmount() == 0) {
			removeItem(slots, item.getAmount(), true, true);
			return;
		}
		if (item.getAmount() < quantity)
			item = new Item(item.getId(), item.getAmount(), /*item.getDegrade()*/0);
		else
			item = new Item(item.getId(), quantity, /*item.getDegrade()*/0);
		boolean noted = false;
		ItemConfig defs = item.getDefinitions();
		if (withdrawNotes) {
			if (!defs.isNoted() && defs.getCertId() != -1) {
				item.setId(defs.getCertId());
				noted = true;
			} else
				player.getPackets().sendGameMessage("You cannot withdraw this item as a note.");
		}
		if(player.getInventory().containsItem(CoalBag.OPENED_COAL_BAG_ID, 1) && item.getId() == CoalBag.COAL_ID) {
			int intercept = player.getCoalBag().intercept(player, item, item.getAmount());
			item.setAmount(item.getAmount() - intercept);
			boolean withdrawComplete = item.getAmount() == 0;
			removeItem(slots, intercept, withdrawComplete, false);
			if(withdrawComplete)
				return;
		}

		if(item.getId() == 995) {
			/*long space = Integer.MAX_VALUE - player.getMoneyPouch().getCoinsAmount();
			space += Integer.MAX_VALUE - player.getInventory().getAmountOf(995);
			if(space > item.getAmount()) {*/
				removeItem(slots, item.getAmount(), true, true);
				player.getInventory().addItemMoneyPouch(item);
			if (clanBank && player.getClanManager() != null)
				Bot.sendLog(Bot.CLAN_BANK, "[type=CLAN-BANK-WITHDRAW][clan="+player.getClanManager().getClan().getClanName()+"][name="+player.getUsername()+"][item="+item.getName()+"("+item.getId()+")x"+Utils.getFormattedNumber(item.getAmount())+"]");
			return;
			/*} else {
				player.sendMessage("Not enough space in your inventory");
				return;
			}*/
		}

		if (noted || defs.isStackable()) {
			if (player.getInventory().getItems().containsOne(item)) {
				int slot = player.getInventory().getItems().getThisItemSlot(item);
				Item invItem = player.getInventory().getItems().get(slot);
				if (invItem.getAmount() + item.getAmount() <= 0) {
					item.setAmount(Integer.MAX_VALUE - invItem.getAmount());
					player.getPackets().sendGameMessage("Not enough space in your inventory.");
				}
			} else if (!player.getInventory().hasFreeSlots()) {
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
				return;
			}
		} else {
			int freeSlots = player.getInventory().getFreeSlots();
			if (freeSlots == 0) {
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
				return;
			}
			if (freeSlots < item.getAmount()) {
				item.setAmount(freeSlots);
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
			}
		}
		removeItem(slots, item.getAmount(), true, false);
		player.getInventory().addItemMoneyPouch(item);
		if (clanBank && player.getClanManager() != null)
			Bot.sendLog(Bot.CLAN_BANK, "[type=CLAN-BANK-WITHDRAW][clan="+player.getClanManager().getClan().getClanName()+"][name="+player.getUsername()+"][item="+item.getName()+"("+item.getId()+")x"+Utils.getFormattedNumber(item.getAmount())+"]");
	}

	public void sendExamine(int fakeSlot) {
		int[] slot = getRealSlot(fakeSlot);
		if (slot == null)
			return;
		Item item = getTabs()[slot[0]][slot[1]];
		player.getPackets().sendGameMessage(ItemExamines.getExamine(item));
	}

	//return had space
	public boolean depositItem(int invSlot, int quantity, boolean refresh) {
		if (quantity < 1 || invSlot < 0 || invSlot > 27)
			return true;
		Item item = player.getInventory().getItem(invSlot);
		if (item == null)
			return true;
		if (item.getId() == 41941 && player.getLootingBag().depositItems()) {
			player.getPackets().sendGameMessage("You deposit your looting bag loot.");
			return true;
		}

		switch(item.getId()) {
			case Zalcano.IMBUED_TEPHRA:
			case Zalcano.TEPHRA:
			case Zalcano.REFINED_TEPHRA:
				player.sendMessage("A magical force stops you from banking the " + item.getName() + ".");
				return false;
		}

		if (clanBank && !ItemConfig.forID(item.getId()).tradeable) {
			player.sendMessage("A magical force stops you from banking the " + item.getName() + ".");
			return false;
		}
		
		int amt = player.getInventory().getItems().getNumberOf(item);
		if (amt < quantity)
			item = new Item(item.getId(), amt, /*item.getDegrade()*/0);
		else
			item = new Item(item.getId(), quantity, /*item.getDegrade()*/0);
		ItemConfig defs = item.getDefinitions();
		int originalId = item.getId();
		if (defs.isNoted() && defs.getCertId() != -1)
			item.setId(defs.getCertId());
		Item bankedItem = getItem(item.getId());
		if (bankedItem != null) {
			if (bankedItem.getAmount() + item.getAmount() <= 0) {
				item.setAmount(Integer.MAX_VALUE - bankedItem.getAmount());
				player.getPackets().sendGameMessage("Not enough space in your bank.");
			} else if (bankedItem.getAmount() + item.getAmount() >= Integer.MAX_VALUE) {
				player.getPackets().sendGameMessage("Could not bank your " + item.getName());
				return true;
			}
		} else if (!hasBankSpace()) {
			player.getPackets().sendGameMessage("Not enough space in your bank.");
			return false;
		}
		player.getInventory().deleteItem(invSlot, new Item(originalId, item.getAmount(), /*item.getDegrade()*/0));
		addItem(item, refresh);
		if (clanBank && player.getClanManager() != null)
			Bot.sendLog(Bot.CLAN_BANK, "[type=CLAN-BANK-DEPOSIT][clan="+player.getClanManager().getClan().getClanName()+"][name="+player.getUsername()+"][item="+item.getName()+"("+item.getId()+")x"+Utils.getFormattedNumber(item.getAmount())+"]");
		return true;
	}

	void addItem(Item item, boolean refresh) {
		addItem(item.getId(), item.getAmount(), refresh);
	}

	public int addItems(Item[] items, boolean refresh) {
		int space = (int) (getMaxBankSize(player) - getBankSize());
		if (space != 0) {
			space = (space < items.length ? space : items.length);
			for (int i = 0; i < space; i++) {
				if (items[i] == null)
					continue;
				addItem(items[i], false);
			}
			if (refresh) {
				refreshTabs();
				refreshItems();
				refreshWealth();
			}
		}
		return space;
	}

	public void addItem(int id, int quantity, boolean refresh) {
		addItem(id, quantity, currentTab, refresh);
	}

	public void addItem(int id, int quantity, int creationTab, boolean refresh) {

		if (player.isUltimateIronman()) {
			boolean add = player.getInventory().addItemDrop(id, quantity);
			if (!add)
				player.sendMessage(new Item(id).getName()+" x "+quantity+" was dropped on the floor since you can not use bank and your inventory is full!");
			else
				player.sendMessage(new Item(id).getName()+" x "+quantity+" was added to your inventory since you can not use bank!");
			return;
		}

		ItemConfig defs = ItemConfig.forID(id);
		if (defs.isNoted() && defs.getCertId() != -1)
			id = defs.getCertId();
		int[] slotInfo = getItemSlot(id);
		if (slotInfo == null) {
			if (creationTab >= getTabs().length)
				creationTab = getTabs().length - 1;
			if (creationTab < 0) // fixed now, alex
				creationTab = 0;
			int slot = getTabs()[creationTab].length;
			Item[] tab = new Item[slot + 1];
			System.arraycopy(getTabs()[creationTab], 0, tab, 0, slot);
			tab[slot] = new Item(id, quantity, 0, true);
			getTabs()[creationTab] = tab;
			if (refresh)
				refreshTab(creationTab);
			total_size++;
		} else {
			Item item = getTabs()[slotInfo[0]][slotInfo[1]];
			if(((long) item.getAmount()) + quantity > Integer.MAX_VALUE)
				item.setAmount(Integer.MAX_VALUE);
			else
				getTabs()[slotInfo[0]][slotInfo[1]] = new Item(item.getId(), item.getAmount() + quantity, /*item.getDegrade()*/0, true);
		}
		if (refresh) {
			refreshItems();
			refreshTotalSize();
			refreshWealth();
		}
	}

	public boolean removeItem(int fakeSlot, int quantity, boolean refresh, boolean forceDestroy) {
		return removeItem(getRealSlot(fakeSlot), quantity, refresh, forceDestroy);
	}

	public boolean removeItem(int[] slot, int quantity, boolean refresh, boolean forceDestroy) {
		if (slot == null)
			return false;
		Item item = getTabs()[slot[0]][slot[1]];
		boolean destroyed = false;
		if (quantity >= item.getAmount() && (!setPlaceHolders || forceDestroy)) {
			if (getTabs()[slot[0]].length == 1 && (forceDestroy || getTabs().length != 1)) {
				destroyTab(slot[0]);
				if (refresh)
					refreshTabs();
				destroyed = true;
			} else {
				Item[] tab = new Item[getTabs()[slot[0]].length - 1];
				System.arraycopy(getTabs()[slot[0]], 0, tab, 0, slot[1]);
				System.arraycopy(getTabs()[slot[0]], slot[1] + 1, tab, slot[1], getTabs()[slot[0]].length - slot[1] - 1);
				getTabs()[slot[0]] = tab;
				if (refresh)
					refreshTab(slot[0]);
			}
			total_size--;
		} else
			getTabs()[slot[0]][slot[1]] = new Item(item.getId(), Math.max(0, item.getAmount() - quantity), /*item.getDegrade()*/0, true);
		if (refresh) {
			refreshItems();
			refreshTotalSize();
			refreshWealth();
		}
		return destroyed;
	}

	public Item[][] clone() {
		Item[][] bank = new Item[getTabs().length][];
		for (int slot = 0; slot < getTabs().length; slot++) {
			bank[slot] = new Item[getTabs()[slot].length];
			for (int i = 0; i < bank[slot].length; i++) {
				bank[slot][i] = getTabs()[slot][i].clone();
			}
		}
		return bank;
	}

	public void copyBank(Player from) {
		setTabs(from.getBank().clone());
	}

	public Item getItem(int id) {
		for (int slot = 0; slot < getTabs().length; slot++) {
			for (Item item : getTabs()[slot])
				if (item.getId() == id)
					return item;
		}
		return null;
	}

	public int[] getItemSlot(int id) {
		for (int tab = 0; tab < getTabs().length; tab++) {
			for (int slot = 0; slot < getTabs()[tab].length; slot++)
				if (getTabs()[tab][slot].getId() == id)
					return new int[]
					{ tab, slot };
		}
		return null;
	}

	public Item getItem(int[] slot) {
		if (slot == null)
			return null;
		return getTabs()[slot[0]][slot[1]];
	}

	public int getStartSlot(int tabId) {
		int slotId = 0;
		for (int tab = 1; tab < (tabId == 0 ? getTabs().length : tabId); tab++)
			slotId += getTabs()[tab].length;

		return slotId;

	}

	public int[] getRealSlot(int slot) {
		for (int tab = 1; tab < getTabs().length; tab++) {
			if (slot >= getTabs()[tab].length)
				slot -= getTabs()[tab].length;
			else
				return new int[]
				{ tab, slot };
		}
		if (slot >= getTabs()[0].length)
			return null;
		return new int[]
		{ 0, slot };
	}
	
	public void switchPlaceHolders() {
		this.setPlaceHolders = !setPlaceHolders;
		refreshPlaceHolders();
	}
	
	public void refreshPlaceHolders() {
		player.getPackets().sendIComponentSprite(762, 124, setPlaceHolders ? 1433 : 1431);
	}

	public void refreshInsertItems() {
		player.getVarsManager().sendVar(304, insertItems ? 1 : 0);
	}
	
	public void refreshViewingTab() {
		player.getVarsManager().sendVarBit(4893, currentTab + 1);
	}

	public void refreshTab(int slot) {
		if (slot == 0)
			return;
		player.getVarsManager().sendVarBit(4885 + (slot - 1), getTabSize(slot));
	}

	public void sendItems() {
		player.getPackets().sendItems(95, getContainerCopy());
	}

	public void refreshItems(int[] slots) {
		player.getPackets().sendUpdateItems(95, getContainerCopy(), slots);
	}

	public Item[] getContainerCopy() {
		if (lastContainerCopy == null)
			lastContainerCopy = generateContainer();
		return lastContainerCopy;
	}

	public void refreshItems() {
		refreshItems(generateContainer(), getContainerCopy());
	}

	public void refreshItems(Item[] itemsAfter, Item[] itemsBefore) {
		if (itemsBefore.length != itemsAfter.length) {
			lastContainerCopy = itemsAfter;
			sendItems();
			return;
		}
		int[] changedSlots = new int[itemsAfter.length];
		int count = 0;
		for (int index = 0; index < itemsAfter.length; index++) {
			if (itemsBefore[index] != itemsAfter[index])
				changedSlots[count++] = index;
		}
		int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		lastContainerCopy = itemsAfter;
		refreshItems(finalChangedSlots);
	}

	public int getBankSize() {
		int size = 0;
		for (int i = 0; i < getTabs().length; i++)
			size += getTabs()[i].length;
		return size;
	}

	public Item[] generateContainer() {
		if(getTabs() == null || getTabs().length < 1)
			setTabs(new Item[1][0]);

		Item[] container = new Item[getBankSize()];
		int count = 0;
		for (int slot = 1; slot < getTabs().length; slot++) {
			System.arraycopy(getTabs()[slot], 0, container, count, getTabs()[slot].length);
			count += getTabs()[slot].length;
		}
		System.arraycopy(getTabs()[0], 0, container, count, getTabs()[0].length);
		return container;
	}

	public void unlockButtons() {
		// unlock bank inter all options
		player.getPackets().sendIComponentSettings(762, 95, 0, 1200, 2622718);
		// unlock bank inv all options
		player.getPackets().sendIComponentSettings(763, 0, 0, 27, 2425982);
	}

	public void switchWithdrawNotes() {
		withdrawNotes = !withdrawNotes;
		refreshWithdrawNotes();
	}
	
	public void refreshWithdrawNotes() {
		player.getVarsManager().sendVar(115, withdrawNotes ? 1 : 0);
	}
	

	public void switchInsertItems() {
		insertItems = !insertItems;
		refreshInsertItems();
	}

	public void setCurrentTab(int currentTab) {
		if (currentTab >= getTabs().length)
			return;
		this.currentTab = currentTab;
		refreshViewingTab();
		refreshWealth();
	}

	public int getLastX() {
		return lastX;
	}

	public void setLastX(int lastX) {
		this.lastX = lastX;
	}

	public void depositAllMoneyPouch(boolean banking) {
		long coinsCount = player.getMoneyPouch().getCoinsAmount();
		Item coins = getItem(995);
		int bankCoinsAmt = coins == null ? 0 : coins.getAmount();
		if ((long)coinsCount + (long)bankCoinsAmt > Integer.MAX_VALUE) {
			player.getPackets().sendGameMessage("Not enough space in your bank.");
			coinsCount = Integer.MAX_VALUE - bankCoinsAmt;
		}
		int space = 0;
		if (coinsCount > 0)
			space = addItems(new Item[]{ new Item(995, (int) coinsCount) }, banking);
		if (space != 0) {
			if (space < 1) {
				player.getPackets().sendGameMessage("Not enough space in your bank.");
				return;
			}
			player.getMoneyPouch().sendDynamicInteraction(coinsCount, true, MoneyPouch.TYPE_REMOVE);
		}
	}

	public int getPin() {
		return pin;
	}

	public void openHelpInterface() {
		player.getInterfaceManager().sendInterface(767);
	}

	public boolean isInsertItems() {
		return insertItems;
	}
	
	public static boolean isInvalidPin(int pin) {
		int pin1 = pin >> 12;
		pin -= pin1 << 12;
		int pin2 = pin >> 8;
		pin -= pin2 << 8;
		int pin3 = pin >> 4;
		pin -= pin3 << 4;
		String pinS = pin1 + "" + pin2 + "" + pin3 + "" + pin;
		for (String p : INVALID_PINS) 
			if (pinS.equalsIgnoreCase(p)) 
				return true;
		return false;
	}

	public void resetBank() {
		setTabs(new Item[1][0]);
		refreshViewingTab();
		refreshTabs();
	}
}
