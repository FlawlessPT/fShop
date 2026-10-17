package pt.flawless.fShop.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import pt.flawless.fShop.managers.SignShopManager;
import pt.flawless.fShop.shops.SignShop;
import pt.flawless.fShop.shops.SignShopImpl;
import pt.flawless.fapi.sounds.FSound;

public class ShopCreateListener implements Listener {
    @EventHandler
    public void onShopCreate(SignChangeEvent e) {
        Player player = e.getPlayer();
        String title = e.getLine(0);

        if (SignShopManager.isServerTitle(title)) {
            if (!player.hasPermission("fshop.admin")) {
                e.setCancelled(true);
                e.getBlock().breakNaturally();
                FSound.fail(player);
                player.sendMessage("§cNão tens permissão para criar lojas do servidor.");
                return;
            }

            SignShopImpl signShopImpl = new SignShopImpl(new SignShop(e, player));

            signShopImpl.createServerShop();
        } else if (title.equalsIgnoreCase(player.getName())) {
            SignShopImpl signShopImpl = new SignShopImpl(new SignShop(e, player));

            signShopImpl.createPlayerShop();
        }
    }
}
