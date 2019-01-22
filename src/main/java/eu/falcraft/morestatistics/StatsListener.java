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
package eu.falcraft.morestatistics;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import net.ess3.api.events.AfkStatusChangeEvent;

/**
 *
 * @author azarias
 */
public class StatsListener implements Listener {

    private final CustomStatsPlugin plugin;
    private final MoreStatistics registry;
    private final Map<UUID, Date> afkPlayers;

    private final ImmutableSet<Material> SHOVELS = ImmutableSet.of(
        Material.WOODEN_SHOVEL, 
        Material.STONE_SHOVEL,
        Material.IRON_SHOVEL,
        Material.GOLDEN_SHOVEL,
        Material.DIAMOND_SHOVEL
    );
    
    private final ImmutableSet<Material> AXES = ImmutableSet.of(
        Material.WOODEN_AXE,
        Material.STONE_AXE,
        Material.IRON_AXE,
        Material.GOLDEN_AXE,
        Material.DIAMOND_AXE
    );

    private final ImmutableSet<Material> STRIPPABLE = ImmutableSet.of(
        Material.OAK_WOOD, Material.OAK_LOG,
        Material.SPRUCE_WOOD, Material.SPRUCE_LOG,
        Material.BIRCH_WOOD, Material.BIRCH_LOG,
        Material.JUNGLE_WOOD, Material.JUNGLE_LOG,
        Material.ACACIA_WOOD, Material.ACACIA_LOG,
        Material.DARK_OAK_WOOD, Material.DARK_OAK_LOG
    );

        

    public StatsListener(CustomStatsPlugin p) {
        plugin = p;
        registry = new MoreStatistics(p);
        afkPlayers = new HashMap<>();
    }

    public MoreStatistics getRegistry() {
        return registry;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent ev){
        registry.loadStatFile(ev.getPlayer());
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityTame(EntityTameEvent ev) {
        if(ev.getOwner() instanceof Player){
            registry.addStat((Player)ev.getOwner(), Statistics.TAME_ENTITY);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLevelChange(PlayerLevelChangeEvent ev){
        if(ev.getOldLevel() < ev.getNewLevel()){
            registry.addStat(ev.getPlayer(), Statistics.LEVEL_WON, ev.getNewLevel() - ev.getOldLevel());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemMend(PlayerItemMendEvent ev){
        registry.addStat(ev.getPlayer(), Statistics.MENDING_REPAIR, ev.getRepairAmount());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerAchievement(PlayerAdvancementDoneEvent ev){
        String adv = ev.getAdvancement().getKey().getKey();
        if(adv.startsWith("recipes/")){
            registry.addStat(ev.getPlayer(), Statistics.DISCOVER_RECIPE);
        } else {
            registry.addStat(ev.getPlayer(), Statistics.ADVANCEMENT_COMPLETE);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (ev.getAction() == Action.RIGHT_CLICK_AIR
                && ev.getMaterial() == Material.FIREWORK_ROCKET
                && ev.getPlayer().isGliding()) {
            registry.addStat(ev.getPlayer(), Statistics.ELYTRA_PROPELS);
        }

        if (ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(ev.getClickedBlock().getType() == Material.CHEST && 
                ev.getClickedBlock().getState() instanceof Chest){
                    Chest c = (Chest) ev.getClickedBlock().getState();
                    if(c.getLootTable() != null){
                        registry.addStat(ev.getPlayer(), Statistics.OPEN_TREASURE);
                    }
            }

            if (isShovel(ev.getMaterial())
                    && ev.getClickedBlock().getType() == Material.GRASS) {
                registry.addStat(ev.getPlayer(), Statistics.PATH_BLOCK);
            } else if(isAxe(ev.getMaterial()) && canBeStripped(ev.getClickedBlock().getType())){
                registry.addStat(ev.getPlayer(), Statistics.STRIPPED_WOOD);
            }
            
        }
    }

    private boolean canBeStripped(Material mat){
        return STRIPPABLE.contains(mat);
    }

    private boolean isAxe(Material mat) {
        return AXES.contains(mat);
    }

    private boolean isShovel(Material mat) {
        return SHOVELS.contains(mat);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRiptide(PlayerRiptideEvent ev){
        registry.addStat(ev.getPlayer(), Statistics.LAUNCH_TRIDENT);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPLayerQuit(PlayerQuitEvent ev) {
        endAfk(ev.getPlayer());
        registry.immediatePlayerSave(ev.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onAfkStatusChange(AfkStatusChangeEvent ev) {
        if (ev.getValue()) {//Become afk
            afkPlayers.put(ev.getAffected().getBase().getUniqueId(), new Date());
        } else {//out of afk
            endAfk(ev.getAffected().getBase());
        }
    }

    private void endAfk(Player concerned) {
        if (afkPlayers.containsKey(concerned.getUniqueId())) {
            Date endTime = new Date();
            Date startTime = afkPlayers.get(concerned.getUniqueId());
            Long afkTime = (endTime.getTime() - startTime.getTime()) / 50;
            afkPlayers.remove(concerned.getUniqueId());
            registry.addStat(concerned, Statistics.AFK_ONE_MINUTE, afkTime.intValue());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onXPGained(PlayerExpChangeEvent ev){
        if(ev.getAmount() > 0){
            registry.addStat(ev.getPlayer(), Statistics.XP_RECEIVED, ev.getAmount());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent ev){
        int length = ev.getMessage().split(" ").length;
        if(length > 0){
            registry.addStat(ev.getPlayer(), Statistics.WORDS_SAID, length);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent ev) {
        //Can also shear mooshroom, gotta filter that
        if (ev.getEntity() instanceof Sheep) {
            registry.addStat(ev.getPlayer(), Statistics.SHEAR_SHEEP);
        }
    }
}
