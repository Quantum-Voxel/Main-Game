package dev.ultreon.qvoxel.client;

import dev.ultreon.libs.crash.v0.CrashLog;

@FunctionalInterface
public interface CrashHook {
    void onCrash(CrashLog log);
}
