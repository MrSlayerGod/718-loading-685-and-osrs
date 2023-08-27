package com.rs.game.npc.others;

import com.rs.Settings;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.game.player.content.pet.PetDetails;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ButtonAction;
import com.rs.net.decoders.handlers.ButtonHandler;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.utils.Colour;
import com.rs.utils.Utils;

import java.util.Arrays;

/**
 * Represents a pet.
 * 
 * @author Emperor
 * 
 */
public final class Pet extends NPC {

	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = -2848843157767889742L;

	/**
	 * The owner.
	 */
	private final Player owner;

	/**
	 * The "near" directions.
	 */
	private final int[][] checkNearDirs;

	/**
	 * The item id.
	 */
	private int itemId;

	/**
	 * The pet details.
	 */
	private final PetDetails details;

	/**
	 * The growth rate of the pet.
	 */
	private double growthRate;

	/**
	 * The pets type.
	 */
	private Pets pet;

	/**
	 * Constructs a new {@code Pet} {@code Object}.
	 * 
	 * @param id
	 *            The NPC id.
	 * @param itemId
	 *            The item id.
	 * @param owner
	 *            The owner.
	 * @param tile
	 *            The world tile.
	 */
	public Pet(int id, int itemId, Player owner, WorldTile tile, PetDetails details) {
		super(id, tile, -1, false);
		this.owner = owner;
		this.itemId = itemId;
		this.checkNearDirs = Utils.getCoordOffsetsNear(super.getSize());
		this.details = details;
		this.pet = Pets.forId(itemId);
	//	setRun(true);
		if (pet == Pets.TROLL_BABY && owner.getPetManager().getTrollBabyName() != null) {
			setName(owner.getPetManager().getTrollBabyName());
		}

		if (pet == Pets.OCT_2020_TOP_DOROR_BIGZY_3 || pet == Pets.OCT_2020_TOP_DOROR_BIGZY_2|| pet == Pets.OCT_2020_TOP_DOROR_BIGZY_1) {
			// flowers pet
			anim(912);
		}

		if(pet == Pets.HORROR_LEFT_ARM || pet == Pets.HORROR_RIGHT_ARM || pet == Pets.HORROR_TAIL) {
			owner.sendMessage("The " + getName() + " is boosting your Prayer experience gained by 15%!");
		}
		sendMainConfigurations();
	}

	@Override
	public void setNextWorldTile(WorldTile nextWorldTile) {
		super.setNextWorldTile(nextWorldTile);
	}

	private boolean updateAnim = false;

	@Override
	public void processNPC() {
		if (pet == Pets.OCT_2020_TOP_DOROR_BIGZY_3 || pet == Pets.OCT_2020_TOP_DOROR_BIGZY_2|| pet == Pets.OCT_2020_TOP_DOROR_BIGZY_1) {
			if(getNextWalkDirection() != -1)
				updateAnim = true;
			if(getNextWalkDirection() == -1 && updateAnim) {
				// this npc is flowers that needs a new anim every movement, very annoying
				anim(912);
				updateAnim = false;
			}
		}
		unlockOrb();
		if (pet == Pets.TROLL_BABY || pet.getFood().length > 0) {
			details.updateHunger(0.025);
			owner.getVarsManager().sendVarBit(4286, (int) details.getHunger());
		}
		if (details.getHunger() >= 90.0 && details.getHunger() < 90.025) {
			owner.getPackets().sendGameMessage("<col=ff0000>Your pet is starving, feed it before it runs off.</col>");
		} else if (details.getHunger() == 100.0 && Settings.PET_RUN_AWAY) {
			owner.getPetManager().setNpcId(-1);
			owner.getPetManager().setItemId(-1);
			owner.setPet(null);
			owner.getPetManager().removeDetails(itemId);
			owner.getPackets().sendGameMessage("Your pet has ran away to find some food!");
			switchOrb(false);
			owner.getInterfaceManager().removeExtras();
			owner.getInterfaceManager().openGameTab(4);
			owner.getPackets().sendIComponentSettings(747, 17, 0, 0, 0);
			finish();
			return;
		}
		if (growthRate > 0.000) {
			details.updateGrowth(growthRate);
			owner.getVarsManager().sendVarBit(4285, (int) details.getGrowth());
			if (details.getGrowth() == 100.0) {
				growNextStage();
			}
		}
		if (!withinDistance(owner, 12)) {
			call();
			return;
		}
			sendFollow();
	}

	/**
	 * Grows into the next stage of this pet (if any).
	 */
	public void growNextStage() {
		if (details.getStage() == 3) {
			return;
		}
		if (pet == null) {
			return;
		}
		int npcId = pet.getNpcId(details.getStage() + 1);
		if (npcId < 1) {
			return;
		}
		details.setStage(details.getStage() + 1);
		int itemId = pet.getItemId(details.getStage());
		if (pet.getNpcId(details.getStage() + 1) > 0) {
			details.updateGrowth(-100.0);
		}
		owner.getPetManager().setItemId(itemId);
		owner.getPetManager().setNpcId(npcId);
		finish();
		Pet newPet = new Pet(npcId, itemId, owner, owner, details);
		newPet.growthRate = growthRate;
		owner.setPet(newPet);
		owner.getPackets().sendGameMessage("<col=ff0000>Your pet has grown larger.</col>");
	}

	
	public void interact(Player player, int option) {
		if (this != player.getPet()) {
			player.getPackets().sendGameMessage("This isn't your pet.");
			return;
		}
		String optionName = getDefinitions().actions[option];
		if (optionName == null)
			return;
		if (optionName.equalsIgnoreCase("talk-to") || optionName.equalsIgnoreCase("talk to"))
			talkTo();
		else if (optionName.equalsIgnoreCase("metamorphosis"))
			meta();
		else if (optionName.equalsIgnoreCase("pick-up") || optionName.equalsIgnoreCase("pick up"))
			pickup();
	}



	private Pets getPetMeta() {
		switch (pet) {
		case HORROR_LEFT_ARM:
			return Pets.HORROR_RIGHT_ARM;
		case HORROR_RIGHT_ARM:
			return Pets.HORROR_TAIL;
		case HORROR_TAIL:
			return Pets.HORROR_LEFT_ARM;
		case TEKTINY:
			return Pets.ENRAGED_TEKTINY;
		case VESPINA:
			return Pets.FLYING_VESPINA;
		case OCT_2020_TOP_DOROR_BIGZY_1:
			return Pets.OCT_2020_TOP_DOROR_BIGZY_2;
		case OCT_2020_TOP_DOROR_BIGZY_2:
			return Pets.OCT_2020_TOP_DOROR_BIGZY_3;
		case OCT_2020_TOP_DOROR_BIGZY_3:
			return Pets.OCT_2020_TOP_DOROR_BIGZY_1;
		case OCT_2020_TOP_DOROR_LUCKY_1:
			return Pets.OCT_2020_TOP_DOROR_LUCKY_2;
		case OCT_2020_TOP_DOROR_LUCKY_2:
			return Pets.OCT_2020_TOP_DOROR_LUCKY_3;
		case OCT_2020_TOP_DOROR_LUCKY_3:
			return Pets.OCT_2020_TOP_DOROR_LUCKY_1;
		case SS_PET_1:
			return Pets.SS_PET_2;
		case SS_PET_2:
			return Pets.SS_PET_3;
		case SS_PET_3:
			return Pets.SS_PET_1;
		case ZIOS_CAVALRY:
			return Pets.ZIOS_CAVALRY_2;
		case ZIOS_CAVALRY_2:
			return Pets.ZIOS_CAVALRY;
		case CALLUS_1:
			return Pets.CALLUS_2;
		case CALLUS_2:
			return Pets.CALLUS_3;
		case CALLUS_3:
			return Pets.CALLUS_1;
		case KALHPITE_PRINCESS:
			return Pets.KALHPITE_PRINCESS_2;
		case KALHPITE_PRINCESS_2:
			return Pets.KALHPITE_PRINCESS;
		case PET_SNAKELING:
			return Pets.PET_SNAKELING_2;
		case PET_SNAKELING_2:
			return Pets.PET_SNAKELING_3;
		case PET_SNAKELING_3:
			return Pets.PET_SNAKELING;
		case PET_DARK_CORE:
			return Pets.CORPOREAL_CRITTER;
		case CORPOREAL_CRITTER:
			return Pets.PET_DARK_CORE;
		case JAL_NIB_REK:
			return Pets.TZREK_ZUK;
		case TZREK_ZUK:
			return Pets.JAL_NIB_REK;
		case NOON:
			return Pets.MIDNIGHT;
		case MIDNIGHT:
			return Pets.NOON;
		case BABY_CHINCHOMPA:
			return Pets.BABY_CHINCHOMPA_2;
		case BABY_CHINCHOMPA_2:
			return Pets.BABY_CHINCHOMPA_3;
		case BABY_CHINCHOMPA_3:
			return Pets.BABY_CHINCHOMPA_4;
		case BABY_CHINCHOMPA_4:
			return Pets.BABY_CHINCHOMPA;
		case HYDRA:
			return Pets.HYDRA_2;
		case HYDRA_2:
			return Pets.HYDRA_3;
		case HYDRA_3:
			return Pets.HYDRA_4;
		case HYDRA_4:
			return Pets.HYDRA;
		default: return null;
		}
	}

	public static void init() {
		for (int i = 0; i < 10; i++) {
			// TODO create dynamic interface handling for these inter
			final int META_INDEX = i;
			ButtonHandler.register(496, 4 + i, 1, (player, slot1, slot2, action) ->
					player.getPet().olmMeta(META_INDEX));
		}
	}

	public static final Pets[] OLM_META = {Pets.DUSTED_OLMLET, Pets.PUPPADILE, Pets.TEKTINY, Pets.ENRAGED_TEKTINY, Pets.VANGUARD, Pets.VASA_MINISTRIO, Pets.VESPINA, Pets.ELDER_OLMLET, Pets.TWISTED_OLMLET, Pets.ANCESTRAL_OLMLET};

	public void olmMeta(int i) {
		if(OLM_META[i] == Pets.TWISTED_OLMLET && !owner.hasUnlockedTwistedOlmlet()) {
			owner.sendMessage("Your pet does not yet know how to do this Metamorphosis.");
			owner.sendMessage("You must hand your Olmet a Dragonshredder crossbow infuse him with its power.");
			return;
		}
		if(OLM_META[i] == Pets.ANCESTRAL_OLMLET && !owner.hasUnlockedAncestralOlmlet()) {
			owner.sendMessage("Your pet does not yet know how to do this Metamorphosis.");
			owner.sendMessage("You must hand your Olmet a Corrosive spirit shield to infuse him with its power.");
			return;
		}
		if(OLM_META[i] == Pets.ELDER_OLMLET && !owner.hasUnlockedElderOlmlet()) {
			owner.sendMessage("Your pet does not yet know how to do this Metamorphosis.");
			owner.sendMessage("You must hand your Olmet an Elder maul to infuse him with its power.");
			return;
		}
		owner.stopAll();
		doMeta(OLM_META[i]);
	}

	public void olmMeta() {
		//owner.getPackets().sendHideIComponent(496, 1, true);
		//owner.getPackets().sendHideIComponent(496, 2, true);
		//owner.getPackets().sendIComponentModel(496, 1, 32681);
		//owner.getPackets().sendIComponentModel(496, 2, 232750);
		owner.getPackets().sendIComponentText(496, 3, "~ Olmlet Metamorphosis ~");

		for (int i = 0; i < 10; i++) {
			String text = "<col=ff981f>" + Utils.formatPlayerNameForDisplay(OLM_META[i].name());
			if(OLM_META[i] == Pets.TWISTED_OLMLET && !owner.hasUnlockedTwistedOlmlet())
				text = Colour.STRIKE.wrap(text);
			if(OLM_META[i] == Pets.ANCESTRAL_OLMLET && !owner.hasUnlockedAncestralOlmlet())
				text = Colour.STRIKE.wrap(text);
			if(OLM_META[i] == Pets.ELDER_OLMLET && !owner.hasUnlockedElderOlmlet())
				text = Colour.STRIKE.wrap(text);
			owner.getPackets().sendIComponentText(496, 4 + i, text);
		}

		owner.getInterfaceManager().sendInterface(496);
	}

	public void meta() {
		if(getPet() != Pets.VESPINA && getPet() != Pets.TEKTINY && // these 2 change into another form first
				Arrays.stream(OLM_META).anyMatch(pets -> pets == getPet()) || getPet() == Pets.FLYING_VESPINA || getPet() == Pets.ENRAGED_TEKTINY) {
			olmMeta();
			return;
		}
		Pets toPet = getPetMeta();
		doMeta(toPet);
	}

	private void doMeta(Pets toPet) {
		if (toPet == null)
			return;
		itemId = toPet.getItemId(details.getStage());
		setNextNPCTransformation(toPet.getNpcId(details.getStage()));
		pet = toPet;
		owner.getPetManager().setItemId(itemId);
		owner.getPetManager().setNpcId(getId());
		sendMainConfigurations();

		if(pet == Pets.TWISTED_OLMLET || pet == Pets.ANCESTRAL_OLMLET || pet == Pets.ELDER_OLMLET) {
			ButtonHandler.refreshEquipBonuses(owner);
		}
	}

	public Pets getPet() {
		return pet;
	}
	
	public void talkTo() {
		owner.getDialogueManager().startDialogue("SimpleNPCMessage", getId(), "Meow! "+owner.getName()+" is the best owner in the world!");
	}
	
	/**
	 * Picks up the pet.
	 */
	public void pickup() {
		pickup(false);
	}

	public void pickup(boolean force) {
		if (!force && !owner.getInventory().hasFreeSlots()) {
			owner.getPackets().sendGameMessage("You have no inventory space to pick up your pet.");
			return;
		}
		owner.setNextAnimation(new Animation(827));
		owner.getInventory().addItem(itemId, 1);
		owner.setPet(null);
		owner.getPetManager().setNpcId(-1);
		owner.getPetManager().setItemId(-1);
		switchOrb(false);
		owner.getInterfaceManager().removeExtras();
		owner.getInterfaceManager().openGameTab(4);
		owner.getPackets().sendIComponentSettings(747, 17, 0, 0, 0);
		finish();
		owner.getCombatDefinitions().refreshBonuses();
	}

	/**
	 * Calls the pet.
	 */
	public void call() {
		int size = getSize();
		WorldTile teleTile = null;
		for (int dir = 0; dir < checkNearDirs[0].length; dir++) {
			final WorldTile tile = new WorldTile(new WorldTile(owner.getX() + checkNearDirs[0][dir], owner.getY() + checkNearDirs[1][dir], owner.getPlane()));
			if (World.isTileFree(tile.getPlane(), tile.getX(), tile.getY(), size)) {
				teleTile = tile;
				break;
			}
		}
		if (teleTile == null) {
			return;
		}
		setNextWorldTile(teleTile);
	}

	private void sendFollow() {
		if (getLastFaceEntity() != owner.getClientIndex())
			setNextFaceEntity(owner);
		if (isFrozen())
			return;
		int size = getSize();
		int targetSize = owner.getSize();
		if (Utils.colides(getX(), getY(), size, owner.getX(), owner.getY(), targetSize) && !owner.hasWalkSteps()) {
			resetWalkSteps();
			if (!addWalkSteps(owner.getX() + targetSize, getY())) {
				resetWalkSteps();
				if (!addWalkSteps(owner.getX() - size, getY())) {
					resetWalkSteps();
					if (!addWalkSteps(getX(), owner.getY() + targetSize)) {
						resetWalkSteps();
						if (!addWalkSteps(getX(), owner.getY() - size)) {
							return;
						}
					}
				}
			}
			return;
		}
		resetWalkSteps();
		if (!clipedProjectile(owner, true) || !Utils.isOnRange(getX(), getY(), size, owner.getX(), owner.getY(), targetSize, 0))
			calcFollow(owner, 2, true, false);
	}

	/**
	 * Sends the main configurations for the Pet interface (+ summoning orb).
	 */
	public void sendMainConfigurations() {
		switchOrb(true);
		owner.getVarsManager().sendVar(448, itemId);// configures
		owner.getPackets().sendCSVarInteger(1436, 0);
		unlockOrb(); // temporary
	}

	/**
	 * Sends the follower details.
	 */
	public void sendFollowerDetails() {
		owner.getVarsManager().sendVarBit(4285, (int) details.getGrowth());
		owner.getVarsManager().sendVarBit(4286, (int) details.getHunger());
		owner.getInterfaceManager().setExtras(662);
		unlock();
		owner.getInterfaceManager().openGameTab(95);
	}

	/**
	 * Switch the Summoning orb state.
	 * 
	 * @param enable
	 *            If the orb should be enabled.
	 */
	public void switchOrb(boolean enable) {
		owner.getVarsManager().sendVar(1174, enable ? getId() : 0);
		if (enable) {
			unlock();
			return;
		}
		lockOrb();
	}

	/**
	 * Unlocks the orb.
	 */
	public void unlockOrb() {
		owner.getPackets().sendHideIComponent(747, 9, false);
		Familiar.sendLeftClickOption(owner);
	}

	/**
	 * Unlocks the interfaces.
	 */
	public void unlock() {
		owner.getPackets().sendHideIComponent(747, 9, false);
	}

	/**
	 * Locks the orb.
	 */
	public void lockOrb() {
		owner.getPackets().sendHideIComponent(747, 9, true);
	}

	/**
	 * Gets the details.
	 * 
	 * @return The details.
	 */
	public PetDetails getDetails() {
		return details;
	}

	/**
	 * Gets the growthRate.
	 * 
	 * @return The growthRate.
	 */
	public double getGrowthRate() {
		return growthRate;
	}

	/**
	 * Sets the growthRate.
	 * 
	 * @param growthRate
	 *            The growthRate to set.
	 */
	public void setGrowthRate(double growthRate) {
		this.growthRate = growthRate;
	}

	/**
	 * Gets the item id of the pet.
	 * 
	 * @return The item id.
	 */
	public int getItemId() {
		return itemId;
	}

	public void setPet(Pets pets) {
		this.pet = pets;
	}
}