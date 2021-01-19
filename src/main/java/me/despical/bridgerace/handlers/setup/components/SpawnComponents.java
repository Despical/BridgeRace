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

package me.despical.bridgerace.handlers.setup.components;

import me.despical.bridgerace.Main;
import me.despical.bridgerace.arena.Arena;
import me.despical.bridgerace.handlers.setup.SetupInventory;
import me.despical.bridgerace.utils.CuboidSelector;
import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class SpawnComponents implements SetupComponent {

	@Override
	public void registerComponent(SetupInventory setupInventory, StaticPane pane) {
		Player player = setupInventory.getPlayer();
		FileConfiguration config = setupInventory.getConfig();
		Arena arena = setupInventory.getArena();
		Main plugin = setupInventory.getPlugin();
		String serializedLocation = LocationSerializer.locationToString(player.getLocation()), path = "instances." + arena.getId() + ".";

		pane.addItem(new GuiItem(new ItemBuilder(Material.REDSTONE_BLOCK)
			.name("&e&lSet First Location")
			.lore("&7Click to set the first location")
			.lore("&7on the place where you are standing")
			.lore("&8(location where first player will")
			.lore("&8be teleported in the game)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(path + "firstplayerlocation"))
			.build(), e -> {

			e.getWhoClicked().closeInventory();
			config.set(path + "firstplayerlocation", serializedLocation);
			arena.setEndLocation(player.getLocation());
			player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aFirst player's location for arena " + arena.getId() + " set at your location!"));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 0, 0);

		pane.addItem(new GuiItem(new ItemBuilder(Material.LAPIS_BLOCK)
			.name("&e&lSet Second Location")
			.lore("&7Click to set the second location")
			.lore("&7on the place where you are standing")
			.lore("&8(location where second player will")
			.lore("&8be teleported in the game)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(path + "secondplayerlocation"))
			.build(), e -> {

			e.getWhoClicked().closeInventory();
			config.set(path + "secondplayerlocation", serializedLocation);
			arena.setEndLocation(player.getLocation());
			player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aSecond player's location for arena " + arena.getId() + " set at your location!"));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 1, 0);

		pane.addItem(new GuiItem(new ItemBuilder(Material.IRON_BLOCK)
			.name("&e&lSet Ending Location")
			.lore("&7Click to set the ending location")
			.lore("&7on the place where you are standing")
			.lore("&8(location where players will")
			.lore("&8be teleported after the game)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(path + "endlocation"))
			.build(), e -> {

			e.getWhoClicked().closeInventory();
			config.set(path + "endlocation", serializedLocation);
			arena.setEndLocation(player.getLocation());
			player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aEnding location for arena " + arena.getId() + " set at your location!"));

			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 2, 0);

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.BLAZE_ROD.parseItem())
			.name("&e&lSet Arena Region")
			.lore("&7Click to set arena's region")
			.lore("&7with the cuboid selector.")
			.lore("&8(area where game will be playing)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(path + "arenamax"))
			.build(), e -> {
			e.getWhoClicked().closeInventory();

			CuboidSelector.Selection selection = plugin.getCuboidSelector().getSelection(player);

			if (selection == null) {
				plugin.getCuboidSelector().giveSelectorWand(player);
				return;
			}

			if (selection.getSecondPos() == null) {
				player.sendMessage(plugin.getChatManager().colorRawMessage("&c&l✖ &cWarning | Please select top corner using right click!"));
				return;
			}

			config.set(path + "arenamin", LocationSerializer.locationToString(selection.getFirstPos()));
			config.set(path + "arenamax", LocationSerializer.locationToString(selection.getSecondPos()));
			player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aGame area of arena " + arena.getId() + " set as you selection!"));
			plugin.getCuboidSelector().removeSelection(player);

			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 3, 0);

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.STICK.parseItem())
			.name("&e&lSet Winning Region")
			.lore("&7Click to set winning region")
			.lore("&7with the cuboid selector.")
			.lore("&8(area where players try to")
			.lore("&8reach to win the race)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(path + "areamax"))
			.build(), e -> {
			e.getWhoClicked().closeInventory();

			CuboidSelector.Selection selection = plugin.getCuboidSelector().getSelection(player);

			if (selection == null) {
				plugin.getCuboidSelector().giveSelectorWand(player);
				return;
			}

			if (selection.getSecondPos() == null) {
				player.sendMessage(plugin.getChatManager().colorRawMessage("&c&l✖ &cWarning | Please select top corner using right click!"));
				return;
			}

			config.set(path + "areamin", LocationSerializer.locationToString(selection.getFirstPos()));
			config.set(path + "areamax", LocationSerializer.locationToString(selection.getSecondPos()));
			player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aWinning area of arena " + arena.getId() + " set as you selection!"));
			plugin.getCuboidSelector().removeSelection(player);

			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 4, 0);
	}
}