package pt.flawless.fShop.listeners;

import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import pt.flawless.fShop.shops.ShopActions;
import pt.flawless.fShop.shops.ShopItems;
import pt.flawless.fShop.shops.ShopLookup;
import pt.flawless.fShop.shops.ShopMenu;
import pt.flawless.fapi.sounds.FSound;

/** Cliques e arrastos no menu da loja. Nada pode sair nem entrar no menu. */
public class ShopMenuListener implements Listener {
    private final ShopActions actions;
    private final ShopItems items;

    public ShopMenuListener(ShopActions actions, ShopItems items) {
        this.actions = actions;
        this.items = items;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof ShopMenu menu)) return;
        // Cancela sempre, antes de qualquer outra verificação: cobre shift-click vindo do inventário de baixo,
        // teclas numéricas e duplo clique.
        e.setCancelled(true);

        ShopMenu.Action action = ShopMenu.actionFor(e.getRawSlot());
        if (action == ShopMenu.Action.NONE || !(e.getWhoClicked() instanceof Player player)) return;

        Optional<ShopLookup.Ready> ready = ShopLookup.ready(menu.block(), player, items);
        if (ready.isEmpty()) {
            player.closeInventory();
            FSound.fail(player);
            player.sendMessage("§cEsta loja já não está disponível.");
            return;
        }
        actions.execute(player, ready.get().offer(), ready.get().item(), action == ShopMenu.Action.BUY);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof ShopMenu) {
            e.setCancelled(true);
        }
    }
}
