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

package me.despical.bridgerace.arena;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.bridgerace.ConfigPreferences;
import me.despical.bridgerace.Main;
import me.despical.bridgerace.api.StatsStorage;
import me.despical.bridgerace.api.events.game.BRGameJoinAttemptEvent;
import me.despical.bridgerace.api.events.game.BRGameLeaveAttemptEvent;
import me.despical.bridgerace.api.events.game.BRGameStopEvent;
import me.despical.bridgerace.handlers.ChatManager;
import me.despical.bridgerace.handlers.PermissionManager;
import me.despical.bridgerace.handlers.items.SpecialItemManager;
import me.despical.bridgerace.handlers.rewards.Reward;
import me.despical.bridgerace.user.User;
import me.despical.bridgerace.utils.Debugger;
import me.despical.commonsbox.compat.Titles;
import me.despical.commonsbox.compat.VersionResolver;
import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.miscellaneous.AttributeUtils;
import me.despical.commonsbox.miscellaneous.MiscUtils;
import me.despical.commonsbox.miscellaneous.PlayerUtils;
import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.commonsbox.string.StringFormatUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class ArenaManager {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	private ArenaManager() {
	}

	/**
	 * Attempts player to join arena.
	 * Calls BRGameJoinAttemptEvent.
	 * Can be cancelled only via above-mentioned event
	 *
	 * @param player player to join
	 * @param arena target arena
	 * @see   BRGameJoinAttemptEvent
	 */
	public static void joinAttempt(Player player, Arena arena) {
		Debugger.debug("[{0}] Initial join attempt for {1}", arena.getId(), player.getName());
		long start = System.currentTimeMillis();
		BRGameJoinAttemptEvent gameJoinAttemptEvent = new BRGameJoinAttemptEvent(player, arena);
		Bukkit.getPluginManager().callEvent(gameJoinAttemptEvent);

		if (!arena.isReady()) {
			player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Arena-Not-Configured"));
			return;
		}

		if (gameJoinAttemptEvent.isCancelled()) {
			player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Join-Cancelled-Via-API"));
			return;
		}

		if (ArenaRegistry.isInArena(player)) {
			player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Already-Playing"));
			return;
		}

		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			if (!player.hasPermission(PermissionManager.getJoinPerm().replace("<arena>", "*")) || !player.hasPermission(PermissionManager.getJoinPerm().replace("<arena>", arena.getId()))) {
				player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Join-No-Permission").replace("%permission%", PermissionManager.getJoinPerm().replace("<arena>", arena.getId())));
				return;
			}
		}

		if (arena.getArenaState() == ArenaState.RESTARTING) {
			return;
		}

		if (arena.getPlayers().size() == 2) {
			player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Full-Game"));
			return;
		}

		Debugger.debug("[{0}] Checked join attempt for {1} initialized", arena.getId(), player.getName());
		User user = plugin.getUserManager().getUser(player);

		arena.getScoreboardManager().createScoreboard(user);

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.saveInventoryToFile(plugin, player);
		}

		arena.addPlayer(player);

		player.setLevel(0);
		player.setExp(1);
		AttributeUtils.healPlayer(player);
		player.setFoodLevel(20);
		player.getInventory().setArmorContents(null);
		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);

		Arrays.stream(StatsStorage.StatisticType.values()).filter(stat -> !stat.isPersistent()).forEach(stat -> user.setStat(stat, 0));

		if (arena.getArenaState() == ArenaState.IN_GAME || arena.getArenaState() == ArenaState.ENDING) {
			arena.teleportToLobby(player);
			player.sendMessage(plugin.getChatManager().colorMessage("In-Game.You-Are-Spectator"));
			player.getInventory().clear();
			player.getInventory().setItem(0, new ItemBuilder(XMaterial.COMPASS.parseItem()).name(plugin.getChatManager().colorMessage("In-Game.Spectator.Spectator-Item-Name")).build());
			player.getInventory().setItem(4, new ItemBuilder(XMaterial.COMPARATOR.parseItem()).name(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Item-Name")).build());
			player.getInventory().setItem(8, SpecialItemManager.getSpecialItem("Leave").getItemStack());
			player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
			ArenaUtils.hidePlayer(player, arena);
			user.setSpectator(true);

			if (VersionResolver.isCurrentEqualOrHigher(VersionResolver.ServerVersion.v1_9_R1)) {
				player.setCollidable(false);
			}

			player.setAllowFlight(true);
			player.setFlying(true);

			for (Player spectator : arena.getPlayers()) {
				if (plugin.getUserManager().getUser(spectator).isSpectator()) {
					player.hidePlayer(plugin, spectator);
				} else {
					player.showPlayer(plugin, spectator);
				}
			}

			ArenaUtils.hidePlayersOutsideTheGame(player, arena);
			Debugger.debug("[{0}] Join attempt as spectator finished for {1} took {2} ms.", arena.getId(), player.getName(), System.currentTimeMillis() - start);

		}

		arena.teleportToLobby(player);
		player.setFlying(false);
		player.setAllowFlight(false);
		arena.doBarAction(Arena.BarAction.ADD, player);

		if (!plugin.getUserManager().getUser(player).isSpectator()) {
			plugin.getChatManager().broadcastAction(arena, player, ChatManager.ActionType.JOIN);
		}

		if (arena.getArenaState() == ArenaState.STARTING || arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS) {
			player.getInventory().setItem(SpecialItemManager.getSpecialItem("Leave").getSlot(), SpecialItemManager.getSpecialItem("Leave").getItemStack());
		}

		player.updateInventory();

		arena.getPlayers().forEach(arenaPlayer -> ArenaUtils.showPlayer(arenaPlayer, arena));
		arena.showPlayers();
		plugin.getSignManager().updateSigns();

		Debugger.debug("[{0}] Join attempt as player for {1} took {2} ms.", arena.getId(), player.getName(), System.currentTimeMillis() - start);
	}

	/**
	 * Attempts player to leave arena.
	 * Calls BRGameLeaveAttemptEvent event.
	 *
	 * @param player player to join
	 * @param arena target arena
	 * @see  BRGameLeaveAttemptEvent
	 */
	public static void leaveAttempt(Player player, Arena arena) {
		Debugger.debug("[{0}] Initial leave attempt for {1}", arena.getId(), player.getName());
		long start = System.currentTimeMillis();
		BRGameLeaveAttemptEvent event = new BRGameLeaveAttemptEvent(player, arena);
		Bukkit.getPluginManager().callEvent(event);
		User user = plugin.getUserManager().getUser(player);

		arena.getScoreboardManager().removeScoreboard(user);

		if (arena.getArenaState() == ArenaState.IN_GAME && !user.isSpectator()) {
			if (arena.getPlayersLeft().size() - 1 == 1) {
				stopGame(false, arena);
				return;
			}
		}

		player.setFlySpeed(0.1f);
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		arena.removePlayer(player);
		arena.teleportToEndLocation(player);

		if (!user.isSpectator()) {
			plugin.getChatManager().broadcastAction(arena, player, ChatManager.ActionType.LEAVE);
		}

		user.setSpectator(false);

		if (VersionResolver.isCurrentEqualOrHigher(VersionResolver.ServerVersion.v1_9_R1)) {
			player.setCollidable(true);
			player.setGlowing(false);
		}

		user.removeScoreboard();
		arena.doBarAction(Arena.BarAction.REMOVE, player);
		AttributeUtils.healPlayer(player);
		player.setFoodLevel(20);
		player.setLevel(0);
		player.setExp(0);
		player.setFlying(false);
		player.setAllowFlight(false);
		player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
		player.setWalkSpeed(0.2f);
		player.setFireTicks(0);

		if (arena.getArenaState() != ArenaState.WAITING_FOR_PLAYERS && arena.getArenaState() != ArenaState.STARTING && arena.getPlayers().size() == 0) {
			arena.setArenaState(ArenaState.ENDING);
			arena.setTimer(0);
		}

		player.setGameMode(GameMode.SURVIVAL);

		for (Player players : plugin.getServer().getOnlinePlayers()) {
			if (!ArenaRegistry.isInArena(players)) {
				PlayerUtils.showPlayer(players, player, plugin);
			}

			PlayerUtils.showPlayer(player, players, plugin);
		}

		arena.teleportToEndLocation(player);

		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED) && plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		}

		plugin.getUserManager().saveAllStatistic(user);
		plugin.getSignManager().updateSigns();
		Debugger.debug("[{0}] Game leave finished for {1} took {2} ms.", arena.getId(), player.getName(), System.currentTimeMillis() - start);
	}

	/**
	 * Stops current arena.
	 * Calls BRGameStopEvent event
	 *
	 * @param quickStop should arena be stopped immediately? (use only in important cases)
	 * @param arena target arena
	 * @see   BRGameStopEvent
	 */
	public static void stopGame(boolean quickStop, Arena arena) {
		Debugger.debug("[{0}] Stop game event initialized with quickStop {1}", arena.getId(), quickStop);
		FileConfiguration config = ConfigUtils.getConfig(plugin, "messages");
		long start = System.currentTimeMillis();
		BRGameStopEvent gameStopEvent = new BRGameStopEvent(arena);

		Bukkit.getPluginManager().callEvent(gameStopEvent);
		arena.setArenaState(ArenaState.ENDING);

		if (quickStop) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> arena.setArenaState(ArenaState.ENDING), 20L * 2);
			arena.broadcastMessage(plugin.getChatManager().colorMessage("In-Game.Messages.Admin-Messages.Stopped-Game"));
		} else {
			Bukkit.getScheduler().runTaskLater(plugin, () -> arena.setArenaState(ArenaState.ENDING), 20L * 10);
		}

		arena.getScoreboardManager().stopAllScoreboards();

		for (Player player : arena.getPlayers()) {
			User user = plugin.getUserManager().getUser(player);

			if (user.getStat(StatsStorage.StatisticType.LOCAL_WON) == 1) {
				user.addStat(StatsStorage.StatisticType.WINS, 1);
				user.addStat(StatsStorage.StatisticType.WIN_STREAK, 1);

				Titles.sendTitle(player, 5, 40, 5, plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Titles.Win"), plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Win").replace("%player%", getWinner(arena).getName()));

				plugin.getRewardsFactory().performReward(player, Reward.RewardType.WIN);
			} else if (user.getStat(StatsStorage.StatisticType.LOCAL_WON) == -1) {
				user.addStat(StatsStorage.StatisticType.LOSES, 1);
				user.setStat(StatsStorage.StatisticType.WIN_STREAK, 0);

				Titles.sendTitle(player, 5, 40, 5, plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose"), plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Lose").replace("%player%", getWinner(arena).getName()));

				plugin.getRewardsFactory().performReward(player, Reward.RewardType.LOSE);
			} else if (user.isSpectator()) {
				Titles.sendTitle(player, 5, 40, 5, plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose"), plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Lose").replace("%player%", getWinner(arena).getName()));
			}

			player.getInventory().clear();
			player.getInventory().setItem(SpecialItemManager.getSpecialItem("Leave").getSlot(), SpecialItemManager.getSpecialItem("Leave").getItemStack());

			if (!quickStop) {
				config.getStringList("In-Game.Messages.Game-End-Messages.Summary-Message").forEach(msg -> MiscUtils.sendCenteredMessage(player, formatSummaryPlaceholders(msg, arena, player)));
			}

			plugin.getUserManager().saveAllStatistic(user);
			user.removeScoreboard();

			if (!quickStop && plugin.getConfig().getBoolean("Firework-When-Game-Ends", true)) {
				new BukkitRunnable() {
					int i = 0;

					public void run() {
						if (i == 4 || !arena.getPlayers().contains(player) || arena.getArenaState() == ArenaState.RESTARTING) {
							this.cancel();
						}

						MiscUtils.spawnRandomFirework(player.getLocation());
						i++;
					}
				}.runTaskTimer(plugin, 30, 30);
			}
		}

		Debugger.debug("[{0}] Stop game event finished took {1} ms", arena.getId(), System.currentTimeMillis() - start);
	}

	private static String formatSummaryPlaceholders(String msg, Arena arena, Player player) {
		String formatted = msg;
		Player winner = getWinner(arena);
		Player loser = arena.getPlayers().stream().filter(p -> StatsStorage.getUserStats(p, StatsStorage.StatisticType.LOCAL_WON) == -1).findFirst().orElse(null);

		formatted = StringUtils.replace(formatted, "%duration%", StringFormatUtils.formatIntoMMSS(plugin.getConfig().getInt("Classic-Gameplay-Time", 300) - arena.getTimer()));

		formatted = StringUtils.replace(formatted, "%winner%", winner != null ? winner.getName() : "");
		formatted = StringUtils.replace(formatted, "%winner_placed_blocks%", Integer.toString(StatsStorage.getUserStats(winner, StatsStorage.StatisticType.LOCAL_PLACED_BLOCKS)));
		formatted = StringUtils.replace(formatted, "%winner_broken_blocks%", Integer.toString(StatsStorage.getUserStats(winner, StatsStorage.StatisticType.LOCAL_BROKEN_BLOCKS)));
		formatted = StringUtils.replace(formatted, "%winner_fell_into_void%", Integer.toString(StatsStorage.getUserStats(winner, StatsStorage.StatisticType.LOCAL_FELL_INTO_VOID)));
		formatted = StringUtils.replace(formatted, "%winner_average_placed_blocks%", Integer.toString(StatsStorage.getUserStats(winner, StatsStorage.StatisticType.LOCAL_PLACED_BLOCKS) / plugin.getConfig().getInt("Classic-Gameplay-Time", 300) - arena.getTimer()));

		formatted = StringUtils.replace(formatted, "%loser%", loser != null ? loser.getName() : "");
		formatted = StringUtils.replace(formatted, "%loser_placed_blocks%", Integer.toString(StatsStorage.getUserStats(loser, StatsStorage.StatisticType.LOCAL_PLACED_BLOCKS)));
		formatted = StringUtils.replace(formatted, "%loser_broken_blocks%", Integer.toString(StatsStorage.getUserStats(loser, StatsStorage.StatisticType.LOCAL_BROKEN_BLOCKS)));
		formatted = StringUtils.replace(formatted, "%loser_fell_into_void%", Integer.toString(StatsStorage.getUserStats(loser, StatsStorage.StatisticType.LOCAL_FELL_INTO_VOID)));
		formatted = StringUtils.replace(formatted, "%loser_average_placed_blocks%", Integer.toString(StatsStorage.getUserStats(loser, StatsStorage.StatisticType.LOCAL_PLACED_BLOCKS) / (plugin.getConfig().getInt("Classic-Gameplay-Time", 300) - arena.getTimer())));

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			formatted = PlaceholderAPI.setPlaceholders(player, formatted);
		}

		return formatted;
	}

	private static Player getWinner(Arena arena) {
		return arena.getPlayers().stream().filter(p -> StatsStorage.getUserStats(p, StatsStorage.StatisticType.LOCAL_WON) == 1).findFirst().orElse(null);
	}
}