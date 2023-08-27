package mgitests;

public class RouteFinderMod {

	private static final int ALTERNATIVE_ROUTE_MAX_DISTANCE = 100;
	private static final int ALTERNATIVE_ROUTE_RANGE = 10;
	private static final int QUEUE_SIZE = 4096;
	private static final int GRAPH_SIZE = 128;
	private static int[][] directions = new int[GRAPH_SIZE][GRAPH_SIZE];
	private static int[][] distances = new int[GRAPH_SIZE][GRAPH_SIZE];
	private static int[] queueX = new int[RouteFinderMod.QUEUE_SIZE];
	private static int[] queueY = new int[RouteFinderMod.QUEUE_SIZE];

	public static int foundMapX;
	public static int foundMapY;

	/**
	 * Finds route,
	 * 
	 * @return returns number of steps found.
	 */
	public static int findRoute(CollisionData collision, int routeType, int creatureSize, int fromMapX, int fromMapY, int toMapX, int toMapY, int targetSizeX, int targetSizeY, int targetFace, int[] pathBufferX, int[] pathBufferY, boolean findAlternative, int blockFlag) {
		for (int x = 0; x < GRAPH_SIZE; x++) {
			for (int y = 0; y < GRAPH_SIZE; y++) {
				RouteFinderMod.directions[x][y] = 0;
				RouteFinderMod.distances[x][y] = 99999999;
			}
		}
		boolean foundRoute;

		switch (creatureSize) {
		case 1:
			foundRoute = RouteFinderMod.findRouteSize1(collision, routeType, fromMapX, fromMapY, toMapX, toMapY, targetSizeX, targetSizeY, targetFace, blockFlag);
			break;
		case 2:
			foundRoute = RouteFinderMod.findRouteSize2(collision, routeType, fromMapX, fromMapY, toMapX, toMapY, targetSizeX, targetSizeY, targetFace, blockFlag);
			break;
		default:
			foundRoute = RouteFinderMod.findRouteSizeX(collision, routeType, fromMapX, fromMapY, toMapX, toMapY, targetSizeX, targetSizeY, targetFace, creatureSize, blockFlag);
			break;
		}

		int arrayOffsetX = fromMapX - 64;
		int arrayOffsetY = fromMapY - 64;
		int foundMapX = RouteFinderMod.foundMapX;
		int foundMapY = RouteFinderMod.foundMapY;
		if (!foundRoute) {
			if (!findAlternative)
				return -1;
			int lowestCost = 2147483647;
			int lowestDistance = 2147483647;
			int checkRange = RouteFinderMod.ALTERNATIVE_ROUTE_RANGE;
			for (int checkMapX = toMapX - checkRange; toMapX + checkRange >= checkMapX; checkMapX++) {
				for (int checkMapY = toMapY - checkRange; toMapY + checkRange >= checkMapY; checkMapY++) {
					int arrayX = checkMapX - arrayOffsetX;
					int arrayY = checkMapY - arrayOffsetY;
					if (arrayX >= 0 && arrayY >= 0 && arrayX < GRAPH_SIZE && arrayY < GRAPH_SIZE && RouteFinderMod.distances[arrayX][arrayY] < RouteFinderMod.ALTERNATIVE_ROUTE_MAX_DISTANCE) {
						int deltaX = 0;
						if (toMapX <= checkMapX) {
							if (targetSizeX + toMapX - 1 < checkMapX)
								deltaX = 1 - toMapX - (targetSizeX - checkMapX);
						} else
							deltaX = toMapX - checkMapX;
						int deltaY = 0;
						if (toMapY <= checkMapY) {
							if (targetSizeY + toMapY - 1 < checkMapY)
								deltaY = 1 - toMapY - (targetSizeY - checkMapY);
						} else
							deltaY = toMapY - checkMapY;
						int cost = deltaX * deltaX + deltaY * deltaY;
						if (cost < lowestCost || (cost == lowestCost && RouteFinderMod.distances[arrayX][arrayY] < lowestDistance)) {
							lowestDistance = RouteFinderMod.distances[arrayX][arrayY];
							foundMapY = checkMapY;
							foundMapX = checkMapX;
							lowestCost = cost;
						}
					}
				}
			}
			if (lowestCost == 2147483647)
				return -1;
		}
		if (foundMapX == fromMapX && fromMapY == foundMapY)
			return 0;
		int queueWrite = 0;
		RouteFinderMod.queueX[queueWrite] = foundMapX;
		RouteFinderMod.queueY[queueWrite++] = foundMapY;
		int direction = RouteFinderMod.directions[foundMapX - arrayOffsetX][foundMapY - arrayOffsetY];
		while (fromMapX != foundMapX || foundMapY != fromMapY) {
			RouteFinderMod.queueX[queueWrite] = foundMapX;
			RouteFinderMod.queueY[queueWrite++] = foundMapY;
			if ((direction & 0x1) == 0) {
				if ((direction & 0x4) != 0)
					foundMapY--;
			} else
				foundMapY++;
			if ((direction & 0x2) != 0)
				foundMapX++;
			else if ((direction & 0x8) != 0)
				foundMapX--;
			direction = RouteFinderMod.directions[foundMapX - arrayOffsetX][foundMapY - arrayOffsetY];
		}
		RouteFinderMod.queueX[queueWrite] = foundMapX;
		RouteFinderMod.queueY[queueWrite++] = foundMapY;
		int numSteps = 0;
		while (queueWrite-- > 0) {
			pathBufferX[numSteps] = RouteFinderMod.queueX[queueWrite];
			pathBufferY[numSteps++] = RouteFinderMod.queueY[queueWrite];
			if (pathBufferX.length <= numSteps)
				break;
		}
		return numSteps;
	}

	private static boolean findRouteSize1(CollisionData collision, int routeType, int fromMapX, int fromMapY, int toMapX, int toMapY, int targetSizeX, int targetSizeY, int targetFace, int blockFlag) {
		int currentMapX = fromMapX;
		int currentMapY = fromMapY;
		int currentArrayOffsetX = 64;
		int currentArrayOffsetY = 64;
		int baseArrayOffsetX = fromMapX - currentArrayOffsetX;
		int baseArrayOffsetY = fromMapY - currentArrayOffsetY;
		directions[currentArrayOffsetX][currentArrayOffsetY] = 99;
		distances[currentArrayOffsetX][currentArrayOffsetY] = 0;
		int queueWrite = 0;
		int queueRead = 0;
		RouteFinderMod.queueX[queueWrite] = currentMapX;
		RouteFinderMod.queueY[queueWrite++] = currentMapY;
		int[][] clip = collision.getFlags();
		while (queueRead != queueWrite) {
			currentMapX = RouteFinderMod.queueX[queueRead];
			currentMapY = RouteFinderMod.queueY[queueRead];
			int currentClipMapX = currentMapX - collision.getOffsetX();
			int currentClipMapY = currentMapY - collision.getOffsetY();
			currentArrayOffsetX = currentMapX - baseArrayOffsetX;
			currentArrayOffsetY = currentMapY - baseArrayOffsetY;
			queueRead = queueRead + 1 & 0xfff;

			if (routeType == -4) { // standart type, only checks if we are at
				// destination coordinates.
				if (toMapX == currentMapX && toMapY == currentMapY) {
					RouteFinderMod.foundMapY = currentMapY;
					RouteFinderMod.foundMapX = currentMapX;
					return true;
				}
			} else if (routeType == -3) {
				if (collision.checkMove(currentMapX, currentMapY, toMapX, toMapY, 1, 1, targetSizeX, targetSizeY)) {
					RouteFinderMod.foundMapX = currentMapX;
					RouteFinderMod.foundMapY = currentMapY;
					return true;
				}
			} else if (routeType == -2) { // used for interacting
				if (collision.checkMove(currentMapX, currentMapY, toMapX, toMapY, 1, 1, targetSizeX, targetSizeY, blockFlag)) {
					RouteFinderMod.foundMapX = currentMapX;
					RouteFinderMod.foundMapY = currentMapY;
					return true;
				}
			} else if (routeType == -1) { // used for npcs clipping
				if (collision.checkMove_(currentMapX, currentMapY, toMapX, toMapY, 1, targetSizeX, targetSizeY, blockFlag)) {
					RouteFinderMod.foundMapX = currentMapX;
					RouteFinderMod.foundMapY = currentMapY;
					return true;
				}
			} else if (routeType == 0 || routeType == 1 || routeType == 2 || routeType == 3 || routeType == 9) { // used
				// for
				// walking
				// to
				// objects
				// mostly
				if (collision.withinDistance(routeType, toMapX, toMapY, currentMapX, currentMapY, targetFace, 1)) {
					RouteFinderMod.foundMapY = currentMapY;
					RouteFinderMod.foundMapX = currentMapX;
					return true;
				}
			} else if (collision.checkMove(routeType, currentMapX, currentMapY, toMapX, toMapY, 1, targetFace)) { // used
				// for
				// objects
				// too
				RouteFinderMod.foundMapX = currentMapX;
				RouteFinderMod.foundMapY = currentMapY;
				return true;
			}

			int distance = distances[currentArrayOffsetX][currentArrayOffsetY] + 1;
			if (currentArrayOffsetX > 0 && directions[currentArrayOffsetX - 1][currentArrayOffsetY] == 0 && (clip[currentClipMapX - 1][currentClipMapY] & 0x42240000) == 0) {
				// goes to west
				RouteFinderMod.queueX[queueWrite] = currentMapX - 1;
				RouteFinderMod.queueY[queueWrite] = currentMapY;
				directions[currentArrayOffsetX - 1][currentArrayOffsetY] = 2;
				distances[currentArrayOffsetX - 1][currentArrayOffsetY] = distance;
				queueWrite = queueWrite + 1 & 0xfff;
			}
			if (currentArrayOffsetX < (RouteFinderMod.GRAPH_SIZE - 1) && directions[currentArrayOffsetX + 1][currentArrayOffsetY] == 0 && (clip[currentClipMapX + 1][currentClipMapY] & 0x60240000) == 0) {
				// goes to east
				RouteFinderMod.queueX[queueWrite] = currentMapX + 1;
				RouteFinderMod.queueY[queueWrite] = currentMapY;
				directions[currentArrayOffsetX + 1][currentArrayOffsetY] = 8;
				distances[currentArrayOffsetX + 1][currentArrayOffsetY] = distance;
				queueWrite = queueWrite + 1 & 0xfff;

			}
			if (currentArrayOffsetY > 0 && directions[currentArrayOffsetX][currentArrayOffsetY - 1] == 0 && (clip[currentClipMapX][currentClipMapY - 1] & 0x40a40000) == 0) {
				// goes to south
				RouteFinderMod.queueX[queueWrite] = currentMapX;
				RouteFinderMod.queueY[queueWrite] = currentMapY - 1;
				directions[currentArrayOffsetX][currentArrayOffsetY - 1] = 1;
				distances[currentArrayOffsetX][currentArrayOffsetY - 1] = distance;
				queueWrite = queueWrite + 1 & 0xfff;
			}
			if (currentArrayOffsetY < (RouteFinderMod.GRAPH_SIZE - 1) && directions[currentArrayOffsetX][currentArrayOffsetY + 1] == 0 && (clip[currentClipMapX][currentClipMapY + 1] & 0x48240000) == 0) {
				// goes to north
				RouteFinderMod.queueX[queueWrite] = currentMapX;
				RouteFinderMod.queueY[queueWrite] = currentMapY + 1;
				directions[currentArrayOffsetX][currentArrayOffsetY + 1] = 4;
				distances[currentArrayOffsetX][currentArrayOffsetY + 1] = distance;
				queueWrite = queueWrite + 1 & 0xfff;
			}
			if (currentArrayOffsetX > 0 && currentArrayOffsetY > 0 && directions[currentArrayOffsetX - 1][currentArrayOffsetY - 1] == 0 && (clip[currentClipMapX - 1][currentClipMapY - 1] & 0x43a40000) == 0 && (clip[currentClipMapX - 1][currentClipMapY] & 0x42240000) == 0 && (clip[currentClipMapX][currentClipMapY - 1] & 0x40a40000) == 0) {
				// goes to south west
				RouteFinderMod.queueX[queueWrite] = currentMapX - 1;
				RouteFinderMod.queueY[queueWrite] = currentMapY - 1;
				directions[currentArrayOffsetX - 1][currentArrayOffsetY - 1] = 3;
				distances[currentArrayOffsetX - 1][currentArrayOffsetY - 1] = distance;
				queueWrite = queueWrite + 1 & 0xfff;
			}
			if (currentArrayOffsetX < (RouteFinderMod.GRAPH_SIZE - 1) && currentArrayOffsetY > 0 && directions[currentArrayOffsetX + 1][currentArrayOffsetY - 1] == 0 && (clip[currentClipMapX + 1][currentClipMapY - 1] & 0x60e40000) == 0 && (clip[currentClipMapX + 1][currentClipMapY] & 0x60240000) == 0 && (clip[currentClipMapX][currentClipMapY - 1] & 0x40a40000) == 0) {
				// goes to south east
				RouteFinderMod.queueX[queueWrite] = currentMapX + 1;
				RouteFinderMod.queueY[queueWrite] = currentMapY - 1;
				directions[currentArrayOffsetX + 1][currentArrayOffsetY - 1] = 9;
				distances[currentArrayOffsetX + 1][currentArrayOffsetY - 1] = distance;
				queueWrite = queueWrite + 1 & 0xfff;
			}
			if (currentArrayOffsetX > 0 && currentArrayOffsetY < (RouteFinderMod.GRAPH_SIZE - 1) && directions[currentArrayOffsetX - 1][currentArrayOffsetY + 1] == 0 && (clip[currentClipMapX - 1][currentClipMapY + 1] & 0x4e240000) == 0 && (clip[currentClipMapX - 1][currentClipMapY] & 0x42240000) == 0 && (clip[currentClipMapX][currentClipMapY + 1] & 0x48240000) == 0) {
				// goes to north west
				RouteFinderMod.queueX[queueWrite] = currentMapX - 1;
				RouteFinderMod.queueY[queueWrite] = currentMapY + 1;
				directions[currentArrayOffsetX - 1][currentArrayOffsetY + 1] = 6;
				distances[currentArrayOffsetX - 1][currentArrayOffsetY + 1] = distance;
				queueWrite = queueWrite + 1 & 0xfff;
			}
			if (currentArrayOffsetX < (RouteFinderMod.GRAPH_SIZE - 1) && currentArrayOffsetY < (RouteFinderMod.GRAPH_SIZE - 1) && directions[currentArrayOffsetX + 1][currentArrayOffsetY + 1] == 0 && (clip[currentClipMapX + 1][currentClipMapY + 1] & 0x78240000) == 0 && (clip[currentClipMapX + 1][currentClipMapY] & 0x60240000) == 0 && (clip[currentClipMapX][currentClipMapY + 1] & 0x48240000) == 0) {
				// goes to north east
				RouteFinderMod.queueX[queueWrite] = currentMapX + 1;
				RouteFinderMod.queueY[queueWrite] = currentMapY + 1;
				directions[currentArrayOffsetX + 1][currentArrayOffsetY + 1] = 12;
				distances[currentArrayOffsetX + 1][currentArrayOffsetY + 1] = distance;
				queueWrite = queueWrite + 1 & 0xfff;
			}
		}
		RouteFinderMod.foundMapX = currentMapX;
		RouteFinderMod.foundMapY = currentMapY;
		return false;
	}

	private static boolean findRouteSize2(CollisionData collision, int clipType, int fromMapX, int fromMapY, int toMapX, int toMapY, int targetSizeX, int targetSizeY, int targetFace, int blockFlag) {
		int currentMapX = fromMapX;
		int currentMapY = fromMapY;
		int currentArrayOffsetX = 64;
		int currentArrayOffsetY = 64;
		int baseArrayOffsetX = fromMapX - currentArrayOffsetX;
		int baseArrayOffsetY = fromMapY - currentArrayOffsetY;
		directions[currentArrayOffsetX][currentArrayOffsetY] = 99;
		distances[currentArrayOffsetX][currentArrayOffsetY] = 0;
		int queueWrite = 0;
		int queueRead = 0;
		RouteFinderMod.queueX[queueWrite] = currentMapX;
		RouteFinderMod.queueY[queueWrite++] = currentMapY;
		int[][] clip = collision.getFlags();
		while (queueWrite != queueRead) {
			currentMapX = RouteFinderMod.queueX[queueRead];
			currentMapY = RouteFinderMod.queueY[queueRead];
			int currentClipMapX = currentMapX - collision.getOffsetX();
			int currentClipMapY = currentMapY - collision.getOffsetY();
			currentArrayOffsetX = currentMapX - baseArrayOffsetX;
			currentArrayOffsetY = currentMapY - baseArrayOffsetY;
			queueRead = queueRead + 1 & 0xfff;

			if (clipType == -4) { // standart clip type , ignores target and
				// creature size check
				if (toMapX == currentMapX && toMapY == currentMapY) {
					RouteFinderMod.foundMapY = currentMapY;
					RouteFinderMod.foundMapX = currentMapX;
					return true;
				}
			} else if (clipType == -3) {
				if (collision.checkMove(currentMapX, currentMapY, toMapX, toMapY, 2, 2, targetSizeX, targetSizeY)) {
					RouteFinderMod.foundMapX = currentMapX;
					RouteFinderMod.foundMapY = currentMapY;
					return true;
				}
			} else if (clipType == -2) { // used for interacting
				if (collision.checkMove(currentMapX, currentMapY, toMapX, toMapY, 2, 2, targetSizeX, targetSizeY, blockFlag)) {
					RouteFinderMod.foundMapX = currentMapX;
					RouteFinderMod.foundMapY = currentMapY;
					return true;
				}
			} else if (clipType == -1) { // used for npcs clipping
				if (collision.checkMove_(currentMapX, currentMapY, toMapX, toMapY, 2, targetSizeX, targetSizeY, blockFlag)) {
					RouteFinderMod.foundMapX = currentMapX;
					RouteFinderMod.foundMapY = currentMapY;
					return true;
				}
			} else if (clipType == 0 || clipType == 1 || clipType == 2 || clipType == 3 || clipType == 9) { // used
				// for
				// walking
				// to
				// objects
				// mostly
				if (collision.withinDistance(clipType, toMapX, toMapY, currentMapX, currentMapY, targetFace, 2)) {
					RouteFinderMod.foundMapY = currentMapY;
					RouteFinderMod.foundMapX = currentMapX;
					return true;
				}
			} else if (collision.checkMove(clipType, currentMapX, currentMapY, toMapX, toMapY, 2, targetFace)) {
				RouteFinderMod.foundMapX = currentMapX;
				RouteFinderMod.foundMapY = currentMapY;
				return true;
			}

			int distance = distances[currentArrayOffsetX][currentArrayOffsetY] + 1;
			if (currentArrayOffsetX > 0 && directions[currentArrayOffsetX - 1][currentArrayOffsetY] == 0 && (clip[currentClipMapX - 1][currentClipMapY] & 0x43a40000) == 0 && (clip[currentClipMapX - 1][currentClipMapY + 1] & 0x4e240000) == 0) {
				RouteFinderMod.queueX[queueWrite] = currentMapX - 1;
				RouteFinderMod.queueY[queueWrite] = currentMapY;
				queueWrite = queueWrite + 1 & 0xfff;
				directions[currentArrayOffsetX - 1][currentArrayOffsetY] = 2;
				distances[currentArrayOffsetX - 1][currentArrayOffsetY] = distance;
			}
			if (currentArrayOffsetX < (RouteFinderMod.GRAPH_SIZE - 2) && directions[currentArrayOffsetX + 1][currentArrayOffsetY] == 0 && (clip[currentClipMapX + 2][currentClipMapY] & 0x60e40000) == 0 && (clip[currentClipMapX + 2][currentClipMapY + 1] & 0x78240000) == 0) {
				RouteFinderMod.queueX[queueWrite] = currentMapX + 1;
				RouteFinderMod.queueY[queueWrite] = currentMapY;
				directions[currentArrayOffsetX + 1][currentArrayOffsetY] = 8;
				queueWrite = queueWrite + 1 & 0xfff;
				distances[currentArrayOffsetX + 1][currentArrayOffsetY] = distance;
			}
			if (currentArrayOffsetY > 0 && directions[currentArrayOffsetX][currentArrayOffsetY - 1] == 0 && (clip[currentClipMapX][currentClipMapY - 1] & 0x43a40000) == 0 && (clip[currentClipMapX + 1][currentClipMapY - 1] & 0x60e40000) == 0) {
				RouteFinderMod.queueX[queueWrite] = currentMapX;
				RouteFinderMod.queueY[queueWrite] = currentMapY - 1;
				directions[currentArrayOffsetX][currentArrayOffsetY - 1] = 1;
				queueWrite = queueWrite + 1 & 0xfff;
				distances[currentArrayOffsetX][currentArrayOffsetY - 1] = distance;
			}
			if (currentArrayOffsetY < (RouteFinderMod.GRAPH_SIZE - 2) && directions[currentArrayOffsetX][currentArrayOffsetY + 1] == 0 && (clip[currentClipMapX][currentClipMapY + 2] & 0x4e240000) == 0 && (clip[currentClipMapX + 1][currentClipMapY + 2] & 0x78240000) == 0) {
				RouteFinderMod.queueX[queueWrite] = currentMapX;
				RouteFinderMod.queueY[queueWrite] = currentMapY + 1;
				directions[currentArrayOffsetX][currentArrayOffsetY + 1] = 4;
				queueWrite = queueWrite + 1 & 0xfff;
				distances[currentArrayOffsetX][currentArrayOffsetY + 1] = distance;
			}
			if (currentArrayOffsetX > 0 && currentArrayOffsetY > 0 && directions[currentArrayOffsetX - 1][currentArrayOffsetY - 1] == 0 && (clip[currentClipMapX - 1][currentClipMapY] & 0x4fa40000) == 0 && (clip[currentClipMapX - 1][currentClipMapY - 1] & 0x43a40000) == 0 && (clip[currentClipMapX][currentClipMapY - 1] & 0x63e40000) == 0) {
				RouteFinderMod.queueX[queueWrite] = currentMapX - 1;
				RouteFinderMod.queueY[queueWrite] = currentMapY - 1;
				queueWrite = queueWrite + 1 & 0xfff;
				directions[currentArrayOffsetX - 1][currentArrayOffsetY - 1] = 3;
				distances[currentArrayOffsetX - 1][currentArrayOffsetY - 1] = distance;
			}
			if (currentArrayOffsetX < (RouteFinderMod.GRAPH_SIZE - 2) && currentArrayOffsetY > 0 && directions[currentArrayOffsetX + 1][currentArrayOffsetY - 1] == 0 && (clip[currentClipMapX + 1][currentClipMapY - 1] & 0x63e40000) == 0 && (clip[currentClipMapX + 2][currentClipMapY - 1] & 0x60e40000) == 0 && (clip[currentClipMapX + 2][currentClipMapY] & 0x78e40000) == 0) {
				RouteFinderMod.queueX[queueWrite] = currentMapX + 1;
				RouteFinderMod.queueY[queueWrite] = currentMapY - 1;
				directions[currentArrayOffsetX + 1][currentArrayOffsetY - 1] = 9;
				queueWrite = queueWrite + 1 & 0xfff;
				distances[currentArrayOffsetX + 1][currentArrayOffsetY - 1] = distance;
			}
			if (currentArrayOffsetX > 0 && currentArrayOffsetY < (RouteFinderMod.GRAPH_SIZE - 2) && directions[currentArrayOffsetX - 1][currentArrayOffsetY + 1] == 0 && (clip[currentClipMapX - 1][currentClipMapY + 1] & 0x4fa40000) == 0 && (clip[currentClipMapX - 1][currentClipMapY + 2] & 0x4e240000) == 0 && (clip[currentClipMapX][currentClipMapY + 2] & 0x7e240000) == 0) {
				RouteFinderMod.queueX[queueWrite] = currentMapX - 1;
				RouteFinderMod.queueY[queueWrite] = currentMapY + 1;
				directions[currentArrayOffsetX - 1][currentArrayOffsetY + 1] = 6;
				queueWrite = queueWrite + 1 & 0xfff;
				distances[currentArrayOffsetX - 1][currentArrayOffsetY + 1] = distance;
			}
			if (currentArrayOffsetX < (RouteFinderMod.GRAPH_SIZE - 2) && currentArrayOffsetY < (RouteFinderMod.GRAPH_SIZE - 2) && directions[currentArrayOffsetX + 1][currentArrayOffsetY + 1] == 0 && (clip[currentClipMapX + 1][currentClipMapY + 2] & 0x7e240000) == 0 && (clip[currentClipMapX + 2][currentClipMapY + 2] & 0x78240000) == 0 && (clip[currentClipMapX + 2][currentClipMapY + 1] & 0x78e40000) == 0) {
				RouteFinderMod.queueX[queueWrite] = currentMapX + 1;
				RouteFinderMod.queueY[queueWrite] = currentMapY + 1;
				queueWrite = queueWrite + 1 & 0xfff;
				directions[currentArrayOffsetX + 1][currentArrayOffsetY + 1] = 12;
				distances[currentArrayOffsetX + 1][currentArrayOffsetY + 1] = distance;
			}
		}
		RouteFinderMod.foundMapY = currentMapY;
		RouteFinderMod.foundMapX = currentMapX;
		return false;
	}

	private static boolean findRouteSizeX(CollisionData collision, int routeType, int fromMapX, int fromMapY, int toMapX, int toMapY, int targetSizeX, int targetSizeY, int targetFace, int creatureSize, int blockFlag) {
		int currentMapX = fromMapX;
		int currentMapY = fromMapY;
		int currentArrayOffsetX = 64;
		int currentArrayOffsetY = 64;
		int baseArrayOffsetX = fromMapX - currentArrayOffsetX;
		int baseArrayOffsetY = fromMapY - currentArrayOffsetY;
		directions[currentArrayOffsetX][currentArrayOffsetY] = 99;
		distances[currentArrayOffsetX][currentArrayOffsetY] = 0;
		int queueWrite = 0;
		int queueRead = 0;
		RouteFinderMod.queueX[queueWrite] = currentMapX;
		RouteFinderMod.queueY[queueWrite++] = currentMapY;
		int[][] clip = collision.getFlags();
		while (queueRead != queueWrite) {
			currentMapX = RouteFinderMod.queueX[queueRead];
			currentMapY = RouteFinderMod.queueY[queueRead];
			currentArrayOffsetX = currentMapX - baseArrayOffsetX;
			currentArrayOffsetY = currentMapY - baseArrayOffsetY;
			int currentClipMapX = currentMapX - collision.getOffsetX();
			int currentClipMapY = currentMapY - collision.getOffsetY();
			queueRead = queueRead + 1 & 0xfff;

			if (routeType == -4) { // standart clip type , ignores target and
				// creature size check
				if (toMapX == currentMapX && toMapY == currentMapY) {
					RouteFinderMod.foundMapY = currentMapY;
					RouteFinderMod.foundMapX = currentMapX;
					return true;
				}
			} else if (routeType == -3) {
				if (collision.checkMove(currentMapX, currentMapY, toMapX, toMapY, creatureSize, creatureSize, targetSizeX, targetSizeY)) {
					RouteFinderMod.foundMapX = currentMapX;
					RouteFinderMod.foundMapY = currentMapY;
					return true;
				}
			} else if (routeType == -2) { // used for interacting
				if (collision.checkMove(currentMapX, currentMapY, toMapX, toMapY, creatureSize, creatureSize, targetSizeX, targetSizeY, blockFlag)) {
					RouteFinderMod.foundMapX = currentMapX;
					RouteFinderMod.foundMapY = currentMapY;
					return true;
				}
			} else if (routeType == -1) { // used for npcs clipping
				if (collision.checkMove_(currentMapX, currentMapY, toMapX, toMapY, creatureSize, targetSizeX, targetSizeY, blockFlag)) {
					RouteFinderMod.foundMapX = currentMapX;
					RouteFinderMod.foundMapY = currentMapY;
					return true;
				}
			} else if (routeType == 0 || routeType == 1 || routeType == 2 || routeType == 3 || routeType == 9) { // used
				// for
				// walking
				// to
				// objects
				// mostly
				if (collision.withinDistance(routeType, toMapX, toMapY, currentMapX, currentMapY, targetFace, creatureSize)) {
					RouteFinderMod.foundMapY = currentMapY;
					RouteFinderMod.foundMapX = currentMapX;
					return true;
				}
			} else if (collision.checkMove(routeType, currentMapX, currentMapY, toMapX, toMapY, creatureSize, targetFace)) {
				RouteFinderMod.foundMapX = currentMapX;
				RouteFinderMod.foundMapY = currentMapY;
				return true;
			}

			int distance = distances[currentArrayOffsetX][currentArrayOffsetY] + 1;
			while_5_: do {
				if (currentArrayOffsetX > 0 && directions[currentArrayOffsetX - 1][currentArrayOffsetY] == 0 && (clip[currentClipMapX - 1][currentClipMapY] & 0x43a40000) == 0 && ((clip[currentClipMapX - 1][creatureSize + (currentClipMapY - 1)] & 0x4e240000) == 0)) {
					for (int i = 1; i < creatureSize - 1; i++) {
						if ((clip[currentClipMapX - 1][i + currentClipMapY] & 0x4fa40000) != 0)
							break while_5_;
					}
					RouteFinderMod.queueX[queueWrite] = currentMapX - 1;
					RouteFinderMod.queueY[queueWrite] = currentMapY;
					queueWrite = queueWrite + 1 & 0xfff;
					directions[currentArrayOffsetX - 1][currentArrayOffsetY] = 2;
					distances[currentArrayOffsetX - 1][currentArrayOffsetY] = distance;
				}
			} while (false);
			while_6_: do {
				if (-creatureSize + GRAPH_SIZE > currentArrayOffsetX && directions[currentArrayOffsetX + 1][currentArrayOffsetY] == 0 && (clip[currentClipMapX + creatureSize][currentClipMapY] & 0x60e40000) == 0 && ((clip[currentClipMapX + creatureSize][creatureSize + (currentClipMapY - 1)] & 0x78240000) == 0)) {
					for (int i = 1; creatureSize - 1 > i; i++) {
						if ((clip[currentClipMapX + creatureSize][i + currentClipMapY] & 0x78e40000) != 0)
							break while_6_;
					}
					RouteFinderMod.queueX[queueWrite] = currentMapX + 1;
					RouteFinderMod.queueY[queueWrite] = currentMapY;
					directions[currentArrayOffsetX + 1][currentArrayOffsetY] = 8;
					queueWrite = queueWrite + 1 & 0xfff;
					distances[currentArrayOffsetX + 1][currentArrayOffsetY] = distance;
				}
			} while (false);
			while_7_: do {
				if (currentArrayOffsetY > 0 && directions[currentArrayOffsetX][currentArrayOffsetY - 1] == 0 && (clip[currentClipMapX][currentClipMapY - 1] & 0x43a40000) == 0 && (clip[creatureSize + currentClipMapX - 1][currentClipMapY - 1] & 0x60e40000) == 0) {
					for (int i = 1; creatureSize - 1 > i; i++) {
						if ((clip[currentClipMapX + i][currentClipMapY - 1] & 0x63e40000) != 0)
							break while_7_;
					}
					RouteFinderMod.queueX[queueWrite] = currentMapX;
					RouteFinderMod.queueY[queueWrite] = currentMapY - 1;
					directions[currentArrayOffsetX][currentArrayOffsetY - 1] = 1;
					queueWrite = queueWrite + 1 & 0xfff;
					distances[currentArrayOffsetX][currentArrayOffsetY - 1] = distance;
				}
			} while (false);
			while_8_: do {
				if (currentArrayOffsetY < -creatureSize + GRAPH_SIZE && directions[currentArrayOffsetX][currentArrayOffsetY + 1] == 0 && (clip[currentClipMapX][currentClipMapY + creatureSize] & 0x4e240000) == 0 && ((clip[creatureSize + currentClipMapX - 1][creatureSize + currentClipMapY] & 0x78240000) == 0)) {
					for (int i = 1; creatureSize - 1 > i; i++) {
						if ((clip[currentClipMapX + i][currentClipMapY + creatureSize] & 0x7e240000) != 0)
							break while_8_;
					}
					RouteFinderMod.queueX[queueWrite] = currentMapX;
					RouteFinderMod.queueY[queueWrite] = currentMapY + 1;
					directions[currentArrayOffsetX][currentArrayOffsetY + 1] = 4;
					queueWrite = queueWrite + 1 & 0xfff;
					distances[currentArrayOffsetX][currentArrayOffsetY + 1] = distance;
				}
			} while (false);
			while_9_: do {
				if (currentArrayOffsetX > 0 && currentArrayOffsetY > 0 && directions[currentArrayOffsetX - 1][currentArrayOffsetY - 1] == 0 && (clip[currentClipMapX - 1][currentClipMapY - 1] & 0x43a40000) == 0) {
					for (int i = 1; i < creatureSize; i++) {
						if (((clip[currentClipMapX - 1][currentClipMapY + (-1 + i)] & 0x4fa40000) != 0) || ((clip[currentClipMapX + i - 1][currentClipMapY - 1] & 0x63e40000) != 0))
							break while_9_;
					}
					RouteFinderMod.queueX[queueWrite] = currentMapX - 1;
					RouteFinderMod.queueY[queueWrite] = currentMapY - 1;
					directions[currentArrayOffsetX - 1][currentArrayOffsetY - 1] = 3;
					queueWrite = queueWrite + 1 & 0xfff;
					distances[currentArrayOffsetX - 1][currentArrayOffsetY - 1] = distance;
				}
			} while (false);
			while_10_: do {
				if (currentArrayOffsetX < -creatureSize + GRAPH_SIZE && currentArrayOffsetY > 0 && directions[currentArrayOffsetX + 1][currentArrayOffsetY - 1] == 0 && (clip[currentClipMapX + creatureSize][currentClipMapY - 1] & 0x60e40000) == 0) {
					for (int i = 1; i < creatureSize; i++) {
						if ((clip[creatureSize + currentClipMapX][i + (currentClipMapY - 1)] & 0x78e40000) != 0 || ((clip[currentClipMapX + i][currentClipMapY - 1] & 0x63e40000) != 0))
							break while_10_;
					}
					RouteFinderMod.queueX[queueWrite] = currentMapX + 1;
					RouteFinderMod.queueY[queueWrite] = currentMapY - 1;
					directions[currentArrayOffsetX + 1][currentArrayOffsetY - 1] = 9;
					queueWrite = queueWrite + 1 & 0xfff;
					distances[currentArrayOffsetX + 1][currentArrayOffsetY - 1] = distance;
				}
			} while (false);
			while_11_: do {
				if (currentArrayOffsetX > 0 && currentArrayOffsetY < -creatureSize + GRAPH_SIZE && directions[currentArrayOffsetX - 1][currentArrayOffsetY + 1] == 0 && (clip[currentClipMapX - 1][currentClipMapY + creatureSize] & 0x4e240000) == 0) {
					for (int i = 1; i < creatureSize; i++) {
						if ((clip[currentClipMapX - 1][currentClipMapY + i] & 0x4fa40000) != 0 || (clip[i + (currentClipMapX - 1)][currentClipMapY + creatureSize] & 0x7e240000) != 0)
							break while_11_;
					}
					RouteFinderMod.queueX[queueWrite] = currentMapX - 1;
					RouteFinderMod.queueY[queueWrite] = currentMapY + 1;
					directions[currentArrayOffsetX - 1][currentArrayOffsetY + 1] = 6;
					queueWrite = queueWrite + 1 & 0xfff;
					distances[currentArrayOffsetX - 1][currentArrayOffsetY + 1] = distance;
				}
			} while (false);
			while_12_: do {
				if (currentArrayOffsetX < GRAPH_SIZE - creatureSize && currentArrayOffsetY < -creatureSize + GRAPH_SIZE && directions[currentArrayOffsetX + 1][currentArrayOffsetY + 1] == 0 && (clip[currentClipMapX + creatureSize][currentClipMapY + creatureSize] & 0x78240000) == 0) {
					for (int i = 1; i < creatureSize; i++) {
						if ((clip[currentClipMapX + i][currentClipMapY + creatureSize] & 0x7e240000) != 0 || ((clip[currentClipMapX + creatureSize][i + currentClipMapY] & 0x78e40000) != 0))
							break while_12_;
					}
					RouteFinderMod.queueX[queueWrite] = currentMapX + 1;
					RouteFinderMod.queueY[queueWrite] = currentMapY + 1;
					queueWrite = queueWrite + 1 & 0xfff;
					directions[currentArrayOffsetX + 1][currentArrayOffsetY + 1] = 12;
					distances[currentArrayOffsetX + 1][currentArrayOffsetY + 1] = distance;
				}
			} while (false);
		}
		RouteFinderMod.foundMapY = currentMapY;
		RouteFinderMod.foundMapX = currentMapX;
		return false;
	}

}
