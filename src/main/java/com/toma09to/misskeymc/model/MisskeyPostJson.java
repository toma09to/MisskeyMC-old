package com.toma09to.misskeymc.model;

import com.google.gson.Gson;
import com.toma09to.misskeymc.api.MisskeyClient;

import javax.annotation.Nonnull;

public class MisskeyPostJson {
    public String i;
    public String visibility;
    public String cw;
    public Boolean localOnly;
    public String replyId;
    public String channelId;
    public String text;
    public String sinceId;

    private static MisskeyPostJson construct(String i, String visibility, String cw, Boolean localOnly, String replyId, String channelId, String text, String sinceId) {
        MisskeyPostJson json = new MisskeyPostJson();
        json.i = i;
        json.visibility = visibility;
        json.cw = cw;
        json.localOnly = localOnly;
        json.replyId = replyId;
        json.channelId = channelId;
        json.text = text;
        json.sinceId = sinceId;

        return json;
    }

    public static MisskeyPostJson i(@Nonnull String token) {
        return construct(token, null, null, null, null, null, null, null);
    }

    public static MisskeyPostJson notesCreate(@Nonnull String token, String visibility, String cw, boolean localOnly, String replyId, String channelId, @Nonnull String text) {
        MisskeyPostJson json = construct(token, visibility, cw, localOnly, replyId, channelId, text, null);

        // 空文字列のままだとそれも含めてjson化されてパラメーターが不正になる
        if (json.cw == null || json.cw.isEmpty()) {
            json.cw = null;
        }
        if (json.channelId == null || json.channelId.isEmpty()) {
            json.channelId = null;
        }

        return json;
    }

    public static MisskeyPostJson notesMentions(@Nonnull String token, String sinceId) {
        return construct(token, null, null, null, null, null, null, sinceId);
    }

    public static MisskeyPostJson channelsTimeline(@Nonnull String token, @Nonnull String channelId, String sinceId) {
        return construct(token, null, null, null, null, channelId, null, sinceId);
    }

    public String json() {
        return new Gson().toJson(this);
    }
}
