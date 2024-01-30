package com.toma09to.misskeymc;

import com.toma09to.misskeymc.api.MisskeyAuthorizationScheduler;
import com.toma09to.misskeymc.api.MisskeyLogger;
import com.toma09to.misskeymc.api.MisskeyNoteScheduler;
import com.toma09to.misskeymc.database.UsersDatabase;
import com.toma09to.misskeymc.listeners.MisskeyChatListener;
import com.toma09to.misskeymc.listeners.PlayerCheckAuthorizedListener;
import com.toma09to.misskeymc.listeners.PlayerJoinLeaveListener;
import com.toma09to.misskeymc.listeners.PlayerChatListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.toma09to.misskeymc.api.MisskeyClient;

import java.sql.SQLException;

public final class MisskeyMC extends JavaPlugin {
    private MisskeyClient misskey;
    private MisskeyLogger log;
    private String enabledMessage;
    private String disabledMessage;
    private MisskeyNoteScheduler noteScheduler;
    private MisskeyAuthorizationScheduler authorizationScheduler;
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

        boolean requireAuthorization = getConfig().getBoolean("authorization.require");
        String contact = getConfig().getString("authorization.contact");

        String dbHost = getConfig().getString("database.host");
        String dbDatabase = getConfig().getString("database.database");
        String dbUser = getConfig().getString("database.username");
        String dbPassword = getConfig().getString("database.password");

        misskey = new MisskeyClient(address, token, visibility, localOnly, channelId, prefix, isDebug);
        log = new MisskeyLogger(Bukkit.getLogger());

        try {
            // Connect to MySQL
            database = new UsersDatabase(log, dbHost, dbDatabase, dbUser, dbPassword);
        } catch (SQLException e) {
            Bukkit.getPluginManager().disablePlugin(this);
        }

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

        noteScheduler = new MisskeyNoteScheduler(misskey);
        noteScheduler.runTaskTimerAsynchronously(this, 0, 20);

        String botName = misskey.username();
        if (requireAuthorization) {
            Bukkit.getServer().getPluginManager().registerEvents(
                    new PlayerCheckAuthorizedListener(database, serverUrl, botName, contact),
                    this
            );
            authorizationScheduler = new MisskeyAuthorizationScheduler(misskey, database);
            authorizationScheduler.runTaskTimerAsynchronously(this, 0, 100);
        }

        misskey.postNote(enabledMessage, null);
    }

    @Override
    public void onDisable() {
        noteScheduler.stopTask();
        authorizationScheduler.stopTask();
        misskey.postNote(disabledMessage, null);
    }
}
