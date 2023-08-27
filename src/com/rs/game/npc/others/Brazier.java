package com.rs.game.npc.others;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.nightmare.TheNightmare;
import com.rs.game.npc.worldboss.CallusFrostborne;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.TheNightmareInstance;
import com.rs.utils.Utils;

import java.util.Objects;

@SuppressWarnings("serial")
public class Brazier extends NPC {

	public static final int LIT = 21203, UNLIT = 21204;

	private int direction;
	private CallusFrostborne boss;

	public boolean isLit() {
		return this.getId() == LIT;
	}

	private String brazierName;
	public Brazier(CallusFrostborne boss, WorldTile tile, String name) {
		super(LIT, tile, -1, true, true);
		this.boss = boss;
		setCantFollowUnderCombat(true);
		setCantSetTargetAutoRelatio(true);
		setForceMultiArea(true);
		if(boss != null)
			setNextFaceWorldTile(boss.getRespawnTile());
		setRandomWalk(0);
		brazierName = name;
		setCustomCombatScript(CombatScript.DO_NOTHING);
	}

	@Override
	public void setNextFaceEntity(Entity entity) {

	}

	@Override
	public void faceEntity(Entity target) {

	}

	@Override
	public void faceEntity2(Entity target) {

	}

	@Override
	public int getHitbarSprite(Player player) {
		return 22464;
	}

	static final WorldTile centerTile = new WorldTile(2398, 4069, 0);

	@Override
	public void sendDeath(final Entity source) {
		if (getId() == UNLIT) {
			boss.brazierLightCount++;
			// boss.processHit(new Hit(boss, 6250, HitLook.REGULAR_DAMAGE));
			if(boss.brazierLightCount >= 4 && boss.getHitpoints() > 0) {
				boss.processHit(new Hit(boss, boss.getHitpoints(), HitLook.REGULAR_DAMAGE));
			}
			boss.yell("<col=00ffff><shad=0>The " + brazierName + " brazier has been lit, Callus' power is fading! p" + boss.getId() + " braz " + boss.brazierLightCount);
			setNextNPCTransformation(LIT);
		}
		setHitpoints(getMaxHitpoints());
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {

		if(hit.getSource() instanceof  Player) {
			Player player = (Player) hit.getSource();
			int spell = (int) player.getTemporaryAttributtes().getOrDefault("lastSpellCast", -1);
			player.getTemporaryAttributtes().remove("lastSpellCast");
			boolean fireSpell = hit.getLook() == HitLook.MAGIC_DAMAGE && (spell == 45 || spell == 63 || spell == 80 || spell == 91);

			if (fireSpell)
				hit.setDamage(hit.getDamage() * 8);
			else if (hit.getLook() != HitLook.HEALED_DAMAGE)
				hit.setDamage(0);
		}

	}

	@Override
	public void setRespawnTask() {

	}
	
	public void light() {
		setNextNPCTransformation(LIT);
		reset();
		//setDirection(Utils.getAngle(DIRECTIONS_ANGLE[direction][0], DIRECTIONS_ANGLE[direction][1]));
	}
	
	@Override
	public void setHitpoints(int hp) {
		super.setHitpoints(hp);
	}
	
	@Override
	public int getMaxHitpoints() {
		return boss == null || getId() == LIT ? 1 : 40000;
	}

	@Override
	public boolean preAttackCheck(Player attacker) {
		if(getId() == LIT)
			return false;
		return super.preAttackCheck(attacker);
	}

	public void extinguish() {
		setNextNPCTransformation(UNLIT);
		reset();
	}

	public void setBoss(CallusFrostborne callusFrostborne) {
		boss = callusFrostborne;
	}
}
