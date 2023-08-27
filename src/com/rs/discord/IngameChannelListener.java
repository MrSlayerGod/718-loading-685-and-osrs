package com.rs.discord;

import com.rs.login.FriendsChat;
import com.rs.login.Login;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class IngameChannelListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		 User author = event.getAuthor();  //how to prevent self messages
		 if (author.isBot() || author.isFake())
			 return;
		 MessageChannel channel = event.getChannel();
		 if (channel.getIdLong() != Bot.INGAME_CHANNEL)
			 return;
		 String message = event.getMessage().getContentRaw();
		 if (message.length() > 100)
			message = message.substring(0, 100);
		 FriendsChat chat = Login.getFriendChat("onyx");
		 if (chat == null)
			 return;
		 
		 String name = author.getName()+"#"+author.getDiscriminator();
		 String nickname = event.getMember().getNickname();
		 if (nickname != null && !nickname.equals(name))
			 name = nickname+"("+name+")";
		 chat.sendDiscordMessage(name, message);
	}
}
