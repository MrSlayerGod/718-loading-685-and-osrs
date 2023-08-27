package mgitests;

public class CollisionData {

	public static final int UNWALKABLE_FLOOR = 0x200000;
	public static final int DECORATION = 0x40000;
	public static final int OBJECT = 0x100;
	public static final int OBJECT_SOLID = 0x20000;
	public static final int OBJECT_UNKNOWN = 0x40000000;

	public static final int WALL_NORTH = 0x2;
	public static final int WALL_WEST = 0x8;
	public static final int WALL_SOUTH = 0x20;
	public static final int WALL_EAST = 0x80;

	private int[][] flags;
	private int offsetX;
	private int offsetY;
	private int sizeX;
	private int sizeY;

	private CollisionData() {
	}

	public static CollisionData create(int sizeX, int sizeY) {
		CollisionData data = new CollisionData();
		data.offsetX = -1;
		data.offsetY = -1;
		data.sizeX = sizeX + 1 + 5;
		data.sizeY = sizeY + 1 + 5;
		data.flags = new int[data.sizeX][data.sizeY];
		data.reset();
		return data;
	}

	public void reset() {
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				if (x != 0 && y != 0 && x < sizeX - 5 && y < sizeY - 5)
					flags[x][y] = UNWALKABLE_FLOOR;
				else
					flags[x][y] = -1;
			}
		}
	}

	public void flagUnwalkable(int x, int y) {
		x -= offsetX;
		y -= offsetY;
		flags[x][y] |= UNWALKABLE_FLOOR;
	}

	public void unflagUnwalkable(int x, int y) {
		y -= offsetY;
		x -= offsetX;
		flags[x][y] &= ~UNWALKABLE_FLOOR;
	}

	public void flagDecoration(int x, int y) {
		y -= offsetY;
		x -= offsetX;
		flags[x][y] |= DECORATION;
	}

	public void unflagDecoration(int x, int y) {
		y -= offsetY;
		x -= offsetX;
		flags[x][y] &= ~DECORATION;
	}

	public void flagObject(int objX, int objY, int objSizeX, int objSizeY, boolean solid, boolean unknown) {
		int flag = OBJECT;
		if (solid)
			flag |= OBJECT_SOLID;
		objX -= offsetX;
		objY -= offsetY;

		if (unknown)
			flag |= OBJECT_UNKNOWN;
		for (int x = objX; x < (objX + objSizeX); x++) {
			if (x >= 0 && sizeX > x) {
				for (int y = objY; y < (objY + objSizeY); y++) {
					if (y >= 0 && y < sizeY)
						flagClip(x, y, flag);
				}
			}
		}
	}

	public void unflagObject(int objX, int objY, int sizeX, int sizeY, int objFace, boolean solid, boolean unknown) {
		int flag = OBJECT;
		if (solid)
			flag |= OBJECT_SOLID;
		objX -= offsetX;
		objY -= offsetY;
		if (objFace == 1 || objFace == 3) {
			int prevSizeX = sizeX;
			sizeX = sizeY;
			sizeY = prevSizeX;
		}
		if (unknown)
			flag |= OBJECT_UNKNOWN;

		for (int x = objX; x < (objX + sizeX); x++) {
			if (x >= 0 && sizeX > x) {
				for (int y = objY; y < (objY + sizeY); y++) {
					if (y >= 0 && y < sizeY)
						unFlagClip(x, y, flag);
				}
			}
		}
	}

	public void flagWall(int x, int y, int type, int face, boolean solid, boolean unknown) {
		x -= offsetX;
		y -= offsetY;
		if (type == 0) {
			if (face == 0) {
				flagClip(x, y, 128); // east
				flagClip(x - 1, y, 8); // west
			}
			if (face == 1) {
				flagClip(x, y, 2); // north
				flagClip(x, y + 1, 32); // south
			}
			if (face == 2) {
				flagClip(x, y, 8);
				flagClip(x + 1, y, 128);
			}
			if (face == 3) {
				flagClip(x, y, 32);
				flagClip(x, y - 1, 2);
			}
		}
		if (type == 1 || type == 3) {
			if (face == 0) {
				flagClip(x, y, 1);
				flagClip(x - 1, y + 1, 16);
			}
			if (face == 1) {
				flagClip(x, y, 4);
				flagClip(x + 1, y + 1, 64);
			}
			if (face == 2) {
				flagClip(x, y, 16);
				flagClip(x + 1, y - 1, 1);
			}
			if (face == 3) {
				flagClip(x, y, 64);
				flagClip(x - 1, y - 1, 4);
			}
		}
		if (type == 2) {
			if (face == 0) {
				flagClip(x, y, 130);
				flagClip(x - 1, y, 8);
				flagClip(x, y + 1, 32);
			}
			if (face == 1) {
				flagClip(x, y, 10);
				flagClip(x, y + 1, 32);
				flagClip(x + 1, y, 128);
			}
			if (face == 2) {
				flagClip(x, y, 40);
				flagClip(x + 1, y, 128);
				flagClip(x, y - 1, 2);
			}
			if (face == 3) {
				flagClip(x, y, 160);
				flagClip(x, y - 1, 2);
				flagClip(x - 1, y, 8);
			}
		}
		if (solid) {
			if (type == 0) {
				if (face == 0) {
					flagClip(x, y, 65536);
					flagClip(x - 1, y, 4096);
				}
				if (face == 1) {
					flagClip(x, y, 1024);
					flagClip(x, y + 1, 16384);
				}
				if (face == 2) {
					flagClip(x, y, 4096);
					flagClip(x + 1, y, 65536);
				}
				if (face == 3) {
					flagClip(x, y, 16384);
					flagClip(x, y - 1, 1024);
				}
			}
			if (type == 1 || type == 3) {
				if (face == 0) {
					flagClip(x, y, 512);
					flagClip(x - 1, y + 1, 8192);
				}
				if (face == 1) {
					flagClip(x, y, 2048);
					flagClip(x + 1, y + 1, 32768);
				}
				if (face == 2) {
					flagClip(x, y, 8192);
					flagClip(x + 1, y - 1, 512);
				}
				if (face == 3) {
					flagClip(x, y, 32768);
					flagClip(x - 1, y - 1, 2048);
				}
			}
			if (type == 2) {
				if (face == 0) {
					flagClip(x, y, 66560);
					flagClip(x - 1, y, 4096);
					flagClip(x, y + 1, 16384);
				}
				if (face == 1) {
					flagClip(x, y, 5120);
					flagClip(x, y + 1, 16384);
					flagClip(x + 1, y, 65536);
				}
				if (face == 2) {
					flagClip(x, y, 20480);
					flagClip(x + 1, y, 65536);
					flagClip(x, y - 1, 1024);
				}
				if (face == 3) {
					flagClip(x, y, 81920);
					flagClip(x, y - 1, 1024);
					flagClip(x - 1, y, 4096);
				}
			}
		}
		if (unknown) {
			if (type == 0) {
				if (face == 0) {
					flagClip(x, y, 536870912);
					flagClip(x - 1, y, 33554432);
				}
				if (face == 1) {
					flagClip(x, y, 8388608);
					flagClip(x, y + 1, 134217728);
				}
				if (face == 2) {
					flagClip(x, y, 33554432);
					flagClip(x + 1, y, 536870912);
				}
				if (face == 3) {
					flagClip(x, y, 134217728);
					flagClip(x, y - 1, 8388608);
				}
			}
			if (type == 1 || type == 3) {
				if (face == 0) {
					flagClip(x, y, 4194304);
					flagClip(x - 1, y + 1, 67108864);
				}
				if (face == 1) {
					flagClip(x, y, 16777216);
					flagClip(x + 1, y + 1, 268435456);
				}
				if (face == 2) {
					flagClip(x, y, 67108864);
					flagClip(x + 1, y - 1, 4194304);
				}
				if (face == 3) {
					flagClip(x, y, 268435456);
					flagClip(x - 1, y - 1, 16777216);
				}
			}
			if (type == 2) {
				if (face == 0) {
					flagClip(x, y, 545259520);
					flagClip(x - 1, y, 33554432);
					flagClip(x, y + 1, 134217728);
				}
				if (face == 1) {
					flagClip(x, y, 41943040);
					flagClip(x, y + 1, 134217728);
					flagClip(x + 1, y, 536870912);
				}
				if (face == 2) {
					flagClip(x, y, 167772160);
					flagClip(x + 1, y, 536870912);
					flagClip(x, y - 1, 8388608);
				}
				if (face == 3) {
					flagClip(x, y, 671088640);
					flagClip(x, y - 1, 8388608);
					flagClip(x - 1, y, 33554432);
				}
			}
		}
	}

	final void unflagWall(int x, int y, int type, int face, boolean solid, boolean unknown) {
		x -= offsetX;
		y -= offsetY;
		if (type == 0) {
			if (face == 0) {
				unFlagClip(x, y, 128);
				unFlagClip(x - 1, y, 8);
			}
			if (face == 1) {
				unFlagClip(x, y, 2);
				unFlagClip(x, y + 1, 32);
			}
			if (face == 2) {
				unFlagClip(x, y, 8);
				unFlagClip(x + 1, y, 128);
			}
			if (face == 3) {
				unFlagClip(x, y, 32);
				unFlagClip(x, y - 1, 2);
			}
		}
		if (type == 1 || type == 3) {
			if (face == 0) {
				unFlagClip(x, y, 1);
				unFlagClip(x - 1, y + 1, 16);
			}
			if (face == 1) {
				unFlagClip(x, y, 4);
				unFlagClip(x + 1, y + 1, 64);
			}
			if (face == 2) {
				unFlagClip(x, y, 16);
				unFlagClip(x + 1, y - 1, 1);
			}
			if (face == 3) {
				unFlagClip(x, y, 64);
				unFlagClip(x - 1, y - 1, 4);
			}
		}
		if (type == 2) {
			if (face == 0) {
				unFlagClip(x, y, 130);
				unFlagClip(x - 1, y, 8);
				unFlagClip(x, y + 1, 32);
			}
			if (face == 1) {
				unFlagClip(x, y, 10);
				unFlagClip(x, y + 1, 32);
				unFlagClip(x + 1, y, 128);
			}
			if (face == 2) {
				unFlagClip(x, y, 40);
				unFlagClip(x + 1, y, 128);
				unFlagClip(x, y - 1, 2);
			}
			if (face == 3) {
				unFlagClip(x, y, 160);
				unFlagClip(x, y - 1, 2);
				unFlagClip(x - 1, y, 8);
			}
		}
		if (solid) {
			if (type == 0) {
				if (face == 0) {
					unFlagClip(x, y, 65536);
					unFlagClip(x - 1, y, 4096);
				}
				if (face == 1) {
					unFlagClip(x, y, 1024);
					unFlagClip(x, y + 1, 16384);
				}
				if (face == 2) {
					unFlagClip(x, y, 4096);
					unFlagClip(x + 1, y, 65536);
				}
				if (face == 3) {
					unFlagClip(x, y, 16384);
					unFlagClip(x, y - 1, 1024);
				}
			}
			if (type == 1 || type == 3) {
				if (face == 0) {
					unFlagClip(x, y, 512);
					unFlagClip(x - 1, y + 1, 8192);
				}
				if (face == 1) {
					unFlagClip(x, y, 2048);
					unFlagClip(x + 1, y + 1, 32768);
				}
				if (face == 2) {
					unFlagClip(x, y, 8192);
					unFlagClip(x + 1, y - 1, 512);
				}
				if (face == 3) {
					unFlagClip(x, y, 32768);
					unFlagClip(x - 1, y - 1, 2048);
				}
			}
			if (type == 2) {
				if (face == 0) {
					unFlagClip(x, y, 66560);
					unFlagClip(x - 1, y, 4096);
					unFlagClip(x, y + 1, 16384);
				}
				if (face == 1) {
					unFlagClip(x, y, 5120);
					unFlagClip(x, y + 1, 16384);
					unFlagClip(x + 1, y, 65536);
				}
				if (face == 2) {
					unFlagClip(x, y, 20480);
					unFlagClip(x + 1, y, 65536);
					unFlagClip(x, y - 1, 1024);
				}
				if (face == 3) {
					unFlagClip(x, y, 81920);
					unFlagClip(x, y - 1, 1024);
					unFlagClip(x - 1, y, 4096);
				}
			}
		}
		if (unknown) {
			if (type == 0) {
				if (face == 0) {
					unFlagClip(x, y, 536870912);
					unFlagClip(x - 1, y, 33554432);
				}
				if (face == 1) {
					unFlagClip(x, y, 8388608);
					unFlagClip(x, y + 1, 134217728);
				}
				if (face == 2) {
					unFlagClip(x, y, 33554432);
					unFlagClip(x + 1, y, 536870912);
				}
				if (face == 3) {
					unFlagClip(x, y, 134217728);
					unFlagClip(x, y - 1, 8388608);
				}
			}
			if (type == 1 || type == 3) {
				if (face == 0) {
					unFlagClip(x, y, 4194304);
					unFlagClip(x - 1, y + 1, 67108864);
				}
				if (face == 1) {
					unFlagClip(x, y, 16777216);
					unFlagClip(x + 1, y + 1, 268435456);
				}
				if (face == 2) {
					unFlagClip(x, y, 67108864);
					unFlagClip(x + 1, y - 1, 4194304);
				}
				if (face == 3) {
					unFlagClip(x, y, 268435456);
					unFlagClip(x - 1, y - 1, 16777216);
				}
			}
			if (type == 2) {
				if (face == 0) {
					unFlagClip(x, y, 545259520);
					unFlagClip(x - 1, y, 33554432);
					unFlagClip(x, y + 1, 134217728);
				}
				if (face == 1) {
					unFlagClip(x, y, 41943040);
					unFlagClip(x, y + 1, 134217728);
					unFlagClip(x + 1, y, 536870912);
				}
				if (face == 2) {
					unFlagClip(x, y, 167772160);
					unFlagClip(x + 1, y, 536870912);
					unFlagClip(x, y - 1, 8388608);
				}
				if (face == 3) {
					unFlagClip(x, y, 671088640);
					unFlagClip(x, y - 1, 8388608);
					unFlagClip(x - 1, y, 33554432);
				}
			}
		}
	}

	private void flagClip(int x, int y, int flag) {
		flags[x][y] = flags[x][y] | flag;
	}

	private void unFlagClip(int x, int y, int flag) {
		flags[x][y] = flags[x][y] & ~flag;
	}

	public int get(int x, int y) {
		x -= offsetX;
		y -= offsetY;
		return flags[x][y];
	}

	public void set(int x, int y, int v) {
		x -= offsetX;
		y -= offsetY;
		flags[x][y] = v;
	}

	public int[][] getFlags() {
		return flags;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public int getSizeX() {
		return sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public boolean withinDistance(int clipType, int toMapX, int toMapY, int currentMapX, int currentMapY, int targetFace, int selfSize) {
		if (selfSize != 1) {
			if (toMapX >= currentMapX && toMapX <= currentMapX + (selfSize - 1) && toMapY <= toMapY && toMapY <= selfSize - 1 + toMapY)
				return true;
		} else if (currentMapX == toMapX && currentMapY == toMapY)
			return true;
		currentMapY -= offsetY;
		currentMapX -= offsetX;
		toMapY -= offsetY;
		toMapX -= offsetX;
		if (selfSize != 1) {
			int someX = selfSize - 1 + currentMapX;
			int someY = selfSize - 1 + currentMapY;
			if (clipType == 0) {
				if (targetFace != 0) {
					if (targetFace == 1) {
						if (currentMapX <= toMapX && toMapX <= someX && toMapY + 1 == currentMapY)
							return true;
						if (-selfSize + toMapX == currentMapX && toMapY >= currentMapY && toMapY <= someY && (flags[someX][toMapY] & 0x2c0108) == 0)
							return true;
						if (currentMapX == toMapX + 1 && currentMapY <= toMapY && someY >= toMapY && (flags[currentMapX][toMapY] & 0x2c0180) == 0)
							return true;
					} else if (targetFace == 2) {
						if (toMapX + 1 == currentMapX && toMapY >= currentMapY && someY >= toMapY)
							return true;
						if (toMapX >= currentMapX && toMapX <= someX && toMapY + 1 == currentMapY && ((flags[toMapX][currentMapY] & 0x2c0120) == 0))
							return true;
						if (toMapX >= currentMapX && someX >= toMapX && -selfSize + toMapY == currentMapY && ((flags[toMapX][someY] & 0x2c0102) == 0))
							return true;
					} else if (targetFace == 3) {
						if (currentMapX <= toMapX && someX >= toMapX && toMapY - selfSize == currentMapY)
							return true;
						if (currentMapX == -selfSize + toMapX && currentMapY <= toMapY && toMapY <= someY && (flags[someX][toMapY] & 0x2c0108) == 0)
							return true;
						if (toMapX + 1 == currentMapX && toMapY >= currentMapY && toMapY <= someY && (flags[currentMapX][toMapY] & 0x2c0180) == 0)
							return true;
					}
				} else {
					if (toMapX - selfSize == currentMapX && currentMapY <= toMapY && someY >= toMapY)
						return true;
					if (currentMapX <= toMapX && toMapX <= someX && currentMapY == toMapY + 1 && (flags[toMapX][currentMapY] & 0x2c0120) == 0)
						return true;
					if (toMapX >= currentMapX && toMapX <= someX && currentMapY == toMapY - selfSize && (flags[toMapX][someY] & 0x2c0102) == 0)
						return true;
				}
			}
			if (clipType == 2) {
				if (targetFace != 0) {
					if (targetFace != 1) {
						if (targetFace != 2) {
							if (targetFace == 3) {
								if (currentMapX == toMapX - selfSize && toMapY >= currentMapY && toMapY <= someY)
									return true;
								if (currentMapX <= toMapX && someX >= toMapX && currentMapY == toMapY + 1 && (flags[toMapX][currentMapY] & 0x2c0120) == 0)
									return true;
								if (toMapX + 1 == currentMapX && currentMapY <= toMapY && toMapY <= someY && (flags[currentMapX][toMapY] & 0x2c0180) == 0)
									return true;
								if (toMapX >= currentMapX && someX >= toMapX && currentMapY == -selfSize + toMapY)
									return true;
							}
						} else {
							if (toMapX - selfSize == currentMapX && toMapY >= currentMapY && toMapY <= someY && ((flags[someX][toMapY] & 0x2c0108) == 0))
								return true;
							if (toMapX >= currentMapX && toMapX <= someX && currentMapY == toMapY + 1 && (flags[toMapX][currentMapY] & 0x2c0120) == 0)
								return true;
							if (currentMapX == toMapX + 1 && toMapY >= currentMapY && toMapY <= someY)
								return true;
							if (currentMapX <= toMapX && toMapX <= someX && currentMapY == -selfSize + toMapY)
								return true;
						}
					} else {
						if (currentMapX == -selfSize + toMapX && currentMapY <= toMapY && someY >= toMapY && (flags[someX][toMapY] & 0x2c0108) == 0)
							return true;
						if (toMapX >= currentMapX && toMapX <= someX && toMapY + 1 == currentMapY)
							return true;
						if (toMapX + 1 == currentMapX && currentMapY <= toMapY && someY >= toMapY)
							return true;
						if (toMapX >= currentMapX && someX >= toMapX && currentMapY == -selfSize + toMapY && ((flags[toMapX][someY] & 0x2c0102) == 0))
							return true;
					}
				} else {
					if (currentMapX == -selfSize + toMapX && toMapY >= currentMapY && toMapY <= someY)
						return true;
					if (currentMapX <= toMapX && toMapX <= someX && toMapY + 1 == currentMapY)
						return true;
					if (currentMapX == toMapX + 1 && toMapY >= currentMapY && toMapY <= someY && (flags[currentMapX][toMapY] & 0x2c0180) == 0)
						return true;
					if (currentMapX <= toMapX && toMapX <= someX && currentMapY == -selfSize + toMapY && (flags[toMapX][someY] & 0x2c0102) == 0)
						return true;
				}
			}
			if (clipType == 9) {
				if (toMapX >= currentMapX && toMapX <= someX && toMapY + 1 == currentMapY && (flags[toMapX][currentMapY] & 0x2c0120) == 0)
					return true;
				if (currentMapX <= toMapX && toMapX <= someX && currentMapY == -selfSize + toMapY && (flags[toMapX][someY] & 0x2c0102) == 0)
					return true;
				if (toMapX - selfSize == currentMapX && currentMapY <= toMapY && toMapY <= someY && (flags[someX][toMapY] & 0x2c0108) == 0)
					return true;
				if (toMapX + 1 == currentMapX && currentMapY <= toMapY && someY >= toMapY && (flags[currentMapX][toMapY] & 0x2c0180) == 0)
					return true;
			}
		} else {
			if (clipType == 0) {
				if (targetFace == 0) {
					if (toMapX - 1 == currentMapX && toMapY == currentMapY)
						return true;
					if (currentMapX == toMapX && currentMapY == toMapY + 1 && (flags[currentMapX][currentMapY] & 0x2c0120) == 0)
						return true;
					if (currentMapX == toMapX && currentMapY == toMapY - 1 && (flags[currentMapX][currentMapY] & 0x2c0102) == 0)
						return true;
				} else if (targetFace == 1) {
					if (currentMapX == toMapX && toMapY + 1 == currentMapY)
						return true;
					if (toMapX - 1 == currentMapX && toMapY == currentMapY && (flags[currentMapX][currentMapY] & 0x2c0108) == 0)
						return true;
					if (toMapX + 1 == currentMapX && currentMapY == toMapY && (flags[currentMapX][currentMapY] & 0x2c0180) == 0)
						return true;
				} else if (targetFace != 2) {
					if (targetFace == 3) {
						if (toMapX == currentMapX && toMapY - 1 == currentMapY)
							return true;
						if (currentMapX == toMapX - 1 && toMapY == currentMapY && ((flags[currentMapX][currentMapY] & 0x2c0108) == 0))
							return true;
						if (currentMapX == toMapX + 1 && toMapY == currentMapY && ((flags[currentMapX][currentMapY] & 0x2c0180) == 0))
							return true;
					}
				} else {
					if (currentMapX == toMapX + 1 && toMapY == currentMapY)
						return true;
					if (currentMapX == toMapX && toMapY + 1 == currentMapY && (flags[currentMapX][currentMapY] & 0x2c0120) == 0)
						return true;
					if (currentMapX == toMapX && toMapY - 1 == currentMapY && (flags[currentMapX][currentMapY] & 0x2c0102) == 0)
						return true;
				}
			}
			if (clipType == 2) {
				if (targetFace == 0) {
					if (currentMapX == toMapX - 1 && currentMapY == toMapY)
						return true;
					if (currentMapX == toMapX && toMapY + 1 == currentMapY)
						return true;
					if (toMapX + 1 == currentMapX && currentMapY == toMapY && (flags[currentMapX][currentMapY] & 0x2c0180) == 0)
						return true;
					if (toMapX == currentMapX && currentMapY == toMapY - 1 && (flags[currentMapX][currentMapY] & 0x2c0102) == 0)
						return true;
				} else if (targetFace != 1) {
					if (targetFace != 2) {
						if (targetFace == 3) {
							if (toMapX - 1 == currentMapX && currentMapY == toMapY)
								return true;
							if (toMapX == currentMapX && toMapY + 1 == currentMapY && (flags[currentMapX][currentMapY] & 0x2c0120) == 0)
								return true;
							if (toMapX + 1 == currentMapX && toMapY == currentMapY && (flags[currentMapX][currentMapY] & 0x2c0180) == 0)
								return true;
							if (toMapX == currentMapX && currentMapY == toMapY - 1)
								return true;
						}
					} else {
						if (currentMapX == toMapX - 1 && currentMapY == toMapY && ((flags[currentMapX][currentMapY] & 0x2c0108) == 0))
							return true;
						if (toMapX == currentMapX && currentMapY == toMapY + 1 && ((flags[currentMapX][currentMapY] & 0x2c0120) == 0))
							return true;
						if (currentMapX == toMapX + 1 && currentMapY == toMapY)
							return true;
						if (toMapX == currentMapX && toMapY - 1 == currentMapY)
							return true;
					}
				} else {
					if (toMapX - 1 == currentMapX && currentMapY == toMapY && (flags[currentMapX][currentMapY] & 0x2c0108) == 0)
						return true;
					if (toMapX == currentMapX && toMapY + 1 == currentMapY)
						return true;
					if (currentMapX == toMapX + 1 && currentMapY == toMapY)
						return true;
					if (toMapX == currentMapX && toMapY - 1 == currentMapY && (flags[currentMapX][currentMapY] & 0x2c0102) == 0)
						return true;
				}
			}
			if (clipType == 9) {
				if (currentMapX == toMapX && toMapY + 1 == currentMapY && (flags[currentMapX][currentMapY] & 0x20) == 0)
					return true;
				if (toMapX == currentMapX && toMapY - 1 == currentMapY && (flags[currentMapX][currentMapY] & 0x2) == 0)
					return true;
				if (toMapX - 1 == currentMapX && toMapY == currentMapY && (flags[currentMapX][currentMapY] & 0x8) == 0)
					return true;
				if (toMapX + 1 == currentMapX && toMapY == currentMapY && (flags[currentMapX][currentMapY] & 0x80) == 0)
					return true;
			}
		}
		return false;
	}

	public boolean checkMove(int fromMapX, int fromMapY, int toMapX, int toMapY, int fromSizeX, int fromSizeY, int targetSizeX, int targetSizeY, int i_49_) {
		int fromFullX = fromSizeX + fromMapX;
		int fromFullY = fromSizeY + fromMapY;
		int toFullX = toMapX + targetSizeX;
		int toFullY = toMapY + targetSizeY;
		if (fromMapX == toFullX && (i_49_ & 0x2) == 0) {
			int i_59_ = fromMapY > toMapY ? fromMapY : toMapY;
			for (int i_60_ = toFullY > fromFullY ? fromFullY : toFullY; i_59_ < i_60_; i_59_++) {
				if ((flags[toFullX - offsetX - 1][i_59_ - offsetY] & 0x8) == 0)
					return true;
			}
		} else if (toMapX != fromFullX || (i_49_ & 0x8) != 0) {
			if (toFullY == fromMapY && (i_49_ & 0x1) == 0) {
				int i_61_ = toMapX >= fromMapX ? toMapX : fromMapX;
				for (int i_62_ = fromFullX >= toFullX ? toFullX : fromFullX; i_62_ > i_61_; i_61_++) {
					if (((flags[i_61_ - offsetX][toFullY - 1 - offsetY]) & 0x2) == 0)
						return true;
				}
			} else if (toMapY == fromFullY && (i_49_ & 0x4) == 0) {
				int i_63_ = fromMapX > toMapX ? fromMapX : toMapX;
				for (int i_64_ = fromFullX >= toFullX ? toFullX : fromFullX; i_64_ > i_63_; i_63_++) {
					if (((flags[-offsetX + i_63_][-offsetY + toMapY]) & 0x20) == 0)
						return true;
				}
			}
		} else {
			int i_65_ = fromMapY <= toMapY ? toMapY : fromMapY;
			for (int i_66_ = fromFullY >= toFullY ? toFullY : fromFullY; i_65_ < i_66_; i_65_++) {
				if ((flags[toMapX - offsetX][-offsetY + i_65_] & 0x80) == 0)
					return true;
			}
		}
		return false;
	}

	public boolean checkMove_(int fromMapX, int fromMapY, int toMapX, int toMapY, int selfSize, int targetSizeX, int targetSizeY, int i_70_) {
		if (selfSize > 1) {
			if (checkMove(fromMapX, fromMapY, toMapX, toMapY, selfSize, selfSize, targetSizeX, targetSizeY))
				return true;
			return checkMove(fromMapX, fromMapY, toMapX, toMapY, selfSize, selfSize, targetSizeX, targetSizeY, i_70_);
		}
		int i_76_ = toMapX + targetSizeX - 1;
		int i_77_ = targetSizeY + toMapY - 1;
		if (fromMapX >= toMapX && i_76_ >= fromMapX && toMapY <= fromMapY && i_77_ >= fromMapY)
			return true;
		if (fromMapX == toMapX - 1 && toMapY <= fromMapY && fromMapY <= i_77_ && (flags[-offsetX + fromMapX][-offsetY + fromMapY] & 0x8) == 0 && (i_70_ & 0x8) == 0)
			return true;
		if (i_76_ + 1 == fromMapX && toMapY <= fromMapY && i_77_ >= fromMapY && (flags[-offsetX + fromMapX][-offsetY + fromMapY] & 0x80) == 0 && (i_70_ & 0x2) == 0)
			return true;
		if (toMapY - 1 == fromMapY && fromMapX >= toMapX && i_76_ >= fromMapX && ((flags[fromMapX - offsetX][-offsetY + fromMapY] & 0x2) == 0) && (i_70_ & 0x4) == 0)
			return true;
		if (i_77_ + 1 == fromMapY && toMapX <= fromMapX && i_76_ >= fromMapX && ((flags[fromMapX - offsetX][fromMapY - offsetY] & 0x20) == 0) && (i_70_ & 0x1) == 0)
			return true;
		return false;
	}

	public boolean checkMove(int clipType, int fromMapX, int fromMapY, int toMapX, int toMapY, int selfSize, int targetFace) {
		if (selfSize != 1) {
			if (fromMapX <= toMapX && toMapX <= selfSize + fromMapX - 1 && toMapY <= toMapY && toMapY <= selfSize + toMapY - 1)
				return true;
		} else if (fromMapX == toMapX && fromMapY == toMapY)
			return true;
		fromMapX -= offsetX;
		toMapY -= offsetY;
		toMapX -= offsetX;
		fromMapY -= offsetY;
		if (selfSize == 1) {
			if (clipType == 6 || clipType == 7) {
				if (clipType == 7)
					targetFace = targetFace + 2 & 0x3;
				if (targetFace == 0) {
					if (toMapX + 1 == fromMapX && fromMapY == toMapY && (flags[fromMapX][fromMapY] & 0x80) == 0)
						return true;
					if (fromMapX == toMapX && toMapY - 1 == fromMapY && (flags[fromMapX][fromMapY] & 0x2) == 0)
						return true;
				} else if (targetFace != 1) {
					if (targetFace == 2) {
						if (fromMapX == toMapX - 1 && fromMapY == toMapY && (flags[fromMapX][fromMapY] & 0x8) == 0)
							return true;
						if (toMapX == fromMapX && fromMapY == toMapY + 1 && (flags[fromMapX][fromMapY] & 0x20) == 0)
							return true;
					} else if (targetFace == 3) {
						if (fromMapX == toMapX + 1 && fromMapY == toMapY && (flags[fromMapX][fromMapY] & 0x80) == 0)
							return true;
						if (toMapX == fromMapX && fromMapY == toMapY + 1 && (flags[fromMapX][fromMapY] & 0x20) == 0)
							return true;
					}
				} else {
					if (fromMapX == toMapX - 1 && toMapY == fromMapY && (flags[fromMapX][fromMapY] & 0x8) == 0)
						return true;
					if (toMapX == fromMapX && toMapY - 1 == fromMapY && (flags[fromMapX][fromMapY] & 0x2) == 0)
						return true;
				}
			}
			if (clipType == 8) {
				if (toMapX == fromMapX && toMapY + 1 == fromMapY && (flags[fromMapX][fromMapY] & 0x20) == 0)
					return true;
				if (toMapX == fromMapX && fromMapY == toMapY - 1 && (flags[fromMapX][fromMapY] & 0x2) == 0)
					return true;
				if (toMapX - 1 == fromMapX && toMapY == fromMapY && (flags[fromMapX][fromMapY] & 0x8) == 0)
					return true;
				if (toMapX + 1 == fromMapX && toMapY == fromMapY && (flags[fromMapX][fromMapY] & 0x80) == 0)
					return true;
			}
		} else {
			int i_97_ = selfSize + fromMapX - 1;
			int i_98_ = fromMapY - 1 + selfSize;
			if (clipType == 6 || clipType == 7) {
				if (clipType == 7)
					targetFace = targetFace + 2 & 0x3;
				if (targetFace == 0) {
					if (toMapX + 1 == fromMapX && toMapY >= fromMapY && i_98_ >= toMapY && (flags[fromMapX][toMapY] & 0x80) == 0)
						return true;
					if (fromMapX <= toMapX && toMapX <= i_97_ && fromMapY == toMapY - selfSize && (flags[toMapX][i_98_] & 0x2) == 0)
						return true;
				} else if (targetFace == 1) {
					if (toMapX - selfSize == fromMapX && fromMapY <= toMapY && i_98_ >= toMapY && (flags[i_97_][toMapY] & 0x8) == 0)
						return true;
					if (toMapX >= fromMapX && i_97_ >= toMapX && fromMapY == -selfSize + toMapY && (flags[toMapX][i_98_] & 0x2) == 0)
						return true;
				} else if (targetFace == 2) {
					if (toMapX - selfSize == fromMapX && fromMapY <= toMapY && i_98_ >= toMapY && (flags[i_97_][toMapY] & 0x8) == 0)
						return true;
					if (toMapX >= fromMapX && toMapX <= i_97_ && fromMapY == toMapY + 1 && (flags[toMapX][fromMapY] & 0x20) == 0)
						return true;
				} else if (targetFace == 3) {
					if (fromMapX == toMapX + 1 && fromMapY <= toMapY && toMapY <= i_98_ && (flags[fromMapX][toMapY] & 0x80) == 0)
						return true;
					if (fromMapX <= toMapX && toMapX <= i_97_ && toMapY + 1 == fromMapY && (flags[toMapX][fromMapY] & 0x20) == 0)
						return true;
				}
			}
			if (clipType == 8) {
				if (fromMapX <= toMapX && toMapX <= i_97_ && fromMapY == toMapY + 1 && (flags[toMapX][fromMapY] & 0x20) == 0)
					return true;
				if (fromMapX <= toMapX && toMapX <= i_97_ && -selfSize + toMapY == fromMapY && (flags[toMapX][i_98_] & 0x2) == 0)
					return true;
				if (-selfSize + toMapX == fromMapX && toMapY >= fromMapY && i_98_ >= toMapY && (flags[i_97_][toMapY] & 0x8) == 0)
					return true;
				if (fromMapX == toMapX + 1 && fromMapY <= toMapY && toMapY <= i_98_ && (flags[fromMapX][toMapY] & 0x80) == 0)
					return true;
			}
		}
		return false;
	}

	public boolean checkMove(int fromMapX, int fromMapY, int toMapX, int toMapY, int fromSizeX, int fromSizeY, int targetSizeX, int targetSizeY) {
		if (fromMapX >= (toMapX + targetSizeX) || (fromMapX + fromSizeX) <= toMapX)
			return false;
		if (fromMapY >= toMapY + targetSizeY || toMapY >= fromMapY + fromSizeY)
			return false;
		return true;
	}
}
