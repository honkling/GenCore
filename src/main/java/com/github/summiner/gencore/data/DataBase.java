package com.github.summiner.gencore.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;

import com.github.summiner.gencore.events.EventManager;
import com.github.summiner.gencore.events.Events;
import com.github.summiner.gencore.handler.PluginHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

public class DataBase {
    static String folder = String.valueOf(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("GenCore")).getDataFolder());
    private static final String storage_url;
    static Configuration config = PluginHandler.getPlugin().getConfig();
    private static final boolean tanks_enabled = config.getBoolean("tanks.enabled");

    private static Connection connect() {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(storage_url);
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Error connecting to the database': " + e.getMessage());
        }

        return conn;
    }

    public static String[] queryPlayer(Player player) {
        String sql = "SELECT uuid, slots, placed, gen_data, tank_data FROM players WHERE uuid = ?";

        try(
            Connection conn = connect();
            PreparedStatement a = conn.prepareStatement(sql)
        ) {
                a.setString(1, String.valueOf(player.getUniqueId()));
                ResultSet results = a.executeQuery();

                while(results.next()) {
                    String resultUuid = results.getString("uuid");
                    if (resultUuid.equals(player.getUniqueId().toString())) {
                        return new String[]{resultUuid, String.valueOf(results.getInt("slots")), String.valueOf(results.getInt("placed")), results.getString("gen_data"), results.getString("tank_data")};
                    }
                }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Error querying player data for '" + player.getUniqueId() + "': " + e.getMessage());
        } catch (Exception e) {
            Bukkit.getLogger().severe("Unknown error querying player data for '" + player.getUniqueId() + "', see below");
            e.printStackTrace();
        }

        return null;
    }

    public static void addPlayer(Player player, boolean clearData) {
        UUID uuid = player.getUniqueId();
        String sql = "INSERT INTO players(uuid,slots,placed,gen_data,tank_data) VALUES(?,?,?,?,?)";
        try(
            Connection conn = connect();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, String.valueOf(uuid));
            stmt.setInt(2, EventManager.getSlots(player));
            stmt.setInt(3, EventManager.getPlaced(player));
            stmt.setString(4, EventManager.getJson(player));
            stmt.setString(5, tanks_enabled ? Events.tanks.get(uuid).getSavingItems(clearData) : "");
            stmt.executeUpdate();
            if(clearData) {
               PluginHandler.getPlugin().clearData(player);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Error inserting player data for '" + player.getUniqueId() + "': " + e.getMessage());
        } catch (Exception e) {
            Bukkit.getLogger().severe("Unknown error inserting player data for '" + player.getUniqueId() + "', see below");
            e.printStackTrace();
        }
    }

    public static void updatePlayerData(Player player, boolean clearData) {
        String sql = "UPDATE players SET slots = ?, placed = ?, gen_data = ?, tank_data = ? WHERE uuid = ?";
        try(
            Connection conn = connect();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, EventManager.getSlots(player));
            stmt.setInt(2, EventManager.getPlaced(player));
            stmt.setString(3, EventManager.getJson(player));
            stmt.setString(4, String.valueOf(tanks_enabled ? Events.tanks.get(player.getUniqueId()).getSavingItems(clearData) : ""));
            stmt.setString(5, String.valueOf(player.getUniqueId()));
            stmt.executeUpdate();
            if(clearData) {
               PluginHandler.getPlugin().clearData(player);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Error updating player data for '" + player.getUniqueId() + "': " + e.getMessage());
        } catch (Exception e) {
            Bukkit.getLogger().severe("Unknown error updating player data for '" + player.getUniqueId() + "', see below");
            e.printStackTrace();
        }
    }

    public static void savePlayerData(Player player, Boolean clearData) {
        try {
            String[] query = queryPlayer(player);
            if (query == null) {
                addPlayer(player, clearData);
            } else {
                updatePlayerData(player, clearData);
            }
        } catch (NullPointerException e) {
            Bukkit.getLogger().severe(String.valueOf(e));
        }
    }

    public static void createNewTable() {
        String sql = "CREATE TABLE IF NOT EXISTS players (\n\tuuid text PRIMARY KEY,\n\tslots integer NOT NULL,\n\tplaced integer NOT NULL,\n\tgen_data json NOT NULL,\n\ttank_data json NOT NULL\n);";

        try(
            Connection conn = DriverManager.getConnection(storage_url);
            Statement stmt = conn.createStatement();
        ) {
            stmt.execute(sql);
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Error creating SQLite table: " + e.getMessage());
        }
    }

    public static void setupDatabase() {
        File file = new File(folder + "/storage.db");
        if (file.exists()) return;
        try (Connection conn = DriverManager.getConnection(storage_url)) {
            if (conn != null) {
                createNewTable();
                Bukkit.getLogger().info("Created database: " + storage_url);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Error creating SQLite database: " + e.getMessage());
        }
    }

    static {
        storage_url = "jdbc:sqlite:" + folder + "/storage.db".replace("\\", "/");
    }
}
