package com.rs.game.npc.theatreOfBlood.nycolas;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.TOBBoss;
import com.rs.game.npc.theatreOfBlood.nycolas.actions.MagicAttackAction;
import com.rs.game.npc.theatreOfBlood.nycolas.actions.MeleeAttackAction;
import com.rs.game.npc.theatreOfBlood.nycolas.actions.RangeAttackAction;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Nycolas extends TOBBoss {

    public static final int MELEE_NYCOLAS_ID = 28355, MAGIC_NYCOLAS_ID = 28356, RANGE_NYCOLAS_ID = 28357;
    private static final int FALLING_ANIMATION = 27993;

    private static final int SWITCH_CYCLE_DELAY = 16; // Every ten seconds

    private int actionDelay, attackStyleDelay;

    public Nycolas(TheatreOfBlood raid) {
        super(raid, 2, MELEE_NYCOLAS_ID, raid.getTile(160 ,88, 0));
        setNextAnimation(new Animation(FALLING_ANIMATION));
        this.attackStyleDelay = SWITCH_CYCLE_DELAY;
    }

    @Override
    public void processNPC() {
        //super.tick();//destroys the npc
        if (isDead() || raid.getTargets(this).isEmpty())
            return;
        raid.setHPBar(this);
        if (attackStyleDelay-- == 0) {
            attackStyleDelay = SWITCH_CYCLE_DELAY;
            setNextNPCTransformation(calculateNextCycle());
        }
        if (actionDelay-- == 0) {
            actionDelay = getAction().use(this);
        }
    }

    private int calculateNextCycle() {
        int id = getId(), next;
        do {
            next = MELEE_NYCOLAS_ID + Utils.random(3);
        } while (next == id);
        return next;
    }

    private TOBAction getAction() {
        return getId() == MELEE_NYCOLAS_ID ? new MeleeAttackAction() : getId() == RANGE_NYCOLAS_ID ? new RangeAttackAction() : new MagicAttackAction();
    }

    @Override
    public void handleIngoingHit(Hit hit) {
    	if (hit.getLook() == HitLook.MELEE_DAMAGE || hit.getLook() == HitLook.MAGIC_DAMAGE || hit.getLook() == HitLook.RANGE_DAMAGE) {
            boolean heal = false;
            if (getId() == MELEE_NYCOLAS_ID && hit.getLook() != HitLook.MELEE_DAMAGE) {
                heal = true;
            } else if (getId() == MAGIC_NYCOLAS_ID  && hit.getLook() != HitLook.MAGIC_DAMAGE) {
                heal = true;
            } else if (getId() == RANGE_NYCOLAS_ID  && hit.getLook() != HitLook.RANGE_DAMAGE) {
                heal = true;
            }
            if (heal) {
                heal(hit.getDamage());
                Entity source = hit.getSource();
                if (source != null) 
                    source.applyHit(new Hit(this, hit.getDamage(), HitLook.REFLECTED_DAMAGE));
                hit.setDamage(0);
            }
    	}
        super.handleIngoingHit(hit);
    }
    
	@Override
	public void setTarget(Entity target) {
		
	}
	
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
}
