/**
 * 
 */
package com.rs.game.player.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import com.rs.Settings;
import com.rs.executor.GameExecutorManager;
import com.rs.game.Animation;
import com.rs.game.NewForceMovement;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.map.MapInstance;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.NPC;
import com.rs.game.npc.inferno.InfernoJad;
import com.rs.game.npc.inferno.InfernoNPC;
import com.rs.game.npc.inferno.JalAk;
import com.rs.game.npc.inferno.Pillar;
import com.rs.game.npc.inferno.Shield;
import com.rs.game.npc.inferno.TzalZuk;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.Summoning;
import com.rs.game.player.content.collectionlog.CategoryType;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Nov 23, 2017
 */
public class Inferno extends Controller {


	private static final int THHAAR_MEJ_JAL = 27690;
	
	private static final WorldTile OUTSIDE = new WorldTile(4571, 5257, 0), MIDDLE = new WorldTile(2270, 5279, 0);
	
	private static final WorldTile[] PILAR_TILES = {new WorldTile(2270, 5272, 0), new WorldTile(2258, 5286, 0), new WorldTile(2275, 5287, 0)};
	
	private static final WorldTile[] SPAWNS = {new WorldTile(2259, 5288, 0), //1
			 new WorldTile(2278, 5288, 0), //2
			new WorldTile(2262, 5282, 0), //3
			new WorldTile(2279, 5281, 0), //4
			new WorldTile(2269, 5276, 0), //5
			new WorldTile(2263, 5271, 0), //6
			new WorldTile(2258, 5266, 0), //7
			new WorldTile(2274, 5266, 0), //8
			new WorldTile(2279, 5271, 0), //9
			};
	
	private static final WorldTile[] JAD_SPAWN = {new WorldTile(2273, 5284, 0), new WorldTile(2264, 5284, 0), new WorldTile(2268, 5271, 0)};
	
	private static final WorldTile NIMBLER_SPAWN = new WorldTile(2268, 5279, 0);
	
	public static final int NIBBLER = 27691
			, BAT = 27692 //done
			, BLOB = 27693
			, MELEE = 27697
			, RANGER = 27698 //done
			, MAGE = 27699 //done
			, JAD = 27700 //done
			, HEALER  = 27701; //not used
	
	private static final int[][] WAVES = 
		{
				{BAT}, //1
				{BAT, BAT}, //2
				{}, //3
				{BLOB}, //4
				{BAT, BLOB}, //5
				{BAT, BAT, BLOB}, //6
				{BLOB, BLOB}, //7
				{}, //8
				{MELEE}, //9
				{BAT, MELEE}, //10
				{BAT, BAT, MELEE}, //11
				{BLOB, MELEE}, //12
				{BAT, BLOB, MELEE}, //13
				{BAT, BAT, BLOB, MELEE}, //14
				{BLOB, BLOB, MELEE}, //15
				{MELEE, MELEE}, //16
				{}, //17
				{RANGER}, //18
				{BAT, RANGER}, //19
				{BAT, BAT, RANGER}, //20
				{BLOB, RANGER}, //21
				{BAT, BLOB, RANGER}, //22
				{BAT, BAT, BLOB, RANGER}, //23
				{BLOB, BLOB, RANGER}, //24
				{MELEE, RANGER}, //25
				{BAT, MELEE, RANGER}, //26
				{BAT, BAT, MELEE, RANGER}, //27
				{BLOB, MELEE, RANGER}, //28
				{BAT, BLOB, MELEE, RANGER}, //29
				{BAT, BAT, BLOB, MELEE, RANGER}, //30
				{BLOB, BLOB, MELEE, RANGER}, //31
				{MELEE, MELEE, RANGER}, //32
				{RANGER, RANGER}, //33
				{}, //34
				{MAGE}, //35
				{BAT, MAGE}, //36
				{BAT, BAT, MAGE}, //37
				{BLOB, MAGE}, //38
				{BAT, BLOB, MAGE}, //39
				{BAT, BAT, BLOB, MAGE}, //40
				{BLOB, BLOB, MAGE}, //41
				{MELEE, MAGE}, //42
				{BAT, MELEE, MAGE}, //43
				{BAT, BAT, MELEE, MAGE}, //44
				{BLOB, MELEE, MAGE}, //45
				{BAT, BLOB, MELEE, MAGE}, //46
				{BAT, BAT, BLOB, MELEE, MAGE}, //47
				{BLOB, BLOB, MELEE, MAGE}, //48
				{MELEE, MELEE, MAGE}, //49
				{RANGER, MAGE}, //50
				{BAT, RANGER, MAGE}, //51
				{BAT, BAT, RANGER, MAGE}, //52
				{BLOB, RANGER, MAGE}, //53
				{BAT, BLOB, RANGER, MAGE}, //54
				{BAT, BAT, BLOB, RANGER, MAGE}, //55
				{BLOB, BLOB, RANGER, MAGE}, //56
				{MELEE, RANGER, MAGE}, //57
				{BAT, MELEE, RANGER, MAGE}, //58
				{BAT, BAT, MELEE, RANGER, MAGE}, //59
				{BLOB, MELEE, RANGER, MAGE}, //60
				{BAT, BLOB, MELEE, RANGER, MAGE}, //61
				{BAT, BAT, BLOB, MELEE, RANGER, MAGE}, //62
				{BLOB, BLOB, MELEE, RANGER, MAGE}, //63
				{MELEE, MELEE, RANGER, MAGE}, //64
				{RANGER, RANGER, MAGE}, //65
				{MAGE, MAGE}, //66
				{JAD}, //67
				{JAD, JAD, JAD}, //68
				{} //69
		};


			
	
	private static final int[] NIBLERS =
		{
			3,//1
			3, //2
			6, //3
			3,//4
			3, //5
			3, //6
			3, //7
			5, //8
			3, //9
			3, //10
			3, //11
			3, //12
			3, //13
			3, //14
			3, //15
			3, //16
			5, //17
			3, //18
			3, //19
			3, //20
			3, //21
			3, //22
			3, //23
			3, //24
			3, //25
			3, //26
			3, //27
			3, //28
			3, //29
			3, //30
			3, //31
			3, //32
			3, //33
			5, //34
			3, //35
			3, //36
			3, //37
			3, //38
			3, //39
			3, //40
			3, //41
			3, //42
			3, //43
			3, //44
			3, //45
			3, //46
			3, //47
			3, //48
			3, //49
			3, //50
			3, //51
			3, //52
			3, //53
			3, //54
			3, //55
			3, //56
			3, //57
			3, //58
			3, //59
			3, //60
			3, //61
			3, //62
			3, //63
			3, //64
			3, //65
			3, //66
		};
	
	private Shield shield;
	
	private void startWave() {
		if (!isRunning())
			return;
		int wave = getCurrentWave();
		player.getPackets().sendGameMessage("<col=FF0040>Wave: "+wave);
		if (wave >= 69) {
			player.lock();
			player.stopAll();
			player.getDialogueManager().startDialogue("SimpleMessage", "A great power is starting to shake the caves...");
			player.getPackets().sendHideIComponent(1186, 7, true);
			FadingScreen.fade(player, () -> {
				if (!isRunning())
					return;
				player.getPackets().sendCameraShake(3, 12, 25, 12, 25);
				player.getPackets().sendBlackOut(2);
				player.getVarsManager().sendVar(1241, 1);
				player.getPackets().sendHideIComponent(1186, 7, false);
				player.closeInterfaces();
				Dialogue.sendNPCDialogueNoContinue(player, THHAAR_MEJ_JAL, Dialogue.NORMAL, "Oh no! TzKal-Zok's prison is breaking down. This not meant to have happened. There's nothing I can do for you now JalYt!");
				WorldTile playerPos = map.getTile(new WorldTile(2271, 5292, 0));
				player.setNextWorldTile(playerPos);
				player.setNextFaceWorldTile(playerPos.transform(0, 1, 0));
				WorldTile lookTo = map.getTile(new WorldTile(2271, 5292, 0));
				player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()),
						Cutscene.getY(player, lookTo.getY()), 2000);
				WorldTile posTile = map.getTile(new WorldTile(2275, 5278, 0));
				player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()), Cutscene.getY(player, posTile.getY()), 4000);
				TzalZuk boss = new TzalZuk(this, map.getTile(new WorldTile(2268, 5302, 0)));
				boss.setNextAnimation(new Animation(27563));
				WorldTasksManager.schedule(new WorldTask() {

					boolean second;
					@Override
					public void run() {
						if (!isRunning()) {
							stop();
							return;
						}
						if (!second) {
							second = true;
							WorldObject object = World.getObjectWithId(map.getTile(new WorldTile(2270, 5299, 0)), 130338);
							if (object != null) 
								World.removeObject(object);
							 object = World.getObjectWithId(map.getTile(new WorldTile(2267, 5304, 1)), 130356);
								if (object != null) 
									World.removeObject(object);
							WorldObject wall;
							World.spawnObject(wall = new WorldObject(130345, 10, 3, map.getTile(new WorldTile(2268, 5300, 0))));
							World.sendObjectAnimation(wall, new Animation(27561));
							World.spawnObject(wall = new WorldObject(130346, 10, 3, map.getTile(new WorldTile(2273, 5300, 0))));
							
							//rocks
							World.spawnObject(new WorldObject(130317, 10, 0, map.getTile(new WorldTile(2274, 5300, 0))));
							World.spawnObject(new WorldObject(130317, 10, 3, map.getTile(new WorldTile(2267, 5300, 0))));
							
							World.spawnObject(new WorldObject(130325, 10, 3, map.getTile(new WorldTile(2266, 5302, 0))));
							World.spawnObject(new WorldObject(130313, 10, 2, map.getTile(new WorldTile(2267, 5302, 0))));

							World.spawnObject(new WorldObject(130313, 10, 0, map.getTile(new WorldTile(2274, 5302, 0))));
							World.spawnObject(new WorldObject(130325, 10, 0, map.getTile(new WorldTile(2275, 5302, 0))));

							
							//130213
							
							
							World.sendObjectAnimation(wall, new Animation(27561));
							shield = new Shield(Inferno.this, map.getTile(new WorldTile(2270, 5300, 0)));
							WorldTile shieldPos = map.getTile(new WorldTile(2270, 5297, 0));
							shield.addWalkSteps(shieldPos.getX(), shieldPos.getY(), 3, false);
						} else {
							stop();
							Dialogue.closeNoContinueDialogue(player);
							//player.getPackets().sendStopCameraShake();
							player.getPackets().sendResetCamera();
							player.getPackets().sendBlackOut(0);
							player.getVarsManager().sendVar(1241, 0);
							player.unlock();
						}
					}
					
				}, 4, 4);
			});
			return;
		}
		
		Pillar pilarTarget = null;
		if (pillars != null) {
			List<Pillar> pillarsL = new ArrayList<Pillar>();
			for (Pillar p : pillars)
				if (p != null && !p.hasFinished())
					pillarsL.add(p);
			if (pillarsL.size() > 0)
				pilarTarget = pillarsL.get(Utils.random(pillarsL.size()));
		}
		
		if (wave-1 < NIBLERS.length) {
			for (int i = 0; i < NIBLERS[wave-1]; i++) {
				//spawn nimblers
				InfernoNPC npc = new InfernoNPC(NIBBLER, map.getTile(NIMBLER_SPAWN.transform(Utils.random(3), Utils.random(3), 0)));
				npc.setTarget(pilarTarget == null ? player : pilarTarget);
				if (pilarTarget != null) {
					npc.setIntelligentRouteFinder(true); //walk under eachother
					npc.setLureDelay(60000);
					npc.setLastAttackByTargetInfinite();
				}
			}
		}
		
		WorldTile[] spawns = wave == 68 ? JAD_SPAWN : SPAWNS;
		if (wave-1 < WAVES.length) { //non boss
			for (int i = 0; i < WAVES[wave-1].length; i++) {
				int id = WAVES[wave-1][i];
				WorldTile tile = map.getTile(spawns[i]);
				InfernoNPC npc = id == BLOB ? new JalAk(tile) : id == JAD ? new InfernoJad(tile, wave == 68 ? 3 : 5) :
						new InfernoNPC(id, tile);
				npc.setTarget(player);
				npc.getCombat().setCombatDelay((i * 4) + 3);
				npc.setLastAttackByTargetInfinite();
			}
		}
	}
	
	
	public static void enter(Player player, boolean testMode) {
		if (Settings.HOSTED && !player.isCompletedFightCaves() && !player.containsItem(6570)) {
			player.getPackets().sendGameMessage("You haven't completed fight caves yet nor have a fire cape!");
			return;
		}
		if (player.getFamiliar() != null || player.getPet() != null || Summoning.hasPouch(player) || Pets.hasPet(player)) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "No Kimit-Zil in the pits! This is a fight for YOU, not your friends!");
			return;
		}
		player.getControlerManager().startControler("Inferno", testMode ? 66 : 1, null, testMode);
	}
	
	private MapInstance map;
	private boolean logoutAtEnd;
	private boolean login;
	private Pillar[] pillars;
	private int timer;
	
	@Override
	public void start() {
		load(false);
	}
	
	public boolean login() {
		load(true);
		return false;
	}
	
	@Override
	public boolean logout() {
		if (!isRunning())  //didnt tp inside anyway
			return false;
		exit(LOGOUT_EXIT);
		return false;
	}
	
	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (!isRunning()) 
			return false;
		if (object.getId() == 130283) {
			exit(NORMAL_EXIT);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (!isRunning()) 
			return false;
		if (interfaceId == 182 && (componentId == 6 || componentId == 13)) {
			if (!logoutAtEnd) {
				logoutAtEnd = true;
				player.getPackets().sendGameMessage("<col=ff0000>You will be logged out automatically at the end of this wave.");
				player.getPackets().sendGameMessage("<col=ff0000>If you log out sooner, you will have to repeat this wave.");
			} else
				player.disconnect(true, true);
			return false;
		}
		return true;
	}
	
	@Override
	public void magicTeleported(int type) {
		exit(TELE_EXIT);
	}
	
	@Override
	public void forceClose() {
		exit(NORMAL_EXIT); // to prevent glitching
	}

	private static final int LOGOUT_EXIT = 0, NORMAL_EXIT = 1, TELE_EXIT = 2;

	public static final int INFERNAL_CAPE = 51295;

	private void exit(int type) {
		if (!isRunning())  //didnt tp inside anyway
			return;
		WorldTile outside = new WorldTile(OUTSIDE, 1); // radomizes alil
		if (type == LOGOUT_EXIT) {
			savePillars();
			player.setLocation(outside);
		} else {
			player.setForceMultiArea(false);
			player.setLargeSceneView(false);
			if (type == NORMAL_EXIT) {
				player.useStairs(-1, outside, 0, 2);
				int wave = getCurrentWave();
				if (wave >= 70) {
					player.reset();
					player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "You are very impressive for a JalYt. You managed to deaf TzKal-Zuk! Please accept this cape as a token of appreciation.");
					player.getPackets().sendGameMessage("You were victorious!!");
					if (!isTestMode()) {
						World.sendNews(player, player.getDisplayName() + " has completed <col=D80000>The Inferno<col=ff8c38>!", 1);
						LuckyPets.checkPet(player, LuckyPet.JAL_NIB_REK, "The Inferno");
						player.getInventory().addItemDrop(INFERNAL_CAPE, 1, outside);
						player.getInventory().addItemDrop(6571, 1, outside);
						player.getInventory().addItemDrop(6529, 32128 * 10, outside);
						player.getInventory().addItemDrop(995, 5000000, outside);
						player.incrementInfernoCompletions();
						player.getCollectionLog().add(CategoryType.MINIGAMES, "The Inferno", new Item(INFERNAL_CAPE));
					}
				} else if (wave == 1)
					player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "Well I suppose you tried... better luck next time.");
				 else {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "Well done in The Inferno, here, take TokKul as reward.");
					if (!isTestMode()) {
						int tokkul = wave * 16064 / WAVES.length;
						player.getInventory().addItemDrop(6529, tokkul * 10, outside);
						if (wave > 10)
							player.getInventory().addItemDrop(995, wave >= 68 ? 2500000 : wave >=  62 ? 1000000 : wave >= 50 ? 500000 : wave >= 34 ? 250000 : 100000);
					}
				}
			}
			removeControler();// remove in logout too to prevent double call
		}
		map.destroy(null);
	}
	
	public boolean isRunning() {
		return map != null && map.getStage() == Stages.RUNNING;
	}
	
	private void setPillars() {
		if (getCurrentWave() >= 67) //no more pillars
			return;
		pillars = new Pillar[PILAR_TILES.length];
		int[] hp = getPillarsHP();
		for (int i = 0; i < pillars.length; i++) {
			if (hp != null && hp[i] <= 0)
				continue;
			pillars[i] = new Pillar(map.getTile(PILAR_TILES[i]));
			if (hp != null && hp[i] > 0)
				pillars[i].setHitpoints(hp[i]);
		}
		GameExecutorManager.slowExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (!isRunning())
					return;
				try {
					for (int i = 0; i < pillars.length; i++) {
						if (pillars[i] == null || pillars[i].hasFinished())
							continue;
						pillars[i].clip(true);
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			});
	}
	
	private void savePillars() {
		if (pillars == null)
			return;
		setARGS();
		int[] hp = new int[pillars.length];
		for (int i = 0; i < pillars.length; i++)
			hp[i] = pillars[i] == null || pillars[i].hasFinished() ? 0 : pillars[i].getHitpoints();
		getArguments()[1] = hp;
	}
	
	@Override
	public void process() {
		if (!isRunning())
			return;
		checkWave();
	}
	
	private boolean delayed;
	
	private int getPillarsCount() {
		int count = 0;
		if (pillars != null) {
			for (Pillar pillar : pillars)
				if (pillar != null && !pillar.hasFinished())
					count++;
		}
		return count;
	}
	
	private void checkWave() {
		if (delayed || player.isLocked() || player.hasFinished())
			return;
		timer++;
		if (timer % 100 == 0)
			playMusic(); // so that music doesnt get replaced
		List<Integer> npcs = World.getRegion(map.getInstanceMapID()).getNPCsIndexes();
		if (npcs == null || (npcs != null && !npcs.isEmpty() && npcs.size()-getPillarsCount() > 0)) 
			return;
		
		int wave = getCurrentWave();
		if (wave >= 69) { //win
			setCurrentWave(70);
			exit(NORMAL_EXIT);
			return;
		}
		if (wave >= 66) { //end of wave 66
			if (pillars != null) {
				for (int i = 0; i < pillars.length; i++) {
					if (pillars[i] != null && !pillars[i].hasFinished() && !pillars[i].isDead()) 
						pillars[i].sendDeath(player);
				}
				pillars = null;
			}
		}
		nextWave();
	}
	
	private void delayWave(boolean start) {
		delayed = true;
		GameExecutorManager.fastExecutor.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					startWave();
					delayed = false;
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, start ? 6000 : 3000);
	}
	
	private void nextWave() {
		if (!isRunning())
			return;
		playMusic();
		int nextWave = getCurrentWave() + 1;
		setCurrentWave(nextWave);
		if (logoutAtEnd) {
			player.disconnect(true, true);
			return;
		}
		player.getPackets().sendGameMessage("Wave completed!");
		delayWave(false);
	}
	
	@Override
	public void moved() {
		if (!isRunning()|| !login)
			return;
		login = false;
		delayWave(true);
	}
	
	private void load(boolean login) {
		player.lock();
		player.stopAll();
		delayed = true;//to avoid starting too early
		map = new MapInstance(280, 656);
		
		Runnable event = new Runnable() {
		
			public void run() {
				GameExecutorManager.slowExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							map.load(() -> {
								setPillars();
								player.useStairs(!login ? 17274 : -1, map.getTile(MIDDLE), 0, (!login ? 5 : 2));
								player.setNextFaceWorldTile(map.getTile(MIDDLE).transform(0, 1, 0));
								player.setForceMultiArea(true);
								player.setLargeSceneView(true);
								if (!login) {
									player.getAppearence().setHidden(false);
									player.getDialogueManager().startDialogue("SimpleMessage", "You hit the ground in the centre of The Inferno.");
									player.getPackets().sendHideIComponent(1186, 7, false);
									delayWave(true);
								} else {
									WorldTasksManager.schedule(new WorldTask() {
										@Override
										public void run() {
											Inferno.this.login = true;
										}
									}, 1);
								}
								playMusic();
							});
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				});
			}
		};
		
		if (!login) {
			player.setNextAnimation(new Animation(6723));
			player.setNextForceMovement(new NewForceMovement(new WorldTile(player), 4, new WorldTile(/*4571*/player.getX(), 5262, 0), 7, Utils.getAngle(0, 1)));
			WorldTasksManager.schedule(new WorldTask() {

				private int stage;
				
				@Override
				public void run() {
					if (stage == 0) {
						player.getDialogueManager().startDialogue("SimpleMessage", "You jump into the fiery cauldron of The Inferno; your heart is pulsating.");
						player.getPackets().sendHideIComponent(1186, 7, true);
						FadingScreen.fade(player, 5400, event);
					} else if (stage == 1) {
						player.getAppearence().setHidden(true);
						player.getDialogueManager().startDialogue("SimpleMessage", "You fall and fall  and feel the temperature rising.");
					} else if (stage == 2)
						player.getDialogueManager().startDialogue("SimpleMessage", "Your heart is in your throat...");
					else {
						stop();
						return;
					}
					stage++;
				}
				
			}, 4, 4);
		} else 
			event.run();
	}
	
	private void setARGS() {
		if (getArguments() == null || getArguments().length == 0)
			this.setArguments(new Object[3]);
	}
	
	public boolean isTestMode() {
		if (getArguments() == null || getArguments().length < 3)
			return false;
		return (Boolean) getArguments()[2];
	}
	
	public int getCurrentWave() {
		if (getArguments() == null || getArguments().length < 1)
			return 1;
		return getArguments()[0] == null ? 1 : (Integer) getArguments()[0];
	}

	public void setCurrentWave(int wave) {
		setARGS();
		getArguments()[0] = wave;
	}
	
	private int[] getPillarsHP() {
		if (getArguments() == null || getArguments().length < 2)
			return null;
		return (int[]) getArguments()[1];
	}
	

	
	private void playMusic() {
		player.getMusicsManager().playOSRSMusic("Inferno");
	}
	
	public MapInstance getMap() {
		return map;
	}
	
	public Shield getShield() {
		return shield;
	}
	
	public void killAll() {
		if (!isRunning())
			return;
		List<Integer> npcs = World.getRegion(map.getInstanceMapID()).getNPCsIndexes();
		if (npcs != null) {
			for (Integer i : npcs) {
				NPC npc = World.getNPCs().get(i);
				if (npc != null && !npc.hasFinished())
					npc.finish();
			}
		}
	}
	
	@Override
	public boolean sendDeath() {
		player.lock(8);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("You have been defeated!");
				} else if (loop == 3) {
					player.reset();
					exit(NORMAL_EXIT);
					player.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}
}
