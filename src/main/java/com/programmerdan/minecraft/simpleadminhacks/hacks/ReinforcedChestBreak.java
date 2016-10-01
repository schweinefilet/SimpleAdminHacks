package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.ReinforcedChestBreakConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;

import java.util.*;
import java.util.logging.Level;

/**
 * Sends every 3 minutes a message to the admins if a chest is broken
 */
public class ReinforcedChestBreak extends SimpleHack<ReinforcedChestBreakConfig> implements Listener {

    public static String NAME = "ReinforcedChestBreak";

    private ReinforcementManager manager;
    private Messenger messenger;
    private Set<String> messages;

    public ReinforcedChestBreak(SimpleAdminHacks plugin, ReinforcedChestBreakConfig config) {
        super(plugin, config);
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, plugin());
    }

    @Override
    public void registerCommands() {}

    @Override
    public void dataBootstrap() {
        messages = new HashSet<>();

        manager = Citadel.getReinforcementManager();
        messenger = new Messenger();

        Bukkit.getScheduler().runTaskTimer(plugin(), messenger, 0, config.getDelay() * 20);
    }

    @Override
    public void unregisterListeners() {}

    @Override
    public void unregisterCommands() {}

    @Override
    public void dataCleanup() {}

    @Override
    public String status() {
        return "Delay: " + config.getDelay();
    }

    /**
     * Gets fired by the BLockBreakEvent and checks if the block is reinforced
     * @param eve BLockBreakEvent
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent eve) {
        if (eve.getBlock().getType().equals(Material.CHEST)) {
            if(manager.isReinforced(eve.getBlock())) {
                String name = eve.getPlayer().getDisplayName();
                Location loc = eve.getBlock().getLocation();

                String msg = setVars(name,
                                    String.valueOf(loc.getBlockX()),
                                    String.valueOf(loc.getBlockY()),
                                    String.valueOf(loc.getBlockZ()));

                if(messages.add(msg)) {
                    plugin().log(Level.INFO, msg);
                }
            }
        }
    }

    /**
     * Builds the message
     * @param name the player name
     * @param x block x
     * @param y block y
     * @param z block z
     * @return returns the builded String
     */
    private String setVars(String name, String x, String y, String z) {
        return ChatColor.translateAlternateColorCodes('&', config.getMessage()
                                                 .replace("%player%", name)
                                                 .replace("%x%", x)
                                                 .replace("%y%", y)
                                                 .replace("%z%", z));
    }

    private class Messenger implements Runnable {

        @Override
        public void run() {
            for (String message: messages) {
                plugin().serverOperatorBroadcast(message);
            }
            messages.clear();
        }
    }
}
