package com.github.summiner.gencore.util;

import com.github.summiner.gencore.events.Events;
import com.github.summiner.gencore.handler.PluginHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

    public Configuration config = PluginHandler.getPlugin().getConfig();

    private final String prefix = config.getString("placeholders.prefix");
    private final String max_gens = config.getString("placeholders.maxgens");
    private final String placed_gens = config.getString("placeholders.placedgens");

    @Override
    public @NotNull String getAuthor() {
        return "Summiner, Symmettry (Optimizer)";
    }

    @Override
    public @NotNull String getIdentifier() {
        assert prefix != null;
        return prefix;
    }

    @Override
    public @NotNull String getVersion() {
        return "1.6.2-R1";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if(!player.isOnline()) {
            if(params.equalsIgnoreCase(max_gens) || params.equalsIgnoreCase(placed_gens)) return "0";
            return null;
        }
        if(params.equalsIgnoreCase(max_gens)) return String.valueOf(Events.slots_gens.get(player));
        else if(params.equalsIgnoreCase(placed_gens)) return String.valueOf(Events.placed_gens.get(player));
        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

}