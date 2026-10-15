package pt.flawless.fShop.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import pt.flawless.fShop.shops.SignShop;
import pt.flawless.fShop.shops.SignShopImpl;
import pt.flawless.fapi.logs.FConsoleLogger;

public class SignClickListener implements Listener {
    @EventHandler
    public void onSignClickEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        // A SignShop can only be destroyed while sneaking
        if (player.isSneaking()) return;

        Block block = e.getClickedBlock();

        if (block.getState() instanceof Sign) {
            SignShopImpl signShop = new SignShopImpl(new SignShop((Sign) block.getState(), player));

            try {
                if (signShop.isSignShop()) {
                    signShop.insertItemInHand(player.getItemInHand());
                    e.setCancelled(true);
                }
            } catch (Exception exception) {
                FConsoleLogger.sendConsoleMessage(exception.getMessage());
                e.setCancelled(true);
            }
        }
    }
}

