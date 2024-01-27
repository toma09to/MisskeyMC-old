package com.toma09to.misskeymc.api;

import com.toma09to.misskeymc.events.MisskeyChatEvent;
import com.toma09to.misskeymc.model.MisskeyNoteJson;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class MisskeyNoteScheduler extends BukkitRunnable {
    private final MisskeyClient client;
    private boolean isServerStop;

    public MisskeyNoteScheduler(MisskeyClient client) {
        this.client = client;
        this.isServerStop = false;
    }

    public void stopTask() {
        isServerStop = true;
    }

    @Override
    public void run() {
        if (isServerStop) {
            cancel();
            return;
        }
        MisskeyNoteJson note = client.getNote();
        if (note == null) return;

        MisskeyChatEvent event = new MisskeyChatEvent(note);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }
}
