package pt.flawless.fShop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import pt.flawless.fShop.shops.ShopItems;
import pt.flawless.fShop.shops.ShopKey;

/** Partir a placa apaga o item da loja (só toca na BD se havia item guardado). */
public class ShopBreakListener implements Listener {
    private final ShopItems items;

    public ShopBreakListener(ShopItems items) {
        this.items = items;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        items.remove(ShopKey.of(e.getBlock()));
    }
}
