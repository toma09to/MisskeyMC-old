package com.toma09to.misskeymc.api;

import com.toma09to.misskeymc.database.UsersDatabase;
import com.toma09to.misskeymc.model.MisskeyNoteJson;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.Objects;

public class MisskeyAuthorizationScheduler extends BukkitRunnable {
    private final MisskeyClient client;
    private final UsersDatabase database;
    private boolean isServerStop;

    public MisskeyAuthorizationScheduler(MisskeyClient client, UsersDatabase database) {
        this.client = client;
        this.database = database;
        this.isServerStop = false;
    }

    public void stopTask() { isServerStop = true; }

    @Override
    public void run() {
        if (isServerStop) {
            cancel();
            return;
        }
        MisskeyNoteJson challenge = client.getDirectMessage();
        if (challenge == null) return;

        if (Objects.equals(challenge.visibility, "specified")) {
            String token = challenge.text.replace("@" + client.username(), "").trim();
            String userId = challenge.user.id;

            Bukkit.getLogger().info(token);
            Bukkit.getLogger().info(userId);
            try {
                if (database.authorizeUser(token, userId)) {
                    client.postNote("認証に成功しました。", challenge.id);
                } else {
                    client.postNote("認証に失敗しました。再度お試しください。", challenge.id);
                }
            } catch (SQLException e) {
                client.postNote("認証中にエラーが発生しました。再度お試しください。", challenge.id);
                Bukkit.getLogger().warning(e.getMessage());
            }
        }
    }
}
