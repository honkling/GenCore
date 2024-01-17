package com.github.summiner.gencore.gens;

import com.github.summiner.gencore.util.ColorUtil;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Generator {

    private final Material drop;
    private final Long sell;
    private final Long upgrade;
    private final Material next;
    private final ItemStack item;

    public Generator(final Material block, final String name, final Material drop, final Long sell, final Long upgrade, final List<String> lore, @Nullable final Material next) {
        this.drop = drop;
        this.sell = sell;
        this.upgrade = upgrade;
        this.next = next;
        this.item = generateItem(block, name, lore);
    }

    public Material getDrop() {
        return drop;
    }

    public Long getSell() {
        return sell;
    }

    public Long getUpgradeCost() {
        return upgrade;
    }

    public Material getNextBlock() {
        return next;
    }

    public ItemStack getItem() {
        return item;
    }

    private ItemStack generateItem(Material block, String name, List<String> unformattedLore) {
        NBTItem nbt = new NBTItem(new ItemStack(block));
        nbt.setBoolean("isGen", true);
        ItemStack item = nbt.getItem();
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColorUtil.formatColor(name));
        List<String> lore = new ArrayList<>();
        unformattedLore.forEach(line -> lore.add(ColorUtil.formatColor(line)));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
