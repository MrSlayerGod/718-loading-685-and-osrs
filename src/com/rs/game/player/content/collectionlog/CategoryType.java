package com.rs.game.player.content.collectionlog;

import java.io.Serializable;

/**
 * @author Simplex
 * @since May 08, 2020
 */
public enum CategoryType {
    BOSSES("Bosses", "Kills:"),
    RAIDS("Raids", "Completions:"),
    CLUES("Clues", "Completed:"),
    MINIGAMES("Minigames", "Completions:"),
    OTHERS("Others", null);

    String name, killString;

    CategoryType(String name, String killString) {
        this.name = name;
        this.killString = killString;
    }
}
