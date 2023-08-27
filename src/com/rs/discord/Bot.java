package com.rs.discord;

import java.util.List;

import com.rs.Settings;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild.Ban;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * 
 * @author Alex (Dragonkk)
 * Mar 5, 2019
 */
public class Bot {


	public static final long INGAME_CHANNEL = -1,
			ALL_CHANNEL = 939330815811264533L, LOGIN_LOGOUT_CHANNEL = 939330791853408336L,
			PUBLIC_CHAT_CHANNEL = 939330855560683621L, PRIVATE_CHAT_CHANNEL = 939330884455252008L,
			FRIEND_CHAT_CHANNEL = 939330901068902410L, CLAN_CHAT_CHANNEL = 939330914763296798L, COMMAND_CHANNEL = 939331080257941595L,
			TRADE_STACK_CANNEL = 939330929674031134L, PICKUP_DROP_CHANNEL = 939330951362801726L, ACTIVITIES_CHANNEL = 939331006740172800L,
			REPORTS_CHANNEL = 939331328518787132L, SELL_BUY_CHANNEL = 939331034846199859L, KILL_DEATH_CHANNEL = 939330972397207552L,
			DONATIONS_CHANNEL = 939331247157702716L, BOX_CHANNEL = 939331225028550656L, ANTIBOT_CHANNEL = 939331146813153401L,
			REF_CHANNEL = 939331170112512001L, TRACK_PC = 939331345598013460L, RAID_REWARDS = PICKUP_DROP_CHANNEL, CLAN_BANK = 1048793350108622969L;
	
	
    private static JDA jda;
    
    static 	int sent = 0;
  
    /*
    public static void main5(String[] args) throws LoginException, InterruptedException {
    
    	  jda = new JDABuilder(AccountType.BOT).setToken("NDYzNDA0Njk4NzcxMjU5Mzk0.Dhv7dg.sC-N3UlyX4yjxDnJn8fmoD8iBIc").buildBlocking();
          jda.getPresence().setGame(Game.playing("Onyx RSPS"));
      //	sendMessage(ALL_CHANNEL, "test");
      	System.out.println(jda.isAutoReconnect());
      	
      	Guild guild = jda.getGuilds().get(0);
      	int total = guild.getMembers().size();
      
      	for (Member m : guild.getMembers()) {
      		
      		User user = m.getUser();
      		try {

         	 //  if (user.getIdLong() == 210788805148540928L)
           	   user.openPrivateChannel().queue((channel) ->
               {
            		try {
            			System.out.println("sent: "+(sent++)+"/"+total);
                   channel.sendMessage("Hey guys! \r\n" + 
                   		"\r\n" + 
                   		"How was your Halloween? We at Onyx hope you had a good one! We wanted to just let you know that after a little delay we have finally released our Halloween event! This one features a brand new Grim Reaper boss along with an array of new Halloween items including the brand new and op Demonic Reaper�s set. \r\n" + 
                   		"\r\n" + 
                   		"Be sure to check out our patch notes for all the details: https://onyxftw.com/forum/index.php?/topic/1914-onyx-ii-update-8-halloween-event/!\r\n" + 
                   		"\r\n" + 
                   		"We hope to see you in-game!").queue();
             		} catch (Throwable e) {
              			e.printStackTrace();
              		}
               });
      		} catch (Throwable e) {
      			e.printStackTrace();
      		}
      	}
		
    }
    */
    public static void restart() {
    	if (!Settings.HOSTED)
    		return;
    	try {
        	jda.shutdown();
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
    	init(false);
    }
    
	public static void main2(String[] args) {
		try {
			// jda = new
			// JDABuilder(AccountType.BOT).setToken("NDYzNDA0Njk4NzcxMjU5Mzk0.Dhv7dg.sC-N3UlyX4yjxDnJn8fmoD8iBIc").build();

			jda = JDABuilder.createDefault("NDYzNDA0Njk4NzcxMjU5Mzk0.Dhv7dg.sC-N3UlyX4yjxDnJn8fmoD8iBIc").build()
					.awaitReady();

			jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("Onyx RSPS"));

			System.out.println(jda.getGuilds().size());
			List<Ban> ban = jda.getGuilds().get(0).retrieveBanList().complete();
			
			JDA jda2 = JDABuilder.createDefault("ODk4MjEwMDg4NDEyNjQ3NDY2.YWg5VQ.I9G-iDZb1Fs6abC1tgpVkm37OMY").build()
					.awaitReady();

			jda2.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("Matrix RSPS"));
			
			List<Ban> ban2 = jda2.getGuilds().get(0).retrieveBanList().complete();
			System.out.println(ban.size());
			for (Ban oldBan : ban) {

				jda2.getGuilds().get(0).ban(oldBan.getUser(), 7, oldBan.getReason()).complete();
				System.out.println("banned "+oldBan.getUser()+", "+oldBan.getReason());
			}
			
			System.out.println(ban2.size());
			
			
			// sendLog(INGAME_CHANNEL, "fix");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
    
    public static void init(boolean login) {
    	if (!Settings.HOSTED)
    		return;
    	/*new Thread() {
    		
    		public void run() {
    	    	*/  try {
    	    		
    	    		if (login) 
    	    			jda = JDABuilder.createDefault("ODk4MjEwMDg4NDEyNjQ3NDY2.YWg5VQ.I9G-iDZb1Fs6abC1tgpVkm37OMY").addEventListeners(new IngameChannelListener()).build().awaitReady();
    	    		else
    	    			jda = JDABuilder.createDefault("ODk4MjEwMDg4NDEyNjQ3NDY2.YWg5VQ.I9G-iDZb1Fs6abC1tgpVkm37OMY").build().awaitReady();
    	              jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("Matrix RSPS"));
    	              // jda.getPresence().setGame(Game.playing("Onyx RSPS"));
    	              /*if (login)
    	            	  jda.addEventListener(new IngameChannelListener());*/
    	          } catch (Throwable e) {
    	              e.printStackTrace();
    	          }/*
    		}
    	}.start();*/
    	
    }
    
    public static void sendLog(long id, String text) {
    	sendMessage(ALL_CHANNEL, text);
    	sendMessage(id, text);
    }
    
	public static void sendMessage(long id, String text) {
		if (id == -1)
			return;
		if (text.length() >= 1000) 
			text = text.substring(0, 1000);
    	text = text.replace("@", "");
		try {
			if (jda == null) // failed to launch disabled.
				return;
			// temporary cuz dont wanna spam users with drops until message definied
			// properly
			// id = STAFF_CHANNEL;
			TextChannel channel = jda.getTextChannelById(id);
			if (channel == null)
				return;
			int count = 0; // prevent deadlock
			while (text.contains("<") && text.contains(">") && count++ < 10) {
				int index = text.indexOf(">"); // gets first index
				if (index == -1)
					break;
				text = text.substring(0, text.indexOf("<")) + text.substring(text.indexOf(">") + 1, text.length());
			}
			channel.sendMessage(text).queue();
		} catch (Throwable e) { // just in case as lib doesnt seem very stable
			e.printStackTrace();
		}
	}
    
}
