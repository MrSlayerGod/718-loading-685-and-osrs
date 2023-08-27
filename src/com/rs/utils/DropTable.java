package com.rs.utils;

import com.rs.game.item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OSRS Style drop tables
 *
 * @author Simplex
 * @since Sep 08, 2020
 */
public class DropTable {
    private final DropCategory[] categories;

    public DropCategory rollCategory() {
        return Utils.random(categories);
    }

    public ItemDrop roll() {
        return rollCategory().rollDrop();
    }


    public List<ItemDrop> getAllDropsOnTable() {
        ArrayList<ItemDrop> itemDrops = new ArrayList<>();
        for(DropCategory cat : categories) {
            for(ItemDrop drop : cat.getDrops()) {
                if(drop != null) {
                    itemDrops.add(drop);
                }
            }
        }
        return itemDrops.stream().distinct().collect(Collectors.toList());
    }

    public Integer[] getCollectionLog() {
        List<ItemDrop> itemDrops = getAllDropsOnTable();
        Integer[] intD = new Integer[itemDrops.size()];
        for(int i = 0; i < intD.length; i++)
            intD[i] = itemDrops.get(i).getId();
        return intD;
    }

    public DropCategory getCat(String name) {
        Optional<DropCategory> category = Arrays.stream(categories).filter(dropCategory -> dropCategory.name.equalsIgnoreCase(name)).findAny();
        return category.isPresent() ? category.get() : null;
    }

    public DropTable(ItemDrop... drops) {
       this(new DropCategory("", 1, false, drops));
    }

    public DropTable(DropCategory... cats) {
        int totalWeight = 0;

        for(int i = 0; i < cats.length; i++)
            totalWeight += cats[i].weight;

        categories = new DropCategory[totalWeight];

        int categoryFill = 0;
        for(DropCategory dropCategory : cats) {
            for(int i = 0; i < dropCategory.weight; i++) {
                categories[categoryFill++] = dropCategory;
            }
        }
    }

    public static class DropCategory {
        private final String name;
        private final int weight;
        private final ItemDrop[] dropList;

        private boolean announceDrop;

        public ItemDrop[] getDrops() {
            return dropList;
        }

        public Integer[] dropListToIntArr() {
            Integer[] list = new Integer[dropList.length];
            for(int i = 0; i < dropList.length; i++)
                list[i] = dropList[i].getId();
            return list;
        }

        public static DropCategory create(String common, int weight, boolean announceDrop, int[] ints) {
            DropCategory cat = create(common, weight, ints);
            cat.setAnnounceDrop(announceDrop);
            return cat;
        }

        public static DropCategory create(String common, int weight, int[] ints) {
            Item[] items = new Item[ints.length];
            for(int i = 0; i < ints.length; i++)
                items[i] = new Item(ints[i]);
            return create(common, weight, items);
        }

        public ItemDrop rollDrop() {
            return Utils.random(dropList).clone();
        }

        public static DropCategory create(String name, int categoryWeight, boolean announceDrop, Item... drops) {
            DropCategory cat = create(name, categoryWeight, drops);
            cat.setAnnounceDrop(announceDrop);
            return cat;
        }
        /**
         * Create a category using an item list
         */
        public static DropCategory create(String name, int categoryWeight, Item... drops) {
            ItemDrop[] itemDrops = new ItemDrop[drops.length];

            for(int i = 0; i < drops.length; i++) {
                itemDrops[i] = new ItemDrop(drops[i].getId(), drops[i].getAmount());
            }

            return new DropCategory(name, categoryWeight, itemDrops);
        }

        public DropCategory(String name, int categoryWeight, boolean announceDrop, ItemDrop... drops) {
            this(name, categoryWeight, drops);
            this.announceDrop = announceDrop;
        }

        public DropCategory(String name, int categoryWeight, ItemDrop... drops) {
            int totalWeight = 0;

            for(int i = 0; i < drops.length; i++) {
                totalWeight += drops[i].weight;
                drops[i].setParent(this);
            }

            this.name = name;
            weight = categoryWeight;
            dropList = new ItemDrop[totalWeight];

            int dropFill = 0;
            for(ItemDrop d : drops) {
                for(int i = 0; i < d.weight; i++) {
                    dropList[dropFill++] = d;
                }
            }
        }

        public String getName() {
            return name;
        }

        public boolean isAnnounceDrop() {
            return announceDrop;
        }

        public void setAnnounceDrop(boolean announceDrop) {
            this.announceDrop = announceDrop;
        }
    }

    public static class ItemDrop {
        private DropCategory parent = null;
        private boolean announceDrop;
        private final int weight;
        private final int min;
        private final int max;

        public ItemDrop clone() {
            ItemDrop itemDrop = new ItemDrop(id, min, max, weight);
            itemDrop.setParent(getParent());
            return itemDrop;
        }

        public ItemDrop(int i) {
            id = i;
            min = max = weight = 1;
        }

        public int getWeight() {
            return weight;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        public int getId() {
            return id;
        }

        private final int id;

        private Item item = null;

        /**
         * Roll amount or return previously rolled amount
         */
        public Item get() {
            if(item == null) {
                int amt = min;
                if (min != max && max != 1) {
                    amt = Utils.random(min, max);
                }
                item = new Item(id, amt);
            }
            return item;
        }

        public ItemDrop(int id, int min, int max, int rolls, boolean announceDrop) {
            this(id, min, max, rolls);
            this.announceDrop = announceDrop;
        }

        public ItemDrop(int id, int min, int max, int rolls) {
            this.weight = rolls;
            this.min = min;
            this.max = max;
            this.id = id;
        }
        public ItemDrop(int id, int max, int weight) {
            this.weight = weight;
            this.max = max;
            this.min = max == 1 ? 1 : (int) ((double)max * 0.70);
            this.id = id;
        }

        public ItemDrop(int id, int max) {
            this.weight = 1;
            this.min = max == 1 ? 1 : (int) ((double)max * 0.70);
            this.max = max;
            this.id = id;
        }

        public void setParent(DropCategory cat) {
            parent = cat;
        }

        public DropCategory getParent() {
            return parent;
        }

        public boolean isAnnounceDrop() {
            return announceDrop || getParent().isAnnounceDrop();
        }
    }
}

