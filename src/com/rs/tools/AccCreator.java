package com.rs.tools;

import java.io.File;
import java.io.FileInputStream;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.login.account.Account;
import com.rs.utils.LoginFilesManager;
import com.rs.utils.SerializationUtilities;
import com.rs.utils.Utils;

public class AccCreator {

	public static void main(String[] args) throws Throwable {
		Settings.HOSTED = true;
		Cache.init();
		LoginFilesManager.init();

		com.rs.login.DisplayNames names = LoginFilesManager.loadDisplayNames();

		int last = 0;
		int amt = 0;
		FileInputStream fis = new FileInputStream(new File("dump.dat"));
		for (;;) {
			if ((amt - last) > 2000) {
				last = amt;
				System.err.println("Progress:" + amt);
				LoginFilesManager.flush();
			}
			int b1 = fis.read();
			if (b1 == -1)
				break;

			int length = b1 + (fis.read() << 8) + (fis.read() << 16) + (fis.read() << 24);
			byte[] data = new byte[length];
			fis.read(data);

			AccData acc = (AccData) SerializationUtilities.loadObject(data);

			Account account = new Account(acc.username, acc.password, 0);
			names.reserveDisplayName(acc.username, Utils.formatPlayerNameForDisplay(acc.username));

			Player player = new Player();
			
			if (acc.inventory != null) {
				for (int i = 0; i < acc.inventory.length; i++)
					player.getInventory().getItems().set(i, acc.inventory[i]);
			}

			if (acc.bank != null) {
				player.getBank().addItems(acc.bank, false);
			}

			if (acc.equipment != null)
				for (int i = 0; i < acc.equipment.length; i++)
					player.getEquipment().getItems().set(i, acc.equipment[i]);

			player.getMoneyPouch().setCoinsAmount(acc.pouch);

			player.setGeManager(acc.ge);

			player.getSkills().setXp(acc.exp);

			account.writeFile(0, SerializationUtilities.storeObject(player));
			LoginFilesManager.saveAccount(account);
			amt++;

		}
		fis.close();

		LoginFilesManager.saveDisplayNames(names);
		LoginFilesManager.flush();

	}

}
