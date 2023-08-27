package com.rs.game.player;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;

public class Projectile {

    private WorldTile shooter, receiver;
    private int gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset;

    public Projectile(int gfxId, int startHeight, int endHeight, int delay, int speed, int curve, int startDistanceOffset) {
        this.shooter = null;
        this.receiver = null;
        this.gfxId = gfxId;
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.speed = speed;
        this.delay = delay;
        this.curve = curve;
        this.startDistanceOffset = startDistanceOffset;
    }

    /**
     * Fire from tile -> tile
     */
    public int fire(Entity sender, WorldTile start, WorldTile end) {
        return World.sendProjectile(sender, start.clone(), end.clone(), this.getGfx(), this.getStartHeight(), this.getEndHeight(), this.getSpeed(), this.getDelay(), this.getCurve(), this.getStartDistanceOffset());
    }

    /**
     * Fire from entity -> tile
     */
    public int fire(Entity start, WorldTile end) {
        return World.sendProjectile(start, end, getGfx(), getStartHeight(), getEndHeight(), getSpeed(), getDelay(), getCurve(), getStartDistanceOffset());
    }

    /**
     * Fire from entity -> entity
     */
	public int fire(Entity start, Entity fin) {
        Player player = null;

        if(start instanceof Player)
            player = (Player) start;
        else if(fin instanceof Player)
            player = (Player) fin;

        if(player != null) {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                return World.raidProjectile(raid, start, fin, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset);
            }
        }

        return World.sendProjectile(start, fin, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset);
	}

    public Projectile(WorldTile shooter, WorldTile receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
        this.shooter = shooter;
        this.receiver = receiver;
        this.gfxId = gfxId;
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.speed = speed;
        this.delay = delay;
        this.curve = curve;
        this.startDistanceOffset = startDistanceOffset;
    }

    public WorldTile getShooter() {
        return shooter;
    }

    public WorldTile getReceiver() {
        return receiver;
    }

    public int getGfx() {
        return gfxId;
    }

    public int getStartHeight() {
        return startHeight;
    }

    public int getEndHeight() {
        return endHeight;
    }

    public int getSpeed() {
        return speed;
    }

    public int getDelay() {
        return delay;
    }

    public int getCurve() {
        return curve;
    }

    public int getStartDistanceOffset() {
        return startDistanceOffset;
    }

    public void setSpeed(int i) {
	    speed = i;
    }
}
