package test;

import java.util.ArrayList;
import java.util.List;


public class Room {

	int x;
	int y;
	int key;
	int lock; //This should be changed to also allow skills/guardian
	boolean isCritPath;
	
	/**
	 * The parent room is the room that is on the path to the base
	 */
	public Room parent;
	
	/**
	 * All the connected rooms leading away from the base
	 */
	public List<Room> children;
	
	public Room(Room parent, int x, int y) {
		this.children = new ArrayList<Room>();
		this.parent = parent;
		if (parent != null) //Base doesn't have a parent
			parent.children.add(this);
		this.x = x;
		this.y = y;
	}
	
}
