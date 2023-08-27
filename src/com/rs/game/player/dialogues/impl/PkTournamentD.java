package com.rs.game.player.dialogues.impl;

import com.rs.Settings;
import com.rs.game.item.Item;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.player.dialogues.Dialogue;

import java.util.concurrent.TimeUnit;

/**
 * @author Simplex
 * @since Jun 07, 2020
 */
public class PkTournamentD extends Dialogue {
    int npc;
    int type;

    @Override
    public void start() {
        npc = (int) parameters[0];
        type = (int) parameters[1];
        stage = 0;

        if(type == 0)
            joinDialogue(0, 0);
        else
            leaveDialogue(0, 0);
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if(Settings.PK_TOURNAMENTS_DISABLED) {
            sendDialogue("Pk Tournaments have been disabled by an Admin.");
            stage = 3;
            return;
        }
    	
    	if (stage == 102) {
    		if (componentId == OPTION_1)
    			PkTournament.enterSpectate(player);
    		end();
    		return;
    	}
    	
    	
        stage ++;

        if(type == 0)
            joinDialogue(interfaceId, componentId);
        else
            leaveDialogue(interfaceId, componentId);
    }

    private void leaveDialogue(int interfaceId, int componentId) {
    }

    private void joinDialogue(int interfaceId, int componentId) {
   	if (stage == 101) {
   		sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Spectate", "Cancel");
   		stage = 102;
   		return;
    }
       //System.out.println(stage);
    	
        if(PkTournament.canJoin) {
            switch(stage) {
                case 0:
                    sendNPCDialogue(npc, NORMAL, "There is a " + PkTournament.getType().getFormattedName() + " PK Tournament running. Would you like to join?");
                    break;
                case 1:
                    sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Yes, sign me up.", "No, I'd rather not.");
                    break;
                case 2:
                    if (componentId == OPTION_1) {
                        if(player.getInventory().getItems().getFreeSlots() != player.getInventory().getItemsContainerSize()
                                || player.getEquipment().getItems().getFreeSlots() != player.getEquipment().getItems().getSize()
                                || player.getFamiliar() != null || player.getPet() != null) {
                            sendNPCDialogue(npc, NORMAL, "You must bank all items, equipment and pets before entering the PK Tournament.");
                        } else {
                            PkTournament.join(player);
                        }
                    } else {
                        end();
                    }
                    break;
                case 3:
                    end();
                    break;
            }
        } else if((PkTournament.isRunning) && !PkTournament.canJoin){
            if(stage == 0) {
              //  sendNPCDialogue(npc, NORMAL, "There is a " + PkTournament.getType() + " PK Tournament underway.<br>Sign up has ended.");
                sendNPCDialogue(npc, NORMAL, "Sign up has ended, would you like to spectate a fight?");
                stage = 100;
            } else
            	end();
        } else if(!PkTournament.isRunning){
            if(stage == 0) {
                sendNPCDialogue(npc, NORMAL, "There's no PK Tournament running right now. Would you like to pay 5m to start a PK Tournament?.");
                //  sendNPCDialogue(npc, NORMAL, "There's no PK Tournament running right now. Come back around 8:00 PM server time or every 6 hours.");
            } else if (stage == 1) {
                this.sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Pay 5m and start a Fun PK Tournament (no rewards)!", "No thank you!");
            } else if (stage == 2) {
                if (componentId == OPTION_1) {
                    if(PkTournament.isRunning || PkTournament.canJoin) {
                        player.sendMessage("There is already a PK Tournament running.");
                        end();
                        return;
                    }
                    if (player.getInventory().getCoinsAmount() < 5000000) {
                        sendNPCDialogue(npc, NORMAL, "Come back once you have enough cash!");
                        return;
                    }
                    player.getInventory().removeItemMoneyPouch(new Item(995, 5000000));
                    PkTournament.fun = true;
                    player.sendMessage("Fun tournament enabled.");
                    PkTournament.initTournament(5, TimeUnit.MINUTES);
                }
                end();
            } else end();
        }
    }

    @Override
    public void finish() {

    }
}
