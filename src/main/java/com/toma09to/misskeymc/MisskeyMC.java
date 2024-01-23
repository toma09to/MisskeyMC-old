package com.toma09to.misskeymc;

import com.toma09to.misskeymc.listeners.PlayerJoinLeaveListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.toma09to.misskeymc.api.MisskeyClient;

public final class MisskeyMC extends JavaPlugin {
    private MisskeyClient misskey;
    private String enabledMessage;
    private String disabledMessage;

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

        misskey = new MisskeyClient(address, token, visibility, localOnly, channelId, prefix, isDebug);

        Bukkit.getServer().getPluginManager().registerEvents(
                new PlayerJoinLeaveListener(misskey, serverUrl, joinMessage, quitMessage),
                this
        );

        misskey.sendPost(enabledMessage);
    }

    @Override
    public void onDisable() {
        misskey.sendPost(disabledMessage);
    }
}
