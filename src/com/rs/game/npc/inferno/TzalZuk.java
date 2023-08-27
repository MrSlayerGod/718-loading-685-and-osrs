/**
 * 
 */
package com.rs.game.npc.inferno;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Inferno;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Nov 26, 2017
 */
@SuppressWarnings("serial")
public class TzalZuk extends NPC {

	
	private Inferno inferno;
	private int delay;
	private int addCount;
	private boolean jadSpawned;
	private boolean healersSpawned;
	
	public TzalZuk(Inferno inferno, WorldTile tile) {
		super(27706, tile, -1, true, true);
		setCantSetTargetAutoRelatio(true);
		setCantFollowUnderCombat(true);
		setForceMultiArea(true);
		setIntelligentRouteFinder(true); //just to be considered boss
		setCapDamage(1000);
		this.inferno = inferno;
		this.delay = 10;
	}
	
	private void spawnAdds() {
		InfernoNPC npc = new InfernoNPC(27699, inferno.getMap().getTile(new WorldTile(2265, 5285, 0)));
		npc.getCombat().setCombatDelay(4);
		npc.setCantFollowUnderCombat(true);
		npc.setTarget(inferno.getShield().hasFinished() ? inferno.getPlayer() : inferno.getShield());
		npc = new InfernoNPC(27698, inferno.getMap().getTile(new WorldTile(2276, 5285, 0)));
		npc.getCombat().setCombatDelay(8);
		npc.setCantFollowUnderCombat(true);
		npc.setTarget(inferno.getShield().hasFinished() ? inferno.getPlayer() : inferno.getShield());
		addCount++;
	}
	
	private void spawnJad() {
		InfernoNPC npc = new InfernoNPC(27700, inferno.getMap().getTile(new WorldTile(2269, 5284, 0)));
		npc.getCombat().setCombatDelay(4);
		npc.setCantFollowUnderCombat(true);
		npc.setTarget(inferno.getShield().hasFinished() ? inferno.getPlayer() : inferno.getShield());
		jadSpawned = true;
	}
	//gfx 88 projectile
	//gfx 5509 explode
	private void spawnHealers() {
		inferno.getPlayer().getPackets().sendGameMessage("TzKal-Zuk has become enraged and is fighting for his life.");
		InfernoNPC npc = new InfernoNPC(27708, inferno.getMap().getTile(new WorldTile(2261, 5299, 0)));
		npc.setCantFollowUnderCombat(true);
		npc.setTarget(this);
		npc = new InfernoNPC(27708, inferno.getMap().getTile(new WorldTile(2266, 5299, 0)));
		npc.setCantFollowUnderCombat(true);
		npc.setTarget(this);
		npc = new InfernoNPC(27708, inferno.getMap().getTile(new WorldTile(2281, 5299, 0)));
		npc.setCantFollowUnderCombat(true);
		npc.setTarget(this);
		npc = new InfernoNPC(27708, inferno.getMap().getTile(new WorldTile(2275, 5299, 0)));
		npc.setCantFollowUnderCombat(true);
		npc.setTarget(this);
		healersSpawned  = true;
	}
	//27708
	
	//2269 5284 0 jad
	
	
	//85% /30% -> adds 
	//40% -> jad
	//25% -> lava healers
	@Override
	public void processNPC() {
		super.processNPC();
		if (isDead() || inferno == null || !inferno.isRunning() || inferno.getShield() == null || !inferno.getPlayer().withinDistance(this, 64) || inferno.getPlayer().isDead())
			return;
		
		double perc = (double)getHitpoints() / (double)getMaxHitpoints();
		if ((perc <= 0.85 && addCount < 1) || (perc <= 0.3 && addCount < 2))
				spawnAdds();
		if (perc <= 0.4 && !jadSpawned)
			spawnJad();
		if (perc <= 0.25 && !healersSpawned)
			spawnHealers();
		if (delay > 0) {
			delay--;
			return;
		}
		Player player = inferno.getPlayer();
		Shield shield = inferno.getShield();
		Entity target = shield.hasFinished() || player.getX() < (shield.getX()-2) || player.getX() > (shield.getX() + shield.getSize() + 1) ? player : shield;
		setNextAnimation(new Animation(27566));
		World.sendProjectile(this, target, 6375, 74, 16, 45, 45, 16, 64);
		if (target != shield)
			CombatScript.delayHit(this, 2, inferno.getPlayer(), CombatScript.getRegularHit(this, Utils.random(2510)+1));
		delay = 6;
	}
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
	
	@Override
	public void setTarget(Entity target) {

	}
	
	@Override
	public void finish() {
		super.finish();
		inferno.killAll();
	}
	
	@Override
	public void checkSlayer(Player killer) {
		if (inferno == null || inferno.isTestMode())
			return;
		super.checkSlayer(killer);
	}
}
