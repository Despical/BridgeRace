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

package me.despical.bridgerace;

import me.despical.bridgerace.api.StatsStorage;
import me.despical.bridgerace.arena.Arena;
import me.despical.bridgerace.arena.ArenaRegistry;
import me.despical.bridgerace.arena.ArenaUtils;
import me.despical.bridgerace.commands.CommandHandler;
import me.despical.bridgerace.events.*;
import me.despical.bridgerace.events.spectator.SpectatorEvents;
import me.despical.bridgerace.events.spectator.SpectatorItemEvents;
import me.despical.bridgerace.handlers.BungeeManager;
import me.despical.bridgerace.handlers.ChatManager;
import me.despical.bridgerace.handlers.PermissionManager;
import me.despical.bridgerace.handlers.PlaceholderManager;
import me.despical.bridgerace.handlers.items.SpecialItem;
import me.despical.bridgerace.handlers.language.LanguageManager;
import me.despical.bridgerace.handlers.rewards.RewardsFactory;
import me.despical.bridgerace.handlers.sign.SignManager;
import me.despical.bridgerace.user.User;
import me.despical.bridgerace.user.UserManager;
import me.despical.bridgerace.user.data.MysqlManager;
import me.despical.bridgerace.utils.*;
import me.despical.commonsbox.compat.VersionResolver;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.database.MysqlDatabase;
import me.despical.commonsbox.miscellaneous.AttributeUtils;
import me.despical.commonsbox.scoreboard.ScoreboardLib;
import me.despical.commonsbox.serializer.InventorySerializer;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class Main extends JavaPlugin {

    private ExceptionLogHandler exceptionLogHandler;
    private boolean forceDisable;
    private ChatManager chatManager;
    private ConfigPreferences configPreferences;
    private UserManager userManager;
    private MysqlDatabase database;
	private BungeeManager bungeeManager;
	private RewardsFactory rewardsFactory;
	private SignManager signManager;
	private CuboidSelector cuboidSelector;
	private CommandHandler commandHandler;
	private LanguageManager languageManager;

    @Override
    public void onEnable() {
        if (!validateIfPluginShouldStart()) {
			forceDisable = true;
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		exceptionLogHandler = new ExceptionLogHandler(this);
		saveDefaultConfig();

		Debugger.setEnabled(getDescription().getVersion().contains("d") || getConfig().getBoolean("Debug-Messages"));
		Debugger.debug("Initialization start");

		if (getConfig().getBoolean("Developer-Mode")) {
			Debugger.deepDebug(true);
			Debugger.debug("Deep debug enabled");
			getConfig().getStringList("Listenable-Performances").forEach(Debugger::monitorPerformance);
		}

		long start = System.currentTimeMillis();
		configPreferences = new ConfigPreferences(this);

		setupFiles();
		initializeClasses();
//		checkUpdate();

		Debugger.debug("Initialization finished took {0} ms", System.currentTimeMillis() - start);

		if (configPreferences.getOption(ConfigPreferences.Option.NAMETAGS_HIDDEN)) {
			Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () ->
				Bukkit.getOnlinePlayers().forEach(ArenaUtils::updateNameTagsVisibility), 60, 140);
		}
    }

	@Override
	public void onDisable() {
		if (forceDisable) {
			return;
		}

		Debugger.debug("System disable initialized");
		long start = System.currentTimeMillis();

		Bukkit.getLogger().removeHandler(exceptionLogHandler);
		saveAllUserStatistics();

		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			database.shutdownConnPool();
		}

		for (Arena arena : ArenaRegistry.getArenas()) {
			arena.getScoreboardManager().stopAllScoreboards();

			for (Player player : arena.getPlayers()) {
				arena.doBarAction(Arena.BarAction.REMOVE, player);
				arena.teleportToEndLocation(player);
				player.setFlySpeed(0.1f);
				player.setWalkSpeed(0.2f);

				if (configPreferences.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(this, player);
				} else {
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
				}

				if (configPreferences.getOption(ConfigPreferences.Option.ALLOW_PVP)) {
					AttributeUtils.resetAttackCooldown(player);
				}
			}
		}

		Debugger.debug("System disable finished took {0} ms", System.currentTimeMillis() - start);
	}

    private boolean validateIfPluginShouldStart() {
        if (VersionResolver.isCurrentLower(VersionResolver.ServerVersion.v1_8_R1)) {
            Debugger.sendConsoleMessage("&cYour server version is not supported by Bridge Race!");
            Debugger.sendConsoleMessage("&cMaybe you consider changing your server version?");
            return false;
        } try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (ClassNotFoundException e) {
            Debugger.sendConsoleMessage("&cYour server software is not supported by Bridge Race!");
            Debugger.sendConsoleMessage("&cWe support only Spigot and forks! Shutting off...");
            return false;
        }

        return true;
    }

	private void initializeClasses() {
		ScoreboardLib.setPluginInstance(this);
		chatManager = new ChatManager(this);

		if (configPreferences.getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			bungeeManager = new BungeeManager(this);
		}

		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			FileConfiguration config = ConfigUtils.getConfig(this, "mysql");
			database = new MysqlDatabase(config.getString("user"), config.getString("password"), config.getString("address"));
		}

		languageManager = new LanguageManager(this);
		userManager = new UserManager(this);
		SpecialItem.loadAll();
		PermissionManager.init();

		new JoinEvent(this);
		new QuitEvent(this);
		new LobbyEvent(this);
		new CraftEvents(this);
		new ChatEvents(this);
		new Events(this);
		new StatisticEvents(this);
		new SpectatorEvents(this);
		new SpectatorItemEvents(this);

		signManager = new SignManager(this);
		ArenaRegistry.registerArenas();
		signManager.loadSigns();
		signManager.updateSigns();
		rewardsFactory = new RewardsFactory(this);
		commandHandler = new CommandHandler(this);
		cuboidSelector = new CuboidSelector(this);

		registerSoftDependenciesAndServices();
	}

	private void registerSoftDependenciesAndServices() {
		Debugger.debug("Hooking into soft dependencies");
		long start = System.currentTimeMillis();

		startPluginMetrics();

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			Debugger.debug("Hooking into PlaceholderAPI");
			new PlaceholderManager().register();
		}

		Debugger.debug("Hooked into soft dependencies took {0} ms", System.currentTimeMillis() - start);
	}

	private void startPluginMetrics() {
		Metrics metrics = new Metrics(this, 9694);

		if (!metrics.isEnabled()) {
			return;
		}

		metrics.addCustomChart(new Metrics.SimplePie("database_enabled", () -> String.valueOf(configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED))));
		metrics.addCustomChart(new Metrics.SimplePie("bungeecord_hooked", () -> String.valueOf(configPreferences.getOption(ConfigPreferences.Option.BUNGEE_ENABLED))));
		metrics.addCustomChart(new Metrics.SimplePie("locale_used", () -> languageManager.getPluginLocale().getPrefix()));
		metrics.addCustomChart(new Metrics.SimplePie("update_notifier", () -> {
			if (getConfig().getBoolean("Update-Notifier.Enabled", true)) {
				return getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true) ? "Enabled with beta notifier" : "Enabled";
			}

			return getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true) ? "Beta notifier only" : "Disabled";
		}));
	}

	private void checkUpdate() {
		if (!getConfig().getBoolean("Update-Notifier.Enabled", true)) {
			return;
		}

		UpdateChecker.init(this, 0).requestUpdateCheck().whenComplete((result, exception) -> {
			if (!result.requiresUpdate()) {
				return;
			}

			if (result.getNewestVersion().contains("b")) {
				if (getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true)) {
					Debugger.sendConsoleMessage("[Bridge Race] Found a new beta version available: v" + result.getNewestVersion());
					Debugger.sendConsoleMessage("[Bridge Race] Download it on SpigotMC.");
				}

				return;
			}

			Debugger.sendConsoleMessage("[Bridge Race] Found a new version available: v" + result.getNewestVersion());
			Debugger.sendConsoleMessage("[Bridge Race] Download it SpigotMC.");
		});
	}

	private void setupFiles() {
		for (String fileName : Arrays.asList("arenas", "bungee", "rewards", "stats", "items", "mysql", "messages")) {
			File file = new File(getDataFolder() + File.separator + fileName + ".yml");

			if (!file.exists()) {
				saveResource(fileName + ".yml", false);
			}
		}
	}

	private void saveAllUserStatistics() {
		for (Player player : getServer().getOnlinePlayers()) {
			User user = userManager.getUser(player);

			if (userManager.getDatabase() instanceof MysqlManager) {
				StringBuilder update = new StringBuilder(" SET ");

				for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;
					if (update.toString().equalsIgnoreCase(" SET ")) {
						update.append(stat.getName()).append("'='").append(user.getStat(stat));
					}

					update.append(", ").append(stat.getName()).append("'='").append(user.getStat(stat));
				}

				String finalUpdate = update.toString();
				((MysqlManager) userManager.getDatabase()).getDatabase().executeUpdate("UPDATE " + ((MysqlManager) getUserManager().getDatabase()).getTableName() + finalUpdate + " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';");
				continue;
			}

			Arrays.asList(StatsStorage.StatisticType.values()).forEach(stat -> userManager.getDatabase().saveStatistic(user, stat));
		}
	}

	public ChatManager getChatManager() {
		return chatManager;
	}

	public ConfigPreferences getConfigPreferences() {
		return configPreferences;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public MysqlDatabase getMysqlDatabase() {
    	return database;
	}

	public BungeeManager getBungeeManager() {
		return bungeeManager;
	}

	public RewardsFactory getRewardsFactory() {
		return rewardsFactory;
	}

	public SignManager getSignManager() {
		return signManager;
	}

	public CuboidSelector getCuboidSelector() {
		return cuboidSelector;
	}

	public CommandHandler getCommandHandler() {
		return commandHandler;
	}
}