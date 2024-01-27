package com.toma09to.misskeymc.model;

import com.google.gson.Gson;

import javax.annotation.Nonnull;

public class MisskeyPostJson {
    public String i;
    public String visibility;
    public String cw;
    public Boolean localOnly;
    public String channelId;
    public String text;
    public String sinceId;

    public static MisskeyPostJson notesCreate(@Nonnull String token, String visibility, String cw, boolean localOnly, String channelId, @Nonnull String text) {
        MisskeyPostJson json = new MisskeyPostJson();
        json.i = token;
        json.visibility = visibility;
        json.cw = cw;
        json.localOnly = localOnly;
        json.channelId = channelId;
        json.text = text;
        json.sinceId = null;

        // 空文字列のままだとそれも含めてjson化されてパラメーターが不正になる
        if (json.cw == null || json.cw.isEmpty()) {
            json.cw = null;
        }
        if (json.channelId == null || json.channelId.isEmpty()) {
            json.channelId = null;
        }

        return json;
    }

    public static MisskeyPostJson channelsTimeline(@Nonnull String token, @Nonnull String channelId, String sinceId) {
        MisskeyPostJson json = new MisskeyPostJson();
        json.i = token;
        json.visibility = null;
        json.cw = null;
        json.localOnly = null;
        json.channelId = channelId;
        json.text = null;
        json.sinceId = sinceId;

        return json;
    }

    public String json() {
        return new Gson().toJson(this);
    }
}
