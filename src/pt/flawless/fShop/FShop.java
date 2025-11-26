package pt.flawless.fShop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fShop.listeners.ShopCreateListener;
import pt.flawless.fShop.listeners.SignClickListener;
import pt.flawless.fapi.logs.FConsoleLogger;

public class FShop extends JavaPlugin {
    public static Plugin plugin;
    FConsoleLogger consoleLogger = new FConsoleLogger(this.getName());

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new SignClickListener(), this);
        Bukkit.getPluginManager().registerEvents(new ShopCreateListener(), this);
    }

    @Override
    public void onEnable() {
        plugin = this;

        registerEvents();

        consoleLogger.sendEnablePluginMessage();
    }

    @Override
    public void onDisable() {
        consoleLogger.sendDisablePluginMessage();
    }

}
