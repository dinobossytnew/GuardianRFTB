package dev.mruniverse.rigoxrftb.rigoxrftb.listeners;

import dev.mruniverse.rigoxrftb.rigoxrftb.RigoxRFTB;

public class ListenerUtil {
    private RigoxRFTB plugin;
    public ListenerUtil(RigoxRFTB main){
        plugin = main;
    }
    public void registerListeners() {
        plugin.getLogs().info("Registering listeners..");
        plugin.getServer().getPluginManager().registerEvents(new PlayerListeners(plugin),plugin);
    }
    public void stopWorking(int errorCode) {
        plugin.getLogs().info("The plugin stop working, error code: " + errorCode);
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }
}
