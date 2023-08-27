package com.rs.game.npc.theatreOfBlood.verzikVitur;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class TornadoNPC extends NPC {

    public static final int TORNADO_ID = 28386;

    private VerzikVitur boss;
    private Player target;

    public TornadoNPC(VerzikVitur boss, Player target, WorldTile tile) {
        super(TORNADO_ID, tile, -1, true, true);
        this.boss = boss;
        this.target = target;
        setForceMultiArea(true);
        setIntelligentRouteFinder(true);
    }

    @Override
    public void processNPC() {
      //  super.tick();//destroys the npc
        if (isDead() || boss == null || target == null || !boss.isRunning())
            return;

        if (boss.isDead() || target.isDead() || boss.getRaid().getTargets(this).isEmpty()) {
            finish();
            return;
        }

        //walkTo(new EntityStrategy(target), RouteType.SIMPLE);
        this.resetWalkSteps();
       // calcFollow(target, false);
        this.addWalkSteps(target.getX(), target.getY(), 1, false);
        if (!Utils.collides(this, target)) {
            return;
        }

        boss.heal((int) (getMaxHitpoints() * 0.05));
        target.applyHit(new Hit(this, Utils.random(200), HitLook.REGULAR_DAMAGE));
    }
    
	@Override
	public void setTarget(Entity target) {

	}
	
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}


}
