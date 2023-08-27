package com.rs.utils;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * @since Jan 03, 2021
 */
public class TopCox implements Serializable {
	private static final long serialVersionUID = 4117658865093005494L;

	// mode -> raid size -> index
	private static TopCox[][][] ranks;

	private String fc, names[];
	private long time;

	public TopCox(ChambersOfXeric cox, int totalPoints) {
		fc = cox.getFc();
		names = new String[cox.getPointMap().keySet().size()];
		time = cox.getRaidTime();
		int i = 0;
		String perc;

		for(Player player : cox.getTeam().stream().distinct().collect(Collectors.toList())) {
			String name = player.getDisplayName();
			perc = String.format("(" + Colour.RAID_PURPLE.wrap("%.2f") + "%%)", (((double) cox.getPointMap().get(player.getUsername().toLowerCase()) / totalPoints) * 100));
			names[i++] = name + " " + perc + "";
		}
	}
	
	public static void init() {
		ranks = SerializableFilesManager.loadTopCox();
		if (ranks == null) {
			// osrs mode
			ranks = new TopCox[2][100][25];
		}
	}

	public static final void save() {
		SerializableFilesManager.saveTopCox(ranks);
	}
	
	public static void sort(int mode, int size) {
		Arrays.sort(ranks[mode][size], (arg0, arg1) -> {
			if (arg0 == null)
				return 1;
			if (arg1 == null)
				return -1;
			if (arg0.time > arg1.time)
				return 1;
			else if (arg0.time < arg1.time)
				return -1;
			else
				return 0;
		});
	}
	
	public static void showRanks(Player player, int mode, int size) {
		for (int i = 0; i < 310; i++)
			player.getPackets().sendIComponentText(275, i, "");
		for (int i = 0; i < ranks[mode][size].length; i++) {
			if (ranks[mode][size][i] == null)
				break;

			String text = "";/*
			if (i >= 0 && i <= 2)
				text = "<img=1><col=ff9900>";
			else if (i <= 9)
				text = "<col=80>";
			else if (i <= 50)
				text = "<col=38610B>";
			else
				text = "<col=000000>";*/

			StringBuilder clan = new StringBuilder();
			int written = 0;
			for(String n : ranks[mode][size][i].names) {
				if(written++>0)
					clan.append(", ");
				if(written%3==0)
					clan.append("<br>" + text);
				clan.append(n);
				if(written == 5) // only show top 5 names
					break;
			}
			if(i != 0)
				clan.append("<br>");

			if(size == 1) {
				//solo
				player.getPackets()
						.sendIComponentText(
								275,
								i + 10,
								text
										+ "Rank "
										+ (i + 1) + " "
										+ Utils.formatPlayerNameForDisplay(ranks[mode][size][i].fc)
										+ " Time:  " + Colour.DARK_RED.wrap(Utils.formatTimeCox(ranks[mode][size][i].time))
										+ "<br>");
			} else {
				player.getPackets()
						.sendIComponentText(
								275,
								i + 10,
								text + (i > 0 ? "<br>" : "")//space out lines, currently overlapping
										+ "Rank "
										+ (i + 1) + " FC: "
										+ Colour.DARK_RED.wrap(Utils.formatPlayerNameForDisplay(ranks[mode][size][i].fc))
										+ " Time:  " + Colour.DARK_RED.wrap(Utils.formatTimeCox(ranks[mode][size][i].time))
										+ " <br>"
										+ text + clan.toString()
										+ "<br>");
			}
		}

		if(ranks[mode][size][0] == null) {
			player.getPackets().sendIComponentText(275, 11, "No records have been set for this raid.");
		}

		player.getPackets().sendIComponentText(275, 1,
				"Top " + (size == 1 ? "Solo" : size + "-man") + " "+ (mode == 0 ? "OSRS" : "MATRIX") +" raids");
		player.getInterfaceManager().sendInterface(275);
	}

	public static void addRank(ChambersOfXeric cox, int mode, int points) {
		boolean record = false;
		for(int i = 0; i < ranks[mode][cox.getTeamSize()].length; i++) {
			TopCox t = ranks[mode][cox.getTeamSize()][i];
			if(!record) {

				if(t != null && t.fc.equalsIgnoreCase(cox.getFc())) {
					// don't record if holding record, only allow each fc to hold one rank on hs
					//System.out.println("found " + t.fc + " at rank " + i);
					break;
				}

				// check if this team holds this record
				if(t == null || cox.getRaidTime() < t.time) {
					//System.out.println("recording " + cox.getFc() + " at rank " + i);
					// no standing record / beat the record
					if(i < 3) {
						World.sendNews(
								Colour.RAID_PURPLE.wrap(Utils.formatPlayerNameForDisplay(cox.getFc()))
										+ " <col=ffffff>set #"+(i+1)+" record in <col=ef20ff>"
										+(cox.getTeamSize() == 1 ? "SOLO" : cox.getTeamSize() + "-man")
										+" <col=ffffff>Chambers of Xeric <col=ef20ff>" + (cox.isOsrsRaid() ? "OSRS" : "MATRIX") + "</col> Time: "
										+ Colour.RAID_PURPLE.wrap(Utils.formatTimeCox(cox.getRaidTime())), 1);
					}
					record = true;
				}
			} else {
				// team is setting a new record
				// clear any further records from this team (only hold 1 record at a time)
				if(t != null && t.fc.equalsIgnoreCase(cox.getFc())) {
					ranks[mode][cox.getTeamSize()][i] = null;
					//System.out.println("Clearing record " + i);
				}
			}
		}
		if(record) {
			ranks[mode][cox.getTeamSize()][ranks[mode][cox.getTeamSize()].length-1] = new TopCox(cox, points);
			sort(mode, cox.getTeamSize());
		}
	}

    public static int clean() {
		int recordsCleaned = 0;

		for (int i = 0; i < ranks.length; i++) {
			for (int j = 0; j < ranks[i].length; j++) {
				for (int k = 0; k < ranks[i][j].length-2; k+=2) {
					if(ranks[i][j][k].time == ranks[i][j][k+1].time) {
						ranks[i][j][k + 1] = null;
						recordsCleaned++;
					}
				}

				sort(i, j);
			}
		}

		return recordsCleaned;
    }
}
