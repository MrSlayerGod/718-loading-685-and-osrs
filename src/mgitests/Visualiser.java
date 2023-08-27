package mgitests;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class Visualiser {

	private JFrame frame;
	private Canvas canvas;
	private BufferedImage buffer;
	private CollisionData collision;

	private int scale = 8;
	private int size = 72;

	private int selectedX1, selectedY1;
	private int selectedX2, selectedY2;

	private int[] routeBufferX = new int[4096];
	private int[] routeBufferY = new int[4096];
	private int routeSteps = -1;
	private boolean isAlternative = false;
	private long routeBenchmark = 0L;

	public Visualiser() {
		frame = new JFrame("Pathfinder visualiser");
		canvas = new Canvas();
		canvas.setSize(800, 600);
		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);

		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				String text = KeyEvent.getKeyText(e.getKeyCode()).toLowerCase();
				System.err.println("Typed:" + text);
				switch (text) {
				case "right":
					selectedX1++;
					break;
				case "left":
					selectedX1--;
					break;
				case "up":
					selectedY1++;
					break;
				case "down":
					selectedY1--;
					break;
				case "numpad-6":
					selectedX2++;
					break;
				case "numpad-4":
					selectedX2--;
					break;
				case "numpad-8":
					selectedY2++;
					break;
				case "numpad-2":
					selectedY2--;
					break;
				case "o":
					collision.flagUnwalkable(selectedX1, selectedY1);
					break;
				case "e":
					collision.flagWall(selectedX1, selectedY1, 0, 0, true, true);
					break;
				case "w":
					collision.flagWall(selectedX1, selectedY1, 0, 2, true, true);
					break;
				case "n":
					collision.flagWall(selectedX1, selectedY1, 0, 1, true, true);
					break;
				case "s":
					collision.flagWall(selectedX1, selectedY1, 0, 3, true, true);
					break;
				case "c":
					collision.set(selectedX1, selectedY1, 0);
					break;
				case "enter":
					routeSteps = -1;
					long start = System.nanoTime();
					int steps = RouteFinderMod.findRoute(collision, -4, 1, selectedX1, selectedY1, selectedX2, selectedY2, 1, 1, 0, routeBufferX, routeBufferY, true, 0);
					long took = System.nanoTime() - start;
					if (steps >= 0) {
						routeSteps = steps;
						routeBenchmark = took;
						isAlternative = RouteFinderMod.foundMapX != selectedX2 || RouteFinderMod.foundMapY != selectedY2;
					}
				}

				if (selectedX1 < 0) {
					selectedX1 += size;
				} else if (selectedX1 >= size) {
					selectedX1 -= size;
				}

				if (selectedY1 < 0) {
					selectedY1 += size;
				} else if (selectedY1 >= size) {
					selectedY1 -= size;
				}

				if (selectedX2 < 0) {
					selectedX2 += size;
				} else if (selectedX2 >= size) {
					selectedX2 -= size;
				}

				if (selectedY2 < 0) {
					selectedY2 += size;
				} else if (selectedY2 >= size) {
					selectedY2 -= size;
				}

			}
		});

		collision = CollisionData.create(size, size);

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				collision.unflagUnwalkable(x, y);
			}
		}
	}

	public static void main(String[] args) throws Throwable {
		Visualiser visualiser = new Visualiser();
		visualiser.run();
	}

	public void run() throws Throwable {
		while (true) {
			if (buffer == null || buffer.getWidth() != canvas.getWidth() || buffer.getHeight() != canvas.getHeight()) {
				buffer = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			}
			paint(buffer.getGraphics());

			Graphics g = canvas.getGraphics();
			g.drawImage(buffer, 0, 0, null);
			g.dispose();
			// frame.repaint();
			try {
				Thread.sleep(5);
			} catch (Throwable t) {
			}
		}
	}

	public int invert(int c) {
		return size - c;
	}

	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		int freeX = canvas.getWidth() - (size * scale);
		int freeY = canvas.getHeight() - (size * scale);
		int minX = freeX / 2;
		int minY = freeY / 2;
		int squareWidth = scale;
		int squareHeight = scale;

		g.setColor(Color.GREEN);
		g.drawRect(minX - 1, minY - 1, (size * squareWidth) + 2, (size * squareHeight) + 1);

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				int flag = collision.get(x, y);
				if ((flag & CollisionData.UNWALKABLE_FLOOR) != 0) {
					g.setColor(Color.RED);
					g.fillRect(minX + (x * squareWidth), minY + (invert(y) * squareHeight) - squareHeight, squareWidth, squareHeight);
				}

				if ((flag & CollisionData.WALL_NORTH) != 0) {
					g.setColor(Color.BLUE);
					g.fillRect(minX + (x * squareWidth), minY + (invert(y) * squareHeight) - squareHeight, squareWidth, squareHeight / 4);
				}

				if ((flag & CollisionData.WALL_SOUTH) != 0) {
					g.setColor(Color.BLUE);
					g.fillRect(minX + (x * squareWidth), minY + (invert(y) * squareHeight) - squareHeight + ((squareHeight / 4) * 3), squareWidth, squareHeight / 4);
				}

				if ((flag & CollisionData.WALL_EAST) != 0) {
					g.setColor(Color.BLUE);
					g.fillRect(minX + (x * squareWidth), minY + (invert(y) * squareHeight) - squareHeight, squareWidth / 4, squareHeight);
				}

				if ((flag & CollisionData.WALL_WEST) != 0) {
					g.setColor(Color.BLUE);
					g.fillRect(minX + (x * squareWidth) + ((squareWidth / 4) * 3), minY + (invert(y) * squareHeight) - squareHeight, squareWidth / 4, squareHeight);
				}
			}
		}

		if (routeSteps > 0) {
			g.setColor(Color.GREEN);
			for (int i = 0; i < routeSteps; i++) {
				int x = routeBufferX[i];
				int y = routeBufferY[i];
				g.fillRect(minX + (x * squareWidth), minY + (invert(y) * squareWidth) - squareWidth, squareWidth, squareHeight);
			}
		}

		g.setColor(Color.YELLOW);
		g.fillRect(minX + (selectedX1 * squareWidth), minY + (invert(selectedY1) * squareWidth) - squareWidth, squareWidth, squareHeight);

		g.setColor(Color.ORANGE);
		g.fillRect(minX + (selectedX2 * squareWidth), minY + (invert(selectedY2) * squareWidth) - squareWidth, squareWidth, squareHeight);

		g.setColor(Color.WHITE);
		g.drawLine(minX + (selectedX1 * squareWidth), minY + (invert(selectedY1) * squareWidth) - squareWidth, minX + (selectedX2 * squareWidth), minY + (invert(selectedY2) * squareWidth) - squareWidth);

		g.setColor(Color.GREEN);
		g.setFont(new Font("Courier", Font.BOLD, 12));
		g.drawString("Selection1[" + selectedX1 + "," + selectedY1 + "]", 0, 10);
		g.drawString("Selection2[" + selectedX2 + "," + selectedY2 + "]", 0, 20);
		g.drawString("Controls:", 0, 30);
		g.drawString("Arrow keys - move selector 1", 0, 40);
		g.drawString("Pad keys - move selector 2", 0, 50);
		g.drawString("o - place unmovable block", 0, 60);
		g.drawString("e - place east wall", 0, 70);
		g.drawString("w - place west wall", 0, 80);
		g.drawString("n - place north wall", 0, 90);
		g.drawString("s - place south wall", 0, 100);
		g.drawString("c - clear flags", 0, 110);
		g.drawString("r - generate random", 0, 120);
		g.drawString("enter - find route from 1 to 2", 0, 130);
		g.drawString("Route results:" + (routeSteps == -1 ? "route not found." : (isAlternative ? "alternative route found." : "route found.")), 0, 140);
		if (routeSteps >= 0) {
			g.drawString("Steps:" + routeSteps, 0, 150);
			g.drawString("Took " + ((double) routeBenchmark / 1000000D) + " ms.", 0, 160);
		}

		g.setColor(Color.RED);
		// draw3DCube(g, 300, 300, 200, 200, -100);
		// draw3DLine(g, 300, 300, 10, 10, -10);
		g.dispose();
	}

	public void draw3DCube(Graphics g, int x, int y, int width, int height, int depth) {
		g.drawRect(x, y, width, height);
		g.drawRect(x + depth, y - depth, width, height);
		g.drawLine(x, y, x + depth, y - depth);
		g.drawLine(x + width, y, x + depth + width, y - depth);
		g.drawLine(x, y + height, x + depth, y - depth + height);
		g.drawLine(x + width, y + height, x + depth + width, y - depth + height);
	}

	public void draw3DLine(Graphics g, int x, int y, int width, int height, int depth) {
		g.drawLine(x, y, x + depth, y - depth);
	}
}
