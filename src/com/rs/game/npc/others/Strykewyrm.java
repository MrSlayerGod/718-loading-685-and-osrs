package com.rs.game.npc.others;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Strykewyrm extends NPC {

	private boolean hasEmerged;

	public Strykewyrm(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (isDead())
			return;
		if (hasEmerged) {
			if (!isUnderCombat()) {
				if (isCantInteract())
					return;
				setNextAnimation(new Animation(12796));
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						reset();
					}
				});
			}
		}
	}

	@Override
	public void reset() {
		if (hasEmerged)
			setNextNPCTransformation(getId() - 1);
		hasEmerged = false;
		setCantInteract(false);
		super.reset();
	}

	public static void handleStomping(final Player player, final NPC npc) {
		if (npc.isCantInteract() || ((Strykewyrm) npc).hasEmerged)
			return;
		if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
			if (player.getAttackedBy() != npc && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
				player.getPackets().sendGameMessage("You are already in combat.");
				return;
			}
			if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
				if (npc.getAttackedBy() instanceof NPC) {
					npc.setAttackedBy(player);
				} else {
					player.getPackets().sendGameMessage("That npc is already in combat.");
					return;
				}
			}
		}
		int requiredLevel = npc.isXmas() ? 1 : npc.getId() == 9462 ? 93 : npc.getId() == 9464 ? 77 : npc.getId() == 9466 ? 73 : 1;
		if (player.getSkills().getLevel(Skills.SLAYER) < requiredLevel) {
			player.getPackets().sendGameMessage("You need a Slayer level of at least "+requiredLevel+"to fight a Stykewyrm.");
			return;
		}
		player.setNextAnimation(new Animation(4278));
		npc.setCantInteract(true);
		WorldTasksManager.schedule(new WorldTask() {

			int ticks;
			@Override
			public void run() {
				ticks++;
				if (ticks == 2) {
					npc.setNextAnimation(new Animation(12795));
					npc.setNextNPCTransformation(npc.getId() + 1);
				} else if (ticks == 4) {
					((Strykewyrm) npc).setEmerged(true);
					npc.getCombat().setTarget(player);
					npc.setCantInteract(false);
					stop();
					return;
				}
			}
		}, 0, 0);
	}

	public void setEmerged(boolean hasEmerged) {
		this.hasEmerged = hasEmerged;
	}

	public boolean hasEmerged() {
		return hasEmerged;
	}
}
