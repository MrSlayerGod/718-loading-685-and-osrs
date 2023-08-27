package com.rs.net.decoders.handlers;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.discord.Bot;
import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.item.Item;
import com.rs.game.minigames.stealingcreation.SCRewards;
import com.rs.game.npc.familiar.Familiar.SpecialAttack;
import com.rs.game.npc.others.Mimic;
import com.rs.game.npc.others.PolyporeCreature;
import com.rs.game.npc.others.Revenant;
import com.rs.game.player.*;
import com.rs.game.player.actions.*;
import com.rs.game.player.actions.Fletching.Fletch;
import com.rs.game.player.actions.HerbCleaning.Herbs;
import com.rs.game.player.actions.firemaking.Firemaking;
import com.rs.game.player.content.*;
import com.rs.game.player.content.Drinkables.Drink;
import com.rs.game.player.content.FlyingEntityHunter.FlyingEntities;
import com.rs.game.player.content.Summoning.Pouch;
import com.rs.game.player.content.box.*;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.content.prayer.Burying.Bone;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.controllers.*;
import com.rs.game.player.dialogues.impl.*;
import com.rs.game.player.dialogues.impl.CombinationsD.Combinations;
import com.rs.game.player.dialogues.impl.SqirkFruitSqueeze.SqirkFruit;
import com.rs.game.player.dialogues.impl.UpgradeItemOption.Upgrade;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.io.InputStream;
import com.rs.utils.Colour;
import com.rs.utils.Direction;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.List;

public class InventoryOptionsHandler {

    public static final int[] INFINITY_RING_ITEMS = {49710, 15018, 15019, 15020, 15220, 42691, 42692, 43202, 19669, 51752, 52975, 42785};

    public static final int[] BOOTS_OF_THE_GODS_ITEMS = {21787, 21790, 21793, 43239, 43235, 51733, 43237, 25523, };

    public static final int[] GLOVES_OF_THE_GODS_ITEMS = {7462, 24454, 52981, 49544, 18347, 25523 };

    
    public static void register(int[] id, int actionIndex, ItemAction runnable) {
        for(int i : id) {
            register(i, actionIndex, runnable);
        }
    }

    public static void register(int id, int actionIndex, ItemAction runnable) {
        actionRepository.putIfAbsent(id, new ItemAction[10]);
        ItemAction[] actionList = actionRepository.get(id);
        if(actionList[actionIndex] != null) {
            System.err.println("!! [InventoryOption] Warning: " + id + " action " + actionIndex + " is being overwritten!");
        }
        actionList[actionIndex] = runnable;
    }

    public static boolean handle(Player player, Item item, int action, int fromSlot) {
        item.setFromSlot(fromSlot);  // used for removing/swapping items from correct slot

        if(action < 1)
            return false;
        if (actionRepository.containsKey(item.getId())) {
            //System.out.println("A " + object.getId() + " act " + action);
            ItemAction act = actionRepository.get(item.getId())[action];
            if(act != null) {
                act.handle(player, item);
                return true;
            }
        }

        return false;
    }

    static HashMap<Integer, ItemAction[]> actionRepository = new HashMap<Integer, ItemAction[]>();

    public static void handleItemOption2(final Player player, final int slotId, final int itemId, Item item) {
        if (player.isLocked() || player.getEmotesManager().isDoingEmote())
            return;
        if(handle(player, item, 2, slotId))
            return;
        else if (Firemaking.isFiremaking(player, itemId))
            return;
        else if (itemId == 4155)
            player.getSlayerManager().checkKillsLeft();
        else if (item.getId() == CoalBag.COAL_BAG_ID)
            player.getCoalBag().openBag();
        else if (item.getId() == CoalBag.OPENED_COAL_BAG_ID)
            player.getCoalBag().closeBag();
        else if (item.getId() == GemBag.GEM_BAG_ID)
            player.getGemBag().openBag();
        else if (item.getId() == GemBag.OPENED_GEM_BAG_ID)
            player.getGemBag().closeBag();
        else if (itemId == 15262)
            ItemSets.openSkillPack(player, itemId, 12183, 5000, player.getInventory().getAmountOf(itemId));
        else if (itemId == 15362)
            ItemSets.openSkillPack(player, itemId, 230, 50, player.getInventory().getAmountOf(itemId));
        else if (itemId == 15363)
            ItemSets.openSkillPack(player, itemId, 228, 50, player.getInventory().getAmountOf(itemId));
        else if (itemId == 15364)
            ItemSets.openSkillPack(player, itemId, 222, 50, player.getInventory().getAmountOf(itemId));
        else if (itemId == 15365)
            ItemSets.openSkillPack(player, itemId, 9979, 50, player.getInventory().getAmountOf(itemId));
        else if (item.getId() == MysteryBox.ID || item.getId() == MysteryBox.PREMIUM_ID)
            MysteryBox.open(player, item.getId(), true);
        else if (itemId == MinigameBox.ID)
            MinigameBox.open(player);
        else if (item.getId() == MysteryPetBox.ID)
            MysteryPetBox.open(player, true);
        else if (item.getId() == MysteryGodBox.ID)
            MysteryGodBox.open(player, true);
        else if (item.getId() == MysteryAuraBox.ID)
            MysteryAuraBox.open(player, true);
        else if (item.getId() == MysteryBox.BEG_ID)
            MysteryBox.openBeginner(player, true);
        else if (item.getId() == MoneyBox.ID)
            MoneyBox.open(player, true);
        else if (itemId == 1225) {
            // player.getPackets().sendInputIntegerScript("What would you like to do when
            // you grow up?");
            // player.getTemporaryAttributtes().put("xformring", Boolean.TRUE);
        } else if (itemId >= 5509 && itemId <= 5514) {
            int pouch = -1;
            if (itemId == 5509)
                pouch = 0;
            if (itemId == 5510 || itemId == 5511)
                pouch = 1;
            if (itemId == 5512)
                pouch = 2;
            if (itemId == 5514)
                pouch = 3;
            Runecrafting.emptyPouch(player, pouch);
            player.stopAll(false);
        } else if (itemId >= 15086 && itemId <= 15100) {
            Dicing.handleRoll(player, itemId, true);
            return;
        } else if (itemId == 6583 || itemId == 7927) {
            AccessorySmithing.ringTransformation(player, itemId);
        } else if (item.getDefinitions().containsOption(1, "Extinguish")) {
            if (LightSource.extinguishSource(player, slotId, false))
                return;
        } else {
            if (player.isEquipDisabled())
                return;
			if (player.getSwitchItemCache().isEmpty()) {
				player.getSwitchItemCache().add(slotId);
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						List<Integer> slots = player.getSwitchItemCache();
						int[] slot = new int[slots.size()];
						for (int i = 0; i < slot.length; i++)
							slot[i] = slots.get(i);
						player.getSwitchItemCache().clear();
									if (player.isDead())
							return;
						ButtonHandler.sendWear(player, slot);
						player.stopAll(false, true, false);
					}
				});
			} else if (!player.getSwitchItemCache().contains(slotId)) {
				player.getSwitchItemCache().add(slotId);
			}
            /*Item[] copy = player.getInventory().getItems().getItemsCopy();
            if (ButtonHandler.sendWear2(player, slotId, item.getId())) {
                ButtonHandler.refreshEquipBonuses(player);
                player.getInventory().refreshItems(copy);
                player.getAppearence().generateAppearenceData();
                player.getPackets().sendSound(2240, 0, 1);
            }*/
        }
    }

    public static void dig(final Player player) {
        player.resetWalkSteps();
        player.setNextAnimation(new Animation(830));
        player.lock();
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.unlock();
                if (player.getTreasureTrailsManager().useDig())
                    return;
                if (Barrows.digIntoGrave(player))
                    return;
                if (player.getX() == 3005 && player.getY() == 3376 || player.getX() == 2999 && player.getY() == 3375
                        || player.getX() == 2996 && player.getY() == 3377
                        || player.getX() == 2989 && player.getY() == 3378
                        || player.getX() == 2987 && player.getY() == 3387
                        || player.getX() == 2984 && player.getY() == 3387) {
                    // mole
                    player.getControlerManager().startControler("GiantMole");
                    return;
                } else if (player.withinDistance(new WorldTile(2748, 3734, 0), 2)) {
                    player.lock();
                    player.setNextGraphics(new Graphics(80, 5, 60));
                    FadingScreen.fade(player, 1000, new Runnable() {

                        @Override
                        public void run() {
                            player.unlock();
                            player.setNextWorldTile(new WorldTile(2696, 10121, 0));
                        }
                    });
                    player.getPackets().sendGameMessage("You fall through the ground into a network of tunnels.");
                    return;
                }
                player.getPackets().sendGameMessage("You find nothing.");
            }

        });
    }

    public static void handleItemOption1(final Player player, final int slotId, final int itemId, Item item) {
        if (player.isLocked() || player.getEmotesManager().isDoingEmote())
            return;
        player.stopAll(false);
        if(handle(player, item, 1, slotId))
            return;
        else if (player.getTreasureTrailsManager().useItem(item, slotId))
            return;
        else if (Consumables.eat(player, item, slotId)) {
            return;
        } else if (Drinkables.drink(player, item, slotId))
            return;
        if (SCRewards.transform(player, item.getId(), false, false))
            return;
        else if (itemId == Casket.ID)
            Casket.open(player);
        else if(itemId == Mimic.MIMIC_CASKET) {
            if (Magic.canTeleport(player)) {
                player.getDialogueManager().startDialogue("OpenMimicD", slotId);
                //Mimic.openCasket(player);
            }
        } else if (itemId == MinigameBox.ID)
            MinigameBox.open(player);
        else if (itemId == RunePouch.ID)
            RunePouch.openRunePouch(player);
        else if (AerialFishing.isCook(player, itemId))
            return;
        else if (itemId == 41738) {
            player.lock(1);
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().addItemDrop(Herbs.values()[Utils.random(5)].getHerbId() + 1, Utils.random(1, 5));
            player.getInventory().addItemDrop(Herbs.values()[Utils.random(Herbs.values().length - 9)].getHerbId() + 1, Utils.random(1, 4));
        } else if (itemId == 11328 || itemId == 11330 || itemId == 11332) {
            player.lock(1);
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().addItem(new Item(Utils.random(2) == 0 ? (itemId == 11332 ? 11326 : 11324) : 11334, 1));
            player.getSkills().addXp(Skills.COOKING, itemId == 11332 ? 15 : 10);
            player.getPackets().sendGameMessage("You gut the fish.", true);
        } else if (itemId == 407) {
            player.lock(1);
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().addItemMoneyPouch(new Item(995, 1000));
            player.getInventory().addItem(new Item(Utils.random(2) == 0 ? 411 : 413, 1));
            player.getPackets().sendGameMessage("You open the oyster.", true);
        } else if (itemId == 2574)
            player.getTreasureTrailsManager().useSextant();
        else if (itemId == 1856)
            player.getPackets().sendOpenURL(Settings.HELP_LINK);
        else if (item.getId() == 20667)
            Magic.useVecnaSkull(player);
        else if(item.getId() == 50724)
            Magic.imbuedHeart(player);
        else if (item.getId() == 25587) {
            if (player.getInventory().getAmountOf(item.getId()) < 1500000) {
                player.getPackets().sendGameMessage("You need 1.500.000 upgrade fragments to create an upgrade gem!");
                return;
            }
            player.lock(1);
            player.getInventory().deleteItem(item.getId(), 1500000);
            player.getInventory().addItemDrop(25523, 1);
            player.anim(27185);
            player.getPackets().sendGameMessage("You craft an upgrade gem.", true);
        } else if (item.getId() == 25205) {
            if (!World.isTileFree(player.getPlane(), player.getX(), player.getY() - 1, 3)) {
                player.getPackets().sendGameMessage("You need clear space outside in order to place a deposit box.");
                return;
            }
            if (player.getControlerManager().getControler() != null
                    && !(player.getControlerManager().getControler() instanceof Wilderness)) {
                player.getPackets().sendGameMessage("You can't set a deposit box here.");
                return;
            }
            player.getInventory().deleteItem(slotId, item);
            player.setNextAnimation(new Animation(832));
            player.lock(1);
            World.spawnObjectTemporary(
                    new WorldObject(73268, 10, 0, player.getX() + 1, player.getY(), player.getPlane()), 3600 * 1000);
        } else if (itemId >= 5509 && itemId <= 5514) {
            int pouch = -1;
            if (itemId == 5509)
                pouch = 0;
            if (itemId == 5510)
                pouch = 1;
            if (itemId == 5512)
                pouch = 2;
            if (itemId == 5514)
                pouch = 3;
            Runecrafting.fillPouch(player, pouch);
            return;
        } else if (itemId == 952) {// spade
            dig(player);
            return;
        } else if (itemId == 10952) {
            if (Slayer.isUsingBell(player))
                return;
        } else if (HerbCleaning.clean(player, item, slotId))
            return;
        else if (TrapAction.isTrap(player, new WorldTile(player), itemId))
            return;
        else if (GemCutting.isCutting(player, itemId))
            return;
        else if (Bone.forId(itemId) != null) {
            Bone.bury(player, slotId);
            return;
        } else if (Magic.useTabTeleport(player, itemId))
            return;
        else if (Magic.useAncientTabTeleport(player, itemId))
            return;
        else if (item.getId() == 25433) {
            Magic.usePKTabTeleport(player);
        } else if (item.getDefinitions().containsOption("Summon")) {
            Pouch pouch = Pouch.forId(itemId);
            if (pouch != null)
                Summoning.spawnFamiliar(player, pouch);
        } else if (item.getId() == 22370)
            Summoning.openDreadNipSelection(player);
    	else if (item.getId() == 18839) {
            player.getPackets().sendGameMessage("You study the scroll and learn a new Prayer: " + Colour.DARK_RED.wrap("Rigour"));
            player.getInventory().deleteItem(item);
            player.setRigourUnlocked(true);
    	} else if (item.getId() == 18344) {
            player.getPackets().sendGameMessage("You study the scroll and learn a new Prayer: " + Colour.DARK_RED.wrap("Augury"));
            player.getInventory().deleteItem(item);
            player.setAuguryUnlocked(true);
    	}else if (item.getId() == 7509 || item.getId() == 7510) {
            player.setNextForceTalk(new ForceTalk("Ow! It nearly broke my tooth!"));
            player.getPackets().sendGameMessage("The rock cake resists all attempts to eat it.");
            player.applyHit(new Hit(player,
                    player.getHitpoints() - 10 < 35 ? player.getHitpoints() - 35 < 0 ? 0 : player.getHitpoints() - 35
                            : 10,
                    HitLook.REGULAR_DAMAGE));

        } else if (ItemTransportation.transportationDialogue(player, item, false, true))
            return;
        else if (Lamps.isSelectable(itemId) || Lamps.isSkillLamp(itemId) || Lamps.isOtherLamp(itemId))
            Lamps.processLampClick(player, slotId, itemId);
        else if (LightSource.lightSource(player, slotId))
            return;
        else if (LightSource.extinguishSource(player, slotId, false))
            return;
        else if (itemId == TheCollector.COLLECTION_LOG)
            player.getCollectionLog().open();
        else if (itemId == 299) {
			/*if (player.withinDistance(Settings.START_PLAYER_LOCATION, 32)) {
				player.getPackets().sendGameMessage("Planting flowers in this area has been disabled. Do ::dice instead.");
				return;
			}*/
            FlowerPoker.itemClick(player, item);
        } else if (itemId == 4251)
            Magic.useEctoPhial(player, item);
        else if (itemId == 15262)
            ItemSets.openSkillPack(player, itemId, 12183, 5000, 1);
        else if (itemId == 15362)
            ItemSets.openSkillPack(player, itemId, 230, 50, 1);
        else if (itemId == 15363)
            ItemSets.openSkillPack(player, itemId, 228, 50, 1);
        else if (itemId == 15364)
            ItemSets.openSkillPack(player, itemId, 222, 50, 1);
        else if (itemId == 15365)
            ItemSets.openSkillPack(player, itemId, 9979, 50, 1);
        else if ((item.getDefinitions().containsOption(0, "Grind") || item.getDefinitions().containsOption(0, "Powder"))
                && Herblore.isRawIngredient(player, itemId))
            return;
        else if (itemId == 2798 || itemId == 3565 || itemId == 3576 || itemId == 19042)
            player.getTreasureTrailsManager().openPuzzle(itemId);
        else if (itemId == 22445)
            player.getDialogueManager().startDialogue("NeemDrupeSqueeze");
        else if (itemId == 1775 || itemId == 23193)
            player.getDialogueManager().startDialogue("GlassBlowingD", itemId == 23193 ? 1 : 0);
        else if (itemId == 22444)
            PolyporeCreature.sprinkleOil(player, null);
        else if (itemId == 550)
            player.getInterfaceManager().sendInterface(270);
        else if (itemId == 24154 || itemId == 24155)
            player.getSquealOfFortune().processItemClick(slotId, itemId, item);
        else if (itemId == AncientEffigies.SATED_ANCIENT_EFFIGY || itemId == AncientEffigies.GORGED_ANCIENT_EFFIGY
                || itemId == AncientEffigies.NOURISHED_ANCIENT_EFFIGY
                || itemId == AncientEffigies.STARVED_ANCIENT_EFFIGY)
            player.getDialogueManager().startDialogue("AncientEffigiesD", itemId);
        else if (itemId == 4155)
            player.getDialogueManager().startDialogue("EnchantedGemDialouge",
                    player.getSlayerManager().getCurrentMaster().getNPCId());
        else if (itemId >= 23653 && itemId <= 23658)
            FightKiln.useCrystal(player, itemId);
        else if (itemId == 20124 || itemId == 20123 || itemId == 20122 || itemId == 20121)
            GodswordCreating.attachKeys(player);
        else if (itemId == 6)
            DwarfMultiCannon.setUp(player);
        else if (itemId == 15707)
            player.getDungManager().openPartyInterface();
        else if (Nest.isNest(itemId))
            Nest.searchNest(player, slotId);
        else if (itemId == 14057) // broomstick
            player.setNextAnimation(new Animation(10532));
        else if (itemId == 21776) {
            if (player.getSkills().getLevel(Skills.CRAFTING) < 77) {
                player.getPackets()
                        .sendGameMessage("You need a Crafting level of at least 77 in order to combine the shards.");
                return;
            } else if (player.getInventory().containsItem(itemId, 100)) {
                player.setNextAnimation(new Animation(713));
                player.setNextGraphics(new Graphics(1383));
                player.getInventory().deleteItem(new Item(itemId, 100));
                player.getInventory().addItem(new Item(21775, 1));
                player.getSkills().addXp(Skills.CRAFTING, 150);
                player.getPackets().sendGameMessage("You combine the shards into an orb.");
            } else {
                player.getPackets()
                        .sendGameMessage("You need at least 100 shards in order to create an orb of armadyl.");
            }
        } else if (itemId == 5974) {
            if (!player.getInventory().containsItemToolBelt(Smithing.HAMMER)) {
                player.getDialogueManager().startDialogue("SimpleMessage",
                        "You need a hammer in order to break open a coconut.");
                return;
            }
            player.getInventory().addItem(new Item(5976, 1));
            player.getInventory().deleteItem(new Item(5974, 1));
            player.getPackets()
                    .sendGameMessage("You smash the coconut with a hammer and it breaks into two symmetrical pieces.");
        } else if (itemId == SqirkFruitSqueeze.SqirkFruit.AUTUMM.getFruitId())
            player.getDialogueManager().startDialogue("SqirkFruitSqueeze", SqirkFruit.AUTUMM);
        else if (itemId == SqirkFruitSqueeze.SqirkFruit.SPRING.getFruitId())
            player.getDialogueManager().startDialogue("SqirkFruitSqueeze", SqirkFruit.SPRING);
        else if (itemId == SqirkFruitSqueeze.SqirkFruit.SUMMER.getFruitId())
            player.getDialogueManager().startDialogue("SqirkFruitSqueeze", SqirkFruit.SUMMER);
        else if (itemId == SqirkFruitSqueeze.SqirkFruit.WINTER.getFruitId())
            player.getDialogueManager().startDialogue("SqirkFruitSqueeze", SqirkFruit.WINTER);
        else if (item.getDefinitions().getName().startsWith("Burnt"))
            player.getDialogueManager().startDialogue("SimplePlayerMessage", "Ugh, this is inedible.");
        else if (item.getId() >= 25425 && item.getId() <= 25429 || item.getId() == 25493)
            Donations.tierDonatorTicket(player, item.getId());
        else if (item.getId() >= 25437 && item.getId() <= 25440 || item.getId() == 25494)
            Donations.tierUpgradeDonatorTicket(player, item.getId());
        else if (item.getId() == 25435) {
            player.lock(1);
            player.getInventory().deleteItem(item.getId(), 1);
            player.getInventory().addItemMoneyPouch(new Item(995, 200000));
        } else if (item.getId() == MysteryBox.ID || item.getId() == MysteryBox.PREMIUM_ID)
            MysteryBox.open(player, item.getId(), false);
        else if (item.getId() == MysteryPetBox.ID)
            MysteryPetBox.open(player, false);
        else if (item.getId() == MysteryGodBox.ID)
            MysteryGodBox.open(player, false);
        else if (item.getId() == MysteryAuraBox.ID)
            MysteryAuraBox.open(player, false);
        else if (item.getId() == MoneyBox.ID)
            MoneyBox.open(player, false);
        else if (item.getId() == CorruptedCasket.ID)
            CorruptedCasket.open(player);
        else if (item.getId() == HalloweenBox.ID)
            HalloweenBox.open(player);
        else if (item.getId() == ChristmasBox.ID)
            ChristmasBox.open(player);
        else if (item.getId() == EasterBox.ID)
            EasterBox.open(player);
        else if (item.getId() == MysteryBox.BEG_ID)
            MysteryBox.openBeginner(player, false);
        else if (item.getId() == 989 || item.getId() == 53083 || item.getId() == 25523
                || item.getId() == 53951)
            Magic.sendCommandTeleportSpell(player, new WorldTile(2758, 3494, 1));
        else if (item.getId() == 41941)
            player.getLootingBag().check();
        else if (item.getId() == CoalBag.COAL_BAG_ID || item.getId() == CoalBag.OPENED_COAL_BAG_ID)
            player.getCoalBag().fill();
        else if (item.getId() == GemBag.GEM_BAG_ID || item.getId() == GemBag.OPENED_GEM_BAG_ID)
            player.getGemBag().fill();
        else if (item.getId() == 25481)
            player.getPackets().sendGameMessage("You can use this scroll to imbue items such as rings.");
        else if (item.getId() == 25739)
            player.getPackets().sendGameMessage("You can use this scroll to imbue infinity jewelry.");
        else if (item.getId() == 25760)
            player.getPackets().sendGameMessage("You can use this scroll to imbue ultimate bandos armour.");
        else if (item.getId() == 25761)
            player.getPackets().sendGameMessage("You can use this scroll to imbue ultimate armadyl armour.");
        else if (item.getId() == 25762)
            player.getPackets().sendGameMessage("You can use this scroll to imbue ultimate subjugation armour.");
        else if (item.getId() == 52477)
            player.getPackets().sendGameMessage("You raise the hilt, inspecting each section carefully. It looks as though it could combine with a powerful parrying dagger.");
        else if (item.getId() == 51043)
            player.getPackets().sendGameMessage("You sense a dark magic emanating from the insignia. It looks like this could be attached to a wand.");
        else if (item.getId() == 51730)
            player.getPackets().sendGameMessage("Fallen from the centre of a Grotesque Guardian. This could be attached to a pair of Bandos boots.");
        else if (itemId == 52517) {
            Magic.sendTeleportSpell(player, 8939, 8941, 1576, 1577, 1, 0, TheatreOfBlood.OUTSIDE, 3, true, player.getControlerManager().getControler() instanceof TheatreOfBloodController ? Magic.OBJECT_TELEPORT : Magic.ITEM_TELEPORT);
            player.getInventory().deleteItem(52517, 1);
        } else if (itemId == DollarContest.FRAGMENT_ID)
            DollarContest.repair(player);
        else if (itemId == DollarContest.KEY_ID)
            DollarContest.info(player);
        else if (item.getDefinitions().containsOption(0, "Craft")
                || item.getDefinitions().containsOption(0, "Fletch")) {
            if (player.getInventory().containsItemToolBelt(946, 1)) {
                Fletch fletch = Fletching.isFletching(item, new Item(946));
                if (fletch != null) {
                    player.getDialogueManager().startDialogue("FletchingD", fletch);
                    return;
                }
            }
            if (player.getInventory().containsItemToolBelt(1755, 1)) {
                Fletch fletch = Fletching.isFletching(item, new Item(1755));
                if (fletch != null) {
                    player.getDialogueManager().startDialogue("FletchingD", fletch);
                    return;
                }
            }
            if (player.getInventory().containsItemToolBelt(Fletching.DUNGEONEERING_KNIFE, 1)) {
                Fletch fletch = Fletching.isFletching(item, new Item(Fletching.DUNGEONEERING_KNIFE));
                if (fletch != null) {
                    player.getDialogueManager().startDialogue("FletchingD", fletch);
                    return;
                }
            }
            if (LeatherCraftingD.isExtraItem(itemId) || player.getInventory().containsItemToolBelt(1733, 1)) {
                int leatherIndex = LeatherCraftingD.getIndex(itemId);
                if (leatherIndex != -1) {
                    player.getDialogueManager().startDialogue("LeatherCraftingD", leatherIndex,
                            player.getDungManager().isInside());
                    return;
                }
            }

            player.getDialogueManager().startDialogue("ItemMessage",
                    "You need a knife, chisel or needle to complete the action.", 946);
        } else
            player.getPackets().sendGameMessage("Nothing interesting happens.");
        if (Settings.DEBUG)
            Logger.log("ItemHandler", "Item Select:" + itemId + ", Slot Id:" + slotId);
    }

    /*
     * returns the other
     */
    public static Item contains(int id1, Item item1, Item item2) {
        if (item1.getId() == id1)
            return item2;
        if (item2.getId() == id1)
            return item1;
        return null;
    }

    public static boolean contains(int id1, int id2, Item... items) {
        boolean containsId1 = false;
        boolean containsId2 = false;
        for (Item item : items) {
            if (item.getId() == id1)
                containsId1 = true;
            else if (item.getId() == id2)
                containsId2 = true;
        }
        return containsId1 && containsId2;
    }

    public static boolean contains(int id1, int id2, int... items) {
        boolean containsId1 = false;
        boolean containsId2 = false;
        for (int item : items) {
            if (item == id1)
                containsId1 = true;
            else if (item == id2)
                containsId2 = true;
        }
        return containsId1 && containsId2;
    }

    public static void handleInterfaceOnInterface(final Player player, InputStream stream) {
        int usedWithId = stream.readUnsignedShort();
        int toSlot = stream.readUnsignedShortLE128();
        int interfaceId = stream.readUnsignedShort();
        int interfaceComponent = stream.readUnsignedShort();
        int interfaceId2 = stream.readInt() >> 16;
        int fromSlot = stream.readUnsignedShort();
        int itemUsedId = stream.readUnsignedShortLE128();
        if (player.isLocked() || player.getEmotesManager().isDoingEmote())
            return;
        if ((interfaceId == 747 || interfaceId == 662) && interfaceId2 == Inventory.INVENTORY_INTERFACE) {
            player.stopAll();
            if (player.getFamiliar() != null) {
                player.getFamiliar().setSpecial(true);
                if (player.getFamiliar().getSpecialAttack() == SpecialAttack.ITEM) {
                    if (player.getFamiliar().hasSpecialOn())
                        player.getFamiliar().submitSpecial(toSlot);
                }
            }
            return;
        }
        if (interfaceId == Inventory.INVENTORY_INTERFACE && interfaceId == interfaceId2
                && !player.getInterfaceManager().containsInventoryInter()) {
            player.stopAll();
            if (toSlot >= 28 || fromSlot >= 28 || toSlot == fromSlot)
                return;
            Item usedWith = player.getInventory().getItem(toSlot);
            Item itemUsed = player.getInventory().getItem(fromSlot);
            if (itemUsed == null || usedWith == null || itemUsed.getId() != itemUsedId
                    || usedWith.getId() != usedWithId)
                return;

            // fix for players in a glitched state without charged blowpipe
            // but still have chargee blowpipe in charge manager
            if(contains(42924, 42934, itemUsed, usedWith)) {
                if(!player.containsItem(42926)) {
                    if(player.getCharges().getCharges(42926) > 0) {
                        int slot = itemUsed.getId() == 42924 ? fromSlot : toSlot;
                        player.getInventory().getItems().get(slot).setId(42926);
                        player.getInventory().refresh();
                    }
                } else {
                    player.sendMessage("You may only have one charged blowpipe at a time.");
                }
            }
            if (!player.getControlerManager().canUseItemOnItem(itemUsed, usedWith))
                return;
            Fletch fletch = Fletching.isFletching(usedWith, itemUsed);
            if (fletch != null) {
                player.getDialogueManager().startDialogue("FletchingD", fletch);
                return;
            }
            int herblore = Herblore.isHerbloreSkill(itemUsed, usedWith);
            if (herblore > -1) {
                player.getDialogueManager().startDialogue("HerbloreD", herblore, itemUsed, usedWith);
                return;
            }
            int leatherIndex = LeatherCraftingD.getIndex(itemUsedId) == -1 ? LeatherCraftingD.getIndex(usedWithId)
                    : LeatherCraftingD.getIndex(itemUsedId);
            if (leatherIndex != -1 && ((itemUsedId == 1733 || usedWithId == 1733)
                    || LeatherCraftingD.isExtraItem(usedWithId) || LeatherCraftingD.isExtraItem(itemUsedId))) {
                player.getDialogueManager().startDialogue("LeatherCraftingD", leatherIndex,
                        player.getDungManager().isInside());
                return;
            }
            Item gem = contains(UpgradeItemOption.GEM_ID, itemUsed, usedWith);
            if (gem != null) {
                // player.getPackets().sendGameMessage("This item can not be upgraded.");
                // return;
                Upgrade upgrade = UpgradeItemOption.getUpgrade(gem);
                if (upgrade != null) {
                    if(!upgrade.name().toUpperCase().startsWith("CATALYST")) {
                        player.getDialogueManager().startDialogue("UpgradeItemOption", upgrade, true);
                        return;
                    }
                }
            }
            Item keepsakeItem = contains(KeepsakeItem.KEY, itemUsed, usedWith);
            if (keepsakeItem != null) {
                player.getDialogueManager().startDialogue("KeepsakeItem", keepsakeItem);
                return;
            }
            Combinations combination = Combinations.isCombining(itemUsedId, usedWithId);
            if (combination != null) {
                player.getDialogueManager().startDialogue("CombinationsD", combination);
                return;
            } else if (itemUsed.getId() == ChristmasBox.DYE || usedWith.getId() == ChristmasBox.DYE) {
                ChristmasBox.upgradeEvil(player, itemUsed.getId() == ChristmasBox.DYE ? usedWith : itemUsed);
                return;
            } else if (Firemaking.isFiremaking(player, itemUsed, usedWith))
                return;
            else if (OrnamentKits.attachKit(player, itemUsed, usedWith, fromSlot, toSlot))
                return;
            else if (AmuletAttaching.isAttaching(itemUsedId, usedWithId))
                player.getDialogueManager().startDialogue("AmuletAttaching");
            else if (GemCutting.isCutting(player, itemUsed, usedWith))
                return;
            else if (AttachingOrbsDialouge.isAttachingOrb(player, itemUsed, usedWith))
                return;
            else if (TreeSaplings.hasSaplingRequest(player, itemUsedId, usedWithId)) {
                if (itemUsedId == 5354)
                    TreeSaplings.plantSeed(player, usedWithId, fromSlot);
                else
                    TreeSaplings.plantSeed(player, itemUsedId, toSlot);
            } else if (Drinkables.mixPot(player, itemUsed, usedWith, fromSlot, toSlot, true) != -1)
                return;
            else if (WeaponPoison.poison(player, itemUsed, usedWith, false))
                return;
            else if(HalloweenBox.dyeItem(player, itemUsed, usedWith))
                return;
            else if(ImbuedScrolls.imbue(player, itemUsed.getId(), usedWith.getId()))
                return;
            else if (usedWith.getId() == RunePouch.ID)
                RunePouch.storeRunePouch(player, itemUsed, itemUsed.getAmount());
            else if (PrayerBooks.isGodBook(itemUsedId, false) || PrayerBooks.isGodBook(usedWithId, false)) {
                PrayerBooks.bindPages(player, itemUsed.getName().contains(" page ") ? usedWithId : itemUsedId);
            } else if (contains(20767, 51295, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(51285, 1);
                player.getInventory().addItem(51282, 1);
            } else if (contains(20767, 6570, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(43329, 1);
                player.getInventory().addItem(43330, 1);
            } else if (contains(20072, 52477, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(20072, 1);
                player.getInventory().deleteItem(52477, 1);
                player.getInventory().addItem(52322, 1);
                player.getPackets().sendGameMessage("You combine the dragon defender.");
            } else if (contains(53908, 15259, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(53908, 1);
                player.getInventory().deleteItem(15259, 1);
                player.getInventory().addItem(53677, 1);
                player.getPackets().sendGameMessage("You attach the Zalcano shard to the Dragon pickaxe.");
            } else if (contains(24455, 54417, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(25588, 1);
                player.getPackets().sendGameMessage("You combine the maces. Your mace starts shinning with power.");
            } else if (contains(6914, 51043, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(6914, 1);
                player.getInventory().deleteItem(51043, 1);
                player.getInventory().addItem(51006, 1);
                player.getPackets().sendGameMessage("Power shoots down the length of the wand, empowering it with the darkness of Xeric's Kodai wizards.");
	/*		} else if (contains(25470, 49553, itemUsed, usedWith)) {
				player.lock(2);
				player.getInventory().deleteItem(itemUsed);
				player.getInventory().deleteItem(usedWith);
				player.getInventory().addItem(25479, 1);
				player.getPackets().sendGameMessage("The looter's amulet fuses with your zenyte jewelry.");
			} else if (contains(25470, 49547, itemUsed, usedWith)) {
				player.lock(2);
				player.getInventory().deleteItem(itemUsed);
				player.getInventory().deleteItem(usedWith);
				player.getInventory().addItem(25480, 1);
				player.getPackets().sendGameMessage("The looter's amulet fuses with your zenyte jewelry.");
			*/
            } else if (contains(51018, 25523, itemUsed, usedWith) || contains(51018, 21776, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(51018, 1) || !player.getInventory().containsItem(25523, 1) || !player.getInventory().containsItem(21776, 50)) {
                    player.getPackets().sendGameMessage("You need an Ancestral Hat, an Upgrade Gem and 50 Shards of Armadyl to craft a Catalyst Hat");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(51018, 1);
                player.getInventory().deleteItem(25523, 1);
                player.getInventory().deleteItem(21776, 50);
                player.getInventory().addItem(44702, 1);
                player.getPackets().sendGameMessage("Using your magical strength, you fuse the items together to craft a Catalyst Hat!");
            } else if (contains(51021, 25523, itemUsed, usedWith) || contains(51021, 21776, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(51021, 1) || !player.getInventory().containsItem(25523, 1) || !player.getInventory().containsItem(21776, 50)) {
                    player.getPackets().sendGameMessage("You need Ancestral Robe Top, an Upgrade Gem and 50 Shards of Armadyl to craft a Catalyst Robe Top");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(51021, 1);
                player.getInventory().deleteItem(25523, 1);
                player.getInventory().deleteItem(21776, 50);
                player.getInventory().addItem(25695, 1);
                player.getPackets().sendGameMessage("Using your magical strength, you fuse the items together to craft a Catalyst Robe Top!");
            } else if (contains(51024, 25523, itemUsed, usedWith) || contains(51024, 21776, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(51024, 1) || !player.getInventory().containsItem(25523, 1) || !player.getInventory().containsItem(21776, 50)) {
                    player.getPackets().sendGameMessage("You need Ancestral Robe Bottoms, an Upgrade Gem and 50 Shards of Armadyl to craft Catalyst Robe Bottoms");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(51024, 1);
                player.getInventory().deleteItem(25523, 1);
                player.getInventory().deleteItem(21776, 50);
                player.getInventory().addItem(25696, 1);
                player.getPackets().sendGameMessage("Using your magical strength, you fuse the items together to craft Catalyst Robe Bottoms!");
            } else if (contains(6922, 25523, itemUsed, usedWith) || contains(6922, 21776, itemUsed, usedWith) || contains(6922, 49544, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(6922, 1) || !player.getInventory().containsItem(25523, 1) || !player.getInventory().containsItem(21776, 50) || !player.getInventory().containsItem(49544, 1)) {
                    player.getPackets().sendGameMessage("You need infinity gloves, a Tormented Bracelet, an Upgrade Gem and 50 Shards of Armadyl to craft a pair of Catalyst Gloves");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(49544, 1);
                player.getInventory().deleteItem(6922, 1);
                player.getInventory().deleteItem(25523, 1);
                player.getInventory().deleteItem(21776, 50);
                player.getInventory().addItem(25697, 1);
                player.getPackets().sendGameMessage("Using your magical strength, you fuse the items together to craft a pair of Catalyst Gloves!");
            } else if (contains(6920, 25523, itemUsed, usedWith) || contains(6920, 21776, itemUsed, usedWith) || contains(6920, 21793, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(6920, 1) || !player.getInventory().containsItem(25523, 1) || !player.getInventory().containsItem(21776, 50) || !player.getInventory().containsItem(21793, 1)) {
                    player.getPackets().sendGameMessage("You need Infinity Boots, Ragefire Boots, an Upgrade Gem and 50 Shards of Armadyl to craft a pair of Catalyst Boots");
                    return;
                }
                player.lock(2);
                player.lock(2);
                player.getInventory().deleteItem(21793, 1);
                player.getInventory().deleteItem(6920, 1);
                player.getInventory().deleteItem(25523, 1);
                player.getInventory().deleteItem(21776, 50);
                player.getInventory().addItem(25698, 1);
                player.getPackets().sendGameMessage("Using your magical strength, you fuse the items together to craft a pair of Catalyst Boots!");
            } else if (contains(21777, 25701, itemUsed, usedWith) || contains(21777, 21776, itemUsed, usedWith) || contains(21777, 54422, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(21777, 1) || !player.getInventory().containsItem(25701, 1) || !player.getInventory().containsItem(21776, 250) || !player.getInventory().containsItem(54422, 1)) {
                    player.getPackets().sendGameMessage("You need an Armadyl Battlestaff, a Nightmare Staff, a Cataclysm Orb and 250 Shards of Armadyl to create the Cataclysm Staff!");
                    return;
                }
                player.lock(2);
                player.lock(2);
                player.getInventory().deleteItem(54422, 1);
                player.getInventory().deleteItem(21777, 1);
                player.getInventory().deleteItem(25701, 1);
                player.getInventory().deleteItem(21776, 250);
                player.getInventory().addItem(25699, 1);
                player.getPackets().sendGameMessage("Using your magical strength, you fuse the items together to create the Cataclysm Staff!");

            } else if (contains(25700, 25495, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(25700, 1);
                player.getInventory().deleteItem(25495, 1);
                player.getInventory().addItem(25702, 1);
                player.getPackets().sendGameMessage("You combine a Catalyst Sigil with an Almighty Spirit Shield to create the Catalyst Spirit Shield!");
            } else if (contains(42002, 25483, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(25484, 1);
                player.getPackets().sendGameMessage("The occult necklace fuses with the soul stone.");
            } else if (contains(54419, 52326, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(25578, 1);
                player.getPackets().sendGameMessage("The armour combines together. You can smell death from it.");
            } else if (contains(54420, 52327, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(25577, 1);
                player.getPackets().sendGameMessage("The armour combines together. You can smell death from it.");
            } else if (contains(54421, 52328, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(25576, 1);
                player.getPackets().sendGameMessage("The armour combines together. You can smell death from it.");
		/*} else if (contains(25470, 25484, itemUsed, usedWith)) {
				player.lock(2);
				player.getInventory().deleteItem(itemUsed);
				player.getInventory().deleteItem(usedWith);
				player.getInventory().addItem(25485, 1);
				player.getPackets().sendGameMessage("The looter's amulet fuses with your soul necklace.");
			} else if (contains(25479, 25485, itemUsed, usedWith)
					|| contains(25479, 25480, itemUsed, usedWith)
					|| contains(25480, 25485, itemUsed, usedWith)) {
				
				if (!player.getInventory().containsItem(25479, 1)
						|| !player.getInventory().containsItem(25480, 1)
						|| !player.getInventory().containsItem(25485, 1)) {
					player.getPackets().sendGameMessage("You need torture, anguish and soul looter's to create an infinity necklace.");
					return;
				}
				player.lock(2);
				player.getInventory().deleteItem(25479, 1);
				player.getInventory().deleteItem(25480, 1);
				player.getInventory().deleteItem(25485, 1);
				player.getInventory().addItem(25486, 1);
				player.getPackets().sendGameMessage("The three amulets combine into a mythical amulet.");*/
            } else if (contains(25627, 25495, itemUsed, usedWith) || contains(25627, 51000, itemUsed, usedWith) || contains(51000, 25495, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(25495, 1) || !player.getInventory().containsItem(51000, 1) || !player.getInventory().containsItem(25627, 1)) {
                    player.getPackets().sendGameMessage("You need a Corrosive sigil, Twisted buckler and an Almighty spirit shield to create a corrosive spirit shield.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(25495, 1);
                player.getInventory().deleteItem(51000, 1);
                player.getInventory().deleteItem(25627, 1);
                player.getInventory().addItem(25628, 1);
                World.sendNews("<col=FF4500><shad=0>" + player.getDisplayName() + " has created a Corrosive spirit shield.", 1);
                player.getPackets().sendGameMessage("You combine the Corrosive sigil, Twisted buckler and an Almighty spirit shield to create a Corrosive spirit shield.");
            } else if (contains(25631, 13902, itemUsed, usedWith) || contains(25631, 43576, itemUsed, usedWith) || contains(25631, 51003, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(25631, 1) || !player.getInventory().containsItem(13902, 1) || !player.getInventory().containsItem(43576, 1) || !player.getInventory().containsItem(51003, 1)) {
                    player.getPackets().sendGameMessage("You need Tekton's blueprints, Statius' warhammer, Elder maul and a Dragon warhammer to create an Elder warhammer.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(25631, 1);
                player.getInventory().deleteItem(13902, 1);
                player.getInventory().deleteItem(43576, 1);
                player.getInventory().deleteItem(51003, 1);
                player.getInventory().addItem(25630, 1);
                World.sendNews("<col=FF4500><shad=0>" + player.getDisplayName() + " has created an Elder warhammer.", 1);
                player.getPackets().sendGameMessage("You combine Tekton's blueprints, Statius' warhammer, Elder maul and a Dragon warhammer to create an Elder warhammer.");
            } else if (contains(14484, 25632, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(14484, 1);
                player.getInventory().deleteItem(25632, 1);
                player.getInventory().addItem(25633, 1);
                World.sendNews("<col=FF4500><shad=0>" + player.getDisplayName() + " has created Dragonhunter claws.", 1);
                player.getPackets().sendGameMessage("You attach Olm's claw to the Dragon claws and create Dragonhunter claws.");
            } else if (contains(51012, 25632, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(51012, 1);
                player.getInventory().deleteItem(25632, 1);
                player.getInventory().addItem(25629, 1);
                World.sendNews("<col=FF4500><shad=0>" + player.getDisplayName() + " has created a Dragonshredder crossbow.", 1);
                player.getPackets().sendGameMessage("You combine Olm's claw with the Dragonhunter crossbow and create a Dragonshredder crossbow.");
            } else if (contains(25470, itemUsed, usedWith) != null
                    || contains(25484, itemUsed, usedWith) != null
                    || contains(49553, itemUsed, usedWith) != null
                    || contains(49547, itemUsed, usedWith) != null
                    || contains(6585, itemUsed, usedWith) != null) {

                if (!player.getInventory().containsItem(25470, 1)
                        || !player.getInventory().containsItem(25484, 1)
                        || !player.getInventory().containsItem(49553, 1)
                        || !player.getInventory().containsItem(49547, 1)
                        || !player.getInventory().containsItem(6585, 1)) {
                    player.getPackets().sendGameMessage("You need fury, looters, torture, anguish and soul necklace to create an infinity necklace.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(6585, 1);
                player.getInventory().deleteItem(25470, 1);
                player.getInventory().deleteItem(25484, 1);
                player.getInventory().deleteItem(49553, 1);
                player.getInventory().deleteItem(49547, 1);
                player.getInventory().addItem(25486, 1);
                player.getPackets().sendGameMessage("The four amulets combine into a mythical amulet.");
            } else if (contains(25664, itemUsed, usedWith) != null && contains(25533, itemUsed, usedWith) != null) {
                player.getInventory().replaceItem(25662, 1, itemUsedId == 25533 ? fromSlot : toSlot);
                player.getInventory().deleteItem(25664, 1);
                player.sendMessage("You combine the Twisted bow Mk. II with the Easter dye.");
            } else if (contains(25664, itemUsed, usedWith) != null && contains(25529, itemUsed, usedWith) != null) {
                player.getInventory().replaceItem(25663, 1, itemUsedId == 25529 ? fromSlot : toSlot);
                player.getInventory().deleteItem(25664, 1);
                player.sendMessage("You combine the Legendary lightning rapier (u) with the Easter dye.");
            } else if (contains(21787 , itemUsed, usedWith) != null
                    || contains(21790 , itemUsed, usedWith) != null
                    || contains(21793 , itemUsed, usedWith) != null
                    || contains(43239 , itemUsed, usedWith) != null
                    || contains(43237 , itemUsed, usedWith) != null
                    || contains(43235 , itemUsed, usedWith) != null
                    || contains(51733 , itemUsed, usedWith) != null
                    || contains(24989 , itemUsed, usedWith) != null
                    || contains(24986 , itemUsed, usedWith) != null
                    || contains(24983 , itemUsed, usedWith) != null) {

                for (int id : BOOTS_OF_THE_GODS_ITEMS) {
                    if (!player.getInventory().containsItem(id, 1)) {
                        player.getPackets().sendGameMessage("You need " + ItemConfig.forID(id).getName() + " to craft The boots of the gods.");
                        return;
                    }
                }


                if (!(player.getInventory().containsItem(24989 , 1)
                        || player.getInventory().containsItem(24986 , 1)
                        || player.getInventory().containsItem(24983, 1))) {
                    player.getPackets().sendGameMessage("You need ONE of the following: Torva boots OR Pernix boots OR Virtus boots.");
                    return;
                }

                if(player.getControlerManager().getControler() != null) {
                    player.sendMessage(Colour.GOLD.wrap("You cannot do this now."));
                    return;
                }

                player.lock(16);


                player.sendMessage(Colour.GOLD.wrap("A great force teleports you..."));
                player.gfx(1177);
                WorldTasksManager.schedule(() -> {
                    player.setDirection(Direction.SOUTH, true);
                    player.setNextWorldTile(3111, 3468, 0); }, 2);


                WorldTasksManager.schedule(() -> {
                    player.gfx(2195); }, 8);
                WorldTasksManager.schedule(() -> {
                    player.gfx(2345);
                    player.anim(2563);
                    player.sendMessage(Colour.GOLD.wrap("You feel a great presence surround you.."));
                  }, 3);

                WorldTile base = new WorldTile(3108, 3467, 0);

                for(int i =3; i < 15; i++) {
                    WorldTasksManager.schedule(() -> {
                        World.sendGraphics(780, base.relative(Utils.random(7), Utils.random(2)));
                        }, i);
                }


                WorldTasksManager.schedule(() -> {
                    for (int id : BOOTS_OF_THE_GODS_ITEMS) {
                        if (!player.getInventory().containsItem(id, 1)) {
                            // in case they somehow drop an item
                            player.getPackets().sendGameMessage("The incantation fails.");
                            return;
                        }
                    }


                    if (!(player.getInventory().containsItem(24989 , 1)
                            || player.getInventory().containsItem(24986 , 1)
                            || player.getInventory().containsItem(24983, 1))) {
                        // in case they somehow drop an item
                        player.getPackets().sendGameMessage("The incantation fails.");
                        return;
                    }

                    player.getPackets().sendGameMessage(Colour.GOLD.wrap("An ethereal spirit removes the most powerful boots of Matrix from your inventory.."));
                    player.getPackets().sendGameMessage(Colour.GOLD.wrap("You are left with but one pair; The boots of the gods."));


                    World.sendNews("<col=ff0000><col=" + Colour.CRIMSON.hex + ">" + player.getDisplayName() + " has created The boots of the gods!", 1);

                    for (int id : BOOTS_OF_THE_GODS_ITEMS)
                        player.getInventory().deleteItem(id, 1);

                    // only delete Torva OR Pernix OR Virtus
                    if(player.getInventory().containsItem(24986, 1))
                        player.getInventory().deleteItem(24986, 1);
                    else if(player.getInventory().containsItem(24989, 1))
                        player.getInventory().deleteItem(24989, 1);
                    else player.getInventory().deleteItem(24983, 1);

                    player.getInventory().addItem(25659, 1);

                    for(int x = 0; x < 7; x ++) {
                        for(int y = 0; y < 3; y ++) {
                            if(Utils.rollDie(3))
                                continue;
                            Graphics gfx = new Graphics(1207, Utils.random(200), 0);
                            World.sendGraphics(null, gfx, new WorldTile(base.getX() + x, base.getY() + y, 0));
                        }
                    }

                }, 15);
            } else if (contains(7462 , itemUsed, usedWith) != null
                    || contains(52981 , itemUsed, usedWith) != null
                    || contains(49544 , itemUsed, usedWith) != null
                    || contains(18347 , itemUsed, usedWith) != null
                     || contains(22358 , itemUsed, usedWith) != null
                    || contains(22362 , itemUsed, usedWith) != null
                    || contains(22366 , itemUsed, usedWith) != null
                    || contains(24977 , itemUsed, usedWith) != null
                    || contains(24974 , itemUsed, usedWith) != null
                    || contains(24980 , itemUsed, usedWith) != null) {
                for (int id : GLOVES_OF_THE_GODS_ITEMS) {
                    if (!player.getInventory().containsItem(id, 1)) {
                        player.getPackets().sendGameMessage("You need " + ItemConfig.forID(id).getName() + " to craft The gloves of the gods.");
                        return;
                    }
                }
                int goliathGloves = -1;
                for (int id = 22358; id <= 22361; id++) {
                	if (player.getInventory().containsItem(id, 1)) {
                		goliathGloves = id;
                		break;
                	}
                }
                if (goliathGloves == -1) {
                    player.getPackets().sendGameMessage("You need goliath gloves to craft The gloves of the gods.");
                    return;
                }
                int swiftGloves = -1;
                for (int id = 22362; id <= 22365; id++) {
                	if (player.getInventory().containsItem(id, 1)) {
                		swiftGloves = id;
                		break;
                	}
                }
                if (swiftGloves == -1) {
                    player.getPackets().sendGameMessage("You need swift gloves to craft The gloves of the gods.");
                    return;
                }
                int spellcasterGloves = -1;
                for (int id = 22366; id <= 22369; id++) {
                	if (player.getInventory().containsItem(id, 1)) {
                		spellcasterGloves = id;
                		break;
                	}
                }
                if (spellcasterGloves == -1) {
                    player.getPackets().sendGameMessage("You need spellcaster gloves to craft The gloves of the gods.");
                    return;
                }


                if (!(player.getInventory().containsItem(24974 , 1)
                        || player.getInventory().containsItem(24977 , 1)
                        || player.getInventory().containsItem(24980, 1))) {
                    player.getPackets().sendGameMessage("You need ONE of the following: Torva gloves OR gloves boots OR Virtus gloves.");
                    return;
                }

                if(player.getControlerManager().getControler() != null) {
                    player.sendMessage(Colour.GOLD.wrap("You cannot do this now."));
                    return;
                }

                player.lock(16);


                player.sendMessage(Colour.GOLD.wrap("A great force teleports you..."));
                player.gfx(1177);
                WorldTasksManager.schedule(() -> {
                    player.setDirection(Direction.SOUTH, true);
                    player.setNextWorldTile(3111, 3468, 0); }, 2);


                WorldTasksManager.schedule(() -> {
                    player.gfx(2195); }, 8);
                WorldTasksManager.schedule(() -> {
                    player.gfx(2345);
                    player.anim(2563);
                    player.sendMessage(Colour.GOLD.wrap("You feel a great presence surround you.."));
                  }, 3);

                WorldTile base = new WorldTile(3108, 3467, 0);

                for(int i =3; i < 15; i++) {
                    WorldTasksManager.schedule(() -> {
                        World.sendGraphics(785, base.relative(Utils.random(7), Utils.random(2)));
                        }, i);
                }
                
                
                
             	player.getInventory().deleteItem(goliathGloves, 1);
             	player.getInventory().deleteItem(swiftGloves, 1);
              	player.getInventory().deleteItem(spellcasterGloves, 1);


                WorldTasksManager.schedule(() -> {
                    for (int id : GLOVES_OF_THE_GODS_ITEMS) {
                        if (!player.getInventory().containsItem(id, 1)) {
                            // in case they somehow drop an item
                            player.getPackets().sendGameMessage("The incantation fails.");
                            return;
                        }
                    }


                    if (!(player.getInventory().containsItem(24974 , 1)
                            || player.getInventory().containsItem(24977 , 1)
                            || player.getInventory().containsItem(24980, 1))) {
                        // in case they somehow drop an item
                        player.getPackets().sendGameMessage("The incantation fails.");
                        return;
                    }

                    player.getPackets().sendGameMessage(Colour.GOLD.wrap("An ethereal spirit removes the most powerful gloves of Matrix from your inventory.."));
                    player.getPackets().sendGameMessage(Colour.GOLD.wrap("You are left with but one pair; The gloves of the gods."));


                    World.sendNews("<col=ff0000><col=" + Colour.CRIMSON.hex + ">" + player.getDisplayName() + " has created The gloves of the gods!", 1);

                    for (int id : GLOVES_OF_THE_GODS_ITEMS)
                        player.getInventory().deleteItem(id, 1);

                    // only delete Torva OR Pernix OR Virtus
                    if(player.getInventory().containsItem(24974, 1))
                        player.getInventory().deleteItem(24974, 1);
                    else if(player.getInventory().containsItem(24977, 1))
                        player.getInventory().deleteItem(24977, 1);
                    else
                    	player.getInventory().deleteItem(24980, 1);

                    player.getInventory().addItem(25672, 1); 

                    for(int x = 0; x < 7; x ++) {
                        for(int y = 0; y < 3; y ++) {
                            if(Utils.rollDie(3))
                                continue;
                            Graphics gfx = new Graphics(1201, Utils.random(200), 0);
                            World.sendGraphics(null, gfx, new WorldTile(base.getX() + x, base.getY() + y, 0));
                        }
                    }

                }, 15);
            } else if (contains(54422, 54511, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(54423, 1);
            } else if (contains(54422, 54514, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(54424, 1);
            } else if (contains(54422, 54517, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(54425, 1);
            } else if (contains(25500, 15492, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(25499, 1);
                player.getPackets().sendGameMessage("The helmet turns red in blood as the curse spreads through it.");
            } else if (contains(25498, 52323, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(25496, 1);
                player.getPackets().sendGameMessage("Your staff absorbs the curse.");
            } else if (contains(25527, itemUsed, usedWith) != null) {
                if (!player.getInventory().containsItem(11694, 1)
                        || !player.getInventory().containsItem(11696, 1)
                        || !player.getInventory().containsItem(11698, 1)
                        || !player.getInventory().containsItem(11700, 1)) {
                    player.getPackets()
                            .sendGameMessage("You need all the godswords in inventory in order to do this.");
                    return;
                }
                player.lock(1);
                player.getInventory().deleteItem(11694, 1);
                player.getInventory().deleteItem(11696, 1);
                player.getInventory().deleteItem(11698, 1);
                player.getInventory().deleteItem(11700, 1);
                player.getInventory().deleteItem(25527, 1);
                player.getInventory().addItem(25526, 1);
                player.getPackets().sendGameMessage("You remold the godswords into one. Hail " + player.getName() + " the almighty!");

            } else if (contains(25497, itemUsed, usedWith) != null) {
                if (!player.getInventory().containsOneItem(13738, 23697)
                        || !player.getInventory().containsOneItem(13740, 23698)
                        || !player.getInventory().containsOneItem(13742, 23699)
                        || !player.getInventory().containsOneItem(13744, 23700)) {
                    player.getPackets()
                            .sendGameMessage("You need all the spirit shields in inventory in order to do this.");
                    return;
                }
                player.lock(1);
                player.getInventory().deleteItem(player.getInventory().containsItem(13738, 1) ? 13738 : 23697, 1);
                player.getInventory().deleteItem(player.getInventory().containsItem(13740, 1) ? 13740 : 23698, 1);
                player.getInventory().deleteItem(player.getInventory().containsItem(13742, 1) ? 13742 : 23699, 1);
                player.getInventory().deleteItem(player.getInventory().containsItem(13744, 1) ? 13744 : 23700, 1);
                player.getInventory().deleteItem(25497, 1);
                player.getInventory().addItem(25495, 1);
                player.getPackets().sendGameMessage("The spirit shields fuse with the blood sigil melting into a single one.");
            } else if (contains(25481, itemUsed, usedWith) != null) {
                int toID = AccessorySmithing.getImbuedId(contains(25481, itemUsed, usedWith).getId());
                if (toID == -1) {
                    player.getPackets().sendGameMessage("You can not imbue this item.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(toID, 1);
                player.getPackets().sendGameMessage("Your item shines as you read the scroll incantation.");
            } else if (contains(25739, itemUsed, usedWith) != null) {
                int toID = AccessorySmithing.getImbuedIdMythical(contains(25739, itemUsed, usedWith).getId());
                if (toID == -1) {
                    player.getPackets().sendGameMessage("You can not imbue this item.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(toID, 1);
                player.getPackets().sendGameMessage("Your item shines as you read the scroll incantation.");
            } else if (contains(itemUsed.getId(), usedWith.getId(), INFINITY_RING_ITEMS)) {
                boolean gotAll = true;
                for (int id : INFINITY_RING_ITEMS) {
                    if (!player.getInventory().containsItem(id, 1)) {
                        player.getPackets().sendGameMessage("You missing ring: " + ItemConfig.forID(id).getName() + " to craft infinity ring.");
                        gotAll = false;
                    }
                }
                if (!gotAll)
                    return;
                for (int id : INFINITY_RING_ITEMS)
                    player.getInventory().deleteItem(id, 1);
                player.getInventory().addItem(25488, 1);
                player.getPackets().sendGameMessage("The rings all resonate together fusing into a dimensional ring.");
            } else if (contains(11728, 51730, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(11728, 1);
                player.getInventory().deleteItem(51730, 1);
                player.getInventory().addItem(51733, 1);
                player.getPackets().sendGameMessage("The second you hold your tourmaline core by the Bandos boots, the core is absorbed rapidly. Perhaps there is some link between the metal in Bandos and tourmaline..");
            } else if (contains(11728, 51730, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(11728, 1);
                player.getInventory().deleteItem(51730, 1);
                player.getInventory().addItem(51733, 1);
                player.getPackets().sendGameMessage("The second you hold your tourmaline core by the Bandos boots, the core is absorbed rapidly. Perhaps there is some link between the metal in Bandos and tourmaline..");
            } else if (contains(25478, 52325, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(25478, 1);
                player.getInventory().deleteItem(52325, 1);
                player.getInventory().addItem(25476, 1);
                player.getPackets().sendGameMessage("Your scythe absorves the gem and starts shining. You sense an evil presence radiating from your it.");
                World.sendNews(player.getDisplayName() + " has successfully created an " + ItemConfig.forID(25476).getName() + "!", 1);
            } else if (contains(25765, 25496, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(25765, 1);
                player.getInventory().deleteItem(25496, 1);
                player.getInventory().addItem(25764, 1);
                player.getPackets().sendGameMessage("Your staff absorves the gem and starts shining. You sense an evil presence radiating from your it.");
                World.sendNews(player.getDisplayName() + " has successfully created an " + ItemConfig.forID(25764).getName() + "!", 1);
            } else if (contains(41889, 52966, itemUsed, usedWith)) {
                player.lock(2);
                player.getInventory().deleteItem(41889, 1);
                player.getInventory().deleteItem(52966, 1);
                player.getInventory().addItem(52978, 1);
                player.getPackets().sendGameMessage("You successfully combine the Hydra claw and the Zamorakian hasta to create the Dragon hunter lance.");
            } else if (contains(52988, 18337, itemUsed, usedWith)
                    || contains(52988, 52111, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(18337, 1)
                        || !player.getInventory().containsItem(52111, 1)) {
                    player.getPackets()
                            .sendGameMessage("You need a bonecrusher and a dragonbone necklace to do this.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(52988, 1);
                player.getInventory().deleteItem(18337, 1);
                player.getInventory().deleteItem(51791, 1);
                player.getInventory().addItem(52986, 1);
                player.getPackets().sendGameMessage("The claw fuses into the dragonbone necklace.");
            } else if (contains(2412, 51797, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(51797, 3)) {
                    player.getPackets()
                            .sendGameMessage("You need three demon hearts to imbue this camp.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(2412, 1);
                player.getInventory().deleteItem(51797, 3);
                player.getInventory().addItem(51791, 1);
                player.getPackets().sendGameMessage("You imbue your saradomin cape with the power of the demon hearts.");
            } else if (contains(2413, 51798, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(51798, 3)) {
                    player.getPackets().sendGameMessage("You need three demon hearts to imbue this camp.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(2413, 1);
                player.getInventory().deleteItem(51798, 3);
                player.getInventory().addItem(51793, 1);
                player.getPackets().sendGameMessage("You imbue your guthix cape with the power of the demon hearts.");
            } else if (contains(2414, 51799, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(51799, 3)) {
                    player.getPackets().sendGameMessage("You need three demon hearts to imbue this camp.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(2414, 1);
                player.getInventory().deleteItem(51799, 3);
                player.getInventory().addItem(51795, 1);
                player.getPackets().sendGameMessage("You imbue your zamorak cape with the power of the demon hearts.");
            } else if (contains(1755, 42927, itemUsed, usedWith)) {
                if (player.getSkills().getLevel(Skills.CRAFTING) < 52) {
                    player.getPackets()
                            .sendGameMessage("You need a Crafting level of 52 in order to make a serpentine helm.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(42927, 1);
                player.getInventory().addItem(42929, 1);
                player.getPackets().sendGameMessage("You craft a serpentine helm.");
                player.getSkills().addXp(Skills.CRAFTING, 120);
            } else if (contains(41941, itemUsed, usedWith) != null) {
                player.getLootingBag().addItem(itemUsed.getId() == 41941 ? toSlot : fromSlot);
            } else if (contains(42934, 42929, itemUsed, usedWith) || contains(42934, 42931, itemUsed, usedWith)) { // serpentine
                // hat
                int charges = (ItemConstants.getItemDefaultCharges(42931) - player.getCharges().getCharges(42931, contains(42934, 42931, itemUsed, usedWith)));
                if (charges == 0)
                    return;
                if (!player.getInventory().containsItem(42934, charges / 10)) {
                    player.getPackets().sendGameMessage(
                            "You need " + charges / 10 + " zulrah scales in order to charge this item.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(42934, charges / 10);
                player.getCharges().maxCharges(42931);
                if (itemUsed.getId() == 42929 || usedWith.getId() == 42929) {
                    player.getInventory().deleteItem(42929, 1);
                    player.getInventory().addItem(42931, 1);
                }
                player.getPackets().sendGameMessage("You attach the zulrah scales to the item.");
            } else if (contains(42934, 42902, itemUsed, usedWith) || contains(42934, 42904, itemUsed, usedWith)) { // serpentine
                // hat
                int charges = (ItemConstants.getItemDefaultCharges(42904) - player.getCharges().getCharges(42904));
                if (!player.getInventory().containsItem(42934, charges / 10)) {
                    player.getPackets().sendGameMessage(
                            "You need " + charges / 10 + " zulrah scales in order to charge this item.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(42934, charges / 10);
                player.getCharges().maxCharges(42904);
                if (itemUsed.getId() == 42902 || usedWith.getId() == 42902) {
                    player.getInventory().deleteItem(42902, 1);
                    player.getInventory().addItem(42904, 1);
                }
                player.getPackets().sendGameMessage("You attach the zulrah scales to the item.");
            } else if (contains(1755, 42922, itemUsed, usedWith)) {
                if (player.getSkills().getLevel(Skills.FLETCHING) < 53) {
                    player.getPackets()
                            .sendGameMessage("You need a Fletching level of 53 in order to make a toxic blowpipe.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(42922, 1);
                player.getInventory().addItem(42924, 1);
                player.getPackets().sendGameMessage("You craft a toxic blowpipe.");
                player.getSkills().addXp(Skills.FLETCHING, 120);
            } else if (contains(42934, 42924, itemUsed, usedWith) || contains(42934, 42926, itemUsed, usedWith)) {
                int charges = (ItemConstants.getItemDefaultCharges(42926)
                        - player.getCharges().getCharges(42926, contains(42934, 42926, itemUsed, usedWith)));
                if (charges == 0)
                    return;
                if (!player.getInventory().containsItem(42934, charges / 10)) {
                    player.getPackets().sendGameMessage(
                            "You need " + charges / 10 + " zulrah scales in order to charge this item.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(42934, charges / 10);
                player.getCharges().maxCharges(42926);
                if (itemUsed.getId() == 42924 || usedWith.getId() == 42924) {
                    player.getInventory().deleteItem(42924, 1);
                    player.getInventory().addItem(42926, 1);
                }
                player.getPackets().sendGameMessage("You attach the zulrah scales to the item.");
            } else if (PlayerCombat.chargeBlowpipe(player, usedWith, itemUsed))
                return;
            else if (PlayerCombat.chargeInfernalBlowpipe(player, usedWith, itemUsed))
                return;
            else if (contains(52386, Pets.OLMLET.getBabyItemId(), itemUsed, usedWith)) {
                player.sendMessage("You must use the Metamorphic dust on Olmlet while it is following you.");
            } else if (contains(41791, 42932, itemUsed, usedWith)) {
                if (player.getSkills().getLevel(Skills.CRAFTING) < 59) {
                    player.getPackets()
                            .sendGameMessage("You need a Crafting level of 59 in order to make a toxic staff of the dead.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(41791, 1);
                player.getInventory().deleteItem(42932, 1);
                player.getInventory().addItem(42902, 1);
                player.getPackets().sendGameMessage("You craft a toxic staff of the dead.");
            } else if (contains(41905, 42932, itemUsed, usedWith)
                    || contains(41908, 42932, itemUsed, usedWith)) {
                if (player.getSkills().getLevel(Skills.CRAFTING) < 59) {
                    player.getPackets()
                            .sendGameMessage("You need a Crafting level of 59 in order to make a trident of the swamp.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(42900, 1);
                player.getPackets().sendGameMessage("You craft a trident of the swamp.");
            } else if (contains(42900, 554, itemUsed, usedWith) || contains(42900, 560, itemUsed, usedWith)
                    || contains(42900, 562, itemUsed, usedWith) || contains(42900, 42934, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(554, 12500)) {
                    player.getPackets().sendGameMessage("You need 12500 fire runes in order  to charge this staff.");
                    return;
                } else if (!player.getInventory().containsItem(562, 2500)) {
                    player.getPackets().sendGameMessage("You need 2500 chaos runes in order to charge this staff.");
                    return;
                } else if (!player.getInventory().containsItem(560, 2500)) {
                    player.getPackets().sendGameMessage("You need 2500 death runes in order to charge this staff.");
                    return;
                } else if (!player.getInventory().containsItem(42934, 2500)) {
                    player.getPackets().sendGameMessage("You need 2500 zulrah scales in order to charge this staff.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(554, 12500);
                player.getInventory().deleteItem(562, 2500);
                player.getInventory().deleteItem(560, 2500);
                player.getInventory().deleteItem(42934, 2500);
                player.getInventory().deleteItem(42900, 1);
                player.getInventory().addItem(42899, 1);
                player.getPackets().sendGameMessage("You charge your trident of the swamp.");
            } else if (contains(41908, 554, itemUsed, usedWith) || contains(41908, 560, itemUsed, usedWith)
                    || contains(41908, 562, itemUsed, usedWith) || contains(41908, 995, itemUsed, usedWith)) {
                if (!player.getInventory().containsItem(554, 12500)) {
                    player.getPackets().sendGameMessage("You need 12500 fire runes in order  to charge this staff.");
                    return;
                } else if (!player.getInventory().containsItem(562, 2500)) {
                    player.getPackets().sendGameMessage("You need 2500 chaos runes in order to charge this staff.");
                    return;
                } else if (!player.getInventory().containsItem(560, 2500)) {
                    player.getPackets().sendGameMessage("You need 2500 death runes in order to charge this staff.");
                    return;
                } else if (player.getInventory().getCoinsAmount() < 25000) {
                    player.getPackets().sendGameMessage("You need 25000 coins in order to charge this staff.");
                    return;
                }
                player.lock(2);
                player.getInventory().deleteItem(554, 12500);
                player.getInventory().deleteItem(562, 2500);
                player.getInventory().deleteItem(560, 2500);
                player.getInventory().deleteItem(995, 25000);
                player.getInventory().deleteItem(41908, 1);
                player.getInventory().addItem(41905, 1);
                player.getPackets().sendGameMessage("You charge your trident of the seas.");
            } else if (contains(22498, 554, itemUsed, usedWith) || contains(22498, 22448, itemUsed, usedWith)) {
                if (player.getSkills().getLevel(Skills.FARMING) < 80) {
                    player.getPackets()
                            .sendGameMessage("You need a Farming level of 80 in order to make a polypore staff.");
                    return;
                } else if (!player.getInventory().containsItem(22448, 3000)) {
                    player.getPackets()
                            .sendGameMessage("You need 3,000 polypore spores in order to make a polypore staff.");
                    return;
                } else if (!player.getInventory().containsItem(554, 15000)) {
                    player.getPackets()
                            .sendGameMessage("You need 15,000 fire runes in order to make a polypore staff.");
                    return;
                }
                player.setNextAnimation(new Animation(15434));
                player.lock(2);
                player.getInventory().deleteItem(554, 15000);
                player.getInventory().deleteItem(22448, 3000);
                player.getInventory().deleteItem(22498, 1);
                player.getInventory().addItem(22494, 1);
                player.getPackets().sendGameMessage(
                        "You attach the polypore spores and infuse the fire runes to the stick in order to create a staff.");
            } else if (contains(22496, 22448, itemUsed, usedWith)) {
                if (player.getSkills().getLevel(Skills.FARMING) < 80) {
                    player.getPackets()
                            .sendGameMessage("You need a Farming level of 80 in order to recharge polypore staff.");
                    return;
                }
                int charges = 3000 - player.getCharges().getCharges(22496);
                if (!player.getInventory().containsItem(22448, charges)) {
                    player.getPackets().sendGameMessage(
                            "You need " + charges + " polypore spores in order to recharge polypore staff.");
                    return;
                }
                player.setNextAnimation(new Animation(15434));
                player.lock(2);
                player.getInventory().deleteItem(22448, charges);
                player.getInventory().deleteItem(22496, 1);
                player.getCharges().resetCharges(22496);
                player.getInventory().addItem(22494, 1);
                player.getPackets().sendGameMessage("You attach the polypore spores to the staff.");
            } else if (contains(11710, 11712, itemUsed, usedWith) || contains(11710, 11714, itemUsed, usedWith)
                    || contains(11712, 11714, itemUsed, usedWith))
                GodswordCreating.joinPieces(player, false);
            else if (Slayer.createSlayerHelmet(player, itemUsedId, usedWithId))
                return;
            else if (itemUsedId == 23191 || usedWithId == 23191) {
                Drink pot = Drinkables.getDrink(itemUsedId == 23191 ? usedWithId : itemUsedId);
                if (pot == null)
                    return;
                player.getDialogueManager().startDialogue("FlaskDecantingD", pot);
            } else if (contains(11690, 11702, itemUsed, usedWith))
                GodswordCreating.attachHilt(player, 0);
            else if (contains(11690, 11704, itemUsed, usedWith))
                GodswordCreating.attachHilt(player, 1);
            else if (contains(11690, 11706, itemUsed, usedWith))
                GodswordCreating.attachHilt(player, 2);
            else if (contains(11690, 11708, itemUsed, usedWith))
                GodswordCreating.attachHilt(player, 3);
            else if (contains(SpiritshieldCreating.HOLY_ELIXIR, SpiritshieldCreating.SPIRIT_SHIELD, itemUsed, usedWith))
                player.getPackets().sendGameMessage("The shield must be blessed at an altar.");
            else if (contains(SpiritshieldCreating.SPIRIT_SHIELD, 13746, itemUsed, usedWith)
                    || contains(SpiritshieldCreating.SPIRIT_SHIELD, 13748, itemUsed, usedWith)
                    || contains(SpiritshieldCreating.SPIRIT_SHIELD, 13750, itemUsed, usedWith)
                    || contains(SpiritshieldCreating.SPIRIT_SHIELD, 13752, itemUsed, usedWith))
                player.getPackets().sendGameMessage("You need a blessed spirit shield to attach the sigil to.");
            else if (contains(SqirkFruitSqueeze.SqirkFruit.AUTUMM.getFruitId(), Herblore.PESTLE_AND_MORTAR, itemUsed,
                    usedWith))
                player.getDialogueManager().startDialogue("SqirkFruitSqueeze", SqirkFruit.AUTUMM);
            else if (contains(SqirkFruitSqueeze.SqirkFruit.SPRING.getFruitId(), Herblore.PESTLE_AND_MORTAR, itemUsed,
                    usedWith))
                player.getDialogueManager().startDialogue("SqirkFruitSqueeze", SqirkFruit.SPRING);
            else if (contains(SqirkFruitSqueeze.SqirkFruit.SUMMER.getFruitId(), Herblore.PESTLE_AND_MORTAR, itemUsed,
                    usedWith))
                player.getDialogueManager().startDialogue("SqirkFruitSqueeze", SqirkFruit.SUMMER);
            else if (contains(SqirkFruitSqueeze.SqirkFruit.WINTER.getFruitId(), Herblore.PESTLE_AND_MORTAR, itemUsed,
                    usedWith))
                player.getDialogueManager().startDialogue("SqirkFruitSqueeze", SqirkFruit.WINTER);
            else if (contains(43231, 11732, itemUsed, usedWith)) {
                if (player.getSkills().getLevelForXp(Skills.RUNECRAFTING) < 60
                        || player.getSkills().getLevelForXp(Skills.MAGIC) < 60) {
                    player.getPackets().sendGameMessage(
                            "You need a Runecrafting level of 60  and Magic level of 60 to create this item.");
                    return;
                }
                player.getSkills().addXp(Skills.RUNECRAFTING, 200);
                player.getSkills().addXp(Skills.MAGIC, 200);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(43239, 1);
            } else if (contains(43227, 6920, itemUsed, usedWith)) {
                if (player.getSkills().getLevelForXp(Skills.RUNECRAFTING) < 60
                        || player.getSkills().getLevelForXp(Skills.MAGIC) < 60) {
                    player.getPackets().sendGameMessage(
                            "You need a Runecrafting level of 60  and Magic level of 60 to create this item.");
                    return;
                }
                player.getSkills().addXp(Skills.RUNECRAFTING, 200);
                player.getSkills().addXp(Skills.MAGIC, 200);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(43235, 1);
            } else if (contains(43229, 2577, itemUsed, usedWith)) {
                if (player.getSkills().getLevelForXp(Skills.RUNECRAFTING) < 60
                        || player.getSkills().getLevelForXp(Skills.MAGIC) < 60) {
                    player.getPackets().sendGameMessage(
                            "You need a Runecrafting level of 60  and Magic level of 60 to create this item.");
                    return;
                }
                player.getSkills().addXp(Skills.RUNECRAFTING, 200);
                player.getSkills().addXp(Skills.MAGIC, 200);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(43237, 1);
            } else if (contains(43233, 6739, itemUsed, usedWith)) {
                if (player.getSkills().getLevelForXp(Skills.WOODCUTTING) < 61
                        || player.getSkills().getLevelForXp(Skills.FIREMAKING) < 85) {
                    player.getPackets().sendGameMessage(
                            "You need a Woodcutting level of 61  and Firemaking level of 85 to create this item.");
                    return;
                }
                player.getSkills().addXp(Skills.WOODCUTTING, 200);
                player.getSkills().addXp(Skills.FIREMAKING, 300);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(43241, 1);
                player.getPackets()
                        .sendGameMessage("You infuse the smouldering stone into the axe to make an infernal axe.");
            } else if (contains(43233, 15259, itemUsed, usedWith)) {
                if (player.getSkills().getLevelForXp(Skills.MINING) < 61
                        || player.getSkills().getLevelForXp(Skills.SMITHING) < 85) {
                    player.getPackets().sendGameMessage(
                            "You need a Mining level of 61  and Smithing level of 85 to create this item.");
                    return;
                }
                player.getSkills().addXp(Skills.MINING, 200);
                player.getSkills().addXp(Skills.SMITHING, 300);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(43243, 1);
                player.getPackets().sendGameMessage(
                        "You infuse the smouldering stone into the pickaxe to make an infernal pickaxe.");
            } else if (contains(43233, 15259, itemUsed, usedWith)) {
                if (player.getSkills().getLevelForXp(Skills.FISHING) < 75
                        || player.getSkills().getLevelForXp(Skills.COOKING) < 85) {
                    player.getPackets().sendGameMessage(
                            "You need a Fishing level of 75 and Cooking level of 85 to create this item.");
                    return;
                }
                player.getSkills().addXp(Skills.FISHING, 200);
                player.getSkills().addXp(Skills.COOKING, 350);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(51031, 1);
                player.getPackets().sendGameMessage(
                        "You infuse the smouldering stone into the harpoon to make an infernal harpoon.");
            } else if (contains(5976, 229, itemUsed, usedWith)) {
                player.getInventory().deleteItem(new Item(5976, 1));
                player.getInventory().deleteItem(new Item(229, 1));
                player.getInventory().addItem(new Item(Herblore.COCONUT_MILK, 1));
                player.getInventory().addItem(new Item(5978, 1));
                player.getPackets().sendGameMessage("You pour the milk of the coconut into a vial.");
            } else if (contains(4151, 21369, itemUsed, usedWith)) {
                if (!player.getSkills().hasRequiriments(Skills.ATTACK, 75, Skills.SLAYER, 80)) {
                    player.getPackets().sendGameMessage(
                            "You need an attack level of 75 and slayer level of 80 in order to attach the whip vine to the whip.");
                    return;
                }
                player.getInventory().replaceItem(21371, 1, toSlot);
                player.getInventory().deleteItem(fromSlot, itemUsed);
                player.getPackets().sendGameMessage("You attach the whip vine to the abbysal whip.");
            } else if (contains(4151, 42004, itemUsed, usedWith)) {
                player.getInventory().replaceItem(42006, 1, toSlot);
                player.getInventory().deleteItem(fromSlot, itemUsed);
                player.getPackets().sendGameMessage("You attach the kraken tentacle to the abbysal whip.");
            } else if (contains(10828, 54268, itemUsed, usedWith)) {
                player.getInventory().replaceItem(54271, 1, toSlot);
                player.getInventory().deleteItem(fromSlot, itemUsed);
                player.getPackets().sendGameMessage("You attach the Basilisk jaw to the helm of Neitiznot.");

                // player.getCharges().resetCharges(42926);
            } else if (contains(985, 987, itemUsed, usedWith)) { // crystal key
                // make
                player.getInventory().deleteItem(toSlot, usedWith);
                itemUsed.setId(989);
                player.getInventory().refresh(fromSlot);
                player.getPackets().sendGameMessage("You join the two halves of the key together.");
            } else
                player.getPackets().sendGameMessage("Nothing interesting happens.");
            if (Settings.DEBUG)
                Logger.log("ItemHandler", "Used:" + itemUsed.getId() + ", With:" + usedWith.getId());
        } else if ((interfaceId == 192 || interfaceId == 430 || interfaceId == 950)
                && interfaceId2 == Inventory.INVENTORY_INTERFACE
                && !player.getInterfaceManager().containsInventoryInter()) {
            if (toSlot >= 28)
                return;
            Item item = player.getInventory().getItem(toSlot);
            if (item == null || item.getId() != usedWithId)
                return;
            if (interfaceId == 192)
                Magic.processNormalSpell(player, interfaceComponent, (byte) toSlot);
            else if (interfaceId == 950)
                Magic.processDungSpell(player, interfaceComponent, (byte) toSlot, -1);
            else
                Magic.processLunarSpell(player, interfaceComponent, (byte) toSlot);
        }
        if (Settings.DEBUG)
            Logger.log("ItemHandler", "ItemOnItem " + usedWithId + ", " + toSlot + ", " + interfaceId + ", "
                    + interfaceComponent + ", " + fromSlot + ", " + itemUsedId);
    }

    public static void handleItemOption3(Player player, int slotId, int itemId, Item item) {
        if (player.isLocked() || player.getEmotesManager().isDoingEmote())
            return;
        player.stopAll(false);
        FlyingEntities impJar = FlyingEntities.forItem((short) itemId);
        if (impJar != null)
            FlyingEntityHunter.openJar(player, impJar, slotId);
        if (LightSource.lightSource(player, slotId))
            return;
        if (OrnamentKits.splitKit(player, item))
            return;
        if (item.getDefinitions().containsInventoryOption(2, "check") ? SCRewards.check(player, itemId)
                : SCRewards.transform(player, item.getId(), false, false))
            return;
        if(handle(player, item, 3, slotId))
            return;
        else if (item.getDefinitions().isBindItem())
            player.getDungManager().bind(item, slotId);
        else if (itemId >= 11095 && itemId <= 11103 && (itemId & 0x1) != 0)
            Revenant.useForinthryBrace(player, item, slotId);
        else if (itemId == CoalBag.COAL_BAG_ID || itemId == CoalBag.OPENED_COAL_BAG_ID)
            player.getCoalBag().check();
        else if (itemId == GemBag.GEM_BAG_ID || itemId == GemBag.OPENED_GEM_BAG_ID)
            player.getGemBag().check();
        else if (itemId >= 13281 && itemId <= 13288)
            player.getSlayerManager().checkKillsLeft();
        else if (itemId == MysteryBox.ID || itemId == MysteryBox.PREMIUM_ID)
            MysteryBox.preview(player);
        else if (itemId == ChristmasBox.ID)
            ChristmasBox.preview(player);
        else if (item.getId() == MysteryBox.BEG_ID)
            MysteryBox.previewBeg(player);
        else if(item.getId() == HalloweenBox.ID)
            HalloweenBox.preview(player);
        else if (itemId == MinigameBox.ID)
            MinigameBox.preview(player);
        else if (itemId == MysteryGodBox.ID)
            MysteryGodBox.preview(player);
        else if (itemId == MysteryAuraBox.ID)
            MysteryAuraBox.preview(player);
        else if (itemId == MysteryPetBox.ID)
            MysteryPetBox.preview(player);
        else if (itemId == MoneyBox.ID)
            MoneyBox.preview(player);
        else if (itemId == 15707)
            Magic.sendTeleportSpell(player, 13652, 13654, 2602, 2603, 1, 0, new WorldTile(3447, 3694, 0), 10, true, Magic.ITEM_TELEPORT);
        else if (itemId == 25528 || itemId == 20767 || itemId == 20769 || itemId == 20771)
            SkillCapeCustomizer.startCustomizing(player, itemId);
        else if (itemId == 52114)
            MythGuild.teleport(player);
        else if (ItemConstants.isLooters(itemId)) {
            /*player.switchAutoLoot();
            player.getPackets().sendGameMessage("Auto loot function: " + (player.isDisableAutoLoot() ? "Disabled" : "Enabled"));*/
            player.getDialogueManager().startDialogue("LootNeckD");
        } else if (itemId == 24437 || itemId == 24439 || itemId == 24440 || itemId == 24441)
            player.getDialogueManager().startDialogue("FlamingSkull", item, slotId);
        else if (Equipment.getItemSlot(itemId) == Equipment.SLOT_AURA)
            player.getAuraManager().sendTimeRemaining(itemId);
        else if (PrayerBooks.isGodBook(itemId, true))
            PrayerBooks.sermanize(player, itemId);
        else if (itemId == DollarContest.FRAGMENT_ID)
            DollarContest.info(player);
        else if (itemId == 11284 || itemId == 52003)
            player.getDialogueManager().startDialogue("SimpleMessage", "There are no charges left within this shield.");
        else if (itemId == 11283 || itemId == 52002)
            player.getCharges().checkCharges(
                    "There is " + ChargesManager.REPLACE + " charges remaining in your dragonfire shield.", itemId);
        else if (itemId == 22444)
            player.getCharges().checkCharges("There is " + ChargesManager.REPLACE + " doses of neem oil remaining.",
                    itemId);
        else if (itemId == 23029)
            player.getCharges().checkCharges("There is " + ChargesManager.REPLACE + " agility bonus xp remaining.",
                    itemId);
        else if ((itemId >= 24450 && itemId <= 24454) || (itemId >= 22358 && itemId <= 22369))
            player.getCharges().checkPercentage("The gloves are " + ChargesManager.REPLACE + "% degraded.", itemId,
                    true);
        else if (itemId >= 22458 && itemId <= 22497)
            player.getCharges().checkPercentage(item.getName() + ": " + ChargesManager.REPLACE + "% remaining.", itemId,
                    false);
        else if (itemId == 20171 || itemId == 20173)
            player.getCharges().checkPercentage(
                    item.getName() + ": has " + player.getCharges().getCharges(item.getId()) + " shots left.", itemId,
                    false);
        else if (itemId == 42926 || itemId == 25502) {
            if (itemId == 25502)
                player.getPackets().sendGameMessage("Internal blowpipe darts: " + (player.getInfernalBlowpipeDarts() == null ? "None"
                        : (player.getInfernalBlowpipeDarts().getName()) + " x "
                        + player.getInfernalBlowpipeDarts().getAmount())
                        + ".");
            else
                player.getCharges()
                        .checkPercentage("Blowpipe darts: "
                                + (player.getBlowpipeDarts() == null ? "None"
                                : (player.getBlowpipeDarts().getName() + " x "
                                + player.getBlowpipeDarts().getAmount()))
                                + ".  Scales: " + ChargesManager.REPLACE + "%", itemId, false);
        } else if (ItemConstants.getItemDefaultCharges(itemId) != -1)
            player.getCharges().checkPercentage("There is " + ChargesManager.REPLACE + "% of charge remaining.", itemId,
                    false);
        else if (itemId >= 20135 && itemId <= 20139 || itemId == 20171)
            player.getPackets().sendGameMessage("There is 100% of charge remaining.");
        else if (itemId == 21371) {
            player.getInventory().replaceItem(4151, 1, slotId);
            player.getInventory().addItem(21369, 1);
            player.getPackets().sendGameMessage("You split the whip vine from the abbysal whip.");
        } else if (itemId == 4155) {
            player.getInterfaceManager().sendInterface(1309);
            player.getPackets().sendIComponentText(1309, 37, "List Co-Op Partner");
        } else if (itemId == 11694 || itemId == 11696 || itemId == 11698 || itemId == 11700)
            GodswordCreating.dismantleGS(player, item, slotId);
        else if (itemId == 23044 || itemId == 23045 || itemId == 23046 || itemId == 23047)
            player.getDialogueManager().startDialogue("MindSpikeD", itemId, slotId);
        else if (item.getId() == 41941)
            player.getLootingBag().check();
        else if ((item.getDefinitions().containsOption("Teleport") || item.getDefinitions().containsOption("Rub"))
                && ItemTransportation.transportationDialogue(player, item, false, true))
            return;
        if (Settings.DEBUG)
            System.out.println("Option 3");
    }

    public static void handleItemOption4(Player player, int slotId, int itemId, Item item) {
        if (Settings.DEBUG)
            System.out.println("Option 4");
        if(handle(player, item, 4, slotId))
            return;
    }

    public static void handleItemOption5(Player player, int slotId, int itemId, Item item) {
        if (Settings.DEBUG)
            System.out.println("Option 5");
        if(handle(player, item, 5, slotId))
            return;
    }

    // option4
    public static void handleItemOption6(Player player, int slot, int itemId, Item item) {
        if (player.isLocked() || player.getEmotesManager().isDoingEmote())
            return;
        player.stopAll(false);
        if(handle(player, item, 2, slot))
            return;
        if (item.getDefinitions().containsInventoryOption(3, "eat") && Consumables.eat(player, item, slot))
            return;
        if (player.getToolbelt().addItem(slot, item))
            return;
        if (Magic.useAncientTabTeleport(player, item.getId()))
            return;
        if (AccessorySmithing.uncharge(player, item))
            return;
        if (player.getTreasureTrailsManager().getScrollLevel(item.getId()) != -1) {
            dig(player);
            return;
        }

        if (item.getDefinitions().containsInventoryOption(3, "revert")
                ? SCRewards.transform(player, item.getId(), false, true)
                : SCRewards.check(player, item.getId()))
            return;
        else if ((item.getDefinitions().containsOption("Rub") || item.getDefinitions().containsOption("Cabbage-port"))
                && ItemTransportation.transportationDialogue(player, item, false, true))
            return;
        else if (Drinkables.emptyPot(player, item, slot))
            return;
        else if (item.getDefinitions().isBindItem())
            player.getDungManager().bind(item, slot);
        else if ((item.getDefinitions().containsOption(3, "Grind") || item.getDefinitions().containsOption(3, "Powder"))
                && Herblore.isRawIngredient(player, itemId))
            return;
        else if (itemId == 995) {
            if (player.isCanPvp()) {
                player.getPackets()
                        .sendGameMessage("You cannot access your money pouch within a player-vs-player zone.");
                return;
            }
            player.getMoneyPouch().sendDynamicInteraction(item.getAmount(), false, MoneyPouch.TYPE_POUCH_INVENTORY);
        } else if (itemId == 43204) {
			if (player.isCanPvp()) {
				player.getPackets()
						.sendGameMessage("You cannot access your money pouch within a player-vs-player zone.");
				return;
			}
			player.getMoneyPouch().setPlatinumToken(item.getAmount(), false);
        } else if (itemId == RunePouch.ID)
            RunePouch.empty(player);
        else if (itemId == 1438)
            Runecrafting.locate(player, 3127, 3405);
        else if (itemId == CoalBag.COAL_BAG_ID || itemId == CoalBag.OPENED_COAL_BAG_ID)
            player.getCoalBag().empty();
        else if (itemId == GemBag.GEM_BAG_ID || itemId == GemBag.OPENED_GEM_BAG_ID)
            player.getGemBag().empty();
        else if (itemId == 42926)
            PlayerCombat.unloadBlowpipe(player);
        else if (itemId == 25502)
            PlayerCombat.unloadInfernalBlowpipe(player);
        else if (itemId == 1440)
            Runecrafting.locate(player, 3306, 3474);
        else if (itemId == 1442)
            Runecrafting.locate(player, 3313, 3255);
        else if (itemId == 1444)
            Runecrafting.locate(player, 3185, 3165);
        else if (itemId == 1446)
            Runecrafting.locate(player, 3053, 3445);
        else if (itemId == 1448)
            Runecrafting.locate(player, 2982, 3514);
        else if (itemId == 1458)
            Runecrafting.locate(player, 2858, 3381);
        else if (itemId == 1454)
            Runecrafting.locate(player, 2408, 4377);
        else if (itemId == 1452)
            Runecrafting.locate(player, 3060, 3591);
        else if (itemId == 1462)
            Runecrafting.locate(player, 2872, 3020);
        else if (itemId == 42922 || itemId == 42924 || itemId == 42927 || itemId == 42929 || itemId == 42932) { // dismanetle
			/*player.lock(1);
			player.getInventory().deleteItem(itemId, 1);
			player.getInventory().addItem(42934, 20000);
			player.getPackets().sendGameMessage("You dismantle this item.", true);*/
            player.getDialogueManager().startDialogue("DismantleZulrahItem", slot, item);
        } else if (itemId == 52322) {
            player.getDialogueManager().startDialogue("DismantleAvernicItem", slot, item);
        } else if (itemId == 54423) {
            if (!player.getInventory().hasFreeSlots()) {
                player.getPackets().sendGameMessage("Not enough space in your inventory.");
                return;
            }
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().addItem(54422, 1);
            player.getInventory().addItem(54511, 1);
        } else if (itemId == 54424) {
            if (!player.getInventory().hasFreeSlots()) {
                player.getPackets().sendGameMessage("Not enough space in your inventory.");
                return;
            }
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().addItem(54422, 1);
            player.getInventory().addItem(54514, 1);
        } else if (itemId == 54425) {
            if (!player.getInventory().hasFreeSlots()) {
                player.getPackets().sendGameMessage("Not enough space in your inventory.");
                return;
            }
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().addItem(54422, 1);
            player.getInventory().addItem(54517, 1);
        } else if (itemId == 54268) {
            player.getPackets().sendGameMessage("It's the jaw of a Basilisk Knight. Use on an helm of Neitiznot to upgrade it!");
        } else if (itemId == 54271) {
            if (!player.getInventory().hasFreeSlots()) {
                player.getPackets().sendGameMessage("Not enough space in your inventory.");
                return;
            }
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().addItem(10828, 1);
            player.getInventory().addItem(54268, 1);
        } else if (itemId == 14057)
            SorceressGarden.teleportToSocreressGarden(player, true);
        else if (itemId == 11283)
            DragonfireShield.empty(player);
        else if (itemId == 52002)
            DragonfireWard.empty(player);
        else if (itemId == 51633)
            SkeletalWyvernShield.empty(player);
        else if (itemId == 15492 || itemId == 13263)
            Slayer.dissasembleSlayerHelmet(player, itemId == 15492);
        else if (itemId == 42006)
            player.getCharges().checkPercentage("There is " + ChargesManager.REPLACE + "% of charge remaining.", itemId,
                    false);
        else if (itemId == 41941)
            player.getDialogueManager().startDialogue("LootingBagSettings");
        else if (Slayer.isBlackMask(itemId)) {
            player.getInventory().replaceItem(8921, 1, slot);
            player.getPackets().sendGameMessage("You remove all the charges from the black mask.");
        } else if (item.getDefinitions().containsOption("Summon")) {
            Pouch pouch = Pouch.forId(itemId);
            if (pouch != null)
                Summoning.spawnFamiliar(player, pouch);
        } else {
            player.getPackets().sendGameMessage("Nothing interesting happens.");
            if (Settings.DEBUG)
                System.out.println("Option 6");
        }
    }

    public static void handleItemOption7(Player player, int slotId, int itemId, Item item) {
        if (player.isLocked() || player.getEmotesManager().isDoingEmote())
            return;
        if(handle(player, item, 7, slotId))
            return;
        if (!player.getControlerManager().canDropItem(item))
            return;
        if (player.tournamentResetRequired()) {
            // has tournament items
            // destroy item
            player.getInventory().deleteItem(slotId, item);
            player.sendMessage("The item disappears as it reaches the ground.");
            return;
        }
        player.stopAll(false);
        if (!player.getBank().hasVerified(10))
            return;
        if (item.getDefinitions().isDestroyItem()) {
            player.getDialogueManager().startDialogue("DestroyItemOption", slotId, item);
            return;
        }
        if (player.getPetManager().spawnPet(itemId, true))
            return;
        if (item.getId() == 707 || item.getId() == 703 || item.getId() == 4045) {
            player.setNextForceTalk(new ForceTalk("Ow! The " + item.getName().toLowerCase() + " exploded!"));
            int damage = item.getId() == 703 ? 350 : 650;
            player.applyHit(new Hit(player,
                    player.getHitpoints() - damage < 35
                            ? player.getHitpoints() - 35 < 0 ? 0 : player.getHitpoints() - 35
                            : damage,
                    HitLook.REGULAR_DAMAGE));
            player.setNextAnimation(new Animation(827));
            player.setNextGraphics(new Graphics(954));
            player.getInventory().deleteItem(slotId, item);
            return;
        }
        if (player.isDungeoneer() && !player.getDungManager().isInside()) {
            player.getPackets().sendGameMessage("Dungeoneers can not drop items outside dungeons.");
            return;
        }
        if (player.getCharges().degradesUponDrop(item)) {
            player.getDialogueManager().startDialogue("DegradeItemOption", slotId, item);
            return;
        }

        long val = item.getAmount() * GrandExchange.getPrice(itemId);
        if(val > 1_000_000)
            player.getDialogueManager().startDialogue("HighValueWarning", slotId, val);
        else
            dropItem(player, slotId, item);
    }
    public static void dropItem(Player player, int slotId, Item item) {
        player.getInventory().deleteItem(slotId, item);
        if (player.getCharges().degradeCompletly(item))
            return;
        if(ChambersOfXeric.isCoxItem(item.getId())) {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                raid.trackFloorItem(World.addCoxFloorItem(item, new WorldTile(player), player));
            }
        } else if (player.getControlerManager().getControler() instanceof Wilderness && ItemConstants.isTradeable(item)) {
            World.addGroundItem(item, new WorldTile(player), player, false, -1);
        } else {
            World.addGroundItem(item, new WorldTile(player), player, true, 60);
        }
        Logger.globalLog(player.getUsername(), player.getSession().getIP(),
                new String(" has dropped item [ id: " + item.getId() + ", amount: " + item.getAmount() + " ]."));
        player.getPackets().sendSound(2739, 0, 1);
        Bot.sendLog(Bot.PICKUP_DROP_CHANNEL, "[type=DROP][name=" + player.getUsername() + "][item=" + item.getName() + "(" + item.getId() + ")x" + Utils.getFormattedNumber(item.getAmount()) + "]");
    }

    public static void handleItemOption8(Player player, int slotId, int itemId, Item item) {
        if(!handle(player, item, 8, slotId))
            player.getInventory().sendExamine(slotId);
    }
}
