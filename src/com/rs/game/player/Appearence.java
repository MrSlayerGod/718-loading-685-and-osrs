package com.rs.game.player;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemConfig;
import com.rs.cache.loaders.NPCConfig;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.content.Costumes;
import com.rs.game.player.content.Deadman;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.custom.CustomItems;
import com.rs.io.OutputStream;
import com.rs.utils.Utils;

public class Appearence implements Serializable {

	private static final long serialVersionUID = 7655608569741626586L;

	private transient int renderEmote;
	private int title;
	private int[] lookI;
	private byte[] colour;
	private boolean male;
	private transient boolean glowRed;
	
	
	
	
	private transient byte[][] appeareanceData;
	
	private transient short transformedNpcId;
	private transient boolean hidePlayer;
	private transient boolean identityHide;
	private transient int forcedWeapon, forcedShield, forcedAmulet, forcedCape;

	private transient Player player;

	public Appearence() {
		male = true;
		title = -1;
		resetAppearence();
	}

	public void setGlowRed(boolean glowRed) {
		this.glowRed = glowRed;
		generateAppearenceData();
	}
	
	
	public void setPlayer(Player player) {
		this.player = player;
		transformedNpcId = -1;
		renderEmote = -1;
		forcedWeapon = forcedShield = forcedAmulet = forcedCape = -1;
		appeareanceData = new byte[8][];
		if (lookI == null)
			resetAppearence();
		else
			for (int i = 0; i < lookI.length; i++) { // temp fix
				if (lookI[i] >= 16384 || lookI[i] < -2) {
					resetAppearence();
					break;
				}
			}
	}

	public void transformIntoNPC(int id) {
		transformedNpcId = (short) id;
		generateAppearenceData();
	}

	public void switchHidden() {
		hidePlayer = !hidePlayer;
		generateAppearenceData();
	}

	public void setHidden(boolean hidden) {
		if (hidePlayer == hidden)
			return;
		hidePlayer = hidden;
		generateAppearenceData();
	}
	
	public void setIdentityHide(boolean hide) {
		identityHide = hide;
		generateAppearenceData();
	}
	
	public void setForcedCape(int cape) {
		forcedCape = cape;
		generateAppearenceData();
	}
	
	public boolean isIdentityHidden() {
		return identityHide;
	}

	public boolean isHidden() {
		return hidePlayer;
	}

	public boolean isGlowRed() {
		return glowRed;
	}
	
	public static String getTitle(boolean male, int title) {
		if (title == 3001)
			return "<col=5F6169>Deadman</col> ";
		if (title == 3002) 
			return "<col=5F6169>" +(male ? "Ironman" : "Ironwoman") + "</col> ";
		if (title == 3003)
			return "<col=ff3300>Dungeoneer</col> ";
		if (title == 3004)
			return "<col=ffff66>Hero</col> ";
		if (title == 3005)
			return "<col=ffff66>Legendary</col> ";
		if (title == 3006)
			return "<col=ffff66>Ultimate Ironman</col> ";
		if (title == 3007)
			return "<col=ff3300>Gambler</col> ";
		if (title == 3008)
			return "<col=0e6b05>Green</col> ";
		if (title == 3009)
            return "<col=ff3300>Spent too much IRL gp on Gallifrey --></col> ";
		if (title == 3010)
			return "<col=ffff00>September Top Donator</col> ";
		if (title == 3011)
			return "<col=990099> of the Praesul</col>";
		if (title == 3012)
			return "<col=ffff00>Octuber Top Donator</col> ";
		if (title == 3013)
			return "<col=ffff00>November Top Donator</col> ";
		if (title == 3014)
			return "<col=ffff00>December Top Donator</col> ";
		if (title == 3015)
			return "<col=ffff00>January Top Donator</col> ";
		if (title == 3016)
			return "<col=ffff00>February Top Donator</col> ";
		if (title == 3017)
			return "<col=ffff00>March Top Donator</col> ";
		if (title == 3018)
			return "<col=ffff00>April Top Donator</col> ";
		if (title == 3019)
			return "<col=ffff00>2x Top Donator</col> ";
		if (title == 3020) 
			return "<col=D80000>Ultimate</col> ";
		if (title == 3021)
			return "<col=ffff00>May Top Donator</col> ";
		if (title == 3022)
			return "<col=ffff00>June Top Donator</col> ";
		if (title == 3023)
			return "<col=ffff00>3x Top Donator</col> ";
		if (title == 3024)
			return "<col=ffff00>N1 Donator</col> ";
		if (title == 3025)
			return "<col=0e6b05>Helper</col> ";
		if (title == 3026)
			return "<col=5F6169>Ultimate Ultimate</col> ";
		if (title == 3027)
			return "<col=D80000>Hardcore</col> ";
		if (title == 3028)
			return "<col=5F6169>Ultimate Hardcore</col> ";
		if (title == 3029)
			return "<col=5F6169>Expert</col> ";
		return title == 0 ? null : ClientScriptMap.getMap(male ? 1093 : 3872).getStringValue(title);
	}
	public String getTitle() {
		/*if (title == 3000)
			return "Genocidal";
		else if (title == 3001)
			return "Unicorn Slayer";*/
		String t = getTitle(male, title);
		int icon = player.getGameModeIcon();
		if (icon > 0 && icon != player.getMessageIcon())
			t = "<img="+(icon <= 2 ? (icon-1) : icon)+"></col> " + (t == null ? "" : t);
		return t;
	}
	
	public boolean hasTitle(int title) {
		return title == 3002;
	}

	public void generateAppearenceData() {
		player.getEquipment().updateKeepsakeFilter();
		generateAppearenceData(false, true);
		generateAppearenceData(true, true);
		generateAppearenceData(false, false);
		generateAppearenceData(true, false);
	}
	
	
	public void generateAppearenceData(boolean old, boolean cosmetic) {
		OutputStream stream = new OutputStream();
		int flag = 0;
		if (!male)
			flag |= 0x1;
		if (transformedNpcId >= 0)
			flag |= 0x2;
		String title = getTitle();
		if (title != null)
			flag |= !title.endsWith(" </col>") && !title.endsWith("</col> ") ? 0x80 : 0x40; // after/before
		if (identityHide) {
			title = "<col=C86400>";
			flag |= 0x80;
			flag |= 0x40;
		}
		stream.writeByte(flag);
		if (title != null) 
			stream.writeVersionedString(title);
		if (identityHide)
			stream.writeVersionedString("</col>");
		stream.writeByte(player.isDeadman() ? Deadman.getIcon(player) : (player.hasSkull() ? player.getSkullId() : -1)); // pk//icon
		stream.writeByte(player.getPrayer().getPrayerHeadIcon()); // prayer icon
		stream.writeByte(hidePlayer ? 1 : 0);
		// npc
		if (transformedNpcId >= 0) {
			stream.writeInt(-1); // 65535 tells it a npc
			stream.writeBigSmart(transformedNpcId);
			stream.writeByte(0);
		} else {
			ItemsContainer<Item> costume = !cosmetic ? Costumes.DEFAULT.getItems() : player.getEquipment().getCostume();
			for (int index = 0; index < 4; index++) {
				if (index == Equipment.SLOT_CAPE && forcedCape != -1) {
					stream.writeInt(16384 + forcedCape);
					continue;
				}
				
				if (index == Equipment.SLOT_WEAPON && forcedWeapon != -1)
					stream.writeInt(16384 + forcedWeapon);
				else if (index == Equipment.SLOT_AMULET && forcedAmulet != -1)
					stream.writeInt(16384 + forcedAmulet);
				else {
					Item item =costume.get(index);
					if(item == null)
						item = player.getEquipment().getItems().get(index);
					if (item == null)
						stream.writeInt(0);
					else
						stream.writeInt(16384 + item.getId());
				}
			}
			Item item =costume.get(Equipment.SLOT_CHEST);
			if(item == null)
				item = player.getEquipment().getItems().get(Equipment.SLOT_CHEST);
			stream.writeInt(item == null ? 0x100 + lookI[2] : 16384 + item.getId());
			item =costume.get(Equipment.SLOT_SHIELD);
			if(item == null)
				item = player.getEquipment().getItems().get(Equipment.SLOT_SHIELD);
			if (item == null || forcedShield != -1) {
				if (forcedShield == -1)
					stream.writeInt(0);
				else
					stream.writeInt(16384 + forcedShield);
			} else
				stream.writeInt(16384 + item.getId());
			
			item =costume.get(Equipment.SLOT_CHEST);
			if(item == null)
				item = player.getEquipment().getItems().get(Equipment.SLOT_CHEST);
			if ((lookI[3] != -1 && item == null) || (item != null && !Equipment.hideArms(old ? CustomItems.getOldLookItem(item.getId()) : item.getId())))
				stream.writeInt(0x100 + getArmsStyle());
			else
				stream.writeInt(0);
			
			item =costume.get(Equipment.SLOT_LEGS);
			if(item == null)
			item = player.getEquipment().getItems().get(Equipment.SLOT_LEGS);
			stream.writeInt(item == null ? 0x100 + lookI[5] : 16384 + item.getId());
			item =costume.get(Equipment.SLOT_HAT);
			if(item == null)
			item = player.getEquipment().getItems().get(Equipment.SLOT_HAT);
			if ((item == null || !Equipment.hideHair(old ? CustomItems.getOldLookItem(item.getId()) : item.getId())))
				stream.writeInt(0x100 + lookI[0]);
			else
				stream.writeInt(0);
			item =costume.get(Equipment.SLOT_HANDS);
			if(item == null)
				item = player.getEquipment().getItems().get(Equipment.SLOT_HANDS);
			
			if (item == null && old && !isMale()) //fix female hands
				item = new Item(1063);
			
			stream.writeInt(item == null ? 0x100 + lookI[4] : 16384 + item.getId());
			item =costume.get(Equipment.SLOT_FEET);
			if(item == null)
			item = player.getEquipment().getItems().get(Equipment.SLOT_FEET);
			stream.writeInt(item == null ? 0x100 + lookI[6] : 16384 + item.getId());
			// tits for female, bear for male
			item =costume.get(male ? Equipment.SLOT_HAT : Equipment.SLOT_CHEST);
			if(item == null)
			item = player.getEquipment().getItems().get(Equipment.SLOT_HAT /*male ? Equipment.SLOT_HAT : Equipment.SLOT_CHEST*/);
			if (item == null || (male && Equipment.showBear(old ? CustomItems.getOldLookItem(item.getId()) : item.getId())))
				stream.writeInt(0x100 + lookI[1]);
			else
				stream.writeInt(0);
			item = player.getEquipment().getItems().get(Equipment.SLOT_AURA);
			if (item == null)
				stream.writeInt(0);
			else
				stream.writeInt(16384 + item.getId());
			int pos = stream.getOffset();
			stream.writeShort(0);
			int hash = 0;
			int slotFlag = -1;
			for (int slotId = 0; slotId < player.getEquipment().getItems().getSize(); slotId++) {
				if (Equipment.DISABLED_SLOTS[slotId] != 0)
					continue;
				slotFlag++;
				/*if((slotId != Equipment.SLOT_CAPE || forcedCape == -1) && costume.get(slotId) != null) {
					ItemConfig defs =costume.get(slotId).getDefinitions();
					if(defs.originalModelColors == null || defs.originalModelColors.length != 4 || costume == Costumes.PALADIN_COSTUME)
						continue;
					int[] colors;
				//	if(player.getEquipment().getCostumeColor() != 0) {
						colors = new int[4];
						colors[0] = player.getEquipment().getCostumeColor();
						colors[1] = colors[0]+12;
						colors[2] = colors[1]+12;
						colors[3] = colors[2]+12;
				/*	}else
						colors = defs.originalModelColors;*/
				/*	if (Arrays.equals(colors, defs.originalModelColors))
						continue;
					hash |= 1 << slotFlag;
					stream.writeByte(0x4); // modify 4 model colors
					int slots = 0 | 1 << 4 | 2 << 8 | 3 << 12;
					stream.writeShort(slots);
					for (int i = 0; i < 4; i++)
						stream.writeShort(colors[i]);
					continue;
				}*/
	/*		Costumes costume = Costumes.CAT_COSTUME;
				if(costume != null &&costume.get(slotId) != null) {
					ItemDefinitions itemdef = costume.get(slotId).getDefinitions();
					Item current = player.getEquipment().getItems().get(slotId);
					if (current != null && (current.getDefinitions().getMaleWornModelId1() == -1 || current.getDefinitions().getFemaleWornModelId1() == -1))
						continue;
					hash |= 1 << slotFlag;
					stream.writeByte(0x1); // modify model ids
					int modelId = player.getAuraManager().getAuraModelId();
					stream.writeBigSmart(modelId); // male modelid1
					stream.writeBigSmart(modelId); // female modelid1
					if (itemdef.getMaleWornModelId2() != -1 || itemdef.getFemaleWornModelId2() != -1) {
						stream.writeBigSmart(itemdef.getMaleWornModelId1());
						stream.writeBigSmart(itemdef.getMaleWornModelId2());
					}
					if (auraDefs.getMaleWornModelId3() != -1 || auraDefs.getFemaleWornModelId3() != -1) {
						int modelId2 = player.getAuraManager().getAuraModelId2();
						stream.writeBigSmart(-1);
						stream.writeBigSmart(modelId2);
					}
				}else */if (slotId == Equipment.SLOT_HAT) {
					int hatId = costume.get(Equipment.SLOT_HAT) == null ? player.getEquipment().getHatId() : costume.get(Equipment.SLOT_HAT).getId();
					if (hatId == 20768 || hatId == 20770 || hatId == 20772) {
						ItemConfig defs = ItemConfig.forID(hatId - 1);
						if ((hatId == 20768 && Arrays.equals(player.getMaxedCapeCustomized(), defs.originalModelColors) || ((hatId == 20770 || hatId == 20772) && Arrays.equals(player.getCompletionistCapeCustomized(), defs.originalModelColors))))
							continue;
						hash |= 1 << slotFlag;
						stream.writeByte(0x4); // modify 4 model colors
						int[] hat = hatId == 20768 ? player.getMaxedCapeCustomized() : player.getCompletionistCapeCustomized();
						int slots = 0 | 1 << 4 | 2 << 8 | 3 << 12;
						stream.writeShort(slots);
						for (int i = 0; i < 4; i++)
							stream.writeShort(hat[i]);
						continue;
					}
				} else if (slotId == Equipment.SLOT_CAPE) {
					int capeId = forcedCape != -1 ? forcedCape : costume.get(Equipment.SLOT_CAPE) == null ? player.getEquipment().getCapeId() : costume.get(Equipment.SLOT_CAPE).getId();
					if (capeId == 25528 || capeId == 20767 || capeId == 20769 || capeId == 20771 || capeId == 32151 || capeId == 32152 || capeId == 32153) {
						ItemConfig defs = ItemConfig.forID(capeId);
						if ((capeId == 20767 && Arrays.equals(player.getMaxedCapeCustomized(), defs.originalModelColors) || ((capeId == 32152 || capeId == 32153 || capeId == 25528 || capeId == 20769 || capeId == 20771) && Arrays.equals(player.getCompletionistCapeCustomized(), defs.originalModelColors))))
							continue;
						hash |= 1 << slotFlag;
						stream.writeByte(0x4); // modify 4 model colors
						int[] cape = capeId == 32151 || capeId == 20767 ? player.getMaxedCapeCustomized() : player.getCompletionistCapeCustomized();
						int slots = 0 | 1 << 4 | 2 << 8 | 3 << 12;
						stream.writeShort(slots);
						for (int i = 0; i < 4; i++)
							stream.writeShort(cape[i]);
						continue;
					} else if (capeId == 20708) {
						ClansManager manager = player.getClanManager();
						if (manager == null)
							continue;
						int[] colors = manager.getClan().getMottifColors();
						ItemConfig defs = ItemConfig.forID(capeId);
						boolean modifyColor = !Arrays.equals(colors, defs.originalModelColors);
						int bottom = manager.getClan().getMottifBottom();
						int top = manager.getClan().getMottifTop();
						if (bottom == 0 && top == 0 && !modifyColor)
							continue;
						hash |= 1 << slotFlag;
						stream.writeByte((modifyColor ? 0x4 : 0) | (bottom != 0 || top != 0 ? 0x8 : 0));
						if (modifyColor) {
							int slots = 0 | 1 << 4 | 2 << 8 | 3 << 12;
							stream.writeShort(slots);
							for (int i = 0; i < 4; i++)
								stream.writeShort(colors[i]);
						}
						if (bottom != 0 || top != 0) {
							int slots = 0 | 1 << 4;
							stream.writeByte(slots);
							stream.writeShort(ClansManager.getMottifTexture(top));
							stream.writeShort(ClansManager.getMottifTexture(bottom));
						}
						continue;
					}
				} else if (slotId == Equipment.SLOT_WEAPON) {
					int weaponId =  costume.get(Equipment.SLOT_WEAPON) == null ? player.getEquipment().getWeaponId() :  costume.get(Equipment.SLOT_WEAPON).getId();
					if (weaponId == 20709) {
						ClansManager manager = player.getClanManager();
						if (manager == null)
							continue;
						int[] colors = manager.getClan().getMottifColors();
						ItemConfig defs = ItemConfig.forID(20709);
						boolean modifyColor = !Arrays.equals(colors, defs.originalModelColors);
						int bottom = manager.getClan().getMottifBottom();
						int top = manager.getClan().getMottifTop();
						if (bottom == 0 && top == 0 && !modifyColor)
							continue;
						hash |= 1 << slotFlag;
						stream.writeByte((modifyColor ? 0x4 : 0) | (bottom != 0 || top != 0 ? 0x8 : 0));
						if (modifyColor) {
							int slots = 0 | 1 << 4 | 2 << 8 | 3 << 12;
							stream.writeShort(slots);
							for (int i = 0; i < 4; i++)
								stream.writeShort(colors[i]);
						}
						if (bottom != 0 || top != 0) {
							int slots = 0 | 1 << 4;
							stream.writeByte(slots);
							stream.writeShort(ClansManager.getMottifTexture(top));
							stream.writeShort(ClansManager.getMottifTexture(bottom));
						}
						continue;
					}
				} else if (slotId == Equipment.SLOT_AURA) {
					int auraId = player.getEquipment().getAuraId();
					if (auraId == -1 || !player.getAuraManager().isActivated()) 
						continue;
					ItemConfig auraDefs = ItemConfig.forID(auraId);
					if (auraDefs.getMaleWornModelId1() == -1 || auraDefs.getFemaleWornModelId1() == -1)
						continue;
					hash |= 1 << slotFlag;
					stream.writeByte(0x1); // modify model ids
					int modelId = player.getAuraManager().getAuraModelId();
					stream.writeBigSmart(modelId); // male modelid1
					stream.writeBigSmart(modelId); // female modelid1
					if (auraDefs.getMaleWornModelId2() != -1 || auraDefs.getFemaleWornModelId2() != -1) {
						int modelId2 = player.getAuraManager().getAuraModelId2();
						stream.writeBigSmart(modelId2);
						stream.writeBigSmart(modelId2);
					}
					continue;
				}
			}
			int pos2 = stream.getOffset();
			stream.setOffset(pos);
			stream.writeShort(hash);
			stream.setOffset(pos2);
		}

		for (int index = 0; index < colour.length; index++)
			// colour length 10
			stream.writeByte(colour[index]);

		stream.writeShort(getRenderEmote(cosmetic));
		stream.writeString(identityHide ? "Unknown" : player.getDisplayName());
		boolean pvpArea = player.isCanPvp();//World.isPvpArea(player);
		stream.writeByte(pvpArea ? player.getSkills().getCombatLevel() : player.getSkills().getCombatLevelWithSummoning());
		stream.writeByte(pvpArea ? player.getSkills().getCombatLevelWithSummoning() : 0);
		stream.writeByte(-1); // higher level acc name appears in front :P
		stream.writeByte(transformedNpcId >= 0 ? 1 : 0); // to end here else id
		// need to send more
		// data
		if (transformedNpcId >= 0) {
			NPCConfig defs = NPCConfig.forID(transformedNpcId);
			stream.writeShort(defs.anInt3029);
		    stream.writeShort(defs.anInt3065);
		    stream.writeShort(defs.anInt3050);
		    stream.writeShort(defs.anInt3042);
		    stream.writeByte(defs.anInt3068);
		}
		
		stream.writeByte(0);

		// done separated for safe because of synchronization
		byte[] appeareanceData = new byte[stream.getOffset()];
		System.arraycopy(stream.getBuffer(), 0, appeareanceData, 0, appeareanceData.length);
		byte[] md5Hash = Utils.encryptUsingMD5(appeareanceData);
		
		this.appeareanceData[getAppearenceIndex(old, cosmetic, false)] = appeareanceData;
		this.appeareanceData[getAppearenceIndex(old, cosmetic, true)] = md5Hash;
	}
	
	private int getAppearenceIndex(boolean old, boolean cosmetic, boolean hash) {
		return  (old ? 1 : 0) + (cosmetic ? 2 : 0) + (hash ? 4 : 0);
	}

	public int getSize() {
		if (transformedNpcId >= 0)
			return NPCConfig.forID(transformedNpcId).boundSize;
		return 1;
	}

	public void setRenderEmote(int id) {
		if (renderEmote == id)
			return;
		this.renderEmote = id;
		generateAppearenceData();
	}

	public int getRenderEmote(boolean cosmetic) {
		if (renderEmote >= 0)
			return renderEmote;
		if (transformedNpcId >= 0) {
			NPCConfig defs = NPCConfig.forID(transformedNpcId);
			HashMap<Integer, Object> data = defs.clientScriptData;
			if (data == null || !data.containsKey(2805)) 
				return defs.renderEmote;
		}
		//this isnt eoc rofl
		/*if(player.getAttackedBy() != null && player.getAttackedByDelay() > Utils.currentTimeMillis())
			return 2699;*/
		if (cosmetic && player.getEquipment().getKeepsakeItemsFiltered() != null && player.getEquipment().getKeepsakeItemsFiltered().get(Equipment.SLOT_WEAPON) != null)
			return player.getEquipment().getKeepsakeItemsFiltered().get(Equipment.SLOT_WEAPON).getDefinitions().getRenderAnimId();
		return player.getEquipment().getWeaponStance();
	}

	public void resetAppearence() {
		lookI = new int[7];
		colour = new byte[10];
		male();
	}

	public void male() {
		lookI[0] = 3; // Hair
		lookI[1] = 14; // Beard
		lookI[2] = 18; // Torso
		lookI[3] = 26; // Arms
		lookI[4] = 34; // Bracelets
		lookI[5] = 38; // Legs
		lookI[6] = 42; // Shoes~

		colour[2] = 16;
		colour[1] = 16;
		colour[0] = 3;
		male = true;
	}

	public void female() {
		lookI[0] = 48; // Hair
		lookI[1] = 48; // Beard
		lookI[2] = 57; // Torso
		lookI[3] = 65; // Arms
		lookI[4] = 68; // Bracelets
		lookI[5] = 77; // Legs
		lookI[6] = 80; // Shoes

		colour[2] = 16;
		colour[1] = 16;
		colour[0] = 3;
		male = false;
	}

	public byte[] getAppeareanceData(Player p2) {
		return appeareanceData[getAppearenceIndex(p2.isOldItemsLook(), !p2.isDisableCosmeticOverrides(), false)];
	}
	
	public byte[] getMD5AppeareanceDataHash(Player p2) {
		return appeareanceData[getAppearenceIndex(p2.isOldItemsLook(), !p2.isDisableCosmeticOverrides(), true)];
	}
	
	public byte[] getAppeareanceData() {
		return getAppeareanceData(player);
	}

	public boolean isMale() {
		return male;
	}

	public void setLook(int i, int i2) {
		lookI[i] = i2;
	}

	public void setColor(int i, int i2) {
		colour[i] = (byte) i2;
	}

	public void setMale(boolean male) {
		this.male = male;
	}

	public void setHairStyle(int i) {
		lookI[0] = i;
	}

	public void setTopStyle(int i) {
		lookI[2] = i;
	}

	public void setBootsStyle(int i) {
		lookI[6] = i;
	}

	public int getTopStyle() {
		return lookI[2];
	}

	public void setArmsStyle(int i) {
		lookI[3] = i;
	}
	
	public int getArmsStyle() {
		if (lookI[3] == -1)
			return player.getAppearence().isMale() ? 26 : 65;
		return lookI[3];
	}

	public void setHandsStyle(int i) {
		lookI[4] = i;
	}

	public void setLegsStyle(int i) {
		lookI[5] = i;
	}

	public int getHairStyle() {
		return lookI[0];
	}

	public void setBeardStyle(int i) {
		lookI[1] = i;
	}

	public int getBeardStyle() {
		return lookI[1];
	}

	public void setSkinColor(int color) {
		colour[4] = (byte) color;
	}

	public int getSkinColor() {
		return colour[4];
	}

	public void setHairColor(int color) {
		colour[0] = (byte) color;
	}

	public void setTopColor(int color) {
		colour[1] = (byte) color;
	}

	public void setLegsColor(int color) {
		colour[2] = (byte) color;
	}

	public int getHairColor() {
		return colour[0];
	}

	public int getBootColor() {
		return colour[5];
	}

	public void setBootsColor(int color) {
		colour[3] = (byte) color;
	}

	public void setTitle(int title) {
		this.title = title;
		generateAppearenceData();
	}

	public boolean isNPC() {
		return transformedNpcId != -1;
	}

	public int getForcedWeapon() {
		return forcedWeapon;
	}

	public void setForcedWeapon(int forcedWeapon) {
		this.forcedWeapon = forcedWeapon;
		generateAppearenceData();
	}

	public int getForcedShield() {
		return forcedShield;
	}

	public void setForcedShield(int forcedShield) {
		this.forcedShield = forcedShield;
		generateAppearenceData();
	}

	public int getForcedAmulet() {
		return forcedAmulet;
	}

	public void setForcedAmulet(int forcedAmulet) {
		this.forcedAmulet = forcedAmulet;
		generateAppearenceData();
	}
}
