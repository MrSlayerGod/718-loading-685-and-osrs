package com.rs.game.npc.others;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.slayer.Kraken;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.TheHorde;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class EvilSnowman extends NPC {

	
	private int wave;
	private List<NPC> minions;
	

	public static Item[] OVERALL_REWARDS =
			new Item[] { new Item(200, 100),
					 new Item(202, 100),
					 new Item(204, 100),
					 new Item(206, 100),
					 new Item(208, 100),
					 new Item(210, 100),
					 new Item(212, 100),
					 new Item(214, 100),
					 new Item(216, 100),
					 new Item(218, 100),
					 new Item(220, 100),
					 new Item(232, 100),
					 new Item(224, 100),
					 new Item(1120, 100),
					 new Item(5973, 100),
					 new Item(10819, 100),
					 new Item(2, 1000),
					 new Item(995, 3000000)};
	
	private static final int[] PVM_RARE = {25545, 25546, 25547, 25548, 25549, 25550, 25551, 25552};

	
	public EvilSnowman(int id, WorldTile tile) {
		super(id, tile, -1, true, true);
		setDirection(Utils.getAngle(0, -1));
	//	setCantFollowUnderCombat(true);
		setCapDamage(1000);
		setLureDelay(0);
		setIntelligentRouteFinder(true);
		minions = new ArrayList<NPC>();
	}
	
	@Override
	public void drop() {
		for (Entity entity : getReceivedDamageSources()) {
			if (entity instanceof Player) {
				int damage = getDamageReceived(entity);
				if (damage >= 1000) {
					Player player = (Player) entity;
				
					if (!player.withinDistance(this))
						continue;
					player.getPackets().sendGameMessage("You receive a reward for your participation in the world boss event.");
					Item reward = OVERALL_REWARDS[Utils.random(OVERALL_REWARDS.length)];
					player.getInventory().addItemDrop(reward.getId(), reward.getAmount());
					player.getInventory().addItemDrop(995, 3000000);
					if (Utils.random(100) == 0) {
						int drop = PVM_RARE[Utils.random(PVM_RARE.length)];
						World.sendNews(player, "RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
						+ " just received <img=11><col=00ACE6>" +new Item(drop).getName()+" from <col=ff9933>Evil Snowman!", 0);
						player.getInventory().addItemDrop(drop, 1);
					}
				}
			}
		}
		super.drop();
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (isMinionsAlive()) 
			hit.setHealHit();
		super.handleIngoingHit(hit);
	}
	
	private boolean isMinionsAlive() {
		for (NPC npc : minions)
			if (!npc.isDead() && !npc.hasFinished())
				return true;
		return false;
	}
	
	public void checkMinions() {
		if (minions == null || isMinionsAlive())
			return;
		if (wave == 0 && getHitpoints() < getMaxHitpoints() * 0.9) {
			setNextForceTalk(new ForceTalk("You're in my realm, fear the kraken!"));
			minions.add(World.spawnNPC(20496, new WorldTile(2723, 5730, 0), -1, true, true));
			minions.add(World.spawnNPC(20496, new WorldTile(2723, 5737, 0), -1, true, true));
			minions.add(World.spawnNPC(20496, new WorldTile(2730, 5730, 0), -1, true, true));
			minions.add(World.spawnNPC(20496, new WorldTile(2730, 5737, 0), -1, true, true));
			wave++;
		} else if (wave == 1 && getHitpoints() < getMaxHitpoints() * 0.7) {
			setNextForceTalk(new ForceTalk("Watch your step, these are my grounds!"));
			minions.add(World.spawnNPC(9463, new WorldTile(2724, 5730, 0), -1, true, true));
			minions.add(World.spawnNPC(9463, new WorldTile(2724, 5734, 0), -1, true, true));
			minions.add(World.spawnNPC(9463, new WorldTile(2724, 5739, 0), -1, true, true));
			minions.add(World.spawnNPC(9463, new WorldTile(2731, 5730, 0), -1, true, true));
			minions.add(World.spawnNPC(9463, new WorldTile(2731, 5734, 0), -1, true, true));
			minions.add(World.spawnNPC(9463, new WorldTile(2731, 5739, 0), -1, true, true));
			wave++;
		} else if (wave == 2 && getHitpoints() < getMaxHitpoints() * 0.5) {
			setNextForceTalk(new ForceTalk("Fire and Ice, I don't have anything nice, beware of the heat!"));
			minions.add(World.spawnNPC(51, new WorldTile(2724, 5730, 0), -1, true, true));
			minions.add(World.spawnNPC(3068, new WorldTile(2724, 5734, 0), -1, true, true));
			minions.add(World.spawnNPC(5363, new WorldTile(2724, 5739, 0), -1, true, true));
			minions.add(World.spawnNPC(5363, new WorldTile(2731, 5730, 0), -1, true, true));
			minions.add(World.spawnNPC(27795, new WorldTile(2731, 5734, 0), -1, true, true));
			minions.add(World.spawnNPC(51, new WorldTile(2731, 5739, 0), -1, true, true));
			wave++;
		} else if (wave == 3 && getHitpoints() < getMaxHitpoints() * 0.3) {
			setNextForceTalk(new ForceTalk("My dogs are hungry, you might make a perfect Christmas Gift"));
			minions.add(World.spawnNPC(13460, new WorldTile(2724, 5730, 0), -1, true, true));
			minions.add(World.spawnNPC(14836, new WorldTile(2724, 5737, 0), -1, true, true));
			minions.add(World.spawnNPC(14836, new WorldTile(2731, 5730, 0), -1, true, true));
			minions.add(World.spawnNPC(13460, new WorldTile(2731, 5737, 0), -1, true, true));
			wave++;
		} else if (wave == 4 && getHitpoints() < getMaxHitpoints() * 0.1) {
			setNextForceTalk(new ForceTalk("My minions do not take damage from your foolish weapons haha"));
			minions.add(World.spawnNPC(6747, new WorldTile(2725, 5730, 0), -1, true, true));
			minions.add(World.spawnNPC(6748, new WorldTile(2725, 5734, 0), -1, true, true));
			minions.add(World.spawnNPC(6749, new WorldTile(2725, 5739, 0), -1, true, true));
			minions.add(World.spawnNPC(6747, new WorldTile(2732, 5730, 0), -1, true, true));
			minions.add(World.spawnNPC(6748, new WorldTile(2732, 5734, 0), -1, true, true));
			minions.add(World.spawnNPC(6749, new WorldTile(2732, 5739, 0), -1, true, true));
			
			minions.add(World.spawnNPC(6747, new WorldTile(2727, 5730, 0), -1, true, true));
			minions.add(World.spawnNPC(6748, new WorldTile(2727, 5734, 0), -1, true, true));
			minions.add(World.spawnNPC(6749, new WorldTile(2727, 5739, 0), -1, true, true));
			minions.add(World.spawnNPC(6747, new WorldTile(2730, 5730, 0), -1, true, true));
			minions.add(World.spawnNPC(6748, new WorldTile(2730, 5734, 0), -1, true, true));
			minions.add(World.spawnNPC(6749, new WorldTile(2730, 5739, 0), -1, true, true));
			
			wave++;
		}
		for (NPC npc : minions) {
			if (!(npc.getId() >= 6747 && npc.getId() <= 6749))
					npc.setHitpoints(npc.getHitpoints());
			npc.setForceAgressive(true);
			npc.setForceTargetDistance(64);
			npc.setNextGraphics(new Graphics(1588, 0, 0));
			npc.faceEntity(this);
			npc.checkAgressivity();
			if (npc instanceof Kraken)
				((Kraken)npc).forceAgro();
			else if (npc instanceof Strykewyrm)
				((Strykewyrm)npc).setEmerged(true);
			npc.setForceLootshare(true);
		}
	}
	
	@Override
	public void setHitpoints(int hitpoints) {
		checkMinions();
		if (hitpoints == 0 && isMinionsAlive())
			hitpoints = 1;
		super.setHitpoints(hitpoints);
	}
	
	
	
	

}
