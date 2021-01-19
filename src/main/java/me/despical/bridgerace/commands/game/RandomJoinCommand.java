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

package me.despical.bridgerace.commands.game;

import me.despical.bridgerace.ConfigPreferences;
import me.despical.bridgerace.arena.Arena;
import me.despical.bridgerace.arena.ArenaManager;
import me.despical.bridgerace.arena.ArenaRegistry;
import me.despical.bridgerace.arena.ArenaState;
import me.despical.bridgerace.commands.SubCommand;
import me.despical.bridgerace.handlers.ChatManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class RandomJoinCommand extends SubCommand {

	public RandomJoinCommand() {
		super("randomjoin");
	}

	@Override
	public String getPossibleArguments() {
		return null;
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, ChatManager chatManager, String[] args) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			return;
		}

		HashMap<Arena, Integer> arenas = new HashMap<>();

		for (Arena arena : ArenaRegistry.getArenas()) {
			if (arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS && arena.getPlayers().size() < 2) {
				arenas.put(arena, arena.getPlayers().size());
			}
		}

		if (!arenas.isEmpty()) {
			Stream<Map.Entry<Arena, Integer>> sorted = arenas.entrySet().stream().sorted(Map.Entry.comparingByValue());
			Arena arena = sorted.findFirst().get().getKey();

			if (arena != null) {
				ArenaManager.joinAttempt((Player) sender, arena);
				return;
			}
		}

		for (Arena arena : ArenaRegistry.getArenas()) {
			if (arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS && arena.getPlayers().size() < 2) {
				ArenaManager.joinAttempt((Player) sender, arena);
				return;
			}
		}

		sender.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("Commands.No-Free-Arenas"));
	}

	@Override
	public List<String> getTutorial() {
		return null;
	}

	@Override
	public CommandType getType() {
		return CommandType.HIDDEN;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}
}