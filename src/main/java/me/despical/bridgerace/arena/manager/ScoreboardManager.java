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

package me.despical.bridgerace.arena.manager;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.bridgerace.Main;
import me.despical.bridgerace.api.StatsStorage;
import me.despical.bridgerace.arena.Arena;
import me.despical.bridgerace.arena.ArenaState;
import me.despical.bridgerace.user.User;
import me.despical.commonsbox.scoreboard.ScoreboardLib;
import me.despical.commonsbox.scoreboard.common.EntryBuilder;
import me.despical.commonsbox.scoreboard.type.Entry;
import me.despical.commonsbox.scoreboard.type.Scoreboard;
import me.despical.commonsbox.scoreboard.type.ScoreboardHandler;
import me.despical.commonsbox.string.StringFormatUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class ScoreboardManager {

	private final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final List<Scoreboard> scoreboards = new ArrayList<>();
	private final Arena arena;

	public ScoreboardManager(Arena arena) {
		this.arena = arena;
	}

	/**
	 * Creates arena scoreboard for target user
	 *
	 * @param user user that represents game player
	 * @see User
	 */
	public void createScoreboard(User user) {
		Scoreboard scoreboard = ScoreboardLib.createScoreboard(user.getPlayer()).setHandler(new ScoreboardHandler() {

			@Override
			public String getTitle(Player player) {
				return plugin.getChatManager().colorMessage("Scoreboard.Title");
			}

			@Override
			public List<Entry> getEntries(Player player) {
				return formatScoreboard(user);
			}
		});

		scoreboard.activate();
		scoreboards.add(scoreboard);
	}

	/**
	 * Removes scoreboard of user
	 *
	 * @param user user that represents game player
	 * @see   User
	 */
	public void removeScoreboard(User user) {
		for (Scoreboard board : scoreboards) {
			if (board.getHolder().equals(user.getPlayer())) {
				scoreboards.remove(board);
				board.deactivate();
				return;
			}
		}
	}

	/**
	 * Forces all scoreboards to deactivate.
	 */
	public void stopAllScoreboards() {
		scoreboards.forEach(Scoreboard::deactivate);
		scoreboards.clear();
	}

	private List<Entry> formatScoreboard(User user) {
		EntryBuilder builder = new EntryBuilder();
		List<String> lines;

		if (arena.getArenaState() == ArenaState.IN_GAME) {
			lines = plugin.getChatManager().getStringList("Scoreboard.Content.Playing");
		} else {
			if (arena.getArenaState() == ArenaState.ENDING) {
				lines = plugin.getChatManager().getStringList("Scoreboard.Content.Playing");
			} else {
				lines = plugin.getChatManager().getStringList("Scoreboard.Content." + arena.getArenaState().getFormattedName());
			}
		}

		for (String line : lines) {
			builder.next(formatScoreboardLine(line, user));
		}

		return builder.build();
	}

	private String formatScoreboardLine(String line, User user) {
		String formattedLine = line;

		formattedLine = StringUtils.replace(formattedLine, "%time%", String.valueOf(arena.getTimer()));
		formattedLine = StringUtils.replace(formattedLine, "%duration%", StringFormatUtils.formatIntoMMSS(plugin.getConfig().getInt("Classic-Gameplay-Time", 300) - arena.getTimer()));
		formattedLine = StringUtils.replace(formattedLine, "%formatted_time%", StringFormatUtils.formatIntoMMSS(arena.getTimer()));
		formattedLine = StringUtils.replace(formattedLine, "%mapname%", arena.getMapName());
		formattedLine = StringUtils.replace(formattedLine, "%players%", String.valueOf(arena.getPlayers().size()));
		formattedLine = StringUtils.replace(formattedLine, "%opponent%", getOpponent(user));
		formattedLine = StringUtils.replace(formattedLine, "%win_streak%", String.valueOf(user.getStat(StatsStorage.StatisticType.WIN_STREAK)));

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			formattedLine = PlaceholderAPI.setPlaceholders(user.getPlayer(), formattedLine);
		}

		return formattedLine;
	}

	public String getOpponent(User user) {
		Arena arena = user.getArena();
		Player[] players = arena.getPlayersLeft().toArray(new Player[0]);

		if (arena.getPlayersLeft().size() < 2) {
			return "";
		}

		if (players[0].equals(user.getPlayer())) {
			return players[1] != null ? players[1].getName() : "";
		} else if (players[1].equals(user.getPlayer())) {
			return players[0] != null ? players[0].getName() : "";
		}

		return "";
	}
}