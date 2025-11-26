package pt.flawless.fShop.listeners;

import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import pt.flawless.fShop.shops.SignShop;

public class ShopCreateListener implements Listener {
    @EventHandler
    public void onShopCreate(SignChangeEvent e) {
        Player p = e.getPlayer();
        String title = e.getLine(0);

        if (title.equalsIgnoreCase("Loja")) {
            SignShop signShop = new SignShop(e, p);

            signShop.createServerShop();
        } else if (title.equalsIgnoreCase(p.getName())) {
            SignShop signShop = new SignShop(e, p);

            signShop.createPlayerShop();
        }
    }
}
