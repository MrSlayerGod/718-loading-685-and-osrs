package mgitests;

import java.util.Random;

public class Puzzle {

	public static void main(String[] args) {

		int[] mix = createMix(5, 5);

		int written = 0;
		for (int i = 0; i < mix.length; i++) {
			System.err.print(mix[i] == -1 ? "-1" : ((mix[i] + 1)) + "\t");
			if (++written >= 5) {
				System.err.println("\n");
				written = 0;
			}
		}

	}

	public static int[] createMix(int width, int height) {
		Random random = new Random();
		int[] left = new int[width * height];
		int[] mix = new int[width * height];
		for (int i = 0; i < left.length; i++)
			left[i] = i;
		left[left.length - 1] = -1;
		left[left.length - 2] = -1;
		left[left.length - 3] = -1;
		int unused = left.length, write = 0;
		int blankx = -1, blanky = -1;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int index = random.nextInt(unused);
				mix[write++] = left[index];
				if (left[index] == left.length - 1) {
					blankx = x;
					blanky = y;
				}
				left[index] = left[--unused];
			}
		}

		int i1 = -1;
		int i2 = -1;
		for (int i = 0; i < mix.length; i++) {
			if (mix[i] == -1) {
				if (i1 == -1) {
					i1 = i;
					mix[i1] = mix.length - 2;
				} else {
					i2 = i;
					mix[i2] = mix.length - 3;
					break;
				}
			}
		}

		//parity check
		int tot = 0;
		for (int i = 1; i < mix.length; i++) {
			for (int j = 0; j < i; j++) {
				if (mix[j] > mix[i])
					tot++;
			}
		}

		tot += (width - 1) - blankx + (height - 1) - blanky;

		if ((tot & 1) == 0) {
			mix[i1] = mix.length - 3;
			mix[i2] = mix.length - 2;
		}

		return mix;

	}

}
