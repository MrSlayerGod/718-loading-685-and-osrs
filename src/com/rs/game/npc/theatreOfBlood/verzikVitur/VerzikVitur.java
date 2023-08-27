package com.rs.game.npc.theatreOfBlood.verzikVitur;


import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.Default;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.TOBBoss;
import com.rs.game.npc.theatreOfBlood.verzikVitur.phase2.BloodAttack;
import com.rs.game.npc.theatreOfBlood.verzikVitur.phase2.ChainAttack;
import com.rs.game.npc.theatreOfBlood.verzikVitur.phase2.ExplodeAttack;
import com.rs.game.npc.theatreOfBlood.verzikVitur.phase2.HealEffectAction;
import com.rs.game.npc.theatreOfBlood.verzikVitur.phase2.NycolasAttack;
import com.rs.game.npc.theatreOfBlood.verzikVitur.phase2.SlamAttack;
import com.rs.game.npc.theatreOfBlood.verzikVitur.phase3.MagicAOEAttack;
import com.rs.game.npc.theatreOfBlood.verzikVitur.phase3.MeleeAOEAttack;
import com.rs.game.npc.theatreOfBlood.verzikVitur.phase3.NycolasSpecialAttack;
import com.rs.game.npc.theatreOfBlood.verzikVitur.phase3.RangeAOEAttack;
import com.rs.game.npc.theatreOfBlood.verzikVitur.phase3.SniperSpecialAttack;
import com.rs.game.npc.theatreOfBlood.verzikVitur.phase3.WebSpecialAttack;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * 
 * @author Alex & cjay0091
 *
 */
@SuppressWarnings("serial")
public class VerzikVitur extends TOBBoss {

	public static final int PHASE_1_SCENE = 28369, PHASE_1 = 28370, PHASE_2_SCENE = 28371, PHASE_2 = 28372, PHASE_3_SCENE = 28373, PHASE_3 = 28374, PHASE_4_SCENE = 28375;
	
	
	private static final int[][] PILLAR_TILES = { {89, 158}, {89, 152} , {89, 146},
			{101, 158}, {101, 152} , {101, 146}};
	
	private int delay;
	private int charges;
	private Pillar[] pillars;
	
	//cjay code
    private int phaseThreeDelay;
    private boolean underThirtyFivePercentHP, underTwentyPercentHP;
    private int numberAttacks;
    private int specialAttackCount;
    private TOBAction action;
    
	public VerzikVitur(TheatreOfBlood raid) {
		
		super(raid, 5, PHASE_1_SCENE, raid.getTile(94, 163, 0));
		this.raid = raid;
		spawnPillars();
        setIntelligentRouteFinder(true);
	}
	
	private void spawnPillars() {
		pillars = new Pillar[PILLAR_TILES.length];
		for (int i = 0; i < PILLAR_TILES.length; i++)
			pillars[i] = new Pillar(raid.getTile(PILLAR_TILES[i][0], PILLAR_TILES[i][1]), raid);
	}
	
	private void killPillars() {
		if (pillars == null)
			return;
		for (Pillar pillar : pillars)
			pillar.sendDeath(this);
		pillars = null;
	}
	
	@Override
	public int getMaxHitpoints() {
		int maxHP = getId() == PHASE_1 || getId() == PHASE_1_SCENE ? 20000 : 32500;
		if (raid != null && raid.getTeamSize() >= 1 && raid.getTeamSize() < 5) {
			int hp = (int) ((raid.getTeamSize() * maxHP / 5) * 0.7);
			int base = (int) (maxHP * 0.3);
			return base + hp;
		}
		//return getId() == PHASE_1 || getId() == PHASE_1_SCENE ? 20000 : 32500;
		return maxHP;
	}
	
    @Override
    public void handleIngoingHit(Hit hit) {
        if (action instanceof HealEffectAction && (hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MAGIC_DAMAGE || hit.getLook() == HitLook.MELEE_DAMAGE)) {
            heal(hit.getDamage());
            hit.setDamage(0);
        }
        super.handleIngoingHit(hit);
    }

	@Override
	public void sendDeath(Entity killer) {
		if (raid != null) {
			if (getId() == PHASE_1) {
				for (Player player : raid.getTeam()) {
					if (player.getEquipment().getWeaponId() == 52516) {
						player.getEquipment().getItems().set(Equipment.SLOT_WEAPON, null);
						player.getEquipment().refresh(Equipment.SLOT_WEAPON);
						player.getAppearence().generateAppearenceData();
						player.getPackets().sendGameMessage(
								"The weapon falls apart in your hand as Verzik's shield is destroyed.");
					}
				}

				int deathDelay = getCombatDefinitions().getDeathDelay();
				WorldTasksManager.schedule(new WorldTask() {
					int loop;

					@Override
					public void run() {
						if (hasFinished() || raid.getStage() != Stages.RUNNING) {
							stop();
							return;
						}
						if (loop == 0) {
							setNextAnimation(new Animation(getCombatDefinitions().getDeathEmote()));
						} else if (loop == deathDelay) {
							setNextAnimation(new Animation(-1));
							setNextNPCTransformation(PHASE_2_SCENE);
							setHitpoints(getMaxHitpoints());
							killPillars();
							WorldTile to = raid.getTile(94, 154);
							resetWalkSteps();
							addWalkSteps(to.getX(), to.getY(), 10, false);
						} else if (loop == deathDelay + 5) {
							setNextNPCTransformation(PHASE_2);
							delay = 6;
							stop();
						}
						loop++;
					}
				}, 0, 1);
				return;
			} else if (getId() == PHASE_2) {
				action = null;
				int deathDelay = getCombatDefinitions().getDeathDelay();
				WorldTasksManager.schedule(new WorldTask() {
					int loop;

					@Override
					public void run() {
						if (hasFinished() || raid.getStage() != Stages.RUNNING) {
							stop();
							return;
						}
						if (loop == 0) {
							setNextAnimation(new Animation(28118));
						} else if (loop == deathDelay + 1) {
							setNextAnimation(new Animation(28119));
							setNextNPCTransformation(PHASE_3_SCENE);
							setHitpoints(getMaxHitpoints());
						} else if (loop >= deathDelay + 3) {
							setNextForceTalk(new ForceTalk("Behold my true nature!"));
							setNextNPCTransformation(PHASE_3);
							setNextAnimation(new Animation(-1));
							delay = 0;
							phaseThreeDelay = 7;
							stop();
						}
						loop++;
					}
				}, 0, 1);
				return;
			} else if (getId() == PHASE_3) {
				setNextAnimation(new Animation(-1));
				setNextNPCTransformation(PHASE_4_SCENE);
			}
		}
		super.sendDeath(killer);
	}
	
	@Override
	public void processNPC() {
		if (getId() == PHASE_1_SCENE || isDead() || raid == null || raid.getTargets(this).isEmpty())
			return;
		raid.setHPBar(this);
		
		if (delay-- >= 0)
			return;
        if (getId() == PHASE_1)
            phase1();
        else if (getId() == PHASE_2)
            phase2();
        else if (getId() == PHASE_3)
            phase3();
	}
	
    private void phase3() {
    	if (!underTwentyPercentHP) {
            int max = getMaxHitpoints();
            if (max * 0.35 >= getHitpoints()) {
                underTwentyPercentHP = true;
                setNextForceTalk(new ForceTalk("I'm not done with you yet!"));

                submit(client -> {
                  //  Position random = this.transform(Utils.random(5), Utils.random(5), 0)
                    /*TornadoNPC npc = (TornadoNPC) raid.spawnNpc(null, TornadoNPC.TORNADO_ID, random.getX(), random.getY(), random.getZ(), -1,
                            Short.MAX_VALUE, 0, 0, 0, false, false);

                    npc.setInitial(this, client);*/
                    new TornadoNPC(this, client, transform(Utils.random(5), Utils.random(5), 0));
                });
            }
        }
    	
    	
        if (numberAttacks == 5) {
            numberAttacks = 0;

            if (specialAttackCount == 0) {
                action = new NycolasSpecialAttack();
            } else if (specialAttackCount == 1) {
                action = new WebSpecialAttack();
            } else if (specialAttackCount == 2) {
                action = new SniperSpecialAttack();
            } else if (specialAttackCount == 3) {
                phaseThreeDelay = 5;
                specialAttackCount = -1;
            }
            specialAttackCount++;
        } else {
            int random = Utils.random(3);
            if (random == 0) {
                action = new MeleeAOEAttack();
            } else if (random == 1) {
                action = new MagicAOEAttack();
            } else if (random == 2) {
                action = new RangeAOEAttack();
            }

            numberAttacks++;
        }

        delay = action.use(this);
        if (delay == 0) {
            int random = Utils.random(2);
            if (random == 0) {
                delay = new RangeAOEAttack().use(this);
            } else {
                delay = new MagicAOEAttack().use(this);
            }
        }
    }

    private void phase2() {
		if (!underThirtyFivePercentHP) {
			 int max = getMaxHitpoints();
             if (max * 0.35 >= getHitpoints()) {
                 underThirtyFivePercentHP = true;
                 this.action = new HealEffectAction();
             }
		}
    	
        if (action == null) {
            if (Utils.random(30) == 0 && !underThirtyFivePercentHP) {
                action = new NycolasAttack();
            } else if (Utils.random(20) == 0 && underThirtyFivePercentHP) {
                action = new HealEffectAction();
            } else if (Utils.random(5) == 0) {
                action = new ChainAttack();
            } else if (Utils.random(3) == 0) {
                action = underThirtyFivePercentHP ? new ExplodeAttack() : new SlamAttack();
            } else {
                action = underThirtyFivePercentHP ? new BloodAttack() : new ExplodeAttack();
            }
        }

        delay = action.use(this);
        if (delay == 0) {
            delay = new ExplodeAttack().use(this);
        }

        this.action = null;
    }
	
	private void phase1() {
		delay = 5;
		if (++charges % 3 == 0) {
			setNextAnimation(new Animation(28109));
			Player player = getRandomPlayer();
			if (player == null)
				return;
			Pillar pillar = getPillar(player);
		    int msDelay = World.sendProjectile(this, pillar != null ? pillar : player, 6580, 50, 41, 36, 41, 16, getSize() * 32);
			Default.delayHit(this, CombatScript.getDelay(msDelay)+1, pillar != null ? pillar : player, Default.getRegularHit(this, Utils.random(1100))); //can1hit person
			if (pillar != null)
				pillar.setNextGraphics(new Graphics(6582, msDelay / 10, 0));
			else
				player.setNextGraphics(new Graphics(6581, msDelay / 10, 0));
		} else
			setNextAnimation(new Animation(28110));
	}
	
	private Pillar getPillar(Player target) {
		WorldTile middle = getMiddleWorldTile();
		
		int x = middle.getX();
		int y = middle.getY();
		int step = 0;
		while ((x != target.getX() || y != target.getY()) && step++ < 200) {
			if (x > target.getX())
				x--;
			else if (x < target.getX())
				x++;
			if (y > target.getY())
				y--;
			else if (y < target.getY())
				y++;
			for (Pillar pillar : pillars) 
				if (!pillar.isDead() && !pillar.hasFinished() && Utils.colides(x, y, 1, pillar.getX(), pillar.getY(), 3)) 
					return pillar;

		}
		
		return null;
	}

	public void start() {
		if (raid == null || getId() != PHASE_1_SCENE || isDead() || hasFinished())
			return;
		setNextNPCTransformation(PHASE_1);
		raid.startLastBoss();
	}
	
	@Override
	public void setNextFaceWorldTile(WorldTile tile) {
		if (getId() == PHASE_1_SCENE || getId() == PHASE_1)
			return;
		super.setNextFaceWorldTile(tile);
	}

	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
	
	@Override
	public void setTarget(Entity target) {
		
	}
	
    public int getPhaseThreeDelay() {
        return phaseThreeDelay;
    }
}
