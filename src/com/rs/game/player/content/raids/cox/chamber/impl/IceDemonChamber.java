package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.game.npc.cox.impl.IceDemon;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.NPCHandler;
import com.rs.net.decoders.handlers.ObjectHandler;

/**
 * @author Simplex
 * @since Nov 01, 2020
 */
public class IceDemonChamber extends Chamber {

    public IceDemonChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    public static void init() {
        NPCHandler.register(IceDemon.ICE_DEMON_FROZEN, 1, ((player, npc)
                -> player.sendMessage("The demon is protected by ice, your attacks would be ineffective!")));
        ObjectHandler.register(129763, 1, ((player, obj)
                -> player.getActionManager().setAction(new Woodcutting(obj, Woodcutting.TreeDefinitions.SAPLING))));
        ObjectHandler.register(131634, 1, ((player, obj)
                -> player.sendMessage("You already have a tinderbox on your toolbelt.")));
        ObjectHandler.register(131862, 1, ((player, obj)
                -> player.sendMessage("You already have a bronze axe on your toolbelt.")));
        ObjectHandler.register(129771, 1, ((player, obj)
                -> player.sendMessage("You already have all of these tools on your toolbelt.")));
        ObjectHandler.register(new int[]{IceDemon.LIT_BRAZIER, IceDemon.UNLIT_BRAZIER}, 1, ((player, obj) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if (raid != null && raid.getIceDemonChamber().getIceDemon() != null) // causing issues on beta
                raid.getIceDemonChamber().getIceDemon().addKindling(player, obj);
        }));
    }

    public IceDemon getIceDemon() {
        return iceDemon;
    }

    public IceDemon iceDemon;

    @Override
    public void onActivation() {
        iceDemon.onActivation();
    }

    @Override
    public void onRaidStart() {
        setDefaultActivationoTask();
        iceDemon = new IceDemon(getRaid());
    }
}
