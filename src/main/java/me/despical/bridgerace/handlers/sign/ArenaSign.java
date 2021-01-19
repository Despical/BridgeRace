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

package me.despical.bridgerace.handlers.sign;

import me.despical.bridgerace.arena.Arena;
import me.despical.commonsbox.compat.VersionResolver;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class ArenaSign {

	private final Sign sign;
	private Block behind;
	private final Arena arena;

	public ArenaSign(Sign sign, Arena arena) {
		this.sign = sign;
		this.arena = arena;

		setBehindBlock();
	}

	private void setBehindBlock() {
		this.behind = null;

		if (sign.getBlock().getType() == Material.getMaterial("WALL_SIGN")) {
			this.behind = VersionResolver.isCurrentEqualOrHigher(VersionResolver.ServerVersion.v1_14_R1) ? getBlockBehind() : getBlockBehindLegacy();
		}
	}

	private Block getBlockBehind() {
		try {
			Object blockData = sign.getBlock().getState().getClass().getMethod("getBlockData").invoke(sign.getBlock().getState());
			BlockFace face = (BlockFace) blockData.getClass().getMethod("getFacing").invoke(blockData);
			Location loc = sign.getLocation(), location = new Location(sign.getWorld(), loc.getBlockX() - face.getModX(), loc.getBlockY() - face.getModY(), loc.getBlockZ() - face.getModZ());
			return location.getBlock();
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Block getBlockBehindLegacy() {
		return sign.getBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
	}

	public Sign getSign() {
		return sign;
	}

	@Nullable
	public Block getBehind() {
		return behind;
	}

	public Arena getArena() {
		return arena;
	}
}