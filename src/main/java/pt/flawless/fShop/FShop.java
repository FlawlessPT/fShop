package pt.flawless.fShop;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fShop.listeners.ShopBreakListener;
import pt.flawless.fShop.listeners.ShopCreateListener;
import pt.flawless.fShop.listeners.SignClickListener;
import pt.flawless.fShop.shops.ShopActions;
import pt.flawless.fShop.shops.ShopItems;
import pt.flawless.fShop.shops.ShopTrade;
import pt.flawless.fShop.shops.SqlShopItemRepository;
import pt.flawless.fapi.database.DatabaseService;
import pt.flawless.fapi.database.Databases;
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

        ShopItems items;
        try {
            DatabaseService database = Databases.get();
            database.migrate(this, List.of("db/fshop/V001__create_shop_items.sql"));
            items = new ShopItems(new SqlShopItemRepository(database),
                    error -> consoleLogger.sendMessage(String.valueOf(error), EMessageTypeEnum.ERROR));
            items.load();
        } catch (Exception e) {
            consoleLogger.sendMessage("não foi possível carregar as lojas: " + e.getMessage(), EMessageTypeEnum.ERROR);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ShopTrade trade = new ShopTrade(
                economy,
                this::runOnMainThread,
                error -> consoleLogger.sendMessage(String.valueOf(error), EMessageTypeEnum.ERROR));

        ShopActions actions = new ShopActions(trade);
        Bukkit.getPluginManager().registerEvents(new SignClickListener(actions, items), this);
        Bukkit.getPluginManager().registerEvents(new ShopCreateListener(items), this);
        Bukkit.getPluginManager().registerEvents(new ShopBreakListener(items), this);

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
