package com.rs.game.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.item.Item;
import com.rs.game.player.actions.GemCutting.Gem;
import com.rs.game.player.dialogues.Dialogue;

/**
 * @author Simplex
 * @since Apr 29, 2020
 */
public class GemBag implements Serializable {

    private static transient final long serialVersionUID = -4688561533891132445L;

    public static transient final List<Integer> ALLOWED_GEMS =// List.of(
    		Arrays.asList( Gem.SAPPHIRE.getUncut(),
            Gem.EMERALD.getUncut(),
            Gem.RUBY.getUncut(),
            Gem.DIAMOND.getUncut(),
            Gem.DRAGONSTONE.getUncut());

    // must statically add allowed gems when object is first initialized
    public GemBag() {
        for (int gem : ALLOWED_GEMS) {
            gemStorage.put(gem, 0);
        }
    }

    public static final int OPENED_GEM_BAG_ID = 54481;
    public static final int GEM_BAG_ID = 18338;

    private static final int MAX_GEM_STORAGE = 60;

    private Map<Integer, Integer> gemStorage = new LinkedHashMap<Integer, Integer>();

    private transient Player player;

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void openBag() {
        int slot = player.getInventory().getItems().getThisItemSlot(GEM_BAG_ID);
        if (slot > -1) {
            player.getInventory().replaceItem(OPENED_GEM_BAG_ID, 1, slot);
            player.sendMessage("You open the Gem bag. Gems will automatically be added while mining and pickpocketing.");
        }
    }

    public void closeBag() {
        int slot = player.getInventory().getItems().getThisItemSlot(OPENED_GEM_BAG_ID);
        if (slot > -1) {
            player.getInventory().replaceItem(GEM_BAG_ID, 1, slot);
            player.sendMessage("You close the Gem bag.");
        }
    }

    public void fill() {
        java.util.List<Item> list = Arrays.stream(player.getInventory().getItems().getItems()).filter(Objects::nonNull)
                .filter(item -> gemStorage.containsKey(item.getId())).collect(Collectors.toList());

        if(list.size() == 0)
            player.sendMessage("You do not have any gems in your inventory.");
        else
            list.stream().forEach(item -> {
                    int gems = gemStorage.get(item.getId());
                    if(gems == MAX_GEM_STORAGE) {
                        player.sendMessage("Your gem bag cannot hold any more " + item.getName() + "s.");
                    } else {
                        int amt = item.getAmount();

                        if(gems + amt > MAX_GEM_STORAGE)
                            amt = MAX_GEM_STORAGE - gems;

                        player.getInventory().deleteItem(item.getId(), amt);
                        gemStorage.put(item.getId(), gems + amt);
                    }
                }
        );
    }

    public void check() {
        StringBuilder sb = new StringBuilder();
        gemStorage.keySet().stream().forEach(gem -> {
            int amt = gemStorage.get(gem);
            sb.append(amt).append(' ').append(ItemConfig.forID(gem).name).append("s / ");
        });

        if(sb.length() == 0)
            sb.append("The bag has no gems in it.") ;

        player.getDialogueManager().startDialogue("SimpleMessage",
                sb.substring(0, sb.length()-3));

    }

    public void empty() {
        ArrayList<Item> gemList = new ArrayList<Item>();

        gemStorage.keySet().stream().forEach(gem -> {
            int amt = gemStorage.get(gem);
            if(amt > 0) {
                // put available gems in a map corresponding to the option click order
                gemList.add(new Item(gem, amt));
            }
        });

        // build options list corresponding to gemList
        String[] options = new String[gemList.size()];;
        for(int i = 0; i < gemList.size(); i++) {
            Item gem = gemList.get(i);
            options[i] = gem.getName() + " (" + gem.getAmount() + ")";
        }

        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                sendOptionsDialogue("Take out which gems?",
                        options);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch(componentId) {
                    case Dialogue.OPTION_1:
                        if(gemList.size() > 0) player.getGemBag().withdraw(gemList.get(0));
                        break;
                    case Dialogue.OPTION_2:
                        if(gemList.size() > 1) player.getGemBag().withdraw(gemList.get(1));
                        break;
                    case Dialogue.OPTION_3:
                        if(gemList.size() > 2) player.getGemBag().withdraw(gemList.get(2));
                        break;
                    case Dialogue.OPTION_4:
                        if(gemList.size() > 3) player.getGemBag().withdraw(gemList.get(3));
                        break;
                    case Dialogue.OPTION_5:
                        if(gemList.size() > 4) player.getGemBag().withdraw(gemList.get(4));
                        break;
                }

                end();
            }

            @Override
            public void finish() {
            }

        });
    }

    private void withdraw(Item removeGem) {
        int space = player.getInventory().getFreeSlots();
        if(space == 0) {
            player.sendMessage("Your inventory is full.");
            return;
        }

        int gems = removeGem.getAmount();
        int remainder = 0;

        if(gems == 0) return;

        if(gems > space) {
            remainder = gems - space;
            gems = space;
        }

        player.getInventory().addItem(removeGem.getId(), gems);
        player.sendMessage("You remove " + gems + " " + removeGem.getName() + (gems==1?"":"s") + " from the Gem bag.");
        gemStorage.put(removeGem.getId(), remainder);
    }
}
