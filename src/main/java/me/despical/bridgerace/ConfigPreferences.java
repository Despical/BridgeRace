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

import me.despical.bridgerace.utils.Debugger;
import me.despical.commonsbox.compat.XMaterial;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class ConfigPreferences {

	private final Main plugin;
	private ItemStack gameMaterial;
	private final Map<Option, Boolean> options = new HashMap<>();

	public ConfigPreferences(Main plugin) {
		this.plugin = plugin;

		loadOptions();
		loadGameMaterial();
	}

	/**
	 * Returns whether option value is true or false
	 *
	 * @param option option to get value from
	 * @return true or false based on user configuration
	 */
	public boolean getOption(Option option) {
		return options.get(option);
	}

	private void loadOptions() {
		for (Option option : Option.values()) {
			options.put(option, plugin.getConfig().getBoolean(option.getPath(), option.getDefault()));
		}
	}

	private void loadGameMaterial() {
		try {
			gameMaterial = XMaterial.matchXMaterial(plugin.getConfig().getString("Game-Material", "WOOL").toUpperCase()).orElse(XMaterial.WHITE_WOOL).parseItem();
		} catch (Exception e) {
			Debugger.sendConsoleMessage("Can not found Material " + plugin.getConfig().getString("Game-Material"));
			gameMaterial = XMaterial.WHITE_WOOL.parseItem();
		}
	}

	public ItemStack getGameMaterial() {
		return gameMaterial;
	}

	public enum Option {
		BOSSBAR_ENABLED("Bossbar-Enabled", true), BUNGEE_ENABLED("BungeeActivated", false),
		CHAT_FORMAT_ENABLED("ChatFormat-Enabled", true), DATABASE_ENABLED("DatabaseActivated", false),
		DISABLE_SEPARATE_CHAT("Disable-Separate-Chat", false), ENABLE_SHORT_COMMANDS("Enable-Short-Commands", false),
		INVENTORY_MANAGER_ENABLED("InventoryManager", true), DISABLE_LEVEL_COUNTDOWN("Disable-Level-Countdown", false),
		DISABLE_LEAVE_COMMAND("Disable-Leave-Command", false),	NAMETAGS_HIDDEN("Nametags-Hidden", true),
		ALLOW_PVP("Allow-PvP", false);

		private final String path;
		private final boolean def;

		Option(String path, boolean def) {
			this.path = path;
			this.def = def;
		}

		public String getPath() {
			return path;
		}

		/**
		 * @return default value of option if absent in config
		 */
		public boolean getDefault() {
			return def;
		}
	}
}