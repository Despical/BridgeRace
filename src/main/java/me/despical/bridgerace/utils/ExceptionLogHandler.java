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

package me.despical.bridgerace.utils;

import me.despical.bridgerace.Main;
import org.bukkit.Bukkit;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Despical
 * <p>
 * Created at 18.12.2020
 */
public class ExceptionLogHandler extends Handler {

    private final Main plugin;
    private final String[] blacklistedClasses = {"user.data.MysqlManager", "commonsbox.database.MysqlDatabase"};

    public ExceptionLogHandler(Main plugin) {
        this.plugin = plugin;

        Bukkit.getLogger().addHandler(this);
    }

    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord record) {
        Throwable throwable = record.getThrown();

        if (!(throwable instanceof Exception) || !throwable.getClass().getSimpleName().contains("Exception")) {
            return;
        }

        if (throwable.getStackTrace().length <= 0) {
            return;
        }

        if (throwable.getCause() != null && throwable.getCause().getStackTrace() != null) {
            if (!throwable.getCause().getStackTrace()[0].getClassName().contains("me.despical.bridgerace")) {
                return;
            }
        }

        if (!throwable.getStackTrace()[0].getClassName().contains("me.despical.bridgerace")) {
            return;
        }

        if (containsBlacklistedClass(throwable)) {
            return;
        }

        record.setThrown(null);

        Exception exception = throwable.getCause() != null ? (Exception) throwable.getCause() : (Exception) throwable;
        StringBuilder stacktrace = new StringBuilder(exception.getClass().getSimpleName());

        if (exception.getMessage() != null) {
            stacktrace.append(" (").append(exception.getMessage()).append(")");
        }

        stacktrace.append("\n");

        for (StackTraceElement str : exception.getStackTrace()) {
            stacktrace.append(str.toString()).append("\n");
        }

        plugin.getLogger().log(Level.WARNING, "[Reporter Service] <<-----------------------------[START]----------------------------->>");
        plugin.getLogger().log(Level.WARNING, stacktrace.toString());
        plugin.getLogger().log(Level.WARNING, "[Reporter Service] <<------------------------------[END]------------------------------>>");

        record.setMessage("[Bridge Race] We have found a bug in the code. Contact us at our official Discord server (Invite link: https://discordapp.com/invite/Vhyy4HA) with the following error given above!");
    }

    private boolean containsBlacklistedClass(Throwable throwable) {
        for (StackTraceElement element : throwable.getStackTrace()) {
            for (String blacklist : blacklistedClasses) {
                if (element.getClassName().contains("me.despical.bridgerace." + blacklist)) {
                    return true;
                }
            }
        }

        return false;
    }
}