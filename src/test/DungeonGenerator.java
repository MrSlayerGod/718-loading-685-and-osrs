package test;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

public class DungeonGenerator {

	private final static int sizeX = 8;
	private final static int sizeY = 8;
	public static final double LOCK_RATE = 0.3;

	public static void main(String[] args) throws InterruptedException {
		List<Point> queue = new ArrayList<Point>();

		final Dungeon dungeon = new Dungeon(sizeX, sizeY);
		queue.add(new Point(dungeon.getBase().x - 1, dungeon.getBase().y));
		queue.add(new Point(dungeon.getBase().x + 1, dungeon.getBase().y));
		queue.add(new Point(dungeon.getBase().x, dungeon.getBase().y - 1));
		queue.add(new Point(dungeon.getBase().x, dungeon.getBase().y + 1));
		JFrame frame = new JFrame() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				dungeon.draw(g);
			}
		};
		frame.pack();
		frame.setSize(250, 250);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		while (!queue.isEmpty()) {
			Point next = random(queue);
			//Ensure the edge is within the dungeon boundary and it doesn't already exist
			if (next.x < 0 || next.y < 0 || next.x >= sizeX || next.y >= sizeY || dungeon.getRoom(next.x, next.y) != null) {
				continue;
			}

			//Connect this edge to a random neighboring room
			Room parent = randomParent(dungeon, next.x, next.y);

			//Create the edge room
			Room room = new Room(parent, next.x, next.y);

			dungeon.addRoom(next.x, next.y, room);

			//Add the edges to the queue, no need to check if it already is occupied
			queue.add(new Point(next.x - 1, next.y));
			queue.add(new Point(next.x + 1, next.y));
			queue.add(new Point(next.x, next.y - 1));
			queue.add(new Point(next.x, next.y + 1));

		}
		//Dungeon generation is done here, now set the path by adding keys and locks (TODO: puzzles, etc ..)
		System.out.println("Assigning doors");
		dungeon.setPath();
		//TODO: Add resources, puzzles, GD's....
		frame.repaint();
		System.out.println("Done");
	}

	static Room randomParent(Dungeon dungeon, int x, int y) {
		Set<Room> neighbors = new HashSet<Room>();
		neighbors.add(dungeon.getRoom(x - 1, y));
		neighbors.add(dungeon.getRoom(x + 1, y));
		neighbors.add(dungeon.getRoom(x, y - 1));
		neighbors.add(dungeon.getRoom(x, y + 1));
		neighbors.remove(null); //Remove rooms that don't exist yet
		return (Room) neighbors.toArray()[(int) (neighbors.size() * Math.random())]; //probably not the best way lol
	}

	static <T> T random(List<T> list) {
		return list.remove((int) (Math.random() * list.size()));
	}

}
