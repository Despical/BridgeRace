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

import me.despical.bridgerace.Main;
import me.despical.bridgerace.arena.ArenaRegistry;
import me.despical.commonsbox.compat.VersionResolver;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * @author Despical
 * <p>
 * Created at 19.12.2020
 */
public class CraftEvents implements Listener {

	public CraftEvents(Main plugin) {
		if (VersionResolver.isCurrentHigher(VersionResolver.ServerVersion.v1_9_R2)) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onItemSwap(PlayerSwapHandItemsEvent e) {
		if (ArenaRegistry.isInArena(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
}