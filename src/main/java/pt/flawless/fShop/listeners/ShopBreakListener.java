package pt.flawless.fShop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import pt.flawless.fShop.shops.PlayerShops;
import pt.flawless.fShop.shops.ShopItems;
import pt.flawless.fShop.shops.ShopKey;

/** Partir a placa apaga o item da loja (só toca na BD se havia item guardado). */
public class ShopBreakListener implements Listener {
    private final ShopItems items;
    private final PlayerShops shops;

    public ShopBreakListener(ShopItems items, PlayerShops shops) {
        this.items = items;
        this.shops = shops;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        ShopKey key = ShopKey.of(e.getBlock());
        items.remove(key);
        shops.remove(key);
    }
}
