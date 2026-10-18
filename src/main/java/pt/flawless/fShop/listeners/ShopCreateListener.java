package pt.flawless.fShop.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import pt.flawless.fShop.managers.SignShopManager;
import pt.flawless.fShop.shops.ChestLocator;
import pt.flawless.fShop.shops.PlayerShops;
import pt.flawless.fShop.shops.ShopItems;
import pt.flawless.fShop.shops.ShopKey;
import pt.flawless.fShop.shops.ShopOwner;
import pt.flawless.fShop.shops.SignShop;
import pt.flawless.fShop.shops.SignShopImpl;
import pt.flawless.fapi.messages.Messages;
import pt.flawless.fapi.sounds.FSound;

public class ShopCreateListener implements Listener {
    private final ShopItems items;
    private final PlayerShops shops;

    public ShopCreateListener(ShopItems items, PlayerShops shops) {
        this.items = items;
        this.shops = shops;
    }

    @EventHandler(ignoreCancelled = true)
    public void onShopCreate(SignChangeEvent e) {
        ShopKey key = ShopKey.of(e.getBlock());
        items.remove(key);
        shops.remove(key);
        Player player = e.getPlayer();
        String title = e.getLine(0);

        if (SignShopManager.isServerTitle(title)) {
            if (!player.hasPermission("fshop.admin")) {
                e.setCancelled(true);
                e.getBlock().breakNaturally();
                FSound.fail(player);
                Messages.noPermission(player);
                return;
            }

            new SignShopImpl(new SignShop(e, player)).createServerShop();
        } else if (title.equalsIgnoreCase(player.getName())) {
            if (ChestLocator.chestFor(e.getBlock()).isEmpty()) {
                e.setCancelled(true);
                e.getBlock().breakNaturally();
                FSound.fail(player);
                player.sendMessage("§cA loja de jogador tem de estar presa a um baú.");
                return;
            }

            if (new SignShopImpl(new SignShop(e, player)).createPlayerShop()) {
                shops.put(key, new ShopOwner(player.getUniqueId(), player.getName()));
            }
        }
    }
}
