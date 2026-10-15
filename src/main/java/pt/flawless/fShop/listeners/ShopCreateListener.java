package pt.flawless.fShop.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import pt.flawless.fShop.shops.SignShop;
import pt.flawless.fShop.shops.SignShopImpl;

public class ShopCreateListener implements Listener {
    @EventHandler
    public void onShopCreate(SignChangeEvent e) {
        Player player = e.getPlayer();
        String title = e.getLine(0);

        if (title.equalsIgnoreCase("Loja")) {
            SignShopImpl signShopImpl = new SignShopImpl(new SignShop(e, player));

            signShopImpl.createServerShop();
        } else if (title.equalsIgnoreCase(player.getName())) {
            SignShopImpl signShopImpl = new SignShopImpl(new SignShop(e, player));

            signShopImpl.createPlayerShop();
        }
    }
}
