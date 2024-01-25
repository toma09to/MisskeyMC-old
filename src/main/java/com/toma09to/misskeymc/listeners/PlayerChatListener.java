package com.toma09to.misskeymc.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import com.toma09to.misskeymc.api.MisskeyClient;

public class PlayerChatListener implements Listener {
    private final MisskeyClient misskey;
    private final String chatMessageTemplate;

    public PlayerChatListener(MisskeyClient misskey, String chat) {
        this.misskey = misskey;
        this.chatMessageTemplate = chat;
    }

    @EventHandler
    public void onPlayerSendChat(AsyncChatEvent event) {
        final String playerName = event.getPlayer().getName();
        final String content = event.message().toString();
        final String message = chatMessageTemplate.replace("%player%", playerName).replace("%content%", content);

        misskey.sendPost(message);
    }
}
