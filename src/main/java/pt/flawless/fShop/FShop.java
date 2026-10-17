package pt.flawless.fShop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fShop.listeners.ShopCreateListener;
import pt.flawless.fShop.listeners.SignClickListener;
import pt.flawless.fShop.shops.ShopTrade;
import pt.flawless.fapi.economy.Economies;
import pt.flawless.fapi.economy.EconomyApi;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;

public class FShop extends JavaPlugin {
    public static Plugin plugin;
    private final FConsoleLogger consoleLogger = new FConsoleLogger(this.getName());
    private boolean enabledOk = false;

    @Override
    public void onEnable() {
        plugin = this;

        EconomyApi economy;
        try {
            economy = Economies.get();
        } catch (RuntimeException e) {
            consoleLogger.sendMessage("não foi possível iniciar a loja: " + e.getMessage(), EMessageTypeEnum.ERROR);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ShopTrade trade = new ShopTrade(
                economy,
                this::runOnMainThread,
                error -> consoleLogger.sendMessage(String.valueOf(error), EMessageTypeEnum.ERROR));

        Bukkit.getPluginManager().registerEvents(new SignClickListener(trade), this);
        Bukkit.getPluginManager().registerEvents(new ShopCreateListener(), this);

        enabledOk = true;
        consoleLogger.sendEnablePluginMessage();
    }

    /** Com o plugin a desligar o scheduler recusa tarefas: regista e segue, sem propagar para a thread da economia. */
    private void runOnMainThread(Runnable task) {
        try {
            getServer().getScheduler().runTask(this, task);
        } catch (RuntimeException e) {
            consoleLogger.sendMessage("tarefa descartada (plugin a desligar?): " + e, EMessageTypeEnum.ERROR);
        }
    }

    @Override
    public void onDisable() {
        if (enabledOk) {
            consoleLogger.sendDisablePluginMessage();
        }
    }

}
