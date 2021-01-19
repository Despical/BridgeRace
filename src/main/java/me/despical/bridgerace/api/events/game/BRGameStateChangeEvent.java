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

package me.despical.bridgerace.api.events.game;

import me.despical.bridgerace.api.events.BridgeRaceEvent;
import me.despical.bridgerace.arena.Arena;
import me.despical.bridgerace.arena.ArenaState;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class BRGameStateChangeEvent extends BridgeRaceEvent {

	private final HandlerList HANDLERS = new HandlerList();
	private final ArenaState arenaState;

	public BRGameStateChangeEvent(Arena eventArena, ArenaState arenaState) {
		super(eventArena);
		this.arenaState = arenaState;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public ArenaState getArenaState() {
		return arenaState;
	}
}