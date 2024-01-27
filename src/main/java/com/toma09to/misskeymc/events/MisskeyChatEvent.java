package com.toma09to.misskeymc.events;

import com.toma09to.misskeymc.model.MisskeyNoteJson;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MisskeyChatEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final MisskeyNoteJson note;

    public MisskeyChatEvent(MisskeyNoteJson note) {
        super(true);
        this.note = note;
    }

    public String getName() {
        return this.note.user.name != null ? this.note.user.name : "";
    }
    public String getUserName() {
        return this.note.user.username != null ? this.note.user.username : "";
    }
    public String getText() {
        return this.note.text != null ? this.note.text : "";
    }
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
