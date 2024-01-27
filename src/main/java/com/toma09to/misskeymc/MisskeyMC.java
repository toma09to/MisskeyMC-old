package com.toma09to.misskeymc;

import com.toma09to.misskeymc.api.MisskeyNoteScheduler;
import com.toma09to.misskeymc.database.UsersDatabase;
import com.toma09to.misskeymc.listeners.MisskeyChatListener;
import com.toma09to.misskeymc.listeners.PlayerJoinLeaveListener;
import com.toma09to.misskeymc.listeners.PlayerChatListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.toma09to.misskeymc.api.MisskeyClient;

import java.sql.SQLException;

public final class MisskeyMC extends JavaPlugin {
    private MisskeyClient misskey;
    private String enabledMessage;
    private String disabledMessage;
    private MisskeyNoteScheduler scheduler;
    private UsersDatabase database;

    @Override
    public void onEnable() {
        saveResource("config.yml", false);

        String address = getConfig().getString("misskey.address");
        String token = getConfig().getString("misskey.token");
        String visibility = getConfig().getString("misskey.visibility");
        boolean localOnly = getConfig().getBoolean("misskey.localOnly");
        String channelId = getConfig().getString("misskey.channelId");
        String prefix = getConfig().getString("misskey.prefix");
        boolean isDebug = getConfig().getBoolean("misskey.debug");
        String serverUrl = getConfig().getString("misskey.serverUrl");

        String joinMessage = getConfig().getString("message.joinMessage");
        String quitMessage = getConfig().getString("message.quitMessage");
        this.enabledMessage = getConfig().getString("message.enabledMessage");
        this.disabledMessage = getConfig().getString("message.disabledMessage");
        String mcToMskyMessage = getConfig().getString("message.minecraftToMisskeyMessage");
        String mskyToMcMessage = getConfig().getString("message.misskeyToMinecraftMessage");

        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            database = new UsersDatabase(getDataFolder().getAbsolutePath() + "/users.db");
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("Failed to connect to the database!" + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }

        misskey = new MisskeyClient(address, token, visibility, localOnly, channelId, prefix, isDebug);

        Bukkit.getServer().getPluginManager().registerEvents(
                new PlayerJoinLeaveListener(misskey, serverUrl, joinMessage, quitMessage),
                this
        );
        Bukkit.getServer().getPluginManager().registerEvents(
                new PlayerChatListener(misskey, mcToMskyMessage),
                this
        );
        Bukkit.getServer().getPluginManager().registerEvents(
                new MisskeyChatListener(mskyToMcMessage),
                this
        );

        scheduler = new MisskeyNoteScheduler(misskey);
        scheduler.runTaskTimerAsynchronously(this, 0, 20);

        misskey.postNote(enabledMessage);
    }

    @Override
    public void onDisable() {
        try {
            database.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        scheduler.stopTask();
        misskey.postNote(disabledMessage);
    }
}
