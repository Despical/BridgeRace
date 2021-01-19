/*
 * Bridge Race - Eliminate your opponent to win!
 * Copyright (C) 2021 Despical
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.bridgerace.arena;

import me.despical.bridgerace.ConfigPreferences;
import me.despical.bridgerace.Main;
import me.despical.commonsbox.miscellaneous.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class ArenaUtils {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	public static void hidePlayer(Player p, Arena arena) {
		for (Player player : arena.getPlayers()) {
			PlayerUtils.hidePlayer(player, p, plugin);
		}
	}

	public static void showPlayer(Player p, Arena arena) {
		for (Player player : arena.getPlayers()) {
			PlayerUtils.showPlayer(player, p, plugin);
		}
	}

	public static void hidePlayersOutsideTheGame(Player player, Arena arena) {
		for (Player players : plugin.getServer().getOnlinePlayers()) {
			if (arena.getPlayers().contains(players)) {
				continue;
			}

			PlayerUtils.hidePlayer(player, players, plugin);
			PlayerUtils.hidePlayer(players, player, plugin);
		}
	}

	public static void updateNameTagsVisibility(final Player p) {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.NAMETAGS_HIDDEN)) {
			return;
		}

		for (Player players : plugin.getServer().getOnlinePlayers()) {
			Arena arena = ArenaRegistry.getArena(players);

			if (arena == null) {
				continue;
			}

			Scoreboard scoreboard = players.getScoreboard();

			if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
				scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
			}

			Team team = scoreboard.getTeam("BRHide");

			if (team == null) {
				team = scoreboard.registerNewTeam("BRHide");
			}

			team.setCanSeeFriendlyInvisibles(false);
			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

			if (arena.getArenaState() == ArenaState.IN_GAME) {
				team.addEntry(p.getName());
			} else if (arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) {
				team.removeEntry(p.getName());
			} else if (arena.getArenaState() == ArenaState.ENDING || arena.getArenaState() == ArenaState.RESTARTING) {
				team.removeEntry(p.getName());
			}

			players.setScoreboard(scoreboard);
		}
	}
}