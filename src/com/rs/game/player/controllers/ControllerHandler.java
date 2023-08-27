package com.rs.game.player.controllers;

import java.util.HashMap;

import com.rs.game.minigames.BrimhavenAgility;
import com.rs.game.minigames.PuroPuro;
import com.rs.game.minigames.WarriorsGuild;
import com.rs.game.minigames.clanwars.ClanWarRequestController;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.minigames.clanwars.WarControler;
import com.rs.game.minigames.duel.DuelArena;
import com.rs.game.minigames.duel.DuelControler;
import com.rs.game.minigames.stealingcreation.StealingCreationController;
import com.rs.game.minigames.stealingcreation.StealingCreationLobbyController;
import com.rs.game.npc.others.zalcano.ZalcanoController;
import com.rs.game.player.content.surpriseevents.LastManStandingController;
import com.rs.game.player.content.surpriseevents.TeamVsTeamController;
import com.rs.game.player.controllers.bossInstance.BossInstanceController;
import com.rs.game.player.controllers.bossInstance.CorporealBeastInstanceController;
import com.rs.game.player.controllers.bossInstance.DagannothKingsInstanceController;
import com.rs.game.player.controllers.bossInstance.KalphiteQueenInstanceController;
import com.rs.game.player.controllers.bossInstance.KrakenInstanceController;
import com.rs.game.player.controllers.castlewars.CastleWarsPlaying;
import com.rs.game.player.controllers.castlewars.CastleWarsWaiting;
import com.rs.game.player.controllers.events.DeathEvent;
import com.rs.game.player.controllers.fightpits.FightPitsArena;
import com.rs.game.player.controllers.fightpits.FightPitsLobby;
import com.rs.game.player.controllers.pestcontrol.PestControlGame;
import com.rs.game.player.controllers.pestcontrol.PestControlLobby;
import com.rs.game.player.controllers.pktournament.PkTournamentGame;
import com.rs.game.player.controllers.pktournament.PkTournamentLobby;
import com.rs.game.player.controllers.pktournament.PkTournamentSpectating;
import com.rs.utils.Logger;

public class ControllerHandler {

	private static final HashMap<Object, Class<? extends Controller>> handledControlers = new HashMap<Object, Class<? extends Controller>>();

	@SuppressWarnings("unchecked")
	public static final void init() {
		try {
			Class<Controller> value1 = (Class<Controller>) Class.forName(Wilderness.class.getCanonicalName());
			handledControlers.put("Wilderness", value1);
			Class<Controller> value2 = (Class<Controller>) Class.forName(Kalaboss.class.getCanonicalName());
			handledControlers.put("Kalaboss", value2);
			Class<Controller> value4 = (Class<Controller>) Class.forName(GodWars.class.getCanonicalName());
			handledControlers.put("GodWars", value4);
			Class<Controller> value5 = (Class<Controller>) Class.forName(ZGDControler.class.getCanonicalName());
			handledControlers.put("ZGDControler", value5);
			Class<Controller> value9 = (Class<Controller>) Class.forName(DuelArena.class.getCanonicalName());
			handledControlers.put("DuelArena", value9);
			Class<Controller> value10 = (Class<Controller>) Class.forName(DuelControler.class.getCanonicalName());
			handledControlers.put("DuelControler", value10);
			Class<Controller> value14 = (Class<Controller>) Class.forName(DTControler.class.getCanonicalName());
			handledControlers.put("DTControler", value14);
			Class<Controller> value17 = (Class<Controller>) Class.forName(CastleWarsPlaying.class.getCanonicalName());
			handledControlers.put("CastleWarsPlaying", value17);
			Class<Controller> value18 = (Class<Controller>) Class.forName(CastleWarsWaiting.class.getCanonicalName());
			handledControlers.put("CastleWarsWaiting", value18);
			handledControlers.put("DungeonControler", (Class<Controller>) Class.forName(DungeonController.class.getCanonicalName()));
			handledControlers.put("clan_wars_request", (Class<Controller>) Class.forName(ClanWarRequestController.class.getCanonicalName()));
			handledControlers.put("clan_war", (Class<Controller>) Class.forName(WarControler.class.getCanonicalName()));
			handledControlers.put("clan_wars_ffa", (Class<Controller>) Class.forName(FfaZone.class.getCanonicalName()));
			handledControlers.put("NomadsRequiem", (Class<Controller>) Class.forName(NomadsRequiem.class.getCanonicalName()));
			handledControlers.put("BorkController", BorkController.class);
			handledControlers.put("BrimhavenAgility", (Class<Controller>) Class.forName(BrimhavenAgility.class.getCanonicalName()));
			handledControlers.put("FightCavesControler", (Class<Controller>) Class.forName(FightCaves.class.getCanonicalName()));
			handledControlers.put("TheHorde", TheHorde.class);
			
			handledControlers.put("FightKilnControler", (Class<Controller>) Class.forName(FightKiln.class.getCanonicalName()));
			handledControlers.put("FightPitsLobby", (Class<Controller>) Class.forName(FightPitsLobby.class.getCanonicalName()));
			handledControlers.put("FightPitsArena", (Class<Controller>) Class.forName(FightPitsArena.class.getCanonicalName()));
			handledControlers.put("PestControlGame", (Class<Controller>) Class.forName(PestControlGame.class.getCanonicalName()));
			handledControlers.put("PestControlLobby", (Class<Controller>) Class.forName(PestControlLobby.class.getCanonicalName()));
			handledControlers.put("Barrows", (Class<Controller>) Class.forName(Barrows.class.getCanonicalName()));
			handledControlers.put("Falconry", (Class<Controller>) Class.forName(Falconry.class.getCanonicalName()));
			handledControlers.put("QueenBlackDragonControler", (Class<Controller>) Class.forName(QueenBlackDragonController.class.getCanonicalName()));
			handledControlers.put("HouseControler", (Class<Controller>) Class.forName(HouseControler.class.getCanonicalName()));
			handledControlers.put("RuneSpanControler", (Class<Controller>) Class.forName(RunespanControler.class.getCanonicalName()));
			handledControlers.put("DeathEvent", (Class<Controller>) Class.forName(DeathEvent.class.getCanonicalName()));
			handledControlers.put("SorceressGarden", (Class<Controller>) Class.forName(SorceressGarden.class.getCanonicalName()));
			handledControlers.put("CrucibleControler", (Class<Controller>) Class.forName(CrucibleControler.class.getCanonicalName()));
			handledControlers.put("StealingCreationLobby", (Class<Controller>) Class.forName(StealingCreationLobbyController.class.getCanonicalName()));
			handledControlers.put("StealingCreationGame", (Class<Controller>) Class.forName(StealingCreationController.class.getCanonicalName()));
			handledControlers.put("NewPlayerController", (Class<Controller>) Class.forName(NewPlayerController.class.getCanonicalName()));
			handledControlers.put("PuroPuro", (Class<Controller>) Class.forName(PuroPuro.class.getCanonicalName()));
			handledControlers.put("RuneEssenceController", (Class<Controller>) Class.forName(RuneEssenceController.class.getCanonicalName()));
			handledControlers.put("UnderGroundDungeon", (Class<Controller>) Class.forName(UnderGroundDungeon.class.getCanonicalName()));
			handledControlers.put("TerrorDogsTarnsLairController", (Class<Controller>) Class.forName(TerrorDogsTarnsLairController.class.getCanonicalName()));
			handledControlers.put("WarriorsGuild", (Class<Controller>) Class.forName(WarriorsGuild.class.getCanonicalName()));
			handledControlers.put("JadinkoLair", (Class<Controller>) Class.forName(JadinkoLair.class.getCanonicalName()));
			handledControlers.put("KuradalDungeon", (Class<Controller>) Class.forName(KuradalDungeon.class.getCanonicalName()));
			handledControlers.put("SawmillController", (Class<Controller>) Class.forName(SawmillController.class.getCanonicalName()));
			handledControlers.put("NewSpawnPlayerController", (Class<Controller>) Class.forName(NewSpawnPlayerController.class.getCanonicalName()));
			handledControlers.put("LastManStandingController", LastManStandingController.class);
			handledControlers.put("TeamVsTeamController", TeamVsTeamController.class);
			handledControlers.put("ZulrahShrine", ZulrahShrine.class);
			handledControlers.put("Inferno", Inferno.class);
			handledControlers.put("SkotizoLair", SkotizoLair.class);
			handledControlers.put("GiantMole", GiantMole.class);
			handledControlers.put("VorkathLair", VorkathLair.class);
			handledControlers.put("HydraLair", HydraLair.class);
			handledControlers.put("GrotesqueGuardianLair", GrotesqueGuardianLair.class);
			handledControlers.put("ChamberOfXericController", ChambersOfXericController.class);
			handledControlers.put("TheatreOfBloodController", TheatreOfBloodController.class);
			handledControlers.put("TheNightmareInstance", TheNightmareInstance.class);
			handledControlers.put("AerialFishing", AerialFishing.class);
			handledControlers.put("ZalcanoController", ZalcanoController.class);
			handledControlers.put("PkTournamentLobby", PkTournamentLobby.class);
			handledControlers.put("PkTournamentGame", PkTournamentGame.class);
			handledControlers.put("PkTournamentSpectating", PkTournamentSpectating.class);
			
			//boss instance
			handledControlers.put("BossInstanceController", BossInstanceController.class);
			handledControlers.put("KalphiteQueenInstanceController", KalphiteQueenInstanceController.class);
			handledControlers.put("DagannothKingsInstanceController", DagannothKingsInstanceController.class);
			handledControlers.put("CorporealBeastInstanceController", CorporealBeastInstanceController.class);
			handledControlers.put("KrakenInstanceController", KrakenInstanceController.class);
		
			// handledControlers.put("SlaughterFieldsControler", (Class<Controller>) Class.forName(SlaughterFieldsControler.class.getCanonicalName()));
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

	public static final void reload() {
		handledControlers.clear();
		init();
	}

	public static final Controller getControler(Object key) {
		if (key instanceof Controller)
			return (Controller) key;
		Class<? extends Controller> classC = handledControlers.get(key);
		if (classC == null)
			return null;
		try {
			return classC.newInstance();
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
}
