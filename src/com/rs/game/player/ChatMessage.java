package com.rs.game.player;

import com.rs.Settings;
import com.rs.utils.Censor;
import com.rs.utils.Utils;

public class ChatMessage {

	private String message;
	private String filteredMessage;

	public ChatMessage(String message) {
		if(message != null && Settings.DISABLE_GLOBAL_PROFANITY) {
			message = Censor.getFilteredMessage(message);
		}
		if (!(this instanceof QuickChatMessage)) {
			filteredMessage = Censor.getFilteredMessage(message);
			this.message = Utils.fixChatMessage(message);
		} else
			this.message = message;
	}

	public String getMessage(boolean filtered) {
		return filtered ? filteredMessage : message;
	}
}
