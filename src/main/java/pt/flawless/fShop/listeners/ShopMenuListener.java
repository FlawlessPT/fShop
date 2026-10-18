package pt.flawless.fShop.listeners;

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
import pt.flawless.fShop.shops.ShopSides;
import pt.flawless.fapi.sounds.FSound;

/** Cliques e arrastos no menu da loja. Nada pode sair nem entrar no menu. */
public class ShopMenuListener implements Listener {
    private final ShopActions actions;
    private final ShopItems items;
    private final ShopSides sides;

    public ShopMenuListener(ShopActions actions, ShopItems items, ShopSides sides) {
        this.actions = actions;
        this.items = items;
        this.sides = sides;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof ShopMenu menu)) return;
        // Cancela sempre, antes de qualquer outra verificação: cobre shift-click vindo do inventário de baixo,
        // teclas numéricas e duplo clique.
        e.setCancelled(true);

        ShopMenu.Action action = ShopMenu.actionFor(e.getRawSlot());
        if (action == ShopMenu.Action.NONE || !(e.getWhoClicked() instanceof Player player)) return;

        ShopLookup.Resolution resolution = ShopLookup.ready(menu.block(), player, items, sides);
        if (resolution.ready() == null) {
            player.closeInventory();
            FSound.fail(player);
            player.sendMessage(resolution.error() != null ? resolution.error() : "§cEsta loja já não está disponível.");
            return;
        }
        ShopLookup.Ready ready = resolution.ready();
        actions.execute(player, ready.offer(), ready.item(), ready.side(), action == ShopMenu.Action.BUY);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof ShopMenu) {
            e.setCancelled(true);
        }
    }
}
