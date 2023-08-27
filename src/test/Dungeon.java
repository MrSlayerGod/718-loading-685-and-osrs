package test;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Dungeon {
	
	public Base base;
	private Room[][] rooms;

	public Dungeon(int sizeX, int sizeY) {
		int x = (int) (Math.random() * sizeX);
		int y = (int) (Math.random() * sizeY);
		rooms = new Room[sizeX][sizeY];
		rooms[x][y] = base = new Base(x, y);
	}

	public Base getBase() {
		return base;
	}

	public Room getRoom(int x, int y) {
		if (x < 0 || y < 0 || x >= rooms.length || y >= rooms[x].length) {
			return null;
		}
		return rooms[x][y];
	}

	public void draw(Graphics g) {
		g.fillRect(base.x * 20 + 27, base.y * 20 + 47, 6, 6);
		for (int i = 0; i < rooms.length; i++) {
			for (int j = 0; j < rooms[i].length; j++) {
				if (rooms[i][j] == null)
					continue;
				if (rooms[i][j].lock != 0) {
					g.setColor(Color.red);
					g.drawString("" + rooms[i][j].lock, i * 20 + 24, j * 20 + 55);
				}
				if (rooms[i][j].key != 0) {

					g.setColor(Color.green);
					g.drawString("" + rooms[i][j].key, i * 20 + 31, j * 20 + 55);
				}
				if (!(rooms[i][j] instanceof Base)) {
					// Draw a line toward the parent
					if (rooms[i][j].isCritPath) {
						g.setColor(Color.red);
					} else {
						g.setColor(Color.black);

					}

					g.drawLine(i * 20 + 30, j * 20 + 50, rooms[i][j].parent.x * 20 + 30, rooms[i][j].parent.y * 20 + 50);
				}
			}
		}
	}

	public void addRoom(int x, int y, Room room) {
		rooms[x][y] = room;
	}

	public void removeRoom(Room r) {
		r.parent.children.remove(r);
		rooms[r.x][r.y]= null; 
	}
	
	/**
	 * Locking algorithm description: The algorithm will start at the base and
	 * crawl through the dungeon, randomly adding locks to doors until it cannot
	 * move anymore At this point one random lock will be selected and the key
	 * will be dropped in a random place in this crawled space, dead ends are
	 * more likely to receive keys The algorithm then opens this lock and
	 * continues crawling here locking random doors until it is again completely
	 * blocked Again a lock is selected and the key is dropped somewhere This
	 * continues until there are no more locks to drop keys for
	 */
	public void setPath() {
		List<Integer> availableLocks = new ArrayList<Integer>();
		for (int i = 1; i <= 64; i++) {
			availableLocks.add(i);
		}
		//Shuffle the keys so it doesn't use the same ones every dungeon
		//Collections.shuffle(availableLocks);

		//This is the queue of locked rooms that still need a key to be added to the dungeon
		Queue<Room> unresolved = new LinkedList<Room>();
		//This is a list of rooms that are not a dead end, have been crawled and have not received a key yet
		List<Room> resolvedNoKey = new LinkedList<Room>();
		//Same as resolvedNoKey but contains only dead ends
		List<Room> deadEndsNoKey = new LinkedList<Room>();

		//Start adding locks from the base
		addLocks(false, base, availableLocks, unresolved, resolvedNoKey, deadEndsNoKey);

		while (!unresolved.isEmpty()) {
			Room r = unresolved.poll();
			System.out.println("resolving lock " + r.lock + " [" + r.x + ", " + r.y + "]");
			System.out.println("open rooms without key: " + resolvedNoKey.size());
			System.out.println("locked rooms: " + unresolved.size());

			//Usually we will prefer to put keys in dead ends to make the dungeon less linear and have more back and forth walking
			if (!deadEndsNoKey.isEmpty() && !(Math.random() > 0.75 && !resolvedNoKey.isEmpty())) {
				Collections.shuffle(deadEndsNoKey);
				Room keyRoom = deadEndsNoKey.remove(0);
				System.out.println("Adding key to dead end: " + r.lock + " [" + keyRoom.x + ", " + keyRoom.y + "]");
				keyRoom.key = r.lock;//unresolvedLocks.remove(0);
			} else {
				Collections.shuffle(resolvedNoKey);
				Room keyRoom = resolvedNoKey.remove(0);
				System.out.println("ADDING key: " + r.lock + " [" + keyRoom.x + ", " + keyRoom.y + "]");
				keyRoom.key = r.lock;//unresolvedLocks.remove(0);
			}
			addLocks(false, r, availableLocks, unresolved, resolvedNoKey, deadEndsNoKey);
			Collections.shuffle((LinkedList<Room>) unresolved);
		}
		solve();

		//A final step would be trimming some branches without keys/lock to make the dungeon not always use the full room

		//Removes only the most outer dead branches, the parents of these could also be dead but thats fine. Call multiple times to trim more
		trim(base);

	}

	private boolean trim(Room r) {
		if (r.children.isEmpty()) {
			if (r.lock == 0 && r.key == 0) {
				return true;
			}
		} else {
			Iterator<Room> it = r.children.iterator();
			Room c;
			while(it.hasNext()) {
				c = it.next();
				if(trim(c)) {
					it.remove();
					removeRoom(c);
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param isRecursive
	 *            Only recursive calls are allowed to add a lock, non recursive
	 *            calls are the base and already locked doors.
	 * @param r
	 *            The room to explore further from
	 * @param availableLocks
	 *            The locks that haven't been used yet
	 * @param unresolved
	 *            A list of rooms that a lock has been added to and the key
	 *            hasn't been assigned to a room yet
	 * @param resolvedNoKey
	 *            A list of rooms that have been resolved ('opened') and a key
	 *            could be placed
	 * @param deadEndsNoKey
	 */
	private void addLocks(boolean isRecursive, Room r, List<Integer> availableLocks, Queue<Room> unresolved, List<Room> resolvedNoKey, List<Room> deadEndsNoKey) {
		//25% chance to add a lock to the room (non recursive calls already have a lock or is the base room)
		if (DungeonGenerator.LOCK_RATE > Math.random() && isRecursive && !availableLocks.isEmpty()) {
			r.lock = availableLocks.remove(0);
			System.out.println("Adding lock " + r.lock + " " + r.x + " " + r.y);
			unresolved.add(r);
			return;
		}
		//We could use only one list here but the dungeon becomes a lot more interesting if dead ends are more likely to have a key
		if (r.children.isEmpty()) {
			deadEndsNoKey.add(r);
		} else {
			resolvedNoKey.add(r);
		}
		for (Room c : r.children) {
			//Recursive call for all the child rooms
			addLocks(true, c, availableLocks, unresolved, resolvedNoKey, deadEndsNoKey);
		}
	}

	//Testing code, although may be useful for further dungeon generation purposes
	Room end = null;

	public void solve() {
		Set<Integer> keys = new HashSet<Integer>();
		Queue<Integer> nkeys = new LinkedList<Integer>();

		//while (solve_(base, keys));

		while (solve2_search(base, keys, nkeys)) {
			end = findLock(nkeys.peek());
			keys.addAll(nkeys);
			nkeys.clear();
		}

		Queue<Integer> cpLocks = new LinkedList<Integer>();
		Set<Integer> unlocked = new HashSet<Integer>();

		//TODO: apparently the crit path is only like 20 rooms, so we shouldn't look for the longest one
		//Not necessarily the longest path but pretty close as it took the algorithm the longest to reach this final lock, you'd have to calculate the critical path from each lock for the optimal boss room.
		Room cp = end;
		while (!cp.children.isEmpty()) {
			cp = cp.children.get(0);
		}
		//Boss room always is a key door (atleast i think so?) and a dead end
		if (cp != end) {
			cp.lock = end.lock;
			end.lock = 0;
		}

		while (cp != null) {
			cp.isCritPath = true;
			if (cp.lock != 0) {
				cpLocks.add(cp.lock);
				unlocked.add(cp.lock);
				System.out.println("main crit path lock: " + cp.lock);
			}
			cp = cp.parent;
		}

		Integer cpl;
		while ((cpl = cpLocks.poll()) != null) {
			Room r = findKey(cpl);
			while (r != null) {
				r.isCritPath = true;
				if (r.lock != 0 && !unlocked.contains(r.lock)) {
					cpLocks.add(r.lock);
					unlocked.add(r.lock);
					System.out.println("crit path lock: " + r.lock);

				}
				r = r.parent;
			}
		}
		int cpc = 0;
		int cpk = 0;
		int k = 0;
		for (int i = 0; i < rooms.length; i++) {
			for (int j = 0; j < rooms[i].length; j++) {
				if (rooms[i][j].isCritPath) {
					cpc++;
					if (rooms[i][j].lock != 0) {
						cpk++;
					}
				}
				if (rooms[i][j].lock != 0) {
					k++;
				}

			}
		}
		System.out.println("Crit path: " + cpc + "/" + (rooms.length * rooms[0].length) + " rooms.");
		System.out.println("Crit path: " + cpk + "/" + k + " keys.");

	}

	Set<Integer> solveLocks = new HashSet<Integer>();

	//Solving method 1, looks for keys and opens at the same time, which makes it slightly more biased to ordering of childs
	@SuppressWarnings("unused")
	@Deprecated
	private boolean solve_(Room r, Set<Integer> keys) {
		boolean progress = false;
		if (keys.add(r.key)) {
			System.out.println("Found key: " + r.key);
			progress = true;
		}
		for (Room c : r.children) {
			//Check if we can pass
			if (keys.contains(c.lock) || c.lock == 0) {
				if (solveLocks.add(c.lock)) {
					System.out.println("Opened new door: " + c.lock);
					progress = true;
					end = c;
				}
				progress |= solve_(c, keys);
			}
		}

		return progress;
	}

	//Solving method 2, slightly smarter about determining the best end door since it doesn't open any doors until all keys have been picked up, then opens all at once and repeats
	//A final, much simpler option would be to just select the last generated lock but this could occasionally make the dungeon extremely short.
	private boolean solve2_search(Room r, Set<Integer> keys, Queue<Integer> newKeys) {
		boolean progress = false;
		if (r.key != 0 && !keys.contains(r.key)) {
			newKeys.add(r.key);
			System.out.println("Found key: " + r.key);
			progress = true;
		}
		for (Room c : r.children) {
			//Check if we can pass
			if (keys.contains(c.lock) || c.lock == 0) {
				progress |= solve2_search(c, keys, newKeys);
			}
		}

		return progress;
	}

	private Room findKey(int k) {
		for (int i = 0; i < rooms.length; i++) {
			for (int j = 0; j < rooms[i].length; j++) {
				if (rooms[i][j].key == k)
					return rooms[i][j];
			}
		}
		return null;
	}

	private Room findLock(int l) {
		for (int i = 0; i < rooms.length; i++) {
			for (int j = 0; j < rooms[i].length; j++) {
				if (rooms[i][j].lock == l)
					return rooms[i][j];
			}
		}
		return null;
	}

}
