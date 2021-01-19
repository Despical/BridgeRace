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
import me.despical.bridgerace.api.StatsStorage;
import me.despical.bridgerace.arena.Arena;
import me.despical.bridgerace.arena.ArenaManager;
import me.despical.bridgerace.arena.ArenaRegistry;
import me.despical.bridgerace.arena.ArenaState;
import me.despical.bridgerace.handlers.rewards.Reward;
import me.despical.bridgerace.user.User;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.serializer.LocationSerializer;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * @author Despical
 * <p>
 * Created at 20.12.2020
 */
public class StatisticEvents implements Listener {

	private final Main plugin;

	public StatisticEvents(Main plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (!ArenaRegistry.isInArena(event.getPlayer())) {
			return;
		}

		if (event.getBlock().getType() == plugin.getConfigPreferences().getGameMaterial().getType()) {
			User user = plugin.getUserManager().getUser(event.getPlayer());
			user.addStat(StatsStorage.StatisticType.LOCAL_BROKEN_BLOCKS, 1);
		}
	}

	@EventHandler
	public void onBuild(BlockPlaceEvent event) {
		if (!ArenaRegistry.isInArena(event.getPlayer())) {
			return;
		}

		if (event.getBlock().getType() == plugin.getConfigPreferences().getGameMaterial().getType()) {
			User user = plugin.getUserManager().getUser(event.getPlayer());
			user.addStat(StatsStorage.StatisticType.LOCAL_PLACED_BLOCKS, 1);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onVoid(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player victim = (Player) e.getEntity();
		Arena arena = ArenaRegistry.getArena(victim);

		if (arena == null) {
			return;
		}

		e.setCancelled(true);

		if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
			if (arena.getPlayers().get(0).equals(victim)) {
				victim.teleport(arena.getFirstPlayerLocation());
			} else if (arena.getPlayers().get(1).equals(victim)) {
				victim.teleport(arena.getSecondPlayerLocation());
			}

			if (arena.getArenaState() == ArenaState.IN_GAME) {
				User user = plugin.getUserManager().getUser(victim);
				user.addStat(StatsStorage.StatisticType.LOCAL_FELL_INTO_VOID, 1);
			}
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (!ArenaRegistry.isInArena(player)) {
			return;
		}

		Arena arena = ArenaRegistry.getArena(player);

		if (arena.getArenaState() != ArenaState.IN_GAME) {
			return;
		}

		if (isInArea(player)) {
			User user = plugin.getUserManager().getUser(player);
			user.setStat(StatsStorage.StatisticType.LOCAL_WON, 1);
			user.addStat(StatsStorage.StatisticType.WIN_STREAK, 1);
			plugin.getRewardsFactory().performReward(player, Reward.RewardType.WIN);

			User opponent = plugin.getUserManager().getUser(Bukkit.getPlayer(arena.getScoreboardManager().getOpponent(user)));
			opponent.setStat(StatsStorage.StatisticType.LOCAL_WON, -1);
			opponent.setStat(StatsStorage.StatisticType.WIN_STREAK, 0);
			plugin.getRewardsFactory().performReward(player, Reward.RewardType.LOSE);

			ArenaManager.stopGame(false, arena);
		}
	}

	private boolean isInArea(Player player) {
		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
		Arena arena = ArenaRegistry.getArena(player);
		Location first = LocationSerializer.locationFromString(config.getString("instances." + arena.getId() + ".areamin")),
			second = LocationSerializer.locationFromString(config.getString("instances." + arena.getId() + ".areamax")), origin = player.getLocation();

		return new IntRange(first.getX(), second.getX()).containsDouble(origin.getX())
			&& new IntRange(first.getY(), second.getY()).containsDouble(origin.getY())
			&& new IntRange(first.getZ(), second.getZ()).containsDouble(origin.getZ());
	}
}