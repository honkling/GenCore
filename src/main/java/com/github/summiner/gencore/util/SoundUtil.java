package com.github.summiner.gencore.util;

import com.github.summiner.gencore.handler.PluginHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.NoSuchElementException;

public class SoundUtil {
    public static void playSound(Player player, SoundUtil.Sound sound) {
        if (sound == null) return;
        player.playSound((Entity) player, sound.getBukkitSound(), sound.getVolume(), sound.getPitch());
    }

    public static Sound fromConfigSection(Configuration config, String configKey) {
        if (config.isConfigurationSection(configKey)) {
            ConfigurationSection soundSection = config.getConfigurationSection(configKey);
            assert soundSection != null;
            String id = soundSection.getString("id");
            float volume = (float) soundSection.getDouble("volume", 1.0);
            float pitch = (float) soundSection.getDouble("pitch", 1.0);
            if (id == null || volume <= 0 || pitch <= 0) {
                throw new IllegalArgumentException("Sound with config key '" + configKey + "' had no id, or its volume or pitch was below 0");
            }
            return new Sound(id, volume, pitch);
        }
        else if (config.isString(configKey)) {
            return new Sound(config.getString(configKey), 1.0F, 1.0F);
        }
        throw new IllegalArgumentException("Sound with config key '" + configKey + "' was not a supported type");
    }

    public static class Sound {
        private final String id;
        private final org.bukkit.Sound bukkitSound;
        private final float volume;
        private final float pitch;

        public Sound(String id, float volume, float pitch) {
            this.id = id;
            this.volume = volume;
            this.pitch = pitch;
            NamespacedKey key = NamespacedKey.fromString(id);
            if (key == null) {
                throw new IllegalArgumentException("Id '" + id + "' is not a valid namespaced key");
            }
            this.bukkitSound = Registry.SOUNDS.get(key);
            if (this.bukkitSound == null) {
                throw new NoSuchElementException("Sound '" + id + "' does not exist");
            }
        }

        public String getId() {
            return id;
        }

        public org.bukkit.Sound getBukkitSound() {
            return bukkitSound;
        }

        public float getVolume() {
            return volume;
        }

        public float getPitch() {
            return pitch;
        }
    }
}
