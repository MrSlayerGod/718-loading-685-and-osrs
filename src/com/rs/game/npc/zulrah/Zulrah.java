/**
 * 
 */
package com.rs.game.npc.zulrah;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.zulrah.action.MagicAttack;
import com.rs.game.npc.zulrah.action.MeleeAttack;
import com.rs.game.npc.zulrah.action.RangeAttack;
import com.rs.game.npc.zulrah.action.SpawnCloud;
import com.rs.game.npc.zulrah.action.SpawnSpawn;
import com.rs.game.npc.zulrah.action.SwitchColor;
import com.rs.game.npc.zulrah.action.ZulrahAction;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.ZulrahShrine;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Nov 4, 2017
 */
@SuppressWarnings("serial")
public class Zulrah extends NPC {

	
	private ZulrahAction[][][] ACTIONS = {
			{ //rotation 1
				{new SwitchColor(ZulrahColor.GREEN, ZulrahPosition.NORTH), new RangeAttack(), new RangeAttack(), new RangeAttack(), new RangeAttack(), new RangeAttack(), new SpawnCloud(ZulrahSpawnPosition.SOUTH_WEST),  new SpawnCloud(ZulrahSpawnPosition.SOUTH_EAST), new SpawnCloud(ZulrahSpawnPosition.EAST),  new SpawnCloud(ZulrahSpawnPosition.WEST) }, //1
				{new SwitchColor(ZulrahColor.RED, ZulrahPosition.NORTH), new MeleeAttack(), new MeleeAttack() }, //2
				{new SwitchColor(ZulrahColor.BLUE, ZulrahPosition.NORTH), new MagicAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), }, //3
				{new SwitchColor(ZulrahColor.GREEN, ZulrahPosition.SOUTH), new RangeAttack(), new RangeAttack(), new RangeAttack(), new RangeAttack(), new RangeAttack(), new SpawnSpawn(ZulrahSpawnPosition.SOUTH_WEST), new SpawnCloud(ZulrahSpawnPosition.SOUTH_WEST), new SpawnCloud(ZulrahSpawnPosition.SOUTH_EAST), new SpawnSpawn(ZulrahSpawnPosition.EAST) }, //4
				{new SwitchColor(ZulrahColor.RED, ZulrahPosition.NORTH), new MeleeAttack(), new MeleeAttack()}, //5
				{new SwitchColor(ZulrahColor.BLUE, ZulrahPosition.WEST), new MagicAttack(), new MagicAttack(), new MagicAttack(), new MagicAttack(),new MagicAttack(), }, //6
				{new SwitchColor(ZulrahColor.GREEN, ZulrahPosition.SOUTH), new SpawnCloud(ZulrahSpawnPosition.SOUTH_EAST),  new SpawnCloud(ZulrahSpawnPosition.SOUTH_WEST), new SpawnCloud(ZulrahSpawnPosition.WEST), new SpawnSpawn(ZulrahSpawnPosition.SOUTH_EAST), new SpawnSpawn(ZulrahSpawnPosition.SOUTH_EAST)}, //7
				{new SwitchColor(ZulrahColor.BLUE, ZulrahPosition.SOUTH), new RangeAttack(), new MagicAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack() , new SpawnSpawn(ZulrahSpawnPosition.WEST), new SpawnCloud(ZulrahSpawnPosition.SOUTH_WEST), new SpawnSpawn(ZulrahSpawnPosition.WEST), new SpawnCloud(ZulrahSpawnPosition.SOUTH_EAST), new SpawnCloud(ZulrahSpawnPosition.EAST), new SpawnSpawn(ZulrahSpawnPosition.WEST)}, //8
				{new SwitchColor(ZulrahColor.GREEN, ZulrahPosition.WEST), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new SpawnSpawn(ZulrahSpawnPosition.SOUTH_EAST), new SpawnSpawn(ZulrahSpawnPosition.SOUTH_WEST), new SpawnCloud(ZulrahSpawnPosition.SOUTH_WEST),  new SpawnCloud(ZulrahSpawnPosition.SOUTH_EAST), new SpawnCloud(ZulrahSpawnPosition.EAST),  new SpawnCloud(ZulrahSpawnPosition.WEST) }, //9
				{new SwitchColor(ZulrahColor.RED, ZulrahPosition.NORTH), new MeleeAttack(), new MeleeAttack() } //10
				},
			{ //rotation 2
				{new SwitchColor(ZulrahColor.GREEN, ZulrahPosition.NORTH), new RangeAttack(), new RangeAttack(), new RangeAttack(), new RangeAttack(), new RangeAttack(), new SpawnCloud(ZulrahSpawnPosition.SOUTH_WEST),  new SpawnCloud(ZulrahSpawnPosition.SOUTH_EAST), new SpawnCloud(ZulrahSpawnPosition.EAST),  new SpawnCloud(ZulrahSpawnPosition.WEST) }, //1
				{new SwitchColor(ZulrahColor.RED, ZulrahPosition.NORTH), new MeleeAttack(), new MeleeAttack() }, //2
				{new SwitchColor(ZulrahColor.BLUE, ZulrahPosition.NORTH), new MagicAttack(), new MagicAttack(), new MagicAttack(), new MagicAttack(), new MagicAttack(), new MagicAttack() }, //3
				{new SwitchColor(ZulrahColor.GREEN, ZulrahPosition.WEST), new SpawnCloud(ZulrahSpawnPosition.EAST), new SpawnCloud(ZulrahSpawnPosition.SOUTH_EAST), new SpawnCloud(ZulrahSpawnPosition.SOUTH_WEST), new SpawnSpawn(ZulrahSpawnPosition.SOUTH_EAST), new SpawnSpawn(ZulrahSpawnPosition.SOUTH_WEST) }, //4
				{new SwitchColor(ZulrahColor.BLUE, ZulrahPosition.SOUTH), new MagicAttack(), new MagicAttack(), new MagicAttack(), new MagicAttack(), new MagicAttack() , new SpawnSpawn(ZulrahSpawnPosition.SOUTH_EAST), new SpawnSpawn(ZulrahSpawnPosition.SOUTH_WEST), new SpawnCloud(ZulrahSpawnPosition.SOUTH_EAST), new SpawnCloud(ZulrahSpawnPosition.SOUTH_WEST), new SpawnCloud(ZulrahSpawnPosition.EAST)}, //5
				{new SwitchColor(ZulrahColor.RED, ZulrahPosition.NORTH), new MeleeAttack(), new MeleeAttack() }, //6
				{new SwitchColor(ZulrahColor.GREEN, ZulrahPosition.EAST), new RangeAttack(), new RangeAttack(), new RangeAttack(), new RangeAttack(), new RangeAttack(), new RangeAttack()}, //7
				{new SwitchColor(ZulrahColor.BLUE, ZulrahPosition.SOUTH), new MagicAttack(), new MagicAttack(), new MagicAttack(), new MagicAttack(), new MagicAttack() , new SpawnSpawn(ZulrahSpawnPosition.SOUTH_EAST), new SpawnSpawn(ZulrahSpawnPosition.SOUTH_WEST), new SpawnCloud(ZulrahSpawnPosition.SOUTH_EAST), new SpawnCloud(ZulrahSpawnPosition.SOUTH_WEST), new SpawnCloud(ZulrahSpawnPosition.EAST)}, //8
				{new SwitchColor(ZulrahColor.GREEN, ZulrahPosition.WEST), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new RangeAttack(), new MagicAttack(), new SpawnSpawn(ZulrahSpawnPosition.SOUTH_EAST), new SpawnSpawn(ZulrahSpawnPosition.SOUTH_WEST), new SpawnCloud(ZulrahSpawnPosition.SOUTH_WEST),  new SpawnCloud(ZulrahSpawnPosition.SOUTH_EAST), new SpawnCloud(ZulrahSpawnPosition.EAST),  new SpawnCloud(ZulrahSpawnPosition.WEST) }, //9
				{new SwitchColor(ZulrahColor.RED, ZulrahPosition.NORTH), new MeleeAttack(), new MeleeAttack() } //10
				}
			
			
	};
	
	public static final int ID = 22042;
	
	private ZulrahShrine shrine;
	
	private int rotation;
	private int wave;
	private int action;
	private int delay;
	private boolean firstWave;
	
	private List<WorldObject> objects;

	private boolean hardMode;
	
	public Zulrah(ZulrahShrine shrine) {
		super(ID, shrine.getWorldTileReal(ZulrahPosition.NORTH.getTile()), -1, true, true);
		firstWave = true;
		rotation = Utils.random(ACTIONS.length);
		objects = new CopyOnWriteArrayList<WorldObject>();
		this.shrine = shrine;
		setDropRateFactor(4);
		setForceMultiArea(true);
		setIntelligentRouteFinder(true);
	}

	public void setHardMode() {
		hardMode = true;
		this.setDifficultyMultiplier(1.5);
	}

	@Override
	public double getMagePrayerMultiplier() {
		return hardMode ? 0.3 : 0;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return hardMode ? 0.3 : 0;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return hardMode ? 0.3 : 0;
	}
	
	@Override
	public void processNPC() {
		if (shrine == null || delay > 0 || isDead()) {
			delay--;
			return;
		}
		Player player = this.getShrine().getPlayer();
		if (player.hasFinished() || !player.withinDistance(this, 64) || !shrine.isRunning()) //just to prevent glitching
			return;
		delay = ACTIONS[rotation][wave][action].use(this);
		action++;
		if (action >= ACTIONS[rotation][wave].length) {
			action = 0;
			wave = (wave+1) % ACTIONS[rotation].length;
			if (wave == 0) //repeat
				rotation = Utils.random(ACTIONS.length);
			firstWave = false;
		}
	}

	/**
	 * @return the objects
	 */
	public List<WorldObject> getObjects() {
		return objects;
	}

	public boolean isFirstWave() {
		return firstWave;
	}

	/**
	 * @author dragonkk(Alex)
	 * Nov 5, 2017
	 * @return
	 */
	public ZulrahShrine getShrine() {
		return shrine;
	}
	
	@Override
	public void setTarget(Entity entity) {
		
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (hit.getLook() == HitLook.MELEE_DAMAGE)
			hit.setDamage(hit.getDamage()/2);
		super.handleIngoingHit(hit);
	}


	public void drop() {
		super.drop();
		World.addGroundItem(new Item(42938, 1), new WorldTile(shrine.getPlayer()), shrine.getPlayer(), true, 60);
	}

}
