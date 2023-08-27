package com.rs.game.npc.randomEvent;

import java.util.ArrayList;
import java.util.List;

import com.rs.Settings;
import com.rs.game.ForceTalk;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.CombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

/*
 * will finish tomorrow. got the whole code in my mind
 */
@SuppressWarnings("serial")
public class CombatEventNPC extends NPC {

	private final Player target;
	
	private double[] bonuses;

	public static enum CombatEventNPCS {
		TREE_SPIRIT(4470, Skills.WOODCUTTING, "Leave these woods and never return!"),
		ROCK_GOLEM(8648, Skills.MINING, "Raarrrgghh! Flee human!"),
		RIVER_TROLL(8646, Skills.FISHING, "Fishies be mine! Leave dem fishies!"),
		SHADE(8645, Skills.PRAYER, null),
		ZOMBIE(75, -2, "Braaiinnzzzzzzzzzz"), //-2 any combat skill or slayer
		EVIL_CHICKEN(3375, -1, null),
		SWARM(411, -1, null);
		/*SNOWMAN1(6747, -1, null),
		SNOWMAN2(6747, -1, null),
		SNOWMAN3(6747, -1, null);*/
		
		private CombatEventNPCS(int id, int skill, String spawnMessage) {
			this.id = id;
			this.skill = skill;
			this.spawnMessage = spawnMessage;
		}
		
		private int id, skill;
		private String spawnMessage;
	}
	
	public static void startRandomEvent(Player player, int skill) {
		if(Settings.DISABLE_RANDOM_EVENTS)
			return;
		/*if (true == true)
			return;*/
		List<CombatEventNPCS> events = new ArrayList<CombatEventNPCS>();
		for(CombatEventNPCS e : CombatEventNPCS.values()) {
			//if(e.skill == skill || e.skill == -1 || (e.skill == -2 && (skill == Skills.SLAYER || skill == Skills.SUMMONING || (skill >= Skills.ATTACK && skill <= Skills.MAGIC))))
				events.add(e);
		}
		if(events.size() == 0)
			return;
		player.stopAll();
		new CombatEventNPC(events.get(Utils.random(events.size())), player);
	}
	
	public CombatEventNPC(CombatEventNPCS cbn, Player target) {
		super(cbn.id, new WorldTile(target), -1, true, true);
		setIntelligentRouteFinder(true);
		setForceAgressive(true);
		if(getDefinitions().hasAttackOption()) {
			setCombatLevel((int) (target.getSkills().getCombatLevelWithSummoning()*1.3));
			this.bonuses = getBonuses(getCombatLevel());
			setHitpoints(getMaxHitpoints());
		}else
			setCantSetTargetAutoRelatio(true);
		getCombat().addAttackedByDelay(target);
		getCombat().setTarget(target);
		this.target = target;
		if(cbn.spawnMessage != null)
			setNextForceTalk(new ForceTalk(cbn.spawnMessage));
	}
	
	
	
	public static boolean canRandomEvent(Player player) {
		return player.getControlerManager().getControler() == null && !player.getCutscenesManager().hasCutscene() && !player.isDead()
				&& !player.isCanPvp();
	}
	
	@Override
	public double getMagePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.6;
	}
	
	@Override
	public int getMaxHitpoints() {
		return getCombatLevel() * 20 + 1;
	}
	
	public int getMaxHit() {
		int cb = getCombatLevel();
		return cb == -1 ? 10 : (cb / 2);
	}

	@Override
	public double[] getBonuses() {
		return bonuses == null ? super.getBonuses() : bonuses;
	}
	
	
	public double[] getBonuses(int level) {
		double[] bonuses = new double[10];
		bonuses[CombatDefinitions.RANGE_ATTACK] = level * 5;
		bonuses[CombatDefinitions.STAB_ATTACK] = level * 5;
		bonuses[CombatDefinitions.MAGIC_ATTACK] = level * 5;
		bonuses[CombatDefinitions.STAB_DEF] = bonuses[CombatDefinitions.CRUSH_DEF] = bonuses[CombatDefinitions.SLASH_DEF] = level * 5;
		bonuses[CombatDefinitions.RANGE_DEF] = level * 5; 
		bonuses[CombatDefinitions.MAGIC_DEF] = level * 5;
		return bonuses;
	}
	
	@Override
	public void processNPC() {
		if (target.hasFinished() || !withinDistance(target, 16) || !canRandomEvent(target) || !isUnderCombat())
			finish();
		else
			super.processNPC();
	}

	

}
