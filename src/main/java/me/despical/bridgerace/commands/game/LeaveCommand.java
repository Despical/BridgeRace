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
import me.despical.bridgerace.commands.SubCommand;
import me.despical.bridgerace.handlers.ChatManager;
import me.despical.bridgerace.utils.Debugger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class LeaveCommand extends SubCommand {

	public LeaveCommand() {
		super("leave");
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
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_LEAVE_COMMAND)) {
			Player player = (Player) sender;

			if (!checkIsInGameInstance(player)) {
				return;
			}

			player.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("Commands.Teleported-To-The-Lobby", player));

			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
				plugin.getBungeeManager().connectToHub(player);
				Debugger.debug("{0} was teleported to the Hub server", player.getName());
				return;
			}

			Arena arena = ArenaRegistry.getArena(player);

			ArenaManager.leaveAttempt(player, arena);
			Debugger.debug("{0} has left the arena {1}! Teleported to end location", player.getName(), arena.getId());
		}
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