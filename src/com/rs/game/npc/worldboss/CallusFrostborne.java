package com.rs.game.npc.worldboss;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.item.Item;
import com.rs.game.minigames.WorldBosses;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.combat.impl.CallusPhase4;
import com.rs.game.npc.others.Brazier;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.LightSource;
import com.rs.game.player.content.NPCKillLog;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.controllers.CallusController;
import com.rs.game.player.controllers.TheHorde;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class CallusFrostborne extends NPC {

    public static final int ICE_BALL_CHANCE = 16, SNOW_STORM_CHANCE = 25, ARENA_CLEAR_CHANCE = 20;

    private static final int PHASE_1_NPC = 21200, PHASE_2_NPC = 21201, PHASE_3_NPC = 21202, PHASE_4_NPC = 21212;
    public static boolean DEBUG = true;


    public static final String[] TEXTS = new String[]
            {
                    "The world will become Ice!",
                    "This world will be a cold, desolate wasteland!",
                    "A new Ice Age is upon us!",
                    "No warmth can save you now!",
                    "No flame matches my power!",
                    "Feel the wrath of my frozen core"};

    public List<WorldTile> icicles = new ArrayList<WorldTile>();

    private List<NPC> minnions = new ArrayList<NPC>();

    private int explosionTicks;
    private boolean allPlayersInFinalArea;

    public CallusFrostborne(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setLureDelay(0);
        setCapDamage(700);
        setCombatLevel(2760);
        setNextNPCTransformation(PHASE_1_NPC);
        setIntelligentRouteFinder(true);
        setRun(true);
        setForceMultiAttacked(true);
        setForceMultiArea(true);
        setForceAgressive(true);
        this.setForceLootshare(true);
        setDropRateFactor(9);
        boolean respawn = false;

        if(braziers.size() != 4 && braziers.size() != 0) {
            braziers.forEach(NPC::finish);
            braziers.clear();
            respawn = true;
        }

        if(braziers.size() == 0 || respawn) {
            braziers.add(new Brazier(this, new WorldTile(2387, 4057, 0), "South-Western"));
            braziers.add(new Brazier(this, new WorldTile(2387, 4081, 0), "North-Western"));
            braziers.add(new Brazier(this, new WorldTile(2411, 4081, 0), "North-Eastern"));
            braziers.add(new Brazier(this, new WorldTile(2411, 4057, 0), "South-Eastern"));
        } else {
            for(Brazier brazier : braziers) {
                brazier.setBoss(this);
            }
        }

        finalKillArea = Utils.random(FINAL_AREA.length - 1);

        this.setCantFollowUnderCombat(true);
    }

    public int finalKillArea = 0;

    public static WorldTile[][] FINAL_AREA = {
            {new WorldTile(2397,4060, 0), new WorldTile(2402,4062, 0)},
            {new WorldTile(2407,4067, 0), new WorldTile(2409,4073, 0)},
            {new WorldTile(2390,4067, 0), new WorldTile(2393,4072, 0)},
    };

    public boolean inFinalArea(Entity source) {
        if (source.getX() >= FINAL_AREA[finalKillArea][0].getX()
        && source.getY() >= FINAL_AREA[finalKillArea][0].getY()
        && source.getX() <= FINAL_AREA[finalKillArea][1].getX()
        && source.getY() <= FINAL_AREA[finalKillArea][1].getY())
            return true;
        return false;
    }
    private byte effect;

    public byte getEffect() {
        return effect;
    }

    public void setEffect(byte effect) {
        this.effect = effect;
    }

    private int playersNearby = 0;

    private double prayerEnrageCount = 1.0;

    private boolean rangeAttack;

    public void setRangeAttack(boolean rangeAttack) {
        this.rangeAttack = rangeAttack;
    }

    public boolean isRangeAttack() {
        return rangeAttack;
    }

    public static List<Brazier> braziers = new ArrayList<>();

    public static int getCallusShrineCoins() {
        return callusShrineCoins;
    }

    public static void setCallusShrineCoins(int callusShrineCoins) {
        CallusFrostborne.callusShrineCoins = callusShrineCoins;
    }

    public static void addCallusShrineCoins(int sac) {
        CallusFrostborne.callusShrineCoins += sac;

        if(callusShrineCoins >= CALLUS_SPAWN_VALUE) {
            //World.sendNews("<shad=0><col=00ffff>Callus has been resurrected, ::callus to help fend him off!", 1);
            setCallusShrineCoins(0);
        //    WorldBosses.forceNext(WorldBosses.WorldBoss.Callus.ordinal());
        }
    }

    public static int getRemainingCoinsToSpawn() {
        return CALLUS_SPAWN_VALUE - getCallusShrineCoins();
    }

    private static int callusShrineCoins = 0;

    public static final int CALLUS_SPAWN_VALUE = 100_000_000;

    public static void init() {
        ObjectHandler.register(133307, 1, (Player player, WorldObject obj) -> {
            if(player.getInventory().containsItem(995, 1))
              player.getDialogueManager().startDialogue("CallusShrine", 100);
            else
                player.getDialogueManager().startDialogue("CallusShrine", 101);
        });
        ObjectHandler.register(133307, 2, (Player player, WorldObject obj) -> {
            player.getDialogueManager().startDialogue("CallusShrine", 101);
        });
        ObjectHandler.register(131957, 1, (Player player, WorldObject obj) -> {
            player.getDialogueManager().startDialogue("ItemMessage", "There is a note inscribed on the crystal:<br>" +
                    "Nickk 08/20/2020: <col=00ffff><shad=0>\"Cross ice\" and \"Inspect <col=00ffff><shad=0>Cyan Crystal\" need removed.", 7573);
        });

        ObjectHandler.register(new int[]{106461, 106462}, 1, (Player player, WorldObject obj) -> {
            if ((player.getX() == 2400 || player.getX() == 2399) && player.getY() == 4041) {
                if (!LightSource.hasLightSource(player)) {
                    player.getDialogueManager().startDialogue("ItemMessage", "You must have a light source to keep warm while facing Callus.", 34534);
                } else {
                    player.getDialogueManager().startDialogue("EnterCallusD", obj);
                }
            }
        });
    }

    public static void setSiphonTiles(List<WorldTile> siphonTiles) {
        CallusFrostborne.siphonTiles = siphonTiles;
    }

    public static List<WorldTile> getSiphonTiles() {
        return siphonTiles;
    }

    public static List<WorldTile> siphonTiles = new ArrayList<>();

    public int tick = 0;

    public int lastMinionSpawnHP = 25000;

    public static void enterCallus(Player player, WorldObject object) {
        player.lock(3);
        player.sendMessage("You attempt to pass the ice gate..");
        player.resetWalkSteps();
        WorldTasksManager.schedule(new WorldTask() {
            int cycle = 1;
            WorldTile toTile = new WorldTile(player.getX(), player.getY() + 3, 0);

            @Override
            public void run() {
                if (cycle == 1) {
                    player.anim(1115);
                    player.setNextForceMovement(new ForceMovement(new WorldTile(player), 0, toTile, 2,
                            ForceMovement.NORTH));
                } else if (cycle == 2) {
                    player.applyHit(new Hit(null, (int) (player.getMaxHitpoints() * 0.333), HitLook.REGULAR_DAMAGE));
                    player.getPrayer().drainPrayer(player.getPrayer().getPrayerpoints());
                    player.sendMessage("You are badly injured from swan diving through the ice gate.");
                    player.setNextWorldTile(toTile);
                    player.faceObject(object);
                    player.getControlerManager().startControler(new CallusController());
                } else if (cycle == 3) {
                    stop();
                }
                cycle++;
            }
        }, 0, 1);
    }

    public static void testDrops(Player player, int i) {
        ArrayList<Item> drops = new ArrayList<>();

        for(int d = 0; d <= i; d++) {
            for(Drop drop : dropItems(true, true)) {
                boolean found = false;
                int amt = Utils.random(drop.getMinAmount(), drop.getMaxAmount());
                for(Item dropList : drops) {
                    if(dropList.getId() == drop.getItemId()) {
                        dropList.setAmount(dropList.getAmount()+amt);
                        found = true;
                        break;
                    }
                }
                if(!found)
                    drops.add(new Item(drop.getItemId(), amt));
            }
        }

        Collections.sort(drops, Comparator.comparingInt(Item::getAmount));

        player.getBank().resetBank();
        for(Item item : drops) {
            player.getBank().addItem(item.getId(), item.getAmount(), false);
            player.sendMessage(item.getName() + "  x  " + item.getAmount() + "");
        }
        player.getBank().refreshViewingTab();
        player.getBank().refreshTabs();
        player.getBank().openBank();
    }

    /**
     * On a boss kill, a random drop is picked by moving through the applicable tables based on rolls.
     * This is a rewritten table that only uses tableRoll and tierRoll removing dropRoll for simplification.
     * More math. More POWA BABY.
     */
    public static ArrayList<Drop> dropItems(boolean mvp, boolean topMVP){
        double tableRoll = Utils.random(mvp ? 1.0 : 0.9);
        double tierRoll = Utils.random(1.0);
        if(topMVP)
            tableRoll = Utils.random(0.6, 1.0);
        //double dropRoll = Utils.random(1.0);
        Drop[] possibleDrops;
        ArrayList<Drop> drops = new ArrayList<>();

        /* Relative rarities:
         *	- Drop on Skilling table: 60%
         *	- Drop on Cash/BM table - 20%
         *	- Drop on Supply drop/Lower tier gear table - 10%
         *	- Drop on Nice-to-have/Oof table - 6%
         *	- UBER 1337 CASH TABLE - 3.2%
         * 	- Drop on Armour table - .4%
         * 	- Drop on Sigil table - .3%
         * 	- Drop on Orb table - .1%
         */

        //Skilling table - 40% chance to hit table [3/5 kills]
        if (tableRoll < .4000){
            //Lower tier supply table (Herbs, Raw fish, Ores, Bars, Logs/Planks, Uncuts) - 24% chance to hit table [6/25 kills]
            if (tierRoll <= .5000){
                possibleDrops = new Drop[]{
                        //Lower tier Herbs
                        new Drop(208, 100, 125), 			//100-125 Rannars
                        new Drop(3050, 100, 125),			//100-125 Toadflax
                        new Drop(212, 100, 125),			//100-125 Avantoe
                        new Drop(214, 100, 125),			//100-125 Kwuarm

                        //Lower tier Raw fishies
                        new Drop(384, 150, 250), 			//150-250 Raw shark
                        new Drop(390, 150, 250),			//150-250 Raw Manta

                        //Lower tier Ores
                        new Drop(454, 150, 200),			//150-200 Coal ore
                        new Drop(445, 150, 200),			//150-200 Gold ore
                        new Drop(448, 125, 150),			//100-150 Mithril ore

                        //Lower tier Bars
                        new Drop(2360, 75, 100),			//100-150 Mithril bars

                        //Lower tier Logs/Planks
                        new Drop(1516, 200, 250),			//200-250 Yew logs
                        new Drop(8779, 200, 250),			//200-250 Oak planks

                        //Lower tier Uncuts
                        new Drop(1624, 150, 200),			//150-200 Uncut sapphire
                        new Drop(8779, 125, 175),			//125-175 Uncut emerald
                        new Drop(1620, 100, 125)			//100-125 Uncut ruby
                };
                drops.add(Utils.random(possibleDrops));

                //Mid tier supply table (Herbs, Raw fish, Ores, Bars, Logs/Planks, Uncuts) - 18% chance to hit table [9/50 kills]
            } else if (tierRoll > .5000 && tierRoll <= .8000){
                possibleDrops = new Drop[]{
                        //Mid tier Herbs
                        new Drop(3052, 75, 100),			//75-100 Snapdragon
                        new Drop(216, 75, 100),				//75-100 Cadantine
                        new Drop(2486, 75, 100),			//75-100 Lantadyme

                        //Mid tier Raw fishies
                        new Drop(15271, 125, 150), 			//125-150 Raw Rocktail
                        new Drop(43440, 125, 150),			//125-150 Raw Anglerfish

                        //Mid tier Ores
                        new Drop(450, 75, 125),				//75-125 Adamant ore

                        //Mid tier Bars
                        new Drop(2362, 60, 80),				//60-80 Adamant bars

                        //Mid tier Logs/Planks
                        new Drop(6332, 150, 200),			//150-200 Mahogany logs
                        new Drop(1514, 150, 175),			//150-175 Magic logs

                        //Mid tier Uncuts
                        new Drop(1618, 75, 100),			//75-100 Uncut diamond
                        new Drop(1632, 50, 75)				//50-75 Uncut dragonstone
                };
                drops.add(Utils.random(possibleDrops));

                //High tier supply table (Herbs, Ores, Bars, Logs/Planks, Uncuts) - 12% chance to hit table [3/25]
            } else {
                possibleDrops = new Drop[]{
                        //High tier Herbs
                        new Drop(218, 60, 80),				//60-80 Dwarf weed
                        new Drop(220, 60, 80),				//60-80 Torstol

                        //High tier Ores
                        new Drop(452, 50, 75),				//50-75 Rune ore

                        //High tier Bars
                        new Drop(2364, 30, 50),				//30-50 Rune bars

                        //High tier Logs/Planks
                        new Drop(8783, 75, 125),			//75-125 Mahogany planks
                        new Drop(49670, 75, 125),			//75-125 Redwood logs

                        //High tier Uncuts
                        new Drop(6572, 1, 2)				//1-2 Uncut onyx
                };
                drops.add(Utils.random(possibleDrops));
            }

            //Coins/Blood money table - 40% chance to hit table [1/5 kills]
        } else if (tableRoll > .4000 && tableRoll <= .6000){
            //Lower tier Coins/Blood money - 12% chance to hit table [3/25]
            if (tierRoll < .9000){
                drops.add(new Drop(995, 10_000_000, 20_500_000));		//10M-15.5M Coins

                //High tier Coins/Blood money - 2% chance to hit table [3/50 kills]
            } else {
                drops.add(new Drop(995, 25_000_000, 50_000_000));	//25M-50M Coins
            }

            //Supply drop/Lower tier gear table - 10% chance to hit table [1/10 kills]
        } else if (tableRoll > .6000 && tableRoll <= .9000){
            //Food drops (Brew flasks, Super restore flasks, Renewal flasks, Anglerfish, Rocktails, Overloads) - 4% chance to hit table [1/25 kills]
            //Low tier Armadyl shard drops - 4% chance to hit table
            if (mvp || tierRoll <= .4000){
                possibleDrops = new Drop[]{
                        new Drop(23352, 75, 100),			//75-100 Brew flasks
                        new Drop(23400, 75, 100),			//75-100 Super restore flasks
                        new Drop(23610, 50, 75),			//50-75 Renewal flasks
                        new Drop(43442, 150, 200),			//150-200 Anglerfish
                        new Drop(15273, 150, 200),			//150-200 Rocktails
                        new Drop(23531, 10, 10),			//10 Overload flasks
                        new Drop(21776, 5, 10)				//5-10 Armadyl shard
                };
                drops.add(Utils.random(possibleDrops));

                //Glacor drop table (Mid tier Shard drops + Glacor boots) - 3% chance to hit table [1/33 kills]
            } else if (tierRoll > .4000 && tierRoll <= .7500){
                possibleDrops = new Drop[]{
                        new Drop(21787, 1, 1), 				//Steadfast boots
                        new Drop(21790, 1, 1),				//Glaiven boots
                        new Drop(21793, 1, 1),				//Ragefire boots
                        new Drop(21776, 12, 18)				//12-18 Armadyl shards
                };
                drops.add(Utils.random(possibleDrops));

                //High tier Armadyl shards table - 2.5% chance to hit [1/40 kills]
            } else if (tierRoll > .7500 && tierRoll <= .9000){
                drops.add(new Drop(21776, 25, 50));		//25-50 Armadyl shards

                //Highest tier Armadyl shards + ABS table - 1% chance to hit [1/100 kills]
            } else {
                possibleDrops = new Drop[]{
                        new Drop(21776, 50, 100),			//50-100 Armadyl shards
                        new Drop(21777, 1, 1)				//1 Armadyl battle staff
                };
                drops.add(Utils.random(possibleDrops));
            }

            //Nice-to-haves table/Oofs - 6% chance to hit table [3/50 kills]
        } else if (tableRoll > .9000 && tableRoll <= .9600){
            //Low tier Nice-to-haves/oofs (Boxes, Effigy, Spin tickets) - 4.2% chance to hit table [39/1000 or 1/25.6 kills]
            if (tierRoll <= .70 ){
                drops.add(new Drop(11846, 1, 1));		//ahrim's set
                //Mid tier Nice-to-haves (Boxes, Chaotics) - .72% chance to hit table [9/1250 or 1/138.88 kills]
            } else if (tierRoll > .7000 && tierRoll <= .8200){
                possibleDrops = new Drop[] {
                        new Drop(25436, 1, 1),				//Premium mbox
                        new Drop(18349, 1, 1),				//Chaotic rapier
                        new Drop(18351, 1, 1),				//Chaotic longsword
                        new Drop(18353, 1, 1),				//Chaotic maul
                        new Drop(18355, 1, 1),				//Chaotic staff
                        new Drop(18357, 1, 1),				//Chaotic crossbow
                        new Drop(18359, 1, 1),				//Chaotic kiteshield
                        new Drop(18361, 1, 1)				//Eagle-eye kiteshield
                };
                drops.add(Utils.random(possibleDrops));

                //High tier Nice-to-haves (God mbox) - %.6 chance to hit table [3/500 or 1/166.66 kills]
            } else if (tierRoll > .8200 && tierRoll <= .9200){
                possibleDrops = new Drop[]{
                        new Drop(21775, 1, 1),				// orb of arma
                        new Drop(22494, 1, 1),				// polypore staff
                        new Drop(49544, 1, 1),				// tormented bracelet
                        new Drop(24155, 5, 5)				// 5 Double spin tickets
                };
                drops.add(Utils.random(possibleDrops));

                //Very rare Nice-to-haves (Looter's necklace) - .24% chance to hit table [3/1250 or 1/416.66 kills]
            } else if (tierRoll > .9200 && tierRoll <= .9600){
                drops.add(new Drop(6890, 1, 1));		//mages' book

                //Legendary Nice-to-haves/First oof (Upgrade Gem) -.18% chance to hit table [9/5000 or 1/555.55 kills]
            } else if (tierRoll > .9600 && tierRoll <= .9900){
                drops.add(new Drop(6914, 1, 1));		//master wand
                //Utter OOF table - .06% chance to hit table [3/5000 or 1,666.66 kills]
            } else if (tierRoll > .9900 && tierRoll < .999999){
                /*possibleDrops = new Drop[]{
                        new Drop(1038, 1, 1),			//Red phat
                        new Drop(1040, 1, 1),			//Yellow phat
                        new Drop(1042, 1, 1),			//Blue phat
                        new Drop(1044, 1, 1),			//Green phat
                        new Drop(1046, 1, 1),			//Purple phat
                        new Drop(1048, 1, 1)			//White phat
                };
                drops.add(Utils.random(possibleDrops));*/
                drops.add(new Drop(25470, 1, 1));		//Looter's necklace
                //Good luck getting this [1,666,666 kills]
            } else {
                drops.add(new Drop(25699, 1, 1));		//Cataclysm staff
            }

            //UBER 1337 COIN TABLE - 3.2% chance to hit table [1/31]
        } else if (tableRoll > .9600 && tableRoll <= .9920) {
            //Low cash drop - 2.1% chance to hit table [1/47.6 kills]
            if (tierRoll <= .7000 ){
                drops.add(new Drop(995, 50000000, 75000000));		//50-75M coins

                //Mid tier cash drop - .45% chance to hit table [1/222.22 kills]
            } else if (tierRoll > .7000 && tierRoll <= .8500){
                drops.add(new Drop(995, 75000000, 125000000));		//75M-125M

                //High tier cash drop - .3% chance to hit table [1/333.33 kills]
            } else if (tierRoll > .8500 && tierRoll <= .9500){
                drops.add(new Drop(995, 125000000, 175000000));		//125M-175M

                //Highest tier cash drop - .06% chance to hit table [1/666.66 kills]
            } else {
                drops.add(new Drop(995, 175000000, 250000000));		//175M-250M
            }

            //Cataclysm robes table - .35% chance to hit table [1/250 kills, 1/1250 for an individual piece]
        } else if (tableRoll > .9920 && tableRoll <= .9955){
            possibleDrops = new Drop[]{
                    new Drop(44702, 1, 1),					//Catalyst Hat
                    new Drop(25695, 1, 1),					//Catalyst robe top
                    new Drop(25696, 1, 1),					//Catalyst robe bottom
                    new Drop(25697, 1, 1),					//Catalyst gloves
                    new Drop(25698, 1, 1)					//Catalyst boots
            };
            drops.add(Utils.random(possibleDrops));

            //Cataclystic sigil - .3% chance to hit table [1/333 kills]
        } else if (tableRoll > .9955 && tableRoll <= .9985) {
            drops.add(new Drop(25700, 1, 1));				//Cataclystic sigil

            //Catacylsm orb - .15% chance to hit table [1/666 kills]
        } else if (tableRoll > .9985){
            drops.add(new Drop(25701, 1, 1));				//Cataclysm orb
        }
        return drops;
    }



    public int snowScreenAttack() {
        forceTalk("I bring with me the power of Winter!");
        for(Entity entity : World.getNearbyPlayers(this, false)) {
            if(entity.isPlayer()) {
                Player p = entity.asPlayer();
                int fm = p.getSkills().getLevel(Skills.FIREMAKING);
                if(fm >= 80 && Utils.random(6) == 0 // 15%
                        || fm >= 90 && Utils.random(6) == 0) { // 30%
                    p.sendMessage("Your skilfully lit light source withstands Callus' attempt to extinguish it!");
                    continue;
                }
                LightSource.extinguishAll(entity.asPlayer());
                entity.asPlayer().sendMessage("<col=00ffff><shad=0>Callus' icy wind extinguishes your light source!");

                //p.getInterfaceManager().setOverlay(96, true);
                //p.getPackets().sendBlackOut(2);
            }
            entity.gfx(2009);
        }

        return 3;
    }

    public int iceballBarrageAttack() {
        ArrayList<Entity> targets = getPossibleTargets();

        if(targets.size() == 0) {
            return -1;
        }

        Collections.shuffle(targets);

        int attackDelay = 20;

        for(int i = 0; i < targets.size() / 3; i++) {
            Player target = targets.get(i).asPlayer();
            target.sendMessage("<col=00ffff><shad=0>Callus is firing a devastating attack at you!");

            final int projMS = World.sendProjectile(this, target, 6459, 45, 41, 12, 41, 16, this.getSize() * 32);
            final int fin = CombatScript.getDelay(projMS);

            attackDelay = fin;

            target.addFreezeDelay(projMS);
            CallusFrostborne callus = this;
            callus.anim(9967);


            WorldTasksManager.schedule(new WorldTask() {
                int cycle = 0;
                @Override
                public void run() {
                    if (callus.hasFinished() || !callus.withinDistance(target, 32)) {
                        stop();
                        return;
                    }
                    if(cycle++ == fin-1) {
                        List<Entity> playerList = World.getNearbyPlayers(target, false)
                                .stream().filter(entity -> Utils.isOnRange(target, entity, 2)).collect(Collectors.toList());
                        int playersInRange = playerList.size();
                        if(playersInRange > 0) {
                            int d = (1000 + Utils.random(250)) / playersInRange;
                            int damage = d < 200 ? 200 : d;
                            playerList.stream().forEach(player -> {
                                if(!player.isFrozen()) {
                                    player.applyHit(new Hit(null, damage, HitLook.MAGIC_DAMAGE));
                                    player.setNextGraphics(new Graphics(6460));
                                }
                            });
                        }
                        stop();
                    } else {
                        target.gfx(5365);
                    }
                }
            }, 0,0);
        }

        return attackDelay + 3;
    }


    public static WorldTile[] ARENA_CLEAR_TILES = {
        new WorldTile(2392, 4072, 0),
        new WorldTile(2392, 4069, 0),
        new WorldTile(2392, 4066, 0),
        new WorldTile(2396, 4062, 0),
        new WorldTile(2398, 4062, 0),
        new WorldTile(2401, 4062, 0),
        new WorldTile(2403, 4062, 0),
        new WorldTile(2407, 4066, 0),
        new WorldTile(2407, 4069, 0),
        new WorldTile(2407, 4072, 0)
    };

    public int arenaClearAttack() {
        CallusFrostborne callus = this;

        setNextAnimation(new Animation(9964));

        int delayMS = 10000;
        for(WorldTile tile : ARENA_CLEAR_TILES) {
            int projMS = World.sendProjectile(this, tile, 6459, 45, 5, 48, 41/3, 16, this.getSize() * 32);
            projMS = World.sendProjectile(this, tile, 6459, 45, 5, 36, 41/2, 16, this.getSize() * 32);
            projMS = World.sendProjectile(this, tile, 6459, 45, 5, 24, 41, 16, this.getSize() * 32);
            projMS = World.sendProjectile(this, tile, 6459, 45, 5, 12, 41, 16, this.getSize() * 32);
            if(projMS < delayMS) delayMS = projMS;
            //World.sendGraphics(callus, new Graphics(5365, 0, 0), tile);
        }
        final int fin = CombatScript.getDelay(delayMS);
        WorldTasksManager.schedule(new WorldTask() {
            int tick = 0;
            @Override
            public void run() {
                if (callus.isDead() || callus.hasFinished()) {
                    stop();
                    return;
                }

                if(tick++ < fin) {
                    return;
                }

                List<Entity> players = World.getNearbyPlayers(callus, false);
                for (WorldTile tile : ARENA_CLEAR_TILES) {
                    // World.spawnObjectTemporary(new WorldObject(132000, 10, 0, new WorldTile(tile)), 16000, true, false);
                    World.sendGraphics(callus, new Graphics(5365, 0, 0), tile);
                    players.stream().filter(player -> tile.withinDistance(player, 2)).forEach(
                            player -> {
                                // don't allow the arena clear to stack with other mechanics
                                if(!player.isFrozen()) {
                                    player.applyHit(new Hit(null, (300 + Utils.random(300)), HitLook.MAGIC_DAMAGE));
                                    player.setNextGraphics(new Graphics(6460));
                                }
                            }
                    );
                }
                stop();
            }
        }, 0, 0);

        return fin + 3;
    }


    @Override
    public void processNPC() {
        tick++;

        if (isDead())
            return;

        // if(getX() != getRespawnTile().getX() || getY() != getRespawnTile().getY()) {
            // setNextWorldTile(new WorldTile(getRespawnTile()));
        // }

        int targets = getPossibleTargets(true, false).size();
        if (minnions.size() == 0 && lastMinionSpawnHP - getHitpoints() > 1000 || Utils.random(200) == 0) {
            lastMinionSpawnHP = getHitpoints();
            if (Utils.random(6) == 1) {
                setNextForceTalk(new ForceTalk("Glacytes, dispense with these pests.."));
                // only spawn half minions during last phase
                final int SPAWN_COUNT = getId() == PHASE_4_NPC && getHitpoints() < 12500 ? (int) (targets * 0.50) : targets;
                for (int i = 0; i < SPAWN_COUNT; i++) {
                    tileLoop:
                    for (int tileAttempt = 0; tileAttempt < 10; tileAttempt++) {
                        WorldTile tile = new WorldTile(this, 6);
                        if (World.isTileFree(0, tile.getX(), tile.getY(), 1)) {
                            NPC minion = World.spawnNPC(14302 + Utils.random(3), tile, -1, true, true);
                            minion.setName("Callus' Glacyte");
                            minion.setForceTargetDistance(64);
                            minion.setForceAgressive(true);
                            minion.setForceMultiArea(true);
                            minion.setForceMultiAttacked(true);
                            minion.getCombat().setTarget(getCombat().getTarget());
                            minion.setHitpoints(minion.getMaxHitpoints());
                            registerMinnion(minion);
                            break tileLoop;
                        }
                    }
                }
            }
        }

        List<Entity> nearbyPlrs = World.getNearbyPlayers(this, false, 32);
        Collections.shuffle(nearbyPlrs);
        playersNearby = nearbyPlrs.size();
        prayerEnrageCount = 1.0;

        int playersPrayerDrained = 0;
        allPlayersInFinalArea = true;

        for (Entity p2 : nearbyPlrs) {
            if (p2 instanceof Player) {
                Player p = (Player) p2;
                if(p.hasTeleported() || p.getControlerManager().getControler() == null || !(p.getControlerManager().getControler() instanceof CallusController))
                    continue;

                // snow screen
                boolean hasLight = LightSource.hasLightSource(p);

                if (!hasLight) {
                    p.getPackets().sendBlackOut(2);
                    p.getInterfaceManager().setOverlay(hasLight ? 97 : 96, true);
                    p.applyHit(null, Utils.random(30, 60));
                    p.applyHit(null, Utils.random(30, 60));
                    p.sendMessage("<col=00ffff><shad=0>You are slowly freezing to death!");
                } else {
                    p.getPackets().sendBlackOut(0);
                    p.getInterfaceManager().removeOverlay(true);
                }

                int distanceToCallus = Utils.getDistance(p, this);
                if (distanceToCallus < 5) {
                    p.sendMessage("<col=00ffff><shad=0>You're too close to Callus, cold radiates off of him!");
                    p.applyHit(new Hit(null, 500 - (distanceToCallus * 100), HitLook.MAGIC_DAMAGE));
                    return;
                }

                if (p.getPrayer().isMageProtecting() || p.getPrayer().isMeleeProtecting() || p.getPrayer().isRangeProtecting())
                    prayerEnrageCount += 0.02;

                if (getId() == PHASE_2_NPC) {
                    if (tick % 4 == 0) {
                        if (playersPrayerDrained++ < 5 && p.getPrayer().getPrayerpoints() > 0) {
                            int drain = 50 + Utils.random(50);
                            p.getCombatDefinitions().desecreaseSpecialAttack(0);
                            drain = Math.min(p.getPrayer().getPrayerpoints(), drain);
                            p.getPrayer().drainPrayer(drain);
                            p.gfx(2222);
                            processHit(new Hit(this, drain / 2, Hit.HitLook.HEALED_DAMAGE));
                            p.sendMessage("<col=00ffff><shad=0>Callus drains your prayer, healing his health!");
                        }
                    }
                }
            }
        }

        // cap at 100% enrage dmg
        prayerEnrageCount = Math.min(2.0, prayerEnrageCount);

        if (getId() == PHASE_3_NPC) {
            setHitpoints(getMaxHitpoints() - (6250 * brazierLightCount));

            if (braziers.stream().filter(brazier -> brazier.isLit()).count() < 4) {
                World.getNearbyPlayers(this, false).forEach(entity -> entity.applyHit(null, Utils.random(10, 30)));
            }/* else {
                if(brazierLightCount < 4) {
                    if (explosionTicks-- <= 0) {
                        explosionTicks = 12;
                        extinguishBrazier();
                    } else if(explosionTicks == 11) {
                        yell("<col=00ffff><shad=0>Callus begins charging an attack...");
                        setNextSecondaryBar(new SecondaryBar(0, 350, 2, false));
                    }
                }
            }*/
        }

        if (minnions != null && minnions.size() > 0) {
            Iterator<NPC> it$ = minnions.iterator();
            while (it$.hasNext()) {
                NPC npc = it$.next();
                if (npc != null && npc.hasFinished())
                    it$.remove();
            }
        }

        if (getRespawnTile() != null) {
            int deltaX = getX() - getRespawnTile().getX();
            int deltaY = getY() - getRespawnTile().getY();
            if (deltaX < -30 || deltaX > 30 || deltaY < -30 || deltaY > 30) {
                setNextWorldTile(getRespawnTile().transform(0, 0, 0));
            }
        }

        if (!getCombat().process()) {
            checkAgressivity();
			/*if (!checkAgressivity() && phase != 0 && getHitpoints() >= getMaxHitpoints()) {
				phase = 0;
				setNextNPCTransformation(15186);
			}*/
        }

    }

    public void yell(String s) {
        for(Entity e : getPossibleTargets()) {
            if(e instanceof Player) {
                ((Player) e).sendMessage(s);
            }
        }
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkPlayers, boolean checkNpcs) {
        ArrayList<Entity> list = super.getPossibleTargets(true, true);
        for (NPC minnion : minnions)
            list.remove(minnion);
        return list;
    }

    @Override
    public void drop() {

        //super.drop();
    }

    public void callusDrops() {
        ArrayList<Drop> drops;
        Item item;
        int plrs = getReceivedDamageSources().size();
        Set<Entity> dmgDealers = getReceivedDamageSources();
        ArrayList<Player> topDamageDealers = new ArrayList<>();
        dmgDealers.stream().distinct().filter(Entity::isPlayer).forEach(entity -> topDamageDealers.add(entity.asPlayer()));

        int mvpsAmt = plrs < 4 ? 1 : plrs < 7 ? 2 : 3;

        Collections.sort(topDamageDealers, (o1, o2) -> o2.getCallusDropWeight() - o1.getCallusDropWeight());
        int topMVPDamage = topDamageDealers.get(0).getCallusDropWeight();

        for (int i = 0; i < topDamageDealers.size(); i++) {
            Player player = topDamageDealers.get(i);
            int damage = getDamageReceived(player);

            if (damage >= 500) {
                if (!player.withinDistance(this, 64)) {
                    player.sendMessage("Your reward sinks into the snow and ice; you were too far away to collect it.");
                    continue;
                }

                boolean isMvp = i < mvpsAmt;

                drops = dropItems(isMvp, player == topDamageDealers.get(0));

                if(isMvp) {
                    player.sendMessage("<col=00ffff><shad=0>The gods notice your efforts bringing down Callus! (<col=ffffff>" + Utils.getFormattedNumber(player.getCallusDropWeight()) + "<col=00ffff> damage)");
                    player.resetCallusDropWeight();
                } else
                    player.sendMessage("<col=00ffff><shad=0>Callus damage stored: " + Utils.getFormattedNumber(player.getCallusDropWeight()) + " - MVP: " + topDamageDealers.get(0).getDisplayName() + " (<col=ffffff>" + Utils.getFormattedNumber(topMVPDamage) + " <col=00ffff>damage)");

                drops.add(0, new Drop(21776, 1, 3)); //Armadyl shard(s) [1-3 shards]
                drops.add(0, new Drop(21773, 10, 33));//Armadyl runes [10-30]

                drops.add(0, new Drop(23352, 5, 25));//flasks
                drops.add(0, new Drop(23400, 3, 10));//flasks
                LuckyPets.checkBossPet(player, this);

                for(Drop d : drops) {
                    item = new Item(d.getItemId(), Utils.random(d.getMinAmount(), d.getMaxAmount()));
                    if(d.getItemId() != 21776 && d.getItemId() != 21773 && d.getItemId() != 23352 && d.getItemId() != 23400)
                        yell("<col=00ffff><shad=0>" + player.getDisplayName() + " received drop: <col=ff981f>" + Utils.getFormattedNumber(item.getAmount()) + " <col=00ffff>x <col=ff981f>" + item.getName());
                    /*if(GrandExchange.getPrice(item.getId()) > 10_000_000) {
                        World.sendNews(player, "World boss dropped " + item.getAmount() + " x " + item.getName(), 1);
                        player.setLootbeam(World.addGroundItem(item, new WorldTile(getCoordFaceX(1), getCoordFaceY(1), getPlane()), player, true, 60));
                    } else {
                        World.addGroundItem(item, new WorldTile(getCoordFaceX(1), getCoordFaceY(1), getPlane()), player, true, 60);
                    }*/
                    // create new drop so amount is consistent wiht above message
                    sendDrop(player,new Drop(item.getId(), item.getAmount(), item.getAmount()));
               }
            } else {
                player.sendMessage("<col=00ffff><shad=0>You did not deal enough damage to Callus to be worthy of a reward.");
            }
        }
    }

    @Override
    public List<Player> getForceLootSharingPeople() {
        List<Player> players = super.getForceLootSharingPeople();
        for (Player player : players.toArray(new Player[players.size()]))
            if (getDamageReceived(player) < 500)
                players.remove(player);
        return players;
    }

    @Override
    public Item sendDrop(Player player, Drop drop) {
        return super.sendDrop(player, drop);
    }

    public int brazierLightCount = 0;

    public void extinguishBrazier() {
        gfx(2600);
        yell("<col=ff0000>The heat from the Braziers damages Callus' unstable form!");
        brazierLightCount++;
        //processHit(new Hit(this, 6250, HitLook.REGULAR_DAMAGE));
        setHitpoints(getMaxHitpoints() - (6250 * brazierLightCount));
        WorldTasksManager.schedule(() -> {
            forceTalk("ARRGHH!");
            anim(9955);
        });
        setNextSecondaryBar(new SecondaryBar(0, 350, 2, false));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                braziers.forEach(Brazier::extinguish);
                forceTalk("You will regret meddling in my plane!");
                getPossibleTargets().forEach(entity -> {
                    entity.asPlayer().sendMessage("<col=00ffff><shad=0>The force of Callus' explosion has extinguished the braziers. Light them before you freeze to death!");
                    applyHit(new Hit(null, 250, HitLook.REGULAR_DAMAGE));
                });
            }
        }, 3);
    }

    @Override
    public void sendDeath(final Entity source) {
        endFight();

        resetWalkSteps();
        getCombat().removeTarget();
        setNextAnimation(null);
        setNextGraphics(new Graphics(1780));

        lastMinionSpawnHP = getMaxHitpoints();

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                setNextGraphics(new Graphics(1780));
                if (getId() == PHASE_1_NPC) { // enduring -> sapping
                    setHitpoints(getMaxHitpoints());
                    setNextNPCTransformation(PHASE_2_NPC);
                    setNextForceTalk(new ForceTalk("Your life will be mine.."));
                    for (Entity trg : getPossibleTargets(true, true)) {
                        if (trg == CallusFrostborne.this || !trg.withinDistance(CallusFrostborne.this, 3))
                            continue;
                        trg.applyHit(new Hit(null, Utils.random(50) + 100, HitLook.DESEASE_DAMAGE));
                    }
                } else if (getId() == PHASE_2_NPC) { // sapping -> unstable
                    //setHealRestoreRate(0);
                    setHitpoints(getMaxHitpoints());
                    setNextNPCTransformation(PHASE_3_NPC);

                    braziers.forEach(Brazier::extinguish);
                    forceTalk("You will regret meddling in my plane!");
                    yell("<col=00ffff><shad=0>The force of Callus' explosion has extinguished the braziers. Light them before you freeze to death!");
                    for (Entity trg : getPossibleTargets(true, true)) {
                        if (trg == CallusFrostborne.this || !trg.withinDistance(CallusFrostborne.this, 3))
                            continue;
                        trg.applyHit(new Hit(null, Utils.getRandom(200) + 100, HitLook.DESEASE_DAMAGE));
                    }
                } else if (getId() == PHASE_3_NPC) { // unstable -> undying
                    brazierLightCount = 0;
                    //setHealRestoreRate(10);
                    setHitpoints(getMaxHitpoints());
                    setNextNPCTransformation(PHASE_4_NPC);
                    setNextForceTalk(new ForceTalk("You will be destroyed!"));
                    setName("Callus Frostborne (Undying)");
                    for (Entity trg : getPossibleTargets(true, true)) {
                        if (trg == CallusFrostborne.this || !trg.withinDistance(CallusFrostborne.this, 3))
                            continue;
                        trg.applyHit(new Hit(null, Utils.getRandom(600) + 100, HitLook.DESEASE_DAMAGE));
                    }
                } else if (getId() == PHASE_4_NPC) {
                    setName("Callus Frostborne");
                    setNextForceTalk(new ForceTalk("No! This cannot be my fate!"));
                    for (Entity trg : getPossibleTargets(true, true)) {
                        if (trg == CallusFrostborne.this || !trg.withinDistance(CallusFrostborne.this, 3))
                            continue;
                        trg.applyHit(new Hit(null, Utils.getRandom(800) + 100, HitLook.DESEASE_DAMAGE));
                    }

                    callusDrops();
                    Player killer = getMostDamageReceivedSourcePlayer();
                    if (killer != null) {
                        NPCKillLog.addKill(killer, "Callus");
                        resetReceivedDamage();
                    }
                    World.sendNews("Callus Frostborne has been slain!", World.WORLD_NEWS);
                    CallusFrostborne.super.sendDeath(source);
                    return;
                }
            }
        }, 3);
    }

    public void registerMinnion(NPC minnion) {
        minnions.add(minnion);
    }

    public List<NPC> getMinnions() {
        return minnions;
    }


    @Override
    public int getMaxHitpoints() {
        int hp = super.getMaxHitpoints();

        if (getRegionId() != 9551)
            return hp;

        double pc = World.getPlayerCount();
        double mult = Math.max(1, /*((pc - 50d)*/pc / 50d);//every player above 100 increase 1%
        return (int) (hp * mult);
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if(getId() == PHASE_3_NPC) {
            if(hit.getSource().isPlayer()) {
                hit.getSource().asPlayer().sendMessage("<col=00ffff><shad=0>Callus is not fazed by your attacks!");
            }
            hit.setDamage(0);
        }

        if (minnions.size() > 0) {
            hit.setHealHit();
            super.handleIngoingHit(hit);
            return;
        }

        if (hit.getLook() != HitLook.MAGIC_DAMAGE && hit.getLook() != HitLook.HEALED_DAMAGE) {
            hit.setDamage(0);
            if (hit.getSource() instanceof Player) {
                hit.getSource().asPlayer().sendMessage("<col=00ffff><shad=0>Your attacks have no effect on Callus!");
                //hit.getSource().asPlayer().sendMessage("<col=00ffff>Bad hit="+ hit.getDamage() + " " + hit.getLook().name() + " ");
            }
        } else {

            if (hit.getSource() instanceof Player) {
                Player player = (Player) hit.getSource();
                int spell = (int) player.getTemporaryAttributtes().getOrDefault("lastSpellCast", -1);
                player.getTemporaryAttributtes().remove("lastSpellCast");
                boolean fireSpell = spell == 45 || spell == 63 || spell == 80 || spell == 91;
                if (!fireSpell) {
                    hit.setDamage((int) (hit.getDamage() * 0.1));
                } else {
                    int dist = Utils.getDistance(player, this);
                    if(dist > 8) {
                        player.sendMessage("<col=00ffff><shad=0>The strength of your fire attack weakens as it travels through Callus' frigid domain.");
                        player.sendMessage("<col=00ffff><shad=0>You need to be closer!");
                        hit.setDamage((int) (hit.getDamage() * 0.1));
                    } else {
                        hit.setDamage((int) (hit.getDamage() * 1.4));
                    }
                }
                // damage reduc mechanic: reduce damage every 5 players by 5% up to 20
                double reduc = playersNearby >= 20 ? 0.8 : playersNearby >= 15 ? 0.85
                        : playersNearby >= 10 ? 0.9 : playersNearby >= 5 ? 0.95 : 1;
                hit.setDamage((int) (hit.getDamage() * reduc));
            }
        }

        if(getId() == PHASE_4_NPC) {
            if(getHitpoints() <= 12500 && !inFinalArea(hit.getSource()) && hit.getLook() != HitLook.HEALED_DAMAGE) {
                hit.setDamage(0);
                if(hit.getSource().isPlayer())
                    hit.getSource().asPlayer().sendMessage("<col=00ffff><shad=0>Callus is invulnerable from this angle!");
            }
        }

        if(hit.getSource().isPlayer() && hit.getDamage() > 0 && hit.getLook() != HitLook.HEALED_DAMAGE) {
            hit.getSource().asPlayer().increaseCallusDropWeight(hit.getDamage());
        }
        super.handleIngoingHit(hit);
    }

    @Override
    public double getMagePrayerMultiplier() {
        return getId() == PHASE_3_NPC || getId() == PHASE_4_NPC ? 5 : 2.5;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return getId() == PHASE_3_NPC || getId() == PHASE_4_NPC ? 5 : 2.5;
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return getId() == PHASE_3_NPC || getId() == PHASE_4_NPC ? 5 : 2.5;
    }

    public int standardAttack(CombatScript script) {
        boolean buff = getId() == PHASE_3_NPC || getId() == PHASE_4_NPC;

        NPCCombatDefinitions defs = getCombatDefinitions();

        CallusFrostborne callus = this;

        List<Entity> targets = getPossibleTargets(true, true);

        if (Utils.random(4) == 0)
            setRangeAttack(!isRangeAttack());
        if (getEffect() == 1)
            targets.stream().filter(Entity::isPlayer).forEach(entity -> entity.asPlayer().getPrayer().drainPrayer(10));

        switch (Utils.getRandom(5)) {
            case 0:
            case 1:
            case 2:
                targets.stream().filter(Entity::isPlayer).forEach(entity ->
                        sendDistancedAttack(this, entity, script));
                break;
            case 3:
                targets.stream().forEach(e -> {
                    if (e != this) {
                        if (Utils.isOnRange(getX(), getY(), getSize(), e.getX(), e.getY(), e.getSize(), 0)) {
                            setNextAnimation(new Animation(9955));
                            int dmg = script.getRandomMaxHit(this, 250, NPCCombatDefinitions.MELEE, e);
                            if(buff) dmg *= 1.25;
                            script.delayHit(this, 0, e, script.getMeleeHit(this, dmg));
                            if(getId() == PHASE_2_NPC)
                                processHit(new Hit(callus, (int) (dmg * (Utils.random(0.02, 0.05))), Hit.HitLook.HEALED_DAMAGE));
                        } else
                            sendDistancedAttack(this, e, script);
                    }
                });
                break;

            case 4:
                targets.stream().forEach(e -> {
                    final WorldTile tile = new WorldTile(e);
                    setNextAnimation(new Animation(9955));
                    World.sendProjectile(this, tile, 2314, 50, 0, 46, 20, 0, 10);
                    setRangeAttack(true);
                    WorldTasksManager.schedule(new WorldTask() {

                        Entity player = e;
                        @Override
                        public void run() {
                            //for (Entity e : getPossibleTargets()) {
                                //if (e instanceof Player) {
                                    //Player player = (Player) e;
                                    int dmg = player.getHitpoints() / 3;
                                    if (player.withinDistance(tile, 0))
                                        player.applyHit(new Hit(e, dmg, Hit.HitLook.RANGE_DAMAGE));
                                    World.sendGraphics(callus, new Graphics(2315), tile);

                            //if(getId() == PHASE_2_NPC)
                                //processHit(new Hit(null, (int) (dmg * (Utils.random(0.02, 0.05))), Hit.HitLook.HEALED_DAMAGE));
                                //}
                            //}
                        }
                    }, 3);
                });
                break;
        }

        return defs.getAttackDelay() + 2;
    }

    private void sendDistancedAttack(CallusFrostborne npc, final Entity target, CombatScript script) {
        boolean buff = getId() == PHASE_3_NPC || getId() == PHASE_4_NPC;
        boolean isRangedAttack = npc.isRangeAttack();
        if (isRangedAttack) {
            int dmg = script.getRandomMaxHit(npc, 220, NPCCombatDefinitions.RANGE, target);
            if(buff) dmg *= 1.25;
            script.delayHit(npc, 2, target, script.getRangeHit(npc, dmg));
            World.sendProjectile(npc, target, 962, 50, 30, 46, 30, 0, 10);
            if(getId() == PHASE_2_NPC)
                processHit(new Hit(npc, (int) (dmg * (Utils.random(0.02, 0.05))), Hit.HitLook.HEALED_DAMAGE));
        } else {
            int dmg = script.getRandomMaxHit(npc, 200, NPCCombatDefinitions.MAGE, target);
            if(buff) dmg *= 1.25;
            script.delayHit(npc, 2, target, script.getMagicHit(npc, dmg));
            World.sendProjectile(npc, target, 634, 50, 30, 46, 30, 5, 10);
            if(getId() == PHASE_2_NPC)
                processHit(new Hit(npc, (int) (dmg * (Utils.random(0.02, 0.05))), Hit.HitLook.HEALED_DAMAGE));
            if (Utils.random(5) == 0) {
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        target.setNextGraphics(new Graphics(369));
                        target.addFreezeDelay(7000);
                    }
                });
            }
        }
        npc.setNextAnimation(new Animation(isRangedAttack ? 9968 : 9967));
    }

    public void endFight() {
        for(NPC npc : minnions)
            npc.finish();
        minnions.clear();
    }
}
