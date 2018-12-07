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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author azarias
 */
public class MoreStatistics {

    private final CustomStatsPlugin plugin;

    private final ConcurrentHashMap<UUID, PlayerStat> playersStats;

    private  final CopyOnWriteArraySet<UUID> modifiedStats;

    private BukkitTask taskId = null;

    public MoreStatistics(CustomStatsPlugin p) {
        plugin = p;
        playersStats = new ConcurrentHashMap<>();
        modifiedStats = new CopyOnWriteArraySet<>();
    }

    /**
     * Increments by one the given stat
     * of the given player
     */
    public void addStat(AnimalTamer p, Statistics stat) {
        addStat(p, stat, 1);
    }

    /**
     * Increment by the given value, the given stat
     * of the given player
     * @param p the player who's stat is going to be incremented
     * @param stat the stat that's going to be incremented
     * @param value the value to increment
     */
    public void addStat(AnimalTamer p, Statistics stat, int value) {
        if (!playersStats.containsKey(p.getUniqueId())) {
            playersStats.put(p.getUniqueId(), new PlayerStat());
        }
        PlayerStat stats = playersStats.get(p.getUniqueId());
        stats.addStat(stat, value);
        updatePlayerStats(p);
    }
    
    /**
     * Whenever the plugin is disabled (server shutdown)
     * Saves all the modifieds stats
     * and unload all the statistics
     */
    public void flush(){
        saveModifiedStats(plugin.getDataFolder().toPath());
        modifiedStats.clear();
        playersStats.clear();
    }

    /**
     * Update the stats of the player, in ram
     * and, if there is not one already, scedules an update to
     * save the modified statistics (in one hour)
     */
    private void updatePlayerStats(AnimalTamer p) {
        modifiedStats.add(p.getUniqueId());
        if (taskId == null) {
            BukkitScheduler scheduler = plugin.getServer().getScheduler();
            long oneHour = /* 1 second */ 20 * /* 1 minute */ 60 * /* 1 hour */ 60L;
            Path pluginPath = plugin.getDataFolder().toPath();
            taskId = scheduler.runTaskLaterAsynchronously(plugin, () -> saveModifiedStats(pluginPath), oneHour);
        }
    }

    /**
     * When a player quits, directly save his stats
     * and unload them
     * @param p
     */
    public void immediatePlayerSave(final AnimalTamer p){
        final Path pluginPath = plugin.getDataFolder().toPath();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> savePlayerStat(p, pluginPath));
    }

    /**
     * Saves the stats of a singlye player
     * is called asynchronously so it uses only thread-safes 
     * variables
     * @param p
     * @param pluginPath
     */
    private synchronized void savePlayerStat(final AnimalTamer p, final Path pluginPath){
        try {
            if(!Files.exists(pluginPath)){
                Files.createDirectory(pluginPath);
            }
            Path fPath = pluginPath.resolve(p.getUniqueId().toString() + ".json");
            Files.write(fPath, playersStats.get(p.getUniqueId()).toJSON().getBytes());
            modifiedStats.remove(p.getUniqueId());
            playersStats.remove(p.getUniqueId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save only the stats that were modifieds since the last save
     * Flushes the set of the modifieds stats
     * @param pluginPath path to the plugin where to save the data
     */
    private synchronized void saveModifiedStats(Path pluginPath) {
        try {
            if (!Files.exists(pluginPath)) {
                Files.createDirectory(pluginPath);
            }
            for (UUID u : modifiedStats) {
                Path fPath = pluginPath.resolve(u.toString() + ".json");
                Files.write(fPath, playersStats.get(u).toJSON().getBytes());
            }
            modifiedStats.clear();
            taskId = null;
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save file", ex);
        }
    }


    /**
     * Load for the given player, the given json file
     * @param player player to load
     * @param jsonFile json file
     */
    public void loadStatFile(Player player) {
        try {
            Path jsonFile = plugin.getDataFolder().toPath().resolve(player.getUniqueId() + ".json");
            if(Files.exists(jsonFile)){
                JsonParser parser = new JsonParser();
                String fileContent = new String(Files.readAllBytes(jsonFile)).trim();
                JsonElement obj = parser.parse(fileContent);
                if (obj.isJsonObject()) {
                    playersStats.put(player.getUniqueId(), new PlayerStat(obj.getAsJsonObject(), player));
                } else {
                    throw new JsonParseException("Invalid statistics file found");
                }
            } else {
                playersStats.put(player.getUniqueId(), new PlayerStat());
            }
        } catch (IOException | JsonParseException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load stat file of " + player.getUniqueId(), ex);
        }
    }
}
