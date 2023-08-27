package com.rs.game.player.dialogues;

import java.security.InvalidParameterException;
import java.util.Arrays;

import com.rs.cache.loaders.ItemConfig;
import com.rs.cache.loaders.NPCConfig;
import com.rs.game.player.Player;

public abstract class Dialogue {

	protected Player player;
	protected byte stage = -1;

	public Dialogue() {

	}

	public Object[] parameters;

	public void setPlayer(Player player) {
		this.player = player;
	}

	public abstract void start();

	public abstract void run(int interfaceId, int componentId);

	public abstract void finish();

	protected final void end() {
		player.getDialogueManager().finishDialogue();
	}

	public static final int NORMAL = 9827, QUESTIONS = 9827, MAD = 9789, MOCK = 9878, LAUGHING = 9851, WORRIED = 9775, HAPPY = 9850, CONFUSED = 9830, DRUNK = 9835, ANGERY = 9790, SAD = 9775, SCARED = 9780;
	protected static final String DEFAULT_OPTIONS_TITLE = "Select an option";
	

	protected static final short SEND_1_TEXT_CHAT = 241;
	protected static final short SEND_2_TEXT_CHAT = 242;
	protected static final short SEND_3_TEXT_CHAT = 243;
	protected static final short SEND_4_TEXT_CHAT = 244;
	
	protected static final byte IS_NOTHING = -1, IS_PLAYER = 0;
	public static final byte IS_NPC = 1;
	protected static final byte IS_ITEM = 2;

	public void hideContinueOption(int type) {
		player.getPackets().sendHideIComponent(type == IS_PLAYER ? 1191 : 1184, 18, true);
	}

	public void sendContinueOption(int type) {
		player.getPackets().sendHideIComponent(type == IS_PLAYER ? 1191 : 1184, 18, false);
	}

	public boolean sendNPCDialogue(int npcId, int animationId, String... text) {
		return sendEntityDialogue(IS_NPC, npcId, animationId, text);
	}

	public boolean sendItemDialogue(int itemId, String... text) {
		return sendEntityDialogue(IS_ITEM, itemId, -1, text);
	}

	public boolean sendPlayerDialogue(int animationId, String... text) {
		return sendEntityDialogue(IS_PLAYER, -1, animationId, text);
	}

	/*
	 * 
	 * auto selects title, new dialogues
	 */
	public boolean sendEntityDialogue(int type, int entityId, int animationId, String... text) {
		
		String title = "";
		if (type == IS_PLAYER) {
			title = player.getDisplayName();
		} else if (type == IS_NPC) {
			title = NPCConfig.forID(entityId).getToNPCName(player);
		} else if (type == IS_ITEM)
			title = ItemConfig.forID(entityId).getName();
		return sendEntityDialogue(type, title, entityId, animationId, text);
	}
	public static boolean sendEntityDialogue(Player player, int type, int entityId, int animationId, String... text) {

		String title = "";
		if (type == IS_PLAYER) {
			title = player.getDisplayName();
		} else if (type == IS_NPC) {
			title = NPCConfig.forID(entityId).getToNPCName(player);
		} else if (type == IS_ITEM)
			title = ItemConfig.forID(entityId).getName();
		return sendEntityDialogue(player, type, title, entityId, animationId, text);
	}
	/*
	 * idk what it for
	 */
	public int getP() {
		return 1;
	}

	public static final int OPTION_1 = 11, OPTION_2 = 13, OPTION_3 = 14, OPTION_4 = 15, OPTION_5 = 16;

	public boolean sendOptionsDialogue(String title, String... options) {
		if (options.length > 5) {
			throw new InvalidParameterException("The max options length is 5.");
		}
		
		if (player.isOsrsGameframe() && options.length > 1) {
			int interfaceID = 224 + (options.length * 2);
			player.getInterfaceManager().sendChatBoxInterface(interfaceID);
			player.getPackets().sendIComponentText(interfaceID, 1, title);
			for (int i = 0; i < options.length; i++)
				player.getPackets().sendIComponentText(interfaceID, 2 + i, options[i]);
			return true;
		}
		
		
		String[] newopts = new String[5];
		for (int i = 0; i < 5; i++)
			newopts[i] = "";
		int ptr = 0;
		for (String s : options) {
			if (s != null) {
				newopts[ptr++] = s;
			}
		}
		player.getInterfaceManager().sendChatBoxInterface(1188);
		player.getPackets().sendIComponentText(1188, 20, title);
		player.getPackets().sendExecuteScript(5589, newopts[4], newopts[3], newopts[2], newopts[1], newopts[0], options.length);
		return true;
	}

	public static boolean sendNPCDialogueNoContinue(Player player, int npcId, int animationId, String... text) {
		return sendEntityDialogueNoContinue(player, IS_NPC, npcId, animationId, text);
	}

	public static boolean sendPlayerDialogueNoContinue(Player player, int animationId, String... text) {
		return sendEntityDialogueNoContinue(player, IS_PLAYER, -1, animationId, text);
	}
	
	public static boolean sendItemDialogueNoContinue(Player player, int itemID, String... text) {
		return sendEntityDialogueNoContinue(player, IS_ITEM, itemID, -1, text);
	}

	/*
	 * 
	 * auto selects title, new dialogues
	 */
	public static boolean sendEntityDialogueNoContinue(Player player, int type, int entityId, int animationId, String... text) {
		String title = "";
		if (type == IS_PLAYER) {
			title = player.getDisplayName();
		} else if (type == IS_NPC) {
			title = NPCConfig.forID(entityId).getName();
		} else if (type == IS_ITEM)
			title = ItemConfig.forID(entityId).getName();
		return sendEntityDialogueNoContinue(player, type, title, entityId, animationId, text);
	}

	public static boolean sendEntityDialogueNoContinue(Player player, int type, String title, int entityId, int animationId, String... texts) {
		StringBuilder builder = new StringBuilder();
		for (int line = 0; line < texts.length; line++)
			builder.append(" " + texts[line]);
		String text = builder.toString();
		if (player.isOsrsGameframe()) {
			String[] lines = wrapText(text, 45);
			int linesCount = Math.min(4, getActualArrayLength(lines));
			int interfaceID = (type == IS_PLAYER ? 67 : 244) + linesCount;
			player.getInterfaceManager().replaceRealChatBoxInterface(interfaceID);
			player.getPackets().sendIComponentText(interfaceID, 3, title);
			for (int i = 0; i < linesCount; i++)
				player.getPackets().sendIComponentText(interfaceID, 4  + i, lines[i]);
			int faceWidget = type == IS_PLAYER && linesCount == 1 ? 1 : 2;
			if (type == IS_NPC)
					player.getPackets().sendNPCOnIComponent(interfaceID, faceWidget, entityId);
			else if (type == IS_PLAYER)
				player.getPackets().sendPlayerOnIComponent(interfaceID, faceWidget);
			else if (type == IS_ITEM)
				player.getPackets().sendItemOnIComponent(interfaceID, faceWidget, entityId, 1);
			if (animationId != -1)
				player.getPackets().sendIComponentAnimation(animationId, interfaceID, faceWidget);
			return true;
		}
		
		
		player.getInterfaceManager().replaceRealChatBoxInterface(1192);
		player.getPackets().sendIComponentText(1192, 16, title);
		player.getPackets().sendIComponentText(1192, 12, text);
		if (type == IS_ITEM)
			player.getPackets().sendItemOnIComponent(1192, 11, entityId, 1); // there
		else
			player.getPackets().sendEntityOnIComponent(type == IS_PLAYER, entityId, 1192, 11);
		if (animationId != -1)
			player.getPackets().sendIComponentAnimation(animationId, 1192, 11);
		return true;
	}

	public static boolean sendEmptyDialogue(Player player) {
		player.getInterfaceManager().replaceRealChatBoxInterface(89);
		return true;
	}

	public static void closeNoContinueDialogue(Player player) {
		player.getInterfaceManager().closeReplacedRealChatBoxInterface();
	}
	
    private static int getActualArrayLength(String[] array) {
        int index = 0;
        while (index < array.length && array[index] != null) index++;
        return index;
    }
	
	
	private static String[] wrapText(String message2, int maxLineLength2) {
        String[] lines = new String[5];
        String temp;
        String[] messages = message2.split("<br>");
        
        int i = 0;
        for (String message : messages) {
			 for (; i < lines.length;) {
				
				
				int realLength = 0;
				boolean skip = false;
				for (int k = 0; k < message.length(); k++) {
					char c = message.charAt(k);
					if (c == '<') {
						skip = true;
						continue;
					}
					if (c == '>') {
						skip = false;
						continue;
					}
					if (skip)
						continue;
					realLength++;
					
				}
				int maxLineLength = maxLineLength2 + (message.length()-realLength);
			
				
				if (message.length() <= maxLineLength) {
					lines[i++] = message;
					break;
				}

				temp = message.substring(0, maxLineLength);
				int lastIndex = temp.lastIndexOf(" ");

				lines[i] = temp.substring(0, lastIndex == 0 ? maxLineLength : lastIndex);
				message = message.substring(lines[i].length(), message.length());
				i++;
			}
        }
        return lines;
    }
	
	/*
	 * new dialogues
	 */
	public boolean sendEntityDialogue(int type, String title, int entityId, int animationId, String... texts) {
		return sendEntityDialogue(player, type, title, entityId, animationId, texts);
	}

	public static boolean sendEntityDialogue(Player player, int type, String title, int entityId, int animationId, String... texts) {
		StringBuilder builder = new StringBuilder();
		for (int line = 0; line < texts.length; line++)
			builder.append(" " + texts[line]);
		String text = builder.toString();


		if (player.isOsrsGameframe()) {

			String[] lines = wrapText(text, 45);
			int linesCount = Math.min(4, getActualArrayLength(lines));

			int interfaceID = (type == IS_PLAYER ? 63 : 240) + linesCount;
			player.getInterfaceManager().sendChatBoxInterface(interfaceID);
			player.getPackets().sendIComponentText(interfaceID, 3, title);
			for (int i = 0; i < linesCount; i++)
				player.getPackets().sendIComponentText(interfaceID, 4  + i, lines[i]);
			if (type == IS_NPC)
				player.getPackets().sendNPCOnIComponent(interfaceID, 2, entityId);
			else if (type == IS_PLAYER)
				player.getPackets().sendPlayerOnIComponent(interfaceID, 2);
			else if (type == IS_ITEM)
				player.getPackets().sendItemOnIComponent(interfaceID, 2, entityId, 1);
			if (animationId != -1)
				player.getPackets().sendIComponentAnimation(animationId, interfaceID, 2);
			return true;
		}

		if (type == IS_NPC) {
			player.getInterfaceManager().sendChatBoxInterface(1184);
			player.getPackets().sendIComponentText(1184, 17, title);
			player.getPackets().sendIComponentText(1184, 13, text);
			player.getPackets().sendNPCOnIComponent(1184, 11, entityId);
			if (animationId != -1)
				player.getPackets().sendIComponentAnimation(animationId, 1184, 11);
		} else if (type == IS_PLAYER) {
			player.getInterfaceManager().sendChatBoxInterface(1191);
			player.getPackets().sendIComponentText(1191, 8, title);
			player.getPackets().sendIComponentText(1191, 17, text);
			player.getPackets().sendPlayerOnIComponent(1191, 15);
			if (animationId != -1)
				player.getPackets().sendIComponentAnimation(animationId, 1191, 15);
		} else if (type == IS_ITEM) {
			player.getInterfaceManager().sendChatBoxInterface(1184);
			for (int i = 0; i < 3; i++)
				player.getPackets().sendHideIComponent(1184, 14 + i, true);
			player.getPackets().sendIComponentText(1184, 17, title);
			player.getPackets().sendIComponentText(1184, 13, text);
			player.getPackets().sendItemOnIComponent(1184, 11, entityId, 1); // there
			// is
			// a
			// config
			// for
			// this
			if (animationId != -1)
				player.getPackets().sendIComponentAnimation(animationId, 1184, 11);
		}
		return true;
	}
	public static boolean sendDialogueNoContinue(Player player, String... texts) {
		StringBuilder builder = new StringBuilder();
		for (int line = 0; line < texts.length; line++)
			builder.append(" " + texts[line]);
		String text = builder.toString();
		
		if (player.isOsrsGameframe()) {
			String[] lines = wrapText(text, 70);
			int linesCount = Math.min(4, getActualArrayLength(lines));
			int interfaceID = 214 + linesCount;
			player.getInterfaceManager().replaceRealChatBoxInterface(interfaceID);
			player.getPackets().sendIComponentText(interfaceID, 1, "");
			for (int i = 0; i < linesCount; i++)
				player.getPackets().sendIComponentText(interfaceID, 2  + i, lines[i]);
			return true;
		}
		
		player.getInterfaceManager().replaceRealChatBoxInterface(1190);
		player.getPackets().sendIComponentText(1190, 4, text);
		return true;
	}


	public boolean sendDialogue(String... texts) {
		StringBuilder builder = new StringBuilder();
		for (int line = 0; line < texts.length; line++)
			builder.append((line == 0 ? "<p=" + getP() + ">" : "<br>") + texts[line]);
		String text = builder.toString();
		if (player.isOsrsGameframe()) {//seems to use new system already
		/*	player.getInterfaceManager().sendChatBoxInterface(210);
			player.getPackets().sendIComponentText(210, 1, text);*/
			String[] lines = wrapText(text, 70);
			int linesCount = Math.min(4, getActualArrayLength(lines));
			int interfaceID = 210 + linesCount;
			player.getInterfaceManager().sendChatBoxInterface(interfaceID);
			player.getPackets().sendIComponentText(interfaceID, 1, "");
			for (int i = 0; i < linesCount; i++)
				player.getPackets().sendIComponentText(interfaceID, 2  + i, lines[i]);
			return true;
		}
		player.getInterfaceManager().sendChatBoxInterface(1186);
		player.getPackets().sendIComponentText(1186, 1, text);
		return true;
	}

	public boolean sendEntityDialogue(short interId, String[] talkDefinitons, byte type, int entityId, int animationId) {
		if (type == IS_PLAYER || type == IS_NPC) { // auto convert to new
			// dialogue all old dialogues
			String[] texts = new String[talkDefinitons.length - 1];
			for (int i = 0; i < texts.length; i++)
				texts[i] = talkDefinitons[i + 1];
			sendEntityDialogue(type, talkDefinitons[0], entityId, animationId, texts);
			return true;
		}
		return true;
	}

	public int getOrdinal(int componentId) {
		return componentId == OPTION_1 ? 0 : componentId - 12;
	}
}
