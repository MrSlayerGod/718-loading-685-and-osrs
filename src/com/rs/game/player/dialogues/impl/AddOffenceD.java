package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;
import com.rs.net.LoginClientChannelManager;
import com.rs.net.LoginProtocol;
import com.rs.net.encoders.LoginChannelsPacketEncoder;
import com.rs.utils.Utils;

public class AddOffenceD extends Dialogue {

	private String target;
	private int type;
	private long time;

	@Override
	public void start() {
		target = (String) parameters[0];

		stage = 0;
		type = -1;
		sendOptionsDialogue("Select punishment for " + target, "Ip Ban", "Ip Mute", "Ban", "Mute");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 0) {
			if (componentId == OPTION_1) {
				if (player.getRights() < 1) { //ipban
					stage = -1;
					sendDialogue("This option is available to mod+ only.");
					return;
				}
				type = LoginProtocol.OFFENCE_ADDTYPE_IPBAN;
				sendTimeChoice();
			} else if (componentId == OPTION_2) {
				if (player.getRights() < 1 && !player.isSupporter()) {
					stage = -1;
					sendDialogue("This option is available to support+ only.");
					return;
				}
				type = LoginProtocol.OFFENCE_ADDTYPE_IPMUTE;
				sendTimeChoice();
			} else if (componentId == OPTION_3) {
				if (player.getRights() < 1) {
					stage = -1;
					sendDialogue("This option is available to mod+ only.");
					return;
				}
				type = LoginProtocol.OFFENCE_ADDTYPE_BAN;
				sendTimeChoice();
			} else if (componentId == OPTION_4) {
				if (player.getRights() < 1 && !player.isSupporter()) {
					stage = -1;
					sendDialogue("This option is available to support+ only.");
					return;
				}
				type = LoginProtocol.OFFENCE_ADDTYPE_MUTE;
				sendTimeChoice();
			} else
				end();
		} else if (stage == 1) {
			if (componentId == OPTION_1) {
				time = 1000 * 60 * 60 * 1;
				punish();
			} else if (componentId == OPTION_2) {
				time = 1000 * 60 * 60 * 24;
				punish();
			} else if (componentId == OPTION_3) {
				time = 1000 * 60 * 60 * 48;
				punish();
			} else if (componentId == OPTION_4) {
				if (player.getRights() < 1) {
					stage = -1;
					sendDialogue("This option is available to mod+ only.");
					return;
				}
				time = 1000 * 60 * 60 * 24 * 7;
				punish();
			} else if (componentId == OPTION_5) {
				if (player.getRights() < 1) {
					stage = -1;
					sendDialogue("This option is available to mod+ only.");
					return;
				}
				time = 1000l * 60l * 60l * 24l * 7l * 4l * 12l * 50l;//1000 * 60 * 60 * 24 * 365;
				//time = -1;
				punish();
			} else {
				end();
			}
		} else {
			end();
		}
	}

	private void sendTimeChoice() {
		stage = 1;
		sendOptionsDialogue("Select punishment for " + target, "1 hour", "24 hours", "48 hours", "Week", "Permanent");
	}

	private void punish() {
		if (type == -1) {
			end();
			return;
		}

		long expires = 1000l * 60l * 60l * 24l * 7l * 4l * 12l * 50l; // 50 years
		if (time >= 0)
			expires = Utils.currentTimeMillis() + time;
		LoginClientChannelManager.sendUnreliablePacket(LoginChannelsPacketEncoder.encodeAddOffence(type, target, player.getUsername(), "Offence added by OffenceAddDialogue", expires).trim());

		stage = -1;
		sendDialogue("Request for " + target + " punishment was successfully sent!");

	}

	@Override
	public void finish() {

	}

}
