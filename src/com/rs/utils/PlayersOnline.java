package com.rs.utils;

import com.rs.Settings;
import com.rs.executor.GameExecutorManager;
import com.rs.game.World;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class PlayersOnline {

    private static long lastUpdate;

    public static void updateCount() {
        if (!Settings.HOSTED || Settings.WORLD_ID != 1 || Utils.currentTimeMillis() - lastUpdate < 5000) //update only once every 5 seconds
            return;
        lastUpdate = Utils.currentTimeMillis();
        GameExecutorManager.slowExecutor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    URL url = new URL("https://matrixrsps.io/playercount.php?key=asdf1234&count=" + World.getPlayerCount());

                    URLConnection con = url.openConnection();
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);

                //    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                 //   String results = reader.readLine();
               //     reader.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
