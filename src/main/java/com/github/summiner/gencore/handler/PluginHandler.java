package com.github.summiner.gencore.handler;

import com.github.summiner.gencore.GenCore;

public class PluginHandler {
    public static GenCore getPlugin() {
        return GenCore.getPlugin(GenCore.class);
    }
}
