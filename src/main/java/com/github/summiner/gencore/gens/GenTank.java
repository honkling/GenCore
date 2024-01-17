package com.github.summiner.gencore.gens;

import com.github.summiner.gencore.handler.PluginHandler;
import com.github.summiner.gencore.util.ColorUtil;
import com.github.summiner.gencore.util.SoundUtil;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.text.NumberFormat;
import java.util.*;

public class GenTank {

    private final HashMap<Material, Generator> gens = PluginHandler.getPlugin().generatorData;
    private  HashMap<Material, Long> items = new HashMap<>();
    public static NumberFormat numFormat = NumberFormat.getInstance(new Locale("en", "US"));
    private final UUID uuid;

    public GenTank(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    static Configuration config = PluginHandler.getPlugin().getConfig();
    public final Boolean exp_enabled = config.getBoolean("tanks.exp.enabled");
    public final String message_solditems = config.getString("messages.solditems");

    public void addItems(Material a, Long b) {
        items.putIfAbsent(a, 0L);
        items.replace(a, items.get(a) + b);
    }

    public String getSavingItems(Boolean d) {
        JSONObject data = new JSONObject();
        data.putAll(items);
        if(d) items.clear();
        if(data.toString() == null) return "";
        return data.toString();
    }

    public void importSavingItems(String data) {
        try {
            HashMap jsonData = new Gson().fromJson(data, items.getClass());
            HashMap<Material, Long> parsedData = new HashMap<>();
            jsonData.forEach((key, value) -> parsedData.put(Material.getMaterial(key.toString().toUpperCase()), Long.parseLong(String.valueOf(value).split("\\.")[0])));
            items = parsedData;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public Double getValue() {
        final Double[] total = {0.0};
        items.forEach((key, value) -> {
            if(gens.containsKey(key)) {
                total[0] = total[0] +(gens.get(key).getSell()*Double.parseDouble(String.valueOf(value)));
            }
        });
        return total[0];
    }

    public Long getSize() {
        final Long[] total = {0L};
        items.forEach((key, value) -> {
            total[0] += value;
        });
        return total[0];
    }

    public void sellItems(Double multi) {
        final Double[] total = {0.0};
        final Long[] total2 = {0L};
        items.forEach((key, value) -> {
            if(gens.containsKey(key)) {
                Generator gen = gens.get(key);
                total[0] += (gen.getSell()*Double.parseDouble(String.valueOf(value)))*multi;
                total2[0] += value;
            }
            items.replace(key, items.get(key)-value);
        });
        Player player = Bukkit.getPlayer(this.getUuid());
        if(exp_enabled) {
           PluginHandler.getPlugin().sendCommand(player, (Objects.requireNonNull(config.getString("tanks.exp.command"))).replaceAll("\\{amount}", String.valueOf(total[0] * config.getDouble("tanks.exp.amount"))));
        }
        assert message_solditems != null;
        assert player != null;
        player.sendMessage(ColorUtil.formatColor(message_solditems.replaceAll("\\{items}", numFormat.format(total2[0])).replaceAll("\\{amount}", numFormat.format(total[0]))));
        SoundUtil.playSound(player, SoundUtil.fromConfigSection(PluginHandler.getPlugin().getConfig(), "sound.solditems"));
        PluginHandler.getPlugin().addMoney(player, total[0]);
    }
}
