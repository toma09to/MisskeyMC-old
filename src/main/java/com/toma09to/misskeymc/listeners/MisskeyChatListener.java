package com.toma09to.misskeymc.listeners;

import com.toma09to.misskeymc.events.MisskeyChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MisskeyChatListener implements Listener {
    private final String chatMessageTemplate;

    public MisskeyChatListener(String template) {
        this.chatMessageTemplate = template;
    }

    @EventHandler
    public void onMisskeySendChat(MisskeyChatEvent event) {
        final String name = event.getName();
        final String userName = event.getUserName();
        final String content = event.getText();
        final String messageText = chatMessageTemplate
                .replace("%name%", name)
                .replace("%username%", userName)
                .replace("%content%", content);
        final Component message = Component.text(messageText);

        Bukkit.getServer().sendMessage(message);
    }
}
