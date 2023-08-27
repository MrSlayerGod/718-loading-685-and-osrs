package com.rs.game.npc.others.zalcano;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.utils.Utils;

import static com.rs.game.npc.others.zalcano.Zalcano.BLUE_DEMONIC_SYMBOL_ID;
import static com.rs.game.npc.others.zalcano.Zalcano.RED_DEMONIC_SYMBOL_ID;

/**
 * @author Simplex
 * @since May 24, 2020
 */
public class DemonicCircles {

    public static WorldObject[][] PATTERNS = {
            {
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3029, 6057, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3036, 6057, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3025, 6053, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3030, 6053, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3035, 6053, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3040, 6053, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3029, 6050, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3036, 6050, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3029, 6047, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3036, 6047, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3025, 6044, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3030, 6044, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3035, 6044, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3040, 6044, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3029, 6040, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3036, 6040, 0))
            },
            {
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3030, 6059, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3035, 6059, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3029, 6055, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3036, 6055, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3022, 6052, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3026, 6053, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3039, 6053, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3043, 6052, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3030, 6050, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3035, 6050, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3030, 6047, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3035, 6047, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3022, 6045, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3043, 6045, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3026, 6044, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3039, 6044, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3029, 6042, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3036, 6042, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3030, 6038, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3035, 6038, 0)),
            },
            {
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3030, 6055, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3035, 6055, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3026, 6053, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3039, 6053, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3030, 6051, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3035, 6051, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3030, 6047, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3035, 6047, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3026, 6045, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3039, 6045, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3030, 6043, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3035, 6043, 0)),
            },
            {
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3030, 6056, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3035, 6056, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3029, 6052, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3036, 6052, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3025, 6051, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3040, 6051, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3025, 6046, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3040, 6046, 0)),

                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3029, 6045, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3036, 6045, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3030, 6041, 0)),
                    new WorldObject(RED_DEMONIC_SYMBOL_ID, 10, 0, new WorldTile(3035, 6041, 0)),
            }
    } ;

    public static WorldObject[] getRandomPattern() {
        return PATTERNS[Utils.random(PATTERNS.length)];
    }
}
