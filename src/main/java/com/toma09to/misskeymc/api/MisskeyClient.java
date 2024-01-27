package com.toma09to.misskeymc.api;

import com.google.gson.Gson;
import com.toma09to.misskeymc.model.MisskeyNoteJson;
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
    private String sinceId;

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
        this.sinceId = null;
    }
    public void postNote(String message) {
        String url = this.address + "/api/notes/create";
        MisskeyPostJson body = MisskeyPostJson.notesCreate(token, visibility, null, localOnly, channelId, prefix + message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.json()))
                .build();

        if (isDebug) {
            log.info("url: " + url);
            log.info("body: " + body);
        }

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warning("Misskey server doesn't send 200 OK");
                log.warning(String.valueOf(response.statusCode()));
                log.warning(response.body());
            }
        } catch (IOException e) {
            log.warning("IOException was thrown");
        } catch (InterruptedException e) {
            log.warning("InterruptedException was thrown");
        }
    }

    public MisskeyNoteJson getNote() {
        String url = this.address + "/api/channels/timeline";
        MisskeyPostJson requestBody = MisskeyPostJson.channelsTimeline(token, channelId, sinceId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.json()))
                .build();

        if (isDebug) {
            log.info("url: " + url);
            log.info("body: " + requestBody);
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

        MisskeyNoteJson[] messages = new Gson().fromJson(responseBody, MisskeyNoteJson[].class);
        if (messages.length == 0) return null;
        if (messages[0].user.isBot) return null;

        sinceId = messages[0].id;
        return messages[0];
    }
}
