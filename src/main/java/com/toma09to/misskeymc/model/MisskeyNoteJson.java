package com.toma09to.misskeymc.model;

public class MisskeyNoteJson {
    public String id;
    public MisskeyUser user;
    public String text;

    public static class MisskeyUser {
        public String id;
        public String name;
        public String username;
        public boolean isBot;
    }
}
