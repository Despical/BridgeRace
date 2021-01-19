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

import me.despical.bridgerace.Main;
import me.despical.bridgerace.arena.ArenaRegistry;
import me.despical.bridgerace.handlers.ChatManager;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public abstract class SubCommand {

	protected final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final String name;
	private String permission;
	private final String[] aliases;

	public SubCommand(String name) {
		this(name, new String[0]);
	}

	public SubCommand(String name, String... aliases) {
		this.name = name;
		this.aliases = aliases;
	}

	public String getName() {
		return name;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public final boolean hasPermission(CommandSender sender) {
		if (permission == null) return true;
		return sender.hasPermission(permission);
	}

	public final boolean checkIsInGameInstance(Player player) {
		if (!ArenaRegistry.isInArena(player)) {
			player.sendMessage(plugin.getChatManager().colorMessage("Commands.Not-Playing", player));
			return false;
		}

		return true;
	}

	public abstract String getPossibleArguments();

	public abstract int getMinimumArguments();

	public abstract void execute(CommandSender sender, ChatManager chatManager, String[] args) throws CommandException;

	public abstract List<String> getTutorial();

	public abstract CommandType getType();

	public abstract SenderType getSenderType();

	public enum CommandType {
		GENERIC, HIDDEN
	}

	public enum SenderType {
		PLAYER, BOTH
	}

	public final boolean isValidTrigger(String name) {
		if (this.name.equalsIgnoreCase(name)) {
			return true;
		}

		if (aliases != null) {
			for (String alias : aliases) {
				if (alias.equalsIgnoreCase(name)) {
					return true;
				}
			}
		}

		return false;
	}
}