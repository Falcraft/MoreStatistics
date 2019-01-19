/*
 * The MIT License
 *
 * Copyright 2017-2018 azarias.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.azarias.morestatistics;


import com.google.gson.JsonObject;

import org.bukkit.entity.Player;

import fr.azarias.morestatistics.Updater.STAT_VERSION;

/**
 * The object representing the statistics of a player
 *
 * @author azarias
 */
public class PlayerStat {

    private final JsonObject documentRoot;
    private final JsonObject stats;

    /**
     * Empty statistics
     * when this is a new player who connected
     */
    public PlayerStat(Player p) {
        documentRoot = new JsonObject();
        stats = new JsonObject();
        documentRoot.addProperty("DataVersion", "1");
        documentRoot.add("stats", stats);
        Updater.addExistingStats(documentRoot, p);
    }

    /**
     * When a already known player connects
     * checks if we need to update the version
     * @param from
     */
    public PlayerStat(JsonObject from, Player player) {
        // check version and maybe update it
        if(!from.has("DataVersion")){
            documentRoot = Updater.toVersion(STAT_VERSION.VERSION_ONE, from, player);
        } else {
            documentRoot = Updater.addExistingStats(from, player);
        }
        stats = documentRoot.getAsJsonObject("stats");
    }

    /**
     * Increments the stat of the given increment
     * @param stat
     * @param increment
     */
    public void addStat(Statistics stat, int increment){
        stat.addToJson(stats, increment);
    }

    /**
     * Increment the given stat by one
     * @param stat
     */
    public void addStat(Statistics stat){
        addStat(stat, 1);
    }

    public String toJSON() {
        return documentRoot.toString();
    }
}
