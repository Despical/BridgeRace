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
import me.despical.bridgerace.arena.ArenaRegistry;
import me.despical.bridgerace.commands.SubCommand;
import me.despical.bridgerace.handlers.ChatManager;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.miscellaneous.MiscUtils;
import me.despical.commonsbox.serializer.LocationSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class CreateCommand extends SubCommand {

	public CreateCommand() {
		super("create");

		setPermission("br.admin.create");
	}

	@Override
	public String getPossibleArguments() {
		return "<ID>";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, ChatManager chatManager, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("Commands.Type-Arena-Name"));
			return;
		}

		Player player = (Player) sender;

		if (ArenaRegistry.getArena(args[0]) != null) {
			player.sendMessage(chatManager.getPrefix() + chatManager.colorRawMessage("&cArena with that ID already exists!"));
			player.sendMessage(chatManager.getPrefix() + chatManager.colorRawMessage("&cUsage: /br create <ID>"));
			return;
		}

		if (ConfigUtils.getConfig(plugin, "arenas").contains("instances." + args[0])) {
			player.sendMessage(chatManager.getPrefix() + chatManager.colorRawMessage("Instance/Arena already exists! Use another ID or delete it first!"));
		} else {
			createInstanceInConfig(args[0]);
			player.sendMessage(ChatColor.BOLD + "--------------------------------------------");
			MiscUtils.sendCenteredMessage(player, "&eInstance " + args[0] + " created!");
			player.sendMessage("");
			MiscUtils.sendCenteredMessage(player, "&eEdit this arena via &6/br edit " + args[0] + "&a!");
			player.sendMessage(ChatColor.BOLD + "--------------------------------------------");
		}
	}

	private void createInstanceInConfig(String id) {
		String path = "instances." + id + ".", loc = LocationSerializer.locationToString(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

		config.set(path + "endlocation", loc);
		config.set(path + "firstplayerlocation", loc);
		config.set(path + "secondplayerlocation", loc);
		config.set(path + "areamin", loc);
		config.set(path + "areamax", loc);
		config.set(path + "arenamin", loc);
		config.set(path + "arenamax", loc);
		config.set(path + "mapname", id);
		config.set(path + "signs", new ArrayList<>());
		config.set(path + "isdone", false);

		ConfigUtils.saveConfig(plugin, config, "arenas");

		Arena arena = new Arena(id);
		arena.setMapName(config.getString(path + "mapname"));
		arena.setEndLocation(LocationSerializer.locationFromString(config.getString(path + "endlocation")));
		arena.setFirstPlayerLocation(LocationSerializer.locationFromString(config.getString(path + "firstplayerlocation")));
		arena.setSecondPlayerLocation(LocationSerializer.locationFromString(config.getString(path + "secondplayerlocation")));
		arena.setReady(false);

		ArenaRegistry.registerArena(arena);
	}

	@Override
	public List<String> getTutorial() {
		return Collections.singletonList("Creates new arena with default settings");
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