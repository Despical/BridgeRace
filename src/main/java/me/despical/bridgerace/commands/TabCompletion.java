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

package me.despical.bridgerace.commands;

import me.despical.bridgerace.api.StatsStorage;
import me.despical.bridgerace.arena.Arena;
import me.despical.bridgerace.arena.ArenaRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class TabCompletion implements TabCompleter {

	public CommandHandler commandHandler;

	public TabCompletion(CommandHandler commandHandler) {
		this.commandHandler = commandHandler;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>(), commands = commandHandler.getSubCommands().stream().map(SubCommand::getName).collect(Collectors.toList());

		if (args.length == 1) {
			StringUtil.copyPartialMatches(args[0], commands, completions);
		}

		if (args.length == 2) {
			if (Stream.of("create", "help", "list", "reload", "randomjoin", "stop").anyMatch(args[0]::equalsIgnoreCase)) {
				return null;
			}

			if (args[0].equalsIgnoreCase("top")) {
				return Arrays.asList(StatsStorage.StatisticType.values()).stream().filter(StatsStorage.StatisticType::isPersistent).map(statistic -> statistic.name().toLowerCase(Locale.ENGLISH)).collect(Collectors.toList());
			}

			if (args[0].equalsIgnoreCase("stats")) {
				return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			}

			List<String> arenas = ArenaRegistry.getArenas().stream().map(Arena::getId).collect(Collectors.toList());

			StringUtil.copyPartialMatches(args[1], arenas, completions);
			Collections.sort(arenas);
			return arenas;
		}

		Collections.sort(completions);
		return completions;
	}
}