package com.rs.game.minigames;

import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import com.rs.cache.loaders.ItemConfig;
import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class Trivia {

    public static final String[][] QUESTIONS = new String[][]{
            {"What is the lowest number you can roll with a Dicebag?", "1"},
            {"How many Loyalty Tokens does a Thok's Sword cost?", "1000"},
            {"How many Dungeoneering Tokens do you need to buy a Chaotic?", "200000"},
            {"How much xp do you need to achieve 120 in a skill (to the nearest million)?", "104m"},
            {"How many offensive spells are there in the Ancients spellbook?", "20"},
            {"What is the maximum amount of cash a player can hold in inventory?", "2147m"},
            {"How many skills do we have on Matrix?", "25"},
            {"How many pieces of armor are there in the Justiciar set?", "3"},
            {"How many trivia questions must be answered for a regular Completionist Cape?", "3"},
           // {"How many spins do you get per day if you are a Diamond Donator?", "5"},
            {"How many vote tickets can you receive every 12H?", "10"},
            {"How many Quest bossfights do we offer on Matrix?", "6"},
            {"How many Donator Ranks do we have on Matrix?", "6"},
            {"How many waves are there in the Inferno?", "69"},
            {"What revision of RSPS does Matrix fall under?", "718"},
            {"What Summoning level do you need to summon a Lava Titan?", "83"},
            {"What Agility level do you need to access the Advanced Gnome Course?", "85"},
            {"How many different color flowers are available from Mithril Seeds?", "9"},
            {"What Crafting level do you need to craft an Amulet of Fury?", "90"},
            {"Which Prayer level unlocks Soul Split?", "92"},
            {"What Herblore level do you need to create a Prayer Renewal?", "94"},
            {"Which Prayer level unlocks Turmoil?", "95"},
            {"What Summoning level do you need to summon a Pack Yak?", "96"},
            {"What Summoning level do you need to summon a Steel Titan?", "99"},
            {"What is the name of the altar that allows you to switch to any spellbook?", "altar of the occult"},
            {"What spellbook has Ice Barrage on it?", "ancients"},
            {"What is the rarest and most expensive partyhat color on our server?", "black"},
            {"What is the name of the PvM event with the most variety of bosses?", "boss bonanza"},
            {"What is the name of the new World Boss on Matrix?", "callus"},
            {"What minigame have teams that consist of Zamorak and Saradomin?", "castle wars"},
           // {"What is the new best-in-slot magic weapon on Matrix?", "cataclysm"},
            {"What drops the Primordial crystal?", "cerberus"},
            {"Which boss drops the Divine Sigil?", "corporeal beast"},
            {"Which Donator Rank allows access to ::ddz?", "diamond"},
            {"What is the command to get to our Gambling Zone?", "dice"},
            {"What platform can you join to stay up-to-date with Matrix news, market, media, etc?", "discord"},
            {"What minigame allows you to obtain Swift Gloves?", "dominion tower"},
            {"What fantasy monster is presented on our client background?", "dragon"},
            {"Who is the Owner and Developer of Matrix?", "dragonkk"},
            {"What is the strongest version of the Completionist Cape?", "elite"},
            {"What is the name of the wilderness boss that randomly spawns throughout the day?", "galvek"},
            {"What is the name of the Bandos GWD General?", "graardor"},
            {"What Hunter creature can you catch at level 77?", "grenwall"},
            //{"What is the thread number for our Guide Directory (contains links to most guides)?", "2657"},
            {"Which Dungeoneering setting highlights the fastest route to the boss via the minimap, at a minor XP loss?", "guide mode"},
            {"What Dungeoneering item was the original Twisted Bow, in terms of enemy Magic scaling?", "hexhunter bow"},
            {"What armor type do you have a chance of acquiring from Minigame Boxes? ", "hybrid"},
            {"What is the best-in-slot ring on Matrix?", "infinity ring"},
            {"What boss drops the Trident of the Seas?", "kraken"},
            {"What is the highest level Slayer Master available on Matrix?", "kuradal"},
            {"What is the name of the final boss in the Theatre of Blood?", "lady verzik"},
            {"What Dungeoneering floor type is the highest Experience per hour?", "large"},
           // {"What melee weapon on Matrix is the fastest hitting?", "llru"},
            //{"Who helped Zio construct the VIP Zone?", "monk"},
            {"What boss drops Torva, Pernix, and Virtus equipment?", "nex"},
           // {"Who is the designer of the Callus boss?", "nick"},
            {"What is the name of the website for our forums?", "matrixrsps.io"},
            {"What Dungeoneering setting, not recommended for beginners, disables the team key pouch?", "pre share"},
            {"What are the name of a Loadout you can customize via the rightmost icon in the Quest tab?", "preset"},
            {"What command allows me to teleport to the previous location I was in?", "prev"},
            {"What is Dragonkk's favorite color partyhat?", "red"},
            {"What boss drops the Royal Crossbow (give the abbreviation)?", "qbd"},
            {"Which ring reduces the cost of any weapon's special attack by 10%?", "ring of vigour"},
            {"What is the name of the NPC that runs the vote point shop?", "robin"},
            {"What stall can you pickpocket at ::home if you have 60 Thieving?", "ruby"},
            {"Which Donator Rank allows access to ::vip?", "legendary"},
            {"What is the best way to search items dropped by NPC's?", "searchitem"},
           // {"Who is the new Developer of Matrix?", "simplex"},
            {"What is the name of the Dark Beast boss in the Theatre of Blood?", "sotetseg"},
            {"When receiving a gravestome upon death, which direction does it spawn from the bank at ::home?", "south"},
            {"What is the name of the upgraded version of Justiciar armor?", "templar"},
          //  {"What is the name of Nick and Zio's unique PvM events?", "the horde"},
            {"What monster drops Dragon Claws?", "tormented demon"},
            {"What is Zulrah's signature ranged weapon drop?", "toxic blowpipe"},
            {"What command opens up the Matrix Teleport Interface?", "tp"},
            {"What is the most powerful ranged weapon versus monsters with a high magic level?", "twisted bow"},
            {"What is the name of the Spider boss within the Wilderness?", "venenatis"},
            {"Which Wilderness Demi-boss has two phases?", "vet'ion"},
            {"What is the name of the blue dragon boss?", "vorkath"},
            {"What is the top Donator Rank on Matrix?", "vip"},
         //   {"Who is the Co-Owner of Matrix?", "zio"},

    };
    //make it so rewards dont repeat
    private static final int[] REWARDS = {23713, 23714, 14882, 14883, 14878, 23715, 23716};

    private static int question;
    private static long expireTime;
    private static final List<String> players = new LinkedList<String>();


    public static void init() {
        question = -1;
        setTask();
    }

    public static void start() {
        expireTime = Utils.currentTimeMillis() + (1000 * 60 * 10);
        players.clear();
        question = Utils.random(QUESTIONS.length);
        String s = QUESTIONS[question][0], s2 = "";
      /*  int[] k = {Utils.random(s.length()-1), Utils.random(s.length()-1), Utils.random(s.length()-1)};
        outer: for(int i = 0; i < s.length(); i ++) {
            for(int l : k) {
                if(l == i) {
                    s2 += "_";
                    continue outer;
                }
            }

            s2 += s.charAt(i);
        }*/

        World.sendNews("[Trivia] ::answer <col=9999ff>" + /*s2*/s + "<col=ff8c38> to win rewards!", 1);
        for (Player player : World.getPlayers()) {
            if (!player.hasStarted() || player.hasFinished())
                continue;
            player.getInterfaceManager().sendNotification("EVENT", "::answer " + QUESTIONS[question][0]);
        }
    }

    private static void setTask() {
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    start();
                    setTask();
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }

        }, Utils.random(60000 * 15, 60000 * 105)); //every random 1hour
    }

    private static boolean expired() {
        return expireTime < Utils.currentTimeMillis();
    }

    public static void check(Player player, String message) {
        if (question == -1 || !QUESTIONS[question][1].equalsIgnoreCase(message)) {
            player.getPackets().sendGameMessage("Wrong answer!");
            return;
        }
        if (players.contains(player.getUsername())) {
            player.getPackets().sendGameMessage("You already claimed this reward.");
            return;
        }
        if (players.size() >= 1) {
            player.getPackets().sendGameMessage("Too many people already claimed this reward. Try to be faster the next time.");
            return;
        }
        if (expired()) {
            player.getPackets().sendGameMessage("Too late! this trivia session already expired.");
            return;
        }
        players.add(player.getUsername());
        player.increaseWonTrivias();
        player.getMusicsManager().playMusic(156); //exam condition
        int id = getReward(player);
        player.getBank().addItem(id, 1, false);
        player.getPackets().sendGameMessage("You have received " + ItemConfig.forID(id).getName() + " from trivia!");
        player.getPackets().sendGameMessage("Your rewards were added to bank.");

        World.sendNews(player,
                player.getDisplayName() + " won trivia! Answer: " + QUESTIONS[question][1], 1);
    }


    private static int getReward(Player player) {
        return REWARDS[Utils.random(REWARDS.length)];
    }

}
