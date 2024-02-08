package com.toma09to.misskeymc.api;

import com.google.gson.Gson;
import com.toma09to.misskeymc.model.MisskeyNoteJson;
import com.toma09to.misskeymc.model.MisskeyNoteJson.MisskeyUser;
import com.toma09to.misskeymc.model.MisskeyPostJson;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MisskeyClient {
    private final String address;
    private final String token;
    private final String visibility;
    private final boolean localOnly;
    private final String channelId;

    private final String prefix;
    private final boolean isDebug;
    private final HttpClient client;
    private final MisskeyLogger log;
    private final Gson gson;
    private String channelSinceId;
    private String mentionSinceId;

    public MisskeyClient(String address, String token, String visibility, boolean localOnly, String channelId, String prefix, boolean isDebug) {
        this.address = address;
        this.token = token;
        this.visibility = visibility;
        this.localOnly = localOnly;
        this.channelId = channelId;
        this.prefix = prefix;
        this.isDebug = isDebug;
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.log = new MisskeyLogger(Bukkit.getLogger());
        this.gson = new Gson();
        this.channelSinceId = null;
        this.mentionSinceId = null;
    }

    private String responseBody(String url, MisskeyPostJson requestBody) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.json()))
                .build();

        if (isDebug) {
            log.info("url: " + url);
            log.info("body: " + requestBody.json());
        }

        String responseBody;
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            responseBody = response.body();

            if (response.statusCode() != 200) {
                log.warning("Misskey server doesn't send 200 OK");
                log.warning(String.valueOf(response.statusCode()));
                log.warning(responseBody);
                return null;
            }
        } catch (IOException | InterruptedException e) {
            log.warning(e.getMessage());
            return null;
        }

        return responseBody;
    }
    public String username() {
        MisskeyPostJson requestBody = MisskeyPostJson.i(token);

        String responseBody = responseBody(address + "/api/i", requestBody);
        if (responseBody == null) return null;

        return gson.fromJson(responseBody, MisskeyUser.class).username;
    }
    public void postNote(String message, String replyId) {
        MisskeyPostJson requestBody = MisskeyPostJson.notesCreate(token, visibility, null, localOnly, replyId, channelId, prefix + message);

        responseBody(address + "/api/notes/create", requestBody);
    }

    public MisskeyNoteJson getNote() {
        MisskeyPostJson requestBody = MisskeyPostJson.channelsTimeline(token, channelId, channelSinceId);

        String responseBody = responseBody(address + "/api/channels/timeline", requestBody);

        MisskeyNoteJson[] messages = gson.fromJson(responseBody, MisskeyNoteJson[].class);
        if (messages.length == 0) return null;

        channelSinceId = messages[0].id;

        if (messages[0].user.isBot) return null;
        return messages[0];
    }
    public MisskeyNoteJson getDirectMessage() {
        // 一番最初のメッセージには返信しないようにする
        boolean isFirst = mentionSinceId == null;

        MisskeyPostJson requestBody = MisskeyPostJson.notesMentions(token, mentionSinceId);

        String responseBody = responseBody(address + "/api/notes/mentions", requestBody);

        MisskeyNoteJson[] messages = gson.fromJson(responseBody, MisskeyNoteJson[].class);
        if (messages.length == 0) return null;

        mentionSinceId = messages[0].id;

        if (messages[0].user.isBot) return null;
        return !isFirst ? messages[0] : null;
    }
}
