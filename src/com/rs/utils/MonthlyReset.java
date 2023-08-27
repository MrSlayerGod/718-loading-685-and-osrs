package com.rs.utils;

import com.rs.Settings;
import com.rs.discord.Bot;
import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.net.Session;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @author Simplex
 * created on 2021-02-02
 */
public class MonthlyReset implements Serializable {
    public String month;

    public static MonthlyReset monthlyReset = null;

    public static void init() {
        LocalDate d = LocalDate.now();
        monthlyReset = SerializableFilesManager.loadMonthlyReset();

        if(monthlyReset == null) {
            monthlyReset = new MonthlyReset();
            monthlyReset.month = d.getMonth().toString();
        }

        GameExecutorManager.slowExecutor.scheduleWithFixedDelay(() -> {
            try {
                LocalDate date = LocalDate.now();
                if(!monthlyReset.month.equals(date.getMonth().toString())) {
                    // month changed
                    String m = monthlyReset.month;
                    Bot.sendLog(Bot.DONATIONS_CHANNEL, "[MONTHLY-RESET (TEST)]"
                            + "\n*"+m+" Top Donors*\n" + MTopDonator.getTopString()
                            + "\n*"+m+" Top Voters*\n" + MTopVoter.getTopString());
                    monthlyReset.month = date.getMonth().toString();
                    MTopDonator.resetMTopDonator();
                    MTopVoter.resetMTopVoter();
                }
            } catch (Throwable e) {
                Logger.handle(e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static void save() {
        SerializableFilesManager.saveMonthlyReset(monthlyReset);
    }
}
