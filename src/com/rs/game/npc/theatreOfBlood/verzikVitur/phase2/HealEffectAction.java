package com.rs.game.npc.theatreOfBlood.verzikVitur.phase2;


import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.theatreOfBlood.TOBAction;
import com.rs.game.npc.theatreOfBlood.verzikVitur.NycolasMemetos;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.utils.Utils;

public class HealEffectAction implements TOBAction {

    @Override
    public int use(NPC npc) {
        npc.anim(28117);

        WorldTile position = new WorldTile(npc);
        for (int index = 0; index < 2; index++) {
        	WorldTile random = position.transform(Utils.random(6), Utils.random(6), 0);
            NycolasMemetos mob = new NycolasMemetos((VerzikVitur) npc, random);/*(NycolasMemetos) npc.raid.spawnNpc(null, NycolasMemetos.NYCOLAS_MEMTOS_ID, random.getX(),
                    random.getY(), random.getZ(), 1, 200, 0, 0, 100, false, false);*/
            mob.randomWalk();
        }
        return 10;
    }
}
