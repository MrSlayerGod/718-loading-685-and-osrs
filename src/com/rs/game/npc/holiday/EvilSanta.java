package com.rs.game.npc.holiday;

import com.rs.Settings;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.box.ChristmasBox;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.seasonalEvents.XmasBoss;
import com.rs.game.player.controllers.TheHorde;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.NPCHandler;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Bounds;
import com.rs.utils.Colour;
import com.rs.utils.Direction;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * @since Dec 18, 2020
 */
public class EvilSanta extends NPC {
    public static final Bounds ICE_ARENA_BOUNDS = new Bounds(2722, 5729, 2735, 5742, 0);
    public static final Bounds GIFT_BOUNDS = new Bounds(2722+2, 5729+2, 2735-2, 5742-2, 0);
    public static final Bounds DROP_BOUNDS = new Bounds(2706, 5711, 2735, 5723, 0);

    private static final Projectile PROJECTILE = new Projectile(1547, 85, 31, 51, 56, 16, 64);
    private static final int PLAYERS_REQUIRED = 1;

    private boolean activated = false, fightOver = false;
    private int attackRotation = 0;
    private Specs lastSpec = null;

    public boolean isFightOver() {
        return fightOver;
    }

    private enum Specs {
        FLAMES, PRESENTS, IMPS
    }

    public EvilSanta(WorldTile tile) {
        super(1552, tile, -1, true, true);
        setCombat();
        setForceMultiAttacked(true);
        setForceMultiArea(true);
        setForceAgressive(true);
        setCantFollowUnderCombat(true);
        setCapDamage(1000);
        setLureDelay(0);
        lock();
    }

    public static Item[] OVERALL_REWARDS = {
        new Item(200, 100),
        new Item(202, 100),
        new Item(204, 100),
        new Item(206, 100),
        new Item(208, 100),
        new Item(210, 100),
        new Item(212, 100),
        new Item(214, 100),
        new Item(216, 100),
        new Item(218, 100),
        new Item(220, 100),
        new Item(232, 100),
        new Item(224, 100),
        new Item(226, 100),
        new Item(1120, 100),
        new Item(5973, 100),
        new Item(10819, 100),
        new Item(2, 1000),
        new Item(990, 3),
        new Item(25447, 3000),
        new Item(1516, 200),
        new Item(1514, 100),
        new Item(1392, 50),
        new Item(1754, 50),
        new Item(1752, 35),
        new Item(8779, 250),
        new Item(8783, 150),
        new Item(240, 100),
        new Item(2360, 150),
        new Item(2362, 100),
        new Item(2364, 50),
        new Item(5315, 10),
        new Item(5314, 15),
        new Item(5316, 5),
        new Item(441, 350),
        new Item(443, 250),
        new Item(445, 200),
        new Item(12158, 100),
        new Item(12159, 100),
        new Item(12160, 100),
        new Item(12163, 100),
        new Item(383, 250),
        new Item(535, 200),
        new Item(537, 100),
        new Item(43439, 100),
        new Item(995, 3000000)
    };

    private static final Item[] PVM_RARE = {
        new Item(25545, 1),
        new Item(25546, 1),
        new Item(25547, 1),
        new Item(25548, 1),
        new Item(25549, 1),
        new Item(25550, 1),
        new Item(25551, 1),
        new Item(25552, 1)
    };

    private static final Item[] PARTICIPATION_REWARDS = {
        new Item(4708, 1),
        new Item(4710, 1),
        new Item(4712, 1),
        new Item(4714, 1),
        new Item(4716, 1),
        new Item(4718, 1),
        new Item(4720, 1),
        new Item(4722, 1),
        new Item(4724, 1),
        new Item(4726, 1),
        new Item(4728, 1),
        new Item(4730, 1),
        new Item(4732, 1),
        new Item(4734, 1),
        new Item(4736, 1),
        new Item(4738, 1),
        new Item(4745, 1),
        new Item(4747, 1),
        new Item(4749, 1),
        new Item(4751, 1),
        new Item(4753, 1),
        new Item(4755, 1),
        new Item(4757, 1),
        new Item(4759, 1),
        new Item(21736, 1),
        new Item(21744, 1),
        new Item(21752, 1),
        new Item(21760, 1),
        new Item(6735, 1),
        new Item(6733, 1),
        new Item(6731, 1),
        new Item(6737, 1),
        new Item(1050, 1),
        new Item(3140, 1),
        new Item(4087, 1),
        new Item(11732, 1),
        new Item(15426, 1),
        new Item(6856, 1),
        new Item(6857, 1),
        new Item(6858, 1),
        new Item(6859, 1),
        new Item(6860, 1),
        new Item(6861, 1),
        new Item(6862, 1),
        new Item(6863, 1),
        new Item(43307, 20000)
    };

    public static void init() {

        NPCHandler.register(1552, 1, (player, npc) -> {
            int plrs = npc.getAllTargets().size();
            if(XmasBoss.getSanta().fightOver) {
                player.getDialogueManager().startDialogue("NPCMessage", 1552, Dialogue.HAPPY,
                       "Christmas is saved!");
            } else {
                if(plrs < PLAYERS_REQUIRED) {
                    player.getDialogueManager().startDialogue("NPCMessage", 1552, Dialogue.MOCK,
                            "Bring some more friends here.. I've got a.. heh.. " + Colour.RED.wrap("surprise") + ".. for you...");
                } else {
                    player.getDialogueManager().startDialogue("NPCMessage", 1552, Dialogue.LAUGHING,
                            Colour.RED.wrap("Hahahahaha!"));
                }
            }
        });

        ObjectHandler.register(new int[] {56933}, 1, (player, obj) -> {
            Magic.sendCommandTeleportSpell(player, new WorldTile(2728, 5730, 0));
        });

        ObjectHandler.register(new int[] {47758, 65819, 66005}, 1, (player, obj) -> {
            EvilSanta santa = XmasBoss.getSanta();
            if(santa != null)
                santa.openPresent(player, obj);
        });
    }

    public static void douseFire(Player player, WorldObject object) {
        if(player.getEquipment().getWeaponId() != 10501) {
            player.sendMessage("You need to throw a snowball at the flame to douse it.");
            return;
        }
        player.stopAll();
        player.faceObject(object);
        player.getEquipment().removeAmmo(10501, -1);
        int delay = CombatScript.getDelay(World.sendProjectile(player, object, 1209, 32, 20, 40, 50, 16, 0));
        player.setNextAnimation(new Animation(7530));
        //player.lock(2);
        WorldTasksManager.schedule(() -> {
            object.remove();
        }, delay);
    }

    public static void douseImp(Player player, NPC imp) {
        if(player.getEquipment().getWeaponId() != 10501) {
            player.sendMessage("You need to throw a snowball at the flame to douse it.");
            return;
        }
        player.stopAll();
        player.setNextFaceWorldTile(imp);
        player.getEquipment().removeAmmo(10501, -1);
        int delay = CombatScript.getDelay(World.sendProjectile(player, imp, 1209, 32, 20, 40, 50, 16, 0));
        player.setNextAnimation(new Animation(7530));
        //player.lock(2);
        WorldTasksManager.schedule(() -> {
            imp.applyHit(player, 10 + (imp.getMaxHitpoints() / 2));
        }, delay);
    }

    @Override
    public boolean preAttackCheck(Player attacker) {
        if(!ICE_ARENA_BOUNDS.inBounds(attacker.clone())) {
            attacker.sendMessage("You must be inside the arena to damage Infernal Santa!");
            attacker.resetCombat();
            attacker.resetWalkSteps();
            return false;
        }
        return super.preAttackCheck(attacker);
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        if(fightOver) {
            hit.setDamage(0);
        }
        if(!imps.isEmpty()) {
            if(hit.getSource() != null && hit.getSource().isPlayer()) {
                hit.getSource().asPlayer().sendMessage("The imps are protecting Infernal Santa!");
                hit.setHealHit();
            }
        }
        super.handleIngoingHit(hit);
    }

    @Override
    public void processNPC() {
        List<Entity> targets = getAllTargets();
        if(!activated) {
            if(targets.stream().filter(entity -> entity.distance(this) < 8).count() >= PLAYERS_REQUIRED) {
                activated = true;
                lock();
                startTransformation();
            }
        }

        if(getId() == 1552) {
            return;
        }

        super.processNPC();
    }

    private void startTransformation() {

        WorldTasksManager.scheduleRevolving(event -> {
            anim(6601);
            forceTalk("Aghh..");
            event.add(() -> {
                forceTalk("Something.. is..");
            });
            event.add(() -> {
                forceTalk(".. Wrong");
            });
            event.delay(1);
            event.add(() -> {
                gfx(2325);
                anim(7312);
                setNextNPCTransformation(1553);
            });
            event.add(() -> {
                WorldTasksManager.schedule(() ->
                    anim(15535));
                forceTalk("Haahahahaha!");
            });
            event.delay(1);
            event.add(() -> {
                forceTalk("Come and get your gift!");
            });
            event.add(() -> {
                unlock();
            });
        });

        // collect positions for flames
        flameBoxTiles = this.clone().area(2);
    }

    private List<WorldTile> flameBoxTiles;

    private void flameAttack() {
        gfx(2754);
        anim(17808);
        forceTalk("Flalala-lalala... FLAMES!");
        WorldTasksManager.schedule(() -> anim(1979));

        WorldTasksManager.schedule(() -> {
            flameBoxTiles.stream().forEach(tile ->
                    igniteTile(tile));

            for(Entity p : getAllTargets())
                if(World.isTileFree(p, 1)) // might have a flame already
                    igniteTile(p.clone());

            for (int i = 0; i < 12; i++) {
                igniteTile(ICE_ARENA_BOUNDS.randomPosition());
            }
        }, 2);
    }

    private void igniteTile(WorldTile tile) {
        if(Utils.rollDie(4, 1)) {
            // 25% chance to skip ignite
            World.sendGraphics(5086, tile);
            return;
        }
        if(!World.isTileFree(tile, 1))
            return;

        //World.sendGraphics(369, tile); // 66132
        WorldObject object = new WorldObject(Settings.OSRS_OBJECTS_OFFSET + 32297, 10, Utils.random(0, 3), tile);
        World.spawnObject(object);
        World.unclipTile(object);
        WorldTasksManager.schedule(new WorldTask() {
            int tick = 0;
            @Override
            public void run() {
                if(tick++ >= 15) {
                    stop();
                    object.remove();
                    return;
                }
                if(tick %2 == 0)
                    return;
                getAllTargets().stream().filter(entity -> entity.matches(tile)).forEach(entity -> {
                    if(!object.isRemoved()) {
                        int dmg = Utils.random(50, 250);
                        entity.applyHit(EvilSanta.this, dmg);
                        EvilSanta.this.applyHit(EvilSanta.this, dmg, Hit.HitLook.HEALED_DAMAGE);
                        if (entity.isPlayer()) {
                            entity.asPlayer().sendMessage("<col=800000>Infernal santa is sapping your health!");
                        }
                    }
                });
            }
        }, 0, 0);
    }

    private void setCombat() {
        setCustomCombatScript(new CombatScript() {
            @Override public Object[] getKeys() { return new Object[0]; }

            /**
             * combat container for timer / agro
             */
            @Override
            public int attack(NPC npc, Entity target) {
                if(isLocked())
                    return 0;
                EvilSanta.this.attack();
                return 5;
            }
        });
    }

    private void attack() {
        // only use basica attacks while imps are up
        if(imps.size() > 0) {
            basicAttack();
            return;
        }

        // use a special every 3 attacks
        if(attackRotation++ % 3 == 0) {
            // collect a list of special attacks, remove last used spec
            List<Specs> specList = Arrays.stream(Specs.values()).filter(s -> s != lastSpec).collect(Collectors.toList());

            // pick random spec
            Specs spec = Utils.get(specList);

            // always start with flame attack
            if(lastSpec == null) {
                spec = Specs.FLAMES;
            }

            switch(spec) {
                default:
                case FLAMES:
                    flameAttack();
                    break;
                case PRESENTS:
                    rainPresents();
                    break;
                case IMPS:
                    impAttack();
                    break;
            }

            lastSpec = spec;
        } else {
            basicAttack();
        }

    }

    private void basicAttack() {
        List<Entity> targets = getAllTargets();
        if(targets.size() == 0)
            return;

        anim(8767);
        setNextGraphics(new Graphics(1546, 0, 160));
        setNextFaceWorldTile(Utils.get(targets));
        targets.stream().forEach(target -> {
            int delay = CombatScript.getDelay(PROJECTILE.fire(this, target));
            int maxDamage = 300;
            if (target.isPlayer() && target.asPlayer().getPrayer().isMageProtecting())
                maxDamage /= 2;
            int damage = CombatScript.getRandomMaxHit(this, maxDamage, NPCCombatDefinitions.MAGE, target);
            WorldTasksManager.schedule(() -> {
                target.setNextGraphics(new Graphics(1548, 0, 124));
                target.applyHit(this, damage, Hit.HitLook.MAGIC_DAMAGE);
            }, delay);
        });
    }

    private ArrayList<NPC> imps = new ArrayList<>();

    private WorldObject createSpawner(WorldTile tile) {
        return new WorldObject(66132, 10, Utils.random(0, 3), tile);
    }

    @Override
    public void sendDeath(Entity source) {
        forceTalk("This is not the last you've seen of me!");
        gfx(2325);

        for(NPC npc : imps)
            npc.finish();

        fightOver = true;

        WorldTasksManager.schedule(() -> {

            anim(2755);
            WorldTasksManager.schedule(() -> {
                anim(7312);
                setNextNPCTransformation(1552);
                setHitpoints(getMaxHitpoints());

                WorldTasksManager.schedule(() -> {
                    drop();
                }, 2);
            }, 1);
        }, 2);
    }

    private HashMap<WorldObject, Player> damagePlayerPresents = new HashMap<>();
    private static final int[] PRESENT_IDS = {47758, 65819, 66005};

    @Override
    public void drop() {
        forceTalk("You've saved Christmas!");
        getReceivedDamageSources().stream().distinct().filter(Entity::isPlayer).forEach(
                entity -> {
                    int damage = getDamageReceived(entity);

                    if(damage > 1000) {
                        // normal reward
                        if (((Player) entity).getControlerManager().getControler() instanceof TheHorde && getReceivedDamageSources().size() == 1)
                            return;
                        Player player = (Player) entity;
                        LuckyPets.checkPet(player, LuckyPets.LuckyPet.XMAS_2020);

                        if (!player.withinDistance(this))
                            return;

                        if (Utils.random(180) == 1) {
                            Item drop = PVM_RARE[Utils.random(PVM_RARE.length)];
                            World.sendNews(player, "<col=ff0000>RARE! <col=00ff00>"+Utils.formatPlayerNameForDisplay(player.getDisplayName())
                                    + " <col=ff0000>just received <img=11><col=00ff00>" +drop.getName()+" <col=ff0000>from Infernal santa!", 0);
                            player.getInventory().addItemDrop(drop.getId(), drop.getAmount());
                        }

                        Item reward = OVERALL_REWARDS[Utils.random(OVERALL_REWARDS.length)];

                        if (NPC.announceDrop(new Drop(reward.getId(), reward.getAmount(), reward.getAmount()))) {
                            World.sendNews(player, "<col=ff0000>RARE! <col=00ff00>"+Utils.formatPlayerNameForDisplay(player.getDisplayName())
                                    + " <col=ff0000>just received <img=11><col=00ff00>" +reward.getName()+" <col=ff0000>from Infernal santa!", 0);
                            player.getInventory().addItemDrop(reward.getId(), reward.getAmount());
                        }

                        player.getInventory().addItemDrop(reward.getId(), reward.getAmount());
                        player.getPackets().sendGameMessage("You receive a reward for your participation in the Christmas boss event.");
                    }

                    // zio's 2nd participation reward
                    WorldObject present = new WorldObject(Utils.random(PRESENT_IDS), 10, 0, DROP_BOUNDS.randomPosition());
                    int fs = 20;
                    while(!World.isTileFree(present, 1) && fs-- > 0)
                        present.setLocation(DROP_BOUNDS.randomPosition());
                    int delay = CombatScript.getDelay(PROJECTILE.fire(this, present));
                    damagePlayerPresents.put(present, entity.asPlayer());
                    WorldTasksManager.schedule(() -> {
                        World.sendGraphics(this, new Graphics(1548, 0, 20), present);
                        WorldTasksManager.schedule(()-> {
                            World.spawnObject(present);
                        });
                    }, delay);
                } );
    }


    private void openPresent(Player player, WorldObject obj) {
        if(damagePlayerPresents.containsKey(obj)) {
            Player to = damagePlayerPresents.get(obj);
            if(to == null || to == player || to.hasFinished()) {
                if(Utils.random(150) == 1) {
                    World.sendNews( "<col=" + Colour.ORANGE.hex + "><shad=ff0000>" + player.getName() + " has found an Infernal dye inside a Christmas present!", 0);
                    player.getInventory().addItemDrop(ChristmasBox.DYE, 1);
                }
                if(Utils.random(150) == 1) {
                    World.sendNews( "<col=" + Colour.ORANGE_RED.hex + "><shad=ff0000>" + player.getName() + " has found an Infernal cracker inside a Christmas present!", 0);
                    player.getInventory().addItemDrop(ChristmasBox.CRACKER, 1);
                }
                player.sendMessage(Colour.ORANGE_RED.wrap("Merry Christmas! Thank you for participating in Gallifrey's Christmas event."));
                player.getInventory().addItem(Utils.get(PARTICIPATION_REWARDS).clone());

                // always 3m coins
                player.getInventory().addItemDrop(995, 3000000);

                damagePlayerPresents.remove(player);
                obj.remove();
            } else {
                player.sendMessage("This present is for " + to.getDisplayName() + "!");
            }
        } else {
            obj.remove();
            player.sendMessage("The gift was empty!"); // shouldn't happen
        }
    }
    private void impAttack() {
        anim(6298);

        WorldObject[] spawners = new WorldObject[] {
                createSpawner(clone().relative(2, 0)),
                createSpawner(clone().relative(-2, 0)),
                createSpawner(clone().relative(0, 2)),
                createSpawner(clone().relative(0, -2))
        };

        Arrays.stream(spawners).forEach(object -> World.spawnObjectTemporary(object, 10000));

        NPC imp;

        for(Entity entity : getAllTargets()) {
            imp = spawnImp(entity, spawners);
            if(imp != null)
                imps.add(imp);
        }

        for (int i = 0; i < 3; i++) {
            imp = spawnImp(null, spawners);
            if(imp != null)
                imps.add(imp);
        }
    }

    private NPC spawnImp(Entity e, WorldObject[] spawners) {
        WorldObject spawner = Utils.random(spawners);
        if(e == null) {
            List<Entity> targets = getAllTargets();
            if(targets.size() == 0)
                return null;
            e = Utils.get(targets);
        }
        final Entity target = e;

        NPC imp = new NPC(8536, spawner.clone(), -1, true, true) {

            int tick = 0;

            Entity entity = target;

            @Override
            public void sendDeath(Entity source) {
                EvilSanta.this.imps.remove(this);
                if(EvilSanta.this.imps.isEmpty()) {
                    Arrays.stream(spawners).forEach(object -> object.remove());
                }
                super.sendDeath(source);
            }

            @Override
            public void processNPC() {
                if(this.isDead() || this.hasFinished() || tick ++ < 5) { // stand still for a few ticks
                    return;
                }

                if(Utils.rollDie(6, 1)) {
                    forceTalk("Hehehe");
                }

                if(entity.isDead() || entity.hasFinished() || entity.distance(this) > 10) {
                    List<Entity> targs = EvilSanta.this.getAllTargets();
                    if(targs.size() != 0)
                        entity = Utils.get(targs);
                    else {
                        resetCombat();
                        return;
                    }
                } else {
                    this.setTarget(entity);
                }

                // luring outside of arena
                if(!ICE_ARENA_BOUNDS.inBounds(entity) && !entity.isTeleporting()) {
                    entity.resetWalkSteps();
                    entity.setNextWorldTile(this.clone());
                    entity.gfx(Settings.OSRS_GFX_OFFSET + 1039);
                    if(entity.isPlayer()) {
                        entity.setTeleporting(1);
                        entity.asPlayer().sendMessage("The imp pulls you inside the arena!");
                    }
                }

                if(Utils.isOnRange(this, entity, 0)) {
                    anim(169);
                    entity.applyHit(this, 10);
                    //EvilSanta.this.applyHit(this, 30, Hit.HitLook.HEALED_DAMAGE);
                    if(entity.isPlayer()) {
                        entity.asPlayer().sendMessage("The infernal imp is burning you!");
                        entity.gfx(2187);
                    }
                }
                super.processNPC();
            }
        };
        imp.setCanWalkNPC(true);
        imp.setCustomCombatScript(CombatScript.DO_NOTHING);
        imp.forceTalk(Utils.rollDie(4, 1) ? "Evil!" : Utils.rollDie(3, 1) ? "Fire!" : Utils.rollDie(2, 1) ? "Anarchy!" : "Destruction!");
        return imp;
    }

    /**
     * Returns the possible x directions gifts can be drops on in the arena
     * give a cardinal direction
     */
    public int getXConstraint(Direction dir) {
        switch(dir) {
            default:
            case WEST:
                return -Utils.random(10);
            case NORTH:
            case SOUTH:
                return -10 + Utils.random(20);
            case EAST: return Utils.random(10);
        }
    }

    /**
     * Returns the possible y directions gifts can be drops on in the arena
     * give a cardinal direction
     */
    public int getYConstraint(Direction dir) {
        switch(dir) {
            case SOUTH:
            default:
                return -Utils.random(10);
            case NORTH:
                return Utils.random(10);
            case EAST:
            case WEST:
                return -10 + Utils.random(20);
        }
    }

    /**
     * Max presents dropped per attack
     */
    private static final int MAX_PRESENTS = 12;

    /*
     * Santa will focus 2 random directions and rain presents that explode.
     * Players will take damage based on proximity to the landing tile.
     * If players are on the landing tile they will be instantly killed.
     */
    private void rainPresents() {
        List<WorldTile> targetPositions = new ArrayList<>();
        Direction d = Direction.getRandomCardinal();

        // first tick turn and animate
        forceTalk("HO HO HO, MERRY CHRISTMAS! ");
        setDirection(d, true);
        anim(12658);
        lock();

        WorldTasksManager.scheduleRevolving(event -> {
            event.add(()->{});
            event.add(() -> {
                anim(4411);
            });
            event.add(() -> {
                // 2nd tick get targets positions and drop the presents
                for (int j = 0; j < MAX_PRESENTS; j++) {
                    // must be final for lambda
                    final WorldTile prezTile = new WorldTile(0,0,0);
                    int failsafe = 20;
                    do {
                        prezTile.setLocation(getX() + (getXConstraint(d)),
                                getY() + (getYConstraint(d)), getPlane());
                    } while((!World.isTileFree(prezTile, 1) || targetPositions.stream().anyMatch(t->t.matches(prezTile)) || !GIFT_BOUNDS.inBounds(prezTile, 1)) && failsafe-- > 0);

                    World.sendGraphics(this, new Graphics(610), prezTile);
                    targetPositions.add(prezTile);
                }

                // always add on the boss for meleers
                if(d == Direction.EAST || d == Direction.WEST) {
                    targetPositions.add(this.clone().relative(0,1));
                    targetPositions.add(this.clone().relative(0,-1));
                } else {
                    targetPositions.add(this.clone().relative(1,0));
                    targetPositions.add(this.clone().relative(-1,0));
                }

                // after present fully drops (2ticks) effect players
                WorldTasksManager.schedule(() -> {
                    // attack is finished at this point, resume attacking
                    unlock();

                    List<Entity> targets = getAllTargets();

                    targetPositions.stream().forEach((tile) -> {
                        // start gfx on target pos
                        World.sendGraphics(this, new Graphics(611), tile);
                    });

                    // explosion tick
                    targets.stream().forEach(entity -> {
                        // collect targets in proximity
                        // only allowed to be hit by 1 present
                        // prioritize distance 0 presents
                        int dist = -1;
                        if(targetPositions.stream().anyMatch(tile -> tile.distance(entity) == 0))
                            dist = 0;
                        else if(targetPositions.stream().anyMatch(tile -> tile.distance(entity) == 1))
                            dist = 1;
                        if(dist != -1) {
                            // damage target in proximity
                            int damage = (int) ((double)entity.getMaxHitpoints() * 0.70);
                            if(dist == 1)
                                damage /= 2;
                            if(entity.canTakeDamage() && damage > 0) {
                                entity.applyHit(this, Utils.random(damage/2, damage));
                            }
                        }
                    });
                }, 1);
            });
        });
    }


    @Override
    public void setNextFaceEntity(Entity entity) {

    }
}
