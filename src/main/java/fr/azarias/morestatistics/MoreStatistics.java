/*
 * The MIT License
 *
 * Copyright 2017 azarias.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author azarias
 */
public class MoreStatistics {

    private final CustomStatsPlugin plugin;

    private final Map<UUID, PlayerStat> playersStats;

    private final Set<UUID> modifiedStats;

    private Integer taskId = null;

    public MoreStatistics(CustomStatsPlugin p) {
        plugin = p;
        playersStats = new HashMap<>();
        modifiedStats = new HashSet<>();
        loadPlayersStats();
    }

    public void addStat(AnimalTamer p, String statName) {
        addStat(p, statName, 1);
    }

    public void addStat(AnimalTamer p, String statName, int value) {
        if (!playersStats.containsKey(p.getUniqueId())) {
            playersStats.put(p.getUniqueId(), new PlayerStat());
        }
        PlayerStat stats = playersStats.get(p.getUniqueId());
        stats.addStat(statName, value);
        savePlayerStats(p);
    }
    
    public void flush(){
        saveStatsToFile();
    }

    private void savePlayerStats(AnimalTamer p) {
        modifiedStats.add(p.getUniqueId());
        if (taskId == null) {
            BukkitScheduler scheduler = plugin.getServer().getScheduler();
            taskId = scheduler.scheduleAsyncDelayedTask(plugin, this::saveStatsToFile, 100L);//Save every hour
        }
    }

    private synchronized void saveStatsToFile() {
        try {
            Path pluginPath = plugin.getDataFolder().toPath();
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

    private void loadPlayersStats() {
        Path stats = plugin.getDataFolder().toPath();
        if (!Files.exists(stats)) {
            return;//No need to check for any stats
        }
        try {
            Files.walk(stats).forEach(file -> {
                if (Files.isRegularFile(file) && file.toString().toLowerCase().endsWith(".json")) {
                    loadStatFile(file);
                }
            });
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create file", ex);
        }
    }

    private void loadStatFile(Path jsonFile) {
        try {
            String uuid = jsonFile.getFileName().toString();
            JSONParser parser = new JSONParser();
            String fileContent = new String(Files.readAllBytes(jsonFile)).trim();
            Object obj = parser.parse(fileContent);
            if (obj instanceof JSONObject) {
                playersStats.put(UUID.fromString(uuid.replace(".json", "")), new PlayerStat((JSONObject) obj));
            }
        } catch (IOException | ParseException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load", ex);
        }
    }
}
