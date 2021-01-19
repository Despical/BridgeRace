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

package me.despical.bridgerace.commands.admin.arena;

import me.despical.bridgerace.arena.Arena;
import me.despical.bridgerace.arena.ArenaManager;
import me.despical.bridgerace.arena.ArenaRegistry;
import me.despical.bridgerace.arena.ArenaState;
import me.despical.bridgerace.commands.SubCommand;
import me.despical.bridgerace.handlers.ChatManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class StopCommand extends SubCommand {

	public StopCommand() {
		super("stop");

		setPermission("br.admin.stop");
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
		if (!checkIsInGameInstance((Player) sender)) {
			return;
		}

		Arena arena = ArenaRegistry.getArena((Player) sender);

		if (arena.getArenaState() == ArenaState.ENDING) {
			sender.sendMessage(chatManager.colorMessage("Commands.Already-Ending"));
			return;
		}

		ArenaManager.stopGame(true, arena);
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Stop the arena you're in", "You must be in target arena!");
	}

	@Override
	public CommandType getType() {
		return CommandType.GENERIC;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}
}