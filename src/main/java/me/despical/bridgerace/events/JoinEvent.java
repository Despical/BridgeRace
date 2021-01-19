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

package me.despical.bridgerace.events;

import me.despical.bridgerace.ConfigPreferences;
import me.despical.bridgerace.Main;
import me.despical.bridgerace.arena.ArenaRegistry;
import me.despical.commonsbox.miscellaneous.PlayerUtils;
import me.despical.commonsbox.serializer.InventorySerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Despical
 * <p>
 * Created at 19.12.2020
 */
public class JoinEvent implements Listener {

	private final Main plugin;

	public JoinEvent(Main plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		plugin.getUserManager().loadStatistics(plugin.getUserManager().getUser(event.getPlayer()));

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			ArenaRegistry.getArenas().get(ArenaRegistry.getBungeeArena()).teleportToLobby(event.getPlayer());
			return;
		}

		for (Player player : plugin.getServer().getOnlinePlayers()) {
			if (!ArenaRegistry.isInArena(player)) {
				continue;
			}

			PlayerUtils.hidePlayer(player, event.getPlayer(), plugin);
			PlayerUtils.hidePlayer(event.getPlayer(), player, plugin);
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, event.getPlayer());
		}
	}
}