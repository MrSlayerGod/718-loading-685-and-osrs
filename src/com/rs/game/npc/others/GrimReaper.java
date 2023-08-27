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
import com.rs.game.player.Player;
import com.rs.game.player.content.box.HalloweenBox;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.controllers.TheHorde;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class GrimReaper extends NPC {
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
	
	private static final int[] PVM_RARE = {25534, 25535, 25536, 25537, 25538, 25539, 25540, 25541};

	
	public GrimReaper(int id, WorldTile tile) {
		super(id, tile, -1, true, true);
		setDirection(Utils.getAngle(1, 0));
		setCantFollowUnderCombat(true);
		setCapDamage(1000);
		setLureDelay(0);
		minions = new ArrayList<NPC>();
	}
	
	@Override
	public void drop() {
		for (Entity entity : getReceivedDamageSources()) {
			if (entity instanceof Player) {
				int damage = getDamageReceived(entity);
				if (((Player) entity).getControlerManager().getControler() instanceof TheHorde && getReceivedDamageSources().size() == 1)
					return;
				if (damage >= 1000) {
					Player player = (Player) entity;
					LuckyPets.checkPet(player, LuckyPets.LuckyPet.HWEEN_2020);

					if (!player.withinDistance(this))
						continue;

					player.getPackets().sendGameMessage("You receive a reward for your participation in the halloween boss event.");
					Item reward = OVERALL_REWARDS[Utils.random(OVERALL_REWARDS.length)];
					player.getInventory().addItemDrop(reward.getId(), reward.getAmount());
					player.getInventory().addItemDrop(995, 3000000);

					if (Utils.random(10) == 0 && !player.containsItem(25543)) {
						int drop = 25543;
					/*	World.sendNews(player, Utils.formatPlayerNameForDisplay(player.getDisplayName())
						+ " just received <img=11><col=00ACE6>" +new Item(drop).getName()+" from <col=ff9933>Grim Reaper!", 0);*/
						player.getInventory().addItemDrop(drop, 1);
					}
					if (Utils.random(150) == 0) {
						int drop = HalloweenBox.DYE;
						World.sendNews(player, "<col=ff981f><shad=900090>RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
								+ " just received <img=11><col=00ACE6>" +new Item(drop).getName()+" from <col=ff9933>Grim Reaper!", 0);
						player.getInventory().addItemDrop(drop, 1);
					}

					if (Utils.random(180) == 0) {
						int drop = PVM_RARE[Utils.random(PVM_RARE.length)];
						World.sendNews(player, "RARE! "+Utils.formatPlayerNameForDisplay(player.getDisplayName())
						+ " just received <img=11><col=00ACE6>" +new Item(drop).getName()+" from <col=ff9933>Grim Reaper!", 0);
						player.getInventory().addItemDrop(drop, 1);
					}
				}
			}
		}
		super.drop();
	}
	
	@Override
	public void setNextFaceEntity(Entity entity) {
		
	}
	
	@Override
	public void setNextFaceRectanglePrecise(WorldTile base, int sizeX, int sizeY) {
		
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
			setNextForceTalk(new ForceTalk("Don't get burnt, dance with the fire of death!"));
			minions.add(World.spawnNPC(50, new WorldTile(4087, 5230, 0), -1, true, true));
			minions.add(World.spawnNPC(50, new WorldTile(4099, 5230, 0), -1, true, true));
			minions.add(World.spawnNPC(50, new WorldTile(4093, 5225, 0), -1, true, true));
			wave++;
		} else if (wave == 1 && getHitpoints() < getMaxHitpoints() * 0.7) {
			setNextForceTalk(new ForceTalk("Fear the wrath of my firehawks!"));
			minions.add(World.spawnNPC(8549, new WorldTile(4089, 5232, 0), -1, true, true));
			minions.add(World.spawnNPC(8549, new WorldTile(4097, 5232, 0), -1, true, true));
			minions.add(World.spawnNPC(8549, new WorldTile(4089, 5229, 0), -1, true, true));
			minions.add(World.spawnNPC(8549, new WorldTile(4097, 5229, 0), -1, true, true));
			wave++;
		} else if (wave == 2 && getHitpoints() < getMaxHitpoints() * 0.5) {
			setNextForceTalk(new ForceTalk("It's time to anchor down brave peasant!"));
			minions.add(World.spawnNPC(5666, new WorldTile(4089, 5233, 0), -1, true, true));
			minions.add(World.spawnNPC(5666, new WorldTile(4099, 5233, 0), -1, true, true));
			minions.add(World.spawnNPC(5666, new WorldTile(4089, 5230, 0), -1, true, true));
			minions.add(World.spawnNPC(5666, new WorldTile(4099, 5230, 0), -1, true, true));
			minions.add(World.spawnNPC(5666, new WorldTile(4096, 5227, 0), -1, true, true));
			minions.add(World.spawnNPC(5666, new WorldTile(4092, 5227, 0), -1, true, true));
			wave++;
		} else if (wave == 3 && getHitpoints() < getMaxHitpoints() * 0.3) {
			setNextForceTalk(new ForceTalk("My dogs will crush you alive!!"));
			minions.add(World.spawnNPC(2745, new WorldTile(4088, 5231, 0), -1, true, true));
			minions.add(World.spawnNPC(2745, new WorldTile(4098, 5231, 0), -1, true, true));
			minions.add(World.spawnNPC(2745, new WorldTile(4093, 5225, 0), -1, true, true));
			wave++;
		} else if (wave == 4 && getHitpoints() < getMaxHitpoints() * 0.1) {
			setNextForceTalk(new ForceTalk("Face my wrath!!"));
			minions.add(World.spawnNPC(28097, new WorldTile(4091, 5229, 0), -1, true, true));
			wave++;
		}
		for (NPC npc : minions) {
			//npc.setHitpoints(npc.getHitpoints() * 3);
			npc.setForceAgressive(true);
			npc.setForceTargetDistance(64);
			npc.setNextGraphics(new Graphics(1588, 0, 0));
			npc.faceEntity(this);
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
