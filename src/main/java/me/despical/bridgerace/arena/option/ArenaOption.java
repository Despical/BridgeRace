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

package me.despical.bridgerace.arena.option;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public enum ArenaOption {

	/**
	 * Current arena timer, ex. 5 seconds before game starts.
	 */
	TIMER(0);

	private final int defaultValue;

	ArenaOption(int defaultValue) {
		this.defaultValue = defaultValue;
	}

	public int getDefaultValue() {
		return defaultValue;
	}
}