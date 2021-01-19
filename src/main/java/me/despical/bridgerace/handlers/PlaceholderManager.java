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

package me.despical.bridgerace.handlers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.despical.bridgerace.Main;
import me.despical.bridgerace.api.StatsStorage;
import me.despical.bridgerace.arena.Arena;
import me.despical.bridgerace.arena.ArenaRegistry;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class PlaceholderManager extends PlaceholderExpansion {

	private final Main plugin = JavaPlugin.getPlugin(Main.class);

	@Override
	public boolean persist() {
		return true;
	}

	public String getIdentifier() {
		return "br";
	}

	public String getAuthor() {
		return plugin.getDescription().getVersion();
	}

	public String onPlaceholderRequest(Player player, String id) {
		if (player == null) {
			return null;
		}

		switch (id.toLowerCase()) {
			case "games_played":
				return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.GAMES_PLAYED));
			case "win_streak":
				return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.WIN_STREAK));
			case "wins":
				return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.WINS));
			case "loses":
				return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.LOSES));
			default:
				return handleArenaPlaceholderRequest(id);
		}
	}

	public String getVersion() {
		return "1.0.0";
	}

	private String handleArenaPlaceholderRequest(String id) {
		if (!id.contains(":")) {
			return null;
		}

		String[] data = id.split(":");
		Arena arena = ArenaRegistry.getArena(data[0]);

		if (arena == null) {
			return null;
		}

		switch (data[1].toLowerCase()) {
			case "players":
				return String.valueOf(arena.getPlayers().size());
			case "players_left":
				return String.valueOf(arena.getPlayersLeft().size());
			case "timer":
				return String.valueOf(arena.getTimer());
			case "state":
				return String.valueOf(arena.getArenaState());
			case "state_pretty":
				return arena.getArenaState().getFormattedName();
			case "mapname":
				return arena.getMapName();
			default:
				return null;
		}
	}
}