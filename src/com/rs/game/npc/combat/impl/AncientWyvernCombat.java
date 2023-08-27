package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.utils.Utils;

public class AncientWyvernCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 27792, 27793, 27794, 27795 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		boolean meleeDistance = Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(), target.getSize(), 0);
		int attackStyle = Utils.random(meleeDistance ? 3 : 2);
		switch (attackStyle) {
		case 0:
			npc.setNextAnimation(new Animation(27653));
			int angle = Utils.getAngle(target.getCoordFaceX(target.getSize())-npc.getCoordFaceX(npc.getSize()), target.getCoordFaceY(target.getSize())-npc.getCoordFaceY(npc.getSize()));
			int v = angle >> 11;
		//	int direction = Utils.getMoveDirection(target.getCoordFaceX(target.getSize())-npc.getCoordFaceX(npc.getSize()), target.getCoordFaceY(target.getSize())-npc.getCoordFaceY(npc.getSize()));
	//	System.out.println(direction);
			byte[] dirs = Utils.getDirection(angle);
			World.sendGraphics(npc, new Graphics(6392, 0, npc.getId() == 27795 ? 500 : 100, v), new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()).transform(npc.getSize()/2 * dirs[0], npc.getSize()/2 * dirs[1], 0));
		//	npc.setNextGraphics(new Graphics(6392, 0, npc.getId() == 27795 ? 100 : 0));
			target.setNextGraphics(new Graphics(502, 60, 100));
			if (Utils.random(10) == 0)
				target.addFreezeDelay(5000);
			delayHit(npc, 1, target, getRegularHit(npc, Utils.getRandom(SkeletalWyvernCombat.hasShield(target) ? 50 : 600)));
			break;
		case 1:
			npc.setNextAnimation(new Animation(27653));
			npc.setNextGraphics(new Graphics(6393));
			if (!meleeDistance)
				World.sendProjectile(npc, target, 6394, 0, 0, 30, 120, 0, npc.getSize() * (npc.getId() == 27795 ? 16 : 32) + 32);
			delayHit(npc, 4, target, new Hit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target), npc.getId() == 27794 || npc.getId() == 27792 ? HitLook.RANGE_DAMAGE : HitLook.REGULAR_DAMAGE));
			break;
		case 2:
			int style = Utils.random(3);
			npc.setNextAnimation(new Animation(style == 0 ? 27651 : style == 1 ? 27654 : 27658));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			break;
		}
		return defs.getAttackDelay();
	}

}
