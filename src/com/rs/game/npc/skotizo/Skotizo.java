/**
 * 
 */
package com.rs.game.npc.skotizo;

import com.rs.game.ForceTalk;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.controllers.SkotizoLair;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Dec 20, 2017
 */
@SuppressWarnings("serial")
public class Skotizo extends NPC {

	private SkotizoLair lair;
	private NPC[] spawns;
	private int moves;
	
	public Skotizo(SkotizoLair lair) {
		super(27286, lair.getMap().getTile(new WorldTile(1693, 9874, 0)), -1, true, true);
		setMoves();
		this.lair = lair;
		setDropRateFactor(2);
		setForceMultiArea(true);
		setIntelligentRouteFinder(true);
	}
	
	private void setMoves() {
		moves = 3 + Utils.random(15);
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		super.handleIngoingHit(hit);
		if (lair == null || (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE))
			return;
		int altarsAlive = lair.getAliveAltarsCount();
		if (altarsAlive > 0) 
			hit.setDamage((int) (hit.getDamage() * (double)(4-altarsAlive)*0.25d));

	}
	
	public void activateAltar() {
		if (lair != null) {
			moves--;
			if (moves == 0) {
				lair.activateAltar();
				setMoves();
			}
		}
	}
	
	public void spawnSpawns() {
		if (spawns == null && lair != null && this.getHitpoints() < getMaxHitpoints()/2) {
			setNextForceTalk(new ForceTalk("Gar mulno ful talgo!"));
			spawns = new NPC[3];
			int count = 0;
			int[][] dirs = Utils.getCoordOffsetsNear(4);
			for (int dir = 0; dir < dirs[0].length; dir++) {
				final WorldTile tile = new WorldTile(new WorldTile(getX() + dirs[0][dir], getY() + dirs[1][dir], getPlane()));
				if (World.isTileFree(tile.getPlane(), tile.getX(), tile.getY(), 2)) {
					spawns[count] = new NPC(27287, tile, -1, true, true);
					spawns[count].setForceMultiArea(true);
					spawns[count++].setTarget(lair.getPlayer());
				}
				if (count == spawns.length)
					break;
			}
		}
	}
	
	@Override
	public void finish() {
		if (lair != null)
			lair.finishAltars();
		if (spawns != null)
			for (NPC n : spawns)
				if (n != null)
					n.finish();
		super.finish();
	}
}
