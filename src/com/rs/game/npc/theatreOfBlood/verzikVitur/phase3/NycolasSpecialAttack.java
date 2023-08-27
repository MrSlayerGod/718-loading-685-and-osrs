package com.rs.game.npc.theatreOfBlood.verzikVitur.phase3;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikNycolas;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class NycolasSpecialAttack implements TOBAction {

    @Override
    public int use(NPC npc) {
    	  final VerzikVitur verzik = (VerzikVitur) npc;
        for (Player target : verzik.getRaid().getTargets(npc)) {
         //   Position pos = npc.getPosition().randomize(8);
        	WorldTile pos = npc.transform(Utils.random(8), Utils.random(8), 0);
            VerzikNycolas spawn = new VerzikNycolas(verzik.getRaid(), target, pos);/* (VerzikNycolas) npc..spawnNpc(null, VerzikNycolas.getRandomId(), pos.getX(), pos.getY(), pos.getZ(),
                    -1, 11, 0, 0, 0, false, false);
            spawn.setTarget(target);*/
        }
        return 1;
    }
}
