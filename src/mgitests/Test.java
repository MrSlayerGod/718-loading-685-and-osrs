package mgitests;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.rs.game.route.Flags;

public class Test {


	public static void main(String[] args) throws IOException {
		int id = 6473;
		int x= id >> 8 & 255;
		int y= id & 255;
		System.out.println((short) -32767);
	//	System.out.println(Utils.getAnimationDefinitionsSize());
	//	System.out.println(Utils.getGraphicDefinitionsSize());
	}


	
	public static void main2(String[] args) throws Throwable {

		int[] flaglist = new int[]
		{
			// main algorhytm flags
			0x43a40000,
			0x4e240000,
			0x60e40000,
			0x78240000,
			0x43a40000,
			0x60e40000,
			0x4e240000,
			0x78240000,
			0x4fa40000,
			0x43a40000,
			0x63e40000,
			0x63e40000,
			0x60e40000,
			0x78e40000,
			0x4fa40000,
			0x4e240000,
			0x7e240000,
			0x7e240000,
			0x78240000,
			0x78240000,

			// object path strategy
			0x2c0120,
			0x2c0102,
			0x2c0108,
			0x2c0180,
			0x2c0120,
			0x2c0102,
			0x2c0108,
			0x2c0180,

		};

		List<Integer> parsed = new ArrayList<Integer>();

		for (int i = 0; i < flaglist.length; i++) {
			int flag = flaglist[i];
			if (parsed.contains(flag))
				continue; // prevent repeation (cba sorting list)
			parsed.add(flag);
			if ((flag & 0x100) != 0) {
				System.err.println("Found 0x100 in 0x" + Integer.toString(flag, 16));
			}
			if ((flag & 0x20000) != 0) {
				System.err.println("Found 0x2000 in 0x" + Integer.toString(flag, 16));
			}
			if ((flag & 0x40000000) != 0) {
				System.err.println("Found 0x40000000 in 0x" + Integer.toString(flag, 16));
			}
			if ((flag & 0x400000) != 0) {
				System.err.println("Found 0x400000 in 0x" + Integer.toString(flag, 16));
			}
			if ((flag & 0x200000) != 0) {
				System.err.println("Found 0x200000 in 0x" + Integer.toString(flag, 16));
			}
		}

		for (int value : parsed) {
			int remainder = value & ~(0x100 | 0x20000 | 0x40000000 | 0x40000 | 0x200000);
			System.err.println("Remainder: 0x" + Integer.toString(remainder, 16) + " from: 0x" + Integer.toString(value, 16));
		}

		Scanner scanner = new Scanner(System.in);
		for (String str = scanner.nextLine(); str != null; str = scanner.nextLine()) {
			int value = str.startsWith("0x") ? Integer.parseInt(str.substring(2), 16) : Integer.parseInt(str);
			System.err.println("Disassembling 0x" + Integer.toString(value, 16));
			for (int bit = 0; bit < 32; bit++) {
				if ((value & (1 << bit)) != 0)
					System.err.println("Found 0x" + Integer.toString(1 << bit, 16));
			}

			StringBuilder flagbuilder = new StringBuilder();
			flagbuilder.append('(');
			for (Field field : Flags.class.getDeclaredFields()) {
				if ((value & field.getInt(null)) == 0)
					continue;

				if (flagbuilder.length() <= 1) {
					flagbuilder.append("Flags." + field.getName());
				} else {
					flagbuilder.append(" | Flags." + field.getName());
				}
			}
			flagbuilder.append(')');
			System.err.println(flagbuilder.toString());
		}
		scanner.close();
	}
}
