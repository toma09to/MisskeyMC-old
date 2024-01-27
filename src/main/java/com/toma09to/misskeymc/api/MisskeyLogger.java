package com.toma09to.misskeymc.api;

import java.util.logging.Logger;

public class MisskeyLogger {
    private final Logger log;
    private final String prefix = "[MisskeyMC] ";

    public MisskeyLogger(Logger log) {
        this.log = log;
    }

    public void info(String message) {
        log.info(prefix + message);
    }
    public void warning(String message) {
        log.warning(prefix + message);
    }
}
