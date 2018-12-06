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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wither;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityTameEvent;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.Material;
import org.bukkit.block.data.type.Jukebox;
import org.bukkit.event.block.Action;

/**
 *
 * @author azarias
 */
public class StatsListener implements Listener {

    private final CustomStatsPlugin plugin;
    private final MoreStatistics registry;
    private final Map<UUID, Date> afkPlayers;

    public StatsListener(CustomStatsPlugin p) {
        plugin = p;
        registry = new MoreStatistics(p);
        afkPlayers = new HashMap<>();
    }

    public MoreStatistics getRegistry() {
        return registry;
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent ev) {
        if (!ev.isCancelled()) {
            registry.addStat(ev.getOwner(), "custom.tameEntity");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent ev) {
        //Propels event seems to be cancelled ...
        if (ev.getAction() == Action.RIGHT_CLICK_AIR
                && ev.getMaterial() == Material.FIREWORK_ROCKET
                && ev.getPlayer().isGliding()) {
            registry.addStat(ev.getPlayer(), "stat.elytraPropels");
        }

        if (ev.isCancelled()) {
            return;
        }
        if (ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (ev.getMaterial().isRecord()
                    && ev.getClickedBlock().getType() == Material.JUKEBOX
                    && !((Jukebox) ev.getClickedBlock().getBlockData()).hasRecord()) {
                registry.addStat(ev.getPlayer(), "stat.recordPlayed");
            } else if (isShovel(ev.getMaterial())
                    && ev.getClickedBlock().getType() == Material.GRASS) {
                registry.addStat(ev.getPlayer(), "stat.pathBlock");
            }
        }
    }

    private boolean isShovel(Material mat) {
        return mat == Material.WOODEN_SHOVEL || mat == Material.IRON_SHOVEL || mat == Material.GOLDEN_SHOVEL
                || mat == Material.DIAMOND_SHOVEL || mat == Material.STONE_SHOVEL;
    }

    @EventHandler
    public void onPLayerQui(PlayerQuitEvent ev) {
        endAfk(ev.getPlayer());
    }

    @EventHandler
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
            registry.addStat(concerned, "stat.afkOneMinute", afkTime.intValue());
        }
    }

    /**
     * Might be handled by minecraft on its own now ?
     * @param ev
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent ev) {
        if (!(ev.getEntity() instanceof Player)) {
            Player pl = ev.getEntity().getKiller();
            if (pl != null) {
                if (ev.getEntity() instanceof EnderDragon) {
                    registry.addStat(pl, "stat.killEntity.EnderDragon");
                } else if (ev.getEntity() instanceof Wither) {
                    registry.addStat(pl, "stat.killEntity.Wither");
                } else if (ev.getEntity() instanceof IronGolem) {
                    registry.addStat(pl, "stat.killEntity.IronGolem");
                }
            }
        }

    }

    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent ev) {
        //Can also shear mooshroom, gotta filter that
        if (!ev.isCancelled() && ev.getEntity() instanceof Sheep) {
            registry.addStat(ev.getPlayer(), "stat.shearSheep");
        }
    }
}
