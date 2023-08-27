package com.rs.game.player.content;

import com.rs.game.player.Player;

public class ImbuedScrolls {

    public enum Imbue {

        U_B_C(25505, 25742, 25760),
        U_B_T(25506, 25743, 25760),
        U_B_H(25507, 25744, 25760),
        U_B_G(25508, 25745, 25760),
        U_B_B(25509, 25746, 25760),
        U_B_S(25510, 25747, 25760),

        U_A_H(25511, 25748, 25761),
        U_A_C(25512, 25749, 25761),
        U_A_S(25513, 25750, 25761),
        U_A_G(25514, 25751, 25761),
        U_A_B(25515, 25752, 25761),
        U_A_SH(25516, 25753, 25761),

        U_S_H(25517, 25754, 25761),
        U_S_T(25518, 25755, 25761),
        U_S_L(25519, 25756, 25761),
        U_S_G(25520, 25757, 25761),
        U_S_B(25521, 25758, 25761),
        U_S_S(25522, 25759, 25761),

        ;

        private int fromID, toID, scrollID;

        private Imbue(int fromID, int toID, int scrollID) {
            this.fromID = fromID;
            this.toID = toID;
            this.scrollID = scrollID;
        }
    }


    public static Imbue getImbue(int fromID, int onID) {
        for (Imbue imbue : Imbue.values()) {
            if (imbue.fromID == fromID && imbue.scrollID == onID)
                return imbue;
        }
        for (Imbue imbue : Imbue.values()) {
            if (imbue.fromID == onID && imbue.scrollID == fromID)
                return imbue;
        }
        return null;
    }

    public static boolean imbue(Player player, int fromID, int onID) {
        Imbue imbue = getImbue(fromID, onID);
        if (imbue == null)
            return false;
        player.lock(2);
        player.getInventory().deleteItem(fromID, 1);
        player.getInventory().deleteItem(onID, 1);
        player.getInventory().addItem(imbue.toID, 1);
        player.getPackets().sendGameMessage("Your item shines as you read the scroll incantation.");
        return true;
    }
}
