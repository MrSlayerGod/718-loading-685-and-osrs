package com.rs.game.npc.nightmare;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.CombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.game.player.controllers.TheNightmareInstance;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class TheNightmare extends NPC {


	public static boolean FORCE_DROP_INQ;
	
	
	public static final int NIGHTMARE_BOSS_DEAD = 29432
			, NIGHTMARE_BOSS_PHASE1_SHIELD = 29425, NIGHTMARE_BOSS_PHASE2_SHIELD = 29426 , NIGHTMARE_BOSS_PHASE3_SHIELD = 29427
					, NIGHTMARE_BOSS_PHASE1 = 29428, NIGHTMARE_BOSS_PHASE2 = 29429 , NIGHTMARE_BOSS_PHASE3 = 29430,
					NIGHTMARE_BOSS_SLEEPWALKER = 29431;
	
	public static final WorldTile[] SPORES = new WorldTile[] {
			//inner
			new WorldTile(3869, 9951, 3),
			new WorldTile(3875, 9951, 3),
			new WorldTile(3872, 9948, 3),
			new WorldTile(3872, 9954, 3),
			//outer
			new WorldTile(3864, 9951, 3),
			new WorldTile(3880, 9951, 3),
			new WorldTile(3872, 9943, 3),
			new WorldTile(3872, 9959, 3),
			//pillars
			new WorldTile(3867, 9956, 3),
			new WorldTile(3867, 9946, 3),
			new WorldTile(3877, 9946, 3),
			new WorldTile(3877, 9956, 3)
	};
	
	private int phase, shieldHP;
	private boolean shield;
	private Totem[] totems;
	
	private long shadowSpecial;
	private long phaseSpecial;
	private boolean firstSpecial;
	
	private int[] flowerPower;
	
	public TheNightmare() {
		super(NIGHTMARE_BOSS_DEAD, new WorldTile(3870, 9949, 3), -1, true, true);
		setIntelligentRouteFinder(true);
		setForceMultiArea(true);
		setLureDelay(6000);//approximately 6 seconds lure
		setTotems();
		setRandomWalk(0);
		reset();

	//	activateTotems();
	}
	
	public void setFlowerPower(int... pos) {
		flowerPower = pos;
	}
	
	public void setPhase(int phase) {
		this.phase = phase;
		shadowSpecial = Utils.currentTimeMillis() + Utils.random(10000, 45000);
		phaseSpecial = Utils.currentTimeMillis() + Utils.random(10000, 45000);
		firstSpecial = Utils.random(2) == 0;
	}
	
	public boolean hasHusks() {
		List<Integer> npcsIndexes = World.getRegion(getRegionId()).getNPCsIndexes();
		if (npcsIndexes != null) {
			for (int npcIndex : npcsIndexes) {
				NPC npc = World.getNPCs().get(npcIndex);
				if (npc != null && !npc.hasFinished() && (npc instanceof Husk))
					return true;
			}
		}
		return false;
	}
	
	public boolean isShadowSpecialReady() {
		return shadowSpecial < Utils.currentTimeMillis();
	}
	
	public boolean isPhaseSpecialReady() {
		/*if (phaseSpecial > 10000)
			phaseSpecial = 5000;*/
		return phaseSpecial < Utils.currentTimeMillis();
	}
	
	public boolean isFirstSpecial() {
		return firstSpecial;
	}
	
	
	@Override
	public void sendDeath(final Entity source) {
		if (shield && phase < 3) {
			setShield(false);
			return;
		}
	//	super.sendDeath(source);
		final NPCCombatDefinitions defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		if (!isDead())
			setHitpoints(0);
		final int deathDelay = defs.getDeathDelay() - 3;
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= deathDelay) {
					drop();
					reset();
					TheNightmareInstance.reset();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}
	
	//TODO
	
	public static void main(String[] args) {
		int damage = 1000;
		List<String> killers = new ArrayList<String>();
		killers.add("test");
		killers.add("test2");
		killers.add("test2");
		killers.add("test2");
		killers.add("test2");
		killers.add("test2");
		int alivePlayerCount = Math.max(killers.size(), 5);
		double dropRate = 2;
		for (int k = 0; k < 10000000; k++) {
			int total = 0;
			int secondRollChance = Math.min(80, (alivePlayerCount-5));
			boolean twice = secondRollChance > Utils.random(100);
			
			for (int i = 0; i < (twice ? 2 : 1); i++) {
				//rares
				List<Drop> drops = new ArrayList<Drop>();
				if (Utils.random((int) (600 / dropRate)) == 0)
					drops.add(new Drop(54419, 1, 1));
				if (Utils.random((int) (600 / dropRate)) == 0)
					drops.add(new Drop(54420, 1, 1));
				if (Utils.random((int) (600 / dropRate)) == 0)
					drops.add(new Drop(54421, 1, 1));
				if (Utils.random((int) (400 / dropRate)) == 0)
					drops.add(new Drop(54422, 1, 1));
				if (Utils.random((int) (1200 / dropRate)) == 0 || FORCE_DROP_INQ) {
					drops.add(new Drop(54417, 1, 1));
					FORCE_DROP_INQ = false;
				}
				
				if (!drops.isEmpty()) {
					long totalDamage = 0;
					for (String player : killers) 
						totalDamage += damage;
					long win = Utils.random(totalDamage);
					totalDamage = 0;
					l: for (String player : killers) {
						totalDamage += damage;
						if (totalDamage > win) { //found winner.
							Drop drop = drops.get(Utils.random(drops.size()));
							total++;
							//System.out.println("1st "+player+", "+drop.getItemId()+", "+k);
							///sendDrop(player, drop);
							break l;
						}
					}
				}
				if (total > 1) {
					System.out.println("broken");
				}
				//very rares
				drops.clear();
				if (Utils.random((int) (1800 / dropRate)) == 0)
					drops.add(new Drop(54511, 1, 1));
				if (Utils.random((int) (1800 / dropRate)) == 0)
					drops.add(new Drop(54514, 1, 1));
				if (Utils.random((int) (1800 / dropRate)) == 0)
					drops.add(new Drop(54517, 1, 1));
				
				l: if (!drops.isEmpty()) {
					long totalDamage = 0;
					for (String player : killers) 
						totalDamage += damage;
					long win = Utils.random(totalDamage);
					totalDamage = 0;
					for (String player : killers) {
						totalDamage += damage;
						if (totalDamage > win) { //found winner.
							Drop drop = drops.get(Utils.random(drops.size()));
							//sendDrop(player, drop);
						//	System.out.println("2nd "+player+", "+drop.getItemId()+", "+k);
							break l;
						}
					}
					
				}
			}
		}
	}
	
	@Override
	public void drop() {
		super.drop();
		
		Player killer = getMostDamageReceivedSourcePlayer();
		
		sendDrop(killer, new Drop(532, 1, 1));
		
		double dropRate = 0;
		List<Player> killers = getForceLootSharingPeople();
		for (Player player : killers.toArray(new Player[killers.size()])) {
			Integer damage = getDamageReceived(player);
			if (damage < 500 || !(player.getControlerManager().getControler() instanceof TheNightmareInstance))
				killers.remove(player);
		}
		if (killers.isEmpty())
			return;
		
		
		for (Player p2 : killers) 
			dropRate += p2.getDropRateMultiplier() * Drops.getNerfDrop(p2);
		dropRate /= killers.size();
		dropRate *= Drops.NERF_DROP_RATE;
		dropRate *= 1.2; //20% more than osrs base on top of rate.
		
		
		//dditionally, there is a chance that both are rolled a second time (independent of the first rolls), with this chance being (partySize - 5)%, capped between 0 and 75 percent
		int secondRollChance = Math.min(killers.size()-5, Math.min(80, (getAlivePlayersCount()-5)));
		boolean twice = secondRollChance > Utils.random(100);
		
		for (int i = 0; i < (twice ? 2 : 1); i++) {
			//rares
			List<Drop> drops = new ArrayList<Drop>();
			if (Utils.random((int) (600 / dropRate)) == 0)
				drops.add(new Drop(54419, 1, 1));
			if (Utils.random((int) (600 / dropRate)) == 0)
				drops.add(new Drop(54420, 1, 1));
			if (Utils.random((int) (600 / dropRate)) == 0)
				drops.add(new Drop(54421, 1, 1));
			if (Utils.random((int) (400 / dropRate)) == 0)
				drops.add(new Drop(54422, 1, 1));
			if (Utils.random((int) (1200 / dropRate)) == 0)
				drops.add(new Drop(54417, 1, 1));
			
			if (!drops.isEmpty()) {
				long totalDamage = 0;
				for (Player player : killers) 
					totalDamage += getDamageReceived(player);
				long win = Utils.random(totalDamage);
				totalDamage = 0;
				l: for (Player player : killers) {
					totalDamage += getDamageReceived(player);
					if (totalDamage > win) { //found winner.
						Drop drop = drops.get(Utils.random(drops.size()));
						sendDrop(player, drop);
						break l;
					}
				}
			}
			//very rares
			drops.clear();
			if (Utils.random((int) (1800 / dropRate)) == 0)
				drops.add(new Drop(54511, 1, 1));
			if (Utils.random((int) (1800 / dropRate)) == 0)
				drops.add(new Drop(54514, 1, 1));
			if (Utils.random((int) (1800 / dropRate)) == 0)
				drops.add(new Drop(54517, 1, 1));
			
			l: if (!drops.isEmpty()) {
				long totalDamage = 0;
				for (Player player : killers) 
					totalDamage += getDamageReceived(player);
				long win = Utils.random(totalDamage);
				totalDamage = 0;
				for (Player player : killers) {
					totalDamage += getDamageReceived(player);
					if (totalDamage > win) { //found winner.
						Drop drop = drops.get(Utils.random(drops.size()));
						sendDrop(player, drop);
						break l;
					}
				}
			}
		}
		for (Player player : killers) {
			if (player != killer) //cuz already checked
				checkSlayer(player);
			
			sendDrop(player, new Drop(526, 1, 1));
			sendDrop(player, DROPS[Utils.random(DROPS.length)]);
			if (Utils.random(player == killer ? 2 : 4) == 0)
				sendDrop(player, DROPS[Utils.random(DROPS.length)]);
			//if (player == killer && Utils.random(10) == 0) //10% chance two drops
			LuckyPets.checkPet(player, LuckyPet.NIGHTMARE, "The Nightmare");
		}
	}
	
	private static final Drop[] DROPS = {
			//runes
			new Drop(890, 32, 196),
			new Drop(892, 12, 515),
			new Drop(564, 15, 214),
			new Drop(561, 6, 138),
			new Drop(560, 24, 165),
			new Drop(565, 13, 129),
			new Drop(566, 12, 57),
			//resources
			new Drop(7937, 420, 1414),
			new Drop(1622, 1, 26),
			new Drop(1620, 2, 35),
			new Drop(1516, 14, 111),
			new Drop(1514, 3, 30),
			new Drop(445, 14, 79),
			new Drop(454, 16, 253),
			new Drop(448, 15, 50),
			new Drop(450, 8, 33),
			new Drop(216, 1, 7),
			new Drop(220, 1, 16),
			//consumables
			new Drop(385, 1, 15),
			new Drop(365, 1, 18),
			new Drop(140, 2, 10),
			new Drop(2434, 3, 3),
			new Drop(6687, 1, 9),
			new Drop(189, 1, 8),
			new Drop(10927, 1, 11),
			new Drop(995, 2717 * 50, 21800 * 50)
	};
	
	public void resetShadowSpecial() {
		shadowSpecial = Utils.currentTimeMillis() + Utils.random(15000, 45000);
	}
	
	public void resetPhaseSpecial() {
		phaseSpecial =  Utils.currentTimeMillis() + Utils.random(15000, 45000);
		firstSpecial = !firstSpecial;
	}
	
	public void attackTank() {
		if (isCantInteract())
			return;
		Player tank = null;
		double defence = Double.MIN_VALUE; 
		for (Entity target : World.getNearbyPlayers(this, false)) {
			Player player = (Player) target;
			double def = Math.max(Math.max(player.getBonuses()[CombatDefinitions.STAB_DEF], player.getBonuses()[CombatDefinitions.SLASH_DEF]), player.getBonuses()[CombatDefinitions.CRUSH_DEF]);
			if (defence < def) {
				tank = player;
				defence = def;
			}
		}
		if (tank != null && getCombat().getTarget() != tank) 
			getCombat().setTarget(tank);
	}
	
	@Override
	public void processNPC() {
		if (isDead() || isLocked())
			return;
		
		super.processNPC();
		attackTank(); //switches agro to tank always
	}
	
	public void wakeup() {
		TheNightmareInstance.sendMessage("<col=D80000>The Nightmare has awaken!");
		
		/*if (true == true) {
			setShield(true);
			setCantInteract(false);
			
			setShield(false);
			startSleepwalker();
			return;
		}*/
		
		setNextAnimation(new Animation(28611));
		
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (hasFinished())
					return;
				setPhase(0); 
				setShield(true);
				setCantInteract(false);
				setCantFollowUnderCombat(false); 
				
				
			}
			
		}, 8);
	}
	
	//send every tick
	public void updateInterface(Player player) {
		if ((isTotemActive() || !isAwaken()))  //if pillar phase or sleeping
			player.setLastTarget(null); //remove hp bar
		player.getPackets().sendHideIComponent(3203, 0, !isTotemActive()); //pillars
		if (isTotemActive()) {
			for (int i = 0; i < totems.length; i++) {
				int base = (i+1) * 7;
				int hp = totems[i].getHitpoints();
				player.getPackets().sendHideIComponent(3203, base + 2, hp != 0); //white
				if (hp != 0)
					player.getPackets().setWidgetSize(3203, base + 1, (60 - hp * 60 / totems[i].getMaxHitpoints()), 13);
			}
		}
		player.getPackets().sendHideIComponent(3203, 32, !isAwaken()); //bossbar
		if (isAwaken()) {
			player.getPackets().sendIComponentText(3203, 42, (getHitpoints() / (player.isOldHitLook() ? 10 : 1)) +"/"+(getMaxHitpoints() / (player.isOldHitLook() ? 10 : 1)));
			player.getPackets().sendHideIComponent(3203, 39, !shield);
			player.getPackets().sendHideIComponent(3203, 40, !shield);
			player.getPackets().setWidgetSize(3203, shield ? 40 : 38, getHitpoints() * 219 / getMaxHitpoints(), 18);
		}
	}
	
	@Override
	public void setHitpoints(int hp) {
		super.setHitpoints(hp);
		TheNightmareInstance.updateInterfaceAll();
	}
	
	public int getAlivePlayersCount() {
		return Math.max(5, TheNightmareInstance.getPlayersCount());
	}
	public void setShield(boolean on) {
		shield = on;
		shieldHP = ((getAlivePlayersCount()-5) * 176000 / 75) + 20000;
		setNextNPCTransformation((shield ? NIGHTMARE_BOSS_PHASE1_SHIELD : NIGHTMARE_BOSS_PHASE1)  + phase );
		setHitpoints(shield ? shieldHP : (24000 - (8000 * phase)));
		resetReceivedHits();
		setRefreshHitbars(true);
		if (!shield) {
			TheNightmareInstance.sendMessage("<col=D80000>As the Nightmare's shield fails, the totems in the area are activated.");
			activateTotems();
		}
	}
	
	@Override
	public int getHitbarSprite(Player player) {
		return shield ? 22450 : 22191; //super.getHitbarSprite();
	}
	
	@Override
	public int getMaxHitpoints() {
		return shield ? shieldHP : 24000;
	}
	
	public int getTotemHP() {
		return shieldHP == 0 ? 1000 : (shieldHP / 4);
	}
	
	public static final int FIRST_PHASE = 0, SECOND_PHASE = 1, THIRD_PHASE = 2;
	
	public int getPhase() {
		return phase; 
	}
	
	public boolean isAwaken() {
		return getId() != NIGHTMARE_BOSS_DEAD;
	}
	
	
	private static final WorldTile[] TOTEM_TILES =
		{new WorldTile(3863, 9958, 3), new WorldTile(3879, 9958, 3),
			new WorldTile(3863, 9942, 3), new WorldTile(3879, 9942, 3)};
	
	private void setTotems() {
		totems = new Totem[TOTEM_TILES.length];
		for (int i = 0; i < totems.length; i++)
			totems[i] = new Totem(this, TOTEM_TILES[i], i);
	}
	
	@Override
	public void finish() {
		super.finish();
		if (totems != null) {
			for (Totem totem : totems)
				if (totem != null)
					totem.finish();
		}
	}
	
	public boolean isTotemActive() {
		return totems[0].getId() != Totem.INACTIVE;
	}

	public void switchPhase() {
		if (hasFinished())
			return;
		for (Totem totem : totems) 
			if (totem.getId() != Totem.CHARGED)
				return;
		TheNightmareInstance.sendMessage("<col=D80000>All four totems are fully charged.");
		
		int msDelay = 0;
		for (Totem totem : totems) 
			msDelay = Math.max(msDelay, World.sendProjectile(totem, this, 6768, 140, 36, 50, 90, 5, 64));
		
		WorldTasksManager.schedule(new WorldTask() {

			boolean sleepwalker = false;
			
			@Override
			public void run() {
				if (hasFinished()) {
					stop();
					return;
				}
				if (sleepwalker) {
					startSleepwalker();
					stop();
				} else {
					sleepwalker = true;
					setNextGraphics(new Graphics(6769));
					applyHit(new Hit(TheNightmare.this, 8000, HitLook.REGULAR_DAMAGE));
					desactivateTotems();
					if (phase >= 2) { //death
						stop();
						return;
					}
				}
			}
			
		}, CombatScript.getDelay(msDelay), 0);
	}
	
	public boolean isSleepwalker() {
		return getId() == NIGHTMARE_BOSS_SLEEPWALKER;
	}
	
	public void startSleepwalker() { //TP mid then 
		anim(28607);
		resetWalkSteps();
		//setCantInteract(true);
		setCantInteract(true);
		setNextFaceEntity(null);//stop facing tank
		WorldTasksManager.schedule(new WorldTask() {

			boolean sleepwalker = false;
			
			@Override
			public void run() {
				if (hasFinished()) {
					stop();
					return;
				}
				if (sleepwalker) {
					stop();
					finishSleepwalker();
				} else {
					setNextWorldTile(getRespawnTile());
					anim(28609);
					sleepwalker = true;
				}
			}
			
		}, 1, 1);
	}
	
	public void finishSleepwalker() { 
		//make sure to delete npcs if instance destroyed on mid of phase
		TheNightmareInstance.sendMessage("<col=D80000>The Nightmare begins to charge up a devastating attack.");
		setNextNPCTransformation(NIGHTMARE_BOSS_SLEEPWALKER);
		//28572
		int sleepwalkerCount = Math.min(TheNightmareInstance.getPlayersCount(), 24);
		List<NPC> sleepwalkers = new LinkedList<NPC>();
		
		for (int i = 0; i < sleepwalkerCount; i++) {
			WorldTile tile = new WorldTile(3864, 9951, 3);
			a: for (int t = 0; t < 100; t++) {//if cant pos after 100k attempts give up. shouldnt happen.
				
				int b = Utils.random(4);
				
				WorldTile tile2 = new WorldTile(3863+
						(b == 0 ? 0 : b == 1 ? 18 : Utils.random(19)
								),9942
						
						+(b == 2 ? 0 : b == 3 ? 18 : Utils.random(19)), 3);
				if (World.isFloorFree(tile2.getPlane(), tile2.getX(), tile2.getY())) {
					tile = tile2;
					break a;
				}
			}
			NPC sleepwalker = new Sleepwalker(29446 + Utils.random(6), tile);
			sleepwalker.setNextFaceEntity(this);
			sleepwalker.anim(28572);
			sleepwalkers.add(sleepwalker);
		}
		
		WorldTasksManager.schedule(new WorldTask() {

			int eatenNightmares = 0;
			
			@Override
			public void run() {
				if (hasFinished()) {
					for (NPC npc : sleepwalkers)
						npc.finish();
					stop();
					return;
				}
				for (NPC npc : sleepwalkers) 
					if (!npc.isDead() && !npc.hasFinished() && !npc.isCantInteract()) {
						if (Utils.collides(npc, TheNightmare.this)) {
							npc.anim(28571);
							npc.resetWalkSteps();
							npc.setCantInteract(true);
							eatenNightmares++;
							WorldTasksManager.schedule(new WorldTask() {
								@Override
								public void run() {
									npc.finish();
								}
								
							}, 1);
						} else
							npc.setForceWalk(getMiddleWorldTile());
					}
				for (NPC npc : sleepwalkers) 
					if (!npc.hasFinished())
						return;
				stop();
				anim(28604);
				
				
				int damage = 50 + (eatenNightmares * 950 / sleepwalkers.size());//(eatenNightmares * 30);
				for (Entity target : World.getNearbyPlayers(TheNightmare.this, false)) {
					target.setNextGraphics(new Graphics(6782, 90, 0));
					CombatScript.delayHit(TheNightmare.this, 3, target, new Hit(TheNightmare.this,  damage, HitLook.REGULAR_DAMAGE));
				}
				
				WorldTasksManager.schedule(new WorldTask() {
					
					@Override
					public void run() {
						if (hasFinished()) 
							return;
						setPhase(phase+1);
						TheNightmareInstance.update();
						setShield(true);
						setCantInteract(false);
						TheNightmareInstance.sendMessage("<col=D80000>The Nightmare restores her shield.");
					}
					
				}, 8);
			}
			
		}, 3, 1);
		
	}
	
	
	public void activateTotems() {
		for (Totem totem : totems) 
			totem.activate();
	}
	
	public void desactivateTotems() {
		for (Totem totem : totems) 
			totem.inactive();
	}
	
	
	public void reset() {
		phase = shieldHP = 0;
		shield = false;
		flowerPower = null;
		setNextNPCTransformation(NIGHTMARE_BOSS_DEAD);
		setCantInteract(true);
		setCantFollowUnderCombat(true); 
		super.reset();
		
	}
	
	@Deprecated
	public Hit handleOutgoingHit(Hit hit, Entity target) {
		if (target instanceof Player) {
			Player player = (Player) target;
			if (player.getPrayer().isUsingProtectionPrayer() &&
					(hit.getLook() == HitLook.MELEE_DAMAGE && !player.getPrayer().isMeleeProtecting())
					|| (hit.getLook() == HitLook.RANGE_DAMAGE && !player.getPrayer().isRangeProtecting())
					|| (hit.getLook() == HitLook.MAGIC_DAMAGE && !player.getPrayer().isMageProtecting())) 
				hit.setDamage((int) (hit.getDamage() * 1.2));
		}
		return super.handleOutgoingHit(hit, target);
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (shield) {
			if (hit.getLook() != HitLook.HEALED_DAMAGE)
				hit.setLook(HitLook.BLUE_DAMAGE);
			if (flowerPower != null && hit.getSource() != null && hit.getSource() != this) {
				if (!(hit.getSource().getX() >= flowerPower[0] && hit.getSource().getX() <= flowerPower[1]
						&& hit.getSource().getY() >= flowerPower[2] && hit.getSource().getY() <= flowerPower[3]))
					hit.setHealHit();
			}
		} else if (hit.getSource() != this || hit.getLook() != HitLook.REGULAR_DAMAGE) {
			hit.setDamage(0);
			if (hit.getSource() instanceof Player) 
				((Player)hit.getSource()).getPackets().sendGameMessage("Your attacks have no effect on the Nightmare.");
			
		}
	}
	
	public boolean hasShield() {
		return shield;
	}
	
	public void healTotems() {
		if (!shield) {
			for (Totem totem : totems)
				if (!totem.isDead() && totem.getId() == Totem.ACTIVE)
					totem.applyHit(new Hit(this, Utils.random(200)+1, HitLook.HEALED_DAMAGE));
		}
	}
		
	
	
	@Override
	public boolean restoreHitPoints() {
		return false;
	}
	
	@Override
	public double getMagePrayerMultiplier() {
		return 0.2;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0.2;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.2;
	}
	
	
	
}
