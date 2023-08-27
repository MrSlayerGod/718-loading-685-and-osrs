package com.rs.game.player.content;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class CrystalChest {
/*
	private final static Item REWARDS[][] =
	{
		{ new Item(1631), new Item(1969), new Item(995, 2000) },
		{ new Item(1631) },
		{
			new Item(1631),
			new Item(554, 50),
			new Item(556, 50),
			new Item(555, 50),
			new Item(557, 50),
			new Item(559, 50),
			new Item(558, 50),
			new Item(560, 10),
			new Item(561, 10),
			new Item(562, 10),
			new Item(563, 10),
			new Item(564, 10) },
		{ new Item(1631), new Item(2363, 10) },
		{ new Item(1631), new Item(454, 100) },
		{ new Item(1631), new Item(441, 150) },
		{ new Item(1631), new Item(1603, 2), new Item(1601, 2) },
		{ new Item(1631), new Item(371, 5), new Item(995, 1000) },
		{ new Item(1631), new Item(987), new Item(995, 750) },
		{ new Item(1631), new Item(985), new Item(995, 750) },
		{ new Item(1631), new Item(1183) },
		{ new Item(1631), new Item(1079), new Item(1093) } };*/
	
	private final static Item REWARDS[][] =
	{
			
		{ new Item(995, 1000000) }, //coins 1m
		{  new Item(995, 2000000) }, //coins 2m
		{ new Item(1631), new Item(1518, 250) }, // maple log 250
		{ new Item(1631), new Item(1516, 150) }, // yew log 150
		{ new Item(1631), new Item(1514, 100) }, //magic log 100
		{ new Item(1631), new Item(1624, 25), new Item(1622, 25) }, //uncut sapphire + emerald 25 each
		{ new Item(1631), new Item(1620, 15), new Item(1618, 20) }, // uncut diamond + ruby 15 each
		{ new Item(1631), new Item(1392, 10) }, // battlestaff 10
		{ new Item(1631), new Item(1754, 25) }, // green dhide 25
		{ new Item(1631), new Item(1752, 15) }, //blue dhide 20
		{ new Item(1631), new Item(537, 15) }, // dragon bone 15
		{ new Item(1631), new Item(8779, 250) }, // oak plank 250 
		{ new Item(1631), new Item(8783, 150)}, // mahogany plank 150
		{ new Item(1631), new Item(208, 20), new Item(208, 50) }, // ranarr 20 snape 50
		{ new Item(1631), new Item(3052, 20), new Item(224, 50) }, // snap drag 20 red spider 50
		{ new Item(1631), new Item(3050, 20), new Item(6694, 35) }, //toadflax 20 crushed nest 35
		{ new Item(1631), new Item(989, 1) }, // crystal key 1
		{ new Item(1631), new Item(5973, 100) }, // papaya fruit 100
		{ new Item(1631), new Item(226, 100) }, // limp 100
		{ new Item(1631), new Item(240, 100) }, //whiteberries 100
		{ new Item(1631), new Item(445, 250) }, //gold ore 250
		{ new Item(1631), new Item(2360, 100) }, // mithril bar 100
		{ new Item(1631), new Item(5315, 5) }, //yew seed 5
	
		{ new Item(1631), new Item(11230, 250) },
		{ new Item(1631), new Item(11229, 250) }};
	;
	
		


	public static void openChest(final Player player, final WorldObject object) {
		player.getInventory().deleteItem(989, 1);
	//	double random = Utils.random(0, 100);
		final int reward = Utils.random(REWARDS.length);//random <= 39.69 ? 0 : random <= 56.41 ? 1 : random <= 64.41 ? 2 : random <= 67.65 ? 3 : random <= 74.2 ? 4 : random <= 76.95 ? 5 : random <= 81.18 ? 6 : random <= 91.75 ? 7 : random <= 95.01 ? 8 : random <= 98.68 ? 9 : random <= 99.74 ? 10 : 11;
		player.setNextAnimation(new Animation(536));
		player.lock(2);
		player.getPackets().sendGameMessage("You unlock the chest with your key.");
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (World.containsObjectWithId(object, object.getId())) {
					WorldObject openedChest = new WorldObject(object.getId() + 1, object.getType(), object.getRotation(), object.getX(), object.getY(), object.getPlane());
					World.spawnObjectTemporary(openedChest, 600, false, true);
				}
				player.getPackets().sendGameMessage("You find some treasure in the chest!");
				for (Item item : REWARDS[reward])
					player.getInventory().addItemDrop(item.getId(), item.getAmount());
			}
		}, 0);
	}
}
