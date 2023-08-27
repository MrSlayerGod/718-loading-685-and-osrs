package com.rs.game.npc.others;

import java.util.List;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.OwnedObjectManager;
import com.rs.game.player.OwnedObjectManager.ConvertEvent;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.TrapAction.HunterNPC;
import com.rs.game.player.actions.TrapAction.Traps;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class HunterTrapNPC extends NPC {

	private Traps trap;
	private HunterNPC hNPC;

	private WorldObject o;
	private int captureTicks;

	public HunterTrapNPC(HunterNPC hNPC, int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		this.hNPC = hNPC;
		this.trap = hNPC.getTrap();
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (captureTicks > 0) {
			captureTicks++;
			if (captureTicks == 5) {
				if (hNPC.equals(HunterNPC.CRIMSON_SWIFT) || hNPC.equals(HunterNPC.GOLDEN_WARBLER) || hNPC.equals(HunterNPC.COPPER_LONGTAIL) || hNPC.equals(HunterNPC.CERULEAN_TWITCH) || hNPC.equals(HunterNPC.TROPICAL_WAGTAIL) || hNPC.equals(HunterNPC.WIMPY_BIRD))
					addWalkSteps(o.getX(), o.getY(), -1, false);
			} else if (captureTicks == 6) {
				setNextAnimation(new Animation(hNPC.getIds()[1]));
			} else if (captureTicks == 7) {// up to five
				if (!OwnedObjectManager.convertIntoObject(o, new WorldObject(hNPC.getIds()[0], o.getType(), o.getRotation(), new WorldTile(o.getTileHash())), new ConvertEvent() {

					@Override
					public boolean canConvert(final Player player) {
						if (player == null || isDead())
							return false;
						int currentLevel = player.getSkills().getLevel(Skills.HUNTER), lureLevel = hNPC.getLureLevel();
						double ratio = ((double) (trap.getRequirementLevel() + 20) / lureLevel) * currentLevel;
						if (currentLevel < lureLevel || ratio < Utils.random(100))
							return false;
						return true;
					}
				})) {
					int anim = hNPC.getIds()[2];
					if (anim != -1)
						setNextAnimation(new Animation(anim));
					OwnedObjectManager.convertIntoObject(o, new WorldObject(trap.getIds()[2], o.getType(), o.getRotation(), new WorldTile(o.getTileHash())), null);
				} else
					setRespawnTask();
			} else if (captureTicks == 8) {
				setCantInteract(false);
			} else if (captureTicks == 10) {
				o = null;
				captureTicks = 0;
			}
			return;
		}

		if (o != null || hasFinished())
			return;
		List<WorldObject> objects = World.getRegion(getRegionId()).getSpawnedObjects();
		if (objects == null)
			return;
		for (final WorldObject o : objects) {
			if (o.getId() != trap.getIds()[1] || !withinDistance(o, 4) || Utils.random(30) != 0)//We don't want it instant or too far ^.^
				continue;
			this.o = o;
			this.captureTicks = 1;
			setCantInteract(true);
			resetWalkSteps();
			calcFollow(o, true);
			faceObject(o);
			break;
		}
	}
}